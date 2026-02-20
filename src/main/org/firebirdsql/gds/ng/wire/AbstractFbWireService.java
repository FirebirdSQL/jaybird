// SPDX-FileCopyrightText: Copyright 2015-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.ServiceParameterBuffer;
import org.firebirdsql.gds.ServiceRequestBuffer;
import org.firebirdsql.gds.impl.wire.XdrInputStream;
import org.firebirdsql.gds.ng.AbstractFbService;
import org.firebirdsql.gds.ng.DefaultDatatypeCoder;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.gds.ng.WarningMessageCallback;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.sql.SQLException;

import static java.util.Objects.requireNonNull;

/**
 * Abstract service implementation for the wire protocol.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public abstract class AbstractFbWireService extends AbstractFbService<WireServiceConnection>
        implements FbWireService {

    protected final ProtocolDescriptor protocolDescriptor;
    protected final FbWireOperations wireOperations;

    /**
     * Creates an AbstractFbWireDatabase instance.
     *
     * @param connection
     *         A WireConnection with an established connection to the server.
     * @param descriptor
     *         The ProtocolDescriptor that created this connection (this is
     *         used for creating further dependent objects).
     */
    protected AbstractFbWireService(WireServiceConnection connection, ProtocolDescriptor descriptor) {
        super(connection, DefaultDatatypeCoder.forEncodingFactory(connection.getEncodingFactory()));
        protocolDescriptor = requireNonNull(descriptor, "parameter descriptor should be non-null");
        wireOperations = descriptor.createWireOperations(connection, getServiceWarningCallback());
    }

    @Override
    public final int getHandle() {
        // The handle is always 0 for a TCP/IP service
        return 0;
    }

    @Override
    public void forceClose() throws SQLException {
        try {
            if (connection.isConnected()) {
                connection.close();
            }
        } catch (IOException e) {
            throw FbExceptionBuilder.ioWriteError(e);
        } finally {
            serviceListenerDispatcher.detached(this);
            serviceListenerDispatcher.shutdown();
            exceptionListenerDispatcher.shutdown();
        }
    }

    @Override
    public final ServiceParameterBuffer createServiceParameterBuffer() {
        return protocolDescriptor.createServiceParameterBuffer(connection);
    }

    @Override
    public final ServiceRequestBuffer createServiceRequestBuffer() {
        return protocolDescriptor.createServiceRequestBuffer(connection);
    }

    @Override
    public final boolean isAttached() {
        return super.isAttached() && isConnected();
    }

    /**
     * Checks if a physical connection to the server is established.
     *
     * @throws SQLException
     *         If not connected.
     */
    protected final void checkConnected() throws SQLException {
        if (!isConnected()) {
            throw FbExceptionBuilder.toNonTransientConnectionException(JaybirdErrorCodes.jb_notConnectedToServer);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code true} if a physical connection to the server is established
     */
    @Override
    protected final boolean isConnected() {
        return connection.isConnected();
    }

    /**
     * Checks if a physical connection to the server is established and if the
     * connection is attached to a database.
     * <p>
     * This method calls {@link #checkConnected()}, so it is not necessary to
     * call both.
     * </p>
     *
     * @throws SQLException
     *         If the database not connected or attached.
     */
    protected final void checkAttached() throws SQLException {
        checkConnected();
        if (!isAttached()) {
            throw FbExceptionBuilder.toNonTransientConnectionException(JaybirdErrorCodes.jb_notAttachedToDatabase);
        }
    }

    @Override
    public void setNetworkTimeout(int milliseconds) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkConnected();
            wireOperations.setNetworkTimeout(milliseconds);
        }
    }

    /**
     * @see XdrStreamAccess#getXdrIn()
     */
    protected final XdrInputStream getXdrIn() throws SQLException {
        return getXdrStreamAccess().getXdrIn();
    }

    /**
     * @see XdrStreamAccess#withTransmitLock(TransmitAction)
     * @since 7
     */
    protected final void withTransmitLock(TransmitAction transmitAction) throws IOException, SQLException {
        getXdrStreamAccess().withTransmitLock(transmitAction);
    }

    @Override
    public final GenericResponse readGenericResponse(@Nullable WarningMessageCallback warningCallback)
            throws SQLException, IOException {
        return wireOperations.readGenericResponse(warningCallback);
    }

    @Override
    public final XdrStreamAccess getXdrStreamAccess() {
        return connection.getXdrStreamAccess();
    }

    @Override
    public final FbWireOperations getWireOperations() {
        return wireOperations;
    }

    /**
     * Closes the WireConnection associated with this connection.
     *
     * @throws IOException
     *         For errors closing the connection.
     */
    protected final void closeConnection() throws IOException {
        if (!connection.isConnected()) return;
        try (LockCloseable ignored = withLock()) {
            try {
                connection.close();
            } finally {
                setDetached();
            }
        }
    }

}
