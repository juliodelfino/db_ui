package com.delfino.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.delfino.dao.DbInfoDao;
import com.delfino.db.DbConnection;
import com.delfino.model.User;
import com.delfino.util.Constants;
import com.delfino.util.TestUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import spark.Request;
import spark.Response;
import spark.Session;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DbConnection.class})
public class TableControllerTest {

	private TableController testObj = new TableController();

	private static Request req;
	private static Response res;
	private static final String SERVER_ERROR = "Internal Server Error";

	private static String connId = "3e096c94";
	private static String userId = "root";
	private static String catalog = "mysql";
	private static String table = "fb_users";
	
	@BeforeClass
	public static void setup() throws SQLException {

        PowerMockito.mockStatic(DriverManager.class);
        
		req = mock(Request.class);
		res = mock(Response.class);
		Session ses = mock(Session.class);
		User user = new User();
		user.setUsername("root");
		user.setAdmin(true);
		when(req.session()).thenReturn(ses);
		when(req.session().attribute(Constants.SESSION_USER)).thenReturn(user);	
		
		when(req.queryParams("id")).thenReturn(connId);
		when(req.queryParams("catalog")).thenReturn(catalog);
		when(req.queryParams("table")).thenReturn(table);
		when(req.queryParams("connId")).thenReturn(connId);
	}
	
	@Test
	public void testGetIndex() throws Exception {
		
		DbInfoDao dbDao = spy(new DbInfoDao());
		DbConnection conn = mock(DbConnection.class);
		doReturn(conn).when(dbDao).connect(connId, userId);
		TestUtils.setField(testObj, "dbDao", dbDao);
	
		//test getIndex
		String html = (String)testObj.getIndex.handle(req, res);
		assertFalse(html.contains(SERVER_ERROR));
	}

	@Test
	public void testGetColumns() throws Exception {
		
		DbInfoDao dbDao = spy(new DbInfoDao());
		DbConnection conn = mock(DbConnection.class);
		doReturn(conn).when(dbDao).connect(connId, userId);
		TestUtils.setField(testObj, "dbDao", dbDao);
		when(conn.getColumns(table)).thenReturn("");

		//test getIndex
		String html = (String)testObj.getColumns.handle(req, res);
		assertFalse(html.contains(SERVER_ERROR));
	}
	
	
	@Test
	public void testOtherRoutes() throws Exception {

		String selectQuery = "SELECT * FROM fb_users";
		String insertQuery = "INSERT INTO fb_users values (1)";
			
		DbInfoDao dbDao = spy(new DbInfoDao());
		DbConnection conn = mock(DbConnection.class);
		doReturn(conn).when(dbDao).connect(connId, userId);
		TestUtils.setField(testObj, "dbDao", dbDao);

		//test getQueryHistory
		String html = (String)testObj.getQueryHistory.handle(req, res);
		int queryHistory = new Gson().fromJson(html, List.class).size();
		assertFalse(html.contains(SERVER_ERROR));
		
		//test getQuery - SELECT
		when(req.queryParams("q")).thenReturn(selectQuery);
		when(conn.executeQuery(selectQuery, catalog)).thenReturn("{\"error\": false}");
		html = (String)testObj.getQuery.handle(req, res);
		JsonObject json = new Gson().fromJson(html, JsonObject.class);
		assertFalse(json.getAsJsonObject().get("error").getAsBoolean());
		
		//test getQuery - INSERT
		when(req.queryParams("q")).thenReturn(insertQuery);
		when(conn.executeUpdate(insertQuery, catalog)).thenReturn(1);
		html = (String)testObj.getQuery.handle(req, res);
		json = new Gson().fromJson(html, JsonObject.class);
		assertNull(json.getAsJsonObject().get("error"));

		//test getQueryHistory again
		html = (String)testObj.getQueryHistory.handle(req, res);
		List<Map> list = new Gson().fromJson(html, List.class);
		assertEquals(queryHistory + 2, list.size());
		assertEquals(insertQuery, list.get(0).get("log"));
		assertEquals(selectQuery, list.get(1).get("log"));
		assertFalse(html.contains(SERVER_ERROR));
		
		//test deleteQueryHistory
		when(req.queryParams("t")).thenReturn((String)list.get(0).get("timestamp"));
		assertTrue((boolean)testObj.deleteQueryHistory.handle(req, res));
		when(req.queryParams("t")).thenReturn((String)list.get(1).get("timestamp"));
		assertTrue((boolean)testObj.deleteQueryHistory.handle(req, res));
	}
}
