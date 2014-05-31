/*
 * $Id$
 * 
 * Firebird Open Source J2EE Connector - JDBC Driver
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
package org.firebirdsql.gds.impl.argument;

import java.io.IOException;
import java.io.OutputStream;

/**
 * {@link Argument} implementation for String values
 */
public class StringArgument extends Argument {

    public StringArgument(int type, String value) {
        super(type);
        if (value == null) {
            throw new IllegalArgumentException("String value should not be null");
        }
        this.value = value;
        // TODO Use correct Encoding
        asBytes = value.getBytes();
        if (asBytes.length > getMaxSupportedLength()) {
            throw new IllegalArgumentException(String.format("byte array derived from String value should not be longer than %d bytes, length was %d", getMaxSupportedLength(), asBytes.length));
        }
    }

    /**
     * @return Maximum supported length of the argument as byte array.
     */
    protected int getMaxSupportedLength() {
        return 255;
    }

    public void writeTo(OutputStream outputStream) throws IOException {
        outputStream.write(getType());
        writeLength(asBytes.length, outputStream);
        outputStream.write(asBytes);
    }

    public int getLength() {
        return asBytes.length + 2;
    }

    @Override
    public String getValueAsString() {
        return value;
    }

    @Override
    public int getValueAsInt() {
        return Integer.parseInt(value);
    }

    protected void writeLength(int length, OutputStream outputStream) throws IOException {
        outputStream.write(length);
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

        return this.getType() == otherStringArgument.getType() && value.equals(otherStringArgument.value);
    }

    private final String value;
    private final byte[] asBytes;
}
