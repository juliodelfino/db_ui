package com.delfino.model;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.delfino.util.DbUtil;

public class CatalogInfo {
	
	private static final String NO_LABEL = "DEFAULT";

	private Map<String, TableInfo> tables = new LinkedHashMap<>();

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
	
	public String getCatalogLabel() {
		return StringUtils.isEmpty(catalog) ? NO_LABEL : catalog;
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
	
	public String getSchemaLabel() {
		return StringUtils.isEmpty(schema) ? NO_LABEL : schema;
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
