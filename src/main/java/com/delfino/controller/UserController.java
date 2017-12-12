package com.delfino.controller;

import com.delfino.adaptor.ExceptionAdaptor;
import com.delfino.dao.UserDao;
import com.delfino.model.User;
import com.delfino.util.AppException;
import com.delfino.util.Constants;
import com.delfino.util.RequestUtil;

import spark.Route;

public class UserController extends ControllerBase {

	private UserDao userDao = new UserDao();
	private ExceptionAdaptor exAdaptor = new ExceptionAdaptor();

	private Route getLogin = (req, res) -> {

		return renderPage(req, "user/login.html");
	};

	private Route postLogin = (req, res) -> {
		User user = RequestUtil.extract(req, User.class);
		boolean authenticated = userDao.validate(user);
		if (authenticated) {
			req.session().attribute(Constants.SESSION_USER, user.getUsername().toUpperCase());
		}
		return authenticated;
	};

	private Route getLogout = (req, res) -> {

		req.session().removeAttribute(Constants.SESSION_USER);
		res.redirect(Constants.PATH_LOGIN);
		return null;
	};
	
	private Route getSettings = (req, res) -> {

		return renderContent(req, "user/settings.html");
	};
	
	private Route postPassword = (req, res) -> {
		
		User user = RequestUtil.extract(req, User.class);
		boolean valid = userDao.validate(user);
		if (!valid) {
			return exAdaptor.convert(new AppException("Old password is invalid."));
		}
		String newPass = req.queryParams("password_new");
		String confirmPass = req.queryParams("password_confirm");
		if (!newPass.equals(confirmPass)) {
			return exAdaptor.convert(new AppException("New passwords don't match."));
		}
		user.setPassword(newPass);
		return userDao.update(user);
	};

}
