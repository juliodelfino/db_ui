package com.delfino.controller;

import java.util.HashMap;
import java.util.Map;

import com.delfino.util.ViewUtil;

import spark.Route;

public class MainController extends ControllerBase {

	private Route index = (req, res) -> "Hellooo";

	private Route home = (req, res) -> {

	    Map<String, Object> model = new HashMap<>();
	    model.put("name", req.queryParams("q"));
	    return ViewUtil.render(model, "home.html");
	};
	

}
