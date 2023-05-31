/*
 * Firebird Open Source JDBC Driver
 *
 * Distributable under LGPL license.
 * You may obtain a copy of the License at http://www.gnu.org/copyleft/lgpl.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * LGPL License for more details.
 *
 * This file was created by members of the firebird development team.
 * All individual contributions remain the Copyright (C) of those
 * individuals.  Contributors to this file are either listed here or
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.jna.fbclient;

import com.sun.jna.IntegerType;
import com.sun.jna.Native;

/**
 * JNA wrapper for ISC_STATUS. Size depends on pointer size of the target platform.
 *
 * @author Mark Rotteveel
 */
public class ISC_STATUS extends IntegerType {
    /** Size of an ISC_STATUS, in bytes. */
    public static final int SIZE = Native.POINTER_SIZE;

    /** Create a zero-valued ISC_STATUS. */
    public ISC_STATUS() {
        this(0);
    }

    /** Create an ISC_STATUS with the given value. */
    public ISC_STATUS(long value) {
        super(SIZE, value);
    }
}
