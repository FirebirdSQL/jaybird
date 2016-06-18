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
package org.firebirdsql.gds.impl.jni;

import org.firebirdsql.gds.ISCConstants;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import java.io.ByteArrayOutputStream;

/**
 * Provides implementation common to both ServiceParameterBufferImp and
 * ServiceRequestBufferImp
 */
abstract class ParameterBufferBase implements java.io.Serializable {

    // Parameter Buffer Implementation

    public void addArgument(int argumentType, String value) {
        getArgumentsList().add(new StringArgument(argumentType, value));
    }

    public void addArgument(int argumentType, int value) {
        getArgumentsList().add(new NumericArgument(argumentType, value));
    }

    public void addArgument(int argumentType, long value) {
        boolean isBigIntSpb = argumentType == ISCConstants.isc_spb_rpr_commit_trans_64
                || argumentType == ISCConstants.isc_spb_rpr_rollback_trans_64
                || argumentType == ISCConstants.isc_spb_rpr_recover_two_phase_64;
        if (isBigIntSpb) {
            getArgumentsList().add(new BigIntArgument(argumentType, value));
        } else {
            addArgument(argumentType, (int) value);
        }
    }

    public void addArgument(int argumentType) {
        getArgumentsList().add(new SingleItem(argumentType));
    }

    public void addArgument(int type, byte[] content) {
        getArgumentsList().add(new ByteArrayArgument(type, content));
    }

    public String getArgumentAsString(int type) {
        final List argumentsList = getArgumentsList();
        for (int i = 0, n = argumentsList.size(); i < n; i++) {
            final Argument argument = (Argument) argumentsList.get(i);
            if (argument.getType() == type) { return argument
                    .getValueAsString(); }
        }
        return null;
    }

    public int getArgumentAsInt(int type) {
        final List argumentsList = getArgumentsList();
        for (int i = 0, n = argumentsList.size(); i < n; i++) {
            final Argument argument = (Argument) argumentsList.get(i);
            if (argument.getType() == type) { return argument.getValueAsInt(); }
        }
        return 0;
    }

    public boolean hasArgument(int type) {
        final List argumentsList = getArgumentsList();

        for (int i = 0, n = argumentsList.size(); i < n; i++) {
            final Argument argument = (Argument) argumentsList.get(i);
            if (argument.getType() == type) return true;
        }
        return false;
    }

    public void removeArgument(int type) {
        final List argumentsList = getArgumentsList();
        for (int i = 0, n = argumentsList.size(); i < n; i++) {
            final Argument argument = (Argument) argumentsList.get(i);
            if (argument.getType() == type) {
                argumentsList.remove(i);
                return;
            }
        }
    }

    // Object Implementation

    public boolean equals(Object other) {
        if (other == null || !(other instanceof ParameterBufferBase))
            return false;

        final ParameterBufferBase otherServiceBufferBase = (ParameterBufferBase) other;

        return otherServiceBufferBase.getArgumentsList().equals(
                this.getArgumentsList());
    }

    public int hashCode() {
        return getArgumentsList().hashCode();
    }

    // Internal methods

    protected void writeArgumentsTo(ByteArrayOutputStream outputStream) {
        for (int i = 0, n = arguments.size(); i < n; i++) {
            final Argument currentArgument = ((Argument) arguments.get(i));

            currentArgument.writeTo(outputStream);
        }
    }

    protected List getArgumentsList() {
        return arguments;
    }

    // PRIVATE MEMBERS

    private final List arguments = new ArrayList();

    // ---------------------------------------------------------------------------
    // Inner Classes
    // ---------------------------------------------------------------------------

    // ---------------------------------------------------------------------------
    // Argument - Abstract base
    // ---------------------------------------------------------------------------
    protected abstract static class Argument implements java.io.Serializable {

        abstract int getType();

        String getValueAsString() {
            throw new UnsupportedOperationException("Cannot get the value for this argument type as a string");
        }

        int getValueAsInt() {
            throw new UnsupportedOperationException("Cannot get the value of this argument type as int");
        }

        /**
         * @return The value as long
         */
        long getValueAsLong() {
            throw new UnsupportedOperationException("Cannot get the value of this argument type as long");
        }

        abstract void writeTo(ByteArrayOutputStream outputStream);
    }

    // ---------------------------------------------------------------------------
    // StringArgument
    // ---------------------------------------------------------------------------
    protected static class StringArgument extends Argument {

        StringArgument(int type, String value) {
            this.type = type;
            this.value = value;
        }

