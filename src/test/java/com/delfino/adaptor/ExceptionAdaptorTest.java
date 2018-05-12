package com.delfino.adaptor;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.delfino.model.User;
import com.google.gson.Gson;

public class ExceptionAdaptorTest {

	ExceptionAdaptor adaptor = new ExceptionAdaptor();
	Gson gson = new Gson();
	
	@Test
	public void testConvert() {
		
		Exception ex = new IllegalStateException("test message here");
		
		String result = adaptor.convert(ex);
		Map map = gson.fromJson(result, Map.class);
		assertEquals(ex.getMessage(), map.get("message"));
	}
}
