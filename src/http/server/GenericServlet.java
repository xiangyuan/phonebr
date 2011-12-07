/*******************************************************************************************
 * Copyright (c) 2010 Wondershare Co., Ltd.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of Wondershare Co., Ltd. 
 * You shall not disclose such Confidential Information and shall use it only in accordance 
 * with the terms of the license agreement you entered into with Wondershare.
 ******************************************************************************************/

package com.wondershare.http.server;

import java.io.IOException;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;

/**
 * @title
 * @author Wondershare XiangYuan
 * @version 2011-7-7 下午02:38:08
 */
public abstract class GenericServlet implements IServlet {

	protected static final String REQUEST_GET = "get";
	protected static final String REQUEST_POST = "post";
	
	
	@Override
	public void doService(HttpRequest request, HttpResponse response,
			HttpContext context) throws IOException,
			ServletException {
		String reqType = request.getRequestLine().getMethod();
		if (REQUEST_GET.equalsIgnoreCase(reqType)) {
			doGet(request, response,context);
		} else if (REQUEST_POST.equalsIgnoreCase(reqType)) {
			doPost(request, response,context);
		}
	}

	/**
	 * handle the get request
	 * 
	 * @param request
	 *            request server
	 * @param response
	 *            response client
	 * @param params
	 *            request params
	 * @param out
	 *            print out html message
	 * @throws IOException
	 * @throws ServletException
	 */
	protected abstract void doGet(HttpRequest request, HttpResponse response,
			HttpContext context) throws IOException,
			ServletException;

	/**
	 * @param request
	 * @param response
	 * @param params
	 * @param out
	 * @throws IOException
	 * @throws ServletException
	 */
	protected abstract void doPost(HttpRequest request, HttpResponse response,
			HttpContext context) throws IOException,
			ServletException;
}
