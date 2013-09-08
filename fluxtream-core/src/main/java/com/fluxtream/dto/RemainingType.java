package com.fluxtream.dto;


public enum RemainingType {
	SUMMABLE(0), AVERAGE(1);

	public final int value;

	private RemainingType(int value) {
		this.value = value;

	}

	public static RemainingType resolve(String remainingTypeName) {
		return RemainingType.valueOf(remainingTypeName.toUpperCase());
	}

	public static RemainingType resolveByValue(int value) {
		for (RemainingType remainingType : RemainingType.values()) {
			if (remainingType.value == value) {
				return remainingType;
			}
		}
		return null;
	}
}
