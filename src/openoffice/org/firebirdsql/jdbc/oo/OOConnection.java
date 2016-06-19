/*
 * $Id$
 *
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
package org.firebirdsql.jdbc.oo;

import org.firebirdsql.jca.FBManagedConnection;
import org.firebirdsql.jdbc.FBConnection;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;

public class OOConnection extends FBConnection {

    private static final Logger log = LoggerFactory.getLogger(OOConnection.class);

    private OODatabaseMetaData metaData;

    public OOConnection(FBManagedConnection mc) {
        super(mc);
        try {
            super.setHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT);
        } catch (SQLException e) {
            // ignore
            log.debug("Unexpected exception setting holdability", e);
        }
    }

    @Override
    public void setHoldability(int holdability) {
        if (holdability == ResultSet.HOLD_CURSORS_OVER_COMMIT) return;

        final String message = "Holdability not modified. OpenOffice/LibreOffice compatibility always uses HOLD_CURSORS_OVER_COMMIT";
        log.debug(message);
        addWarning(new SQLWarning(message));
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        synchronized (getSynchronizationObject()) {
            if (metaData == null) metaData = new OODatabaseMetaData(this);
            return metaData;
        }
    }

}
