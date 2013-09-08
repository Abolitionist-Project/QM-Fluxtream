package com.fluxtream.connectors.myfitnesspal;

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

import com.fluxtream.Configuration;
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
@RequestMapping("/myfitnesspal")
public class MyFitnessPalController {

	public static final Logger LOGGER = LoggerFactory.getLogger(MyFitnessPalController.class);

	@Autowired
	GuestService guestService;

	@Autowired
	MyFitnessPalUpdater updater;

	@Qualifier("connectorUpdateServiceImpl")
	@Autowired
	ConnectorUpdateService connectorUpdateService;

	@Qualifier("apiDataServiceImpl")
	@Autowired
	ApiDataService apiDataService;

	@Autowired
	Configuration env;

	@RequestMapping(value = "/enterCredentials")
	public ModelAndView signin(HttpServletRequest request) {
		ModelAndView mav = new ModelAndView("connectors/myfitnesspal/enterCredentials");
		return mav;
	}

	@RequestMapping(value = "/submitCredentials")
	public ModelAndView setupMyFitnessPal(HttpServletRequest request, HttpServletResponse response)
			throws RateLimitReachedException, Exception {
		ModelAndView mav = new ModelAndView();
		String username = request.getParameter(ApiDataService.USERNAME);
		String password = request.getParameter(ApiDataService.PASSWORD);

		List<String> required = new ArrayList<String>();
		if (!StringUtils.hasText(username)) {
			required.add(ApiDataService.USERNAME);
		}
		if (!StringUtils.hasText(password)) {
			required.add(ApiDataService.PASSWORD);
		}

		if (required.size() != 0) {
			request.setAttribute(ApiDataService.USERNAME, username);
			mav.setViewName("connectors/myfitnesspal/enterCredentials");
			mav.addObject("required", required);
			return mav;
		}
		ScrappingConnectorCredentials cred = new ScrappingConnectorCredentials();
		cred.username = username;
		cred.password = password;

		long guestId = AuthHelper.getGuestId();

		Connector connector = Connector.getConnector(ConnectorNames.MYFITNESSPAL);

		try {
			updater.testConnection(cred, guestId);
		} catch (Exception e) {
			mav.setViewName("connectors/myfitnesspal/enterCredentials");
			mav.addObject("errorMessage", e.getMessage());
			return mav;
		}

		apiDataService.updateScrappingConnectorCredentials(guestId, connector, cred);

		connectorUpdateService.updateConnector(guestId, connector, true);

		mav.setViewName("connectors/myfitnesspal/success");
		return mav;
	}

}
