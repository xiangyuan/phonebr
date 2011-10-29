package com.bee.br.phone;

public interface ISMS{
	public enum State{
		none,
		read,
		unread,
		draft,
		sent,
		failed,
		queued;
		
		public static State valueOfCode(int code) {
			for (State state : values()) {
				if (state.ordinal() == code)
					return state;
			}
			return State.none;
		}
	}
	
	/**
	 */
	public String getId();	
	public void setId(String id);
	
	/**
	 */
	public String getNumber();	
	public void setNumber(String number);
	/**
	 */
	public String getTime();	
	public void setTime (String time);
	/**
	 */
	public String getContent();
	public void setContent(String content);
	
	/**
	 */
	public String getThreadId();	
	public void setThreadId(String threadId);
	
	/**
	 */
	public State getState();	
	public void setState(State state);
	
	String getContactId();
	void setContactId(String contactId);
}
