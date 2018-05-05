package com.delfino.model;

import java.util.Set;

public class TableInfo {

	private String name;
	private int rowCount;
	private String tableCatalog;
	private String tableType;
	private String tableSchema;
	private String remarks;
	private Set<String> primaryKeys;
	
	public TableInfo(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getRowCount() {
		return rowCount;
	}
	public void setRowCount(int rowCount) {
		this.rowCount = rowCount;
	}
	

	
	public String getTableType() {
		return tableType;
	}

	public void setTableType(String tableType) {
		this.tableType = tableType;
	}

	public String getTableSchema() {
		return tableSchema;
	}

	public void setTableSchema(String tableSchema) {
		this.tableSchema = tableSchema;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getTableCatalog() {
		return tableCatalog;
	}

	public void setTableCatalog(String tableCatalog) {
		this.tableCatalog = tableCatalog;
	}
	
	
	
	@Override
	public String toString() {
		return "TableInfo [name=" + name + ", rowCount=" + rowCount + ", tableCatalog=" + tableCatalog
				+ ", tableType=" + tableType + ", tableSchema=" + tableSchema + ", remarks=" + remarks + "]";
	}

	public Set getPrimaryKeys() {
		return primaryKeys;
	}
	
	public void setPrimaryKeys(Set primaryKeys) {
		this.primaryKeys = primaryKeys;
	}
}
