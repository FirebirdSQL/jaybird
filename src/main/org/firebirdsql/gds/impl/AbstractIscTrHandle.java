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

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.IscBlobHandle;
import org.firebirdsql.gds.IscDbHandle;
import org.firebirdsql.gds.IscStmtHandle;
import org.firebirdsql.gds.IscTrHandle;

/**
 * Abstract implementation of the {@link org.firebirdsql.gds.IscTrHandle} 
 * interface.
 * 
 * @author <a href="mailto:alberola@users.sourceforge.net">Alejandro Alberola</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 */
public abstract class AbstractIscTrHandle implements IscTrHandle {

    private final List<IscBlobHandle> blobs = Collections.synchronizedList(new LinkedList<IscBlobHandle>());
    private final Set<IscStmtHandle> stmts = Collections.synchronizedSet(new HashSet<IscStmtHandle>());
    private IscDbHandle rtr_rdb;
    private int state = NOTRANSACTION;
    private int rtr_id;

    public void forgetResultSets() {
        synchronized(stmts) {
            for (Iterator<IscStmtHandle> iter = stmts.iterator(); iter.hasNext();) {
                AbstractIscStmtHandle stmt = (AbstractIscStmtHandle) iter.next();
                stmt.clearRows();
            }
            
            stmts.clear();
        }
    }

    public void addBlob(IscBlobHandle blob) {
        blobs.add(blob);
    }

    public void removeBlob(IscBlobHandle blob) {
        blobs.remove(blob);
    }

    public void registerStatementWithTransaction(IscStmtHandle stmt) {
        stmts.add(stmt);
    }

    public void unregisterStatementFromTransaction(IscStmtHandle stmt) {
        stmts.remove(stmt);
    }

    public void addWarning(GDSException warning) {
        rtr_rdb.addWarning(warning);
    }

    public IscDbHandle getDbHandle() {
        return rtr_rdb;
    }

    public void setDbHandle(final IscDbHandle db) {
        this.rtr_rdb = db;
        rtr_rdb.addTransaction(this);
    }

    public void unsetDbHandle() {
        rtr_rdb.removeTransaction(this);
        rtr_rdb = null;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public void setTransactionId(final int rtr_id) {
        this.rtr_id = rtr_id;
    }

    public int getTransactionId() {
        return rtr_id;
    }
    
    @Override
    public int hashCode() {
        return rtr_id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof AbstractIscTrHandle))
            return false;
        AbstractIscTrHandle that = (AbstractIscTrHandle) obj;
        return this.rtr_id == that.rtr_id;
    }
}
