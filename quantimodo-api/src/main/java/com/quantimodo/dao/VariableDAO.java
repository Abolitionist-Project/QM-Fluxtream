package com.quantimodo.dao;

import com.quantimodo.data.Variable;
import com.quantimodo.data.CombinationOperation;

import java.util.Collections;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import java.sql.Types;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>VariableDAO</code> deals with <code>Variable</code>s in the database.
 * 
 * @author C Erler
 * @version 1.0, 07/08/13 
 */

public final class VariableDAO {
	private static final Logger logger = LoggerFactory.getLogger(VariableDAO.class);
	private final DAOBase database;
	
	private VariableDAO() { throw new UnsupportedOperationException("Default constructor is invalid."); }
	VariableDAO(final DAOBase database) {
		this.database = database;
	}
	
	// Tables
	//   cats:  qm-variable-categories
	//   unit:  qm-units
	//   vars:  qm-variables
	public final List<Variable> get(Map<String, String> whereSubclauses, final Object... parameters) {
		if (whereSubclauses == null) whereSubclauses = new HashMap<String, String>();
		whereSubclauses.put("userID", "vars.user IN (0, :userID)");
		final String sql = "SELECT vars.name AS variableName, cats.name AS variableCategoryName, unit.`abbreviated-name` AS abbreviatedDefaultUnitName, " +
		                     "vars.`combination-operation` AS combinationOperation " +
		                   "FROM `qm-variables` AS vars " +
		                   "LEFT JOIN `qm-variable-categories` AS cats ON vars.`variable-category` = cats.id " +
		                   "INNER JOIN `qm-units` AS unit ON vars.`default-unit` = unit.id";
		return database.getUsingUserID(new RowMapper<Variable>() {
			public final Variable mapRow(final ResultSet results, final int rowNumber) throws SQLException {
				final String variableName = results.getString("variableName");
				final String variableCategoryName = results.getString("variableCategoryName");
				final String abbreviatedDefaultUnitName = results.getString("abbreviatedDefaultUnitName");
				final CombinationOperation combinationOperation = CombinationOperation.valueOf(results.getByte("combinationOperation"));
				return new Variable(variableName, variableCategoryName, abbreviatedDefaultUnitName, combinationOperation);
			}
		}, sql, ":userID", whereSubclauses, parameters);
	}
	
	/**
	 * Returns all <code>Variable</code>s.
	 * 
	 * @return a <code>List</code> of all of the <code>Variable</code>s.
	 */
	public final List<Variable> getAll() {
		return get(null);
	}
	
	final Map<String, Integer> getNameIndex() {
		final SqlRowSet rows = database.get("SELECT id, name FROM `qm-variables`", null);
		final Map<String, Integer> result = new HashMap<String, Integer>();
		while (rows.next()) result.put(rows.getString("name"), rows.getInt("id"));
		return result;
	}
	
	public final void put(final Variable variables) {
		put(Collections.<Variable>singletonList(variables));
	}
	
	public final void put(final Collection<? extends Variable> variables) {
		putAs(false, variables);
	}
	
	public final void putForAllUsers(final Variable variables) {
		putForAllUsers(Collections.<Variable>singletonList(variables));
	}
	
	public final void putForAllUsers(final Collection<? extends Variable> variables) {
		if (!database.currentUserIsAdmin()) throw new AuthorizationException("Only administrative users can add all-user variables.");
		putAs(true, variables);
	}
	
	private final void putAs(final boolean isAllUser, final Collection<? extends Variable> variables) {
		if (variables == null) return;
		final int length = variables.size();
		if (length == 0) return;
		
		final MapSqlParameterSource[] parameterRows = new MapSqlParameterSource[length]; {
			int i = 0;
			for (final Variable variable : variables) {
				final MapSqlParameterSource parameterRow = new MapSqlParameterSource();
				parameterRow.addValue("variableName", variable.getName(), Types.VARCHAR);
				parameterRow.addValue("variableCategoryName", variable.getCategoryName(), Types.VARCHAR);
				parameterRow.addValue("abbreviatedDefaultUnitName", variable.getAbbreviatedDefaultUnitName(), Types.VARCHAR);
				parameterRow.addValue("combinationOperation", variable.getCombinationOperation().getMagicNumber(), Types.TINYINT);
				parameterRows[i++] = parameterRow;
			}
		}
		final String sql = new StringBuilder("INSERT INTO `qm-variables` (user, name, `variable-category`, `default-unit`, `combination-operation`) VALUES (")
		                   .append(isAllUser ? "0, " : ":userID, ")
		                   .append(  ":variableName, " +
		                             "(SELECT id FROM `qm-variable-categories` WHERE name = :variableCategoryName), " +
		                             "(SELECT id FROM `qm-units` WHERE `abbreviated-name` = :abbreviatedDefaultUnitName), " +
		                             ":combinationOperation" +
		                           ") ON DUPLICATE KEY UPDATE " +
		                           "`variable-category` = VALUES(`variable-category`), " +
		                           "`default-unit` = VALUES(`default-unit`), " +
		                           "`combination-operation` = VALUES(`combination-operation`)")
		                   .toString();
		database.putUsingUserID(sql, parameterRows);
	}
}
