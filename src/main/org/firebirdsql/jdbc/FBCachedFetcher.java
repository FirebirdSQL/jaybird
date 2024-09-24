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

import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.listeners.StatementListener;
import org.firebirdsql.jdbc.field.FBField;
import org.firebirdsql.jdbc.field.FBFlushableField;
import org.firebirdsql.jdbc.field.FieldDataProvider;
import org.firebirdsql.jdbc.field.JdbcTypeConverter;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@NullMarked
final class FBCachedFetcher extends AbstractFetcher implements FBFetcher {

    private List<RowValue> rows;
    private int rowNum;
    private final Supplier<LockCloseable> lockAction;

    FBCachedFetcher(GDSHelper gdsHelper, FetchConfig fetchConfig, FbStatement stmtHandle,
            FBObjectListener.FetcherListener fetcherListener) throws SQLException {
        super(fetchConfig, fetcherListener);
        lockAction = stmtHandle::withLock;

        // load all rows from statement
        try {
            var rowListener = new RowListener();
            stmtHandle.addStatementListener(rowListener);
            try {
                int actualFetchSize = actualFetchSize();
                int maxRows = getMaxRows();
                while (!rowListener.isAllRowsFetched() && (maxRows == 0 || rowListener.size() < maxRows)) {
                    if (maxRows > 0) {
                        actualFetchSize = Math.min(actualFetchSize, maxRows - rowListener.size());
                    }
                    stmtHandle.fetchRows(actualFetchSize);
                }
                rows = rowListener.getRows();
            } finally {
                stmtHandle.removeStatementListener(rowListener);
            }

            // check if there are blobs to cache
            RowDescriptor rowDescriptor = stmtHandle.getRowDescriptor();
            boolean[] isBlob = determineBlobs(rowDescriptor);
            if (isBlob != null) {
                for (RowValue row : rows) {
                    cacheBlobsInRow(gdsHelper, rowDescriptor, isBlob, row);
                }
            }
        } finally {
            stmtHandle.closeCursor();
        }
    }

    /**
     * Populates the cached fetcher with the supplied data.
     *
     * @param rows
     *         data for the rows
     * @param fetchConfig
     *         fetch configuration
     * @param fetcherListener
     *         fetcher listener
     * @param rowDescriptor
     *         row descriptor (cannot be null when {@code retrieveBlobs} is {@code true})
     * @param gdsHelper
     *         GDS Helper (cannot be null when {@code retrieveBlobs} is {@code true})
     * @param retrieveBlobs
     *         {@code true} when the blobs need to be retrieved from the server and the current column values in
     *         {@code rows} of a blob is the blobid, otherwise the column values in {@code rows} for a blob should be
     *         the blob data.
     */
    FBCachedFetcher(List<RowValue> rows, FetchConfig fetchConfig, FBObjectListener.FetcherListener fetcherListener,
            RowDescriptor rowDescriptor, @Nullable GDSHelper gdsHelper, boolean retrieveBlobs) throws SQLException {
        super(fetchConfig, fetcherListener);
        assert !retrieveBlobs || rowDescriptor != null && gdsHelper != null
                : "Need non-null rowDescriptor and gdsHelper for retrieving blobs";
        this.rows = new ArrayList<>(rows);
        if (retrieveBlobs) {
            boolean[] isBlob = determineBlobs(rowDescriptor);
            if (isBlob != null) {
                for (RowValue row : rows) {
                    cacheBlobsInRow(gdsHelper, rowDescriptor, isBlob, row);
                }
            }
        }
        // Formally, using NO_OP could result in a thread-safety issue, but we accept that as it is uncommon, and usage
        // is likely restricted to one thread at a time
        lockAction = gdsHelper != null ? gdsHelper::withLock : () -> LockCloseable.NO_OP;
    }

    /**
     * Determines the columns that are blobs.
     *
     * @param rowDescriptor
     *         row descriptor
     * @return {@code null} if there are no blob columns, otherwise a {@code boolean[]} marking the blob columns.
     */
    private static boolean @Nullable [] determineBlobs(final RowDescriptor rowDescriptor) {
        boolean hasBlobs = false;
        boolean[] isBlob = new boolean[rowDescriptor.getCount()];
        for (int i = 0; i < rowDescriptor.getCount(); i++) {
            FieldDescriptor field = rowDescriptor.getFieldDescriptor(i);
            isBlob[i] = JdbcTypeConverter.isJdbcType(field, Types.BLOB)
                        || JdbcTypeConverter.isJdbcType(field, Types.LONGVARBINARY)
                        || JdbcTypeConverter.isJdbcType(field, Types.LONGVARCHAR);
            if (isBlob[i]) {
                hasBlobs = true;
            }
        }
        return hasBlobs ? isBlob : null;
    }

