package com.delfino.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AppPropertiesTest {

	@Test
	public void testGetInstance() {
		AppProperties props = AppProperties.getInstance();
		assertEquals("Database Viewer", props.get("application_name"));
		assertEquals("Database Viewer", props.getProperty("application_name"));
		assertEquals(3001, props.getInt("port"));
	}
}
