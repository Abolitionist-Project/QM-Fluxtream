package com.fluxtream.mvc.controllers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fluxtream.Configuration;

@Controller
public class ErrorController {

	Logger logger = Logger.getLogger(ErrorController.class);

	@Autowired
	Configuration env;

	public final static String SERVLET_EXCEPTION_ATTR = "javax.servlet.error.exception";

	public final static String JSP_EXCEPTION_ATTR = "javax.servlet.jsp.jspException";

    @RequestMapping(value = "/accessDenied")
    public String accessDenied(HttpServletRequest request,
                                     HttpServletResponse response) throws IOException {
        if (request.getParameter("json")!=null) {
            response.getWriter().write("{\"result\":\"KO\",\"message\":\"Access Denied\"}");
            return null;
        }
        else return "accessDenied";
    }


}
