/*
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
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.gds.ng.*;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Implementation of {@link org.firebirdsql.gds.ng.FbDatabaseFactory} for the wire protocol implementation.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public final class FbWireDatabaseFactory implements FbDatabaseFactory {

    private static final FbWireDatabaseFactory INSTANCE = new FbWireDatabaseFactory();

    @Override
    public FbWireDatabase connect(IConnectionProperties connectionProperties) throws SQLException {
        final WireDatabaseConnection connection = new WireDatabaseConnection(connectionProperties);
        return performConnect(connection);
    }

    @Override
    public FbService serviceConnect(IServiceProperties serviceProperties) throws SQLException {
        final WireServiceConnection connection = new WireServiceConnection(serviceProperties);
        return performConnect(connection);
    }

    private <T extends FbWireAttachment> T performConnect(WireConnection<?, T> connection) throws SQLException {
        try {
            connection.socketConnect();
            return connection.identify();
        } catch (SQLException ex) {
            try {
                connection.close();
            } catch (IOException ioex) {
                ex.setNextException(new SQLException(ioex.getMessage(), ioex));
            }
            throw ex;
        }
    }

    public static FbWireDatabaseFactory getInstance() {
        return INSTANCE;
    }
}
