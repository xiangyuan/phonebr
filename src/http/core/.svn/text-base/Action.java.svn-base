/*******************************************************************************************
 * Copyright (c) 2010 Wondershare Co., Ltd.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of Wondershare Co., Ltd. 
 * You shall not disclose such Confidential Information and shall use it only in accordance 
 * with the terms of the license agreement you entered into with Wondershare.
 ******************************************************************************************/

package com.wondershare.http.core;

import com.wondershare.http.cmd.ActionType;
import com.wondershare.http.cmd.IAction;

public class Action implements IAction {

	private ActionType actionType;
	
	private String pattern;
	
	public Action(String pattern) {
		this.pattern = pattern;
	}
	@Override
	public String getType() {
		return actionType.name();
	}

	@Override
	public void setType(ActionType type) {
		this.actionType = type;
	}

	@Override
	public String getPattern() {
		return pattern;
	}

	@Override
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

}
