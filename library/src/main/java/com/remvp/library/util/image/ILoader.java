package com.remvp.library.util.image;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * 图片加载策略
 */
public interface ILoader<T> {
    /**
     * 初始化，如设置缓存大小，策略等
     */
    void init();

    /**
     * 设置image图片
     *
     * @param imageUrl  图片地址
     * @param imageView 载体
     */
    void with(String imageUrl, ImageView imageView);

    /**
     * 获取bitmap
     *
     * @param imageUrl 图片地址
     */
    Bitmap getImageBitmap(String imageUrl);

    /**
     * 获取bitmap
     *
     * @param imageUrl 图片地址
     */
    void getImageBitmap(String imageUrl, ImageLoaderUtil.LoadBitmapListener listener);

    /**
     * 清除图片缓存
     */
    void clearImageCache();

    /**
     * 获取图片缓存大小
     */
    String getImageCacheSize();

    /**
     * 获取策略
     *
     * @return
     */
    T getStrategy();
}
