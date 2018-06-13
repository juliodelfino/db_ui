package com.delfino.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class DbCacheSchema {
	
	private Map<String, CatalogInfo> catalogs = new LinkedHashMap<>();

	/**
	 * @return the catalogs
	 */
	public Map<String, CatalogInfo> getCatalogs() {
		return catalogs;
	}

	/**
	 * @param catalogs the catalogs to set
	 */
	public void setCatalogs(Map<String, CatalogInfo> catalogs) {
		this.catalogs = catalogs;
	}
}
