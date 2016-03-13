package com.gu.baselibrary.baseui.view;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * View delegate base class
 * 视图层代理的接口协议
 *
 * @author kymjs (http://www.kymjs.com/) on 10/23/15.
 */
public interface IDelegate {

    /**
     * 创建视图View
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     */
    void create(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    /**
     * 此方法里做一些初始化的操作
     */
    void initWidget();

    /**
     * @return 返回root视图
     */
    View getRootView();

    /**
     * @return 返回toolbar对象
     */
    Toolbar getToolbar();

    /**
     * @return menu菜单id
     */
    int getOptionsMenuId();

}
