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
package org.firebirdsql.gds.ng;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.encodings.EncodingDefinition;
import org.firebirdsql.encodings.IEncodingFactory;
import org.firebirdsql.jdbc.FBSQLException;

import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;

/**
 * Abstract class with common logic for connections.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public abstract class AbstractConnection {

    protected IConnectionProperties connectionProperties;
    protected EncodingDefinition encodingDefinition;
    protected IEncodingFactory encodingFactory;

    protected AbstractConnection(IConnectionProperties connectionProperties, IEncodingFactory encodingFactory) throws SQLException {
        this.connectionProperties = new FbConnectionProperties(connectionProperties);
        final String firebirdEncodingName = connectionProperties.getEncoding();
        final String javaCharsetAlias = connectionProperties.getCharSet();

        encodingDefinition = encodingFactory.getEncodingDefinition(firebirdEncodingName, javaCharsetAlias);
        if (encodingDefinition == null || encodingDefinition.isInformationOnly()) {
            if (firebirdEncodingName == null && javaCharsetAlias == null) {
                // TODO Signal warning?
                // TODO Use the default encoding (and its matching Firebird encoding) instead of NONE
                encodingDefinition = encodingFactory.getEncodingDefinition("NONE", null);
            } else {
                // TODO Don't throw exception if encoding/charSet is null (see also TODO inside EncodingFactory.getEncodingDefinition)
                throw new SQLNonTransientConnectionException(
                        String.format("No valid encoding definition for Firebird encoding %s and/or Java charset %s",
                                firebirdEncodingName, javaCharsetAlias),
                        FBSQLException.SQL_STATE_CONNECTION_ERROR);
            }
        }
        this.encodingFactory = encodingFactory.withDefaultEncodingDefinition(encodingDefinition);
    }

    /**
     * Performs the connection identification phase of the protocol and
     * returns the FbWireDatabase implementation for the agreed protocol.
     *
     * @return FbDatabase
     * @throws SQLException
     */
    public abstract FbDatabase identify() throws SQLException;

    public final String getServerName() {
        return connectionProperties.getServerName();
    }

    public final int getPortNumber() {
        return connectionProperties.getPortNumber();
    }

    public final String getDatabaseName() {
        return connectionProperties.getDatabaseName();
    }

    /**
     * @return An immutable copy of the current connection properties.
     */
    public final IConnectionProperties getConnectionProperties() {
        return connectionProperties.asImmutable();
    }

    public final EncodingDefinition getEncodingDefinition() {
        return this.encodingDefinition;
    }

    public final Encoding getEncoding() {
        return this.encodingDefinition.getEncoding();
    }

    public final IEncodingFactory getEncodingFactory() {
        return encodingFactory;
    }

    public final short getConnectionDialect() {
        return connectionProperties.getConnectionDialect();
    }
}
