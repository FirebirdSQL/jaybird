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
import org.firebirdsql.gds.VaxEncoding;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serial;
import java.util.EnumSet;

/**
 * {@link Argument} implementation for bigint (long) values.
 * @since 3.0
 */
public final class BigIntArgument extends TypedArgument {

    private static final EnumSet<ArgumentType> SUPPORTED_ARGUMENT_TYPES =
            EnumSet.of(ArgumentType.TraditionalDpb, ArgumentType.Wide, ArgumentType.IntSpb, ArgumentType.BigIntSpb);
    @Serial
    private static final long serialVersionUID = -6152038317321572191L;

    private final long value;

    public BigIntArgument(int type, ArgumentType argumentType, long value) {
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
    public void copyTo(ParameterBuffer buffer, Encoding encoding) {
        buffer.addArgument(getType(), value);
    }

    @Override
    public boolean equals(Object other) {
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
