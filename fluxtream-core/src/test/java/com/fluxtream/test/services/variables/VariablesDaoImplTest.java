package com.fluxtream.test.services.variables;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.fluxtream.dao.VariablesDao;
import com.fluxtream.domain.Guest;
import com.fluxtream.domain.VariableSettings;
import com.fluxtream.dto.FillingDto;
import com.fluxtream.dto.FillingDto.FillingType;
import com.fluxtream.dto.GroupingDto;
import com.fluxtream.dto.RemainingType;
import com.fluxtream.dto.UnitDto;
import com.fluxtream.dto.VariableBehaviour;
import com.fluxtream.dto.VariableDto;
import com.fluxtream.dto.VariableType;

public class VariablesDaoImplTest implements VariablesDao {

	private final static Map<Integer, VariableDto> VARIABLE_DTOS = new HashMap<Integer, VariableDto>();
	private final static Map<Integer, Map<DateTime, Double>> VARIABLE_VALUES = new HashMap<Integer, Map<DateTime, Double>>();
	static {
		VariableDto var1 = new VariableDto();
		var1.id = 20;
		Map<DateTime, Double> prior1Values = new HashMap<DateTime, Double>();
		prior1Values.put(new DateTime(2013, 4, 13, 9, 0, 0, 0), 10.0);
		prior1Values.put(new DateTime(2013, 4, 13, 10, 30, 0, 0), 20.0);
		prior1Values.put(new DateTime(2013, 4, 13, 10, 30, 10, 0), 30.0);
		prior1Values.put(new DateTime(2013, 4, 14, 10, 0, 0, 0), 20.0);
		prior1Values.put(new DateTime(2013, 4, 24, 10, 0, 0, 0), 0.0);
		prior1Values.put(new DateTime(2013, 5, 30, 10, 0, 0, 0), 20.0);
		VARIABLE_DTOS.put(var1.id, var1);
		UnitDto priorUnitDto1 = new UnitDto();
		priorUnitDto1.siUnit = 1;
		priorUnitDto1.siUnitsPerThisUnit = 1;
		priorUnitDto1.useAverage = true;
		var1.unitDto = priorUnitDto1;
		VARIABLE_VALUES.put(var1.id, prior1Values);

		VariableDto var2 = new VariableDto();
		var2.id = 30;
		Map<DateTime, Double> prior2Values = new HashMap<DateTime, Double>();
		prior2Values.put(new DateTime(2013, 4, 13, 9, 0, 0, 0), 20.0);
		prior2Values.put(new DateTime(2013, 4, 13, 9, 10, 0, 0), 20.0);
		prior2Values.put(new DateTime(2013, 4, 13, 10, 40, 0, 0), 40.0);
		prior2Values.put(new DateTime(2013, 4, 13, 10, 40, 10, 0), 60.0);
		prior2Values.put(new DateTime(2013, 4, 14, 10, 0, 0, 0), 40.0);
		prior2Values.put(new DateTime(2013, 4, 25, 10, 0, 0, 0), 20.0);
		prior2Values.put(new DateTime(2013, 5, 30, 10, 0, 0, 0), 40.0);
		VARIABLE_DTOS.put(var2.id, var2);
		UnitDto priorUnitDto2 = new UnitDto();
		priorUnitDto2.siUnit = 1;
		priorUnitDto2.siUnitsPerThisUnit = 2;
		priorUnitDto2.useAverage = true;
		var2.unitDto = priorUnitDto2;
		VARIABLE_VALUES.put(var2.id, prior2Values);

		VariableDto variableDto1 = new VariableDto();
		variableDto1.id = 1;
		variableDto1.keepZeroes = true;
		variableDto1.setPrioritizedVariables(Arrays.asList(var1, var2));
		variableDto1.type = VariableType.AGGREGATED;
		UnitDto unitDto1 = new UnitDto();
		unitDto1.siUnit = 1;
		unitDto1.siUnitsPerThisUnit = 4;
		unitDto1.useAverage = false;
		variableDto1.unitDto = unitDto1;
		VARIABLE_DTOS.put(variableDto1.id, variableDto1);

		VariableDto variableDto2 = new VariableDto();
		variableDto2.id = 2;
		variableDto2.keepZeroes = false;
		variableDto2.setRemainingVariables(Arrays.asList(var1, var2));
		variableDto2.type = VariableType.AGGREGATED;
		UnitDto unitDto2 = new UnitDto();
		unitDto2.siUnit = 1;
		unitDto2.siUnitsPerThisUnit = 4;
		unitDto2.useAverage = true;
		variableDto2.unitDto = unitDto2;
		variableDto2.remainingType = RemainingType.SUMMABLE;
		VARIABLE_DTOS.put(variableDto2.id, variableDto2);

		VariableDto variableDto3 = new VariableDto();
		variableDto3.id = 3;
		variableDto3.keepZeroes = false;
		variableDto3.type = VariableType.AGGREGATED;
		UnitDto unitDto3 = new UnitDto();
		unitDto3.siUnit = 1;
		unitDto3.siUnitsPerThisUnit = 1;
		unitDto3.useAverage = false;
		variableDto3.unitDto = unitDto3;
		Map<DateTime, Double> values3 = new HashMap<DateTime, Double>();
		values3.put(new DateTime(2013, 4, 13, 9, 0, 0, 0), 20.0);
		values3.put(new DateTime(2013, 4, 13, 9, 10, 0, 0), 20.0);
		VARIABLE_VALUES.put(variableDto3.id, values3);
		VARIABLE_DTOS.put(variableDto3.id, variableDto3);

		VariableDto variableDto4 = new VariableDto();
		variableDto4.id = 4;
		variableDto4.keepZeroes = false;
		variableDto4.setRemainingVariables(Arrays.asList(var1, var2));
		variableDto4.type = VariableType.AGGREGATED;
		UnitDto unitDto4 = new UnitDto();
		unitDto4.siUnit = 1;
		unitDto4.siUnitsPerThisUnit = 4;
		unitDto4.useAverage = true;
		variableDto4.unitDto = unitDto4;
		variableDto4.remainingType = RemainingType.SUMMABLE;
		variableDto4.timeShiftUnit = 4;
		variableDto4.timeShift = 10.0;
		VARIABLE_DTOS.put(variableDto4.id, variableDto4);

		VariableDto variableDto5 = new VariableDto();
		variableDto5.id = 5;
		variableDto5.keepZeroes = false;
		variableDto5.type = VariableType.AGGREGATED;
		UnitDto unitDto5 = new UnitDto();
		unitDto5.siUnit = 1;
		unitDto5.siUnitsPerThisUnit = 4;
		unitDto5.useAverage = true;
		variableDto5.unitDto = unitDto5;
		variableDto5.remainingType = RemainingType.SUMMABLE;
		variableDto5.nonEmptyPeriod = GroupingDto.HOURLY;
		FillingDto fillingDto = new FillingDto();
		fillingDto.type = FillingType.CONSTANT;
		fillingDto.value = 15.0;
		variableDto5.filling = fillingDto;
		VARIABLE_DTOS.put(variableDto5.id, variableDto5);
		Map<DateTime, Double> values5 = new HashMap<DateTime, Double>();
		values5.put(new DateTime(2013, 4, 13, 9, 0, 0, 0), 20.0);
		values5.put(new DateTime(2013, 4, 13, 11, 10, 0, 0), 10.0);
		VARIABLE_VALUES.put(variableDto5.id, values5);

		VariableDto variableDto6 = new VariableDto();
		variableDto6.id = 6;
		variableDto6.keepZeroes = false;
		variableDto6.type = VariableType.AGGREGATED;
		UnitDto unitDto6 = new UnitDto();
		unitDto6.siUnit = 1;
		unitDto6.siUnitsPerThisUnit = 4;
		unitDto6.useAverage = true;
		variableDto6.unitDto = unitDto6;
		variableDto6.remainingType = RemainingType.SUMMABLE;
		variableDto6.nonEmptyPeriod = GroupingDto.DAILY;
		FillingDto fillingDto6 = new FillingDto();
		fillingDto6.type = FillingType.CONSTANT;
		fillingDto6.value = 48.0;
		variableDto6.filling = fillingDto6;
		VARIABLE_DTOS.put(variableDto6.id, variableDto6);
		Map<DateTime, Double> values6 = new HashMap<DateTime, Double>();
		values6.put(new DateTime(2013, 4, 13, 9, 0, 0, 0), 2.0);
		values6.put(new DateTime(2013, 4, 13, 10, 0, 0, 0), 4.0);
		values6.put(new DateTime(2013, 4, 15, 11, 10, 0, 0), 8.0);
		VARIABLE_VALUES.put(variableDto6.id, values6);

		VariableDto variableDto7 = new VariableDto();
		variableDto7.id = 7;
		variableDto7.keepZeroes = false;
		variableDto7.setRemainingVariables(Arrays.asList(var1));
		variableDto7.type = VariableType.AGGREGATED;
		UnitDto unitDto7 = new UnitDto();
		unitDto7.siUnit = 1;
		unitDto7.siUnitsPerThisUnit = 4;
		unitDto7.useAverage = true;
		variableDto7.unitDto = unitDto7;
		variableDto7.remainingType = RemainingType.SUMMABLE;
		variableDto7.timeShiftUnit = 4;
		variableDto7.timeShift = -1.0;
		VARIABLE_DTOS.put(variableDto7.id, variableDto7);
	}

