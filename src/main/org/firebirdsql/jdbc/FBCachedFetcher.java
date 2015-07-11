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

import java.sql.*;
import java.util.ArrayList;

import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.XSQLVAR;
import org.firebirdsql.gds.impl.AbstractIscStmtHandle;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.jdbc.field.*;

class FBCachedFetcher implements FBFetcher {

    private final boolean forwardOnly;
    private Object[] rowsArray;
    private int rowNum = 0;

    private int fetchSize;
    
    private final FBObjectListener.FetcherListener fetcherListener;

    FBCachedFetcher(GDSHelper gdsHelper, int fetchSize, int maxRows, AbstractIscStmtHandle stmt_handle,
            FBObjectListener.FetcherListener fetcherListener, boolean forwardOnly) throws SQLException {
        this.fetcherListener = fetcherListener;
        this.forwardOnly = forwardOnly;
        final ArrayList rowsSets = new ArrayList(100);
        final XSQLVAR[] xsqlvars = stmt_handle.getOutSqlda().sqlvar;

        // Check if there is blobs to catch
        final boolean[] isBlob = new boolean[xsqlvars.length];
        final boolean hasBlobs = determineBlobs(xsqlvars, isBlob);
        
        // load all rows from statement
        int rowsCount = 0;
        try {
            if (fetchSize == 0)
                fetchSize = MAX_FETCH_ROWS;
            this.fetchSize = fetchSize;

            // the following if, is only for callable statement				
            if (!stmt_handle.isAllRowsFetched() && stmt_handle.size() == 0) {
                do {
                    if (maxRows != 0 && fetchSize > maxRows - rowsCount)
                        fetchSize = maxRows - rowsCount;
                    gdsHelper.fetch(stmt_handle, fetchSize);

                    final int fetchedRowCount = stmt_handle.size();
                    if (fetchedRowCount > 0){
                        byte[][][] rows = stmt_handle.getRows();
                        // Copy of right length when less rows fetched than requested
                        if (rows.length > fetchedRowCount) {
                            final byte[][][] tempRows = new byte[fetchedRowCount][][];
                            System.arraycopy(rows, 0, tempRows, 0, fetchedRowCount);
                            rows = tempRows;
                        }
                        rowsSets.add(rows);
                        rowsCount += fetchedRowCount;
                        stmt_handle.removeRows();
                    }
                } while (!stmt_handle.isAllRowsFetched() && (maxRows == 0 || rowsCount <maxRows));

                // now create one list with known capacity					 
                int rowCount = 0;
                rowsArray = new Object[rowsCount];
                for (int i = 0; i < rowsSets.size(); i++){
                    final Object[] oneSet = (Object[]) rowsSets.get(i);
                    final int toCopy = Math.min(oneSet.length, rowsCount - rowCount);
                    System.arraycopy(oneSet, 0, rowsArray, rowCount, toCopy);
                    rowCount += toCopy;
                }
                rowsSets.clear();
            } else {
                rowsArray = stmt_handle.getRows();
                stmt_handle.removeRows();
            }

            if (hasBlobs){
                cacheBlobs(gdsHelper, xsqlvars, isBlob);
            }
            gdsHelper.closeStatement(stmt_handle, false);
        } catch (GDSException ge) {
            throw new FBSQLException(ge);
        }
    }

    /**
     * Populates the cached fetcher with the supplied data.
     *
     * @param rows
     *         Data for the rows
     * @param fetcherListener
     *         Fetcher listener
     * @param xsqlvars
     *         Row descriptor (cannot be null when {@code retrieveBlobs} is {@code true})
     * @param gdsHelper
     *         GDS Helper (cannot be null when {@code retrieveBlobs} is {@code true})
     * @param retrieveBlobs
     *         {@code true} when the blobs need to be retrieved from the server and the current column values in
     *         {@code rows} of a blob is the blobid, otherwise the column values in {@code rows} for a blob should be
     *         the blob data.
     * @throws SQLException
     */
    FBCachedFetcher(ArrayList rows, FBObjectListener.FetcherListener fetcherListener, XSQLVAR[] xsqlvars,
            GDSHelper gdsHelper, boolean retrieveBlobs) throws SQLException {
        assert retrieveBlobs && xsqlvars != null && gdsHelper != null || !retrieveBlobs : "Need non-null xsqlvars and gdsHelper for retrieving blobs";
        rowsArray = rows.toArray();
        this.fetcherListener = fetcherListener;
        forwardOnly = false;
        if (retrieveBlobs) {
            final boolean[] isBlob = new boolean[xsqlvars.length];
            final boolean hasBlobs = determineBlobs(xsqlvars, isBlob);
            if (hasBlobs){
                cacheBlobs(gdsHelper, xsqlvars, isBlob);
            }
        }
    }

    /**
     * Determines the columns that are blobs.
     *
     * @param xsqlvars The row descriptor
     * @param isBlob Boolean array with length equal to {@code xsqlvars}, modified by this method
     * @return {@code true} if there are one or more blob columns.
     */
    private static boolean determineBlobs(final XSQLVAR[] xsqlvars, final boolean[] isBlob) {
        assert xsqlvars.length == isBlob.length : "length of isBlob should be equal to length of xsqlvars";
        boolean hasBlobs = false;
        for (int i = 0; i < xsqlvars.length; i++){
            isBlob[i] = FBField.isType(xsqlvars[i], Types.BLOB) ||
                    FBField.isType(xsqlvars[i], Types.BINARY) ||
                    FBField.isType(xsqlvars[i], Types.LONGVARCHAR);
            if (isBlob[i])
                hasBlobs = true;
        }
        return hasBlobs;
    }

