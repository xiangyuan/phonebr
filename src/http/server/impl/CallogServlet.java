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
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.protocol.HttpContext;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog.Calls;

import com.wondershare.http.server.GenericServlet;
import com.wondershare.http.server.ServletException;
import com.wondershare.util.AndroidLog;
import com.wondershare.util.SysConst;
import com.wondershare.util.Utils;

/**
 * @title 处理通话记录
 * @author Wondershare XiangYuan
 * @version 2011-7-18 上午09:53:35
 */
public class CallogServlet extends GenericServlet {

	private Context mContext = null;

	/**
	 * 通话记录
	 */
	private Uri callogUri = null;
	
	public CallogServlet(Context context) {
		this.mContext = context;
		callogUri = Calls.CONTENT_URI;
//		callogUri = Uri.parse("content://call_call_log");
	}

	@Override
	protected void doGet(HttpRequest request, HttpResponse response,
			HttpContext context) throws IOException, ServletException {
		Cursor cursor = mContext.getContentResolver().query(callogUri, SysConst.CALLOG_PROJECTION, null, null, null);
		final String html = Utils.spellCallogToHTML(cursor);
		HttpEntity entity = new EntityTemplate(new ContentProducer() {
			@Override
			public void writeTo(OutputStream outstream) throws IOException {
				OutputStreamWriter out = new OutputStreamWriter(outstream);
				out.write(html);
				out.flush();
			}
		});
		((EntityTemplate)entity).setContentType("text/html");
		response.setEntity(entity);
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
