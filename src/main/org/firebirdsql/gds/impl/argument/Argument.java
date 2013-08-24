/*
 * $Id$
 * 
 * Firebird Open Source J2EE Connector - JDBC Driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.gds.impl.argument;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * Base class for arguments to the parameter buffer.
 */
public abstract class Argument implements Serializable {

    private final int type;

    protected Argument(int type) {
        this.type = type;
    }

    /**
     * @return The type of the argument
     */
    public final int getType() {
        return type;
    }

    /**
     * @return The value as string
     */
    public String getValueAsString() {
        throw new RuntimeException("Cannot get the value for this argument type as a string");
    }

    /**
     * @return The value as int
     */
    public int getValueAsInt() {
        throw new RuntimeException("Cannot get the value of this argument type as int");
    }

    /**
     * Writes the arguments to the supplied {@link OutputStream} in the XDR format of the type.
     *
     * @param outputStream OutputStream
     * @throws IOException For errors writing to the OutputStream
     */
    public abstract void writeTo(OutputStream outputStream) throws IOException;

    /**
     * @return Total length of the buffer item when written to the OutputStream by {@link #writeTo(java.io.OutputStream)}.
     * This includes the item, the value and other items contributing to the total length (eg the length of the value).
     */
    public abstract int getLength();

    @Override
    public int hashCode() {
        return type;
    }
}
