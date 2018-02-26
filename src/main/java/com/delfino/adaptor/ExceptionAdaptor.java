package com.delfino.adaptor;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ExceptionAdaptor implements Adaptor<Exception, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionAdaptor.class);
    private ObjectMapper mapper = new ObjectMapper();

    public String convert(Exception ex) {

        Map resultMap = new HashMap();
        resultMap.put("error", true);
        resultMap.put("message", ex.getMessage());
        
        try {
			return mapper.writeValueAsString(resultMap);
		} catch (JsonProcessingException e) {
			LOGGER.error(e.getMessage(), e);
		}
        return "";
    }
}
