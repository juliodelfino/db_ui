package com.delfino.adaptor;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.delfino.model.CatalogInfo;

public class CatalogInfoListAdaptor implements Adaptor<ResultSet, List<CatalogInfo>> {

	@Override
	public List<CatalogInfo> convert(ResultSet rs) throws SQLException {
		
		Set<String> colNames = getColumnNames(rs);
		List<CatalogInfo> values = new ArrayList<>();
        while (rs.next()) {
        	CatalogInfo cat = new CatalogInfo();
        	if (colNames.contains("TABLE_CAT")) {
            	cat.setCatalog(rs.getString("TABLE_CAT"));
        	}
        	if (colNames.contains("TABLE_SCHEM")) {
            	cat.setCatalog(rs.getString("TABLE_CATALOG"));
            	cat.setSchema(rs.getString("TABLE_SCHEM"));
        	}
        	values.add(cat);
        }
        values.sort((x, y) -> x.getCatalog().compareTo(y.getCatalog()));
        return values;
	}

	private Set<String> getColumnNames(ResultSet rs) throws SQLException {
		
		Set<String> colNames = new HashSet<>();
		ResultSetMetaData meta = rs.getMetaData();
		for (int i=1; i<=meta.getColumnCount(); i++) {
			colNames.add(meta.getColumnLabel(i));
		}
		return colNames;
	}

}
