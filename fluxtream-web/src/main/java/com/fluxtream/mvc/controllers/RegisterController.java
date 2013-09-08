package com.fluxtream.mvc.controllers;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.fluxtream.Configuration;
import com.fluxtream.services.GuestService;
import com.mysql.jdbc.StringUtils;

@Controller
public class RegisterController {

	Logger logger = Logger.getLogger(RegisterController.class);

	@Qualifier("authenticationManager")
	AuthenticationManager authenticationManager;

	@Autowired
	GuestService guestService;

	@RequestMapping("/createAccountForm")
	public String createAccountForm() {
		return "createAccountForm";
	}

	@RequestMapping("/register")
	public String register() {
		return "register";
	}

	@RequestMapping("/login")
	public String createLoginPage() {
		return "login";
	}

	@RequestMapping("/createAccount")
	public ModelAndView createAccount(@RequestParam(value = "email") String email,
			@RequestParam(value = "firstname", required = false) String firstname,
			@RequestParam(value = "lastname", required = false) String lastname,
			@RequestParam("password1") String password, @RequestParam("password2") String password2,
			HttpServletRequest request, HttpServletResponse response) throws Exception {

		List<String> required = new ArrayList<String>();
		List<String> errors = new ArrayList<String>();
		if (StringUtils.isEmptyOrWhitespaceOnly(email)) {
			required.add("email");
		}
		if (StringUtils.isEmptyOrWhitespaceOnly(password)) {
			required.add("password");
		}
		if (StringUtils.isEmptyOrWhitespaceOnly(password2)) {
			required.add("password2");
		}
		if (password.length() < Configuration.PASSWORD_MIN_LENGTH) {
			errors.add("passwordTooShort");
		}
		if (!password.equals(password2)) {
			errors.add("passwordsDontMatch");
		}
		if (guestService.getGuest(email) != null) {
			errors.add("userExists");
		}

		if (errors.size() != 0 || required.size() != 0) {
			logger.info("action=register errors=true");
			ModelAndView mav = new ModelAndView("createAccountForm");
			mav.addObject("email", email);
			mav.addObject("firstname", firstname);
			mav.addObject("lastname", lastname);
			mav.addObject("errors", errors);
			mav.addObject("required", required);
			return mav;
		}

		email = email.trim();

		logger.info("action=register success=true email=" + email);
		guestService.createGuest(email, firstname, lastname, password, email);
		request.getSession().setAttribute("email", email);
		request.getSession().setAttribute("password", password);
		return new ModelAndView("createAccountForm");

	}

}
