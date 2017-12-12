package com.delfino.filter;

import com.delfino.util.Constants;

import spark.Filter;
import spark.Request;
import spark.Response;

public class SkipAuthFilter implements Filter {

	@Override
	public void handle(Request req, Response res) throws Exception {

		if (req.pathInfo().contains("login")) {
			return;
		}
		if (!req.session().attributes().contains(Constants.SESSION_USER)) {
            req.session().attribute("loginRedirect", req.pathInfo());
			res.redirect(Constants.PATH_LOGIN);
		}
	}

}
