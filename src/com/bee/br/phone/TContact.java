package com.bee.br.phone;

import java.util.ArrayList;
import java.util.List;


/**
 * @author liyajie1209
 *
 */
public class TContact implements IContact {
	private String id;
	private String oldId;
	private String rawId;

	private String familyName, middleName, givenName;
	private String prefix, displayName, suffix;
	private String phoneticFamily, phoneticMiddle, phoneticGiven;
	
	private boolean isStarred;
	private String iconPath;
	
	private String ringtoneId;
	private boolean isSysRingtone;
	
	private List<TItem> groupIds;
	
	private List<TItem> numbers;
	private List<TItem> emails;
	private List<TAddress> addresses;
	
	private List<TItem> ims;
	private List<TOrganization> organizations;
	private List<TItem> websites;
	private List<TItem> events;
	private List<TItem> nickNames;
	private List<TItem> notes;
	private List<TItem> sipAddresses;
		
	@Override
	public String getId() {
		return id;
	}
	
	@Override
	public void setId(String id) {
		oldId = this.id; 
		this.id = id;		
	}
	
	@Override
	public String getOldId() {
		return oldId;
	}
	
	@Override
	public boolean isStarred() {
		return isStarred;
	}
	
	@Override
	public void setIsStarred(boolean isStarred) {
		this.isStarred = isStarred;
	}
	
	@Override
	public String getRingtoneId() {
		return ringtoneId;
	}
	
	@Override
	public void setRingtoneId(String ringtoneId) {
		this.ringtoneId = ringtoneId;
	}
	
	@Override
	public boolean isSysRingtone() {
		return isSysRingtone;
	}
	
	@Override
	public void setIsSysRingtone(boolean isSysRingtone) {
		this.isSysRingtone = isSysRingtone;
	}
	
	@Override
	public String getIconPath() {
		return iconPath;
	}
	
	@Override
	public void setIconPath(String path) {
		this.iconPath = path;
		
	}
	
	@Override
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public void setDisplayName(String name) {
		displayName = name;		
	}
	
	@Override
	public String getMiddleName() {
		return middleName;
	}
	
