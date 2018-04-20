package com.delfino.controller;

import java.util.List;

import com.delfino.dao.DbInfoDao;
import com.delfino.dao.UserDao;
import com.delfino.model.DbInfo;
import com.delfino.model.TableInfo;
import com.delfino.model.TreeNode;
import com.delfino.util.AppException;
import com.delfino.util.RequestUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import spark.Route;

public class TableController extends ControllerBase {

	private DbInfoDao dbDao = new DbInfoDao();
	private UserDao userDao = new UserDao();
	private Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	public Route getQuery = (req, res) -> {

		String userId = RequestUtil.getUsername(req);
		String sql = req.queryParams("q");
		String connId = req.queryParams("connId");
		userDao.saveQuery(sql);
		return dbDao.connect(connId, userId).executeQuery(sql);
	};

	public Route getColumns = (req, res) -> {

		String userId = RequestUtil.getUsername(req);
		String table = req.queryParams("table");
		String connId = req.queryParams("connId");
		return dbDao.connect(connId, userId).getColumns(table);
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
			dbInfo.setTables(dbDao.connect(connId, userId).getDbMetadata());
			tblInfo = dbInfo.getTable(tableName);
			if (tblInfo == null) {
				throw new AppException("No table found with name=" + tableName);
			}
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
}
