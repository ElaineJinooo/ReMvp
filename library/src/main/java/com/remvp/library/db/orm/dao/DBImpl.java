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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.remvp.library.db.orm.AbTableHelper;
import com.remvp.library.db.orm.DBHelper;
import com.remvp.library.db.orm.annotation.ActionType;
import com.remvp.library.db.orm.annotation.Column;
import com.remvp.library.db.orm.annotation.Id;
import com.remvp.library.db.orm.annotation.RelationDao;
import com.remvp.library.db.orm.annotation.RelationsType;
import com.remvp.library.db.orm.annotation.Table;
import com.remvp.library.util.AbStrUtil;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The Class DBImpl.
 * <p>
 * 修改了增删改查关联表问题
 * {@link #insertAbs(Object, boolean)}
 * {@link #delete(String, Object)}
 * {@link #update(String, Object)}
 * {@link #queryListAbs(String[], String, String[], String, String, String, String)}
 * by 2016/03/16 张效伟修改 注：update不一定修改了，作者忘记有么有更改
 */
public class DBImpl<T> extends AbBasicDBDao<T> {
    /**
     * The tag.
     */
    private static final String TAG = "DBImpl";
    /**
     * 锁对象
     */
    private final ReentrantLock lock = new ReentrantLock();

    /**
     * The table name.
     */
    private String mTableName;

    /**
     * 自定义主键
     */

    private String idColumn;

    /**
     * The clazz.
     */
    private Class<T> clazz;

    /**
     * The all fields.
     */
    private List<Field> allFields;

    /**
     * The Constant METHOD_INSERT.
     */
    private final int METHOD_INSERT = 0;

    /**
     * The Constant METHOD_UPDATE.
     */
    private final int METHOD_UPDATE = 1;

    /**
     * The Constant TYPE_NOT_INCREMENT.
     */
    private final int TYPE_NOT_INCREMENT = 0;

    /**
     * The Constant TYPE_INCREMENT.
     */
    private final int TYPE_INCREMENT = 1;

    /**
     * 这个Dao的数据库对象
     */
    private SQLiteDatabase mSQLiteDatabase = null;
    /**
     * The mSQLiteDatabase helper.
     */
    private DBHelper mSQLiteOpenHelper;

    /**
     * 用一个对象实体初始化这个数据库操作实现类.
     *
     * @param dbHelper 数据库操作实现类
     * @param clazz    映射对象实体
     */
    public DBImpl(DBHelper dbHelper, Class<T> clazz) {
        this.mSQLiteOpenHelper = dbHelper;
        if (clazz == null) {
            this.clazz = ((Class<T>) ((ParameterizedType) super
                    .getClass().getGenericSuperclass())
                    .getActualTypeArguments()[0]);
        } else {
            this.clazz = clazz;
        }

        if (this.clazz.isAnnotationPresent(Table.class)) {
            Table table = this.clazz.getAnnotation(Table.class);
            this.mTableName = table.name();
        }

        // 加载所有字段
        this.allFields = AbTableHelper.joinFields(this.clazz.getDeclaredFields(),
                this.clazz.getSuperclass().getDeclaredFields());

        // 找到主键
        for (Field field : this.allFields) {
            if (field.isAnnotationPresent(Id.class)) {
                Column column = field.getAnnotation(Column.class);
                this.idColumn = column.name();
                break;
            }
//            if (field.isAnnotationPresent(IdDao.class)) {
//                Column column = field.getAnnotation(Column.class);
//                this.idColumn = column.name();
//                mIdColumnFields = field;
//                break;
//            }
        }
//        Log.d(TAG, "clazz:" + this.clazz + " mTableName:" + this.mTableName
//                + " idColumn:" + this.idColumn);
    }

    /**
     * 是否在操作数据库
     *
     * @return
     */
    public boolean isLocked() {
        return lock.isLocked();
    }

    /**
     * 是否在操作数据库
     *
     * @return
     */
    public boolean isOpen() {
        if (mSQLiteDatabase == null) {
            return false;
        }
        return mSQLiteDatabase.isOpen();
    }

    /**
     * 初始化这个数据库操作实现类.
     *
     * @param dbHelper 数据库操作实现类
     */
    public DBImpl(DBHelper dbHelper) {
        this(dbHelper, null);
    }


    /**
     * 描述：TODO.
     *
     * @return the mSQLiteDatabase helper
     */
    public SQLiteOpenHelper getSQLiteOpenHelper() {
        return mSQLiteOpenHelper;
    }

    /**
     * 描述：查询一条.
     *
     * @param id the id
     * @return the t
     */
    @Override
    protected T queryOneAbs(int id) {
        synchronized (lock) {
            String selection = this.idColumn + " = ?";
            String[] selectionArgs = {Integer.toString(id)};
            Log.d(TAG, "[queryOne]: select * from " + this.mTableName + " where "
                    + this.idColumn + " = '" + id + "'");
            List<T> list = queryListAbs(null, selection, selectionArgs, null, null, null,
                    null);
            if ((list != null) && (list.size() > 0)) {
                return list.get(0);
            }
            return null;
        }
    }

    /**
     * 描述：查询一条.
     *
     * @param id the id
     * @return the t
     */
    @Override
    protected T queryOneAbs(String id) {
        synchronized (lock) {
            String selection = this.idColumn + " = ?";
            String[] selectionArgs = {id};
            Log.d(TAG, "[queryOne]: select * from " + this.mTableName + " where "
                    + this.idColumn + " = '" + id + "'");
            List<T> list = queryListAbs(null, selection, selectionArgs, null, null, null,
                    null);
            if ((list != null) && (list.size() > 0)) {
                return list.get(0);
            }
            return null;
        }
    }

    /**
     * @param column 某一列的列明
     * @param data   某一列数据
     * @return
     */
    @Override
    protected T queryOneAbs(String column, String data) {
        synchronized (lock) {
            String selection = column + " = ?";
            String[] selectionArgs = {data};
            Log.d(TAG, "[queryOne]: select * from " + this.mTableName + " where "
                    + this.idColumn + " = '" + column + "'");
            List<T> list = queryListAbs(null, selection, selectionArgs, null, null, null,
                    null);
            if ((list != null) && (list.size() > 0)) {
                return list.get(0);
            }
            return null;
        }
    }

    /**
     * 描述：一种更灵活的方式查询，不支持对象关联，可以写完整的sql.
     *
     * @param sql           完整的sql如：select * from a ,b where a.id=b.id and a.id = ?
     * @param selectionArgs 绑定变量值
     * @param clazz         返回的对象类型
     * @return the list
     */
    @Override
    protected List<T> queryRawAbs(String sql, String[] selectionArgs, Class<T> clazz) {

        List<T> list = new ArrayList<T>();
        Cursor cursor = null;
        try {
            lock.lock();
            Log.d(TAG, "[queryRaw]: " + getLogSql(sql, selectionArgs));
            cursor = mSQLiteDatabase.rawQuery(sql, selectionArgs);
            getListFromCursor(clazz, list, cursor);


            //获取关联域的操作类型和关系类型
            String foreignKey = null;
            String type = null;
            String action = null;
            /**
             * 父类列表属性与外键对应
             */
            String name = null;
            //需要判断是否有关联表
            for (Field childField : allFields) {
                if (!childField.isAnnotationPresent(RelationDao.class)) {
                    continue;
                }

                RelationDao relationDao = childField.getAnnotation(RelationDao.class);
                //获取外键列名
                foreignKey = relationDao.foreignKey();
                //关联类型
                type = relationDao.type();
                //操作类型
                action = relationDao.action();

                name = relationDao.name();
                //设置可访问
                childField.setAccessible(true);

//                if (!(action.indexOf(ActionType.query) != -1)) {
//                    return list;
//                }

                //得到关联表的表名查询
                for (T entity : list) {
                    if (RelationsType.one2one.equals(type)) {
                        //一对一关系
                        //获取这个实体的表名
                        String childTableName = "";
                        if (childField.getType().isAnnotationPresent(Table.class)) {
                            Table table = childField.getType().getAnnotation(Table.class);
                            childTableName = table.name();
                        }

                        List<T> RelationsDaoList = new ArrayList<T>();
                        Field[] relationsDaoEntityFields = childField.getType().getDeclaredFields();
                        for (Field RelationsDaoEntityField : relationsDaoEntityFields) {
                            RelationsDaoEntityField.setAccessible(true);
                            Column RelationsDaoEntityColumn = RelationsDaoEntityField.getAnnotation(Column.class);
                            if (RelationsDaoEntityColumn == null) {
                                Log.i(TAG, "DBImpl: queryList:00000000000");
                                continue;
                            }
                            //获取外键的值作为关联表的查询条件
                            if (foreignKey.equals(RelationsDaoEntityColumn.name())) {

                                //主表的用于关联表的foreignKey值
                                String value = "-1";
                                for (Field entityField : allFields) {
                                    //设置可访问
                                    entityField.setAccessible(true);
                                    Column entityForeignKeyColumn = entityField.getAnnotation(Column.class);
                                    if (entityForeignKeyColumn == null) {
                                        continue;
                                    }
                                    if (entityForeignKeyColumn.name().equals(name)) {
                                        value = String.valueOf(entityField.get(entity));
                                        break;
                                    }
                                }
                                Log.i(TAG, "DBImpl: queryList:" +
                                        " [c0000000000]" + "RelationsDaoTableName=" +
                                        childTableName + "   foreignKey=" + foreignKey
                                        + "  value=" + value);
                                //查询数据设置给这个域
                                cursor = mSQLiteDatabase.query(childTableName, null, foreignKey + " = ?", new String[]{value}, null, null, null, null);
                                getListFromCursor(childField.getType(), RelationsDaoList, cursor);
                                if (RelationsDaoList.size() > 0) {
                                    //获取关联表的对象设置值
                                    childField.set(entity, RelationsDaoList.get(0));
                                }

                                break;
                            }
                        }

                    } else if (RelationsType.one2many.equals(type) || RelationsType.many2many.equals(type)) {
                        //一对多关系

                        RelationDao relationDao1 = childField.getAnnotation(RelationDao.class);
                        //获取外键列名
                        foreignKey = relationDao1.foreignKey();
                        //得到泛型里的class类型对象
                        Class listEntityClazz = null;
                        Class<?> fieldClass = childField.getType();
                        if (fieldClass.isAssignableFrom(List.class)) {
                            Type fc = childField.getGenericType();
                            if (fc == null) continue;
                            if (fc instanceof ParameterizedType) {
                                ParameterizedType pt = (ParameterizedType) fc;
                                listEntityClazz = (Class) pt.getActualTypeArguments()[0];
                            }
                        }

                        if (listEntityClazz == null) {
                            Log.e(TAG, "对象模型需要设置List的泛型");
                            return null;
                        }

                        //得到表名
                        String tableName = "";
                        if (listEntityClazz.isAnnotationPresent(Table.class)) {
                            Table table = (Table) listEntityClazz.getAnnotation(Table.class);
                            tableName = table.name();
                        }

                        List<T> RelationsDaoList = new ArrayList<T>();
                        Field[] declaredFields = listEntityClazz.getDeclaredFields();
                        for (Field field : declaredFields) {
                            field.setAccessible(true);
                            Column relationsDaoEntityColumn = field.getAnnotation(Column.class);

                            //获取外键的值作为关联表的查询条件
                            if (relationsDaoEntityColumn != null && relationsDaoEntityColumn.name().equals(foreignKey)) {

                                //主表的用于关联表的foreignKey值
                                String value = "-1";
                                for (Field entityField : allFields) {
                                    //设置可访问
                                    entityField.setAccessible(true);
                                    Column entityForeignKeyColumn = entityField.getAnnotation(Column.class);
                                    if (entityForeignKeyColumn.name().equals(name)) {
                                        value = String.valueOf(entityField.get(entity));
                                        break;
                                    }
                                }
                                //查询数据设置给这个域
                                cursor = mSQLiteDatabase.query(tableName, null, foreignKey + " = ?", new String[]{value}, null, null, null, null);
                                getListFromCursor(listEntityClazz, RelationsDaoList, cursor);
                                if (RelationsDaoList.size() > 0) {
                                    //获取关联表的对象设置值
                                    childField.set(entity, RelationsDaoList);
                                }

                                break;
                            }
                        }

                    }
                }
            }
        } catch (Exception e) {
            Log.e(this.TAG, "[queryRaw] from DB Exception.");
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
            lock.unlock();
        }

        return list;
    }

    /**
     * 描述：是否存在.
     *
     * @param sql           the sql
     * @param selectionArgs the selection args
     * @return if is exist
     */
    @Override
    protected boolean isExistAbs(String sql, String[] selectionArgs) {
        Cursor cursor = null;
        try {
            lock.lock();
            Log.d(TAG, "[isExist]: " + getLogSql(sql, selectionArgs));
            cursor = mSQLiteDatabase.rawQuery(sql, selectionArgs);
            if (cursor.getCount() > 0) {
                return true;
            }
        } catch (Exception e) {
            Log.e(this.TAG, "[isExist] from DB Exception.");
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
            lock.unlock();
        }
        return false;
    }

    /**
     * 描述：查询所有数据.
     *
     * @return the list
     */
    @Override
    protected List<T> queryListAbs() {
        return queryListAbs(null, null, null, null, null, null, null);
    }

    @Override
    protected List<T> queryListAbs(int page, int pageSize) {
        String limit = (page - 1) * pageSize + "," + pageSize;
        Log.i(TAG, "DBImpl: queryList: [dddddddddddd]=" + limit);
        return queryListAbs(null, null, null, null, null, null, limit);
    }

    /**
     * 描述：查询列表.
     *
     * @param columns       the columns
     * @param where         the selection
     * @param selectionArgs the selection args
     * @param groupBy       the group by
     * @param having        the having
     * @param orderBy       the order by
     * @param limit         the limit
     * @return the list
     */
    @Override
    protected List<T> queryListAbs(String[] columns, String where,
                                   String[] selectionArgs, String groupBy, String having,
                                   String orderBy, String limit) {

        List<T> list = new ArrayList<T>();
        Cursor cursor = null;
        try {
            lock.lock();
            Log.d(TAG, "[queryList] from " + this.mTableName + " where " + where
                    + "(" + selectionArgs + ")" + " group by " + groupBy + " having " + having + " order by " + orderBy + " limit " + limit);
            cursor = mSQLiteDatabase.query(this.mTableName, columns, where,
                    selectionArgs, groupBy, having, orderBy, limit);

            getListFromCursor(this.clazz, list, cursor);

            closeCursor(cursor);

            //获取关联域的操作类型和关系类型
            String foreignKey = null;
            String type = null;
            String action = null;
            /**
             * 父类列表属性与外键对应
             */
            String name = null;
            //需要判断是否有关联表
            for (Field childField : allFields) {
                if (!childField.isAnnotationPresent(RelationDao.class)) {
                    continue;
                }

                RelationDao relationDao = childField.getAnnotation(RelationDao.class);
                //获取外键列名
                foreignKey = relationDao.foreignKey();
                //关联类型
                type = relationDao.type();
                //操作类型
                action = relationDao.action();

                name = relationDao.name();
                //设置可访问
                childField.setAccessible(true);

//                if (!(action.indexOf(ActionType.query) != -1)) {
//                    return list;
//                }

                //得到关联表的表名查询
                for (T entity : list) {
                    if (RelationsType.one2one.equals(type)) {
                        //一对一关系
                        //获取这个实体的表名
                        String childTableName = "";
                        if (childField.getType().isAnnotationPresent(Table.class)) {
                            Table table = childField.getType().getAnnotation(Table.class);
                            childTableName = table.name();
                        }

                        List<T> relationsDaoList = new ArrayList<T>();
                        Field[] relationsDaoEntityFields = childField.getType().getDeclaredFields();
                        for (Field RelationsDaoEntityField : relationsDaoEntityFields) {
                            RelationsDaoEntityField.setAccessible(true);
                            Column RelationsDaoEntityColumn = RelationsDaoEntityField.getAnnotation(Column.class);
                            if (RelationsDaoEntityColumn == null) {
                                Log.i(TAG, "DBImpl: queryList:00000000000");
                                continue;
                            }
                            //获取外键的值作为关联表的查询条件
                            if (foreignKey.equals(RelationsDaoEntityColumn.name())) {

                                //主表的用于关联表的foreignKey值
                                String value = "-1";
                                for (Field entityField : allFields) {
                                    //设置可访问
                                    entityField.setAccessible(true);
                                    Column entityForeignKeyColumn = entityField.getAnnotation(Column.class);
                                    if (entityForeignKeyColumn == null) {
                                        continue;
                                    }
                                    if (entityForeignKeyColumn.name().equals(name)) {
                                        value = String.valueOf(entityField.get(entity));
                                        break;
                                    }
                                }
                                Log.i(TAG, "DBImpl: queryList:" +
                                        " [c0000000000]" + "RelationsDaoTableName=" +
                                        childTableName + "   foreignKey=" + foreignKey
                                        + "  value=" + value);
                                //查询数据设置给这个域
                                cursor = mSQLiteDatabase.query(childTableName, null, foreignKey + " = ?", new String[]{value}, null, null, null, null);
                                getListFromCursor(childField.getType(), relationsDaoList, cursor);
                                if (relationsDaoList.size() > 0) {
                                    //获取关联表的对象设置值
                                    childField.set(entity, relationsDaoList.get(0));
                                }

                                break;
                            }
                        }

                    } else if (RelationsType.one2many.equals(type) || RelationsType.many2many.equals(type)) {
                        //一对多关系

                        RelationDao relationDao1 = childField.getAnnotation(RelationDao.class);
                        //获取外键列名
                        foreignKey = relationDao1.foreignKey();
                        //得到泛型里的class类型对象
                        Class listEntityClazz = null;
                        Class<?> fieldClass = childField.getType();
                        if (fieldClass.isAssignableFrom(List.class)) {
                            Type fc = childField.getGenericType();
                            if (fc == null) continue;
                            if (fc instanceof ParameterizedType) {
                                ParameterizedType pt = (ParameterizedType) fc;
                                listEntityClazz = (Class) pt.getActualTypeArguments()[0];
                            }
                        }

                        if (listEntityClazz == null) {
                            Log.e(TAG, "对象模型需要设置List的泛型");
                            return null;
                        }

                        //得到表名
                        String tableName = "";
                        if (listEntityClazz.isAnnotationPresent(Table.class)) {
                            Table table = (Table) listEntityClazz.getAnnotation(Table.class);
                            tableName = table.name();
                        }

                        List<T> relationsDaoList = new ArrayList<T>();
                        Field[] declaredFields = listEntityClazz.getDeclaredFields();
                        for (Field field : declaredFields) {
                            field.setAccessible(true);
                            Column relationsDaoEntityColumn = field.getAnnotation(Column.class);

                            //获取外键的值作为关联表的查询条件
                            if (relationsDaoEntityColumn != null && relationsDaoEntityColumn.name().equals(foreignKey)) {

                                //主表的用于关联表的foreignKey值
                                String value = "-1";
                                for (Field entityField : allFields) {
                                    //设置可访问
                                    entityField.setAccessible(true);
                                    Column entityForeignKeyColumn = entityField.getAnnotation(Column.class);
                                    if (entityForeignKeyColumn.name().equals(name)) {
                                        value = String.valueOf(entityField.get(entity));
                                        break;
                                    }
                                }
                                //查询数据设置给这个域
                                cursor = mSQLiteDatabase.query(tableName, null, foreignKey + " = ?", new String[]{value}, null, null, null, null);
                                getListFromCursor(listEntityClazz, relationsDaoList, cursor);
                                if (relationsDaoList.size() > 0) {
                                    //获取关联表的对象设置值
                                    childField.set(entity, relationsDaoList);
                                }

                                break;
                            }
                        }

                    }
                }
            }

        } catch (Exception e) {
            Log.e(this.TAG, "[queryList] from DB Exception");
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
            lock.unlock();
        }

        return list;
    }


    /**
     * 描述：简单一些的查询.
     *
     * @param where         the selection
     * @param selectionArgs the selection args
     * @return the list
     * @author: zhaoqp
     */
    @Override
    protected List<T> queryListAbs(String where, String[] selectionArgs) {
        return queryListAbs(null, where, selectionArgs, null, null, null, null);
    }

    /**
     * 从游标中获得映射对象列表.
     *
     * @param list   返回的映射对象列表
     * @param cursor 当前游标
     * @return the list from cursor
     * @throws IllegalAccessException the illegal access exception
     * @throws InstantiationException the instantiation exception
     */
    private void getListFromCursor(Class<?> clazz, List<T> list, Cursor cursor)
            throws IllegalAccessException, InstantiationException {
        while (cursor.moveToNext()) {
            Object entity = clazz.newInstance();
            // 加载所有字段
            List<Field> allFields = AbTableHelper.joinFields(entity.getClass().getDeclaredFields(),
                    entity.getClass().getSuperclass().getDeclaredFields());


            for (Field field : allFields) {
                Column column = null;
                if (field.isAnnotationPresent(Column.class)) {
                    column = field.getAnnotation(Column.class);

                    field.setAccessible(true);
                    Class<?> fieldType = field.getType();

                    int c = cursor.getColumnIndex(column.name());
                    if (c < 0) {
                        continue; // 如果不存则循环下个属性值
                    } else if ((Integer.TYPE == fieldType)
                            || (Integer.class == fieldType)) {
                        field.set(entity, cursor.getInt(c));
                    } else if (String.class == fieldType) {
                        field.set(entity, cursor.getString(c));
                    } else if ((Long.TYPE == fieldType)
                            || (Long.class == fieldType)) {
                        field.set(entity, Long.valueOf(cursor.getLong(c)));
                    } else if ((Float.TYPE == fieldType)
                            || (Float.class == fieldType)) {
                        field.set(entity, Float.valueOf(cursor.getFloat(c)));
                    } else if ((Short.TYPE == fieldType)
                            || (Short.class == fieldType)) {
                        field.set(entity, Short.valueOf(cursor.getShort(c)));
                    } else if ((Double.TYPE == fieldType)
                            || (Double.class == fieldType)) {
                        field.set(entity, Double.valueOf(cursor.getDouble(c)));
                    } else if (Date.class == fieldType) {// 处理java.util.Date类型,update2012-06-10
                        Date date = new Date();
                        date.setTime(cursor.getLong(c));
                        field.set(entity, date);
                    } else if (Blob.class == fieldType) {
                        field.set(entity, cursor.getBlob(c));
                    } else if (Character.TYPE == fieldType) {
                        String fieldValue = cursor.getString(c);
                        if ((fieldValue != null) && (fieldValue.length() > 0)) {
                            field.set(entity, Character.valueOf(fieldValue.charAt(0)));
                        }
                    } else if ((Boolean.TYPE == fieldType) || (Boolean.class == fieldType)) {
                        String temp = cursor.getString(c);
                        if ("true".equals(temp) || "1".equals(temp)) {
                            field.set(entity, true);
                        } else {
                            field.set(entity, false);
                        }
                    }

                }
            }

            list.add((T) entity);
        }
    }

    /**
     * 描述：插入实体.
     *
     * @param entity the entity
     * @return the long
     */
    @Override
    protected long insertAbs(T entity) {
        return insertAbs(entity, true);
    }

    /**
     * 描述：插入实体.
     *
     * @param entity the entity
     * @param flag   the flag
     * @return the long
     */
    @Override
    protected long insertAbs(T entity, boolean flag) {
        String sql = null;
        long row = 0L;
        try {
            lock.lock();
            ContentValues cv = new ContentValues();
            if (flag) {
                // id自增
                sql = setContentValues(entity, cv, TYPE_INCREMENT, METHOD_INSERT);
            } else {
                // id需指定
                sql = setContentValues(entity, cv, TYPE_NOT_INCREMENT, METHOD_INSERT);
            }
            Log.i(TAG, "DBImpl: insert: [*********"
                    + "[insert]: insert into " + this.mTableName + " " + sql);
            row = mSQLiteDatabase.insert(this.mTableName, null, cv);

            //获取关联域的操作类型和关系类型
            String foreignKey = null;
            String type = null;
            String action = null;
            //需要判断是否有关联表
            for (Field relationsDaoField : allFields) {
                if (!relationsDaoField.isAnnotationPresent(RelationDao.class)) {
                    continue;
                }
                RelationDao RelationsDao = relationsDaoField.getAnnotation(RelationDao.class);
                //获取外键列名
                foreignKey = RelationsDao.foreignKey();
                //关联类型
                type = RelationsDao.type();
                //操作类型
                action = RelationsDao.action();
                //设置可访问
                relationsDaoField.setAccessible(true);
                if (!(action.indexOf(ActionType.insert) != -1)) {
                    return row;
                }
                if (RelationsType.one2one.equals(type)) {
                    //一对一关系
                    //获取关联表的对象
                    T relationsDaoEntity = (T) relationsDaoField.get(entity);
                    if (relationsDaoEntity != null) {
                        ContentValues contentValues = new ContentValues();
                        if (flag) {
                            // id自增
                            sql = setContentValues(relationsDaoEntity, contentValues, TYPE_INCREMENT, METHOD_INSERT);
                        } else {
                            // id需指定
                            sql = setContentValues(relationsDaoEntity, contentValues, TYPE_NOT_INCREMENT, METHOD_INSERT);
                        }
                        String relationsDaoTableName = "";
                        if (relationsDaoEntity.getClass().isAnnotationPresent(Table.class)) {
                            Table table = relationsDaoEntity.getClass().getAnnotation(Table.class);
                            relationsDaoTableName = table.name();
                        }

                        Log.d(TAG, "[insert]: insert into " + relationsDaoTableName + " " + sql);
                        row += mSQLiteDatabase.insert(relationsDaoTableName, null, contentValues);
                    }
                } else if (RelationsType.one2many.equals(type) || RelationsType.many2many.equals(type)) {
                    //一对多关系
                    //获取关联表的对象
                    List<T> list = (List<T>) relationsDaoField.get(entity);
                    if (list != null && list.size() > 0) {
                        for (T RelationsDaoEntity : list) {
                            ContentValues RelationsDaoCv = new ContentValues();
                            if (flag) {
                                // id自增
                                sql = setContentValues(RelationsDaoEntity, RelationsDaoCv, TYPE_INCREMENT, METHOD_INSERT);
                            } else {
                                // id需指定
                                sql = setContentValues(RelationsDaoEntity, RelationsDaoCv, TYPE_NOT_INCREMENT, METHOD_INSERT);
                            }
                            String relationsDaoTableName = "";
                            if (RelationsDaoEntity.getClass().isAnnotationPresent(Table.class)) {
                                Table table = RelationsDaoEntity.getClass().getAnnotation(Table.class);
                                relationsDaoTableName = table.name();
                            }

                            Log.d(TAG, "[******ddddddd]: insert into " + relationsDaoTableName + " " + sql);
                            row += mSQLiteDatabase.insert(relationsDaoTableName, null, RelationsDaoCv);
                        }
                    }

                }
            }

        } catch (Exception e) {
            Log.d(this.TAG, "[insert] into DB Exception.");
            e.printStackTrace();
            row = -1;
        } finally {
            lock.unlock();
        }
        return row;
    }


    /**
     * 描述：插入列表
     */
    @Override
    protected long insertListAbs(List<T> entityList) {
        return insertListAbs(entityList, true);
    }

    /**
     * 描述：插入列表
     */
    @Override
    protected long insertListAbs(List<T> entityList, boolean flag) {
        String sql = null;
        long rows = 0;
        try {
            lock.lock();
            for (T entity : entityList) {
                ContentValues cv = new ContentValues();
                if (flag) {
                    // id自增
                    sql = setContentValues(entity, cv, TYPE_INCREMENT, METHOD_INSERT);
                } else {
                    // id需指定
                    sql = setContentValues(entity, cv, TYPE_NOT_INCREMENT, METHOD_INSERT);
                }

                Log.d(TAG, "[insertList]: insert into " + this.mTableName + " " + sql);
                rows += mSQLiteDatabase.insert(this.mTableName, null, cv);


                //获取关联域的操作类型和关系类型
                String foreignKey = null;
                String type = null;
                String action = null;
                Field field = null;
                //需要判断是否有关联表
                for (Field RelationsDaoField : allFields) {
                    if (!RelationsDaoField.isAnnotationPresent(RelationDao.class)) {
                        continue;
                    }

                    RelationDao RelationsDao = RelationsDaoField.getAnnotation(RelationDao.class);
                    //获取外键列名
                    foreignKey = RelationsDao.foreignKey();
                    //关联类型
                    type = RelationsDao.type();
                    //操作类型
                    action = RelationsDao.action();
                    //设置可访问
                    RelationsDaoField.setAccessible(true);
                    field = RelationsDaoField;
                }

                if (field == null) {
                    continue;
                }

                if (!(action.indexOf(ActionType.insert) != -1)) {
                    continue;
                }

                if (RelationsType.one2one.equals(type)) {
                    //一对一关系
                    //获取关联表的对象
                    T RelationsDaoEntity = (T) field.get(entity);
                    if (RelationsDaoEntity != null) {
                        ContentValues RelationsDaoCv = new ContentValues();
                        if (flag) {
                            // id自增
                            sql = setContentValues(RelationsDaoEntity, RelationsDaoCv, TYPE_INCREMENT, METHOD_INSERT);
                        } else {
                            // id需指定
                            sql = setContentValues(RelationsDaoEntity, RelationsDaoCv, TYPE_NOT_INCREMENT, METHOD_INSERT);
                        }
                        String RelationsDaoTableName = "";
                        if (RelationsDaoEntity.getClass().isAnnotationPresent(Table.class)) {
                            Table table = RelationsDaoEntity.getClass().getAnnotation(Table.class);
                            RelationsDaoTableName = table.name();
                        }

                        Log.d(TAG, "[insertList]: insert into " + RelationsDaoTableName + " " + sql);
                        rows += mSQLiteDatabase.insert(RelationsDaoTableName, null, RelationsDaoCv);
                    }

                } else if (RelationsType.one2many.equals(type) || RelationsType.many2many.equals(type)) {
                    //一对多关系
                    //获取关联表的对象
                    List<T> list = (List<T>) field.get(entity);
                    if (list != null && list.size() > 0) {
                        for (T RelationsDaoEntity : list) {
                            ContentValues RelationsDaoCv = new ContentValues();
                            if (flag) {
                                // id自增
                                sql = setContentValues(RelationsDaoEntity, RelationsDaoCv, TYPE_INCREMENT, METHOD_INSERT);
                            } else {
                                // id需指定
                                sql = setContentValues(RelationsDaoEntity, RelationsDaoCv, TYPE_NOT_INCREMENT, METHOD_INSERT);
                            }
                            String RelationsDaoTableName = "";
                            if (RelationsDaoEntity.getClass().isAnnotationPresent(Table.class)) {
                                Table table = RelationsDaoEntity.getClass().getAnnotation(Table.class);
                                RelationsDaoTableName = table.name();
                            }

                            Log.d(TAG, "[insertList]: insert into " + RelationsDaoTableName + " " + sql);
                            rows += mSQLiteDatabase.insert(RelationsDaoTableName, null, RelationsDaoCv);
                        }
                    }

                }
            }
        } catch (Exception e) {
            Log.d(this.TAG, "[insertList] into DB Exception.");
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

        return rows;
    }


    /**
     * 描述：按id删除.
     *
     * @param id the id
     */
    @Override
    protected long deleteAbs(int id) {
        long rows = -1;
        try {
            lock.lock();
            String where = this.idColumn + " = ?";
            String[] whereValue = {Integer.toString(id)};
            Log.d(TAG, "[delete]: delelte from " + this.mTableName + " where "
                    + where.replace("?", String.valueOf(id)));
            rows = mSQLiteDatabase.delete(this.mTableName, where, whereValue);
//            mSQLiteDatabase.delete()
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return rows;
    }

    /**
     * 删除集合
     *
     * @param ids
     * @return
     */
    @Override
    protected List<T> deleteListAbs(List<T> ids) {
        if (ids == null || ids.size() == 0) {
            return null;
        }
        try {
            lock.lock();
            int size = ids.size();
            int size1 = size - 1;
//            String[] whereValue = new String[ids.size()];
            StringBuilder where = new StringBuilder("delete from ");
            StringBuilder builder = new StringBuilder(" (");
            for (int i = 0; i < ids.size(); i++) {
                T data = ids.get(i);
                // 加载所有字段
                List<Field> fileds = AbTableHelper.joinFields(data.getClass().getDeclaredFields(),
                        data.getClass().getSuperclass().getDeclaredFields());
                // 找到主键
                for (Field field : fileds) {
                    Log.i(TAG, "DBImpl: deleteList: [111111*******]=");
                    if (field.isAnnotationPresent(Id.class)) {
                        Log.i(TAG, "DBImpl: deleteList: [2222222*******]=");
                        field.setAccessible(true);
                        String fieldValue = (String) field.get(data);
                        if (i == size1) {
                            builder.append("'").append(fieldValue).append("')");
                        } else {
                            builder.append("'").append(fieldValue).append("',");
                        }
                        break;
                    }
                }
            }
            where.append(this.mTableName).append(" where ").append(this.idColumn).append(" in").append(builder.toString());
            Log.i(TAG, "DBImpl: deleteList: [3333333*******]=" + where.toString());
            mSQLiteDatabase.execSQL(where.toString());
            ids = null;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
            return ids;
        }
    }

    /**
     * 描述：按id删除.
     *
     * @param id the id
     */
    @Override
    protected long deleteAbs(String id) {
        long rows = -1;
        try {
            lock.lock();
            String where = this.idColumn + " = ?";
            String[] whereValue = {id};
            Log.d(TAG, "[delete]: delelte from " + this.mTableName + " where "
                    + where.replace("?", id));
            rows = mSQLiteDatabase.delete(this.mTableName, where, whereValue);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return rows;
    }

    /**
     * 描述：按id删除.
     *
     * @param ids the ids
     */
    @Override
    public long deleteAbs(int[] ids) {
        long rows = -1;
        try {
            lock.lock();
            if (ids.length > 0) {
                StringBuilder builder = new StringBuilder(this.idColumn);
                builder.append(" in (");
                for (int i = 0; i < ids.length; i++) {
                    builder.append(ids[i])
                            .append(",");
                }
                builder.deleteCharAt(builder.length() - 1);
                builder.append(")");
                Log.i(TAG, "DBImpl: delete: [ids]="
                        + builder.toString());
                rows = mSQLiteDatabase.delete(this.mTableName, builder.toString(), null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "DBImpl: delete: [ids]="
                    + e);
        } finally {
            lock.unlock();
        }
        return rows;
    }

    /**
     * @param ids 根据指定的ID来删除数据,该实体类必须制定ID
     * @return
     */
    @Override
    protected long deleteAbs(String[] ids) {
        long rows = -1;
        try {
            lock.lock();
            if (ids.length > 0) {
                StringBuilder builder = new StringBuilder(this.idColumn);
                builder.append(" in (");
                for (int i = 0; i < ids.length; i++) {
                    builder.append(ids[i])
                            .append(",");
                }
                builder.deleteCharAt(builder.length() - 1);
                builder.append(")");
                Log.i(TAG, "DBImpl: delete: [ids]="
                        + builder.toString());
                rows = mSQLiteDatabase.delete(this.mTableName, builder.toString(), null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "DBImpl: delete: [ids]="
                    + e);
        } finally {
            lock.unlock();
        }


        return rows;

    }


    /**
     * 描述：按条件删除数据
     */
    @Override
    protected long deleteAbs(String whereClause, String[] whereArgs) {
        long rows = -1;
        try {
            lock.lock();
            String mLogSql = getLogSql(whereClause, whereArgs);
            if (!AbStrUtil.isEmpty(mLogSql)) {
                mLogSql += " where ";
            }
            Log.d(TAG, "[delete]: delete from " + this.mTableName + mLogSql);
            rows = mSQLiteDatabase.delete(this.mTableName, whereClause, whereArgs);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return rows;
    }

    /**
     * 描述：清空数据
     */
    @Override
    protected long deleteAllAbs() {
        long rows = -1;
        try {
            lock.lock();
            Log.i(TAG, "DBImpl: deleteAll: [mTableName]=" + mTableName);
            rows = mSQLiteDatabase.delete(this.mTableName, null, null);
            //需要判断是否有关联表
            for (Field relationsDaoField : allFields) {
                if (!relationsDaoField.isAnnotationPresent(RelationDao.class)) {
                    continue;
                }
                //设置可访问
                relationsDaoField.setAccessible(true);
                String relationsDaoTableName = getRetationTable(relationsDaoField);
                Log.i(TAG, "DBImpl: deleteAll: [relationsDaoTableName=]=" + relationsDaoTableName);
                rows += mSQLiteDatabase.delete(relationsDaoTableName, null, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return rows;
    }

    String getRetationTable(Field childField) {
        //得到表名
        String tableName = "";

        String type = null;

        RelationDao relationDao = childField.getAnnotation(RelationDao.class);
        //关联类型
        type = relationDao.type();
        //设置可访问
        childField.setAccessible(true);

        //得到关联表的表名查询

        if (RelationsType.one2one.equals(type)) {
            //一对一关系
            //获取这个实体的表名
            if (childField.getType().isAnnotationPresent(Table.class)) {
                Table table = childField.getType().getAnnotation(Table.class);
                tableName = table.name();
            }


        } else if (RelationsType.one2many.equals(type) || RelationsType.many2many.equals(type)) {

            //得到泛型里的class类型对象
            Class listEntityClazz = null;
            Class<?> fieldClass = childField.getType();
            if (fieldClass.isAssignableFrom(List.class)) {
                Type fc = childField.getGenericType();
//                if (fc == null) continue;
                if (fc instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType) fc;
                    listEntityClazz = (Class) pt.getActualTypeArguments()[0];
                }
            }


            if (listEntityClazz.isAnnotationPresent(Table.class)) {
                Table table = (Table) listEntityClazz.getAnnotation(Table.class);
                tableName = table.name();
            }
        }

        return tableName;
    }

    protected long delete(String column, T entity) {
        long rows = -1;
        try {
            lock.lock();
            String where = column + " = ?";
//            for (Field field : allFields) {
//                Column column1 = field.getAnnotation(Column.class);
//                if (column1.name().equals(column)) {
//                    field.setAccessible(true);
//                    String str = (String) field.get(entity);
//                    String[] whereValue = {str};
//                    Log.i( TAG, "DBImpl: : [deleteOne0000= delete from " + mTableName
//                            + " where " + column + "= " + str);
//                    rows = mSQLiteDatabase.delete(this.mTableName, where, whereValue);
//                    break;
//                }
//            }


            //获取关联域的操作类型和关系类型
            String foreignKey = null;
            String type = null;
            //需要判断是否有关联表
            for (Field relationsDaoField : allFields) {
                Column column1 = relationsDaoField.getAnnotation(Column.class);
                if (column1.name().equals(column)) {
                    relationsDaoField.setAccessible(true);
                    String str = (String) relationsDaoField.get(entity);
                    String[] whereValue = {str};
                    Log.i(TAG, "DBImpl: : [deleteOne0000= delete from " + mTableName
                            + " where " + column + "= " + str);
                    rows = mSQLiteDatabase.delete(this.mTableName, where, whereValue);
//                    break;
                }

                if (!relationsDaoField.isAnnotationPresent(RelationDao.class)) {
                    continue;
                }

                RelationDao relationDao = relationsDaoField.getAnnotation(RelationDao.class);
                //获取外键列名
                foreignKey = relationDao.foreignKey();
                //关联类型
                type = relationDao.type();
                //设置可访问
                relationsDaoField.setAccessible(true);
                if (RelationsType.one2one.equals(type)) {
                    //一对一关系
                    //获取关联表的对象
                    relationsDaoField.setAccessible(true);
                    String relationsDaoTableName = "";
                    if (relationsDaoField.getType().isAnnotationPresent(Table.class)) {
                        Table table = relationsDaoField.getType().getAnnotation(Table.class);
                        relationsDaoTableName = table.name();
                    } else {
                        return rows;
                    }

                    String where1 = foreignKey + " = ?";
                    Field field = entity.getClass().getDeclaredField(relationDao.name());
                    field.setAccessible(true);
                    String str1 = (String) field.get(entity);
                    String[] whereValue1 = {str1};
                    Log.i(TAG, "DBImpl: : [deleteOne11111= delete from " + relationsDaoTableName
                            + " where " + foreignKey + "= " + str1);
                    rows = mSQLiteDatabase.delete(relationsDaoTableName, where1, whereValue1);


                } else if (RelationsType.one2many.equals(type) || RelationsType.many2many.equals(type)) {
                    //一对多关系
                    //获取关联表的对象
                    List<T> list = (List<T>) relationsDaoField.get(entity);

                    if (list != null && list.size() > 0) {
                        for (T relationsDaoEntity : list) {
                            String relationsDaoTableName = "";
                            if (relationsDaoEntity.getClass().isAnnotationPresent(Table.class)) {
                                Table table = relationsDaoEntity.getClass().getAnnotation(Table.class);
                                relationsDaoTableName = table.name();
                                String where1 = foreignKey + " = ?";
                                String str1 = (String) entity.getClass().getField(relationDao.name()).get(entity);
                                String[] whereValue1 = {str1};
                                Log.i(TAG, "DBImpl: : [deleteOne11111= delete from " + relationsDaoTableName
                                        + " where " + foreignKey + "= " + str1);
                                rows = mSQLiteDatabase.delete(relationsDaoTableName, where1, whereValue1);
                            }
                        }
                    }

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return rows;

    }

    /**
     * @param entity 根据主键删除单条数据.,该实体类必须制定ID
     * @return
     */
    @Override
    protected long deleteOneAbs(T entity) {
        return delete(this.idColumn, entity);
    }

    @Override
    protected long deleteOneByColumnAbs(String column, T entity) {
        return delete(column, entity);
    }

    /**
     * 描述：更新实体.
     *
     * @param entity the entity
     * @return the long
     */
    @Override
    protected long updateAbs(T entity) {
        long row = 0;
        try {
            lock.lock();
            ContentValues cv = new ContentValues();

            //注意返回的sql中包含主键列
            String sql = setContentValues(entity, cv, TYPE_NOT_INCREMENT, METHOD_UPDATE);

            String where = this.idColumn + " = ?";
            int id = Integer.parseInt(cv.get(this.idColumn).toString());
            //set sql中不能包含主键列
//            cv.remove(this.idColumn);

            Log.d(TAG, "[execSql]: execSql " + this.mTableName + " set " + sql
                    + " where " + where.replace("?", String.valueOf(id)));

            String[] whereValue = {Integer.toString(id)};
            row = mSQLiteDatabase.update(this.mTableName, cv, where, whereValue);


        } catch (Exception e) {
            Log.d(this.TAG, "[execSql] DB Exception.");
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return row;
    }

    @Override
    protected long updateByColumnAbs(String column, T entity) {
        return update(column, entity);
    }

//    /**
//     * 描述：更新实体.
//     *
//     * @param entity the entity
//     * @return the long
//     */
//    @Override
//    public long execSql(T entity) {
//        return execSql(this.idColumn, entity);
//    }

    private long update(String column, T entity) {
        long rows = -1;
//    String column="";
        try {
            lock.lock();
            ContentValues cv = new ContentValues();

            //注意返回的sql中包含主键列
            String sql = setContentValues(entity, cv, TYPE_NOT_INCREMENT, METHOD_UPDATE);
            String where = column + " = ?";
            //set sql中不能包含主键列
//            cv.remove(this.idColumn);
            String idValues = (String) cv.get(column);
            Log.i(TAG, "DBImpl: execSql: [8888888899999]=" + idValues);

            String[] whereValue = {idValues};
            rows = mSQLiteDatabase.update(this.mTableName, cv, where, whereValue);


            //获取关联域的操作类型和关系类型
            String foreignKey = null;
            String type = null;
            //需要判断是否有关联表
            for (Field relationsDaoField : allFields) {
                if (!relationsDaoField.isAnnotationPresent(RelationDao.class)) {
                    continue;
                }

                RelationDao relationDao = relationsDaoField.getAnnotation(RelationDao.class);
                //获取外键列名
                foreignKey = relationDao.foreignKey();
                //关联类型
                type = relationDao.type();
                //设置可访问
                relationsDaoField.setAccessible(true);
                if (RelationsType.one2one.equals(type)) {
                    //一对一关系
                    //获取关联表的对象
                    relationsDaoField.setAccessible(true);
                    String relationsDaoTableName = "";
                    if (relationsDaoField.getType().isAnnotationPresent(Table.class)) {
                        Table table = relationsDaoField.getType().getAnnotation(Table.class);
                        relationsDaoTableName = table.name();
                    } else {
                        return rows;
                    }

                    String where1 = foreignKey + " = ?";
                    ContentValues cv1 = new ContentValues();
                    Object obj = relationsDaoField.get(entity);
                    if (obj == null) {
                        return rows;
                    }
                    //注意返回的sql中包含主键列
                    String sql1 = setContentValues(obj, cv1, TYPE_NOT_INCREMENT, METHOD_UPDATE);
                    //set sql中不能包含主键列
                    String idValues1 = (String) cv1.get(foreignKey);

                    String[] whereValue11 = {idValues1};
                    Log.i(TAG, "DBImpl: execSql: " +
                            "[sql10000]=" + sql1 + "     idValues1=" + idValues1);
                    rows = mSQLiteDatabase.update(relationsDaoTableName, cv1, where1, whereValue11);


                } else if (RelationsType.one2many.equals(type) || RelationsType.many2many.equals(type)) {
                    //一对多关系
                    //获取关联表的对象
                    List<T> list = (List<T>) relationsDaoField.get(entity);

                    if (list != null && list.size() > 0) {
                        for (T relationsDaoEntity : list) {
                            String relationsDaoTableName = "";
                            if (relationsDaoEntity.getClass().isAnnotationPresent(Table.class)) {
                                Table table = relationsDaoEntity.getClass().getAnnotation(Table.class);
                                relationsDaoTableName = table.name();

                                String where1 = foreignKey + " = ?";
                                ContentValues cv1 = new ContentValues();
                                Object obj = relationsDaoField.get(entity);
                                if (obj == null) {
                                    return rows;
                                }
                                //注意返回的sql中包含主键列
                                String sql1 = setContentValues(obj, cv1, TYPE_NOT_INCREMENT, METHOD_UPDATE);
                                //set sql中不能包含主键列
                                String idValues1 = (String) cv1.get(foreignKey);

                                String[] whereValue11 = {idValues1};
                                Log.i(TAG, "DBImpl: execSql: " +
                                        "[sql10000]=" + sql1 + "     idValues1=" + idValues1);
                                rows = mSQLiteDatabase.update(relationsDaoTableName, cv1, where1, whereValue11);

                            }
                        }
                    }

                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return rows;
    }

    /**
     * @param entityList 数据列表,ID主键
     * @return
     */
    @Override
    protected long updateListAbs(List<T> entityList) {
        String sql = null;
        long rows = -1;
        try {
            lock.lock();
            for (T entity : entityList) {
                ContentValues cv = new ContentValues();

                sql = setContentValues(entity, cv, TYPE_NOT_INCREMENT,
                        METHOD_UPDATE);

                String where = this.idColumn + " = ?";
                int id = Integer.parseInt(cv.get(this.idColumn).toString());
                cv.remove(this.idColumn);

                Log.d(TAG, "[execSql]: execSql " + this.mTableName + " set " + sql
                        + " where " + where.replace("?", String.valueOf(id)));

                String[] whereValue = {Integer.toString(id)};
                rows = mSQLiteDatabase.update(this.mTableName, cv, where, whereValue);


                //获取关联域的操作类型和关系类型
                String foreignKey = null;
                String type = null;
                //需要判断是否有关联表
                for (Field relationsDaoField : allFields) {
                    if (!relationsDaoField.isAnnotationPresent(RelationDao.class)) {
                        continue;
                    }

                    RelationDao relationDao = relationsDaoField.getAnnotation(RelationDao.class);
                    //获取外键列名
                    foreignKey = relationDao.foreignKey();
                    //关联类型
                    type = relationDao.type();
                    //设置可访问
                    relationsDaoField.setAccessible(true);
                    if (RelationsType.one2one.equals(type)) {
                        //一对一关系
                        //获取关联表的对象
                        relationsDaoField.setAccessible(true);
                        String relationsDaoTableName = "";
                        if (relationsDaoField.getType().isAnnotationPresent(Table.class)) {
                            Table table = relationsDaoField.getType().getAnnotation(Table.class);
                            relationsDaoTableName = table.name();
                        } else {
                            return rows;
                        }

                        String where1 = foreignKey + " = ?";
                        ContentValues cv1 = new ContentValues();
                        Object obj = relationsDaoField.get(entity);
                        if (obj == null) {
                            return rows;
                        }
                        //注意返回的sql中包含主键列
                        String sql1 = setContentValues(obj, cv1, TYPE_NOT_INCREMENT, METHOD_UPDATE);
                        //set sql中不能包含主键列
                        String idValues1 = (String) cv1.get(foreignKey);

                        String[] whereValue11 = {idValues1};
                        Log.i(TAG, "DBImpl: execSql: " +
                                "[sql10000]=" + sql1 + "     idValues1=" + idValues1);
                        rows = mSQLiteDatabase.update(relationsDaoTableName, cv1, where1, whereValue11);


                    } else if (RelationsType.one2many.equals(type) || RelationsType.many2many.equals(type)) {
                        //一对多关系
                        //获取关联表的对象
                        List<T> list = (List<T>) relationsDaoField.get(entity);

                        if (list != null && list.size() > 0) {
                            for (T relationsDaoEntity : list) {
                                String relationsDaoTableName = "";
                                if (relationsDaoEntity.getClass().isAnnotationPresent(Table.class)) {
                                    Table table = relationsDaoEntity.getClass().getAnnotation(Table.class);
                                    relationsDaoTableName = table.name();

                                    String where1 = foreignKey + " = ?";
                                    ContentValues cv1 = new ContentValues();
                                    Object obj = relationsDaoField.get(entity);
                                    if (obj == null) {
                                        return rows;
                                    }
                                    //注意返回的sql中包含主键列
                                    String sql1 = setContentValues(obj, cv1, TYPE_NOT_INCREMENT, METHOD_UPDATE);
                                    //set sql中不能包含主键列
                                    String idValues1 = (String) cv1.get(foreignKey);

                                    String[] whereValue11 = {idValues1};
                                    Log.i(TAG, "DBImpl: execSql: " +
                                            "[sql10000]=" + sql1 + "     idValues1=" + idValues1);
                                    rows = mSQLiteDatabase.update(relationsDaoTableName, cv1, where1, whereValue11);

                                }
                            }
                        }

                    }
                }

            }
        } catch (Exception e) {
            Log.d(this.TAG, "[execSql] DB Exception.");
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

        return rows;
    }

//    /**
//     * 描述：更新列表
//     */
//    @Override
//    public long updateListIdDao(List<T> entityList) {
//        String sql = null;
//        long rows = -1;
//        try {
//            lock.lock();
//            for (T entity : entityList) {
//                ContentValues cv = new ContentValues();
//
//                sql = setContentValues(entity, cv, TYPE_NOT_INCREMENT,
//                        METHOD_UPDATE);
//
//                String where = this.idColumn + " = ?";
//                String idValues = (String) cv.get(this.idColumn);
////                int id = Integer.parseInt(cv.get(this.idColumn).toString());
//
////                cv.remove(this.idColumn);
//
//                Log.d(TAG, "[execSql]: execSql " + this.mTableName + " set " + sql
//                        + " where " + where.replace("?", idValues));
//
//                String[] whereValue = {idValues};
//                mSQLiteDatabase.execSql(this.mTableName, cv, where, whereValue);
//
//
//                //获取关联域的操作类型和关系类型
//                String foreignKey = null;
//                String type = null;
//                //需要判断是否有关联表
//                for (Field relationsDaoField : allFields) {
//                    if (!relationsDaoField.isAnnotationPresent(RelationDao.class)) {
//                        continue;
//                    }
//
//                    RelationDao relationDao = relationsDaoField.getAnnotation(RelationDao.class);
//                    //获取外键列名
//                    foreignKey = relationDao.foreignKey();
//                    //关联类型
//                    type = relationDao.type();
//                    //设置可访问
//                    relationsDaoField.setAccessible(true);
//                    if (RelationsType.one2one.equals(type)) {
//                        //一对一关系
//                        //获取关联表的对象
//                        relationsDaoField.setAccessible(true);
//                        String relationsDaoTableName = "";
//                        if (relationsDaoField.getType().isAnnotationPresent(Table.class)) {
//                            Table table = relationsDaoField.getType().getAnnotation(Table.class);
//                            relationsDaoTableName = table.name();
//                        } else {
//                            return rows;
//                        }
//
//                        String where1 = foreignKey + " = ?";
//                        ContentValues cv1 = new ContentValues();
//                        Object obj = relationsDaoField.get(entity);
//                        if (obj == null) {
//                            return rows;
//                        }
//                        //注意返回的sql中包含主键列
//                        String sql1 = setContentValues(obj, cv1, TYPE_NOT_INCREMENT, METHOD_UPDATE);
//                        //set sql中不能包含主键列
//                        String idValues1 = (String) cv1.get(foreignKey);
//
//                        String[] whereValue11 = {idValues1};
//                        Log.i( TAG, "DBImpl: execSql: " +
//                                "[sql10000]=" + sql1 + "     idValues1=" + idValues1);
//                        rows = mSQLiteDatabase.execSql(relationsDaoTableName, cv1, where1, whereValue11);
//
//
//                    } else if (RelationsType.one2many.equals(type) || RelationsType.many2many.equals(type)) {
//                        //一对多关系
//                        //获取关联表的对象
//                        List<T> list = (List<T>) relationsDaoField.get(entity);
//
//                        if (list != null && list.size() > 0) {
//                            for (T relationsDaoEntity : list) {
//                                String relationsDaoTableName = "";
//                                if (relationsDaoEntity.getClass().isAnnotationPresent(Table.class)) {
//                                    Table table = relationsDaoEntity.getClass().getAnnotation(Table.class);
//                                    relationsDaoTableName = table.name();
//
//                                    String where1 = foreignKey + " = ?";
//                                    ContentValues cv1 = new ContentValues();
//                                    Object obj = relationsDaoField.get(entity);
//                                    if (obj == null) {
//                                        return rows;
//                                    }
//                                    //注意返回的sql中包含主键列
//                                    String sql1 = setContentValues(obj, cv1, TYPE_NOT_INCREMENT, METHOD_UPDATE);
//                                    //set sql中不能包含主键列
//                                    String idValues1 = (String) cv1.get(foreignKey);
//
//                                    String[] whereValue11 = {idValues1};
//                                    Log.i( TAG, "DBImpl: execSql: " +
//                                            "[sql10000]=" + sql1 + "     idValues1=" + idValues1);
//                                    rows = mSQLiteDatabase.execSql(relationsDaoTableName, cv1, where1, whereValue11);
//
//                                }
//                            }
//                        }
//
//                    }
//                }
//
//            }
//        } catch (Exception e) {
//            Log.d(this.TAG, "[execSql] DB Exception.");
//            e.printStackTrace();
//        } finally {
//            lock.unlock();
//        }
//
//        return rows;
//    }

    /**
     * 设置这个ContentValues.
     *
     * @param entity 映射实体
     * @param cv     the cv
     * @param type   id类的类型，是否自增
     * @param method 预执行的操作
     * @return sql的字符串
     * @throws IllegalAccessException the illegal access exception
     */
    private String setContentValues(Object entity, ContentValues cv, int type,
                                    int method) throws IllegalAccessException {
        StringBuffer strField = new StringBuffer("(");
        StringBuffer strValue = new StringBuffer(" values(");
        StringBuffer strUpdate = new StringBuffer(" ");

        // 加载所有字段
        List<Field> allFields = AbTableHelper.joinFields(entity.getClass().getDeclaredFields(),
                entity.getClass().getSuperclass().getDeclaredFields());
        for (Field field : allFields) {
            if (!field.isAnnotationPresent(Column.class)) {
                continue;
            }
            Column column = field.getAnnotation(Column.class);

            field.setAccessible(true);
            Object fieldValue = field.get(entity);
            if (fieldValue == null)
                continue;
//            if ((type == TYPE_INCREMENT) && (field.isAnnotationPresent(Id.class))) {
//
//                continue;
//            }
            // 处理java.util.Date类型,execSql
            if (Date.class == field.getType()) {
                // 2012-06-10
                cv.put(column.name(), ((Date) fieldValue).getTime());
                continue;
            }
            String value = String.valueOf(fieldValue);
            cv.put(column.name(), value);
            if (method == METHOD_INSERT) {
                strField.append(column.name()).append(",");
                strValue.append("'").append(value).append("',");
            } else {
                strUpdate.append(column.name()).append("=").append("'").append(
                        value).append("',");
            }

        }
        if (method == METHOD_INSERT) {
            strField.deleteCharAt(strField.length() - 1).append(")");
            strValue.deleteCharAt(strValue.length() - 1).append(")");
            Log.i(TAG, "DBImpl: setContentValues: [inerttttttttt]="
                    + strField.toString() + strValue.toString());
            return strField.toString() + strValue.toString();
        } else {
            Log.i(TAG, "DBImpl: setContentValues: [inerttttttttt11]="
                    + strUpdate.deleteCharAt(strUpdate.length() - 1).append(" ").toString());
            return strUpdate.deleteCharAt(strUpdate.length() - 1).append(" ").toString();
        }
    }

    /**
     * 描述：查询为map列表.
     *
     * @param sql           the sql
     * @param selectionArgs the selection args
     * @return the list
     */
    @Override
    protected List<Map<String, String>> queryMapListAbs(String sql, String[] selectionArgs) {
        Cursor cursor = null;
        List<Map<String, String>> retList = new ArrayList<Map<String, String>>();
        try {
            lock.lock();
            Log.d(TAG, "[queryMapList]: " + getLogSql(sql, selectionArgs));
            cursor = mSQLiteDatabase.rawQuery(sql, selectionArgs);
            while (cursor.moveToNext()) {
                Map<String, String> map = new HashMap<String, String>();
                for (String columnName : cursor.getColumnNames()) {
                    int c = cursor.getColumnIndex(columnName);
                    if (c < 0) {
                        continue; // 如果不存在循环下个属性值
                    } else {
                        map.put(columnName.toLowerCase(), cursor.getString(c));
                    }
                }
                retList.add(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "[queryMapList] from DB exception");
        } finally {
            closeCursor(cursor);
            lock.unlock();
        }
        return retList;
    }


    /**
     * 描述：查询数量.
     *
     * @param sql           the sql
     * @param selectionArgs the selection args
     * @return the int
     */
    @Override
    protected int queryCountAbs(String sql, String[] selectionArgs) {
        Cursor cursor = null;
        int count = 0;
        try {
            lock.lock();
            Log.d(TAG, "[queryCount]: " + getLogSql(sql, selectionArgs));
            cursor = mSQLiteDatabase.query(this.mTableName, null, sql, selectionArgs, null, null, null);
            if (cursor != null) {
                count = cursor.getCount();
            }
        } catch (Exception e) {
            Log.e(TAG, "[queryCount] from DB exception");
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
            lock.unlock();
        }
        return count;
    }

    @Override
    protected int queryCountAbs() {
        Cursor cursor = null;
        int count = 0;
        try {
            lock.lock();


//                String sql = "SELECT COUNT(*) FROM " + DB_TABLE_PLACES;
            String sql = "select count(*) from " + this.mTableName;
            SQLiteStatement statement = mSQLiteDatabase.compileStatement(sql);
            count = (int) statement.simpleQueryForLong();
//            cursor = mSQLiteDatabase.queryRaw(sql, null);
//            if (cursor != null) {
//                count = cursor.getCount();
//            }
        } catch (Exception e) {
            Log.e(TAG, "[queryCount] from DB exception");
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
            lock.unlock();
        }
        return count;
    }

    /**
     * 描述：执行特定的sql.
     *
     * @param sql           the sql
     * @param selectionArgs the selection args
     */
    @Override
    public void execSqlAbs(String sql, Object[] selectionArgs) {
        try {
            lock.lock();

            if (selectionArgs == null) {
                mSQLiteDatabase.execSQL(sql);
            } else {
                mSQLiteDatabase.execSQL(sql, selectionArgs);
            }
            Log.d(TAG, "[execSql]: success" + getLogSql(sql, selectionArgs));
        } catch (Exception e) {
            Log.e(TAG, "[execSql] DB exception.");
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 描述：获取写数据库，数据操作前必须调用
     *
     * @param transaction 是否开启事务
     * @throws
     */
    protected void startWritableDatabase(boolean transaction) {
        try {
            mSQLiteOpenHelper.openDb();
            lock.lock();
            if (mSQLiteDatabase == null || !mSQLiteDatabase.isOpen() && !mSQLiteOpenHelper.isOpenDb()) {
//                this.mSQLiteOpenHelper.close();
                mSQLiteDatabase = this.mSQLiteOpenHelper.getWritableDatabase();
            }
//            if (mSQLiteDatabase != null && transaction) {
//                mSQLiteDatabase.beginTransaction();
//            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

    }

    /**
     * 描述：获取读数据库，数据操作前必须调用
     *
     * @param transaction 是否开启事务
     * @throws
     */
    protected synchronized void startReadableDatabase(boolean transaction) {
        try {
            mSQLiteOpenHelper.openDb();
            lock.lock();
            if (mSQLiteDatabase == null || !mSQLiteDatabase.isOpen()) {
                mSQLiteDatabase = this.mSQLiteOpenHelper.getReadableDatabase();
            }

//            if (mSQLiteDatabase != null && transaction) {
//                mSQLiteDatabase.beginTransaction();
//            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "DBImpl: startReadableDatabase: [transaction]="
                    + e);
        } finally {
            lock.unlock();
        }

    }


    /**
     * 描述：操作完成后设置事务成功后才能调用closeDatabase(true);
     *
     * @throws
     */
    protected void setTransactionSuccessful() {
//        try {
//            if (mSQLiteDatabase != null) {
//                mSQLiteDatabase.setTransactionSuccessful();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            Log.i( TAG, "DBImpl: setTransactionSuccessful: []="
//                    + e);
//        }
    }

    /**
     * 描述：关闭数据库，数据操作后必须调用
     *
     * @param transaction 关闭事务
     * @throws
     */
    protected void closeDatabase(boolean transaction) {
        try {
            if (mSQLiteOpenHelper.canCloseDb()) {
                if (mSQLiteDatabase != null) {
                    Log.i(TAG, "DBImpl: closeDatabase: [ddddddd]="
                            + mSQLiteDatabase.isOpen() + "   " + mSQLiteOpenHelper.isOpenDb());
//                    if (transaction) {
//                        mSQLiteDatabase.endTransaction();
//                    }
                    if (mSQLiteDatabase.isOpen()) {
                        mSQLiteDatabase.close();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "DBImpl: closeDatabase: [transaction]="
                    + e);
        }
    }


    /**
     * 打印当前sql语句.
     *
     * @param sql  sql语句，带？
     * @param args 绑定变量
     * @return 完整的sql
     */
    private String getLogSql(String sql, Object[] args) {
        if (args == null || args.length == 0) {
            return sql;
        }
        for (int i = 0; i < args.length; i++) {
            sql = sql.replaceFirst("\\?", "'" + String.valueOf(args[i]) + "'");
        }
        return sql;
    }
}
