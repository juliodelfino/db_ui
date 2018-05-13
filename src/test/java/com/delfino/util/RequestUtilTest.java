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

	//	Mockito.when(req.session()).thenReturn(ses);
		Mockito.when(req.session().attribute(Constants.SESSION_USER)).thenReturn(user);
		assertEquals(user.getUsername(), RequestUtil.getUsername(req));
		
	}
}
