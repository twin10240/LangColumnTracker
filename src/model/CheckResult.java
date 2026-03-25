package model;

import java.util.Collections;
import java.util.List;

public class CheckResult {
    private final String selectId;
    private final boolean hasWithClause;
    private final List<String> tableNames;
    private final List<String> langTableNames;
    private final List<String> unusedLangTableNames; // 테이블은 사용했지만 다국어는 미사용
    private final List<String> usedColumns;
    private final List<String> unusedColumns;

    public CheckResult(String selectId,
                       boolean hasWithClause,
                       List<String> tableNames,
                       List<String> langTableNames,
                       List<String> unusedLangTableNames,
                       List<String> usedColumns,
                       List<String> unusedColumns) {
        this.selectId             = selectId;
        this.hasWithClause = hasWithClause;
        this.tableNames           = tableNames;
        this.langTableNames       = langTableNames;
        this.unusedLangTableNames = unusedLangTableNames;
        this.usedColumns          = usedColumns;
        this.unusedColumns        = unusedColumns;
    }

    public String getSelectId()                   { return selectId; }
    public boolean isHasWithClause()              { return hasWithClause; }
    public List<String> getTableNames()           { return tableNames; }
    public List<String> getLangTableNames()       { return langTableNames; }
    public List<String> getUnusedLangTableNames() { return unusedLangTableNames; }
    public List<String> getUsedColumns()          { return usedColumns; }
    public List<String> getUnusedColumns()        { return unusedColumns; }

    public String toBoxString(int index) {
        String divider = "  " + repeatStr("─", 50);

        StringBuilder sb = new StringBuilder();
        sb.append(divider).append("\n");
        sb.append("  [select #").append(index).append("] ").append(selectId).append("\n");
        sb.append(divider).append("\n");
        sb.append("  WITH절 사용          : ").append(hasWithClause ? "사용" : "미사용").append("\n");
        sb.append("  사용 테이블          : ").append(tableNames.isEmpty()           ? "없음" : tableNames).append("\n");
        sb.append("  사용 다국어 테이블   : ").append(langTableNames.isEmpty()       ? "없음" : langTableNames).append("\n");
        sb.append("  미사용 다국어 테이블 : ").append(unusedLangTableNames.isEmpty() ? "없음" : unusedLangTableNames).append("\n");
        sb.append("  사용된 컬럼          : ").append(usedColumns.isEmpty()          ? "없음" : usedColumns).append("\n");
        sb.append("  미사용 컬럼          : ").append(unusedColumns.isEmpty() ? "없음" : unusedColumns).append("\n");

        sb.append(divider);
        return sb.toString();
    }

    private String repeatStr(String str, int count) {
        return String.join("", Collections.nCopies(count, str));
    }
}