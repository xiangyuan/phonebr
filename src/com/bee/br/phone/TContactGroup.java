package com.bee.br.phone;


public class TContactGroup implements IContactGroup {
	private String id;
	private String systemId;
	private String note;
	private String name;
	private boolean isVisiable;
//	private int accountId;
	
	private String oldId;
	
	public TContactGroup(){
//		accountId = -1;
	}
	
	@Override
	public String getNote() {

		return note;
	}

	@Override
	public String getSystemId() {

		return systemId;
	}

	@Override
	public boolean isVisiable() {

		return isVisiable;
	}

	@Override
	public void setNote(String note) {

		this.note = note;
	}

	@Override
	public void setSystemId(String systemId) {

		this.systemId = systemId;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {

		return name;
	}

	@Override
	public void setId(String id) {
		oldId = this.id; // ������һ��ֵ
		this.id = id;
	}

	@Override
	public void setName(String name) {

		this.name = name;
	}

	@Override
	public void setVisiable(boolean isVisiable) {

		this.isVisiable = isVisiable;
	}
	
//	@Override
//	public int getAccountId() {
//		return accountId;
//	}
//	@Override
//	public void setAccountId(int accountId) {
//		this.accountId = accountId;
//	}
	@Override
	public String getOldId() {
		return oldId;
	}
}
