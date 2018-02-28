package com.delfino.dao;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.delfino.db.JsonDb;
import com.delfino.model.DbSchema;
import com.delfino.model.User;
import com.delfino.util.AppException;
import com.delfino.util.AppProperties;

import spark.utils.StringUtils;

public class UserDao {

	private static final Logger LOGGER = LoggerFactory.getLogger(AppProperties.class);

	private JsonDb<DbSchema> jsonDb = JsonDb.getInstance(AppProperties.get("data_dir"), DbSchema.class);

	public UserDao() {
		if (jsonDb.get().getUsers().isEmpty()) {
			jsonDb.get().getUsers().add(createRootUser());
			jsonDb.save();
		}
	}
	
	private User createRootUser() {
		User root = new User();
		root.setUsername("root");
		root.setPassword(hashText("welcome"));
		root.setAdmin(true);
		return root;
	}

	public List<User> getAll() {

		List<User> users = jsonDb.get().getUsers();
		return users != null ? users : new ArrayList();
	}

	public User validate(User user) {

		String hashedPassword = hashText(user.getPassword());
		return getAll().stream().filter(u -> u.getUsername().equalsIgnoreCase(user.getUsername())
				&& u.getPassword().equals(hashedPassword)).findFirst().orElse(null);
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
		List<User> users = getAll();
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

	public boolean add(User user, String[] dbAccess) {
		if (add(user)) {
			jsonDb.get().getUserDbMap()
				.put(user.getUsername(), Arrays.asList(dbAccess));
			return jsonDb.save();
		}
		return false;
	}

	public boolean updatePassword(User user) {
		User dbUser = getAll().stream()
				.filter(u -> u.getUsername().equals(user.getUsername()))
				.findFirst().get();
		dbUser.setPassword(hashText(user.getPassword()));
		return jsonDb.save();
	}

	public boolean update(User user, String[] dbAccess) {
		User dbUser = getAll().stream()
				.filter(u -> u.getUsername().equals(user.getUsername()))
				.findFirst().get();
		dbUser.setAdmin(user.isAdmin());
		if (!StringUtils.isEmpty(user.getPassword())) {
			dbUser.setPassword(hashText(user.getPassword()));
		}
		jsonDb.get().getUserDbMap()
			.put(user.getUsername(), Arrays.asList(dbAccess));
		return jsonDb.save();
	}

	public Object get(String username) {
		return getAll().stream().filter(user -> user.getUsername().equals(username))
			.findFirst().get();
	}

	public boolean delete(String username) {
		jsonDb.get().getUserDbMap().remove(username);
		jsonDb.get().getUsers().removeIf(u -> u.getUsername().equals(username));
		return jsonDb.save();
	}

}
