package com.delfino.model;

import java.util.List;

public class DbWithUsers extends DbInfo {

	private List<String> users;

	public List<String> getUsers() {
		return users;
	}

	public void setUsers(List<String> users) {
		this.users = users;
	}
}
