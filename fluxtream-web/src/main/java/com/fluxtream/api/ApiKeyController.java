package com.fluxtream.api;

import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fluxtream.auth.AuthHelper;
import com.fluxtream.connectors.Connector;
import com.fluxtream.domain.ApiKey;
import com.fluxtream.domain.Guest;
import com.fluxtream.mvc.models.StatusModel;
import com.fluxtream.services.ApiDataService;
import com.fluxtream.services.ApiDataService.ScrappingConnectorCredentials;
import com.fluxtream.services.GuestService;
import com.google.gson.Gson;

@Controller
public class ApiKeyController {

	@Autowired
	GuestService guestService;

	@Qualifier("apiDataServiceImpl")
	@Autowired
	ApiDataService apiDataService;

	@POST
	@RequestMapping(value = "/api_key/update")
	@Produces({ MediaType.APPLICATION_JSON })
	public @ResponseBody
	String saveConnectorSettings(@RequestParam("connector") String connectorName,
			@RequestParam("enabled") String enabled,
			@RequestParam("hourly_update_interval") String hourlyUpdateInterval,
			@RequestParam("username") String username, @RequestParam("password") String password) {
		Guest guest = AuthHelper.getGuest();
		if (guest == null) {
			throw new RuntimeException("Cannot find logged in user");
		}
		ApiKey apiKey = guestService.getApiKey(guest.getId(), Connector.getConnector(connectorName));
		if (enabled != null) {
			apiKey.enabled = Boolean.valueOf(enabled);
		}
		if (hourlyUpdateInterval != null) {
			apiKey.hourlyUpdateInterval = Integer.parseInt(hourlyUpdateInterval);
		}
		guestService.updateApiKey(apiKey);

		if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
			ScrappingConnectorCredentials cred = new ScrappingConnectorCredentials();
			cred.username = username;
			cred.password = password;
			apiDataService.updateScrappingConnectorCredentials(guest.getId(), apiKey.getConnector(), cred);
		}

		StatusModel result = new StatusModel(true, "Successfully updated api key state!");
		return new Gson().toJson(result);
	}

}
