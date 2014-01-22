package com.simon.host;

import android.app.Application;

/**
 */
public class MainApplication extends Application {

    private DexLoaderHelper mDexLoaderHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        mDexLoaderHelper = new DexLoaderHelper(this);
        mDexLoaderHelper.init(this);
    }

    public DexLoaderHelper getDexLoaderHelper() {
        return mDexLoaderHelper;
    }
}
