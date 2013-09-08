package com.quantimodo.dao;

import com.quantimodo.data.MeasurementSource;

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
 * <code>MeasurementSourceDAO</code> deals with <code>MeasurementSource</code>s in the database.
 * 
 * @author C Erler
 * @version 1.0, 07/08/13 
 */

public final class MeasurementSourceDAO {
	private static final Logger logger = LoggerFactory.getLogger(MeasurementSourceDAO.class);
	private final DAOBase database;
	
	private MeasurementSourceDAO() { throw new UnsupportedOperationException("Default constructor is invalid."); }
	/*package*/ MeasurementSourceDAO(final DAOBase database) {
		this.database = database;
	}

	void clearCaches() {
		getAllCache = null;
		getNameIndexCache = null;
	}
		
	public final List<MeasurementSource> get(final Map<String, String> whereSubclauses, final Object... parameters) {
		final String sql = "SELECT name FROM `qm-measurement-sources`";
		return database.get(new RowMapper<MeasurementSource>() {
			public final MeasurementSource mapRow(final ResultSet results, final int rowNumber) throws SQLException {
				final String name = results.getString("name");
				return new MeasurementSource(results.getString("name"));
			}
		}, sql, whereSubclauses, parameters);
	}
	
	/**
	 * Returns all <code>MeasurementSource</code>s.
	 * 
	 * @return a <code>List</code> of all of the logged-in user's <code>Measurement</code>s, or <tt>null</tt> if no one is logged in.
	 */
	public synchronized final List<MeasurementSource> getAll() {
		return (getAllCache == null) ? (getAllCache = get(null)) : getAllCache;
	}
	volatile List<MeasurementSource> getAllCache = null;
	
	synchronized final Map<String, Short> getNameIndex() {
		if (getNameIndexCache != null) return getNameIndexCache;
		
		final SqlRowSet rows = database.get("SELECT id, name FROM `qm-measurement-sources`", null);
		final Map<String, Short> result = new HashMap<String, Short>();
		while (rows.next()) result.put(rows.getString("name"), rows.getShort("id"));
		return getNameIndexCache = result;
	}
	volatile Map<String, Short> getNameIndexCache = null;
	
	public final void put(final MeasurementSource measurementSource) {
		put(Collections.<MeasurementSource>singletonList(measurementSource));
	}
	
	public final void put(final Collection<? extends MeasurementSource> measurementSources) {
		if (!database.currentUserIsAdmin()) throw new AuthorizationException("Only administrative users can add measurement sources.");
		if (measurementSources == null) return;
		final int length = measurementSources.size();
		if (length == 0) return;
		final MapSqlParameterSource[] parameterRows = new MapSqlParameterSource[length]; {
			int i = 0;
			for (final MeasurementSource measurementSource : measurementSources) {
				final MapSqlParameterSource parameterRow = new MapSqlParameterSource();
				parameterRow.addValue("name", measurementSource.getName(), Types.VARCHAR);
				parameterRows[i++] = parameterRow;
			}
		}
		database.put("INSERT IGNORE INTO `qm-measurement-sources` (name) VALUES (:name)", parameterRows);
		clearCaches();
	}
}
