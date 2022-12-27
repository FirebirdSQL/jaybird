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
package org.firebirdsql.gds.impl.argument;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.gds.ParameterBuffer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.EnumSet;

/**
 * {@link Argument} implementation for byte values.
 * @since 5
 */
public final class ByteArgument extends Argument {

    private static final EnumSet<ArgumentType> SUPPORTED_ARGUMENT_TYPES =
            EnumSet.of(ArgumentType.TraditionalDpb, ArgumentType.Wide, ArgumentType.ByteSpb);
    private final ArgumentType argumentType;
    private final byte value;

    public ByteArgument(int type, ArgumentType argumentType, byte value) {
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
        // 2: 1 for type + 1 for data
        return 2 + argumentType.getLengthSize();
    }

    private void writeValue(final OutputStream outputStream, final byte value) throws IOException {
        argumentType.writeLength(1, outputStream);
        outputStream.write(value);
    }

    @Override
    public int getValueAsInt() {
        return value;
    }

    @Override
    public long getValueAsLong() {
        return value;
    }

    @Override
    public void copyTo(ParameterBuffer buffer, Encoding encoding) {
        buffer.addArgument(getType(), value);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ByteArgument)) {
            return false;
        }

        final ByteArgument otherByteArgument = (ByteArgument) other;

        return this.getType() == otherByteArgument.getType() && this.value == otherByteArgument.value;
    }

    @Override
    public int hashCode() {
        int result = 41 * 23 + getType();
        result = 41 * result + value;
        return result;
    }
}
