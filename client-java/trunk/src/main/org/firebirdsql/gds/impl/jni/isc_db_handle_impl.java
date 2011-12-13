/*
 * $Id$
 * 
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
package org.firebirdsql.gds.impl.jni;

import org.firebirdsql.gds.impl.AbstractIscDbHandle;

/**
 * Describe class <code>isc_db_handle_impl</code> here.
 * 
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public final class isc_db_handle_impl extends AbstractIscDbHandle {

    private int rdb_id_ptr;

    public isc_db_handle_impl() {
    }

    protected void invalidate() {
        invalid = true;
    }

    void setRdb_id_ptr(int rdb_id_ptr, int value) {
        setRdbId(value);
        this.rdb_id_ptr = rdb_id_ptr;
    }

    public int getRdb_id_ptr() {
        return rdb_id_ptr;
    }

    void addTransaction(isc_tr_handle_impl tr) {
        checkValidity();
        rdb_transactions.add(tr);
    }

    void removeTransaction(isc_tr_handle_impl tr) {
        checkValidity();
        rdb_transactions.remove(tr);
    }
}
