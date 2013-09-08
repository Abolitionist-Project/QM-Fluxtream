package com.quantimodo.api;

import com.quantimodo.data.Failure;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@ControllerAdvice
public class ErrorController {
	private static final Logger logger = LoggerFactory.getLogger(APIController.class);
	
	@RequestMapping(value = "/401/", method = { RequestMethod.GET, RequestMethod.POST }, produces = "application/json")
	protected @ResponseBody Failure unauthorized(final HttpServletResponse response) {
		response.setStatus(401);
		return new Failure("Login required");
	}
	
	@ExceptionHandler(Throwable.class)
	public @ResponseBody Failure handleException(final Throwable error, final HttpServletResponse response) {
		logger.error("Exception encountered", error);
		response.setStatus(500);
		return new Failure(error);
	}
}
