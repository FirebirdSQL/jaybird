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
package org.firebirdsql.gds.ng.jna;

import org.firebirdsql.encodings.EncodingDefinition;
import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.encodings.IEncodingFactory;
import org.firebirdsql.gds.ng.IConnectionProperties;
import org.firebirdsql.jna.fbclient.FbClientLibrary;

import java.sql.SQLException;

/**
 * Class handling the initial setup of the JNA database connection.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
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

    /**
     * Contrary to the description in the super class, this will simply return an unconnected instance.
     *
     * @return FbDatabase instance
     * @throws SQLException
     */
    @Override
    public JnaDatabase identify() throws SQLException {
        return new JnaDatabase(this);
    }
}
