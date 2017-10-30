package com.remvp.library.adapter;

import java.util.List;

public interface Adapter<T> {
    void setData(List<T> data);

    void setData(List<T> data, boolean isRefresh);
}
