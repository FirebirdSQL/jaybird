// SPDX-FileCopyrightText: Copyright 2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.util;

import org.jspecify.annotations.NullUnmarked;

import java.sql.SQLException;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * An implementation of {@link SQLExceptionThrowingFunction} to wrap a {@link Function}.
 *
 * @param <T>
 *         type of the input to the function
 * @param <R>
 *         type of the result of the function
 * @author Mark Rotteveel
 * @since 6
 */
@NullUnmarked
final class SQLExceptionThrowingFunctionWrappingFunction<T, R> implements SQLExceptionThrowingFunction<T, R> {

    private final Function<T, R> wrapped;

    SQLExceptionThrowingFunctionWrappingFunction(Function<T, R> wrapped) {
        this.wrapped = requireNonNull(wrapped, "wrapped");
    }

    @Override
    public R apply(T t) throws SQLException {
        try {
            return wrapped.apply(t);
        } catch (UncheckedSQLException e) {
            throw e.getCause();
        }
    }

    @Override
    public Function<T, R> toFunction() {
        return wrapped;
    }

}
