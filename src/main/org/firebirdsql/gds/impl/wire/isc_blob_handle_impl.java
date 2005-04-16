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

package org.firebirdsql.gds.impl.wire;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.IscBlobHandle;

/**
 * Describe class <code>isc_blob_handle_impl</code> here.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public final class isc_blob_handle_impl implements IscBlobHandle {

    private isc_db_handle_impl db;
    private isc_tr_handle_impl tr;
    private int rbl_id;
    private long blob_id;
//    private isc_blob_handle_impl next;
    private int rbl_flags;
    private int position;

    isc_blob_handle_impl() {};

    public isc_tr_handle_impl getTr() {
        return tr;
    }

    public void setTr(isc_tr_handle_impl value) {
        tr = value;
    }

    public isc_db_handle_impl getDb() {
        return db;
    }

    public void setDb(isc_db_handle_impl value) {
        db = value;
    }

    public long getBlobId() {
        return blob_id;
    }

    public void setBlobId(long value) {
        blob_id = value;
    }

    public int getRbl_id() {
        return rbl_id;
    }

    public void setRbl_id(int value) {
        rbl_id = value;
    }
/* not used
    public int getRbl_flags() {
        return rbl_flags;
    }
*/
    public void rbl_flagsAdd(int value) {
        rbl_flags |= value;
    }

    public void rbl_flagsRemove(int value) {
        rbl_flags &= ~value;
    }
    
    public int getPosition() {
        return position;
    }
    
    public void setPosition(int position) {
        this.position = position;
    }
    
    // only used in the tests
    public boolean isEof() {
        return (rbl_flags & ISCConstants.RBL_eof_pending) != 0;
    }
}
