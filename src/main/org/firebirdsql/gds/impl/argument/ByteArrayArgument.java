/*
 SPDX-FileCopyrightText: Copyright 2003 Ryan Baldwin
 SPDX-FileCopyrightText: Copyright 2003-2005 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2004 Gabriel Reid
 SPDX-FileCopyrightText: Copyright 2012-2023 Mark Rotteveel
 SPDX-FileCopyrightText: Copyright 2016 Ivan Arabadzhiev
 SPDX-License-Identifier: LGPL-2.1-or-later
*/
package org.firebirdsql.gds.impl.argument;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.gds.ParameterBuffer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serial;
import java.util.Arrays;

/**
 * {@link Argument} implementation for byte arrays.
 * <p>
 * This implementation supports byte arrays up to length of the argument type.
 * </p>
 */
public final class ByteArrayArgument extends TypedArgument {

    @Serial
    private static final long serialVersionUID = -8636439991275911102L;
    
    private final byte[] value;

    /**
     * Initializes an instance of ByteArrayArgument.
     *
     * @param type
     *        Parameter type
     * @param value
     *        Byte array with a maximum length defined by {@code argumentType}.
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
            throw new IllegalArgumentException(
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

    @Override
    public void copyTo(ParameterBuffer buffer, Encoding encoding) {
        buffer.addArgument(getType(), value.clone());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof final ByteArrayArgument otherByteArrayArgument)) return false;
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
