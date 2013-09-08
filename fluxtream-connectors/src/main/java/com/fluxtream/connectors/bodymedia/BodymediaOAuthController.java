package com.fluxtream.connectors.bodymedia;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import oauth.signpost.http.HttpParameters;

import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fluxtream.Configuration;
import com.fluxtream.auth.AuthHelper;
import com.fluxtream.connectors.Connector;
import com.fluxtream.connectors.ConnectorNames;
import com.fluxtream.connectors.updaters.UpdateInfo;
import com.fluxtream.domain.Guest;
import com.fluxtream.services.GuestService;

@Controller
@RequestMapping(value = "/bodymedia")
public class BodymediaOAuthController {

	private static final Logger LOG = LoggerFactory.getLogger(BodymediaOAuthController.class);

	@Autowired
	GuestService guestService;

	@Autowired
	Configuration env;

	private static final String BODYMEDIA_OAUTH_CONSUMER = "bodymediaOAuthConsumer";
	private static final String BODYMEDIA_OAUTH_PROVIDER = "bodymediaOAuthProvider";

	@RequestMapping(value = "/token")
	public String getToken(HttpServletRequest request) throws IOException, ServletException,
			OAuthMessageSignerException, OAuthNotAuthorizedException, OAuthExpectationFailedException,
			OAuthCommunicationException {

		String oauthCallback = env.getHomeBaseUrl() + "bodymedia/upgradeToken";
		if (request.getParameter("guestId") != null)
			oauthCallback += "?guestId=" + request.getParameter("guestId");

		String apiKey = env.get("bodymediaConsumerKey");

		OAuthConsumer consumer = buildConsumer(apiKey);
		OAuthProvider provider = buildProvider(apiKey);

		request.getSession().setAttribute(BODYMEDIA_OAUTH_CONSUMER, consumer);
		request.getSession().setAttribute(BODYMEDIA_OAUTH_PROVIDER, provider);

		String approvalPageUrl = provider.retrieveRequestToken(consumer, oauthCallback) + "&oauth_api=" + apiKey;

		// approvalPageUrl = URLDecoder.decode(approvalPageUrl, "UTF-8");

		if (LOG.isDebugEnabled()) {
			LOG.debug("The approval page url is: " + approvalPageUrl);
		}

		return "redirect:" + approvalPageUrl;
	}

	@RequestMapping(value = "/upgradeToken")
	public String upgradeToken(HttpServletRequest request) throws NoSuchAlgorithmException, IOException,
			OAuthMessageSignerException, OAuthNotAuthorizedException, OAuthExpectationFailedException,
			OAuthCommunicationException {

		OAuthConsumer consumer = (OAuthConsumer) request.getSession().getAttribute(BODYMEDIA_OAUTH_CONSUMER);
		OAuthProvider provider = (OAuthProvider) request.getSession().getAttribute(BODYMEDIA_OAUTH_PROVIDER);
		String verifier = request.getParameter("oauth_verifier");
		provider.retrieveAccessToken(consumer, verifier);
		Guest guest = AuthHelper.getGuest();

		guestService.setApiKeyAttribute(guest.getId(), getConnector(), "api_key", env.get("bodymediaConsumerKey"));
		guestService.setApiKeyAttribute(guest.getId(), getConnector(), "accessToken", consumer.getToken());
		guestService.setApiKeyAttribute(guest.getId(), getConnector(), "tokenSecret", consumer.getTokenSecret());
		guestService.setApiKeyAttribute(guest.getId(), getConnector(), "tokenExpiration", provider
				.getResponseParameters().get("xoauth_token_expiration_time").first());

		return "redirect:/analyze/from/" + getConnector().getName();
	}

	public void replaceToken(UpdateInfo updateInfo) throws OAuthExpectationFailedException,
			OAuthMessageSignerException, OAuthCommunicationException, OAuthNotAuthorizedException {
		String apiKey = guestService.getApiKeyAttribute(updateInfo.getGuestId(), getConnector(), "api_key");
		OAuthConsumer consumer = new DefaultOAuthConsumer(apiKey, env.get("bodymediaConsumerSecret"));
		String accessToken = guestService.getApiKeyAttribute(updateInfo.getGuestId(), getConnector(), "accessToken");
		consumer.setTokenWithSecret(accessToken,
				guestService.getApiKeyAttribute(updateInfo.getGuestId(), getConnector(), "tokenSecret"));
		HttpParameters additionalParameter = new HttpParameters();
		additionalParameter.put("api_key", apiKey);
		additionalParameter.put("oauth_token", accessToken);
		consumer.setAdditionalParameters(additionalParameter);

		OAuthProvider provider = buildProvider(apiKey);

		provider.retrieveAccessToken(consumer, null);

		guestService.setApiKeyAttribute(updateInfo.getGuestId(), getConnector(), "api_key",
				env.get("bodymediaConsumerKey"));
		guestService.setApiKeyAttribute(updateInfo.getGuestId(), getConnector(), "accessToken", consumer.getToken());
		guestService.setApiKeyAttribute(updateInfo.getGuestId(), getConnector(), "tokenSecret",
				consumer.getTokenSecret());
		guestService.setApiKeyAttribute(updateInfo.getGuestId(), getConnector(), "tokenExpiration", provider
				.getResponseParameters().get("xoauth_token_expiration_time").first());
	}

	private OAuthProvider buildProvider(String apiKey) {
		HttpClient httpClient = env.getHttpClient();

		OAuthProvider provider = new CommonsHttpOAuthProvider("https://api.bodymedia.com/oauth/request_token?api_key="
				+ apiKey, "https://api.bodymedia.com/oauth/access_token?api_key=" + apiKey,
				"https://api.bodymedia.com/oauth/authorize?api_key=" + apiKey, httpClient);
		return provider;
	}

	private OAuthConsumer buildConsumer(String apiKey) {
		OAuthConsumer consumer = new DefaultOAuthConsumer(apiKey, env.get("bodymediaConsumerSecret"));
		HttpParameters additionalParameter = new HttpParameters();
		additionalParameter.put("api_key", apiKey);
		consumer.setAdditionalParameters(additionalParameter);
		return consumer;
	}

	private Connector getConnector() {
		return Connector.getConnector(ConnectorNames.BODYMEDIA);
	}

}
