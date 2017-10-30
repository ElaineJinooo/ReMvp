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

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.List;
import java.util.Map;

/**
 * The Class AbDBDao.
 */
public abstract class AbBasicDBDao<T> {

    /**
     * 获取数据库.
     *
     * @return the db helper
     */
    protected abstract SQLiteOpenHelper getSQLiteOpenHelper();

    /**
     * 插入实体类,默认主键自增,调用insert(T,true);.
     *
     * @param entity 映射实体
     * @return 插入成功的数据ID
     */
    protected abstract long insertAbs(T entity);

    /**
     * 插入实体类.
     *
     * @param entity 映射实体
     * @param flag   flag为true是自动生成主键,flag为false时需手工指定主键的值.
     * @return 插入成功的数据行号
     */
    protected abstract long insertAbs(T entity, boolean flag);

    /**
     * 插入实体类列表，默认主键自增,调用insertList(List<T>,true);.
     *
     * @param entityList 映射实体列表
     * @return 插入成功的数据行号的和
     */
    protected abstract long insertListAbs(List<T> entityList);

    /**
     * 插入实体类列表.
     *
     * @param entityList 映射实体列表
     * @param flag       flag为true是自动生成主键,flag为false时需手工指定主键的值
     * @return 插入成功的数据行号的和
     */
    protected abstract long insertListAbs(List<T> entityList, boolean flag);


    /**
     * 根据ID删除数据.
     *
     * @param id 数据ID主键,该实体类必须制定ID
     */
    protected abstract long deleteAbs(String id);

    /**
     * 数据ID主键.
     *
     * @param ids 根据指定的ID来删除数据,该实体类必须制定ID
     */
    protected abstract long deleteAbs(String[] ids);

    /**
     * 根据ID删除数据.
     *
     * @param id 数据ID主键,该实体类必须制定ID
     */
    protected abstract long deleteAbs(int id);

    /**
     * 根据ID删除数据（多个）.
     *
     * @param ids 数据ID主键,该实体类必须制定ID
     */
    protected abstract long deleteAbs(int[] ids);

    /**
     * @param ids 根据ID删除数据.,该实体类必须制定ID
     * @return
     */
    protected abstract List<T> deleteListAbs(List<T> ids);


    /**
     * 根据where删除数据.
     *
     * @param whereClause where语句
     * @param whereArgs   where参数
     */
    protected abstract long deleteAbs(String whereClause, String[] whereArgs);

    /**
     * 删除所有数据.
     */
    protected abstract long deleteAllAbs();

    /**
     * @param entity 根据主键删除单条数据.,该实体类必须制定ID
     * @return
     */
    protected abstract long deleteOneAbs(T entity);

    /**
     * @param column 根据某一个列删除单条数据.
     * @param entity
     * @return
     */
    protected abstract long deleteOneByColumnAbs(String column, T entity);

    /**
     * 更新数据.
     *
     * @param entity 表中必须指定ID主键
     * @return 修改成功的数据行号
     */
    protected abstract long updateAbs(T entity);

    /**
     * 更新数据.
     *
     * @param entity 根据某一列来修改表中的数据
     * @return 修改成功的数据行号
     */
    protected abstract long updateByColumnAbs(String column, T entity);
    /**
     * 更新数据.
     *
     * @param entityList 数据列表,表中必须指定ID主键
     * @return 修改成功的数据行号和
     */
    protected abstract long updateListAbs(List<T> entityList);

    /**
     * 根据获取一条数据.
     *
     * @param id 数据ID主键
     * @return 一条数据映射实体
     */
    protected abstract T queryOneAbs(int id);

    /**
     * 根据获取一条数据.
     *
     * @param id 数据ID主键
     * @return 一条数据映射实体
     */
    protected abstract T queryOneAbs(String id);

    /**
     * 根据获取一条数据.
     *
     * @param column 数据某一列
     * @param data   数据某一列
     * @return 一条数据映射实体
     */
    protected abstract T queryOneAbs(String column, String data);

    /**
     * 执行查询语句.
     *
     * @param sql           sql语句
     * @param selectionArgs 绑定变量的参数值
     * @param clazz         返回的对象类型
     * @return 映射实体列表
     */
    protected abstract List<T> queryRawAbs(String sql, String[] selectionArgs, Class<T> clazz);

    /**
     * 查询列表.
     *
     * @return 映射实体列表
     */
    protected abstract List<T> queryListAbs();

    /**
     * @param page     查询的当前页
     * @param pageSize 查询当前页大小
     * @return
     */
    protected abstract List<T> queryListAbs(int page, int pageSize);

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
    protected abstract List<T> queryListAbs(String[] columns, String selection,
                                            String[] selectionArgs, String groupBy, String having,
                                            String orderBy, String limit);

    /**
     * 映射实体列表.
     *
     * @param selection     where语句的sql
     * @param selectionArgs where语句的sql的绑定变量的参数
     * @return 映射实体列表
     */
    protected abstract List<T> queryListAbs(String selection, String[] selectionArgs);

    /**
     * 检查是否存在数据.
     *
     * @param sql           sql语句
     * @param selectionArgs 绑定变量的参数值
     * @return 如果存在返回true, 不存在为false
     */
    protected abstract boolean isExistAbs(String sql, String[] selectionArgs);

    /**
     * 将查询的结果保存为名值对map.
     *
     * @param sql           查询sql
     * @param selectionArgs 绑定变量的参数值
     * @return 返回的Map中的key全部是小写形式.
     */
    protected abstract List<Map<String, String>> queryMapListAbs(String sql, String[] selectionArgs);

    /**
     * 返回一个查询的结果条数.
     *
     * @param sql           查询sql，where后面的语句
     * @param selectionArgs 绑定变量的参数值
     * @return 总条数.
     */
    protected abstract int queryCountAbs(String sql, String[] selectionArgs);

    /**
     * 返回一个查询的结果条数.
     *
     * @return 总条数.
     */
    protected abstract int queryCountAbs();

    /**
     * 封装执行sql代码.
     *
     * @param sql           sql语句，where后面的语句
     * @param selectionArgs 绑定变量的参数值
     */
    protected abstract void execSqlAbs(String sql, Object[] selectionArgs);


    /**
     * 得到列值.
     *
     * @param columnName the column name
     * @param cursor     the cursor
     * @return the string column value
     */
    public String getStringColumnValueAbs(String columnName, Cursor cursor) {
        return cursor.getString(cursor.getColumnIndex(columnName));
    }

    /**
     * 得到列值.
     *
     * @param columnName the column name
     * @param cursor     the cursor
     * @return the int column value
     */
    public int getIntColumnValueAbs(String columnName, Cursor cursor) {
        return cursor.getInt(cursor.getColumnIndex(columnName));
    }

    /**
     * 描述：关闭数据库与游标.
     *
     * @param cursor the cursor
     * @param db     the db
     */
    public void closeDB(Cursor cursor, SQLiteDatabase db) {
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }
        if (db != null && db.isOpen()) {
            db.close();
            db = null;
        }
    }

    /**
     * 描述：关闭游标.
     *
     * @param cursor the cursor
     */
    public void closeCursor(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }
    }


}
