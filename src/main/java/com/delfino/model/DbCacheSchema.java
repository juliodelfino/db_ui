package com.delfino.model;

import java.util.HashMap;
import java.util.Map;

public class DbCacheSchema {
	
	private Map<String, TableInfo> tables = new HashMap<>();

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
	
	
}
