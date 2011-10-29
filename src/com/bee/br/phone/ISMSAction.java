package com.bee.br.phone;

public interface ISMSAction extends ITargetAction {		
	int getCount();
	public ISMS[] getSMS();
	
	//void getSMS(ICutListener listener);
	
	//public boolean sendSMS(String[] numbers, ISMS sms);

	public boolean removeSMS(ISMS[] item);
	public ISMS getSMS(String id);
	
	public ISMS getUnReadSMS(String address, String content, String time);
	
	
	public void  updateSMS(ISMS[] item);
	
	//public ISMS createFromPdu(Object OBJpdus); 
	
	boolean Add(ISMS[] items);
	
	int deleteAll();
	
	boolean updateSMSState(ISMS sms, int type);
	//void notify(ISMS sms);
}
