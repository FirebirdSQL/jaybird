package org.firebirdsql.jdbc;

import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.isc_stmt_handle;
import org.firebirdsql.jgds.isc_stmt_handle_impl;
import java.sql.SQLException;
import java.sql.Statement;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

class FBStatementFetcher implements FBFetcher {

    private FBConnection c;

    private FBStatement fbStatement;
    private FBResultSet rs;

    private isc_stmt_handle stmt;
          
    private Object[] rowsArray;
    private int size;
    private byte[][] nextRow;

    private final static Logger log = LoggerFactory.getLogger(FBStatementFetcher.class,false);

    private int rowNum = 0;
    private int rowPosition = 0;
    private boolean isEmpty = false;     
    private boolean isBeforeFirst = false;
    private boolean isFirst = false;
    private boolean isLast = false;
    private boolean isAfterLast = false;
	 
    FBStatementFetcher(FBConnection c, FBStatement fbStatement, 
        isc_stmt_handle stmth, FBResultSet rs) throws SQLException 
    {
        this.c = c;
        this.fbStatement = fbStatement;
        this.stmt = stmth;
        this.rs = rs;
            
        c.registerStatement(fbStatement);
            
        isEmpty = false;
        isBeforeFirst = false;
        isFirst = false;
        isLast = false;
        isAfterLast = false;
            
        try {
            // stored procedures
            if (stmt.getAllRowsFetched()){
                rowsArray = stmt.getRows();
                size = stmt.size();
            }
            fetch();
            if (nextRow==null)
                isEmpty = true;
            else 
                isBeforeFirst = true;
        }
        catch (SQLException sqle) {
            throw sqle;
        }
    }

    public boolean next() throws SQLException {
        isBeforeFirst = false;
        isFirst = false;
        isLast = false;
        isAfterLast = false;
                
        if (log!=null) log.debug("FBResultSet next - FBStatementFetcher");
                    
        if (isEmpty)
            return false;
        else if (nextRow == null || (fbStatement.maxRows!=0 && rowNum==fbStatement.maxRows)){
            isAfterLast = true;
            rowNum=0;
            return false;
        }
        else {
            try {
                rs.row = nextRow;
                fetch();
                rowNum++;
                    
                if(rowNum==1)
                    isFirst=true;
                    
                if((nextRow==null) || (fbStatement.maxRows!=0 && rowNum==fbStatement.maxRows))
                    isLast = true;
                        
                return true;
            }
            catch (SQLException sqle) {
                throw sqle;
            }
        }
    }

    public void fetch() throws SQLException {
        int maxRows = 0;
        if (fbStatement.maxRows != 0)
            maxRows = fbStatement.maxRows - rowNum;
        int fetchSize = fbStatement.fetchSize;
        if (fetchSize == 0)
            fetchSize = MAX_FETCH_ROWS;				
        if (maxRows != 0 && fetchSize > maxRows)
            fetchSize = maxRows;
        //
        if (!stmt.getAllRowsFetched() && (rowsArray == null || size == rowPosition)){
            try {
                c.fetch(stmt, fetchSize);
                rowPosition = 0;
					 rowsArray = stmt.getRows();
					 size = stmt.size();
            }
            catch (GDSException ge) {
                throw new FBSQLException(ge);
            }
        }
        if (rowsArray!=null && size > rowPosition) {
            nextRow = (byte[][]) rowsArray[rowPosition];
            // help the garbage collector
            rowsArray[rowPosition] = null;
            rowPosition++;
        }
        else
            nextRow = null;
    }
	 
    public void close() throws SQLException {
        fbStatement.closeResultSet();
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
