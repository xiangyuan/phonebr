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
import java.net.Socket;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpException;
import org.apache.http.HttpResponseFactory;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;

import android.content.Context;

/**
 * @title
 * @author Wondershare XiangYuan
 * @version 2011-7-7 下午03:14:42
 */
public class Dispatcher implements Runnable {

	/** 接收一个连接 **/
	private Socket socket = null;

	private HttpService httpService = null;

	private Context mContext = null;

	public Dispatcher(Socket s, Context context) {
		this.mContext = context;
		this.socket = s;
		initHttpService();
	}

	protected void initHttpService() {
		BasicHttpProcessor proc = new BasicHttpProcessor();
		ConnectionReuseStrategy connStrategy = new DefaultConnectionReuseStrategy();
		HttpResponseFactory responseFactory = new DefaultHttpResponseFactory();

		proc.addInterceptor(new ResponseDate());
		proc.addInterceptor(new ResponseServer());
		proc.addInterceptor(new ResponseContent());
		proc.addInterceptor(new ResponseConnControl());
		httpService = new HttpService(proc, connStrategy, responseFactory);
	}

	@Override
	public void run() {
		DefaultHttpServerConnection httpServerConnnection = null;
		try {
			httpServerConnnection = new DefaultHttpServerConnection();
			httpServerConnnection.bind(socket, new BasicHttpParams());
			// 处理响应
			TResponse response = TResponse.getInstance();
			response.setContext(mContext);
			httpService.setHandlerResolver(response.getHttpRequestHandler());
			
			// 接收请求
			httpService.handleRequest(httpServerConnnection,
					new BasicHttpContext());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (HttpException e) {
			e.printStackTrace();
		} finally {
			try {
				if (httpServerConnnection != null) {
					httpServerConnnection.shutdown();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
