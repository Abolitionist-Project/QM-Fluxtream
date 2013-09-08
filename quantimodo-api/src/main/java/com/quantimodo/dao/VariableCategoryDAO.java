package com.quantimodo.dao;

import com.quantimodo.data.VariableCategory;

import java.util.Collections;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.sql.DataSource;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import java.sql.Types;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>VariableCategoryDAO</code> deals with <code>VariableCategory</code>s in the database.
 * 
 * @author C Erler
 * @version 1.0, 07/08/13 
 */

public final class VariableCategoryDAO {
	private static final Logger logger = LoggerFactory.getLogger(VariableCategoryDAO.class);
	private final DAOBase database;
	
	private VariableCategoryDAO() { throw new UnsupportedOperationException("Default constructor is invalid."); }
	VariableCategoryDAO(final DAOBase database) {
		this.database = database;
	}
	
	void clearCaches() {
		getAllCache = null;
		getNameIndexCache = null;
	}
	
	public final List<VariableCategory> get(final Map<String, String> whereSubclauses, final Object... parameters) {
		final String sql = "SELECT name FROM `qm-variable-categories`";
		return database.get(new RowMapper<VariableCategory>() {
			public final VariableCategory mapRow(final ResultSet results, final int rowNumber) throws SQLException {
				final String name = results.getString("name");
				return new VariableCategory(name);
			}
		}, sql, whereSubclauses, parameters);
	}
	
	/**
	 * Returns all <code>VariableCategory</code>s.
	 * 
	 * @return a <code>List</code> of all of the <code>VariableCategory</code>s.
	 */
	public synchronized final List<VariableCategory> getAll() {
		return (getAllCache == null) ? (getAllCache = get(null)) : getAllCache;
	}
	volatile List<VariableCategory> getAllCache = null;
	
	synchronized final Map<String, Byte> getNameIndex() {
		if (getNameIndexCache != null) return getNameIndexCache;
		
		final SqlRowSet rows = database.get("SELECT id, name FROM `qm-variable-categories`", null);
		final Map<String, Byte> result = new HashMap<String, Byte>();
		while (rows.next()) result.put(rows.getString("name"), rows.getByte("id"));
		return getNameIndexCache = result;
	}
	volatile Map<String, Byte> getNameIndexCache = null;
	
	public final void put(final VariableCategory variableCategory) {
		put(Collections.<VariableCategory>singletonList(variableCategory));
	}
	
	public final void put(final Collection<? extends VariableCategory> variableCategories) {
		if (!database.currentUserIsAdmin()) throw new AuthorizationException("Only administrative users can add variable categories.");
		if (variableCategories == null) return;
		final int length = variableCategories.size();
		if (length == 0) return;
		final MapSqlParameterSource[] parameterRows = new MapSqlParameterSource[length]; {
			int i = 0;
			for (final VariableCategory variableCategory : variableCategories) {
				final MapSqlParameterSource parameterRow = new MapSqlParameterSource();
				parameterRow.addValue("name", variableCategory.getName(), Types.VARCHAR);
				parameterRows[i++] = parameterRow;
			}
		}
		database.put("INSERT IGNORE INTO `qm-variable-categories` (name) VALUES (:name)", parameterRows);
		clearCaches();
	}
}
