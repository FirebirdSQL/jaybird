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
/*
 * The Original Code is the Firebird Java GDS implementation.
 *
 * The Initial Developer of the Original Code is Alejandro Alberola.
 * Portions created by Alejandro Alberola are Copyright (C) 2001
 * Boix i Oltra, S.L. All Rights Reserved.
 */
package org.firebirdsql.gds.impl;

import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.IscDbHandle;
import org.firebirdsql.gds.IscStmtHandle;
import org.firebirdsql.gds.IscTrHandle;
import org.firebirdsql.gds.XSQLDA;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

/**
 * Abstract implementation of the {@link org.firebirdsql.gds.IscStmtHandle}
 * interface.
 */
public abstract class AbstractIscStmtHandle implements IscStmtHandle {
    
    private static final Logger log = LoggerFactory.getLogger(AbstractIscStmtHandle.class, false);
    
    private String executionPlan;
    private int statementType = IscStmtHandle.TYPE_UNKNOWN;
    private String statement;
    private int insertCount;
    private int updateCount;
    private int deleteCount;
    private int selectCount;
    private XSQLDA in_sqlda = null;
    private XSQLDA out_sqlda = null;
    private int rsr_id;
    private byte[][][] rows;
    private int size;
    private boolean allRowsFetched = false;
    private boolean isSingletonResult = false;
    private boolean hasOpenResultSet = false;
    private IscTrHandle trHandle;
    private IscDbHandle rsr_rdb;
    
    public String getStatementText() {
        return statement;
    }
    
    public void setStatementText(String statement) {
        this.statement = statement;
    }

    public String getExecutionPlan() {
        return executionPlan;
    }
    
    public void setExecutionPlan(String plan) {
        this.executionPlan = plan;
    }

    public int getStatementType() {
        return statementType;
    }
    
    public void setStatementType(int statementType) {
        this.statementType = statementType;
    }
    
    public void setInsertCount(int value) {
        insertCount = value;
    }

    public int getInsertCount() {
        return insertCount;
    }

    public void setUpdateCount(int value) {
        updateCount = value;
    }

    public int getUpdateCount() {
        return updateCount;
    }

    public void setDeleteCount(int value) {
        deleteCount = value;
    }

    public int getDeleteCount() {
        return deleteCount;
    }

    public void setSelectCount(int value) {
        selectCount = value;
    }

    public int getSelectCount() {
        return selectCount;
    }

    public XSQLDA getInSqlda() {
        return in_sqlda;
    }

    public XSQLDA getOutSqlda() {
        return out_sqlda;
    }

    public void setInSqlda(XSQLDA xsqlda) {
        in_sqlda = xsqlda;
    }

    public void setOutSqlda(XSQLDA xsqlda) {
        out_sqlda = xsqlda;
    }

    public void ensureCapacity(int maxSize) {
        if (rows == null || rows.length < maxSize)
            rows = new byte[maxSize][][];
        size = 0;
    }

    public void clearRows() {
        size = 0;
        if (rows != null)
            rows = null;
        allRowsFetched = false;
        hasOpenResultSet = false;
    }

    public boolean isAllRowsFetched() {
        return allRowsFetched;
    }

    public void setAllRowsFetched(boolean value) {
        allRowsFetched = value;
    }

    public boolean isSingletonResult() {
        return isSingletonResult;
    }

    public void setSingletonResult(boolean value) {
        isSingletonResult = value;
    }

    public int getRsrId() {
        return rsr_id;
    }

    public void setRsrId(int value) {
        rsr_id = value;
    }

    public boolean hasOpenResultSet() {
        return hasOpenResultSet;
    }

    public void notifyOpenResultSet() {
        hasOpenResultSet = true;
    }

    public int size() {
        return size;
    }

    public byte[][][] getRows() {
        // TODO Return rows up to size?
        return rows;
    }

    public void removeRows() {
        rows = null;
        size = 0;
    }

    public void addRow(byte[][] row) {
        rows[size++] = row;
    }

    public IscTrHandle getTransaction() {
        return trHandle;
    }

    public void registerTransaction(IscTrHandle trHandle) {
        this.trHandle = trHandle;
    }

    public void unregisterTransaction() {
        this.trHandle = null;
    }

    public void addWarning(GDSException warning) {
        // TODO: Store warnings on statement level? (currently only used from JNI-code)
        rsr_rdb.addWarning(warning);
    }

    public IscDbHandle getRsr_rdb() {
        return rsr_rdb;
    }

    public void setRsr_rdb(IscDbHandle value) {
        rsr_rdb = value;
    }

    public boolean isValid() {
        return rsr_rdb != null && rsr_rdb.isValid();
    }

    /**
     * Helper method to print the bytes of a row using hex representation.
     * @param row Row to print
     */
    public static void printRow(byte[][] row) {
        StringBuilder sb = new StringBuilder();
        sb.append('\t');
        
        if (row == null)
            sb.append("null");
        else {
            for (int i = 0; i < row.length; i++) {
                
                if (row[i] == null)
                    sb.append("null");
                else {
                    for (int j = 0; j < row[i].length; j++) {
                        String hexValue = Integer.toHexString(row[i][j] & 0xff);
                        if (hexValue.length() == 1)
                            sb.append(0);
                        
                        sb.append(hexValue);
                        if (j < row[i].length - 1)
                            sb.append(' ');
                    }
                }
                
                if (i < row.length - 1)
                    sb.append(", ");
            }
        }

        if (log != null)
            log.debug(sb.toString());
    }
}
