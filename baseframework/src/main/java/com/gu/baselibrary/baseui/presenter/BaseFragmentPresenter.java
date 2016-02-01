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
package com.gu.baselibrary.baseui.presenter;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gu.baselibrary.baseui.view.IDelegate;
import com.gu.baselibrary.utils.LogUtils;

import java.lang.reflect.Field;

import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;


/**
 * Presenter base class for Fragment
 * Presenter层的实现基类
 *
 * @param <T> View delegate class type
 * @author kymjs (http://www.kymjs.com/) on 10/23/15.
 */
public abstract class BaseFragmentPresenter<T extends IDelegate> extends Fragment {
    /**
     * Log tag
     */
    protected static String TAG_LOG = null;
    protected T viewDelegate;
    private boolean isFirstResume = true;
    private boolean isFirstVisible = true;
    private boolean isFirstInvisible = true;
    private boolean isPrepared = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            viewDelegate = getDelegateClass().newInstance();
            TAG_LOG = this.getClass().getSimpleName();
            if (isBindEventBus()) {
                EventBus.getDefault().register(this);
            }
        } catch (java.lang.InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        viewDelegate.create(inflater, container, savedInstanceState);
        return viewDelegate.getRootView();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewDelegate.initWidget();
        initViewsAndEvents();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (viewDelegate.getOptionsMenuId() != 0) {
            inflater.inflate(viewDelegate.getOptionsMenuId(), menu);
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (viewDelegate == null) {
            try {
                viewDelegate = getDelegateClass().newInstance();
                TAG_LOG = this.getClass().getSimpleName();
                if (isBindEventBus()) {
                    EventBus.getDefault().register(this);
                }
            } catch (java.lang.InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initPrepare();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isFirstResume) {
            isFirstResume = false;
            return;
        }
        if (getUserVisibleHint()) {
            onUserVisible();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (isFirstVisible) {
                isFirstVisible = false;
                initPrepare();
            } else {
                onUserVisible();
            }
        } else {
            if (isFirstInvisible) {
                isFirstInvisible = false;
                onFirstUserInvisible();
            } else {
                onUserInvisible();
            }
        }
    }


    /**
     * 当Fragment第一次从可见变不可见的时候，可以做的事情，不推荐在里面做处理
     */
    private void onFirstUserInvisible() {

    }

    private synchronized void initPrepare() {
        if (isPrepared) {
            ontUserFirsVisible();
        } else {
            isPrepared = true;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getUserVisibleHint()) {
            onUserInvisible();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        viewDelegate = null;
        if (isBindEventBus()) {
            EventBus.getDefault().unregister(this);
        }
    }

    /**
     * 这个方法是Fragment的方法，用来在Fragment上管理Fragment；
     * 使用时，如果用的V4包，会碰到no Activity错误，
     * 是因为v4包没重置childFragmentManager，重写父Fragment的onDetch方法
     */
    @Override
    public void onDetach() {
        super.onDetach();
        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 跳转一个Activity
     *
     * @param clazz
     */
    protected void go(Class<?> clazz) {
        Intent intent = new Intent(getActivity(), clazz);
        startActivity(intent);
    }

    /**
     * 跳转一个Activity then kill
     *
     * @param clazz
     */
    protected void goThenKill(Class<?> clazz) {
        Intent intent = new Intent(getActivity(), clazz);
        startActivity(intent);
        getActivity().finish();
    }

    /**
     * 跳转一个Activity with bundle
     *
     * @param clazz
     * @param bundle
     */
    protected void go(Class<?> clazz, Bundle bundle) {
        Intent intent = new Intent(getActivity(), clazz);
        if (null != bundle) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }

    /**
     * 跳转一个Activity with bundle then kill
     *
     * @param clazz
     * @param bundle
     */
    protected void goThenKill(Class<?> clazz, Bundle bundle) {
        Intent intent = new Intent(getActivity(), clazz);
        if (null != bundle) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
        getActivity().finish();
    }


    /**
     * 跳转一个Activity并等待返回结果
     *
     * @param clazz
     * @param requestCode
     */
    protected void goForResult(Class<?> clazz, int requestCode) {
        Intent intent = new Intent(getActivity(), clazz);
        startActivityForResult(intent, requestCode);
    }

    /**
     * 跳转一个Activity并等待返回结果 with bundle
     *
     * @param clazz
     * @param requestCode
     * @param bundle
     */
    protected void goForResult(Class<?> clazz, int requestCode, Bundle bundle) {
        Intent intent = new Intent(getActivity(), clazz);
        if (null != bundle) {
            intent.putExtras(bundle);
        }
        startActivityForResult(intent, requestCode);
    }


    protected abstract Class<T> getDelegateClass();

    /**
     * @return Fragment绑定的布局文件id
     */
    protected abstract int getContentViewLayoutID();

    /**
     * 是否绑定EventBus
     */
    protected abstract boolean isBindEventBus();

    /**
     * 当用户第一次可以看到这个Fragment的时候，我们可以在里面进行一些数据的请求初始化操作
     */
    protected abstract void ontUserFirsVisible();

    /**
     * Fragment用户不可见的时候可以 做的事情 就是onPause中应该做的事情就放这个方法
     */
    protected abstract void onUserInvisible();

    /**
     * Fragment用户可见的时候，可以做的事情
     */
    protected abstract void onUserVisible();

    /**
     * 初始化一些布局和数据
     */
    protected abstract void initViewsAndEvents();
}
