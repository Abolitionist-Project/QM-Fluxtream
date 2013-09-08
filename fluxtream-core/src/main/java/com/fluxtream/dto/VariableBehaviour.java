package com.fluxtream.dto;


public enum VariableBehaviour {
	OUTPUT(1), INPUT(0);

	public final int exceptValue;

	private VariableBehaviour(int exceptValue) {
		this.exceptValue = exceptValue;

	}

	public static VariableBehaviour resolveType(String typeName) {
		return VariableBehaviour.valueOf(typeName.toUpperCase());
	}
}
