package com.bee.br.phone;

/**
 * the group information
 * @author liyajie1209
 *
 */
public interface IContactGroup {
	public String getId();
	public void setId(String id);

	public String getName();
	public void setName(String name);

	public void setVisiable(boolean isVisiable);
	public boolean isVisiable();

	public String getSystemId();
	public void setSystemId(String systemId);

	public String getNote();
	public void setNote(String note);
	String getOldId();
}
