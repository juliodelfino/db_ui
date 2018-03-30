package com.delfino.adaptor;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;

import com.delfino.model.Column;
import com.fasterxml.jackson.core.JsonProcessingException;

public class ResultSetAdaptor implements Adaptor<ResultSet, Map> {

    public ResultSetAdaptor() {
    }
    
    public Map convert(ResultSet resultSet) throws SQLException, JsonProcessingException {

        List<Column> columns = new ArrayList<Column>();
        List data = new ArrayList<>();

        ResultSetMetaData metaData = resultSet.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            String columnName = metaData.getColumnName(i);
            Column col = new Column(columnName);
            col.setPrimaryKey(metaData.isAutoIncrement(i) && metaData.isNullable(i) == 0);
            columns.add(col);
        }

        while (resultSet.next()) {
            List row = new ArrayList<>();

            for (int i = 1; i <= metaData.getColumnCount(); i++) {
            	Object value = resultSet.getObject(i);
            	value = value instanceof String ? 
            		StringEscapeUtils.escapeHtml((String)value) : value;
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
