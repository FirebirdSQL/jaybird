// SPDX-FileCopyrightText: Copyright 2014-2015 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.gds.ng.*;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Implementation of {@link org.firebirdsql.gds.ng.FbDatabaseFactory} for the wire protocol implementation.
 *
 * @author Mark Rotteveel
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
