package com.delfino.db;

import com.delfino.adaptor.ExceptionAdaptor;
import com.delfino.adaptor.ResultSetAdaptor;
import com.delfino.main.Application;
import com.delfino.model.Column;
import com.delfino.model.DbInfo;
import com.delfino.model.TableInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
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
    private Gson gson = new GsonBuilder().create();
    private DbInfo dbInfo;
    Connection conn = null;

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
            Statement stmt = getConnection().createStatement();
            rs = stmt.executeQuery(sql);
            result = gson.toJson(adaptor.convert(rs));
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
        Map<String, TableInfo> tableMap = new LinkedHashMap();
        ResultSet rs = md.getTables(null, null, "%", null);
        List<TableInfo> tables = toTableInfos(rs).stream()
        		.filter(t -> "TABLE".equals(t.getTableType()))
        		.collect(Collectors.toList());
        
        tables.stream().forEach(t -> tableMap.put(t.getName(), t));
        
        try {
        	queryAllRowCounts(tableMap);
        } catch (SQLException sqlEx) {
        	LOGGER.error(sqlEx.getMessage(), sqlEx);
        	queryRowCountOneByOne(tableMap);
        }
        return tableMap;
    }

	private void queryRowCountOneByOne(Map<String, TableInfo> tableMap) {
        
		tableMap.values().stream().forEach(t -> {
			
	        String sql = String.format(" SELECT COUNT(*) FROM \"%s\" ", t.getName());
	        Statement stmt;
			try {
				stmt = getConnection().createStatement();
		        ResultSet rs = stmt.executeQuery(sql);
		        rs.next();
		        t.setRowCount((int)rs.getLong(1));
		        rs.close();
			} catch (SQLException e) {
				LOGGER.error("Error during query: " + sql, e);
			}
		});
	}

	private void queryAllRowCounts(Map<String, TableInfo> tableMap) throws SQLException {
		
        String sql = tableMap.keySet().stream()
        		.map(t -> String.format(" SELECT '%s', COUNT(*) FROM %s ", t, t))
        		.collect(Collectors.joining(" UNION ALL "));
        Statement stmt = getConnection().createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
        	String tableName = rs.getString(1);
        	tableMap.get(tableName).setRowCount(rs.getInt(2));
        } 
        rs.close();
	}

	private List<TableInfo> toTableInfos(ResultSet rs) throws SQLException {
		List<TableInfo> values = new ArrayList();
        while (rs.next()) {
        	TableInfo tbl = new TableInfo(rs.getString("TABLE_NAME"));
        	tbl.setTableCatalog(rs.getString("TABLE_CAT"));
        	tbl.setTableSchema(rs.getString("TABLE_SCHEM"));
        	tbl.setTableType(rs.getString("TABLE_TYPE"));
        	tbl.setRemarks(rs.getString("REMARKS"));
        	values.add(tbl);
        }
        rs.close();
        values.sort((x, y) -> x.getName().compareTo(y.getName()));
        return values;
	}

	public String getColumns(String table) throws SQLException {
        DatabaseMetaData md = getConnection().getMetaData();
        ResultSet rs = md.getColumns(null, null, table, "%");
        try {
			Map map = adaptor.convert(rs);
			map = filterByColumns(map, 
				Arrays.asList("COLUMN_NAME", "TYPE_NAME", "COLUMN_SIZE", "IS_NULLABLE"));
			return gson.toJson(map);
		} catch (JsonProcessingException e) {
			return exAdaptor.convert(e);
		}
	}

	private Map filterByColumns(Map map, List<String> filterCols) {
		Map newMap = new HashMap();
		List newCols = new ArrayList();
		List newData = new ArrayList();
		List<Column> cols = (List) map.get("columns");
		List<List> data = (List) map.get("data");
		List<Integer> colIndices = new ArrayList();
		for (int i=0; i<cols.size(); i++) {
			Column col = cols.get(i);
			if (filterCols.contains(col.title)) {
				newCols.add(col);
				colIndices.add(i);
			}
		}
		for (List row : data) {
			newData.add(colIndices.stream().map(i -> row.get(i))
				.collect(Collectors.toList()));
		}
		
		newMap.put("columns", newCols);
		newMap.put("data", newData);
		return newMap;
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
