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

package org.firebirdsql.ngds;

import org.firebirdsql.gds.isc_db_handle;
import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.isc_stmt_handle;

import java.util.ArrayList;


/**
 * Describe class <code>isc_tr_handle_impl</code> here.
 *
 * @author <a href="mailto:alberola@users.sourceforge.net">Alejandro Alberola</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public final class isc_tr_handle_impl implements org.firebirdsql.gds.isc_tr_handle {
    private int rtr_id;
    private int rtr_id_ptr = 0;

    private isc_db_handle_impl rtr_rdb;
    private ArrayList blobs = new ArrayList();
    private ArrayList stmts = new ArrayList();

    private int state = NOTRANSACTION;

    public isc_tr_handle_impl() {
    }

    public void addWarning(GDSException warning) {
        rtr_rdb.addWarning(warning);
    }

    public isc_db_handle getDbHandle() {
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

    int getTransactionId() {
        return rtr_id;
    }

    void setTransactionIdPtr(final int rtr_id_ptr, int value) {
        setTransactionId(value);
        this.rtr_id_ptr = rtr_id_ptr;
    }

    int getTransactionIdPtr() {
        return rtr_id_ptr;
    }

    void setDbHandle(final isc_db_handle_impl db) {
        this.rtr_rdb = db;
        rtr_rdb.addTransaction(this);
    }

    void unsetDbHandle() {
        rtr_rdb.removeTransaction(this);
        rtr_rdb = null;
    }

    void addBlob(isc_blob_handle_impl blob) {
        blobs.add(blob);
    }

    void removeBlob(isc_blob_handle_impl blob) {
        blobs.remove(blob);
    }
	 
    public void registerStatementWithTransaction(isc_stmt_handle stmt) {
        synchronized(stmts) {
            stmts.add(stmt);
		}
    }

    public void forgetResultSets() {
        synchronized(stmts) {
		    for (int i=0; i< stmts.size(); i++)
                ((isc_stmt_handle)stmts.get(i)).clearRows();
            
            stmts.clear();
		}
    }
}
