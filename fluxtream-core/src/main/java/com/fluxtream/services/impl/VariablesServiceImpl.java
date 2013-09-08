package com.fluxtream.services.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fluxtream.Configuration;
import com.fluxtream.dao.UnitsDao;
import com.fluxtream.dao.VariablesDao;
import com.fluxtream.domain.Guest;
import com.fluxtream.dto.GroupingDto;
import com.fluxtream.dto.RemainingType;
import com.fluxtream.dto.UnitDto;
import com.fluxtream.dto.VariableBehaviour;
import com.fluxtream.dto.VariableDto;
import com.fluxtream.dto.VariableType;
import com.fluxtream.services.VariablesService;
import com.fluxtream.services.exceptions.BaseServiceException;

@Service
public class VariablesServiceImpl implements VariablesService {

	private static final Logger LOG = LoggerFactory.getLogger(VariablesServiceImpl.class);

	private static List<GroupingDto> variableNonEmptyMeasures = Arrays.asList(GroupingDto.DAILY, GroupingDto.WEEKLY,
			GroupingDto.MONTHLY);

	@Autowired
	VariablesDao variablesDao;

	@Autowired
	UnitsDao unitsDao;

	@Autowired
	Configuration configuration;

	@Override
	public List<UnitDto> getUnits() {
		return unitsDao.getUnits();
	}

	@Override
	public List<UnitDto> getTimeShiftUnits() {
		List<UnitDto> units = unitsDao.getUnits(Integer.valueOf(configuration
				.get("database.defaults.variable.settings.time_shift_si_unit")));
		List<Integer> excludedUnits = configuration
				.getAsIntList("database.defaults.variable.settings.exclude_time_shift_units");
		for (Iterator<UnitDto> iterator = units.iterator(); iterator.hasNext();) {
			UnitDto unitDto = iterator.next();
			if (excludedUnits.contains(unitDto.id)) {
				iterator.remove();
			}
		}
		return units;
	}

	@Override
	public Integer saveVariable(Guest guest, VariableDto variableDto) {
		if (variableDto.id == null) {
			return variablesDao.saveVariable(guest, variableDto);
		}
		variablesDao.updateVariable(guest, variableDto);
		return variableDto.id;
	}

	@Override
	public List<VariableDto> getVariables(Guest guest, VariableBehaviour behaviour, Integer categoryId,
			VariableType type) {
		return variablesDao.getVariables(guest, behaviour, categoryId, type);
	}

	@Override
	public List<VariableDto> getVariables(Guest guest, VariableBehaviour behaviour, VariableType type) {
		return variablesDao.getVariablesForGuest(guest, behaviour, type);
	}

	@Override
	public List<VariableDto> getVariables(Guest guest, VariableBehaviour behaviour, VariableType type, Integer siUnit) {
		return variablesDao.getVariablesBySiUnitForGuest(guest, behaviour, type, siUnit);
	}

	@Override
	public VariableDto getVariable(Guest guest, Integer varId) {
		return variablesDao.findVariable(guest, varId);
	}

	@Override
	public VariableDto getVariable(Guest guest, String applicationName, String variableName) {
		return variablesDao.findVariable(guest, applicationName, variableName);
	}

	@Override
	public void deleteVariable(Guest guest, Integer varId) {
		variablesDao.deleteVariable(guest, varId);
	}

	@Override
	public VariableDto getVariableValues(Guest guest, Integer varId, DateTime startTime, DateTime endTime,
			GroupingDto grouping) {
		VariableDto variableInfo = variablesDao.findVariable(guest, varId);
		if (variableInfo == null) {
			throw new BaseServiceException("Trying to get values for not existed variable", this.getClass());
		}
		variableInfo.grouping = grouping;

		if (!CollectionUtils.isEmpty(variableInfo.getPrioritizedVariables())) {
			for (VariableDto variableDto : variableInfo.getPrioritizedVariables()) {
				variableDto.setValues(variablesDao.getVariableValues(guest, variableDto.id, startTime, endTime,
						configuration.getTimeZoneForGuest(guest)));
			}
			doPrioritizedGrouping(variableInfo);
		}

		if (CollectionUtils.isEmpty(variableInfo.getValues())
				&& !CollectionUtils.isEmpty(variableInfo.getRemainingVariable())) {
			for (VariableDto variableDto : variableInfo.getRemainingVariable()) {
				variableDto.setValues(variablesDao.getVariableValues(guest, variableDto.id, startTime, endTime,
						configuration.getTimeZoneForGuest(guest)));
			}
			doRemainingGrouping(variableInfo);
		}

		if (CollectionUtils.isEmpty(variableInfo.getValues())) {
			variableInfo.setValues(variablesDao.getVariableValues(guest, variableInfo.id, startTime, endTime,
					configuration.getTimeZoneForGuest(guest)));
		}

		doGrouping(variableInfo);

		return variableInfo;

	}

