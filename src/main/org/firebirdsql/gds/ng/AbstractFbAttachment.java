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
import org.firebirdsql.encodings.IEncodingFactory;
import org.firebirdsql.gds.impl.GDSServerVersion;
import org.firebirdsql.gds.impl.GDSServerVersionException;
import org.firebirdsql.gds.ng.dbcrypt.DbCryptCallback;
import org.firebirdsql.gds.ng.dbcrypt.DbCryptCallbackSpi;
import org.firebirdsql.gds.ng.dbcrypt.simple.StaticValueDbCryptCallbackSpi;
import org.firebirdsql.gds.ng.listeners.ExceptionListener;
import org.firebirdsql.gds.ng.listeners.ExceptionListenerDispatcher;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Objects.requireNonNull;
import static org.firebirdsql.gds.JaybirdErrorCodes.jb_dbCryptCallbackInitError;

/**
 * Common behavior for {@link AbstractFbService} and {@link AbstractFbDatabase}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public abstract class AbstractFbAttachment<T extends AbstractConnection<? extends IAttachProperties, ? extends FbAttachment>>
        implements FbAttachment {

    private static final Logger log = LoggerFactory.getLogger(AbstractFbAttachment.class);

    private final AtomicBoolean attached = new AtomicBoolean();
    private final Object syncObject = new SyncObject();
    protected final ExceptionListenerDispatcher exceptionListenerDispatcher = new ExceptionListenerDispatcher(this);
    protected final T connection;
    private final DatatypeCoder datatypeCoder;
    private GDSServerVersion serverVersion;
    private ServerVersionInformation serverVersionInformation;

    protected AbstractFbAttachment(T connection, DatatypeCoder datatypeCoder) {
        this.connection = requireNonNull(connection, "parameter connection should be non-null");
        this.datatypeCoder = requireNonNull(datatypeCoder, "parameter datatypeCoder should be non-null");
    }

    @Override
    public GDSServerVersion getServerVersion() {
        return serverVersion;
    }

    /**
     * Sets the Firebird version string.
     * <p>
     * This method should only be called by this instance.
     * </p>
     *
     * @param versionString
     *         Raw version string
     */
    protected final void setServerVersion(String versionString) {
        try {
            serverVersion = GDSServerVersion.parseRawVersion(versionString);
        } catch (GDSServerVersionException e) {
            log.error(String.format("Received unsupported server version \"%s\", replacing with dummy invalid version ",
                    versionString), e);
            serverVersion = GDSServerVersion.INVALID_VERSION;
        }
        serverVersionInformation = ServerVersionInformation.getForVersion(serverVersion);
    }


    protected ServerVersionInformation getServerVersionInformation() {
        return serverVersionInformation;
    }

    /**
     * Called when this attachment is attached.
     * <p>
     * Only this {@link org.firebirdsql.gds.ng.AbstractFbDatabase} instance should call this method.
     * </p>
     */
    protected final void setAttached() {
        attached.set(true);
    }

    @Override
    public boolean isAttached() {
        return attached.get();
    }

    /**
     * Called when this attachment is detached.
     * <p>
     * Only this {@link AbstractFbAttachment} instance should call this method.
     * </p>
     */
    protected final void setDetached() {
        attached.set(false);
    }

    @Override
    public final Object getSynchronizationObject() {
        return syncObject;
    }

    @Override
    public final IEncodingFactory getEncodingFactory() {
        return connection.getEncodingFactory();
    }

    @Override
    public final Encoding getEncoding() {
        return connection.getEncoding();
    }

    @Override
    public final DatatypeCoder getDatatypeCoder() {
        return datatypeCoder;
    }

    @Override
    public final void addExceptionListener(ExceptionListener listener) {
        exceptionListenerDispatcher.addListener(listener);
    }

    @Override
    public final void removeExceptionListener(ExceptionListener listener) {
        exceptionListenerDispatcher.removeListener(listener);
    }

    /**
     * Checks if the attachment is connected, and throws a {@link SQLException} if it isn't connected.
     */
    protected abstract void checkConnected() throws SQLException;

    /**
     * Performs {@link #close()} suppressing any exception.
     */
    protected final void safelyDetach() {
        try {
            close();
        } catch (Exception ex) {
            // ignore, but log
            log.debug("Exception on safely detach", ex);
        }
    }

    private static final DbCryptCallbackSpi DEFAULT_DB_CRYPT_CALLBACK_SPI = new StaticValueDbCryptCallbackSpi();

    /**
     * Creates an instance of {@link DbCryptCallback} for this attachment.
     *
     * @return Database encryption callback.
     * @throws SQLException For errors initializing the callback
     */
    protected final DbCryptCallback createDbCryptCallback() throws SQLException {
        // TODO Make plugin selectable from config
        try {
            final String dbCryptConfig = connection.getAttachProperties().getDbCryptConfig();
            return DEFAULT_DB_CRYPT_CALLBACK_SPI.createDbCryptCallback(dbCryptConfig);
        } catch (RuntimeException e) {
            throw new FbExceptionBuilder()
                    .nonTransientConnectionException(jb_dbCryptCallbackInitError)
                    .cause(e)
                    .toSQLException();
        }
    }
    
}
