package com.bee.br.phone;

import java.util.List;

import android.net.Uri;
import android.os.RemoteException;


public interface IContactAction extends ITargetAction {
	public IContact[] getAllContacts();
	void getAllContacts(ICutListener listener);
	
	public IContact[] writeContacts(IContact[] contacts);

	boolean updateContacts(IContact[] contacts) throws RemoteException;

	boolean removeContacts(IContact[] contacts);
	
	IContact getOldContact(IContact newContact);
	
	int getCount();
	int deleteAll();
	
	Uri insert(IContact contact);
	List<Uri> insert(IContact[] contacts);
	int update(IContact contact, IContact oldContact);
	int delete(IContact[] contacts);
	
	String registerHeadUri(IContact contact);
}
