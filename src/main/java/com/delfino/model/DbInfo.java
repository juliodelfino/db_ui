package com.delfino.model;

public class DbInfo {

    private String driver;
    private String url;
    private String username;
    private String password;
    private String connectionName;
    private String connId;

	private transient String status = "Disconnected";
    private transient DbCacheSchema cache = new DbCacheSchema();

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

	public DbCacheSchema getCache() {
		return cache;
	}

	public void setCache(DbCacheSchema dbCacheSchema) {
		this.cache = dbCacheSchema;
	}

	@Override
	public String toString() {
		return "DbInfo [driver=" + driver + ", url=" + url + ", username=" + username + ", password=" + password
			+ ", connectionName=" + connectionName + ", connId=" + connId + "]";
	}

	public TableInfo getTable(String tableName) {
		return cache.getTables().get(tableName);
	}
	
	public int getTableCount() {
		return cache.getTables().size();
	}
}
