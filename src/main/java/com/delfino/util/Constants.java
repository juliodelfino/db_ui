package com.delfino.util;

import java.util.Arrays;
import java.util.List;

public interface Constants {

    List<String> HTTP_METHODS = Arrays.asList("post","get","delete");

	String STATIC_FILES = "/public";

	String PATH_LOGIN = "/user/login";

	String SESSION_USER = "SESSION_USER";

	String PATH_HOME = "/db";

	String LOGIN_REDIRECT = "loginRedirect";
	
	String DATA_JSON = "data.json";
	
	static enum TreeNodeType {
		DBCONN, CATALOG, TABLE
	};
}
