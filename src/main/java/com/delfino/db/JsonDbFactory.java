package com.delfino.db;

import java.util.HashMap;
import java.util.Map;

import com.delfino.util.AppProperties;

public class JsonDbFactory {

	private static Map<String, JsonDb> jsonDbMap = new HashMap<>();
	
	public static <T> JsonDb<T> getInstance(String dataFile, Class<T> jsonDataModel) {
		String fullDataFile = AppProperties.get("data_dir") + "/" + dataFile;
		JsonDb<T> instance = jsonDbMap.get(dataFile);
		if (instance == null) {
			instance = new JsonDb<T>(fullDataFile, jsonDataModel);
			jsonDbMap.put(dataFile, instance);
		}
		return instance;
	}
   
}
