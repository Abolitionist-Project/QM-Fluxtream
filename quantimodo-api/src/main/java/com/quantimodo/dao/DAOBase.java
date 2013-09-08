package com.quantimodo.dao;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.core.RowMapper;
import java.sql.Types;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>DAOBase</code> deals with database communication.
 * 
 * @author C Erler
 * @version 1.0, 07/08/13 
 */

@Repository
public final class DAOBase {
	private NamedParameterJdbcTemplate database;
	public final MeasurementDAO measurements;
	public final MeasurementSourceDAO measurementSources;
	public final UnitDAO units;
	public final UnitCategoryDAO unitCategories;
	public final VariableDAO variables;
	public final VariableCategoryDAO variableCategories;
	public final VariableUserSettingsDAO variableUserSettings;
	
	private static final Logger logger = LoggerFactory.getLogger(DAOBase.class);
	
	private DAOBase() {
		this.measurements = new MeasurementDAO(this);
		this.measurementSources = new MeasurementSourceDAO(this);
		this.units = new UnitDAO(this);
		this.unitCategories = new UnitCategoryDAO(this);
		this.variables = new VariableDAO(this);
		this.variableCategories = new VariableCategoryDAO(this);
		this.variableUserSettings = new VariableUserSettingsDAO(this);
	}
	
	@Autowired
	public void setDataSource(final DataSource dataSource) {
		if (database == null) database = new NamedParameterJdbcTemplate(dataSource);
	}
	
	/**
	 * Returns the ID of the logged-in user.
	 * 
	 * @return the ID of the logged-in user or <tt>null</tt> if no one is logged in.
	 */
	public static final Long getCurrentUserID() {
		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) return null;
		final String username = authentication.getName();
		if (username == null) return null;
		
		final Long userID;
		try {
			return Long.valueOf(username);
		} catch (final NumberFormatException e) {
			return null;
		}
	}
	
	public static final boolean currentUserIsAdmin() {
		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) return false;
		for (final GrantedAuthority authority : authentication.getAuthorities()) {
			final String authorityName = authority.getAuthority();
			if ((authorityName != null) && (authorityName.equals("ROLE_ADMIN"))) return true;
		}
		return false;
	}
	
	public final SqlRowSet get(final String sql, final Map<String, String> whereSubclauses, final Object... parameters) {
		final Map<String, Object> parameterMap = argsToMap(parameters);
		return database.queryForRowSet(addWhereClause(sql, whereSubclauses, parameterMap), parameterMap);
	}
	
	public final <T> List<T> get(final RowMapper<T> rowMapper, final String sql, final Map<String, String> whereSubclauses, final Object... parameters) {
		final Map<String, Object> parameterMap = argsToMap(parameters);
		return database.query(addWhereClause(sql, whereSubclauses, parameterMap), parameterMap, rowMapper);
	}
	
	public final SqlRowSet getUsingUserID(final String sql, final String userIDField, final Map<String, String> whereSubclauses, final Object... parameters) {
		final Long userID = getCurrentUserID();
		if (userID == null) return null;
		final Map<String, Object> parameterMap = argsToMapUsingUserID(userID, parameters);
		return database.queryForRowSet(addWhereClauseUsingUserID(sql, userIDField, whereSubclauses, parameterMap), parameterMap);
	}
	
	public final <T> List<T> getUsingUserID(final RowMapper<T> rowMapper, final String sql, final String userIDField,
			final Map<String, String> whereSubclauses, final Object... parameters) {
		final Long userID = getCurrentUserID();
		if (userID == null) return null;
		final Map<String, Object> parameterMap = argsToMapUsingUserID(userID, parameters);
		return database.query(addWhereClauseUsingUserID(sql, userIDField, whereSubclauses, parameterMap), parameterMap, rowMapper);
	}
	
	public final void put(final String sql, final MapSqlParameterSource[] parameterRows) {
		database.batchUpdate(sql, parameterRows);
	}
	
	public final void putUsingUserID(final String sql, final MapSqlParameterSource[] parameterRows) {
		final long userID = getCurrentUserID();
		for (final MapSqlParameterSource parameterRow : parameterRows) {
			parameterRow.addValue("userID", userID, Types.BIGINT);
		}
		put(sql, parameterRows);
	}
	
	public final void execute(final String sql, final Object... parameters) {
		database.update(sql, argsToMap(parameters));
	}
	
	public final void executeUsingUserID(final String sql, final Object... parameters) {
		database.update(sql, argsToMapUsingUserID(getCurrentUserID(), parameters));
	}
	
	private static final Map<String, Object> argsToMap(final Object... parameters) {
		final int length = parameters.length;
		switch (length) {
			case 0:
				return Collections.<String, Object>emptyMap();
			case 2:
				return Collections.<String, Object>singletonMap((String) parameters[0], parameters[1]);
			default:
				final Map<String, Object> result = new HashMap<String, Object>();
				for (int i = 0; i < length; i += 2) {
					result.put((String) parameters[i], parameters[i + 1]);
				}
				return result;
		}
	}
	
	private static final Map<String, Object> argsToMapUsingUserID(final long userID, final Object... parameters) {
		final int length = parameters.length;
		if (length == 0) return Collections.<String, Object>singletonMap("userID", userID);
		final Map<String, Object> result = new HashMap<String, Object>();
		result.put("userID", userID);
		for (int i = 0; i < length; i += 2) {
			result.put((String) parameters[i], parameters[i + 1]);
		}
		return result;
	}
	
	private static final String addWhereClause(final String sql, final Map<String, String> whereSubclauses, final Map<String, Object> parameters) {
		StringBuilder whereClause = null;
		if (whereSubclauses != null) {
			for (final String parameterName : whereSubclauses.keySet()) {
				if (parameters.get(parameterName) != null) {
					final String whereSubclause = whereSubclauses.get(parameterName);
					if (whereClause == null) whereClause = new StringBuilder(" WHERE ").append(whereSubclause);
					else whereClause.append(" AND ").append(whereSubclause);
				}
			}
		}
		if (whereClause == null) return sql + ";";
		return whereClause.append(';').insert(0, sql).toString();
	}
	
	private static final String addWhereClauseUsingUserID(final String sql, final String userIDField,
			final Map<String, String> whereSubclauses, final Map<String, Object> parameters) {
		StringBuilder whereClause = new StringBuilder(" WHERE ").append(userIDField).append(" = :userID");
		if (whereSubclauses != null) {
			for (final String parameterName : whereSubclauses.keySet()) {
				if (parameters.get(parameterName) != null) whereClause.append(" AND ").append(whereSubclauses.get(parameterName));
			}
		}
		return whereClause.append(';').insert(0, sql).toString();
	}
}
