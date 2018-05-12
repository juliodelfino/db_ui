package com.delfino.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DbSchemaOld {

	private List<User> users = new ArrayList<>();
	private Map<String, List> userDbMap = new HashMap<>();
	private Map<String, DbConnInfo> databases = new HashMap<>();

	public List<User> getUsers() {
		return users;
	}
	public void setUsers(List<User> users) {
		this.users = users;
	}
	public Map<String, List> getUserDbMap() {
		return userDbMap;
	}
	public void setUserDbMap(Map<String, List> userDbMap) {
		this.userDbMap = userDbMap;
	}
	public Map<String, DbConnInfo> getDatabases() {
		return databases;
	}
	public void setDatabases(Map<String, DbConnInfo> databases) {
		this.databases = databases;
	}
}
