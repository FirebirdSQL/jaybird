/*
 SPDX-FileCopyrightText: Copyright 2003 Ryan Baldwin
 SPDX-FileCopyrightText: Copyright 2003-2005 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2004 Gabriel Reid
 SPDX-FileCopyrightText: Copyright 2012-2024 Mark Rotteveel
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
 * {@link Argument} implementation for byte values.
 *
 * @since 5
 */
public final class ByteArgument extends TypedArgument {

    private static final EnumSet<ArgumentType> SUPPORTED_ARGUMENT_TYPES =
            EnumSet.of(ArgumentType.TraditionalDpb, ArgumentType.Wide, ArgumentType.ByteSpb);
    @Serial
    private static final long serialVersionUID = 3202369601515235550L;
    
    private final byte value;

    public ByteArgument(int type, ArgumentType argumentType, byte value) {
        super(type, argumentType);
        if (!SUPPORTED_ARGUMENT_TYPES.contains(argumentType)) {
            throw new IllegalArgumentException("Invalid argument type: " + argumentType);
        }
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
    @SuppressWarnings("java:S4144")
    public long getValueAsLong() {
        return value;
    }

    @Override
    public void copyTo(ParameterBuffer buffer, Encoding encoding) {
        buffer.addArgument(getType(), value);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof final ByteArgument otherByteArgument)) return false;
        return this.getType() == otherByteArgument.getType() && this.value == otherByteArgument.value;
    }

    @Override
    public int hashCode() {
        int result = 41 * 23 + getType();
        result = 41 * result + value;
        return result;
    }
}
