/*******************************************************************************************
 * Copyright (c) 2010 Wondershare Co., Ltd.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of Wondershare Co., Ltd. 
 * You shall not disclose such Confidential Information and shall use it only in accordance 
 * with the terms of the license agreement you entered into with Wondershare.
 ******************************************************************************************/

package com.wondershare.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

import com.wondershare.http.core.WebServer;

import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog.Calls;

public class Utils {

	public static final String REQUEST_SCHEME = "http://";

	public static final int PORT_NUM = WebServer.NET_REQUEST_PORT;

	/**
	 * 得到基本请求的url
	 * 
	 * @return
	 */
	public static String getBaseURLString() {
		return (REQUEST_SCHEME + getLocalIpAddress() + ":" + PORT_NUM);
		/*+ Environment.getExternalStorageDirectory());*/
	}

	/**
	 * 得到手机上面的ip地址
	 * 
	 * @return
	 */
	public static String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * @param context
	 * @param id
	 * @return
	 */
	public static String openHTMLString(Context context, int id) {
		InputStream is = context.getResources().openRawResource(id);

		return Utils.convertStreamToString(is);
	}

	/**
	 * 将输入流变为字符串
	 * 
	 * @param is
	 * @return
	 */
	public static String convertStreamToString(InputStream is) {
		if (is != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is,
						"UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return writer.toString();
		} else {
			return "";
		}
	}

	/**
	 * 将通话记录写过去
	 * 
	 * @param cursor
	 * @return
	 * @throws MalformedURLException
	 */
	public static String spellCallogToHTML(Cursor cursor) throws MalformedURLException {
		URL url = new URL(getBaseURLString() + "/assets/basic.css");
		StringBuilder sb = new StringBuilder(500);
		sb
				.append(
						"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">")
				.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" >")
				.append(
						"<head><title>通话记录</title><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"")
				.append(url.toString())
				.append("\"/>")
				.append(
						"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">")
				.append(
						"<META name=\"viewport\" content=\"width=device-width,minimum-scale=1.0,maximum-scale=1.0\"/>")
				.append("<META http-equiv=\"Pragma\" content=\"no-cache\"/>")
				.append(
						"<META http-equiv=\"Cache-Control\" content=\"no-cache,no-store\"/></head><body>")
				.append(
						"<div id=\"navigation\"><ul><li><a href=\"contacts/\">联系人</a></li><li><a href=\"settings/\">系统设置</a></li><li><a href=\"calls/\">通话记录</a></li><li><a href=\"network/\">网络</a></li>")
				.append(
						"<li><a href=\"media/\">多媒体</a></li></ul></div><div id='page'>");

		if (cursor != null && cursor.moveToFirst()) {
			sb
					.append("<table align=\"center\" width=\"600px\" ><tr align=\"center\"><th>编号</th><th>姓名</th><th>电话码</th><th>通话时间</th><th>日期</th><th>类型</th></tr>");
			do {
				sb.append("<tr align='center'>");
				sb.append("<td>").append(cursor.getInt(cursor.getColumnIndex(Calls._ID))).append("</td>");
				String t = cursor.getString(cursor.getColumnIndex(Calls.CACHED_NAME));
				String name = t == null ? "未知" : t;
				sb.append("<td>").append(name).append("</td>");
				sb.append("<td>").append(cursor.getString(cursor.getColumnIndex(Calls.NUMBER))).append("</td>");
				sb.append("<td>").append(formatCallTime(cursor.getInt(cursor.getColumnIndex(Calls.DURATION)))).append("</td>");
				sb.append("<td>").append(formatDate(cursor.getLong(cursor.getColumnIndex(Calls.DATE)))).append("</td>");
				sb.append("<td>").append(callType(cursor.getInt(cursor.getColumnIndex(Calls.TYPE)))).append("</td>");
				sb.append("</tr>");
			} while (cursor.moveToNext());
			sb.append("</table>");
		} else {
			String t = getBaseURLString() + "/assets/navbar.png";
			sb.append("<p>暂时没有通话记录</p><img src=\"").append(t).append(
					"\"/>");
		}
		sb
				.append("</div><div id=\"footer\"><p>@copryright Xiangyuan</p></div></body></html>");
		return sb.toString();
	}
	
	/**
	 * @param seconds
	 * @return
	 */
	public static String formatCallTime(int seconds) {
		int h = seconds / 60 / 60;
		int mu = (seconds / 60) % 60;
		int se = seconds % 60;
		return String.format("%d:%d:%d", h,mu,se);
	}
	
	/**
	 * @param time
	 * @return
	 */
	public static String formatDate(long time) {
		Date d = new Date();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		return (df.format(d));
	}
	
	/**
	 * 电话类型
	 * @param type
	 * @return
	 */
	public static String callType(int type) {
		String result = "";
		if (Calls.INCOMING_TYPE == type) {
			result = "来电";
		} else if (Calls.OUTGOING_TYPE == type) {
			result = "去电";
		} else if (Calls.MISSED_TYPE == type) {
			result = "未接电话";
		}
		return result;
	}
}
