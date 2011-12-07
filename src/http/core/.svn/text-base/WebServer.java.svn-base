/*******************************************************************************************
 * Copyright (c) 2010 Wondershare Co., Ltd.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of Wondershare Co., Ltd. 
 * You shall not disclose such Confidential Information and shall use it only in accordance 
 * with the terms of the license agreement you entered into with Wondershare.
 ******************************************************************************************/

package com.wondershare.http.core;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import android.content.Context;

import com.wondershare.http.server.ISocketCallback;
import com.wondershare.http.server.IWebServer;
import com.wondershare.util.AndroidLog;

/**
 * @title
 * @author Wondershare XiangYuan
 * @version 2011-7-7 下午02:59:03
 */
public class WebServer extends Thread implements IWebServer, ISocketCallback {

	// private ServerSocketChannel ssc = null;
	/** 请求的端口号 **/
	public static final int NET_REQUEST_PORT = 8080;

	private ServerSocket ss = null;

	private boolean isStart = true;

	/**
	 * @return the isStart
	 */
	public boolean isStart() {
		return isStart;
	}

	/**
	 * @param isStart
	 *            the isStart to set
	 */
	public void setStart(boolean isStart) {
		this.isStart = isStart;
	}

	private String callbackMsg = "";

	private Context mContext = null;

	public WebServer(Context mContext) {
		this.mContext = mContext;
		// try {
		// // ssc = ServerSocketChannel.open();
		// // ssc.configureBlocking(false);
		// ss = new ServerSocket(NET_REQUEST_PORT);
		// ss.setReuseAddress(true);
		// ss.setSoTimeout(5000);
		//			
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
	}

	@Override
	public void run() {
		try {
			ss = new ServerSocket(NET_REQUEST_PORT);
			ss.setReuseAddress(true);
//			ss.setSoTimeout(5000);
			while (isStart) {
				final Socket socket = ss.accept();
				// 开始处理分发器
				Dispatcher dispatcher = new Dispatcher(socket, mContext);
				new Thread(dispatcher).start();
			}
		} catch (Exception e) {
			e.printStackTrace();
			AndroidLog.log(e.getMessage());
			closeServer();
		}
	}

	public void closeServer() {
		isStart = false;
		try {
			if (ss != null) {
				ss.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getCallbackMessage() {
		return callbackMsg;
	}

}
