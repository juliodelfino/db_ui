package com.delfino.dao;

import java.util.ArrayList;
import java.util.List;

import com.delfino.model.DbSchema;
import com.delfino.util.AppProperties;
import com.delfino.db.JsonDb;

public class UserDbDao {

	private JsonDb<DbSchema> jsonDb = JsonDb.getInstance(AppProperties.get("data_dir"), DbSchema.class);

	public void addUserDb(String userId, String connectionName) {
		List<String> dbList = getUserDbList(userId);		
		if (dbList.isEmpty()) {
			jsonDb.get().getUserDbMap().put(userId, dbList);
		}
		dbList.add(connectionName);
		jsonDb.save();
	}

	public List<String> getUserDbList(String userId) {
		List<String> dbList = jsonDb.get().getUserDbMap().get(userId);
		return dbList == null ? new ArrayList() : dbList;
	}
}
