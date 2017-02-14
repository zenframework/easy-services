package org.zenframework.easyservices.test.dynamic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamFactoryImpl implements StreamFactory {

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
    public OutputStream getOuptputStream() throws IOException {
        return new FileOutputStream(targetFile);
    }

}
