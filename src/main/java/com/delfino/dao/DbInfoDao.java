package com.delfino.dao;

import com.delfino.model.DbConnection;
import com.delfino.model.DbInfo;
import com.delfino.model.JsonDbModel;
import com.delfino.util.AppProperties;
import com.delfino.util.JsonDb;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import spark.utils.StringUtils;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.hdfs.server.datanode.dataNodeHome_jsp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DbInfoDao {

	private JsonDb<JsonDbModel> jsonDb = JsonDb.getInstance(AppProperties.get("data_dir"), JsonDbModel.class);
	private Map<String, DbConnection> dbConnMap = new HashMap<>();
	private UserDbDao userDbDao = new UserDbDao();
	
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
