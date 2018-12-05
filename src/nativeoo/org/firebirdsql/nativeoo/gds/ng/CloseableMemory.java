package org.firebirdsql.nativeoo.gds.ng;

import com.sun.jna.Memory;

/**
 * Memory class for send and receive native messages using OO API.
 *
 * @since 4.0
 */
public class CloseableMemory extends Memory implements AutoCloseable {

    public CloseableMemory(long size) {
        super(size);
    }

    @Override
    public void close() {
        dispose();
    }
}
