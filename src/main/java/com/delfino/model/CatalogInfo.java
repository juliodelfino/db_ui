package com.delfino.model;

import java.util.HashMap;
import java.util.Map;

public class CatalogInfo {

	private Map<String, TableInfo> tables = new HashMap<>();

	private String name;
	
	/**
	 * @return the tables
	 */
	public Map<String, TableInfo> getTables() {
		return tables;
	}

	/**
	 * @param tables the tables to set
	 */
	public void setTables(Map<String, TableInfo> tables) {
		this.tables = tables;
	}

	public TableInfo getTable(String tableName) {
		return tables.get(tableName);
	}
	
	public int getTableCount() {
		return tables.size();
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
}
