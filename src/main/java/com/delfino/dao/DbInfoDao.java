package com.delfino.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.delfino.db.DbConnection;
import com.delfino.db.JsonDb;
import com.delfino.db.JsonDbFactory;
import com.delfino.model.DbInfo;
import com.delfino.model.DbSchema;
import com.delfino.model.TreeNode;
import com.delfino.util.AppProperties;
import com.delfino.util.Constants;

import spark.utils.StringUtils;

public class DbInfoDao {

	private JsonDb<DbSchema> jsonDb = 
		JsonDbFactory.getInstance(Constants.DATA_JSON, DbSchema.class);
	private Map<String, DbConnection> dbConnMap = new HashMap<>();
	private UserDbDao userDbDao = new UserDbDao();

	public List<DbInfo> getAll() {

		return jsonDb.get().getDatabases().entrySet().stream()
				.map(Entry::getValue)
				.collect(Collectors.toList());
	}
	
	public Map<String, DbInfo> getAll(String userId) {

		Set<String> userDbs = userDbDao.getUserDbList(userId);
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
		dbInfoUpdate.setUrl(dbInfo.getUrl());
		dbInfoUpdate.setDriver(dbInfo.getDriver());
		dbInfoUpdate.setUsername(dbInfo.getUsername());
		dbInfoUpdate.setPassword(dbInfo.getPassword());
		jsonDb.get().getDatabases().put(dbInfoUpdate.getConnId(), dbInfoUpdate);
		return jsonDb.save();
	}

	public boolean delete(DbInfo dbInfo, String userId) {
		DbConnection dbConn = dbConnMap.get(dbInfo.getConnId());
		if (dbConn != null) {
			dbConn.close();
		}
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

	public List getDbTree(String userId) {
		return getAll(userId).values()
			.stream().map(db -> { 
				TreeNode node = new TreeNode(db.getConnId(), db.getConnectionName(), null);
				node.setNodes(db.getTables().values()
					.stream().map(t -> new TreeNode(db.getConnId() + t.getName(), t.getName(), node))
					.collect(Collectors.toList()));
				return node;
			}).sorted((n1, n2) -> n1.getText().compareTo(n2.getText()))
			.collect(Collectors.toList());
	}

	public void updateUserAccess(String connId, String[] users) {
		
		Map<String, Set> userDbMap = jsonDb.get().getUserDbMap();
		userDbMap.entrySet().forEach(e -> e.getValue().remove(connId));
		Stream.of(users).forEach(user -> {
			Set dbs = userDbMap.get(user);
			if (dbs == null) {
				dbs = new HashSet();
			}
			dbs.add(connId);
			userDbMap.put(user, dbs);
		});
		jsonDb.save();
	}
}
