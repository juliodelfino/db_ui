package com.delfino.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.delfino.model.CatalogInfo;
import com.delfino.model.DbCacheSchema;
import com.delfino.model.DbConnSchema;
import com.delfino.model.TableInfo;
import com.delfino.model.UserCacheSchema;

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
}
