package com.quantimodo.dao;

import com.quantimodo.data.Measurement;
import com.quantimodo.data.CombinationOperation;

import java.util.Collections;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

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
 * <code>MeasurementDAO</code> deals with <code>Measurement</code>s in the database.
 * 
 * @author C Erler
 * @version 1.0, 07/08/13 
 */

public final class MeasurementDAO {
	private static final Logger logger = LoggerFactory.getLogger(MeasurementDAO.class);
	private final DAOBase database;
	
	private MeasurementDAO() { throw new UnsupportedOperationException("Default constructor is invalid."); }
	/*package*/ MeasurementDAO(final DAOBase database) {
		this.database = database;
	}
	
	// Tables:
	//   meas:  qm-measurements
	//   srcs:  qm-measurement-sources
	//   vars:  qm-variables
	//   sets:  qm-variable-user-settings
	//   unit:  qm-units
	public final List<Measurement> get(final Map<String, String> whereClauses, final Object... parameters) {
		final String sql = "SELECT " +
		                     "srcs.name AS source, " +
		                     "vars.name AS `variable-name`, " +
		                     "vars.`combination-operation` AS `combination-operation`, " +
		                     "meas.timestamp AS timestamp, " +
		                     "meas.value AS value, " +
		                     "unit.`abbreviated-name` AS `abbreviated-unit-name`, " +
		                     "stor.name AS `storage-unit-name`, " +
		                     "unit.name AS `presentation-unit-name` " +
		                   "FROM `qm-measurements` AS meas " +
		                   "INNER JOIN `qm-measurement-sources` AS srcs ON meas.source = srcs.id " +
		                   "INNER JOIN `qm-variables` AS vars ON meas.variable = vars.id " +
		                   "LEFT JOIN `qm-variable-user-settings` AS sets ON meas.user = sets.user AND vars.id = sets.variable " +
		                   "INNER JOIN `qm-units` AS stor ON meas.unit = stor.id " +
		                   "INNER JOIN `qm-units` AS unit ON COALESCE(sets.unit, vars.`default-unit`) = unit.id";
		return database.getUsingUserID(new RowMapper<Measurement>() {
			public final Measurement mapRow(final ResultSet results, final int rowNumber) throws SQLException {
				final String measurementSource = results.getString("source");
				final String variableName = results.getString("variable-name");
				final CombinationOperation combinationOperation = CombinationOperation.valueOf(results.getByte("combination-operation"));
				final long timestamp = results.getInt("timestamp") * 60000L;
				final double value = database.units.convert(results.getString("storage-unit-name"), results.getString("presentation-unit-name"), results.getDouble("value"));
				final String abbreviatedUnitName = results.getString("abbreviated-unit-name");
				return new Measurement(measurementSource, variableName, combinationOperation, timestamp, value, abbreviatedUnitName);
			}
		}, sql, "meas.user", whereClauses, parameters);
	}
	
	public final List<Measurement> getAll() {
		return get(null);
	}
	  
	public final void put(final Collection<? extends Measurement> measurements) {
		if (measurements == null) return;
		final int length = measurements.size();
		if (length == 0) return;
		final MapSqlParameterSource[] parameterRows = new MapSqlParameterSource[length]; {
			final Map<String, Short> measurementSourceNameIndex = database.measurementSources.getNameIndex();
			final Map<String, Integer> variableNameIndex = database.variables.getNameIndex();
			final Map<String, Short> abbreviatedUnitNameIndex = database.units.getAbbreviatedNameIndex();
			int i = 0;
			for (final Measurement measurement : measurements) {
				final MapSqlParameterSource parameterRow = new MapSqlParameterSource();
				{
					final String measurementSource = measurement.getMeasurementSource();
					final Short measurementSourceID = measurementSourceNameIndex.get(measurementSource);
					if (measurementSourceID == null) throw new ConstraintException("No such measurement source (" + measurementSource + ")");
					parameterRow.addValue("measurementSourceID", measurementSourceID, Types.SMALLINT);
				}
				{
					final String variableName = measurement.getVariableName();
					final Integer variableID = variableNameIndex.get(variableName);
					if (variableID == null) throw new ConstraintException("No such variable (" + variableName + ")");
					parameterRow.addValue("variableID", variableID, Types.INTEGER);
				}
				parameterRow.addValue("combinationOperation", measurement.getCombinationOperation().getMagicNumber(), Types.TINYINT);
				parameterRow.addValue("timestamp", (int) (measurement.getTimestamp() / 60000L), Types.INTEGER);
				parameterRow.addValue("value", measurement.getValue(), Types.DOUBLE);
				{
					final String abbreviatedUnitName = measurement.getAbbreviatedUnitName();
					final Short unitID = abbreviatedUnitNameIndex.get(abbreviatedUnitName);
					if (unitID == null) throw new ConstraintException("No such unit (" + abbreviatedUnitName + ")");
					parameterRow.addValue("unitID", abbreviatedUnitNameIndex.get(measurement.getAbbreviatedUnitName()), Types.SMALLINT);
				}
				parameterRows[i++] = parameterRow;
			}
		}
		database.putUsingUserID("INSERT INTO `qm-measurements` (user, variable, source, timestamp, value, unit) " +
		                          "VALUES (:userID, :variableID, :measurementSourceID, :timestamp, :value, :unitID) " +
		                          "ON DUPLICATE KEY UPDATE value = VALUES(value), unit = VALUES(unit)",
		                          parameterRows
		);
	}
}
