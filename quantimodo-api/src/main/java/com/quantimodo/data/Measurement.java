package com.quantimodo.data;

public class Measurement {
	private final String measurementSource;
	
	private final String variableName;
	private final CombinationOperation combinationOperation;
	
	private final int epochMinuteTimestamp;
	private final double value;
	private final String abbreviatedUnitName;
	
	public Measurement(final String measurementSource,
	                   final String variableName, final CombinationOperation combinationOperation,
	                   final long timestamp, final double value, final String abbreviatedUnitName) {
		this.measurementSource = measurementSource;
		this.variableName = variableName;
		this.combinationOperation = combinationOperation;
		this.epochMinuteTimestamp = (int) (timestamp/60000L);
		this.value = value;
		this.abbreviatedUnitName = abbreviatedUnitName;
	}
	
	public String getMeasurementSource() { return measurementSource; }
	public String getVariableName() { return variableName; }
	public CombinationOperation getCombinationOperation() { return combinationOperation; }
	public long getTimestamp() { return ((long) epochMinuteTimestamp)*60000L; }
	public double getValue() { return value; }
	public String getAbbreviatedUnitName() { return abbreviatedUnitName; }
	
	public String toString() {
		return new StringBuilder("<Measurement: ")
		           .append("{ measurementSource: ").append(getMeasurementSource())
		           .append(", variableName: ").append(getVariableName())
		           .append(", combinationOperation: ").append(getCombinationOperation())
		           .append(", timestamp: ").append(getTimestamp())
		           .append(", value: ").append(getValue())
		           .append(", abbreviatedUnitName: ").append(getAbbreviatedUnitName())
		           .append(" }>").toString();
	}
}
