package com.delfino.db.properties;

import com.delfino.util.AppProperties;
import com.facebook.presto.jdbc.PrestoConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.stream.Collectors;

public class PrestoPropertiesSetter extends PropertiesSetter {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrestoPropertiesSetter.class);

    @Override
    public void setProperties(Connection connection) throws SQLException {

        String prefix = getDbType() + ".";
        PrestoConnection pc = connection.unwrap(PrestoConnection.class);
        Map<String, Object> properties =
                AppProperties.getInstance().entrySet().stream().filter(e -> ((String)e.getKey()).startsWith(prefix))
                .collect(Collectors.toMap(e -> ((String)e.getKey()).replace("presto.", ""), e -> e.getValue()));
        LOGGER.info("Setting properties specific to " + getDbType());
        properties.entrySet().stream().forEach(e -> LOGGER.info(e.toString()));
        properties.entrySet().forEach(e -> pc.setSessionProperty((String)e.getKey(), String.valueOf(e.getValue())));
    }
}
