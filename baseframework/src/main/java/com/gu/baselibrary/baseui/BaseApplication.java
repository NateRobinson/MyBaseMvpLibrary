package com.gu.baselibrary.baseui;

import android.app.Application;
import android.os.StrictMode;
import android.support.annotation.NonNull;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.GINGERBREAD;

/**
 * Created by Nate on 2015/9/29.
 */
public class BaseApplication extends Application {

    public static BaseApplication mInstance = null;
    // Volley request queue
    private RequestQueue mRequestQueue;

    @Override
    public void onCreate() {
        super.onCreate();
        enabledStrictMode();
        mInstance = this;
        //创建默认的ImageLoader配置参数
        ImageLoaderConfiguration configuration = ImageLoaderConfiguration.createDefault(this);
        //Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(configuration);
    }

    /**
     * 开发阶段开启严苛模式
     */
    private void enabledStrictMode() {
        if (SDK_INT >= GINGERBREAD) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder() //
                    .detectAll() //
                    .penaltyLog() //
//                    .penaltyDeath() //
                    .build());
        }
    }

    /**
     * @return BaseApplication
     */
    public static BaseApplication getInstance() {
        return mInstance;
    }

    /**
     * 返回一个Volley请求队列，用来进行网络请求用
     *
     * @return {@link com.android.volley.RequestQueue}
     */
    public RequestQueue getVolleyRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(this);
        }
        return mRequestQueue;
    }

    /**
     * 添加到一个网络请求到Volley请求队列
     *
     * @param request 将被添加到Volley请求队列的网络请求
     */
    public static void addRequest(@NonNull final Request<?> request) {
        request.setRetryPolicy(new DefaultRetryPolicy(10 * 1000, 1, 1.0f));
        getInstance().getVolleyRequestQueue().add(request);
    }

    /**
     * 添加一个带有标志tag的网络请求到Volley请求队列
     *
     * @param request 将被添加到Volley请求队列的网络请求
     * @param tag     标记这个请求的tag
     */
    public static void addRequest(@NonNull final Request<?> request, @NonNull final String tag) {
        request.setTag(tag);
        addRequest(request);
    }

    /**
     * 根据请求标志来从Volley请求队列中取消对应的网络请求
     *
     * @param tag associated with the Volley requests to be cancelled
     */
    public static void cancelAllRequests(@NonNull final String tag) {
        if (getInstance().getVolleyRequestQueue() != null) {
            getInstance().getVolleyRequestQueue().cancelAll(tag);
        }
    }
}
