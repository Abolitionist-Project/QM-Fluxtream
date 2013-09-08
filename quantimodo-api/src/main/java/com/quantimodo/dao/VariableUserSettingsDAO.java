package com.quantimodo.dao;

import com.quantimodo.data.VariableUserSettings;

import java.util.Collections;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import java.sql.Types;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>VariableUserSettingsDAO</code> deals with <code>VariableUserSettings</code>s in the database.
 * 
 * @author C Erler
 * @version 1.0, 07/08/13 
 */

public final class VariableUserSettingsDAO {
	private static final Logger logger = LoggerFactory.getLogger(VariableUserSettingsDAO.class);
	private final DAOBase database;
	
	private VariableUserSettingsDAO() { throw new UnsupportedOperationException("Default constructor is invalid."); }
	VariableUserSettingsDAO(final DAOBase database) {
		this.database = database;
	}
	
	// Tables
	//   sets:  qm-variable-user-settings
	//   unit:  qm-units
	//   vars:  qm-variables
	public final List<VariableUserSettings> get(final Map<String, String> whereSubclauses, final Object... parameters) {
		final String sql = "SELECT vars.name AS variableName, unit.`abbreviated-name` AS abbreviatedUnitName " +
		                   "FROM `qm-variable-user-settings` AS sets " +
		                   "INNER JOIN `qm-variables` AS vars ON sets.variable = vars.id " +
		                   "LEFT JOIN `qm-units` AS unit ON sets.unit = unit.id";
		return database.getUsingUserID(new RowMapper<VariableUserSettings>() {
			public final VariableUserSettings mapRow(final ResultSet results, final int rowNumber) throws SQLException {
				final String variableName = results.getString("variableName");
				final String unitName = results.getString("abbreviatedUnitName");
				return new VariableUserSettings(variableName, unitName);
			}
		}, sql, "sets.user", whereSubclauses, parameters);
	}
	
	/**
	 * Returns all <code>VariableUserSettings</code>s.
	 * 
	 * @return a <code>List</code> of all of the <code>VariableUserSettings</code>s.
	 */
	public final List<VariableUserSettings> getAll() {
		return get(null);
	}
	
	public final void put(final VariableUserSettings variableUserSettings) {
		put(Collections.<VariableUserSettings>singletonList(variableUserSettings));
	}
	
	public final void put(final Collection<? extends VariableUserSettings> variableUserSettings) {
		if (variableUserSettings == null) return;
		final int length = variableUserSettings.size();
		if (length == 0) return;
		
		final MapSqlParameterSource[] parameterRows = new MapSqlParameterSource[length]; {
			int i = 0;
			for (final VariableUserSettings variableUserSettingsInstance : variableUserSettings) {
				final MapSqlParameterSource parameterRow = new MapSqlParameterSource();
				parameterRow.addValue("variableName", variableUserSettingsInstance.getVariableName(), Types.VARCHAR);
				final String abbreviatedUnitName = variableUserSettingsInstance.getAbbreviatedUnitName();
				if (abbreviatedUnitName == null)
					parameterRow.addValue("abbreviatedUnitName", null, Types.NULL);
				else
					parameterRow.addValue("abbreviatedUnitName", abbreviatedUnitName, Types.VARCHAR);
				parameterRows[i++] = parameterRow;
			}
		}
		database.putUsingUserID("INSERT INTO `qm-variable-user-settings` (user, variable, unit) VALUES (" +
		                          ":userID, " +
		                          "(SELECT id FROM `qm-variables` WHERE name = :variableName), " +
		                          "IF(:abbreviatedUnitName IS NULL, NULL, (SELECT `id` FROM `qm-units` WHERE `abbreviated-name` = :abbreviatedUnitName))" +
		                        ")",
		                        parameterRows
		);
	}
}
