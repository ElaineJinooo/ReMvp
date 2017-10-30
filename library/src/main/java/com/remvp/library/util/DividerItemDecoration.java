package com.remvp.library.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * onDraw 在Item绘制前绘制（会画在Item图层下面）
 * onDrawOver 在Item绘制后绘制（会画在Item图层上面）
 * getItemOffsets 间距,会在测量Item的时候调用
 */
public class DividerItemDecoration extends RecyclerView.ItemDecoration {

    private static final int[] ATTRS = new int[]{
            android.R.attr.listDivider
    };

    @IntDef({HORIZONTAL_LIST, VERTICAL_LIST, GRID})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LayoutMode {
    }

    public static final int HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL;
    public static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;
    public static final int GRID = 2;

    private Drawable mDivider;
    private int mHeight;

    private int mOrientation;
    private int marginLeft;
    private int marginRight;
    private int marginTop;
    private int marginBottom;
    private boolean isDrawOver = true;

    public DividerItemDecoration(Context context, @LayoutMode int orientation) {
        final TypedArray a = context.obtainStyledAttributes(ATTRS);
        mDivider = a.getDrawable(0);
        a.recycle();
        setOrientation(orientation);
    }

    public DividerItemDecoration(Context context, int resId, @LayoutMode int orientation) {
        mDivider = ContextCompat.getDrawable(context, resId);//context.getResources().getDrawable(resId);
        setOrientation(orientation);
    }

    public void setOrientation(@LayoutMode int orientation) {
        if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST && orientation != GRID) {
            throw new IllegalArgumentException("invalid orientation");
        }
        mOrientation = orientation;
    }

    public void setDividerHeight(int height) {
        mHeight = height;
    }

    public void setMargin(int marginLeft, int marginTop, int marginRight, int marginBottom) {
        this.marginLeft = marginLeft;
        this.marginRight = marginRight;
        this.marginTop = marginTop;
        this.marginBottom = marginBottom;
    }

    /**
     * 设置是否在item上面绘制
     *
     * @param drawOver
     */
    public void setDrawOver(boolean drawOver) {
        isDrawOver = drawOver;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        if (isDrawOver) {
            return;
        }
        draw(c, parent, state);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (!isDrawOver) {
            return;
        }
        draw(c, parent, state);
    }

    private void draw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (mOrientation == VERTICAL_LIST) {
            drawVertical(c, parent);
        } else if (mOrientation == HORIZONTAL_LIST) {
            drawHorizontal(c, parent);
        } else {
            drawVertical(c, parent);
            drawHorizontal(c, parent);
        }
    }

    public void drawVertical(Canvas c, RecyclerView parent) {
        if (parent.getChildCount() == 0) return;

        final int left = parent.getPaddingLeft();
        final int right = parent.getWidth() - parent.getPaddingRight();

//        final View child = parent.getChildAt(0);
//        if (child.getHeight() == 0) return;
        int space = mDivider.getIntrinsicHeight();

        if (mHeight > 0) {
            space = mHeight;
        }
        int childCount = parent.getChildCount();

        for (int i = 1; i <= childCount - 1; i++) {
            if (mOrientation == GRID) {
                int spanCount = getSpanCount(parent);
                if (i % spanCount != 0) {
                    continue;
                }
            }
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            int top = child.getTop() - params.topMargin;//child.getBottom() + params.bottomMargin + mDivider.getIntrinsicWidth();
            int bottom = top + space;
//            Log.i("DividerItemDecoration", String.format("drawVertical:int i =%d top = %d  left = %d", i, top, left));
            mDivider.setBounds(left + marginLeft, top + marginTop, right - marginRight, bottom - marginBottom);
            mDivider.draw(c);
        }
    }

    /**
     * 列
     *
     * @param c
     * @param parent
     */
    public void drawHorizontal(Canvas c, RecyclerView parent) {
        final int top = parent.getPaddingTop();
        final int bottom = parent.getHeight() - parent.getPaddingBottom();
        int spaceWidth = mDivider.getIntrinsicWidth();
        int spaceHeight = mDivider.getIntrinsicHeight();
        if (mHeight != 0) {
            spaceWidth = mHeight;
            spaceHeight = mHeight;
        }
        final int childCount = parent.getChildCount();
        for (int i = 1; i <= childCount - 1; i++) {
            if (mOrientation == GRID) {
                int spanCount = getSpanCount(parent);
                if (i % spanCount == 0) {
                    continue;
                }
            }
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams params =
                    (RecyclerView.LayoutParams) child.getLayoutParams();
            int left = child.getLeft() - params.leftMargin;//child.getRight() + params.rightMargin + spaceHeight;
            int right = left + spaceWidth;
//            Log.i("DividerItemDecoration", String.format("drawHorizontal:int i =%d left = %d  top = %d", i, left, top));
            mDivider.setBounds(left + marginLeft, top + marginTop, right - marginRight, bottom - marginBottom);
            mDivider.draw(c);
        }
    }

    //控制RecyclerView留出空位
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (mDivider == null || (mHeight == 0 && mDivider.getIntrinsicWidth() == 0 && mDivider.getIntrinsicHeight() == 0)) {
            outRect.set(0, 0, 0, 0);
            return;
        }
        int spaceWidth = mDivider.getIntrinsicWidth();
        int spaceHeight = mDivider.getIntrinsicHeight();
        if (mHeight != 0) {
            spaceWidth = mHeight;
            spaceHeight = mHeight;
        }
