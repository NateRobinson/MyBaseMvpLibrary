package com.nate.base.p;

import android.os.Bundle;
import android.view.View;

import com.gu.baselibrary.baseui.presenter.BaseActivityPresenter;
import com.gu.baselibrary.utils.NetUtils;
import com.nate.base.R;
import com.nate.base.v.MainActivityView;

public class MainActivity extends BaseActivityPresenter<MainActivityView> {


    @Override
    protected Class<MainActivityView> getDelegateClass() {
        return MainActivityView.class;
    }

    @Override
    protected void getBundleExtras(Bundle extras) {

    }

    @Override
    protected boolean isCustomPendingTransition() {
        return false;
    }

    @Override
    protected TransitionMode getCustomPendingTransitionType() {
        return null;
    }

    @Override
    protected boolean isBindEventBus() {
        return false;
    }

    @Override
    protected void doOnNetworkConnected(NetUtils.NetType type) {

    }

    @Override
    protected void doOnNetworkDisConnected() {

    }

    @Override
    protected void initViewsAndEvents() {
        viewDelegate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewDelegate.copy();
            }
        }, R.id.copy_btn);
    }
}
