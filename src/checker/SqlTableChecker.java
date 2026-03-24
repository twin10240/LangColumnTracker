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
            String sqlWithoutWith = removeWithClause(sql);
            String selectColumns  = extractSelectColumns(sqlWithoutWith);

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
     * 메인 SELECT 컬럼 추출
     * - FROM 절 인라인 뷰: 내부로 진입해서 추출
     * - SELECT 컬럼 자리 스칼라 서브쿼리: 제거
     */
    private String extractSelectColumns(String sql) {
        String trimmed = sql.trim();

        // FROM 바로 뒤에 서브쿼리가 오는 인라인 뷰 케이스
        // 예: SELECT T.* FROM (SELECT ...)T
        if (isInlineViewPattern(trimmed)) {
            String innerSql = extractInlineViewSql(trimmed);
            if (innerSql != null) {
                return extractSelectColumns(innerSql); // 내부 SQL로 재귀 진입
            }
        }

        // 일반 케이스: 스칼라 서브쿼리 제거 후 SELECT ~ FROM 추출
        String removedScalar = removeScalarSubQueries(trimmed);
        String regex = "SELECT\\s+(.*?)\\s+FROM\\s";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(removedScalar);

        return matcher.find() ? matcher.group(1).trim() : "";
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
        // FROM 이후 첫 번째 괄호 위치 찾기
        String upperSql = sql.toUpperCase();
        int fromIndex = upperSql.indexOf("FROM");
        if (fromIndex == -1) return null;

        int openIndex = sql.indexOf('(', fromIndex);
        if (openIndex == -1) return null;

        // 괄호 depth 추적으로 인라인 뷰 전체 추출
        int depth = 1;
        int i = openIndex + 1;

        while (i < sql.length() && depth > 0) {
            char c = sql.charAt(i);
            if      (c == '(') depth++;
            else if (c == ')') depth--;
            i++;
        }

        // 괄호 안의 SQL 반환
        return sql.substring(openIndex + 1, i - 1).trim();
    }

    /**
     * SELECT 컬럼 자리의 스칼라 서브쿼리만 제거
     * 판단 기준: 괄호 닫힌 후 AS 컬럼명 또는 , 또는 FROM 이 오는 경우
     * 예: (SELECT CODE_NM FROM ...) AS CODE_NM  → 제거
     *     COALESCE(B.USER_NM, A.USER_NM)        → 유지
     */
    private String removeScalarSubQueries(String sql) {
        StringBuilder result = new StringBuilder();
        int i = 0;

        while (i < sql.length()) {
            char c = sql.charAt(i);

            if (c == '(') {
                // 괄호 안 내용 추출
                int depth = 1;
                int start = i + 1;
                int j = i + 1;

                while (j < sql.length() && depth > 0) {
                    if      (sql.charAt(j) == '(') depth++;
                    else if (sql.charAt(j) == ')') depth--;
                    j++;
                }

                String inner = sql.substring(start, j - 1).trim();

                // 괄호 안이 SELECT로 시작하면 스칼라 서브쿼리 → 제거
                if (inner.toUpperCase().startsWith("SELECT")) {
                    i = j; // 괄호 전체 스킵
                } else {
                    // SQL 함수 괄호 → 유지
                    result.append(sql, i, j);
                    i = j;
                }
            } else {
                result.append(c);
                i++;
            }
        }

        return result.toString();
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
}