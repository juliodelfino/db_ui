package com.delfino.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mockito.Mockito;

import com.delfino.model.User;

import spark.Request;
import spark.Session;

public class RequestUtilTest {

	@Test
	public void testGetUser() {
		Request req = Mockito.mock(Request.class);
		Session ses = Mockito.mock(Session.class);
		User user = new User();
		user.setUsername("jamie");

		Mockito.when(req.session()).thenReturn(ses);
		Mockito.when(req.session().attribute(Constants.SESSION_USER)).thenReturn(user);
		assertEquals(user.getUsername(), RequestUtil.getUsername(req));
		
	}

	@Test
	public void testExtract() {
		String username = "steve";
		String password = "yeah";
		String fullname = "Stevenson Johnson";
		String isAdmin = "true";
		Request req = Mockito.mock(Request.class);
		Mockito.when(req.queryParams("username")).thenReturn(username);
		Mockito.when(req.queryParams("password")).thenReturn(password);
		Mockito.when(req.queryParams("fullName")).thenReturn(fullname);
		Mockito.when(req.queryParams("admin")).thenReturn(isAdmin);
		
		//test
		User user = RequestUtil.extract(req, User.class);
		assertEquals(username, user.getUsername());
		assertEquals(password, user.getPassword());
		assertEquals(fullname, user.getFullName());
		assertEquals(isAdmin, user.isAdmin() + "");
	}

	@Test
	public void testGetDbDriver() {
		assertEquals("sqlserver", RequestUtil.getDbDriver("jdbc:sqlserver://localhost"));
	}
}
