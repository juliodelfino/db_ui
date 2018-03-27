package com.delfino.controller;

import com.delfino.util.AppException;

import spark.Route;

public class ErrorController extends ControllerBase {

	public Route internalServerError = (req, res) -> {
		return renderContent(req, "error/500.html");
	};

	public Route notFound = (req, res) -> {
		if (req.attribute("exception") == null) {
			req.attribute("exception", 
				new AppException("The web page " + req.pathInfo() + " doesn't exist."));
		}
		return renderContent(req, "error/404.html");
	};
	
}
