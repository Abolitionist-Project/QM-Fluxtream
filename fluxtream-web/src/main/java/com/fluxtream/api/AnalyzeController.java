package com.fluxtream.api;

import java.util.Locale;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fluxtream.Configuration;
import com.fluxtream.auth.AuthHelper;
import com.fluxtream.domain.Guest;
import com.fluxtream.dto.GroupingDto;
import com.fluxtream.dto.VariableBehaviour;
import com.fluxtream.dto.VariableSettingsDto;
import com.fluxtream.dto.VariableType;
import com.fluxtream.mvc.models.StatusModel;
import com.fluxtream.services.AnalyzeService;
import com.fluxtream.services.EmailService;
import com.fluxtream.services.GuestService;
import com.fluxtream.utils.Utils;
import com.google.common.collect.ImmutableSortedMap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

@Controller
public class AnalyzeController {

	private static final Logger LOG = Logger.getLogger(AnalyzeController.class);

	private static DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("MMM dd, yyyy").withLocale(
			Locale.ENGLISH);

	@Autowired
	private AnalyzeService analyzeService;

	@Autowired
	private GuestService guestService;

	@Autowired
	EmailService emailService;

	@Autowired
	Configuration env;

	@GET
	@RequestMapping(value = "/analyze/applications")
	@Produces({ MediaType.APPLICATION_JSON })
	public @ResponseBody
	String getApplications(@RequestParam(required = true, value = "type") String typeName) {
		if (typeName == null) {
			LOG.warn("Requested /analyze/applications without type");
			throw new RuntimeException("Specify application type (input or output)");
		}
		VariableBehaviour type = VariableBehaviour.resolveType(typeName);
		if (type == null) {
			LOG.warn("Requested /analyze/applications with unexpected type " + type);
			throw new RuntimeException("Used incorrect application type " + type);
		}
		Map<Integer, String> applications = analyzeService.getApplications(type);

		ImmutableSortedMap<Integer, String> sortedMap = Utils.toSortedMap(applications);
		JsonArray jsonArray = new JsonArray();
		for (Integer key : sortedMap.keySet()) {
			JsonArray element = new JsonArray();
			element.add(new JsonPrimitive(key));
			element.add(new JsonPrimitive(sortedMap.get(key)));
			jsonArray.add(element);
		}
		return jsonArray.toString();
	}

	@GET
	@RequestMapping(value = "/analyze/variables_settings/get")
	@Produces({ MediaType.APPLICATION_JSON })
	public @ResponseBody
	String getVariableSettings() {
		Guest guest = guestService.getGuestById(AuthHelper.getGuestId());
		VariableSettingsDto variableSettings = analyzeService.findVariableSettings(guest);
		return new Gson().toJson(variableSettings);
	}

	@POST
	@RequestMapping(value = "/analyze/variables_settings/save")
	@Produces({ MediaType.APPLICATION_JSON })
	public @ResponseBody
	String saveVariableSettings(@RequestParam(required = true, value = "input_type") String inputType,
			@RequestParam(required = true, value = "input_id") Integer inputId,
			@RequestParam(required = true, value = "output_type") String outputType,
			@RequestParam(required = true, value = "output_id") Integer outputId,
			/*@RequestParam(value = "from") String from, @RequestParam(value = "to") String to,*/
			@RequestParam(value = "grouping") Integer groupingValue) {
		Guest guest = guestService.getGuestById(AuthHelper.getGuestId());
		//DateTime fromTime;
		//if (StringUtils.hasText(from)) {
		//	fromTime = dateTimeFormatter.parseDateTime(from);
		//} else {
		//	fromTime = new DateTime(0);
		//}
		//DateTime toTime;
		//if (StringUtils.hasText(to)) {
		//	toTime = dateTimeFormatter.parseDateTime(to);
		//} else {
		//	toTime = new DateTime();
		//}

		VariableType inputApplicationType = VariableType.resolveType(inputType);
		if (inputApplicationType == null) {
			LOG.warn("Requested /analyze/variables_settings/save without input_type");
			throw new RuntimeException("Specify appType");
		}

		VariableType outputApplicationType = VariableType.resolveType(outputType);
		if (outputApplicationType == null) {
			LOG.warn("Requested /analyze/variables_values without output_type");
			throw new RuntimeException("Specify appType");
		}
		GroupingDto grouping = GroupingDto.resolveByValue(groupingValue);
		if (grouping == null) {
			LOG.warn("Requested /analyze/variables_settings/save without grouping");
			throw new RuntimeException("Specify grouping");
		}

		analyzeService.saveVariableSettings(guest, inputApplicationType, inputId, outputApplicationType, outputId,
				grouping/*, fromTime, toTime*/);
		StatusModel result = new StatusModel(true, "Successfully saved guest variable settings!");
		return new Gson().toJson(result);
	}

	@POST
	@RequestMapping(value = "/analyze/feedback/send")
	@Produces({ MediaType.APPLICATION_JSON })
	public @ResponseBody
	String sendFeedback(@RequestParam("feedback") String feedback) {
		Guest guest = guestService.getGuestById(AuthHelper.getGuestId());
		DateTime dateTime = new DateTime();
		String datePart = dateTime.getMonthOfYear() + "/" + dateTime.getDayOfMonth() + "-" + dateTime.getMonthOfYear()
				+ "/" + dateTime.getDayOfMonth();
		String wrikeSubject = env.get("email.wrike.feedback.path") + "::" + feedback + " [" + datePart + "]";
		String[] ccEmails = env.get("email.wrike.feedback.cc").split(";");
		emailService.sendEmail(guest.email, env.get("email.wrike.feedback.to"), wrikeSubject, "", ccEmails);
		StatusModel result = new StatusModel(true, "Feedback was successfuly sent!");
		return new Gson().toJson(result);

	}
}
