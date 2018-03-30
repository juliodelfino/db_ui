package com.delfino.controller;

import java.util.List;
import java.util.stream.Collectors;

import com.delfino.annotation.AppRoute;
import com.delfino.dao.DbInfoDao;
import com.delfino.dao.UserDao;
import com.delfino.model.DbInfo;
import com.delfino.model.User;

import spark.Route;

public class AdminController extends ControllerBase {

	private UserDao userDao = new UserDao();
	private DbInfoDao dbInfoDao = new DbInfoDao();
	
	@AppRoute(skipAuthentication = true)
	public Route getIndex = (req, res) -> {
		List<DbInfo> dbList = dbInfoDao.getAll();
		req.attribute("dbs", dbList);
		req.attribute("db_users", dbList.stream().collect(
			Collectors.toMap(db -> db.getConnId(), 
				db -> userDao.getDbUsers(db.getConnId())
					.stream().map(User::getUsername)
					.collect(Collectors.toList())
			)));
		return renderContent(req, "admin/index.html");
	};
}
