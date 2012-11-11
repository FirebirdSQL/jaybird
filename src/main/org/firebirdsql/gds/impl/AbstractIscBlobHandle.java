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
package org.firebirdsql.gds.impl;

import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.IscBlobHandle;
import org.firebirdsql.gds.IscDbHandle;
import org.firebirdsql.gds.IscTrHandle;

/**
 * Abstract implementation of the {@link org.firebirdsql.gds.IscBlobHandle}
 * interface.
 */
public abstract class AbstractIscBlobHandle implements IscBlobHandle {

    private IscDbHandle db;
    private IscTrHandle tr;
    private int rbl_id;
    private long blob_id;
    private boolean isEndOfFile = false;

    public void addWarning(GDSException warning) {
        db.addWarning(warning);
    }

    public IscTrHandle getTr() {
        return tr;
    }

    public void setTr(IscTrHandle value) {
        tr = value;
    }

    public IscDbHandle getDb() {
        return db;
    }

    public void setDb(IscDbHandle value) {
        db = value;
    }

    public long getBlobId() {
        return blob_id;
    }

    public void setBlobId(long value) {
        blob_id = value;
    }

    public int getRblId() {
        return rbl_id;
    }

    public void setRblId(int value) {
        rbl_id = value;
    }
    
    public boolean isEof() {
        return isEndOfFile;
    }
    
    public void setEof() {
        isEndOfFile = true;
    }
}