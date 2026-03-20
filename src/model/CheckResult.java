package model;

import java.util.Collections;
import java.util.List;

public class CheckResult {
    private final String selectId;
    private final List<String> tableNames;
    private final List<String> langTableNames;
    private final List<String> unusedLangTableNames; // 테이블은 사용했지만 다국어는 미사용
    private final List<String> usedColumns;
    private final List<String> unusedColumns;

    public CheckResult(String selectId,
                       List<String> tableNames,
                       List<String> langTableNames,
                       List<String> unusedLangTableNames,
                       List<String> usedColumns,
                       List<String> unusedColumns) {
        this.selectId             = selectId;
        this.tableNames           = tableNames;
        this.langTableNames       = langTableNames;
        this.unusedLangTableNames = unusedLangTableNames;
        this.usedColumns          = usedColumns;
        this.unusedColumns        = unusedColumns;
    }

    public String getSelectId()                   { return selectId; }
    public List<String> getTableNames()           { return tableNames; }
    public List<String> getLangTableNames()       { return langTableNames; }
    public List<String> getUnusedLangTableNames() { return unusedLangTableNames; }
    public List<String> getUsedColumns()          { return usedColumns; }
    public List<String> getUnusedColumns()        { return unusedColumns; }

    public String toBoxString(int index) {
        String divider = "  " + repeatStr("─", 50);

        return "  [select #" + index + "] " + selectId + "\n" +
               divider + "\n" +
               "  사용 테이블          : " + (tableNames.isEmpty()           ? "없음" : tableNames)           + "\n" +
               "  사용 다국어 테이블   : " + (langTableNames.isEmpty()       ? "없음" : langTableNames)       + "\n" +
               "  사용된 컬럼          : " + (usedColumns.isEmpty()          ? "없음" : usedColumns)          + "\n" +
               "  미사용 다국어 테이블 : " + (unusedLangTableNames.isEmpty() ? "없음" : unusedLangTableNames) + "\n" +
               "  미사용 컬럼          : " + (unusedColumns.isEmpty()        ? "없음" : unusedColumns)        + "\n" +
               divider;
    }

    private String repeatStr(String str, int count) {
        return String.join("", Collections.nCopies(count, str));
    }
}