package com.delfino.main;

import java.io.File;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.delfino.db.JsonDb;
import com.delfino.model.DbConnSchema;
import com.delfino.model.DbSchemaOld;

public class DbReformatter {

	public static void main(String[] args) {
		
		String dataFile = args[0];
		String newDataFile = new File(dataFile).getParent() 
				+ "/data" + Instant.now().toEpochMilli() + ".json";
		DbSchemaOld dbOld = JsonDb.loadJson(dataFile, DbSchemaOld.class);
		DbConnSchema dbNew = new DbConnSchema();
		dbNew.setDatabases(dbOld.getDatabases());
		dbNew.setUserDbMap(dbOld.getUserDbMap().entrySet().stream()
				.collect(Collectors.toMap(k -> k.getKey(), v -> new HashSet(v.getValue()))));
		dbNew.setUsers(dbOld.getUsers().stream()
				.collect(Collectors.toMap(e -> e.getUsername(), e -> e)));
		JsonDb.saveJson(dbNew, newDataFile);
	}
}
