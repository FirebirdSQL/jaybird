// SPDX-FileCopyrightText: Copyright 2003-2004 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2022-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.ng.fields.RowValue;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.sql.SQLException;
import java.sql.SQLNonTransientException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

/**
 * Decorator that handles tracking updates, deletes and inserts of an updatable result set.
 * <p>
 * This fetcher handles the updatable result set behaviour defined in <a href="https://github.com/FirebirdSQL/jaybird/blob/master/devdoc/jdp/jdp-2021-04-real-scrollable-cursor-support.md">jdp-2021-04</a>
 * for server-side scrollable cursors and <a href="https://github.com/FirebirdSQL/jaybird/blob/master/devdoc/jdp/jdp-2024-05-behavior-of-updatable-result-sets.adoc">jdp-2024-05</a>
 * for emulated scrollable cursors.
 * </p>
 * <p>
 * This behaviour can be summarized as: updates are visible, deletes are visible (with a deletion marker row),
 * and inserts occur at the end of the cursor.
 * </p>
 *
 * @since 5
 */
@NullMarked
final class FBUpdatableFetcher implements FBFetcher {

    private final FBFetcher fetcher;
    private final RowValue deletedRowMarker;

    private FBObjectListener.FetcherListener fetcherListener;
    private Map<Integer, RowValue> modifiedRows = new HashMap<>();

    private int position;
    private List<RowValue> insertedRows = new ArrayList<>();
    private int firstInsertPosition;
    private int fetcherSize = -1;

    private final InternalFetcherListener rowListener = new InternalFetcherListener();

    /**
     * Creates an updatable fetcher wrapping a real fetcher.
     *
     * @param fetcher
     *         The fetcher decorated by this fetcher
     * @param fetcherListener
     *         Fetcher listener
     * @param deletedRowMarker
     *         Deleted row marker
     */
    FBUpdatableFetcher(FBFetcher fetcher, FBObjectListener.FetcherListener fetcherListener, RowValue deletedRowMarker) {
        if (!deletedRowMarker.isDeletedRowMarker()) {
            throw new IllegalArgumentException("deletedRowMarker should return true for isDeletedRowMarker()");
        }
        this.fetcher = fetcher;
        fetcher.setFetcherListener(rowListener);
        this.fetcherListener = fetcherListener;
        this.deletedRowMarker = deletedRowMarker;
    }

    @Override
    public FetchConfig getFetchConfig() {
        return fetcher.getFetchConfig();
    }

    @Override
    public void setReadOnly() throws SQLException {
        throw new SQLNonTransientException("This fetcher implementation cannot be marked read-only",
                SQLStateConstants.SQL_STATE_INVALID_ATTR_VALUE);
    }

    private boolean notifyFetcherRow(int position) throws SQLException {
        return notifyRow(position, rowListener.lastReceivedRow);
    }

    private boolean notifyInsertedRow(int position) throws SQLException {
        int insertPosition = insertPosition(position);
        RowValue rowValue = insertPosition < insertedRows.size() ? insertedRows.get(insertPosition) : null;
        return notifyRow(position, rowValue);
    }

    private boolean notifyRow(int position, @Nullable RowValue originalRowValue) throws SQLException {
        //noinspection DataFlowIssue : Disagree with IntelliJs nullability inference for this method
        RowValue rowValue = modifiedRows.getOrDefault(position, originalRowValue);
        fetcherListener.rowChanged(fetcher, rowValue);
        //noinspection ConstantValue : Disagree with IntelliJs nullability inference
        return rowValue != null;
    }

    private int insertPosition(int position) throws SQLException {
        int firstInsertPosition = firstInsertPosition();
        if (position < firstInsertPosition) {
            throw new SQLException(format("Implementation error: %d is not a valid insert-position (minimum: %d)",
                    position, firstInsertPosition));
        }
        return position - firstInsertPosition();
    }

    private int firstInsertPosition() throws SQLException {
        int firstInsertPosition = this.firstInsertPosition;
        if (firstInsertPosition == 0) {
            return this.firstInsertPosition = fetcherSize() + 1;
        }
        return firstInsertPosition;
    }

    private int fetcherSize() throws SQLException {
        int fetcherSize = this.fetcherSize;
        if (fetcherSize == -1) {
            return this.fetcherSize = fetcher.size();
        }
        return fetcherSize;
    }

    private int cappedPosition(int position) throws SQLException {
        return Math.max(0, Math.min(size() + 1, position));
    }

    @Override
    public boolean first() throws SQLException {
        this.position = 1;
        return fetcher.first() ? notifyFetcherRow(1) : notifyInsertedRow(1);
    }

    @Override
    public boolean last() throws SQLException {
        if (insertedRows.isEmpty()) {
            fetcher.last();
            int position = this.position = fetcher.currentPosition();
            return notifyFetcherRow(position);
        } else {
            int position = this.position = size();
            fetcher.afterLast();
            return notifyInsertedRow(position);
        }
    }

    @Override
    public boolean previous() throws SQLException {
        int position = this.position = cappedPosition(this.position - 1);
        if (position <= fetcherSize()) {
            fetcher.previous();
            assert position == fetcher.currentPosition() : "Position discrepancy: " + position + " <> " + fetcher.currentPosition();
            return notifyFetcherRow(position);
        } else {
            fetcher.afterLast();
            return notifyInsertedRow(position);
        }
    }