	/**
	 * Analyze variableInfo.nonEmptyPeriod, variableInfo.fillingType,
	 * variableInfo.fillingValue value and fill variableInfo according to it
	 * 
	 * @param variableInfo
	 */
	@Deprecated
	private void processNonEmptyPeriods(VariableDto variableInfo, DateTime startTime, DateTime endTime) {
		if (variableInfo.filling == null) {
			return;
		}
		switch (variableInfo.filling.type) {
		case IS_MISSING:
			break;
		case CONSTANT:
			// processConstantFilling(variableInfo, startTime, endTime);
			break;
		case AVERAGE:
			break;
		case INTERPOLATE:
			break;
		}
	}

	@Deprecated
	private void processConstantFilling(VariableDto variableInfo, DateTime startTime, DateTime endTime) {
		Map<Long, Double> groupedValues = variableInfo.getGroupedValues();
		DateTime groupedStartTime = groupDateTime(startTime, variableInfo.grouping);
		long groupingMillis = variableInfo.nonEmptyPeriod.getMillis(groupedStartTime.getMillis());
		long nextTime = groupedStartTime.getMillis() + groupingMillis;
		while (nextTime < startTime.getMillis()) {
			nextTime += variableInfo.grouping.getMillis(nextTime);
		}
		while (nextTime < endTime.getMillis()) {
			groupingMillis = variableInfo.grouping.getMillis(nextTime);
			if (!groupedValues.containsKey(nextTime)) {
				groupedValues.put(nextTime,
						variableInfo.filling.value * groupingMillis / variableInfo.nonEmptyPeriod.getMillis(nextTime));
			}
			nextTime += groupingMillis;
		}
	}

	/**
	 * Form variable values using values from remaining variable
	 * 
	 * @param variableInfo
	 */
	private void doRemainingGrouping(VariableDto variableInfo) {
		HashMap<DateTime, Double> values = new HashMap<DateTime, Double>();
		Map<DateTime, Integer> amountOfValues = new HashMap<DateTime, Integer>();
		for (VariableDto variableDto : variableInfo.getRemainingVariable()) {
			for (DateTime dateTime : variableDto.getValues().keySet()) {
				Double normilizedValue = normalizeUnit(variableDto.getValues().get(dateTime), variableDto.unitDto,
						variableInfo.unitDto);
				if (!values.containsKey(dateTime)) {
					values.put(dateTime, normilizedValue);
					continue;
				}
				Double value = values.get(dateTime);
				switch (variableInfo.remainingType) {
				case SUMMABLE:
					values.put(dateTime, value + normilizedValue);
					break;
				case AVERAGE:
					values.put(dateTime, value + normilizedValue);
					amountOfValues.put(dateTime,
							amountOfValues.containsKey(dateTime) ? amountOfValues.get(dateTime) + 1 : 2);
					break;
				}
			}
		}
		// calculate average values
		if (RemainingType.AVERAGE.equals(variableInfo.remainingType)) {
			for (Map.Entry<DateTime, Integer> entry : amountOfValues.entrySet()) {
				values.put(entry.getKey(), values.get(entry.getKey()) / (entry.getValue() + 0.0d));
			}
		}
		variableInfo.setValues(values);
	}

	/**
	 * Form variable values using values from prioritized variable
	 * 
	 * @param variableInfo
	 */
	private void doPrioritizedGrouping(VariableDto variableInfo) {
		HashMap<DateTime, Double> values = new HashMap<DateTime, Double>();
		for (VariableDto variableDto : variableInfo.getPrioritizedVariables()) {
			for (DateTime dateTime : variableDto.getValues().keySet()) {
				if (!values.containsKey(dateTime)) {
					Double normilizedValue = normalizeUnit(variableDto.getValues().get(dateTime), variableDto.unitDto,
							variableInfo.unitDto);
					values.put(dateTime, normilizedValue);
				}
			}
		}
		variableInfo.setValues(values);
	}

