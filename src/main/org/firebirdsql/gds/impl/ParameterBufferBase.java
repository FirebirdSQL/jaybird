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
package org.firebirdsql.gds.impl;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.Parameter;
import org.firebirdsql.gds.ParameterBuffer;
import org.firebirdsql.gds.impl.argument.*;
import org.firebirdsql.gds.impl.wire.XdrInputStream;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.impl.wire.Xdrable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Base class for parameter buffers
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public abstract class ParameterBufferBase implements ParameterBuffer, Serializable {

    private final List<Argument> arguments = new ArrayList<>();

    private final String defaultEncodingName;
    private final ParameterBufferMetaData parameterBufferMetaData;
    private transient Encoding defaultEncoding;

    /**
     * Creates a {@code ParameterBufferBase}.
     * <p>
     * This uses a default encoding derived from the system default encoding. You usually want to
     * use {@link #ParameterBufferBase(ParameterBufferMetaData, Encoding)} instead.
     * </p>
     *
     * @param parameterBufferMetaData Metadata for the parameter buffer.
     */
    protected ParameterBufferBase(ParameterBufferMetaData parameterBufferMetaData) {
        this(parameterBufferMetaData, EncodingFactory.getPlatformEncoding());
    }

    /**
     * Creates a {@code ParameterBufferBase}.
     *
     * @param parameterBufferMetaData Metadata for the parameter buffer.
     * @param defaultEncoding Default encoding to use for string arguments
     */
    protected ParameterBufferBase(ParameterBufferMetaData parameterBufferMetaData, Encoding defaultEncoding) {
        this.parameterBufferMetaData = parameterBufferMetaData;
        defaultEncodingName = defaultEncoding.getCharsetName();
        this.defaultEncoding = defaultEncoding;
        parameterBufferMetaData.addPreamble(this);
    }

    public final Encoding getDefaultEncoding() {
        return defaultEncoding;
    }

    public final ParameterBufferMetaData getParameterBufferMetaData() {
        return parameterBufferMetaData;
    }

    @Override
    public final int getType() {
        return parameterBufferMetaData.getType();
    }

    @Override
    public final void addArgument(int argumentType, String value) {
        addArgument(argumentType, value, defaultEncoding);
    }

    @Override
    public final void addArgument(int argumentType, String value, Encoding encoding) {
        getArgumentsList().add(new StringArgument(argumentType, parameterBufferMetaData.getStringArgumentType(argumentType), value, encoding));
    }

    @Override
    public final void addArgument(int argumentType, int value) {
        getArgumentsList().add(new NumericArgument(argumentType, parameterBufferMetaData.getIntegerArgumentType(argumentType), value));
    }

    @Override
    public final void addArgument(int argumentType, long value) {
        getArgumentsList().add(new BigIntArgument(argumentType, parameterBufferMetaData.getIntegerArgumentType(argumentType), value));
    }

    @Override
    public final void addArgument(int argumentType) {
        getArgumentsList().add(new SingleItem(argumentType));
    }

    @Override
    public final void addArgument(int type, byte[] content) {
        getArgumentsList().add(new ByteArrayArgument(type, parameterBufferMetaData.getByteArrayArgumentType(type), content));
    }

    @Override
    public final String getArgumentAsString(int type) {
        final List<Argument> argumentsList = getArgumentsList();
        for (final Argument argument : argumentsList) {
            if (argument.getType() == type) {
                return argument.getValueAsString();
            }
        }
        return null;
    }

    @Override
    public final int getArgumentAsInt(int type) {
        final List<Argument> argumentsList = getArgumentsList();
        for (final Argument argument : argumentsList) {
            if (argument.getType() == type) {
                return argument.getValueAsInt();
            }
        }
        return 0;
    }

    @Override
    public final boolean hasArgument(int type) {
        final List<Argument> argumentsList = getArgumentsList();
        for (final Argument argument : argumentsList) {
            if (argument.getType() == type) return true;
        }
        return false;
    }

    @Override
    public final void removeArgument(int type) {
        final List<Argument> argumentsList = getArgumentsList();
        for (int i = 0, n = argumentsList.size(); i < n; i++) {
            final Argument argument = argumentsList.get(i);
            if (argument.getType() == type) {
                argumentsList.remove(i);
                return;
            }
        }
    }

    @Override
    public final Iterator<Parameter> iterator() {
        return new ArrayList<Parameter>(arguments).iterator();
    }

    public final void writeArgumentsTo(OutputStream outputStream) throws IOException {
        for (final Argument currentArgument : arguments) {
            currentArgument.writeTo(outputStream);
        }
    }

    @Override
    public final Xdrable toXdrable() {
        return new ParameterBufferXdrable();
    }

    protected final int getLength() {
        final List<Argument> argumentsList = getArgumentsList();
        int length = 0;
        for (final Argument currentArgument : argumentsList) {
            length += currentArgument.getLength();
        }
        return length;
    }

    protected final List<Argument> getArgumentsList() {
        return arguments;
    }

    @Override
    public final byte[] toBytes() {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            writeArgumentsTo(bout);
        } catch (IOException e) {
            // Doesn't happen with ByteArrayOutputStream
        }
        return bout.toByteArray();
    }

    @Override
    public final byte[] toBytesWithType() {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            bout.write(getType());
            writeArgumentsTo(bout);
        } catch (IOException e) {
            // Doesn't happen with ByteArrayOutputStream
        }
        return bout.toByteArray();
    }

    @Override
    public final int size() {
        return arguments.size();
    }

    @Override
    public final boolean equals(Object other) {
        if (other == null || !(this.getClass().isAssignableFrom(other.getClass())))
            return false;

        final ParameterBufferBase otherServiceBufferBase = (ParameterBufferBase) other;
        return otherServiceBufferBase.getArgumentsList().equals(this.getArgumentsList());
    }

    @Override
    public final int hashCode() {
        return getArgumentsList().hashCode();
    }

    /**
     * Default implementation for serializing the parameter buffer to the XDR output stream
     */
    private class ParameterBufferXdrable implements Xdrable {
        @Override
        public int getLength() {
            return ParameterBufferBase.this.getLength();
        }

        @Override
        public void read(XdrInputStream inputStream, int length) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void write(final XdrOutputStream outputStream) throws IOException {
            writeArgumentsTo(outputStream);
        }
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        defaultEncoding = EncodingFactory.getPlatformDefault().getEncodingForCharsetAlias(defaultEncodingName);
    }
}
