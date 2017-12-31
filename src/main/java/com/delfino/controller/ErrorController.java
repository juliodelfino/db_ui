package com.delfino.controller;

import spark.Request;
import spark.Response;
import spark.Route;

public class ErrorController extends ControllerBase {

	public Route get500 = (req, res) -> {
		return renderContent(req, "error/500.html");
	};

	
}
