package org.zenframework.easyservices.http;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.zenframework.easyservices.ServiceException;

public class ErrorMapper {

    private static final int DEFAULT_STATUS = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

    private final Map<Class<? extends Throwable>, Integer> errorsMap = getDefaultErrorToStatusMap();
    private int defaultStatus = DEFAULT_STATUS;

    public Map<Class<? extends Throwable>, Integer> getErrorsMap() {
        return errorsMap;
    }

    public void setErrorsMap(Map<Class<? extends Throwable>, Integer> errorsMap) {
        this.errorsMap.clear();
        this.errorsMap.putAll(errorsMap);
    }

    public int getStatus(Throwable e) {
        for (Class<?> cls = e.getClass(); cls != null; cls = cls.getSuperclass())
            if (errorsMap.containsKey(cls))
                return errorsMap.get(cls);
        return defaultStatus;
    }

    public int getDefaultStatus() {
        return defaultStatus;
    }

    public void setDefaultStatus(int defaultStatus) {
        this.defaultStatus = defaultStatus;
    }

    private static Map<Class<? extends Throwable>, Integer> getDefaultErrorToStatusMap() {
        Map<Class<? extends Throwable>, Integer> map = new HashMap<Class<? extends Throwable>, Integer>();
        map.put(ServiceException.class, HttpServletResponse.SC_BAD_REQUEST);
        return map;
    }

}
