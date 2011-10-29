package com.bee.br.phone;

public interface Item {	
	String getId();
	void setId(String id);
	String getOldId();
	
	String getKey();
	void setKey(String name);

	String getValue();
	void setValue(String value);
	
	boolean isPrimary();
	void setPrimary(boolean primary);
}
