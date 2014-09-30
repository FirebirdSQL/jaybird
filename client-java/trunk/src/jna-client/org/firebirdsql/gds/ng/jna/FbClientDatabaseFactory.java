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
package org.firebirdsql.gds.ng.jna;

import com.sun.jna.Native;
import com.sun.jna.Platform;
import org.firebirdsql.gds.ng.FbDatabaseFactory;
import org.firebirdsql.gds.ng.IConnectionProperties;
import org.firebirdsql.jna.fbclient.FbClientLibrary;
import org.firebirdsql.jna.fbclient.WinFbClientLibrary;

import java.sql.SQLException;

/**
 * Implementation of {@link org.firebirdsql.gds.ng.FbDatabaseFactory} for establishing connection using the
 * Firebird native client library.
 * <p>
 * A separate factory is used for embedded (TODO: Add FbEmbeddedDatabaseFactory)
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class FbClientDatabaseFactory implements FbDatabaseFactory {

    @Override
    public JnaDatabase connect(IConnectionProperties connectionProperties) throws SQLException {
        final JnaConnection jnaConnection = new JnaConnection(getClientLibrary(), connectionProperties);
        try {
            return jnaConnection.identify();
        } catch (SQLException ex) {
            jnaConnection.disconnect();
            throw ex;
        }
    }

    protected FbClientLibrary getClientLibrary() {
        return ClientHolder.clientLibrary;
    }

    /**
     * Initialization-on-demand depending on classloading behavior specified in JLS 12.4
     */
    private static class ClientHolder {

        private static final FbClientLibrary clientLibrary = initClientLibrary();

        private static FbClientLibrary initClientLibrary() {
            if (Platform.isWindows()) {
                return (FbClientLibrary) Native.loadLibrary("fbclient", WinFbClientLibrary.class);
            } else {
                // TODO Validate if correct
                com.sun.jna.Library library = (com.sun.jna.Library) Native.loadLibrary("fbclient", FbClientLibrary.class);
                return (FbClientLibrary) Native.synchronizedLibrary(library);
            }
        }
    }
}