	@Override
	public Integer saveVariable(Guest guest, VariableDto variableDto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateVariable(Guest guest, VariableDto variableDto) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<VariableDto> getVariables(Guest guest, VariableBehaviour behaviour, Integer categoryId,
			VariableType type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<VariableDto> getVariablesForGuest(Guest guest, VariableBehaviour behaviour, VariableType type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public VariableDto findVariable(Guest guest, Integer varId) {
		return VARIABLE_DTOS.get(varId);
	}

	@Override
	public VariableDto findVariable(Guest guest, String applicationName, String variableName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Integer, String> getApplications(VariableBehaviour variableType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<DateTime, Double> getVariableValues(Guest guest, Integer varId, DateTime startTime, DateTime endTime,
			DateTimeZone dateTimeZone) {
		Map<DateTime, Double> allValues = VARIABLE_VALUES.get(varId);
		Map<DateTime, Double> map = new HashMap<DateTime, Double>();
		for (DateTime dateTime : allValues.keySet()) {
			if (dateTime.isAfter(startTime) && dateTime.isBefore(endTime)) {
				map.put(dateTime, allValues.get(dateTime));
			}
		}
		return map;
	}

	@Override
	public VariableSettings findVariableSettings(long guestId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveVariablesSettings(long guestId, VariableType inputType, Integer inputId, VariableType outputType,
			Integer outputId, GroupingDto grouping/*, DateTime startDateTime, DateTime endDateTime*/) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteVariable(Guest guest, Integer varId) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<VariableDto> getVariablesBySiUnitForGuest(Guest guest, VariableBehaviour behaviour, VariableType type,
			Integer siUnit) {
		// TODO Auto-generated method stub
		return null;
	}

}
