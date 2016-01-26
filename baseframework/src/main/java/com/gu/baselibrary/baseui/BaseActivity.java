package com.gu.baselibrary.baseui;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.flyco.dialog.listener.OnBtnLeftClickL;
import com.flyco.dialog.listener.OnBtnRightClickL;
import com.flyco.dialog.widget.MaterialDialog;
import com.gu.baselibrary.R;
import com.gu.baselibrary.netstatus.NetChangeCallBack;
import com.gu.baselibrary.netstatus.NetStatusReceiver;
import com.gu.baselibrary.utils.NetUtils;
import com.gu.baselibrary.utils.SmartBarUtils;
import com.gu.baselibrary.utils.Toastor;
import com.gu.baselibrary.view.LoadingDialog;
import com.gu.baselibrary.view.loadview.ShapeLoadingDialog;

import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;
import de.greenrobot.event.EventBus;

/**
 * Created by Nate on 2015/9/9. Activity基类
 */
public abstract class BaseActivity extends AppCompatActivity {
    /**
     * Log tag
     */
    protected static String TAG_LOG = null;

    /**
     * 设备屏幕信息参数
     */
    protected int mScreenWidth = 0;

    protected int mScreenHeight = 0;

    protected float mScreenDensity = 0.0f;
    /**
     * 上下文全局变量
     */
    protected Context mContext = null;

    /**
     * 网络状态监听
     */
    protected NetChangeCallBack mNetChangeCallBack = null;

    /**
     * 等待框
     */
    private LoadingDialog dialog;

    /**
     * 几种页面切换动画的枚举类
     */
    public enum TransitionMode {
        LEFT, RIGHT, TOP, BOTTOM, SCALE, FADE
    }

    private ShapeLoadingDialog shapeLoadingDialog;

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

        Bundle extras = getIntent().getExtras();
        // 如果有extras，则在getBundleExtras（）进行处理
        if (null != extras) {
            getBundleExtras(extras);
        }
        //看看是否绑定了EventBus
        if (isBindEventBus()) {
            EventBus.getDefault().register(this);
        }
        // 专门为魅族手机准备的隐藏其smartbar的工具类
        SmartBarUtils.hide(getWindow().getDecorView());
        // 是否全屏应用
        setTranslucentStatus(isApplyStatusBarTranslucency());
        mContext = this;
        TAG_LOG = this.getClass().getSimpleName();
        // 页面堆栈管理
        ActivityCollections.getInstance().addActivity(this);
        // 初始化屏幕相关信息
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mScreenDensity = displayMetrics.density;
        mScreenHeight = displayMetrics.heightPixels;
        mScreenWidth = displayMetrics.widthPixels;
        // 给activity绑定布局文件
        if (getContentViewLayoutID() != 0) {
            setContentView(getContentViewLayoutID());
        } else {
            throw new IllegalArgumentException("You must return a right contentView layout resource Id");
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
        initViewsAndEvents();
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
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        //初始化ButterKnife
        ButterKnife.inject(this);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
        BaseApplication.cancelAllRequests(TAG_LOG);
        //看看是否绑定了EventBus
        if (isBindEventBus()) {
            EventBus.getDefault().unregister(this);
        }
        NetStatusReceiver.unRegisterNetworkStateReceiver(this);
        NetStatusReceiver.removeRegisterNetChangeCallBack(mNetChangeCallBack);
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
     * 展示一个Snackbar toast提示
     *
     * @param msg
     */
    protected void showToast(String msg) {
        if (!TextUtils.isEmpty(msg)) {
            new Toastor(this).showSingletonToast(msg);
        }
    }

    /**
     * 展示一个Snackbar toast提示
     *
     * @param msgId
     */
    protected void showToast(int msgId) {
        new Toastor(this).showSingletonToast(msgId);
    }

    /**
     * 展示一个等待框
     */
    protected void showLoadingDialog() {
        if (!this.isFinishing()) {
            if (shapeLoadingDialog == null) {
                shapeLoadingDialog = new ShapeLoadingDialog(this);
            }
            shapeLoadingDialog.setCanceledOnTouchOutside(false);
            shapeLoadingDialog.setLoadingText("加载中...");
            shapeLoadingDialog.show();
        }
    }

    /**
     * 展示进度条数值
     *
     * @param progress
     */
    protected void modifyLoadingDialogTitle(String progress) {
        if (null != dialog) {
            dialog.setTitle(progress);
        }
    }


    /**
     * 取消一个等待框
     */
    protected void dismissLoadingDialog() {
        if (null != shapeLoadingDialog && !this.isFinishing()) {
            shapeLoadingDialog.dismiss();
        }
    }

    /**
     * 成功类型的弹出框
     *
     * @param title
     * @param content
     */
    protected SweetAlertDialog showSweetDialogSuccess(String title, String content) {
        SweetAlertDialog sd = new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE);
        sd.setTitleText(title);
        sd.setConfirmText("确定");
        sd.setContentText(content);
        // 可以按返回取消
        sd.setCancelable(false);
        // 可以点击外部取消
        sd.setCanceledOnTouchOutside(false);
        sd.show();
        return sd;
    }

