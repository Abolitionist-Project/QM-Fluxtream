package com.fluxtream.connectors.google_calendar;

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
import com.google.api.services.calendar.CalendarScopes;
import com.google.gdata.client.authn.oauth.GoogleOAuthHelper;
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;

@Controller
@RequestMapping(value = "/calendar")
public class GoogleCalendarOAuthController extends BaseGoogleOAuthController {

	@Autowired
	Configuration env;

	@Autowired
	GuestService guestService;

	@RequestMapping(value = "/token")
	public String getLatitudeToken(HttpServletRequest request, HttpServletResponse response) throws IOException,
			ServletException {

		String authUrl = buildAuthUrl(request, Arrays.asList(CalendarScopes.CALENDAR));
		return "redirect:" + authUrl;
	}

	@Override
	protected String buildRedirectUrl() {
		return env.get("homeBaseUrl") + "calendar/upgradeToken";
	}

	@RequestMapping(value = "/upgradeToken")
	public String upgradeToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
		TokenInfo tokenInfo = retrieveTokenInfo(request, Arrays.asList(CalendarScopes.CALENDAR));
		Connector connector = Connector.getConnector(ConnectorNames.GOOGLE_CALENDAR);

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

	protected GoogleOAuthHelper getOAuthHelper() {
		return new GoogleOAuthHelper(new OAuthHmacSha1Signer());
	}

}