    private static void cacheBlobsInRow(final GDSHelper gdsHelper, final RowDescriptor rowDescriptor,
            final boolean[] isBlob, final RowValue localRow) throws SQLException {
        //ugly blob caching workaround.
        for (int j = 0; j < localRow.getCount(); j++) {
            // if field is blob and there is a value to cache
            final byte[] tempData = localRow.getFieldData(j);
            if (isBlob[j] && tempData != null) {
                final FieldDataProvider dataProvider = new FieldDataProvider() {
                    @Override
                    public byte[] getFieldData() {
                        return tempData;
                    }

                    @Override
                    public void setFieldData(byte[] data) {
                        throw new UnsupportedOperationException();
                    }
                };

                // copy data from current row
                final FBFlushableField blob = (FBFlushableField) FBField.createField(
                        rowDescriptor.getFieldDescriptor(j), dataProvider, gdsHelper, false);
                // TODO setCachedObject instead?
                localRow.setFieldData(j, blob.getCachedData());
            }
        }
    }

    @Override
    public boolean next() throws SQLException {
        if (isEmpty()) return false;

        rowNum++;

        if (adjustIfPositionAfterLast()) return false;
        notifyRowChanged(rows.get(rowNum - 1));
        return true;
    }

    private boolean adjustIfPositionAfterLast() throws SQLException {
        if (isAfterLast()) {
            notifyRowChanged(null);
            // keep cursor right after last row
            rowNum = rows.size() + 1;
            return true;
        }
        return false;
    }

    @Override
    public boolean previous() throws SQLException {
        if (isEmpty()) return false;

        rowNum--;

        if (adjustPositionIfBeforeFirst()) return false;
        notifyRowChanged(rows.get(rowNum - 1));
        return true;
    }

    private boolean adjustPositionIfBeforeFirst() throws SQLException {
        if (isBeforeFirst()) {
            notifyRowChanged(null);
            // keep cursor right before the first row
            rowNum = 0;
            return true;
        }
        return false;
    }

    @Override
    public boolean absolute(int row) throws SQLException {
        return setRowNum(row);
    }

    private boolean setRowNum(int row) throws SQLException {
        if (isEmpty()) return false;

        if (row < 0) {
            row = rows.size() + row + 1;
        }

        rowNum = row;

        if (adjustPositionIfBeforeFirst() || adjustIfPositionAfterLast()) return false;

        notifyRowChanged(rows.get(rowNum - 1));
        return true;
    }

    @Override
    public boolean first() throws SQLException {
        return setRowNum(1);
    }

    @Override
    public boolean last() throws SQLException {
        return setRowNum(-1);
    }

    @Override
    public boolean relative(int row) throws SQLException {
        return setRowNum(rowNum + row);
    }

    @Override
    public void beforeFirst() throws SQLException {
        first();
        previous();
    }

    @Override
    public void afterLast() throws SQLException {
        last();
        next();
    }

    @Override
    protected void handleClose(CompletionReason completionReason) {
        rows = Collections.emptyList();
    }

    @Override
    public int getRowNum() {
        // TODO This seems to violate the requirement that it should return 0 when not on a row
        return rowNum;
    }

    @Override
    public boolean isEmpty() {
        return rows.isEmpty();
    }

    @Override
    public boolean isBeforeFirst() {
        return rowNum < 1;
    }

    @Override
    public boolean isFirst() {
        return rowNum == 1;
    }

    @Override
    public boolean isLast() {
        return rowNum == rows.size();
    }

    @Override
    public boolean isAfterLast() {
        return rowNum > rows.size();
    }

    @Override
    public void deleteRow() throws SQLException {
        throw calledUndecorated();
    }

    @Override
    public void insertRow(RowValue data) throws SQLException {
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
        // NOTE: Current implementation of getRowNum is identical, but basically violates requirements
        return rowNum;
    }

    @Override
    public int size() {
        return rows.size();
    }

    @Override
    protected LockCloseable withLock() {
        return lockAction.get();
    }

    private static final class RowListener implements StatementListener {
        private final List<RowValue> rows = new ArrayList<>();
        private boolean allRowsFetched = false;

        @Override
        public void receivedRow(FbStatement sender, RowValue rowValue) {
            rows.add(rowValue);
        }

        @Override
        public void afterLast(FbStatement sender) {
            allRowsFetched = true;
        }

        public boolean isAllRowsFetched() {
            return allRowsFetched;
        }

        public List<RowValue> getRows() {
            return rows;
        }

        /**
         * @return Number of received rows.
         */
        public int size() {
            return rows.size();
        }
    }

}
