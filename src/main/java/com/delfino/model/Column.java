package com.delfino.model;

public class Column {
	public String title;
	public boolean primaryKey;
	private boolean blob;

	public Column(String title) {
		this.title = title;
	}

	public boolean isPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(boolean primaryKey) {
		this.primaryKey = primaryKey;
	}

	public boolean isBlob() {
		return blob;
	}

	public void setBlob(boolean blob) {
		this.blob = blob;
	}
}
