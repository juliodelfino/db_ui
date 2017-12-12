package com.delfino.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

public class AppProperties extends Properties {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppProperties.class);
    private static AppProperties instance;

    private AppProperties(){}

    public static AppProperties getInstance() {

        if (instance == null) {
            instance = new AppProperties();
            try {
                instance.load(AppProperties.class.getResourceAsStream("/application.properties"));
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return instance;
    }
    
    public static String get(String key) {
    	return getInstance().getProperty(key);
    }

	public static int getInt(String key) {
		return Integer.parseInt(get(key));
	}
}

