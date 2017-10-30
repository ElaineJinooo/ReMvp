package com.remvp.library.db.orm;


import com.remvp.library.db.orm.annotation.Table;

public class SqlColumn<T> {
    Class<T> mClass;
    String tableName;
    StringBuilder mSql = new StringBuilder();

    public SqlColumn(Class<T> aClass) {
        this.mClass = aClass;
        tableName = getTableName(aClass);
    }

    public SqlColumn select() {
        mSql.append("select * ");
        return this;
    }

    public SqlColumn select(String selectContent) {
        mSql.append("select ")
                .append(selectContent).append(" ");
        return this;
    }

    public SqlColumn fromTable(String tableName) {
        mSql.append("from ")
                .append(tableName).append(" ");
        return this;
    }

    public SqlColumn fromTable() {
        mSql.append("from ")
                .append(tableName).append(" ");
        return this;
    }

    public SqlColumn where(String colum) {
        mSql.append("where ").append(colum).append("= ");
        return this;
    }

    public SqlColumn values(String values) {
        mSql.append(values).append(" ");
        return this;
    }

    public SqlColumn and(String colum) {
        mSql.append("and ").append(colum).append("= ");
        return this;
    }

    public SqlColumn groupBy(String colum) {
        mSql.append("group by ").append(colum).append(" ");
        return this;
    }

    public SqlColumn orderBy(String colum) {
        mSql.append("order by ").append(colum).append(" ");
        return this;
    }

    public SqlColumn limit(String content) {
        mSql.append("limit ").append(content);
        return this;
    }

    private String getTableName(Class<?> daoClasses) {
        String tableName = "";
        if (daoClasses.isAnnotationPresent(Table.class)) {
            Table table = daoClasses.getAnnotation(Table.class);
            tableName = table.name();
        }
        return tableName;
    }

    public String getSql() {
        return mSql.toString();
    }
    public Class<T> getClazz() {
        return mClass;
    }
}
