// SPDX-FileCopyrightText: Copyright 2001-2002 David Jencks
// SPDX-FileCopyrightText: Copyright 2002-2005 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2003 Blas Rodriguez Somoza
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.impl.wire;

import java.io.IOException;

/**
 * The interface <code>Xdrable</code> represents an object that can read
 * and write itself from a strean in the xdr format used by the firebird 
 * engine.
 *
 * @author David Jencks
 * @version 1.0
 */
public interface Xdrable {

    /**
     * Get the total length (in bytes) of this <code>Xdrable</code> when it
     * is written to XDR format.
     *
     * @return The total length in bytes
     */
    int getLength();

    /**
     * Read in <code>Xdrable</code> in XDR format, from an 
     * <code>XdrInputStream</code>.
     *
     * @param in The input stream from which the object is to be read.
     * @param length The number of bytes to be read
     * @throws IOException if an error occurs while reading from 
     *         the <code>XdrInputStream</code>
     */
    void read(XdrInputStream in, int length) throws IOException;

    /**
     * Write the this <code>Xdrable</code> out in XDR format to
     * an <code>XdrOutputStream</code>.
     *
     * @param out The output stream to which the <code>Xdrable</code> is 
     *        to be written
     * @throws IOException if an error occurs while writing to the
     *         <code>XdrOutputStream</code>
     */
    void write(XdrOutputStream out) throws IOException;

}
