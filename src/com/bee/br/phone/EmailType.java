package com.bee.br.phone;

public enum EmailType {

	 Custom(0),Home(1), Work(2), Other(3), Mobile(4);

	private int code;

	private EmailType(int pCode) {
		code = pCode;
	}

	public int getCode() {
		return code;
	}
	
	public static String toTypeString(int typeCode) {
		for(EmailType type: EmailType.values()){
			if (type.getCode() == typeCode)
				return type.name();
		}		
		return null;
	}
	
	public static EmailType valueOfIgnoreCase(String typeStr) {
		for (EmailType type : EmailType.values()) {
			if (type.name().equalsIgnoreCase(typeStr))
				return type;
		}
		return EmailType.Custom;
	}
}
