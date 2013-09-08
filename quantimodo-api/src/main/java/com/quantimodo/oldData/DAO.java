package com.quantimodo.oldData;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.sql.DataSource;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.beans.factory.BeanFactory;

import org.springframework.dao.IncorrectResultSizeDataAccessException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>DAO</code> deals with database communication.
 * 
 * @author C Erler
 * @version 1.0, 07/08/13 
 */

@Component
public final class DAO {
	public static final DAO instance = new DAO();
	private static final Logger logger = LoggerFactory.getLogger(DAO.class);
	
	private NamedParameterJdbcTemplate sqlHandler;
	
	/**
	 * Constructs a <code>DAO</code> for database communication.
	 */
	private DAO() {}
	
	@Autowired(required = true)
	private void setDataSource(final DataSource database) {
		sqlHandler = new NamedParameterJdbcTemplate(database);
	}
	
	private static final Map<String, Object> argsToMap(final Object... parameters) {
		final int length = parameters.length;
		switch (length) {
			case 0:
				return Collections.<String, Object>emptyMap();
			case 2:
				return Collections.<String, Object>singletonMap((String) parameters[0], parameters[1]);
			default:
				Map<String, Object> result = new HashMap<String, Object>();
				for (int i = 0; i < length; i += 2) {
					result.put((String) parameters[i], parameters[i + 1]);
				}
				return result;
		}
	}
	
	private final Map<String, Object> getSingleResult(final String sql, final Object... parameters) {
		try {
			return sqlHandler.queryForMap(sql, argsToMap(parameters));
		} catch (final IncorrectResultSizeDataAccessException e) {
			return null;
		}
	}
	
	private final List<Map<String, Object>> getResults(final String sql, final Object... parameters) {
		return sqlHandler.queryForList(sql, argsToMap(parameters));
	}
	
	private final String getString(final Map<String, Object> map, final String key) {
		return (String) map.get(key);
	}
	
	private final Number getNumber(final Map<String, Object> map, final String key) {
		return (Number) map.get(key);
	}
	
	private final Boolean getBoolean(final Map<String, Object> map, final String key) {
		final Object value = map.get(key);
		if (value == null) return null;
		if (value instanceof Boolean) return ((boolean) value) ? Boolean.TRUE : Boolean.FALSE;
		return ((Number) value).longValue() == 0 ? Boolean.FALSE : Boolean.TRUE;
	}
	
	private final Byte getByte(final Map<String, Object> map, final String key) {
		final Number value = getNumber(map, key);
		return (value == null) ? null : value.byteValue();
	}
	
	private final Short getShort(final Map<String, Object> map, final String key) {
		final Number value = getNumber(map, key);
		return (value == null) ? null : value.shortValue();
	}
	
	private final Integer getInt(final Map<String, Object> map, final String key) {
		final Number value = getNumber(map, key);
		return (value == null) ? null : value.intValue();
	}
	
	private final Long getLong(final Map<String, Object> map, final String key) {
		final Number value = getNumber(map, key);
		return (value == null) ? null : value.longValue();
	}
	
	private final Float getFloat(final Map<String, Object> map, final String key) {
		final Number value = getNumber(map, key);
		return (value == null) ? null : value.floatValue();
	}
	
	private final Double getDouble(final Map<String, Object> map, final String key) {
		final Number value = getNumber(map, key);
		return (value == null) ? null : value.doubleValue();
	}
	
	/**
	 * Retrieves a <code>Measurement</code> given its primary key.
	 * 
	 * @param user       The <code>User</code> who made the <code>Measurement</code>
         * @param variable   The <code>Variable</code> being measured
	 * @param source     The source that recorded the event
	 * @param timestamp  The time of the event
	 * 
	 * @return the specified <code>Measurement</code> or <tt>null</tt> if it doesn't exist.
	 */
	public Measurement getMeasurement(final User user, final Variable variable, final MeasurementSource measurementSource, final long timestamp) {
		final int minuteResolutionTimestamp = (int) (timestamp/60000L);
		final Map<String, Object> map = getSingleResult(
				"SELECT * FROM `qm-measurements` WHERE " +
	                                "`user` = :userID AND `variable` = :variableID AND `source` = :measurementSourceID AND `timestamp` = :minuteResolutionTimestamp;",
				"userID", user.getID(),
				"variableID", variable.getID(),
				"measurementSource", measurementSource.getID(),
				"minuteResolutionTimestamp", minuteResolutionTimestamp
		);
		return (map == null) ? null : new Measurement(user, variable, measurementSource, minuteResolutionTimestamp, getDouble(map, "value"), getUnit(getShort(map, "unit")));
	}
	
