package com.bee.br.phone;


/**
 */
public enum OrganizationType {

	Custom(0), Work(1), Other(2);

	private int code;

	private OrganizationType(int pCode) {
		code = pCode;
	}

	public int getCode() {
		return code;
	}

	public static String toTypeString(int typeCode) {
		for (OrganizationType type : OrganizationType.values()) {
			if (type.getCode() == typeCode)
				return type.name();
		}
		return null;
	}

	public static OrganizationType valueOfIgnoreCase(String typeStr) {
		for (OrganizationType type : OrganizationType.values()) {
			if (type.name().equalsIgnoreCase(typeStr))
				return type;
		}
		return OrganizationType.Custom;
	}
}
