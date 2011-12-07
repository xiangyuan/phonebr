/*******************************************************************************************
 * Copyright (c) 2010 Wondershare Co., Ltd.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of Wondershare Co., Ltd. 
 * You shall not disclose such Confidential Information and shall use it only in accordance 
 * with the terms of the license agreement you entered into with Wondershare.
 ******************************************************************************************/

package com.wondershare.http.cmd;

/**事件处理**/
public interface IAction {

	/**得到命令类型**/
	public String getType();
	/**设置**/
	public void setType(ActionType type);
	
	public String getPattern();
	
	public void setPattern(String pattern);
	
}
