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

	private static JsonDb instance;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String DATA_FILE = "data.json";
	private String dataFile;
	private T dataCache;
	
	private Class<T> jsonDataModel;
	
	public JsonDb(String datadir, Class<T> jsonDataModel) {
		this.dataFile = datadir + "/" + DATA_FILE;
		this.jsonDataModel = jsonDataModel;
		dataCache = loadJson(dataFile, jsonDataModel);
		setupDirectoryListener(datadir);
	}

	public static <T> JsonDb<T> getInstance(String datadir, Class<T> jsonDataModel) {
		if (instance == null) {
			instance = new JsonDb<T>(datadir, jsonDataModel);
		}
		return instance;
	}
    
    private void setupDirectoryListener(String datadir) {
		FileAlterationMonitor fileMonitor = new FileAlterationMonitor();
		FileAlterationObserver observer = new FileAlterationObserver(datadir);
		observer.addListener(new FileAlterationListenerAdaptor(){

			@Override
			public void onFileChange(File arg0) {
				if (!arg0.getName().endsWith(DATA_FILE)) {
					return;
				}
				dataCache = loadJson(dataFile, jsonDataModel);
				LOGGER.info(arg0 + " has been updated externally.");
			}
		});
		fileMonitor.addObserver(observer);
		try {
			fileMonitor.start();
		} catch (Exception e) {
			LOGGER.error("Directory listener setup failed", e);
		}
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

	private static <T> T loadJson(String dataFile, Class<T> jsonDataModel) {
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
