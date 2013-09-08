package com.fluxtream.api;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.brickred.socialauth.AuthProvider;
import org.brickred.socialauth.Permission;
import org.brickred.socialauth.Profile;
import org.brickred.socialauth.SocialAuthManager;
import org.brickred.socialauth.spring.bean.SocialAuthTemplate;
import org.brickred.socialauth.util.SocialAuthUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.fluxtream.Configuration;
import com.fluxtream.domain.Guest;
import com.fluxtream.services.GuestService;
import com.mysql.jdbc.StringUtils;

@Controller
@RequestMapping(value = "/oauth")
public class OauthController {

	private static Logger LOG = LoggerFactory.getLogger(OauthController.class);

	@Autowired
	private GuestService guestService;

	private String redirectUri;

	@Autowired
	Configuration env;

	private static final String GOOGLE_PROVIDER = "google";

	@Autowired
	private SocialAuthTemplate socialAuthTemplate;
	@Autowired
	private SocialAuthManager socialAuthManager;

	@RequestMapping(value = "/token")
	public String getToken(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("provider") String providerName) throws Exception {

		String redirectUri = buildRedirectUri();

		String authenticationUrl = null;
		if (GOOGLE_PROVIDER.equals(providerName)) {
			authenticationUrl = socialAuthManager.getAuthenticationUrl(providerName, redirectUri,
					Permission.AUTHENTICATE_ONLY);
		} else {
			authenticationUrl = socialAuthManager.getAuthenticationUrl(providerName, redirectUri);
		}
		socialAuthTemplate.setSocialAuthManager(socialAuthManager);
		return "redirect:" + authenticationUrl;
	}

	private String buildRedirectUri() throws UnsupportedEncodingException {
		String redirectUri = env.getHomeBaseUrl();

		if (!redirectUri.endsWith("/")) {
			redirectUri += "/";
		}
		redirectUri += "oauth/upgradeToken";
		return redirectUri;
	}

	@RequestMapping(value = "/upgradeToken")
	public ModelAndView upgradeToken(HttpServletRequest request, HttpServletResponse response) throws Exception {
		AuthProvider provider = null;
		Profile userProfile = null;

		try {
			provider = socialAuthTemplate.getSocialAuthManager().connect(
					SocialAuthUtil.getRequestParametersMap(request));
			userProfile = provider.getUserProfile();
			LOG.debug("Connected Provider : " + provider.getProviderId());
		} catch (Exception e) {
			LOG.debug("Error occured while getting user info from provider : " + provider.getProviderId(), e);
			throw e;
		}

		if (userProfile == null) {
			throw new RuntimeException("Can't get user profile for provider " + provider.getProviderId());
		}

		Guest guestByOauthCredentinals = guestService.getGuestByOauthCredentinals(userProfile.getValidatedId(),
				userProfile.getProviderId());
		if (guestByOauthCredentinals != null) {
			String password = UUID.randomUUID().toString();
			guestService.setPassword(guestByOauthCredentinals.getId(), password);

			request.getSession().setAttribute("username", guestByOauthCredentinals.username);
			request.getSession().setAttribute("password", password);

			request.getSession().setAttribute("userProfile", null);
			return new ModelAndView("index");
		}

		if (userProfile.getDisplayName() == null) {
			fixDisplayName(userProfile);
		}

		request.getSession().setAttribute("userProfile", userProfile);
		return new ModelAndView("index");

	}

	private void fixDisplayName(Profile userProfile) {
		String newDisplayName;
		if (userProfile.getFullName() != null) {
			newDisplayName = userProfile.getFullName();
		} else {
			newDisplayName = userProfile.getFirstName() != null ? userProfile.getFirstName() : "";
			newDisplayName += userProfile.getLastName() != null ? " " + userProfile.getLastName() : "";
		}
		userProfile.setDisplayName(newDisplayName);
	}

	@RequestMapping("/verifyOauthAccount")
	public String verifyOauthAccount() {
		return "verifyOauthAccount";
	}

	@RequestMapping(value = "/accountVerified")
	public ModelAndView oauthAccountVerified(@RequestParam("email") String email,
			@RequestParam(value = "firstname", required = false) String firstname,
			@RequestParam(value = "lastname", required = false) String lastname,
			@RequestParam("providerId") String providerId, @RequestParam("validatedId") String validatedId,
			HttpServletRequest request, HttpServletResponse response) throws Exception {

		List<String> required = new ArrayList<String>();
		List<String> errors = new ArrayList<String>();
		if (StringUtils.isEmptyOrWhitespaceOnly(email))
			required.add("email");
		else if (guestService.getGuest(email) != null) {
			errors.add("userExists");
		}

		email = email.trim();

		if (errors.size() != 0 || required.size() != 0) {
			ModelAndView mav = new ModelAndView("verifyOauthAccount");

			Profile userProfile = new Profile();

			userProfile.setDisplayName(email);
			userProfile.setEmail(email);
			userProfile.setFirstName(firstname);
			userProfile.setLastName(lastname);
			userProfile.setProviderId(providerId);
			userProfile.setValidatedId(validatedId);
			request.getSession().setAttribute("userProfile", userProfile);
			mav.addObject("errors", errors);
			mav.addObject("required", required);
			return mav;
		}
		// create new Guest using oauth data
		String password = UUID.randomUUID().toString();
		guestService.createGuest(email, firstname, lastname, password, email, validatedId, providerId);

		request.getSession().setAttribute("email", email);
		request.getSession().setAttribute("password", password);

		return new ModelAndView("verifyOauthAccount");
	}

	public String getRedirectUri() throws UnsupportedEncodingException {
		if (redirectUri == null) {
			redirectUri = buildRedirectUri();
		}
		return redirectUri;
	}

}
