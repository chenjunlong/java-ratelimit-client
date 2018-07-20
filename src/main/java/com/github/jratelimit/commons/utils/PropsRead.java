package com.github.jratelimit.commons.utils;

import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by chenjunlong on 2018/7/20.
 */
public class PropsRead<K, V> {

    private String url;

    private Properties properties = new Properties();

    private Map<K, V> map = new HashMap<>();

    public PropsRead(String url) throws IOException {
        this.url = url;
        this.read();
    }

    private void read() throws IOException {
        File file = ResourceUtils.getFile("classpath:" + url);
        if (!file.exists()) {
            throw new FileNotFoundException("classpath not found" + url);
        }
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(url);
        properties.load(in);
    }

    public void parseMapIntValue() {
        Enumeration en = properties.propertyNames();
        while (en.hasMoreElements()) {
            String key = (String) en.nextElement();
            Integer value = Integer.parseInt(properties.getProperty(key));
            map.put((K) key, (V) value);
        }
    }

    public void parseMapStringValue() {
        Enumeration en = properties.propertyNames();
        while (en.hasMoreElements()) {
            String key = (String) en.nextElement();
            String value = properties.getProperty(key);
            map.put((K) key, (V) value);
        }
    }

    public Properties getProperties() {
        return properties;
    }

    public Map<K, V> getMap() {
        return map;
    }
}
