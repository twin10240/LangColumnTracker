package model;

public class SelectResult {
    private final String id;             // select id 속성
    private final String parameterType;  // parameterType 속성
    private final String resultType;     // resultType 속성
    private final String sql;            // 실제 SQL 쿼리

    public SelectResult(String id, String parameterType, String resultType, String sql) {
        this.id = id;
        this.parameterType = parameterType;
        this.resultType = resultType;
        this.sql = sql;
    }

    public String getId() { return id; }
    public String getParameterType() { return parameterType; }
    public String getResultType() { return resultType; }
    public String getSql() { return sql; }

    @Override
    public String toString() {
        return "\n  ID            : " + id +
               "\n  parameterType : " + parameterType +
               "\n  resultType    : " + resultType +
               "\n  SQL           : " + sql.trim();
    }
}