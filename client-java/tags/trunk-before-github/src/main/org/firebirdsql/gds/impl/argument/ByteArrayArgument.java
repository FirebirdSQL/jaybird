/*
 * $Id$
 *
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
package org.firebirdsql.gds.impl.argument;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.gds.ParameterBuffer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * {@link Argument} implementation for byte arrays.
 * <p>
 * This implementation supports byte arrays up to 256 bytes.
 * </p>
 */
public final class ByteArrayArgument extends Argument {

    private final byte[] value;

    /**
     * Initializes an instance of ByteArrayArgument.
     *
     * @param type
     *         Parameter type
     * @param value
     *         Byte array with a length up to 255 bytes.
     */
    public ByteArrayArgument(int type, byte[] value) {
        super(type);
        if (value == null) {
            throw new IllegalArgumentException("byte array value should not be null");
        }
        if (value.length > 255) {
            throw new IllegalArgumentException("byte array value should not be longer than 255 bytes, length was " + value.length);
        }
        this.value = value;
    }

    @Override
    public void writeTo(OutputStream outputStream) throws IOException {
        outputStream.write(getType());
        writeLength(value.length, outputStream);
        outputStream.write(value);
    }

    @Override
    public int getLength() {
        return value.length + 2;
    }

    protected void writeLength(int length, OutputStream outputStream) throws IOException {
        outputStream.write(length);
    }

    @Override
    public int getValueAsInt() {
        if (value.length == 1)
            return value[0];
        else
            throw new UnsupportedOperationException("This method is not supported for byte arrays with length > 1");
    }

    @Override
    public void copyTo(ParameterBuffer buffer, Encoding encoding) {
        buffer.addArgument(getType(), value.clone());
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof ByteArrayArgument))
            return false;

        final ByteArrayArgument otherByteArrayArgument = (ByteArrayArgument) other;

        return this.getType() == otherByteArrayArgument.getType() && Arrays.equals(this.value, otherByteArrayArgument.value);
    }

    @Override
    public int hashCode() {
        int result = 23;
        result = 41 * result + getType();
        result = 41 * result + Arrays.hashCode(value);
        return result;
    }
}
