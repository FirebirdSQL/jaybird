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
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.io.Serializable;
import java.sql.SQLException;

/**
 * The class {@code FBStandAloneConnectionManager} provides the default implementation of FirebirdConnectionManager for
 * standalone use. There is no pooling or other features.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @version 1.0
 */
public class FBStandAloneConnectionManager implements XcaConnectionManager, XcaConnectionEventListener, Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(FBStandAloneConnectionManager.class);

    FBStandAloneConnectionManager() {
    }

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
        try {
            ce.getSource().destroy(ce);
        } catch (SQLException e) {
            log.debug("Exception closing unmanaged connection: ", e);
        }
    }

    @Override
    public void connectionErrorOccurred(XcaConnectionEvent ce) {
        log.debug("ConnectionErrorOccurred, ", ce.getException());
        try {
            ce.getSource().destroy(ce);
        } catch (SQLException e) {
            log.debug("further problems destroying connection: ", e);
        }
    }
}
