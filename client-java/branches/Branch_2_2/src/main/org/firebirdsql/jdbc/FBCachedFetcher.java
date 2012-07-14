package org.firebirdsql.jdbc;


import java.sql.*;
import java.util.ArrayList;

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
    
    private FBObjectListener.FetcherListener fetcherListener;

    FBCachedFetcher(GDSHelper gdsHelper, int fetchSize, int maxRows, 
            AbstractIscStmtHandle stmt_handle, FBObjectListener.FetcherListener fetcherListener, boolean forwardOnly) throws SQLException 
    {
        
        this.fetcherListener = fetcherListener;
        this.forwardOnly = forwardOnly;
        
        ArrayList rowsSets = new ArrayList(100);

        AbstractIscStmtHandle stmt =  stmt_handle;
        byte[][] localRow = null;

        XSQLVAR[] xsqlvars = stmt_handle.getOutSqlda().sqlvar;
        //
        // Check if there is blobs to catch
        //
        boolean[] isBlob = new boolean[xsqlvars.length];
        boolean hasBlobs = false;
        for (int i = 0; i < xsqlvars.length; i++){
            isBlob[i] = FBField.isType(xsqlvars[i], Types.BLOB) ||
                FBField.isType(xsqlvars[i], Types.BINARY) ||
                FBField.isType(xsqlvars[i], Types.LONGVARCHAR);
            if (isBlob[i]) 
                hasBlobs = true;
        }
        
        //
        // load all rows from statement
        //
        int rowsCount = 0;
        try {
            if (fetchSize == 0)
                fetchSize = MAX_FETCH_ROWS;
            this.fetchSize = fetchSize;

            // the following if, is only for callable statement				
            if (!stmt.isAllRowsFetched() && stmt.size() == 0) {
                do {
                    if (maxRows != 0 && fetchSize > maxRows - stmt.size())
                        fetchSize = maxRows - stmt.size();
                    gdsHelper.fetch(stmt, fetchSize);
                    if (stmt.size() > 0){
                        rowsSets.add(stmt.getRows());
                        rowsCount += stmt.size();
                        stmt.removeRows();
                    }
                } while (!stmt.isAllRowsFetched() && (maxRows==0 || rowsCount <maxRows));
                // now create one list with known capacity					 
                int rowCount = 0;
                rowsArray  = new Object[rowsCount];
                for (int i=0; i<rowsSets.size(); i++){
                    Object[] oneSet = (Object[]) rowsSets.get(i);
                    if (oneSet.length > rowsCount-rowCount){
                        System.arraycopy(oneSet, 0, rowsArray, rowCount, rowsCount-rowCount);
                        rowCount = rowsCount;
                    }
						  else{
                        System.arraycopy(oneSet, 0, rowsArray, rowCount, oneSet.length);
                        rowCount += oneSet.length;
                    }
                }
                rowsSets.clear();
            }
            else {
                rowsArray = stmt.getRows();
                stmt.removeRows();
            }
            //
            if (hasBlobs){
                for (int i=0;i< rowsArray.length; i++){
                    localRow = (byte[][])rowsArray[i];
                    //ugly blob caching workaround.
                    for (int j = 0; j < localRow.length; j++){    
                        
                        // if field is blob and there is a value in cache
                        if (isBlob[j] && localRow[j] != null ) {
                            
                            // anonymous implementation of the FieldDataProvider interface
                            final byte[] tempData = localRow[j];
                            FieldDataProvider dataProvider = new FieldDataProvider() {
                                public byte[] getFieldData() {
                                    return tempData;
                                }
                                
                                public void setFieldData(byte[] data) {
                                    throw new UnsupportedOperationException();
                                }
                            };

                            // copy data from current row
                            FBField localField = FBField.createField(
                                    xsqlvars[j], dataProvider, gdsHelper, false);
                            
                            FBFlushableField blob = (FBFlushableField)localField;
                            localRow[j] = blob.getCachedData();
                        }
                    }
                }
            }
            gdsHelper.closeStatement(stmt, false);
        }
        catch (GDSException ge) {
            throw new FBSQLException(ge);
        }
    }

    FBCachedFetcher(ArrayList rows, FBObjectListener.FetcherListener fetcherListener) throws SQLException {
        rowsArray = rows.toArray();
        this.fetcherListener = fetcherListener;
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
            throw new FBDriverNotCapableException(
                    "Result set is TYPE_FORWARD_ONLY");
        
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
            throw new FBDriverNotCapableException(
                    "Result set is TYPE_FORWARD_ONLY");

        return absolute(row, false);
    }
    
    private boolean absolute(int row, boolean internal) throws SQLException {
        
        if (forwardOnly && !internal)
            throw new FBDriverNotCapableException(
                    "Result set is TYPE_FORWARD_ONLY");
        
        if (row < 0)
            row = rowsArray.length + row + 1;
        
        if (row == 0 && !internal)
            throw new FBSQLException(
                "You cannot position to the row 0 with absolute() method.");
        
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
            throw new FBDriverNotCapableException(
                    "Result set is TYPE_FORWARD_ONLY");
        
        
        return absolute(1, true);
    }

    public boolean last() throws SQLException {
        if (forwardOnly)
            throw new FBDriverNotCapableException(
                    "Result set is TYPE_FORWARD_ONLY");
        
        
        return absolute(-1, true);
    }

    public boolean relative(int row) throws SQLException {
        if (forwardOnly)
            throw new FBDriverNotCapableException(
                    "Result set is TYPE_FORWARD_ONLY");
        
        
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
        return rowsArray != null ? rowNum == rowsArray.length : false;
    }
    public boolean isAfterLast() {
        return rowNum > rowsArray.length;
    }

    /* (non-Javadoc)
     * @see org.firebirdsql.jdbc.FBFetcher#deleteRow()
     */
    public void deleteRow() throws SQLException {
        Object[] newRows = new Object[rowsArray.length - 1];
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

    /* (non-Javadoc)
     * @see org.firebirdsql.jdbc.FBFetcher#insertRow(byte[][])
     */
    public void insertRow(byte[][] data) throws SQLException {
        Object[] newRows = new Object[rowsArray.length + 1];
        
        if (rowNum == 0)
            rowNum++;
        
        System.arraycopy(rowsArray, 0, newRows, 0, rowNum - 1);
        System.arraycopy(rowsArray, rowNum - 1, newRows, rowNum, rowsArray.length - rowNum + 1);
        newRows[rowNum - 1] = data;

        rowsArray = newRows;

        if (isAfterLast())
            fetcherListener.rowChanged(this, null);
        else
        if (isBeforeFirst())
            fetcherListener.rowChanged(this, null);
        else
            fetcherListener.rowChanged(this, (byte[][])rowsArray[rowNum-1]);
    }

    /* (non-Javadoc)
     * @see org.firebirdsql.jdbc.FBFetcher#updateRow(byte[][])
     */
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
