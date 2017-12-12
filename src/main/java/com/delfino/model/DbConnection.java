package com.delfino.model;

import com.delfino.adaptor.ExceptionAdaptor;
import com.delfino.adaptor.ResultSetAdaptor;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DbConnection {

    private ResultSetAdaptor adaptor = new ResultSetAdaptor();
    private ExceptionAdaptor exAdaptor = new ExceptionAdaptor();
    private DbInfo dbInfo;
    Connection conn = null;
    Statement stmt;

    public DbConnection(DbInfo dbInfo) throws SQLException {
        this.dbInfo = dbInfo;
        //test connection
        getConnection();
    }
    
    private Connection getConnection() throws SQLException {
    	if (conn == null || conn.isClosed()) {
    		conn = DriverManager
    	            .getConnection(dbInfo.getUrl(),dbInfo.getUsername(), dbInfo.getPassword());
    	}
    	return conn;
    }

    public String executeQuery(String sql) throws SQLException  {

        ResultSet rs = null;
        String result = "";
        try {
            stmt = getConnection().createStatement();
            rs = stmt.executeQuery(sql);
            result = adaptor.convert(rs);
        } catch(Exception ex) {
            result = exAdaptor.convert(ex);
        }

        finally {
        	if (rs != null) {
        		rs.close();
        	}
        }

        return result;
    }

	public List getDbMetadata() throws SQLException {
        DatabaseMetaData md = getConnection().getMetaData();
        List<String> tables = new ArrayList();
        ResultSet rs = md.getTables(null, null, "%", null);
        while (rs.next()) {
            tables.add(rs.getString(3));
        }
        rs.close();
        tables.sort((n1, n2) -> n1.compareTo(n2));
        return tables;
    }

	public String getColumns(String table) throws SQLException {
        DatabaseMetaData md = getConnection().getMetaData();
        ResultSet rs = md.getColumns(null, null, table, "%");
        try {
			return adaptor.convert(rs);
		} catch (JsonProcessingException e) {
			return exAdaptor.convert(e);
		}
	}
}
