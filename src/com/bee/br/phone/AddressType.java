package com.bee.br.phone;
/**
 * the address type
 * @author liyajie1209
 *
 */
public enum AddressType {

	Custom(0), Home(1), Work(2), Other(3);

	private int code;

	private AddressType(int pCode) {
		code = pCode;
	}

	public int getCode() {
		return code;
	}

	/**
	 * �����ת��Ϊ����
	 */
	public static String toTypeString(int typeCode) {
		for (AddressType type : AddressType.values()) {
			if (type.getCode() == typeCode)
				return type.name();
		}
		return null;
	}

	public static AddressType valueOfIgnoreCase(String typeStr) {
		for (AddressType type : AddressType.values()) {
			if (type.name().equalsIgnoreCase(typeStr))
				return type;
		}
		return AddressType.Custom;
	}
}
