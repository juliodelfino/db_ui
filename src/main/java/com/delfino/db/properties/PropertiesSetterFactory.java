package com.delfino.db.properties;

import org.apache.commons.lang.WordUtils;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class PropertiesSetterFactory {

    private static Map<String, PropertiesSetter> propsSetterMap = new HashMap<>();

    public static PropertiesSetter getPropertiesSetter(String dbType) throws SQLException {
        if (!propsSetterMap.containsKey(dbType)) {
            Class<? extends PropertiesSetter> clazz = null;
            try {
                clazz = Class.forName(PropertiesSetterFactory.class.getName().replaceAll("PropertiesSetterFactory", "") +
                        WordUtils.capitalize(dbType) + "PropertiesSetter").asSubclass(PropertiesSetter.class);
            } catch (ClassNotFoundException e) {
                return null;
            }
            try {
                PropertiesSetter setter = clazz.newInstance();
                setter.setDbType(dbType);
                propsSetterMap.put(dbType, setter);
            } catch (ReflectiveOperationException e) {
                throw new SQLException(e.getMessage(), e);
            }
        }
        return propsSetterMap.get(dbType);
    }
}