        void writeTo(ByteArrayOutputStream outputStream) {
            outputStream.write(type);

            final byte[] valueBytes = this.value.getBytes();
            final int valueLength = valueBytes.length;

            writeLength(valueLength, outputStream);
            for (int i = 0; i < valueLength; i++)
                outputStream.write(valueBytes[i]);
        }

        String getValueAsString() {
            return value;
        }

        int getValueAsInt() {
            return Integer.parseInt(value);
        }

        protected void writeLength(int length, ByteArrayOutputStream outputStream) {
            outputStream.write(length);
        }

        int getType() {
            return type;
        }

        public int hashCode() {
            return value.hashCode();
        }

        public boolean equals(Object other) {
            if (other == null || !(other instanceof StringArgument))
                return false;

            final StringArgument otherStringArgument = (StringArgument) other;

            return type == otherStringArgument.type
                    && value.equals(otherStringArgument.value);
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

        void writeTo(ByteArrayOutputStream outputStream) {
            outputStream.write(type);
            writeValue(outputStream, this.value);
        }

        protected void writeValue(ByteArrayOutputStream outputStream, final int value) {
            outputStream.write(4);
            outputStream.write(value);
            outputStream.write(value >> 8);
            outputStream.write(value >> 16);
            outputStream.write(value >> 24);
        }

        int getType() {
            return type;
        }

        int getValueAsInt() {
            return value;
        }

        public int hashCode() {
            return type;
        }

        public boolean equals(Object other) {
            if (other == null || !(other instanceof NumericArgument))
                return false;

            final NumericArgument otherNumericArgument = (NumericArgument) other;

            return type == otherNumericArgument.type
                    && value == otherNumericArgument.value;
        }

        private final int type;

        private final int value;
    }

    protected static class BigIntArgument extends Argument {

        private final int type;
        private final long value;

        BigIntArgument(int type, long value) {
            this.type = type;
            this.value = value;
        }

        void writeTo(ByteArrayOutputStream outputStream) {
            outputStream.write(type);
            writeValue(outputStream, this.value);
        }

        protected void writeValue(ByteArrayOutputStream outputStream, final long value) {
            outputStream.write(8);
            outputStream.write((int) value);
            outputStream.write((int) (value >> 8));
            outputStream.write((int) (value >> 16));
            outputStream.write((int) (value >> 24));
            outputStream.write((int) (value >> 32));
            outputStream.write((int) (value >> 40));
            outputStream.write((int) (value >> 48));
            outputStream.write((int) (value >> 56));
        }

        int getType() {
            return type;
        }

        int getValueAsInt() {
            return (int) value;
        }

        long getValueAsLong() {
            return value;
        }

        public int hashCode() {
            return type;
        }

        public boolean equals(Object other) {
            if (other == null || !(other instanceof BigIntArgument))
                return false;

            final BigIntArgument otherBigIntArgument = (BigIntArgument) other;

            return type == otherBigIntArgument.type
                    && value == otherBigIntArgument.value;
        }
    }

    // ---------------------------------------------------------------------------
    // ByteArrayArgument
    // ---------------------------------------------------------------------------
    private static final class ByteArrayArgument extends Argument {

        ByteArrayArgument(int type, byte[] value) {
            this.type = type;
            this.value = value;
        }

        void writeTo(ByteArrayOutputStream outputStream) {
            outputStream.write(type);
            final int valueLength = value.length;
            writeLength(valueLength, outputStream);
            for (int i = 0; i < valueLength; i++)
                outputStream.write(value[i]);
        }

        protected void writeLength(int length,
                ByteArrayOutputStream outputStream) {
            outputStream.write(length);
        }

        int getType() {
            return type;
        }

        int getValueAsInt() {
            if (value.length == 1)
                return value[0];
            else
                throw new UnsupportedOperationException("This method is not "
                        + "supported for byte arrays with length > 1");
        }

        public int hashCode() {
            return type;
        }

        public boolean equals(Object other) {
            if (other == null || !(other instanceof ByteArrayArgument))
                return false;

            final ByteArrayArgument otherByteArrayArgument = (ByteArrayArgument) other;

            return type == otherByteArrayArgument.type
                    && Arrays.equals(value, otherByteArrayArgument.value);
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

        void writeTo(ByteArrayOutputStream outputStream) {
            outputStream.write(item);
        }

        int getType() {
            return item;
        }

        public int hashCode() {
            return item;
        }

        public boolean equals(Object other) {
            if (other == null || !(other instanceof SingleItem))
                return false;

            final SingleItem otherSingleItem = (SingleItem) other;

            return item == otherSingleItem.item;
        }

        private final int item;
    }

}
