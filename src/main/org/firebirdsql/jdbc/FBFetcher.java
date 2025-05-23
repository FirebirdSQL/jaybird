// SPDX-FileCopyrightText: Copyright 2002-2003 Blas Rodriguez Somoza
// SPDX-FileCopyrightText: Copyright 2003-2005 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2005 Gabriel Reid
// SPDX-FileCopyrightText: Copyright 2011-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.util.InternalApi;
import org.jspecify.annotations.NullMarked;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Instances of this interface fetch records from the server.
 * <p>
 * This interface is internal API of Jaybird. Future versions may radically change, move, or make inaccessible this
 * type.
 * </p>
 */
@InternalApi
@NullMarked
public sealed interface FBFetcher permits AbstractFetcher, FBCachedFetcher, FBServerScrollFetcher, FBStatementFetcher,
        FBUpdatableFetcher, ForwardOnlyFetcherDecorator {

    int DEFAULT_FETCH_ROWS = 400;

    /**
     * Move cursor to the first row.
     *
     * @return {@code true} if cursor was moved to the first row.
     */
    boolean first() throws SQLException;

    /**
     * Move cursor to the last row.
     *
     * @return {@code true} if cursor was moved to the last row.
     */
    boolean last() throws SQLException;

    /**
     * Move cursor to the previous row.
     *
     * @return {@code true} if cursor was moved to the previous row.
     */
    boolean previous() throws SQLException;

    /**
     * Move to next row.
     *
     * @return {@code true} if cursor was moved.
     */
    boolean next() throws SQLException;

    /**
     * Move cursor to the absolute row.
     *
     * @param row
     *         absolute row number.
     * @return {@code true} if cursor was successfully moved.
     */
    boolean absolute(int row) throws SQLException;

    /**
     * Move cursor relative to the current row.
     *
     * @param row
     *         relative row position.
     * @return {@code true} if cursor was successfully moved.
     */
    boolean relative(int row) throws SQLException;

    /**
     * Move cursor before first record.
     */
    void beforeFirst() throws SQLException;

    /**
     * Move cursor after last record.
     */
    void afterLast() throws SQLException;

    /**
     * Close this fetcher and corresponding result set.
     * <p>
     * Equivalent to calling {@link #close(CompletionReason)} with {@link CompletionReason#OTHER}.
     * </p>
     */
    void close() throws SQLException;

    /**
     * Close this fetcher and corresponding result set.
     *
     * @param completionReason
     *         Reason for completion
     */
    void close(CompletionReason completionReason) throws SQLException;

    /**
     * @return {@code true} if this fetcher is closed, otherwise {@code false}
     * @since 6
     */
    boolean isClosed();

    /**
     * Get row number.
     *
     * @return row number
     * @see #currentPosition()
     */
    int getRowNum() throws SQLException;

    /**
     * @return {@code true} if the result set is empty; otherwise {@code false}
     */
    boolean isEmpty() throws SQLException;

    /**
     * @return {@code true} if positioned before the first row, contrary to {@link ResultSet#isBeforeFirst()} it also
     * reports {@code true} if empty and next was not invoked
     * @see ResultSet#isBeforeFirst()
     */
    boolean isBeforeFirst() throws SQLException;

    /**
     * @see ResultSet#isFirst()
     */
    boolean isFirst() throws SQLException;

    /**
     * @see ResultSet#isLast()
     */
    boolean isLast() throws SQLException;

    /**
     * @return {@code true} if positioned after the last row, contrary to {@link ResultSet#isBeforeFirst()} it also
     * reports {@code true} if empty and next was invoked
     * @see ResultSet#isAfterLast()
     */
    boolean isAfterLast() throws SQLException;

    /**
     * Signals to the fetcher that an insert is about to be executed.
     * <p>
     * This method is primarily intended for a workaround with {@link FBServerScrollFetcher} if the insert is performed
     * when the server-side cursor is not fully materialized by the server, as that could result in the server also
     * including the inserted row, leading to duplicate reporting of the inserted row. In response to this method,
     * the fetcher can trigger full materialization of the server-side cursor.
     * </p>
     * <p>
     * The default implementation of this method does nothing.
     * </p>
     *
     * @throws SQLException
     *         for database access exceptions
     * @since 5.0.6
     */
    default void beforeExecuteInsert() throws SQLException {
        // default do nothing
    }

    /**
     * Insert row at current position. This method adds a row at the current
     * position in case of updatable result sets after successful execution of
     * the {@link java.sql.ResultSet#insertRow()} method.
     *
     * @param data
     *         row data
     */
    void insertRow(RowValue data) throws SQLException;

    /**
     * Delete row at current position. This method deletes a row at the current
     * position in case of updatable result sets after successful execution of
     * the {@link java.sql.ResultSet#deleteRow()} method.
     */
    void deleteRow() throws SQLException;

    /**
     * Update row at current position. This method updates a row at the current
     * position in case of updatable result sets after successful execution of
     * the {@link java.sql.ResultSet#updateRow()} method.
     *
     * @param data
     *         row data
     */
    void updateRow(RowValue data) throws SQLException;

    /**
     * Notifies the fetcher listener with the row data of the current row (or {{@code null} if not currently in a row).
     */
    void renotifyCurrentRow() throws SQLException;

    /**
     * @return current fetch config of this fetcher
     * @since 6
     */
    FetchConfig getFetchConfig();

    /**
     * Updates the result set behavior of this fetcher to readonly.
     *
     * @throws SQLException
     *         if called on a closed fetcher, or if this fetcher is explicitly used for updatable use cases
     * @since 6
     */
    void setReadOnly() throws SQLException;

    /**
     * Get the suggested number of rows to fetch with each batch fetch.
     *
     * @return The number of rows to be fetched, or {@code 0} for the default fetch size
     */
    int getFetchSize() throws SQLException;

    /**
     * Set the suggested number of rows to fetch with each batch fetch.
     *
     * @param fetchSize
     *         The suggested number of rows to fetch, or {@code 0} to use the default fetch size
     * @throws SQLException
     *         if {@code fetchSize < 0}
     */
    void setFetchSize(int fetchSize) throws SQLException;

    /**
     * Retrieves the fetch direction for this fetcher object.
     *
     * @return current fetch direction
     * @since 6
     */
    int getFetchDirection() throws SQLException;

    /**
     * Gives a hint as to the direction in which the rows in this fetcher object will be processed.
     * <p>
     * NOTE: In practice, existing fetcher implementations only validate and store the value, and the value is
     * effectively ignored.
     * </p>
     *
     * @param direction
     *         fetch direction; one of {@link ResultSet#FETCH_FORWARD}, {@link ResultSet#FETCH_REVERSE}, or
     *         {@link ResultSet#FETCH_UNKNOWN}
     * @since 6
     */
    void setFetchDirection(int direction) throws SQLException;

    /**
     * The current position of the fetcher.
     * <p>
     * Contrary to {@link #getRowNum()}, this should also report the after-last position.
     * </p>
     *
     * @return Position of the fetcher, with {@code 0} for before-first, and {@code size + 1} for after-last.
     * @see #getRowNum()
     */
    int currentPosition() throws SQLException;

    /**
     * Total number of rows of this fetcher, taking the max rows into account if relevant.
     * <p>
     * If the size of the fetcher is not known or not fixed, a {@link SQLException} should be thrown.
     * </p>
     *
     * @return size of fetcher
     * @throws SQLException
     *         For exception retrieving the cursor size, or if it is not possible to determine the fetcher size
     */
    int size() throws SQLException;

    /**
     * Sets the fetcher listener.
     * <p>
     * Any current fetcher is replaced with the provided fetcher listener.
     * </p>
     *
     * @param fetcherListener
     *         Fetcher listener
     */
    void setFetcherListener(FBObjectListener.FetcherListener fetcherListener);

    /**
     * Is the current row a newly inserted row (through the owning result set)?
     *
     * @return {@code true} if the row is newly inserted, {@code false} otherwise
     */
    default boolean rowInserted() throws SQLException {
        return false;
    }

    /**
     * Is the current row a deleted row (through the owning result set)?
     *
     * @return {@code true} if the row is deleted, {@code false} otherwise
     */
    default boolean rowDeleted() throws SQLException {
        return false;
    }

    /**
     * Is the current row an updated row (through the owning result set)?
     *
     * @return {@code true} if the row is updated, {@code false} otherwise
     */
    default boolean rowUpdated() throws SQLException {
        return false;
    }

}
