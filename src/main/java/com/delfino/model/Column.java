package com.delfino.model;

public class Column {
	public String title;
	public boolean primaryKey;

	public boolean isPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(boolean primaryKey) {
		this.primaryKey = primaryKey;
	}

	public Column(String title) {
		this.title = title;
	}
}
