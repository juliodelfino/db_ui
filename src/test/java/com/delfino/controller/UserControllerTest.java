package com.delfino.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.delfino.model.User;
import com.delfino.util.Constants;
import com.delfino.util.RequestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

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
	public void testPostLogin() throws Exception {

		Mockito.when(req.queryParams("username")).thenReturn("root");
		Mockito.when(req.queryParams("password")).thenReturn("welcome");
		
		//test
		String html = (String)testObj.postLogin.handle(req, res);
		assertFalse(html.contains(SERVER_ERROR));
	}
	
	@Test
	public void testPostPassword() throws Exception {

		Mockito.when(req.queryParams("username")).thenReturn("root");
		Mockito.when(req.queryParams("password")).thenReturn("welcome");
		Mockito.when(req.queryParams("password_new")).thenReturn("welcome");
		Mockito.when(req.queryParams("password_confirm")).thenReturn("welcome");
		
		//test
		assertTrue((boolean)testObj.postPassword.handle(req, res));
	}
	
	@Test
	public void testGetInfo_PostInfo() throws Exception {

		Mockito.when(req.queryParams("username")).thenReturn("root");
		String json = (String)testObj.getInfo.handle(req, res);
		Gson gson = new Gson();
		Map userProps = gson.fromJson(json, Map.class);
		User user = new ObjectMapper().convertValue(userProps.get("user"), User.class);
		assertEquals("root", user.getUsername());

		Mockito.when(req.queryParams("password")).thenReturn("welcome");
		Mockito.when(req.queryParams("fullName")).thenReturn(user.getFullName());
		Mockito.when(req.queryParams("admin")).thenReturn(user.isAdmin() + "");
		Mockito.when(req.queryParams("dbaccess")).thenReturn("3e096c94");
		
		//test
		assertTrue((boolean)testObj.postInfo.handle(req, res));
	}
	
	@Test
	public void testAddInfo_DeleteInfo() throws Exception {

		Mockito.when(req.queryParams("username")).thenReturn("tmpuser");
		Mockito.when(req.queryParams("password")).thenReturn("welcome");
		Mockito.when(req.queryParams("fullName")).thenReturn("Temp User");
		Mockito.when(req.queryParams("admin")).thenReturn("false");
		Mockito.when(req.queryParams("dbaccess")).thenReturn("3e096c94");
		
		//test
		assertTrue((boolean)testObj.postNew.handle(req, res));
		
		String json = (String)testObj.getInfo.handle(req, res);
		Gson gson = new Gson();
		Map userProps = gson.fromJson(json, Map.class);
		User user = new ObjectMapper().convertValue(userProps.get("user"), User.class);
		assertEquals("tmpuser", user.getUsername());

		//test
		assertTrue((boolean)testObj.deleteInfo.handle(req, res));
	}
	
	@Test
	public void testOtherRoutes() throws Exception {

		String html = (String)testObj.getSettings.handle(req, res);
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
