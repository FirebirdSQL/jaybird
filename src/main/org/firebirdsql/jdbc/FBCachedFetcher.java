/*
 * $Id$
 *
 * Firebird Open Source J2EE Connector - JDBC Driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.jdbc;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.XSQLVAR;
import org.firebirdsql.gds.impl.AbstractIscStmtHandle;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.jdbc.field.*;

class FBCachedFetcher implements FBFetcher {

    private boolean forwardOnly;
    private Object[] rowsArray;
    private int rowNum = 0;

    private int fetchSize;

    private final FBObjectListener.FetcherListener fetcherListener;

    FBCachedFetcher(GDSHelper gdsHelper, int fetchSize, int maxRows, AbstractIscStmtHandle stmt_handle,
            FBObjectListener.FetcherListener fetcherListener, boolean forwardOnly) throws SQLException {
        this.fetcherListener = fetcherListener;
        this.forwardOnly = forwardOnly;
        final XSQLVAR[] xsqlvars = stmt_handle.getOutSqlda().sqlvar;

        // Check if there is blobs to catch
        boolean[] isBlob = new boolean[xsqlvars.length];
        boolean hasBlobs = false;
        for (int i = 0; i < xsqlvars.length; i++) {
            isBlob[i] = FBField.isType(xsqlvars[i], Types.BLOB) ||
                    FBField.isType(xsqlvars[i], Types.BINARY) ||
                    FBField.isType(xsqlvars[i], Types.LONGVARCHAR);
            if (isBlob[i])
                hasBlobs = true;
        }

        // load all rows from statement
        try {
            if (fetchSize == 0)
                fetchSize = MAX_FETCH_ROWS;
            this.fetchSize = fetchSize;

            // the following if, is only for callable statement				
            if (!stmt_handle.isAllRowsFetched() && stmt_handle.size() == 0) {
                final List<Object[]> rowsSets = new ArrayList<Object[]>(100);
                int rowsCount = 0;
                do {
                    if (maxRows != 0 && fetchSize > maxRows - stmt_handle.size()) {
                        fetchSize = maxRows - stmt_handle.size();
                    }
                    gdsHelper.fetch(stmt_handle, fetchSize);
                    if (stmt_handle.size() > 0) {
                        rowsSets.add(stmt_handle.getRows());
                        rowsCount += stmt_handle.size();
                        stmt_handle.removeRows();
                    }
                } while (!stmt_handle.isAllRowsFetched() && (maxRows == 0 || rowsCount < maxRows));

                // now create one list with known capacity					 
                int rowCount = 0;
                rowsArray = new Object[rowsCount];
                for (Object[] oneSet : rowsSets) {
                    if (oneSet.length > rowsCount - rowCount) {
                        System.arraycopy(oneSet, 0, rowsArray, rowCount, rowsCount - rowCount);
                        rowCount = rowsCount;
                    } else {
                        System.arraycopy(oneSet, 0, rowsArray, rowCount, oneSet.length);
                        rowCount += oneSet.length;
                    }
                }
                rowsSets.clear();
            } else {
                rowsArray = stmt_handle.getRows();
                stmt_handle.removeRows();
            }

            if (hasBlobs) {
                for (Object aRowsArray : rowsArray) {
                    cacheBlobsInRow(gdsHelper, xsqlvars, isBlob, (byte[][]) aRowsArray);
                }
            }
            gdsHelper.closeStatement(stmt_handle, false);
        } catch (GDSException ge) {
            throw new FBSQLException(ge);
        }
    }

    FBCachedFetcher(List<byte[][]> rows, FBObjectListener.FetcherListener fetcherListener) throws SQLException {
        rowsArray = rows.toArray();
        this.fetcherListener = fetcherListener;
    }

    private static void cacheBlobsInRow(final GDSHelper gdsHelper, final XSQLVAR[] xsqlvars, final boolean[] isBlob,
            final byte[][] localRow) throws SQLException {
        //ugly blob caching workaround.
        for (int j = 0; j < localRow.length; j++) {
            // if field is blob and there is a value to cache
            if (isBlob[j] && localRow[j] != null) {
                final byte[] tempData = localRow[j];
                FieldDataProvider dataProvider = new FieldDataProvider() {
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
                        xsqlvars[j], dataProvider, gdsHelper, false);
                localRow[j] = blob.getCachedData();
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
            rowNum = rowsArray.length + 1;

            return false;
        }

        fetcherListener.rowChanged(this, (byte[][]) rowsArray[rowNum - 1]);

        return true;
    }

    @Override
    public boolean previous() throws SQLException {
        if (forwardOnly)
            throw new FBDriverNotCapableException("Result set is TYPE_FORWARD_ONLY");

        if (isEmpty())
            return false;

        rowNum--;

        if (isBeforeFirst()) {
            fetcherListener.rowChanged(this, null);

            // keep cursor right before the first row
            rowNum = 0;
            return false;
        }

        fetcherListener.rowChanged(this, (byte[][]) rowsArray[rowNum - 1]);

        return true;
    }

    @Override
    public boolean absolute(int row) throws SQLException {
        if (forwardOnly)
            throw new FBDriverNotCapableException("Result set is TYPE_FORWARD_ONLY");

        return absolute(row, false);
    }

    private boolean absolute(int row, boolean internal) throws SQLException {
        if (forwardOnly && !internal)
            throw new FBDriverNotCapableException("Result set is TYPE_FORWARD_ONLY");

        if (row < 0)
            row = rowsArray.length + row + 1;

        if (row == 0 && !internal)
            throw new FBSQLException("You cannot position to row 0 with absolute() method.");

        if (isEmpty())
            return false;

        rowNum = row;

        if (isBeforeFirst()) {
            fetcherListener.rowChanged(this, null);

            // keep cursor right before the first row
            rowNum = 0;
            return false;
        }

        if (isAfterLast()) {
            fetcherListener.rowChanged(this, null);
            rowNum = rowsArray.length + 1;
            return false;
        }

        fetcherListener.rowChanged(this, (byte[][]) rowsArray[rowNum - 1]);

        return true;
    }

    @Override
    public boolean first() throws SQLException {
        if (forwardOnly)
            throw new FBDriverNotCapableException("Result set is TYPE_FORWARD_ONLY");

        return absolute(1, true);
    }

    @Override
    public boolean last() throws SQLException {
        if (forwardOnly)
            throw new FBDriverNotCapableException("Result set is TYPE_FORWARD_ONLY");

        return absolute(-1, true);
    }

    @Override
    public boolean relative(int row) throws SQLException {
        if (forwardOnly)
            throw new FBDriverNotCapableException("Result set is TYPE_FORWARD_ONLY");

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
        rowsArray = new Object[0];
    }

    @Override
    public int getRowNum() {
        return rowNum;
    }

    @Override
    public boolean isEmpty() {
        return rowsArray == null || rowsArray.length == 0;
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
        return rowsArray != null && rowNum == rowsArray.length;
    }

    @Override
    public boolean isAfterLast() {
        return rowNum > rowsArray.length;
    }

    @Override
    public void deleteRow() throws SQLException {
        final Object[] newRows = new Object[rowsArray.length - 1];
        System.arraycopy(rowsArray, 0, newRows, 0, rowNum - 1);

        if (rowNum < rowsArray.length)
            System.arraycopy(rowsArray, rowNum, newRows, rowNum - 1, rowsArray.length - rowNum);

        rowsArray = newRows;

        if (isAfterLast())
            fetcherListener.rowChanged(this, null);
        else if (isBeforeFirst())
            fetcherListener.rowChanged(this, null);
        else
            fetcherListener.rowChanged(this, (byte[][]) rowsArray[rowNum - 1]);
    }

    @Override
    public void insertRow(byte[][] data) throws SQLException {
        final Object[] newRows = new Object[rowsArray.length + 1];

        if (rowNum == 0)
            rowNum++;

        System.arraycopy(rowsArray, 0, newRows, 0, rowNum - 1);
        System.arraycopy(rowsArray, rowNum - 1, newRows, rowNum, rowsArray.length - rowNum + 1);
        newRows[rowNum - 1] = data;

        rowsArray = newRows;

        if (isAfterLast())
            fetcherListener.rowChanged(this, null);
        else if (isBeforeFirst())
            fetcherListener.rowChanged(this, null);
        else
            fetcherListener.rowChanged(this, (byte[][]) rowsArray[rowNum - 1]);
    }

    @Override
    public void updateRow(byte[][] data) throws SQLException {
        if (!isAfterLast() && !isBeforeFirst()) {
            rowsArray[rowNum - 1] = data;
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
}
