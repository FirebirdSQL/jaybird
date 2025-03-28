// SPDX-FileCopyrightText: Copyright 2022-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.ng.CursorFlag;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.FetchDirection;
import org.firebirdsql.gds.ng.FetchType;
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.listeners.StatementListener;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

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
 * @author Mark Rotteveel
 * @since 5
 */
@NullMarked
final class FBServerScrollFetcher extends AbstractFetcher implements FBFetcher {

    private static final int CURSOR_SIZE_UNKNOWN = -1;

    private final FbStatement stmt;

    // We delay knowing the local and server-side sizes. Requesting the server-side cursor size triggers a full
    // materialization of the cursor on the server, so delaying this avoids overhead at the cost of some more complexity
    // in this fetcher.

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

    FBServerScrollFetcher(FetchConfig fetchConfig, FbStatement stmt, FBObjectListener.FetcherListener fetcherListener)
            throws SQLException {
        super(fetchConfig, fetcherListener);
        if (!stmt.supportsFetchScroll()) {
            throw new FBDriverNotCapableException("Statement implementation does not support server-side scrollable result sets; this exception indicates a bug in Jaybird");
        }
        if (!stmt.isCursorFlagSet(CursorFlag.CURSOR_TYPE_SCROLLABLE)) {
            throw new FBDriverNotCapableException("Statement does not have CURSOR_TYPE_SCROLLABLE; this exception indicates a bug in Jaybird");
        }
        this.stmt = stmt;
    }

    private boolean inWindow(int position) throws SQLException {
        int windowSize = rows.size();
        int rowsOffset = this.rowsOffset;
        if (windowSize == 0 || rowsOffset == 0 || (getMaxRows() != 0 && position > requireCursorSize())) {
            return false;
        }
        return rowsOffset <= position && position < rowsOffset + windowSize;
    }

    private @Nullable RowValue rowChange(int newLocalPosition) throws SQLException {
        localPosition = newLocalPosition;
        return inWindow(newLocalPosition) ? rows.get(newLocalPosition - rowsOffset) : null;
    }

    private boolean notifyRowChange(int newLocalPosition) throws SQLException {
        return notifyRow(rowChange(newLocalPosition));
    }

    private boolean notifyRow(@Nullable RowValue rowValue) throws SQLException {
        notifyRowChanged(rowValue);
        return rowValue != null;
    }

    private RowListener fetchWithListener(FetchType fetchType, int fetchSize, int position) throws SQLException {
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
        int rowCount = listener.rowValues.size();
        serverPosition = serverPositionCalculation.newServerPosition(rowCount, listener);
        if (rowCount == 0) {
            rowsOffset = 0;
        } else {
            addRowsToWindow(listener, fetchDirection);
        }
    }

