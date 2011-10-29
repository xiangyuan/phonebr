package com.bee.br.phone;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.PendingIntent;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.BaseColumns;

public class SMSFactory implements ISMSAction {

	// public static ISMSAction getSMSManager(Context context) {
	// if(SysConst.SysVersion() <= 1.5f)
	// return new SMSManager15(context);
	// else
	// return new SMSManager(context);
	// }


	public interface SmsCols {
		final String TYPE = "type";
		final String THREAD_ID = "thread_id";
		final String ADDRESS = "address";
		final String PERSON_ID = "person";
		final String DATE = "date";
		final String READ = "read";
		final String BODY = "body";
		final String STATUS = "status";
	}

	protected static final Uri CONTENT_URI = Uri.parse("content://sms");
	protected static final Uri SENT_CONTENT_URI = Uri
			.parse("content://sms/sent");
	// protected static final String FILTER = "!imichat";

	public static final String SMS_SEND_ACTION = "SMS_SEND_ACTION";

	public static final int TYPE_ALL = 0;
	public static final int TYPE_INBOX = 1;
	public static final int TYPE_SENT = 2;
	public static final int TYPE_DRAFT = 3;
	public static final int TYPE_OUTBOX = 4;
	public static final int TYPE_FAILED = 5; // for failed outgoing messages
	public static final int TYPE_QUEUED = 6; // for messages to send later

	/** _ID, address, date, body, thread_id, type, read */
	private static final String SEL_COLUMN[] = { BaseColumns._ID,
			SmsCols.ADDRESS, SmsCols.DATE, SmsCols.BODY, SmsCols.THREAD_ID,
			SmsCols.TYPE, SmsCols.READ, SmsCols.PERSON_ID };

	protected ContentResolver contentResolver;

	protected ArrayList<PendingIntent> piList;

	private Object lockObj;

	private DraftSearch draftSearch;

	public SMSFactory(Context context) {
//		this.context = context;
		contentResolver = context.getContentResolver();

		lockObj = new Object();
	}

	@Override
	public int getCount() {
		int count = 0;
		Cursor cursor = contentResolver.query(CONTENT_URI,
				new String[] { BaseColumns._ID }, null, null, null);
		if (cursor != null) {
			count = cursor.getCount();
			cursor.close();
		}
		return count;
	}

	@Override
	public ISMS[] getSMS() {
		Cursor cursor = contentResolver.query(CONTENT_URI, SEL_COLUMN, null,
				null, null);

		if (cursor != null) {
			int count = cursor.getCount();
			ArrayList<ISMS> listISMS = new ArrayList<ISMS>(count);

			if (cursor.moveToFirst()) {
				do {
					ISMS sms = getSMS(cursor);
					listISMS.add(sms);
				} while (cursor.moveToNext());
			}
			cursor.close();
			return listISMS.toArray(new ISMS[0]);
		}
		return null;
	}

	// @Override
	// public void getSMS(ICutListener listener) {
	// try {
	// draftSearch = new DraftSearch();
	// Cursor cursor = contentResolver.query(CONTENT_URI, SEL_COLUMN, null,
	// null, null);
	//
	// if (cursor != null) {
	// if (cursor.moveToFirst()) {
	// synchronized (lockObj) {
	// int rowCount = cursor.getCount();
	//
	// int count = listener.getTimeCount(rowCount);
	// int oneMax = listener.getOneMax();
	//
	// ArrayList<ISMS> listSMS = new ArrayList<ISMS>(oneMax);
	// int index = 0;
	// do {
	// ISMS sms = getSMS(cursor);
	// listSMS.add(sms);
	//
	// if (listSMS.size() >= oneMax) {
	// ISMS[] smsArr = listSMS.toArray(new ISMS[0]);
	//
	// listener.responseSubGet(index, count, smsArr);
	// listSMS.clear();
	// index++;
	//
	// if (index >= count)
	// break;
	// }
	// } while (cursor.moveToNext());
	//
	// cursor.close();
	//
	// if (listSMS.size() >= 0) {
	// ISMS[] smsArr = listSMS.toArray(new ISMS[0]);
	//
	// listener.responseSubGet(count - 1, count, smsArr);
	// listSMS.clear();
	// }
	// }
	// return;
	// }
	// cursor.close();
	// }
	// listener.responseSubGet(0, 0, null); // null
	// } finally {
	// draftSearch.close();
	// draftSearch = null;
	// }
	// }

