package com.delfino.controller;

import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.delfino.util.AppException;

import spark.Request;
import spark.Response;
import spark.Session;

public class ErrorControllerTest {

	private ErrorController testObj = new ErrorController();

	private static Request req;
	private static Response res;
	
	@BeforeClass
	public static void setup() {
		
		req = Mockito.mock(Request.class);
		res = Mockito.mock(Response.class);
		Session ses = Mockito.mock(Session.class);
		Mockito.when(req.session()).thenReturn(ses);
	}
	
	@Test
	public void testRoutes() throws Exception {

		String html = (String)testObj.notFound.handle(req, res);
		assertTrue(html.contains("Not Found"));
		
		html = (String)testObj.internalServerError.handle(req, res);
		assertTrue(html.contains("Internal Server Error"));

		Mockito.when(req.attribute("exception")).thenReturn(new AppException("test error 123"));
		html = (String)testObj.internalServerError.handle(req, res);
		assertTrue(html.contains("Internal Server Error"));
	}
}
