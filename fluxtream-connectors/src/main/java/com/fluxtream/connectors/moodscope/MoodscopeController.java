package com.fluxtream.connectors.moodscope;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.fluxtream.auth.AuthHelper;
import com.fluxtream.connectors.Connector;
import com.fluxtream.connectors.ConnectorNames;
import com.fluxtream.connectors.updaters.RateLimitReachedException;
import com.fluxtream.services.ApiDataService;
import com.fluxtream.services.ApiDataService.ScrappingConnectorCredentials;
import com.fluxtream.services.ConnectorUpdateService;
import com.fluxtream.services.GuestService;

/**
 * @author alucab
 * 
 */

@Controller
@RequestMapping("/moodscope")
public class MoodscopeController {

	public static final Logger LOGGER = LoggerFactory.getLogger(MoodscopeController.class);

	@Autowired
	GuestService guestService;

	@Autowired
	MoodscopeUpdater updater;

	@Qualifier("connectorUpdateServiceImpl")
	@Autowired
	ConnectorUpdateService connectorUpdateService;

	@Qualifier("apiDataServiceImpl")
	@Autowired
	ApiDataService apiDataService;

	@RequestMapping(value = "/enterCredentials")
	public ModelAndView signin(HttpServletRequest request) {
		ModelAndView mav = new ModelAndView("connectors/moodscope/enterCredentials");
		return mav;
	}

	@RequestMapping(value = "/submitCredentials")
	public ModelAndView setupMoodscope(HttpServletRequest request, HttpServletResponse response)
			throws RateLimitReachedException, Exception {
		ModelAndView mav = new ModelAndView();
		String email = request.getParameter(ApiDataService.USERNAME);
		String password = request.getParameter(ApiDataService.PASSWORD);

		List<String> required = new ArrayList<String>();
		if (!StringUtils.hasText(email)) {
			required.add(ApiDataService.USERNAME);
		}
		if (!StringUtils.hasText(password)) {
			required.add(ApiDataService.PASSWORD);
		}

		if (required.size() != 0) {
			request.setAttribute(ApiDataService.USERNAME, email);
			mav.setViewName("connectors/moodscope/enterCredentials");
			mav.addObject("required", required);
			return mav;
		}

		ScrappingConnectorCredentials cred = new ScrappingConnectorCredentials();
		cred.username = email;
		cred.password = password;

		try {
			updater.testConnectionAndCredentials(cred);
		} catch (Exception e) {
			mav.setViewName("connectors/moodscope/enterCredentials");
			mav.addObject("errorMessage", e.getMessage());
			return mav;
		}

		long guestId = AuthHelper.getGuestId();

		Connector connector = Connector.getConnector(ConnectorNames.MOODSCOPE);


		apiDataService.updateScrappingConnectorCredentials(guestId, connector, cred);

		connectorUpdateService.updateConnector(AuthHelper.getGuestId(), connector, true);

		mav.setViewName("connectors/moodscope/success");
		return mav;
	}

}
