package com.quantimodo.api;

import com.quantimodo.data.Measurement;
import com.quantimodo.data.MeasurementSource;
import com.quantimodo.data.Unit;
import com.quantimodo.data.UnitCategory;
import com.quantimodo.data.Variable;
import com.quantimodo.data.VariableCategory;
import com.quantimodo.data.VariableUserSettings;

import com.quantimodo.data.Success;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import com.quantimodo.dao.DAOBase;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class APIController {
	private static final Logger logger = LoggerFactory.getLogger(APIController.class);
	private final DAOBase database;
	@Autowired
	public APIController(final DAOBase daoBase) {
		this.database = daoBase;
	}

	//@Autowired
	//private UnitCategoryDAO unitCategoryDAO;

	//@Autowired
	//private VariableCategoryDAO variableCategoryDAO;

	@RequestMapping(value = "/measurements", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody List<Measurement> getMeasurements(
		@RequestParam(required = false) final String variableName,
		@RequestParam(required = false) final Long startTime,
		@RequestParam(required = false) final Long endTime
	) {
		return database.measurements.get(MEASUREMENTS_WHERE_SUBCLAUSES, "variableName", variableName, "startTime", startTime, "endTime", endTime);
	}
	public static final Map<String, String> MEASUREMENTS_WHERE_SUBCLAUSES;
	static {
		MEASUREMENTS_WHERE_SUBCLAUSES = new HashMap<String, String>();
		MEASUREMENTS_WHERE_SUBCLAUSES.put("variableName", "vars.name = :variableName");
		MEASUREMENTS_WHERE_SUBCLAUSES.put("startTime", "meas.timestamp >= :startTime div 60");
		MEASUREMENTS_WHERE_SUBCLAUSES.put("endTime", "meas.timestamp <= :endTime div 60");
	}
	
	@RequestMapping(value = "/measurements", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public @ResponseBody Success postMeasurements(@RequestBody final ArrayList<Measurement> measurements) {
		database.measurements.put(measurements);
		return new Success();
	}
	
	@RequestMapping(value = "/measurementSources", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody List<MeasurementSource> getMeasurementSources() {
		return database.measurementSources.getAll();
	}
	
	@RequestMapping(value = "/measurementSources", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public @ResponseBody Success postMeasurementSources(@RequestBody final ArrayList<MeasurementSource> measurementSources) {
		database.measurementSources.put(measurementSources);
		return new Success();
	}
	
	@RequestMapping(value = "/units", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody List<Unit> getUnits(
		@RequestParam(required = false) final String unitName,
		@RequestParam(required = false) final String abbreviatedUnitName,
		@RequestParam(required = false) final String categoryName
	) {
		return database.units.get(UNITS_WHERE_SUBCLAUSES, "unitName", unitName, "abbreviatedUnitName", abbreviatedUnitName, "categoryName", categoryName);
	}
	public static final Map<String, String> UNITS_WHERE_SUBCLAUSES;
	static {
		UNITS_WHERE_SUBCLAUSES = new HashMap<String, String>();
		UNITS_WHERE_SUBCLAUSES.put("unitName", "unit.name = :unitName");
		UNITS_WHERE_SUBCLAUSES.put("abbreviatedUnitName", "unit.`abbreviated-name` = :abbreviatedUnitName");
		UNITS_WHERE_SUBCLAUSES.put("categoryName", "cats.name = :categoryName");
	}
	
	@RequestMapping(value = "/units", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public @ResponseBody Success postUnits(@RequestBody final ArrayList<Unit> units) {
		database.units.put(units);
		return new Success();
	}
	
	@RequestMapping(value = "/unitCategories", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody List<UnitCategory> getUnitCategories() {
		return database.unitCategories.getAll();
		//return unitCategoryDAO.get();
	}
	
	@RequestMapping(value = "/unitCategories", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public @ResponseBody Success postUnitCategories(@RequestBody final ArrayList<UnitCategory> unitCategories) {
		database.unitCategories.put(unitCategories);
		//unitCategoryDAO.put(unitCategories);
		return new Success();
	}
	
	@RequestMapping(value = "/variables", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody List<Variable> getVariables(@RequestParam(required = false) final String categoryName) {
		if (categoryName == null) return database.variables.getAll();
		return database.variables.get(VARIABLES_WHERE_SUBCLAUSES, "categoryName", categoryName);
	}
	public static final Map<String, String> VARIABLES_WHERE_SUBCLAUSES;
	static {
		VARIABLES_WHERE_SUBCLAUSES = new HashMap<String, String>();
		VARIABLES_WHERE_SUBCLAUSES.put("categoryName", "cats.name = :categoryName");
	}
	
	@RequestMapping(value = "/variables", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public @ResponseBody Success postVariables(@RequestBody final ArrayList<Variable> variables) {
		database.variables.put(variables);
		return new Success();
	}
	
	@RequestMapping(value = "/variableCategories", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody List<VariableCategory> getVariableCategories() {
		return database.variableCategories.getAll();
		//return variableCategoryDAO.get();
	}
	
	@RequestMapping(value = "/variableCategories", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public @ResponseBody Success postVariableCategories(@RequestBody final ArrayList<VariableCategory> variableCategories) {
		database.variableCategories.put(variableCategories);
		//variableCategoryDAO.put(variableCategories);
		return new Success();
	}
	
	@RequestMapping(value = "/variableUserSettings", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody List<VariableUserSettings> getVariableUserSettings(
		@RequestParam(required = false) final String variableName
	) {
		if (variableName == null) return database.variableUserSettings.getAll();
		return database.variableUserSettings.get(VARIABLE_USER_SETTINGS_WHERE_SUBCLAUSES, "variableName", variableName);
	}
	public static final Map<String, String> VARIABLE_USER_SETTINGS_WHERE_SUBCLAUSES;
	static {
		VARIABLE_USER_SETTINGS_WHERE_SUBCLAUSES = new HashMap<String, String>();
		VARIABLE_USER_SETTINGS_WHERE_SUBCLAUSES.put("variableName", "vars.name = :variableName");
	}
	
	@RequestMapping(value = "/variableUserSettings", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public @ResponseBody Success postVariableUserSettings(@RequestBody final ArrayList<VariableUserSettings> variableUserSettings) {
		database.variableUserSettings.put(variableUserSettings);
		return new Success();
	}
}
