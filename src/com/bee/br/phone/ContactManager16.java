package com.bee.br.phone;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.Contacts;

import com.bee.br.phone.ContactHelper.IContactItem;
import com.bee.br.phone.ContactHelper.IItemCompare;

/**
 * Contact manager for android 1.6 and below.
 */
@SuppressWarnings("deprecation")
public class ContactManager16 extends ContactFactory {
	/** _ID, DISPLAY_NAME, NOTES, CUSTOM_RINGTONE, STARRED */
	private static final String CONTACT_PROJECTION[] = { Contacts.People._ID,
			Contacts.People.NAME/* DISPLAY_NAME */, Contacts.People.NOTES,
			Contacts.People.CUSTOM_RINGTONE, Contacts.People.STARRED };

	/** PERSON_ID, NUMBER, TYPE, LABEL, _ID */
	private static final String PHONE_PROJECTION[] = {
			Contacts.Phones.PERSON_ID, Contacts.Phones.NUMBER,
			Contacts.Phones.TYPE, Contacts.Phones.LABEL, Contacts.Phones._ID };

	/** PERSON_ID, COMPANY, TITLE, TYPE, LABEL, _ID */
	private static final String ORGANIZATION_PROJECTION[] = {
			Contacts.Organizations.PERSON_ID, Contacts.Organizations.COMPANY,
			Contacts.Organizations.TITLE, Contacts.Organizations.TYPE,
			Contacts.Organizations.LABEL, Contacts.Organizations._ID };

	/** PERSON_ID, GROUP_ID, _ID */
	private static final String GROUP_PROJECTION[] = {
			Contacts.GroupMembership.PERSON_ID,
			Contacts.GroupMembership.GROUP_ID, Contacts.GroupMembership._ID };

	/** ContactMethods._ID (BaseColumns._ID) */
	private static final String[] ITEM_ID_PROJECTION = { Contacts.ContactMethods._ID };

	/** ContactMethods.PERSON_ID, DATA, TYPE, LABEL, AUX_DATA, KIND, _ID */
	protected static final String ITEM_DATA_PROJECTION[] = {
			Contacts.ContactMethods.PERSON_ID, Contacts.ContactMethods.DATA,
			Contacts.ContactMethods.TYPE, Contacts.ContactMethods.LABEL,
			Contacts.ContactMethods.AUX_DATA, Contacts.ContactMethods.KIND,
			Contacts.ContactMethods._ID };

	private ContactNumber contactNumber;
	private ContactOrganization contactOrganization;
	private ContactEmail contactEmail;
	private ContactIm contactIm;
	private ContactAddress contactAddress;
	private ContactGroup contactGroup;

	private ItemCompare itemCompare;
	private IItemCompare<IOrganization> organizationCompare;
	private IItemCompare<IAddress> addressCompare;

	public ContactManager16(Context context) {
		super(context);

		contactNumber = new ContactNumber();
		contactOrganization = new ContactOrganization();
		contactEmail = new ContactEmail();
		contactIm = new ContactIm();
		contactAddress = new ContactAddress();
		contactGroup = new ContactGroup();

		itemCompare = new ItemCompare();
		organizationCompare = contactOrganization;
		addressCompare = contactAddress;
	}

	@Override
	public int getCount() {
		int count = 0;
		Cursor cursor = contentResolver.query(Contacts.People.CONTENT_URI,
				new String[] { Contacts.People._ID }, null, null, null);
		if (cursor != null) {
			count = cursor.getCount();
			cursor.close();
		}
		return count;
	}

	@Override
	public IContact[] getAllContacts() {
		ArrayList<IContact> listContacts = getAllBaseInfo();
		if (listContacts != null) {
			IContact[] contacts = listContacts.toArray(new IContact[0]);

			getAllContactsItemData(contacts);
			return contacts;
		}
		return null;
	}

	private void getAllContactsItemData(IContact[] contacts) {
		fillAllItemData(contacts);
		fillAllGroupMembers(contacts);
		fillAllOrganization(contacts);
		fillAllPhone(contacts);
	}

	@Override
	public void getAllContacts(ICutListener listener) {
		ArrayList<IContact> contacts = getAllBaseInfo();

		int rowCount = contacts == null ? 0 : contacts.size();

		if (rowCount > 0) {
			int count = listener.getTimeCount(rowCount);
			int oneMax = listener.getOneMax();

			ArrayList<IContact> subContacts = new ArrayList<IContact>(oneMax);
			int index = 0;

			while (contacts.size() > 0) {
				IContact contact = contacts.get(0);

				subContacts.add(contact);
				contacts.remove(0);

				if (subContacts.size() >= oneMax) {
					IContact[] subContactArr = subContacts
							.toArray(new IContact[0]);
					getAllContactsItemData(subContactArr);

					subContacts.clear();
					index++;

					if (index >= count)
						break;
				}
			}

			if (subContacts.size() > 0) {
				IContact[] subContactArr = subContacts.toArray(new IContact[0]);
				getAllContactsItemData(subContactArr);

				subContacts.clear();
			}
			return;
		}
	}

