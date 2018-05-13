package com.delfino.controller;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.delfino.model.User;
import com.delfino.util.Constants;

import spark.Request;
import spark.Response;
import spark.Session;

public class UserControllerTest {

	private UserController testObj = new UserController();

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

		Mockito.when(req.queryParams("username")).thenReturn("root");
		String html = (String)testObj.getInfo.handle(req, res);
		assertFalse(html.contains(SERVER_ERROR));

		html = (String)testObj.getLogout.handle(req, res);
		assertNull(html);

		html = (String)testObj.getLogin.handle(req, res);
		assertNull(html);
		
		Mockito.when(req.session().attribute(Constants.SESSION_USER)).thenReturn(null);
		html = (String)testObj.getLogin.handle(req, res);
		assertFalse(html.contains(SERVER_ERROR));
	}
}
