package com.quantimodo.dao;

import com.quantimodo.data.UnitCategory;

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
 * <code>UnitCategoryDAO</code> deals with <code>UnitCategory</code>s in the database.
 * 
 * @author C Erler
 * @version 1.0, 07/08/13 
 */

public final class UnitCategoryDAO {
	private static final Logger logger = LoggerFactory.getLogger(UnitCategoryDAO.class);
	private final DAOBase database;
	
	private UnitCategoryDAO() { throw new UnsupportedOperationException("Default constructor is invalid."); }
	UnitCategoryDAO(final DAOBase database) {
		this.database = database;
	}
	
	void clearCaches() {
		getAllCache = null;
		getNameIndexCache = null;
	}
	
	public final List<UnitCategory> get(final Map<String, String> whereSubclauses, final Object... parameters) {
		final String sql = "SELECT name FROM `qm-unit-categories`";
		return database.get(new RowMapper<UnitCategory>() {
			public final UnitCategory mapRow(final ResultSet results, final int rowNumber) throws SQLException {
				final String name = results.getString("name");
				return new UnitCategory(name);
			}
		}, sql, whereSubclauses, parameters);
	}
	
	/**
	 * Returns all <code>UnitCategory</code>s.
	 * 
	 * @return a <code>List</code> of all of the <code>UnitCategory</code>s.
	 */
	public synchronized final List<UnitCategory> getAll() {
		return (getAllCache == null) ? (getAllCache = get(null)) : getAllCache;
	}
	volatile List<UnitCategory> getAllCache = null;
	
	synchronized final Map<String, Byte> getNameIndex() {
		if (getNameIndexCache != null) return getNameIndexCache;
		
		final SqlRowSet rows = database.get("SELECT id, name FROM `qm-unit-categories`", null);
		final Map<String, Byte> result = new HashMap<String, Byte>();
		while (rows.next()) result.put(rows.getString("name"), rows.getByte("id"));
		return getNameIndexCache = result;
	}
	volatile Map<String, Byte> getNameIndexCache = null;
	
	public final void put(final UnitCategory unitCategory) {
		put(Collections.<UnitCategory>singletonList(unitCategory));
	}
	
	public final void put(final Collection<? extends UnitCategory> unitCategories) {
		if (!database.currentUserIsAdmin()) throw new AuthorizationException("Only administrative users can add unit categories.");
		if (unitCategories == null) return;
		final int length = unitCategories.size();
		if (length == 0) return;
		final MapSqlParameterSource[] parameterRows = new MapSqlParameterSource[length]; {
			int i = 0;
			for (final UnitCategory unitCategory : unitCategories) {
				final MapSqlParameterSource parameterRow = new MapSqlParameterSource();
				parameterRow.addValue("name", unitCategory.getName(), Types.VARCHAR);
				parameterRows[i++] = parameterRow;
			}
		}
		database.put("INSERT IGNORE INTO `qm-unit-categories` (name) VALUES (:name)", parameterRows);
		clearCaches();
	}
}
