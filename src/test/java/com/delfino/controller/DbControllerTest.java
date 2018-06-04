package com.delfino.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.sql.DriverManager;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.delfino.dao.DbInfoDao;
import com.delfino.db.DbConnection;
import com.delfino.model.DbConnInfo;
import com.delfino.model.User;
import com.delfino.util.Constants;
import com.delfino.util.TestUtils;
import com.google.gson.Gson;
import com.mysql.jdbc.Connection;

import spark.Request;
import spark.Response;
import spark.Session;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DbConnection.class})
@PowerMockIgnore({"javax.crypto.*" })
public class DbControllerTest {

	private DbController testObj = new DbController();

	private static Request req;
	private static Response res;
	private static final String SERVER_ERROR = "Internal Server Error";
	
	@BeforeClass
	public static void setup() {

        PowerMockito.mockStatic(DriverManager.class);
        
		req = Mockito.mock(Request.class);
		res = Mockito.mock(Response.class);
		Session ses = Mockito.mock(Session.class);
		User user = new User();
		user.setUsername("root");
		when(req.session()).thenReturn(ses);
		when(req.session().attribute(Constants.SESSION_USER)).thenReturn(user);	
	}

	@Test
	public void testPostConnectDb() throws Exception {

		when(req.queryParams("url")).thenReturn("jdbc:mysql://localhost");
		when(req.queryParams("username")).thenReturn("root");
		when(req.queryParams("password")).thenReturn("yeah");
		when(req.queryParams("connectionName")).thenReturn("false");
		when(req.queryParams("connId")).thenReturn("111");

		DbInfoDao dbDao = spy(new DbInfoDao());
		DbConnection dbConn = mock(DbConnection.class);
		doReturn(dbConn).when(dbDao).connect(any(DbConnInfo.class));
		TestUtils.setField(testObj, "dbDao", dbDao);
		Connection conn = mock(Connection.class);
		when(dbConn.getConnection()).thenReturn(conn);
		when(conn.getCatalog()).thenReturn("mysql");
		
		//test
		String json = (String)testObj.postConnectDb.handle(req, res);
		DbConnInfo dbInfo = new Gson().fromJson(json, DbConnInfo.class);
		assertEquals("111", dbInfo.getConnId());

		//test
		assertTrue((boolean)testObj.postInfoUpdate.handle(req, res));
		
		//test
		assertTrue((boolean)testObj.deleteInfo.handle(req, res));
	}
	
	@Test
	public void testRoutes() throws Exception {
		
		String connId = "3e096c94";
		String html = (String)testObj.getIndex.handle(req, res);
		assertFalse(html.contains(SERVER_ERROR));

		when(req.queryParams("id")).thenReturn(connId);
		html = (String)testObj.getDbConnInfo.handle(req, res);
		assertFalse(html.contains(SERVER_ERROR));

		when(req.queryParams("connId")).thenReturn(connId);
		html = (String)testObj.getInfo.handle(req, res);
		assertFalse(html.contains(SERVER_ERROR));

		when(req.queryParams("catalog")).thenReturn("mysql");
		html = (String)testObj.getDbInfo.handle(req, res);
		assertFalse(html.contains(SERVER_ERROR));

		html = (String)testObj.getNewDbConn.handle(req, res);
		assertFalse(html.contains(SERVER_ERROR));
	}
}
