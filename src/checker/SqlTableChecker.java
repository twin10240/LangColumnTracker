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

    // SELECT ~ FROM 사이 조회 컬럼 추출
    private String extractSelectColumns(String sql) {
        String regex = "SELECT\\s+(.*?)\\s+FROM\\s";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(sql);
        return matcher.find() ? matcher.group(1).trim() : "";
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