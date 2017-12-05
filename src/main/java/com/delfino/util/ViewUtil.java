package com.delfino.util;

import java.io.IOException;
import java.util.Map;

import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;
import spark.utils.IOUtils;

public class ViewUtil {
	
	public static String STATIC_FILES = "/public";
	
    public static String render(Map<String, Object> model, String templatePath) {
        return new VelocityTemplateEngine().render(
        		new ModelAndView(model, STATIC_FILES + "/" + templatePath));
    }

	public static Object renderHtml(String htmlPath) throws IOException {
		return IOUtils.toString(ViewUtil.class.getResourceAsStream(
				STATIC_FILES + "/" + htmlPath));
	}
}
