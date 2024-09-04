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
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.FetchDirection;
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.listeners.StatementListener;

import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Statement fetcher for read-only case. It differs from updatable cursor case
 * by the cursor position after {@link #next()} call. This class changes cursor
 * position to point to the next row.
 */
sealed class FBStatementFetcher extends AbstractFetcher implements FBFetcher permits FBUpdatableCursorFetcher {

    private static final int NO_ASYNC_FETCH = -1;
    private static final int MINIMUM_ASYNC_FETCH_ROW_COUNT = 10;
    private static final int ASYNC_FETCH_FACTOR = 3;

    private boolean wasFetched;

    protected final GDSHelper gdsHelper;

    private int maxActualFetchSize;
    private int asyncFetchOnRemaining = NO_ASYNC_FETCH;

    protected final FbStatement stmt;

    private Deque<RowValue> rows;
    private final RowListener rowListener = new RowListener();
    private boolean allRowsFetched;
    protected RowValue nextRow;

    private int rowNum;

    private boolean isEmpty;
    private boolean isBeforeFirst = true;
    private boolean isFirst;
    private boolean isLast;
    private boolean isAfterLast;

    @SuppressWarnings("java:S1872")
    FBStatementFetcher(GDSHelper gdsHelper, FetchConfig fetchConfig, FbStatement stmth,
            FBObjectListener.FetcherListener fetcherListener) {
        super(fetchConfig, fetcherListener);
        this.gdsHelper = gdsHelper;
        stmt = stmth;
        stmt.addStatementListener(rowListener);
        // Compare by class name because the class might not be loaded
        if (stmth.getClass().getName().equals("org.firebirdsql.gds.ng.jna.JnaStatement")) {
            // Performs only singular fetches, so only need space for one row
            rows = new ArrayDeque<>(1);
        } else {
            // The default size is 16, pre-sizing it at fetch size would avoid overhead of resizing, but given a lot of
            // queries will produce only one or a few rows, that would result in unnecessary memory overhead. If a lot
            // of rows are fetched, the first fetch and possibly the first async fetch will take the hit of resizing,
            // but then reach a steady state.
            rows = new ArrayDeque<>();
        }
    }

    protected RowValue getNextRow() throws SQLException {
        if (!wasFetched) fetch();
        return nextRow;
    }

    protected void setNextRow(RowValue nextRow) {
        this.nextRow = nextRow;

        if (!wasFetched) {
            wasFetched = true;

            if (nextRow == null) {
                isEmpty = true;
            } else {
                isBeforeFirst = true;
            }
        }
    }

    @Override
    public boolean next() throws SQLException {
        if (!wasFetched) fetch();

        setIsBeforeFirst(false);
        setIsFirst(false);
        setIsLast(false);
        setIsAfterLast(false);

        if (isEmpty()) return false;

        int maxRows = getMaxRows();
        if (getNextRow() == null || (maxRows != 0 && getRowNum() == maxRows)) {
            setIsAfterLast(true);
            allRowsFetched = true;
            setRowNum(0);
            return false;
        } else {
            notifyRowChanged(getNextRow());
            fetch();
            setRowNum(getRowNum() + 1);

            if (getRowNum() == 1) {
                setIsFirst(true);
            }

            if ((getNextRow() == null) || (maxRows != 0 && getRowNum() == maxRows)) {
                setIsLast(true);
            }

            return true;
        }
    }

    @Override
    public boolean absolute(int row) throws SQLException {
        throw notScrollable();
    }

    @Override
    public boolean first() throws SQLException {
        throw notScrollable();
    }

    @Override
    public boolean last() throws SQLException {
        throw notScrollable();
    }

    @Override
    public boolean previous() throws SQLException {
        throw notScrollable();
    }

    @Override
    public boolean relative(int row) throws SQLException {
        throw notScrollable();
    }

    @Override
    public void beforeFirst() throws SQLException {
        throw notScrollable();
    }

    @Override
    public void afterLast() throws SQLException {
        throw notScrollable();
    }

    protected final void fetch() throws SQLException {
        try (var ignored = withLock()) {
            checkOpen();
            if (!allRowsFetched && rows.isEmpty()) {
                int actualFetchSize = actualFetchSize();
                if (actualFetchSize > 0) {
                    stmt.fetchRows(actualFetchSize);
                }
            } else if (!allRowsFetched && rows.size() == asyncFetchOnRemaining) {
                int actualFetchSize = actualFetchSize();
                if (actualFetchSize > 1) {
                    // NOTE: Using > 1 instead of > 0 as attempting to async fetch 1 row is ignored anyway
                    stmt.asyncFetchRows(actualFetchSize);
                }
            }

            setNextRow(rows.pollFirst());
        }
    }

    @Override
    protected int actualFetchSize() {
        int fetchSize = super.actualFetchSize();
        int maxRows = getMaxRows();
        if (maxRows == 0) {
            return fetchSize;
        }
        return Math.min(fetchSize, maxRows - rowNum - rows.size());
    }

    @Override
    protected void handleClose(CompletionReason completionReason) throws SQLException {
        try {
            stmt.closeCursor(completionReason.isTransactionEnd() || completionReason.isCompletesStatement());
        } finally {
            stmt.removeStatementListener(rowListener);
            rows = new ArrayDeque<>(0);
        }
    }

    @Override
    public int getRowNum() {
        return rowNum;
    }

    public void setRowNum(int rowNumValue) {
        this.rowNum = rowNumValue;
    }

    @Override
    public boolean isEmpty() throws SQLException {
        if (!wasFetched) fetch();
        return isEmpty;
    }

    public void setIsEmpty(boolean isEmptyValue) {
        this.isEmpty = isEmptyValue;
    }

    @Override
    public boolean isBeforeFirst() throws SQLException {
        if (!wasFetched) fetch();
        return isBeforeFirst;
    }

    public void setIsBeforeFirst(boolean isBeforeFirstValue) {
        this.isBeforeFirst = isBeforeFirstValue;
    }

    @Override
    public boolean isFirst() throws SQLException {
        if (!wasFetched) fetch();
        return isFirst;
    }

    public void setIsFirst(boolean isFirstValue) {
        this.isFirst = isFirstValue;
    }

    @Override
    public boolean isLast() throws SQLException {
        if (!wasFetched) fetch();
        return isLast;
    }

    public void setIsLast(boolean isLastValue) {
        this.isLast = isLastValue;
    }

    @Override
    public boolean isAfterLast() throws SQLException {
        if (!wasFetched) fetch();
        return isAfterLast;
    }

    public void setIsAfterLast(boolean isAfterLastValue) {
        this.isAfterLast = isAfterLastValue;
    }

    @Override
    public void deleteRow() throws SQLException {
        // empty
    }

    @Override
    public void insertRow(RowValue data) throws SQLException {
        // empty
    }

    @Override
    public void updateRow(RowValue data) throws SQLException {
        // empty
    }

    @Override
    public int currentPosition() throws SQLException {
        throw new FBDriverNotCapableException("Cannot report current position. This is a bug in the calling code, because this method is not expected to be called on this implementation");
    }

    @Override
    public int size() throws SQLException {
        throw new FBDriverNotCapableException("Cannot report total size. This is a bug in the calling code, because this method is not expected to be called on this implementation");
    }

    private static SQLException notScrollable() {
        return FbExceptionBuilder.forNonTransientException(JaybirdErrorCodes.jb_operationNotAllowedOnForwardOnly)
                .toSQLException();
    }

    @Override
    protected LockCloseable withLock() {
        return stmt.withLock();
    }

    private final class RowListener implements StatementListener {
        @Override
        public void receivedRow(FbStatement sender, RowValue rowValue) {
            rows.addLast(rowValue);
        }

        @Override
        public void afterLast(FbStatement sender) {
            allRowsFetched = true;
        }

        @Override
        public void fetchComplete(FbStatement sender, FetchDirection fetchDirection, int rows) {
            if (rows > maxActualFetchSize) {
                maxActualFetchSize = rows;
                if (rows >= MINIMUM_ASYNC_FETCH_ROW_COUNT * 3 / 2) {
                    asyncFetchOnRemaining = Math.max(rows / ASYNC_FETCH_FACTOR, MINIMUM_ASYNC_FETCH_ROW_COUNT);
                }
            }
        }
    }
}
