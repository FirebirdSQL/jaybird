/*
 * $Id$
 *
 * Firebird Open Source J2ee connector - jdbc driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.gds.impl.wire;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Base class for BlobParameterBufferImp and DatabaseParameterBufferImp and
 * perhaps eventually TransactionParameterBuffer.
 */
public abstract class ParameterBufferBase implements java.io.Serializable, Xdrable {
    // Parameter Buffer Implementation

    public void addArgument(int argumentType, String value) {
        getArgumentsList().add(new StringArgument(argumentType, value));
    }

    public void addArgument(int argumentType, int value) {
        getArgumentsList().add(new NumericArgument(argumentType, value));
    }

    public void addArgument(int argumentType) {
        getArgumentsList().add(new SingleItem(argumentType));
    }

    public void addArgument(int type, byte[] content) {
        getArgumentsList().add(new ByteArrayArgument(type, content));
    }

    public String getArgumentAsString(int type) {
        final List<Argument> argumentsList = getArgumentsList();
        for (final Argument argument : argumentsList) {
            if (argument.getType() == type) {
                return argument.getValueAsString();
            }
        }
        return null;
    }

    public int getArgumentAsInt(int type) {
        final List<Argument> argumentsList = getArgumentsList();
        for (final Argument argument : argumentsList) {
            if (argument.getType() == type) {
                return argument.getValueAsInt();
            }
        }
        return 0;
    }

    public boolean hasArgument(int type) {
        final List<Argument> argumentsList = getArgumentsList();
        for (final Argument argument : argumentsList) {
            if (argument.getType() == type) return true;
        }
        return false;
    }

    public void removeArgument(int type) {
        final List<Argument> argumentsList = getArgumentsList();
        for (int i = 0, n = argumentsList.size(); i < n; i++) {
            final Argument argument = argumentsList.get(i);
            if (argument.getType() == type) {
                argumentsList.remove(i);
                return;
            }
        }
    }

    // Xdrable Implementation

    public int getLength() {
        final List<Argument> argumentsList = getArgumentsList();
        int length = 0;
        for (final Argument currentArgument : argumentsList) {
            length += currentArgument.getLength();
        }
        return length;
    }

    public void read(XdrInputStream inputStream, int length) throws IOException {
    }

    public void write(XdrOutputStream outputStream) throws IOException {
        writeArgumentsTo(outputStream);
    }

