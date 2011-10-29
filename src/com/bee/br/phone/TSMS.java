package com.bee.br.phone;


public class TSMS implements ISMS {
	private String id;
	private String contactId;
	private String number;
	private String time;
	private String content;
	private String threadId;
	private State state;

	@Override
	public String getContent() {
		return content;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getNumber() {
		return number;
	}

	@Override
	public String getTime() {
		return time;
	}

	@Override
	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public void setNumber(String number) {
		this.number = number;
	}

	@Override
	public void setTime(String time) {
		this.time = time;
	}

	@Override
	public String getThreadId() {
		return threadId;
	}

	@Override
	public void setThreadId(String threadId) {
		this.threadId = threadId;		
	}

	@Override
	public State getState() {
		return state;
	}

	@Override
	public void setState(State state) {
		this.state = state;		
	}

	@Override
	public String getContactId() {
		return contactId;
	}
	@Override
	public void setContactId(String contactId) {
		this.contactId = contactId;
	}
}
