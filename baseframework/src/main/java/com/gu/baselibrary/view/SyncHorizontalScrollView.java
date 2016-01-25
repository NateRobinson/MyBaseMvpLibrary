package com.gu.baselibrary.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;

public class SyncHorizontalScrollView extends HorizontalScrollView {

    private View view;
    private ImageView leftImage;
    private ImageView rightImage;
    private int windowWitdh = 0;
    private Activity mContext;

    public void setSomeParam(View view, ImageView leftImage,
                             ImageView rightImage, Activity context) {
        this.mContext = context;
        this.view = view;
        this.leftImage = leftImage;
        this.rightImage = rightImage;
        DisplayMetrics dm = new DisplayMetrics();
        this.mContext.getWindowManager().getDefaultDisplay().getMetrics(dm);
        windowWitdh = dm.widthPixels;
    }

    public SyncHorizontalScrollView(Context context) {
        super(context);
    }

    public SyncHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 监听滑动事件，来控制左右箭头的显示与隐藏
     *
     * @param l
     * @param t
     * @param oldl
     * @param oldt
     */
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (!mContext.isFinishing() && view != null && rightImage != null
                && leftImage != null) {
            if (view.getWidth() <= windowWitdh) {
                leftImage.setVisibility(View.GONE);
                rightImage.setVisibility(View.GONE);
            } else {
                if (l == 0) {
                    leftImage.setVisibility(View.GONE);
                    rightImage.setVisibility(View.VISIBLE);
                } else if (view.getWidth() - l == windowWitdh) {
                    leftImage.setVisibility(View.VISIBLE);
                    rightImage.setVisibility(View.GONE);
                } else {
                    leftImage.setVisibility(View.VISIBLE);
                    rightImage.setVisibility(View.VISIBLE);
                }
            }
        }
    }
}
