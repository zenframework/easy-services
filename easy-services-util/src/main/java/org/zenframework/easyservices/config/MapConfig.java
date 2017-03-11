package org.zenframework.easyservices.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapConfig extends AbstractConfig {

    private final Map<Object, Object> map = new HashMap<Object, Object>();

    public MapConfig(Map<?, ?> map) {
        this.map.putAll(map);
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public List<String> getNames() {
        List<String> list = new ArrayList<String>(map.size());
        for (Object key : map.keySet())
            list.add(key.toString());
        return list;
    }

    @Override
    public Object getParam(String name) {
        return map.get(name);
    }

    @Override
    public void setParam(String name, Object value) {
        map.put(name, value);
    }

}
