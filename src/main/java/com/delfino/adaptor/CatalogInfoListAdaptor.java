package com.delfino.adaptor;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.delfino.model.CatalogInfo;

public class CatalogInfoListAdaptor implements Adaptor<ResultSet, List<CatalogInfo>> {

	@Override
	public List<CatalogInfo> convert(ResultSet rs) throws SQLException {
		
		Set<String> colNames = getColumnNames(rs);
		List<CatalogInfo> values = new ArrayList<>();
        while (rs.next()) {
        	CatalogInfo cat = new CatalogInfo();
        	//Resolve the column name whether it's TABLE_CAT or table_cat
        	String catalogColName = colNames.stream().filter(c -> c.equalsIgnoreCase("table_cat")).findFirst().orElse(null);
			String schemaColName = colNames.stream().filter(c -> c.equalsIgnoreCase("table_schem")).findFirst().orElse(null);

        	if (Objects.nonNull(catalogColName)) {
            	cat.setCatalog(rs.getString(catalogColName));
        	}
        	if (Objects.nonNull(schemaColName)) {
				cat.setSchema(rs.getString(schemaColName));
				//try to replace TABLE_CAT with TABLE_CATALOG if there's any
				catalogColName = colNames.stream().filter(c -> c.equalsIgnoreCase("table_catalog")).findFirst().orElse(null);
        		if (Objects.nonNull(catalogColName)) {
					cat.setCatalog(rs.getString(catalogColName));
				}
        	}
        	values.add(cat);
        }
        values.sort((x, y) -> x.getCatalog() != null || y.getCatalog() != null ?
				x.getCatalog().compareTo(y.getCatalog()) : 0);
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
