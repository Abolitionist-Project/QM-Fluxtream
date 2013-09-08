package com.fluxtream.dao.impl;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import javax.sql.DataSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.fluxtream.dao.DataDao;
import com.fluxtream.dto.DataDto;
import com.fluxtream.domain.Guest;
import com.fluxtream.dto.VariableDto;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import org.joda.time.DateTime;

@Component
public class DataDaoImpl implements DataDao {
	private final NamedParameterJdbcTemplate jdbcTemplate;
	@Autowired(required = true)
	public DataDaoImpl(final DataSource dataSource) {
		this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	}

	// Create/update
	@Override
	public boolean insert(final Guest guest, final int variableId, final double value, final Date startTime, final int duration) {
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("data_owner", guest.getId());
		params.put("variable", variableId);
		params.put("value", value);
		params.put("start_time_utc", startTime);
		params.put("duration_in_seconds", duration);

		return 1 == jdbcTemplate.update("insert into qm_qs_data (data_owner, variable, value, start_time_utc, duration_in_seconds) " +
                                    "value (:data_owner, :variable, :value, :start_time_utc, :duration_in_seconds) " +
                                    "on duplicate key update value=values(value), duration_in_seconds=values(duration_in_seconds)", params); 
	}

	// Read
	private static final DataDto convertMap(final Map<String, Object> data) {
		final DataDto result = new DataDto();
		result.id = (Long) data.get("id");
		result.guestId = (Long) data.get("data_owner");
		result.variableId = (Integer) data.get("variable");
		result.value = (Float) data.get("value");
		result.startTime = new DateTime(((Date) data.get("start_time_utc")).getTime());
		result.duration = (Integer) data.get("duration_in_seconds");
		return result;
	}

	@Override
	public DataDto get(final long id) {
		return convertMap(jdbcTemplate.queryForMap("select * from qm_qs_data where id=:id", Collections.singletonMap("id", id)));
	}

	@Override
	public List<DataDto> getAll(final Guest guest) {
		final List<Map<String, Object>> dataRows = jdbcTemplate.queryForList("select * from qm_qs_data where data_owner=:guest_id",
		                                                                 Collections.singletonMap("guest_id", guest.getId()));

		final List<DataDto> results = new ArrayList<DataDto>(dataRows.size());
		for (final Map<String, Object> dataRow : dataRows) {
			results.add(convertMap(dataRow));
		}
		return results;
	}

	@Override
	public List<DataDto> getByVariable(final Guest guest, final int variableId) {
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("guest_id", guest.getId());
		params.put("variable_id", variableId);

		final List<Map<String, Object>> dataRows = jdbcTemplate.queryForList("select * from qm_qs_data where data_owner=:guest_id AND variable=:variable_id", params);

		final List<DataDto> results = new ArrayList<DataDto>(dataRows.size());
		for (final Map<String, Object> dataRow : dataRows) {
			results.add(convertMap(dataRow));
		}
		return results;
	}

	// Delete
	@Override
	public boolean delete(final long id) {
		return 1 == jdbcTemplate.update("delete from qm_qs_data where id=:id", Collections.singletonMap("id", id));
	}

	@Override
	public int deleteAll(final Guest guest) {
		return jdbcTemplate.update("delete from qm_qs_data where data_owner=:guest_id", Collections.singletonMap("guest_id", guest.getId()));
	}

	@Override
	public int deleteByVariable(final Guest guest, final int variableId) {
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("guest_id", guest.getId());
		params.put("variable_id", variableId);

		return jdbcTemplate.update("delete from qm_qs_data where data_owner=:guest_id AND variable=:variable_id", params);
	}
}

