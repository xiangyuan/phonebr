package com.alger.engine;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <title>下载引擎</title> 
 * @author XiangYuan 
 * @version 0.1 
 * @time 2011-12-6
 * @mailto liyajie1209@gmail.com
 */
public class DowloaderEngine {

    
    /**
     * 创建一个线程池服务
     * 可以重复利用线程对象
     */
    public static ExecutorService threadPool = Executors.newCachedThreadPool();
    
    
    /**
     * 创建一个下载任务
     * @param url　下载的地址url
     * @param storePath 下载后存储地址
     * @param fileName　下载后存储文件名
     */
    public void newDowloadTask(int threadNum,String url,String storePath,String fileName) {
        DowloadTask task = new DowloadTask(threadNum, url, storePath + File.separatorChar + fileName);
        threadPool.execute(task);
    }
}
