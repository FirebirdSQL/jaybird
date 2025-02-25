/*
 SPDX-FileCopyrightText: Copyright 2001-2002 David Jencks
 SPDX-FileCopyrightText: Copyright 2002 Blas Rodriguez Somoza
 SPDX-FileCopyrightText: Copyright 2003-2005 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2012-2023 Mark Rotteveel
 SPDX-License-Identifier: LGPL-2.1-or-later
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
