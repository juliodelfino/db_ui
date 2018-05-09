package com.delfino.controller;

import java.util.List;

import com.delfino.adaptor.ExceptionAdaptor;
import com.delfino.dao.DbInfoDao;
import com.delfino.dao.UserDao;
import com.delfino.db.DbConnection;
import com.delfino.model.DbInfo;
import com.delfino.model.TableInfo;
import com.delfino.model.TreeNode;
import com.delfino.util.AppException;
import com.delfino.util.RequestUtil;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import spark.Route;

public class TableController extends ControllerBase {
	
    private ExceptionAdaptor exAdaptor = new ExceptionAdaptor();
	private DbInfoDao dbDao = new DbInfoDao();
	private UserDao userDao = new UserDao();
	private Gson gson = new GsonBuilder()
    		.setDateFormat("yyyy-MM-dd HH:mm:ss")
			.setPrettyPrinting().disableHtmlEscaping().create();

	public Route getQuery = (req, res) -> {

		String userId = RequestUtil.getUsername(req);
		String sqlQuery = req.queryParams("q");
		String connId = req.queryParams("connId");
		DbConnection dbConn = dbDao.connect(connId, userId);
		String result = "";
		sqlQuery = sqlQuery.replaceAll("\n", "");
		for (String sql : sqlQuery.split(";")) {
			sql = sql.trim();
			if (sql.matches("(SELECT|select).*")) {
				try {
					result = dbConn.executeQuery(sql);
					userDao.saveQuery(sql, userId);
				} catch (Exception ex) {
					return exAdaptor.convert(ex);
				}
			} else if (sql.matches("(INSERT|insert|UPDATE|update|DELETE|delete|"
					+ "CREATE|create|ALTER|alter|DROP|drop).*") && 
				RequestUtil.getUser(req).isAdmin()) {
				try {
					result = dbConn.executeUpdate(sql) + " row(s) updated";
					result = gson.toJson(ImmutableMap.of("message", result));
					userDao.saveQuery(sql, userId);
				} catch (Exception ex) {
					return exAdaptor.convert(ex);
				}
			} else {
				return dbConn.getData(sql);
				//return exAdaptor.convert(new SQLException("Unknown starting keyword: " + sql));
			}
		}
		return result;
	};

	public Route getColumns = (req, res) -> {

		String userId = RequestUtil.getUsername(req);
		String table = req.queryParams("table");
		String connId = req.queryParams("connId");
		try {
			return dbDao.connect(connId, userId).getColumns(table);
		} catch (Exception ex) {
			return exAdaptor.convert(ex);
		}
	};

	public Route getIndex = (req, res) -> {

		String userId = RequestUtil.getUsername(req);
		String connId = req.queryParams("id");
		String tableName = req.queryParams("table");
		DbInfo dbInfo = dbDao.getDb(connId, userId);
		if (dbInfo == null) {
			throw new AppException("No database found with ID=" + connId);
		}
		req.attribute("dbInfo", dbInfo);
		TableInfo tblInfo = dbInfo.getTable(tableName);
		if (tblInfo == null) {
			throw new AppException("No table found with name=" + tableName);
		}
		req.attribute("table", tblInfo);
		
		List<TreeNode> list = dbDao.getDbTree(userId);
		TreeNode selectedDb = list.stream().filter(n -> n.getId().equals(connId)).findFirst().get();    
		TreeNode selectedNode = selectedDb.getNodes().stream().filter(n -> n.getText().equals(tableName)).findFirst().get();
		selectedDb.setState("expanded", true);
		selectedDb.setState("selected", true);
		selectedNode.setState("selected", true);
		req.attribute("DB_TREE_DATA", gson.toJson(list));
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
}
