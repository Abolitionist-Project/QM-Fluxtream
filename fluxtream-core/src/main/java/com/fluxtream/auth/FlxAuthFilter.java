package com.fluxtream.auth;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fluxtream.Configuration;
import com.fluxtream.services.GuestService;

public class FlxAuthFilter extends UsernamePasswordAuthenticationFilter {

	Logger logger = Logger.getLogger(UsernamePasswordAuthenticationFilter.class);

	@Autowired
	GuestService guestService;

	@Autowired
	Configuration env;

	@Override
	public Authentication attemptAuthentication(javax.servlet.http.HttpServletRequest request,
			javax.servlet.http.HttpServletResponse response) throws AuthenticationException {
		Authentication authentication = super.attemptAuthentication(request, response);
		return authentication;
	}

	@Override
	protected String obtainPassword(HttpServletRequest request) {
		String password = (String) request.getSession().getAttribute("password");
		if (password == null || password.isEmpty()) {
			password = request.getParameter("f_password");
		}
		return password;
	}

	@Override
	protected String obtainUsername(HttpServletRequest request) {
		String username = (String) request.getSession().getAttribute("username");
		if (username == null || username.isEmpty()) {
			username = request.getParameter("f_username");
		}
		return username;
	}

}
