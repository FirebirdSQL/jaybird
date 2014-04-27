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

package org.firebirdsql.gds.impl.jni;

import org.firebirdsql.gds.XSQLDA;
import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.impl.AbstractIscStmtHandle;
import org.firebirdsql.gds.impl.AbstractIscTrHandle;

/**
 * Describe class <code>isc_stmt_handle_impl</code> here.
 *
 * @author <a href="mailto:alberola@users.sourceforge.net">Alejandro Alberola</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public final class isc_stmt_handle_impl extends AbstractIscStmtHandle {
    private int rsr_id;
    private int rsr_id_ptr = 0;

    private isc_db_handle_impl rsr_rdb;
    private XSQLDA in_sqlda = null;
    private XSQLDA out_sqlda = null;
    private byte[][][] rows;
    private int size;
    private boolean allRowsFetched = false;
    private boolean isSingletonResult = false;
    private boolean hasOpenResultSet = false;

    private int statementType;
    private int insertCount;
    private int updateCount;
    private int deleteCount;
    private int selectCount; //????
    
    private AbstractIscTrHandle trHandle;

    public void addWarning(GDSException warning) {
        rsr_rdb.addWarning(warning);
    }

    public isc_stmt_handle_impl() {
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
        if (rows== null || rows.length<maxSize)
            rows = new byte[maxSize][][];
        size=0;
    }

    public void clearRows() {
        size = 0;
        if (rows != null)
            rows = null;
        allRowsFetched = false;
        hasOpenResultSet = false;
    }

    public void setStatementType(int value) {
        statementType = value;
    }

    public int getStatementType() {
        return statementType;
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

     public int getRsr_id_ptr() {
        return rsr_id_ptr;
    }

    public void setRsr_id_ptr(int rsr_id_ptr,int value) {
        setRsrId(value);
        this.rsr_id_ptr = rsr_id_ptr;
    }

    public isc_db_handle_impl getRsr_rdb() {
        return rsr_rdb;
    }

    public void setRsr_rdb(isc_db_handle_impl value) {
        rsr_rdb = value;
    }

    public boolean isValid() {
        return rsr_rdb != null && rsr_rdb.isValid();
    }
    
    public boolean hasOpenResultSet() {
        return hasOpenResultSet;
    }
    
    void notifyOpenResultSet() {
        hasOpenResultSet = true;
    }
    
    public int size() {
        return size;
    }

    public byte[][][] getRows() {
        return rows;
    }

    public void removeRows() {
        rows = null;
        size = 0;
    }

    public void addRow(byte[][] row) {
        rows[size++] = row;
    }
    
    public AbstractIscTrHandle getTransaction() {
        return trHandle;
    }

    public void registerTransaction(AbstractIscTrHandle trHandle) {
        this.trHandle = trHandle;
    }

    public void unregisterTransaction() {
        this.trHandle = null;
    }
}
