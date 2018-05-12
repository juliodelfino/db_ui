package com.delfino.adaptor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.delfino.model.TableInfo;

public class TableInfoListAdaptor implements Adaptor<ResultSet, List<TableInfo>> {

	@Override
	public List<TableInfo> convert(ResultSet rs) throws SQLException {
		List<TableInfo> values = new ArrayList();
        while (rs.next()) {
        	TableInfo tbl = new TableInfo(rs.getString("TABLE_NAME"));
        	tbl.setTableCatalog(rs.getString("TABLE_CAT"));
        	tbl.setTableSchema(rs.getString("TABLE_SCHEM"));
        	tbl.setTableType(rs.getString("TABLE_TYPE"));
        	tbl.setRemarks(rs.getString("REMARKS"));
        	values.add(tbl);
        }
        values.sort((x, y) -> x.getName().compareTo(y.getName()));
        return values;
	}

}
