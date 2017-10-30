package com.remvp.library.adapter;

public abstract class BaseRecyclerViewHolder {
    protected BaseRecyclerAdp.BaseAdapterEh mHolder;

    public BaseRecyclerViewHolder(BaseRecyclerAdp.BaseAdapterEh holder, int viewType) {
        this.mHolder = holder;
    }
}
