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
package org.firebirdsql.gds.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.security.auth.Subject;

import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.IscDbHandle;


/**
 * Abstract implementation of {@link org.firebirdsql.gds.IscDbHandle} interface.
 * This class defines additional information that can be obtained when 
 * connecting to the database. 
 */
public abstract class AbstractIscDbHandle implements IscDbHandle {
    
    // TODO: Consider to introduce generics on isc_tr_handle_impl (so methods can be moved up from isc_db_handle_impl)

    private volatile boolean invalid;
    private int rdb_id;
    private Subject subject;
    private List rdb_warnings = new ArrayList();
    private int dialect;
    private int protocol;
    private GDSServerVersion serverVersion;
    private int ODSMajorVersion;
    private int ODSMinorVersion;
    private int resp_object;
    private byte[] resp_data;
    protected Collection rdb_transactions = new ArrayList();
    private long resp_blob_id;
    
    protected AbstractIscDbHandle() {
    }
    
    protected AbstractIscDbHandle(byte[] defaultResp_data) {
        resp_data = defaultResp_data;
    }
    
    public int getDatabaseProductMajorVersion() {
        return serverVersion.getMajorVersion();
    }

    public int getDatabaseProductMinorVersion() {
        return serverVersion.getMinorVersion();
    }
    
    public String getDatabaseProductName() {
        return serverVersion.getServerName();
    }

    public String getDatabaseProductVersion() {
        return serverVersion.getFullVersion();
    }
    
    public int getDialect() {
        return dialect;
    }
    
    public void setDialect(int value) {
        dialect = value;
    }

    public int getProtocol() {
        return protocol;
    }
    
    public void setProtocol(int value) {
        protocol = value;
    }
    
    public int getODSMajorVersion() {
        return ODSMajorVersion;
    }
    
    public void setODSMajorVersion(int value) {
        ODSMajorVersion = value;
    }
    
    public int getODSMinorVersion() {
        return ODSMinorVersion;
    }
    
    public void setODSMinorVersion(int value) {
        ODSMinorVersion = value;
    }

    public String getVersion() {
        return serverVersion.toString();
    }

    public void setVersion(String version) throws GDSException {
        setVersion(new String[] { version });
    }
    
    public void setVersion(String... version) throws GDSException {
        this.serverVersion = GDSServerVersion.parseRawVersion(version);
    }

    public boolean isValid() {
        return !invalid;
    }

    protected void checkValidity() {
        if (invalid)
            throw new IllegalStateException("This database handle is invalid and cannot be used anymore.");
    }

    public void setRdbId(int rdb_id) {
        checkValidity();
        this.rdb_id = rdb_id;
    }

    public int getRdbId() {
        checkValidity();
        return rdb_id;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public Subject getSubject() {
        return subject;
    }

    public List getWarnings() {
        checkValidity();
        synchronized (rdb_warnings) {
            return new ArrayList(rdb_warnings);
        }
    }

    public void addWarning(GDSException warning) {
        checkValidity();
        synchronized (rdb_warnings) {
            rdb_warnings.add(warning);
        }
    }

    public void clearWarnings() {
        checkValidity();
        synchronized (rdb_warnings) {
            rdb_warnings.clear();
        }
    }

    public void setResp_object(int value) {
        resp_object = value;
    }

    public int getResp_object() {
        return resp_object;
    }

    public void setResp_data(byte[] value) {
        resp_data = value;
    }

    public byte[] getResp_data() {
        return resp_data;
    }

    public boolean hasTransactions() {
        checkValidity();
        return !rdb_transactions.isEmpty();
    }

    public Collection getTransactions() {
        return new ArrayList(rdb_transactions);
    }

    public int getOpenTransactionCount() {
        checkValidity();
        return rdb_transactions.size();
    }

    public void setResp_blob_id(long value) {
        resp_blob_id = value;
    }

    public long getResp_blob_id() {
        return resp_blob_id;
    }
    
    protected final synchronized void invalidateHandle() {
        invalid = true;
    }
   
}
