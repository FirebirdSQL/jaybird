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
import java.util.Collections;
import java.util.List;

import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.encodings.IEncodingFactory;
import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.IscDbHandle;
import org.firebirdsql.gds.IscTrHandle;

/**
 * Abstract implementation of {@link org.firebirdsql.gds.IscDbHandle} interface.
 * This class defines additional information that can be obtained when 
 * connecting to the database. 
 */
public abstract class AbstractIscDbHandle implements IscDbHandle {
    
    private volatile boolean invalid;
    private int rdb_id;
    private final List<GDSException> rdb_warnings = Collections.synchronizedList(new ArrayList<GDSException>());
    private int dialect;
    private int protocol;
    private GDSServerVersion serverVersion;
    private int ODSMajorVersion;
    private int ODSMinorVersion;
    protected final Collection<IscTrHandle> rdb_transactions = Collections.synchronizedList(new ArrayList<IscTrHandle>());
    private IEncodingFactory encodingFactory = EncodingFactory.getDefaultInstance().withDefaultEncodingDefinition();
    
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
        this.serverVersion = GDSServerVersion.parseRawVersion(version);
    }

    public boolean isValid() {
        return !invalid;
    }

    public void setRdbId(int rdb_id) {
        checkValidity();
        this.rdb_id = rdb_id;
    }

    public int getRdbId() {
        checkValidity();
        return rdb_id;
    }

    public List<GDSException> getWarnings() {
        checkValidity();
        synchronized (rdb_warnings) {
            return new ArrayList<GDSException>(rdb_warnings);
        }
    }

    public void addWarning(GDSException warning) {
        checkValidity();
        rdb_warnings.add(warning);
    }

    public void clearWarnings() {
        checkValidity();
        rdb_warnings.clear();
    }

    public boolean hasTransactions() {
        checkValidity();
        return !rdb_transactions.isEmpty();
    }

    public Collection<IscTrHandle> getTransactions() {
        synchronized (rdb_transactions) {
            return new ArrayList<IscTrHandle>(rdb_transactions);
        }
    }

    public int getOpenTransactionCount() {
        checkValidity();
        return rdb_transactions.size();
    }

    public void addTransaction(IscTrHandle tr) {
        checkValidity();
        rdb_transactions.add(tr);
    }

    public void removeTransaction(IscTrHandle tr) {
        checkValidity();
        rdb_transactions.remove(tr);
    }

    public IEncodingFactory getEncodingFactory() {
        return encodingFactory;
    }

    public void setEncodingFactory(IEncodingFactory encodingFactory) {
        this.encodingFactory = encodingFactory;
    }
    
    protected final synchronized void invalidateHandle() {
        invalid = true;
    }
    
    protected void checkValidity() {
        if (invalid)
            throw new IllegalStateException("This database handle is invalid and cannot be used anymore.");
    }
}