	@Override
	public ISMS getUnReadSMS(String address, String content, String time) {
		ISMS sms = null;
		String select = "address =? and TYPE =" + TYPE_INBOX
				+ " and read=0 and ( body =? or date >=? )";

		Cursor cursor = contentResolver.query(CONTENT_URI, SEL_COLUMN, select,
				new String[] { address, content, time }, "date desc");
		if (cursor.moveToNext()) {
			sms = getSMS(cursor);
		}
		cursor.close();
		return sms;
	}

	@Override
	public ISMS getSMS(String id) {
		synchronized (lockObj) {
			ISMS sms = null;
			if (id != null) {
				Cursor cursor = contentResolver.query(CONTENT_URI, SEL_COLUMN,
						BaseColumns._ID + "=" + id, null, null);
				if (cursor.moveToNext()) {
					sms = getSMS(cursor);
				}
				cursor.close();
			}
			return sms;
		}
	}

	private ISMS getSMS(Cursor cursor) {
		ISMS sms = new TSMS();
		sms.setId(cursor.getString(0));

		String number = cursor.getString(1);
		sms.setNumber(number);

		sms.setTime(cursor.getString(2));
		sms.setContent(cursor.getString(3));

		long thread_id = cursor.getLong(4);
		sms.setThreadId(String.valueOf(thread_id));

		int type = cursor.getInt(5);

		switch (type) {
		case TYPE_INBOX:
			int read = cursor.getInt(6);
			if (read == 0)
				sms.setState(ISMS.State.unread);
			else
				sms.setState(ISMS.State.read);
			break;

		case TYPE_SENT:
			sms.setState(ISMS.State.sent);
			break;

		default:
		case TYPE_DRAFT:
			sms.setState(ISMS.State.draft);

			if (number == null && draftSearch != null) {
				number = draftSearch.searchNumber(thread_id);
				if (number != null)
					sms.setNumber(number);
			}
			break;

		case TYPE_FAILED:
			sms.setState(ISMS.State.failed);
			break;

		case TYPE_OUTBOX:
		case TYPE_QUEUED:
			sms.setState(ISMS.State.queued);
			break;
		}

		// if(!cursor.isNull(7)) {
		// long person = cursor.getLong(7);
		// String contactId = getContactId(person);
		//
		// sms.setContactId(contactId);
		// }
		return sms;
	}

	protected Uri insertSms(String address, String body) {
		ContentValues values = new ContentValues();
		values.clear();
		values.put(SmsCols.ADDRESS, address);
		values.put(SmsCols.BODY, body);
		return contentResolver.insert(SENT_CONTENT_URI, values);
	}

	@Override
	public boolean Add(ISMS[] items) {
		boolean isOk = false;
		ContentValues values = new ContentValues();
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		for (ISMS sms : items) {
			values.clear();

			values.put(SmsCols.ADDRESS, sms.getNumber());
			values.put(SmsCols.DATE, Long.parseLong(sms.getTime()));
			values.put(SmsCols.BODY, sms.getContent());
			// values.put(SmsCols.THREAD_ID,
			// Integer.valueOf(sms.getThreadId()));

			ISMS.State state = sms.getState();
			switch (state) {
			default:
			case read:
				values.put(SmsCols.TYPE, TYPE_INBOX);
				break;

			case unread:
				values.put(SmsCols.TYPE, TYPE_INBOX);
				break;

			case draft:
				values.put(SmsCols.TYPE, TYPE_DRAFT);
				break;

			case sent:
				values.put(SmsCols.TYPE, TYPE_SENT);
				break;

			case failed:
				values.put(SmsCols.TYPE, TYPE_FAILED);
				break;

			case queued:
				values.put(SmsCols.TYPE, TYPE_OUTBOX); // TYPE_QUEUED
				break;
			}
			values.put(SmsCols.READ, state != ISMS.State.unread);
			// values.put(SmsCols.STATUS, -1);
			ops.add(ContentProviderOperation.newInsert(CONTENT_URI)
					.withValues(values).build());
			// contentResolver.insert(CONTENT_URI, values);
		}
		try {
			contentResolver.applyBatch(CONTENT_URI.getAuthority(), ops);
			isOk = true;
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			e.printStackTrace();
		}
		return (isOk);
	}

