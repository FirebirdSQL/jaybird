package org.firebirdsql.jdbc;

import org.firebirdsql.gds.*;
import org.firebirdsql.jgds.*;
import java.sql.SQLException;
import java.sql.Statement;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

class FBStatementFetcher implements FBFetcher {

    private FBConnection c;

    private FBStatement fbStatement;
    private FBResultSet rs;

    private isc_stmt_handle_impl stmt;
          
    private byte[][] nextRow;

    private final Logger log = LoggerFactory.getLogger(getClass(),false);

    private int rowNum = 0;
    private boolean isEmpty = false;     
    private boolean isBeforeFirst = false;
    private boolean isFirst = false;
    private boolean isLast = false;
    private boolean isAfterLast = false;
	 
    FBStatementFetcher(FBConnection c, FBStatement fbStatement, 
        isc_stmt_handle stmt, FBResultSet rs) throws SQLException 
    {
        this.c = c;
        this.fbStatement = fbStatement;
        this.stmt = (isc_stmt_handle_impl) stmt;
        this.rs = rs;
            
        c.registerStatement(fbStatement);
            
        isEmpty = false;
        isBeforeFirst = false;
        isFirst = false;
        isLast = false;
        isAfterLast = false;
            
        try {
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
//                System.arraycopy(nextRow,0,row,0,row.length);
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
        if (!stmt.allRowsFetched && stmt.rows.size() == 0){
            try {
                c.fetch(stmt);
				}
            catch (GDSException ge) {
                throw new FBSQLException(ge);
            }
        }
        if (stmt.rows.size() > 0) {
            nextRow = (byte[][]) stmt.rows.remove(0);
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
