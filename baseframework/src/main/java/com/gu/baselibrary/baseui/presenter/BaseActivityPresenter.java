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
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.gu.baselibrary.R;
import com.gu.baselibrary.baseui.ActivityCollections;
import com.gu.baselibrary.baseui.view.IDelegate;
import com.gu.baselibrary.netstatus.NetChangeCallBack;
import com.gu.baselibrary.netstatus.NetStatusReceiver;
import com.gu.baselibrary.utils.NetUtils;
import com.gu.baselibrary.view.LoadingDialog;
import com.gu.baselibrary.view.loadview.ShapeLoadingDialog;

import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;
import de.greenrobot.event.EventBus;


/**
 * Presenter base class for Activity
 * Presenter层的实现基类
 *
 * @param <T> View delegate class type
 * @author kymjs (http://www.kymjs.com/) on 10/23/15.
 */
public abstract class BaseActivityPresenter<T extends IDelegate> extends AppCompatActivity {
    /**
     * Log tag
     */
    protected static String TAG_LOG = null;
    /**
     * 视图代理对象
     */
    protected T viewDelegate;
    /**
     * 网络状态监听
     */
    protected NetChangeCallBack mNetChangeCallBack = null;

    /**
     * 几种页面切换动画的枚举类
     */
    public enum TransitionMode {
        LEFT, RIGHT, TOP, BOTTOM, SCALE, FADE
    }

