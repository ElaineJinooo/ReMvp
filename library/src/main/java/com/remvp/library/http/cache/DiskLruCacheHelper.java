package com.remvp.library.http.cache;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import okhttp3.internal.cache.DiskLruCache;
import okhttp3.internal.io.FileSystem;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.ByteString;
import okio.Okio;
import okio.Source;

/**
 * 缓存文件存储帮助类
 * 使用{@link DiskLruCache}算法
 * 注：如果要使用的话一定要调{@link #init(File, long)}否则不能保存
 */
public class DiskLruCacheHelper {
    private static final String DIR_NAME = "diskCache";
    private static final int MAX_COUNT = 5 * 1024 * 1024;
    private static final int DEFAULT_APP_VERSION = 1;

    /**
     * The default valueCount when open DiskLruCache.
     */
    private static final int DEFAULT_VALUE_COUNT = 1;

    private static final String TAG = "DiskLruCacheHelper";

    private DiskLruCache mDiskLruCache;

    /**
     * 初始化
     *
     * @param directory 文件路径
     * @param maxSize   大小
     */
    public void init(File directory, long maxSize) {
        mDiskLruCache = DiskLruCache.create(FileSystem.SYSTEM,
                directory, DEFAULT_APP_VERSION, DEFAULT_VALUE_COUNT, maxSize);
    }

    private static class DiskLruCacheHelperInstance {
        private final static DiskLruCacheHelper instance = new DiskLruCacheHelper();
    }

    public static DiskLruCacheHelper getInstance() {
        return DiskLruCacheHelperInstance.instance;
    }

    public boolean isInit() {
        return mDiskLruCache != null;
    }

    // =======================================
    // ============== String 数据 读写 =============
    // =======================================

