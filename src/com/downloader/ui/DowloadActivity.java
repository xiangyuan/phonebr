
package com.downloader.ui;

import com.alger.engine.DowloaderEngine;

import java.io.File;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * <title></title> 
 * @author XiangYuan 
 * @version 0.1 
 * @time 2011-12-6
 * @mailto liyj2@wondershare.cn
 */
public class DowloadActivity extends Activity {

    private TextView downloadUrl;
    private EditText downloadThreadNum;
    private Button downloadBt;
    private ProgressBar downloadProgressBar;
    private TextView progressMessage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        downloadUrl = (TextView) findViewById(R.id.url);
        downloadThreadNum = (EditText) findViewById(R.id.threadNum);
        MyApplication application = (MyApplication) getApplication();
        downloadUrl.setText(application.getContentURL());
        progressMessage = (TextView) findViewById(R.id.progressValue);
        downloadProgressBar = (ProgressBar) findViewById(R.id.progressBar1);
        downloadBt = (Button) findViewById(R.id.download);
        
    }

    /**
     * 按钮点后执行
     * 
     * @param view
     */
    public void perfomClick(View view) {
        downloadProgressBar.setVisibility(View.VISIBLE);
        progressMessage.setVisibility(View.VISIBLE);
        downloadProgressBar.setMax(100);
        downloadProgressBar.setProgress(0);
        download();
    }

    /**
     * 开始下载
     */
    private void download() {
        // 获取SD卡目录
        String dowloadDir = Environment.getExternalStorageDirectory()
                + "/downloader/";
        File file = new File(dowloadDir);
        // 创建下载目录
        if (!file.exists()) {
            file.mkdirs();
        }

        DowloaderEngine engine = new DowloaderEngine();
        HandlerUtils.getInstance().setHandler(handler);
        
        // 读取下载线程数，如果为空，则单线程下载
        int downloadTN = Integer.valueOf("".equals(downloadThreadNum.getText()
                .toString()) ? "10" : downloadThreadNum.getText().toString());
        // 如果下载文件名为空则获取Url尾为文件名
//        int fileNameStart = downloadUrl.getText().toString().lastIndexOf("/");
//        String fileName = downloadUrl
//                .getText().toString().substring(fileNameStart);
        // 开始下载前把下载按钮设置为不可用
        downloadBt.setClickable(false);
        // 进度条设为0
        downloadProgressBar.setProgress(0);
        // 启动文件下载线程
        engine.newDowloadTask(downloadTN, downloadUrl.getText().toString(), file.getPath(), "output.mp4");
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // 当收到更新视图消息时，计算已完成下载百分比，同时更新进度条信息
            if (msg.what == 1) {
                progressMessage.setText("当前进度:" + msg.obj.toString() + "%");
            }
            if (msg.what == 2) {
                downloadBt.setClickable(true);
                progressMessage.setText("下载完成！");
            }
        }
    };
}
