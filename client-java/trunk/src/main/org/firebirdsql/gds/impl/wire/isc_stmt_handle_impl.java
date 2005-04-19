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

package org.firebirdsql.gds.impl.wire;

import org.firebirdsql.gds.XSQLDA;
import org.firebirdsql.gds.impl.AbstractIscStmtHandle;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

/**
 * Describe class <code>isc_stmt_handle_impl</code> here.
 *
 * @author <a href="mailto:alberola@users.sourceforge.net">Alejandro Alberola</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public final class isc_stmt_handle_impl extends AbstractIscStmtHandle {
    private static Logger log = LoggerFactory.getLogger(GDS_Impl.class, false);

    
    private int rsr_id;
    private isc_db_handle_impl rsr_rdb;
    private XSQLDA in_sqlda = null;
    private XSQLDA out_sqlda = null;
    private byte[][][] rows;
    private int size;
    private boolean allRowsFetched = false;
    private boolean isSingletonResult = false;

    private boolean hasOpenResultSet;
    
    private int statementType;
    private int insertCount;
    private int updateCount;
    private int deleteCount;
    private int selectCount; //????

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

    public int getRsr_id() {
        return rsr_id;
    }

    public void setRsr_id(int value) {
        rsr_id = value;
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
    
    public isc_db_handle_impl getRsr_rdb() {
        return rsr_rdb;
    }

    public void setRsr_rdb(isc_db_handle_impl value) {
        rsr_rdb = value;
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
        //if (log != null) printRow(row);
    }

    private void printRow(byte[][] row) {
        StringBuffer sb = new StringBuffer();
        sb.append("\t");
        
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
                            hexValue = "0" + hexValue;
                        
                        sb.append(hexValue);
                        if (j < row[i].length - 1)
                            sb.append(" ");
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
