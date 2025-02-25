// SPDX-FileCopyrightText: Copyright 2014-2024 Mark Rotteveel
// SPDX-FileCopyrightText: Copyright 2016 Adriano dos Santos Fernandes
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.jna;

import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.encodings.IEncodingFactory;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.impl.DbAttachInfo;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.IConnectionProperties;
import org.firebirdsql.jna.fbclient.FbClientLibrary;

import java.sql.SQLException;

/**
 * Class handling the initial setup of the JNA database connection.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public class JnaDatabaseConnection extends JnaConnection<IConnectionProperties, JnaDatabase>  {

    /**
     * Creates a JnaDatabaseConnection (without establishing a connection to the server).
     *
     * @param clientLibrary
     *         Client library to use
     * @param connectionProperties
     *         Connection properties
     */
    public JnaDatabaseConnection(FbClientLibrary clientLibrary, IConnectionProperties connectionProperties)
            throws SQLException {
        this(clientLibrary, connectionProperties, EncodingFactory.getPlatformDefault());
    }

    /**
     * Creates a JnaDatabaseConnection (without establishing a connection to the server).
     *
     * @param clientLibrary
     *         Client library to use
     * @param connectionProperties
     *         Connection properties
     * @param encodingFactory
     *         Factory for encoding definitions
     */
    public JnaDatabaseConnection(FbClientLibrary clientLibrary, IConnectionProperties connectionProperties,
            IEncodingFactory encodingFactory) throws SQLException {
        super(clientLibrary, connectionProperties, encodingFactory);
    }

    @Override
    protected String createAttachUrl(DbAttachInfo dbAttachInfo, IConnectionProperties connectionProperties)
            throws SQLException {
        if (!dbAttachInfo.hasAttachObjectName()) {
            throw FbExceptionBuilder.forNonTransientConnectionException(JaybirdErrorCodes.jb_invalidConnectionString)
                    // Using original attach object name as that may well be non-null even if it is null in dbAttachInfo
                    .messageParameter(connectionProperties.getAttachObjectName(),
                            "null or empty database name in connection string")
                    .toSQLException();
        }
        return toAttachUrl(dbAttachInfo);
    }

    /**
     * Contrary to the description in the super class, this will simply return an unconnected instance.
     *
     * @return FbDatabase instance
     */
    @Override
    public JnaDatabase identify() throws SQLException {
        return new JnaDatabase(this);
    }
}
