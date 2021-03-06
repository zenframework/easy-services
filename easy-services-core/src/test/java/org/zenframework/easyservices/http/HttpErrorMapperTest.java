package org.zenframework.easyservices.http;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import org.zenframework.easyservices.http.HttpErrorMapper;

import junit.framework.TestCase;

public class HttpErrorMapperTest extends TestCase {

    private final HttpErrorMapper errorMapper = new HttpErrorMapper();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        errorMapper.getErrorsMap().put(FileNotFoundException.class, 1);
        errorMapper.getErrorsMap().put(IOException.class, 2);
        errorMapper.setDefaultStatus(3);
    }

    public void testGetStatus() throws Exception {
        assertErrorStatus(new FileNotFoundException(), 1);
        assertErrorStatus(new MalformedURLException(), 2);
        assertErrorStatus(new IOException(), 2);
        assertErrorStatus(new Exception(), 3);
    }

    private void assertErrorStatus(Throwable e, int expectedStatus) {
        assertEquals(expectedStatus, errorMapper.getStatus(e));
    }

}
