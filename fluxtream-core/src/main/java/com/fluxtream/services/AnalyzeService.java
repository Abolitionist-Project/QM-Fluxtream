package com.fluxtream.services;

import java.util.Map;

import org.joda.time.DateTime;

import com.fluxtream.domain.Guest;
import com.fluxtream.dto.GroupingDto;
import com.fluxtream.dto.VariableBehaviour;
import com.fluxtream.dto.VariableSettingsDto;
import com.fluxtream.dto.VariableType;

public interface AnalyzeService {

	Map<Integer, String> getApplications(VariableBehaviour type);

	VariableSettingsDto findVariableSettings(Guest guest);

	void saveVariableSettings(Guest guest, VariableType inputType, Integer inputId, VariableType outputType,
			Integer outputId, GroupingDto grouping/*, DateTime startDateTime, DateTime endDateTime*/);

}
