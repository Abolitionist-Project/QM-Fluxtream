package com.fluxtream.connectors.runkeeper;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fluxtream.Configuration;
import com.fluxtream.auth.AuthHelper;
import com.fluxtream.connectors.Connector;
import com.fluxtream.domain.Guest;
import com.fluxtream.services.GuestService;

/**
 * 
 * @author Candide Kemmler (candide@fluxtream.com)
 */
@Controller
@RequestMapping(value = "/runkeeper")
public class RunKeeperController {

	private static final String RUNKEEPER_SERVICE = "runkeeperService";

	@Autowired
	Configuration env;

	@Autowired
	GuestService guestService;

	private static final Token EMPTY_TOKEN = null;

	@RequestMapping(value = "/token")
	public String getRunkeeperToken(HttpServletRequest request) throws IOException, ServletException {

		OAuthService service = getOAuthService();
		request.getSession().setAttribute(RUNKEEPER_SERVICE, service);

		// Obtain the Authorization URL
		String authorizationUrl = service.getAuthorizationUrl(EMPTY_TOKEN);

		return "redirect:" + authorizationUrl;
	}

	// TODO: there is no RunKeeperApi class. consider how to fix this method
	public OAuthService getOAuthService() {
		// return new ServiceBuilder()
		// .provider(RunKeeperApi.class)
		// .apiKey(getConsumerKey())
		// .apiSecret(getConsumerSecret())
		// .callback(env.get("homeBaseUrl") + "runkeeper/upgradeToken")
		// .build();
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@RequestMapping(value = "/upgradeToken")
	public String upgradeToken(HttpServletRequest request) throws IOException {
		final String code = request.getParameter("code");
		Verifier verifier = new Verifier(code);
		OAuthService service = (OAuthService) request.getSession().getAttribute(RUNKEEPER_SERVICE);

		Token accessToken = service.getAccessToken(EMPTY_TOKEN, verifier);
		final String token = accessToken.getToken();

		Guest guest = AuthHelper.getGuest();

		guestService.setApiKeyAttribute(guest.getId(), Connector.getConnector("runkeeper"), "accessToken", token);

		request.getSession().removeAttribute(RUNKEEPER_SERVICE);
		return "redirect:/analyze/from/runkeeper";
	}

	String getConsumerKey() {
		return env.get("runkeeperConsumerKey");
	}

	String getConsumerSecret() {
		return env.get("runkeeperConsumerSecret");
	}

}
