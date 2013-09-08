package com.fluxtream.services;

import java.util.List;

import org.joda.time.DateTime;

import com.fluxtream.domain.Guest;
import com.fluxtream.dto.GroupingDto;
import com.fluxtream.dto.UnitDto;
import com.fluxtream.dto.VariableBehaviour;
import com.fluxtream.dto.VariableDto;
import com.fluxtream.dto.VariableType;

public interface VariablesService {

	List<VariableDto> getVariables(Guest guest, VariableBehaviour behaviour, Integer categoryId, VariableType type);

	List<VariableDto> getVariables(Guest guest, VariableBehaviour behaviour, VariableType type);

	List<VariableDto> getVariables(Guest guest, VariableBehaviour behaviour, VariableType type, Integer siUnit);

	VariableDto getVariable(Guest guest, Integer varId);

	VariableDto getVariable(Guest guest, String applicationName, String variableName);

	VariableDto getVariableValues(Guest guest, Integer varId, DateTime startTime, DateTime endTime, GroupingDto grouping);

	Integer saveVariable(Guest guest, VariableDto variableDto);

	List<UnitDto> getUnits();

	List<UnitDto> getTimeShiftUnits();

	void deleteVariable(Guest guest, Integer varId);

	List<GroupingDto> getVariableNonEmptyMeasures();

}
