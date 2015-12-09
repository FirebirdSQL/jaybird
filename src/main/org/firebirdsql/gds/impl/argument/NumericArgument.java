/*
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
import org.firebirdsql.gds.VaxEncoding;

import java.io.IOException;
import java.io.OutputStream;
import java.util.EnumSet;

/**
 * {@link Argument} implementation for numeric (integer) values
 */
public final class NumericArgument extends Argument {

    private static final EnumSet<ArgumentType> SUPPORTED_ARGUMENT_TYPES =
            EnumSet.of(ArgumentType.TraditionalDpb, ArgumentType.Wide, ArgumentType.IntSpb, ArgumentType.ByteSpb);
    private final ArgumentType argumentType;
    private final int value;

    public NumericArgument(int type, ArgumentType argumentType, int value) {
        super(type);
        if (!SUPPORTED_ARGUMENT_TYPES.contains(argumentType)) {
            throw new IllegalArgumentException("Invalid argument type: " + argumentType);
        }
        this.argumentType = argumentType;
        this.value = value;
    }

    public void writeTo(final OutputStream outputStream) throws IOException {
        outputStream.write(getType());
        writeValue(outputStream, value);
    }

    public int getLength() {
        if (argumentType == ArgumentType.ByteSpb) {
            // 2: 1 for type + 1 for data; no length
            return 2;
        }
        // 5: 1 for type + 4 for data
        return 5 + argumentType.getLengthSize();
    }

    protected void writeValue(final OutputStream outputStream, final int value) throws IOException {
        if (argumentType == ArgumentType.ByteSpb) {
            outputStream.write(value);
        } else {
            argumentType.writeLength(4, outputStream);
            VaxEncoding.encodeVaxIntegerWithoutLength(outputStream, value);
        }
    }

    @Override
    public final int getValueAsInt() {
        return value;
    }

    @Override
    public void copyTo(ParameterBuffer buffer, Encoding encoding) {
        buffer.addArgument(getType(), value);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof NumericArgument)) {
            return false;
        }

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