	private void doGrouping(VariableDto variableInfo) {
		Map<DateTime, Double> variableValues = variableInfo.getValues();
		if (LOG.isDebugEnabled()) {
			LOG.debug("Are going to get following values for " + variableInfo.id + " value with "
					+ variableInfo.grouping + " grouping, " + variableInfo.keepZeroes + " keep zeroes and "
					+ variableInfo.unitDto.useAverage + " use average. " + variableValues);
		}
		Map<Long, Double> groupedVariableValues = new HashMap<Long, Double>();
		Map<Long, Integer> summarableValuesCount = new HashMap<Long, Integer>();
		UnitDto shiftedUnit = unitsDao.findUnitById(variableInfo.timeShiftUnit);
		for (DateTime dateTime : variableValues.keySet()) {
			Double value = variableValues.get(dateTime);
			if (skipVariableValue(variableInfo, value)) {
				continue;
			}
			if (shiftedUnit != null) {
				dateTime = shiftTime(shiftedUnit, variableInfo.timeShift, dateTime);
			}
			Long groupedDateTime = groupDateTime(dateTime, variableInfo.grouping).getMillis();

			if (groupedVariableValues.containsKey(groupedDateTime)) {
				Double lastValue = groupedVariableValues.get(groupedDateTime);
				groupedVariableValues.put(groupedDateTime, lastValue + value);
			} else {
				groupedVariableValues.put(groupedDateTime, value);
			}
			// variables can be summable or average
			if (!variableInfo.unitDto.useAverage) {
				continue;
			}
			// if avarage than just accumulate info for future
			if (summarableValuesCount.containsKey(groupedDateTime)) {
				summarableValuesCount.put(groupedDateTime, summarableValuesCount.get(groupedDateTime) + 1);
			} else {
				summarableValuesCount.put(groupedDateTime, 1);
			}
		}

		processAggregation(groupedVariableValues, summarableValuesCount);

		if (LOG.isDebugEnabled()) {
			LOG.debug("Got following after groping operation " + groupedVariableValues);
		}

		variableInfo.setGroupedValues(groupedVariableValues);
	}

	private DateTime shiftTime(UnitDto shiftedUnit, Double timeShift, DateTime dateTime) {
		if (timeShift == 0.0d) {
			return dateTime;
		}
		// assume that si unit is second
		if (timeShift > 0) {
			return dateTime.plusSeconds((int) (shiftedUnit.siUnitsPerThisUnit * timeShift));
		} else {
			return dateTime.minusSeconds((int) (-shiftedUnit.siUnitsPerThisUnit * timeShift));
		}

	}

	private Double normalizeUnit(Double unitValue, UnitDto unitDto, UnitDto normalizationUnit) {
		return (unitValue * unitDto.siUnitsPerThisUnit) / normalizationUnit.siUnitsPerThisUnit;
	}

	private void processAggregation(Map<Long, Double> groupedVariableValues, Map<Long, Integer> summarableValuesCount) {
		for (Long dateTime : summarableValuesCount.keySet()) {
			Double value = groupedVariableValues.get(dateTime);
			groupedVariableValues.put(dateTime, value / summarableValuesCount.get(dateTime).doubleValue());
		}
	}

	// uses to skip some values, for ex. 0 values when keep_zeroes=false
	private boolean skipVariableValue(VariableDto variableInfo, Double val) {
		if (!variableInfo.keepZeroes && val == 0.0d) {
			return true;
		}
		if (variableInfo.minValue != null && val < variableInfo.minValue) {
			return true;
		}
		if (variableInfo.maxValue != null && val > variableInfo.maxValue) {
			return true;
		}
		return false;
	}

	private DateTime groupDateTime(DateTime dateTime, GroupingDto groping) {
		if (GroupingDto.MINUTELY.equals(groping)) {
			return new DateTime(dateTime.year().get(), dateTime.monthOfYear().get(), dateTime.dayOfMonth().get(),
					dateTime.hourOfDay().get(), dateTime.minuteOfHour().get(), 0, 0, dateTime.getZone());
		}
		if (GroupingDto.HOURLY.equals(groping)) {
			return new DateTime(dateTime.year().get(), dateTime.monthOfYear().get(), dateTime.dayOfMonth().get(),
					dateTime.hourOfDay().get(), 0, 0, 0, dateTime.getZone());
		}
		if (GroupingDto.DAILY.equals(groping)) {
			return new DateTime(dateTime.year().get(), dateTime.monthOfYear().get(), dateTime.dayOfMonth().get(), 0, 0,
					0, 0, dateTime.getZone());
		}
		if (GroupingDto.WEEKLY.equals(groping)) {
			DateTime dateTime2 = new DateTime(dateTime.year().get(), dateTime.monthOfYear().get(), dateTime
					.dayOfMonth().get(), 0, 0, 0, 0, dateTime.getZone());
			return dateTime2.withDayOfWeek(1);
		}
		if (GroupingDto.MONTHLY.equals(groping)) {
			return new DateTime(dateTime.year().get(), dateTime.monthOfYear().get(), 1, 0, 0, 0, 0, dateTime.getZone());
		}
		throw new RuntimeException("Please, specify correct Grouping");
	}

	@Override
	public List<GroupingDto> getVariableNonEmptyMeasures() {
		return variableNonEmptyMeasures;
	}

}
