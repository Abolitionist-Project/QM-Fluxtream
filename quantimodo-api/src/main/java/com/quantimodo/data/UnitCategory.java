package com.quantimodo.data;

public class UnitCategory {

	private Long id;
	private String name;

	public UnitCategory(final String name) {
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return new StringBuilder("<UnitCategory: ")
				.append("{ name: ").append(getName())
				.append(" }>").toString();
	}
}
