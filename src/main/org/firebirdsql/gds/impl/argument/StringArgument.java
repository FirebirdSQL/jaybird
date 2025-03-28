/*
 SPDX-FileCopyrightText: Copyright 2003 Ryan Baldwin
 SPDX-FileCopyrightText: Copyright 2003-2005 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2004 Gabriel Reid
 SPDX-FileCopyrightText: Copyright 2012-2023 Mark Rotteveel
 SPDX-License-Identifier: LGPL-2.1-or-later
*/
package org.firebirdsql.gds.impl.argument;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.gds.ParameterBuffer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serial;
import java.util.EnumSet;

/**
 * {@link Argument} implementation for String values
 */
public final class StringArgument extends TypedArgument {

    private static final EnumSet<ArgumentType> SUPPORTED_ARGUMENT_TYPES =
            EnumSet.of(ArgumentType.TraditionalDpb, ArgumentType.Wide, ArgumentType.StringSpb);
    @Serial
    private static final long serialVersionUID = -7980793147101287101L;
    
    private final String value;
    private final byte[] asBytes;
    private final Encoding encoding;

    public StringArgument(int type, ArgumentType argumentType, String value, Encoding encoding) {
        super(type, argumentType);
        if (!SUPPORTED_ARGUMENT_TYPES.contains(argumentType)) {
            throw new IllegalArgumentException("Invalid argument type: " + argumentType);
        }
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
            throw new IllegalArgumentException(String.format(
                    "byte array derived from String value should not be longer than %d bytes, length was %d",
                    argumentType.getMaxLength(), asBytes.length));
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
        int result = 23;
        result = 41 * result + getType();
        result = 41 * result + value.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof final StringArgument otherStringArgument)) return false;
        return this.getType() == otherStringArgument.getType() && value.equals(otherStringArgument.value);
    }
}
