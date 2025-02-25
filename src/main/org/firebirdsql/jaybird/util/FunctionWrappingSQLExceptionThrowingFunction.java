// SPDX-FileCopyrightText: Copyright 2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.util;

import org.jspecify.annotations.NullUnmarked;

import java.sql.SQLException;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * An implementation of {@link Function} to wrap a {@link SQLExceptionThrowingFunction}.
 *
 * @param <T>
 *         type of the input to the function
 * @param <R>
 *         type of the result of the function
 * @author Mark Rotteveel
 * @since 6
 */
@NullUnmarked
final class FunctionWrappingSQLExceptionThrowingFunction<T, R> implements Function<T, R> {

    private final SQLExceptionThrowingFunction<T, R> wrapped;

    FunctionWrappingSQLExceptionThrowingFunction(SQLExceptionThrowingFunction<T, R> wrapped) {
        this.wrapped = requireNonNull(wrapped, "wrapped");
    }

    @Override
    public R apply(T t) {
        try {
            return wrapped.apply(t);
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    SQLExceptionThrowingFunction<T, R> unwrap() {
        return wrapped;
    }

}
