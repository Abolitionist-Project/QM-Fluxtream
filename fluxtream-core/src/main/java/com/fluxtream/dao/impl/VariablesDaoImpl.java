package com.fluxtream.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fluxtream.Configuration;
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
import com.fluxtream.utils.JPAUtils;

@Component
public class VariablesDaoImpl implements VariablesDao {

	private static final String GET_APPS = "SELECT qmv.source_application as appId, qma.name as appName from qm_applications qma "
			+ "join qm_variables qmv on (qma.id=qmv.source_application)"
			+ "where qmv.input_variable<>:exceptVar order by appName";

	/*
	 * XXX see https://github.com/mikepsinn/Quantimodo/issues/197
	 */
//	private static final String GET_VARS_IN_CATEGORY = "SELECT qv.id, qv.name, qv.data_owner, qv.type, qa.name as appName  from qm_variables qv "
//			+ " left join qm_applications qa on qa.id=qv.source_application "
//			+ "where qv.input_variable<>:exceptVarType and qv.variable_category=:categoryId and qv.type=:type and qv.data_owner in (:dataOwners) "
//			+ "order by qv.name";
	private static final String GET_VARS_IN_CATEGORY = "SELECT qv.id, qv.name, qv.data_owner, qv.type, qa.name as appName"
			+ " FROM qm_variables qv LEFT JOIN qm_applications qa ON qa.id=qv.source_application"
			+ " WHERE qv.input_variable<>:exceptVarType"
			+ " AND qv.variable_category=:categoryId"
			+ " AND qv.type=:type"
			+ " ORDER BY qv.name";

	private static final String GET_ALL_VARS_FOR_USER = "SELECT qv.id, qv.name, qv.data_owner, qv.type, qa.name as appName, "
			+ "qu.id as unitId, qu.name as unitName, qu.si_unit as siUnit "
			+ "from qm_variables qv "
			+ "left join qm_applications qa on qa.id=qv.source_application "
			+ "left join qm_units qu on qu.id=qv.unit_used "
			+ "where qv.input_variable<>:exceptVarType and qv.type=:type and qv.data_owner in (:dataOwners) order by appName, qv.name";

	private static final String GET_ALL_VARS_BY_SI_UNIT_FOR_USER = "SELECT qv.id, qv.name, qv.data_owner, qv.type, qa.name as appName, "
			+ "qu.id as unitId, qu.name as unitName, qu.si_unit as siUnit "
			+ "from qm_variables qv "
			+ "left join qm_applications qa on qa.id=qv.source_application "
			+ "left join qm_units qu on qu.id=qv.unit_used "
			+ "where qv.input_variable<>:exceptVarType and qv.type=:type and qv.data_owner in (:dataOwners) and qu.si_unit=:siUnit "
			+ "order by appName, qv.name";

	private static final String GET_VAR_VALS = "select start_time_utc, value from qm_qs_data "
			+ "where data_owner=:dataOwner and variable=:varId and start_time_utc between :startTime and :endTime order by start_time_utc";

	private static final String GET_VAR_BY_ID = "select qv.id, qv.data_owner, qv.type, qv.variable_category, qv.source_application, qa.name as appName, "
			+ "qv.unit_used, qu.si_units_per_this_unit as siUnitsPerUnit, qu.si_unit as siUnit, qu.name as unitName, qu.use_average as useAverage, qv.name, qv.keep_zeroes, "
			+ "qv.min_value, qv.max_value, qv.time_shift_unit, qv.time_shift , qv.non_empty_period, qv.filling_type, "
			+ "qv.filling_value, qv.remaining_type, qvc.name as categoryName "
			+ "from qm_variables qv "
			+ "left join qm_applications qa on (qa.id=qv.source_application) "
			+ "join qm_units qu on (qu.id=qv.unit_used) "
			+ "left join qm_variable_categories qvc on (qvc.id=qv.variable_category) where qv.id=:varId";

