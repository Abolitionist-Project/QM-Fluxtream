package com.fluxtream.api;

import java.util.List;
import java.util.Locale;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fluxtream.auth.AuthHelper;
import com.fluxtream.domain.Guest;
import com.fluxtream.dto.GroupingDto;
import com.fluxtream.dto.UnitDto;
import com.fluxtream.dto.VariableBehaviour;
import com.fluxtream.dto.VariableDto;
import com.fluxtream.dto.VariableType;
import com.fluxtream.mvc.models.StatusModel;
import com.fluxtream.services.CategoriesService;
import com.fluxtream.services.GuestService;
import com.fluxtream.services.VariablesService;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Ordering;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

@Controller
@RequestMapping(value = "/variables")
public class VariablesController {

	private static DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("MMM dd, yyyy").withLocale(
			Locale.ENGLISH);

	private static final Logger LOGGER = LoggerFactory.getLogger(VariablesController.class);

	@Autowired
	VariablesService variablesService;

	@Autowired
	CategoriesService categoriesService;

	@Autowired
	GuestService guestService;

	@GET
	@RequestMapping(value = "/single/all")
	@Produces({ MediaType.APPLICATION_JSON })
	public @ResponseBody
	String getSingleVariables(@RequestParam(value = "behaviour") String behaviour) {
		VariableBehaviour variableBehaviour = VariableBehaviour.resolveType(behaviour);
		if (variableBehaviour == null) {
			LOGGER.warn("Requested /analyze/variables with unexpected type " + variableBehaviour);
			throw new RuntimeException("Used incorrect variable type " + variableBehaviour);
		}
		Guest guest = guestService.getGuestById(AuthHelper.getGuestId());

		List<VariableDto> variables = variablesService.getVariables(guest, variableBehaviour, VariableType.SINGLE);
		return new Gson().toJson(variables);
	}

	@GET
	@RequestMapping(value = "/single/by_si_unit")
	@Produces({ MediaType.APPLICATION_JSON })
	public @ResponseBody
	String getSingleVariables(@RequestParam(value = "behaviour") String behaviour,
			@RequestParam(value = "si_unit") Integer siUnit) {
		VariableBehaviour variableBehaviour = VariableBehaviour.resolveType(behaviour);
		if (variableBehaviour == null) {
			LOGGER.warn("Requested /analyze/variables with unexpected type " + variableBehaviour);
			throw new RuntimeException("Used incorrect variable type " + variableBehaviour);
		}
		Guest guest = guestService.getGuestById(AuthHelper.getGuestId());

		List<VariableDto> variables = variablesService.getVariables(guest, variableBehaviour, VariableType.SINGLE,
				siUnit);
		return new Gson().toJson(variables);
	}

	@GET
	@RequestMapping(value = "/aggregated")
	@Produces({ MediaType.APPLICATION_JSON })
	public @ResponseBody
	String getAggregatedVariables(@RequestParam(value = "behaviour") String behaviour,
			@RequestParam(required = true, value = "category_id") Integer categoryId) {
		VariableBehaviour variableBehaviour = VariableBehaviour.resolveType(behaviour);
		if (variableBehaviour == null) {
			LOGGER.warn("Requested /analyze/variables with unexpected type " + variableBehaviour);
			throw new RuntimeException("Used incorrect variable type " + variableBehaviour);
		}
		Guest guest = guestService.getGuestById(AuthHelper.getGuestId());

		/*
		 * XXX guest is not longer required, see https://github.com/mikepsinn/Quantimodo/issues/197
		 */
		List<VariableDto> variables = variablesService.getVariables(guest, variableBehaviour, categoryId,
				VariableType.AGGREGATED);

		return new Gson().toJson(variables);
	}

	@GET
	@RequestMapping(value = "/aggregated/all")
	@Produces({ MediaType.APPLICATION_JSON })
	public @ResponseBody
	String getAggregatedVariables(@RequestParam(value = "behaviour") String behaviour) {
		VariableBehaviour variableBehaviour = VariableBehaviour.resolveType(behaviour);
		if (variableBehaviour == null) {
			LOGGER.warn("Requested /analyze/variables with unexpected type " + variableBehaviour);
			throw new RuntimeException("Used incorrect variable type " + variableBehaviour);
		}
		Guest guest = guestService.getGuestById(AuthHelper.getGuestId());

		List<VariableDto> variables = variablesService.getVariables(guest, variableBehaviour, VariableType.AGGREGATED);

		return new Gson().toJson(variables);
	}

