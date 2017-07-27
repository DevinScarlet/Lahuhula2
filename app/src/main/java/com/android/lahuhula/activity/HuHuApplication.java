package com.android.lahuhula.activity;

import android.app.Application;

/**
 * Created by Devin on 2017/6/18.
 */
public class HuHuApplication extends Application {
    private static HuHuApplication apdApp = null;

    private static synchronized void setApp(HuHuApplication app) {
        apdApp = app;
    }

    public static synchronized HuHuApplication getApp() {
        return apdApp;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setApp(this);
    }
}