    private void addRowsToWindow(RowListener listener, FetchDirection fetchDirection) {
        // NOTE: This is safe as long as the fetcher isn't closed, and afterward, this shouldn't get called
        ArrayList<RowValue> rows = (ArrayList<RowValue>) this.rows;
        List<RowValue> newRows = listener.rowValues;
        int rowCount = newRows.size();
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

    private void synchronizeServerPosition(int expectedPosition) throws SQLException {
        if (serverPosition != expectedPosition) {
            stmt.fetchScroll(FetchType.ABSOLUTE, -1, expectedPosition);
            serverPosition = expectedPosition;
        }
    }

    @Override
    public boolean first() throws SQLException {
        try (var ignored = withLock()) {
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
        try (var ignored = withLock()) {
            checkOpen();
            int cursorSize = this.cursorSize;
            int newLocalPosition;
            if (cursorSize == 0) {
                newLocalPosition = 1;
            } else if (inWindow(cursorSize)) {
                newLocalPosition = cursorSize;
            } else {
                RowListener listener;
                if (getMaxRows() != 0 && ((cursorSize = requireCursorSize()) < requireServerCursorSize())) {
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
        try (var ignored = withLock()) {
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
        try (var ignored = withLock()) {
            checkOpen();
            int oldLocalPosition = localPosition;
            int maxRows = getMaxRows();
            int cursorSize = maxRows != 0 && oldLocalPosition != 0 ? requireCursorSize() : this.cursorSize;
            int newLocalPosition =
                    (cursorSize != CURSOR_SIZE_UNKNOWN ? Math.min(cursorSize, oldLocalPosition) : oldLocalPosition) + 1;
            if (!inWindow(newLocalPosition)) {
                int fetchSize = actualFetchSize();
                if (maxRows != 0) {
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
        try (var ignored = withLock()) {
            checkOpen();
            // Overflow beyond cursor size is handled by inWindow returning false
            int newLocalPosition = row >= 0 ? row : Math.max(0, requireCursorSize() + 1 + row);
            if (!inWindow(newLocalPosition)) {
                if (getMaxRows() != 0 && newLocalPosition > requireCursorSize()) {
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
        try (var ignored = withLock()) {
            checkOpen();
            int oldLocalPosition = localPosition;
            // Overflow beyond cursor size is handled by inWindow returning false
            int newLocalPosition = Math.max(0, oldLocalPosition + row);
            if (row != 0 && !inWindow(newLocalPosition)) {
                if (getMaxRows() != 0 && newLocalPosition > requireCursorSize()) {
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
        try (var ignored = withLock()) {
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
        try (var ignored = withLock()) {
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
    protected void handleClose(CompletionReason completionReason) throws SQLException {
        rowsOffset = 0;
        rows = Collections.emptyList();
        stmt.closeCursor(completionReason.isTransactionEnd() || completionReason.isCompletesStatement());
    }

    @Override
    public int getRowNum() throws SQLException {
        try (var ignored = withLock()) {
            // NOTE Relying on isAfterLast to (indirectly) call checkOpen()
            return isAfterLast() ? 0 : localPosition;
        }
    }

    @Override
    public boolean isEmpty() throws SQLException {
        try (var ignored = withLock()) {
            // NOTE Relying on requireCursorSize to call checkOpen()
            return requireCursorSize() == 0;
        }
    }

    @Override
    public boolean isBeforeFirst() throws SQLException {
        try (var ignored = withLock()) {
            checkOpen();
            return localPosition == 0;
        }
    }

    @Override
    public boolean isFirst() throws SQLException {
        try (var ignored = withLock()) {
            checkOpen();
            return localPosition == 1 && requireCursorSize() > 0;
        }
    }

    @Override
    public boolean isLast() throws SQLException {
        try (var ignored = withLock()) {
            // NOTE Relying on requireCursorSize to call checkOpen()
            int cursorSize = requireCursorSize();
            return localPosition == cursorSize && cursorSize > 0;
        }
    }

    @Override
    public boolean isAfterLast() throws SQLException {
        try (var ignored = withLock()) {
            if (localPosition == 0) return false;
            // NOTE Relying on requireCursorSize to call checkOpen()
            int cursorSize = requireCursorSize();
            return localPosition > cursorSize;
        }
    }

    @Override
    public void beforeExecuteInsert() throws SQLException {
        requireCursorSize();
    }

    @Override
    public void insertRow(RowValue data) throws SQLException {
        throw calledUndecorated();
    }

    @Override
    public void deleteRow() throws SQLException {
        throw calledUndecorated();
    }

    @Override
    public void updateRow(RowValue data) throws SQLException {
        throw calledUndecorated();
    }

    private static UnsupportedOperationException calledUndecorated() {
        return new UnsupportedOperationException(
                "Implementation error: FBServerScrollFetcher should be decorated with FBUpdatableFetcher");
    }

    @Override
    public int currentPosition() {
        return localPosition;
    }

    @Override
    public int size() throws SQLException {
        return requireCursorSize();
    }

    private int retrieveServerCursorSize() throws SQLException {
        return stmt.getCursorInfo(new byte[] { (byte) INF_RECORD_COUNT, isc_info_end }, 10, buffer -> {
            if (buffer[0] != INF_RECORD_COUNT) {
                throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_infoResponseEmpty)
                        .messageParameter("cursor")
                        .toSQLException();
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
            int maxRows = getMaxRows();
            cursorSize = this.cursorSize = maxRows == 0 ? serverCursorSize : Math.min(maxRows, serverCursorSize);
        }
        return cursorSize;
    }

    private int requireServerCursorSize() throws SQLException {
        int serverCursorSize = this.serverCursorSize;
        if (serverCursorSize != CURSOR_SIZE_UNKNOWN) return serverCursorSize;
        if (!stmt.hasFetched()) {
            // A fetch is required before we can retrieve the cursor size, fetch without moving current position
            stmt.fetchScroll(FetchType.RELATIVE, -1, 0);
        }
        return this.serverCursorSize = retrieveServerCursorSize();
    }

    @Override
    protected LockCloseable withLock() {
        return stmt.withLock();
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
