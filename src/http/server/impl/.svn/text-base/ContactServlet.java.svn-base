/*******************************************************************************************
 * Copyright (c) 2010 Wondershare Co., Ltd.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of Wondershare Co., Ltd. 
 * You shall not disclose such Confidential Information and shall use it only in accordance 
 * with the terms of the license agreement you entered into with Wondershare.
 ******************************************************************************************/

package com.wondershare.http.server.impl;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;

import android.content.Context;

import com.wondershare.http.server.GenericServlet;
import com.wondershare.http.server.ServletException;
import com.wondershare.util.AndroidLog;

/**
 * @title
 * deal with contact
 * @author Wondershare XiangYuan
 * @version 2011-7-18 上午10:07:32
 */
public class ContactServlet extends GenericServlet {

	@SuppressWarnings("unused")
	private Context mContext = null;

	public ContactServlet(Context context) {
		this.mContext = context;
	}
	
	@Override
	protected void doGet(HttpRequest request, HttpResponse response,
			HttpContext context) throws IOException, ServletException {

	}

	@Override
	protected void doPost(HttpRequest request, HttpResponse response,
			HttpContext context) throws IOException, ServletException {
		doGet(request, response, context);
	}

	@Override
	public void handle(HttpRequest request, HttpResponse response,
			HttpContext context) throws HttpException, IOException {
		try {
			super.doService(request, response, context);
		} catch (ServletException e) {
			e.printStackTrace();
			AndroidLog.log(e.getMessage());
		}
	}

}
