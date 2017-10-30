package com.remvp.library.db.orm.dao;

import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.remvp.library.db.orm.DBHelper;
import com.remvp.library.db.orm.SqlColumn;

import java.util.List;
import java.util.Map;

/**
 * 新增了无锁增删改
 * {@link #insertNoLock(Object)}
 * {@link #deleteOneByColumnNoLock(String, Object)}
 * {@link #updateByColumnNoLock(String, Object)}
 * <p>
 * 新增了根据某一列，而非主键进行更新
 * {@link #updateByColumn(String, Object)}
 */
public abstract class BaseDao<T> extends DBImpl<T> {
    private final String TAG = "BaseDao";
    public byte[] lock = new byte[0];

    public BaseDao(DBHelper dbHelper, Class<T> clazz) {
        super(dbHelper, clazz);
    }

    @Override
    public SQLiteOpenHelper getSQLiteOpenHelper() {
        return super.getSQLiteOpenHelper();
    }

    public T queryOne(int id) {
        synchronized (this.lock) {
            startReadableDatabase(true);
            T result = super.queryOneAbs(id);
            closeDatabase(true);
            return result;
        }
    }

    /**
     * 根据id查询某一条数据
     *
     * @param id
     * @return
     */
    public T queryOne(String id) {
        synchronized (this.lock) {
            startReadableDatabase(true);
            T result = super.queryOneAbs(id);
            closeDatabase(true);
            return result;
        }
    }

    /**
     * 根据某一列查询数据
     *
     * @param column
     * @param data
     * @return
     */
    public T queryOne(String column, String data) {
        synchronized (this.lock) {
            startReadableDatabase(true);
            T result = super.queryOneAbs(column, data);
            closeDatabase(true);
            return result;
        }
    }

    /**
     * 灵活使用sql语句进行查询
     *
     * @param sql
     * @param selectionArgs
     * @param clazz
     * @return
     */
    public List<T> queryRaw(String sql, String[] selectionArgs, Class<T> clazz) {
        synchronized (this.lock) {
            startReadableDatabase(true);
            List<T> result = super.queryRawAbs(sql, selectionArgs, clazz);
            closeDatabase(true);
            return result;
        }
    }

    /**
     * @param sql
     * @param selectionArgs
     * @return
     */
    public List<T> queryRaw(SqlColumn<T> sql, String[] selectionArgs) {
        synchronized (this.lock) {
            startReadableDatabase(true);
            List<T> result = super.queryRawAbs(sql.getSql(), selectionArgs, sql.getClazz());
            closeDatabase(true);
            return result;
        }
    }


    public boolean isExist(String sql, String[] selectionArgs) {
        synchronized (this.lock) {
            startReadableDatabase(true);
            boolean result = super.isExistAbs(sql, selectionArgs);
            closeDatabase(true);
            return result;
        }
    }

    /**
     * 查找所有信息
     *
     * @return
     */
    public List<T> queryList() {
        synchronized (this.lock) {
            startReadableDatabase(true);
            List<T> result = super.queryListAbs();
            closeDatabase(true);
            return result;
        }
    }

    /**
     * 分页查询
     *
     * @param page
     * @param pageSize
     * @return
     */
    public List<T> queryList(int page, int pageSize) {
        synchronized (this.lock) {
            startReadableDatabase(true);
            List<T> result = super.queryListAbs(page, pageSize);
            closeDatabase(true);
            return result;
        }
    }

    /**
     * 根据自定义sql语句查询
     *
     * @param sql
     * @param selectionArgs
     */
    public void execSql(String sql, String[] selectionArgs) {
        synchronized (this.lock) {
            startWritableDatabase(true);
            super.execSqlAbs(sql, selectionArgs);
            closeDatabase(true);
        }

    }

    /**
     * 查询某些列，并追加限制
     *
     * @param columns
     * @param selection
     * @param selectionArgs
     * @param groupBy
     * @param having
     * @param orderBy
     * @param limit
     * @return
     */
    public List<T> queryList(String[] columns, String selection,
                             String[] selectionArgs, String groupBy, String having,
                             String orderBy, String limit) {
        synchronized (this.lock) {
            startReadableDatabase(true);
            List<T> result = super.queryListAbs(columns, selection, selectionArgs,
                    groupBy, having, orderBy, limit);
            closeDatabase(true);
            return result;
        }
    }

