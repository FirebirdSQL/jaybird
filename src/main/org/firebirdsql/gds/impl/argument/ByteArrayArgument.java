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
import org.firebirdsql.gds.impl.ParameterBufferMetaData;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * {@link Argument} implementation for byte arrays.
 * <p>
 * This implementation supports byte arrays up to length of the argument type.
 * </p>
 */
public final class ByteArrayArgument extends TypedArgument {

    private static final long serialVersionUID = -8636439991275911102L;
    
    private final byte[] value;

    /**
     * Initialises an instance of ByteArrayArgument.
     *
     * @param type
     *         parameter type
     * @param argumentType
     *         argument type
     * @param value
     *         byte array
     * @throws IllegalArgumentException
     *         if {@code type} is not valid for byte arrays, or if {@code value} is {@code null}
     * @throws LengthOverflowException
     *         if the length of {@code value} exceeds {@link ArgumentType#getMaxLength()}
     */
    public ByteArrayArgument(int type, ArgumentType argumentType, byte[] value) {
        super(type, argumentType);
        if (argumentType != ArgumentType.TraditionalDpb && argumentType != ArgumentType.Wide
                && argumentType != ArgumentType.StringSpb) {
            throw new IllegalArgumentException(
                    "ByteArrayArgument only works for TraditionalDpb, Wide, or StringSpb was: " + argumentType);
        }
        if (value == null) {
            throw new IllegalArgumentException("byte array value should not be null");
        }
        if (value.length > argumentType.getMaxLength()) {
            throw new LengthOverflowException(
                    String.format("byte array value should not be longer than %d bytes, length was %d",
                            argumentType.getMaxLength(), value.length));
        }
        this.value = value;
    }

    @Override
    public void writeTo(OutputStream outputStream) throws IOException {
        outputStream.write(getType());
        argumentType.writeLength(value.length, outputStream);
        outputStream.write(value);
    }

    @Override
    public int getLength() {
        return 1 + argumentType.getLengthSize() + value.length;
    }

    @Override
    public int getValueAsInt() {
        if (value.length == 1) {
            return value[0];
        } else {
            throw new UnsupportedOperationException("This method is not supported for byte arrays with length > 1");
        }
    }

    // primarily intended for testing purposes
    public byte[] getValueAsBytes() {
        return value.clone();
    }

    @Override
    public void copyTo(ParameterBuffer buffer, Encoding encoding) {
        buffer.addArgument(getType(), value.clone());
    }

    @Override
    public ByteArrayArgument transformTo(ParameterBufferMetaData parameterBufferMetaData) {
        ArgumentType newArgumentType = parameterBufferMetaData.getByteArrayArgumentType(getType());
        if (newArgumentType == argumentType) return this;
        return new ByteArrayArgument(getType(), newArgumentType, value);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ByteArrayArgument)) {
            return false;
        }

        final ByteArrayArgument otherByteArrayArgument = (ByteArrayArgument) other;

        return this.getType() == otherByteArrayArgument.getType()
                && Arrays.equals(this.value, otherByteArrayArgument.value);
    }

    @Override
    public int hashCode() {
        int result = 23;
        result = 41 * result + getType();
        result = 41 * result + Arrays.hashCode(value);
        return result;
    }
}