//        Log.i("DividerItemDecoration", String.format("spaceWidth = %d  spaceHeight = %d", spaceWidth, spaceHeight));
        if (mOrientation == VERTICAL_LIST) {
            outRect.set(0, 0, 0, spaceHeight);
        } else if (mOrientation == HORIZONTAL_LIST) {
            outRect.set(0, 0, spaceWidth, 0);
        } else {
            int spanCount = getSpanCount(parent);
            int childCount = parent.getAdapter().getItemCount();
            int itemPosition = parent.getChildAdapterPosition(view);
            if (isLastRaw(parent, itemPosition, spanCount, childCount)) {
                // 如果是最后一行，则不需要绘制底部
                outRect.set(0, 0, spaceWidth, 0);
            } else if (isLastColum(parent, itemPosition, spanCount, childCount)) {
                // 如果是最后一列，则不需要绘制右边
                outRect.set(0, 0, 0, spaceHeight);
            } else {
                outRect.set(0, 0, spaceWidth, spaceHeight);
            }
        }
    }

    private int getSpanCount(RecyclerView parent) {
        // 列数
        int spanCount = -1;
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            spanCount = ((GridLayoutManager) layoutManager).getSpanCount();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            spanCount = ((StaggeredGridLayoutManager) layoutManager)
                    .getSpanCount();
        }
        return spanCount;
    }

    private boolean isLastColum(RecyclerView parent, int pos, int spanCount,
                                int childCount) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            if ((pos + 1) % spanCount == 0)// 如果是最后一列，则不需要绘制右边
            {
                return true;
            }
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int orientation = ((StaggeredGridLayoutManager) layoutManager)
                    .getOrientation();
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                if ((pos + 1) % spanCount == 0)// 如果是最后一列，则不需要绘制右边
                {
                    return true;
                }
            } else {
                childCount = childCount - childCount % spanCount;
                if (pos >= childCount)// 如果是最后一列，则不需要绘制右边
                    return true;
            }
        }
        return false;
    }

    private boolean isLastRaw(RecyclerView parent, int pos, int spanCount,
                              int childCount) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            childCount = childCount - childCount % spanCount;
            if (pos >= childCount)// 如果是最后一行，则不需要绘制底部
                return true;
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int orientation = ((StaggeredGridLayoutManager) layoutManager)
                    .getOrientation();
            // StaggeredGridLayoutManager 且纵向滚动
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                childCount = childCount - childCount % spanCount;
                // 如果是最后一行，则不需要绘制底部
                if (pos >= childCount)
                    return true;
            } else
            // StaggeredGridLayoutManager 且横向滚动
            {
                // 如果是最后一行，则不需要绘制底部
                if ((pos + 1) % spanCount == 0) {
                    return true;
                }
            }
        }
        return false;
    }

}
