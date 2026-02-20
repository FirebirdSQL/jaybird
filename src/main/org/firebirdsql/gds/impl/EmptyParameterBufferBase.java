// SPDX-FileCopyrightText: Copyright 2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.impl;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.gds.Parameter;
import org.firebirdsql.gds.ParameterBuffer;
import org.firebirdsql.gds.impl.wire.XdrInputStream;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.impl.wire.Xdrable;
import org.firebirdsql.jaybird.util.ByteArrayHelper;
import org.jspecify.annotations.Nullable;

import java.io.OutputStream;
import java.io.Serial;
import java.util.Collections;
import java.util.Iterator;

/**
 * Base class for empty and immutable parameter buffers.
 *
 * @since 7
 */
abstract sealed class EmptyParameterBufferBase implements ParameterBuffer permits EmptyBlobParameterBuffer {

    @Serial
    private static final long serialVersionUID = 4686474795567008222L;

    private final ParameterBufferMetaData parameterBufferMetaData;

    EmptyParameterBufferBase(ParameterBufferMetaData parameterBufferMetaData) {
        this.parameterBufferMetaData = parameterBufferMetaData;
    }

    @Override
    public final int getType() {
        return parameterBufferMetaData.getType();
    }

    private static void immutable() {
        throw new UnsupportedOperationException("this parameter buffer is immutable");
    }

    @Override
    public final void addArgument(int argumentType) {
        immutable();
    }

    @Override
    public final void addArgument(int argumentType, String value) {
        immutable();
    }

    @Override
    public final void addArgument(int argumentType, String value, Encoding encoding) {
        immutable();
    }

    @Override
    public final void addArgument(int argumentType, byte value) {
        immutable();
    }

    @Override
    public final void addArgument(int argumentType, int value) {
        immutable();
    }

    @Override
    public final void addArgument(int argumentType, long value) {
        immutable();
    }

    @Override
    public final void addArgument(int argumentType, byte[] content) {
        immutable();
    }

    @Override
    public final void removeArgument(int argumentType) {
        immutable();
    }

    @Override
    public final @Nullable String getArgumentAsString(int argumentType) {
        return null;
    }

    @Override
    public final int getArgumentAsInt(int argumentType) {
        return 0;
    }

    @Override
    public final boolean hasArgument(int argumentType) {
        return false;
    }

    @Override
    public final Iterator<Parameter> iterator() {
        return Collections.emptyIterator();
    }

    @Override
    public final void writeArgumentsTo(OutputStream outputStream) {
        // nothing to write
    }

    @Override
    public final Xdrable toXdrable() {
        return new Xdrable() {
            @Override
            public int getLength() {
                return 0;
            }

            @Override
            public void read(XdrInputStream in, int length) {
                immutable();
            }

            @Override
            public void write(XdrOutputStream out) {
                // nothing to write
            }
        };
    }

    @Override
    public final byte[] toBytes() {
        return ByteArrayHelper.emptyByteArray();
    }

    @Override
    public final byte[] toBytesWithType() {
        return ByteArrayHelper.emptyByteArray();
    }

    @Override
    public final int size() {
        return 0;
    }

    @Override
    public final boolean isEmpty() {
        return true;
    }

}
