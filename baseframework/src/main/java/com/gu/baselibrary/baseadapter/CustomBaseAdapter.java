package com.gu.baselibrary.baseadapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @param <T>
 * @author 追梦工厂
 * @ClassName: BaseAdapter
 * @Description: 封装的listviewadapter
 * @date 2015年4月26日 上午10:06:33
 */
public abstract class CustomBaseAdapter<T> extends ArrayAdapter<T> {
    protected static String TAG_LOG = null;
    private View parent;
    /**
     * listview的item资源id
     */
    private int resourceId;

    protected List<T> list = new ArrayList<>();

    public CustomBaseAdapter(Context context, int resource, List<T> list) {
        super(context, resource, list);
        this.list = list;
        this.resourceId = resource;
        TAG_LOG = this.getClass().getSimpleName();
    }

    @Override
    public T getItem(int position) {
        return list.get(position);
    }

    /**
     * 根据position移除listview某一项
     *
     * @param position
     */
    public void remove(int position) {
        this.list.remove(position);
        notifyDataSetChanged();
    }

    /**
     * 根据t对象移除listview某一项
     *
     * @param t
     */
    public void remove(T t) {
        this.list.remove(t);
        notifyDataSetChanged();
    }

    /**
     * 当ListView数据发生变化时,调用此方法来更新ListView
     */
    public void updateListView(List<T> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (list == null) {
            return 0;
        }
        return list.size();
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        this.parent = parent;
        BaseViewHolder viewHolder = BaseViewHolder.get(getContext(), parent, resourceId, position, convertView);
        // 设置每个item控件
        setConvert(viewHolder, getItem(position));
        return viewHolder.getConvertView();
    }

    /**
     * @param @param viewHolder
     * @param @param t 设定文件
     * @return void 返回类型
     * @Title: setConvert
     * @Description: 抽象方法，由子类去实现每个itme如何设置
     */
    public abstract void setConvert(BaseViewHolder viewHolder, T t);

    public void setList(List<T> list) {
        this.list = list;
    }

    /**
     * 跳转另一个活动
     *
     * @param clazz
     */
    protected void go(Context context, Class<?> clazz) {
        Intent intent = new Intent(context, clazz);
        context.startActivity(intent);
    }

    /**
     * 跳转另一个活动并传递参数
     *
     * @param clazz
     * @param bundle
     */
    protected void go(Context context, Class<?> clazz, Bundle bundle) {
        Intent intent = new Intent(context, clazz);
        if (null != bundle) {
            intent.putExtras(bundle);
        }
        context.startActivity(intent);
    }

    /**
     * 跳转另一个活动并结束当前
     *
     * @param clazz
     */
    protected void goThenKill(Context context, Class<?> clazz) {
        Intent intent = new Intent(context, clazz);
        context.startActivity(intent);
        ((Activity) context).finish();
    }

    /**
     * 跳转另一个活动并结束，并传递参数
     *
     * @param clazz
     * @param bundle
     */
    protected void goThenKill(Context context, Class<?> clazz, Bundle bundle) {
        Intent intent = new Intent(context, clazz);
        if (null != bundle) {
            intent.putExtras(bundle);
        }
        context.startActivity(intent);
        ((Activity) context).finish();
    }

    /**
     * 展示一个Snackbar toast提示
     *
     * @param msg
     */
    protected void showToast(String msg) {
        if (!TextUtils.isEmpty(msg)) {
            Snackbar.make(parent, msg, Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * 展示一个Snackbar toast提示
     *
     * @param msgId
     */
    protected void showToast(int msgId) {
        String msg = getContext().getApplicationContext().getResources().getString(msgId);
        if (!TextUtils.isEmpty(msg)) {
            Snackbar.make(parent, msg, Snackbar.LENGTH_SHORT).show();
        }
    }

}
