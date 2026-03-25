package checker;

import model.CheckResult;
import model.SelectResult;
import schema.TableSchema;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
* ### 동작 흐름
```
1단계 테이블 루프 완료 후 usedColumns:
  [PU_P00220_LANG.SYSDEF_NM, PU_P00230_LANG.SYSDEF_NM, PU_P00030_LANG.SYSDEF_NM, MIIL.ITEM_GRP_NM]

2단계 의심 컬럼 체크:
  CI_CODEDTL → SYSDEF_NM 체크
    → alreadyUsed: PU_P00220_LANG.SYSDEF_NM이 .SYSDEF_NM으로 끝남 → true → 스킵 ✅

  CI_ITEM → ITEM_NM 체크
    → alreadyUsed: false → selectColumns에서 발견 → 의심 컬럼 추가 ✅
```
* */

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
            boolean hasWithClause = sql.trim().startsWith("WITH"); // WITH절 사용 여부 체크

            List<String> tableNames           = new ArrayList<>();
            List<String> langTableNames       = new ArrayList<>();
            List<String> unusedLangTableNames = new ArrayList<>(); // 테이블은 사용했지만 다국어는 미사용
            List<String> usedColumns          = new ArrayList<>();
            List<String> unusedColumns        = new ArrayList<>();
            List<String> suspiciousColumns    = new ArrayList<>();

            // 1단계: 테이블 기준 체크 먼저 완료
            for (TableSchema schema : TableSchema.values()) {
                String tableName     = schema.getTableName().toUpperCase();
                String langTableName = schema.getLangTableName().toUpperCase();

                boolean tableUsed     = containsTable(sql, tableName);
                boolean langTableUsed = containsTable(sql, langTableName);

                if (tableUsed) {
                    tableNames.add(tableName);

                    if (langTableUsed) {
                        langTableNames.add(langTableName);

                        List<String> aliases = extractAliases(sql, langTableName);
                        for (String column : schema.getColumns()) {
                            boolean isUsed = false;
                            for (String alias : aliases) {
                                String target = alias + "." + column.toUpperCase();
                                if (selectColumns.contains(target)) {
                                    usedColumns.add(target);
                                    isUsed = true;
                                }
                            }
                            if (!isUsed) unusedColumns.add(column);
                        }
                    } else {
                        unusedLangTableNames.add(langTableName);
                        for (String column : schema.getColumns()) {
                            unusedColumns.add(column);
                        }
                    }
                }
            }

            // 2단계: 모든 테이블 루프 끝난 후 의심 컬럼 체크 ← 핵심 수정
            for (TableSchema schema : TableSchema.values()) {
                String tableName = schema.getTableName().toUpperCase();
                String langTableName = schema.getLangTableName().toUpperCase();

                // 이미 사용된 테이블은 스킵
                if (tableNames.contains(tableName)) continue;

                for (String column : schema.getColumns()) {
                    // 이미 usedColumns에 있는 컬럼은 스킵 (모든 테이블 체크 끝난 후라 완전한 상태)
                    boolean alreadyUsed = usedColumns.stream()
                            .anyMatch(used -> used.toUpperCase().endsWith("." + column.toUpperCase()));
                    if (alreadyUsed) continue;

                    if (containsColumn(selectColumns, column)) {
                        suspiciousColumns.add(column + " (원본테이블: " + tableName + ", 다국어테이블: " + langTableName + ")");
                    }
                }
            }

            if (tableNames.isEmpty() && suspiciousColumns.isEmpty()) continue;

            checkResults.add(new CheckResult(
                selectResult.getId(),
                hasWithClause,
                tableNames,
                langTableNames,
                unusedLangTableNames,
                usedColumns,
                unusedColumns,
                suspiciousColumns
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

    /**
     * 조회 컬럼 영역에서 컬럼명 단어 단위 체크
     * 예: USER_NM이 A.USER_NM, B.USER_NM 등으로 사용되는지 확인
     */
    private boolean containsColumn(String selectColumns, String column) {
        String regex = "(?<![\\w])" + column + "(?![\\w])";
        return selectColumns.matches("(?s).*" + regex + ".*");
    }
}