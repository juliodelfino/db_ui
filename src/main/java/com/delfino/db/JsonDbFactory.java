package com.delfino.db;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.delfino.model.DbSchema;
import com.delfino.util.AppProperties;
import com.delfino.util.Constants;

public class JsonDbFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonDbFactory.class);
	private static Map<String, JsonDb> jsonDbMap = new HashMap();
	
	static {
		setupDirectoryListener(AppProperties.get("data_dir"));
	}
	
	public static <T> JsonDb<T> getInstance(String dataFile, Class<T> jsonDataModel) {
		String fullDataFile = AppProperties.get("data_dir") + "/" + dataFile;
		JsonDb instance = jsonDbMap.get(dataFile);
		if (instance == null) {
			instance = new JsonDb<T>(fullDataFile, jsonDataModel);
			jsonDbMap.put(dataFile, instance);
		}
		return instance;
	}
   
    private static void setupDirectoryListener(String datadir) {
		FileAlterationMonitor fileMonitor = new FileAlterationMonitor();
		FileAlterationObserver observer = new FileAlterationObserver(datadir);
		observer.addListener(new FileAlterationListenerAdaptor(){

			@Override
			public void onFileChange(File arg0) {
				LOGGER.info(arg0 + " has been updated.");
				if (!arg0.getName().endsWith(Constants.DATA_JSON)) {
					return;
				}
				JsonDbFactory.getInstance(Constants.DATA_JSON, DbSchema.class).reload();
			}
		});
		fileMonitor.addObserver(observer);
		try {
			fileMonitor.start();
		} catch (Exception e) {
			LOGGER.error("Directory listener setup failed", e);
		}
	}
}
