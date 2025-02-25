// SPDX-FileCopyrightText: Copyright 2014-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.jna;

import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.encodings.IEncodingFactory;
import org.firebirdsql.gds.impl.DbAttachInfo;
import org.firebirdsql.gds.ng.IServiceProperties;
import org.firebirdsql.jaybird.props.PropertyConstants;
import org.firebirdsql.jna.fbclient.FbClientLibrary;

import java.sql.SQLException;

/**
 * Class handling the initial setup of the JNA service connection.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public final class JnaServiceConnection  extends JnaConnection<IServiceProperties, JnaService>  {

    /**
     * Creates a JnaServiceConnection (without establishing a connection to the server).
     *
     * @param clientLibrary
     *         Client library to use
     * @param connectionProperties
     *         Connection properties
     */
    public JnaServiceConnection(FbClientLibrary clientLibrary, IServiceProperties connectionProperties)
            throws SQLException {
        this(clientLibrary, connectionProperties, EncodingFactory.getPlatformDefault());
    }

    /**
     * Creates a JnaServiceConnection (without establishing a connection to the server).
     *
     * @param clientLibrary
     *         Client library to use
     * @param connectionProperties
     *         Connection properties
     * @param encodingFactory
     *         Factory for encoding definitions
     */
    public JnaServiceConnection(FbClientLibrary clientLibrary, IServiceProperties connectionProperties,
            IEncodingFactory encodingFactory) throws SQLException {
        super(clientLibrary, connectionProperties, encodingFactory);
    }

    @Override
    protected String createAttachUrl(DbAttachInfo dbAttachInfo, IServiceProperties attachProperties) {
        if (!dbAttachInfo.hasAttachObjectName()) {
            // fallback to service_mgr
            dbAttachInfo = dbAttachInfo.withAttachObjectName(PropertyConstants.DEFAULT_SERVICE_NAME);
        }
        return toAttachUrl(dbAttachInfo);
    }

    /**
     * Contrary to the description in the super class, this will simply return an unconnected instance.
     *
     * @return FbDatabase instance
     */
    @Override
    public JnaService identify() throws SQLException {
        return new JnaService(this);
    }
}
