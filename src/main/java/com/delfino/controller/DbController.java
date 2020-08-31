package com.delfino.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.delfino.adaptor.ExceptionAdaptor;
import com.delfino.dao.DbInfoDao;
import com.delfino.db.DbConnection;
import com.delfino.main.Application;
import com.delfino.model.CatalogInfo;
import com.delfino.model.DbConnInfo;
import com.delfino.model.TreeNode;
import com.delfino.util.AppException;
import com.delfino.util.DbUtil;
import com.delfino.util.RequestUtil;
import com.delfino.util.ViewUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import spark.Request;
import spark.Route;
import spark.utils.StringUtils;

public class DbController extends ControllerBase {

	private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

	private DbInfoDao dbDao = new DbInfoDao();
	private Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private ExceptionAdaptor exAdaptor = new ExceptionAdaptor();

	public final Route postConnectDb = (req, res) -> {

		String userId = RequestUtil.getUsername(req);
		DbConnInfo dbInfo = RequestUtil.extract(req, DbConnInfo.class);
		try {
			DbConnection dbConn = dbDao.connect(dbInfo);
			dbInfo.setDriver(RequestUtil.getDbDriver(dbInfo.getUrl()));
			dbInfo.setDefaultCatalog(dbConn.getConnection().getCatalog());
			dbDao.add(dbInfo, userId);
			dbInfo = dbDao.getDb(dbInfo.getConnId(), userId);
			tryUpdateUserAccess(req, dbInfo);
			return gson.toJson(dbInfo);
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
			return exAdaptor.convert(ex);
		}
	};

	public final Route getInfo = (req, res) -> {

		String userId = RequestUtil.getUsername(req);
		String connId = req.queryParams("connId");
		DbConnInfo dbInfo = (DbConnInfo) BeanUtils.cloneBean(dbDao.getDb(connId, userId));
		dbInfo.setPassword(null);
		return gson.toJson(dbInfo);
	};

	public final Route postInfoUpdate = (req, res) -> {

		String userId = RequestUtil.getUsername(req);
		DbConnInfo dbInfoUpdate = RequestUtil.extract(req, DbConnInfo.class);
		boolean success = dbDao.update(dbInfoUpdate, userId);
		if (success) {
			tryUpdateUserAccess(req, dbInfoUpdate);
		}
		return success;
	};

	public Route deleteInfo = (req, res) -> {

		String userId = RequestUtil.getUsername(req);
		DbConnInfo dbInfoUpdate = RequestUtil.extract(req, DbConnInfo.class);
		return dbDao.delete(dbInfoUpdate, userId);
	};

//	public Route getAllDb = (req, res) -> {
//
//		String userId = RequestUtil.getUsername(req);
//		return gson.toJson(dbDao.getAll(userId));
//	};
	
	//new methods
	public final Route getIndex = (req, res) -> {
		String userId = RequestUtil.getUsername(req);
		List<DbConnInfo> userDbs = new ArrayList(dbDao.getAll(userId).values());
		userDbs.sort((c1, c2) -> c1.getConnectionName().compareTo(c2.getConnectionName()));
		req.attribute("dbList", userDbs);
		req.attribute("DB_TREE_DATA", gson.toJson(dbDao.getDbTree(userId)));
		return renderContent(req, "db/index.html");
	};
	
	public final Route getDbConnInfo = (req, res) -> {
		String userId = RequestUtil.getUsername(req);
		String connId = req.queryParams("id");
		boolean refresh = req.queryParams("refresh") != null ? 
				Boolean.parseBoolean(req.queryParams("refresh")) : false;
		DbConnInfo dbInfo = dbDao.getDb(connId, userId, refresh);
		if (dbInfo == null) {
			throw new AppException("No db connection found with ID=" + connId);
		}
		req.attribute("dbInfo", dbInfo);
		req.attribute("dbPhoto", ViewUtil.getDbPhoto(dbInfo));
		
		req.attribute("catalogs", dbInfo.getCache().getCatalogs());
		List<TreeNode> list = dbDao.getDbTree(userId);
		TreeNode selected = list.stream().filter(n -> n.getId().equals(connId)).findFirst().get();    
		selected.setState("selected", true);
		req.attribute("DB_TREE_DATA", gson.toJson(list));
		dbInfo.setStatus("Active");
		return renderContent(req, "db/dbconninfo.html");
	};

	public final Route getDbInfo = (req, res) -> {
		String userId = RequestUtil.getUsername(req);
		String connId = req.queryParams("id");
		String catalogName = req.queryParams("catalog");
		String schemaName = req.queryParams("schema");
		boolean refresh = req.queryParams("refresh") != null ? 
				Boolean.parseBoolean(req.queryParams("refresh")) : false;
		DbConnInfo dbConnInfo = dbDao.getDb(connId, userId, refresh);
		if (dbConnInfo == null) {
			throw new AppException("No db connection found with ID=" + connId);
		}
		req.attribute("dbInfo", dbConnInfo);
		req.attribute("catalog", catalogName);
		req.attribute("schema", schemaName == null ? "" : schemaName);
		
		String catSchema = DbUtil.getUniqueName(catalogName, schemaName);
		CatalogInfo cat = dbConnInfo.getCache().getCatalogs().get(catSchema);
		if (cat == null) {
			if (!dbConnInfo.getCache().getCatalogs().keySet().stream().anyMatch(key -> key.startsWith(catalogName))) {
				throw new AppException("No catalog found with name=" + catalogName);
			}
			cat = new CatalogInfo(catalogName, "%");
		}
//		if (refresh || cat.getTables().isEmpty()) {
//			dbDao.updateTableCache(dbConnInfo, cat);
//		}
		req.attribute("tables", cat.getTables());
		List<TreeNode> list = dbDao.getDbTree(userId);
		TreeNode selected = list.stream().filter(n -> n.getId().equals(connId)).findFirst().get();
		String catalogLabel = StringUtils.isEmpty(catalogName) ? CatalogInfo.NO_LABEL : catalogName;
		TreeNode selectedCat = selected.getNodes().stream().filter(n -> n.getText().equals(catalogLabel)).findFirst().get();
		selectedCat.setState("selected", true);
		req.attribute("DB_TREE_DATA", gson.toJson(list));
		dbConnInfo.setStatus("Active");
		return renderContent(req, "db/dbinfo.html");
	};

	public final Route getNewDbConn = (req, res) -> {
		return renderContent(req, "db/newdbconn.html");
	};

	private void tryUpdateUserAccess(Request req, DbConnInfo dbInfo) {

		String users = req.queryParams("users");
		if (StringUtils.isNotEmpty(users) && RequestUtil.getUser(req).isAdmin()) {
			dbDao.updateUserAccess(dbInfo.getConnId(), users.split(" "));
		}
	}
}