	private static final String GET_VAR_BY_APP_AND_NAME = "select qv.id, qv.data_owner, qv.type, qv.variable_category, qv.source_application, "
			+ "qa.name as appName, qv.unit_used, qu.si_units_per_this_unit as siUnitsPerUnit, qu.si_unit as siUnit, qu.name as unitName, qu.use_average as useAverage, qv.name, "
			+ "qv.keep_zeroes, qv.min_value, qv.max_value, qv.time_shift_unit, qv.time_shift , qv.non_empty_period, "
			+ "qv.filling_type, qv.filling_value, qv.remaining_type, qvc.name as categoryName "
			+ "from qm_variables qv "
			+ "left join qm_applications qa on (qa.id=qv.source_application) "
			+ "join qm_units qu on (qu.id=qv.unit_used) "
			+ "left join qm_variable_categories qvc on (qvc.id=qv.variable_category) where qv.data_owner=:dataOwner AND qv.name=:varName AND qa.name=:appName";

	private static final String GET_REMAINING_VARIABLES = "select qv.id, qv.data_owner, qv.type, qv.variable_category, qv.source_application, qa.name as appName, "
			+ "qv.unit_used, qu.si_units_per_this_unit as siUnitsPerUnit, qu.si_unit as siUnit, qu.name as unitName, qu.use_average as useAverage, "
			+ "qv.name, qv.keep_zeroes, qv.min_value, qv.max_value, "
			+ " qv.time_shift_unit, qv.time_shift, qv.non_empty_period, qv.filling_type, qv.filling_value, qv.remaining_type, qvc.name as categoryName "
			+ "from qm_variable_remaining qvr "
			+ "join qm_variables qv on qv.id=qvr.variable_id "
			+ "left join qm_applications qa on (qa.id=qv.source_application) "
			+ "join qm_units qu on (qu.id=qv.unit_used) "
			+ "left join qm_variable_categories qvc on (qvc.id=qv.variable_category) where qvr.aggregated_variable_id=:aggregatedVariableId";

	private static final String GET_PRIORITIZED_VARIABLES = "select qvp.priority, qv.id, qv.data_owner, qv.type, qv.variable_category, qv.source_application, qa.name as appName, "
			+ "qv.unit_used, qu.si_units_per_this_unit as siUnitsPerUnit, qu.si_unit as siUnit, qu.name as unitName, qu.use_average as useAverage, qv.name, qv.keep_zeroes, qv.min_value, qv.max_value, "
			+ " qv.time_shift_unit, qv.time_shift , qv.non_empty_period, qv.filling_type, qv.filling_value, qv.remaining_type, qvc.name as categoryName "
			+ "from qm_variable_prioritized qvp "
			+ "join qm_variables qv on qv.id=qvp.variable_id "
			+ "left join qm_applications qa on (qa.id=qv.source_application) "
			+ "join qm_units qu on (qu.id=qv.unit_used) "
			+ "left join qm_variable_categories qvc on (qvc.id=qv.variable_category) "
			+ "where qvp.aggregated_variable_id=:aggregatedVariableId order by qvp.priority";

	private final static String SAVE_VARIABLE = "insert into qm_variables set data_owner=:dataOwner, name=:name, variable_category=:categoryId, "
			+ "keep_zeroes=:keepZeroes, unit_used=:unit, max_value=:maxValue, min_value=:minValue, time_shift=:timeShift, "
			+ "time_shift_unit=:timeShiftUnit, non_empty_period=:nonEmptyPeriod, filling_type=:fillingType, filling_value=:fillingValue, "
			+ "type=:type, remaining_type=:remainingType";

	private final static String UPDATE_VARIABLE = "update qm_variables set name=:name, variable_category=:categoryId, "
			+ "keep_zeroes=:keepZeroes, unit_used=:unit, max_value=:maxValue, min_value=:minValue, time_shift=:timeShift, "
			+ "time_shift_unit=:timeShiftUnit, non_empty_period=:nonEmptyPeriod, filling_type=:fillingType, filling_value=:fillingValue, remaining_type=:remainingType "
			+ "where id=:id and data_owner=:dataOwner";

	private final static String REMOVE_VARIABLE = "delete from qm_variables where id=:id and data_owner=:dataOwner";

	private final static String REMOVE_REMAINING_VARIABLE = "delete from qm_variable_remaining where "
			+ "aggregated_variable_id=:aggregatedVariableId";

	private final static String UPDATE_REMAINING_VARIABLE = "insert into qm_variable_remaining set "
			+ "aggregated_variable_id=:aggregatedVariableId, variable_id=:variableId";

	private final static String REMOVE_PRIORITIZED_VARIABLE = "delete from qm_variable_prioritized where "
			+ "aggregated_variable_id=:aggregatedVariableId";

