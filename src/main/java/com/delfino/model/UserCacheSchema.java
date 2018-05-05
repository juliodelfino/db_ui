package com.delfino.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserCacheSchema {

	private List<SqlLog> queryLogs = new ArrayList();

	/**
	 * @return the queryLogs
	 */
	public List<SqlLog> getQueryLogs() {
		return queryLogs;
	}

	/**
	 * @param queryLogs the queryLogs to set
	 */
	public void setQueryLogs(List<SqlLog> queryLogs) {
		this.queryLogs = queryLogs;
	}

	public void addQueryLog(String sql) {
		queryLogs.add(0, new SqlLog(new Date(), sql));
		if (queryLogs.size() > 50) {
			queryLogs = queryLogs.subList(0, 50);
		}
	}

}