    // Object Implementation

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof ParameterBufferBase))
            return false;

        final ParameterBufferBase otherServiceBufferBase = (ParameterBufferBase) other;

        return otherServiceBufferBase.getArgumentsList().equals(this.getArgumentsList());
    }

    @Override
    public int hashCode() {
        return getArgumentsList().hashCode();
    }

    // Internal methods

    protected void writeArgumentsTo(OutputStream outputStream) throws IOException {
        for (final Argument currentArgument : arguments) {
            currentArgument.writeTo(outputStream);
        }
    }

    protected List<Argument> getArgumentsList() {
        return arguments;
    }

    // PRIVATE MEMBERS

    private final List<Argument> arguments = new ArrayList<Argument>();

    // ---------------------------------------------------------------------------
    // Inner Classes
    // ---------------------------------------------------------------------------

    // ---------------------------------------------------------------------------
    // Argument - Abstract base
    // ---------------------------------------------------------------------------
    protected abstract static class Argument implements java.io.Serializable {

        abstract int getType();

        String getValueAsString() {
            throw new RuntimeException("Cannot get the value for this argument type as a string");
        }

        int getValueAsInt() {
            throw new RuntimeException("Cannot get the value of this argument type as int");
        }

        abstract void writeTo(OutputStream outputStream) throws IOException;

        abstract int getLength();
    }

    // ---------------------------------------------------------------------------
    // StringArgument
    // ---------------------------------------------------------------------------
    protected static class StringArgument extends Argument {

        StringArgument(int type, String value) {
            this.type = type;
            this.value = value;
        }

        void writeTo(OutputStream outputStream) throws IOException {
            outputStream.write(type);

            final byte[] valueBytes = this.value.getBytes();
            final int valueLength = valueBytes.length;

            writeLength(valueLength, outputStream);
            outputStream.write(valueBytes);
        }

        int getLength() {
            return value.getBytes().length + 2;
        }

        @Override
        String getValueAsString() {
            return value;
        }

        @Override
        int getValueAsInt() {
            return Integer.parseInt(value);
        }

        protected void writeLength(int length, OutputStream outputStream) throws IOException {
            outputStream.write(length);
        }

        int getType() {
            return type;
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            if (other == null || !(other instanceof StringArgument))
                return false;

            final StringArgument otherStringArgument = (StringArgument) other;

            return type == otherStringArgument.type && value.equals(otherStringArgument.value);
        }

        private final int type;
        private final String value;
    }

    // ---------------------------------------------------------------------------
    // NumericArgument
    // ---------------------------------------------------------------------------
    protected static class NumericArgument extends Argument {

        NumericArgument(int type, int value) {
            this.type = type;
            this.value = value;
        }

        void writeTo(OutputStream outputStream) throws IOException {
            outputStream.write(type);
            writeValue(outputStream, value);
        }

        int getLength() {
            return 6;
        }

        protected void writeValue(OutputStream outputStream, final int value) throws IOException {
            outputStream.write(4);
            outputStream.write(value);
            outputStream.write(value >> 8);
            outputStream.write(value >> 16);
            outputStream.write(value >> 24);
        }

        int getType() {
            return type;
        }

        @Override
        int getValueAsInt() {
            return value;
        }

        @Override
        public int hashCode() {
            return type;
        }

        @Override
        public boolean equals(Object other) {
            if (other == null || !(other instanceof NumericArgument))
                return false;

            final NumericArgument otherNumericArgument = (NumericArgument) other;

            return type == otherNumericArgument.type && value == otherNumericArgument.value;
        }

        private final int type;
        private final int value;
    }

    // ---------------------------------------------------------------------------
    // ByteArrayArgument
    // ---------------------------------------------------------------------------
    private static final class ByteArrayArgument extends Argument {

        ByteArrayArgument(int type, byte[] value) {
            this.type = type;
            this.value = value;
        }

        void writeTo(OutputStream outputStream) throws IOException {
            outputStream.write(type);
            final int valueLength = value.length;
            writeLength(valueLength, outputStream);
            outputStream.write(value);
        }

        protected void writeLength(int length, OutputStream outputStream) throws IOException {
            outputStream.write(length);
        }

        int getType() {
            return type;
        }

        int getLength() {
            return value.length + 2;
        }

        @Override
        int getValueAsInt() {
            if (value.length == 1)
                return value[0];
            else
                throw new UnsupportedOperationException("This method is not supported for byte arrays with length > 1");
        }

        @Override
        public int hashCode() {
            return type;
        }

        @Override
        public boolean equals(Object other) {
            if (other == null || !(other instanceof ByteArrayArgument))
                return false;

            final ByteArrayArgument otherByteArrayArgument = (ByteArrayArgument) other;

            return type == otherByteArrayArgument.type && Arrays.equals(value, otherByteArrayArgument.value);
        }

        private final int type;
        private final byte[] value;
    }

    // ---------------------------------------------------------------------------
    // SingleItem
    // ---------------------------------------------------------------------------
    private static final class SingleItem extends Argument {

        SingleItem(int item) {
            this.item = item;
        }

        void writeTo(OutputStream outputStream) throws IOException {
            outputStream.write(item);
        }

        int getLength() {
            return 1;
        }

        int getType() {
            return item;
        }

        @Override
        public int hashCode() {
            return item;
        }

        @Override
        public boolean equals(Object other) {
            if (other == null || !(other instanceof SingleItem))
                return false;

            final SingleItem otherSingleItem = (SingleItem) other;

            return item == otherSingleItem.item;
        }

        private final int item;
    }
}