	private final static String UPDATE_PRIORITIZED_VARIABLE = "insert into qm_variable_prioritized set "
			+ "aggregated_variable_id=:aggregatedVariableId, variable_id=:variableId, priority=:priority";

	@PersistenceContext
	private EntityManager em;

	@Autowired
	Configuration configuration;

	private final NamedParameterJdbcTemplate jdbcTemplate;

	@Autowired(required = true)
	public VariablesDaoImpl(DataSource dataSource) {
		this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	}

	@Override
	public List<VariableDto> getVariables(Guest guest, VariableBehaviour behaviour, final Integer categoryId,
			VariableType type) {

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("exceptVarType", behaviour.exceptValue);
		params.put("categoryId", categoryId);
		params.put("type", type.value);
		List<Long> dataOwners = new ArrayList<Long>();
		/*
		 * XXX guest is not longer required, see https://github.com/mikepsinn/Quantimodo/issues/197
		 */
//		dataOwners.add(guest.getId());
		final Long globalDataOwner = configuration.getLong(Configuration.GLOBAL_DATA_OWNER);
//		dataOwners.add(globalDataOwner);

		params.put("dataOwners", dataOwners);
		return jdbcTemplate.query(GET_VARS_IN_CATEGORY, params, new RowMapper<VariableDto>() {

			@Override
			public VariableDto mapRow(ResultSet rs, int rowNum) throws SQLException {
				VariableDto variableDto = new VariableDto();
				variableDto.id = rs.getInt("id");
				variableDto.name = rs.getString("name");
				variableDto.application = rs.getString("appName");
				variableDto.dataOwner = rs.getLong("data_owner");
				variableDto.categoryId = categoryId;
				variableDto.type = VariableType.resolveTypeByValue(rs.getInt("type"));
				variableDto.global = globalDataOwner.equals(variableDto.dataOwner);
				return variableDto;
			}
		});
	}

	@Override
	public List<VariableDto> getVariablesForGuest(Guest guest, VariableBehaviour behaviour, VariableType type) {

		final Long globalDataOwner = configuration.getLong(Configuration.GLOBAL_DATA_OWNER);

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("exceptVarType", behaviour.exceptValue);
		params.put("type", type.value);

		List<Long> dataOwners = new ArrayList<Long>();
		dataOwners.add(guest.getId());
		// TODO: remove it later. it's just temporary workaround
		if (VariableType.SINGLE.equals(type)) {
			dataOwners.add(globalDataOwner);
		}

		params.put("dataOwners", dataOwners);

		return jdbcTemplate.query(GET_ALL_VARS_FOR_USER, params, new RowMapper<VariableDto>() {

			@Override
			public VariableDto mapRow(ResultSet rs, int rowNum) throws SQLException {
				VariableDto variableDto = new VariableDto();
				variableDto.id = rs.getInt("id");
				variableDto.name = rs.getString("name");
				variableDto.application = rs.getString("appName");
				variableDto.dataOwner = rs.getLong("data_owner");
				variableDto.type = VariableType.resolveTypeByValue(rs.getInt("type"));
				variableDto.global = globalDataOwner.equals(variableDto.dataOwner);
				UnitDto unitDto = new UnitDto();
				unitDto.id = rs.getInt("unitId");
				unitDto.siUnit = rs.getInt("siUnit");
				unitDto.name = rs.getString("unitName");
				variableDto.unitDto = unitDto;
				return variableDto;
			}
		});
	}

	@Override
	public List<VariableDto> getVariablesBySiUnitForGuest(Guest guest, VariableBehaviour behaviour, VariableType type,
			Integer siUnit) {

		final Long globalDataOwner = configuration.getLong(Configuration.GLOBAL_DATA_OWNER);

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("exceptVarType", behaviour.exceptValue);
		params.put("type", type.value);
		params.put("siUnit", siUnit);

		List<Long> dataOwners = new ArrayList<Long>();
		dataOwners.add(guest.getId());
		// TODO: remove it later. it's just temporary workaround
		if (VariableType.SINGLE.equals(type)) {
			dataOwners.add(globalDataOwner);
		}

		params.put("dataOwners", dataOwners);

		return jdbcTemplate.query(GET_ALL_VARS_BY_SI_UNIT_FOR_USER, params, new RowMapper<VariableDto>() {

			@Override
			public VariableDto mapRow(ResultSet rs, int rowNum) throws SQLException {
				VariableDto variableDto = new VariableDto();
				variableDto.id = rs.getInt("id");
				variableDto.name = rs.getString("name");
				variableDto.application = rs.getString("appName");
				variableDto.dataOwner = rs.getLong("data_owner");
				variableDto.type = VariableType.resolveTypeByValue(rs.getInt("type"));
				variableDto.global = globalDataOwner.equals(variableDto.dataOwner);
				UnitDto unitDto = new UnitDto();
				unitDto.id = rs.getInt("unitId");
				unitDto.siUnit = rs.getInt("siUnit");
				unitDto.name = rs.getString("unitName");
				variableDto.unitDto = unitDto;
				return variableDto;
			}
		});
	}

