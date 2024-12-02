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
import org.firebirdsql.gds.ng.FbAttachment;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.jdbc.FBObjectListener.FetcherListener;
import org.firebirdsql.util.InternalApi;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.sql.SQLException;

import static java.util.Objects.requireNonNull;

/**
 * Common base class for {@link FBFetcher} implementations.
 *
 * @author Mark Rotteveel
 * @since 6
 */
@InternalApi
@NullMarked
abstract sealed class AbstractFetcher implements FBFetcher
        permits FBCachedFetcher, FBServerScrollFetcher, FBStatementFetcher {

    private FetchConfig fetchConfig;
    private FetcherListener fetcherListener;
    private @Nullable RowValue currentRow;
    private volatile boolean closed;

    AbstractFetcher(FetchConfig fetchConfig, FetcherListener fetcherListener) {
        this.fetchConfig = requireNonNull(fetchConfig, "fetchConfig");
        this.fetcherListener = requireNonNull(fetcherListener, "fetcherListener");
    }

    @Override
    public final void setFetcherListener(FetcherListener fetcherListener) {
        this.fetcherListener = requireNonNull(fetcherListener, "fetcherListener");
    }

    /**
     * Calls {@link FetcherListener#rowChanged(FBFetcher, RowValue)} of the fetcher listener of this instance.
     *
     * @param newRow
     *         new row
     * @throws SQLException
     *         for exceptions thrown by {@link FetcherListener#rowChanged(FBFetcher, RowValue)}
     */
    protected final void notifyRowChanged(@Nullable RowValue newRow) throws SQLException {
        currentRow = newRow;
        fetcherListener.rowChanged(this, newRow);
    }

    @Override
    public final void renotifyCurrentRow() throws SQLException {
        fetcherListener.rowChanged(this, currentRow);
    }

    @Override
    public final void close() throws SQLException {
        close(CompletionReason.OTHER);
    }

    /**
     * {@inheritDoc}
     * <p>
     * After marking the fetcher as closed, this method calls {@link #handleClose(CompletionReason)}. Subclasses need to
     * override that method if they need additional cleanup.
     * </p>
     */
    @Override
    public final void close(CompletionReason completionReason) throws SQLException {
        if (isClosed()) return;
        if (completionReason == CompletionReason.CONNECTION_ABORT) {
            closed = true;
            handleClose(completionReason);
        } else {
            try (var ignored = withLock()) {
                closed = true;
                handleClose(completionReason);
            }
        }
    }

    /**
     * Method to perform cleanup during a {@link #close(CompletionReason)}.
     * <p>
     * Subclasses should override this when they need to perform additional cleanup during close. Subclasses should
     * <strong>not</strong> call {@link #withLock()} in this method.
     * </p>
     *
     * @param completionReason
     *         completion reason
     * @throws SQLException
     *         for errors closing resources
     */
    protected void handleClose(CompletionReason completionReason) throws SQLException {
        // default do nothing
    }

    @Override
    public final boolean isClosed() {
        return closed;
    }

    protected final void checkOpen() throws SQLException {
        if (closed) {
            throw FbExceptionBuilder.toNonTransientException(JaybirdErrorCodes.jb_resultSetClosed);
        }
    }

    @Override
    public final FetchConfig getFetchConfig() {
        try (var ignored = withLock()) {
            return fetchConfig;
        }
    }

    @Override
    public final void setReadOnly() {
        try (var ignored = withLock()) {
            fetchConfig = fetchConfig.withReadOnly();
        }
    }

    /**
     * Returns {@code maxRows} without checking if this fetcher is open.
     *
     * @return maximum number of rows to fetch, or {@code 0} for no maximum
     */
    protected final int getMaxRows() {
        return getFetchConfig().maxRows();
    }

    @Override
    public final int getFetchSize() throws SQLException {
        checkOpen();
        return getFetchConfig().fetchSize();
    }

    /**
     * Get the actual fetch size.
     * <p>
     * The default implementation returns either the fetch size of the fetch config, or
     * {@link FBFetcher#DEFAULT_FETCH_ROWS}. Subclasses may override this to use their own defaults or &mdash; for
     * example &mdash; take the already fetched rows and max rows into account.
     * </p>
     *
     * @return fetch size
     */
    protected int actualFetchSize() {
        return getFetchConfig().fetchSizeOr(FBFetcher.DEFAULT_FETCH_ROWS);
    }

    @Override
    public final void setFetchSize(int fetchSize) throws SQLException {
        checkOpen();
        try (var ignored = withLock()) {
            fetchConfig = fetchConfig.withFetchSize(fetchSize);
        }
    }

    @Override
    public final int getFetchDirection() throws SQLException {
        checkOpen();
        return getFetchConfig().direction();
    }

    @Override
    public final void setFetchDirection(int direction) throws SQLException {
        checkOpen();
        try (var ignored = withLock()) {
            fetchConfig = fetchConfig.withDirection(direction);
        }
    }

    /**
     * @see FbAttachment#withLock()
     */
    protected abstract LockCloseable withLock();

}
