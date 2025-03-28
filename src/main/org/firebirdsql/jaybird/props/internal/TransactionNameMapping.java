// SPDX-FileCopyrightText: Copyright 2021-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.props.internal;

import org.firebirdsql.util.InternalApi;

import java.sql.Connection;

/**
 * Mapping of transaction names to JDBC transaction code and vice versa.
 *
 * @author Mark Rotteveel
 * @since 5
 */
@InternalApi
public final class TransactionNameMapping {

    public static final String TRANSACTION_NONE = "TRANSACTION_NONE";
    public static final String TRANSACTION_READ_UNCOMMITTED = "TRANSACTION_READ_UNCOMMITTED";
    public static final String TRANSACTION_READ_COMMITTED = "TRANSACTION_READ_COMMITTED";
    public static final String TRANSACTION_REPEATABLE_READ = "TRANSACTION_REPEATABLE_READ";
    public static final String TRANSACTION_SERIALIZABLE = "TRANSACTION_SERIALIZABLE";

    private TransactionNameMapping() {
        // no instances
    }

    /**
     * Maps a transaction isolation level name to the JDBC transaction isolation level.
     * <p>
     * Accepted string names are {@code "TRANSACTION_NONE"}, {@code "TRANSACTION_READ_UNCOMMITTED"},
     * {@code "TRANSACTION_READ_COMMITTED"}, {@code "TRANSACTION_REPEATABLE_READ"}, {@code "TRANSACTION_SERIALIZABLE"}.
     * To simplify mapping connection properties, the string value of the isolation level integer (e.g. {@code "2"} for
     * {@link Connection#TRANSACTION_READ_COMMITTED}) is also accepted.
     * </p>
     *
     * @param isolationLevelName
     *         Transaction isolation level name or integer string (not {@code null})
     * @return JDBC standard isolation level value
     * @throws IllegalArgumentException
     *         For an unknown transaction isolation level name
     */
    public static int toIsolationLevel(String isolationLevelName) {
        return switch (isolationLevelName) {
            case TRANSACTION_NONE, "" + Connection.TRANSACTION_NONE ->
                    Connection.TRANSACTION_NONE;
            case TRANSACTION_READ_UNCOMMITTED, "" + Connection.TRANSACTION_READ_UNCOMMITTED ->
                    Connection.TRANSACTION_READ_UNCOMMITTED;
            case TRANSACTION_READ_COMMITTED, "" + Connection.TRANSACTION_READ_COMMITTED ->
                    Connection.TRANSACTION_READ_COMMITTED;
            case TRANSACTION_REPEATABLE_READ, "" + Connection.TRANSACTION_REPEATABLE_READ ->
                    Connection.TRANSACTION_REPEATABLE_READ;
            case TRANSACTION_SERIALIZABLE, "" + Connection.TRANSACTION_SERIALIZABLE ->
                    Connection.TRANSACTION_SERIALIZABLE;
            default -> throw new IllegalArgumentException(
                    "Unknown transaction isolation level name: " + isolationLevelName);
        };
    }

    /**
     * Maps a JDBC transaction isolation level to a string name.
     *
     * @param isolationLevel
     *         JDBC isolation level
     * @return name of the isolation level (e.g. {@code "TRANSACTION_READ_COMMITTED"})
     * @throws IllegalArgumentException
     *         When {@code isolationLevel} is not a known JDBC transaction isolation level
     */
    public static String toIsolationLevelName(int isolationLevel) {
        return toIsolationLevelName(isolationLevel, false);
    }

    /**
     * Maps a JDBC transaction isolation level to a string name.
     *
     * @param isolationLevel
     *         JDBC isolation level
     * @param lenient
     *         {@code true} return integer string for unknown values, {@code false} throw
     *         {@code IllegalArgumentException} for unknown values
     * @return name of the isolation level (e.g. TRANSACTION_READ_COMMITTED), or, when {@code lenient} is {@code true},
     * the integer string for unknown values
     * @throws IllegalArgumentException
     *         When {@code isolationLevel} is not a known JDBC transaction isolation level and {@code lenient} is {@code
     *         false}
     */
    public static String toIsolationLevelName(int isolationLevel, boolean lenient) {
        switch (isolationLevel) {
        case Connection.TRANSACTION_NONE:
            return TRANSACTION_NONE;
        case Connection.TRANSACTION_READ_UNCOMMITTED:
            return TRANSACTION_READ_UNCOMMITTED;
        case Connection.TRANSACTION_READ_COMMITTED:
            return TRANSACTION_READ_COMMITTED;
        case Connection.TRANSACTION_REPEATABLE_READ:
            return TRANSACTION_REPEATABLE_READ;
        case Connection.TRANSACTION_SERIALIZABLE:
            return TRANSACTION_SERIALIZABLE;
        default:
            if (lenient) {
                return String.valueOf(isolationLevel);
            } else {
                throw new IllegalArgumentException("Unknown transaction isolation level: " + isolationLevel);
            }
        }
    }

}