	@Override
	public Map<Integer, String> getApplications(VariableBehaviour variableType) {

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("exceptVar", variableType.exceptValue);
		List<Map<String, Object>> apps = jdbcTemplate.queryForList(GET_APPS, params);

		Map<Integer, String> result = new HashMap<Integer, String>();
		for (Map<String, Object> variable : apps) {
			result.put(Integer.valueOf(variable.get("appId") + ""), variable.get("appName").toString());
		}
		return result;
	}

	@Override
	public Map<DateTime, Double> getVariableValues(Guest guest, Integer varId, DateTime startTime, DateTime endTime,
			DateTimeZone dateTimeZone) {

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("dataOwner", guest.getId());
		params.put("varId", varId);
		params.put("startTime", startTime.toString());
		params.put("endTime", endTime.toString());
		List<Map<String, Object>> varVals = jdbcTemplate.queryForList(GET_VAR_VALS, params);

		Map<DateTime, Double> result = new HashMap<DateTime, Double>();
		for (Map<String, Object> variable : varVals) {
			result.put(new DateTime(variable.get("start_time_utc"), dateTimeZone),
					Double.valueOf(variable.get("value").toString()));
		}
		return result;
	}

	@Override
	public VariableDto findVariable(Guest guest, final Integer varId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("varId", varId);

		final Long globalDataOwner = configuration.getLong(Configuration.GLOBAL_DATA_OWNER);

		VariableDto variableDto = jdbcTemplate.query(GET_VAR_BY_ID, params, new ResultSetExtractor<VariableDto>() {

			@Override
			public VariableDto extractData(ResultSet rs) throws SQLException, DataAccessException {
				if (!rs.next()) {
					return null;
				}
				return extractVariableDto(globalDataOwner, rs);
			}
		});

		fillUpByRemainingVariables(globalDataOwner, variableDto);
		fillUpByPrioritizedVariables(globalDataOwner, variableDto);

		return variableDto;
	}

	@Override
	public VariableDto findVariable(Guest guest, String applicationName, String variableName) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("dataOwner", guest == null ? 0 : guest.getId());
		params.put("appName", applicationName);
		params.put("varName", variableName);

		final Long globalDataOwner = configuration.getLong(Configuration.GLOBAL_DATA_OWNER);

		VariableDto variableDto = jdbcTemplate.query(GET_VAR_BY_APP_AND_NAME, params,
				new ResultSetExtractor<VariableDto>() {

					@Override
					public VariableDto extractData(ResultSet rs) throws SQLException, DataAccessException {
						if (!rs.next()) {
							return null;
						}
						return extractVariableDto(globalDataOwner, rs);
					}
				});

		fillUpByRemainingVariables(globalDataOwner, variableDto);
		fillUpByPrioritizedVariables(globalDataOwner, variableDto);

