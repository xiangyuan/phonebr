/*******************************************************************************************
 * Copyright (c) 2010 Wondershare Co., Ltd.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of Wondershare Co., Ltd. 
 * You shall not disclose such Confidential Information and shall use it only in accordance 
 * with the terms of the license agreement you entered into with Wondershare.
 ******************************************************************************************/

package com.wondershare.http.core;

import org.apache.http.protocol.HttpRequestHandlerRegistry;

import android.content.Context;

import com.wondershare.http.server.impl.CallogServlet;
import com.wondershare.http.server.impl.ContactServlet;
import com.wondershare.http.server.impl.HomePageServlet;

public class TResponse {

	private HttpRequestHandlerRegistry registryResponse = null;
	
	//private List<IAction> patterns = new ArrayList<IAction>(20);
	
	private static TResponse instance;
	
	private Context mContext = null;
	
	protected TResponse() {
		registryResponse = new HttpRequestHandlerRegistry();
		//patterns.add(new Action())
	}
	
	public static synchronized TResponse getInstance() {
		if (instance == null) {
			instance = new TResponse();
		}
		return (instance);
	}
	/**
	 * 注册处理器
	 * @return
	 */
	public synchronized HttpRequestHandlerRegistry getHttpRequestHandler() {
		addRequestType();
		return (registryResponse);
	}
	
	/**
	 * @param action
	 * @param severlet
	 */
	public void addRequestType() {
		registryResponse.register("*",new HomePageServlet(mContext));
		//通话记录
		registryResponse.register("/calls*", new CallogServlet(mContext));
		//联系人
		registryResponse.register("/contacts*", new ContactServlet(mContext));
	}
	
	public void setContext(Context mContext) {
		this.mContext = mContext;
	}
}
