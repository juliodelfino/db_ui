package com.delfino.main;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import com.delfino.util.AppProperties;
import com.delfino.util.TestUtils;
import com.delfino.util.TestUtils.UrlResponse;

public class ApplicationTest {

	@Test
	public void testMain() throws ReflectiveOperationException, IOException {
		Application.main(null);
		
		UrlResponse res = new UrlResponse();
		TestUtils.getResponse("GET", "/user/login", res);
		assertEquals(HttpStatus.OK_200, res.status);
		assertTrue(res.body.contains(AppProperties.get("application_name")));
	}
}
