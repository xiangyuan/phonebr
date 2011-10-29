package com.bee.br.phone;

public interface IPlaylist {

	public String getId();
	public void setId(String id);

	public String getName();
	public void setName(String name);

	public Item[] getMembers();
	public void setMember(Item[] members);
	
	String getOldId();
}
