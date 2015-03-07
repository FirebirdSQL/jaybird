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

/**
 * {@link Argument} implementation for numeric (integer) values
 */
public class NumericArgument extends Argument {

    private final int value;

    public NumericArgument(int type, int value) {
        super(type);
        this.value = value;
    }

    public void writeTo(OutputStream outputStream) throws IOException {
        outputStream.write(getType());
        writeValue(outputStream, value);
    }

    public int getLength() {
        return 6;
    }

    protected void writeValue(OutputStream outputStream, final int value) throws IOException {
        outputStream.write(4);
        outputStream.write(value);
        outputStream.write(value >> 8);
        outputStream.write(value >> 16);
        outputStream.write(value >> 24);
    }

    @Override
    public int getValueAsInt() {
        return value;
    }

    @Override
    public void copyTo(ParameterBuffer buffer, Encoding encoding) {
        buffer.addArgument(getType(), value);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof NumericArgument))
            return false;

        final NumericArgument otherNumericArgument = (NumericArgument) other;

        return this.getType() == otherNumericArgument.getType() && this.value == otherNumericArgument.value;
    }

    @Override
    public int hashCode() {
        int result = 23;
        result = 41 * result + getType();
        result = 41 * result + value;
        return result;
    }
}
