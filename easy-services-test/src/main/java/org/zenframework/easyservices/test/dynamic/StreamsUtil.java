package org.zenframework.easyservices.test.dynamic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

public class StreamsUtil {

    private StreamsUtil() {}

    public static void copy(InputStream in, OutputStream out) throws IOException {
        try {
            IOUtils.copy(in, out);
        } finally {
            IOUtils.closeQuietly(in, out);
        }
    }

    public static void copy(RmiInputStream in, RmiOutputStream out) throws IOException {
        try {
            for (byte[] buf = in.read(8192); buf != null; buf = in.read(8192))
                out.write(buf);
        } finally {
            IOUtils.closeQuietly(in, out);
        }
    }

    public static File createTestFile(File file, int size) throws IOException {
        OutputStream out = new FileOutputStream(file);
        byte[] buf = new byte[8192];
        try {
            for (int i = 0; i < size / 8192; i++) {
                for (int j = 0; j < 8192; j++)
                    buf[j] = (byte) (j % 256);
                out.write(buf);
            }
            if (size % 8192 > 0) {
                for (int j = 0; j < size % 8192; j++)
                    buf[j] = (byte) (j % 256);
                out.write(buf, 0, size % 8192);
            }
        } finally {
            out.close();
        }
        return file;
    }

    public static boolean equals(File f1, File f2) throws IOException {
        InputStream in1 = new FileInputStream(f1);
        InputStream in2 = new FileInputStream(f2);
        try {
            return IOUtils.contentEquals(in1, in2);
        } finally {
            IOUtils.closeQuietly(in1, in2);
        }
    }

}