    /**
     * 保存 String数据 到 缓存中
     *
     * @param key   保存的key
     * @param value 保存的数据
     */
    public void put(String key, String value) {
        DiskLruCache.Editor edit = null;
        BufferedSink sink = null;
        try {
            edit = editor(key);
            if (edit == null) return;
            sink = Okio.buffer(edit.newSink(0));
            sink.writeUtf8(value);
            sink.flush();
            edit.commit();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                //s
                edit.abort();//write REMOVE
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } finally {
            try {
                if (sink != null)
                    sink.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取String数据
     *
     * @param key 获取的key
     * @return
     */
    public String getAsString(String key) {
        BufferedSource bufferedSource = null;
        Source source = null;
        source = get(key);
        if (source == null) return null;
        String str = null;
        try {
            bufferedSource = Okio.buffer(source);
            str = bufferedSource.readUtf8();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedSource != null)
                    bufferedSource.close();
                source.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return str;
    }


//    public void put(String key, JSONObject jsonObject) {
//        put(key, jsonObject.toString());
//    }
//
//    public JSONObject getAsJson(String key) {
//        String val = getAsString(key);
//        try {
//            if (val != null)
//                return new JSONObject(val);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    // =======================================
//    // ============ JSONArray 数据 读写 =============
//    // =======================================
//
//    public void put(String key, JSONArray jsonArray) {
//        put(key, jsonArray.toString());
//    }
//
//    public JSONArray getAsJSONArray(String key) {
//        String JSONString = getAsString(key);
//        try {
//            JSONArray obj = new JSONArray(JSONString);
//            return obj;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

    // =======================================
    // ============== byte 数据 读写 =============
    // =======================================

    /**
     * 保存 byte数据 到 缓存中
     *
     * @param key   保存的key
     * @param value 保存的数据
     */
    public void put(String key, byte[] value) {
        DiskLruCache.Editor editor = null;
        BufferedSink sink = null;
        try {
            editor = editor(key);
            if (editor == null) return;
            sink = Okio.buffer(editor.newSink(0));
            sink.write(value);
            sink.flush();
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                editor.abort();//write REMOVE
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        } finally {
            try {
                if (sink != null)
                    sink.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取byte[]数据
     *
     * @param key 获取的key
     * @return
     */
    public byte[] getAsBytes(String key) {
        byte[] res = new byte[256];
        Source source = null;
        BufferedSource bufferedSource = null;
        source = get(key);
        if (source == null) return null;
        try {
            bufferedSource = Okio.buffer(source);
            bufferedSource.read(res);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedSource != null)
                    bufferedSource.close();
                source.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return res;
    }

    // =======================================
    // ============== 序列化 数据 读写 =============
    // =======================================

    /**
     * 保存 序列化数据 到 缓存中
     *
     * @param key   保存的key
     * @param value 保存的数据
     */
    public void put(String key, Serializable value) {
        DiskLruCache.Editor editor = editor(key);
        BufferedSink sink = null;
        ObjectOutputStream oos = null;
        ByteArrayOutputStream bos = null;
        sink = Okio.buffer(editor.newSink(0));
        if (editor == null) return;
        try {
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeObject(value);
            oos.flush();
            sink.write(bos.toByteArray());
            sink.flush();
            editor.commit();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                editor.abort();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } finally {
            try {
                if (oos != null)
                    oos.close();
                if (bos != null)
                    bos.close();
                sink.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取序列化数据
     *
     * @param key 获取的key
     * @param <T>
     * @return
     */
    public <T> T getAsSerializable(String key) {
        T t = null;
        Source source = get(key);
        BufferedSource bufferedSource = null;
        ObjectInputStream ois = null;
        if (source == null) return null;
        try {
            bufferedSource = Okio.buffer(source);
            ois = new ObjectInputStream(bufferedSource.inputStream());
            t = (T) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (ois != null)
                    ois.close();
                if (bufferedSource != null)
                    bufferedSource.close();
                source.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return t;
    }

//    // =======================================
//    // ============== bitmap 数据 读写 =============
//    // =======================================
//    public void put(String key, Bitmap bitmap) {
//        put(key, ImageUtils.bitmap2Bytes(bitmap));
//    }
//
//    public Bitmap getAsBitmap(String key) {
//        byte[] bytes = getAsBytes(key);
//        if (bytes == null) return null;
//        return ImageUtils.bytes2Bitmap(bytes);
//    }
//
//    // =======================================
//    // ============= drawable 数据 读写 =============
//    // =======================================
//    public void put(String key, Drawable value) {
//        put(key, ImageUtils.drawable2Bitmap(value));
//    }
//
//    public Drawable getAsDrawable(String key) {
//        byte[] bytes = getAsBytes(key);
//        if (bytes == null) {
//            return null;
//        }
//        return ImageUtils.bitmap2Drawable(ImageUtils.bytes2Bitmap(bytes));
//    }
    // =======================================
    // ============= other methods =============
    // =======================================

    /**
     * 移除数据
     *
     * @param key 移除的key
     * @return
     */
    public boolean remove(String key) {
        try {
            key = ByteString.encodeUtf8(key).md5().hex();
            return mDiskLruCache.remove(key);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void close() throws IOException {
        mDiskLruCache.close();
    }

    public void delete() throws IOException {
        mDiskLruCache.delete();
    }

    public void flush() throws IOException {
        mDiskLruCache.flush();
    }

    public boolean isClosed() {
        return mDiskLruCache.isClosed();
    }

    public long size() {
        try {
            return mDiskLruCache.size();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void setMaxSize(long maxSize) {
        mDiskLruCache.setMaxSize(maxSize);
    }

    public File getDirectory() {
        return mDiskLruCache.getDirectory();
    }

    public long getMaxSize() {
        return mDiskLruCache.getMaxSize();
    }


    // =======================================
    // ===遇到文件比较大的，可以直接通过流读写 =====
    // =======================================
    //basic editor
    public DiskLruCache.Editor editor(String key) {
        try {
            key = key(key);
            //wirte DIRTY
            DiskLruCache.Editor edit = mDiskLruCache.edit(key);
            //edit maybe null :the entry is editing
            if (edit == null) {
                Log.w(TAG, "the entry spcified key:" + key + " is editing by other . ");
            }
            return edit;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    //basic get
    public Source get(String key) {
        try {
            DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key(key));
            if (snapshot == null) //not find entry , or entry.readable = false
            {
                Log.e(TAG, "not find entry , or entry.readable = false");
                return null;
            }
            //write READ
            return snapshot.getSource(0);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    public DiskLruCache.Snapshot getSnapshot(String key) {
        try {
            DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key(key));
            if (snapshot == null) //not find entry , or entry.readable = false
            {
                Log.e(TAG, "not find entry , or entry.readable = false");
                return null;
            }
            //write READ
            return snapshot;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    public boolean doContainsKey(String key) {
        if (mDiskLruCache == null) {
            return false;
        }
        try {
            return mDiskLruCache.get(key(key)) != null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 缓存是否过期
     *
     * @param key       数据的key
     * @param existTime 保存时间
     * @return true过期
     */
    public boolean isExpiry(String key, long existTime) {
        if (mDiskLruCache == null) {
            return false;
        }
        if (existTime > -1) {//-1表示永久性存储 不用进行过期校验
            //为什么这么写，请了解DiskLruCache，看它的源码
            File file = new File(getDirectory(), key(key) + "." + 0);
            if (isCacheDataFailure(file, existTime)) {//没有获取到缓存,或者缓存已经过期!
                return true;
            }
        }
        return false;
    }

    /**
     * 判断文件是否已经失效
     *
     * @param dataFile 文件
     * @param time     保存时间
     */
    private boolean isCacheDataFailure(File dataFile, long time) {
        if (!dataFile.exists()) {
            return false;
        }
        long existTime = System.currentTimeMillis() - dataFile.lastModified();
        return existTime > time;
    }


    // =======================================
    // ============== 序列化 数据 读写 =============
    // =======================================

    private File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }

    /**
     * 将key编码
     *
     * @param string key
     * @return
     */
    public String key(String string) {
        return ByteString.encodeUtf8(string).md5().hex();
    }

}
