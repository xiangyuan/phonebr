package com.bee.br.phone;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.Groups;

import com.bee.br.utils.TargetNotFoundException;


public class ContactGroupManager implements IContactGroupAction {
	
	private static final String C_SysFirst = "System Group:";
	
	private ContentResolver contentResolver;

	public ContactGroupManager(Context context) {
		contentResolver = context.getContentResolver();
	}

	@Override
	public IContactGroup[] addContactGroup(IContactGroup[] groups)
			throws TargetNotFoundException {
		if (groups == null || groups.length == 0)
			throw new TargetNotFoundException();

		if (groups != null && groups.length != 0) {			
			for (int i = 0; i < groups.length; i++) {
				ContentValues values = new ContentValues();
				
				groups[i].setName(repairGroupName(groups[i].getName()));
				values.put(Groups.TITLE, groups[i].getName());
				
				if (groups[i].getNote() != null)
					values.put(Groups.NOTES, groups[i].getNote());
				//values.put(Groups.SYSTEM_ID, groups[i].getSystemId());
				
//				values.put(Groups.ACCOUNT_NAME, "pcsc");
//				values.put(Groups.ACCOUNT_TYPE, "com.htc.android.pcsc");
				
				groups[i].setVisiable(true);  // �״β����ܼӲ���ʾ��Ⱥ��
				values.put(Groups.GROUP_VISIBLE, groups[i].isVisiable());
				 
//				checkAccountInfo(groups[i].getAccountId(), values); // ����˺���Ϣ

				Uri uri = contentResolver.insert(Groups.CONTENT_URI, values);
				long id = ContentUris.parseId(uri);
				groups[i].setId(String.valueOf(id));
			}
			return groups;
		}
		return null;
	}
	
	private String repairGroupName(String name){
		if (name != null && name.startsWith(C_SysFirst)){
			name = name.substring(C_SysFirst.length()).trim();
		}
		return name;
	}

	@Override
	public IContactGroup[] getContactGroups() {
		String[] cols = new String[]{Groups._ID, Groups.TITLE, Groups.NOTES, Groups.SYSTEM_ID, Groups.GROUP_VISIBLE/*, Groups.ACCOUNT_NAME, Groups.ACCOUNT_TYPE*/};
		Cursor cursor = contentResolver.query(Groups.CONTENT_URI, cols,
				Groups.DELETED + "=0", null, null);
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				ArrayList<IContactGroup> contactGroupList = new ArrayList<IContactGroup>();
						
//				String accoutName, accoutType;
				do {
					IContactGroup contactGroup = new TContactGroup();
					contactGroup.setId(cursor.getString(0));
					contactGroup.setName(cursor.getString(1));
					contactGroup.setNote(cursor.getString(2));
					contactGroup.setSystemId(cursor.getString(3));
					contactGroup.setVisiable(cursor.getInt(4) != 0);
				
//					accoutName = cursor.getString(5); // ����˺���Ϣ
//					accoutType = cursor.getString(6);
//					contactGroup.setAccountId(ContactAccountManager.getDefault(contentResolver).getAccountIndex(accoutName, accoutType));
				
					contactGroupList.add(contactGroup);
				} while (cursor.moveToNext());
			
				cursor.close();
				return contactGroupList.toArray(new IContactGroup[0]);
			}
			cursor.close();
		}
		return null;
	}

	@Override
	public boolean removeContactGroup(IContactGroup[] groups) {
		if (groups != null) {
			for (int i = 0; i < groups.length; i++) {
				long id = Long.parseLong(groups[i].getId());
				Uri uri = ContentUris.withAppendedId(Groups.CONTENT_URI, id);
				ContentValues values = new ContentValues();
				values.put(Groups.DELETED, 1);
				contentResolver.update(uri, values, null, null);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean updateContactGroup(IContactGroup[] groups) {
		if (groups != null) {			
			for (int i = 0; i < groups.length; i++) {
				long id = Long.parseLong(groups[i].getId());
				Uri uri = ContentUris.withAppendedId(Groups.CONTENT_URI, id);
				ContentValues values = new ContentValues();
				
				groups[i].setName(repairGroupName(groups[i].getName()));
				values.put(Groups.TITLE, groups[i].getName());
								
				if (groups[i].getNote() != null)
					values.put(Groups.NOTES, groups[i].getNote());
				//values.put(Groups.SYSTEM_ID, groups[i].getSystemId());
				
				groups[i].setVisiable(true);
//				values.put(Groups.GROUP_VISIBLE, groups[i].isVisiable());
								
//				checkAccountInfo(groups[i].getAccountId(), values); // ����˺���Ϣ
				
				contentResolver.update(uri, values, null, null);
			}
			return true;
		}
		return false;
	}

//	/** ����˺���Ϣ�������ں��� */
//	private void checkAccountInfo(int accountId, ContentValues values){
//		if (accountId >= 0) {
//			IContactAccount account = ContactAccountManager.getDefault(contentResolver).getAccount(accountId);
//			if (account != null) {
//				values.put(Groups.ACCOUNT_NAME,
//						account.getAccount_name());
//				values.put(Groups.ACCOUNT_TYPE,
//						account.getAccount_type());
//			}
//		}
//	}
}
