package com.fluxtream.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

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
import com.fluxtream.dao.CategoriesDao;
import com.fluxtream.domain.Guest;
import com.fluxtream.dto.CategoryDto;
import com.fluxtream.dto.VariableDto;

@Component
public class CategoriesDaoImpl implements CategoriesDao {

	private final static String SAVE_CATEGORY = "insert into qm_variable_categories set name=:name, data_owner=:dataOwner";

	private final static String UPDATE_CATEGORY = "update qm_variable_categories set name=:name where id=:categoryId and data_owner=:dataOwner";

	/*
	 * see https://github.com/mikepsinn/Quantimodo/issues/197
	 */
//	private final static String GET_CATEGORIES = "select id, name, data_owner from qm_variable_categories where data_owner in (:dataOwners)";
	private final static String GET_CATEGORIES = "select id, name, data_owner" +
			" from qm_variable_categories c" +
			" where (select count(*) from qm_variables where variable_category = c.id) > 0";

	private final static String GET_CATEGORY = "select qvc.id, qvc.name, qvc.data_owner, qv.id as varId, qv.name as varName, qv.data_owner as varDataOwner, "
			+ "qa.name as appName "
			+ "from qm_variable_categories qvc "
			+ "left join qm_variables qv on qv.variable_category=qvc.id "
			+ "left join qm_applications qa on qa.id=qv.source_application "
			+ "where qvc.id=:categoryId and qvc.data_owner in (:dataOwners) order by appName, varName";

	private final static String UNCATEGORIZE_CATEGORY_VARIABLES = "update qm_variables set variable_category=:unknownCategory "
			+ "where data_owner=:dataOwner and variable_category=:categoryId";

	private final static String UPDATE_CATEGORY_VARIABLES = "update qm_variables set "
			+ "variable_category=:categoryId where id=:variableId and data_owner=:dataOwner";

	private final NamedParameterJdbcTemplate jdbcTemplate;

	@Autowired
	Configuration configuration;

	@Autowired(required = true)
	public CategoriesDaoImpl(DataSource dataSource) {
		this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	}

	@Transactional(readOnly = false)
	@Override
	public Integer saveCategory(Guest guest, CategoryDto categoryDto) throws DataAccessException {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("name", categoryDto.name);
		params.put("dataOwner", guest.getId());
		SqlParameterSource parameterSource = new MapSqlParameterSource(params);

		jdbcTemplate.update(SAVE_CATEGORY, parameterSource, keyHolder);
		int categoryId = ((Long) keyHolder.getKey()).intValue();
		categoryDto.id = categoryId;
		updateCategoryVariables(guest.getId(), categoryDto);
		return categoryId;
	}

	private void updateCategoryVariables(Long guestId, CategoryDto categoryDto) {
		HashMap<String, Object> uncategorizeParameters = new HashMap<String, Object>();
		Integer categoryId = categoryDto.id;
		uncategorizeParameters.put("categoryId", categoryId);
		uncategorizeParameters.put("dataOwner", guestId);
		String unknownCategory = configuration.get("database.defaults.unknown_category_id");
		uncategorizeParameters.put("unknownCategory", unknownCategory);
		jdbcTemplate.update(UNCATEGORIZE_CATEGORY_VARIABLES, uncategorizeParameters);

		List<? extends VariableDto> variables = categoryDto.getVariables();
		if (variables == null) {
			return;
		}

		Map[] batchMap = new HashMap[variables.size()];
		int i = 0;
		for (VariableDto variableDto : variables) {
			HashMap<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("categoryId", categoryId);
			parameters.put("variableId", variableDto.id);
			parameters.put("dataOwner", guestId);
			batchMap[i++] = parameters;
		}

		jdbcTemplate.batchUpdate(UPDATE_CATEGORY_VARIABLES, batchMap);
	}

	@Transactional(readOnly = false)
	@Override
	public Integer updateCategory(Guest guest, CategoryDto categoryDto) throws DataAccessException {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("name", categoryDto.name);
		params.put("dataOwner", guest.getId());
		params.put("categoryId", categoryDto.id);
		SqlParameterSource parameterSource = new MapSqlParameterSource(params);

		jdbcTemplate.update(UPDATE_CATEGORY, parameterSource, keyHolder);

		updateCategoryVariables(guest.getId(), categoryDto);
		return categoryDto.id;
	}

	@Transactional(readOnly = true)
	@Override
	public List<CategoryDto> getCategories(Guest guest) {
		final Long globalDataOwner = configuration.getLong(Configuration.GLOBAL_DATA_OWNER);

		Map<String, Object> params = new HashMap<String, Object>();
		List<Long> dataOwners = new ArrayList<Long>();
		/*
		 * see https://github.com/mikepsinn/Quantimodo/issues/197
		 */
//		dataOwners.add(guest.getId());
//		/*
//		 * will only show the categories that the user has values for their variables
//		 */
//		dataOwners.add(globalDataOwner);
		params.put("dataOwners", dataOwners);

		return jdbcTemplate.query(GET_CATEGORIES, params, new RowMapper<CategoryDto>() {

			@Override
			public CategoryDto mapRow(ResultSet rs, int rowNum) throws SQLException {
				CategoryDto categoryDto = new CategoryDto();
				categoryDto.id = rs.getInt("id");
				categoryDto.name = rs.getString("name");
				categoryDto.dataOwner = rs.getLong("data_owner");
				categoryDto.global = globalDataOwner.equals(categoryDto.dataOwner);
				return categoryDto;
			}
		});
	}

	@Override
	public CategoryDto findCategory(Guest guest, Integer categoryId) {
		final Long globalDataOwner = configuration.getLong(Configuration.GLOBAL_DATA_OWNER);

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("categoryId", categoryId);
		List<Long> dataOwners = new ArrayList<Long>();
		dataOwners.add(guest.getId());
		dataOwners.add(globalDataOwner);
		params.put("dataOwners", dataOwners);

		return jdbcTemplate.query(GET_CATEGORY, params, new ResultSetExtractor<CategoryDto>() {

			@Override
			public CategoryDto extractData(ResultSet rs) throws SQLException, DataAccessException {
				if (!rs.next()) {
					return null;
				}
				CategoryDto categoryDto = new CategoryDto();
				categoryDto.id = rs.getInt("id");
				categoryDto.name = rs.getString("name");
				categoryDto.dataOwner = rs.getLong("data_owner");
				List<VariableDto> variableDtos = new ArrayList<VariableDto>();
				categoryDto.global = globalDataOwner.equals(categoryDto.dataOwner);
				do {
					// check if category has no variables
					if (rs.getObject("varId") == null) {
						break;
					}
					VariableDto variableDto = new VariableDto();
					variableDto.id = rs.getInt("varId");
					variableDto.name = rs.getString("varName");
					variableDto.dataOwner = rs.getLong("varDataOwner");
					variableDto.application = rs.getString("appName");
					variableDto.global = globalDataOwner.equals(variableDto.dataOwner);
					variableDtos.add(variableDto);
				} while (rs.next());
				categoryDto.setVariables(variableDtos);
				return categoryDto;
			}
		});
	}
}
