package com.quantimodo.data;

public class VariableUserSettings {
	private final String variableName;
	private final String abbreviatedUnitName;
	
	public VariableUserSettings(final String variableName, final String abbreviatedUnitName) {
		this.variableName = variableName;
		this.abbreviatedUnitName = abbreviatedUnitName;
	}
	
	public String getVariableName() { return variableName; }
	public String getAbbreviatedUnitName() { return abbreviatedUnitName; }
	
	public String toString() {
		return new StringBuilder("<VariableUserSettings: ")
		           .append("{ variableName: ").append(getVariableName())
		           .append(", abbreviatedUnitName: ").append(getAbbreviatedUnitName())
		           .append(" }>").toString();
	}
}
