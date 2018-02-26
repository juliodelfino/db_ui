package com.delfino.filter;

import java.util.List;
import java.util.Set;

import com.delfino.util.Constants;

import spark.Filter;
import spark.Redirect.Status;
import spark.utils.StringUtils;
import spark.Request;
import spark.Response;
import spark.Spark;

public class SkipAuthFilter implements Filter {

	private List<String> paths;
	
	public SkipAuthFilter(List<String> paths) {
		this.paths = paths;
	}
	
	@Override
	public void handle(Request req, Response res) throws Exception {

		if (paths != null &&
			paths.stream().anyMatch(
					path -> req.pathInfo().equals(path))) {
			return;
		}
		if (!req.session().attributes().contains(Constants.SESSION_USER)) {
			boolean ajax = "XMLHttpRequest".equals(
                    req.headers("X-Requested-With"));
			if (ajax) {
				Spark.halt(401);
			}
			else {
	            req.session().attribute(Constants.LOGIN_REDIRECT, req.pathInfo() + 
	            		(StringUtils.isEmpty(req.queryString()) ? "" : 
	            			("?" + req.queryString())));
				res.redirect(Constants.PATH_LOGIN);
			}
		}
	}

}
