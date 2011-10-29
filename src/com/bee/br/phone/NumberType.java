package com.bee.br.phone;


/**
 */
public enum NumberType {
	Custom(0), Home(1), Mobile(2), Work(3), Work_Fax(4), Home_Fax(5), Pager(6), Other(
			7), Callback(8), Car(9), Company_Main(10), ISDN(11), Main(12), Other_Fax(
			13), Radio(14), Telex(15), TTY_TTD(16), Work_Mobile(17), Work_Pager(
			18), Assistant(19), MMS(20);

	private int code;

	private NumberType(int pCode) {
		code = pCode;
	}

	public int getCode() {
		return code;
	}

	public static String toTypeString(int typeCode) {
		for (NumberType type : NumberType.values()) {
			if (type.getCode() == typeCode)
				return type.name();
		}
		return null;
	}

	public static NumberType valueOfIgnoreCase(String typeStr) {
		for (NumberType type : NumberType.values()) {
			if (type.name().equalsIgnoreCase(typeStr))
				return type;
		}
		return NumberType.Custom;
	}
}