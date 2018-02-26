package com.delfino.controller;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.VelocityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.delfino.adaptor.ExceptionAdaptor;
import com.delfino.util.AppProperties;
import com.delfino.util.Constants;
import com.delfino.util.ViewUtil;

import spark.ModelAndView;
import spark.Request;
import spark.template.velocity.VelocityTemplateEngine;

public abstract class ControllerBase {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerBase.class);
	private static final VelocityTemplateEngine TEMPLATE;
	private static VelocityEngine VELOCITY_ENG;
	private static String VIEWS = Constants.STATIC_FILES + "/views";
	
	static {
		TEMPLATE = new VelocityTemplateEngine();
		try {
			Field f = TEMPLATE.getClass().getDeclaredField("velocityEngine");
			f.setAccessible(true);
			VELOCITY_ENG = (VelocityEngine)f.get(TEMPLATE);
		} catch (SecurityException | ReflectiveOperationException e) {
			LOGGER.error(e.getMessage(), e);
			VELOCITY_ENG = null;
		}
	}
	
    private String render(Request req, Map<String, Object> model, String templatePath) {
        model.putAll((Map)AppProperties.getInstance());
        req.attributes().stream().forEach(
        		attr -> model.put(attr, req.attribute(attr)));
        req.session().attributes().stream().forEach(
        		attr -> model.put(attr, req.session().attribute(attr)));
        model.put("ViewUtil", ViewUtil.class);
        try {
        return TEMPLATE.render(
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
		String moduleName = req.attribute("moduleName");
		String methodName = extractMethodName(contentPage);
		List<String> cssFiles = filterNonexistentFiles("/public/assets/css/", Arrays.asList(
				String.format("%s.css", moduleName),
				String.format("%s_%s.css", moduleName, methodName)));
		List<String> jsFiles = filterNonexistentFiles("/public/assets/js/", Arrays.asList(
				String.format("%s.js", moduleName),
				String.format("%s_%s.js", moduleName, methodName)));
		
		if (!cssFiles.isEmpty()) {
			map.put("css_files", cssFiles);
		}
		if (!jsFiles.isEmpty()) {
			map.put("js_files", jsFiles);
		}
		return render(req, map, Constants.STATIC_FILES + "/layouts/page.html");
	}
	
	private String extractMethodName(String contentPage) {

		int startIdx = contentPage.indexOf("/");
		return contentPage.substring(startIdx + 1, contentPage.indexOf("."));
	}

	private List<String> filterNonexistentFiles(String dir, List<String> files) {
		return files.stream().filter(f -> {
			boolean exists = VELOCITY_ENG.resourceExists(dir + f);
			LOGGER.debug(dir + f + " exists? " + exists);
			return exists;
		}).collect(Collectors.toList());
	} 
}
