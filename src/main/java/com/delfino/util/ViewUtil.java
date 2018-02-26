package com.delfino.util;

import com.delfino.model.DbInfo;

public class ViewUtil {

	public static String getDbPhoto(DbInfo db) {
		return "/assets/images/db/" + db.getDriver().toLowerCase() + "-db.png";
	}
}
