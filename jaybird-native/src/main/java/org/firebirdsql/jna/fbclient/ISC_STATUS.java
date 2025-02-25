// SPDX-FileCopyrightText: Copyright 2014-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jna.fbclient;

import com.sun.jna.IntegerType;
import com.sun.jna.Native;

import java.io.Serial;

/**
 * JNA wrapper for ISC_STATUS. Size depends on pointer size of the target platform.
 *
 * @author Mark Rotteveel
 */
@SuppressWarnings({ "unused", "java:S101" })
public class ISC_STATUS extends IntegerType {
    
    /** Size of an ISC_STATUS, in bytes. */
    public static final int SIZE = Native.POINTER_SIZE;
    
    @Serial
    private static final long serialVersionUID = 5394203292501996292L;

    /** Create a zero-valued ISC_STATUS. */
    public ISC_STATUS() {
        this(0);
    }

    /** Create an ISC_STATUS with the given value. */
    public ISC_STATUS(long value) {
        super(SIZE, value);
    }
}
