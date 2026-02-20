/*
 SPDX-FileCopyrightText: Copyright 2003 Ryan Baldwin
 SPDX-FileCopyrightText: Copyright 2003-2008 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2004 Gabriel Reid
 SPDX-FileCopyrightText: Copyright 2015-2026 Mark Rotteveel
 SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
*/
package org.firebirdsql.gds;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.gds.impl.wire.Xdrable;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Iterator;

/**
 * Instance of this interface represents a Parameter Buffer it is extended by various parameter buffer interfaces.
 *
 * @author Mark Rotteveel
 * @see org.firebirdsql.gds.ParameterBuffer
 * @see org.firebirdsql.gds.ServiceRequestBuffer
 * @see org.firebirdsql.gds.TransactionParameterBuffer
 * @see org.firebirdsql.gds.ServiceParameterBuffer
 * @since 3.0
 */
public interface ParameterBuffer extends Iterable<Parameter>, Serializable {

    /**
     * @return The parameter buffer type identifier
     */
    int getType();

    /**
     * Add argument with no parameters.
     *
     * @param argumentType
     *         type of argument.
     */
    void addArgument(int argumentType);

    /**
     * Add string argument with the default encoding.
     *
     * @param argumentType
     *         type of argument.
     * @param value
     *         string value to add.
     */
    void addArgument(int argumentType, String value);

    /**
     * Add string argument.
     *
     * @param argumentType
     *         type of argument.
     * @param value
     *         string value to add.
     * @param encoding
     *         encoding to use for conversion to bytes
     */
    void addArgument(int argumentType, String value, Encoding encoding);

    /**
     * Add a byte argument.
     *
     * @param argumentType
     *         type of argument.
     * @param value
     *         byte value to add.
     */
    void addArgument(int argumentType, byte value);

    /**
     * Add integer argument.
     *
     * @param argumentType
     *         type of argument.
     * @param value
     *         integer value to add.
     */
    void addArgument(int argumentType, int value);

    /**
     * Add long argument.
     *
     * @param argumentType
     *         type of argument.
     * @param value
     *         long value to add.
     */
    void addArgument(int argumentType, long value);

    /**
     * Add array of bytes.
     *
     * @param argumentType
     *         type of argument.
     * @param content
     *         content of argument.
     */
    void addArgument(int argumentType, byte[] content);

    /**
     * Remove the first occurrence of the specified argument.
     *
     * @param argumentType
     *         type of argument to remove.
     */
    void removeArgument(int argumentType);

    /**
     * Get argument as string.
     *
     * @param argumentType
     *         type of argument to find.
     * @return argument as string or {@code null} if nothing found.
     */
    @SuppressWarnings("unused")
    @Nullable String getArgumentAsString(int argumentType);

    /**
     * Get argument as int.
     *
     * @param argumentType
     *         type of argument to find.
     * @return argument as string or <code>0</code> if nothing found.
     */
    int getArgumentAsInt(int argumentType);

    /**
     * Check if this parameter buffer has specified argument.
     *
     * @param argumentType
     *         type of argument to find.
     * @return <code>true</code> if this buffer contains specified argument.
     */
    boolean hasArgument(int argumentType);

    /**
     * Returns an iterator over a copy of the parameters in this parameter buffer.
     * <p>
     * It is safe to iterate over this iterator while modifying the parameter buffer. Changes will not be reflected in
     * the iterator.
     * </p>
     *
     * @return Iterator over the parameters in this parameter buffer.
     */
    @Override
    Iterator<Parameter> iterator();

    /**
     * Writes the arguments in the implementation specific serialization into the {@code OutputStream}.
     *
     * @param outputStream
     *         The {@code OutputStream} to write to
     * @throws IOException
     *         Errors produced by the output stream during writes
     */
    void writeArgumentsTo(OutputStream outputStream) throws IOException;

    /**
     * @return {@code Xdrable} to write (and optionally read) this instance as Xdr.
     */
    Xdrable toXdrable();

    /**
     * Converts this parameter buffer to a byte array.
     * <p>
     * This byte array includes the extra header-bytes (if any), but does not include the type information. If
     * the parameter buffer is empty, implementations may return either an empty byte array or an array with only
     * the extra header bytes.
     * </p>
     *
     * @return Byte array with serialization of this parameter buffer
     * @see #toBytesWithType()
     */
    byte[] toBytes();

    /**
     * Converts this parameter buffer to a byte array with type information.
     * <p>
     * This byte array includes the type information and the extra header bytes (if any). If the parameter buffer is
     * empty, implementations may return either an empty byte array or an array with only the type and extra header
     * bytes.
     * </p>
     *
     * @return Byte array with serialization of this parameter buffer
     * @see #toBytes()
     */
    byte[] toBytesWithType();

    /**
     * @return the number of parameters stored.
     */
    int size();

    /**
     * @return {@code true} if empty, {@code false} if this buffer contains at least one parameter
     * @since 7
     */
    default boolean isEmpty() {
        return size() == 0;
    }

}
