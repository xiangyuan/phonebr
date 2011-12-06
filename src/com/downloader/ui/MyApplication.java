package com.downloader.ui;

import android.app.Application;
import android.content.res.Configuration;

/**
 * <title></title> 
 * @author XiangYuan 
 * @version 0.1 
 * @time 2011-12-5
 * @mailto liyj2@wondershare.cn
 */
public class MyApplication extends Application { 
    
    public static final String CONTENT_URL = "http://sinastorage.cn/fs/800/1/dec9f14e9c002891957feddcb2135f5e39810359/mp4/%E5%8C%97%E4%BA%AC%E6%AC%A2%E8%BF%8E%E4%BD%A0.mp4?origin=d133.d.iask.com";

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
    
    
    /**
     * @return
     */
    public String getContentURL() {
        return (CONTENT_URL);
    }
}
