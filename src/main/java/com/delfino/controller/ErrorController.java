package com.delfino.controller;

import spark.Route;

public class ErrorController extends ControllerBase {

	public Route internalServerError = (req, res) -> {
		return renderContent(req, "error/500.html");
	};

	public Route notFound = (req, res) -> {
		return renderContent(req, "error/404.html");
	};
	
}
