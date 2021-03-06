package com.delfino.util;

import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.delfino.controller.TableController;
import com.delfino.dao.DbInfoDao;

import static org.mockito.Mockito.when;

public class TestUtils {

	public static Answer<?> getMockResultSet_next(int maxCount) {
		return new Answer() {
		    private int count = 0;

		    public Object answer(InvocationOnMock invocation) {
		    	count++;
		        return count <= maxCount;
		    }
		};
	}

	public static ResultSet getMockResultSet(List<List> rowData) throws SQLException {

		ResultSet rs = mock(ResultSet.class);
		for (int rId=0; rId<rowData.size(); rId++) {
			List row = rowData.get(rId);
			for (int cId=0; cId < row.size(); cId++) {
				Object data = row.get(cId);
				if (data instanceof String) {
					when(rs.getString(cId + 1)).thenReturn((String)rowData.get(rId).get(cId));
				} else if (data instanceof Integer) {
				
					when(rs.getInt(cId + 1)).thenReturn((Integer)rowData.get(rId).get(cId));
				}
				when(rs.getObject(cId + 1)).thenReturn(rowData.get(rId).get(cId));
			}
		}
		when(rs.next()).thenAnswer(getMockResultSet_next(rowData.size()));
		
		return rs;
	}

	public static void setField(Object testObj, String fieldToSet, Object newFieldValue) 
			throws ReflectiveOperationException {
		Field f = testObj.getClass().getDeclaredField(fieldToSet);
		f.setAccessible(true);
		f.set(testObj, newFieldValue);
	}

	public static void getResponse(String requestMethod, String path, UrlResponse response) 
			throws IOException {
		
		URL url = new URL("http://localhost:" + AppProperties.get("port") + path);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod(requestMethod);
		connection.connect();
		String res = IOUtils.toString(connection.getInputStream(), "UTF-8");
		response.body = res;
		response.status = connection.getResponseCode();
		response.headers = connection.getHeaderFields();
	}
	
	public static class UrlResponse {
		public Map<String, List<String>> headers;
		public String body;
		public int status;
	}
}
