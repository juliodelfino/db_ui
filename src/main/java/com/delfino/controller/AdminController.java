package com.delfino.controller;

import com.delfino.annotation.AppRoute;
import com.delfino.dao.DbInfoDao;
import com.delfino.dao.UserDao;

import spark.Route;

public class AdminController extends ControllerBase {

	private UserDao userDao = new UserDao();
	private DbInfoDao dbInfoDao = new DbInfoDao();
	
	@AppRoute(skipAuthentication = true)
	public Route getIndex = (req, res) -> {
		req.attribute("dbs", dbInfoDao.getAll());
		return renderContent(req, "admin/index.html");
	};
}
