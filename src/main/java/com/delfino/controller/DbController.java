package com.delfino.controller;

import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.delfino.adaptor.ExceptionAdaptor;
import com.delfino.dao.DbInfoDao;
import com.delfino.main.Application;
import com.delfino.model.DbInfo;
import com.delfino.util.AppException;
import com.delfino.util.RequestUtil;
import com.delfino.util.ViewUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import spark.Route;

public class DbController extends ControllerBase {

	private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

	private DbInfoDao dbDao = new DbInfoDao();
	private Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private ExceptionAdaptor exAdaptor = new ExceptionAdaptor();

	public Route postConnectDb = (req, res) -> {

		String userId = RequestUtil.getUsername(req);
		DbInfo dbInfo = RequestUtil.extract(req, DbInfo.class);
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
	
	//new methods
	public Route getIndex = (req, res) -> {
		String userId = RequestUtil.getUsername(req);
		req.attribute("dbList", dbDao.getAll(userId));
		return renderContent(req, "db/index.html");
	};

	public Route getDbInfo = (req, res) -> {
		String userId = RequestUtil.getUsername(req);
		String connId = req.queryParams("id");
		boolean refresh = req.queryParams("refresh") != null ? 
				Boolean.parseBoolean(req.queryParams("refresh")) : false;
		DbInfo dbInfo = dbDao.getDb(connId, userId);
		if (dbInfo == null) {
			throw new AppException("No database found with ID=" + connId);
		}
		req.attribute("dbInfo", dbInfo);
		req.attribute("dbPhoto", ViewUtil.getDbPhoto(dbInfo));
		
		Map meta = dbInfo.getTables();
		if (meta.isEmpty() || refresh) {
			meta = dbDao.connect(connId, userId).getDbMetadata();
			dbInfo.setTables(meta);
		}
		
		req.attribute("tables", meta);
		dbInfo.setStatus("Active");
		return renderContent(req, "db/dbinfo.html");
	};

	public Route getNewDbConn = (req, res) -> {
		return renderContent(req, "db/newdbconn.html");
	};
}
