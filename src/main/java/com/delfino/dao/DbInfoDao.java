package com.delfino.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import com.delfino.db.DbConnection;
import com.delfino.db.JsonDb;
import com.delfino.model.DbInfo;
import com.delfino.model.DbSchema;
import com.delfino.util.AppProperties;

import spark.utils.StringUtils;

public class DbInfoDao {

	private JsonDb<DbSchema> jsonDb = JsonDb.getInstance(AppProperties.get("data_dir"), DbSchema.class);
	private Map<String, DbConnection> dbConnMap = new HashMap<>();
	private UserDbDao userDbDao = new UserDbDao();

	public List<DbInfo> getAll() {

		return jsonDb.get().getDatabases().entrySet().stream()
				.map(Entry::getValue)
				.collect(Collectors.toList());
	}
	
	public Map<String, DbInfo> getAll(String userId) {

		List<String> userDbs = userDbDao.getUserDbList(userId);
		Map<String, DbInfo> dbMap = jsonDb.get().getDatabases().entrySet().stream()
				.filter(e -> userDbs.contains(e.getKey()))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
		return dbMap != null ? dbMap : new HashMap();
	}

	public boolean add(DbInfo dbInfo, String userId) {
		if (StringUtils.isEmpty(dbInfo.getConnId())) {
			dbInfo.setConnId(UUID.randomUUID().toString().substring(0, 8));
		}
		jsonDb.get().getDatabases().put(dbInfo.getConnId(), dbInfo);
		userDbDao.addUserDb(userId, dbInfo.getConnId());
		
		return jsonDb.save();
	}

	public boolean update(DbInfo dbInfoUpdate, String userId) {

		DbInfo dbInfo = getDb(dbInfoUpdate.getConnId(), userId);
		dbInfoUpdate.setUsername(dbInfo.getUsername());
		dbInfoUpdate.setPassword(dbInfo.getPassword());
		jsonDb.get().getDatabases().put(dbInfoUpdate.getConnId(), dbInfoUpdate);
		return jsonDb.save();
	}

	public boolean delete(DbInfo dbInfo, String userId) {
		jsonDb.get().getUserDbMap().get(userId).remove(dbInfo.getConnId());
		jsonDb.get().getDatabases().remove(dbInfo.getConnId());
		return jsonDb.save();
	}

	public DbInfo getDb(String connId, String userId) {
		return getAll(userId).get(connId);
	}

	public DbConnection connect(String connId, String userId) throws SQLException {

		DbConnection dbConn = dbConnMap.get(connId);
		if (dbConn == null) {
			dbConn = new DbConnection(getDb(connId, userId));
			dbConnMap.put(connId, dbConn);
		}
		return dbConn;
	}

	public DbConnection connect(DbInfo dbInfo) throws SQLException {

		return new DbConnection(dbInfo);
	}
}