	@Override
	public void updateSMS(ISMS[] items) {
		if (items != null) {
			int uCount = 0;
			ContentValues values = new ContentValues();
			values.put(SmsCols.READ, 1);

			for (ISMS item : items) {
				// ISMS.State type = item.getState();

				// if (type == ISMS.State.unread) {
				String id = item.getId();
				Uri uri = ContentUris.withAppendedId(CONTENT_URI,
						Long.parseLong(id));

				int count = contentResolver.update(uri, values, null, null);
				uCount += count;
				// }
				item.setState(ISMS.State.read);
			}
		}
	}

	@Override
	public int deleteAll() {
		return contentResolver.delete(CONTENT_URI, null, null);
	}

	@Override
	public boolean removeSMS(ISMS[] item) {
		for (int i = 0; i < item.length; i++) {
			// String threadId = item[i].getThreadId();
			String id = item[i].getId();
			// if (Integer.parseInt(threadId) != -1) {
			// String uriThread = "content://sms/conversations/"
			// + threadId;
			// contentResolver.delete(Uri.parse(uriThread), null, null);
			// }
			if (Integer.parseInt(id) != -1) {
				Uri uriId = ContentUris.withAppendedId(CONTENT_URI,
						Long.parseLong(id));
				contentResolver.delete(uriId, null, null);
			}
		}
		return true;
	}

	@Override
	public boolean updateSMSState(ISMS sms, int type) {
		synchronized (lockObj) {
			ContentValues values = new ContentValues();
			values.put(SmsCols.TYPE, type);

			Uri uri = ContentUris.withAppendedId(CONTENT_URI,
					Long.parseLong(sms.getId()));
			int result = contentResolver.update(uri, values, null, null);
			return result > 0;
		}
	}

	private class DraftSearch {
		private boolean isValid, isInit;
		private HashMap<Long, String> numbers;

		private Cursor threads_cursor, canonical_addresses_cursor;

		private static final String threads_where = "_id, recipient_ids from threads --";
		private static final String canonical_addresses_where = "_id, address from canonical_addresses --";

		private void init() {
			isValid = false;

			try {
				threads_cursor = contentResolver.query(CONTENT_URI,
						new String[] { threads_where }, null, null, null);
				if (threads_cursor != null) {
					canonical_addresses_cursor = contentResolver.query(
							CONTENT_URI,
							new String[] { canonical_addresses_where }, null,
							null, null);

					isValid = canonical_addresses_cursor != null;
				}
			} catch (Exception e) {
			}
		}

		public String searchNumber(long thread_id) {
			if (!isInit) {
				init();
				isInit = true;
			}
			if (!isValid)
				return null;

			if (numbers != null && numbers.containsKey(thread_id))
				return numbers.get(thread_id);

			if (threads_cursor.moveToFirst()) {
				long t_thread_id, t_recipient_ids, c_recipient_ids;
				do {
					t_thread_id = threads_cursor.getLong(0);
					if (t_thread_id == thread_id) {
						t_recipient_ids = threads_cursor.getLong(1);

						if (canonical_addresses_cursor.moveToFirst()) {
							do {
								c_recipient_ids = canonical_addresses_cursor
										.getLong(0);
								if (t_recipient_ids == c_recipient_ids) {
									String number = canonical_addresses_cursor
											.getString(1);

									if (numbers == null)
										numbers = new HashMap<Long, String>();
									numbers.put(thread_id, number);

									return number;
								}
							} while (canonical_addresses_cursor.moveToNext());
						}
					}

				} while (threads_cursor.moveToNext());
			}
			return null;
		}

		public void close() {
			if (threads_cursor != null) {
				threads_cursor.close();
				threads_cursor = null;
			}
			if (canonical_addresses_cursor != null) {
				canonical_addresses_cursor.close();
				canonical_addresses_cursor = null;
			}
			if (numbers != null) {
				numbers.clear();
				numbers = null;
			}
		}
	}
}