	/**
	 * Retrieves a <code>MeasurementSource</code> given its primary key.
	 * 
	 * @param measurementSourceID  The ID of the <code>MeasurementSource</code>
	 * 
	 * @return the specified <code>MeasurementSource</code> or <tt>null</tt> if it doesn't exist.
	 */
	public MeasurementSource getMeasurementSource(final short measurementSourceID) {
		final Map<String, Object> map = getSingleResult(
				"SELECT * FROM `qm-measurement-sources` WHERE `id` = :measurementSourceID;",
				"measurementSourceID", measurementSourceID
		);
		return (map == null) ? null : new MeasurementSource(measurementSourceID, getString(map, "name"));
	}
	
	/**
	 * Retrieves a <code>Unit</code> given its primary key.
	 * 
	 * @param user      The <code>User</code> whose settings we're retrieving
	 * @param variable  The <code>Variable</code> that the settings apply to
	 * 
	 * @return the specified <code>Unit</code> or <tt>null</tt> if it doesn't exist.
	 */
	public Unit getUnit(final short unitID) {
		final Map<String, Object> map = getSingleResult(
				"SELECT " +
					"u.`name` AS `name`, " +
					"u.`abbreviated-name` AS `abbreviated-name`, " +
					"c.`id` AS `category-id`, " +
					"c.`name` AS `category-name`, " +
					"u2.`id` AS `canonical-unit-id`, " +
					"u2.`category` AS `canonical-unit-category-id`, " +
					"u2.`name` AS `canonical-unit-name`, " +
					"u2.`abbreviated-name` AS `canonical-unit-abbreviated-name`, " +
				"FROM `qm-units` AS u " +
					"INNER JOIN `qm-unit-categories` AS c ON u.`category` = c.`id` " +
					"LEFT JOIN `qm-units` AS u2 ON c.`canonical-unit` = u2.`id` " +
				"WHERE u.`id` = :unitID;",
				"unitID", unitID
		);
		if (map == null) return null;
		final String name            = getString(map, "name");
		final String abbreviatedName = getString(map, "abbreviated-name");
		final byte   categoryID      = getByte  (map, "category-id");
		final String categoryName    = getString(map, "category-name");
		
		final short  canonicalUnitID              = getShort (map, "canonical-unit-id");
		final byte   canonicalUnitCategoryID      = getByte  (map, "canonical-unit-category-id");
		final String canonicalUnitName            = getString(map, "canonical-unit-name");
		final String canonicalUnitAbbreviatedName = getString(map, "canonical-unit-abbreviated-name");
		
		if (categoryID != canonicalUnitCategoryID) {
			logger.error(String.format("Unit category %d (%s) has a canonical unit that's not actually in that unit category."));
		}
		final UnitCategory category = new UnitCategory(categoryID, categoryName, null);

		final Unit canonicalUnit; {
			final SortedMap<Byte, Map<String, Object>> sortedSteps = getUnitConversionOperations(canonicalUnitID);
			final int length = sortedSteps.size();
			
			final Unit.Operation[] operations = new Unit.Operation[length];
			final double[] operands = new double[length];
			
			int i = 0;
			for (final Map<String, Object> step : sortedSteps.values()) {
				operations[i] = Unit.toOperation(getByte(step, "operation"));
				operands[i] = getDouble(step, "value");
				i++;
			}
			canonicalUnit = new Unit(canonicalUnitID, category, canonicalUnitName, canonicalUnitAbbreviatedName, operations, operands);
		}
		category.setCanonicalUnit(canonicalUnit);
		if (unitID == canonicalUnitID) return canonicalUnit;
		
		final Unit result; {
			final SortedMap<Byte, Map<String, Object>> sortedSteps = getUnitConversionOperations(unitID);
			final int length = sortedSteps.size();
			
			final Unit.Operation[] operations = new Unit.Operation[length];
			final double[] operands = new double[length];
			
			int i = 0;
			for (final Map<String, Object> step : sortedSteps.values()) {
				operations[i] = Unit.toOperation(getByte(step, "operation"));
				operands[i] = getDouble(step, "value");
				i++;
			}
			result = new Unit(unitID, category, canonicalUnitName, canonicalUnitAbbreviatedName, operations, operands);
		}
		return result;
	}
	private SortedMap<Byte, Map<String, Object>> getUnitConversionOperations(final short unitID) {
		final List<Map<String, Object>> steps = getResults(
				"SELECT * FROM `qm-unit-conversions` WHERE `unit` = :unitID;",
				"unitID", unitID
		);
		if ((steps == null) || (steps.isEmpty())) return null;
		
		final SortedMap<Byte, Map<String, Object>> sortedSteps = new TreeMap<Byte, Map<String, Object>>();
		for (final Map<String, Object> step : steps) {
			sortedSteps.put(getByte(step, "step-number"), step);
		}
		
		return sortedSteps;
	}
	
