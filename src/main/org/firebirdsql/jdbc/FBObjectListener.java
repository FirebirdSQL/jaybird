// SPDX-FileCopyrightText: Copyright 2003-2005 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2011-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.util.InternalApi;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Defines a set of listeners that will be called in different situations.
 *
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 */
@InternalApi
public final class FBObjectListener {

    private FBObjectListener() {
        // no instances
    }

    @NullMarked
    public interface FetcherListener {

        /**
         * Notify listener that underlying row was changed.
         * <p>
         * The default implementation does nothing.
         * </p>
         *
         * @param fetcher
         *         instance of {@link FBFetcher} that caused this event
         * @param newRow
         *         new row
         */
        default void rowChanged(FBFetcher fetcher, @Nullable RowValue newRow) throws SQLException {
            // do nothing
        }

    }

    /**
     * Implementation of {@link org.firebirdsql.jdbc.FBObjectListener.FetcherListener} that does nothing.
     */
    public static final class NoActionFetcherListener implements FetcherListener {

        private static final FetcherListener INSTANCE = new NoActionFetcherListener();

        public static FetcherListener instance() {
            return INSTANCE;
        }

        private NoActionFetcherListener() {
        }

    }

    /**
     * Listener for the events generated by the result set.
     */
    @NullMarked
    public interface ResultSetListener {

        /**
         * Notify listener that result set was closed.
         * <p>
         * The default implementation does nothing.
         * </p>
         *
         * @param rs
         *         result set that was closed
         */
        default void resultSetClosed(ResultSet rs) throws SQLException {
            // do nothing
        }

        /**
         * Notify listener that execution of some row updating operation started.
         * <p>
         * The default implementation does nothing.
         * </p>
         *
         * @param updater
         *         instance of {@link FirebirdRowUpdater}
         */
        default void executionStarted(FirebirdRowUpdater updater) throws SQLException {
            // do nothing
        }

        /**
         * Notify listener that execution of some row updating operation is completed.
         * <p>
         * The default implementation does nothing.
         * </p>
         *
         * @param updater
         *         instance of {@link FirebirdRowUpdater}
         */
        default void executionCompleted(FirebirdRowUpdater updater, boolean success) throws SQLException {
            // do nothing
        }

    }

    /**
     * Implementation of {@link org.firebirdsql.jdbc.FBObjectListener.ResultSetListener} that does nothing.
     */
    public static final class NoActionResultSetListener implements ResultSetListener {

        private static final ResultSetListener INSTANCE = new NoActionResultSetListener();

        public static ResultSetListener instance() {
            return INSTANCE;
        }

        private NoActionResultSetListener() {
        }

    }

    /**
     * Listener for the events generated by statements.
     */
    public interface StatementListener {

        /**
         * Get the connection object to which this listener belongs to.
         *
         * @return instance of {@link FBConnection}
         * @throws SQLException
         *         if something went wrong
         */
        FBConnection getConnection() throws SQLException;

        /**
         * Notify listener that statement execution is being started.
         *
         * @param stmt
         *         statement that is being executed
         */
        void executionStarted(AbstractStatement stmt) throws SQLException;

        /**
         * Notify the listener that statement was closed.
         *
         * @param stmt
         *         statement that was closed
         */
        void statementClosed(AbstractStatement stmt) throws SQLException;

        /**
         * Notify the listener that statement is completed. This is shortcut method for
         * {@code statementCompleted(AbstractStatement, true)}.
         *
         * @param stmt
         *         statement that was completed.
         */
        void statementCompleted(AbstractStatement stmt) throws SQLException;

        /**
         * Notify the listener that statement is completed and tell whether execution was successful or not.
         *
         * @param stmt
         *         statement that was completed
         * @param success
         *         {@code true} if completion was successful
         */
        void statementCompleted(AbstractStatement stmt, boolean success) throws SQLException;

    }

    /**
     * Listener for the events generated by BLOBs.
     */
    public interface BlobListener {

        /**
         * Notify listener that execution of some BLOB operation had been started.
         * <p>
         * The default implementation does nothing.
         * </p>
         *
         * @param blob
         *         instance of {@link FirebirdBlob} that caused this event
         * @throws SQLException
         *         if something went wrong
         */
        default void executionStarted(FirebirdBlob blob) throws SQLException {
            // do nothing
        }

        /**
         * Notify listener that execution of some BLOB operation had been completed.
         * <p>
         * The default implementation does nothing.
         * </p>
         *
         * @param blob
         *         instance of {@link FirebirdBlob} that caused this event.
         * @throws SQLException
         *         if something went wrong.
         */
        default void executionCompleted(FirebirdBlob blob) throws SQLException {
            // do nothing
        }

    }

    /**
     * Implementation of {@link org.firebirdsql.jdbc.FBObjectListener.BlobListener} that does nothing.
     */
    public static final class NoActionBlobListener implements BlobListener {

        private static final BlobListener INSTANCE = new NoActionBlobListener();

        public static BlobListener instance() {
            return INSTANCE;
        }

        private NoActionBlobListener() {
        }

    }

}