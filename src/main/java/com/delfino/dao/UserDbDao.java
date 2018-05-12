package com.delfino.dao;

import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.delfino.db.JsonDb;
import com.delfino.db.JsonDbFactory;
import com.delfino.model.DbConnSchema;
import com.delfino.util.AppProperties;
import com.delfino.util.Constants;

public class UserDbDao {

	private JsonDb<DbConnSchema> jsonDb = JsonDbFactory.getInstance(Constants.DATA_JSON, DbConnSchema.class);

	public void addUserDb(String userId, String connectionName) {
		Set<String> dbList = getUserDbList(userId);		
		if (dbList.isEmpty()) {
			jsonDb.get().getUserDbMap().put(userId, dbList);
		}
		dbList.add(connectionName);
		jsonDb.save();
	}

	/**
	 * Gets a list of database connIds from the specified userId.
	 * @param userId
	 * @return
	 */
	public Set<String> getUserDbList(String userId) {
		Set<String> dbList = jsonDb.get().getUserDbMap().get(userId);
		return dbList == null ? new HashSet() : dbList;
	}
	
	/**
	 * Gets a list of user IDs from the specified database connId.
	 * @param connId
	 * @return
	 */
	public List<String> getDbUserList(String connId) {
		return jsonDb.get().getUserDbMap().entrySet().stream()
				.filter(e -> e.getValue().contains(connId)).map(Entry::getKey)
				.collect(Collectors.toList());
	}
}
