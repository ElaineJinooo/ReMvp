package com.remvp.library.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Recycler适配器基类
 * 在{@link #onCreateItemView()}中创建布局
 * 单中布局{@link #addItemView(int)}
 * 多种布局{@link #addItemView(int, int)}
 * 垂直列表android:layout_height="wrap_content"
 * <p>
 */
public abstract class BaseRecyclerAdp<T, F extends BaseRecyclerViewHolder>
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Adapter<T> {
    protected Context context;
    public List<T> data = new ArrayList<>();
    /**
     * -1头部布局，-2尾部布局
     */
    private SparseIntArray layoutIds = new SparseIntArray();

    private BaseRecyclerAdp() {

    }

    public BaseRecyclerAdp(Context context) {
        onCreateItemView();
        this.context = context;
    }

    @Override
    public void setData(List<T> data, boolean isRefresh) {
        if (isRefresh) {
            this.data.clear();
        }
        if (data != null && data.size() != 0) {
            this.data.addAll(data);
        }
        notifyDataSetChanged();
    }

    @Override
    public void setData(List<T> data) {
        setData(data, true);
    }

    public T getItem(int position) {
        return data.get(position);
    }

    /**
     * 创建布局
     * 单中布局{@link #addItemView(int)}
     * 多种布局{@link #addItemView(int, int)}
     */
    public abstract void onCreateItemView();

    /**
     * 多个布局
     *
     * @param viewType 布局类型
     * @param viewId   layoutId
     */
    public void addItemView(int viewType, @LayoutRes int viewId) {
        layoutIds.put(viewType, viewId);
    }

    /**
     * 仅适用一种布局
     *
     * @param viewId
     */
    public void addItemView(int viewId) {
        layoutIds.put(0, viewId);
    }

    /**
     * item点击事件
     *
     * @param <T>
     */
    public interface ItemClick<T> {
        void onItemClick(int position, T bean);
    }

    /**
     * 设置Item点击事件
     *
     * @param listener
     */
    public void setOnItemClick(ItemClick listener) {
        mItemClick = listener;
    }

    ItemClick mItemClick;

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder,
                                 final int position) {
        if (holder instanceof BaseRecyclerAdp.BaseAdapterEh) {
            final int viewType = getItemViewType(position);
            T data = this.data.get(position);
            onBindViewHolder(((BaseAdapterEh<F>) holder).holder, data, position, viewType);
        }
    }

    /**
     * 相应View渲染数据
     *
     * @param holder
     * @param data
     * @param position
     * @param viewType
     */
    public abstract void onBindViewHolder(F holder, T data, int position, int viewType);


    @Override
    public BaseAdapterEh onCreateViewHolder(ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(
                context).inflate(layoutIds.get(viewType), parent,
                false);
        BaseAdapterEh<F> holder = new BaseAdapterEh<F>(layout);
        F child = getChildHolder(holder, viewType);
        holder.holder = child;
        return holder;
    }

    @Override
    public int getItemCount() {
        if (data == null) {
            return 0;
        }
        return data.size();
    }

    public void remove(T bean) {
        data.remove(bean);
        notifyDataSetChanged();
    }

    public void remove(int position) {
        data.remove(position);
        notifyItemRemoved(position);
    }

    /**
     * 初始化ViewHolder
     *
     * @param holder
     * @param viewType
     * @return
     */
    public abstract F getChildHolder(BaseAdapterEh holder, int viewType);

    public class BaseAdapterEh<F extends BaseRecyclerViewHolder> extends RecyclerView.ViewHolder {
        F holder;
        View layout;

        public <M> M findView(int id) {
            return (M) layout.findViewById(id);
        }

        public BaseAdapterEh(View itemView) {
            super(itemView);
            layout = itemView;
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mItemClick != null) {
                        int position = getLayoutPosition();
                        mItemClick.onItemClick(position, data.get(position));
                    }
                }
            });
        }

        public void setLayoutParams(ViewGroup.LayoutParams params) {
            layout.setLayoutParams(params);
        }
    }

}
