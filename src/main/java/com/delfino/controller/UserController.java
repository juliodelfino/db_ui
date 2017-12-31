package com.delfino.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.delfino.adaptor.ExceptionAdaptor;
import com.delfino.adaptor.ListAdaptor;
import com.delfino.annotation.AppRoute;
import com.delfino.dao.DbInfoDao;
import com.delfino.dao.UserDao;
import com.delfino.model.User;
import com.delfino.util.AppException;
import com.delfino.util.Constants;
import com.delfino.util.RequestUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import spark.Route;

public class UserController extends ControllerBase {

	private UserDao userDao = new UserDao();
	private ExceptionAdaptor exAdaptor = new ExceptionAdaptor();
	private ListAdaptor listAdaptor = new ListAdaptor();
	private Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private DbInfoDao dbDao = new DbInfoDao();

	@AppRoute(skipAuthentication=true)
	public Route getLogin = (req, res) -> {

		return renderPage(req, "user/login.html");
	};

	public Route postLogin = (req, res) -> {
		User user = RequestUtil.extract(req, User.class);
		User validatedUser = userDao.validate(user);
		boolean authenticated = validatedUser != null;
		if (authenticated) {
			req.session().attribute(Constants.SESSION_USER, validatedUser);
		}
		return authenticated;
	};

	public Route getLogout = (req, res) -> {

		req.session().removeAttribute(Constants.SESSION_USER);
		res.redirect(Constants.PATH_LOGIN);
		return null;
	};
	
	public Route getSettings = (req, res) -> {

		return renderContent(req, "user/settings.html");
	};
	
	public Route postPassword = (req, res) -> {
		
		User user = RequestUtil.extract(req, User.class);
		boolean valid = userDao.validate(user) != null;
		if (!valid) {
			return exAdaptor.convert(new AppException("Old password is invalid."));
		}
		String newPass = req.queryParams("password_new");
		String confirmPass = req.queryParams("password_confirm");
		if (!newPass.equals(confirmPass)) {
			return exAdaptor.convert(new AppException("New passwords don't match."));
		}
		user.setPassword(newPass);
		return userDao.updatePassword(user);
	};

	@AppRoute(skipAuthentication=true)
	public Route getInfo = (req, res) -> {

		String username = req.queryParams("username");
		Map map = new HashMap();
		map.put("user", userDao.get(username));
		map.put("dbList", dbDao.getAll(username).keySet());
		return gson.toJson(map);
	};

	@AppRoute(skipAuthentication=true)
	public Route postInfo = (req, res) -> {

		User user = RequestUtil.extract(req, User.class);
		String[] dbAccess = req.queryParams("dbaccess").split(" ");
		return userDao.update(user, dbAccess);
	};
	
	public Route deleteInfo = (req, res) -> {

		String username = req.queryParams("username");
		return userDao.delete(username);
	};

	@AppRoute(skipAuthentication=true)
	public Route postNew = (req, res) -> {

		User user = RequestUtil.extract(req, User.class);
		String[] dbAccess = req.queryParams("dbaccess").split(" ");
		return userDao.add(user, dbAccess);
	};
	
	public Route getList = (req, res) -> {
		
		return listAdaptor.convert(userDao.getAll());
	};
}