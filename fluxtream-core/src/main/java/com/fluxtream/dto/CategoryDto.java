package com.fluxtream.dto;

import java.util.List;

public class CategoryDto {

	public Integer id;

	public String name;

	public Long dataOwner;

	public boolean global;

	private List<VariableDto> variables;

	public List<VariableDto> getVariables() {
		return variables;
	}

	public void setVariables(List<VariableDto> variables) {
		this.variables = variables;
	}
}
