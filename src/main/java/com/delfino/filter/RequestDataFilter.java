package com.delfino.filter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import spark.Filter;
import spark.Request;
import spark.Response;

public class RequestDataFilter implements Filter {

	@Override
	public void handle(Request req, Response res) throws Exception {
		Matcher m = Pattern.compile("^/(\\w+)").matcher(req.pathInfo());
		if (m.find()) {
			req.attribute("moduleName", m.group(1));
		}
	}

}
