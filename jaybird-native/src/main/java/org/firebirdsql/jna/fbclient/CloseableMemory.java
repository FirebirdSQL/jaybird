package org.firebirdsql.jna.fbclient;

import com.sun.jna.Memory;

/**
 * Memory class for send and receive native messages using OO API.
 *
 * @since 6.0
 */
public class CloseableMemory extends Memory implements AutoCloseable {

    public CloseableMemory(long size) {
        super(size);
    }
}
