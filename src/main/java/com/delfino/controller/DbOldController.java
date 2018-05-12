package com.delfino.controller;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.delfino.adaptor.ExceptionAdaptor;
import com.delfino.dao.DbInfoDao;
import com.delfino.main.Application;
import com.delfino.model.DbConnInfo;
import com.delfino.model.TableInfo;
import com.delfino.util.AppException;
import com.delfino.util.RequestUtil;
import com.delfino.util.ViewUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import spark.Route;
import spark.Spark;

public class DbOldController extends ControllerBase {

	private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

	private DbInfoDao dbDao = new DbInfoDao();
	private Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private ExceptionAdaptor exAdaptor = new ExceptionAdaptor();

	public Route getIndex = (req, res) -> {
		return renderContent(req, "dbold/index.html");
	};

	public Route postConnectDb = (req, res) -> {

		String userId = RequestUtil.getUsername(req);
		DbConnInfo dbInfo = RequestUtil.extract(req, DbConnInfo.class);
		try {
			dbDao.connect(dbInfo);
			dbInfo.setDriver(RequestUtil.getDbDriver(dbInfo.getUrl()));
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
		DbConnInfo dbInfo = (DbConnInfo) BeanUtils.cloneBean(dbDao.getDb(connId, userId));
		dbInfo.setPassword(null);
		return gson.toJson(dbInfo);
	};

	public Route postInfoUpdate = (req, res) -> {

		String userId = RequestUtil.getUsername(req);
		DbConnInfo dbInfoUpdate = RequestUtil.extract(req, DbConnInfo.class);
		return dbDao.update(dbInfoUpdate, userId);
	};

	public Route deleteInfo = (req, res) -> {

		String userId = RequestUtil.getUsername(req);
		DbConnInfo dbInfoUpdate = RequestUtil.extract(req, DbConnInfo.class);
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
//		try {
//			map.put("tables", dbDao.connect(connId, userId).getDbMetadata());
			return renderPage(req, map, "dbold/table_view.html");
//		} catch (SQLException ex) {
//			LOGGER.error(ex.getMessage(), ex);
//			map.put("exception", ex);
//			return renderPage(req, map, "dbold/error.html");
//		}
	};

	public Route getQuery = (req, res) -> {

		String userId = RequestUtil.getUsername(req);
		String sql = req.queryParams("q");
		String connId = req.queryParams("connId");
		return dbDao.connect(connId, userId).executeQuery(sql, "");
	};

	public Route getColumns = (req, res) -> {

		String userId = RequestUtil.getUsername(req);
		String table = req.queryParams("table");
		String connId = req.queryParams("connId");
		return dbDao.connect(connId, userId).getColumns(table);
	};
}
