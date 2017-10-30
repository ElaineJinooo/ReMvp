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
package com.remvp.library.db.orm;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.remvp.library.db.orm.annotation.Table;
import com.remvp.library.util.AbStrUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * The Class DBHelper.java
 * 描述：手机data/data下面的数据库
 * 对于更新数据库，只更新新增表和新增表的列 {@link #onUpgrade(SQLiteDatabase)}
 * 对于删除表和删除列未做处理
 */
public class DBHelper extends SQLiteOpenHelper {
    private final String TAG = "DBHelper";
    /**
     * The model classes.
     */
    private final Class<?>[] modelClasses;
    /**
     * 计算访问数据库线程的个数，用于多个线程同时操作数据库，一个线程完成后关闭数据库，其他线程出错
     * Android只支持单线程写入数据库
     */
    private static AtomicInteger mAtomicInteger = new AtomicInteger();

    /**
     * 初始化一个AbSDDBHelper.
     *
     * @param context      应用context
     * @param name         数据库名
     * @param factory      数据库查询的游标工厂
     * @param version      数据库的新版本号
     * @param modelClasses 要初始化的表的对象
     */
    public DBHelper(Context context, String name, CursorFactory factory,
                    int version, Class<?>[] modelClasses) {
        super(context, name, factory, version);
        this.modelClasses = modelClasses;
    }

    /**
     * 描述：表的创建.
     *
     * @param db 数据库对象
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        AbTableHelper.createTablesByClasses(db, this.modelClasses);
    }

    /**
     * 描述：表的重建.
     *
     * @param db         数据库对象
     * @param oldVersion 旧版本号
     * @param newVersion 新版本号
     *                   int, int)
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "DBHelper: onUpgrade: [uuuuuuu]="
                + "  oldVersion=" + oldVersion + "  newVersion" + newVersion);
        try {
//            List olderTables = saveOldTables(db, this.modelClasses);
//            onCreate(db);
//            restoreData(db, this.modelClasses, olderTables);
            onUpgrade(db);
            Log.i(TAG, "DBHelper: onUpgrade: [sssssss]="
                    + "成功了");
        } catch (Exception e) {
            Log.i(TAG, "DBHelper: onUpgrade: [eeeee]="
                    + e);
        }

    }

    /**
     * 更新数据库，只实现了新建表和新增表的列
     *
     * @param db
     */
    private void onUpgrade(SQLiteDatabase db) {
        List<String> oldTables = getOldTables(db);
        List<String> newTables = getNewTables();
        Map<String, Integer> map = getDiff(oldTables, newTables);
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (entry.getValue() == 1) {
                //删除表
//                String sql = "DROP TABLE IF EXISTS " + entry.getKey();
//                Log.d(TAG, "dropTable[" + entry.getKey() + "]:" + sql);
//                db.execSQL(sql);
            } else if (entry.getValue() == 2) {
                //新建表
                Class<?> clazz = getClassByTableName(entry.getKey());
                if (clazz != null) {
                    AbTableHelper.createTable(db, clazz);
                }
                Log.d(TAG, "onUpgrade: create table" + entry.getKey());
            } else {
                //更新表
                String tableName = entry.getKey();
                onUpgradeColumns(db, tableName);
            }
        }
    }

    /**
     * 更新表的列，只实现了新增列
     *
     * @param db
     * @param tableName
     */
    private void onUpgradeColumns(SQLiteDatabase db, String tableName) {
        Class<?> clazz = getClassByTableName(tableName);
        List<String> oldColumns = getColumns(db, tableName);
        List<String> newColumns = getColumns(clazz, tableName);
        Map<String, Integer> map = getDiff(oldColumns, newColumns);
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (entry.getValue() == 1) {
                //删除列
                Log.d(TAG, "onUpgradeColumns: delete 暂不执行");
            } else if (entry.getValue() == 2) {
                //新建列
                String columnName = entry.getKey();
                String columnType = getColumnType(clazz, tableName, entry.getKey());
                StringBuilder sql = new StringBuilder("ALTER TABLE ");
                sql.append(tableName)
                        .append(" ADD COLUMN '").append(columnName)
                        .append("' ").append(columnType)
                        .append(";");
                db.execSQL(sql.toString());
                Log.d(TAG, "onUpgradeColumns: " + sql.toString());
            }
        }
    }

    /**
     * 对比两个列表的不同，返回Map<String, Integer>
     *
     * @param one
     * @param two
     * @return key是列表的值，value 1：只有第一个列表有，2：只有第二个列表有，3：两个列表都有
     */
    private Map<String, Integer> getDiff(List<String> one, List<String> two) {
        Map<String, Integer> map = new HashMap<String, Integer>(one.size() + two.size());
        for (String string : one) {
            map.put(string, 1);
        }
        for (String string : two) {
            Integer cc = map.get(string);
            if (cc != null) {
                map.put(string, 3);
                continue;
            }
            map.put(string, 2);
        }
        return map;
    }

    private Class<?> getClassByTableName(String tableName) {
        for (Class<?> clazz : this.modelClasses) {
            if (tableName.equals(getTableName(clazz))) {
                return clazz;
            }
        }
        return null;
    }

    /**
     * 获取当前数据库(低版本)内表名集合
     *
     * @param db
     * @return
     */
    private List<String> getOldTables(SQLiteDatabase db) {
        List<String> list = new ArrayList<>();
        Cursor cursor = db.rawQuery("select name from sqlite_master where type='table';", null);
        while (cursor.moveToNext()) {
            //遍历出表名
            String tablename = cursor.getString(0);
            list.add(tablename);
        }
        return list;
    }

    /**
     * 通过{@link #modelClasses}获取新版本表名集合
     *
     * @return
     */
    private List<String> getNewTables() {
        List<String> list = new ArrayList<>();
        for (Class<?> clazz : this.modelClasses) {
            list.add(getTableName(clazz));
        }
        return list;
    }

    /**
     * 根据注解class获取列集合
     *
     * @param clazz
     * @param tableName
     * @return
     */
    private List<String> getColumns(Class<?> clazz, String tableName) {
        DaoConfig daoConfig = new DaoConfig(clazz, tableName);
        List<String> properties = new ArrayList();
        for (int j = 0; j < daoConfig.properties.length; j++) {
            String columnName = daoConfig.properties[j].columnName;
            properties.add(columnName);
        }
        return properties;
    }

    /**
     * 获取列的类型
     *
     * @param clazz
     * @param tableName
     * @param columnsName
     * @return
     */
    private String getColumnType(Class<?> clazz, String tableName, String columnsName) {
        DaoConfig daoConfig = new DaoConfig(clazz, tableName);
        for (int j = 0; j < daoConfig.properties.length; j++) {
            String columnName = daoConfig.properties[j].columnName;
            if (columnName.equals(columnsName)) {
                return daoConfig.properties[j].type;
            }
        }
        return "TEXT";
    }

    /**
     * 根据注解class获取表名
     *
     * @param daoClasses
     * @return
     */
    private String getTableName(Class<?> daoClasses) {
        String tablename = "";
        if (daoClasses.isAnnotationPresent(Table.class)) {
            Table table = daoClasses.getAnnotation(Table.class);
            tablename = table.name();
        }
        if (AbStrUtil.isEmpty(tablename)) {
            Log.i(TAG, "DaoConfig: DaoConfig: [daoClasses]="
                    + "想要映射的实体[" + daoClasses.getName() + "],未注解@Table(name=\"?\"),被跳过");

        }
        return tablename;
    }

    private List<String> saveOldTables(SQLiteDatabase db, Class<?>[] daoClasses) {
        List<String> list = new ArrayList<>();
        Cursor cursor = db.rawQuery("select name from sqlite_master where type='table';", null);
        while (cursor.moveToNext()) {
            //遍历出表名
            String tablename = cursor.getString(0);
            list.add(tablename);
            Log.d(TAG, "DBHelper: onUpgrade: [*********]="
                    + tablename);
            String tempTableName = tablename.concat("_TEMP");
            StringBuilder builder = new StringBuilder();
            builder.append("ALTER TABLE ")
                    .append(tablename)
                    .append(" RENAME TO ")
                    .append(tempTableName);
            db.execSQL(builder.toString());
        }
        return list;
    }

    private void restoreData(SQLiteDatabase db, Class<?>[] daoClasses, List olderTables) {

        for (int i = 0; i < daoClasses.length; i++) {
            String table = getTableName(daoClasses[i]);
            if (AbStrUtil.isEmpty(table) || !olderTables.contains(table)) {
                continue;
            }
            DaoConfig daoConfig = new DaoConfig(daoClasses[i], table);

            String tableName = daoConfig.mTableName;
            String tempTableName = daoConfig.mTableName.concat("_TEMP");
            ArrayList<String> properties = new ArrayList();
            for (int j = 0; j < daoConfig.properties.length; j++) {
                String columnName = daoConfig.properties[j].columnName;

                if (getColumns(db, tempTableName).contains(columnName)) {
                    properties.add(columnName);
                }
            }
            StringBuilder insertTableStringBuilder = new StringBuilder();
            insertTableStringBuilder.append("INSERT INTO ").append(tableName).append(" (");
            insertTableStringBuilder.append(TextUtils.join(",", properties));
            insertTableStringBuilder.append(") SELECT ");
            insertTableStringBuilder.append(TextUtils.join(",", properties));
            insertTableStringBuilder.append(" FROM ").append(tempTableName).append(";");

            StringBuilder dropTableStringBuilder = new StringBuilder();

            dropTableStringBuilder.append("DROP TABLE ").append(tempTableName);

            db.execSQL(insertTableStringBuilder.toString());
            Log.i(TAG, "DBHelper: restoreData: [rrr11111]="
                    + insertTableStringBuilder.toString());
            db.execSQL(dropTableStringBuilder.toString());
            Log.i(TAG, "DBHelper: restoreData: [rrr22222]="
                    + dropTableStringBuilder.toString());
        }
    }


    /**
     * 根据当前数据库的表名获取表的列集合
     *
     * @param db
     * @param tableName
     * @return
     */
    private List<String> getColumns(SQLiteDatabase db, String tableName) {
        List<String> columns = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + tableName + " limit 1", null);
            if (cursor != null) {
                columns = new ArrayList<>(Arrays.asList(cursor.getColumnNames()));
            }
        } catch (Exception e) {
            Log.v(tableName, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return columns;
    }

    /**
     * 关闭数据库
     */
    public boolean canCloseDb() {
        return mAtomicInteger.decrementAndGet() == 0;
    }

    /**
     * 计算访问数据库库个数
     */
    public void openDb() {
        mAtomicInteger.incrementAndGet();
    }

    /**
     * 计算访问数据库库个数
     */
    public boolean isOpenDb() {
        return mAtomicInteger.get() == 0;
    }
}
