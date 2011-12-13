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
package org.firebirdsql.gds.impl.wire;

import java.io.IOException;
import java.net.Socket;

import org.firebirdsql.gds.impl.AbstractIscDbHandle;

/**
 * Describe class <code>isc_db_handle_impl</code> here.
 * 
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class isc_db_handle_impl extends AbstractIscDbHandle {

    private static final int DEFAULT_RESP_DATA = 65536;

    Socket socket;
    public XdrOutputStream out;
    public WireXdrInputStream in;
    private int resp_data_len;
    EventCoordinator eventCoordinator;

    public isc_db_handle_impl() {
        resp_data = new byte[DEFAULT_RESP_DATA];
    }

    protected void invalidate() throws IOException {

        if (invalid)
            return;
        
        // TODO: Shouldn't this synchronize on *this*?
        in.close();
        out.close();
        socket.close();

        in = null;
        out = null;
        socket = null;

        invalid = true;
    }

    void addTransaction(isc_tr_handle_impl tr) {
        checkValidity();
        rdb_transactions.add(tr);
    }

    void removeTransaction(isc_tr_handle_impl tr) {
        checkValidity();
        rdb_transactions.remove(tr);
    }

    public byte[] getResp_data_truncated() {
        byte[] dest = new byte[getResp_data_len()];
        System.arraycopy(getResp_data(), 0, dest, 0, dest.length);
        return dest;
    }

    public void setResp_data_len(int len) {
        this.resp_data_len = len;
    }

    public int getResp_data_len() {
        return this.resp_data_len;
    }
}