    @Override
    public boolean next() throws SQLException {
        int position = this.position = cappedPosition(this.position + 1);
        if (position <= fetcherSize()) {
            fetcher.next();
            assert position == fetcher.currentPosition() : "Position discrepancy: " + position + " <> " + fetcher.currentPosition();
            return notifyFetcherRow(position);
        } else {
            fetcher.afterLast();
            return notifyInsertedRow(position);
        }
    }

    @Override
    public boolean absolute(int row) throws SQLException {
        int position = row >= 0 ? cappedPosition(row) : cappedPosition(size() + 1 + row);
        return internalAbsolute(position);
    }

    private boolean internalAbsolute(int position) throws SQLException {
        this.position = position;
        if (position <= fetcherSize()) {
            fetcher.absolute(position);
            return notifyFetcherRow(position);
        } else {
            fetcher.afterLast();
            return notifyInsertedRow(position);
        }
    }

    @Override
    public boolean relative(int row) throws SQLException {
        int position = cappedPosition(this.position + row);
        return internalAbsolute(position);
    }

    @Override
    public void beforeFirst() throws SQLException {
        position = 0;
        fetcher.beforeFirst();
        notifyFetcherRow(0);
    }

    @Override
    public void afterLast() throws SQLException {
        int position = this.position = size() + 1;
        fetcher.afterLast();
        notifyFetcherRow(position);
    }

    @Override
    public void close() throws SQLException {
        close(CompletionReason.OTHER);
    }

    @Override
    public void close(CompletionReason completionReason) throws SQLException {
        try {
            fetcher.close(completionReason);
        } finally {
            modifiedRows.clear();
            modifiedRows = emptyMap();
            insertedRows.clear();
            insertedRows = emptyList();
        }
    }

    @Override
    public boolean isClosed() {
        return fetcher.isClosed();
    }

    @Override
    public int getRowNum() throws SQLException {
        int position = this.position;
        return position <= size() ? position : 0;
    }

    @Override
    public boolean isEmpty() throws SQLException {
        return size() == 0;
    }

    @Override
    public boolean isBeforeFirst() throws SQLException {
        return position == 0;
    }

    @Override
    public boolean isFirst() throws SQLException {
        return position == 1 && !isEmpty();
    }

    @Override
    public boolean isLast() throws SQLException {
        int size = size();
        return position == size && size > 0;
    }

    @Override
    public boolean isAfterLast() throws SQLException {
        return position > size();
    }

    @Override
    public void beforeExecuteInsert() throws SQLException {
        fetcher.beforeExecuteInsert();
    }

    @Override
    public void insertRow(RowValue data) throws SQLException {
        insertedRows.add(data);
        fetcherListener.rowChanged(this, data);
    }

    @Override
    public void deleteRow() throws SQLException {
        modifiedRows.put(position, deletedRowMarker);
        fetcherListener.rowChanged(this, deletedRowMarker);
    }

    @Override
    public void updateRow(RowValue data) throws SQLException {
        modifiedRows.put(position, data);
        fetcherListener.rowChanged(this, data);
    }

    @Override
    public void renotifyCurrentRow() throws SQLException {
        int position = this.position;
        // We can reuse the lastReceivedRow of the fetcher listener if the current row is not stored in this fetcher
        if (position <= fetcherSize()) {
            notifyFetcherRow(position);
        } else {
            notifyInsertedRow(position);
        }
    }

    @Override
    public int getFetchSize() throws SQLException {
        return fetcher.getFetchSize();
    }

    @Override
    public void setFetchSize(int fetchSize) throws SQLException {
        fetcher.setFetchSize(fetchSize);
    }

    @Override
    public int getFetchDirection() throws SQLException {
        return fetcher.getFetchDirection();
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        fetcher.setFetchDirection(direction);
    }

    @Override
    public int currentPosition() {
        return position;
    }

    @Override
    public int size() throws SQLException {
        return fetcherSize() + insertedRows.size();
    }

    @Override
    public void setFetcherListener(FBObjectListener.FetcherListener fetcherListener) {
        this.fetcherListener = fetcherListener;
    }

    @Override
    public boolean rowInserted() throws SQLException {
        return !(isBeforeFirst() || isAfterLast()) && firstInsertPosition() <= position;
    }

    @Override
    public boolean rowUpdated() throws SQLException {
        if (isBeforeFirst() || isAfterLast()) {
            return false;
        }

        RowValue rowValue = modifiedRows.get(position);
        return !(rowValue == null || rowValue.isDeletedRowMarker());
    }

    @Override
    public boolean rowDeleted() throws SQLException {
        if (isBeforeFirst() || isAfterLast()) {
            return false;
        }

        RowValue rowValue = modifiedRows.get(position);
        return rowValue != null && rowValue.isDeletedRowMarker();
    }

    private static final class InternalFetcherListener implements FBObjectListener.FetcherListener {

        @Nullable RowValue lastReceivedRow;

        @Override
        public void rowChanged(FBFetcher fetcher, @Nullable RowValue newRow) {
            lastReceivedRow = newRow;
        }

    }
}
