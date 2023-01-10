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
        switch (isolationLevelName) {
        case TRANSACTION_NONE:
        case "" + Connection.TRANSACTION_NONE:
            return Connection.TRANSACTION_NONE;
        case TRANSACTION_READ_UNCOMMITTED:
        case "" + Connection.TRANSACTION_READ_UNCOMMITTED:
            return Connection.TRANSACTION_READ_UNCOMMITTED;
        case TRANSACTION_READ_COMMITTED:
        case "" + Connection.TRANSACTION_READ_COMMITTED:
            return Connection.TRANSACTION_READ_COMMITTED;
        case TRANSACTION_REPEATABLE_READ:
        case "" + Connection.TRANSACTION_REPEATABLE_READ:
            return Connection.TRANSACTION_REPEATABLE_READ;
        case TRANSACTION_SERIALIZABLE:
        case "" + Connection.TRANSACTION_SERIALIZABLE:
            return Connection.TRANSACTION_SERIALIZABLE;
        default:
            throw new IllegalArgumentException("Unknown transaction isolation level name: " + isolationLevelName);
        }
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
