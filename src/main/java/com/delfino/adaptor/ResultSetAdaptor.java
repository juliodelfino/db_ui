package com.delfino.adaptor;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ResultSetAdaptor implements Adaptor<ResultSet, String> {

    private Gson gson = new GsonBuilder().create();

    public ResultSetAdaptor() {
    }
    
    public String convert(ResultSet resultSet) throws SQLException, JsonProcessingException {

        List<Column> columns = new ArrayList<Column>();
        List data = new ArrayList<>();

        ResultSetMetaData metaData = resultSet.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            String columnName = metaData.getColumnName(i);
            columns.add(new Column(columnName));
        }

        while (resultSet.next()) {
            List row = new ArrayList<>();

            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                row.add(resultSet.getObject(i));
            }

            data.add(row);
        }

        Map resultMap = new HashMap();
        resultMap.put("columns", columns);
        resultMap.put("data", data);

        return gson.toJson(resultMap);
    }
}
