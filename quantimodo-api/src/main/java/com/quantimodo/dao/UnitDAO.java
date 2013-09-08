package com.quantimodo.dao;

import com.quantimodo.data.Unit;
import com.quantimodo.data.CombinationOperation;

import java.util.Collections;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import java.sql.Types;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>UnitDAO</code> deals with <code>Unit</code>s in the database.
 * 
 * @author C Erler
 * @version 1.0, 07/08/13 
 */

@EnableTransactionManagement
public final class UnitDAO {
	private static final Logger logger = LoggerFactory.getLogger(UnitDAO.class);
	private final DAOBase database;
	
	private UnitDAO() { throw new UnsupportedOperationException("Default constructor is invalid."); }
	UnitDAO(final DAOBase database) {
		this.database = database;
	}
	
	void clearCaches() {
		getAllCache = null;
		getAllByNameCache = null;
		getNameIndexCache = null;
		getAbbreviatedNameIndexCache = null;
	}
	
	// Tables
	//   cats:  qm-unit-categories
	//   unit:  qm-units
	public final List<Unit> get(final Map<String, String> whereSubclauses, final Object... parameters) {
		final String sql = "SELECT unit.id AS id, unit.name AS name, unit.`abbreviated-name` AS abbreviatedName, cats.name AS categoryName " +
		                   "FROM `qm-units` AS unit " +
		                   "INNER JOIN `qm-unit-categories` AS cats ON unit.category = cats.id";
		return database.get(new RowMapper<Unit>() {
			public final Unit mapRow(final ResultSet results, final int rowNumber) throws SQLException {
				final String name = results.getString("name");
				final String abbreviatedName = results.getString("abbreviatedName");
				final String categoryName = results.getString("categoryName");
				final Unit.ConversionStep[] conversionSteps = getConversion(results.getShort("id"));
				return new Unit(name, abbreviatedName, categoryName, conversionSteps);
			}
		}, sql, whereSubclauses, parameters);
	}
	
	private Unit.ConversionStep[] getConversion(final short unitID) {
		final List<Unit.ConversionStep> results = database.get(new RowMapper<Unit.ConversionStep>() {
			public final Unit.ConversionStep mapRow(final ResultSet results, final int rowNumber) throws SQLException {
				final Unit.ConversionStep.Operation operation = Unit.ConversionStep.Operation.valueOf(results.getByte("operation"));
				final double value = results.getDouble("value");
				return new Unit.ConversionStep(operation, value);
			}
		}, "SELECT operation, value FROM `qm-unit-conversions` WHERE unit = :unitID ORDER BY `step-number`", null, "unitID", unitID);
		return results.toArray(new Unit.ConversionStep[0]);
	}
	
	/**
	 * Returns all <code>Unit</code>s.
	 * 
	 * @return a <code>List</code> of all of the <code>Unit</code>s.
	 */
	public synchronized final List<Unit> getAll() {
		if (getAllCache == null) {
			getAllCache = get(null);
			getAllByNameCache = new HashMap<String, Unit>();
			for (final Unit unit : getAllCache) { getAllByNameCache.put(unit.getName(), unit); }
		}
		return getAllCache;
	}
	public final Map<String, Unit> getAllByName() {
		getAll();
		return getAllByNameCache;
	}
	volatile List<Unit> getAllCache = null;
	volatile Map<String, Unit> getAllByNameCache = null;
	
	synchronized final Map<String, Short> getNameIndex() {
		if (getNameIndexCache != null) return getNameIndexCache;
		
		final SqlRowSet rows = database.get("SELECT id, name FROM `qm-units`", null);
		final Map<String, Short> result = new HashMap<String, Short>();
		while (rows.next()) result.put(rows.getString("name"), rows.getShort("id"));
		return getNameIndexCache = result;
	}
	volatile Map<String, Short> getNameIndexCache = null;
	
