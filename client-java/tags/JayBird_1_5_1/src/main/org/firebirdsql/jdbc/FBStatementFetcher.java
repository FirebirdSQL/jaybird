/*
 * Firebird Open Source J2ee connector - jdbc driver
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

import java.sql.SQLException;

import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.isc_stmt_handle;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

/**
 * Statement fetcher for read-only case. It differs from updatable cursor case
 * by the cursor position after {@link #next()} call. This class changes cursor
 * position to point to the next row.
 */
class FBStatementFetcher implements FBFetcher {

    private boolean closed;
    
    private boolean wasFetched;
    
    private AbstractConnection c;

    protected AbstractStatement fbStatement;
    protected FBResultSet rs;

    private isc_stmt_handle stmt;
          
    private Object[] rowsArray;
    private int size;

    protected byte[][] _nextRow;

    private final static Logger log = LoggerFactory.getLogger(FBStatementFetcher.class,false);

    private int rowNum = 0;
    private int rowPosition = 0;
    private boolean isEmpty = false;     
    private boolean isBeforeFirst = false;
    private boolean isFirst = false;
    private boolean isLast = false;
    private boolean isAfterLast = false;
	 
    FBStatementFetcher(AbstractConnection c, AbstractStatement fbStatement, 
        isc_stmt_handle stmth, FBResultSet rs) throws SQLException 
    {
        
        Object syncObject = fbStatement.getSynchronizationObject();
        synchronized(syncObject) {
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
                
            // stored procedures
            if (stmt.getAllRowsFetched()){
                rowsArray = stmt.getRows();
                size = stmt.size();
            }
        }
    }

    protected byte[][] getNextRow() throws SQLException {
        if (!wasFetched)
            fetch();
        
        return _nextRow;
    }
    
    protected void setNextRow(byte[][] nextRow) {
        _nextRow = nextRow;
        
        if (!wasFetched) {
            wasFetched = true;
            
            if (_nextRow == null)
                isEmpty = true;
            else
                isBeforeFirst = true;
        }
    }
    
    public boolean next() throws SQLException {
        if (!wasFetched)
            fetch();
        
        setIsBeforeFirst(false);
        setIsFirst(false);
        setIsLast(false);
        setIsAfterLast(false);

        if (isEmpty())
            return false;
        else if (getNextRow() == null || (fbStatement.maxRows!=0 && getRowNum()==fbStatement.maxRows)){
            setIsAfterLast(true);
            setRowNum(0);
            return false;
        }
        else {
            try {
                rs.row = getNextRow();
                fetch();
                setRowNum(getRowNum() + 1);
                
                if(getRowNum() == 1)
                    setIsFirst(true);

                if((getNextRow()==null) || (fbStatement.maxRows!=0 && getRowNum() == fbStatement.maxRows))
                    setIsLast(true);

                return true;
            }
            catch (SQLException sqle) {
                throw sqle;
            }
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
    
    public void beforeFirst() throws SQLException {
        throw new FBDriverNotCapableException();
    }
    
    public void afterLast() throws SQLException {
        throw new FBDriverNotCapableException();
    }
    public void fetch() throws SQLException {
        
        Object syncObject = fbStatement.getSynchronizationObject();
        synchronized(syncObject) {
        
            checkClosed();
            
            int maxRows = 0;
            
            if (fbStatement.maxRows != 0)
                maxRows = fbStatement.maxRows - rowNum;
            
            int fetchSize = fbStatement.fetchSize;
            if (fetchSize == 0)
                fetchSize = MAX_FETCH_ROWS;
            
            if (maxRows != 0 && fetchSize > maxRows)
                fetchSize = maxRows;
    
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
                setNextRow((byte[][]) rowsArray[rowPosition]);
                // help the garbage collector
                rowsArray[rowPosition] = null;
                rowPosition++;
            }
            else
                setNextRow(null);
        }
    }
	 
    public void close() throws SQLException {
        Object syncObject = fbStatement.getSynchronizationObject();
        synchronized(syncObject) {
            try {
                fbStatement.releaseResultSet();
            } finally {
                closed = true;
            }
        }
    }
    
    private void checkClosed() throws SQLException {
        if (closed)
            throw new FBSQLException("Result set is already closed.");
    }
    
    public AbstractStatement getStatement() {
        return fbStatement;
    }
    public int getRowNum() {
        return rowNum;
    }

    public void setRowNum(int rowNumValue) {
        this.rowNum = rowNumValue;
    }
    public boolean isEmpty() throws SQLException {
        if (!wasFetched)
            fetch();
        
        return isEmpty;
    }

    public void setIsEmpty(boolean isEmptyValue) {
        this.isEmpty = isEmptyValue;
    }
    public boolean isBeforeFirst() throws SQLException {
        if (!wasFetched)
            fetch();
        
        return isBeforeFirst;
    }

    public void setIsBeforeFirst(boolean isBeforeFirstValue) {
        this.isBeforeFirst = isBeforeFirstValue;
    }
    public boolean isFirst() throws SQLException {
        if (!wasFetched)
            fetch();
        
        return isFirst;
    }

    public void setIsFirst(boolean isFirstValue) {
        this.isFirst = isFirstValue;
    }

    public boolean isLast() throws SQLException {
        if (!wasFetched)
            fetch();
        
        return isLast;
    }
    
    public void setIsLast(boolean isLastValue) {
        this.isLast = isLastValue;
    }
    
    public boolean isAfterLast() throws SQLException {
        
        if (!wasFetched)
            fetch();
        
        return isAfterLast;
    }

    public void setIsAfterLast(boolean isAfterLastValue) {
        this.isAfterLast = isAfterLastValue;
    }
    
    
}