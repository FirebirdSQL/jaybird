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
 *
 * The Original Code is the Firebird Java GDS implementation.
 *
 * The Initial Developer of the Original Code is Alejandro Alberola.
 * Portions created by Alejandro Alberola are Copyright (C) 2001
 * Boix i Oltra, S.L. All Rights Reserved.
 */

package org.firebirdsql.jgds;

import java.util.*;
import java.net.*;

import javax.security.auth.Subject;

import org.firebirdsql.gds.GDSException;

/**
 * Describe class <code>isc_db_handle_impl</code> here.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class isc_db_handle_impl implements org.firebirdsql.gds.isc_db_handle {
    private int rdb_id;
    private Subject subject;
    private Collection rdb_transactions = new ArrayList();
    private List rdb_warnings = new ArrayList();
    
    private boolean invalid;
    
    private void checkValidity() {
        if (invalid)
            throw new IllegalStateException(
                "This database handle is invalid and cannot be used anymore.");
    }
    
    void invalidate() throws java.io.IOException {
        in.close();
        out.close();
        socket.close();
        
        in = null;
        out = null;
        socket = null;

        invalid = true;
    }
    
    /** @todo Implement statement handle tracking correctly */
    // Vector rdb_sql_requests = new Vector();
    
    Socket socket;
    XdrOutputStream out;
    XdrInputStream in;
    int op = -1;

    public isc_db_handle_impl() {
    }

    void setRdb_id(int rdb_id) {
        checkValidity();
        this.rdb_id = rdb_id;
    }

    public int getRdb_id() {
        checkValidity();
        return rdb_id;
    }

    void setSubject(Subject subject) {
        this.subject = subject;
    }

    public Subject getSubject() {
        return subject;
    }

    public boolean hasTransactions()
    {
        checkValidity();
        return !rdb_transactions.isEmpty();
    }
    
    int getOpenTransactionCount() {
        checkValidity();
        return rdb_transactions.size();
    }

    void addTransaction(isc_tr_handle_impl tr)
    {
        checkValidity();
        rdb_transactions.add(tr);
    }

    void removeTransaction(isc_tr_handle_impl tr)
    {
        checkValidity();
        rdb_transactions.remove(tr);
    }
    
    public List getWarnings() {
        checkValidity();
        synchronized(rdb_warnings) {
            return new ArrayList(rdb_warnings);
        }
    }
    
    public void addWarning(GDSException warning) {
        checkValidity();
        synchronized(rdb_warnings) {
            rdb_warnings.add(warning);
        }
    }
    
    public void clearWarnings() {
        checkValidity();
        synchronized(rdb_warnings) {
            rdb_warnings.clear();
        }
    }
}
