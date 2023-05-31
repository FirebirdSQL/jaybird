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
package org.firebirdsql.gds.ng;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.encodings.EncodingDefinition;
import org.firebirdsql.encodings.IEncodingFactory;
import org.firebirdsql.gds.ng.dbcrypt.DbCryptCallback;
import org.firebirdsql.gds.ng.dbcrypt.DbCryptCallbackSpi;
import org.firebirdsql.gds.ng.dbcrypt.simple.StaticValueDbCryptCallbackSpi;
import org.firebirdsql.jdbc.SQLStateConstants;
import org.firebirdsql.util.InternalApi;

import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.util.concurrent.locks.ReentrantLock;

import static org.firebirdsql.gds.JaybirdErrorCodes.jb_dbCryptCallbackInitError;

/**
 * Abstract class with common logic for connections.
 *
 * @param <T> Type of attach properties
 * @param <C> Type of connection handle
 * @author Mark Rotteveel
 * @since 3.0
 */
public abstract class AbstractConnection<T extends IAttachProperties<T>, C extends FbAttachment> {

    private static final DbCryptCallbackSpi DEFAULT_DB_CRYPT_CALLBACK_SPI = new StaticValueDbCryptCallbackSpi();

    private final ReentrantLock lock = new ReentrantLock();
    // cache method reference instead of creating a new one for each withLock() operation
    private final LockCloseable lockCloseable = lock::unlock;
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
                tempEncodingDefinition = encodingFactory.getEncodingDefinition("NONE", null);
            } else {
                throw new SQLNonTransientConnectionException(
                        String.format("No valid encoding definition for Firebird encoding %s and/or Java charset %s",
                                firebirdEncodingName, javaCharsetAlias),
                        SQLStateConstants.SQL_STATE_CONNECTION_ERROR);
            }
        }
        encodingDefinition = tempEncodingDefinition;
        this.encodingFactory = encodingFactory.withDefaultEncodingDefinition(encodingDefinition);
        // Overwrite with normalized values and specify missing values, eg if only charSet was specified, encoding will be set
        this.attachProperties.setEncoding(encodingDefinition.getFirebirdEncodingName());
        this.attachProperties.setCharSet(encodingDefinition.getJavaEncodingName());
    }

    /**
     * @see FbAttachment#withLock()
     */
    protected final LockCloseable withLock() {
        lock.lock();
        return lockCloseable;
    }

    /**
     * @see FbAttachment#isLockedByCurrentThread()
     */
    protected final boolean isLockedByCurrentThread() {
        return lock.isHeldByCurrentThread();
    }

    /**
     * Performs the connection identification phase of the protocol and returns the connection handle implementation
     * for the agreed protocol.
     *
     * @return Connection handle (ie {@link FbDatabase} or {@link FbService})
     * @throws SQLException For exceptions connecting
     */
    public abstract C identify() throws SQLException;

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

    /**
     * Creates an instance of {@link DbCryptCallback} for this connection.
     *
     * @return Database encryption callback.
     * @throws SQLException For errors initializing the callback
     */
    @InternalApi
    public final DbCryptCallback createDbCryptCallback() throws SQLException {
        // TODO Make plugin selectable from config
        try {
            final String dbCryptConfig = getAttachProperties().getDbCryptConfig();
            return DEFAULT_DB_CRYPT_CALLBACK_SPI.createDbCryptCallback(dbCryptConfig);
        } catch (RuntimeException e) {
            throw new FbExceptionBuilder()
                    .nonTransientConnectionException(jb_dbCryptCallbackInitError)
                    .cause(e)
                    .toSQLException();
        }
    }
}
