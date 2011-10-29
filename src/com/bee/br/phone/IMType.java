package com.bee.br.phone;

public enum IMType {
	AIM(0), Windows_Live(1), Yahoo(2), Skype(3), QQ(4), Google_Talk(5), ICQ(6), Jabber(7), Netmeeting(8), Custom(
			9);

	private int code;

	private IMType(int pCode) {
		code = pCode;
	}

	public int getCode() {
		return code;
	}
	
	public static String toTypeString(int typeCode) {
		for(IMType type: IMType.values()){
			if (type.getCode() == typeCode)
				return type.name();
		}		
		return null;
	}
	
	public static IMType valueOfIgnoreCase(String typeStr) {
		for (IMType type : IMType.values()) {
			if (type.name().equalsIgnoreCase(typeStr))
				return type;
		}
		return IMType.Custom;
	}
}
