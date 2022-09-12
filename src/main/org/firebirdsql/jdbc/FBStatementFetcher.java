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
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.listeners.StatementListener;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Statement fetcher for read-only case. It differs from updatable cursor case
 * by the cursor position after {@link #next()} call. This class changes cursor
 * position to point to the next row.
 */
class FBStatementFetcher implements FBFetcher {

    private boolean closed;
    private boolean wasFetched;

    protected final GDSHelper gdsHelper;
    protected FBObjectListener.FetcherListener fetcherListener;

    protected final int maxRows;
    protected int fetchSize;

    protected final FbStatement stmt;

    private List<RowValue> rows = new ArrayList<>();
    private final RowListener rowListener = new RowListener();
    private boolean allRowsFetched;
    protected RowValue _nextRow;

    private int rowNum;
    private int rowPosition;

    private boolean isEmpty;
    private boolean isBeforeFirst = true;
    private boolean isFirst;
    private boolean isLast;
    private boolean isAfterLast;

    FBStatementFetcher(GDSHelper gdsHelper, FbStatement stmth, FBObjectListener.FetcherListener fetcherListener,
            int maxRows, int fetchSize) throws SQLException {
        this.gdsHelper = gdsHelper;
        this.stmt = stmth;
        stmt.addStatementListener(rowListener);
        this.fetcherListener = fetcherListener;
        this.maxRows = maxRows;
        this.fetchSize = fetchSize;
    }

    protected RowValue getNextRow() throws SQLException {
        if (!wasFetched) fetch();
        return _nextRow;
    }

    protected void setNextRow(RowValue nextRow) {
        _nextRow = nextRow;

        if (!wasFetched) {
            wasFetched = true;

            if (_nextRow == null) {
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

        if (isEmpty()) {
            return false;
        } else if (getNextRow() == null || (maxRows != 0 && getRowNum() == maxRows)) {
            setIsAfterLast(true);
            allRowsFetched = true;
            fetcherListener.allRowsFetched(this);
            setRowNum(0);
            return false;
        } else {
            fetcherListener.rowChanged(this, getNextRow());
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
        notScrollable();
        return false;
    }

    @Override
    public boolean first() throws SQLException {
        notScrollable();
        return false;
    }

    @Override
    public boolean last() throws SQLException {
        notScrollable();
        return false;
    }

    @Override
    public boolean previous() throws SQLException {
        notScrollable();
        return false;
    }

    @Override
    public boolean relative(int row) throws SQLException {
        notScrollable();
        return false;
    }

    @Override
    public void beforeFirst() throws SQLException {
        notScrollable();
    }

    @Override
    public void afterLast() throws SQLException {
        notScrollable();
    }

    public void fetch() throws SQLException {
        try (LockCloseable ignored = stmt.withLock()) {
            checkClosed();
            int maxRows = 0;

            if (this.maxRows != 0) maxRows = this.maxRows - rowNum;

            int fetchSize = this.fetchSize;
            if (fetchSize == 0) fetchSize = DEFAULT_FETCH_ROWS;

            if (maxRows != 0 && fetchSize > maxRows) fetchSize = maxRows;

            if (!allRowsFetched && (rows.isEmpty() || rows.size() == rowPosition)) {
                rows.clear();
                stmt.fetchRows(fetchSize);
                rowPosition = 0;
            }

            if (rows.size() > rowPosition) {
                setNextRow(rows.get(rowPosition));
                // help the garbage collector
                rows.set(rowPosition, null);
                rowPosition++;
            } else {
                setNextRow(null);
            }
        }
    }

    @Override
    public void close() throws SQLException {
        close(CompletionReason.OTHER);
    }

    @Override
    public void close(CompletionReason completionReason) throws SQLException {
        closed = true;
        try {
            stmt.closeCursor(completionReason.isTransactionEnd() || completionReason.isCompletesStatement());
        } finally {
            stmt.removeStatementListener(rowListener);
            rows = Collections.emptyList();
            fetcherListener.fetcherClosed(this);
        }
    }

    private void checkClosed() throws SQLException {
        if (closed) throw new FBSQLException("Result set is already closed.");
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
    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
    }

    @Override
    public int currentPosition() throws SQLException {
        throw new FBDriverNotCapableException("Cannot report current position. This is a bug in the calling code, because this method is not expected to be called on this implementation");
    }

    @Override
    public int size() throws SQLException {
        throw new FBDriverNotCapableException("Cannot report total size. This is a bug in the calling code, because this method is not expected to be called on this implementation");
    }

    @Override
    public void setFetcherListener(FBObjectListener.FetcherListener fetcherListener) {
        this.fetcherListener = fetcherListener;
    }

    @Override
    public int getFetchSize() {
        return fetchSize;
    }

    private void notScrollable() throws SQLException {
        throw new FbExceptionBuilder().nonTransientException(JaybirdErrorCodes.jb_operationNotAllowedOnForwardOnly)
                .toSQLException();
    }

    private final class RowListener implements StatementListener {
        @Override
        public void receivedRow(FbStatement sender, RowValue rowValue) {
            rows.add(rowValue);
        }

        @Override
        public void afterLast(FbStatement sender) {
            allRowsFetched = true;
        }
    }
}
