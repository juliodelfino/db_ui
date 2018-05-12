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
import com.delfino.model.CatalogInfo;
import com.delfino.model.DbCacheSchema;
import com.delfino.model.DbConnInfo;
import com.delfino.model.DbConnSchema;
import com.delfino.model.TreeNode;
import com.delfino.model.UserCacheSchema;
import com.delfino.util.AppProperties;
import com.delfino.util.Constants;
import com.delfino.util.Constants.TreeNodeType;

import spark.utils.StringUtils;

public class DbInfoDao {

	private JsonDb<DbConnSchema> jsonDb = 
		JsonDbFactory.getInstance(Constants.DATA_JSON, DbConnSchema.class);
	private Map<String, DbConnection> dbConnMap = new HashMap<>();
	private UserDbDao userDbDao = new UserDbDao();

	public List<DbConnInfo> getAll() {

		return jsonDb.get().getDatabases().entrySet().stream()
				.map(Entry::getValue)
				.collect(Collectors.toList());
	}
	
	public Map<String, DbConnInfo> getAll(String userId) {

		Set<String> userDbs = userDbDao.getUserDbList(userId);
		Map<String, DbConnInfo> dbMap = jsonDb.get().getDatabases().entrySet().stream()
				.filter(e -> userDbs.contains(e.getKey()))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
		return dbMap != null ? dbMap : new HashMap();
	}

	public boolean add(DbConnInfo dbInfo, String userId) {
		if (StringUtils.isEmpty(dbInfo.getConnId())) {
			dbInfo.setConnId(UUID.randomUUID().toString().substring(0, 8));
		}
		jsonDb.get().getDatabases().put(dbInfo.getConnId(), dbInfo);
		userDbDao.addUserDb(userId, dbInfo.getConnId());
		
		return jsonDb.save();
	}

	public boolean update(DbConnInfo dbInfoUpdate, String userId) throws SQLException {

		DbConnInfo dbInfo = getDb(dbInfoUpdate.getConnId(), userId);
		dbInfoUpdate.setUrl(dbInfo.getUrl());
		dbInfoUpdate.setDriver(dbInfo.getDriver());
		dbInfoUpdate.setUsername(dbInfo.getUsername());
		dbInfoUpdate.setPassword(dbInfo.getPassword());
		jsonDb.get().getDatabases().put(dbInfoUpdate.getConnId(), dbInfoUpdate);
		return jsonDb.save();
	}

	public boolean delete(DbConnInfo dbInfo, String userId) {
		DbConnection dbConn = dbConnMap.get(dbInfo.getConnId());
		if (dbConn != null) {
			dbConn.close();
		}
		jsonDb.get().getUserDbMap().get(userId).remove(dbInfo.getConnId());
		jsonDb.get().getDatabases().remove(dbInfo.getConnId());
		return jsonDb.save();
	}


	public DbConnInfo getDb(String connId, String userId) throws SQLException {

		return getDb(connId, userId, false);
	}

	public DbConnInfo getDb(String connId, String userId, boolean refresh) throws SQLException {
		DbConnInfo dbInfo = getAll(userId).get(connId);
		if (dbInfo != null) {
			JsonDb<DbCacheSchema> dbCache = 
				JsonDbFactory.getInstance("dbcache_" + dbInfo.getConnId(), DbCacheSchema.class);
			dbInfo.setCache(dbCache.get());
			if (refresh || dbCache.get().getCatalogs().isEmpty()) {
				updateCache(dbInfo);
			}
		}
		return dbInfo;
	}
	
	public boolean updateCache(DbConnInfo dbInfo) throws SQLException {
		
		JsonDb<DbCacheSchema> dbCache = 
			JsonDbFactory.getInstance("dbcache_" + dbInfo.getConnId(), DbCacheSchema.class);
		dbInfo.setCache(dbCache.get());
		DbConnection dbConn = connect(dbInfo);
		dbCache.get().setCatalogs(dbConn.getDbCatalogs());
		boolean result = dbCache.save();
		dbInfo.setCache(dbCache.get());
		return result;
	}

	public boolean updateTableCache(DbConnInfo dbConnInfo, CatalogInfo cat) throws SQLException {
		
		JsonDb<DbCacheSchema> dbCache = 
				JsonDbFactory.getInstance("dbcache_" + dbConnInfo.getConnId(), DbCacheSchema.class);
		DbConnection dbConn = connect(dbConnInfo);
		cat.setTables(dbConn.getDbTables(cat.getName()));
		return dbCache.save();
	}

	public DbConnection connect(String connId, String userId) throws SQLException {

		DbConnection dbConn = dbConnMap.get(connId);
		if (dbConn == null) {
			dbConn = new DbConnection(getDb(connId, userId));
			dbConnMap.put(connId, dbConn);
		}
		return dbConn;
	}

	public DbConnection connect(DbConnInfo dbInfo) throws SQLException {

		return new DbConnection(dbInfo);
	}

	public List getDbTree(String userId) {
		return getAll(userId).values()
			.stream().map(db -> { 
				TreeNode node = new TreeNode(db.getConnId(), null, null, db.getConnectionName(), TreeNodeType.DBCONN);
				node.setNodes(getCatalogTree(db, node));
				return node;
			}).sorted((n1, n2) -> n1.getText().compareTo(n2.getText()))
			.collect(Collectors.toList());
	}

	private List<TreeNode> getCatalogTree(DbConnInfo db, TreeNode parent) {
		return db.getCache().getCatalogs().values()
		.stream().map(cat -> {
			
			TreeNode node = new TreeNode(db.getConnId(), cat.getName(), null, cat.getName(), TreeNodeType.CATALOG);
			node.setNodes(getTableTree(db, cat, node));
			return node;
		})
		.collect(Collectors.toList());
	}

	private List<TreeNode> getTableTree(DbConnInfo db, CatalogInfo cat, TreeNode node) {
		return cat.getTables().values()
		.stream().map(t -> new TreeNode(db.getConnId(), cat.getName(),  t.getName(), t.getName(), TreeNodeType.TABLE))
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
