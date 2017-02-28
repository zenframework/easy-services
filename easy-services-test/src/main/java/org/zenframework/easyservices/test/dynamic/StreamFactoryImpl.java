package org.zenframework.easyservices.test.dynamic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.server.UnicastRemoteObject;

public class StreamFactoryImpl extends UnicastRemoteObject implements StreamFactory {

    private static final long serialVersionUID = 1L;

    private final File sourceFile;
    private final File targetFile;

    public StreamFactoryImpl(File sourceFile, File targetFile) throws IOException {
        this.sourceFile = sourceFile;
        this.targetFile = targetFile;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(sourceFile);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return new FileOutputStream(targetFile);
    }

    @Override
    public RmiInputStream getRmiInputStream() throws IOException {
        return new RmiInputStreamImpl(getInputStream());
    }

    @Override
    public RmiOutputStream getRmiOutputStream() throws IOException {
        return new RmiOutputStreamImpl(getOutputStream());
    }

}
