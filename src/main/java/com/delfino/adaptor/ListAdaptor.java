package com.delfino.adaptor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.delfino.model.Column;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ListAdaptor implements Adaptor<Collection, String> {

	private Gson gson = new GsonBuilder().create();

	@Override
	public String convert(Collection input) {

		List<Column> columns = new ArrayList<Column>();

		Class type = input.iterator().next().getClass();
		List<Field> fields = Arrays.asList(type.getDeclaredFields());
		for (Field f : fields) {
			columns.add(new Column(f.getName()));
		}

		Map resultMap = new HashMap();
		resultMap.put("columns", columns);
		resultMap.put("data", input);

		return gson.toJson(resultMap);
	}

}
