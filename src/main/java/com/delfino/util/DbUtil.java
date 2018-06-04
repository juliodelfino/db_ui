package com.delfino.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.delfino.db.DbConnection;
import com.delfino.model.CatalogInfo;

import spark.utils.StringUtils;

public class DbUtil {
	
    private static final Logger LOGGER = LoggerFactory.getLogger(DbUtil.class);
    
	public static void setCatalogInfo(Connection connection, CatalogInfo cat) {

		try {
			DatabaseMetaData meta = connection.getMetaData();
			if (meta.supportsCatalogsInTableDefinitions() && StringUtils.isNotEmpty(cat.getCatalog())) {
				connection.setCatalog(cat.getCatalog());
			}
			if (meta.supportsSchemasInTableDefinitions() && StringUtils.isNotEmpty(cat.getSchema())) {
				connection.setSchema(cat.getSchema());
			}
		} catch (SQLException ex) {
			LOGGER.error(ex.getMessage(), ex);
		}
	}

	public static String getUniqueName(String catalog, String schema) {
		return Stream.of((catalog == null ? "" : catalog),
				(schema == null ? "" : schema)).filter(StringUtils::isNotEmpty)
				.collect(Collectors.joining(" - "));
	}
}
