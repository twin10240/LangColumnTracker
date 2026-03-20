package schema;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public enum TableSchemaExam {
    /*
    * 1. XML 파싱 -> SQL
    * 2. 해당 테이블이 있는지
    * 3. 사용되였다면 LANG 테이블 존재 체크
    * 4. 만약 있다면 컬럼 체크
    * */

    TABLE_NM("TABLE_NM", "LANG_TABLE_NM", new String[]{"LANG_NM"});

    private final String tableName;
    private final String langTableName;
    private final Set<String> columns;

    // 생성자에서 String 배열을 받아 Set으로 변환
    TableSchemaExam(String tableName, String langTableName, String[] columns) {
        this.tableName = tableName;
        this.langTableName = langTableName;
        // Arrays.asList를 거쳐 HashSet으로 변환하여 불변 Set으로 만듦
        this.columns = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(columns)));
    }

    public String getTableName() {
        return tableName;
    }

    public String getLangTableName() {
        return langTableName;
    }

    public Set<String> getColumns() {
        return columns;
    }

    /**
     * 테이블명으로 Enum 상수를 찾는 역방향 조회 (Java 8 Stream API 사용)
     */
    public static TableSchemaExam fromString(String text) {
        return Arrays.stream(TableSchemaExam.values())
                .filter(t -> t.tableName.equalsIgnoreCase(text))
                .findFirst()
                .orElse(null);
    }
}
