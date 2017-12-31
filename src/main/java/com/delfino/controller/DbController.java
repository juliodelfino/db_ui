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

	public Route getIndex = (req, res) -> {
		return renderContent(req, "db/index.html");
	};

	public Route postConnectDb = (req, res) -> {

		String userId = RequestUtil.getUsername(req);
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

	public Route getInfo = (req, res) -> {

		String userId = RequestUtil.getUsername(req);
		String connId = req.queryParams("connId");
		DbInfo dbInfo = (DbInfo) BeanUtils.cloneBean(dbDao.getDb(connId, userId));
		dbInfo.setPassword(null);
		return gson.toJson(dbInfo);
	};

	public Route postInfoUpdate = (req, res) -> {

		String userId = RequestUtil.getUsername(req);
		DbInfo dbInfoUpdate = RequestUtil.extract(req, DbInfo.class);
		return dbDao.update(dbInfoUpdate, userId);
	};

	public Route deleteInfo = (req, res) -> {

		String userId = RequestUtil.getUsername(req);
		DbInfo dbInfoUpdate = RequestUtil.extract(req, DbInfo.class);
		return dbDao.delete(dbInfoUpdate, userId);
	};

	public Route getAllDb = (req, res) -> {

		String userId = RequestUtil.getUsername(req);
		return gson.toJson(dbDao.getAll(userId));
	};

	public Route getTableView = (req, res) -> {

		String userId = RequestUtil.getUsername(req);
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

	public Route getQuery = (req, res) -> {

		String userId = RequestUtil.getUsername(req);
		String sql = req.queryParams("q");
		String connId = req.queryParams("connId");
		return dbDao.connect(connId, userId).executeQuery(sql);
	};

	public Route getColumns = (req, res) -> {

		String userId = RequestUtil.getUsername(req);
		String table = req.queryParams("table");
		String connId = req.queryParams("connId");
		return dbDao.connect(connId, userId).getColumns(table);
	};
}
