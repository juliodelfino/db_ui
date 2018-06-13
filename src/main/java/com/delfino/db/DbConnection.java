package com.delfino.db;

import java.lang.reflect.Method;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.delfino.adaptor.CatalogInfoListAdaptor;
import com.delfino.adaptor.ResultSetAdaptor;
import com.delfino.adaptor.TableInfoListAdaptor;
import com.delfino.model.CatalogInfo;
import com.delfino.model.Column;
import com.delfino.model.DbConnInfo;
import com.delfino.model.TableInfo;
import com.delfino.util.CryptUtil;
import com.delfino.util.DbUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class DbConnection {
	
    private static final Logger LOGGER = LoggerFactory.getLogger(DbConnection.class);

    private ResultSetAdaptor adaptor = new ResultSetAdaptor();
    private CatalogInfoListAdaptor catInfoAdaptor = new CatalogInfoListAdaptor();
    private TableInfoListAdaptor tblInfoAdaptor = new TableInfoListAdaptor();
    private Gson gson = new GsonBuilder()
    		.setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    private DbConnInfo dbInfo;
    Connection conn = null;

    public DbConnection(DbConnInfo dbInfo) throws SQLException {
        this.dbInfo = dbInfo;
    }
    
    public Connection getConnection() throws SQLException {
    	try {
    	if (conn == null || conn.isClosed()) {
    		String pass = dbInfo.getPassword();
    		if (dbInfo.isEncrypted()) {
    			try {
					pass = CryptUtil.decrypt(pass);
				} catch (GeneralSecurityException e) {
					LOGGER.error("Error in decrypting db password", e);
				}
    		}
    		conn = DriverManager
    	        .getConnection(dbInfo.getUrl(),dbInfo.getUsername(), pass);
    	}
    	return conn;
    	} catch (SQLException ex) {
    		LOGGER.error("Error in connecting to {}", dbInfo.getUrl());
    		throw ex;
    	}
    }

	public boolean testConnection() throws SQLException {
		getConnection();
		return true;
	}

    public String executeQuery(String sql, CatalogInfo cat) throws SQLException, JsonProcessingException  {

        ResultSet rs = null;
        String result = "";
        try {
        	DbUtil.setCatalogInfo(getConnection(), cat);
            Statement stmt = getConnection().createStatement();
            rs = stmt.executeQuery(sql);
            Map resultMap = adaptor.convert(rs, dbInfo.getCache().getCatalogs().get(cat.getCatalog()));
            result = gson.toJson(resultMap);
        }
        finally {
        	if (rs != null) {
        		rs.close();
        	}
        }
        return result;
    }

	public int executeUpdate(String sql, CatalogInfo cat) throws SQLException {
		
        int rs = 0;
    	DbUtil.setCatalogInfo(getConnection(), cat);
        Statement stmt = getConnection().createStatement();
        rs = stmt.executeUpdate(sql);
        stmt.close();
        return rs;
	}

	public Map getDbCatalogs() throws SQLException {
        DatabaseMetaData md = getConnection().getMetaData();
        Map<String, CatalogInfo> catalogMap = new LinkedHashMap();
        ResultSet rs = md.supportsSchemasInTableDefinitions() ? md.getSchemas() : md.getCatalogs();
        Collection<CatalogInfo> catalogs = catInfoAdaptor.convert(rs);
        rs.close();
        catalogs.stream().forEach(t -> {
        	catalogMap.put(t.getName(), t);
        });
        catalogMap.putAll(getEmptyCatalogs());
        return catalogMap;
    }
	
	private Map getEmptyCatalogs() throws SQLException {
        DatabaseMetaData md = getConnection().getMetaData();
        Map<String, CatalogInfo> catalogMap = new LinkedHashMap();
        ResultSet rs = md.getTables("", "", "%", null);
        List<TableInfo> tableList = tblInfoAdaptor.convert(rs);
        tableList.sort((t1, t2) -> t1.getName().compareTo(t2.getName()));
        rs.close();
        for (TableInfo t : tableList) {
        	String catSchema = DbUtil.getUniqueName(t.getTableCatalog(), t.getTableSchema());
        	CatalogInfo cat = catalogMap.get(catSchema);
        	if (cat == null) {
        		cat = new CatalogInfo(t.getTableCatalog(), t.getTableSchema());
        		catalogMap.put(catSchema, cat);
        	}
        	cat.getTables().put(t.getName(), t);
        	if (t.getTableType().equals("TABLE")) {
	        	try {
					t.setPrimaryKeys(getPrimaryKeys(cat, t.getName()));
				} catch (JsonProcessingException e) {
		        	LOGGER.error("Error retrieving primary keys of this table: " + t.getName(), e);
				}
        	}
        }
        return catalogMap;
    }

	public Map<String, TableInfo> getDbTables(CatalogInfo catInfo) throws SQLException {
		DatabaseMetaData md = getConnection().getMetaData();
        Map<String, TableInfo> tableMap = new LinkedHashMap();
        ResultSet rs = md.getTables(catInfo.getCatalog(), catInfo.getSchema(), "%", null);
        List<TableInfo> tableList = tblInfoAdaptor.convert(rs);
        rs.close();
        for (TableInfo t : tableList) {
        	if (t.getTableType().equals("TABLE")) {
	        	try {
					t.setPrimaryKeys(getPrimaryKeys(catInfo, t.getName()));
				} catch (JsonProcessingException e) {
		        	LOGGER.error("Error retrieving primary keys of this table: " + t.getName(), e);
				}
        	}
        	tableMap.put(t.getName(), t);
        }
        
        return tableMap;
	}
	
	public void updateRowCount(Map<String, TableInfo> allTables) {
		
		Map<String, TableInfo> tableMap = allTables.entrySet().stream()
				.filter(e -> e.getValue().getTableType().equals("TABLE"))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, 
						(t1, t2) -> t1, LinkedHashMap::new));
        //THIS SLOWS DOWN THE RETRIEVAL OF TABLES
        try {
        	queryAllRowCounts(tableMap);
        } catch (SQLException sqlEx) {
        	LOGGER.error(sqlEx.getMessage(), sqlEx);
        	queryRowCountOneByOne(tableMap);
        }
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

	public String getColumns(CatalogInfo cat, String table) throws SQLException, JsonProcessingException {
        DatabaseMetaData md = getConnection().getMetaData();
        ResultSet rs = md.getColumns(cat.getCatalog(), cat.getSchema(), table, "%");
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
	
	public Set getPrimaryKeys(CatalogInfo cat, String table) throws SQLException, JsonProcessingException   {
        DatabaseMetaData md = getConnection().getMetaData();
        ResultSet rs = md.getPrimaryKeys(cat.getCatalog(), cat.getSchema(), table);
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

	private static Map filterByColumns(Map map, List<String> filterCols) {
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
            				"columns", Arrays.asList(new Column("col1")),
        					"data", Arrays.asList(Arrays.asList(tmp + ""))));
        		}
        	}
        	else {
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
