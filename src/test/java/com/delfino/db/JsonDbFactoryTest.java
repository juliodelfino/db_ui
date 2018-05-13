package com.delfino.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.delfino.model.CatalogInfo;
import com.delfino.model.DbCacheSchema;
import com.delfino.model.DbConnSchema;
import com.delfino.model.TableInfo;
import com.delfino.model.User;
import com.delfino.model.UserCacheSchema;
import com.delfino.util.AppProperties;

public class JsonDbFactoryTest {

	@Test
	public void testGetInstance() {
		
		JsonDb<DbConnSchema> jsonDbConn = 
				JsonDbFactory.getInstance("data.json", DbConnSchema.class);
		DbConnSchema dbConnSchema = jsonDbConn.get();
		String username = dbConnSchema.getUsers().keySet().stream().findFirst().get();
		assertEquals(username, dbConnSchema.getUsers().get(username).getUsername());
		
		String dbConnId = dbConnSchema.getDatabases().keySet().stream().findFirst().get();
		assertEquals(dbConnId, dbConnSchema.getDatabases().get(dbConnId).getConnId());

		JsonDb<DbCacheSchema> jsonDbCache = 
				JsonDbFactory.getInstance("dbcache_" + dbConnId, DbCacheSchema.class);
		DbCacheSchema dbCacheSchema = jsonDbCache.get();
		
		String tblCat = "mysql";
		assertTrue(dbCacheSchema.getCatalogs().containsKey(tblCat));
		CatalogInfo catInfo = dbCacheSchema.getCatalogs().get(tblCat);
		TableInfo tblInfo = catInfo.getTable("fb_users");
		assertEquals(tblCat, catInfo.getName());
		assertEquals(tblCat, tblInfo.getTableCatalog());
		assertEquals(1, catInfo.getTableCount());
		
		assertTrue(tblInfo.getPrimaryKeys().contains("id"));
		

		JsonDb<UserCacheSchema> jsonUserCache = 
				JsonDbFactory.getInstance("usercache_" + username, UserCacheSchema.class);
		UserCacheSchema userSchema = jsonUserCache.get();
		assertEquals(3, userSchema.getQueryLogs().size());
		
	}
	
	@Test
	public void testSave() throws IOException {
		
		String tmpFile = "tmpFile1.json";
		
		JsonDb<DbConnSchema> jsonDbConn = 
				JsonDbFactory.getInstance(tmpFile, DbConnSchema.class);
		User user = new User();
		user.setUsername("mitch");
		user.setPassword("yeah");
		user.setAdmin(false);
		jsonDbConn.get().getUsers().put(user.getUsername(), user);
		
		//test
		jsonDbConn.save();

		JsonDb<DbConnSchema> jsonDbConn2 = 
				JsonDbFactory.getInstance(tmpFile, DbConnSchema.class);
		
		assertTrue(jsonDbConn2.get().getUsers().containsKey(user.getUsername()));
		jsonDbConn.reload();
		assertTrue(jsonDbConn2.get().getUsers().containsKey(user.getUsername()));
		
		assertTrue(new File(AppProperties.get("data_dir") + "/" + tmpFile).delete());
		
	}
}
