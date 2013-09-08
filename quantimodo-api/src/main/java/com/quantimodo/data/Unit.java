package com.quantimodo.data;

import java.util.Arrays;

public class Unit {
	private final String name;
	private final String abbreviatedName;
	private final String categoryName;
	private final ConversionStep[] conversionSteps;
	
	public Unit(final String name, final String abbreviatedName, final String categoryName, final ConversionStep[] conversionSteps) {
		this.name = name;
		this.abbreviatedName = abbreviatedName;
		this.categoryName = categoryName;
		this.conversionSteps = conversionSteps;
	}
	
	public String getName() { return name; }
	public String getAbbreviatedName() { return abbreviatedName; }
	public String getCategoryName() { return categoryName; }
	public ConversionStep[] getConversionSteps() { return conversionSteps; }
	
	public static final class ConversionStep {
		public static enum Operation {
			ADD(0), MULTIPLY(1);
			
			final private byte stableMagicNumber;
			
			private Operation(final int stableMagicNumber) {
				this.stableMagicNumber = (byte) stableMagicNumber;
			}
			
			public static Operation valueOf(final byte stableMagicNumber) {
				for (final Operation operation : Operation.values()) {
					if (stableMagicNumber == operation.stableMagicNumber) return operation;
				}
				return null;
			}
			
			public byte getMagicNumber() {
				return stableMagicNumber;
			}
		}		
		
		public final Operation operation;
		public final double value;
		
		public ConversionStep(final Operation operation, final double value) {
			this.operation = operation;
			this.value = value;
		}
		
		public ConversionStep(final String operation, final double value) {
			this(Operation.valueOf(operation), value);
		}
		
		public ConversionStep(final byte operation, final double value) {
			this(Operation.valueOf(operation), value);
		}
		
		public double forward(final double value) {
			switch (this.operation) {
				case ADD: return value + this.value;
				case MULTIPLY: return value * this.value;
				default: throw new RuntimeException("Unknown kind of unit conversion step");
			}
		}
		
		public double reverse(final double value) {
			switch (this.operation) {
				case ADD: return value - this.value;
				case MULTIPLY: return value / this.value;
				default: throw new RuntimeException("Unknown kind of unit conversion step");
			}
		}
		
		public String toString() {
			return new StringBuilder(this.operation.toString()).append(' ').append(Double.toString(this.value)).toString();
		}
	}
	
	public static final double convert(final Unit fromUnit, final Unit toUnit, double value) {
		for (final ConversionStep step : fromUnit.getConversionSteps()) { value = step.forward(value); }
		final ConversionStep[] toUnitSteps = toUnit.getConversionSteps();
		for (int i = toUnitSteps.length - 1; i >= 0; i--) { value = toUnitSteps[i].reverse(value); }
		return value;
	}
	
	public String toString() {
		return new StringBuilder("<Unit: ")
		           .append("{ name: ").append(getName())
		           .append(", abbreviatedName: ").append(getAbbreviatedName())
		           .append(", categoryName: ").append(getCategoryName())
		           .append(", conversionSteps: ").append(Arrays.toString(getConversionSteps()))
		           .append(" }>").toString();
	}
}
