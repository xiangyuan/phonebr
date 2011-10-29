package com.bee.br.phone;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Contacts;

import com.bee.br.utils.TargetNotFoundException;



@SuppressWarnings("deprecation")
public class ContactGroupManager16 implements IContactGroupAction {

	private ContentResolver contentResolver;

	public ContactGroupManager16(Context context) {
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
				values.put(Contacts.Groups.NAME, groups[i].getName());
				values.put(Contacts.Groups.NOTES, groups[i].getNote());
				values.put(Contacts.Groups.SYSTEM_ID, groups[i].getSystemId());
				Uri uri = contentResolver.insert(Contacts.Groups.CONTENT_URI,
						values);
				groups[i].setId(String.valueOf(ContentUris.parseId(uri)));
			}
			return groups;
		}
		return null;
	}

	@Override
	public IContactGroup[] getContactGroups() {
		String projection[] = new String[] { Contacts.Groups._ID,
				Contacts.Groups.NAME, Contacts.Groups.NOTES,
				Contacts.Groups.SYSTEM_ID };
		Cursor cursor = contentResolver.query(Contacts.Groups.CONTENT_URI,
				projection, null, null, null);
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				ArrayList<IContactGroup> listContactGroup = new ArrayList<IContactGroup>();
				do {
					IContactGroup contactGroup = new TContactGroup();
					contactGroup.setId(cursor.getString(0));
					contactGroup.setName(cursor.getString(1));
					contactGroup.setNote(cursor.getString(2));
					contactGroup.setSystemId(cursor.getString(3));
					listContactGroup.add(contactGroup);
				} while (cursor.moveToNext());
				
				cursor.close();
				return listContactGroup.toArray(new IContactGroup[0]);
			}
			cursor.close();
		}
		return null;
	}

	@Override
	public boolean removeContactGroup(IContactGroup[] groups) {
		if (groups != null && groups.length > 0) {
			for (int i = 0; i < groups.length; i++) {
				Uri uri = ContentUris.withAppendedId(
						Contacts.Groups.CONTENT_URI, Long.parseLong(groups[i]
								.getId()));
				contentResolver.delete(uri, null, null);
			}
		}
		return false;
	}

	@Override
	public boolean updateContactGroup(IContactGroup[] groups) {
		if (groups != null && groups.length > 0) {
			for (int i = 0; i < groups.length; i++) {
				Uri uri = ContentUris.withAppendedId(
						Contacts.Groups.CONTENT_URI, Long.parseLong(groups[i]
								.getId()));
				ContentValues values = new ContentValues();
				values.put(Contacts.Groups.NAME, groups[i].getName());
				values.put(Contacts.Groups.NOTES, groups[i].getNote());
				values.put(Contacts.Groups.SYSTEM_ID, groups[i].getSystemId());
				contentResolver.update(uri, values, null, null);
			}
		}
		return false;
	}

}
