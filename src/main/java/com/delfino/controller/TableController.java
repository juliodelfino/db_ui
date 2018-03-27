package com.delfino.controller;

import java.util.Arrays;
import java.util.Map;

import com.delfino.dao.DbInfoDao;
import com.delfino.model.DbInfo;
import com.delfino.model.TableInfo;
import com.delfino.util.AppException;
import com.delfino.util.RequestUtil;
import spark.Route;

public class TableController extends ControllerBase {

	private DbInfoDao dbDao = new DbInfoDao();

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
		return renderContent(req, "table/index.html");
	};
}
