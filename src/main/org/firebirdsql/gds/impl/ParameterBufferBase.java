/*
 SPDX-FileCopyrightText: Copyright 2003 Ryan Baldwin
 SPDX-FileCopyrightText: Copyright 2003-2005 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2004 Gabriel Reid
 SPDX-FileCopyrightText: Copyright 2012-2026 Mark Rotteveel
 SPDX-License-Identifier: LGPL-2.1-or-later
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
import org.firebirdsql.jaybird.util.ByteArrayHelper;
import org.jspecify.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Base class for parameter buffers
 *
 * @author Mark Rotteveel
 */
public abstract class ParameterBufferBase implements ParameterBuffer, Serializable {

    @Serial
    private static final long serialVersionUID = 8812835147477954476L;

    private final List<Argument> arguments = new ArrayList<>();

    private final String defaultEncodingName;
    private ParameterBufferMetaData parameterBufferMetaData;
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
        addArgument(createStringArgument(argumentType, value, encoding));
    }

    private StringArgument createStringArgument(int argumentType, String value, Encoding encoding) {
        try {
            return new StringArgument(
                    argumentType, parameterBufferMetaData.getStringArgumentType(argumentType), value, encoding);
        } catch (LengthOverflowException e) {
            if (tryUpgradeMetaData()) {
                // Do not inline parameterBufferMetaData or argument type; they might be different here than in the try
                return new StringArgument(
                        argumentType, parameterBufferMetaData.getStringArgumentType(argumentType), value, encoding);
            }
            throw e;
        }
    }

    @Override
    public final void addArgument(int argumentType, byte value) {
        addArgument(new ByteArgument(argumentType, parameterBufferMetaData.getByteArgumentType(argumentType), value));
    }

    @Override
    public final void addArgument(int argumentType, int value) {
        addArgument(new NumericArgument(
                argumentType, parameterBufferMetaData.getIntegerArgumentType(argumentType), value));
    }

    @Override
    public final void addArgument(int argumentType, long value) {
        addArgument(new BigIntArgument(
                argumentType, parameterBufferMetaData.getIntegerArgumentType(argumentType), value));
    }

    @Override
    public final void addArgument(int argumentType) {
        addArgument(new SingleItem(argumentType, parameterBufferMetaData.getSingleArgumentType(argumentType)));
    }

    @Override
    public final void addArgument(int type, byte[] content) {
        addArgument(createByteArrayArgument(type, content));
    }

    private ByteArrayArgument createByteArrayArgument(int type, byte[] content) {
        try {
            return new ByteArrayArgument(type, parameterBufferMetaData.getByteArrayArgumentType(type), content);
        } catch (LengthOverflowException e) {
            if (tryUpgradeMetaData()) {
                // Do not inline parameterBufferMetaData or argument type; they might be different here than in the try
                return new ByteArrayArgument(type, parameterBufferMetaData.getByteArrayArgumentType(type), content);
            }
            throw e;
        }
    }

    protected final void addArgument(Argument argument) {
        arguments.add(argument);
    }

    @Override
    public final @Nullable String getArgumentAsString(int type) {
        return findFirst(type).map(Argument::getValueAsString).orElse(null);
    }

    @SuppressWarnings("OptionalIsPresent")
    @Override
    public final int getArgumentAsInt(int type) {
        Optional<Argument> argumentOpt = findFirst(type);
        return argumentOpt.isPresent() ? argumentOpt.get().getValueAsInt() : 0;
    }

    @Override
    public final boolean hasArgument(int type) {
        return findFirst(type).isPresent();
    }

    protected Optional<Argument> findFirst(int type) {
        return arguments.stream().filter(argument -> argument.getType() == type).findFirst();
    }

    @Override
    public final void removeArgument(int type) {
        Iterator<Argument> argumentIterator = arguments.iterator();
        while (argumentIterator.hasNext()) {
            if (argumentIterator.next().getType() == type) {
                argumentIterator.remove();
                return;
            }
        }
    }

    @Override
    public final Iterator<Parameter> iterator() {
        return new ArrayList<Parameter>(arguments).iterator();
    }

    public final void writeArgumentsTo(OutputStream outputStream) throws IOException {
        for (Argument currentArgument : arguments) {
            currentArgument.writeTo(outputStream);
        }
    }

    @Override
    public final Xdrable toXdrable() {
        return new ParameterBufferXdrable();
    }

    protected final int getLength() {
        return arguments.stream().mapToInt(Argument::getLength).sum();
    }

    protected final List<Argument> getArgumentsList() {
        return arguments;
    }

    @Override
    public final byte[] toBytes() {
        if (isEmpty()) return ByteArrayHelper.emptyByteArray();
        var bout = new ByteArrayOutputStream();
        try {
            writeArgumentsTo(bout);
        } catch (IOException e) {
            // Doesn't happen with ByteArrayOutputStream
        }
        return bout.toByteArray();
    }

    @Override
    public final byte[] toBytesWithType() {
        if (isEmpty()) return ByteArrayHelper.emptyByteArray();
        var bout = new ByteArrayOutputStream();
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
    public final boolean isEmpty() {
        return arguments.isEmpty();
    }

    private boolean tryUpgradeMetaData() {
        if (!parameterBufferMetaData.isUpgradable()) return false;
        try {
            ParameterBufferMetaData newParameterBufferMetaData = parameterBufferMetaData.upgradeMetaData();
            List<Argument> newArguments = arguments.stream()
                    .map(argument -> argument.transformTo(newParameterBufferMetaData))
                    .toList();
            arguments.clear();
            arguments.addAll(newArguments);
            parameterBufferMetaData = newParameterBufferMetaData;
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    @SuppressWarnings("java:S2097")
    public final boolean equals(@Nullable Object other) {
        if (other == null || !(this.getClass().isAssignableFrom(other.getClass()))) {
            return false;
        }

        final ParameterBufferBase otherServiceBufferBase = (ParameterBufferBase) other;
        return otherServiceBufferBase.arguments.equals(this.arguments);
    }

    @Override
    public final int hashCode() {
        return arguments.hashCode();
    }

    /**
     * Default implementation for serializing the parameter buffer to the XDR output stream
     */
    private final class ParameterBufferXdrable implements Xdrable {
        @Override
        public int getLength() {
            return ParameterBufferBase.this.getLength();
        }

        @Override
        public void read(XdrInputStream inputStream, int length) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void write(final XdrOutputStream outputStream) throws IOException {
            writeArgumentsTo(outputStream);
        }
    }

    @Serial
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        defaultEncoding = EncodingFactory.getPlatformDefault().getEncodingForCharsetAlias(defaultEncodingName);
    }
}
