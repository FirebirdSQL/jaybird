package org.firebirdsql.jdbc;


import java.sql.*;
import java.util.ArrayList;

import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.isc_stmt_handle;
import org.firebirdsql.jdbc.field.*;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

class FBCachedFetcher implements FBFetcher {

    private boolean forwardOnly;
    private Object[] rowsArray;
    private AbstractStatement fbStatement;
    private FBResultSet rs;
    private int rowNum = 0;

    private final static Logger log = LoggerFactory.getLogger(FBCachedFetcher.class,false);
          
    FBCachedFetcher(AbstractConnection c, AbstractStatement fbStatement, 
            isc_stmt_handle stmt_handle, FBResultSet rs) throws SQLException 
    {
        
        this.forwardOnly = rs.getType() == ResultSet.TYPE_FORWARD_ONLY;
        
        ArrayList rowsSets = new ArrayList(100);
        ArrayList rows = new ArrayList(100);

        isc_stmt_handle stmt =  stmt_handle;
        byte[][] localRow = null;
            
        this.fbStatement = fbStatement;
        this.rs = rs;
            
        //
        // Check if there is blobs to catch
        //
        boolean[] isBlob = new boolean[rs.xsqlvars.length];
        boolean hasBlobs = false;
        for (int i = 0; i < rs.xsqlvars.length; i++){
            isBlob[i] = FBField.isType(rs.xsqlvars[i], Types.BLOB) ||
                FBField.isType(rs.xsqlvars[i], Types.BINARY) ||
                FBField.isType(rs.xsqlvars[i], Types.LONGVARCHAR);
            if (isBlob[i]) 
                hasBlobs = true;
        }
        //
        // load all rows from statement
        //
        int rowsCount = 0;
        try {
            int fetchSize = fbStatement.fetchSize;
            if (fetchSize == 0)
                fetchSize = MAX_FETCH_ROWS;
				// the following if, is only for callable statement				
				if (!stmt.getAllRowsFetched() && stmt.size() == 0) {
                do {
                    if (fbStatement.maxRows != 0 && fetchSize > fbStatement.maxRows - stmt.size())
                        fetchSize = fbStatement.maxRows - stmt.size();
                    c.fetch(stmt, fetchSize);
                    if (stmt.size() > 0){
                        rowsSets.add(stmt.getRows());
                        rowsCount += stmt.size();
                        stmt.removeRows();
                    }
                } while (!stmt.getAllRowsFetched() && (fbStatement.maxRows==0 || rowsCount <fbStatement.maxRows));
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
                                    rs.xsqlvars[j], dataProvider,false);
                            
                            FBFlushableField blob = (FBFlushableField)localField;
                                  
                            if (blob instanceof FBBlobField)
                                ((FBBlobField)blob).setConnection(c);
                            else
                            if (blob instanceof FBLongVarCharField)
                                ((FBLongVarCharField)blob).setConnection(c);
                                
                            localRow[j] = blob.getCachedObject();
                        }
                    }
                }
            }
            c.closeStatement(stmt, false);
        }
        catch (GDSException ge) {
            throw new FBSQLException(ge);
        }
    }

    FBCachedFetcher(ArrayList rows, FBResultSet rs) throws SQLException {
        rowsArray = rows.toArray();
        this.rs = rs;
    }

    public boolean next() throws SQLException {
        if (isEmpty())
            return false;
        
        rowNum++;
        
        if (isAfterLast()) {
            rs.row = null;
            // keep cursor right after last row 
            rowNum = rowsArray.length + 1;
            
            return false;
        }

        rs.row = (byte[][])rowsArray[rowNum-1];		  
               
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
            rs.row = null;
            
            // keep cursor right before the first row
            rowNum = 0;
            return false;
        }
            
        rs.row = (byte[][])rowsArray[rowNum-1];       
        
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
            rs.row = null;
            
            // keep cursor right before the first row
            rowNum = 0;
            return false;
        } 
        
        if (isAfterLast()) {
            rs.row = null;
            rowNum = rowsArray.length + 1;
            return false;
        }
        
        rs.row = (byte[][])rowsArray[rowNum-1];       
        
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
    }

    public AbstractStatement getStatement() {
        return fbStatement;
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
}
