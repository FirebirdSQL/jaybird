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
package org.firebirdsql.common.assertions;

import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Various assertions for result set.
 *
 * @author Mark Rotteveel
 * @since 6
 */
public final class ResultSetAssertions {

    private ResultSetAssertions() {
        // no instances
    }

    /**
     * Asserts that {@code rs} has a next row by checking if {@link ResultSet#next()} returned {@code true}.
     * <p>
     * Equivalent to using {@link #assertNextRow(ResultSet, String)} with message {@code "Expected a row"}.
     * </p>
     *
     * @param rs
     *         result set
     */
    public static void assertNextRow(ResultSet rs) {
        assertNextRow(rs, "Expected a row");
    }

    /**
     * Asserts that {@code rs} has a next row by checking if {@link ResultSet#next()} returned {@code true}.
     *
     * @param rs
     *         result set
     * @param message
     *         message to use for the assertion if {@link ResultSet#next()} returned {@code false}
     */
    public static void assertNextRow(ResultSet rs, String message) {
        assertTrue(assertDoesNotThrow(rs::next, "No exception expected for ResultSet.next()"), message);
    }

    /**
     * Asserts that {@code rs} has no next row by checking if {@link ResultSet#next()} returned {@code false}.
     * <p>
     * Equivalent to using {@link #assertNoNextRow(ResultSet, String)} with message {@code "Expected no more rows"}.
     * </p>
     *
     * @param rs
     *         result set
     */
    public static void assertNoNextRow(ResultSet rs) {
        assertNoNextRow(rs, "Expected no more rows");
    }

    /**
     * Asserts that {@code rs} has no next row by checking if {@link ResultSet#next()} returned {@code false}.
     *
     * @param rs
     *         result set
     * @param message
     *         message to use for the assertion if {@link ResultSet#next()} returned {@code true}
     */
    public static void assertNoNextRow(ResultSet rs, String message) {
        assertFalse(assertDoesNotThrow(rs::next, "No exception expected for ResultSet.next()"), message);
    }

}
