// SPDX-FileCopyrightText: Copyright 2015-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.impl.argument;

import org.firebirdsql.gds.VaxEncoding;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Argument metadata type.
 * <p>
 * Indicates how the argument should be represented in the parameter buffer. Primary use case is to distinguish between
 * 3.0 "wide" arguments, and 2.5 and earlier "traditional" arguments.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
@SuppressWarnings("java:S115")
public enum ArgumentType {
    TraditionalDpb {
        @Override
        public int getLengthSize() {
            return 1;
        }

        @Override
        public int getMaxLength() {
            return 255;
        }

        @Override
        public void writeLength(int length, OutputStream outputStream) throws IOException {
            outputStream.write(length);
        }
    },
    SingleTpb {
        @Override
        public int getLengthSize() {
            return 0;
        }

        @Override
        public int getMaxLength() {
            return 0;
        }

        @Override
        public void writeLength(int length, OutputStream outputStream) {
            // Do nothing; no length encoding
        }
    },
    StringSpb {
        @Override
        public int getLengthSize() {
            return 2;
        }

        @Override
        public int getMaxLength() {
            return 65536;
        }

        @Override
        public void writeLength(int length, OutputStream outputStream) throws IOException {
            VaxEncoding.encodeVaxInteger2WithoutLength(outputStream, length);
        }
    },
    BigIntSpb {
        @Override
        public int getLengthSize() {
            return 0;
        }

        @Override
        public int getMaxLength() {
            return 8;
        }

        @Override
        public void writeLength(int length, OutputStream outputStream) {
            // Do nothing; no length encoding
        }
    },
    IntSpb {
        @Override
        public int getLengthSize() {
            return 0;
        }

        @Override
        public int getMaxLength() {
            return 4;
        }

        @Override
        public void writeLength(int length, OutputStream outputStream) {
            // Do nothing; no length encoding
        }
    },
    ByteSpb {
        @Override
        public int getLengthSize() {
            return 0;
        }

        @Override
        public int getMaxLength() {
            return 1;
        }

        @Override
        public void writeLength(int length, OutputStream outputStream) {
            // Do nothing; no length encoding
        }
    },
    Wide {
        @Override
        public int getLengthSize() {
            return 4;
        }

        @Override
        public int getMaxLength() {
            // Firebird theoretically supports longer, but given array limitations in Java this is sufficient
            return Integer.MAX_VALUE;
        }

        @Override
        public void writeLength(int length, OutputStream outputStream) throws IOException {
            VaxEncoding.encodeVaxIntegerWithoutLength(outputStream, length);
        }
    };

    /**
     * @return The size of the encoded length; {@code 0} (zero) if the length is not part of the message.
     */
    public abstract int getLengthSize();

    /**
     * @return The maximum length of encoded data.
     */
    public abstract int getMaxLength();

    /**
     * Writes the length into the stream in the proper format.
     * <p>
     * For arguments without encoded length, this method should not do anything.
     * </p>
     *
     * @param length length to encode
     * @param outputStream Output stream
     * @throws IOException For errors writing to the stream
     */
    public abstract void writeLength(int length, OutputStream outputStream) throws IOException;
}
