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

import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.encodings.IEncodingFactory;
import org.firebirdsql.gds.ng.AbstractConnection;
import org.firebirdsql.gds.ng.IConnectionProperties;
import org.firebirdsql.jdbc.FBSQLException;
import org.firebirdsql.jna.fbclient.FbClientLibrary;

import java.sql.SQLException;

/**
 * Class handling the initial setup of the JNA connection.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public final class JnaConnection extends AbstractConnection {

    private FbClientLibrary clientLibrary;

    /**
     * Creates a JnaConnection (without establishing a connection to the server).
     *
     * @param clientLibrary
     *         Client library to use
     * @param connectionProperties
     *         Connection properties
     */
    public JnaConnection(FbClientLibrary clientLibrary, IConnectionProperties connectionProperties)
            throws SQLException {
        this(clientLibrary, connectionProperties, EncodingFactory.getDefaultInstance());
    }

    /**
     * Creates a JnaConnection (without establishing a connection to the server).
     *
     * @param clientLibrary
     *         Client library to use
     * @param connectionProperties
     *         Connection properties
     * @param encodingFactory
     *         Factory for encoding definitions
     */
    public JnaConnection(FbClientLibrary clientLibrary, IConnectionProperties connectionProperties,
            IEncodingFactory encodingFactory) throws SQLException {
        super(connectionProperties, encodingFactory);
        this.clientLibrary = clientLibrary;
    }

    public FbClientLibrary getClientLibrary() throws SQLException {
        if (clientLibrary == null) {
            throw new SQLException("Client library has been unloaded", FBSQLException.SQL_STATE_CONNECTION_ERROR);
        }
        return clientLibrary;
    }

    public void disconnect() {
        clientLibrary = null;
    }

    /**
     * Contrary to the description in the super class, this will simply return an unconnected instance.
     *
     * @return FbDatabase instance
     * @throws SQLException
     */
    @Override
    public JnaDatabase identify() throws SQLException {
        if (clientLibrary == null) {
            throw new SQLException("Client library has been unloaded", FBSQLException.SQL_STATE_CONNECTION_ERROR);
        }
        return new JnaDatabase(this);
    }
}
