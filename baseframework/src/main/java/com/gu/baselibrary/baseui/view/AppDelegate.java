/*
 * Copyright (c) 2015, 张涛.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gu.baselibrary.baseui.view;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.gu.baselibrary.utils.Toastor;

/**
 * View delegate base class
 * 视图层代理的基类
 *
 * @author kymjs (http://www.kymjs.com/) on 10/23/15.
 */
public abstract class AppDelegate implements IDelegate {

    protected final SparseArray<View> mViews = new SparseArray<View>();
    protected View rootView = null;

    @Override
    public void create(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int rootLayoutId = getRootLayoutId();
        if (rootView == null) {
            rootView = inflater.inflate(rootLayoutId, container, false);
        }
    }

    /**
     * @param id
     * @param <T>
     * @return 通过id，获取到控件对象
     */
    public <T extends View> T get(int id) {
        return (T) bindView(id);
    }

    /**
     * @param id
     * @param <T>
     * @return 绑定视图对象并返回
     */
    private <T extends View> T bindView(int id) {
        T view = (T) mViews.get(id);
        if (view == null) {
            view = (T) rootView.findViewById(id);
            mViews.put(id, view);
        }
        return view;
    }

    /**
     * 设置点击监听
     *
     * @param listener
     * @param ids
     */
    public void setOnClickListener(View.OnClickListener listener, int... ids) {
        if (ids == null) {
            return;
        }
        for (int id : ids) {
            get(id).setOnClickListener(listener);
        }
    }

    /**
     * 返回当前的Activity
     *
     * @param <T>
     * @return
     */
    public <T extends Activity> T getActivity() {
        return (T) rootView.getContext();
    }

    /**
     * 展示一个toast提示
     *
     * @param msg
     */
    protected void showToast(String msg) {
        if (!TextUtils.isEmpty(msg)) {
            new Toastor(rootView.getContext()).showSingletonToast(msg);
        }
    }

    /**
     * 展示一个toast提示
     *
     * @param msgId
     */
    protected void showToast(int msgId) {
        new Toastor(rootView.getContext()).showSingletonToast(msgId);
    }

    /**
     * @return 返回root视图的id
     */
    public abstract int getRootLayoutId();

    /**
     * 此方法里做一些初始化的操作
     */
    @Override
    public abstract void initWidget();

    /**
     * 返回当前的View
     */

    @Override
    public View getRootView() {
        return rootView;
    }

    /**
     * @return menu菜单id
     */
    @Override
    public abstract int getOptionsMenuId();

    /**
     * @return 返回toolbar对象
     */
    @Override
    public abstract Toolbar getToolbar();
}
