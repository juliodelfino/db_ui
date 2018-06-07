package com.delfino.adaptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.sql.rowset.serial.SerialBlob;

import org.junit.Test;

import com.delfino.model.CatalogInfo;
import com.delfino.model.Column;
import com.delfino.model.TableInfo;
import com.delfino.model.User;
import com.delfino.util.TestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;

public class ResultSetAdaptorTest {
	
	ResultSetAdaptor adaptor = new ResultSetAdaptor();
	Gson gson = new Gson();
	
	@Test
	public void testConvert() throws SQLException, JsonProcessingException {
	
		User user = new User();
		byte[] byteData = "text".getBytes();
		String encodedByteData = Base64.getEncoder().encodeToString(byteData);
		List initData = Arrays.asList(user, new SerialBlob(byteData), byteData);
		List expected = Arrays.asList(user, encodedByteData, encodedByteData);
		
		ResultSet rs = TestUtils.getMockResultSet(Arrays.asList(initData));
		
		ResultSetMetaData rsmd = mock(ResultSetMetaData.class);
		when(rs.getMetaData()).thenReturn(rsmd);
		when(rsmd.getColumnCount()).thenReturn(3);
		when(rsmd.getColumnName(1)).thenReturn("OBJ");
		when(rsmd.getColumnName(2)).thenReturn("TEXT");
		when(rsmd.getColumnName(3)).thenReturn("BYTES");
		
		//test
		Map map = adaptor.convert(rs);
		List<Column> cols = (List)map.get("columns");
		assertEquals(3, cols.size());
		assertEquals("OBJ", cols.get(0).title);
		assertEquals("TEXT", cols.get(1).title);
		assertEquals("BYTES", cols.get(2).title);
		
		assertEquals(Arrays.asList(expected), map.get("data"));
	}
	
	@Test
	public void testConvertWithCatalogInfo() throws SQLException, JsonProcessingException {

		User user = new User();
		byte[] byteData = "text".getBytes();
		String encodedByteData = Base64.getEncoder().encodeToString(byteData);
		List initData = Arrays.asList(user, new SerialBlob(byteData), byteData);
		List expected = Arrays.asList(user, encodedByteData, encodedByteData);
		
		ResultSet rs = TestUtils.getMockResultSet(Arrays.asList(initData));
		
		ResultSetMetaData rsmd = mock(ResultSetMetaData.class);
		when(rs.getMetaData()).thenReturn(rsmd);
		when(rsmd.getColumnCount()).thenReturn(3);
		when(rsmd.getColumnName(1)).thenReturn("OBJ");
		when(rsmd.getColumnName(2)).thenReturn("TEXT");
		when(rsmd.getColumnName(3)).thenReturn("BYTES");

		String tableName = "fb_users";
		when(rsmd.getTableName(any(Integer.class))).thenReturn(tableName);
		
		CatalogInfo cat = new CatalogInfo();
		cat.setTables(ImmutableMap.of(tableName, new TableInfo(tableName)));
		
		//test with CatalogInfo
		Map map = adaptor.convert(rs, cat);

		assertEquals(Arrays.asList(expected), map.get("data"));
		
		assertTrue(map.containsKey("data"));
		assertEquals(3, ((List)map.get("columns")).size());
		assertEquals(Arrays.asList(expected), map.get("data"));	
		

	}
}
