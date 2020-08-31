package com.delfino.util;

import java.util.Arrays;
import java.util.List;

public interface Constants {

	String GET = "get";
	String POST = "post";
	String DELETE = "delete";

    List<String> HTTP_METHODS = Arrays.asList(GET, POST, DELETE);

	String STATIC_FILES = "/public";

	String PATH_LOGIN = "/user/login";

	String SESSION_USER = "SESSION_USER";

	String PATH_HOME = "/db";

	String LOGIN_REDIRECT = "loginRedirect";
	
	String DATA_JSON = "data.json";
	
	enum TreeNodeType {
		DBCONN, CATALOG, SCHEMA, TABLE
	}
}