	@Override
	public IContact getOldContact(IContact newContact) {
		IContact contact = getOneBaseInfo(newContact);
		if (contact != null) {
			fillOneItemData(contact);
			fillOneGroupMembers(contact);
			fillOneOrganization(contact);
			fillOnePhone(contact);
		}
		return contact;
	}

	@Override
	public int deleteAll() {
		int delCount = 0;
		int commitFlag = 0;
		Cursor cursor = contentResolver.query(Contacts.People.CONTENT_URI,
				new String[] { Contacts.People._ID }, null, null, null);
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				do {
					if (commitFlag > 90) {
						commitFlag = 0;
						deleteContacts(ops);
						ops.clear();
					}
					String id = cursor.getString(0);
					ops.add(ContentProviderOperation
							.newDelete(Contacts.People.CONTENT_URI)
							.withSelection(Contacts.People._ID + "=?",
									new String[] { id }).build());
					commitFlag++;
					delCount++;
				} while (cursor.moveToNext());
				deleteContacts(ops);
			}
			cursor.close();
		}
		return delCount;
	}

	@Override
	public int delete(IContact[] contacts) {
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		int delCount = 0;
		int commitFlag = 0;
		for (IContact contact : contacts) {
			if (commitFlag > 35) {
				commitFlag = 0;
				deleteContacts(ops);
			}
			ops.add(ContentProviderOperation
					.newDelete(Contacts.People.CONTENT_URI)
					.withSelection(Contacts.People._ID + "=?",
							new String[] { contact.getId() }).build());
			delCount++;
			commitFlag++;
		}
		deleteContacts(ops);
		return (delCount);
	}

	/***
	 * 删除所有的联系人，进行批处理
	 * */
	private int deleteContacts(ArrayList<ContentProviderOperation> patch) {
		// int dCount = contentResolver.delete(Contacts.People.CONTENT_URI,
		// Contacts.People._ID + "=" + personId, null);
		int len = 0;
		ContentProviderResult[] datas = null;
		try {
			datas = contentResolver.applyBatch(Contacts.AUTHORITY, patch);
			len = datas.length;
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			e.printStackTrace();
		}
		return (len);
		// dCount +=
		// contentResolver.delete(Contacts.GroupMembership.CONTENT_URI,
		// Contacts.GroupMembership.PERSON_ID + "=" + personId, null);
		// dCount += contentResolver.delete(Contacts.Organizations.CONTENT_URI,
		// Contacts.Organizations.PERSON_ID + "=" + personId, null);
		// dCount += contentResolver.delete(Contacts.Phones.CONTENT_URI,
		// Contacts.Phones.PERSON_ID + "=" + personId, null);
		//
		// dCount += contentResolver.delete(Contacts.ContactMethods.CONTENT_URI,
		// Contacts.ContactMethods.PERSON_ID + "=" + personId, null);
	}

	@Override
	public int update(IContact contact, IContact oldContact) {
		int uCount = 0;

		if (oldContact == null) 
			return uCount;

		ContentValues values = new ContentValues();

		String id = contact.getId();
		long personId = Long.valueOf(id);

		Uri contactUri = ContentUris.withAppendedId(
				Contacts.People.CONTENT_URI, Long.parseLong(id));
		if (!equalsStr(contact.getDisplayName(), oldContact.getDisplayName())) {
			if (contact.getDisplayName() == null)
				values.putNull(Contacts.People.NAME);
			else
				values.put(Contacts.People.NAME, contact.getDisplayName());
		}
		if (contact.isStarred() != oldContact.isStarred())
			values.put(Contacts.People.STARRED, contact.isStarred() ? 1 : 0);

		if (!equalsStr(contact.getRingtoneId(), oldContact.getRingtoneId())
				|| contact.isSysRingtone() != oldContact.isSysRingtone()) {
			String ringUri = getRingtoneUri(contact.getRingtoneId(),
					contact.isSysRingtone());

			if (ringUri == null)
				values.putNull(Contacts.People.CUSTOM_RINGTONE);
			else
				values.put(Contacts.People.CUSTOM_RINGTONE, ringUri);
		}

		String note = getOnlyNote(contact);
		String oldNote = oldContact.getNotes() != null
				&& oldContact.getNotes().length > 0 ? oldContact.getNotes()[0]
				.getValue() : null;

		if (!equalsStr(note, oldNote))
			values.put(Contacts.People.NOTES, note);

		if (values.size() > 0)
			uCount = contentResolver.update(contactUri, values, null, null);

		uCount += ContactHelper.operateItems(personId, contactNumber,
				contact.getNumbers(), oldContact.getNumbers(), itemCompare);
		uCount += ContactHelper.operateItems(personId, contactOrganization,
				contact.getOrganizations(), oldContact.getOrganizations(),
				organizationCompare);

		uCount += ContactHelper.operateItems(personId, contactEmail,
				contact.getEmails(), oldContact.getEmails(), itemCompare);
		// IM
		uCount += ContactHelper.operateItems(personId, contactIm,
				contact.getIMs(), oldContact.getIMs(), itemCompare);

		uCount += ContactHelper.operateItems(personId, contactAddress,
				contact.getAddresses(), oldContact.getAddresses(),
				addressCompare);

		uCount += ContactHelper.operateItems(personId, contactGroup,
				contact.getGroupId(), oldContact.getGroupId(), itemCompare);
		return uCount;
	}

	//private ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
	
	@Override
	public List<Uri> insert(IContact[] contacts) {
		
		List<Uri> uris = null;
//		ops.clear();
//		int inCount = 0;
		for (IContact contact : contacts) {
			uris = new ArrayList<Uri>();
			ContentValues values = new ContentValues();
			Uri newContactUri = contentResolver.insert(Contacts.People.CONTENT_URI,
					values);
			long contactId = ContentUris.parseId(newContactUri);
			contact.setId(String.valueOf(contactId));
			uris.add(newContactUri);
//			if (inCount > 35) {
//				inCount = 0;
//				try {
//					contentResolver.applyBatch(ContactsContract.AUTHORITY, ops);
//				} catch (RemoteException e) {
//					e.printStackTrace();
//				} catch (OperationApplicationException e) {
//					e.printStackTrace();
//				}
//			} else {
			
//		ContentValues values = new ContentValues();
//
//		Uri newContactUri = contentResolver.insert(Contacts.People.CONTENT_URI,
//				values);
//		long contactId = ContentUris.parseId(newContactUri);
//		contact.setId(String.valueOf(contactId));
//
		registerHeadUri(contact); 

		if (contact.getDisplayName() != null)
			values.put(Contacts.People.NAME, contact.getDisplayName());

		
		if (contact.getRingtoneId() != null) {
			String ringUri = getRingtoneUri(contact.getRingtoneId(),
					contact.isSysRingtone());

			if (ringUri != null)
				values.put(Contacts.People.CUSTOM_RINGTONE, ringUri);
		}

		//add the stared
		values.put(Contacts.People.STARRED, contact.isStarred() ? 1 : 0);

		//add the not message
		String note = getOnlyNote(contact);
		if (note != null)
			values.put(Contacts.People.NOTES, note);

		contentResolver.update(newContactUri, values, null, null);

		IOrganization[] organizationItem = contact.getOrganizations();
		contactOrganization.insert(contactId, organizationItem, values);

		Item[] groupItem = contact.getGroupId();
		contactGroup.insert(contactId, groupItem, values);

		Item[] numberItem = contact.getNumbers();
		contactNumber.insert(contactId, numberItem, values);

		Item[] emailItem = contact.getEmails();
		contactEmail.insert(contactId, emailItem, values);

		IAddress[] addressItem = contact.getAddresses();
		contactAddress.insert(contactId, addressItem, values);

		Item[] imItem = contact.getIMs();
		contactIm.insert(contactId, imItem, values);
	}

		return (uris);
//		return newContactUri;
}
	/**
	 * content://com.android.contacts/contacts/ +
	 * contact_id
	 */
	@Override
	public String registerHeadUri(IContact contact) {
		Uri uri = ContentUris.withAppendedId(Contacts.People.CONTENT_URI,
				Long.valueOf(contact.getId()));

		// Photos._id = People._id�����ǳ����Ͻ��Ƕȣ���Ȼ��ѯһ��

		String iconUri = uri.toString();
		contact.setIconPath(iconUri);

		return iconUri;
	}

	public boolean isValidPath(String headUri) {
		if (headUri != null) {
			String path_uri = headUri.toLowerCase();
			String content_uri = Contacts.People.CONTENT_URI.toString()
					.toLowerCase();
			return path_uri.startsWith(content_uri);
		} else
			return false;
	}

	public byte[] getBytes(String headUri) {
		byte[] data = null;

		long contact_id = ContentUris.parseId(Uri.parse(headUri));

		Cursor iconCursor = contentResolver
				.query(Contacts.Photos.CONTENT_URI,
						new String[] { Contacts.Photos.DATA },
						Contacts.Photos.PERSON_ID + "='" + contact_id + "'",
						null, null);

		if (iconCursor.moveToFirst()) {
			data = iconCursor.getBlob(0);
		}
		iconCursor.close();
		return data;
	}

	public void setBytes(String headUri, byte[] bytes) {
		long contact_id = ContentUris.parseId(Uri.parse(headUri));

		Cursor iconCursor = contentResolver.query(Contacts.Photos.CONTENT_URI,
				new String[] { Contacts.Photos._ID }, Contacts.Photos.PERSON_ID
						+ "='" + contact_id + "'", null, null);
		long photoId = -1;
		if (iconCursor.moveToFirst())
			photoId = iconCursor.getLong(0);

		iconCursor.close();

		ContentValues values = new ContentValues();
		if (bytes != null)
			values.put(Contacts.Photos.DATA, bytes);
		else
			values.putNull(Contacts.Photos.DATA); // ɾ��ͷ��

		if (photoId >= 0) {
			Uri photoUri = ContentUris.withAppendedId(
					Contacts.Photos.CONTENT_URI, photoId);
			contentResolver.update(photoUri, values, null, null);
		} else {
			values.put(Contacts.Photos.PERSON_ID, contact_id);
			contentResolver.insert(Contacts.Photos.CONTENT_URI, values);
		}
	}

	/** ��ȡ��ϵ�˻���Ϣ */
	private ArrayList<IContact> getAllBaseInfo() {
		ArrayList<IContact> listIContact = new ArrayList<IContact>();

		Cursor cursor = contentResolver.query(Contacts.People.CONTENT_URI,
				CONTACT_PROJECTION, null, null, Contacts.People._ID);
		if (cursor.moveToFirst()) {
			do {
				IContact contact = new TContact();
				fillBaseItemInfo(cursor, contact);

				registerHeadUri(contact);

				listIContact.add(contact);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return listIContact;
	}

	/** ��ȡ������ϵ�˻���Ϣ */
	public IContact getOneBaseInfo(IContact newContact) {
		IContact contact = null;

		long id = Long.valueOf(newContact.getId());
		Uri uri = ContentUris.withAppendedId(Contacts.People.CONTENT_URI, id);
		Cursor cursor = contentResolver.query(uri, CONTACT_PROJECTION, null,
				null, null);
		if (cursor.moveToFirst()) {
			contact = new TContact();
			fillBaseItemInfo(cursor, contact);
		}
		cursor.close();
		return contact;
	}

	/** �����ϵ�˻���Ϣ */
	private void fillBaseItemInfo(Cursor cursor, IContact contact) {
		contact.setId(cursor.getString(0));
		contact.setDisplayName(cursor.getString(1));
		contact.setIsStarred(cursor.getInt(4) != 0 ? true : false);
		String note = cursor.getString(2);
		if (note != null) {
			TItem item = new TItem();
			item.setValue(note);
			contact.addNote(item);
		}
		String ringUri = cursor.getString(3);
		fillRingtoneInfo(contact, ringUri);
	}

	public void fillAllOrganization(IContact[] contacts) {
		Cursor cursor = contentResolver.query(
				Contacts.Organizations.CONTENT_URI, ORGANIZATION_PROJECTION,
				null, null, Contacts.Organizations.PERSON_ID);
		if (cursor.moveToFirst()) {
			IContact lastContact = null;
			do {
				String contactId = cursor.getString(0);

				IContact contact = findContactsById(contacts, contactId,
						lastContact);
				if (contact != null)
					fillOrganizationItem(contact, cursor);
			} while (cursor.moveToNext());
		}
	}

	public void fillOneOrganization(IContact contact) {
		String where = Contacts.Organizations.PERSON_ID + "=" + contact.getId();

		Cursor cursor = contentResolver.query(
				Contacts.Organizations.CONTENT_URI, ORGANIZATION_PROJECTION,
				where, null, null);
		if (cursor.moveToFirst()) {
			do {
				fillOrganizationItem(contact, cursor);
			} while (cursor.moveToNext());

		}
		cursor.close();
	}

	public void fillOrganizationItem(IContact contact, Cursor cursor) {
		String company = cursor.getString(1);
		String title = cursor.getString(2);

		TOrganization organizationItem = new TOrganization();
		organizationItem.setCompany(company);
		organizationItem.setTitle(title);
		int type = cursor.getInt(3);
		if (type == OrganizationType.Custom.getCode())
			organizationItem.setKey(cursor.getString(4));
		else
			organizationItem.setKey(OrganizationType.toTypeString(type));

		String dataId = cursor.getString(5);
		organizationItem.setId(dataId);
		contact.addOrganization(organizationItem);
	}

	public void fillAllPhone(IContact[] contacts) {
		Cursor cursor = contentResolver.query(Contacts.Phones.CONTENT_URI,
				PHONE_PROJECTION, null, null, Contacts.Phones.PERSON_ID);
		if (cursor.moveToFirst()) {
			IContact lastContact = null;
			do {
				String contactId = cursor.getString(0);

				IContact contact = findContactsById(contacts, contactId,
						lastContact);
				if (contact != null)
					fillPhoneItem(contact, cursor);
			} while (cursor.moveToNext());
		}
		cursor.close();
	}

	public void fillOnePhone(IContact contact) {
		String where = Contacts.Phones.PERSON_ID + "=" + contact.getId();

		Cursor cursor = contentResolver.query(Contacts.Phones.CONTENT_URI,
				PHONE_PROJECTION, where, null, null);
		if (cursor.moveToFirst()) {
			do {
				fillPhoneItem(contact, cursor);
			} while (cursor.moveToNext());

		}
		cursor.close();
	}

	private void fillPhoneItem(IContact contact, Cursor cursor) {
		String value = cursor.getString(1);
		if (value != null) {
			TItem item = new TItem();
			item.setValue(value);
			int type = cursor.getInt(2);
			if (type == NumberType.Custom.getCode())
				item.setKey(cursor.getString(3));
			else
				item.setKey(NumberType.toTypeString(type));

			String dataId = cursor.getString(4);
			item.setId(dataId);
			contact.addNumber(item);
		}
	}

	private void fillAllGroupMembers(IContact[] contacts) {
		Cursor cursor = contentResolver.query(
				Contacts.GroupMembership.CONTENT_URI, GROUP_PROJECTION, null,
				null, Contacts.GroupMembership.PERSON_ID);
		if (cursor.moveToFirst()) {
			IContact lastContact = null;
			do {
				String contactId = cursor.getString(0);

				IContact contact = findContactsById(contacts, contactId,
						lastContact);
				if (contact != null)
					fillGroupMembersItem(contact, cursor);
			} while (cursor.moveToNext());
		}
		cursor.close();
	}

	private void fillOneGroupMembers(IContact contact) {
		String where = Contacts.GroupMembership.PERSON_ID + "="
				+ contact.getId();

		Cursor cursor = contentResolver.query(
				Contacts.GroupMembership.CONTENT_URI, GROUP_PROJECTION, where,
				null, null);
		if (cursor.moveToFirst()) {
			do {
				fillGroupMembersItem(contact, cursor);
			} while (cursor.moveToNext());
		}
		cursor.close();
	}

	private void fillGroupMembersItem(IContact contact, Cursor cursor) {
		String value = cursor.getString(1);
		if (value != null) {
			TItem item = new TItem();
			item.setValue(value);

			String dataId = cursor.getString(2);
			item.setId(dataId);
			contact.addGroupId(item);
		}
	}

	private void fillAllItemData(IContact[] contacts) {
		Cursor cursor = contentResolver.query(
				Contacts.ContactMethods.CONTENT_URI, ITEM_DATA_PROJECTION,
				null, null, Contacts.ContactMethods.PERSON_ID);
		if (cursor.moveToFirst()) {
			IContact lastContact = null;
			do {
				String contactId = cursor.getString(0);

				IContact contact = findContactsById(contacts, contactId,
						lastContact);
				if (contact != null)
					getContactDataItem(contact, cursor);
			} while (cursor.moveToNext());
		}
		cursor.close();
	}

	private void fillOneItemData(IContact contact) {
		String where = Contacts.ContactMethods.PERSON_ID + "="
				+ contact.getId();
		Cursor cursor = contentResolver.query(
				Contacts.ContactMethods.CONTENT_URI, ITEM_DATA_PROJECTION,
				where, null, null);
		if (cursor.moveToFirst()) {
			do {
				getContactDataItem(contact, cursor);
			} while (cursor.moveToNext());
		}
		cursor.close();
	}

	/** Email, IM, Address */
	private void getContactDataItem(IContact contact, Cursor cursor) {
		String dataId = cursor.getString(6);
		int kind = cursor.getInt(5);

		if (kind == Contacts.KIND_EMAIL) {
			String value = cursor.getString(1);
			if (value != null) {
				TItem item = new TItem();
				item.setValue(value);
				int type = cursor.getInt(2);
				if (type == EmailType.Custom.getCode())
					item.setKey(cursor.getString(3));
				else
					item.setKey(EmailType.toTypeString(type));

				item.setId(dataId);
				contact.addEmail(item);
			}
		} else if (kind == Contacts.KIND_IM) {
			String value = cursor.getString(1);
			if (value != null) {
				TItem item = new TItem();
				item.setValue(value);

				String auxdataStr = cursor.getString(4);
				Object auxdata = Contacts.ContactMethods
						.decodeImProtocol(auxdataStr);
				if (auxdata instanceof Integer) {
					int typeCode = (Integer) auxdata;
					String typeName = IMType.toTypeString(typeCode);
					item.setKey(typeName);
				} else {
					String customType = (String) auxdata;
					item.setKey(customType);
				}

				item.setId(dataId);
				contact.addIM(item);
			}
		} else if (kind == Contacts.KIND_POSTAL) {
			String value = cursor.getString(1);
			if (value != null) {
				TAddress adressItem = new TAddress();
				adressItem.setFormattedAddress(value);
				int type = cursor.getInt(2);
				if (type == AddressType.Custom.getCode())
					adressItem.setKey(cursor.getString(3));
				else
					adressItem.setKey(AddressType.toTypeString(type));

				adressItem.setId(dataId);
				contact.addAddress(adressItem);
			}
		}
	}

	private String getOnlyNote(IContact contact) {
		if (contact.getNotes() != null) {
			for (Item item : contact.getNotes()) {
				String value = item.getValue();
				if (value != null && value.length() > 0)
					return value;
			}
		}
		return null;
	}

	private abstract class TContactItem<T extends Item> implements
			IContactItem<T> {
		protected static final String C_AND = " AND ";
		private ArrayList<String> n_whereArgs;

		protected abstract String getWhere(long personId, T item);

		protected String getWhereSub(String column, String value) {
			if (value == null || value.length() == 0)
				return " (" + column + " IS NULL OR " + column + "='') ";
			else {
				if (n_whereArgs == null)
					n_whereArgs = new ArrayList<String>();

				n_whereArgs.add(value);

				return column + "=? ";
			}
		}

		private void clearWhereArgs() {
			n_whereArgs = null;
		}

		private String[] getWhereArgs() {
			return n_whereArgs == null || n_whereArgs.size() == 0 ? null
					: n_whereArgs.toArray(new String[0]);
		}

		protected abstract Uri getContentUri();

		@Override
		public Uri queryUri(long personId, T item) {
			clearWhereArgs();

			String where = getWhere(personId, item);
			String[] whereArgs = getWhereArgs();

			Uri uri = null, queryUri = getContentUri();
			Cursor cursor = contentResolver.query(queryUri, ITEM_ID_PROJECTION,
					where, whereArgs, null);

			if (cursor.moveToFirst())
				uri = ContentUris.withAppendedId(queryUri, cursor.getLong(0));

			cursor.close();
			return uri;
		}

		@Override
		public Uri insert(long personId, T item, ContentValues values) {
			insertTo(personId, item, values);

			Uri uri = getContentUri();
			Uri insertUri = contentResolver.insert(uri, values);
			if (insertUri != null) {
				long dataId = ContentUris.parseId(insertUri);
				item.setId(String.valueOf(dataId));
			}
			return insertUri;
		}

		public int insert(long personId, T[] item, ContentValues values) {
			int count = 0;
			if (item != null) {
				for (int j = 0; j < item.length; j++) {
					Uri uri = insert(personId, item[j], values);
					if (uri != null)
						count++;
				}
			}
			return count;
		}

		@Override
		public int update(long personId, T item, T oldItem, ContentValues values) {
			Uri uri = queryUri(personId, oldItem);
			if (uri != null) {
				updateTo(item, values, true);

				return contentResolver.update(uri, values, null, null);
			} else
				return 0;
		}

		@Override
		public int delete(long personId, T item) {
			Uri uri = queryUri(personId, item);
			if (uri != null) {
				return contentResolver.delete(uri, null, null);
			} else
				return 0;
		}
	}

	private class ContactAddress extends TContactItem<IAddress> implements
			IItemCompare<IAddress> {

		@Override
		protected String getWhere(long personId, IAddress item) {
			String dataId = item.getId();

			if (dataId != null)
				return Contacts.ContactMethods._ID + "=" + item.getId();

			String where = Contacts.ContactMethods.PERSON_ID + "=" + personId
					+ C_AND + Contacts.ContactMethods.KIND + "="
					+ Contacts.KIND_POSTAL;

			AddressType type = AddressType.valueOfIgnoreCase(item.getKey());

			where += C_AND + Contacts.ContactMethods.TYPE + "="
					+ type.getCode();
			if (type == AddressType.Custom)
				where += C_AND + Contacts.ContactMethods.LABEL + "='"
						+ item.getKey() + "'";

			where += C_AND
					+ getWhereSub(Contacts.ContactMethods.DATA,
							item.getFormattedAddress());

			return where;
		}

		@Override
		public void updateTo(IAddress item, ContentValues values,
				boolean clearValues) {
			if (clearValues)
				values.clear();

			if (item.getFormattedAddress() == null) {
				char fg = ' ';
				StringBuilder addBuilder = new StringBuilder();

				if (item.getStreet() != null)
					addBuilder.append(item.getStreet()).append(fg);

				if (item.getPobox() != null)
					addBuilder.append(item.getPobox()).append(fg);

				if (item.getNeighborhood() != null)
					addBuilder.append(item.getNeighborhood()).append(fg);

				if (item.getCity() != null) {
					addBuilder.append(item.getCity());

					if (item.getRegion() != null)
						addBuilder.append(',').append(fg)
								.append(item.getRegion()).append(fg);
					else
						addBuilder.append(fg);

				} else if (item.getRegion() != null)
					addBuilder.append(item.getRegion()).append(fg);

				if (item.getPostcode() != null)
					addBuilder.append(item.getPostcode()).append(fg);

				if (item.getCountry() != null)
					addBuilder.append(item.getCountry());

				String address = addBuilder.toString();

				address = address.trim();
				item.setFormattedAddress(address);
			}
			values.put(Contacts.ContactMethods.DATA, item.getFormattedAddress());
		}

		@Override
		public void insertTo(long personId, IAddress item, ContentValues values) {
			values.clear();
			values.put(Contacts.ContactMethods.PERSON_ID, personId);
			values.put(Contacts.ContactMethods.KIND, Contacts.KIND_POSTAL);

			updateTo(item, values, false);

			AddressType type = AddressType.valueOfIgnoreCase(item.getKey());

			values.put(Contacts.ContactMethods.TYPE, type.getCode());
			if (type == AddressType.Custom)
				values.put(Contacts.ContactMethods.LABEL, item.getKey());
		}

		@Override
		protected Uri getContentUri() {
			return Contacts.ContactMethods.CONTENT_URI;
		}

		@Override
		public boolean compareKey(IAddress pcItem, IAddress phoneItem) {
			return equalsStr(pcItem.getKey(), phoneItem.getKey());
		}

		@Override
		public boolean compareValue(IAddress pcItem, IAddress phoneItem) {
			return equalsStr(pcItem.getFormattedAddress(),
					phoneItem.getFormattedAddress());
		}
	}

	private class ContactIm extends TContactItem<Item> {
		@Override
		protected String getWhere(long personId, Item item) {
			String dataId = item.getId();

			if (dataId != null)
				return Contacts.ContactMethods._ID + "=" + item.getId();

			String where = Contacts.ContactMethods.PERSON_ID + "=" + personId
					+ C_AND + Contacts.ContactMethods.KIND + "="
					+ Contacts.KIND_IM;

			IMType type = IMType.valueOfIgnoreCase(item.getKey());

			String auxdataStr = type == IMType.Custom ? Contacts.ContactMethods
					.encodeCustomImProtocol(item.getKey())
					: Contacts.ContactMethods.encodePredefinedImProtocol(type
							.getCode());
			where += C_AND
					+ Contacts.ContactMethods.AUX_DATA
					+ "='"
					+ auxdataStr
					+ "' AND "
					+ getWhereSub(Contacts.ContactMethods.DATA, item.getValue());

			return where;
		}

		@Override
		public void updateTo(Item item, ContentValues values,
				boolean clearValues) {
			if (clearValues)
				values.clear();

			values.put(Contacts.ContactMethods.DATA, item.getValue());
		}

		@Override
		public void insertTo(long personId, Item item, ContentValues values) {
			values.clear();
			values.put(Contacts.ContactMethods.PERSON_ID, personId);
			values.put(Contacts.ContactMethods.KIND, Contacts.KIND_IM);
			values.put(Contacts.ContactMethods.ISPRIMARY, 0);

			updateTo(item, values, false);

			IMType type = IMType.valueOfIgnoreCase(item.getKey());
			String auxdataStr;
			if (type == IMType.Custom) {
				// values.put(Contacts.ContactMethods.TYPE, 0);
				// values.put(Contacts.ContactMethods.LABEL, item.getName());
				auxdataStr = Contacts.ContactMethods
						.encodeCustomImProtocol(item.getKey());
			} else {
				// values.put(Contacts.ContactMethods.TYPE, 2);
				// values.put(Contacts.ContactMethods.AUX_DATA, "pre:" +
				// type.getCode());
				auxdataStr = Contacts.ContactMethods
						.encodePredefinedImProtocol(type.getCode());
			}
			values.put(Contacts.ContactMethods.AUX_DATA, auxdataStr);
			values.put(Contacts.ContactMethods.TYPE,
					Contacts.ContactMethods.TYPE_OTHER);
		}

		@Override
		protected Uri getContentUri() {
			return Contacts.ContactMethods.CONTENT_URI;
		}
	}

	private class ContactEmail extends TContactItem<Item> {
		@Override
		protected String getWhere(long personId, Item item) {
			String dataId = item.getId();

			if (dataId != null)
				return Contacts.ContactMethods._ID + "=" + item.getId();

			String where = Contacts.ContactMethods.PERSON_ID + "=" + personId
					+ C_AND + Contacts.ContactMethods.KIND + "="
					+ Contacts.KIND_EMAIL;

			EmailType type = EmailType.valueOfIgnoreCase(item.getKey());

			where += C_AND + Contacts.ContactMethods.TYPE + "="
					+ type.getCode();
			if (type == EmailType.Custom)
				where += C_AND + Contacts.ContactMethods.LABEL + "='"
						+ item.getKey() + "'";

			where += C_AND
					+ getWhereSub(Contacts.ContactMethods.DATA, item.getValue());

			return where;
		}

		@Override
		public void updateTo(Item item, ContentValues values,
				boolean clearValues) {
			if (clearValues)
				values.clear();

			values.put(Contacts.ContactMethods.DATA, item.getValue());
		}

		@Override
		public void insertTo(long personId, Item item, ContentValues values) {
			values.clear();
			values.put(Contacts.ContactMethods.PERSON_ID, personId);
			values.put(Contacts.ContactMethods.KIND, Contacts.KIND_EMAIL);

			updateTo(item, values, false);

			EmailType type = EmailType.valueOfIgnoreCase(item.getKey());
			values.put(Contacts.ContactMethods.TYPE, type.getCode());
			if (type == EmailType.Custom)
				values.put(Contacts.ContactMethods.LABEL, item.getKey());
		}

		@Override
		protected Uri getContentUri() {
			return Contacts.ContactMethods.CONTENT_URI;
		}
	}

	private class ContactOrganization extends TContactItem<IOrganization>
			implements IItemCompare<IOrganization> {
		@Override
		protected String getWhere(long personId, IOrganization item) {
			String dataId = item.getId();

			if (dataId != null)
				return Contacts.Organizations._ID + "=" + item.getId();

			String where = Contacts.Organizations.PERSON_ID + "='" + personId
					+ "'";

			OrganizationType type = OrganizationType.valueOfIgnoreCase(item
					.getKey());

			where += C_AND + Contacts.Organizations.TYPE + "=" + type.getCode();
			if (type == OrganizationType.Custom)
				where += C_AND + Contacts.Organizations.LABEL + "='"
						+ item.getKey() + "'";

			where += C_AND
					+ getWhereSub(Contacts.Organizations.COMPANY,
							item.getCompany());
			where += C_AND
					+ getWhereSub(Contacts.Organizations.TITLE, item.getTitle());

			return where;
		}

		@Override
		public void updateTo(IOrganization item, ContentValues values,
				boolean clearValues) {
			if (clearValues)
				values.clear();

			values.put(Contacts.Organizations.COMPANY, item.getCompany());
			values.put(Contacts.Organizations.TITLE, item.getTitle());
		}

		@Override
		public void insertTo(long personId, IOrganization item,
				ContentValues values) {
			values.clear();
			values.put(Contacts.Organizations.PERSON_ID, personId);

			updateTo(item, values, false);

			OrganizationType type = OrganizationType.valueOfIgnoreCase(item
					.getKey());

			values.put(Contacts.Organizations.TYPE, type.getCode());
			if (type == OrganizationType.Custom)
				values.put(Contacts.Organizations.LABEL, item.getKey());
		}

		@Override
		protected Uri getContentUri() {
			return Contacts.Organizations.CONTENT_URI;
		}

		@Override
		public boolean compareKey(IOrganization pcItem, IOrganization phoneItem) {
			return equalsStr(pcItem.getKey(), phoneItem.getKey());
		}

		@Override
		public boolean compareValue(IOrganization pcItem,
				IOrganization phoneItem) {
			return equalsStr(pcItem.getCompany(), phoneItem.getCompany())
					&& equalsStr(pcItem.getTitle(), phoneItem.getTitle());
		}
	}

	private class ContactNumber extends TContactItem<Item> {
		@Override
		protected String getWhere(long personId, Item item) {
			String dataId = item.getId();

			if (dataId != null)
				return Contacts.Phones._ID + "=" + item.getId();

			String where = Contacts.Phones.PERSON_ID + "=" + personId;

			NumberType type = NumberType.valueOfIgnoreCase(item.getKey());

			where += C_AND + Contacts.Phones.TYPE + "=" + type.getCode();
			if (type == NumberType.Custom)
				where += C_AND + Contacts.Phones.LABEL + "='" + item.getKey()
						+ "'";

			where += C_AND
					+ getWhereSub(Contacts.Phones.NUMBER, item.getValue());

			return where;
		}

		@Override
		public void updateTo(Item item, ContentValues values,
				boolean clearValues) {
			if (clearValues)
				values.clear();

			values.put(Contacts.Phones.NUMBER, item.getValue());
		}

		@Override
		public void insertTo(long personId, Item item, ContentValues values) {
			values.clear();
			values.put(Contacts.Phones.PERSON_ID, personId);

			updateTo(item, values, false);

			NumberType type = NumberType.valueOfIgnoreCase(item.getKey());

			values.put(Contacts.Phones.TYPE, type.getCode());
			if (type == NumberType.Custom)
				values.put(Contacts.Phones.LABEL, item.getKey());
		}

		@Override
		protected Uri getContentUri() {
			return Contacts.Phones.CONTENT_URI;
		}
	}

	private class ContactGroup extends TContactItem<Item> {
		@Override
		protected String getWhere(long personId, Item item) {
			String dataId = item.getId();

			if (dataId != null)
				return Contacts.GroupMembership._ID + "=" + item.getId();

			String where = Contacts.GroupMembership.PERSON_ID
					+ "='"
					+ personId
					+ "' AND "
					+ getWhereSub(Contacts.GroupMembership.GROUP_ID,
							item.getValue());

			return where;
		}

		@Override
		public int update(long personId, Item item, Item oldItem,
				ContentValues values) {
			delete(personId, oldItem);

			Uri uri = insert(personId, item, values);
			return uri == null ? 0 : 1;
		}

		@Override
		public Uri insert(long personId, Item item, ContentValues values) {
			String groupId = item.getValue();

			return Contacts.People.addToGroup(contentResolver, personId,
					Long.parseLong(groupId));
		}

		@Override
		protected Uri getContentUri() {
			return Contacts.GroupMembership.CONTENT_URI;
		}

		@Override
		public void updateTo(Item item, ContentValues values,
				boolean clearValues) {
			// Ⱥ��û�и���
		}

		@Override
		public void insertTo(long personId, Item item, ContentValues values) {
		}
	}

	@Override
	public Uri insert(IContact contact) {
		// TODO Auto-generated method stub
		return null;
	}
}
