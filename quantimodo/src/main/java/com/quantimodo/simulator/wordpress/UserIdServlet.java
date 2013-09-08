package com.quantimodo.simulator.wordpress;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserIdServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1225416421831034988L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		/*
		 * cannot have session to store the user id
		 */
		// String userId = (String) req.getSession().getAttribute("userId");
		String userId = (String) getServletContext().getAttribute("userId");
		if (userId != null) {
			resp.getOutputStream().print(userId);
		} else {
			resp.getOutputStream().print("none");
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// req.getSession().setAttribute("userId", req.getParameter("userId"));
		getServletContext().setAttribute("userId", req.getParameter("userId"));
		resp.sendRedirect("");
	}

}
