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
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.util.InternalApi;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLNonTransientException;

import static java.util.Objects.requireNonNull;
import static org.firebirdsql.jaybird.util.ConditionalHelpers.firstNonZero;
import static org.firebirdsql.jdbc.SQLStateConstants.SQL_STATE_INVALID_ATTR_VALUE;

/**
 * Fetch configuration values.
 * <p>
 * This class is internal API of Jaybird. Future versions may radically change, move, or make inaccessible this type.
 * </p>
 * <p>
 * The {@code withXXX} methods may return the same object if the new value is equal to the current value.
 * </p>
 *
 * @param fetchSize
 *         the fetch size (value {@code 0} or {@link #USE_DEFAULT_FETCH_SIZE} means use default)
 * @param maxRows
 *         the maximum number of rows (value {@code 0} or {@link #NO_MAX_ROWS} means no maximum)
 * @param direction
 *         the fetch direction (one of {@link ResultSet#FETCH_FORWARD}, {@link ResultSet#FETCH_REVERSE}, or
 *         {@link ResultSet#FETCH_UNKNOWN})
 * @param resultSetBehavior
 *         result set behavior
 * @author Mark Rotteveel
 * @since 6
 */
@InternalApi
public record FetchConfig(int fetchSize, int maxRows, int direction, ResultSetBehavior resultSetBehavior) {

    /**
     * Value of {@link #fetchSize()} which means &quot;use default value&quot;.
     * <p>
     * This class does not know the actual default fetch size; that is considered an implementation detail of
     * {@link FBFetcher} or its implementations.
     * </p>
     */
    public static final int USE_DEFAULT_FETCH_SIZE = 0;
    /**
     * Value of {@link #maxRows()} which means &quot;no maximum/all rows&quot;.
     */
    public static final int NO_MAX_ROWS = 0;

    public FetchConfig {
        if (fetchSize < 0) {
            throw new IllegalArgumentException("fetchSize must be >= 0, was: " + fetchSize);
        }
        if (maxRows < 0) {
            throw new IllegalArgumentException("maxRows must be >= 0, was: " + maxRows);
        }
        if (!(direction == ResultSet.FETCH_FORWARD
              || direction == ResultSet.FETCH_REVERSE
              || direction == ResultSet.FETCH_UNKNOWN)) {
            throw new IllegalArgumentException("Unsupported value for fetch direction, was: " + direction);
        }
        requireNonNull(resultSetBehavior, "resultSetBehavior");
    }

    /**
     * Fetch configuration with specified {@code resultSetBehavior}, default ({@code 0}) for {@code fetchSize} and
     * {@code maxRows}, and {@code direction} {@link ResultSet#FETCH_FORWARD}.
     *
     * @param resultSetBehavior
     *         result set behavior
     */
    public FetchConfig(ResultSetBehavior resultSetBehavior) {
        this(USE_DEFAULT_FETCH_SIZE, NO_MAX_ROWS, ResultSet.FETCH_FORWARD, resultSetBehavior);
    }

    /**
     * Returns {@code fetchSize}, or {@code defaultFetchSize} if {@code fetchSize == USE_DEFAULT_FETCH_SIZE}.
     *
     * @param defaultFetchSize
     *         default fetch size
     * @return fetch size or the default fetch size
     */
    public int fetchSizeOr(int defaultFetchSize) {
        return firstNonZero(fetchSize, defaultFetchSize);
    }

    /**
     * Returns a {@code FetchConfig} with the specified {@code fetchSize} and the current values of this object for
     * {@code maxRows}, {@code direction}, and {@code resultSetBehavior}.
     *
     * @param fetchSize
     *         fetch size, must be {@code >= 0} ({@code 0} or {@link #USE_DEFAULT_FETCH_SIZE} means use default fetch
     *         size)
     * @return fetch config object
     * @throws SQLException
     *         if {@code fetchSize} is negative
     */
    public FetchConfig withFetchSize(int fetchSize) throws SQLException {
        if (this.fetchSize == fetchSize) return this;
        try {
            return new FetchConfig(fetchSize, maxRows, direction, resultSetBehavior);
        } catch (IllegalArgumentException e) {
            throw new SQLNonTransientException(e.getMessage(), SQL_STATE_INVALID_ATTR_VALUE);
        }
    }

    /**
     * Returns a {@code FetchConfig} with the specified {@code maxRows} and the current values of this object for
     * {@code fetchSize}, {@code direction}, and {@code resultSetBehavior}.
     *
     * @param maxRows
     *         max rows, must be {@code >= 0} ({@code 0} or {@link #NO_MAX_ROWS} means no maximum)
     * @return fetch config object
     * @throws SQLException
     *         if {@code maxRows} is negative
     */
    public FetchConfig withMaxRows(int maxRows) throws SQLException {
        if (this.maxRows == maxRows) return this;
        try {
            return new FetchConfig(fetchSize, maxRows, direction, resultSetBehavior);
        } catch (IllegalArgumentException e) {
            throw new SQLNonTransientException(e.getMessage(), SQL_STATE_INVALID_ATTR_VALUE);
        }
    }

    /**
     * Returns a {@code FetchConfig} with the specified {@code direction} and the current values of this object for
     * {@code fetchSize}, {@code maxRows}, and {@code resultSetBehavior}.
     *
     * @param direction
     *         fetch direction (one of {@link ResultSet#FETCH_FORWARD}, {@link ResultSet#FETCH_REVERSE}, or
     *         {@link ResultSet#FETCH_UNKNOWN})
     * @return fetch config object
     * @throws SQLException
     *         if {@code direction} is not one of {@link ResultSet#FETCH_FORWARD}, {@link ResultSet#FETCH_REVERSE}, or
     *         {@link ResultSet#FETCH_UNKNOWN}
     */
    public FetchConfig withDirection(int direction) throws SQLException {
        if (this.direction == direction) return this;
        try {
            return new FetchConfig(fetchSize, maxRows, direction, resultSetBehavior);
        } catch (IllegalArgumentException e) {
            throw FbExceptionBuilder.forNonTransientException(JaybirdErrorCodes.jb_invalidFetchDirection)
                    .messageParameter(direction)
                    .toSQLException();
        }
    }

    /**
     * Returns a {@code FetchConfig} with the {@code resultSetBehavior} set to read-only, and the current values of
     * this object for {@code fetchSize}, {@code maxRows}, and {@code direction}.
     *
     * @return fetch config object
     */
    public FetchConfig withReadOnly() {
        if (resultSetBehavior.isReadOnly()) return this;
        return new FetchConfig(fetchSize, maxRows, direction, resultSetBehavior.withReadOnly());
    }

}
