package org.firebirdsql.jdbc;

import org.firebirdsql.gds.*;
import org.firebirdsql.jgds.*;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

class FBCachedFetcher  implements FBFetcher {

    java.util.ArrayList rows;
    private FBStatement fbStatement;
    private FBResultSet rs;
    private int rowNum = 0;
    private boolean isEmpty = false;     
    private boolean isBeforeFirst = false;
    private boolean isFirst = false;
    private boolean isLast = false;
    private boolean isAfterLast = false;


    private final static Logger log = LoggerFactory.getLogger(FBCachedFetcher.class,false);
          
    FBCachedFetcher(FBConnection c, FBStatement fbStatement
    , isc_stmt_handle stmt_handle, FBResultSet rs) throws SQLException {
        isc_stmt_handle_impl stmt = (isc_stmt_handle_impl) stmt_handle;
        byte[][] localRow = null;
            
        this.fbStatement = fbStatement;
        this.rs = rs;
            
        isEmpty = false;
        isBeforeFirst = false;
        isFirst = false;
        isLast = false;
        isAfterLast = false;
        rows = new ArrayList();
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
        try {
            do {
                c.fetch(stmt);
                if (stmt.rows.size() > 0){
                    rows.addAll(stmt.rows);
                    stmt.rows.clear();
					 }
            } while (!stmt.allRowsFetched && (fbStatement.maxRows==0 || rows.size()<fbStatement.maxRows));
            //
            if (hasBlobs){
                for (int i=0;i< rows.size(); i++){
                    localRow = (byte[][]) rows.get(i);
                    //ugly blob caching workaround.
                    for (int j = 0; i < localRow.length; i++){                   
                        if (isBlob[i] && localRow[i] != null ) {
                            rs.row = localRow;						  
                            FBBlobField blob = (FBBlobField)FBField.createField(rs.xsqlvars[i], rs, i,false);
                            blob.setConnection(c);
                            localRow[i] = blob.getCachedObject();
                            rs.row = null;
                        }
                    }
                }
            }
            if (rows.size()==0)
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
        this.rows = rows;
        this.rs = rs;
            
        isEmpty = false;
        isBeforeFirst = false;
        isFirst = false;
        isLast = false;
        isAfterLast = false;
            
        if (rows.size()==0)
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
        if(rowNum == rows.size()) {
            rs.row = null;
            rowNum = 0;
            isAfterLast = true;
            return false;
        }
        else {
            rowNum++;
                
            if (rowNum == 1)
                isFirst = true;
            if (rowNum == rows.size())
                isLast = true;
            rs.row = (byte[][])rows.get(rowNum-1);
            // clean the rows element as it is used					 
            rows.set(rowNum-1,null);
                
            return true;
        }
    }

    public void close() throws SQLException {
    }

    public Statement getStatement() {
        return fbStatement;
    }
    public int getRowNum() {
        return rowNum;
    }
    public boolean getIsEmpty() {
        return isEmpty;
    }
    public boolean getIsBeforeFirst() {
        return isBeforeFirst;
    }
    public boolean getIsFirst() {
        return isFirst;
    }
    public boolean getIsLast() {
        return isLast;
    }
    public boolean getIsAfterLast() {
        return isAfterLast;
    }
}
