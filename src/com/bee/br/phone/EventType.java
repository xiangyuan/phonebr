package com.bee.br.phone;

public enum EventType {
	Custom(0), Anniversary(1), Other(2), Birthday(3);
	
	private int code;

	private EventType(int pCode) {
		code = pCode;
	}

	public int getCode() {
		return code;
	}

	public static String toTypeString(int typeCode) {
		for (EventType type : EventType.values()) {
			if (type.getCode() == typeCode)
				return type.name();
		}
		return null;
	}

	public static EventType valueOfIgnoreCase(String typeStr) {
		for (EventType type : EventType.values()) {
			if (type.name().equalsIgnoreCase(typeStr))
				return type;
		}
		return EventType.Custom;
	}
}