    public List<T> queryList(String selection, String[] selectionArgs) {
        synchronized (this.lock) {
            startReadableDatabase(true);
            List<T> result = super.queryListAbs(selection, selectionArgs);
            closeDatabase(true);
            return result;
        }
    }

    /**
     * 根据实体类插入一条数据
     *
     * @param entity
     * @return
     */
    public long insert(T entity) {
        synchronized (this.lock) {
            startWritableDatabase(true);
            long result = super.insertAbs(entity);
            setTransactionSuccessful();
            closeDatabase(true);
            return result;
        }
    }

    /**
     * 复杂逻辑多次调用数据库，避免多次连接，省时
     */
    public void synchronizedMethod() {
        startWritableDatabase(true);
    }

    /**
     * 复杂逻辑多次调用数据库，避免多次连接，省时
     */
    public void unSynchronizedMethod() {
        setTransactionSuccessful();
        closeDatabase(true);
    }

    /**
     * 手动对数据库进行上锁，
     * 则需手动调用{@link #synchronizedMethod}
     * 根据某一列，而非主键进行更新
     * 复杂逻辑多次调用数据库，避免多次连接，省时
     *
     * @param column
     * @param entity
     * @return
     */
    public long updateByColumnNoLock(String column, T entity) {
        synchronized (this.lock) {
            long result = super.updateByColumnAbs(column, entity);
            return result;
        }
    }

    /**
     * 手动对数据库进行上锁，
     * 则需手动调用{@link #synchronizedMethod}
     * 根据其他列删除数据
     * 复杂逻辑多次调用数据库，避免多次连接，省时
     *
     * @param column
     * @param entity
     * @return
     */
    public long deleteOneByColumnNoLock(String column, T entity) {
        synchronized (this.lock) {
            long result = super.deleteOneByColumnAbs(column, entity);
            return result;
        }
    }

    /**
     * @param id
     * @return
     */
    public T queryOneNoLock(String id) {
        synchronized (this.lock) {
            T result = super.queryOneAbs(id);
            return result;
        }
    }

    /**
     * 手动对数据库进行上锁，
     * 则需手动调用{@link #synchronizedMethod}
     * 复杂逻辑多次调用数据库，避免多次连接，省时
     *
     * @param entity
     * @return
     */
    public long insertNoLock(T entity) {
        synchronized (this.lock) {

            long result = super.insertAbs(entity);

            return result;
        }
    }

    public long insert(T entity, boolean flag) {
        synchronized (this.lock) {
            startWritableDatabase(true);
            long result = super.insertAbs(entity, flag);
            setTransactionSuccessful();
            closeDatabase(true);
            return result;
        }
    }

    public long insertList(List<T> entityList) {
        synchronized (this.lock) {
            startWritableDatabase(true);
            long result = super.insertListAbs(entityList);
            setTransactionSuccessful();
            closeDatabase(true);
            return result;
        }
    }

    public long insertListNoTransaction(List<T> entityList) {
        synchronized (this.lock) {
            startWritableDatabase(false);
            long result = super.insertListAbs(entityList);
//            setTransactionSuccessful();
            closeDatabase(false);
            return result;
        }
    }

    public long insertList(List<T> entityList, boolean flag) {
        synchronized (this.lock) {
            startWritableDatabase(true);
            long result = super.insertListAbs(entityList, flag);
            setTransactionSuccessful();
            closeDatabase(true);
            return result;
        }
    }

    public long delete(int id) {
        synchronized (this.lock) {
            startWritableDatabase(true);
            long result = super.deleteAbs(id);
            setTransactionSuccessful();
            closeDatabase(true);
            return result;
        }
    }

    public long delete(String id) {
        synchronized (this.lock) {
            startWritableDatabase(true);
            long result = super.deleteAbs(id);
            setTransactionSuccessful();
            closeDatabase(true);
            return result;
        }
    }

    /**
     * 删除集合
     *
     * @param ids
     * @return
     */
    public List<T> deleteList(List<T> ids) {
        synchronized (this.lock) {
            startWritableDatabase(true);
            List<T> result = super.deleteListAbs(ids);
            setTransactionSuccessful();
            closeDatabase(true);
            return result;
        }
    }


    public long delete(int[] ids) {
        synchronized (this.lock) {
            startWritableDatabase(true);
            long result = super.deleteAbs(ids);
            setTransactionSuccessful();
            closeDatabase(true);
            return result;
        }
    }

