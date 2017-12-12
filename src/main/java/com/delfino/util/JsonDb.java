package com.delfino.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.delfino.model.DbInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;

public class JsonDb<T> {
	
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonDb.class);

	private static JsonDb instance;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private String dataFile;
	private T dataCache;
	
	private Class<T> jsonDataModel;
	
	public JsonDb(String datadir, Class<T> jsonDataModel) {
		this.dataFile = datadir + "/data.json";
		this.jsonDataModel = jsonDataModel;
		dataCache = loadJson(dataFile, jsonDataModel);
	}

	public static <T> JsonDb<T> getInstance(String datadir, Class<T> jsonDataModel) {
		if (instance == null) {
			instance = new JsonDb<T>(datadir, jsonDataModel);
		}
		return instance;
	}
	
//	public <T> T get(String key, Class<T> type) {
//		JsonElement jsonElement = GSON.toJsonTree(getDataCache().get(key));
//		T pojo = GSON.fromJson(jsonElement, type);
//		return pojo;
//	}
//
//	public <K,V> Map<K,V> getMap(String key, Class<K> dataKeyType, Class<V> dataValueType) {
//
//		JsonElement jsonElement = GSON.toJsonTree(getDataCache().get(key));
//		Map pojo = GSON.fromJson(jsonElement, Map.class);
//		pojo = (Map)pojo.entrySet().stream().collect(Collectors.toMap(
//				k -> k, 
//				v -> {
//			JsonElement tmpJson = GSON.toJsonTree(v);
//			return GSON.fromJson(tmpJson, dataValueType);
//		}));
//		return pojo;
//	}
	
//	public void set(String key, Object value) {
//		getDataCache().put(key, value);
//		saveJson(getDataCache(), dataFile);
//	}

	public T get() {
		if (dataCache == null) {
			dataCache = loadJson(dataFile, jsonDataModel);
		}
		return dataCache;
	}

	public boolean save() {
		return saveJson(dataCache, dataFile);
	}

	//utility methods

	private static <T> T loadJson(String dataFile, Class<T> jsonDataModel) {
        File f = new File(dataFile);
        if (f.exists()) {
            try (JsonReader reader = new JsonReader(new FileReader(f))) {
                return GSON.fromJson(reader, jsonDataModel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
			return jsonDataModel.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			return null;
		}
	}

	private static <T> boolean saveJson(T dataCache, String dataFile) {
        try {
            FileUtils.write(new File(dataFile), GSON.toJson(dataCache));
            return true;
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
	}
	
}
