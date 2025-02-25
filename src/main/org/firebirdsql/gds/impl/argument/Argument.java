/*
 SPDX-FileCopyrightText: Copyright 2003 Ryan Baldwin
 SPDX-FileCopyrightText: Copyright 2003-2005 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2004 Gabriel Reid
 SPDX-FileCopyrightText: Copyright 2012-2023 Mark Rotteveel
 SPDX-License-Identifier: LGPL-2.1-or-later
*/
package org.firebirdsql.gds.impl.argument;

import org.firebirdsql.gds.Parameter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serial;
import java.io.Serializable;

/**
 * Base class for arguments to the parameter buffer.
 */
public abstract class Argument implements Parameter, Serializable {

    @Serial
    private static final long serialVersionUID = -4547334570142529123L;
    
    private final int type;

    protected Argument(int type) {
        this.type = type;
    }

    /**
     * @return The type of the argument
     */
    @Override
    public final int getType() {
        return type;
    }

    /**
     * @return The value as string
     */
    @Override
    public String getValueAsString() {
        throw new UnsupportedOperationException("Cannot get the value for this argument type as a string");
    }

    /**
     * @return The value as int
     */
    @Override
    public int getValueAsInt() {
        throw new UnsupportedOperationException("Cannot get the value of this argument type as int");
    }

    /**
     * @return The value as long
     */
    @Override
    public long getValueAsLong() {
        throw new UnsupportedOperationException("Cannot get the value of this argument type as long");
    }

    /**
     * Writes the arguments to the supplied {@link OutputStream} in the XDR format of the type.
     *
     * @param outputStream
     *         OutputStream
     * @throws IOException
     *         For errors writing to the OutputStream
     */
    public abstract void writeTo(OutputStream outputStream) throws IOException;

    /**
     * @return Total length of the buffer item when written to the OutputStream by {@link #writeTo(java.io.OutputStream)}.
     * This includes the item, the value and other items contributing to the total length (e.g. the length of the value).
     */
    public abstract int getLength();

}
