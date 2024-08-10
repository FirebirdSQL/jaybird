/*
 * Firebird Open Source JDBC Driver
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
package org.firebirdsql.jaybird.util;

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
