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
public final class isc_db_handle_impl extends AbstractIscDbHandle {

    private static final int DEFAULT_RESP_DATA = 65536;

    Socket socket;
    public XdrOutputStream out;
    public WireXdrInputStream in;
    EventCoordinator eventCoordinator;
    private int resp_object;
    private byte[] resp_data;
    private int resp_data_len;

    isc_db_handle_impl() {
        resp_data = new byte[DEFAULT_RESP_DATA];
    }

    // TODO Consider throwing GDSException instead?
    public void invalidate() throws IOException {
        if (!isValid())
            return;

        IOException ioeToThrow = null;
        
        // TODO: Shouldn't this synchronize on *this*?
        try {
            try {
                in.close();
            } catch (IOException e) {
                ioeToThrow = e;
            }
            try {
                out.close();
            } catch (IOException e) {
                if (ioeToThrow == null) {
                    ioeToThrow = e;
                }
            }
            try {
                socket.close();
            } catch (IOException e) {
                if (ioeToThrow == null) {
                    ioeToThrow = e;
                }
            }
        } finally {
            in = null;
            out = null;
            socket = null;
    
            invalidateHandle();
            if (ioeToThrow != null) {
                throw ioeToThrow;
            }
        }
    }
    
    // TODO merge getResp_data_truncated() and getResp_data() ?

    byte[] getResp_data_truncated() {
        byte[] dest = new byte[getResp_data_len()];
        System.arraycopy(getResp_data(), 0, dest, 0, dest.length);
        return dest;
    }
    
    byte[] getResp_data() {
        return resp_data;
    }

    void setResp_data_len(int len) {
        this.resp_data_len = len;
    }

    int getResp_data_len() {
        return this.resp_data_len;
    }

    void setResp_object(int value) {
        resp_object = value;
    }

    int getResp_object() {
        return resp_object;
    }

    void setResp_data(byte[] value) {
        resp_data = value;
    }
}
