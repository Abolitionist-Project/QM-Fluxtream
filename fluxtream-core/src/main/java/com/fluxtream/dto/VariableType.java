package com.fluxtream.dto;

public enum VariableType {
	SINGLE("SINGLE", 0), AGGREGATED("AGGREGATED", 1);

	public String type;
	public int value;

	private VariableType(String type, int value) {
		this.type = type;
		this.value = value;
	}

	public static VariableType resolveType(String typeName) {
		for (VariableType appType : VariableType.values()) {
			if (appType.type.equals(typeName.toUpperCase())) {
				return appType;
			}
		}
		return null;
	}

	public static VariableType resolveTypeByValue(int value) {
		for (VariableType appType : VariableType.values()) {
			if (appType.value == value) {
				return appType;
			}
		}
		return null;
	}
}
