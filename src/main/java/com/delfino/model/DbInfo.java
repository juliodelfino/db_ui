package com.delfino.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DbInfo {

    private String driver;
    private String url;
    private String username;
    private String password;
    private String connectionName;
    private String connId;

	private transient String status = "Disconnected";
    private transient Map<String, TableInfo> tableMap = new HashMap();

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConnectionName() {
        return connectionName;
    }

    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }

	public String getConnId() {
		return connId;
	}

	public void setConnId(String connId) {
		this.connId = connId;
	}
	
	

    
    public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getTableCount() {
		return tableMap.size();
	}

	public Map getTables() {
		return tableMap;
	}

	public void setTables(Map<String, TableInfo> meta) {
		this.tableMap = meta;
	}

	public TableInfo getTable(String tableName) {
		return tableMap.get(tableName);
	}

	@Override
	public String toString() {
		return "DbInfo [driver=" + driver + ", url=" + url + ", username=" + username + ", password=" + password
				+ ", connectionName=" + connectionName + ", connId=" + connId + "]";
	}
}
