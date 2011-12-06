package com.alger.engine;

import com.downloader.ui.HandlerUtils;

import android.os.Message;

import java.text.NumberFormat;

/**
 * <title>下载监听</title> 
 * @author XiangYuan 
 * @version 0.1 
 * @time 2011-12-6
 * @mailto liyajie1209@gmail.com
 */
public class DowloadListener extends Thread {

    private DowloadTask task = null;
    
    
    public DowloadListener(DowloadTask task) {
        this.task = task;
    }
    
    public void updateUI() {
        long readed = task.getReadByteLen();
        long total = task.getTotalLength();
        NumberFormat format = NumberFormat.getPercentInstance();
        format.setMinimumFractionDigits(2);
        String value = format.format(readed * 1.0 / total);
        System.out.println(value);
        Message msg = HandlerUtils.getInstance().getHandler().obtainMessage();
        msg.what = 1;
        msg.obj = value;
        HandlerUtils.getInstance().getHandler().sendMessage(msg);
    }
    
    public void run() {
        while (!task.isTaskOver()) {
            updateUI();
        }
        Message msg = HandlerUtils.getInstance().getHandler().obtainMessage();
        msg.what = 2;
        msg.obj = String.valueOf(100);
        HandlerUtils.getInstance().getHandler().sendMessage(msg);
    }
}
