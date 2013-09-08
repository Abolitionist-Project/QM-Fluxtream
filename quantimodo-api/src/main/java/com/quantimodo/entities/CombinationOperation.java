package com.quantimodo.entities;

public enum CombinationOperation {
	SUM(0), MEAN(1);

	final private byte stableMagicNumber;

	private CombinationOperation(final int stableMagicNumber) {
		this.stableMagicNumber = (byte) stableMagicNumber;
	}

	public static CombinationOperation valueOf(final byte stableMagicNumber) {
		for (final CombinationOperation operation : CombinationOperation.values()) {
			if (stableMagicNumber == operation.stableMagicNumber) return operation;
		}
		return null;
	}

	public byte getMagicNumber() {
		return stableMagicNumber;
	}
}
