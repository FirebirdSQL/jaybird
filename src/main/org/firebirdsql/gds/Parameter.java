// SPDX-FileCopyrightText: Copyright 2014-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds;

import org.firebirdsql.encodings.Encoding;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Interface for parameters stored in a {@link org.firebirdsql.gds.ParameterBuffer}.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
@NullMarked
public interface Parameter {

    /**
     * The type identifier of the parameter (usually one of the constant values in
     * {@link org.firebirdsql.gds.ISCConstants}).
     *
     * @return The type of the parameter
     */
    int getType();

    /**
     * The value of the parameter as String.
     * <p>
     * The implementation may throw a RuntimeException if the parameter isn't a string (or shouldn't be used as a
     * string).
     * </p>
     *
     * @return The value as string
     */
    String getValueAsString();

    /**
     * The value of the parameter as int.
     * <p>
     * The implementation may throw a RuntimeException if the parameter isn't an int (or shouldn't be used as an
     * int).
     * </p>
     *
     * @return The value as int
     */
    int getValueAsInt();


    /**
     * The value of the parameter as long.
     * <p>
     * The implementation may throw a RuntimeException if the parameter isn't a long (or shouldn't be used as a
     * long).
     * </p>
     *
     * @return The value as long
     */
    long getValueAsLong();

    /**
     * Copies this argument into the supplied buffer, uses the supplied {@code Encoding} for string arguments.
     * <p>
     * An instance of <code>Parameter</code> should know how to copy itself into another buffer (eg an instance
     * of {@link org.firebirdsql.gds.impl.argument.StringArgument} would know to call
     * {@link org.firebirdsql.gds.ParameterBuffer#addArgument(int, String, Encoding)}).
     * </p>
     * <p>
     * The parameter does not need to check if it is the right type of destination buffer (if someone tries to
     * add a TPB argument to a DPB they are free to try that).
     * </p>
     *
     * @param buffer
     *         ParameterBuffer instance
     * @param stringEncoding
     *         Encoding to use for string properties. A value of {@code null} can be used to signal that the
     *         original encoding should be used.
     */
    void copyTo(ParameterBuffer buffer, @Nullable Encoding stringEncoding);
}
