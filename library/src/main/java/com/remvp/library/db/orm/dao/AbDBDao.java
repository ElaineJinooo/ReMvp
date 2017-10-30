/*
 * Copyright (C) 2013 www.418log.org
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.remvp.library.db.orm.dao;

import android.database.sqlite.SQLiteOpenHelper;

import java.util.List;
import java.util.Map;

/**
 * The Interface AbDBDao.
 */
public interface AbDBDao<T> {

    /**
     * 获取数据库.
     *
     * @return the db helper
     */
    SQLiteOpenHelper getmSQLiteOpenHelper();

    /**
     * 插入实体类,默认主键自增,调用insert(T,true);.
     *
     * @param entity 映射实体
     * @return 插入成功的数据ID
     */
    long insert(T entity);

    /**
     * 插入实体类.
     *
     * @param entity 映射实体
     * @param flag   flag为true是自动生成主键,flag为false时需手工指定主键的值.
     * @return 插入成功的数据行号
     */
    long insert(T entity, boolean flag);

    /**
     * 插入实体类列表，默认主键自增,调用insertList(List<T>,true);.
     *
     * @param entityList 映射实体列表
     * @return 插入成功的数据行号的和
     */
    long insertList(List<T> entityList);

    /**
     * 插入实体类列表.
     *
     * @param entityList 映射实体列表
     * @param flag       flag为true是自动生成主键,flag为false时需手工指定主键的值
     * @return 插入成功的数据行号的和
     */
    long insertList(List<T> entityList, boolean flag);


    /**
     * 根据ID删除数据.
     *
     * @param id 数据ID主键
     */
    long deleteLines(String id);

    /**
     * 数据IDDao主键.
     *
     * @param idDaos 数据IDDao主键
     */
    long deleteIds(String... idDaos);

    /**
     * 根据ID删除数据.
     *
     * @param id 数据ID主键
     */
    long deleteLines(int id);

    /**
     * 根据ID删除数据.
     */
    List<T> deleteIdList(List<T> ids);

    /**
     * 根据ID删除数据（多个）.
     *
     * @param ids 数据ID主键
     */
    long deleteLines(Integer... ids);

    /**
     * 根据where删除数据.
     *
     * @param whereClause where语句
     * @param whereArgs   where参数
     */
    long deleteLines(String whereClause, String[] whereArgs);

    /**
     * 删除所有数据.
     */
    long deleteAll();

    /**
     * 根据主键删除单条数据.
     */
    long deleteOne(T entity);

    /**
     * 根据某一个列删除单条数据.
     */
    long deleteOneByColumn(String column, T entity);

    /**
     * 更新数据.
     *
     * @param entity 数据,ID主键
     * @return 修改成功的数据行号
     */
    long update(T entity);

    /**
     * 更新数据.
     *
     * @param entity 数据,ID主键
     * @return 修改成功的数据行号
     */
    long updateByColumn(String column, T entity);

    /**
     * 更新数据.
     *
     * @param entity 数据,ID主键
     * @return 修改成功的数据行号
     */
    long imUpdateId(T entity);


    /**
     * 更新数据.
     *
     * @param entityList 数据列表,ID主键
     * @return 修改成功的数据行号和
     */
    long updateList(List<T> entityList);
//
//    /**
//     * 更新数据.
//     *
//     * @param entityList 数据列表,ID主键
//     * @return 修改成功的数据行号和
//     */
//    long updateListIdDao(List<T> entityList);

    /**
     * 根据获取一条数据.
     *
     * @param id 数据ID主键
     * @return 一条数据映射实体
     */
    T queryOne(int id);

    /**
     * 根据获取一条数据.
     *
     * @param id 数据ID主键
     * @return 一条数据映射实体
     */
    T queryOne(String id);

    /**
     * 根据获取一条数据.
     *
     * @param column 数据某一列
     * @param data   数据某一列
     * @return 一条数据映射实体
     */
    T queryOne(String column, String data);

    /**
     * 执行查询语句.
     *
     * @param sql           sql语句
     * @param selectionArgs 绑定变量的参数值
     * @param clazz         返回的对象类型
     * @return 映射实体列表
     */
    List<T> rawQuery(String sql, String[] selectionArgs, Class<T> clazz);

    /**
     * 查询列表.
     *
     * @return 映射实体列表
     */
    List<T> queryList();

    /**
     * 查询列表.
     *
     * @return 映射实体列表
     */
    List<T> queryList(int page, int pageSize);

    /**
     * 映射实体列表.
     *
     * @param columns       查询的列
     * @param selection     where语句的sql
     * @param selectionArgs where语句的sql的绑定变量的参数
     * @param groupBy       分组语句
     * @param having        分组后的过滤语句
     * @param orderBy       排序
     * @param limit         limit语句
     * @return 映射实体列表
     */
    List<T> queryList(String[] columns, String selection,
                      String[] selectionArgs, String groupBy, String having,
                      String orderBy, String limit);

    /**
     * 映射实体列表.
     *
     * @param selection     where语句的sql
     * @param selectionArgs where语句的sql的绑定变量的参数
     * @return 映射实体列表
     */
    List<T> queryList(String selection, String[] selectionArgs);

    /**
     * 检查是否存在数据.
     *
     * @param sql           sql语句
     * @param selectionArgs 绑定变量的参数值
     * @return 如果存在返回true, 不存在为false
     */
    boolean isExist(String sql, String[] selectionArgs);

    /**
     * 将查询的结果保存为名值对map.
     *
     * @param sql           查询sql
     * @param selectionArgs 绑定变量的参数值
     * @return 返回的Map中的key全部是小写形式.
     */
    List<Map<String, String>> queryMapList(String sql, String[] selectionArgs);

    /**
     * 返回一个查询的结果条数.
     *
     * @param sql           查询sql
     * @param selectionArgs 绑定变量的参数值
     * @return 总条数.
     */
    int queryCount(String sql, String[] selectionArgs);

    /**
     * 返回一个查询的结果条数.
     *
     * @return 总条数.
     */
    int queryCount();

    /**
     * 封装执行sql代码.
     *
     * @param sql           sql语句
     * @param selectionArgs 绑定变量的参数值
     */
    void execSql(String sql, Object[] selectionArgs);

}
