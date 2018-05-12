package com.delfino.dao;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.stream.Collectors;

import javax.naming.Context;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.delfino.db.JsonDb;
import com.delfino.db.JsonDbFactory;
import com.delfino.model.DbConnSchema;
import com.delfino.model.SqlLog;
import com.delfino.model.User;
import com.delfino.model.UserCacheSchema;
import com.delfino.util.AppException;
import com.delfino.util.AppProperties;
import com.delfino.util.Constants;
import com.delfino.util.RequestUtil;
import com.google.gson.JsonElement;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.SimpleBindRequest;

import spark.utils.StringUtils;

public class UserDao {

	private static final Logger LOGGER = LoggerFactory.getLogger(AppProperties.class);
	private UserDbDao userDbDao = new UserDbDao();
	
	private JsonDb<DbConnSchema> jsonDb = JsonDbFactory.getInstance(Constants.DATA_JSON, DbConnSchema.class);
	private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	
	public UserDao() {
		if (jsonDb.get().getUsers().isEmpty()) {
			User root = createRootUser();
			jsonDb.get().getUsers().put(root.getUsername(), root);
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

	public Collection<User> getAll() {

		return jsonDb.get().getUsers().values();
	}

	public List<User> getDbUsers(String connId) {
		
		return userDbDao.getDbUserList(connId).stream()
			.map(id -> jsonDb.get().getUsers().get(id))
			.collect(Collectors.toList());
	}

	public User validate(User user) {

		String hashedPassword = hashText(user.getPassword());
		return getAll().stream().filter(u -> u.getUsername().equalsIgnoreCase(user.getUsername())
				&& u.getPassword().equals(hashedPassword)).findFirst().orElse(null);
	}
	
	public User validateViaLdap(User user) {
		
		try {
			LDAPConnection ldapConn = new LDAPConnection("com.example.local", 389, "Administrator@com.example.local", "admin");
			SimpleBindRequest adminBindRequest = new SimpleBindRequest(user.getUsername(), user.getPassword());
			ldapConn.bind(adminBindRequest);
		} catch (LDAPException e) {
	    	LOGGER.warn("Login via LDAP failed for user " + user, e);
			return null;
		}
		user.setPassword(hashText(user.getPassword()));
		return user;
	}
	
	public User validateViaLdap_2(User user) {
		try
	    {
	        // Set up the environment for creating the initial context
	        Hashtable<String, String> env = new Hashtable<String, String>();
	        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
	        env.put(Context.PROVIDER_URL, AppProperties.get("ldap_server"));
	        // 
	        env.put(Context.SECURITY_AUTHENTICATION, "simple");
	        env.put(Context.SECURITY_PRINCIPAL, user.getUsername()); //we have 2 \\ because it's a escape char
	        env.put(Context.SECURITY_CREDENTIALS, user.getPassword());

	        // Create the initial context

	        DirContext ctx = new InitialDirContext(env);
	        boolean result = ctx != null;

	        if(result) {
	            ctx.close();
	            user.setPassword(hashText(user.getPassword()));
	            return user;
	        }
	    }
	    catch (Exception e)
	    {      
	    	LOGGER.warn("Login via LDAP failed for user " + user, e);
	    }
		return null;
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
		Collection<User> users = getAll();
		if (users.stream().anyMatch(u -> 
			u.getUsername().equalsIgnoreCase(user.getUsername()))) {
			
			String errMsg = String.format("user '%s' already exists", user);
			LOGGER.error(errMsg, new AppException(errMsg));
			return false;
		}
		user.setPassword(hashText(user.getPassword()));
		jsonDb.get().getUsers().put(user.getUsername(), user);
		return jsonDb.save();
	}

	public boolean add(User user, String[] dbAccess) {
		if (add(user)) {
			jsonDb.get().getUserDbMap()
				.put(user.getUsername(), new HashSet(Arrays.asList(dbAccess)));
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
		dbUser.setFullName(user.getFullName());
		if (!StringUtils.isEmpty(user.getPassword())) {
			dbUser.setPassword(hashText(user.getPassword()));
		}
		jsonDb.get().getUserDbMap()
			.put(user.getUsername(), new HashSet(Arrays.asList(dbAccess)));
		return jsonDb.save();
	}

	public Object get(String username) {
		return getAll().stream().filter(user -> user.getUsername().equals(username))
			.findFirst().get();
	}

	public boolean delete(String username) {
		jsonDb.get().getUserDbMap().remove(username);
		jsonDb.get().getUsers().remove(username);
		return jsonDb.save();
	}

	public void saveQuery(String sql, String userId) {
		JsonDb<UserCacheSchema> userCache = 
			JsonDbFactory.getInstance("usercache_" + userId, UserCacheSchema.class);
		userCache.get().addQueryLog(sql);
		userCache.save();
	}

	public List getQueryHistory(String userId) {
		JsonDb<UserCacheSchema> userCache = 
				JsonDbFactory.getInstance("usercache_" + userId, UserCacheSchema.class);
		return userCache.get().getQueryLogs();
	}

	public boolean deleteQueryLog(String userId, String timestamp) {

		boolean result = false;
		JsonDb<UserCacheSchema> userCache = 
				JsonDbFactory.getInstance("usercache_" + userId, UserCacheSchema.class);
		if (timestamp.equalsIgnoreCase("all")) {
			userCache.get().getQueryLogs().clear();
			result = true;
		} else {
			timestamp = StringEscapeUtils.escapeHtml(timestamp);
			LocalDateTime localDate = LocalDateTime.parse(timestamp, formatter);
			Date tmpDate = Date.from(localDate.atZone(ZoneId.systemDefault()).toInstant());
			
			SqlLog tmpLog = userCache.get().getQueryLogs().stream().filter(log -> 
			    log.getTimestamp().toString().equals(tmpDate.toString()))
				.findFirst().orElse(null);
			if (tmpLog != null) {
				userCache.get().getQueryLogs().remove(tmpLog);
				result = true;
			}
			
		}
		if (result) {
			result = userCache.save();
		}
		return result;
	}

}
