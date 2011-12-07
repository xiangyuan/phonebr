/*******************************************************************************************
 * Copyright (c) 2010 Wondershare Co., Ltd.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of Wondershare Co., Ltd. 
 * You shall not disclose such Confidential Information and shall use it only in accordance 
 * with the terms of the license agreement you entered into with Wondershare.
 ******************************************************************************************/

package com.wondershare.util;

import android.util.Log;

public class AndroidLog {

	private static final String LOG_TAG = "webServer";
	
	
	public static final void log(String msg) {
		Log.d(LOG_TAG, msg);
	}
}
