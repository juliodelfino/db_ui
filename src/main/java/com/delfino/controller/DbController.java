package com.delfino.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.delfino.util.Pair;
import com.delfino.util.ViewUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import spark.Route;

public class DbController extends ControllerBase {

	private ObjectMapper mapper = new ObjectMapper();
    
	
	public DbController() throws ClassNotFoundException {

	    Class.forName("com.mysql.jdbc.Driver");

	}
	
	private Route index = (req, res) -> {

		return ViewUtil.renderHtml("db.html");
	};

	private Route query = (req, res) -> {

	    String sql = req.queryParams("q");
	    

	    ResultSet rs = null;
	    String result = "";
		Connection conn = null;
		Statement stmt;
		try {
			conn = DriverManager
			.getConnection("jdbc:mysql://104.199.152.249/lottominer?zeroDateTimeBehavior=convertToNull","root", "***");
			stmt = conn.createStatement();
             rs = stmt.executeQuery(sql);
             result = convertResultSetToJson(rs);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
		finally {
			rs.close();
			conn.close();
		}

	    return result;
	};
	
	public String convertResultSetToJson(ResultSet resultSet) throws SQLException, JsonProcessingException {

	    List<Entry> columns = new ArrayList<Entry>();
	    List data = new ArrayList<>();

        ResultSetMetaData metaData = resultSet.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            String columnName = metaData.getColumnName(i);
            columns.add(new Pair("title", columnName));
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

	    return mapper.writeValueAsString(resultMap);
	}
}