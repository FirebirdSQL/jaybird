/*
 * $Id$
 *
 * Public Firebird Java API.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    1. Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *    3. The name of the author may not be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.fields.BlrCalculator;

import java.io.IOException;
import java.sql.SQLException;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.3
 */
public interface FbWireDatabase extends FbDatabase, XdrStreamAccess {
    // TODO Revise exception usage

    /**
     * Reads the response from the server.
     *
     * @return {@link Response} read.
     * @throws SQLException
     *         For errors returned from the server, or when attempting to read
     * @throws IOException
     *         For errors reading the response from the connection.
     */
    Response readResponse() throws SQLException, IOException;

    /**
     * Release object. TODO Review usage
     *
     * @param operation
     *         Operation
     * @param objectId
     *         Id of the object to release
     * @throws SQLException
     */
    void releaseObject(int operation, int objectId) throws SQLException;

    /**
     * Convenience method to read a Response to a GenericResponse
     *
     * @return GenericResponse
     * @throws SQLException
     *         For errors returned from the server, or when attempting to
     *         read.
     * @throws IOException
     *         For errors reading the response from the connection.
     */
    GenericResponse readGenericResponse() throws SQLException, IOException;

    /**
     * Convenience method to read a Response to a SqlResponse
     *
     * @return SqlResponse
     * @throws SQLException
     *         For errors returned from the server, or when attempting to
     *         read.
     * @throws IOException
     *         For errors reading the response from the connection.
     */
    SqlResponse readSqlResponse() throws SQLException, IOException;

    /**
     * @return The {@link BlrCalculator} instance for this database.
     */
    BlrCalculator getBlrCalculator();

    /**
     * Reads the next operation. Forwards call to {@link org.firebirdsql.gds.ng.wire.WireConnection#readNextOperation()}. The result is stored internally and
     * can be retrieved once using {@link #readNextOperation()}
     *
     * @return next operation
     * @throws IOException
     */
    int readNextOperation() throws IOException;

    /**
     * Reads Vax style integers from the supplied buffer, starting at
     * <code>startPosition</code> and reading for <code>length</code> bytes.
     * <p>
     * This method is useful for lengths up to 4 bytes (ie normal Java integers
     * (<code>int</code>). Use {@link #iscVaxLong(byte[], int, int)} for reading
     * values with length up to 8 bytes.
     * </p>
     *
     * @param buffer
     *         The byte array from which the integer is to be retrieved
     * @param startPosition
     *         The offset starting position from which to start retrieving
     *         byte values
     * @return The integer value retrieved from the bytes
     * @see #iscVaxLong(byte[], int, int)
     * @see #iscVaxInteger2(byte[], int)
     */
    int iscVaxInteger(byte[] buffer, int startPosition, int length);

    /**
     * Reads Vax style integers from the supplied buffer, starting at
     * <code>startPosition</code> and reading for <code>length</code> bytes.
     * <p>
     * This method is useful for lengths up to 8 bytes (ie normal Java longs (
     * <code>long</code>).
     * </p>
     *
     * @param buffer
     *         The byte array from which the integer is to be retrieved
     * @param startPosition
     *         The offset starting position from which to start retrieving
     *         byte values
     * @return The integer value retrieved from the bytes
     * @see #iscVaxLong(byte[], int, int)
     * @see #iscVaxInteger2(byte[], int)
     */
    long iscVaxLong(byte[] buffer, int startPosition, int length);

    /**
     * Variant of {@link #iscVaxInteger(byte[], int, int)} specifically
     * for two-byte integers.
     * <p>
     * Implementations can either delegate to {@link #iscVaxInteger(byte[], int, int)},
     * or implement an optimized version.
     * </p>
     *
     * @param buffer
     *         The byte array from which the integer is to be retrieved
     * @param startPosition
     *         The offset starting position from which to start retrieving
     *         byte values
     * @return The integer value retrieved from the bytes
     * @see #iscVaxInteger(byte[], int, int)
     * @see #iscVaxLong(byte[], int, int)
     */
    int iscVaxInteger2(byte[] buffer, int startPosition);
}
