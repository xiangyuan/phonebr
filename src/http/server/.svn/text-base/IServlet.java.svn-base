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
import org.apache.http.protocol.HttpRequestHandler;

/**
 * @title
 * @author Wondershare XiangYuan
 * @version 2011-7-7 下午02:33:58
 */
public interface IServlet extends HttpRequestHandler {

	/**
	 * @param request
	 * @param response
	 * @param params
	 * @param out
	 * @throws IOException
	 * @throws ServletException
	 */
	public void doService(HttpRequest request, HttpResponse response,
			HttpContext context) throws IOException,
			ServletException;
}
