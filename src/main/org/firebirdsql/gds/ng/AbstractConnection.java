// SPDX-FileCopyrightText: Copyright 2014-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.encodings.EncodingDefinition;
import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.encodings.IEncodingFactory;
import org.firebirdsql.gds.ng.dbcrypt.DbCryptCallback;
import org.firebirdsql.gds.ng.dbcrypt.DbCryptCallbackSpi;
import org.firebirdsql.gds.ng.dbcrypt.simple.StaticValueDbCryptCallbackSpi;
import org.firebirdsql.util.InternalApi;

import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;

import static org.firebirdsql.gds.JaybirdErrorCodes.jb_dbCryptCallbackInitError;
import static org.firebirdsql.gds.JaybirdErrorCodes.jb_invalidConnectionEncoding;

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
        encodingDefinition = resolveEncodingDefinition(attachProperties, encodingFactory);
        this.encodingFactory = encodingFactory.withDefaultEncodingDefinition(encodingDefinition);
        // Overwrite with normalized values and specify missing values, eg if only charSet was specified, encoding will be set
        this.attachProperties.setEncoding(encodingDefinition.getFirebirdEncodingName());
        this.attachProperties.setCharSet(encodingDefinition.getJavaEncodingName());
    }

    private static EncodingDefinition resolveEncodingDefinition(IAttachProperties<?> attachProperties,
            IEncodingFactory encodingFactory) throws SQLException {
        String fbEncodingName = attachProperties.getEncoding();
        String javaCharSet = attachProperties.getCharSet();
        // fallback to NONE if no encoding nor charset is provided
        fbEncodingName =
                fbEncodingName == null && javaCharSet == null ? EncodingFactory.ENCODING_NAME_NONE : fbEncodingName;
        EncodingDefinition encodingDefinition = encodingFactory.getEncodingDefinition(fbEncodingName, javaCharSet);
        if (encodingDefinition == null || encodingDefinition.isInformationOnly()) {
            throw FbExceptionBuilder.forNonTransientConnectionException(jb_invalidConnectionEncoding)
                    // report original value of encoding
                    .messageParameter(attachProperties.getEncoding(), javaCharSet)
                    .toSQLException();
        }
        return encodingDefinition;
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
        //noinspection DataFlowIssue : encoding is not null for the connection encoding
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
            throw FbExceptionBuilder.forNonTransientConnectionException(jb_dbCryptCallbackInitError)
                    .cause(e)
                    .toSQLException();
        }
    }
}
