/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is the Firebird Java GDS implementation.
 *
 * The Initial Developer of the Original Code is Alejandro Alberola.
 * Portions created by Alejandro Alberola are Copyright (C) 2001
 * Boix i Oltra, S.L. All Rights Reserved.
 *
 * Contributor(s):
 *
 * Alternatively, the contents of this file may be used under the
 * terms of the GNU Lesser General Public License Version 2.1 or later
 * (the "LGPL"), in which case the provisions of the LGPL are applicable
 * instead of those above.  If you wish to allow use of your
 * version of this file only under the terms of the LGPL and not to
 * allow others to use your version of this file under the MPL,
 * indicate your decision by deleting the provisions above and
 * replace them with the notice and other provisions required by
 * the LGPL.  If you do not delete the provisions above, a recipient
 * may use your version of this file under either the MPL or the
 * LGPL.
 */

package org.firebirdsql.jgds;

import org.firebirdsql.gds.isc_db_handle;
import java.util.Collection;
import java.util.ArrayList;


public class isc_tr_handle_impl implements org.firebirdsql.gds.isc_tr_handle {
    int rtr_id;
    isc_db_handle_impl rtr_rdb;
    isc_tr_handle_impl rtr_next;
    Collection blobs = new ArrayList();
    //    isc_blob_handle_impl rbl_next;

    private int state = NOTRANSACTION;

    public isc_tr_handle_impl() {
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

    void addBlob(isc_blob_handle_impl blob) {
    blobs.add(blob);
    //        blob.next = rbl_next;
    //        rbl_next = blob;
    }

    void removeBlob(isc_blob_handle_impl blob) {
    blobs.remove(blob);
    }
}
