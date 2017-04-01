package org.zenframework.easyservices.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.LinkedMap;

public class DefaultHeader implements Header {

    public static final String PATH = "PATH";

    private final Map<String, List<String>> fields;

    public DefaultHeader() {
        this(new LinkedMap<String, List<String>>());
    }

    public DefaultHeader(Map<String, List<String>> fields) {
        this.fields = fields;
    }

    public List<String> getField(String name) {
        return fields.get(name);
    }

    public void setField(String name, Object value) {
        fields.put(name, Arrays.asList(value.toString()));
    }

    public void addFieldValue(String name, Object value) {
        List<String> values = fields.get(name);
        if (values == null) {
            values = new ArrayList<String>();
            fields.put(name, values);
        }
        values.add(value.toString());
    }

    public String getString(String name) {
        List<String> values = fields.get(name);
        return values != null && !values.isEmpty() ? values.get(values.size() - 1) : null;
    }

    public Boolean getBoolean(String name) {
        String value = getString(name);
        return value != null ? Boolean.parseBoolean(value) : null;
    }

    public Integer getInteger(String name) {
        String value = getString(name);
        return value != null ? Integer.parseInt(value) : null;
    }

    @Override
    public void read(InputStream in) throws IOException {
        fields.clear();
        DataInputStream data = new DataInputStream(in);
        int len = data.readInt();
        for (int i = 0; i < len; i++) {
            String name = data.readUTF();
            int size = data.readInt();
            List<String> values = new ArrayList<String>(size);
            for (int j = 0; j < size; j++)
                values.add(data.readUTF());
            fields.put(name, values);
        }
    }

    @Override
    public void write(OutputStream out) throws IOException {
        DataOutputStream data = new DataOutputStream(out);
        data.writeInt(fields.size());
        for (Map.Entry<String, List<String>> entry : fields.entrySet()) {
            data.writeUTF(entry.getKey());
            data.writeInt(entry.getValue().size());
            for (String value : entry.getValue())
                data.writeUTF(value);
        }
    }

    public Map<String, List<String>> getFields() {
        return fields;
    }

}
