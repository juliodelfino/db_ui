package com.delfino.controller;

import java.util.HashMap;
import java.util.Map;

import com.delfino.util.ViewUtil;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.template.velocity.VelocityTemplateEngine;

public class LoginController extends ControllerBase {

	private Route index = (req, res) -> {


	    return ViewUtil.render(null, "home.html");
	};
	

}
