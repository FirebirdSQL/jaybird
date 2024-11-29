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
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.encodings.IEncodingFactory;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.impl.DbAttachInfo;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.IConnectionProperties;
import org.firebirdsql.jaybird.props.PropertyConstants;

import java.sql.SQLException;

/**
 * Wire connection instance for connecting to a database
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public final class WireDatabaseConnection extends WireConnection<IConnectionProperties, FbWireDatabase> {

    /**
     * Creates a WireDatabaseConnection (without establishing a connection to the server) with the default
     * protocol collection.
     *
     * @param connectionProperties
     *         Connection properties
     */
    public WireDatabaseConnection(IConnectionProperties connectionProperties) throws SQLException {
        super(connectionProperties);
    }

    /**
     * Creates a WireDatabaseConnection (without establishing a connection to the server).
     *
     * @param connectionProperties
     *         Connection properties
     * @param encodingFactory
     *         Factory for encoding definitions
     * @param protocols
     *         The collection of protocols to use for this connection.
     */
    public WireDatabaseConnection(IConnectionProperties connectionProperties, IEncodingFactory encodingFactory,
            ProtocolCollection protocols) throws SQLException {
        super(connectionProperties, encodingFactory, protocols);
    }

    @Override
    protected DbAttachInfo toDbAttachInfo(IConnectionProperties attachProperties) throws SQLException {
        final DbAttachInfo initialDbAttachInfo = DbAttachInfo.of(attachProperties);

        DbAttachInfo dbAttachInfo = initialDbAttachInfo.hasServerName()
                ? initialDbAttachInfo
                : DbAttachInfo.parseConnectString(initialDbAttachInfo.attachObjectName());

        if (!dbAttachInfo.hasServerName()) {
            // fallback to localhost (preserves backwards compatibility when serverName/host defaulted to localhost)
            dbAttachInfo = dbAttachInfo.withServerName(PropertyConstants.DEFAULT_SERVER_NAME);
        }

        if (!dbAttachInfo.hasAttachObjectName()) {
            throw FbExceptionBuilder.forNonTransientConnectionException(JaybirdErrorCodes.jb_invalidConnectionString)
                    .messageParameter(initialDbAttachInfo.attachObjectName(),
                            "null or empty database name in connection string")
                    .toSQLException();
        }

        return dbAttachInfo;
    }

    @Override
    protected FbWireDatabase createConnectionHandle(ProtocolDescriptor protocolDescriptor) {
        return protocolDescriptor.createDatabase(this);
    }
}
