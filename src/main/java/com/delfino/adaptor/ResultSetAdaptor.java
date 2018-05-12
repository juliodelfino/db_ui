package com.delfino.adaptor;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;

import com.delfino.model.CatalogInfo;
import com.delfino.model.Column;
import com.delfino.model.DbCacheSchema;
import com.delfino.model.DbConnInfo;
import com.fasterxml.jackson.core.JsonProcessingException;

import spark.utils.StringUtils;

public class ResultSetAdaptor implements Adaptor<ResultSet, Map> {
	
	public Map convert(ResultSet rs) throws JsonProcessingException, SQLException {
		return convert(rs, null);
	}
    
    public Map convert(ResultSet resultSet, CatalogInfo dbInfo) throws SQLException, JsonProcessingException {

        List<Column> columns = new ArrayList<Column>();
        List data = new ArrayList<>();

        ResultSetMetaData metaData = resultSet.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            String columnName = metaData.getColumnName(i);
            Column col = new Column(columnName);
            if (dbInfo != null && StringUtils.isNotEmpty(metaData.getTableName(i))) {
	            String tableName = metaData.getTableName(i);
	            col.setPrimaryKey(
	            	dbInfo.getTable(tableName).getPrimaryKeys().contains(columnName));
            }
            columns.add(col);
        }

        while (resultSet.next()) {
            List row = new ArrayList<>();

            for (int i = 1; i <= metaData.getColumnCount(); i++) {
            	Object value = resultSet.getObject(i);
            	if (value instanceof byte[]) {
            		value = Base64.getEncoder().encodeToString((byte[])value);
            		columns.get(i-1).setBlob(true);
            	}
            	else if (value instanceof String) {
            		value = StringEscapeUtils.escapeHtml((String)value);
            	}
                row.add(value);
            }

            data.add(row);
        }

        Map resultMap = new HashMap();
        //structure is based on expected JSON model of jQuery's dataTables
        resultMap.put("columns", columns);
        resultMap.put("data", data);

        return resultMap;
    }
}
