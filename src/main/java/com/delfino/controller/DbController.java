package com.delfino.controller;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.delfino.adaptor.ExceptionAdaptor;
import com.delfino.dao.DbInfoDao;
import com.delfino.main.Application;
import com.delfino.model.DbInfo;
import com.delfino.util.RequestUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import spark.Route;

public class DbController extends ControllerBase {

	private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

	private DbInfoDao dbDao = new DbInfoDao();
	private Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private ExceptionAdaptor exAdaptor = new ExceptionAdaptor();

	public DbController() throws ClassNotFoundException {

		// Class.forName("com.mysql.jdbc.Driver");
		// Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");

	}

	private Route getIndex = (req, res) -> {
		return renderContent(req, "db/index.html");
	};

	private Route postConnectDb = (req, res) -> {

		String userId = RequestUtil.getUser(req);
		DbInfo dbInfo = RequestUtil.extract(req, DbInfo.class);
		try {
			dbDao.connect(dbInfo);
			dbDao.add(dbInfo, userId);
			return gson.toJson(dbInfo);
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
			return exAdaptor.convert(ex);
		}
	};

	private Route getInfo = (req, res) -> {

		String userId = RequestUtil.getUser(req);
		String connId = req.queryParams("connId");
		DbInfo dbInfo = (DbInfo) BeanUtils.cloneBean(dbDao.getDb(connId, userId));
		dbInfo.setPassword(null);
		return gson.toJson(dbInfo);
	};

	private Route postInfoUpdate = (req, res) -> {

		String userId = RequestUtil.getUser(req);
		DbInfo dbInfoUpdate = RequestUtil.extract(req, DbInfo.class);
		return dbDao.update(dbInfoUpdate, userId);
	};

	private Route deleteInfo = (req, res) -> {

		String userId = RequestUtil.getUser(req);
		DbInfo dbInfoUpdate = RequestUtil.extract(req, DbInfo.class);
		return dbDao.delete(dbInfoUpdate, userId);
	};

	private Route getAllDb = (req, res) -> {

		String userId = RequestUtil.getUser(req);
		return gson.toJson(dbDao.getAll(userId));
	};

	private Route getTableView = (req, res) -> {

		String userId = RequestUtil.getUser(req);
		Map map = new HashMap<>();
		String connId = req.queryParams("connId");
		try {
			map.put("tables", dbDao.connect(connId, userId).getDbMetadata());
			return renderPage(req, map, "db/table_view.html");
		} catch (SQLException ex) {
			LOGGER.error(ex.getMessage(), ex);
			map.put("exception", ex);
			return renderPage(req, map, "db/error.html");
		}
	};

	private Route getQuery = (req, res) -> {

		String userId = RequestUtil.getUser(req);
		String sql = req.queryParams("q");
		String connId = req.queryParams("connId");
		return dbDao.connect(connId, userId).executeQuery(sql);
	};

	private Route getColumns = (req, res) -> {

		String userId = RequestUtil.getUser(req);
		String table = req.queryParams("table");
		String connId = req.queryParams("connId");
		return dbDao.connect(connId, userId).getColumns(table);
	};
}
