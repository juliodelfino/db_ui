package com.delfino.dao;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.delfino.model.JsonDbModel;
import com.delfino.model.User;
import com.delfino.util.AppException;
import com.delfino.util.AppProperties;
import com.delfino.util.JsonDb;

public class UserDao {

	private static final Logger LOGGER = LoggerFactory.getLogger(AppProperties.class);

	private JsonDb<JsonDbModel> jsonDb = JsonDb.getInstance(AppProperties.get("data_dir"), JsonDbModel.class);

	public List<User> getUsers() {

		List<User> users = jsonDb.get().getUsers();
		return users != null ? users : new ArrayList();
	}

	public boolean validate(User user) {

		String hashedPassword = hashText(user.getPassword());

		return getUsers().stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(user.getUsername())
				&& u.getPassword().equals(hashedPassword));
	}

	private static String hashText(String text) {
		try {
			byte[] digest = MessageDigest.getInstance("MD5").digest(text.getBytes());
			BigInteger bigInt = new BigInteger(1,digest);
			return bigInt.toString(16);
		} catch (NoSuchAlgorithmException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return text;
	}

	public boolean add(User user) {
		List<User> users = getUsers();
		if (users.stream().anyMatch(u -> 
			u.getUsername().equalsIgnoreCase(user.getUsername()))) {
			
			String errMsg = String.format("user '%s' already exists", user);
			LOGGER.error(errMsg, new AppException(errMsg));
			return false;
		}
		user.setPassword(hashText(user.getPassword()));
		users.add(user);
		return jsonDb.save();
	}

	public boolean update(User user) {
		User dbUser = getUsers().stream()
				.filter(u -> u.getUsername().equals(user.getUsername()))
				.findFirst().get();
		dbUser.setPassword(hashText(user.getPassword()));
		return jsonDb.save();
	}

}