    /**
     * @param whereArgs
     * @return
     */
    public long delete(String[] whereArgs) {
        synchronized (this.lock) {
            startWritableDatabase(true);
            long result = super.deleteAbs(whereArgs);
            setTransactionSuccessful();
            closeDatabase(true);
            return result;
        }
    }


    /**
     * @return
     */
    public long deleteAll() {
        synchronized (this.lock) {
            startWritableDatabase(true);
            long result = super.deleteAllAbs();
            setTransactionSuccessful();
            closeDatabase(true);
            return result;
        }
    }

    /**
     * @param data
     * @return
     */
    public long deleteOne(T data) {
        synchronized (this.lock) {
            startWritableDatabase(true);
            long result = super.deleteOneAbs(data);
            setTransactionSuccessful();
            closeDatabase(true);
            return result;
        }
    }

    /**
     * 根据其他列删除数据
     *
     * @param column
     * @param entity
     * @return
     */
    public long deleteOneByColumn(String column, T entity) {
        synchronized (this.lock) {
            startWritableDatabase(true);
            long result = super.deleteOneByColumnAbs(column, entity);
            setTransactionSuccessful();
            closeDatabase(true);
            return result;
        }
    }


    /**
     * 根据实体类删除一条数据，该实体类必须制定id
     *
     * @param entity
     * @return
     */
    public long update(T entity) {
        synchronized (this.lock) {
            startWritableDatabase(true);
            long result = super.updateAbs(entity);
            setTransactionSuccessful();
            closeDatabase(true);
            return result;
        }
    }

    /**
     * @param entityList 数据列表,ID主键
     * @return
     */
    public long updateList(List<T> entityList) {
        synchronized (this.lock) {
            startWritableDatabase(true);
            long result = super.updateListAbs(entityList);
            setTransactionSuccessful();
            closeDatabase(true);
            return result;
        }
    }


    /**
     * 根据某一列，而非主键进行更新
     *
     * @param column
     * @param entity
     * @return
     */
    public long updateByColumn(String column, T entity) {
        synchronized (this.lock) {
            startWritableDatabase(true);
            long result = super.updateByColumnAbs(column, entity);
            setTransactionSuccessful();
            closeDatabase(true);
            return result;
        }
    }

    public List<Map<String, String>> queryMapList(String sql,
                                                  String[] selectionArgs) {
        synchronized (this.lock) {
            startReadableDatabase(true);
            List<Map<String, String>> result = super.queryMapListAbs(sql,
                    selectionArgs);
            closeDatabase(true);
            return result;
        }
    }

    /**
     * 查询某范围共有多少行
     *
     * @param where         要查询的信息
     * @param selectionArgs 空缺值
     * @return
     */
    public int queryCount(String where, String[] selectionArgs) {
        synchronized (this.lock) {
            startReadableDatabase(true);
            int result = super.queryCountAbs(where, selectionArgs);
            closeDatabase(true);
            return result;
        }
    }

    /**
     * 查询该表共有多少行
     *
     * @return
     */
    public int queryCount() {
        synchronized (this.lock) {
            startReadableDatabase(true);
            int result = super.queryCountAbs();
            closeDatabase(true);
            return result;
        }
    }

    /**
     * 执行sql语句
     *
     * @param sql           the sql
     * @param selectionArgs the selection args
     */
    public void execSql(String sql, Object[] selectionArgs) {
        synchronized (this.lock) {
            startWritableDatabase(true);
            super.execSqlAbs(sql, selectionArgs);
            setTransactionSuccessful();
            closeDatabase(true);
        }
    }

    @Override
    public void startWritableDatabase(boolean transaction) {
        super.startWritableDatabase(transaction);
        Log.i(TAG, "BaseDao: startWritableDatabase: [wwwwwww1111]="
                + isLocked() + "   " + isOpen());
    }

    @Override
    public void startReadableDatabase(boolean transaction) {
        super.startReadableDatabase(transaction);
        Log.i(TAG, "BaseDao: startReadableDatabase: [rrrrrrrr111111]="
                + isLocked() + "   " + isOpen());
    }

    @Override
    public void setTransactionSuccessful() {
        super.setTransactionSuccessful();
    }

    @Override
    public void closeDatabase(boolean transaction) {
        super.closeDatabase(transaction);
    }

}
