package com.bee.br.phone;


import android.content.Context;

import com.bee.br.utils.SysConst;


public class ContactGroupFactory {
	public static IContactGroupAction getContactManager(Context context){
		if(SysConst.SysVersion() < 2.0f)
			return new ContactGroupManager16(context);
		else
			return new ContactGroupManager(context);
	}
}
