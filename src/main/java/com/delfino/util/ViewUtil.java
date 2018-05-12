package com.delfino.util;

import com.delfino.model.DbConnInfo;

public class ViewUtil {

	public static String getDbPhoto(DbConnInfo db) {
		return "/assets/images/db/" + db.getDriver().toLowerCase() + "-db.png";
	}
}
