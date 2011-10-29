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
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.SipAddress;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.Website;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.RawContactsEntity;
import android.util.Log;

import com.bee.br.phone.ContactHelper.IContactItem;
import com.bee.br.phone.ContactHelper.IItemCompare;
import com.bee.br.utils.SysConst;

public class ContactManager extends ContactFactory {

	public static final int BATCH_SIZE = 100;

	/** Contacts._ID, STARRED, RINGTONE */
	private static final String Base_PROJECTION[] = { Contacts._ID,
			Contacts.STARRED, Contacts.CUSTOM_RINGTONE };

	// /** _ID, CONTACT_ID, ACCOUNT_NAME, TYPE */
	// private static final String[] RAW_ACCOUNT_PROJECTION = { RawContacts._ID,
	// RawContacts.CONTACT_ID, RawContacts.ACCOUNT_NAME,
	// RawContacts.ACCOUNT_TYPE };
	/** CONTACT_ID, DATA1(..)10, DATA_ID, MIMETYPE, RAW_CONTACT_ID */
	private static final String EntityDATA_PROJECTION[] = {
			RawContactsEntity.CONTACT_ID, RawContactsEntity.DATA1,
			RawContactsEntity.DATA2, RawContactsEntity.DATA3,
			RawContactsEntity.DATA4, RawContactsEntity.DATA5,
			RawContactsEntity.DATA6, RawContactsEntity.DATA7,
			RawContactsEntity.DATA8, RawContactsEntity.DATA9,
			RawContactsEntity.DATA10, RawContactsEntity.DATA_ID,
			RawContactsEntity.MIMETYPE, RawContactsEntity._ID };

	// /** RawContacts._ID */
	// private static final String[] RawID_PROJECTION = new String[] {
	// RawContacts._ID };
	/** Data._ID */
	private static final String[] DATAID_PROJECTION = new String[] { Data._ID };
	/** Photo.PHOTO */
	private static final String[] PHOTO_PROJECTION = new String[] { Photo.PHOTO };

	// �Ը�Item������
	private ContactNumber contactNumber;
	private ContactOrganization contactOrganization;
	private ContactEmail contactEmail;
	private ContactIm contactIm;
	private ContactAddress contactAddress;
	private ContactGroup contactGroup;
	private ContactWebsite contactWebsite;
	private ContactNickname contactNickname;
	private ContactNote contactNote;
	private ContactName contactName;
	private ContactEvent contactEvent;
	private ContactSipAddress contactSipAddress;

	/**
	 * compare the tow item
	 */
	private ItemCompare itemCompare;
	private IItemCompare<IOrganization> organizationCompare;
	private IItemCompare<IAddress> addressCompare;

	public ContactManager(Context context) {
		super(context);

		contactNumber = new ContactNumber();
		contactOrganization = new ContactOrganization();
		contactEmail = new ContactEmail();
		contactIm = new ContactIm();
		contactAddress = new ContactAddress();
		contactGroup = new ContactGroup();
		contactWebsite = new ContactWebsite();
		contactNickname = new ContactNickname();
		contactNote = new ContactNote();
		contactName = new ContactName();
		contactEvent = new ContactEvent();

		if (SysConst.SysVersion() >= 2.3f)
			contactSipAddress = new ContactSipAddress();

		itemCompare = new ItemCompare();
		organizationCompare = contactOrganization;
		addressCompare = contactAddress;
	}