    public BaseActivityPresenter() {
        try {
            viewDelegate = getDelegateClass().newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException("create IDelegate error");
        } catch (IllegalAccessException e) {
            throw new RuntimeException("create IDelegate error");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (isCustomPendingTransition()) {
            switch (getCustomPendingTransitionType()) {
                case LEFT:
                    overridePendingTransition(R.anim.left_in, R.anim.left_out);
                    break;
                case RIGHT:
                    overridePendingTransition(R.anim.right_in, R.anim.right_out);
                    break;
                case TOP:
                    overridePendingTransition(R.anim.top_in, R.anim.top_out);
                    break;
                case BOTTOM:
                    overridePendingTransition(R.anim.bottom_in, R.anim.bottom_out);
                    break;
                case SCALE:
                    overridePendingTransition(R.anim.scale_in, R.anim.scale_out);
                    break;
                case FADE:
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    break;
            }
        }
        super.onCreate(savedInstanceState);
        // 获取到视图层对象 并且实现了savedInstanceState的捕捉
        //让视图层初始化(如果是Fragment，就需要传递onCreateView方法中的三个参数)
        viewDelegate.create(getLayoutInflater(), null, savedInstanceState);
        //拿到初始化以后的rootview，并设置content
        setContentView(viewDelegate.getRootView());
        // 是否全屏应用
        setTranslucentStatus();
        //设置tag名字为当前class名
        TAG_LOG = this.getClass().getSimpleName();
        // 页面堆栈管理
        ActivityCollections.getInstance().addActivity(this);
        //初始化toolbar
        initToolbar();
        // 如果有extras，则在getBundleExtras（）进行处理
        Bundle extras = getIntent().getExtras();
        if (null != extras) {
            getBundleExtras(extras);
        }
        //看看是否绑定了EventBus
        if (isBindEventBus()) {
            EventBus.getDefault().register(this);
        }
        // 网络监听器
        mNetChangeCallBack = new NetChangeCallBack() {
            @Override
            public void onNetConnected(NetUtils.NetType type) {
                doOnNetworkConnected(type);
            }

            @Override
            public void onNetDisConnected() {
                doOnNetworkDisConnected();
            }
        };
        NetStatusReceiver.registerNetworkStateReceiver(this);
        NetStatusReceiver.registerNetChangeCallBack(mNetChangeCallBack);
        viewDelegate.initWidget();
        initViewsAndEvents();
    }

    /**
     * 设置toolbar
     */
    protected void initToolbar() {
        Toolbar toolbar = viewDelegate.getToolbar();
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (viewDelegate == null) {
            try {
                viewDelegate = getDelegateClass().newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException("create IDelegate error");
            } catch (IllegalAccessException e) {
                throw new RuntimeException("create IDelegate error");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (viewDelegate.getOptionsMenuId() != 0) {
            getMenuInflater().inflate(viewDelegate.getOptionsMenuId(), menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewDelegate = null;
        //看看是否绑定了EventBus
        if (isBindEventBus()) {
            EventBus.getDefault().unregister(this);
        }
        NetStatusReceiver.unRegisterNetworkStateReceiver(this);
        NetStatusReceiver.removeRegisterNetChangeCallBack(mNetChangeCallBack);
    }


    @Override
    public void finish() {
        super.finish();
        ActivityCollections.getInstance().removeActivity(this);
        if (isCustomPendingTransition()) {
            switch (getCustomPendingTransitionType()) {
                case LEFT:
                    overridePendingTransition(R.anim.left_in, R.anim.left_out);
                    break;
                case RIGHT:
                    overridePendingTransition(R.anim.right_in, R.anim.right_out);
                    break;
                case TOP:
                    overridePendingTransition(R.anim.top_in, R.anim.top_out);
                    break;
                case BOTTOM:
                    overridePendingTransition(R.anim.bottom_in, R.anim.bottom_out);
                    break;
                case SCALE:
                    overridePendingTransition(R.anim.scale_in, R.anim.scale_out);
                    break;
                case FADE:
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    break;
            }
        }
    }

    /**
     * set status bar translucency 安卓4.4以上可以开启APP全屏模式 windowTranslucentStatus Flag indicating whether this window
     * requests a translucent status bar. 大意就是说状态栏是否半透明，如果是true的话，你会发现你的Toolbar陷入到状态栏里面了，
     * 所以为了预留空间，需要下面的属性：android:fitsSystemWindows
     */
    protected void setTranslucentStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window win = getWindow();
            WindowManager.LayoutParams winParams = win.getAttributes();
            final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            winParams.flags |= bits;
            win.setAttributes(winParams);
        }
    }

    /**
     * 跳转另一个活动
     *
     * @param clazz
     */
    protected void go(Class<?> clazz) {
        Intent intent = new Intent(this, clazz);
        startActivity(intent);
    }


    /**
     * 跳转另一个活动并传递参数
     *
     * @param clazz
     * @param bundle
     */
    protected void go(Class<?> clazz, Bundle bundle) {
        Intent intent = new Intent(this, clazz);
        if (null != bundle) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }

    /**
     * 跳转另一个活动并结束当前
     *
     * @param clazz
     */
    protected void goThenKill(Class<?> clazz) {
        Intent intent = new Intent(this, clazz);
        startActivity(intent);
        finish();
    }

    /**
     * 跳转另一个活动并结束，并传递参数
     *
     * @param clazz
     * @param bundle
     */
    protected void goThenKill(Class<?> clazz, Bundle bundle) {
        Intent intent = new Intent(this, clazz);
        if (null != bundle) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
        finish();
    }

    /**
     * 开始一个活动，并等待返回结果
     *
     * @param clazz
     * @param requestCode
     */
    protected void goForResult(Class<?> clazz, int requestCode) {
        Intent intent = new Intent(this, clazz);
        startActivityForResult(intent, requestCode);
    }

    /**
     * 开始一个活动，并等待返回结果，并传递参数
     *
     * @param clazz
     * @param requestCode
     * @param bundle
     */
    protected void goForResult(Class<?> clazz, int requestCode, Bundle bundle) {
        Intent intent = new Intent(this, clazz);
        if (null != bundle) {
            intent.putExtras(bundle);
        }
        startActivityForResult(intent, requestCode);
    }

    /**
     * @return 获取自定义的视图层代理
     */
    protected abstract Class<T> getDelegateClass();

    /**
     * 处理Bundle传参
     *
     * @param extras
     */
    protected abstract void getBundleExtras(Bundle extras);

    /**
     * @return true--自定义页面的切换动画   false--不自定义
     */
    protected abstract boolean isCustomPendingTransition();

    /**
     * @return 返回自定义的动画切换方式
     */
    protected abstract TransitionMode getCustomPendingTransitionType();

    /**
     * 是否绑定了EventBus
     *
     * @return
     */
    protected abstract boolean isBindEventBus();

    /**
     * 网络连接连起来了
     */
    protected abstract void doOnNetworkConnected(NetUtils.NetType type);

    /**
     * 网络连接断开
     */
    protected abstract void doOnNetworkDisConnected();

    /**
     * 初始化所有布局和event事件
     */
    protected abstract void initViewsAndEvents();
}
