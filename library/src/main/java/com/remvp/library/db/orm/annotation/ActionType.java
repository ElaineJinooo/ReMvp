package com.remvp.library.db.orm.annotation;

/**
 * 名称：ActionType.java
 * 描述：关联关系操作类型
 */
public class ActionType {
    public static final String query = "queryRaw";
    public static final String insert = "insert";
    public static final String update = "execSql";
    public static final String delete = "imDeleteLines";
    public static final String query_insert = "query_insert";
}
