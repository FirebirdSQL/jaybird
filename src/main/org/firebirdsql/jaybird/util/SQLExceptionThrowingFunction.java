// SPDX-FileCopyrightText: Copyright 2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.util;

import org.jspecify.annotations.NullUnmarked;

import java.sql.SQLException;
import java.util.function.Function;

/**
 * Function that can throw a {@link SQLException}.
 *
 * @param <T>
 *         type of the input to the function
 * @param <R>
 *         type of the result of the function
 * @author Mark Rotteveel
 * @since 6
 */
@FunctionalInterface
@NullUnmarked
public interface SQLExceptionThrowingFunction<T, R> {

    R apply(T t) throws SQLException;

    /**
     * Converts this instance to a normal {@link Function}, throwing {@link UncheckedSQLException} instead of
     * {@link SQLException}.
     *
     * @return normal {@link Function} instance throwing {@link UncheckedSQLException}
     * @see #toSQLExceptionThrowingFunction(Function)
     */
    default Function<T, R> toFunction() {
        return new FunctionWrappingSQLExceptionThrowingFunction<>(this);
    }

    /**
     * Convenience method to convert a {@link SQLExceptionThrowingFunction} to a {@link Function}.
     * <p>
     * Equivalent to {@code f.toFunction()}, but can be used for call-site type inference.
     * </p>
     *
     * @param f
     *         function to convert
     * @param <T>
     *         type of the input to the function
     * @param <R>
     *         type of the result of the function
     * @return normal {@link Function} instance throwing {@link UncheckedSQLException}
     * @see #toFunction()
     */
    static <T, R> Function<T, R> toFunction(SQLExceptionThrowingFunction<T, R> f) {
        return f.toFunction();
    }

    /**
     * Converts a normal {@link Function} to a {@link SQLExceptionThrowingFunction}, unwrapping
     * any {@link UncheckedSQLException} thrown to {@link SQLException}.
     *
     * @param f
     *         function to convert
     * @param <T>
     *         type of the input to the function
     * @param <R>
     *         type of the result of the function
     * @return {@link SQLExceptionThrowingFunction} instance
     * @see #toFunction()
     */
    static <T, R> SQLExceptionThrowingFunction<T, R> toSQLExceptionThrowingFunction(Function<T, R> f) {
        if (f instanceof FunctionWrappingSQLExceptionThrowingFunction<T,R> wrapper) {
            return wrapper.unwrap();
        }
        return new SQLExceptionThrowingFunctionWrappingFunction<>(f);
    }
    
}
