package com.bee.br.phone;
/**
 * the contact information
 * @author liyajie1209
 *
 */
public interface IContact{

	public String getId();
	public void setId(String id);
	String getOldId();
	
	String getRawId();
	void setRawId(String rawId);
		
	String getDisplayName();
	void setDisplayName(String name);

	String getFamilyName();
	void setFamilyName(String familyName);
	
	String getMiddleName();
	void setMiddleName(String middleName);

	String getGivenName();
	void setGivenName(String givenName);
	
	String getPrefix();
	void setPrefix(String prefix);
	
	String getSuffix();
	void setSuffix(String suffix);
	
	String getPhoneticFamily();
	void setPhoneticFamily(String phoneticFamily);
	
	String getPhoneticMiddle();
	void setPhoneticMiddle(String phoneticMiddle);
	
	String getPhoneticGiven();
	void setPhoneticGiven(String phoneticGiven);
	
	/**
	 * @return the icon path, then get the icon by the file channel, null means
	 *         no icon.
	 */
	public String getIconPath();
	public void setIconPath(String path);

	public String getRingtoneId();
	public void setRingtoneId(String ringtoneId);

	public Item[] getNumbers();
	//public void setNumbers(Item[] items);
	public void addNumber(TItem number);

	public Item[] getEmails();
	//public void setEmails(Item[] items);
	public void addEmail(TItem email);

	public Item[] getIMs();
	//public void setIMs(Item[] items);
	public void addIM(TItem im);

	public IAddress[] getAddresses();
	//public void setAddresss(IAddress[] addresses);
	public void addAddress(TAddress address);

	public IOrganization[] getOrganizations();
	//public void setOrganizations(IOrganization[] addresses);
	public void addOrganization(TOrganization org);

	public boolean isStarred();
	public void setIsStarred(boolean isStarred);

	public boolean isSysRingtone();
	public void setIsSysRingtone(boolean isSysRingtone);
	
	Item[] getEvent();
	void addEvent(TItem event);
	
	public Item[] getGroupId();
	//public void setGroupId(Item[] groupId);
	public void addGroupId(TItem groupId);
	
	public Item[] getWebsites();
	//public void setWebsites(Item[] webSites);
	public void addWebsite(TItem website);
	
	public Item[] getNotes();
	public void addNote(TItem note);

	public Item[] getNickNames();
	public void addNickname(TItem nickname);
	
	public Item[] getSipAddresses();
	public void addSipAddress(TItem sipAddress);
	
	int filterItems();
}
