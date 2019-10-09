package com.delfino.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.delfino.adaptor.ExceptionAdaptor;
import com.delfino.dao.DbInfoDao;
import com.delfino.dao.UserDao;
import com.delfino.db.DbConnection;
import com.delfino.model.CatalogInfo;
import com.delfino.model.DbConnInfo;
import com.delfino.model.TableInfo;
import com.delfino.model.TreeNode;
import com.delfino.util.AppException;
import com.delfino.util.DbUtil;
import com.delfino.util.RequestUtil;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import spark.Route;
import spark.utils.StringUtils;

public class TableController extends ControllerBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(TableController.class);
    
	private ExceptionAdaptor exAdaptor = new ExceptionAdaptor();
	private DbInfoDao dbDao = new DbInfoDao();
	private UserDao userDao = new UserDao();
	private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").setPrettyPrinting().disableHtmlEscaping()
			.create();

	public Route getQuery = (req, res) -> {

		String userId = RequestUtil.getUsername(req);
		String sqlQuery = req.queryParams("q");
		String queryId = req.queryParams("qId");
		String connId = req.queryParams("connId");
		String catalogName = req.queryParams("catalog");
		String schemaName = req.queryParams("schema");
		CatalogInfo cat = new CatalogInfo(catalogName, schemaName);
		try {
			DbConnection dbConn = dbDao.connect(connId, userId);
			String result = "";
			sqlQuery = sqlQuery.replaceAll("\n", "");
			for (String sql : sqlQuery.split(";")) {
				sql = sql.trim();
				if (StringUtils.isEmpty(sql)) {
					continue;
				} else if (sql.matches("(SELECT|select|SHOW|show|WITH|with).*")) {

					result = dbConn.executeQuery(sql, cat, queryId);
					userDao.saveQuery(sql, userId);

				} else if (sql.matches(
						"(INSERT|insert|UPDATE|update|DELETE|delete|" + "CREATE|create|ALTER|alter|DROP|drop).*")
						&& RequestUtil.getUser(req).isAdmin()) {
					result = dbConn.executeUpdate(sql, cat, queryId) + " row(s) updated";
					result = gson.toJson(ImmutableMap.of("message", result));
					userDao.saveQuery(sql, userId);

				} else {
					return dbConn.getData(sql);
					// return exAdaptor.convert(new SQLException("Unknown
					// starting keyword: " + sql));
				}
			}

			return result;
		} catch (Exception ex) {
			LOGGER.error("Error executing query: " + sqlQuery, ex);
			return exAdaptor.convert(ex);
		}
	};

	public Route getColumns = (req, res) -> {

		String userId = RequestUtil.getUsername(req);
		String table = req.queryParams("table");
		String connId = req.queryParams("connId");
		String catalogName = req.queryParams("catalog");
		CatalogInfo cat = new CatalogInfo(catalogName, null);
		try {
			return dbDao.connect(connId, userId).getColumns(cat, table);
		} catch (Exception ex) {
			return exAdaptor.convert(ex);
		}
	};

	public Route getIndex = (req, res) -> {

		String userId = RequestUtil.getUsername(req);
		String connId = req.queryParams("id");
		String catalogName = req.queryParams("catalog");
		String schemaName = req.queryParams("schema");
		String tableName = req.queryParams("table");
		DbConnInfo dbInfo = dbDao.getDb(connId, userId);
		if (dbInfo == null) {
			throw new AppException("No db connection found with ID=" + connId);
		}
		req.attribute("dbInfo", dbInfo);
		String catSchema = DbUtil.getUniqueName(catalogName, schemaName);
		CatalogInfo catInfo = dbInfo.getCache().getCatalogs().get(catSchema);
		if (catInfo == null) {
			throw new AppException("No database found with name=" + catSchema);
		}
		req.attribute("catalog", catalogName);
		req.attribute("schema", schemaName);
		TableInfo tblInfo = catInfo.getTable(tableName);
		if (tblInfo == null) {
			throw new AppException("No table found with name=" + tableName);
		}
		req.attribute("table", tblInfo);

		List<TreeNode> dbTree = dbDao.getDbTree(userId);
		TreeNode selectedNode = dbTree.stream().filter(n -> n.getId().equals(connId)).findFirst().get()
			.getNodes().stream().filter(n -> n.getText().equals(catalogName)).findFirst().get()
			.getNodes().stream().filter(n -> StringUtils.isNotEmpty(schemaName) ? n.getText().equals(schemaName) : true).findFirst().get()
			.getNodes().stream().filter(n -> n.getText().equals(tableName)).findFirst().get();

		selectedNode.setState("selected", true);
		req.attribute("DB_TREE_DATA", gson.toJson(dbTree));
		return renderContent(req, "table/index.html");
	};

	public Route getQueryHistory = (req, res) -> {

		String userId = RequestUtil.getUsername(req);
		return gson.toJson(userDao.getQueryHistory(userId));
	};

	public Route deleteQueryHistory = (req, res) -> {

		String timestamp = req.queryParams("t");
		String userId = RequestUtil.getUsername(req);
		return userDao.deleteQueryLog(userId, timestamp);
	};

	public Route getCancelQuery = (req, res) -> {

		String queryId = req.queryParams("qId");
		String userId = RequestUtil.getUsername(req);
		String connId = req.queryParams("connId");
		try {
			DbConnection dbConn = dbDao.connect(connId, userId);
			dbConn.cancelQueryById(queryId);
			return 0;
		} catch (Exception ex) {
			LOGGER.error("Error cancelling query: " + queryId, ex);
			return exAdaptor.convert(ex);
		}
	};
}
