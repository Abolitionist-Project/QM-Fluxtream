package com.fluxtream.services.impl;

import java.util.Map;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fluxtream.Configuration;
import com.fluxtream.dao.VariablesDao;
import com.fluxtream.domain.Guest;
import com.fluxtream.domain.VariableSettings;
import com.fluxtream.dto.GroupingDto;
import com.fluxtream.dto.VariableBehaviour;
import com.fluxtream.dto.VariableDto;
import com.fluxtream.dto.VariableSettingsDto;
import com.fluxtream.dto.VariableType;
import com.fluxtream.services.AnalyzeService;

@Service
public class AnalyzeServiceImpl implements AnalyzeService {

	private static final Logger LOG = LoggerFactory.getLogger(AnalyzeServiceImpl.class);

	@Autowired
	VariablesDao variablesDao;

	@Autowired
	Configuration configuration;

	@Override
	public Map<Integer, String> getApplications(VariableBehaviour type) {
		return variablesDao.getApplications(type);
	}

	@Override
	public VariableSettingsDto findVariableSettings(Guest guest) {
		VariableSettings variableSettings = variablesDao.findVariableSettings(guest.getId());
		if (variableSettings == null) {
			return null;
		}
		VariableSettingsDto variableSettingsDto = new VariableSettingsDto();
		variableSettingsDto.inputType = variableSettings.inputType;
		variableSettingsDto.inputId = variableSettings.inputId;
		VariableDto inputVariable = variablesDao.findVariable(guest, variableSettings.inputId);
		variableSettingsDto.inputCategory = inputVariable.categoryId;

		variableSettingsDto.outputType = variableSettings.outputType;
		VariableDto outputVariable = variablesDao.findVariable(guest, variableSettings.outputId);
		variableSettingsDto.outputId = variableSettings.outputId;
		variableSettingsDto.outputCategory = outputVariable.categoryId;

		variableSettingsDto.grouping = GroupingDto.resolveByValue(variableSettings.groupingValue);
		//variableSettingsDto.startTime = new DateTime(variableSettings.startTime,
		//		configuration.getTimeZoneForGuest(guest));
		//variableSettingsDto.endTime = new DateTime(variableSettings.endTime, configuration.getTimeZoneForGuest(guest));
		return variableSettingsDto;
	}

	@Override
	public void saveVariableSettings(Guest guest, VariableType inputType, Integer inputId, VariableType outputType,
			Integer outputId, GroupingDto grouping/*, DateTime startDateTime, DateTime endDateTime*/) {
		variablesDao.saveVariablesSettings(guest.getId(), inputType, inputId, outputType, outputId, grouping/*,
				startDateTime, endDateTime*/);
	}

}
