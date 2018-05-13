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

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import static org.mockito.Matchers.any;

import com.delfino.dao.DbInfoDao;
import com.delfino.db.DbConnection;
import com.delfino.model.User;
import com.delfino.model.UserCacheSchema;
import com.delfino.util.Constants;
import com.delfino.util.TestUtils;
import com.google.gson.Gson;

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
        
		req = Mockito.mock(Request.class);
		res = Mockito.mock(Response.class);
		Session ses = Mockito.mock(Session.class);
		User user = new User();
		user.setUsername("root");
		Mockito.when(req.session()).thenReturn(ses);
		Mockito.when(req.session().attribute(Constants.SESSION_USER)).thenReturn(user);	
	}
	
	@Test
	public void testRoutes() throws Exception {

		Mockito.when(req.queryParams("id")).thenReturn("3e096c94");
		Mockito.when(req.queryParams("catalog")).thenReturn("mysql");
		Mockito.when(req.queryParams("table")).thenReturn("fb_users");
		
		//test getIndex
		String html = (String)testObj.getIndex.handle(req, res);
		assertFalse(html.contains(SERVER_ERROR));
		
		Connection conn = Mockito.mock(Connection.class);
		PowerMockito.when(DriverManager.getConnection(
    		"jdbc:mysql://localhost", "root", "yeah"))
        	.thenReturn(conn);

		Mockito.when(req.queryParams("connId")).thenReturn("3e096c94");
		Mockito.when(req.queryParams("q")).thenReturn("SELECT 1");

		//test getQuery
		html = (String)testObj.getQuery.handle(req, res);
		assertFalse(html.contains(SERVER_ERROR));

		//test getQueryHistory
		html = (String)testObj.getQueryHistory.handle(req, res);
		List list = new Gson().fromJson(html, List.class);
		assertEquals(3, list.size());
		assertFalse(html.contains(SERVER_ERROR));

	}
}
