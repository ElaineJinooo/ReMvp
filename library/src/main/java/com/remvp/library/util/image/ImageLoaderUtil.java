package com.remvp.library.util.image;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.widget.ImageView;

/**
 * 图片加载帮助类
 */
public class ImageLoaderUtil {
    private ILoader iLoader;

    private static class ImageLoaderUtilInstance {
        private final static ImageLoaderUtil RETROFIT_CLIENT = new ImageLoaderUtil();
    }

    private ImageLoaderUtil() {
    }

    public static ImageLoaderUtil getInstance() {
        return ImageLoaderUtilInstance.RETROFIT_CLIENT;
    }

    /**
     * 设置图片加载策略并初始化
     *
     * @param iLoader 策略
     */
    public void setILoader(ILoader iLoader) {
        this.iLoader = iLoader;
        iLoader.init();
    }

    /**
     * 获取图片加载策略
     *
     * @param t   策略
     * @param <T>
     * @return
     */
    public <T> ILoader<T> getiLoader(T t) {
        return iLoader;
    }

    /**
     * 设置image图片
     *
     * @param imageUrl  图片地址
     * @param imageView 载体
     */
    public void setImage(String imageUrl, ImageView imageView) {
        if (TextUtils.isEmpty(imageUrl) || imageView == null || iLoader == null) {
            return;
        }
        iLoader.with(imageUrl, imageView);
    }

    /**
     * 获取bitmap
     *
     * @param url 图片地址
     * @return
     */
    public Bitmap getImage(String url) {
        if (TextUtils.isEmpty(url) || iLoader == null) {
            return null;
        }
        return iLoader.getImageBitmap(url);
    }

    /**
     * 获取bitmap
     *
     * @param url 图片地址
     * @return
     */
    public void getImage(String url, LoadBitmapListener loadBitmapListener) {
        if (TextUtils.isEmpty(url) || iLoader == null) {
            return;
        }
        iLoader.getImageBitmap(url, loadBitmapListener);
    }

    /**
     * 获取图片缓存大小
     *
     * @return
     */
    public String getImageCacheSize() {
        if (iLoader == null) {
            return "0.0M";
        }
        return iLoader.getImageCacheSize();
    }

    /**
     * 清除图片缓存
     */
    public void clearImageCache() {
        if (iLoader == null) {
            return;
        }
        iLoader.clearImageCache();
    }

    public interface LoadBitmapListener {
        void onSuccess(Bitmap bitmap);
    }

}
