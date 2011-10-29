package com.bee.br.phone;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore.Audio;

import com.bee.br.phone.ContactHelper.IItemCompare;
import com.bee.br.utils.SysConst;


/**
 * @author liyajie1209
 * 
 */
public abstract class ContactFactory implements IContactAction {
	/**
	 * @param context
	 * @return
	 */
	public static IContactAction getContactManager(Context context) {
		if (SysConst.SysVersion() < 2.0f)
			return new ContactManager16(context);
		else
			return new ContactManager(context);
	}

	private static final String PREFIX_MEDIA_EXTERNAL = "content://media/external";
	private static final String DEFAULT_RINGTONE = "content://settings/system/ringtone";

	protected Context context;
	protected ContentResolver contentResolver;

	public ContactFactory(Context context) {
		this.context = context;
		contentResolver = context.getContentResolver();
	}

	@Override
	public IContact[] writeContacts(IContact[] contacts) {
		// 此处方法很有问题，效率问题，应该用批处理
		if (contacts != null) {
//			for (IContact contact : contacts) {
//				// insert(contact);
//			}
			insert(contacts);
		}
		return contacts;
	}

	@Override
	public boolean updateContacts(IContact[] contacts) {
		if (contacts == null)
			return false;

		for (IContact contact : contacts) {
			IContact oldContact = getOldContact(contact);

			update(contact, oldContact);
		}
		return true;
	}

	@Override
	public boolean removeContacts(IContact[] contacts) {
		if (contacts == null)
			return false;
		delete(contacts);
		return true;
	}

	protected boolean equalsStr(String newStr, String oldStr) {
		return newStr == null ? oldStr == null : newStr.equals(oldStr);
	}

	protected void fillRingtoneInfo(IContact contact, String ringUri) {
		boolean isDefaultRingtone = ringUri == null
				|| ringUri.trim().length() == 0
				|| ringUri.equals(DEFAULT_RINGTONE);

		if (isDefaultRingtone) {
			// try {
			// if (nDefaultRing == null) {
			// Uri uri = RingtoneManager.getActualDefaultRingtoneUri(context,
			// RingtoneManager.TYPE_RINGTONE);
			// nDefaultRing = uri.toString();
			// }
			// ringUri = nDefaultRing;
			// } catch (Exception e) {
			// ringUri = null;
			// }
			contact.setRingtoneId(null); // ϵͳĬ������
			contact.setIsSysRingtone(true);
			return;
		}
		if (ringUri != null) {
			contact.setIsSysRingtone(!ringUri.startsWith(PREFIX_MEDIA_EXTERNAL));

			try {
				long ringtoneId = ContentUris.parseId(Uri.parse(ringUri));
				contact.setRingtoneId(String.valueOf(ringtoneId));
			} catch (Exception e) {
				if (!isDefaultRingtone)
					fillRingtoneInfo(contact, null);
			}
		}
	}

	protected String getRingtoneUri(String ringtoneId, boolean isSysRingtone) {
		if (ringtoneId == null)
			return null;
		else {
			Uri uri = isSysRingtone ? Audio.Media.INTERNAL_CONTENT_URI
					: Audio.Media.EXTERNAL_CONTENT_URI;

			long id = Long.valueOf(ringtoneId);
			return ContentUris.withAppendedId(uri, id).toString();
		}
	}

	protected IContact findContactsById(IContact[] contacts, String contactId,
			IContact lastContact) {
		if (lastContact != null && equalsStr(contactId, lastContact.getId()))
			return lastContact;

		if (contacts != null) {
			for (int i = 0; i < contacts.length; i++) {
				String tmpId = contacts[i].getId();
				if (equalsStr(contactId, tmpId)) {
					lastContact = contacts[i];
					return lastContact;
				}
			}
		}
		return null;
	}

	public class ItemCompare implements IItemCompare<Item> {
		@Override
		public boolean compareKey(Item pcItem, Item phoneItem) {
			return equalsStr(pcItem.getKey(), phoneItem.getKey());
		}

		@Override
		public boolean compareValue(Item pcItem, Item phoneItem) {
			return equalsStr(pcItem.getValue(), phoneItem.getValue());
		}
	}
}
