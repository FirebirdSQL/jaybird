// SPDX-FileCopyrightText: Copyright 2015-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
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
            dbAttachInfo = DbAttachInfo.parseConnectString(initialDbAttachInfo.attachObjectName());
        } else {
            // fallback to localhost + service_mgr
            return new DbAttachInfo(PropertyConstants.DEFAULT_SERVER_NAME, initialDbAttachInfo.portNumber(),
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
