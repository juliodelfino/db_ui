package com.delfino.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DbSchema {

	private Map<String, User> users = new HashMap<>();
	private Map<String, List> userDbMap = new HashMap<>();
	private Map<String, DbInfo> databases = new HashMap<>();

	public Map<String, User> getUsers() {
		return users;
	}
	public void setUsers(Map<String, User> users) {
		this.users = users;
	}
	public Map<String, List> getUserDbMap() {
		return userDbMap;
	}
	public void setUserDbMap(Map<String, List> userDbMap) {
		this.userDbMap = userDbMap;
	}
	public Map<String, DbInfo> getDatabases() {
		return databases;
	}
	public void setDatabases(Map<String, DbInfo> databases) {
		this.databases = databases;
	}
}