	/**
	 * Retrieves a <code>UnitCategory</code> given its primary key.
	 * 
	 * @param unitCategoryID  The ID of the <code>UnitCategory</code>
	 * 
	 * @return the specified <code>UnitCategory</code> or <tt>null</tt> if it doesn't exist.
	 */
	public UnitCategory getUnitCategory(final byte unitCategoryID) {
		final Map<String, Object> map = getSingleResult(
				"SELECT * FROM `qm-unit-categories` WHERE `id` = :unitCategoryID;",
				"unitCategoryID", unitCategoryID
		);
		return (map == null) ? null : new UnitCategory(unitCategoryID, getString(map, "name"), getUnit(getShort(map, "canonical-unit")));
	}
	
	/**
	 * Retrieves a <code>User</code> given its primary key.
	 * 
	 * @param userID  The ID of the <code>User</code>
	 * 
	 * @return the specified <code>User</code> or <tt>null</tt> if it doesn't exist.
	 */
	public User getUser(final long userID) {
		return new User(userID, false);
	}
	
	/**
	 * Retrieves a <code>Variable</code> given its primary key.
	 * 
	 * @param variableID  The ID of the <code>Variable</code>
	 * 
	 * @return the specified <code>Variable</code> or <tt>null</tt> if it doesn't exist.
	 */
	public Variable getVariable(final int variableID) {
		final Map<String, Object> map = getSingleResult(
				"SELECT * FROM `qm-variables` WHERE `id` = :variableID;",
				"variableID", variableID
		);
		return (map == null) ? null : new Variable(variableID, getUser(getLong(map, "user")), getString(map, "name"), getVariableCategory(getByte(map, "category")),
				getUnit(getShort(map, "default-unit")), Variable.toOperation(getByte(map, "combination-operation")));
	}
	
	/**
	 * Retrieves a <code>VariableCategory</code> given its primary key.
	 * 
	 * @param variableCategoryID  The ID of the <code>VariableCategory</code>
	 * 
	 * @return the specified <code>VariableCategory</code> or <tt>null</tt> if it doesn't exist.
	 */
	public VariableCategory getVariableCategory(final byte variableCategoryID) {
		final Map<String, Object> map = getSingleResult(
				"SELECT * FROM `qm-variable-categories` WHERE `id` = :variableCategoryID;",
				"variableCategoryID", variableCategoryID
		);
		return (map == null) ? null : new VariableCategory(variableCategoryID, getString(map, "name"));
	}
	
	/**
	 * Retrieves a <code>VariableUserSettings</code> given its primary key.
	 * 
	 * @param user      The <code>User</code> whose settings we're retrieving
	 * @param variable  The <code>Variable</code> that the settings apply to
	 * 
	 * @return the specified <code>VariableUserSetting</code> or <tt>null</tt> if it doesn't exist.
	 */
	public VariableUserSettings getVariableUserSettings(final User user, final Variable variable) {
		final Map<String, Object> map = getSingleResult(
				"SELECT * FROM `qm-variable-user-settings` WHERE `user` = :userID AND `variable` = :variableID;",
				"userID", user.getID(),
				"variableID", variable.getID()
		);
		return (map == null) ? null : new VariableUserSettings(user, variable, getUnit(getShort(map, "unit")));
	}
}
