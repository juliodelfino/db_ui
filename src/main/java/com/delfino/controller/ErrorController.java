package com.delfino.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import spark.Request;
import spark.Route;

public class ErrorController extends ControllerBase {

	private Gson gson = new GsonBuilder().setPrettyPrinting().create();
	
	public Route internalServerError = (req, res) -> {
		
		generateExceptionMessage(req);
		boolean ajax = "XMLHttpRequest".equals(req.headers("X-Requested-With"));
		return ajax ? req.attribute("exceptionMessage") 
				: renderContent(req, "error/500.html");
	};

	public Route notFound = (req, res) -> {
		
		generateExceptionMessage(req);
		if (req.attribute("exception") == null) {
			req.attribute("exceptionMessage", 
					"The web page " + req.pathInfo() + " doesn't exist.");
		}
		boolean ajax = "XMLHttpRequest".equals(req.headers("X-Requested-With"));
		return ajax ? req.attribute("exceptionMessage") 
				: renderContent(req, "error/404.html");
	};

	private void generateExceptionMessage(Request req) {
		
		if (req.attribute("exception") != null) {
			Exception ex = req.attribute("exception");
			req.attribute("exceptionMessage", ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}
	}
	
}
