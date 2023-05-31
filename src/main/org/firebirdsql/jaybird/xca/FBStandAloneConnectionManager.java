/*
 * Firebird Open Source JDBC Driver
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
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.jaybird.xca;

import org.firebirdsql.jdbc.FirebirdConnection;

import java.io.Serial;
import java.io.Serializable;
import java.sql.SQLException;

/**
 * The class {@code FBStandAloneConnectionManager} provides the default implementation of FirebirdConnectionManager for
 * standalone use. There is no pooling or other features.
 *
 * @author David Jencks
 * @author Mark Rotteveel
 */
final class FBStandAloneConnectionManager implements XcaConnectionManager, XcaConnectionEventListener, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final System.Logger log = System.getLogger(FBStandAloneConnectionManager.class.getName());

    @Override
    public FirebirdConnection allocateConnection(FBManagedConnectionFactory mcf, FBConnectionRequestInfo cxRequestInfo)
            throws SQLException {
        FBManagedConnection mc = mcf.createManagedConnection(cxRequestInfo);
        mc.setManagedEnvironment(false);
        mc.addConnectionEventListener(this);
        return mc.getConnection();
    }

    @Override
    public void connectionClosed(XcaConnectionEvent ce) {
        destroyConnection(ce);
    }

    @Override
    public void connectionErrorOccurred(XcaConnectionEvent ce) {
        log.log(System.Logger.Level.TRACE, "ConnectionErrorOccurred", ce.getException());
        destroyConnection(ce);
    }

    private void destroyConnection(XcaConnectionEvent ce) {
        FBManagedConnection mc = ce.getSource();
        try {
            mc.destroy(ce);
        } catch (SQLException e) {
            log.log(System.Logger.Level.WARNING, "Exception closing unmanaged connection", e);
        } finally {
            mc.removeConnectionEventListener(this);
        }
    }
}
