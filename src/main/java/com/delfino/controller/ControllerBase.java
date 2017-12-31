package com.delfino.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.velocity.exception.VelocityException;

import com.delfino.util.AppProperties;
import com.delfino.util.Constants;

import spark.ModelAndView;
import spark.Request;
import spark.template.velocity.VelocityTemplateEngine;

public abstract class ControllerBase {
	
	private static String VIEWS = Constants.STATIC_FILES + "/views";
	
    private String render(Request req, Map<String, Object> model, String templatePath) {
        model.putAll((Map)AppProperties.getInstance());
        req.attributes().stream().forEach(
        		attr -> model.put(attr, req.attribute(attr)));
        req.session().attributes().stream().forEach(
        		attr -> model.put(attr, req.session().attribute(attr)));
        try {
        return new VelocityTemplateEngine().render(
        		new ModelAndView(model, templatePath));
        } catch (VelocityException ex) {
        	req.attribute("exception", ex);
        	throw ex;
        }
    }

	protected String renderPage(Request req, String pageName) throws IOException {
		return renderPage(req, new HashMap(), pageName);
	}

	protected String renderPage(Request req, Map<String, Object> model, String pageName) throws IOException {
		return render(req, model, VIEWS + "/" + pageName);
	}

	protected String renderContent(Request req, String contentPage) {
		Map<String, Object> map = new HashMap<>();
		map.put("content_page", VIEWS + "/" + contentPage);
		return render(req, map, Constants.STATIC_FILES + "/layouts/page.html");
	}
}
