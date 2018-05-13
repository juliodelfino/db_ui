package com.delfino.controller;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.delfino.model.User;
import com.delfino.util.Constants;

import spark.Request;
import spark.Response;
import spark.Session;

public class DbControllerTest {

	private DbController testObj = new DbController();

	private static Request req;
	private static Response res;
	private static final String SERVER_ERROR = "Internal Server Error";
	
	@BeforeClass
	public static void setup() {
		
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
		
		assertNotNull(testObj);
		assertNotNull(testObj.getIndex);
		String html = (String)testObj.getIndex.handle(req, res);
		assertFalse(html.contains(SERVER_ERROR));

		Mockito.when(req.queryParams("id")).thenReturn("3e096c94");
		html = (String)testObj.getDbConnInfo.handle(req, res);
		assertFalse(html.contains(SERVER_ERROR));

		Mockito.when(req.queryParams("catalog")).thenReturn("mysql");
		html = (String)testObj.getDbInfo.handle(req, res);
		assertFalse(html.contains(SERVER_ERROR));

		html = (String)testObj.getNewDbConn.handle(req, res);
		assertFalse(html.contains(SERVER_ERROR));
	}
}