	@Override
	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}
	
	@Override
	public String getFamilyName() {
		return familyName;
	}
	@Override
	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}

	@Override
	public String getGivenName() {
		return givenName;
	}
	@Override
	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	@Override
	public String getPrefix() {
		return prefix;
	}

	@Override
	public void setPrefix(String prefix) {
		this.prefix = prefix;		
	}

	@Override
	public String getSuffix() {
		return suffix;
	}

	@Override
	public void setSuffix(String suffix) {
		this.suffix = suffix;		
	}

	@Override
	public String getPhoneticFamily() {
		return phoneticFamily;
	}

	@Override
	public void setPhoneticFamily(String phoneticFamily) {
		this.phoneticFamily = phoneticFamily;		
	}

	@Override
	public String getPhoneticMiddle() {
		return phoneticMiddle;
	}

	@Override
	public void setPhoneticMiddle(String phoneticMiddle) {
		this.phoneticMiddle = phoneticMiddle;		
	}

	@Override
	public String getPhoneticGiven() {
		return phoneticGiven;
	}

	@Override
	public void setPhoneticGiven(String phoneticGiven) {
		this.phoneticGiven = phoneticGiven;		
	}
	
	@Override
	public void addAddress(TAddress address) {
		if(addresses == null)
			addresses = new ArrayList<TAddress>();
		
		addresses.add(address);
	}

	@Override
	public void addEmail(TItem email) {
		if(emails == null)
			emails = new ArrayList<TItem>();
		
		emails.add(email);
	}

	@Override
	public void addGroupId(TItem groupId) {
		if(groupIds == null)
			groupIds = new ArrayList<TItem>();
		
		groupIds.add(groupId);
	}

	@Override
	public void addIM(TItem im) {
		if(ims == null)
			ims = new ArrayList<TItem>();
		
		ims.add(im);
	}

	@Override
	public void addNickname(TItem nickname) {
		if(nickNames == null)
			nickNames = new ArrayList<TItem>();
		
		nickNames.add(nickname);
	}

	@Override
	public void addNote(TItem note) {
		if(notes == null)
			notes = new ArrayList<TItem>();
		
		notes.add(note);
	}

	@Override
	public void addNumber(TItem number) {
		if(numbers == null)
			numbers = new ArrayList<TItem>();
		
		numbers.add(number);
	}

	@Override
	public void addOrganization(TOrganization org) {
		if(organizations == null)
			organizations = new ArrayList<TOrganization>();
		
		organizations.add(org);
	}

	@Override
	public void addWebsite(TItem website) {
		if(websites == null)
			websites = new ArrayList<TItem>();
		
		websites.add(website);
	}

	@Override
	public IAddress[] getAddresses() {
		if(addresses == null)
			return null;
		
		return addresses.toArray(new IAddress[0]);
	}

	@Override
	public Item[] getEmails() {
		if(emails == null)
			return null;
		
		return emails.toArray(new Item[0]);
	}

	@Override
	public Item[] getGroupId() {
		if(groupIds == null)
			return null;
		
		return groupIds.toArray(new Item[0]);
	}

	@Override
	public Item[] getIMs() {
		if(ims == null)
			return null;
		
		return ims.toArray(new Item[0]);
	}

	@Override
	public Item[] getNickNames() {
		if(nickNames == null)
			return null;
		
		return nickNames.toArray(new Item[0]);
	}

	@Override
	public Item[] getNotes() {
		if(notes == null)
			return null;
		
		return notes.toArray(new Item[0]);
	}
	
	@Override
	public Item[] getNumbers() {
		if(numbers == null)
			return null;
		
		return numbers.toArray(new Item[0]);
	}

	@Override
	public IOrganization[] getOrganizations() {
		if(organizations == null)
			return null;
		
		return organizations.toArray(new IOrganization[0]);
	}

	@Override
	public Item[] getWebsites() {
		if(websites == null)
			return null;
		
		return websites.toArray(new Item[0]);
	}

	@Override
	public String getRawId() {
		return rawId;
	}
	
	@Override
	public void setRawId(String rawId) {
		this.rawId = rawId;
	}

	@Override
	public Item[] getEvent() {
		return events == null ? null : events.toArray(new Item[0]);
	}

	@Override
	public void addEvent(TItem event) {
		if (events == null)
			events = new ArrayList<TItem>();
		events.add(event);
	}
	
	@Override
	public Item[] getSipAddresses() {
		return sipAddresses == null ? null : sipAddresses.toArray(new Item[0]);
	}

	@Override
	public void addSipAddress(TItem sipAddress) {
		if (sipAddresses == null)
			sipAddresses = new ArrayList<TItem>();
		sipAddresses.add(sipAddress);		
	}
	
	@Override
	public int filterItems() {
		int count = 0;
		if (addresses != null)  count += filterItems(addresses);
		if (emails != null)		count += filterItems(emails);
		if (ims != null)		count += filterItems(ims);
		if (notes != null)		count += filterItems(notes);
		if (numbers != null)	count += filterItems(numbers);
		if (organizations != null)	count += filterItems(organizations);
		if (websites != null)	count += filterItems(websites);
		if (nickNames != null)	count += filterItems(nickNames);
		if (groupIds != null)	count += filterItems(groupIds);
		if (events != null)		count += filterItems(events);
		
		filterName();
		return count;
	}
	
	private void filterName() {
		familyName = middleName = givenName = null;
		prefix  = displayName = suffix = null;
		phoneticFamily = phoneticMiddle = phoneticGiven = null;		
		ringtoneId = null;
	}
	
	private <T extends Item> int filterItems(List<T> items) {
		int i = 0;
		if (items != null) {
			for (; i <items.size();) {
				Item item = items.get(i);
			
				if (item.getOldId() == null)
					items.remove(i);
				else
					i++;
			}
		}
		return i;
	}
}
