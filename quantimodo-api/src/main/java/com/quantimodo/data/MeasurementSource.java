package com.quantimodo.data;

public class MeasurementSource {
	private final String name;
	
	public MeasurementSource(final String name) {
		this.name = name;
	}
	
	public String getName() { return name; }
	
	public String toString() {
		return new StringBuilder("<MeasurementSource: ")
		           .append("{ name: ").append(getName())
		           .append(" }>").toString();
	}
}
