package com.delfino.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.BDDMockito;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.doReturn;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import static org.mockito.Matchers.any;

import com.delfino.dao.DbInfoDao;
import com.delfino.db.DbConnection;
import com.delfino.db.JsonDbFactory;
import com.delfino.model.User;
import com.delfino.model.UserCacheSchema;
import com.delfino.util.Constants;
import com.delfino.util.TestUtils;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
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
	
	@BeforeClass
	public static void setup() throws SQLException {

        PowerMockito.mockStatic(DriverManager.class);
        
		req = mock(Request.class);
		res = mock(Response.class);
		Session ses = mock(Session.class);
		User user = new User();
		user.setUsername("root");
		when(req.session()).thenReturn(ses);
		when(req.session().attribute(Constants.SESSION_USER)).thenReturn(user);	
	}
	
	@Test
	public void testRoutes() throws Exception {

		String connId = "3e096c94";
		String userId = "root";
		String catalog = "mysql";
		when(req.queryParams("id")).thenReturn(connId);
		when(req.queryParams("catalog")).thenReturn(catalog);
		when(req.queryParams("table")).thenReturn("fb_users");
		
		//test getIndex
		String html = (String)testObj.getIndex.handle(req, res);
		assertFalse(html.contains(SERVER_ERROR));
		
		String query = "SELECT 1";
		when(req.queryParams("connId")).thenReturn(connId);
		when(req.queryParams("q")).thenReturn(query);
		DbInfoDao dbDao = spy(new DbInfoDao());
		DbConnection conn = mock(DbConnection.class);
		doReturn(conn).when(dbDao).connect(connId, userId);
		TestUtils.setField(testObj, "dbDao", dbDao);
		when(conn.executeQuery(query, catalog)).thenReturn("{\"error\": false}");

		//test getQueryHistory
		html = (String)testObj.getQueryHistory.handle(req, res);
		int queryHistory = new Gson().fromJson(html, List.class).size();
		assertFalse(html.contains(SERVER_ERROR));
		
		//test getQuery
		html = (String)testObj.getQuery.handle(req, res);
		JsonObject json = new Gson().fromJson(html, JsonObject.class);
		assertFalse(json.getAsJsonObject().get("error").getAsBoolean());

		//test getQueryHistory again
		html = (String)testObj.getQueryHistory.handle(req, res);
		List<Map> list = new Gson().fromJson(html, List.class);
		assertEquals(queryHistory + 1, list.size());
		assertFalse(html.contains(SERVER_ERROR));
		
		//test deleteQueryHistory
		when(req.queryParams("t")).thenReturn((String)list.get(0).get("timestamp"));
		assertTrue((boolean)testObj.deleteQueryHistory.handle(req, res));
	}
}
