package com.delfino.adaptor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.delfino.model.CatalogInfo;

public class CatalogInfoListAdaptor implements Adaptor<ResultSet, List<CatalogInfo>> {

	@Override
	public List<CatalogInfo> convert(ResultSet rs) throws SQLException {
		
		List<CatalogInfo> values = new ArrayList();
        while (rs.next()) {
        	CatalogInfo cat = new CatalogInfo();
        	cat.setName(rs.getString("TABLE_CAT"));
        	values.add(cat);
        }
        values.sort((x, y) -> x.getName().compareTo(y.getName()));
        return values;
	}

}
