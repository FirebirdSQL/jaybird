/*
 * $Id$
 *
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

import java.sql.SQLException;

import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.impl.AbstractIscStmtHandle;
import org.firebirdsql.gds.impl.GDSHelper;

/**
 * Statement fetcher for read-only case. It differs from updatable cursor case
 * by the cursor position after {@link #next()}call. This class changes cursor
 * position to point to the next row.
 */
class FBStatementFetcher implements FBFetcher {

    private boolean closed;
    private boolean wasFetched;

    protected final GDSHelper gdsHelper;
    protected final FBObjectListener.FetcherListener fetcherListener;

    protected final int maxRows;
    protected int fetchSize;

    protected final Synchronizable syncProvider;
    protected final AbstractIscStmtHandle stmt;

    private Object[] rowsArray;
    private int size;
    protected byte[][] _nextRow;
    private int rowNum = 0;
    private int rowPosition = 0;

    private boolean isEmpty;
    private boolean isBeforeFirst;
    private boolean isFirst;
    private boolean isLast;
    private boolean isAfterLast;

    FBStatementFetcher(GDSHelper gdsHelper, Synchronizable syncProvider, AbstractIscStmtHandle stmth,
            FBObjectListener.FetcherListener fetcherListener, int maxRows, int fetchSize) throws SQLException {
        this.gdsHelper = gdsHelper;
        this.stmt = stmth;
        this.syncProvider = syncProvider;
        this.fetcherListener = fetcherListener;
        this.maxRows = maxRows;
        this.fetchSize = fetchSize;

        synchronized (syncProvider.getSynchronizationObject()) {
            // stored procedures
            if (stmt.isAllRowsFetched()) {
                rowsArray = stmt.getRows();
                size = stmt.size();
            }
        }
    }

    protected byte[][] getNextRow() throws SQLException {
        if (!wasFetched) fetch();
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
        if (!wasFetched) fetch();

        setIsBeforeFirst(false);
        setIsFirst(false);
        setIsLast(false);
        setIsAfterLast(false);

        if (isEmpty())
            return false;
        else if (getNextRow() == null
                || (maxRows != 0 && getRowNum() == maxRows)) {
            setIsAfterLast(true);
            fetcherListener.allRowsFetched(this);
            setRowNum(0);
            return false;
        } else {
            fetcherListener.rowChanged(this, getNextRow());
            fetch();
            setRowNum(getRowNum() + 1);

            if (getRowNum() == 1) setIsFirst(true);

            if (getNextRow() == null
                    || (maxRows != 0 && getRowNum() == maxRows)) {
                setIsLast(true);
            }
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

    public void beforeFirst() throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void afterLast() throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void fetch() throws SQLException {
        synchronized (syncProvider.getSynchronizationObject()) {
            checkClosed();

            int maxRows = 0;

            if (this.maxRows != 0) maxRows = this.maxRows - rowNum;

            int fetchSize = this.fetchSize;
            if (fetchSize == 0) fetchSize = MAX_FETCH_ROWS;

            if (maxRows != 0 && fetchSize > maxRows) fetchSize = maxRows;

            if (!stmt.isAllRowsFetched()
                    && (rowsArray == null || size == rowPosition)) {
                try {
                    gdsHelper.fetch(stmt, fetchSize);
                    rowPosition = 0;
                    rowsArray = stmt.getRows();
                    size = stmt.size();
                } catch (GDSException ge) {
                    throw new FBSQLException(ge);
                }
            }

            if (rowsArray != null && size > rowPosition) {
                setNextRow((byte[][]) rowsArray[rowPosition]);
                // help the garbage collector
                rowsArray[rowPosition] = null;
                rowPosition++;
            } else
                setNextRow(null);
        }
    }

    public void close() throws SQLException {
        closed = true;
        try {
            gdsHelper.closeStatement(this.stmt, false);
        } catch(GDSException ex) {
            throw new FBSQLException(ex);
        } finally {
            fetcherListener.fetcherClosed(this);
        }
    }

    private void checkClosed() throws SQLException {
        if (closed) throw new FBSQLException("Result set is already closed.");
    }

    public int getRowNum() {
        return rowNum;
    }

    public void setRowNum(int rowNumValue) {
        this.rowNum = rowNumValue;
    }

    public boolean isEmpty() throws SQLException {
        if (!wasFetched) fetch();

        return isEmpty;
    }

    public void setIsEmpty(boolean isEmptyValue) {
        this.isEmpty = isEmptyValue;
    }

    public boolean isBeforeFirst() throws SQLException {
        if (!wasFetched) fetch();

        return isBeforeFirst;
    }

    public void setIsBeforeFirst(boolean isBeforeFirstValue) {
        this.isBeforeFirst = isBeforeFirstValue;
    }

    public boolean isFirst() throws SQLException {
        if (!wasFetched) fetch();

        return isFirst;
    }

    public void setIsFirst(boolean isFirstValue) {
        this.isFirst = isFirstValue;
    }

    public boolean isLast() throws SQLException {
        if (!wasFetched) fetch();

        return isLast;
    }

    public void setIsLast(boolean isLastValue) {
        this.isLast = isLastValue;
    }

    public boolean isAfterLast() throws SQLException {
        if (!wasFetched) fetch();

        return isAfterLast;
    }

    public void setIsAfterLast(boolean isAfterLastValue) {
        this.isAfterLast = isAfterLastValue;
    }

    public void deleteRow() throws SQLException {
        // empty
    }

    public void insertRow(byte[][] data) throws SQLException {
        // empty
    }

    public void updateRow(byte[][] data) throws SQLException {
        // empty
    }

    public void setFetchSize(int fetchSize){
        this.fetchSize = fetchSize;
    }

    public int getFetchSize(){
        return fetchSize;
    }

}
