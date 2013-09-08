package com.fluxtream.dao;

import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.fluxtream.domain.Guest;
import com.fluxtream.domain.VariableSettings;
import com.fluxtream.dto.GroupingDto;
import com.fluxtream.dto.VariableBehaviour;
import com.fluxtream.dto.VariableDto;
import com.fluxtream.dto.VariableType;

public interface VariablesDao {

	Integer saveVariable(Guest guest, VariableDto variableDto);

	void updateVariable(Guest guest, VariableDto variableDto);

	List<VariableDto> getVariables(Guest guest, VariableBehaviour behaviour, Integer categoryId, VariableType type);

	List<VariableDto> getVariablesForGuest(Guest guest, VariableBehaviour behaviour, VariableType type);

	VariableDto findVariable(Guest guest, Integer varId);

	VariableDto findVariable(Guest guest, String applicationName, String variableName);

	/**
	 * Get applications with specified variable types
	 * 
	 * @param variableType
	 * @return
	 */
	Map<Integer, String> getApplications(VariableBehaviour variableType);

	Map<DateTime, Double> getVariableValues(Guest guest, Integer varId, DateTime startTime, DateTime endTime,
			DateTimeZone dateTimeZone);

	VariableSettings findVariableSettings(long guestId);

	void saveVariablesSettings(long guestId, VariableType inputType, Integer inputId, VariableType outputType,
			Integer outputId, GroupingDto grouping/*, DateTime startDateTime, DateTime endDateTime*/);

	void deleteVariable(Guest guest, Integer varId);

	List<VariableDto> getVariablesBySiUnitForGuest(Guest guest, VariableBehaviour behaviour, VariableType type,
			Integer siUnit);

}
