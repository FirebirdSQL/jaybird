package org.firebirdsql.jdbc;


import java.sql.*;
import java.util.ArrayList;

import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.isc_stmt_handle;
import org.firebirdsql.jdbc.field.*;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

class FBCachedFetcher implements FBFetcher {

    private Object[] rowsArray;
    private AbstractStatement fbStatement;
    private FBResultSet rs;
    private int rowNum = 0;
    private boolean isEmpty = false;     
    private boolean isBeforeFirst = false;
    private boolean isFirst = false;
    private boolean isLast = false;
    private boolean isAfterLast = false;

    private final static Logger log = LoggerFactory.getLogger(FBCachedFetcher.class,false);
          
    FBCachedFetcher(AbstractConnection c, AbstractStatement fbStatement, 
            isc_stmt_handle stmt_handle, FBResultSet rs) throws SQLException 
    {
        ArrayList rowsSets = new ArrayList(100);
        ArrayList rows = new ArrayList(100);

        isc_stmt_handle stmt =  stmt_handle;
        byte[][] localRow = null;
            
        this.fbStatement = fbStatement;
        this.rs = rs;
            
        isEmpty = false;
        isBeforeFirst = false;
        isFirst = false;
        isLast = false;
        isAfterLast = false;
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
                            
                            // make this row current in result set
                            rs.row = localRow;
                            
                            // copy data from current row
                            FBField localField = FBField.createField(
                                    rs.xsqlvars[j], rs, j,false);
                            
                            FBFlushableField blob = (FBFlushableField)localField;
                                  
                            if (blob instanceof FBBlobField)
                                ((FBBlobField)blob).setConnection(c);
                            else
                            if (blob instanceof FBLongVarCharField)
                                ((FBLongVarCharField)blob).setConnection(c);
                                
                            localRow[j] = blob.getCachedObject();
                            
                            // set current row in result set to null
                            rs.row = null;
                        }
                    }
                }
            }
            if (rowsArray.length==0)
                 isEmpty = true;
            else
                 isBeforeFirst = true;
            c.closeStatement(stmt, false);
        }
        catch (GDSException ge) {
            throw new FBSQLException(ge);
        }
    }

    FBCachedFetcher(ArrayList rows, FBResultSet rs) throws SQLException {
        rowsArray = rows.toArray();
        this.rs = rs;
            
        isEmpty = false;
        isBeforeFirst = false;
        isFirst = false;
        isLast = false;
        isAfterLast = false;
            
        if (rowsArray.length==0)
            isEmpty = true;
        else
            isBeforeFirst = true;
    }

    public boolean next() throws SQLException {
        isBeforeFirst = false;
        isFirst = false;
        isLast = false;
        isAfterLast = false;
                
        if (log!=null) log.debug("FBResultSet next - FBCachedFetcher");
        if (isEmpty)
            return false;
        else 
        if(rowNum == rowsArray.length) {
            rs.row = null;
            rowNum = 0;
            isAfterLast = true;
            return false;
        }
        else {
            rowNum++;
                
            if (rowNum == 1)
                isFirst = true;
            if (rowNum == rowsArray.length)
                isLast = true;
            rs.row = (byte[][])rowsArray[rowNum-1];		  
            // help the garbage collector
            rowsArray[rowNum-1] = null;
               
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

    public void close() throws SQLException {
    }

    public AbstractStatement getStatement() {
        return fbStatement;
    }
    public int getRowNum() {
        return rowNum;
    }
    public boolean isEmpty() {
        return isEmpty;
    }
    public boolean isBeforeFirst() {
        return isBeforeFirst;
    }
    public boolean isFirst() {
        return isFirst;
    }
    public boolean isLast() {
        return isLast;
    }
    public boolean isAfterLast() {
        return isAfterLast;
    }
}
