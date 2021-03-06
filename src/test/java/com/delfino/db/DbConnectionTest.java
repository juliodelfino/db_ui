package com.delfino.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.delfino.model.CatalogInfo;
import com.delfino.model.DbConnInfo;
import com.delfino.util.TestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;

public class DbConnectionTest {

	private Connection conn;
	private DbConnection sut;
	private DatabaseMetaData dm;
	
	@Before
	public void setup() throws SQLException {

	    conn = mock(Connection.class);
		DbConnInfo dbInfo = new DbConnInfo();
		dbInfo.setUsername("root");
		dbInfo.setPassword("yeah");
		dbInfo.setUrl("jdbc:mysql://localhost");
		
		dm = mock(DatabaseMetaData.class);	
		when(conn.getMetaData()).thenReturn(dm);

		DbConnection dbConn = new DbConnection(dbInfo);
		sut = Mockito.spy(dbConn);
	}
	
    @After
    public void teardown() throws SQLException {
    	conn.close();
    }
	
	@Test
	public void testConnect() throws SQLException {
		doReturn(conn).when(sut).getConnection();
		assertEquals(conn, sut.getConnection());
	}
	
	@Test
	public void testGetDbCatalogs() throws SQLException {

		doReturn(conn).when(sut).getConnection();
		String catalog = "mysql";
		ResultSet rs = mock(ResultSet.class);
		when(dm.getCatalogs()).thenReturn(rs);
		when(rs.getString("TABLE_CAT")).thenReturn(catalog);
		when(rs.next()).thenAnswer(TestUtils.getMockResultSet_next(1));
		
		ResultSetMetaData rsmd = mock(ResultSetMetaData.class);
		when(rs.getMetaData()).thenReturn(rsmd);
		when(rsmd.getColumnCount()).thenReturn(1);
		when(rsmd.getColumnLabel(1)).thenReturn("TABLE_CAT");
		
		//test
		Map map = sut.getDbCatalogs();
		assertTrue(map.keySet().contains(catalog));
	}
	
	@Test
	public void testGetDbCatalogAndSchema() throws SQLException {

		doReturn(conn).when(sut).getConnection();
		String catalog = "mysql";
		ResultSet rs = mock(ResultSet.class);
		when(dm.getCatalogs()).thenReturn(rs);
		when(rs.getString("TABLE_CATALOG")).thenReturn(catalog);
		when(rs.getString("TABLE_SCHEM")).thenReturn("");
		when(rs.next()).thenAnswer(TestUtils.getMockResultSet_next(1));
		
		ResultSetMetaData rsmd = mock(ResultSetMetaData.class);
		when(rs.getMetaData()).thenReturn(rsmd);
		when(rsmd.getColumnCount()).thenReturn(2);
		when(rsmd.getColumnLabel(1)).thenReturn("TABLE_CATALOG");
		when(rsmd.getColumnLabel(2)).thenReturn("TABLE_SCHEM");
		
		//test
		Map map = sut.getDbCatalogs();
		assertTrue(map.keySet().contains(catalog));
	}
	
	@Test
	public void testGetPrimaryKeys() throws SQLException, JsonProcessingException {

		doReturn(conn).when(sut).getConnection();
		CatalogInfo cat = new CatalogInfo("mysql", null);
		String table = "fb_users";
		int count = 1;
		String colname = "id";
		ResultSet rs = mock(ResultSet.class);
		when(dm.getPrimaryKeys(cat.getCatalog(), cat.getSchema(), table)).thenReturn(rs);
		ResultSetMetaData rsmd = mock(ResultSetMetaData.class);
		when(rs.getMetaData()).thenReturn(rsmd);
		when(rsmd.getColumnCount()).thenReturn(count);
		when(rsmd.getColumnName(any(Integer.class))).thenReturn("COLUMN_NAME");
		
		when(rs.next()).thenAnswer(TestUtils.getMockResultSet_next(1));
		when(rs.getObject(any(Integer.class))).thenReturn(colname);
		
		//test
		Set set = sut.getPrimaryKeys(cat, table);
		assertEquals(count, set.size());
		assertTrue(set.contains(colname));
	}
	
	@Test
	public void testGetDbTables() throws SQLException, JsonProcessingException {

		doReturn(conn).when(sut).getConnection();
		CatalogInfo cat = new CatalogInfo("mysql", null);
		String table = "fb_users";
		int count = 1;
		ResultSet rs = mock(ResultSet.class);
		when(rs.next()).thenAnswer(TestUtils.getMockResultSet_next(1));
		when(rs.getString("TABLE_NAME")).thenReturn(table);
		when(rs.getString("TABLE_TYPE")).thenReturn("TABLE");
		when(dm.getTables(cat.getCatalog(), cat.getSchema(), "%", null)).thenReturn(rs);

		rs = TestUtils.getMockResultSet(new ArrayList());
		when(dm.getPrimaryKeys(cat.getCatalog(), cat.getSchema(), table)).thenReturn(rs);
		ResultSetMetaData rsmd = mock(ResultSetMetaData.class);
		when(rs.getMetaData()).thenReturn(rsmd);
		when(rsmd.getColumnCount()).thenReturn(0);
		
		Statement stmt = mock(Statement.class);
		when(conn.createStatement()).thenReturn(stmt);
		ResultSet queryRs = TestUtils.getMockResultSet(
				Arrays.asList(Arrays.asList(table, 1)));
		when(stmt.executeQuery(any(String.class))).thenReturn(queryRs);
		
		//test
//		Map map = sut.getDbTables(cat);
//		assertEquals(count, map.size());
//		assertTrue(map.keySet().contains(table));
		
		when(dm.getColumns(cat.getCatalog(), cat.getSchema(), table, "%")).thenReturn(rs);
		String result = sut.getColumns(cat, table);
		new Gson().fromJson(result, Map.class);
	}
	
	@Test
	public void testExecuteQuery() throws Exception {

		doReturn(conn).when(sut).getConnection();
		CatalogInfo cat = new CatalogInfo("mysql", null);
		String table = "fb_users";

		ResultSet rs = TestUtils.getMockResultSet(new ArrayList());
		when(dm.getPrimaryKeys(cat.getCatalog(), cat.getSchema(), table)).thenReturn(rs);
		
		Statement stmt = mock(Statement.class);
		when(conn.createStatement()).thenReturn(stmt);
		ResultSet queryRs = TestUtils.getMockResultSet(
				Arrays.asList(Arrays.asList(table, 1)));
		when(stmt.executeQuery(any(String.class))).thenReturn(queryRs);

		ResultSetMetaData rsmd = mock(ResultSetMetaData.class);
		when(queryRs.getMetaData()).thenReturn(rsmd);
		when(rsmd.getColumnCount()).thenReturn(0);
		Gson gson = new Gson();
		
		//test
		String json = sut.executeQuery("SELECT 1", cat);
		Map data = gson.fromJson(json, Map.class);

		when(stmt.executeUpdate(any(String.class))).thenReturn(5);
		assertEquals(5, sut.executeUpdate("INSERT INTO fb_users", cat));
		
		json = (String)sut.getData("getTypeInfo");
		data = gson.fromJson(json, Map.class);
	}
}
