package com.delfino.adaptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.delfino.model.User;
import com.google.gson.Gson;

public class ListAdaptorTest {

	ListAdaptor adaptor = new ListAdaptor();
	Gson gson = new Gson();
	
	@Test
	public void testConvert() {
		
		User user1 = new User();
		user1.setFullName("sophie");
		user1.setAdmin(false);

		User user2 = new User();
		user2.setFullName("kyle");
		user2.setAdmin(true);
		
		List<User> users = Arrays.asList(user1, user2);
		
		String result = adaptor.convert(users);
		Map map = gson.fromJson(result, Map.class);
		assertEquals(users.size(), ((List)map.get("data")).size());
	}
}
