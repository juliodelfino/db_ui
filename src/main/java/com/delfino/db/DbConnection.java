package com.delfino.db;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.delfino.adaptor.ResultSetAdaptor;
import com.delfino.model.Column;
import com.delfino.model.DbInfo;
import com.delfino.model.TableInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class DbConnection {

    private ResultSetAdaptor adaptor = new ResultSetAdaptor();
    private static final Logger LOGGER = LoggerFactory.getLogger(DbConnection.class);
    private Gson gson = new GsonBuilder()
    		.setDateFormat("yyyy-MM-dd HH:mm:ss").create();
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

    public String executeQuery(String sql) throws Exception  {

        ResultSet rs = null;
        String result = "";
        try {
            Statement stmt = getConnection().createStatement();
            rs = stmt.executeQuery(sql);
            Map resultMap = adaptor.convert(rs, dbInfo);
            result = gson.toJson(resultMap);
        }
        finally {
        	if (rs != null) {
        		rs.close();
        	}
        }
        return result;
    }

	public int executeUpdate(String sql) throws SQLException {
		
        int rs = 0;
        Statement stmt = getConnection().createStatement();
        rs = stmt.executeUpdate(sql);
        stmt.close();
        return rs;
	}

	public Map getDbMetadata() throws SQLException {
        DatabaseMetaData md = getConnection().getMetaData();
        Map<String, TableInfo> tableMap = new LinkedHashMap();
        ResultSet rs = md.getTables(null, null, "%", null);
        List<TableInfo> tables = toTableInfos(rs).stream()
        		.filter(t -> "TABLE".equals(t.getTableType()))
        		.collect(Collectors.toList());
        
        for (TableInfo t : tables) {
        	try {
				t.setPrimaryKeys(getPrimaryKeys(t.getName()));
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	tableMap.put(t.getName(), t);
        }
        tables.stream().forEach(t -> {
        	tableMap.put(t.getName(), t);
        });
        
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

	public String getColumns(String table) throws SQLException, JsonProcessingException {
        DatabaseMetaData md = getConnection().getMetaData();
        ResultSet rs = md.getColumns(null, null, table, "%");
        try {
			Map map = adaptor.convert(rs);
			map = filterByColumns(map, 
				Arrays.asList("COLUMN_NAME", "TYPE_NAME", "COLUMN_SIZE", 
						"IS_NULLABLE", "ORDINAL_POSITION", "IS_AUTOINCREMENT"));
			return gson.toJson(map);
		} finally {
			if (rs != null) {
				rs.close();
			}
		}
	}
	
	public Set getPrimaryKeys(String table) throws SQLException, JsonProcessingException   {
        DatabaseMetaData md = getConnection().getMetaData();
        ResultSet rs = md.getPrimaryKeys(null, null, table);
        try {
			Map map = adaptor.convert(rs);
			List<List> keys = (List) filterByColumns(map, Arrays.asList("COLUMN_NAME")).get("data");
			return !keys.isEmpty() ? new HashSet((List)keys.stream()
					.flatMap(List::stream).collect(Collectors.toList())) : new HashSet();
		} finally {
			if (rs != null) {
				rs.close();
			}
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

	public Object getData(String sql) throws SQLException, JsonProcessingException, ReflectiveOperationException {

        ResultSet rs = null;
        String result = "";
        try {
        	DatabaseMetaData md = getConnection().getMetaData();
        	Method m = md.getClass().getMethod(sql);
        	if (m != null) {
        		Object tmp = m.invoke(md);
        		if (tmp instanceof ResultSet) {
            		result = gson.toJson(adaptor.convert((ResultSet) tmp));
        		} else {
            		result = gson.toJson(ImmutableMap.of(
            				"columns", Arrays.asList(new Column("yo")),
        					"data", Arrays.asList(Arrays.asList(tmp + ""))));
        		}
        	}
        	else if (sql.startsWith("TYPEINFO")) {
        		result = gson.toJson(adaptor.convert(md.getTypeInfo()));
        	} else if (sql.startsWith("SCHEMAS")) {
        		result = gson.toJson(adaptor.convert(md.getSchemas()));
        	} else if (sql.startsWith("CATALOGS")) {
        		result = gson.toJson(adaptor.convert(md.getCatalogs()));
        	} else if (sql.startsWith("CLIENTINFO")) {
        		result = gson.toJson(adaptor.convert(md.getClientInfoProperties()));
        	} else {
        		throw new SQLException("unknown command: " + sql);
        	}
        }
        finally {
        	if (rs != null) {
        		rs.close();
        	}
        }
        return result;
	}
}
