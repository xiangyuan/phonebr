package com.alger.engine;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;

/**
 * <title>具体的下载线程</title> 
 * @author XiangYuan 
 * @version 0.1 
 * @time 2011-12-6
 * @mailto liyajie1209@gmail.com
 */
public class DowloadThread extends Thread {

    /**
     * buffer size to use
     */
    public static final int BUFFER_SIZE = 1024 * 8;
    /**
     * 一个线程是否下载完成
     */
    private boolean isComplemted = false;
    
    /**
     * 开始位置与结束位置
     */
    private long startLoc,endLoc;
    
    /**
     * 下载地址
     */
    private URL sourceURL = null;
    
    /**
     * 一个线程的
     */
    private File tempFile = null;
    
    /**
     * 是否是新启动的
     */
    private boolean isNewStart = true;
    
    private long curLoc;
    
    private long readByteLen = 0;
    
    
    /**
     * @param sourceURL
     * @param tempFile
     * @param startLoc
     * @param endLoc
     */
    public DowloadThread(URL sourceURL,File tempFile,long startLoc,long endLoc) {
        this.sourceURL = sourceURL;
        this.tempFile = tempFile;
        this.curLoc = this.startLoc = startLoc;
        this.endLoc = endLoc;
    }
    
    public void run() {
        System.out.println("线程" + getId() + "启动......");
        BufferedInputStream bis = null;
        RandomAccessFile fos = null;
        byte[] buf = new byte[BUFFER_SIZE];
        URLConnection con = null;
        try {
            con = sourceURL.openConnection();
            con.setAllowUserInteraction(true);
            if (isNewStart) {
                isNewStart = false;
                con.setRequestProperty("Range", "bytes=" + startLoc + "-" + endLoc);
                fos = new RandomAccessFile(tempFile, "rw");
                fos.seek(startLoc);
            } else {
                con.setRequestProperty("Range", "bytes=" + curLoc + "-" + endLoc);
                fos = new RandomAccessFile(tempFile, "rw");
                fos.seek(curLoc);
            }
            bis = new BufferedInputStream(con.getInputStream());
            while (curLoc < endLoc) {
                int len = bis.read(buf, 0, BUFFER_SIZE);
                if (len == -1) {
                    break;
                }
                fos.write(buf, 0, len);
                curLoc = curLoc + len;
                if (curLoc > endLoc) {
                    readByteLen += len - (curLoc - endLoc) + 1; //获取正确读取的字节数
                } else {
                    readByteLen += len;
                }
            }
            System.out.println("线程" + getId() + "已经下载完毕。");
            isComplemted = true;
            bis.close();
            fos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * 是否执行完成
     * @return
     */
    public boolean isComplemted() {
        return isComplemted;
    }
    
    /**
     * @return
     */
    public long getReadBytesLen() {
        return (readByteLen);
    }

}
