package com.remvp.library.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.remvp.library.R;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;

public class FileUtil {
    private static final String TAG = "FileUtil";

    /**
     * Uri to the absolute file path (String)
     *
     * @param context
     * @param uri
     * @return the file path or null
     */
    public static String getRealFilePath(final Context context, final Uri uri) {
        if (null == uri)
            return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri,
                    new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    public static final int SIZETYPE_B = 1;// 获取文件大小单位为B的double值
    public static final int SIZETYPE_KB = 2;// 获取文件大小单位为KB的double值
    public static final int SIZETYPE_MB = 3;// 获取文件大小单位为MB的double值
    public static final int SIZETYPE_GB = 4;// 获取文件大小单位为GB的double值

    /**
     * 获取文件指定文件的指定单位的大小
     *
     * @param filePath 文件路径
     * @return double值的大小
     */
    public static String getFileOrFilesSize(String filePath) {
        File file = new File(filePath);
        long blockSize = 0;
        try {
            if (file.isDirectory()) {
                blockSize = getFileSizes(file);
            } else {
                blockSize = getFileSize(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("获取文件大小", "获取失败!");
        }
        double fileSize = formatFileSize(blockSize, 1);
        String size = "";
        if (fileSize > 1024) {
            double f = formatFileSize(blockSize, 2);
            if (f > 1024) {
                double size2 = formatFileSize(blockSize, 3);
                if (size2 > 1024) {
                    double size3 = formatFileSize(blockSize, 4);
                    size = size3 + "G";
                } else {
                    size = size2 + "M";
                }

            } else {
                size = f + "KB";
            }
        } else {
            size = fileSize + "B";
        }

        return size;
    }

    /**
     * 调用此方法自动计算指定文件或指定文件夹的大小
     *
     * @param filePath 文件路径
     * @return 计算好的带B、KB、MB、GB的字符串
     */
    public String getAutoFileOrFilesSize(String filePath) {
        File file = new File(filePath);
        long blockSize = 0;
        try {
            if (file.isDirectory()) {
                blockSize = getFileSizes(file);
            } else {
                blockSize = getFileSize(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("获取文件大小", "获取失败!");
        }
        return formatFileSize(blockSize);
    }

    /**
     * 获取指定文件大小
     *
     * @return
     * @throws Exception
     */
    private static long getFileSize(File file) throws Exception {
        long size = 0;
        if (file.exists()) {
            FileInputStream fis = null;
            fis = new FileInputStream(file);
            size = fis.available();
        } else {
            file.createNewFile();
            Log.e("获取文件大小", "文件不存在!");
        }
        return size;
    }

    /**
     * 获取指定文件夹
     *
     * @param f
     * @return
     * @throws Exception
     */
    private static long getFileSizes(File f) throws Exception {
        long size = 0;
        File flist[] = f.listFiles();
        for (int i = 0; i < flist.length; i++) {
            if (flist[i].isDirectory()) {
                size = size + getFileSizes(flist[i]);
            } else {
                size = size + getFileSize(flist[i]);
            }
        }
        return size;
    }

    /**
     * 转换文件大小
     *
     * @param fileS
     * @return
     */
    private static String formatFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";
        if (fileS == 0) {
            return wrongSize;
        }
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
        }
        return fileSizeString;
    }

    /**
     * 转换文件大小,指定转换的类型
     *
     * @param fileS
     * @param sizeType
     * @return
     */
    public static double formatFileSize(long fileS, int sizeType) {
        DecimalFormat df = new DecimalFormat("#.00");
        double fileSizeLong = 0;
        switch (sizeType) {
            case SIZETYPE_B:
                fileSizeLong = Double.valueOf(df.format((double) fileS));
                break;
            case SIZETYPE_KB:
                fileSizeLong = Double.valueOf(df.format((double) fileS / 1024));
                break;
            case SIZETYPE_MB:
                fileSizeLong = Double.valueOf(df.format((double) fileS / 1048576));
                break;
            case SIZETYPE_GB:
                fileSizeLong = Double.valueOf(df
                        .format((double) fileS / 1073741824));
                break;
            default:
                break;
        }
        return fileSizeLong;
    }

    /**
     * 复制单个文件
     *
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    public void copyFile(String oldPath, String newPath) {
        File oldfile = new File(oldPath);
        if (oldfile.exists()) { // 文件存在时
            InputStream inStream = null;
            FileOutputStream fs = null;
            // 读入原文件
            try {
                inStream = new FileInputStream(oldPath);
                fs = new FileOutputStream(newPath);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.e(TAG, "【FileUtil.copyFile()】【e=" + e + "】");
            }
            try {
                byte[] buffer = new byte[1444];
                int bytesum = 0;
                int byteread = 0;
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; // 字节数 文件大小
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
                fs.close();
            } catch (Exception e) {
                Log.e(TAG, "【FileUtil.copyFile()】【e=" + e + "】");
                e.printStackTrace();
            }
        }
    }

    public String write(Context context, String filename, String content)
            throws IOException {
        if (!filename.endsWith(".txt")) {
            filename = filename + ".txt";
        }
        FileOutputStream fos = context.openFileOutput(filename,
                Context.MODE_PRIVATE + Context.MODE_APPEND
                        + Context.MODE_WORLD_READABLE
                        + Context.MODE_WORLD_WRITEABLE);
        fos.write(content.getBytes());
        fos.flush();
        fos.close();
        return context.getFilesDir().getPath() + "/" + filename;
    }

    /**
     * 递归删除文件和文件夹
     *
     * @param file 要删除的根目录
     */
    public void deleteFile(File file) {
        if (file == null || !file.exists()) {
            Log.i(TAG, "【FileUtil.deleteFile()】【 info=w文件不存在】");
            return;
        }
        if (file.isFile()) {
            boolean isdelete = file.delete();
            Log.i(TAG, "【FileUtil.RecursionDeleteFile()】【isdelete="
                    + isdelete + "】");
            return;
        }
        if (file.isDirectory()) {
            File[] childFile = file.listFiles();
            if (childFile == null || childFile.length == 0) {
                boolean isdelete = file.delete();
                Log.i(TAG, "【FileUtil.isDirectory()】【isdelete="
                        + isdelete + "】");
                return;
            }
            for (File f : childFile) {
                deleteFile(f);
            }
            boolean delete = file.delete();
            Log.i(TAG, "【FileUtil.RecursionDeleteFile()】【isdelete="
                    + delete + "】");
        }
    }

    public void deleteFile(final String path) {
        Log.i( TAG, "【FileUtil.deleteFile()】【 info=info】" + path);
        try {
            File dir = new File(path);
            boolean isFile = dir.isFile();
            Log.i( TAG, "【FileUtil.deleteFile()】【file=" + isFile
                    + "】");
            if (isFile) {
                boolean isdelete = dir.delete();
                Log.i( TAG, "【FileUtil.deleteFile()】【isdelete="
                        + isdelete + "】");
            } else {
                File[] fs = dir.listFiles();
                if (fs != null) {
                    final int size = fs.length;
                    for (int i = 0; i < size; i++) {
                        boolean isdelete = fs[i].delete();
                        Log.i( TAG,
                                "【FileUtil.deleteFile()】【isdelete=" + isdelete
                                        + "】");
                    }
                }
                boolean isdeletedir = dir.delete();
                Log.i( TAG, "【FileUtil.deleteFile()】【isdeletedir="
                        + isdeletedir + "】");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e( TAG, "【FileUtil.deleteFile()】【 info=e】" + e);
        }
    }

    public byte[] file2byte(String filePath) {
        byte[] buffer = null;
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }

    public void byte2File(byte[] buf, String filePath, String fileName) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            File dir = new File(filePath);
            if (!dir.exists() && dir.isDirectory()) {
                dir.mkdirs();
            }
            file = new File(filePath + File.separator + fileName);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(buf);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * @param context
     * @return
     */
    public String getPhotoPath(Context context) {
        String error_report_dir = Environment.getExternalStorageDirectory()
                .getAbsolutePath()
                + File.separator
                + context.getString(R.string.app_name) + File.separator;

        return error_report_dir;
    }
}
