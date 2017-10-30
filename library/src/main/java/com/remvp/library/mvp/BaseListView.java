package com.remvp.library.mvp;

import java.util.List;

/**
 * 列表界面的View
 */
public interface BaseListView<T> extends CEView<List<T>> {
    /**
     * 设置总页数
     *
     * @param count 页数
     */
    void setPageCount(String count);
}
