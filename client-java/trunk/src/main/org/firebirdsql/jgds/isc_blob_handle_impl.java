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

package org.firebirdsql.jgds;

import org.firebirdsql.gds.GDS;
import org.firebirdsql.gds.isc_blob_handle;
import org.firebirdsql.gds.isc_db_handle;
import org.firebirdsql.gds.isc_tr_handle;

/**
 * Describe class <code>isc_blob_handle_impl</code> here.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public final class isc_blob_handle_impl implements isc_blob_handle {

    isc_db_handle_impl db;
    isc_tr_handle_impl tr;
    int rbl_id;
    long blob_id;
    isc_blob_handle_impl next;
    int rbl_flags;

    isc_blob_handle_impl() {};

    public long getBlobId() {
        return blob_id;
    }

     public void setBlobId(long blob_id) {
         this.blob_id = blob_id;
     }

    public boolean isEof() {
    return (rbl_flags & GDS.RBL_eof_pending) != 0;
    }

}