	// http://localhost:8080/variables/values?app_id=1&app_type=g&var_id=undefined&from=&to=Mar%2022,%202013&grouping=daily
	@GET
	@RequestMapping(value = "/values")
	@Produces({ MediaType.APPLICATION_JSON })
	public @ResponseBody
	String getVariableValues(@RequestParam(value = "var_id") String varId, @RequestParam(value = "from") String from,
			@RequestParam(value = "to") String to, @RequestParam(value = "grouping") Integer groupingValue) {
		if (varId == null) {
			LOGGER.warn("Requested /analyze/variables_values without var_id");
			throw new RuntimeException("Specify variable id");
		}
		Guest guest = guestService.getGuestById(AuthHelper.getGuestId());
		// Mar 15, 2013
		DateTime fromTime;
		if (StringUtils.hasText(from)) {
			fromTime = dateTimeFormatter.parseDateTime(from);
		} else {
			fromTime = new DateTime(0);
		}
		DateTime toTime;
		if (StringUtils.hasText(to)) {
			toTime = dateTimeFormatter.parseDateTime(to);
		} else {
			toTime = new DateTime();
		}

		GroupingDto grouping = GroupingDto.resolveByValue(groupingValue);
		if (grouping == null) {
			LOGGER.warn("Requested /analyze/variables_values without grouping");
			throw new RuntimeException("Specify grouping");
		}

		VariableDto variableDto = getFullVariableInfoJson(Integer.valueOf(varId), guest, fromTime, toTime, grouping);
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

		return gson.toJson(variableDto);
	}

	private VariableDto getFullVariableInfoJson(Integer varId, Guest guest, DateTime fromTime, DateTime toTime,
			GroupingDto grouping) {
		VariableDto fullVariableInfo = variablesService.getVariableValues(guest, varId, fromTime, toTime, grouping);
		fullVariableInfo.setGroupedValues(ImmutableSortedMap.copyOf(fullVariableInfo.getGroupedValues(),
				Ordering.natural()));
		return fullVariableInfo;
	}

	@RequestMapping(value = "/variable/{id}", method = RequestMethod.GET)
	@Produces({ MediaType.APPLICATION_JSON })
	public @ResponseBody
	String getVariable(@PathVariable(value = "id") Integer varId) {
		Guest guest = guestService.getGuestById(AuthHelper.getGuestId());
		return new Gson().toJson(variablesService.getVariable(guest, varId));
	}

	@RequestMapping(value = "/variable/{id}", method = RequestMethod.DELETE)
	@Produces({ MediaType.APPLICATION_JSON })
	public @ResponseBody
	String deleteVariable(@PathVariable(value = "id") Integer varId) {
		Guest guest = guestService.getGuestById(AuthHelper.getGuestId());
		Gson gson = new Gson();
		JsonObject jsonObject = new JsonObject();
		try {
			variablesService.deleteVariable(guest, varId);
			StatusModel result = new StatusModel(true, "Successfully removed variable!");

			jsonObject.add("status_model", gson.toJsonTree(result));
			jsonObject.add("id", new JsonPrimitive(varId));
			return gson.toJson(jsonObject);
		} catch (DataAccessException e) {
			LOGGER.warn("Error occured while deleting variable", e);
			StatusModel result = new StatusModel(false, "This name is used already, please, enter another");
			jsonObject.add("status_model", gson.toJsonTree(result));
			return gson.toJson(jsonObject);
		}
	}

	@POST
	@RequestMapping(value = "/variable", headers = { "Accept=application/json" })
	@Produces({ MediaType.APPLICATION_JSON })
	public @ResponseBody
	String saveVariable(@RequestBody VariableDto variableDto) {
		Guest guest = guestService.getGuestById(AuthHelper.getGuestId());
		Gson gson = new Gson();
		JsonObject jsonObject = new JsonObject();
		try {
			Integer savedId = variablesService.saveVariable(guest, variableDto);
			StatusModel result = new StatusModel(true, "Successfully added new variable!");

			jsonObject.add("status_model", gson.toJsonTree(result));
			jsonObject.add("id", new JsonPrimitive(savedId));
			return gson.toJson(jsonObject);
		} catch (DataAccessException e) {
			LOGGER.warn("Error occured while saving new variable", e);
			StatusModel result = new StatusModel(false, "This name is used already, please, enter another");
			jsonObject.add("status_model", gson.toJsonTree(result));
			return gson.toJson(jsonObject);
		}
	}

	@GET
	@RequestMapping(value = "/units")
	@Produces({ MediaType.APPLICATION_JSON })
	public @ResponseBody
	String getUnits() {
		List<UnitDto> unitTypes = variablesService.getUnits();
		return new Gson().toJson(unitTypes);
	}

	@GET
	@RequestMapping(value = "/time_shift_units")
	@Produces({ MediaType.APPLICATION_JSON })
	public @ResponseBody
	String getTimeShiftUnits() {
		List<UnitDto> unitTypes = variablesService.getTimeShiftUnits();
		return new Gson().toJson(unitTypes);
	}

	@GET
	@RequestMapping(value = "/non_empty_measures")
	@Produces({ MediaType.APPLICATION_JSON })
	public @ResponseBody
	String getVariableNonEmptyMeasures() {
		List<GroupingDto> nonEmptyMeasures = variablesService.getVariableNonEmptyMeasures();
		return new Gson().toJson(nonEmptyMeasures);
	}

}
