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

package org.firebirdsql.gds.impl.jni;

import org.firebirdsql.gds.IscSvcHandle;
import org.firebirdsql.gds.GDSException;

import java.util.ArrayList;
import java.util.List;

/**
 * ngds implementation for isc_svc_handle.
 */
class isc_svc_handle_impl implements IscSvcHandle {

    private List warnings = new ArrayList();
    private int handle = 0;

    public isc_svc_handle_impl() {
    }

    public List getWarnings() {
        synchronized (this.warnings) {
            return new ArrayList(this.warnings);
        }
    }

    public void clearWarnings() {
        synchronized (this.warnings) {
            this.warnings.clear();
        }
    }

    public synchronized boolean isValid() {
        return this.handle != 0;
    }

    public synchronized boolean isNotValid() {
        return !isValid();
    }

    void setHandle(int value) {
        this.handle = value;
    }

    int getHandle() {
        return this.handle;
    }

    void addWarning(GDSException warning) {
        synchronized (this.warnings) {
            this.warnings.add(warning);
        }
    }
}
