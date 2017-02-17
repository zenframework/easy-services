package org.zenframework.easyservices.js.babel;

public interface Babel {

    String PRESET_REACT = "react";

    String transform(String script, String... presets) throws Exception;

}