	@Override
	public int getCount() {
		int count = 0;

		Cursor cursor = contentResolver.query(Contacts.CONTENT_URI,
				new String[] { Contacts._ID }, null, null, null);
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
			fillAllItemData(contacts);
			return contacts;
		}
		return null;
	}

	@Override
	public void getAllContacts(ICutListener listener) {
		ArrayList<IContact> contacts = getAllBaseInfo();

		int rowCount = contacts == null ? 0 : contacts.size();

		if (rowCount > 0) {
			int count = listener.getTimeCount(rowCount);
			int oneMax = listener.getOneMax();

			Cursor cursor = contentResolver.query(
					RawContactsEntity.CONTENT_URI, EntityDATA_PROJECTION,
					RawContactsEntity.CONTACT_ID + " IS NOT NULL ", null,
					RawContactsEntity.CONTACT_ID);

			if (cursor != null) {
				if (cursor.moveToFirst()) {
					ArrayList<IContact> subContacts = new ArrayList<IContact>(
							oneMax);
					IContact lastContact;
					long lastContactId;
					int index = 0;

					while (contacts.size() > 0) {
						lastContact = contacts.get(0);
						lastContactId = Long.valueOf(lastContact.getId());

						do {
							long contactId = cursor.getLong(0);

							if (lastContactId == contactId) {

								if (lastContact.getRawId() == null) {
									lastContact.setRawId(cursor.getString(13)); // �����ż���
																				// Raw_ID
									registerHeadUri(lastContact);
								}

								String mimetype = cursor.getString(12);
								fillContactDataItem(lastContact, cursor,
										mimetype);

							} else {
								if (lastContactId < contactId) // OEM Contact
									break; // next contact
							}

						} while (cursor.moveToNext());

						subContacts.add(lastContact);
						contacts.remove(0);

						if (subContacts.size() >= oneMax) {
							subContacts.toArray(new IContact[0]);

							subContacts.clear();
							index++;

							if (index >= count)
								break;
						}
					}
					cursor.close();

					if (subContacts.size() > 0) {
						subContacts.toArray(new IContact[0]);

						subContacts.clear();
					}
					return;
				}
				cursor.close();
			}
		}
	}

	@Override
	public IContact getOldContact(IContact newContact) {
		IContact contact = getOneBaseInfo(newContact);
		if (contact != null)
			fillOneItemData(contact);

		return contact;
	}

	private ArrayList<IContact> getAllBaseInfo() {
		ArrayList<IContact> listIContact = new ArrayList<IContact>();

		Cursor cursor = contentResolver.query(Contacts.CONTENT_URI,
				Base_PROJECTION, null, null, Contacts._ID);
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				do {
					String id = cursor.getString(0);

					IContact contact = new TContact();
					contact.setId(id);
					contact.setIsStarred(cursor.getInt(1) != 0 ? true : false);

					fillRingtoneInfo(contact, cursor.getString(2));
					// registerHeadUri(contact);

					listIContact.add(contact);
				} while (cursor.moveToNext());
			}
			cursor.close();
		}
		return listIContact;
	}

	private IContact getOneBaseInfo(IContact newContact) {
		Cursor cursor = contentResolver.query(Contacts.CONTENT_URI,
				Base_PROJECTION, Contacts._ID + "=" + newContact.getId(), null,
				null);

		IContact contact = null;
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				contact = new TContact();

				contact.setId(newContact.getId());
				contact.setIsStarred(cursor.getInt(1) != 0 ? true : false);

				fillRingtoneInfo(contact, cursor.getString(2));
			}
			cursor.close();
		}
		return contact;
	}

	private void fillAllItemData(IContact[] contacts) {
		Cursor cursor = contentResolver.query(RawContactsEntity.CONTENT_URI,
				EntityDATA_PROJECTION, RawContactsEntity.CONTACT_ID
						+ " IS NOT NULL ", null, RawContactsEntity.CONTACT_ID);

		if (cursor != null) {
			if (cursor.moveToFirst()) {
				IContact lastContact = null;
				do {
					String contactId = cursor.getString(0);

					IContact contact = findContactsById(contacts, contactId,
							lastContact);

					if (contact != null) {
						if (contact.getRawId() == null) {
							contact.setRawId(cursor.getString(13)); // �����ż���
																	// Raw_ID
							registerHeadUri(contact);
						}
						String mimetype = cursor.getString(12);
						fillContactDataItem(contact, cursor, mimetype);
					}
				} while (cursor.moveToNext());
			}
			cursor.close();
		}
	}

	private void fillOneItemData(IContact contact) {
		Cursor cursor = contentResolver.query(RawContactsEntity.CONTENT_URI,
				EntityDATA_PROJECTION, RawContactsEntity.CONTACT_ID + "="
						+ contact.getId(), null, null);
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				do {
					if (contact.getRawId() == null) {
						contact.setRawId(cursor.getString(13)); // �����ż���
																// Raw_ID
						registerHeadUri(contact);
					}
					String mimetype = cursor.getString(12);
					fillContactDataItem(contact, cursor, mimetype);
				} while (cursor.moveToNext());
			}
			cursor.close();
		}
	}

	private void fillContactDataItem(IContact contact, Cursor cursor,
			String mimetype) {
		if (mimetype == null)
			return;

		String data_id = cursor.getString(11);

		if (mimetype.equals(StructuredName.CONTENT_ITEM_TYPE)) {
			contactName.fillItem(cursor, contact);

		} else if (mimetype.equals(Phone.CONTENT_ITEM_TYPE)) {
			TItem item = new TItem();
			item.setId(data_id);

			if (contactNumber.fillItem(cursor, item))
				contact.addNumber(item);

		} else if (mimetype.equals(Email.CONTENT_ITEM_TYPE)) {
			TItem item = new TItem();
			item.setId(data_id);

			if (contactEmail.fillItem(cursor, item))
				contact.addEmail(item);

		} else if (mimetype.equals(Im.CONTENT_ITEM_TYPE)) {
			TItem item = new TItem();
			item.setId(data_id);

			if (contactIm.fillItem(cursor, item))
				contact.addIM(item);

		} else if (mimetype.equals(StructuredPostal.CONTENT_ITEM_TYPE)) {
			TAddress item = new TAddress();
			item.setId(data_id);

			if (contactAddress.fillItem(cursor, item))
				contact.addAddress(item);

		} else if (mimetype.equals(Organization.CONTENT_ITEM_TYPE)) {
			TOrganization item = new TOrganization();
			item.setId(data_id);

			if (contactOrganization.fillItem(cursor, item))
				contact.addOrganization(item);

		} else if (mimetype.equals(Note.CONTENT_ITEM_TYPE)) {
			TItem item = new TItem();
			item.setId(data_id);

			if (contactNote.fillItem(cursor, item))
				contact.addNote(item);

		} else if (mimetype.equals(Website.CONTENT_ITEM_TYPE)) {
			TItem item = new TItem();
			item.setId(data_id);

			if (contactWebsite.fillItem(cursor, item))
				contact.addWebsite(item);

		} else if (mimetype.equals(Nickname.CONTENT_ITEM_TYPE)) {
			TItem item = new TItem();
			item.setId(data_id);

			if (contactNickname.fillItem(cursor, item))
				contact.addNickname(item);

		} else if (mimetype.equals(GroupMembership.CONTENT_ITEM_TYPE)) {
			TItem item = new TItem();
			item.setId(data_id);

			if (contactGroup.fillItem(cursor, item))
				contact.addGroupId(item);

		} else if (mimetype.equals(Event.CONTENT_ITEM_TYPE)) {
			TItem item = new TItem();
			item.setId(data_id);

			if (contactEvent.fillItem(cursor, item))
				contact.addEvent(item);

			// } else if (mimetype.equals(Photo.CONTENT_ITEM_TYPE)) {
			// } else if (mimetype.equals(Relation.CONTENT_ITEM_TYPE)) {
		} else if (contactSipAddress != null
				&& mimetype.equals(contactSipAddress.getMimeType())) {
			TItem item = new TItem();
			item.setId(data_id);

			if (contactSipAddress.fillItem(cursor, item))
				contact.addSipAddress(item);
		}
	}

	@Override
	public int deleteAll() {
		int delCount = 0;
		int commitFlag = 0;
		Cursor cursor = contentResolver.query(Contacts.CONTENT_URI,
				new String[] { Contacts._ID }, null, null, null);
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				do {
					if (commitFlag > BATCH_SIZE) {
						commitFlag = 0;
						deleteContacts(ops);
						ops.clear();
					}
					String id = cursor.getString(0);
					ops.add(ContentProviderOperation
							.newDelete(RawContacts.CONTENT_URI)
							.withSelection(RawContacts.CONTACT_ID + "=?",
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
			if (commitFlag > BATCH_SIZE) {
				commitFlag = 0;
				deleteContacts(ops);
				ops.clear();
			}
			ops.add(ContentProviderOperation
					.newDelete(RawContacts.CONTENT_URI)
					.withSelection(RawContacts.CONTACT_ID + "=?",
							new String[] { contact.getId() }).build());
			delCount++;
			commitFlag++;
		}
		deleteContacts(ops);
		return (delCount);
	}

	/**
	 * delete all the contacts person information
	 * 
	 * @param patch
	 * @return
	 */
	private int deleteContacts(ArrayList<ContentProviderOperation> patch) {
		// int dCount = contentResolver.delete(RawContacts.CONTENT_URI,
		// RawContacts.CONTACT_ID + "=" + contactId, null);
		int len = 0;
		Log.d("liyajie", "the delete batch size" + patch.size());
		ContentProviderResult[] datas = null;
		try {
			datas = contentResolver.applyBatch(ContactsContract.AUTHORITY,
					patch);
			len = datas.length;
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			e.printStackTrace();
		}
		return (len);
	}

	@Override
	public int update(IContact contact, IContact oldContact) {
		int uCount = 0;

		if (oldContact == null) 
			return uCount;

		long contactId = Long.valueOf(contact.getId());

		Uri uri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);

		ContentValues values = new ContentValues();
		{
			if (!equalsStr(contact.getRingtoneId(), oldContact.getRingtoneId())
					|| contact.isSysRingtone() != oldContact.isSysRingtone()) {
				String ringUri = getRingtoneUri(contact.getRingtoneId(),
						contact.isSysRingtone());
				if (ringUri == null)
					values.putNull(Contacts.CUSTOM_RINGTONE);
				else
					values.put(Contacts.CUSTOM_RINGTONE, ringUri);
			}

			if (contact.isStarred() != oldContact.isStarred())
				values.put(Contacts.STARRED, contact.isStarred() ? 1 : 0);

			if (values.size() > 0)
				uCount = contentResolver.update(uri, values, null, null); 
		}
		long raw_id = 0;
		if (contact.getRawId() != null)
			raw_id = Long.valueOf(contact.getRawId());

		if (raw_id <= 0) {
			raw_id = Long.valueOf(oldContact.getRawId());
			// raw_id = transferId(contactId);

			contact.setRawId(String.valueOf(raw_id));
		}
		/* update the contact name information */
		if (!contactName.hasName(contact)) {
			contactName.delete(raw_id, contact); 
		} else if (!contactName.compareName(contact, oldContact)) {
			uCount = contactName.update(raw_id, contact, oldContact, values);

			if (uCount == 0)
				contactName.insert(raw_id, contact, values);
		}

		/******************** the phone number **********************/
		uCount += ContactHelper.operateItems(raw_id, contactNumber,
				contact.getNumbers(), oldContact.getNumbers(), itemCompare);

		/******************** the email information *************************/
		uCount += ContactHelper.operateItems(raw_id, contactEmail,
				contact.getEmails(), oldContact.getEmails(), itemCompare);

		/******************** the conmunication information *************************/
		uCount += ContactHelper.operateItems(raw_id, contactIm,
				contact.getIMs(), oldContact.getIMs(), itemCompare);

		uCount += ContactHelper.operateItems(raw_id, contactAddress,
				contact.getAddresses(), oldContact.getAddresses(),
				addressCompare);

		uCount += ContactHelper.operateItems(raw_id, contactOrganization,
				contact.getOrganizations(), oldContact.getOrganizations(),
				organizationCompare);

		uCount += ContactHelper.operateItems(raw_id, contactNote,
				contact.getNotes(), oldContact.getNotes(), itemCompare);

		uCount += ContactHelper.operateItems(raw_id, contactNickname,
				contact.getNickNames(), oldContact.getNickNames(), itemCompare);

		uCount += ContactHelper.operateItems(raw_id, contactWebsite,
				contact.getWebsites(), oldContact.getWebsites(), itemCompare);

		uCount += ContactHelper.operateItems(raw_id, contactGroup,
				contact.getGroupId(), oldContact.getGroupId(), itemCompare);

		uCount += ContactHelper.operateItems(raw_id, contactEvent,
				contact.getEvent(), oldContact.getEvent(), itemCompare);

		if (contactSipAddress != null) {
			uCount += ContactHelper.operateItems(raw_id, contactSipAddress,
					contact.getSipAddresses(), oldContact.getSipAddresses(),
					itemCompare);
		}
		return uCount;
	}

	@Override
	public List<Uri> insert(final IContact[] contacts) {
		final List<Uri> uris = new ArrayList<Uri>();
		//it cost 106s to run this ok
		Thread t = new Thread() {
			public void run() {
				for (IContact contact : contacts) {
					ContentValues values = new ContentValues();
					/* the aggreation mode */
					values.put(RawContacts.AGGREGATION_MODE,
							RawContacts.AGGREGATION_MODE_DISABLED);
					// if (inCount > 80) {
					// inCount = 0;
					// try {
					// contentResolver.applyBatch(ContactsContract.AUTHORITY,
					// ops);
					// ops.clear();
					// } catch (RemoteException e) {
					// e.printStackTrace();
					// } catch (OperationApplicationException e) {
					// e.printStackTrace();
					// }
					// } else {
					/**
					 * add the batch operation now add the insert handle
					 */
					// ContentProviderOperation emptyInsert =
					// ContentProviderOperation
					// .newInsert(RawContacts.CONTENT_URI).withValues(values)
					// .build();
					Uri rawContactUri = contentResolver.insert(
							RawContacts.CONTENT_URI, values);
					// Uri rawContactUri = emptyInsert.getUri();
					/**
					 * add the batch operation now add the insert handle
					 */
					// ops.add(emptyInsert);

					long rawContactId = ContentUris.parseId(rawContactUri);
					contact.setRawId(String.valueOf(rawContactId));

					String contactId = transferContactId(rawContactId);
					Uri contactUri = ContentUris.withAppendedId(
							Contacts.CONTENT_URI, Long.parseLong(contactId));
					/**
					 * add the detail contact url to the uris
					 */
					uris.add(contactUri);
					contact.setId(contactId);

					registerHeadUri(contact);

					// get the ringtone uri
					String ringUri = getRingtoneUri(contact.getRingtoneId(),
							contact.isSysRingtone());
					if (ringUri != null)
						values.put(Contacts.CUSTOM_RINGTONE, ringUri);

					// is starred mark
					values.put(Contacts.STARRED, contact.isStarred() ? 1 : 0);

					// ops.add(ContentProviderOperation.newUpdate(contactUri)
					// .withValues(values).build());
					contentResolver.update(contactUri, values, null, null);

					// insert data to tables
					contactName.insert(rawContactId, contact, values);

					// add the phone number
					Item[] numberItem = contact.getNumbers();
					contactNumber.insert(rawContactId, numberItem, values);

					// add emails
					Item[] emailItem = contact.getEmails();
					contactEmail.insert(rawContactId, emailItem, values);

					// add the communication message
					Item[] imItem = contact.getIMs();
					contactIm.insert(rawContactId, imItem, values);

					// add addresss message
					IAddress[] addressItem = contact.getAddresses();
					contactAddress.insert(rawContactId, addressItem, values);

					// add the oragization group information
					IOrganization[] organizationItem = contact
							.getOrganizations();
					contactOrganization.insert(rawContactId, organizationItem,
							values);

					// the note information
					Item[] noteItem = contact.getNotes();
					contactNote.insert(rawContactId, noteItem, values);

					// add the name information
					Item[] nickNameItem = contact.getNickNames();
					contactNickname.insert(rawContactId, nickNameItem, values);

					// website information
					Item[] websitesItem = contact.getWebsites();
					contactWebsite.insert(rawContactId, websitesItem, values);

					Item[] groupItem = contact.getGroupId();
					contactGroup.insert(rawContactId, groupItem, values);

					/* htc have the event columns */
					Item[] eventItems = contact.getEvent();
					contactEvent.insert(rawContactId, eventItems, values);

					if (contactSipAddress != null) {
						Item[] sipItems = contact.getSipAddresses();
						contactSipAddress
								.insert(rawContactId, sipItems, values);
					}
				}
			}
		};
		t.start();
		try {
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return (uris);
	}

	@Override
	public String registerHeadUri(IContact contact) {
		String row_id = contact.getRawId();
		Uri uri = ContentUris.withAppendedId(RawContacts.CONTENT_URI,
				Long.valueOf(row_id));

		String headUri = uri.toString();

		contact.setIconPath(headUri);

		return headUri;
	}

	public boolean isValidPath(String headUri) {
		if (headUri != null) {
			String path_uri = headUri.toLowerCase();

			String content_uri = RawContacts.CONTENT_URI.toString()
					.toLowerCase();
			boolean isSame = path_uri.startsWith(content_uri);

			// if (!isSame) {
			// content_uri = Data.CONTENT_URI.toString().toLowerCase();
			// isSame = path_uri.startsWith(content_uri);
			// }
			// if (!isSame) {
			// content_uri = Contacts.CONTENT_URI.toString().toLowerCase();
			// isSame = path_uri.startsWith(content_uri);
			// }
			return isSame;
		}
		return false;
	}

	public byte[] getBytes(String headUri) {
		byte[] data = null;

		// long contact_id = ContentUris.parseId(Uri.parse(headUri));
		// long raw_contact_id = transferId(contact_id); // ��ȡ�׸�
		// raw_contact_id
		long raw_contact_id = ContentUris.parseId(Uri.parse(headUri));

		// RawContactsEntity ���� data ��� RawContacts ��ĺϼ�
		String where = Data.RAW_CONTACT_ID + "=" + raw_contact_id + " AND "
				+ Data.MIMETYPE + "='" + Photo.CONTENT_ITEM_TYPE + "'";

		Cursor cursor = contentResolver.query(Data.CONTENT_URI,
				PHOTO_PROJECTION, where, null, null);
		if (cursor.moveToFirst()) {
			do {
				data = cursor.getBlob(0);

				if (data != null && data.length != 0)
					break;
			} while (cursor.moveToNext());
		}
		cursor.close();
		return data;
	}

	public void setBytes(String headUri, byte[] bytes) {
		if (headUri == null)
			return;

		// long contact_id = ContentUris.parseId(Uri.parse(headUri));
		// long raw_contact_id = transferId(contact_id); // ��ȡ�׸�
		// raw_contact_id
		long raw_contact_id = ContentUris.parseId(Uri.parse(headUri));

		long photoRow = getIndexForPhoto(raw_contact_id); // ���һ�����ж��ͼƬ�ж�Ӧ�����ַ�ʽ�����ʺ�

		ContentValues values = new ContentValues();

		if (bytes != null)
			values.put(Photo.PHOTO, bytes);
		else
			values.putNull(Photo.PHOTO); // ɾ��ͷ��

		if (photoRow >= 0) {
			Uri photoUri = ContentUris.withAppendedId(Data.CONTENT_URI,
					photoRow);
			contentResolver.update(photoUri, values, null, null);
		} else {
			values.put(Data.RAW_CONTACT_ID, raw_contact_id);
			values.put(Data.IS_SUPER_PRIMARY, 1);
			values.put(Data.MIMETYPE, Photo.CONTENT_ITEM_TYPE);

			contentResolver.insert(Data.CONTENT_URI, values);
		}
	}

	/** ��ȡ��Ƭ�ı������ */
	private long getIndexForPhoto(long rawContactId) {
		String select = Data.RAW_CONTACT_ID + "=" + rawContactId + " AND "
				+ Data.MIMETYPE + "='" + Photo.CONTENT_ITEM_TYPE + "'";

		Cursor cursor = contentResolver.query(Data.CONTENT_URI,
				DATAID_PROJECTION, select, null, null);

		long index = -1;
		if (cursor.moveToFirst())
			index = cursor.getLong(0);

		cursor.close();
		return index;
	}

	// /** ���� contactId ��Ӧ���׸� rawContactId */
	// private long transferId(long contactId) {
	// Cursor rawcontactCursor = contentResolver.query(
	// RawContacts.CONTENT_URI, RawID_PROJECTION, RawContacts.DELETED
	// + "=0 AND " + RawContacts.CONTACT_ID + "=" + contactId,
	// null, RawContacts._ID);
	// long rawContactId = -1;
	// if (rawcontactCursor.moveToFirst())
	// rawContactId = rawcontactCursor.getLong(0);
	// rawcontactCursor.close();
	//
	// return rawContactId;
	// }

	/**
	 * @param rawcontatctId
	 * @return
	 */
	private String transferContactId(long rawcontatctId) {
		Uri uri = ContentUris.withAppendedId(RawContacts.CONTENT_URI,
				rawcontatctId);
		Cursor rawcontactCursor = contentResolver.query(uri,
				new String[] { RawContacts.CONTACT_ID }, null, null, null);

		String contactId = null;
		if (rawcontactCursor.moveToFirst())
			contactId = rawcontactCursor.getString(0);

		rawcontactCursor.close();
		return contactId;
	}

	private abstract class TContactItem<T> implements IContactItem<T> {
		protected static final String C_AND = " AND ";
		private ArrayList<String> n_whereArgs;

		protected abstract String getWhere(long rawContactId, T item);

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

		@Override
		public Uri queryUri(long rawContactId, T item) {
			clearWhereArgs();

			String where = getWhere(rawContactId, item);
			String[] whereArgs = getWhereArgs();

			Uri uri = null;
			Cursor dataCursor = contentResolver.query(Data.CONTENT_URI,
					DATAID_PROJECTION, where, whereArgs, null);

			if (dataCursor.moveToFirst())
				uri = ContentUris.withAppendedId(Data.CONTENT_URI,
						dataCursor.getLong(0));

			dataCursor.close();
			return uri;
		}

		@Override
		public Uri insert(long personId, T item, ContentValues values) {
			insertTo(personId, item, values);
			// ContentProviderOperation oper =
			// ContentProviderOperation.newInsert(Data.CONTENT_URI).withValues(values).build();
			// ops.add(oper);
			// Uri uri = oper.getUri();
			Uri uri = contentResolver.insert(Data.CONTENT_URI, values);
			if (uri != null && item instanceof Item) {
				Item iItem = (Item) item;

				long dataId = ContentUris.parseId(uri);
				iItem.setId(String.valueOf(dataId));
			}
			return uri;
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

		// @Override
		// public int bulkInsert(long personId, T[] items) {
		// int iCount = 0;
		// if (items != null && items.length > 0) {
		// ArrayList<ContentValues> valueList = new
		// ArrayList<ContentValues>(items.length);
		//
		// for(int i = 0; i < items.length; i++) {
		// T item = items[i];
		//
		// ContentValues values = new ContentValues();
		// insertTo(personId, item, values);
		//
		// if (values.size() > 0)
		// valueList.add(values);
		// }
		// if(valueList.size() > 0) {
		// ContentValues[] list = valueList.toArray(new ContentValues[0]);
		// iCount = contentResolver.bulkInsert(Data.CONTENT_URI, list);
		// }
		// }
		// return iCount;
		// }

		@Override
		public int update(long rawContactId, T item, T oldItem,
				ContentValues values) {
			Uri uri = queryUri(rawContactId, oldItem);
			if (uri != null) {
				updateTo(item, values, true);

				return contentResolver.update(uri, values, null, null);
			} else
				return 0;
		}

		@Override
		public int delete(long rawContactId, T item) {
			Uri uri = queryUri(rawContactId, item);
			if (uri != null) {
				return contentResolver.delete(uri, null, null);
			} else
				return 0;
		}

		public abstract boolean fillItem(Cursor cursor, T item);
	}

	private class ContactAddress extends TContactItem<IAddress> implements
			IItemCompare<IAddress> {
		@Override
		protected String getWhere(long rawContactId, IAddress item) {
			String dataId = item.getId();
			if (dataId != null)
				return Data._ID + "=" + dataId;

			AddressType type = AddressType.valueOfIgnoreCase(item.getKey());

			String where = Data.RAW_CONTACT_ID + "=" + rawContactId + C_AND
					+ Data.MIMETYPE + "='" + StructuredPostal.CONTENT_ITEM_TYPE
					+ "'";

			where += C_AND + StructuredPostal.TYPE + "=" + type.getCode();
			if (type == AddressType.Custom)
				where += C_AND + StructuredPostal.LABEL + "='" + item.getKey()
						+ "'";

			where += C_AND
					+ getWhereSub(StructuredPostal.FORMATTED_ADDRESS,
							item.getFormattedAddress())
					+ C_AND
					+ getWhereSub(StructuredPostal.STREET, item.getStreet())
					+ C_AND
					+ getWhereSub(StructuredPostal.CITY, item.getCity())
					+ C_AND
					+ getWhereSub(StructuredPostal.REGION, item.getRegion())
					+ C_AND
					+ getWhereSub(StructuredPostal.POSTCODE, item.getPostcode())
					+ C_AND
					+ getWhereSub(StructuredPostal.COUNTRY, item.getCountry())
					+ C_AND
					+ getWhereSub(StructuredPostal.POBOX, item.getPobox())
					+ C_AND
					+ getWhereSub(StructuredPostal.NEIGHBORHOOD,
							item.getNeighborhood());
			return where;
		}

		@Override
		public boolean fillItem(Cursor cursor, IAddress addressItem) {
			String value = cursor.getString(1); // StructuredPostal.FORMATTED_ADDRESS
			if (value != null) {
				addressItem.setFormattedAddress(value);

				addressItem.setStreet(cursor.getString(4));
				addressItem.setPobox(cursor.getString(5));
				addressItem.setNeighborhood(cursor.getString(6));
				addressItem.setCity(cursor.getString(7));
				addressItem.setRegion(cursor.getString(8));
				addressItem.setPostcode(cursor.getString(9));
				addressItem.setCountry(cursor.getString(10));

				int name = cursor.getInt(2); // StructuredPostal.TYPE
				if (name == AddressType.Custom.getCode()) {
					addressItem.setKey(cursor.getString(3)); // StructuredPostal.LABEL
				} else
					addressItem.setKey(AddressType.toTypeString(name));

				return true;
			}
			return false;
		}

		@Override
		public void updateTo(IAddress item, ContentValues values,
				boolean clearValues) {
			if (clearValues)
				values.clear();

			values.put(StructuredPostal.FORMATTED_ADDRESS,
					item.getFormattedAddress());
			values.put(StructuredPostal.STREET, item.getStreet());
			values.put(StructuredPostal.CITY, item.getCity());
			values.put(StructuredPostal.REGION, item.getRegion());
			values.put(StructuredPostal.POSTCODE, item.getPostcode());
			values.put(StructuredPostal.COUNTRY, item.getCountry());
			values.put(StructuredPostal.POBOX, item.getPobox());
			values.put(StructuredPostal.NEIGHBORHOOD, item.getNeighborhood());
		}

		@Override
		public void insertTo(long rawContactId, IAddress item,
				ContentValues values) {
			values.clear();
			values.put(Data.RAW_CONTACT_ID, rawContactId);
			values.put(Data.MIMETYPE, StructuredPostal.CONTENT_ITEM_TYPE);

			updateTo(item, values, false);

			AddressType type = AddressType.valueOfIgnoreCase(item.getKey());

			values.put(StructuredPostal.TYPE, type.getCode());
			if (type == AddressType.Custom)
				values.put(StructuredPostal.LABEL, item.getKey());
		}

		@Override
		public boolean compareKey(IAddress pcItem, IAddress phoneItem) {
			return equalsStr(pcItem.getKey(), phoneItem.getKey());
		}

		@Override
		public boolean compareValue(IAddress pcItem, IAddress phoneItem) {
			return equalsStr(pcItem.getCity(), phoneItem.getCity())
					&& equalsStr(pcItem.getCountry(), phoneItem.getCountry())
					&& equalsStr(pcItem.getPostcode(), phoneItem.getPostcode())
					&& equalsStr(pcItem.getRegion(), phoneItem.getRegion())
					&& equalsStr(pcItem.getStreet(), phoneItem.getStreet())

					&& equalsStr(pcItem.getFormattedAddress(),
							phoneItem.getFormattedAddress())
					&& equalsStr(pcItem.getPobox(), phoneItem.getPobox())
					&& equalsStr(pcItem.getNeighborhood(),
							phoneItem.getNeighborhood());
		}
	}

	private class ContactIm extends TContactItem<Item> {
		@Override
		protected String getWhere(long rawContactId, Item item) {
			String dataId = item.getId();
			if (dataId != null)
				return Data._ID + "=" + dataId;

			IMType type = IMType.valueOfIgnoreCase(item.getKey());

			String where = Data.RAW_CONTACT_ID + "=" + rawContactId + " AND "
					+ Data.MIMETYPE + "='" + Im.CONTENT_ITEM_TYPE + "'";

			if (type == IMType.Custom)
				where += C_AND + Im.PROTOCOL + "='" + Im.PROTOCOL_CUSTOM
						+ "' AND " + Im.CUSTOM_PROTOCOL + "='" + item.getKey()
						+ "'";
			else
				where += " AND " + Im.PROTOCOL + "='" + type.getCode() + "'";

			where += C_AND + getWhereSub(Im.DATA, item.getValue());
			return where;
		}

		@Override
		public boolean fillItem(Cursor cursor, Item item) {
			String value = cursor.getString(1); // Im.DATA
			if (value != null) {
				item.setValue(value);
				int name = cursor.getInt(5); // Im.PROTOCOL
				if (name == IMType.Custom.getCode()
						|| name == Im.PROTOCOL_CUSTOM)
					item.setKey(cursor.getString(6)); // Im.CUSTOM_PROTOCOL
				else
					item.setKey(IMType.toTypeString(name));
				return true;
			}
			return false;
		}

		@Override
		public void updateTo(Item item, ContentValues values,
				boolean clearValues) {
			if (clearValues)
				values.clear();

			values.put(Im.DATA, item.getValue());
		}

		@Override
		public void insertTo(long rawContactId, Item item, ContentValues values) {
			values.clear();
			values.put(Data.RAW_CONTACT_ID, rawContactId);
			values.put(Data.MIMETYPE, Im.CONTENT_ITEM_TYPE);

			updateTo(item, values, false);

			IMType type = IMType.valueOfIgnoreCase(item.getKey());
			if (type == IMType.Custom) {
				// values.put(Data.DATA2, 0);
				values.put(Im.PROTOCOL, Im.PROTOCOL_CUSTOM);
				values.put(Im.CUSTOM_PROTOCOL, item.getKey());
			} else {
				values.put(Im.PROTOCOL, type.getCode());
			}
		}
	}

	private class ContactEmail extends TContactItem<Item> {
		@Override
		protected String getWhere(long rawContactId, Item item) {
			String dataId = item.getId();
			if (dataId != null)
				return Data._ID + "=" + dataId;

			EmailType type = EmailType.valueOfIgnoreCase(item.getKey());

			String where = Data.RAW_CONTACT_ID + "=" + rawContactId + C_AND
					+ Data.MIMETYPE + "='" + Email.CONTENT_ITEM_TYPE + "'";

			if (type == EmailType.Custom) {
				if (item.getKey() == null) {
					where += " AND (" + Email.TYPE + "=" + type.getCode()
							+ " OR " + Email.TYPE + " IS NULL )" + C_AND
							+ Email.LABEL + " IS NULL ";
				} else {
					where += C_AND + Email.TYPE + "=" + type.getCode() + C_AND
							+ Email.LABEL + "='" + item.getKey() + "'";
				}
			} else
				where += " AND " + Email.TYPE + "=" + type.getCode();

			where += C_AND + getWhereSub(Email.DATA, item.getValue());
			return where;
		}

		@Override
		public boolean fillItem(Cursor cursor, Item item) {
			String value = cursor.getString(1); // Email.DATA
			if (value != null) {
				item.setValue(value);
				int name = cursor.getInt(2); // Email.TYPE
				if (name == EmailType.Custom.getCode()) {
					String typeString = cursor.getString(3); // Email.LABEL
					item.setKey(typeString);
				} else
					item.setKey(EmailType.toTypeString(name));
				return true;
			}
			return false;
		}

		@Override
		public void updateTo(Item item, ContentValues values,
				boolean clearValues) {
			if (clearValues)
				values.clear();

			values.put(Email.DATA, item.getValue());
		}

		@Override
		public void insertTo(long rawContactId, Item item, ContentValues values) {
			values.clear();
			values.put(Data.RAW_CONTACT_ID, rawContactId);
			values.put(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);

			updateTo(item, values, false);

			EmailType type = EmailType.valueOfIgnoreCase(item.getKey());
			values.put(Email.TYPE, type.getCode());
			if (type == EmailType.Custom)
				values.put(Email.LABEL, item.getKey());
		}
	}

	private class ContactOrganization extends TContactItem<IOrganization>
			implements IItemCompare<IOrganization> {
		@Override
		protected String getWhere(long rawContactId, IOrganization item) {
			String dataId = item.getId();
			if (dataId != null)
				return Data._ID + "=" + dataId;

			OrganizationType type = OrganizationType.valueOfIgnoreCase(item
					.getKey());

			String where = Data.RAW_CONTACT_ID + "=" + rawContactId + C_AND
					+ Data.MIMETYPE + "='" + Organization.CONTENT_ITEM_TYPE
					+ "'";

			where += C_AND + Organization.TYPE + "=" + type.getCode();
			if (type == OrganizationType.Custom)
				where += C_AND + Organization.LABEL + "='" + item.getKey()
						+ "'";

			where += C_AND
					+ getWhereSub(Organization.COMPANY, item.getCompany())
					+ C_AND
					+ getWhereSub(Organization.TITLE, item.getTitle())
					+ C_AND
					+ getWhereSub(Organization.DEPARTMENT, item.getDepartment())
					+ C_AND
					+ getWhereSub(Organization.JOB_DESCRIPTION,
							item.getJobDescription())
					+ C_AND
					+ getWhereSub(Organization.SYMBOL, item.getSymbol())
					+ C_AND
					+ getWhereSub(Organization.PHONETIC_NAME,
							item.getPhoneticName())
					+ C_AND
					+ getWhereSub(Organization.OFFICE_LOCATION,
							item.getOfficeLocation());
			return where;
		}

		@Override
		public boolean fillItem(Cursor cursor, IOrganization organizationItem) {
			String company = cursor.getString(1);
			String title = cursor.getString(4);

			if (company != null || title != null) {
				organizationItem.setCompany(company);
				organizationItem.setTitle(title);

				organizationItem.setDepartment(cursor.getString(5));
				organizationItem.setJobDescription(cursor.getString(6));
				organizationItem.setSymbol(cursor.getString(7));
				organizationItem.setPhoneticName(cursor.getString(8));
				organizationItem.setOfficeLocation(cursor.getString(9));

				int name = cursor.getInt(2); // Organization.TYPE
				if (name == OrganizationType.Custom.getCode()) {
					String typeString = cursor.getString(3); // Organization.LABEL
					organizationItem.setKey(typeString);
				} else
					organizationItem
							.setKey(OrganizationType.toTypeString(name));

				return true;
			}
			return false;
		}

		@Override
		public void updateTo(IOrganization item, ContentValues values,
				boolean clearValues) {
			if (clearValues)
				values.clear();

			values.put(Organization.COMPANY, item.getCompany());
			values.put(Organization.TITLE, item.getTitle());

			values.put(Organization.DEPARTMENT, item.getDepartment());
			values.put(Organization.JOB_DESCRIPTION, item.getJobDescription());
			values.put(Organization.SYMBOL, item.getSymbol());
			values.put(Organization.PHONETIC_NAME, item.getPhoneticName());
			values.put(Organization.OFFICE_LOCATION, item.getOfficeLocation());
		}

		@Override
		public void insertTo(long rawContactId, IOrganization item,
				ContentValues values) {
			values.clear();
			values.put(Data.RAW_CONTACT_ID, rawContactId);
			values.put(Data.MIMETYPE, Organization.CONTENT_ITEM_TYPE);

			updateTo(item, values, false);

			OrganizationType type = OrganizationType.valueOfIgnoreCase(item
					.getKey());

			values.put(Organization.TYPE, type.getCode());
			if (type == OrganizationType.Custom)
				values.put(Organization.LABEL, item.getKey());
		}

		@Override
		public boolean compareKey(IOrganization pcItem, IOrganization phoneItem) {
			return equalsStr(pcItem.getKey(), phoneItem.getKey());
		}

		@Override
		public boolean compareValue(IOrganization pcItem,
				IOrganization phoneItem) {
			return equalsStr(pcItem.getCompany(), phoneItem.getCompany())
					&& equalsStr(pcItem.getTitle(), phoneItem.getTitle())

					&& equalsStr(pcItem.getDepartment(),
							phoneItem.getDepartment())
					&& equalsStr(pcItem.getJobDescription(),
							phoneItem.getJobDescription())
					&& equalsStr(pcItem.getSymbol(), phoneItem.getSymbol())
					&& equalsStr(pcItem.getPhoneticName(),
							phoneItem.getPhoneticName())
					&& equalsStr(pcItem.getOfficeLocation(),
							phoneItem.getOfficeLocation());
		}
	}

	private class ContactNumber extends TContactItem<Item> {
		@Override
		protected String getWhere(long rawContactId, Item item) {
			String dataId = item.getId();
			if (dataId != null)
				return Data._ID + "=" + dataId;

			NumberType type = NumberType.valueOfIgnoreCase(item.getKey());

			String where = Data.RAW_CONTACT_ID + "=" + rawContactId + C_AND
					+ Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "' ";

			where += C_AND + Phone.TYPE + "=" + type.getCode();
			if (type == NumberType.Custom)
				where += C_AND + Phone.LABEL + "='" + item.getKey() + "'";

			where += C_AND + getWhereSub(Phone.NUMBER, item.getValue());
			return where;
		}

		@Override
		public boolean fillItem(Cursor cursor, Item item) {
			String value = cursor.getString(1); // Phone.NUMBER
			if (value != null) {
				item.setValue(value);
				int name = cursor.getInt(2); // Phone.TYPE;
				if (name == NumberType.Custom.getCode())
					item.setKey(cursor.getString(3)); // Phone.LABEL
				else
					item.setKey(NumberType.toTypeString(name));
				return true;
			} else
				return false;
		}

		@Override
		public void updateTo(Item item, ContentValues values,
				boolean clearValues) {
			if (clearValues)
				values.clear();

			values.put(Phone.NUMBER, item.getValue());
		}

		@Override
		public void insertTo(long rawContactId, Item item, ContentValues values) {
			values.clear();
			values.put(Data.RAW_CONTACT_ID, rawContactId);
			values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);

			updateTo(item, values, false);

			NumberType type = NumberType.valueOfIgnoreCase(item.getKey());

			values.put(Phone.TYPE, type.getCode());
			if (type == NumberType.Custom)
				values.put(Phone.LABEL, item.getKey());
		}
	}

	private class ContactEvent extends TContactItem<Item> {
		@Override
		protected String getWhere(long rawContactId, Item item) {
			String dataId = item.getId();
			if (dataId != null)
				return Data._ID + "=" + dataId;

			EventType type = EventType.valueOfIgnoreCase(item.getKey());

			String where = Data.RAW_CONTACT_ID + "=" + rawContactId + C_AND
					+ Data.MIMETYPE + "='" + Event.CONTENT_ITEM_TYPE + "' ";

			where += C_AND + Event.TYPE + "=" + type.getCode();
			if (type == EventType.Custom)
				where += C_AND + Event.LABEL + "='" + item.getKey() + "'";

			where += C_AND + getWhereSub(Event.START_DATE, item.getValue());
			return where;
		}

		@Override
		public boolean fillItem(Cursor cursor, Item item) {
			String value = cursor.getString(1); // Event.START_DATE
			if (value != null) {
				item.setValue(value);
				int name = cursor.getInt(2); // Event.TYPE;
				if (name == EventType.Custom.getCode())
					item.setKey(cursor.getString(3)); // Event.LABEL
				else
					item.setKey(EventType.toTypeString(name));

				return true;
			}
			return false;
		}

		@Override
		public void updateTo(Item item, ContentValues values,
				boolean clearValues) {
			if (clearValues)
				values.clear();

			values.put(Event.START_DATE, item.getValue());
		}

		@Override
		public void insertTo(long rawContactId, Item item, ContentValues values) {
			values.clear();
			values.put(Data.RAW_CONTACT_ID, rawContactId);
			values.put(Data.MIMETYPE, Event.CONTENT_ITEM_TYPE);

			updateTo(item, values, false);

			EventType type = EventType.valueOfIgnoreCase(item.getKey());

			values.put(Event.TYPE, type.getCode());
			if (type == EventType.Custom)
				values.put(Event.LABEL, item.getKey());
		}
	}

	private class ContactSipAddress extends ContactSimpleItem/*
															 * TContactItem<Item>
															 */{
		@Override
		public String getMimeType() {
			return SipAddress.CONTENT_ITEM_TYPE;
		}

		@Override
		protected String getSimpleColumn() {
			return SipAddress.SIP_ADDRESS;
		}
		// @Override
		// protected String getWhere(long rawContactId, Item item) {
		// String dataId = item.getId();
		// if (dataId != null)
		// return Data._ID + "=" + dataId;
		//
		// String where = Data.RAW_CONTACT_ID + "=" + rawContactId + C_AND
		// + Data.MIMETYPE + "='" + getMimeType() + "' ";
		//
		// if (item.getKey() == null) {
		// where += " AND (" + SipAddress.TYPE + "=" +
		// AddressType.Custom.getCode() + " OR " + SipAddress.TYPE +
		// " IS NULL )" +
		// C_AND + SipAddress.LABEL + " IS NULL ";
		// } else {
		// AddressType type = AddressType.valueOfIgnoreCase(item.getKey());
		// where += C_AND + SipAddress.TYPE + "=" + type.getCode();
		// if (type == AddressType.Custom)
		// where += C_AND + getWhereSub(SipAddress.LABEL, item.getKey());
		// }
		// where += C_AND + getWhereSub(SipAddress.SIP_ADDRESS,
		// item.getValue());
		// return where;
		// }
		//
		// @Override
		// public boolean fillItem(Cursor cursor, Item item) {
		// String value = cursor.getString(1);
		// if (value != null) {
		//
		// item.setValue(value);
		// int name = cursor.getInt(2);
		// if (name == AddressType.Custom.getCode())
		// item.setKey(cursor.getString(3));
		// else
		// item.setKey(AddressType.toTypeString(name));
		//
		// return true;
		// }
		// return false;
		// }
		//
		// @Override
		// public void updateTo(Item item, ContentValues values,
		// boolean clearValues) {
		// if (clearValues)
		// values.clear();
		//
		// values.put(SipAddress.SIP_ADDRESS, item.getValue());
		// }
		//
		// @Override
		// public void insertTo(long rawContactId, Item item, ContentValues
		// values) {
		// values.clear();
		// values.put(Data.RAW_CONTACT_ID, rawContactId);
		// values.put(Data.MIMETYPE, getMimeType());
		//
		// updateTo(item, values, false);
		//
		// AddressType type = AddressType.valueOfIgnoreCase(item.getKey());
		//
		// values.put(SipAddress.TYPE, type.getCode());
		// if (type == AddressType.Custom)
		// values.put(SipAddress.LABEL, item.getKey());
		// }
	}

	private abstract class ContactSimpleItem extends TContactItem<Item> {
		protected abstract String getMimeType();

		protected abstract String getSimpleColumn();

		@Override
		protected String getWhere(long rawContactId, Item item) {
			String dataId = item.getId();
			if (dataId != null)
				return Data._ID + "=" + dataId;

			String where = Data.RAW_CONTACT_ID + "=" + rawContactId + C_AND
					+ Data.MIMETYPE + "='" + getMimeType() + "' AND "
					+ getWhereSub(getSimpleColumn(), item.getValue());

			return where;
		}

		/**
		 * GroupMembership.GROUP_ROW_ID, Nickname.NAME, Website.URL, Note.NOTE,
		 * SipAddress.SIP_ADDRESS
		 */
		@Override
		public boolean fillItem(Cursor cursor, Item item) {
			String value = cursor.getString(1);
			if (value != null) {
				item.setValue(value);
				return true;
			}
			return false;
		}

		@Override
		public void updateTo(Item item, ContentValues values,
				boolean clearValues) {
			if (clearValues)
				values.clear();

			values.put(getSimpleColumn(), item.getValue());
		}

		@Override
		public void insertTo(long rawContactId, Item item, ContentValues values) {
			values.clear();
			values.put(Data.RAW_CONTACT_ID, rawContactId);
			values.put(Data.MIMETYPE, getMimeType());

			updateTo(item, values, false);
		}
	}

	private class ContactGroup extends ContactSimpleItem {
		@Override
		protected String getMimeType() {
			return GroupMembership.CONTENT_ITEM_TYPE;
		}

		@Override
		protected String getSimpleColumn() {
			return GroupMembership.GROUP_ROW_ID;
		}
	}

	private class ContactWebsite extends ContactSimpleItem {
		@Override
		protected String getMimeType() {
			return Website.CONTENT_ITEM_TYPE;
		}

		@Override
		protected String getSimpleColumn() {
			return Website.URL;
		}
	}

	private class ContactNickname extends ContactSimpleItem {
		@Override
		protected String getMimeType() {
			return Nickname.CONTENT_ITEM_TYPE;
		}

		@Override
		protected String getSimpleColumn() {
			return Nickname.NAME;
		}
	}

	private class ContactNote extends ContactSimpleItem {
		@Override
		protected String getMimeType() {
			return Note.CONTENT_ITEM_TYPE;
		}

		@Override
		protected String getSimpleColumn() {
			return Note.NOTE;
		}
	}

	private class ContactName extends TContactItem<IContact> {
		@Override
		protected String getWhere(long rawContactId, IContact item) {
			String where = Data.RAW_CONTACT_ID + "=" + rawContactId + C_AND
					+ Data.MIMETYPE + "='" + StructuredName.CONTENT_ITEM_TYPE
					+ "'";
			return where;
		}

		@Override
		public boolean fillItem(Cursor cursor, IContact contact) {
			contactName.backupName(contact); // ������raw_id���

			contact.setDisplayName(cursor.getString(1)); // StructuredName.DISPLAY_NAME
			contact.setGivenName(cursor.getString(2)); // StructuredName.GIVEN_NAME
			contact.setFamilyName(cursor.getString(3)); // StructuredName.FAMILY_NAME
			contact.setPrefix(cursor.getString(4)); // StructuredName.PREFIX
			contact.setMiddleName(cursor.getString(5)); // StructuredName.MIDDLE_NAME
			contact.setSuffix(cursor.getString(6)); // StructuredName.SUFFIX
			contact.setPhoneticGiven(cursor.getString(7)); // StructuredName.PHONETIC_GIVEN_NAME
			contact.setPhoneticMiddle(cursor.getString(8)); // StructuredName.PHONETIC_MIDDLE_NAME
			contact.setPhoneticFamily(cursor.getString(9)); // StructuredName.PHONETIC_FAMILY_NAME

			contactName.restoreName(contact); // ������raw_id���
			return true;
		}

		@Override
		public void updateTo(IContact item, ContentValues values,
				boolean clearValues) {
			if (clearValues)
				values.clear();

			values.put(StructuredName.FAMILY_NAME, item.getFamilyName()); // StructuredName.FULL_NAME_STYLE
			values.put(StructuredName.GIVEN_NAME, item.getGivenName());
			values.put(StructuredName.MIDDLE_NAME, item.getMiddleName());

			values.put(StructuredName.PREFIX, item.getPrefix());
			values.put(StructuredName.DISPLAY_NAME, item.getDisplayName());
			values.put(StructuredName.SUFFIX, item.getSuffix());

			values.put(StructuredName.PHONETIC_FAMILY_NAME,
					item.getPhoneticFamily());
			values.put(StructuredName.PHONETIC_MIDDLE_NAME,
					item.getPhoneticMiddle());
			values.put(StructuredName.PHONETIC_GIVEN_NAME,
					item.getPhoneticGiven());
		}

		@Override
		public void insertTo(long rawContactId, IContact item,
				ContentValues values) {
			values.clear();
			values.put(Data.RAW_CONTACT_ID, rawContactId);
			values.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);

			// if(!hasName(item))
			// return null;

			updateTo(item, values, false);
		}

		public boolean compareName(IContact contact, IContact oldContact) {
			return equalsStr(contact.getFamilyName(),
					oldContact.getFamilyName())
					&& equalsStr(contact.getMiddleName(),
							oldContact.getMiddleName())
					&& equalsStr(contact.getGivenName(),
							oldContact.getGivenName())
					&& equalsStr(contact.getDisplayName(),
							oldContact.getDisplayName());
		}

		public boolean hasName(IContact contact) {
			boolean is = isEmptyStr(contact.getFamilyName())
					&& isEmptyStr(contact.getMiddleName())
					&& isEmptyStr(contact.getGivenName())
					&& isEmptyStr(contact.getDisplayName());
			return !is;
		}

		private String familyName, middleName, givenName;
		private String displayName;

		/** ���ݵ�ǰ����ϵ����ƣ���ҪΪӦ�Զ��raw_id�� */
		private void backupName(IContact contact) {
			familyName = contact.getFamilyName();
			givenName = contact.getGivenName();
			middleName = contact.getMiddleName();

			// prefix = contact.getPrefix();
			displayName = contact.getDisplayName();
			// suffix = contact.getSuffix();
			// phoneticFamily = contact.getPhoneticFamily();
			// phoneticMiddle = contact.getPhoneticMiddle();
			// phoneticGiven = contact.getPhoneticGiven();
		}

		/** �û���ƻ�ԭ����ҪΪӦ�Զ��raw_id�� */
		private void restoreName(IContact contact) {
			boolean isRestore = false;

			if (isEmptyStr(contact.getDisplayName())
					&& !isEmptyStr(displayName))
				isRestore = true;

			else if (isEmptyStr(contact.getFamilyName())
					&& !isEmptyStr(familyName))
				isRestore = true;

			else if (isEmptyStr(contact.getGivenName())
					&& !isEmptyStr(givenName))
				isRestore = true;

			else if (isEmptyStr(contact.getMiddleName())
					&& !isEmptyStr(middleName))
				isRestore = true;

			if (isRestore) {
				contact.setFamilyName(familyName);
				contact.setGivenName(givenName);
				contact.setMiddleName(middleName);
				contact.setDisplayName(displayName);
			}
		}

		private boolean isEmptyStr(String str) {
			return str == null || str.trim().length() == 0;
		}
	}

	@Override
	public Uri insert(IContact contact) {
		return null;
	}
}