		return variableDto;
	}

	@Override
	public void deleteVariable(Guest guest, Integer varId) {

		removePrioritizedVariables(varId);
		removeRemainingVariables(varId);

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("id", varId);
		params.put("dataOwner", guest.getId());

		jdbcTemplate.update(REMOVE_VARIABLE, params);
	}

	private VariableDto extractVariableDto(final Long globalDataOwner, ResultSet rs) throws SQLException {
		VariableDto variableDto = new VariableDto();
		variableDto.id = rs.getInt("id");
		variableDto.name = rs.getString("name");
		variableDto.applicationId = rs.getObject("source_application") == null ? null : rs.getInt("source_application");
		variableDto.application = rs.getObject("source_application") == null ? null : rs.getString("appName");
		variableDto.dataOwner = rs.getLong("data_owner");
		variableDto.categoryId = rs.getInt("variable_category");
		variableDto.categoryName = rs.getString("categoryName");

		UnitDto unitDto = new UnitDto();
		unitDto.id = rs.getInt("unit_used");
		unitDto.siUnitsPerThisUnit = rs.getDouble("siUnitsPerUnit");
		unitDto.siUnit = rs.getInt("siUnit");
		unitDto.name = rs.getString("unitName");
		unitDto.useAverage = rs.getBoolean("useAverage");
		variableDto.unitDto = unitDto;

		variableDto.keepZeroes = rs.getBoolean("keep_zeroes");
		variableDto.minValue = rs.getObject("min_value") == null ? null : rs.getDouble("min_value");
		variableDto.maxValue = rs.getObject("max_value") == null ? null : rs.getDouble("max_value");
		variableDto.timeShift = rs.getObject("time_shift") == null ? null : rs.getDouble("time_shift");
		variableDto.timeShiftUnit = rs.getInt("time_shift_unit");
		variableDto.nonEmptyPeriod = rs.getObject("non_empty_period") == null ? null : GroupingDto.resolveByValue(rs
				.getInt("non_empty_period"));
		FillingDto fillingTypeDto = new FillingDto();
		fillingTypeDto.type = rs.getObject("filling_type") == null ? null : FillingType.resolveByValue(rs
				.getInt("filling_type"));
		fillingTypeDto.value = rs.getObject("filling_value") == null ? null : rs.getDouble("filling_value");
		variableDto.filling = fillingTypeDto;
		variableDto.global = globalDataOwner.equals(variableDto.dataOwner);
		variableDto.type = VariableType.resolveTypeByValue(rs.getInt("type"));
		variableDto.remainingType = rs.getObject("remaining_type") == null ? null : RemainingType.resolveByValue(rs
				.getInt("remaining_type"));
		return variableDto;
	}

	private void fillUpByRemainingVariables(final Long globalDataOwner, final VariableDto variableDto) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("aggregatedVariableId", variableDto.id);
		jdbcTemplate.query(GET_REMAINING_VARIABLES, paramMap, new ResultSetExtractor<VariableDto>() {

			@Override
			public VariableDto extractData(ResultSet rs) throws SQLException, DataAccessException {
				List<VariableDto> variableDtos = new ArrayList<VariableDto>();
				while (rs.next()) {
					variableDtos.add(extractVariableDto(globalDataOwner, rs));
				}
				variableDto.setRemainingVariables(variableDtos);
				return variableDto;
			}
		});
	}

	private void fillUpByPrioritizedVariables(final Long globalDataOwner, final VariableDto variableDto) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("aggregatedVariableId", variableDto.id);
		jdbcTemplate.query(GET_PRIORITIZED_VARIABLES, paramMap, new ResultSetExtractor<VariableDto>() {

			@Override
			public VariableDto extractData(ResultSet rs) throws SQLException, DataAccessException {
				List<VariableDto> variableDtos = new ArrayList<VariableDto>();
				while (rs.next()) {
					VariableDto extractVariableDto = extractVariableDto(globalDataOwner, rs);
					extractVariableDto.priority = rs.getInt("priority");
					variableDtos.add(extractVariableDto);
				}
				variableDto.setPrioritizedVariables(variableDtos);
				return variableDto;
			}
		});
	}

	@Override
	public VariableSettings findVariableSettings(long guestId) {
		return JPAUtils.findUnique(em, VariableSettings.class, "variableSettings.byGuestId", guestId);
	}

	@Override
	@Transactional
	public void saveVariablesSettings(long guestId, VariableType inputType, Integer inputId, VariableType outputType,
			Integer outputId, GroupingDto grouping/*, DateTime startDateTime, DateTime endDateTime*/) {
		VariableSettings variableSettings = findVariableSettings(guestId);
		if (variableSettings == null) {
			variableSettings = new VariableSettings();
			variableSettings.guestId = guestId;
		}
		variableSettings.inputType = inputType;
		variableSettings.inputId = inputId;
		variableSettings.outputType = outputType;
		variableSettings.outputId = outputId;
		//variableSettings.startTime = startDateTime.getMillis();
		//variableSettings.endTime = endDateTime.getMillis();
		variableSettings.groupingValue = grouping.value;
		em.persist(variableSettings);
	}

	@Override
	@Transactional
	public Integer saveVariable(Guest guest, VariableDto variableDto) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		Map<String, Object> params = new HashMap<String, Object>();
		fillUpSaveVariableParameters(guest, variableDto, params);

		SqlParameterSource parameterSource = new MapSqlParameterSource(params);

		jdbcTemplate.update(SAVE_VARIABLE, parameterSource, keyHolder);

		int id = ((Long) keyHolder.getKey()).intValue();
		updateRemainingVariables(id, variableDto.getRemainingVariable());
		updatePrioritizedVariables(id, variableDto.getPrioritizedVariables());

		return id;
	}

	private void fillUpSaveVariableParameters(Guest guest, VariableDto variableDto, Map<String, Object> params) {
		params.put("dataOwner", guest.getId());
		params.put("name", variableDto.name);
		params.put("keepZeroes", variableDto.keepZeroes);
		params.put("categoryId", variableDto.categoryId);
		params.put("unit", variableDto.unitDto.id);
		params.put("maxValue", variableDto.maxValue);
		params.put("minValue", variableDto.minValue);
		params.put("timeShift", variableDto.timeShift);
		params.put("timeShiftUnit", variableDto.timeShiftUnit);
		params.put("nonEmptyPeriod", variableDto.nonEmptyPeriod == null ? null : variableDto.nonEmptyPeriod.value);
		params.put("fillingType", variableDto.filling == null ? 0 : variableDto.filling.type.value);
		params.put("fillingValue", variableDto.filling == null ? null : variableDto.filling.value);
		params.put("type", variableDto.type.value);
		params.put("remainingType", variableDto.remainingType.value);
	}

	@Override
	@Transactional
	public void updateVariable(Guest guest, VariableDto variableDto) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("id", variableDto.id);
		fillUpSaveVariableParameters(guest, variableDto, params);

		SqlParameterSource parameterSource = new MapSqlParameterSource(params);

		jdbcTemplate.update(UPDATE_VARIABLE, parameterSource, keyHolder);

		updateRemainingVariables(variableDto.id, variableDto.getRemainingVariable());
		updatePrioritizedVariables(variableDto.id, variableDto.getPrioritizedVariables());
	}

	/**
	 * Remove existed remaining variables for this aggregated variable and add
	 * new ones
	 * 
	 * @param id
	 * @param remainingVariableIds
	 */
	private void updateRemainingVariables(Integer id, List<VariableDto> remainingVariableIds) {
		removeRemainingVariables(id);

		if (remainingVariableIds == null) {
			return;
		}

		Map[] batchMap = new HashMap[remainingVariableIds.size()];
		int i = 0;
		for (VariableDto variableDto : remainingVariableIds) {
			HashMap<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("aggregatedVariableId", id);
			parameters.put("variableId", variableDto.id);
			batchMap[i++] = parameters;
		}

		jdbcTemplate.batchUpdate(UPDATE_REMAINING_VARIABLE, batchMap);
	}

	private void removeRemainingVariables(Integer id) {
		HashMap<String, Object> removeRemainingVariablesParams = new HashMap<String, Object>();
		removeRemainingVariablesParams.put("aggregatedVariableId", id);
		jdbcTemplate.update(REMOVE_REMAINING_VARIABLE, removeRemainingVariablesParams);
	}

	/**
	 * Remove existed remaining variables for this aggregated variable and add
	 * new ones
	 * 
	 * @param id
	 * @param remainingVariableIds
	 */
	private void updatePrioritizedVariables(Integer id, List<VariableDto> prioritizedVariableIds) {
		removePrioritizedVariables(id);

		if (prioritizedVariableIds == null) {
			return;
		}

		Map[] batchMap = new HashMap[prioritizedVariableIds.size()];
		int i = 0;
		for (VariableDto variableDto : prioritizedVariableIds) {
			HashMap<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("aggregatedVariableId", id);
			parameters.put("variableId", variableDto.id);
			parameters.put("priority", variableDto.priority);
			batchMap[i++] = parameters;
		}

		jdbcTemplate.batchUpdate(UPDATE_PRIORITIZED_VARIABLE, batchMap);
	}

	private void removePrioritizedVariables(Integer id) {
		HashMap<String, Object> removeRemainingVariablesParams = new HashMap<String, Object>();
		removeRemainingVariablesParams.put("aggregatedVariableId", id);
		jdbcTemplate.update(REMOVE_PRIORITIZED_VARIABLE, removeRemainingVariablesParams);
	}

}
