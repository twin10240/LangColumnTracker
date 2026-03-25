package checker;

import model.CheckResult;
import model.SelectResult;
import schema.TableSchema;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlTableChecker {

    public List<CheckResult> check(List<SelectResult> selectResults) {
        List<CheckResult> checkResults = new ArrayList<>();

        for (SelectResult selectResult : selectResults) {
            String sql            = selectResult.getSql().toUpperCase();
            // i18nExcluded 주석이 있으면 다국어 체크 스킵
            if (isI18nExcluded(sql)) {
                continue;
            }

            String sqlWithoutWith = removeWithClause(sql);
            String selectColumns  = extractSelectColumns(sqlWithoutWith);

             // WITH절 사용 여부 체크
            boolean hasWithClause = sql.trim().startsWith("WITH");  // ← 추가

            List<String> tableNames           = new ArrayList<>();
            List<String> langTableNames       = new ArrayList<>();
            List<String> unusedLangTableNames = new ArrayList<>(); // 테이블은 사용했지만 다국어는 미사용
            List<String> usedColumns          = new ArrayList<>();
            List<String> unusedColumns        = new ArrayList<>();

            for (TableSchema schema : TableSchema.values()) {
                String tableName     = schema.getTableName().toUpperCase();
                String langTableName = schema.getLangTableName().toUpperCase();

                // 1. 테이블 사용 여부 - 사용 안하면 스킵
                boolean tableUsed = containsTable(sql, tableName);
                if (!tableUsed) continue;

                tableNames.add(tableName);

                // 2. 다국어 테이블 사용 여부
                boolean langTableUsed = containsTable(sql, langTableName);

                if (langTableUsed) {
                    langTableNames.add(langTableName);

                    // alias 여러 개 추출
                    List<String> aliases = extractAliases(sql, langTableName);

                    for (String column : schema.getColumns()) {
                        boolean isUsed = false;

                        // 모든 alias에 대해 체크
                        for (String alias : aliases) {
                            String target = alias + "." + column.toUpperCase();
                            if (selectColumns.contains(target)) {
                                usedColumns.add(target);
                                isUsed = true;
                            }
                        }

                        // alias가 없거나 모든 alias에서 미사용이면 미사용 컬럼으로
                        if (!isUsed) {
                            unusedColumns.add(column);
                        }
                    }
                } else {
                    // 테이블은 사용했지만 다국어 테이블은 미사용
                    unusedLangTableNames.add(langTableName);

                    // 다국어 테이블 미사용 시 해당 테이블의 모든 컬럼을 미사용으로 추가 ← 추가
                    for (String column : schema.getColumns()) {
                        unusedColumns.add(column);
                    }
                }
            }

            if (tableNames.isEmpty()) continue;

            checkResults.add(new CheckResult(
                selectResult.getId(),
                hasWithClause,
                tableNames,
                langTableNames,
                unusedLangTableNames,
                usedColumns,
                unusedColumns
            ));
        }

        return checkResults;
    }

    // WITH절 제거
    private String removeWithClause(String sql) {
        String trimmed = sql.trim();
        if (!trimmed.toUpperCase().startsWith("WITH")) return trimmed;

        int depth = 0;
        int lastCloseIndex = -1;

        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if      (c == '(') depth++;
            else if (c == ')') depth--;

            if (depth == 0 && lastCloseIndex != -1) {
                String remaining = trimmed.substring(lastCloseIndex + 1).trim();
                if (remaining.toUpperCase().startsWith("SELECT")) return remaining;
            }

            if (c == ')') lastCloseIndex = i;
        }

        return trimmed;
    }

    /**
     * 1단계: WITH절 제거된 SQL에서 SELECT ~ FROM 사이만 추출 (depth 0 기준)
     * 2단계: 추출된 컬럼 영역에서 스칼라 서브쿼리만 제거
     */
    private String extractSelectColumns(String sql) {
        String trimmed = sql.trim();

        // 1단계: depth 0 기준으로 SELECT ~ 메인 FROM 사이 컬럼 영역 추출
        String columnArea = extractColumnArea(trimmed);

        // 2단계: 컬럼 영역이 * 또는 별칭.* 만 있으면 → FROM 절 인라인 뷰로 진입
        if (isWildCardOnly(columnArea)) {
            String innerSql = extractInlineViewSql(trimmed);
            if (innerSql != null) {
                return extractSelectColumns(innerSql); // 내부 SQL로 재귀 진입
            }
        }

        // 3단계: 컬럼 영역 안의 스칼라 서브쿼리만 제거
        return removeScalarSubQueries(columnArea);
    }

    /**
     * 컬럼 영역이 와일드카드(*) 또는 별칭.* 만 있는지 체크
     * 예: "T.*" → true
     *     "*"   → true
     *     "A.USER_NM, B.USER_NM" → false
     */
    private boolean isWildCardOnly(String columnArea) {
        String trimmed = columnArea.trim();
        // * 또는 별칭.* 패턴만 있는지 체크
        return trimmed.matches("(?i)\\*|\\w+\\.\\*");
    }

    /**
     * depth 0 기준으로 SELECT ~ 메인 FROM 사이 컬럼 영역만 추출
     * FROM 절 이후는 완전히 무시
     */
    private String extractColumnArea(String sql) {
        String upper = sql.toUpperCase();
        int selectIndex = upper.indexOf("SELECT");
        if (selectIndex == -1) return "";

        int i = selectIndex + "SELECT".length();
        StringBuilder columns = new StringBuilder();
        int depth = 0;

        while (i < sql.length()) {
            char c = sql.charAt(i);

            if (c == '(') {
                depth++;
                columns.append(c);
            } else if (c == ')') {
                depth--;
                columns.append(c);
            } else if (depth == 0) {
                // depth 0 일 때만 메인 FROM 체크
                if (upper.startsWith("FROM", i) && isBoundary(sql, i - 1) && isBoundary(sql, i + 4)) {
                    break; // 메인 FROM 만나면 종료
                }
                columns.append(c);
            } else {
                columns.append(c);
            }

            i++;
        }

        return columns.toString().trim();
    }

    /**
     * 단어 경계 체크
     */
    private boolean isBoundary(String sql, int index) {
        if (index < 0 || index >= sql.length()) return true;
        char c = sql.charAt(index);
        return Character.isWhitespace(c) || c == ',' || c == '(' || c == ')';
    }

    /**
     * SELECT T.* FROM( SELECT~ 패턴 감지
     * 즉, FROM 바로 뒤에 괄호+SELECT가 오는 인라인 뷰 패턴
     */
    private boolean isInlineViewPattern(String sql) {
        String regex = "SELECT\\s+.*?\\s+FROM\\s*\\(\\s*SELECT";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        return pattern.matcher(sql).find();
    }

    /**
     * FROM ( SELECT ~ ) 안의 SQL 추출
     */
    private String extractInlineViewSql(String sql) {
        String upperSql = sql.toUpperCase();
        int fromIndex = upperSql.indexOf("FROM");
        if (fromIndex == -1) return null;

        int openIndex = sql.indexOf('(', fromIndex);
        if (openIndex == -1) return null;

        int depth = 1;
        int i = openIndex + 1;

        while (i < sql.length() && depth > 0) {
            char c = sql.charAt(i);
            if      (c == '(') depth++;
            else if (c == ')') depth--;
            i++;
        }

        return sql.substring(openIndex + 1, i - 1).trim();
    }

    /**
     * 컬럼 영역 안의 스칼라 서브쿼리만 제거
     */
    private String removeScalarSubQueries(String columnArea) {
        StringBuilder result = new StringBuilder();
        int i = 0;

        while (i < columnArea.length()) {
            char c = columnArea.charAt(i);

            if (c == '(') {
                int depth = 1;
                int start = i + 1;
                int j = i + 1;

                while (j < columnArea.length() && depth > 0) {
                    if      (columnArea.charAt(j) == '(') depth++;
                    else if (columnArea.charAt(j) == ')') depth--;
                    j++;
                }

                String inner = columnArea.substring(start, j - 1).trim();

                if (inner.toUpperCase().startsWith("SELECT")) {
                    i = j; // 스칼라 서브쿼리 제거
                } else {
                    result.append(columnArea, i, j); // SQL 함수 유지
                    i = j;
                }
            } else {
                result.append(c);
                i++;
            }
        }

        return result.toString().trim();
    }

    // alias 여러 개 추출
    private List<String> extractAliases(String sql, String langTableName) {
        List<String> aliases = new ArrayList<>();
        String regex = "JOIN\\s+" + langTableName + "\\s+(?:AS\\s+)?(\\w+)\\s+ON";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);

        while (matcher.find()) {
            aliases.add(matcher.group(1).toUpperCase()); // 매칭될 때마다 누적
        }

        return aliases;
    }

    // 단어 단위 테이블명 체크
    private boolean containsTable(String sql, String tableName) {
        String regex = "(?<![\\w])(" + tableName + ")(?![\\w])";
        return sql.matches("(?s).*" + regex + ".*");
    }

    /**
     * / *i18nExcluded* /
     * 주석 포함 여부 체크
     */
    private boolean isI18nExcluded(String sql) {
        return sql.contains("/*I18NEXCLUDED*/");
    }
}