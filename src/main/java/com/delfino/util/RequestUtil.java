package com.delfino.util;

import org.apache.commons.lang.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.delfino.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;

import spark.Request;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class RequestUtil {

    private static final ObjectMapper OBJ_MAPPER = new ObjectMapper();

    public static <T> T extract(Request req, Class<T> type) {

        Map<String, String> tmpMap = extractAvailableProperties(req, type);
        return OBJ_MAPPER.convertValue(tmpMap, type);
    }

	private static Map<String, String> extractAvailableProperties(Request req, Class type) {
		Map<String, String> tmpMap = new HashMap<>();
        for (Method m : type.getDeclaredMethods()) {
            if (m.getName().startsWith("set")) {
                String property = m.getName().replace("set", "");
                property = WordUtils.uncapitalize(property);
                if (req.queryParams(property) != null) {
                	tmpMap.put(property, req.queryParams(property));
                }
            }
        }
        return tmpMap;
	}

	public static User getUser(Request req) {
		return req.session().attribute(Constants.SESSION_USER);
	}

	public static String getUsername(Request req) {
		User user = getUser(req);
		return user != null ? user.getUsername() : null;
	}

	public static String getDbDriver(String url) {

		int startIdx = url.indexOf(":") + 1;
		return url.substring(startIdx, url.indexOf(":", startIdx));
	}
}