    /**
     * 错误提示类型的弹出框
     *
     * @param title
     * @param content
     */
    protected SweetAlertDialog showSweetDialogFail(String title, String content) {
        SweetAlertDialog sd = new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE);
        sd.setTitleText(title);
        sd.setContentText(content);
        sd.setConfirmText("确定");
        // 可以按返回取消
        sd.setCancelable(false);
        // 可以点击外部取消
        sd.setCanceledOnTouchOutside(false);
        sd.show();
        return sd;
    }

    /**
     * 提示类型的弹出框
     *
     * @param title
     * @param content
     */
    protected SweetAlertDialog showSweetDialogPrompt(String title, String content) {
        SweetAlertDialog sd = new SweetAlertDialog(this);
        sd.setTitleText(title);
        sd.setContentText(content);
        sd.setConfirmText("确定");
        // 可以按返回取消
        sd.setCancelable(false);
        // 可以点击外部取消
        sd.setCanceledOnTouchOutside(false);
        sd.show();
        return sd;
    }

    /**
     * 提示警告
     */
    protected SweetAlertDialog showSweetDialogConfirm(String title, String content) {
        SweetAlertDialog sd = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
        sd.setTitleText(title);
        sd.setContentText(content);
        sd.setCancelText("取消");
        sd.setConfirmText("确定");
        sd.showCancelButton(true);
        sd.show();
        return sd;
    }

    /**
     * set status bar translucency 安卓4.4以上可以开启APP全屏模式 windowTranslucentStatus Flag indicating whether this window
     * requests a translucent status bar. 大意就是说状态栏是否半透明，如果是true的话，你会发现你的Toolbar陷入到状态栏里面了，
     * 所以为了预留空间，需要下面的属性：android:fitsSystemWindows
     *
     * @param on
     */
    protected void setTranslucentStatus(boolean on) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window win = getWindow();
            WindowManager.LayoutParams winParams = win.getAttributes();
            final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            if (on) {
                winParams.flags |= bits;
            } else {
                winParams.flags &= ~bits;
            }
            win.setAttributes(winParams);
        }
    }

    /**
     * 绑定布局文件
     *
     * @return id of layout resource
     */
    protected abstract int getContentViewLayoutID();

    /**
     * 是否开启应用的全屏展示 安卓4.4以上可以开启APP全屏模式
     *
     * @return
     */
    protected abstract boolean isApplyStatusBarTranslucency();

    /**
     * 是否绑定了EventBus
     *
     * @return
     */
    protected abstract boolean isBindEventBus();

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
     * 初始化所有布局和event事件
     */
    protected abstract void initViewsAndEvents();

    /**
     * 网络连接连起来了
     */
    protected abstract void doOnNetworkConnected(NetUtils.NetType type);

    /**
     * 网络连接断开
     */
    protected abstract void doOnNetworkDisConnected();
}
