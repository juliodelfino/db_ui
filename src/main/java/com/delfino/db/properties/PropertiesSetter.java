package com.delfino.db.properties;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class PropertiesSetter {

    private String dbType;

    public String getDbType() {
        return dbType;
    }

    public abstract void setProperties(Connection connection) throws SQLException;

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }
}
