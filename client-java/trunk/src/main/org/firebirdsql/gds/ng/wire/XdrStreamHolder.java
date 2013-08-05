/*
 * $Id$
 * 
 * Firebird Open Source J2EE Connector - JDBC Driver
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
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.gds.impl.wire.XdrInputStream;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;

import java.lang.ref.WeakReference;
import java.sql.SQLException;

/**
 * Provides weak referenced access to an {@link XdrStreamAccess} implementation.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.3
 */
public final class XdrStreamHolder implements XdrStreamAccess {

    private final WeakReference<XdrStreamAccess> xdrStreamAccessRef;

    /**
     * Creates a new instance of XdrStreamHolder.
     *
     * @param xdrStreamAccess
     *         XdrStreamAccess instance
     */
    public XdrStreamHolder(XdrStreamAccess xdrStreamAccess) {
        xdrStreamAccessRef = new WeakReference<XdrStreamAccess>(xdrStreamAccess);
    }

    @Override
    public XdrInputStream getXdrIn() throws SQLException {
        XdrStreamAccess streamAccess = xdrStreamAccessRef.get();
        if (streamAccess != null) {
            final XdrInputStream xdrIn = streamAccess.getXdrIn();
            if (xdrIn != null) return xdrIn;
        }
        throw new SQLException("XdrInputStream not available");
    }

    @Override
    public XdrOutputStream getXdrOut() throws SQLException {
        XdrStreamAccess streamAccess = xdrStreamAccessRef.get();
        if (streamAccess != null) {
            final XdrOutputStream xdrOut = streamAccess.getXdrOut();
            if (xdrOut != null) return xdrOut;
        }
        throw new SQLException("XdrOutputStream not available");
    }
}
