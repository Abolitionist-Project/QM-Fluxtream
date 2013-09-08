<%@ page language="java" trimDirectiveWhitespaces="true"
	contentType="application/json; charset=UTF-8" pageEncoding="UTF-8"
	isErrorPage="true"%>
{"error":"<% exception.toString() %>  >> <% exception.printStackTrace(response.getWriter()); %>"}