    private void cacheBlobs(final GDSHelper gdsHelper, final XSQLVAR[] xsqlvars, final boolean[] isBlob)
            throws SQLException {
        for (int i=0; i < rowsArray.length; i++){
            final byte[][] localRow = (byte[][]) rowsArray[i];
            //ugly blob caching workaround.
            for (int j = 0; j < localRow.length; j++) {
                // if field is blob and there is a value in cache
                if (isBlob[j] && localRow[j] != null ) {
                    // anonymous implementation of the FieldDataProvider interface
                    final byte[] tempData = localRow[j];
                    final FieldDataProvider dataProvider = new FieldDataProvider() {
                        public byte[] getFieldData() {
                            return tempData;
                        }

                        public void setFieldData(byte[] data) {
                            throw new UnsupportedOperationException();
                        }
                    };

                    // copy data from current row
                    final FBFlushableField blob = (FBFlushableField) FBField
                            .createField(xsqlvars[j], dataProvider, gdsHelper, false);
                    localRow[j] = blob.getCachedData();
                }
            }
        }
    }

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

        fetcherListener.rowChanged(this, (byte[][])rowsArray[rowNum-1]);
               
        return true;
    }

    public boolean previous() throws SQLException {
        if (forwardOnly)
            throw new FBDriverNotCapableException("Result set is TYPE_FORWARD_ONLY");
        
        if (isEmpty())
            return false;
        
        rowNum--;

        if(isBeforeFirst()) {
            fetcherListener.rowChanged(this, null);
            
            // keep cursor right before the first row
            rowNum = 0;
            return false;
        }

        fetcherListener.rowChanged(this, (byte[][])rowsArray[rowNum-1]);
        
        return true;
    }
    
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
            throw new FBSQLException("You cannot position to the row 0 with absolute() method.");
        
        if (isEmpty())
            return false;
        
        rowNum = row;

        if(isBeforeFirst()) {
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

        fetcherListener.rowChanged(this, (byte[][])rowsArray[rowNum-1]);
        
        return true;
    }

    public boolean first() throws SQLException {
        if (forwardOnly)
            throw new FBDriverNotCapableException("Result set is TYPE_FORWARD_ONLY");

        return absolute(1, true);
    }

    public boolean last() throws SQLException {
        if (forwardOnly)
            throw new FBDriverNotCapableException("Result set is TYPE_FORWARD_ONLY");

        return absolute(-1, true);
    }

    public boolean relative(int row) throws SQLException {
        if (forwardOnly)
            throw new FBDriverNotCapableException("Result set is TYPE_FORWARD_ONLY");

        return absolute(rowNum + row, true);
    }
    
    public void beforeFirst() throws SQLException {
        first();
        previous();
    }
    
    public void afterLast() throws SQLException {
        last();
        next();
    }

    public void close() throws SQLException {
        close(CompletionReason.OTHER);
    }

    public void close(CompletionReason completionReason) throws SQLException {
        rowsArray = new Object[0];
    }

    public int getRowNum() {
        return rowNum;
    }
    public boolean isEmpty() {
        return rowsArray == null || rowsArray.length == 0;
    }
    public boolean isBeforeFirst() {
        return !isEmpty() && rowNum < 1;
    }
    public boolean isFirst() {
        return rowNum == 1;
    }
    public boolean isLast() {
        return rowsArray != null && rowNum == rowsArray.length;
    }
    public boolean isAfterLast() {
        return rowNum > rowsArray.length;
    }

    public void deleteRow() throws SQLException {
        final Object[] newRows = new Object[rowsArray.length - 1];
        System.arraycopy(rowsArray, 0, newRows, 0, rowNum - 1);
        
        if (rowNum < rowsArray.length)
            System.arraycopy(rowsArray, rowNum, newRows, rowNum - 1, rowsArray.length - rowNum);
        
        rowsArray = newRows;
        
        if (isAfterLast())
            fetcherListener.rowChanged(this, null);
        else
        if (isBeforeFirst())
            fetcherListener.rowChanged(this, null);
        else
            fetcherListener.rowChanged(this, (byte[][])rowsArray[rowNum-1]);
    }

    public void insertRow(byte[][] data) throws SQLException {
        final Object[] newRows = new Object[rowsArray.length + 1];
        
        if (rowNum == 0)
            rowNum++;
        
        System.arraycopy(rowsArray, 0, newRows, 0, rowNum - 1);
        System.arraycopy(rowsArray, rowNum - 1, newRows, rowNum, rowsArray.length - rowNum + 1);
        newRows[rowNum - 1] = data;

        rowsArray = newRows;

        if (isAfterLast() || isBeforeFirst())
            fetcherListener.rowChanged(this, null);
        else
            fetcherListener.rowChanged(this, (byte[][])rowsArray[rowNum-1]);
    }

    public void updateRow(byte[][] data) throws SQLException {
        if (!isAfterLast() && !isBeforeFirst()) {
            rowsArray[rowNum - 1] = data;
            fetcherListener.rowChanged(this, data);
        }
    }

    public int getFetchSize(){
        return fetchSize;
    }

    public void setFetchSize(int fetchSize){
        this.fetchSize = fetchSize;
    }
}