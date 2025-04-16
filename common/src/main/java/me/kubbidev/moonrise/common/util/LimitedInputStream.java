package me.kubbidev.moonrise.common.util;

import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class LimitedInputStream extends FilterInputStream implements Closeable {

    private final long limit;
    private       long count;

    /**
     * Creates a {@code FilterInputStream} by assigning the  argument {@code in} to the field {@code this.in} so as to
     * remember it for later use.
     *
     * @param in the underlying input stream, or {@code null} if this instance is to be created without an underlying
     *           stream.
     */
    public LimitedInputStream(InputStream in, long limit) {
        super(in);
        this.limit = limit;
    }

    private void checkLimit() throws IOException {
        if (this.count > this.limit) {
            throw new IOException("Limit exceeded");
        }
    }

    @Override
    public int read() throws IOException {
        int res = super.read();
        if (res != -1) {
            this.count++;
            this.checkLimit();
        }
        return res;
    }

    @Override
    public int read(byte @NotNull [] b, int off, int len) throws IOException {
        int res = super.read(b, off, len);
        if (res > 0) {
            this.count += res;
            this.checkLimit();
        }
        return res;
    }
}