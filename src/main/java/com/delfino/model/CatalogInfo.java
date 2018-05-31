package com.delfino.model;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;

import com.delfino.util.DbUtil;

public class CatalogInfo {

	private Map<String, TableInfo> tables = new HashMap<>();

	private String catalog;
	private String schema;
	
	public CatalogInfo(){}
	
	public CatalogInfo(String catalog, String schema) {
		this.catalog = catalog;
		this.schema = schema;
	}

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
	 * @return the catalog
	 */
	public String getCatalog() {
		return catalog;
	}

	/**
	 * @param catalog the catalog to set
	 */
	public void setCatalog(String catalog) {
		this.catalog = catalog;
	}

	/**
	 * @return the schema
	 */
	public String getSchema() {
		return schema;
	}

	/**
	 * @param schema the schema to set
	 */
	public void setSchema(String schema) {
		this.schema = schema;
	}

	public String getName() {
		return DbUtil.getUniqueName(catalog, schema);
	}
}
