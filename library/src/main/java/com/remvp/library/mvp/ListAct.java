package com.remvp.library.mvp;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;

import com.remvp.library.R;
import com.remvp.library.adapter.BaseRecyclerAdp;
import com.remvp.library.view.refresh.PtrFrameLayout;

import java.util.List;

/**
 * 带上推加载列表Activity基类，继承{@link CEAct}
 * 其中{@link CEAct#contentView}为{@link RecyclerView}控件
 * <p>
 */
public abstract class ListAct<A extends BaseRecyclerAdp, T
        , V extends BaseListView<T>
        , P extends ListPresenter<V>>
        extends CEAct<RecyclerView, List<T>, V, P> implements BaseListView<T> {
    private static final String TAG = "ListAct";
    protected int page = 1;
    protected int pageCount = 1;
    protected A adapter;

    @Override
    protected void initView() {
        super.initView();
        if (refreshView != null) {
            refreshView.setMode(PtrFrameLayout.Mode.BOTH);
        }
        adapter = createAdapter();
        contentView.setAdapter(adapter);
        contentView.setLayoutManager(createManager());
    }

    private RecyclerView.LayoutManager createManager() {
        return new LinearLayoutManager(this);
    }

    protected abstract A createAdapter();

    @Override
    public void loadData(boolean isRefresh) {
        if (isRefresh) {
            page = 1;
        }
    }

    @Override
    protected void onLoadMore() {
        if (page > pageCount) {
            toast(R.string.no_more_data);
            if (refreshView != null) {
                refreshView.refreshComplete();
            }
            return;
        }
        loadData(false);
    }

    @Override
    public void setDataInfo(List<T> data, boolean isRefresh) {
        Log.i(TAG, "setDataInfo: data = " + data + ", isRefresh = " + isRefresh);
        if (refreshView != null && refreshView.isRefreshing()) {
            refreshView.refreshComplete();
        }
        if ((data == null || data.size() == 0) && page == 1) {
            adapter.setData(data, true);
            setViewDisplayStatus(1);
            return;
        }
        setViewDisplayStatus(0);
        page++;
        adapter.setData(data, isRefresh);
    }

    @Override
    public void setDataInfo(List<T> data) {
        if (refreshView != null && refreshView.isRefreshing()) {
            refreshView.refreshComplete();
        }
        if ((data == null || data.size() == 0) && page == 1) {
            adapter.setData(data, true);
            setViewDisplayStatus(1);
            return;
        }
        setViewDisplayStatus(0);
        adapter.setData(data, true);
    }

    @Override
    public void setPageCount(String count) {
        if (TextUtils.isEmpty(count)) {
            pageCount = 1;
            return;
        }
        pageCount = Integer.valueOf(count);
    }
}