	synchronized final Map<String, Short> getAbbreviatedNameIndex() {
		if (getAbbreviatedNameIndexCache != null) return getAbbreviatedNameIndexCache;
		
		final SqlRowSet rows = database.get("SELECT id, `abbreviated-name` FROM `qm-units`", null);
		final Map<String, Short> result = new HashMap<String, Short>();
		while (rows.next()) result.put(rows.getString("abbreviated-name"), rows.getShort("id"));
		return getAbbreviatedNameIndexCache = result;
	}
	volatile Map<String, Short> getAbbreviatedNameIndexCache = null;
	
	final double convert(final String fromUnitName, final String toUnitName, final double value) {
		final Map<String, Unit> unitsByName = getAllByName();
		
		Unit fromUnit = unitsByName.get(fromUnitName);
		if (fromUnit == null) throw new ConstraintException("Could not find unit \"" + fromUnitName + "\"");
		
		Unit toUnit = unitsByName.get(toUnitName);
		if (toUnit == null) throw new ConstraintException("Could not find unit \"" + toUnitName + "\"");
		
		if (!fromUnit.getCategoryName().equals(toUnit.getCategoryName()))
			throw new ConstraintException("The units \"" + fromUnitName + "\" and \"" + toUnitName +
					"\" are in different categories; units from one cannot converted into the other");
		
		return Unit.convert(fromUnit, toUnit, value);
	}
	
	@Transactional
	public final void put(final Unit units) {
		put(Collections.<Unit>singletonList(units));
	}
	
	@Transactional
	public final void put(final Collection<? extends Unit> units) {
		if (!database.currentUserIsAdmin()) throw new AuthorizationException("Only administrative users can add units.");
		if (units == null) return;
		final int length = units.size();
		if (length == 0) return;
		
		final MapSqlParameterSource[] parameterRows = new MapSqlParameterSource[length]; {
			int i = 0;
			for (final Unit unit : units) {
				final MapSqlParameterSource parameterRow = new MapSqlParameterSource();
				parameterRow.addValue("name", unit.getName(), Types.VARCHAR);
				parameterRow.addValue("abbreviatedName", unit.getAbbreviatedName(), Types.VARCHAR);
				parameterRow.addValue("categoryName", unit.getCategoryName(), Types.VARCHAR);
				parameterRows[i++] = parameterRow;
			}
		}
		database.put("INSERT INTO `qm-units` (name, `abbreviated-name`, category) VALUES (" +
		               ":name, :abbreviatedName, (SELECT id FROM `qm-unit-categories` WHERE name = :categoryName)" +
		             ") ON DUPLICATE KEY UPDATE name = VALUES(name), category = VALUES(category)", parameterRows);
		clearCaches();
		
		final Map<String, Short> abbreviatedNameIndex = getAbbreviatedNameIndex();
		for (final Unit unit : units) {
			final Short unitID = abbreviatedNameIndex.get(unit.getAbbreviatedName());
			if (unitID == null) continue;
			final Unit.ConversionStep[] operations = unit.getConversionSteps();
			if (operations == null) continue;
			putConversion(unitID, operations);
		}
	}
	
	@Transactional
	private final void putConversion(final short unitID, final Unit.ConversionStep[] operations) {
		database.execute("DELETE FROM `qm-unit-conversions` WHERE unit = :unitID", "unitID", unitID);
		
		final int length = operations.length;
		final MapSqlParameterSource[] parameterRows = new MapSqlParameterSource[length];
		for (int i = 0; i < length; i++) {
			final Unit.ConversionStep operation = operations[i];
			final MapSqlParameterSource parameterRow = new MapSqlParameterSource();
			
			parameterRow.addValue("unitID", unitID, Types.SMALLINT);
			parameterRow.addValue("stepNumber", (byte) i, Types.TINYINT);
			parameterRow.addValue("operation", operation.operation.getMagicNumber(), Types.TINYINT);
			parameterRow.addValue("value", operation.value, Types.DOUBLE);
			
			parameterRows[i] = parameterRow;
		}
		
		database.put("INSERT INTO `qm-unit-conversions` (unit, step-number, operation, value) VALUES (:unitID, :stepNumber, :operation, :value)", parameterRows);
	}
}
