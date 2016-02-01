package com.nate.base.v;

import android.support.v7.widget.Toolbar;
import android.widget.EditText;
import android.widget.TextView;

import com.gu.baselibrary.baseui.view.AppDelegate;
import com.nate.base.R;

/**
 * Created by guxuewu on 2016/2/1.
 */
public class MainActivityView extends AppDelegate {

    TextView tv_test;

    @Override
    public int getRootLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void initWidget() {
        tv_test = get(R.id.tv_test);
    }

    @Override
    public int getOptionsMenuId() {
        return 0;
    }

    @Override
    public Toolbar getToolbar() {
        return null;
    }

    public void copy() {
        String newText = ((EditText) get(R.id.edt_test)).getText().toString();
        tv_test.setText(newText);
    }
}
