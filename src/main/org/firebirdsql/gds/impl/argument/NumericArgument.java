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
import org.firebirdsql.gds.VaxEncoding;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serial;
import java.util.EnumSet;

/**
 * {@link Argument} implementation for numeric (integer) values
 */
public final class NumericArgument extends TypedArgument {

    private static final EnumSet<ArgumentType> SUPPORTED_ARGUMENT_TYPES =
            EnumSet.of(ArgumentType.TraditionalDpb, ArgumentType.Wide, ArgumentType.IntSpb, ArgumentType.ByteSpb);
    @Serial
    private static final long serialVersionUID = -1575745288263119101L;
    
    private final int value;

    public NumericArgument(int type, ArgumentType argumentType, int value) {
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
        if (argumentType == ArgumentType.ByteSpb) {
            // 2: 1 for type + 1 for data; no length
            return 2;
        }
        // 5: 1 for type + 4 for data
        return 5 + argumentType.getLengthSize();
    }

    private void writeValue(final OutputStream outputStream, final int value) throws IOException {
        if (argumentType == ArgumentType.ByteSpb) {
            outputStream.write(value);
        } else {
            argumentType.writeLength(4, outputStream);
            VaxEncoding.encodeVaxIntegerWithoutLength(outputStream, value);
        }
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
        if (!(other instanceof final NumericArgument otherNumericArgument)) return false;
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
