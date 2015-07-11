/*
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
/*
 * The Original Code is the Firebird Java GDS implementation.
 *
 * The Initial Developer of the Original Code is Alejandro Alberola.
 * Portions created by Alejandro Alberola are Copyright (C) 2001
 * Boix i Oltra, S.L. All Rights Reserved.
 */

package org.firebirdsql.gds.impl.wire;

import org.firebirdsql.gds.IscDbHandle;
import org.firebirdsql.gds.impl.AbstractIscStmtHandle;
import org.firebirdsql.gds.impl.AbstractIscTrHandle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Describe class <code>isc_tr_handle_impl</code> here.
 *
 * @author <a href="mailto:alberola@users.sourceforge.net">Alejandro Alberola</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public final class isc_tr_handle_impl extends AbstractIscTrHandle {
    private int rtr_id;
    private isc_db_handle_impl rtr_rdb;
    private ArrayList blobs = new ArrayList();
    private HashSet stmts = new HashSet();

    private int state = NOTRANSACTION;

    public isc_tr_handle_impl() {
    }

    public IscDbHandle getDbHandle() {
        return rtr_rdb;
    }

    void setState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

    void setTransactionId(final int rtr_id)
    {
        this.rtr_id = rtr_id;
    }

    int getTransactionId()
    {
        return rtr_id;
    }

    void setDbHandle(final isc_db_handle_impl db)
    {
        this.rtr_rdb = db;
        rtr_rdb.addTransaction(this);
    }

    void unsetDbHandle()
    {
        rtr_rdb.removeTransaction(this);
        rtr_rdb = null;
    }

    void addBlob(final isc_blob_handle_impl blob) {
        blobs.add(blob);
    }

    void removeBlob(isc_blob_handle_impl blob) {
        blobs.remove(blob);
    }
	 
    public void registerStatementWithTransaction(AbstractIscStmtHandle stmt) {
        synchronized(stmts) {
            stmts.add(stmt);
        }
    }

    public void unregisterStatementFromTransaction(AbstractIscStmtHandle stmt) {
        synchronized(stmts) {
            stmts.remove(stmt);
        }
    }
    
    public void forgetResultSets() {
        synchronized(stmts) {
            for (Iterator iter = stmts.iterator(); iter.hasNext();) {
                AbstractIscStmtHandle stmt = (AbstractIscStmtHandle) iter.next();
                stmt.clearRows();
                stmt.unregisterTransaction();
            }
            
            stmts.clear();
        }
    }
    
    public int hashCode() {
        return rtr_id;
    }
    
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof isc_tr_handle_impl)) return false;
        isc_tr_handle_impl that = (isc_tr_handle_impl)obj;
        return this.rtr_id == that.rtr_id;
    }
}
