
package com.alger.engine;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <title>一下载任务工作</title>
 * 
 * @author XiangYuan
 * @version 0.1
 * @time 2011-12-6
 * @mailto liyajie1209@gmail.com
 */
public class DowloadTask extends Thread {

    /**
     * 下载临时文件后缀，下载完成后将自动被删除
     */
    public final static String TEMP_FILE_POSTFIX = ".tmp";
    /**
     * 线程数
     */
    private int threadNum;

    /**
     * 下载的url地址
     */
    private URL url;

    /**
     * 输出文件名
     */
    private File outFileName = null;

    private DowloadThread[] workers = null;

    /**
     * 是否为新的任务
     */
    private boolean isNewTask = true;

    private DowloadListener listener = null;

    /**
     * 下载的文件总长度
     */
    private int totalLength;

//    /**
//     * 已读取的长度
//     */
//    private long readByteLen;

    /**
     * 创建一个新的下载任务
     * 
     * @param threadNum　线程数
     * @param url　下载源地址
     * @param outFileName　下载后输出地址
     */
    public DowloadTask(int threadNum, String url, String outFileName) {
        this.threadNum = threadNum;
        this.outFileName = new File(outFileName);
        this.isNewTask = true;

        this.workers = new DowloadThread[threadNum];
        // 注册监听器
        this.listener = new DowloadListener(this);

        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        if (isNewTask) {
            // 创建新的任务
            executeTask();
            return;
        }
        // 恢复任务
        resumePreTask();

    }

    /**
     * 创建新的任务
     */
    protected void executeTask() {
        isNewTask = false;// 结束
        // 创建url部分
        URLConnection conn = null;
        try {
            conn = url.openConnection();
            if (conn != null) {
                Map<String, List<String>> headers = conn.getHeaderFields();
                Set<String> keys = headers.keySet();
                for (String key : keys) {
                    // Log.d
                    System.out.println(key + ":" + headers.get(key));
                }
                totalLength = conn.getContentLength();
                if (totalLength <= 0) {
                    // Log.d
                    System.out.println("无法获取资源长度，中断下载进程");
                    return;
                }
                // 分线程下载各段
                // 创建一批临时文件
                long perThreadLen = totalLength / threadNum;
                // remain len
                long remainDataLen = totalLength % threadNum;
                for (int i = 0; i < threadNum; i++) {
                    DowloadThread automicThead = new DowloadThread(url, outFileName, perThreadLen
                            * i, perThreadLen * (i + 1) - 1);
                    workers[i] = automicThead;
                    DowloaderEngine.threadPool.execute(workers[i]);
                }
                DowloadThread automicThead = new DowloadThread(url, outFileName, totalLength
                        - remainDataLen, totalLength);
                DowloaderEngine.threadPool.execute(automicThead);
                DowloaderEngine.threadPool.execute(listener);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 是否支持断点下载
     */
    protected void resumePreTask() {

    }

    /**
     * 任务是否执行完成
     * 
     * @return
     */
    public boolean isTaskOver() {
        boolean completed = true;
        for (DowloadThread automic : workers) {
            completed = automic.isComplemted();
            if (!completed) {
                break;
            }
        }
        return (completed);
    }

    /**
     * @return
     */
    public long getReadByteLen() {
        long readByteLen = 0;
        for (DowloadThread worker : workers) {
            readByteLen += worker.getReadBytesLen();
        }
        return (readByteLen);
    }
    
    /**
     * total length
     * @return
     */
    public long getTotalLength() {
        return (this.totalLength);
    }
}
