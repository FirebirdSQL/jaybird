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
package org.firebirdsql.gds.ng;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.encodings.EncodingDefinition;
import org.firebirdsql.encodings.IEncodingFactory;
import org.firebirdsql.jdbc.SQLStateConstants;

import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;

/**
 * Abstract class with common logic for connections.
 *
 * @param <T> Type of attach properties
 * @param <C> Type of connection handle
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public abstract class AbstractConnection<T extends IAttachProperties<T>, C extends FbAttachment> {

    protected final T attachProperties;
    private final EncodingDefinition encodingDefinition;
    private final IEncodingFactory encodingFactory;

    protected AbstractConnection(T attachProperties, IEncodingFactory encodingFactory) throws SQLException {
        this.attachProperties = attachProperties.asNewMutable();
        final String firebirdEncodingName = attachProperties.getEncoding();
        final String javaCharsetAlias = attachProperties.getCharSet();

        EncodingDefinition tempEncodingDefinition = encodingFactory.getEncodingDefinition(firebirdEncodingName, javaCharsetAlias);
        if (tempEncodingDefinition == null || tempEncodingDefinition.isInformationOnly()) {
            if (firebirdEncodingName == null && javaCharsetAlias == null) {
                // TODO Signal warning?
                // TODO Use the default encoding (and its matching Firebird encoding) instead of NONE
                tempEncodingDefinition = encodingFactory.getEncodingDefinition("NONE", null);
            } else {
                // TODO Don't throw exception if encoding/charSet is null (see also TODO inside EncodingFactory.getEncodingDefinition)
                throw new SQLNonTransientConnectionException(
                        String.format("No valid encoding definition for Firebird encoding %s and/or Java charset %s",
                                firebirdEncodingName, javaCharsetAlias),
                        SQLStateConstants.SQL_STATE_CONNECTION_ERROR);
            }
        }
        encodingDefinition = tempEncodingDefinition;
        this.encodingFactory = encodingFactory.withDefaultEncodingDefinition(encodingDefinition);
    }

    /**
     * Performs the connection identification phase of the protocol and returns the connection handle implementation
     * for the agreed protocol.
     *
     * @return Connection handle (ie {@link FbDatabase} or {@link FbService})
     * @throws SQLException
     */
    public abstract C identify() throws SQLException;

    public final String getServerName() {
        return attachProperties.getServerName();
    }

    public final int getPortNumber() {
        return attachProperties.getPortNumber();
    }

    public final String getAttachObjectName() {
        return attachProperties.getAttachObjectName();
    }

    /**
     * @return An immutable copy of the current attach properties.
     */
    public final T getAttachProperties() {
        return attachProperties.asImmutable();
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
}
