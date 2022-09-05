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

import org.firebirdsql.gds.ng.CursorFlag;
import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.FetchDirection;
import org.firebirdsql.gds.ng.FetchType;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.listeners.StatementListener;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import static org.firebirdsql.gds.ISCConstants.INF_RECORD_COUNT;
import static org.firebirdsql.gds.ISCConstants.isc_info_end;
import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger;
import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger2;

/**
 * Fetcher implementation for server-side scrollable cursors.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
final class FBServerScrollFetcher implements FBFetcher {

    private static final int CURSOR_SIZE_UNKNOWN = -1;

    private final FbStatement stmt;
    private final Object syncObject;
    private final int maxRows;

    private FBObjectListener.FetcherListener fetcherListener;
    private int fetchSize;
    private boolean closed;

    // The cursor size taking account maxRows
    private int cursorSize = CURSOR_SIZE_UNKNOWN;
    // The cursor size on the server (ignoring maxRows)
    private int serverCursorSize = CURSOR_SIZE_UNKNOWN;
    private int serverPosition;
    private int localPosition;

    // Current window of rows
    private List<RowValue> rows = new ArrayList<>();
    // Offset of first row in rows
    private int rowsOffset;

    FBServerScrollFetcher(int initialFetchSize, int maxRows, FbStatement stmt, Synchronizable syncProvider,
            FBObjectListener.FetcherListener fetcherListener) throws SQLException {
        if (!stmt.supportsFetchScroll()) {
            throw new FBDriverNotCapableException("Statement implementation does not support server-side scrollable result sets; this exception indicates a bug in Jaybird");
        }
        if (!stmt.isCursorFlagSet(CursorFlag.CURSOR_TYPE_SCROLLABLE)) {
            throw new FBDriverNotCapableException("Statement does not have CURSOR_TYPE_SCROLLABLE; this exception indicates a bug in Jaybird");
        }
        fetchSize = initialFetchSize;
        this.maxRows = maxRows;
        this.stmt = stmt;
        this.fetcherListener = fetcherListener;
        this.syncObject = syncProvider.getSynchronizationObject();
    }

    private boolean inWindow(int position) throws SQLException {
        int windowSize = rows.size();
        int rowsOffset = this.rowsOffset;
        if (windowSize == 0 || rowsOffset == 0 || (maxRows != 0 && position > requireCursorSize())) {
            return false;
        }
        return rowsOffset <= position && position < rowsOffset + windowSize;
    }

    private RowValue rowChange(int newLocalPosition) throws SQLException {
        localPosition = newLocalPosition;
        return inWindow(newLocalPosition) ? rows.get(newLocalPosition - rowsOffset) : null;
    }

    private boolean notifyRowChange(int newLocalPosition) throws SQLException {
        return notifyRow(rowChange(newLocalPosition));
    }

    private boolean notifyRow(RowValue rowValue) throws SQLException {
        fetcherListener.rowChanged(this, rowValue);
        return rowValue != null;
    }

    private RowListener fetchWithListener(FetchType fetchType, int fetchSize, int position)
            throws SQLException {
        RowListener rowListener = new RowListener();
        stmt.addStatementListener(rowListener);
        try {
            stmt.fetchScroll(fetchType, fetchSize, position);
        } finally {
            stmt.removeStatementListener(rowListener);
        }
        return rowListener;
    }

    private void updateWindow(RowListener listener, ServerPositionCalculation serverPositionCalculation,
            FetchDirection fetchDirection) throws SQLException {
        rows.clear();
        List<RowValue> newRows = listener.rowValues;
        int rowCount = newRows.size();
        int serverPosition = this.serverPosition = serverPositionCalculation.newServerPosition(rowCount, listener);
        if (rowCount == 0) {
            rowsOffset = 0;
        } else {
            // NOTE: This is safe as long as the fetcher isn't closed, and afterwards, this shouldn't get called
            ArrayList<RowValue> rows = (ArrayList<RowValue>) this.rows;
            rows.ensureCapacity(rowCount);
            if (fetchDirection == FetchDirection.REVERSE) {
                if (rowCount == 1) {
                    rows.add(newRows.get(0));
                } else {
                    // Rows are received from high to low, but we need them from low to high
                    ListIterator<RowValue> iter = newRows.listIterator(rowCount);
                    while (iter.hasPrevious()) {
                        rows.add(iter.previous());
                    }
                }
                rowsOffset = listener.beforeFirst ? 1 : serverPosition;
            } else {
                rows.addAll(newRows);
                rowsOffset = serverPosition - rowCount + (listener.afterLast ? 0 : 1);
            }
        }
    }

    private void synchronizeServerPosition(int expectedPosition) throws SQLException {
        if (serverPosition != expectedPosition) {
            stmt.fetchScroll(FetchType.ABSOLUTE, -1, expectedPosition);
            serverPosition = expectedPosition;
        }
    }

    @Override
    public boolean first() throws SQLException {
        synchronized (syncObject) {
            checkOpen();
            int newLocalPosition = 1;
            if (!inWindow(newLocalPosition) && cursorSize != 0) {
                RowListener listener = fetchWithListener(FetchType.FIRST, -1, -1);
                updateWindow(listener, (r, l) -> 1, FetchDirection.UNKNOWN);
                if (listener.afterLast) {
                    cursorSize = 0;
                }
            }
            return notifyRowChange(newLocalPosition);
        }
    }

    @Override
    public boolean last() throws SQLException {
        synchronized (syncObject) {
            checkOpen();
            int cursorSize = this.cursorSize;
            int newLocalPosition;
            if (cursorSize == 0) {
                newLocalPosition = 1;
            } else if (inWindow(cursorSize)) {
                newLocalPosition = cursorSize;
            } else {
                RowListener listener;
                if (maxRows != 0 && ((cursorSize = requireCursorSize()) < requireServerCursorSize())) {
                    listener = fetchWithListener(FetchType.ABSOLUTE, -1, cursorSize);
                } else {
                    listener = fetchWithListener(FetchType.LAST, -1, -1);
                }
                updateWindow(listener, lastServerPosition(), FetchDirection.UNKNOWN);
                newLocalPosition = serverPosition;
            }
            return notifyRowChange(newLocalPosition);
        }
    }

    private ServerPositionCalculation lastServerPosition() {
        return (rowCount, listener) -> rowCount == 0 ? 1 : requireCursorSize();
    }

    @Override
    public boolean previous() throws SQLException {
        synchronized (syncObject) {
            checkOpen();
            int oldLocalPosition = localPosition;
            int newLocalPosition = Math.max(1, oldLocalPosition) - 1;
            if (!inWindow(newLocalPosition)) {
                synchronizeServerPosition(oldLocalPosition);
                RowListener listener = fetchWithListener(FetchType.PRIOR, actualFetchSize(), -1);
                updateWindow(listener, previousServerPosition(oldLocalPosition), FetchDirection.REVERSE);
                if (listener.beforeFirst) {
                    serverPosition = 0;
                }
            }
            return notifyRowChange(newLocalPosition);
        }
    }

    private ServerPositionCalculation previousServerPosition(int initialServerPosition) {
        return (rowCount, listener) -> listener.beforeFirst ? 0 : initialServerPosition - rowCount;
    }

    @Override
    public boolean next() throws SQLException {
        synchronized (syncObject) {
            checkOpen();
            int oldLocalPosition = localPosition;
            boolean hasMaxRows = maxRows > 0;
            int cursorSize = hasMaxRows && oldLocalPosition != 0 ? requireCursorSize() : this.cursorSize;
            int newLocalPosition =
                    (cursorSize != CURSOR_SIZE_UNKNOWN ? Math.min(cursorSize, oldLocalPosition) : oldLocalPosition) + 1;
            if (!inWindow(newLocalPosition)) {
                int fetchSize = actualFetchSize();
                if (hasMaxRows) {
                    if (cursorSize == CURSOR_SIZE_UNKNOWN) {
                        // special case if not fetched yet while maxRows is set
                        fetchSize = Math.min(fetchSize, maxRows);
                    } else {
                        fetchSize = Math.min(fetchSize, cursorSize + 1 - oldLocalPosition);
                    }
                }
                if (fetchSize == 0) {
                    afterLast();
                    return false;
                }
                synchronizeServerPosition(oldLocalPosition);
                RowListener listener = fetchWithListener(FetchType.NEXT, fetchSize, -1);
                updateWindow(listener, nextServerPosition(oldLocalPosition), FetchDirection.FORWARD);
            }
            return notifyRowChange(newLocalPosition);
        }
    }

    private ServerPositionCalculation nextServerPosition(int initialServerPosition) {
        return (rowCount, listener) -> listener.afterLast ? requireCursorSize() + 1 : initialServerPosition + rowCount;
    }

    @Override
    public boolean absolute(int row) throws SQLException {
        synchronized (syncObject) {
            checkOpen();
            // Overflow beyond cursor size is handled by inWindow returning false
            int newLocalPosition = row >= 0 ? row : Math.max(0, requireCursorSize() + 1 + row);
            if (!inWindow(row)) {
                if (maxRows != 0 && newLocalPosition > requireCursorSize()) {
                    afterLast();
                    return false;
                }
                // NOTE: Using the desired position instead of row to avoid issues when maxRow is set,
                // so not using negative fetch offered by server-side cursor
                RowListener listener = fetchWithListener(FetchType.ABSOLUTE, -1, newLocalPosition);
                FetchDirection fetchDirection = row < 0 ? FetchDirection.REVERSE : FetchDirection.FORWARD;
                updateWindow(listener, absoluteServerPosition(row), fetchDirection);
                newLocalPosition = serverPosition;
            }
            return notifyRowChange(newLocalPosition);
        }
    }

    private ServerPositionCalculation absoluteServerPosition(int absoluteRow) {
        return (rowCount, listener) -> {
            if (listener.beforeFirst) {
                return 0;
            } else if (listener.afterLast) {
                return requireCursorSize() + 1;
            } else if (absoluteRow >= 0) {
                // case absoluteRow == 0 should be handled by if (listener.beforeFirst), but covered for completeness
                return absoluteRow;
            } else {
                return requireCursorSize() + absoluteRow + 1;
            }
        };
    }

    @Override
    public boolean relative(int row) throws SQLException {
        synchronized (syncObject) {
            checkOpen();
            int oldLocalPosition = localPosition;
            // Overflow beyond cursor size is handled by inWindow returning false
            int newLocalPosition = Math.max(0, oldLocalPosition + row);
            if (row != 0  && !inWindow(newLocalPosition)) {
                if (maxRows != 0 && newLocalPosition > requireCursorSize()) {
                    afterLast();
                    return false;
                }
                synchronizeServerPosition(oldLocalPosition);
                RowListener listener = fetchWithListener(FetchType.RELATIVE, -1, row);
                FetchDirection fetchDirection = row < 0 ? FetchDirection.REVERSE : FetchDirection.FORWARD;
                updateWindow(listener, relativeServerPosition(row), fetchDirection);
                newLocalPosition = serverPosition;
            }
            return notifyRowChange(newLocalPosition);
        }
    }

    private ServerPositionCalculation relativeServerPosition(int relativeRow) {
        return (rowCount, listener) -> {
            if (listener.beforeFirst) {
                return 0;
            } else if (listener.afterLast) {
                // NOTE: If not for the row != 0 check in relative(int), this would require specific handling
                // for serverPosition == 0 and relativeRow == 0 given the handling of IN_PLACE in V10Statement.processFetchResponse
                return requireCursorSize() + 1;
            } else {
                assert rowCount == 1 : "expected rowCount == 1, was " + rowCount;
                return serverPosition + relativeRow;
            }
        };
    }

    @Override
    public void beforeFirst() throws SQLException {
        synchronized (syncObject) {
            checkOpen();
            if (localPosition != 0) {
                stmt.fetchScroll(FetchType.ABSOLUTE, -1, 0);
                serverPosition = 0;
            }
            notifyRowChange(0);
        }
    }

    @Override
    public void afterLast() throws SQLException {
        synchronized (syncObject) {
            checkOpen();
            int afterLastPosition = requireCursorSize() + 1;
            if (localPosition != afterLastPosition) {
                stmt.fetchScroll(FetchType.ABSOLUTE, -1, afterLastPosition);
                serverPosition = afterLastPosition;
            }
            notifyRowChange(afterLastPosition);
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
            rows.clear();
            rowsOffset = 0;
            rows = Collections.emptyList();
            fetcherListener.fetcherClosed(this);
        }
    }

    private void checkOpen() throws SQLException {
        if (closed) throw new FBSQLException("Result set is already closed.");
    }

    @Override
    public int getRowNum() throws SQLException {
        synchronized (syncObject) {
            // NOTE Relying on isAfterLast to (indirectly) call checkOpen()
            return isAfterLast() ? 0 : localPosition;
        }
    }

    @Override
    public boolean isEmpty() throws SQLException {
        synchronized (syncObject) {
            // NOTE Relying on requireCursorSize to call checkOpen()
            int cursorSize = requireCursorSize();
            return cursorSize == 0;
        }
    }

    @Override
    public boolean isBeforeFirst() throws SQLException {
        synchronized (syncObject) {
            checkOpen();
            return localPosition == 0;
        }
    }

    @Override
    public boolean isFirst() throws SQLException {
        synchronized (syncObject) {
            checkOpen();
            return localPosition == 1 && requireCursorSize() > 0;
        }
    }

    @Override
    public boolean isLast() throws SQLException {
        synchronized (syncObject) {
            // NOTE Relying on requireCursorSize to call checkOpen()
            int cursorSize = requireCursorSize();
            return localPosition == cursorSize && cursorSize > 0;
        }
    }

    @Override
    public boolean isAfterLast() throws SQLException {
        synchronized (syncObject) {
            if (localPosition == 0) return false;
            // NOTE Relying on requireCursorSize to call checkOpen()
            int cursorSize = requireCursorSize();
            return localPosition > cursorSize;
        }
    }

    @Override
    public void insertRow(RowValue data) throws SQLException {
        throw new UnsupportedOperationException("Implementation error: FBServerScrollFetcher should be decorated with FBUpdatableFetcher");
    }

    @Override
    public void deleteRow() throws SQLException {
        throw new UnsupportedOperationException("Implementation error: FBServerScrollFetcher should be decorated with FBUpdatableFetcher");
    }

    @Override
    public void updateRow(RowValue data) throws SQLException {
        throw new UnsupportedOperationException("Implementation error: FBServerScrollFetcher should be decorated with FBUpdatableFetcher");
    }

    private int actualFetchSize() {
        return fetchSize > 0 ? fetchSize : DEFAULT_FETCH_ROWS;
    }

    @Override
    public int getFetchSize() throws SQLException {
        synchronized (syncObject) {
            checkOpen();
            return fetchSize;
        }
    }

    @Override
    public void setFetchSize(int fetchSize) {
        synchronized (syncObject) {
            this.fetchSize = fetchSize;
        }
    }

    @Override
    public int currentPosition() {
        return localPosition;
    }

    @Override
    public int size() throws SQLException {
        return requireCursorSize();
    }

    @Override
    public void setFetcherListener(FBObjectListener.FetcherListener fetcherListener) {
        this.fetcherListener  = fetcherListener;
    }

    private int retrieveServerCursorSize() throws SQLException {
        return stmt.getCursorInfo(new byte[] { (byte) INF_RECORD_COUNT, isc_info_end }, 10, buffer -> {
            if (buffer[0] != INF_RECORD_COUNT) {
                throw new SQLException("Unexpected response buffer");
            }
            int length = iscVaxInteger2(buffer, 1);
            return iscVaxInteger(buffer, 3, length);
        });
    }

    private int requireCursorSize() throws SQLException {
        checkOpen();
        int cursorSize = this.cursorSize;
        if (cursorSize == CURSOR_SIZE_UNKNOWN) {
            int serverCursorSize = requireServerCursorSize();
            cursorSize = this.cursorSize = maxRows == 0 ? serverCursorSize : Math.min(maxRows, serverCursorSize);
        }
        return cursorSize;
    }

    private int requireServerCursorSize() throws SQLException{
        int serverCursorSize = this.serverCursorSize;
        if (serverCursorSize == CURSOR_SIZE_UNKNOWN) {
            if (!stmt.hasFetched()) {
                // A fetch is required before we can retrieve the cursor size, fetch without moving current position
                stmt.fetchScroll(FetchType.RELATIVE, -1, 0);
            }
            serverCursorSize = this.serverCursorSize = retrieveServerCursorSize();
        }
        return serverCursorSize;
    }

    private static final class RowListener implements StatementListener {
        boolean beforeFirst;
        boolean afterLast;

        final List<RowValue> rowValues = new ArrayList<>();

        @Override
        public void beforeFirst(FbStatement sender) {
            beforeFirst = true;
        }

        @Override
        public void afterLast(FbStatement sender) {
            afterLast = true;
        }

        @Override
        public void receivedRow(FbStatement sender, RowValue rowValue) {
            rowValues.add(rowValue);
        }
    }

    @FunctionalInterface
    private interface ServerPositionCalculation {

        int newServerPosition(int receivedRowCount, RowListener listener) throws SQLException;

    }
}
