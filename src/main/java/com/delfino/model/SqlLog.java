package com.delfino.model;

import java.util.Date;

public class SqlLog {
	public Date timestamp;
	public String log;
	
	public SqlLog(Date timestamp, String log) {
		this.timestamp = timestamp;
		this.log = log;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public String getLog() {
		return log;
	}
}
