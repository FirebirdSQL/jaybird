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
    Vector rdb_sql_requests = new Vector();
    Socket socket;
    XdrOutputStream out;
    XdrInputStream in;
    int op = -1;

    public isc_db_handle_impl() {
    }

    void setRdb_id(int rdb_id) {
        this.rdb_id = rdb_id;
    }

    public int getRdb_id() {
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
        return !rdb_transactions.isEmpty();
    }

    void addTransaction(isc_tr_handle_impl tr)
    {
        rdb_transactions.add(tr);
    }

    void removeTransaction(isc_tr_handle_impl tr)
    {
        rdb_transactions.remove(tr);
    }
}
