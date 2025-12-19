package com.mwu.aitok.model.common.handler;


/**
 * List<Long> 的类型转换器实现类，对应数据库的 varchar 类型
// */
//@MappedJdbcTypes(JdbcType.VARCHAR)
//@MappedTypes(List.class)
public class LongListTypeHandler {

//    private static final String COMMA = ",";
//
//    @Override
//    public void setParameter(PreparedStatement ps, int i, List<Long> strings, JdbcType jdbcType) throws SQLException {
//        // 设置占位符
//        ps.setString(i, CollUtil.join(strings, COMMA));
//    }
//
//    @Override
//    public List<Long> getResult(ResultSet rs, String columnName) throws SQLException {
//        String value = rs.getString(columnName);
//        return getResult(value);
//    }
//
//    @Override
//    public List<Long> getResult(ResultSet rs, int columnIndex) throws SQLException {
//        String value = rs.getString(columnIndex);
//        return getResult(value);
//    }
//
//    @Override
//    public List<Long> getResult(CallableStatement cs, int columnIndex) throws SQLException {
//        String value = cs.getString(columnIndex);
//        return getResult(value);
//    }
//
//    private List<Long> getResult(String value) {
//        if (value == null) {
//            return null;
//        }
//        return splitToLong(value, COMMA);
//    }
//
//    public static List<Long> splitToLong(String value, CharSequence separator) {
//        long[] longs = StrUtil.splitToLong(value, separator);
//        return Arrays.stream(longs).boxed().collect(Collectors.toList());
//    }
}
