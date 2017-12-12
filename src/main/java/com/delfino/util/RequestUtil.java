package com.delfino.util;

import org.apache.commons.lang.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;

import java.lang.reflect.Method;

public class RequestUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestUtil.class);

    public static <T> T extract(Request req, Class<T> type) {

        try {
            T instance = type.newInstance();
            for (Method m : type.getDeclaredMethods()) {
                if (m.getName().startsWith("set")) {
                    String property = m.getName().replace("set", "");
                    property = WordUtils.uncapitalize(property);
                    if (req.queryParams(property) != null) {
                        m.invoke(instance, req.queryParams(property));
                    }
                }
            }
            return instance;
        } catch (ReflectiveOperationException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

	public static String getUser(Request req) {
		return req.session().attribute(Constants.SESSION_USER);
	}
}
