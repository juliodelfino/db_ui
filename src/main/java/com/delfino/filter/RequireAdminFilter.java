package com.delfino.filter;

import java.net.NoRouteToHostException;
import java.util.List;

import com.delfino.model.User;
import com.delfino.util.RequestUtil;

import spark.Filter;
import spark.Request;
import spark.Response;

public class RequireAdminFilter implements Filter {

	private List<String> paths;
	
	public RequireAdminFilter(List<String> paths) {
		this.paths = paths;
	}
	
	@Override
	public void handle(Request req, Response res) throws Exception {

		if (paths != null &&
			paths.stream().anyMatch(
					path -> req.pathInfo().equals(path))) {
			User user = RequestUtil.getUser(req);
			if (user != null && user.isAdmin()) {
				return;
			} else {
				throw new NoRouteToHostException();
			}
		}
	}

}
