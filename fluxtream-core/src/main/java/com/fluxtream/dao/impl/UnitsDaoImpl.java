package com.fluxtream.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.fluxtream.dao.UnitsDao;
import com.fluxtream.dto.UnitDto;

@Component
public class UnitsDaoImpl implements UnitsDao {

	private static final String GET_UNIT = "select * from qm_units where id=:id";

	private static final String GET_ALL_UNITS = "select * from qm_units order by name";

	private static final String GET_UNITS_BY_SI_UNIT = "select * from qm_units where si_unit=:siUnit order by name";

	private final NamedParameterJdbcTemplate jdbcTemplate;

	@Autowired(required = true)
	public UnitsDaoImpl(DataSource dataSource) {
		this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List<UnitDto> getUnits() {
		return jdbcTemplate.query(GET_ALL_UNITS, new HashMap(), new RowMapper<UnitDto>() {

			@Override
			public UnitDto mapRow(ResultSet rs, int rowNum) throws SQLException {
				UnitDto unitDto = new UnitDto();
				unitDto.id = rs.getInt("id");
				unitDto.siUnit = rs.getInt("si_unit");
				unitDto.siUnitsPerThisUnit = rs.getDouble("si_units_per_this_unit");
				unitDto.name = rs.getString("name");
				return unitDto;
			}
		});
	}

	@Override
	public List<UnitDto> getUnits(int siUnit) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("siUnit", siUnit);
		return jdbcTemplate.query(GET_UNITS_BY_SI_UNIT, params, new RowMapper<UnitDto>() {
			@Override
			public UnitDto mapRow(ResultSet rs, int rowNum) throws SQLException {
				UnitDto unitDto = new UnitDto();
				unitDto.id = rs.getInt("id");
				unitDto.siUnit = rs.getInt("si_unit");
				unitDto.siUnitsPerThisUnit = rs.getDouble("si_units_per_this_unit");
				unitDto.name = rs.getString("name");
				return unitDto;
			}
		});
	}

	@Override
	public UnitDto findUnitById(Integer unitId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("id", unitId);
		return jdbcTemplate.query(GET_UNIT, params, new ResultSetExtractor<UnitDto>() {

			@Override
			public UnitDto extractData(ResultSet rs) throws SQLException, DataAccessException {
				if (!rs.next()) {
					return null;
				}
				UnitDto unitDto = new UnitDto();
				unitDto.id = rs.getInt("id");
				unitDto.siUnit = rs.getInt("si_unit");
				unitDto.siUnitsPerThisUnit = rs.getDouble("si_units_per_this_unit");
				unitDto.name = rs.getString("name");
				unitDto.useAverage = rs.getBoolean("use_average");
				return unitDto;
			}
		});
	}
}
