package com.delfino.db;

import com.delfino.adaptor.ExceptionAdaptor;
import com.delfino.adaptor.ResultSetAdaptor;
import com.delfino.main.Application;
import com.delfino.model.DbInfo;
import com.delfino.model.TableInfo;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbConnection.class);
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

	public Map getDbMetadata() throws SQLException {
        DatabaseMetaData md = getConnection().getMetaData();
        Map<String, TableInfo> tables = new LinkedHashMap();
        ResultSet rs = md.getTables(null, null, "%", null);
        List<String> names = extractValuesFromColumn(rs, 3);
        
        names.stream().forEach(tableName -> tables.put(tableName, new TableInfo(tableName)));
        
        String sql = tables.keySet().stream()
        		.map(t -> String.format(" SELECT '%s', COUNT(*) FROM %s ", t, t))
        		.collect(Collectors.joining(" UNION ALL "));
        stmt = getConnection().createStatement();
        rs = stmt.executeQuery(sql);
        while (rs.next()) {
        	String tableName = rs.getString(1);
        	tables.get(tableName).setRowCount(rs.getInt(2));
        } 
        rs.close();
        return tables;
    }

	private List extractValuesFromColumn(ResultSet rs, int columnIndex) throws SQLException {
		List<Comparable> values = new ArrayList();
        while (rs.next()) {
        	values.add(rs.getString(columnIndex));
        }
        values.sort((x, y) -> x.compareTo(y));
        return values;
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

	public void close() {
		try {
			if (conn != null && !conn.isClosed()) {
				conn.close();
			}
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}
}
