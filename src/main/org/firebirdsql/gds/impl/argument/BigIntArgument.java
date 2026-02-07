/*
 SPDX-FileCopyrightText: Copyright 2003 Ryan Baldwin
 SPDX-FileCopyrightText: Copyright 2003-2005 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2004 Gabriel Reid
 SPDX-FileCopyrightText: Copyright 2012-2026 Mark Rotteveel
 SPDX-License-Identifier: LGPL-2.1-or-later
*/
package org.firebirdsql.gds.impl.argument;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.gds.ParameterBuffer;
import org.firebirdsql.gds.VaxEncoding;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serial;
import java.util.EnumSet;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;

/**
 * {@link Argument} implementation for bigint (long) values.
 * @since 3.0
 */
public final class BigIntArgument extends TypedArgument {

    private static final Set<ArgumentType> SUPPORTED_ARGUMENT_TYPES = unmodifiableSet(
            EnumSet.of(ArgumentType.TraditionalDpb, ArgumentType.Wide, ArgumentType.IntSpb, ArgumentType.BigIntSpb));
    @Serial
    private static final long serialVersionUID = -6152038317321572191L;

    private final long value;

    public BigIntArgument(int type, ArgumentType argumentType, long value) {
        super(type, checkArgumentType(argumentType, SUPPORTED_ARGUMENT_TYPES));
        this.value = value;
    }

    public void writeTo(final OutputStream outputStream) throws IOException {
        outputStream.write(getType());
        writeValue(outputStream, value);
    }

    public int getLength() {
        if (argumentType == ArgumentType.IntSpb) {
            // 5: 1 for type + 4 for data
            return 5 + argumentType.getLengthSize();
        }
        // 9: 1 for type + 8 for data
        return 9 + argumentType.getLengthSize();
    }

    private void writeValue(final OutputStream outputStream, final long value) throws IOException {
        if (argumentType == ArgumentType.IntSpb) {
            argumentType.writeLength(4, outputStream);
            VaxEncoding.encodeVaxIntegerWithoutLength(outputStream, (int) value);
        } else {
            argumentType.writeLength(8, outputStream);
            VaxEncoding.encodeVaxLongWithoutLength(outputStream, value);
        }
    }

    @Override
    public long getValueAsLong() {
        return value;
    }

    @Override
    public void copyTo(ParameterBuffer buffer, @Nullable Encoding encoding) {
        buffer.addArgument(getType(), value);
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) return true;
        if (!(other instanceof final BigIntArgument otherBigIntArgument)) return false;
        return this.getType() == otherBigIntArgument.getType() && this.value == otherBigIntArgument.value;
    }

    @Override
    public int hashCode() {
        int result = 41 * 23 + getType();
        result = 41 * result + Long.hashCode(value);
        return result;
    }
}
