package com.delfino.db;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

public class JsonDb<T> {
	
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonDb.class);

    private static final Gson GSON = new GsonBuilder()
    		.setDateFormat("yyyy-MM-dd HH:mm:ss")
    		.setPrettyPrinting().create();
	private String dataFile;
	private T dataCache;
	
	private Class<T> jsonDataModel;
	
	public JsonDb(String fullDataFile, Class<T> jsonDataModel) {
		this.dataFile = fullDataFile;
		this.jsonDataModel = jsonDataModel;
		dataCache = loadJson(fullDataFile, jsonDataModel);
	}

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

	public static <T> T loadJson(String dataFile, Class<T> jsonDataModel) {
        File f = new File(dataFile);
        if (f.exists()) {
            try (JsonReader reader = new JsonReader(new FileReader(f))) {
                T dataModel = GSON.fromJson(reader, jsonDataModel);
                LOGGER.info("JSON DB location: " + f.getAbsolutePath());
                return dataModel;
            } catch (IOException e) {
                LOGGER.error("Error reading file " + dataFile, e);
            }
        }
        try {
			T dataModel = jsonDataModel.newInstance();
            LOGGER.info("JSON DB location: " + f.getAbsolutePath());
            return dataModel;
		} catch (InstantiationException | IllegalAccessException e) {
            LOGGER.error(e.getMessage(), e);
			return null;
		}
	}

	public static <T> boolean saveJson(T dataCache, String dataFile) {
		
        try {
            FileUtils.write(new File(dataFile), GSON.toJson(dataCache), "UTF-8");
            return true;
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
	}

	public T reload() {
		dataCache = null;
		return get();
	}
	
}
