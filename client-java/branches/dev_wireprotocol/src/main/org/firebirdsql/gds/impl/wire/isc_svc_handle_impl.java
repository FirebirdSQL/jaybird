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
package org.firebirdsql.gds.impl.wire;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.IscSvcHandle;

/**
 * Describe class <code>isc_svc_handle_impl</code> here.
 */
public final class isc_svc_handle_impl implements IscSvcHandle {
    
    private int handle;
    private List<GDSException> warnings = Collections.synchronizedList(new LinkedList<GDSException>());
    
    private boolean invalid;

    Socket socket;
    public XdrOutputStream out;
    public XdrInputStream in;
    private int resp_object;
    private long resp_blob_id;
    private byte[] resp_data;

    public isc_svc_handle_impl() {
        this.invalid = true;
    }

    public boolean isValid()
    {
        return !invalid;
    }

    void invalidate() throws IOException {
        in.close();
        out.close();
        socket.close();
        
        in = null;
        out = null;
        socket = null;

        invalid = true;
    }
    
    public void setHandle(int rdb_id) {
        this.handle = rdb_id;
        this.invalid = false;
    }

    public int getHandle() {
        checkValidity();
        return handle;
    }
    
    /**
     * @return Returns the resp_blob_id.
     */
    public long getResp_blob_id() {
        return resp_blob_id;
    }
    /**
     * @param resp_blob_id The resp_blob_id to set.
     */
    public void setResp_blob_id(long resp_blob_id) {
        this.resp_blob_id = resp_blob_id;
    }
    /**
     * @return Returns the resp_data.
     */
    public byte[] getResp_data() {
        return resp_data;
    }
    /**
     * @param resp_data The resp_data to set.
     */
    public void setResp_data(byte[] resp_data) {
        this.resp_data = resp_data;
    }
    /**
     * @return Returns the resp_object.
     */
    public int getResp_object() {
        return resp_object;
    }
    /**
     * @param resp_object The resp_object to set.
     */
    public void setResp_object(int resp_object) {
        this.resp_object = resp_object;
    }
    public List<GDSException> getWarnings() {
        checkValidity();
        synchronized(warnings) {
            return new ArrayList<GDSException>(warnings);
        }
    }
    
    public void addWarning(GDSException warning) {
        checkValidity();
        warnings.add(warning);
    }
    
    public void clearWarnings() {
        checkValidity();
        warnings.clear();
    }
    
    /* (non-Javadoc)
     * @see org.firebirdsql.gds.isc_svc_handle#isNotValid()
     */
    public boolean isNotValid() {
        return invalid;
    }
    
    private void checkValidity() {
        if (invalid)
            throw new IllegalStateException(
                "This database handle is invalid and cannot be used anymore.");
    }
}
