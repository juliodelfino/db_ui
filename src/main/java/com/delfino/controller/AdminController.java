package com.delfino.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.delfino.adaptor.ListAdaptor;
import com.delfino.annotation.AppRoute;
import com.delfino.dao.DbInfoDao;
import com.delfino.dao.UserDao;
import com.delfino.dao.UserDbDao;
import com.delfino.model.DbConnInfo;
import com.delfino.model.DbConnWithUsers;
import com.delfino.model.User;

import spark.Route;

public class AdminController extends ControllerBase {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AdminController.class);
    
	private UserDao userDao = new UserDao();
	private DbInfoDao dbInfoDao = new DbInfoDao();
	private UserDbDao userDbDao = new UserDbDao();
	private ListAdaptor listAdaptor = new ListAdaptor();
	
	@AppRoute(requireAdmin=true)
	public Route getIndex = (req, res) -> {
		List<DbConnInfo> dbList = dbInfoDao.getAll().stream()
			.sorted((d1, d2) -> d1.getConnectionName().compareTo(d2.getConnectionName()))
			.collect(Collectors.toList());
		req.attribute("dbs", dbList);

		req.attribute("users", userDao.getAll().stream()
			.sorted((u1, u2) -> u1.getFullName().compareTo(u2.getFullName()))
			.collect(Collectors.toList()));
//		req.attribute("db_users", dbList.stream().collect(
//			Collectors.toMap(db -> db.getConnId(), 
//				db -> userDao.getDbUsers(db.getConnId())
//					.stream().map(User::getUsername)
//					.collect(Collectors.toList())
//			)));
		return renderContent(req, "admin/index.html");
	};

	@AppRoute(requireAdmin=true)
	public Route getUserlist = (req, res) -> {
		
		return listAdaptor.convert(new ArrayList(userDao.getAll()));
	};

	@AppRoute(requireAdmin=true)
	public Route getDblist = (req, res) -> {
		
		List dbs = dbInfoDao.getAll().stream().map(db -> {
				DbConnWithUsers dbu = new DbConnWithUsers();
				try {
					BeanUtils.copyProperties(dbu, db);
				} catch(ReflectiveOperationException ex) {
					LOGGER.error(ex.getMessage(), ex);
				}
				dbu.setUsers(userDbDao.getDbUserList(db.getConnId()));
				return dbu;
			})
			.collect(Collectors.toList());
		return listAdaptor.convert(dbs);
	};
}
