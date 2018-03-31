package com.delfino.controller;

import com.delfino.util.AppException;

import spark.Request;
import spark.Route;

public class ErrorController extends ControllerBase {

	public Route internalServerError = (req, res) -> {
		
		generateExceptionMessage(req);
		return renderContent(req, "error/500.html");
	};

	public Route notFound = (req, res) -> {
		
		generateExceptionMessage(req);
		if (req.attribute("exception") == null) {
			req.attribute("exceptionMessage", 
					"The web page " + req.pathInfo() + " doesn't exist.");
		}
		return renderContent(req, "error/404.html");
	};

	private void generateExceptionMessage(Request req) {
		
		if (req.attribute("exception") != null) {
			Exception ex = req.attribute("exception");
			req.attribute("exceptionMessage", ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}
	}
	
}
