package com.quantimodo.data;

public class VariableCategory {
	private final String name;
	
	public VariableCategory(final String name) {
		this.name = name;
	}
	
	public String getName() { return name; }
	
	public String toString() {
		return new StringBuilder("<VariableCategory: ")
		           .append("{ name: ").append(getName())
		           .append(" }>").toString();
	}
}
