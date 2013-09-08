package com.fluxtream.dto;

import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.joda.time.DateTime;

import com.google.gson.annotations.Expose;

public class VariableDto {

	@Expose
	public Integer id;
	@Expose
	public Long dataOwner;
	@Expose
	public String name;

	@Expose
	public Integer applicationId;
	@Expose
	public String application;

	@Expose
	public Integer categoryId;
	@Expose
	public String categoryName;

	@Expose
	public UnitDto unitDto;

	@Expose
	public Boolean keepZeroes;
	@Expose
	public Double minValue;
	@Expose
	public Double maxValue;

	@Expose
	public Double timeShift;
	@Expose
	public Integer timeShiftUnit;

	@Expose
	public GroupingDto nonEmptyPeriod;

	@Expose
	public FillingDto filling;

	@Expose
	public GroupingDto grouping;

	@Expose
	public VariableType type;

	@Expose
	public boolean global;
	@Expose
	public Integer priority;

	@Expose
	public RemainingType remainingType;

	// ordered by priority
	@Expose
	private List<VariableDto> prioritizedVariables;

	@Expose
	private List<VariableDto> remainingVariables;

	// values for internal calculations
	@JsonIgnore
	private Map<DateTime, Double> values;

	// contains grouped variable values and datetime in millisec
	@Expose
	private Map<Long, Double> groupedValues;

	public List<VariableDto> getRemainingVariable() {
		return remainingVariables;
	}

	public void setRemainingVariables(List<VariableDto> remainingVariables) {
		this.remainingVariables = remainingVariables;
	}

	public Map<Long, Double> getGroupedValues() {
		return groupedValues;
	}

	public void setGroupedValues(Map<Long, Double> groupedValues) {
		this.groupedValues = groupedValues;
	}

	public List<VariableDto> getPrioritizedVariables() {
		return prioritizedVariables;
	}

	public void setPrioritizedVariables(List<VariableDto> prioritizedVariables) {
		this.prioritizedVariables = prioritizedVariables;
	}

	public Map<DateTime, Double> getValues() {
		return values;
	}

	public void setValues(Map<DateTime, Double> values) {
		this.values = values;
	}

}
