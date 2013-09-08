package com.fluxtream.connectors.google_latitude;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fluxtream.Configuration;
import com.fluxtream.auth.AuthHelper;
import com.fluxtream.connectors.Connector;
import com.fluxtream.connectors.ConnectorNames;
import com.fluxtream.connectors.controllers.BaseGoogleOAuthController;
import com.fluxtream.domain.Guest;
import com.fluxtream.services.GuestService;
import com.google.api.services.latitude.LatitudeScopes;

@Controller
@RequestMapping(value = "/google_latitude")
public class GoogleLatitudeOAuthController extends BaseGoogleOAuthController {

	@Autowired
	Configuration env;

	@Autowired
	GuestService guestService;

	@RequestMapping(value = "/token")
	public String getLatitudeToken(HttpServletRequest request, HttpServletResponse response) throws IOException,
			ServletException {

		String authUrl = buildAuthUrl(request, Arrays.asList(LatitudeScopes.LATITUDE_ALL_BEST));
		return "redirect:" + authUrl;

	}

	@Override
	protected String buildRedirectUrl() {
		return env.get("homeBaseUrl") + "google_latitude/upgradeToken";
	}

	@RequestMapping(value = "/upgradeToken")
	public String upgradeToken(HttpServletRequest request, HttpServletResponse response) throws IOException {

		TokenInfo tokenInfo = retrieveTokenInfo(request, Arrays.asList(LatitudeScopes.LATITUDE_ALL_BEST));
		Connector connector = Connector.getConnector(ConnectorNames.GOOGLE_LATITUDE);

		Guest guest = AuthHelper.getGuest();
		saveTokenInfo(guestService, tokenInfo, connector, guest);

		return "redirect:/analyze/from/" + connector.getName();
	}

	@Override
	protected GuestService guestService() {
		return guestService;
	}

	@Override
	protected String getConsumerKey() {
		return env.get("googleConsumerKey");
	}

	@Override
	protected String getConsumerSecret() {
		return env.get("googleConsumerSecret");
	}

}
