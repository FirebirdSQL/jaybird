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
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.encodings.IEncodingFactory;
import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.impl.wire.XdrInputStream;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.ng.AbstractFbDatabase;
import org.firebirdsql.gds.ng.FbBlob;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.WarningMessageCallback;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Abstract class for operations common to all version of the wire protocol implementation.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public abstract class AbstractFbWireDatabase extends AbstractFbDatabase implements FbWireDatabase {

    private static final Logger log = LoggerFactory.getLogger(AbstractFbWireDatabase.class, false);

    protected final AtomicBoolean attached = new AtomicBoolean();
    protected final ProtocolDescriptor protocolDescriptor;
    protected final WireConnection connection;
    private final Object syncObject = new Object();

    /**
     * Creates an AbstractFbWireDatabase instance.
     *
     * @param connection
     *         A WireConnection with an established connection to the server.
     * @param descriptor
     *         The ProtocolDescriptor that created this connection (this is
     *         used for creating further dependent objects).
     */
    protected AbstractFbWireDatabase(WireConnection connection, ProtocolDescriptor descriptor) {
        if (connection == null) throw new IllegalArgumentException("parameter connection should be non-null");
        if (descriptor == null) throw new IllegalArgumentException("parameter descriptor should be non-null");
        this.connection = connection;
        protocolDescriptor = descriptor;
    }

    @Override
    public final Object getSynchronizationObject() {
        return syncObject;
    }

    @Override
    public final short getConnectionDialect() {
        return connection.getConnectionDialect();
    }

    @Override
    public final IEncodingFactory getEncodingFactory() {
        return connection.getEncodingFactory();
    }

    @Override
    public final Encoding getEncoding() {
        return connection.getEncoding();
    }

    /**
     * Gets the XdrInputStream.
     *
     * @return Instance of XdrInputStream
     * @throws SQLException
     *         If no connection is opened or when exceptions occur
     *         retrieving the InputStream
     */
    protected final XdrInputStream getXdrIn() throws SQLException {
        return getXdrStreamAccess().getXdrIn();
    }

    /**
     * Gets the XdrOutputStream.
     *
     * @return Instance of XdrOutputStream
     * @throws SQLException
     *         If no connection is opened or when exceptions occur
     *         retrieving the OutputStream
     */
    protected final XdrOutputStream getXdrOut() throws SQLException {
        return getXdrStreamAccess().getXdrOut();
    }

    @Override
    public final XdrStreamAccess getXdrStreamAccess() {
        return connection.getXdrStreamAccess();
    }

    @Override
    public final boolean isAttached() {
        return attached.get() && connection.isConnected();
    }

    @Override
    public FbBlob createBlobForOutput(FbTransaction transaction, BlobParameterBuffer blobParameterBuffer) throws SQLException {
        return protocolDescriptor.createOutputBlob(this, (FbWireTransaction) transaction, blobParameterBuffer);
    }

    @Override
    public FbBlob createBlobForInput(FbTransaction transaction, BlobParameterBuffer blobParameterBuffer, long blobId) throws SQLException {
        return protocolDescriptor.createInputBlob(this, (FbWireTransaction) transaction, blobParameterBuffer, blobId);
    }

    @Override
    public void consumePackets(int numberOfResponses, WarningMessageCallback warningCallback) {
        while (numberOfResponses > 0) {
            numberOfResponses--;
            try {
                readResponse(warningCallback);
            } catch (Exception e) {
                // TODO Wrap in SQLWarning and register on warning callback?
                // ignoring exceptions
                log.debug("Exception in consumePackets", e);
            }
        }
    }

    /**
     * Processes any deferred actions. Protocol versions that do not support deferred actions should simply do nothing.
     */
    protected abstract void processDeferredActions();

    /**
     * @param response
     *         Response to process
     * @throws java.sql.SQLException
     *         For errors returned from the server.
     */
    public void processResponse(Response response) throws SQLException {
        if (response instanceof GenericResponse) {
            GenericResponse genericResponse = (GenericResponse) response;
            SQLException exception = genericResponse.getException();
            if (exception != null && !(exception instanceof SQLWarning)) {
                throw exception;
            }
        }
    }

    /**
     * Checks if the response included a warning and signals that warning to the
     * WarningMessageCallback.
     *
     * @param response
     *         Response to process
     */
    public void processResponseWarnings(final Response response, WarningMessageCallback warningCallback) {
        if (warningCallback == null) {
            warningCallback = getDatabaseWarningCallback();
        }
        if (response instanceof GenericResponse) {
            GenericResponse genericResponse = (GenericResponse) response;
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
            SQLException exception = genericResponse.getException();
            if (exception != null && exception instanceof SQLWarning) {
                warningCallback.processWarning((SQLWarning) exception);
            }
        }
    }

    /**
     * Reads the next operation. Forwards call to {@link WireConnection#readNextOperation()}.
     *
     * @return next operation
     * @throws java.io.IOException
     */
    public int readNextOperation() throws IOException {
        synchronized (getSynchronizationObject()) {
            processDeferredActions();
            return connection.readNextOperation();
        }
    }
}
