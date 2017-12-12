package com.delfino.dao;

import java.util.ArrayList;
import java.util.List;

import com.delfino.model.JsonDbModel;
import com.delfino.util.AppProperties;
import com.delfino.util.JsonDb;

public class UserDbDao {

	private JsonDb<JsonDbModel> jsonDb = JsonDb.getInstance(AppProperties.get("data_dir"), JsonDbModel.class);

	public void addUserDb(String userId, String connectionName) {
		List<String> dbList = getUserDbList(userId);
		dbList.add(connectionName);
		jsonDb.save();
	}

	public List<String> getUserDbList(String userId) {
		List<String> dbList = jsonDb.get().getUserDbMap().get(userId);
		if (dbList == null) {
			dbList = new ArrayList();
			jsonDb.get().getUserDbMap().put(userId, dbList);
			jsonDb.save();
		}
		return dbList;
	}
}
