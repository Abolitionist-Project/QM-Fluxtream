package com.fluxtream.connectors.healthvault;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.xml.sax.InputSource;

import com.fluxtream.Configuration;
import com.fluxtream.auth.AuthHelper;
import com.fluxtream.connectors.Connector;
import com.fluxtream.domain.Guest;
import com.fluxtream.services.GuestService;
import com.fluxtream.services.SystemService;
import com.microsoft.hsg.ApplicationConfig;
import com.microsoft.hsg.ConnectionFactory;
import com.microsoft.hsg.HVAccessor;
import com.microsoft.hsg.HVException;
import com.microsoft.hsg.Request;

@Controller
@RequestMapping(value = "/healthvault")
public class HealthVaultConnectorController {

	Logger logger = Logger.getLogger(HealthVaultConnectorController.class);

	@Autowired
	Configuration env;

	@Autowired
	SystemService systemService;

	@Autowired
	GuestService guestService;

	@RequestMapping(value = "/token")
	public String getToken(HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {

		StringBuffer url = new StringBuffer();
		url.append(ApplicationConfig.HV_SHELL);
		url.append("/redirect.aspx?target=AUTH&targetqs=");
		url.append(URLEncoder.encode("?appid=", "UTF-8"));
		url.append(URLEncoder.encode(ApplicationConfig.APP_ID, "UTF-8"));
//		url.append(URLEncoder.encode("&redirect=", "UTF-8"));
//		url.append(URLEncoder.encode(env.get("homeBaseUrl") + "healthvault/upgradeToken", "UTF-8"));

		
		
		return "redirect:" + url.toString();
	}

	@RequestMapping(value = "/upgradeToken")
	public ModelAndView upgradeToken(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		
		if (request.getParameter("target") == "AppAuthSuccess") {
			
			String wcToken = request.getParameter("wctoken");			
			String recordId = getSelectedRecordId(wcToken);			

			if (!recordId.equals("")) {
				Connector connector = Connector.getConnector("healthvault");
				Guest guest = AuthHelper.getGuest();

				guestService.setApiKeyAttribute(guest.getId(), connector, "personId", wcToken);
				guestService.setApiKeyAttribute(guest.getId(), connector, "recordId", recordId);

				return new ModelAndView("redirect:/analyze/from/" + connector.getName());
			}
		

//		ModelAndView mav = new ModelAndView("error");
//		mav.addObject("errorMessage", errorMessage);
		
		}

		return new ModelAndView();
	}

	private String getSelectedRecordId(String userAuthToken) throws HVException {
		try {
			Request request = new Request();
			request.setTtl(3600 * 8 + 300);
			request.setMethodName("GetPersonInfo");
			request.setUserAuthToken(userAuthToken);
			HVAccessor accessor = new HVAccessor();
			accessor.send(request, ConnectionFactory.getConnection());
			InputStream is = accessor.getResponse().getInputStream();
			
			StringWriter writer = new StringWriter();
			IOUtils.copy(is, writer, "UTF-8");
			logger.info("this the xml thing holger: " + writer.toString());
			
			XPath xpath = XPathFactory.newInstance().newXPath();
			String exp = "//record/@id";			
			return xpath.evaluate(exp, new InputSource(is));
			
		} catch (HVException he) {
			throw he;
		} catch (Exception e) {
			throw new HVException(e);
		}
	}

}
