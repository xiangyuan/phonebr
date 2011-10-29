package com.bee.br.phone;


public class TItem implements Item {
	private String id;
	private String type;
	private String value;
	
	private String oldid;
	private boolean primary;
	
	@Override
	public String getKey() {
		return type ;
	}

	@Override
	public void setKey(String type) {
		this.type = type;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String getId() {
		return id;
	}
	@Override
	public void setId(String id) {
		oldid = this.id;
		this.id = id;		
	}
	
	@Override
	public String getOldId() {
		return oldid;
	}
	
	@Override
	public boolean isPrimary() {
		return primary;
	}
	@Override
	public void setPrimary(boolean primary) {
		this.primary = primary;
	}
}
