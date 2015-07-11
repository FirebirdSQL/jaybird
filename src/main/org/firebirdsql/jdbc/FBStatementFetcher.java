/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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

import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.listeners.DefaultStatementListener;

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
    protected final FBObjectListener.FetcherListener fetcherListener;

    protected final int maxRows;
    protected int fetchSize;

    protected final Synchronizable syncProvider;
    protected final FbStatement stmt;

    private List<RowValue> rows = new ArrayList<RowValue>();
    private final RowListener rowListener = new RowListener();
    private boolean allRowsFetched;
    protected RowValue _nextRow;

    private int rowNum = 0;
    private int rowPosition = 0;

    private boolean isEmpty = false;
    private boolean isBeforeFirst = false;
    private boolean isFirst = false;
    private boolean isLast = false;
    private boolean isAfterLast = false;

    FBStatementFetcher(GDSHelper gdsHelper, Synchronizable syncProvider,
            FbStatement stmth,
            FBObjectListener.FetcherListener fetcherListener, int maxRows,
            int fetchSize) throws SQLException {

        this.gdsHelper = gdsHelper;
        this.stmt = stmth;
        stmt.addStatementListener(rowListener);
        this.syncProvider = syncProvider;
        this.fetcherListener = fetcherListener;
        this.maxRows = maxRows;
        this.fetchSize = fetchSize;

        synchronized (syncProvider.getSynchronizationObject()) {
            isEmpty = false;
            isBeforeFirst = false;
            isFirst = false;
            isLast = false;
            isAfterLast = false;
            allRowsFetched = false;

            // stored procedures
            // TODO Need to add handling (probably to FBStatement) for EXECUTE PROCEDURE singleton result
            /*
            if (stmt.isAllRowsFetched()) {
                rowsArray = stmt.getRows();
                size = stmt.size();
            }
            */
        }
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

    public boolean absolute(int row) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public boolean first() throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public boolean last() throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public boolean previous() throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public boolean relative(int row) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void beforeFirst() throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void afterLast() throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void fetch() throws SQLException {
        synchronized (syncProvider.getSynchronizationObject()) {
            checkClosed();
            int maxRows = 0;

            if (this.maxRows != 0) maxRows = this.maxRows - rowNum;

            int fetchSize = this.fetchSize;
            if (fetchSize == 0) fetchSize = MAX_FETCH_ROWS;

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
            stmt.closeCursor(completionReason.isTransactionEnd());
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
    public int getFetchSize() {
        return fetchSize;
    }

    private class RowListener extends DefaultStatementListener {
        @Override
        public void receivedRow(FbStatement sender, RowValue rowValue) {
            rows.add(rowValue);
        }

        @Override
        public void allRowsFetched(FbStatement sender) {
            allRowsFetched = true;
        }
    }
}
