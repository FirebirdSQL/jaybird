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
import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.listeners.StatementListener;
import org.firebirdsql.jdbc.field.FBField;
import org.firebirdsql.jdbc.field.FBFlushableField;
import org.firebirdsql.jdbc.field.FieldDataProvider;
import org.firebirdsql.jdbc.field.JdbcTypeConverter;

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class FBCachedFetcher implements FBFetcher {

    private final boolean forwardOnly;
    private List<RowValue> rows;
    private int rowNum;
    private int fetchSize;
    private FBObjectListener.FetcherListener fetcherListener;

    FBCachedFetcher(GDSHelper gdsHelper, int fetchSize, int maxRows, FbStatement stmt_handle,
            FBObjectListener.FetcherListener fetcherListener, boolean forwardOnly) throws SQLException {
        this.fetcherListener = fetcherListener;
        this.forwardOnly = forwardOnly;
        final RowDescriptor rowDescriptor = stmt_handle.getRowDescriptor();

        // Check if there is blobs to catch
        final boolean[] isBlob = new boolean[rowDescriptor.getCount()];
        final boolean hasBlobs = determineBlobs(rowDescriptor, isBlob);

        // load all rows from statement
        this.fetchSize = fetchSize != 0 ? fetchSize : DEFAULT_FETCH_ROWS;

        try {
            RowListener rowListener = new RowListener();
            stmt_handle.addStatementListener(rowListener);
            try {
                int actualFetchSize = getFetchSize();
                while (!rowListener.isAllRowsFetched() && (maxRows == 0 || rowListener.size() < maxRows)) {
                    if (maxRows > 0) {
                        actualFetchSize = Math.min(actualFetchSize, maxRows - rowListener.size());
                    }
                    assert actualFetchSize > 0 : "actualFetchSize should be > 0";
                    stmt_handle.fetchRows(actualFetchSize);
                }
                rows = rowListener.getRows();
            } finally {
                stmt_handle.removeStatementListener(rowListener);
            }

            if (hasBlobs) {
                for (RowValue row : rows) {
                    cacheBlobsInRow(gdsHelper, rowDescriptor, isBlob, row);
                }
            }
        } finally {
            stmt_handle.closeCursor();
        }
    }

    /**
     * Populates the cached fetcher with the supplied data.
     *
     * @param rows
     *         Data for the rows
     * @param fetcherListener
     *         Fetcher listener
     * @param rowDescriptor
     *         Row descriptor (cannot be null when {@code retrieveBlobs} is {@code true})
     * @param gdsHelper
     *         GDS Helper (cannot be null when {@code retrieveBlobs} is {@code true})
     * @param retrieveBlobs
     *         {@code true} when the blobs need to be retrieved from the server and the current column values in
     *         {@code rows} of a blob is the blobid, otherwise the column values in {@code rows} for a blob should be
     *         the blob data.
     */
    FBCachedFetcher(List<RowValue> rows, FBObjectListener.FetcherListener fetcherListener, RowDescriptor rowDescriptor,
            GDSHelper gdsHelper, boolean retrieveBlobs) throws SQLException {
        assert !retrieveBlobs || rowDescriptor != null && gdsHelper != null
                : "Need non-null rowDescriptor and gdsHelper for retrieving blobs";
        this.rows = new ArrayList<>(rows);
        this.fetcherListener = fetcherListener;
        forwardOnly = false;
        if (retrieveBlobs) {
            final boolean[] isBlob = new boolean[rowDescriptor.getCount()];
            final boolean hasBlobs = determineBlobs(rowDescriptor, isBlob);
            if (hasBlobs){
                for (RowValue row : rows) {
                    cacheBlobsInRow(gdsHelper, rowDescriptor, isBlob, row);
                }
            }
        }
    }

    /**
     * Determines the columns that are blobs.
     *
     * @param rowDescriptor The row descriptor
     * @param isBlob Boolean array with length equal to {@code rowDescriptor}, modified by this method
     * @return {@code true} if there are one or more blob columns.
     */
    private static boolean determineBlobs(final RowDescriptor rowDescriptor, final boolean[] isBlob) {
        assert rowDescriptor.getCount() == isBlob.length : "length of isBlob should be equal to length of rowDescriptor";
        boolean hasBlobs = false;
        for (int i = 0; i < rowDescriptor.getCount(); i++) {
            final FieldDescriptor field = rowDescriptor.getFieldDescriptor(i);
            isBlob[i] = JdbcTypeConverter.isJdbcType(field, Types.BLOB) ||
                    JdbcTypeConverter.isJdbcType(field, Types.LONGVARBINARY) ||
                    JdbcTypeConverter.isJdbcType(field, Types.LONGVARCHAR);
            if (isBlob[i])
                hasBlobs = true;
        }
        return hasBlobs;
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
        if (isEmpty())
            return false;

        rowNum++;

        if (isAfterLast()) {
            fetcherListener.rowChanged(this, null);
            // keep cursor right after last row 
            rowNum = rows.size() + 1;

            return false;
        }

        fetcherListener.rowChanged(this, rows.get(rowNum - 1));

        return true;
    }

    @Override
    public boolean previous() throws SQLException {
        checkScrollable();

        if (isEmpty()) {
            return false;
        }

        rowNum--;

        if (isBeforeFirst()) {
            fetcherListener.rowChanged(this, null);

            // keep cursor right before the first row
            rowNum = 0;
            return false;
        }

        fetcherListener.rowChanged(this, rows.get(rowNum - 1));

        return true;
    }

    @Override
    public boolean absolute(int row) throws SQLException {
        return absolute(row, false);
    }

    private boolean absolute(int row, boolean internal) throws SQLException {
        checkScrollable();

        if (row < 0) {
            row = rows.size() + row + 1;
        }

        if (isEmpty()) {
            return false;
        }

        rowNum = row;

        if (isBeforeFirst()) {
            fetcherListener.rowChanged(this, null);

            // keep cursor right before the first row
            rowNum = 0;
            return false;
        }

        if (isAfterLast()) {
            fetcherListener.rowChanged(this, null);
            rowNum = rows.size() + 1;
            return false;
        }

        fetcherListener.rowChanged(this, rows.get(rowNum - 1));

        return true;
    }

    @Override
    public boolean first() throws SQLException {
        return absolute(1, true);
    }

    @Override
    public boolean last() throws SQLException {
        return absolute(-1, true);
    }

    @Override
    public boolean relative(int row) throws SQLException {
        return absolute(rowNum + row, true);
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
    public void close() throws SQLException {
        close(CompletionReason.OTHER);
    }

    @Override
    public void close(CompletionReason completionReason) throws SQLException {
        rows = Collections.emptyList();
        fetcherListener = null;
    }

    @Override
    public int getRowNum() {
        // TODO This seems to violate the requirement that it should return 0 when not on a row
        return rowNum;
    }

    @Override
    public boolean isEmpty() {
        return rows == null || rows.size() == 0;
    }

    @Override
    public boolean isBeforeFirst() {
        return !isEmpty() && rowNum < 1;
    }

    @Override
    public boolean isFirst() {
        return rowNum == 1;
    }

    @Override
    public boolean isLast() {
        return rows != null && rowNum == rows.size();
    }

    @Override
    public boolean isAfterLast() {
        return rowNum > rows.size();
    }

    @Override
    public void deleteRow() throws SQLException {
        rows.remove(rowNum - 1);

        if (isAfterLast() || isBeforeFirst())
            fetcherListener.rowChanged(this, null);
        else
            fetcherListener.rowChanged(this, rows.get(rowNum - 1));
    }

    @Override
    public void insertRow(RowValue data) throws SQLException {
        if (rowNum == 0)
            rowNum++;

        if (rowNum > rows.size()) {
            rows.add(data);
        } else {
            rows.add(rowNum - 1, data);
        }

        if (isAfterLast() || isBeforeFirst())
            fetcherListener.rowChanged(this, null);
        else
            fetcherListener.rowChanged(this, rows.get(rowNum - 1));
    }

    @Override
    public void updateRow(RowValue data) throws SQLException {
        if (!isAfterLast() && !isBeforeFirst()) {
            rows.set(rowNum - 1, data);
            fetcherListener.rowChanged(this, data);
        }
    }

    @Override
    public int getFetchSize() {
        return fetchSize;
    }

    @Override
    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
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
    public void setFetcherListener(FBObjectListener.FetcherListener fetcherListener) {
        this.fetcherListener = fetcherListener;
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

    /**
     * Checks if the result set is scrollable, otherwise throws an {@code SQLException}.
     * @throws SQLException If the result set is {@code TYPE_FORWARD_ONLY}.
     */
    private void checkScrollable() throws SQLException {
        if (forwardOnly) {
            throw new FbExceptionBuilder().nonTransientException(JaybirdErrorCodes.jb_operationNotAllowedOnForwardOnly)
                    .toSQLException();
        }
    }
}
