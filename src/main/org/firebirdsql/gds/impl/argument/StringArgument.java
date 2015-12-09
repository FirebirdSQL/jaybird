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

import java.io.IOException;
import java.io.OutputStream;
import java.util.EnumSet;

/**
 * {@link Argument} implementation for String values
 */
public final class StringArgument extends Argument {

    private static final EnumSet<ArgumentType> SUPPORTED_ARGUMENT_TYPES =
            EnumSet.of(ArgumentType.TraditionalDpb, ArgumentType.Wide, ArgumentType.StringSpb);
    private final String value;
    private final byte[] asBytes;
    private final Encoding encoding;
    private final ArgumentType argumentType;

    @Deprecated
    public StringArgument(int type, ArgumentType argumentType, String value) {
        this(type, argumentType, value, EncodingFactory.getDefaultInstance().getDefaultEncoding());
    }

    public StringArgument(int type, ArgumentType argumentType, String value, Encoding encoding) {
        super(type);
        if (!SUPPORTED_ARGUMENT_TYPES.contains(argumentType)) {
            throw new IllegalArgumentException("Invalid argument type: " + argumentType);
        }
        this.argumentType = argumentType;
        if (encoding == null) {
            throw new IllegalArgumentException("Encoding is required");
        }
        if (value == null) {
            throw new IllegalArgumentException("String value should not be null");
        }
        this.value = value;
        asBytes = encoding.encodeToCharset(value);
        this.encoding = encoding;
        if (asBytes.length > argumentType.getMaxLength()) {
            throw new IllegalArgumentException(String.format("byte array derived from String value should not be longer than %d bytes, length was %d", argumentType.getMaxLength(), asBytes.length));
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
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof StringArgument)) {
            return false;
        }

        final StringArgument otherStringArgument = (StringArgument) other;

        return this.getType() == otherStringArgument.getType() && value.equals(otherStringArgument.value);
    }
}
