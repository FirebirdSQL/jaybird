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
import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.ParameterBuffer;
import org.firebirdsql.gds.impl.ParameterBufferMetaData;

import java.io.IOException;
import java.io.OutputStream;
import java.util.EnumSet;

/**
 * {@link Argument} implementation for String values
 */
public final class StringArgument extends TypedArgument {

    private static final EnumSet<ArgumentType> SUPPORTED_ARGUMENT_TYPES =
            EnumSet.of(ArgumentType.TraditionalDpb, ArgumentType.Wide, ArgumentType.StringSpb);
    private static final long serialVersionUID = -7980793147101287101L;

    private final String value;
    private final byte[] asBytes;
    private final Encoding encoding;

    /**
     * @deprecated will be removed in Jaybird 6, use {@link #StringArgument(int, ArgumentType, String, Encoding)}
     */
    @Deprecated
    public StringArgument(int type, ArgumentType argumentType, String value) {
        this(type, argumentType, value, EncodingFactory.getPlatformEncoding());
    }

    /**
     * Initialises an instance of StringArgument.
     *
     * @param type
     *         parameter type
     * @param argumentType
     *         argument type
     * @param value
     *         string value
     * @param encoding
     *         encoding to use to convert the string to bytes
     * @throws IllegalArgumentException
     *         if {@code type} is not valid for string values, or if {@code value} is {@code null}
     * @throws LengthOverflowException
     *         if the encoded length of {@code value} exceeds {@link ArgumentType#getMaxLength()}
     */
    public StringArgument(int type, ArgumentType argumentType, String value, Encoding encoding) {
        this(type, argumentType, value, null, encoding);
    }

    private StringArgument(int type, ArgumentType argumentType, String value, byte[] asBytes,
            Encoding encoding) {
        super(type, argumentType);
        if (!SUPPORTED_ARGUMENT_TYPES.contains(argumentType)) {
            throw new IllegalArgumentException("Invalid argument type: " + argumentType);
        } else if (encoding == null) {
            throw new IllegalArgumentException("Encoding is required");
        } else if (value == null) {
            throw new IllegalArgumentException("String value should not be null");
        }
        this.value = value;
        this.asBytes = asBytes != null ? asBytes : encoding.encodeToCharset(value);
        this.encoding = encoding;
        if (this.asBytes.length > argumentType.getMaxLength()) {
            throw new LengthOverflowException(String.format(
                    "byte array derived from String value should not be longer than %d bytes, length was %d",
                    argumentType.getMaxLength(), this.asBytes.length));
        }
    }

    @Override
    public void writeTo(final OutputStream outputStream) throws IOException {
        outputStream.write(getType());
        argumentType.writeLength(asBytes.length, outputStream);
        outputStream.write(asBytes);
    }

    @Override
    public int getLength() {
        return 1 + argumentType.getLengthSize() + asBytes.length;
    }

    @Override
    public String getValueAsString() {
        return value;
    }

    @Override
    public int getValueAsInt() {
        return Integer.parseInt(value);
    }

    @Override
    public void copyTo(ParameterBuffer buffer, Encoding stringEncoding) {
        buffer.addArgument(getType(), value, stringEncoding != null ? stringEncoding : encoding);
    }

    @Override
    public StringArgument transformTo(ParameterBufferMetaData parameterBufferMetaData) {
        ArgumentType newArgumentType = parameterBufferMetaData.getStringArgumentType(getType());
        if (newArgumentType == argumentType) return this;
        return new StringArgument(getType(), newArgumentType, value, asBytes, encoding);
    }

    @Override
    public int hashCode() {
        int result = 23;
        result = 41 * result + getType();
        result = 41 * result + value.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof StringArgument)) {
            return false;
        }

        final StringArgument otherStringArgument = (StringArgument) other;

        return this.getType() == otherStringArgument.getType() && value.equals(otherStringArgument.value);
    }
}
