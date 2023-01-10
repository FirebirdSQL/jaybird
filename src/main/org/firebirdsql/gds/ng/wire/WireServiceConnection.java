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
import org.firebirdsql.gds.impl.DbAttachInfo;
import org.firebirdsql.gds.ng.IServiceProperties;
import org.firebirdsql.jaybird.props.PropertyConstants;

import java.sql.SQLException;

/**
 * Wire connection instance for connecting to a service.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public class WireServiceConnection extends WireConnection<IServiceProperties, FbWireService> {

    /**
     * Creates a WireServiceConnection (without establishing a connection to the  server) with the default
     * protocol collection.
     *
     * @param serviceProperties
     *         Service properties
     */
    public WireServiceConnection(IServiceProperties serviceProperties) throws SQLException {
        super(serviceProperties);
    }

    /**
     * Creates a WireServiceConnection (without establishing a connection to the server).
     *
     * @param serviceProperties
     *         Service properties
     * @param encodingFactory
     *         Factory for encoding definitions
     * @param protocols
     *         The collection of protocols to use for this connection.
     */
    public WireServiceConnection(IServiceProperties serviceProperties, IEncodingFactory encodingFactory,
            ProtocolCollection protocols) throws SQLException {
        super(serviceProperties, encodingFactory, protocols);
    }

    @Override
    protected String getCnctFile() {
        String expectedDb = attachProperties.getExpectedDb();
        if (expectedDb != null) {
            return expectedDb;
        }
        return super.getCnctFile();
    }

    @Override
    protected DbAttachInfo toDbAttachInfo(IServiceProperties attachProperties) throws SQLException {
        final DbAttachInfo initialDbAttachInfo = DbAttachInfo.of(attachProperties);

        DbAttachInfo dbAttachInfo;
        if (initialDbAttachInfo.hasServerName()) {
            dbAttachInfo = initialDbAttachInfo;
        } else if (initialDbAttachInfo.hasAttachObjectName()) {
            dbAttachInfo = DbAttachInfo.parseConnectString(initialDbAttachInfo.getAttachObjectName());
        } else {
            // fallback to localhost + service_mgr
            return new DbAttachInfo(PropertyConstants.DEFAULT_SERVER_NAME, initialDbAttachInfo.getPortNumber(),
                    PropertyConstants.DEFAULT_SERVICE_NAME);
        }

        if (!dbAttachInfo.hasServerName()) {
            // fallback to localhost (preserves backwards compatibility when serverName/host defaulted to localhost)
            dbAttachInfo = dbAttachInfo.withServerName(PropertyConstants.DEFAULT_SERVER_NAME);
        }

        if (!dbAttachInfo.hasAttachObjectName()) {
            // fallback to service_mgr
            dbAttachInfo = dbAttachInfo.withAttachObjectName(PropertyConstants.DEFAULT_SERVICE_NAME);
        }

        return dbAttachInfo;
    }

    @Override
    protected FbWireService createConnectionHandle(ProtocolDescriptor protocolDescriptor) {
        return protocolDescriptor.createService(this);
    }
}
