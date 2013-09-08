package com.quantimodo.data;

public class Variable {
	private final String name;
	private final String categoryName;
	private final String abbreviatedDefaultUnitName;
	private final CombinationOperation combinationOperation;
	
	public Variable(final String name, final String categoryName, final String abbreviatedDefaultUnitName, final CombinationOperation combinationOperation) {
		this.name = name;
		this.categoryName = categoryName;
		this.abbreviatedDefaultUnitName = abbreviatedDefaultUnitName;
		this.combinationOperation = combinationOperation;
	}
	
	public String getName() { return name; }
	public String getCategoryName() { return categoryName; }
	public String getAbbreviatedDefaultUnitName() { return abbreviatedDefaultUnitName; }
	public CombinationOperation getCombinationOperation() { return combinationOperation; }
	
	public String toString() {
		return new StringBuilder("<Variable: ")
		           .append("{ name: ").append(getName())
		           .append(", categoryName: ").append(getCategoryName())
		           .append(", abbreviatedDefaultUnitName: ").append(getAbbreviatedDefaultUnitName())
		           .append(", combinationOperation: ").append(getCombinationOperation().toString())
		           .append(" }>").toString();
	}
}
