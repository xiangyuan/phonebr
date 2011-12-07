/*******************************************************************************************
 * Copyright (c) 2010 Wondershare Co., Ltd.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of Wondershare Co., Ltd. 
 * You shall not disclose such Confidential Information and shall use it only in accordance 
 * with the terms of the license agreement you entered into with Wondershare.
 ******************************************************************************************/

package com.wondershare.util;

import android.provider.CallLog.Calls;

public interface SysConst {

	/**
	 * 得到通话记录，包括编号、姓名、电话号码、日期、持续时间
	 */
	String[] CALLOG_PROJECTION = { Calls._ID, Calls.CACHED_NAME, Calls.NUMBER,
			Calls.DATE, Calls.DURATION,Calls.TYPE};
}
