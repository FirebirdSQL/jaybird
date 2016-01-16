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
package org.firebirdsql.gds.ng.wire.version10;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.EventHandle;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.DatabaseParameterBufferExtension;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.TransactionState;
import org.firebirdsql.gds.ng.fields.BlrCalculator;
import org.firebirdsql.gds.ng.wire.*;
import org.firebirdsql.jdbc.FBDriverNotCapableException;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLNonTransientException;

import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.*;
import static org.firebirdsql.gds.ng.TransactionHelper.checkTransactionActive;

/**
 * {@link FbWireDatabase} implementation for the version 10 wire protocol.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class V10Database extends AbstractFbWireDatabase implements FbWireDatabase {

    private static final Logger log = LoggerFactory.getLogger(V10Database.class);

    private int handle;
    private BlrCalculator blrCalculator;
    private FbWireAsynchronousChannel asynchronousChannel;

    /**
     * Creates a V10Database instance.
     *
     * @param connection
     *         A WireConnection with an established connection to the server.
     * @param descriptor
     *         The ProtocolDescriptor that created this connection (this is
     *         used for creating further dependent objects).
     */
    protected V10Database(WireDatabaseConnection connection, ProtocolDescriptor descriptor) {
        super(connection, descriptor);
    }

    @Override
    public int getHandle() {
        return handle;
    }

    @Override
    public void queueEvent(EventHandle eventHandle) throws SQLException {
        try {
            checkAttached();
            // TODO Move to AbstractFbWireDatabase?
            synchronized (getSynchronizationObject()) {
                if (asynchronousChannel == null || !asynchronousChannel.isConnected()) {
                    asynchronousChannel = initAsynchronousChannel();
                    AsynchronousProcessor.getInstance().registerAsynchronousChannel(asynchronousChannel);
                }
                asynchronousChannel.queueEvent(eventHandle);
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public void cancelEvent(EventHandle eventHandle) throws SQLException {
        try {
            checkAttached();
            synchronized (getSynchronizationObject()) {
                if (asynchronousChannel == null || !asynchronousChannel.isConnected()) {
                    // TODO SQL state, standard firebird error code?
                    throw new SQLNonTransientException("Asynchronous channel is not connected, cannot cancel event");
                }
                asynchronousChannel.cancelEvent(eventHandle);
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public void attach() throws SQLException {
        try {
            final DatabaseParameterBuffer dpb = protocolDescriptor.createDatabaseParameterBuffer(connection);
            attachOrCreate(dpb, false);
        } catch (SQLException ex) {
            exceptionListenerDispatcher.errorOccurred(ex);
            throw ex;
        }
    }

    /**
     * @param dpb
     *         Database parameter buffer
     * @param create
     *         <code>true</code> create database, <code>false</code> only
     *         attach
     * @throws SQLException
     *         For errors during attach or create
     */
    protected void attachOrCreate(DatabaseParameterBuffer dpb, boolean create) throws SQLException {
        checkConnected();
        if (isAttached()) {
            throw new SQLException("Already attached to a database");
        }
        synchronized (getSynchronizationObject()) {
            try {
                try {
                    sendAttachOrCreateToBuffer(dpb, create);
                    getXdrOut().flush();
                } catch (IOException e) {
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(e).toSQLException();
                }
                try {
                    authReceiveResponse(null);
                } catch (IOException e) {
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(e).toSQLException();
                }
            } catch (SQLException e) {
                safelyDetach();
                throw e;
            }
            setAttached();
            afterAttachActions();
        }
    }

    /**
     * Sends the buffer for op_attach or op_create
     *
     * @param dpb
     *         Database parameter buffer
     * @param create
     *         <code>true</code> create database, <code>false</code> only
     *         attach
     * @throws SQLException
     *         If the connection is not open
     * @throws IOException
     *         For errors writing to the connection
     */
    protected void sendAttachOrCreateToBuffer(DatabaseParameterBuffer dpb, boolean create)
            throws SQLException, IOException {
        final int operation = create ? op_create : op_attach;
        final XdrOutputStream xdrOut = getXdrOut();

        final Encoding filenameEncoding = getFilenameEncoding(dpb);

        xdrOut.writeInt(operation);
        xdrOut.writeInt(0); // Database object ID
        xdrOut.writeString(connection.getAttachObjectName(), filenameEncoding);

        dpb = ((DatabaseParameterBufferExtension) dpb).removeExtensionParams();

        xdrOut.writeTyped(dpb);
    }

    /**
     * Gets the {@code Encoding} to use for the database filename.
     *
     * @param dpb
     *         Database parameter buffer
     * @return Encoding
     */
    protected Encoding getFilenameEncoding(DatabaseParameterBuffer dpb) {
        final String filenameCharset = dpb.getArgumentAsString(DatabaseParameterBufferExtension.FILENAME_CHARSET);
        if (filenameCharset != null) {
            return EncodingFactory.getDefaultInstance().getOrCreateEncodingForCharset(Charset.forName(filenameCharset));
        }
        return getEncoding();
    }

    /**
     * Processes the response from the server to the attach or create operation.
     *
     * @param genericResponse
     *         GenericResponse received from the server.
     */
    protected void processAttachOrCreateResponse(GenericResponse genericResponse) {
        handle = genericResponse.getObjectHandle();
    }

    /**
     * Additional tasks to execute directly after attach operation.
     * <p>
     * Implementation retrieves database information like dialect ODS and server version.
     * </p>
     *
     * @throws SQLException
     *         For errors reading or writing database information.
     */
    protected void afterAttachActions() throws SQLException {
        getDatabaseInfo(getDescribeDatabaseInfoBlock(), 1024, getDatabaseInformationProcessor());
        // During connect and attach the socketTimeout might be set to the connectTimeout, now reset to 'normal' socketTimeout
        connection.resetSocketTimeout();
    }

    @Override
    protected void internalDetach() throws SQLException {
        // TODO Move to wire operations as it is alsmost identical to service detach?
        synchronized (getSynchronizationObject()) {
            try {
                try {
                    final XdrOutputStream xdrOut = getXdrOut();
                    if (isAttached()) {
                        xdrOut.writeInt(op_detach);
                        xdrOut.writeInt(getHandle());
                    }
                    xdrOut.writeInt(op_disconnect);
                    xdrOut.flush();
                } catch (IOException ex) {
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ex).toSQLException();
                }
                if (isAttached()) {
                    try {
                        // Consume op_detach response
                        wireOperations.readResponse(null);
                    } catch (IOException ex) {
                        throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(ex).toSQLException();
                    }
                }
                try {
                    closeConnection();
                } catch (IOException ex) {
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ex).toSQLException();
                }
            } catch (SQLException ex) {
                try {
                    closeConnection();
                } catch (Exception ex2) {
                    // ignore
                }
                throw ex;
            } finally {
                setDetached();
            }
        }
    }

    @Override
    public void createDatabase() throws SQLException {
        try {
            final DatabaseParameterBuffer dpb = protocolDescriptor.createDatabaseParameterBuffer(connection);
            attachOrCreate(dpb, true);
        } catch (SQLException ex) {
            exceptionListenerDispatcher.errorOccurred(ex);
            throw ex;
        }
    }

    @Override
    public void dropDatabase() throws SQLException {
        try {
            checkAttached();
            synchronized (getSynchronizationObject()) {
                try {
                    try {
                        final XdrOutputStream xdrOut = getXdrOut();
                        xdrOut.writeInt(op_drop_database);
                        xdrOut.writeInt(getHandle());
                        xdrOut.flush();
                    } catch (IOException ioex) {
                        throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ioex)
                                .toSQLException();
                    }
                    try {
                        readResponse(null);
                    } catch (IOException ioex) {
                        throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(ioex)
                                .toSQLException();
                    }
                } finally {
                    try {
                        closeConnection();
                    } catch (IOException e) {
                        log.debug("Ignored exception on connection close in dropDatabase()", e);
                    }
                }
            }
        } catch (SQLException ex) {
            exceptionListenerDispatcher.errorOccurred(ex);
            throw ex;
        }
    }

    @Override
    public FbWireTransaction startTransaction(TransactionParameterBuffer tpb) throws SQLException {
        try {
            checkAttached();
            synchronized (getSynchronizationObject()) {
                try {
                    final XdrOutputStream xdrOut = getXdrOut();
                    xdrOut.writeInt(op_transaction);
                    xdrOut.writeInt(getHandle());
                    xdrOut.writeTyped(tpb);
                    xdrOut.flush();
                } catch (IOException ioex) {
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ioex)
                            .toSQLException();
                }
                try {
                    final GenericResponse response = readGenericResponse(null);
                    final FbWireTransaction transaction = protocolDescriptor.createTransaction(this,
                            response.getObjectHandle(), TransactionState.ACTIVE);
                    transactionAdded(transaction);
                    return transaction;
                } catch (IOException ioex) {
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(ioex)
                            .toSQLException();
                }
            }
        } catch (SQLException ex) {
            exceptionListenerDispatcher.errorOccurred(ex);
            throw ex;
        }
    }

    @Override
    public FbTransaction reconnectTransaction(long transactionId) throws SQLException {
        try {
            checkAttached();
            synchronized (getSynchronizationObject()) {
                try {
                    final XdrOutputStream xdrOut = getXdrOut();
                    xdrOut.writeInt(op_reconnect);
                    xdrOut.writeInt(getHandle());
                    // TODO: Only sending integer, why long?
                    final byte[] buf = new byte[4];
                    // Note: This uses a atypical encoding (as this is actually a TPB without a type)
                    for (int i = 0; i < 4; i++) {
                        buf[i] = (byte) (transactionId >>> (i * 8));
                    }
                    xdrOut.writeBuffer(buf);
                    xdrOut.flush();
                } catch (IOException ioex) {
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ioex)
                            .toSQLException();
                }

                try {
                    final GenericResponse response = readGenericResponse(null);
                    final FbWireTransaction transaction = protocolDescriptor.createTransaction(this,
                            response.getObjectHandle(), TransactionState.PREPARED);
                    transactionAdded(transaction);
                    return transaction;
                } catch (IOException ioex) {
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(ioex)
                            .toSQLException();
                }
            }
        } catch (SQLException ex) {
            exceptionListenerDispatcher.errorOccurred(ex);
            throw ex;
        }
    }

    @Override
    public FbStatement createStatement(FbTransaction transaction) throws SQLException {
        try {
            checkAttached();
            final FbStatement stmt = protocolDescriptor.createStatement(this);
            stmt.addExceptionListener(exceptionListenerDispatcher);
            stmt.setTransaction(transaction);
            return stmt;
        } catch (SQLException ex) {
            exceptionListenerDispatcher.errorOccurred(ex);
            throw ex;
        }
    }

    @Override
    public void cancelOperation(int kind) throws SQLException {
        try {
            if (kind == ISCConstants.fb_cancel_abort) {
                try {
                    // In case of abort we forcibly close the connection
                    // TODO We may need to do additional cleanup (eg notify statements so they can close etc)
                    closeConnection();
                } catch (IOException ioe) {
                    throw new SQLNonTransientConnectionException("Connection abort failed", ioe);
                }
            } else {
                throw new SQLFeatureNotSupportedException(
                        String.format("Cancel Operation isn't supported in this version of the wire protocol (%d).",
                                protocolDescriptor.getVersion()),
                        FBDriverNotCapableException.SQL_STATE_FEATURE_NOT_SUPPORTED);
            }
        } catch (SQLException ex) {
            exceptionListenerDispatcher.errorOccurred(ex);
            throw ex;
        }
    }

    @Override
    public byte[] getDatabaseInfo(byte[] requestItems, int maxBufferLength) throws SQLException {
        // TODO Write common info request implementation shared for db, sql, transaction and blob?
        try {
            checkAttached();
            synchronized (getSynchronizationObject()) {
                try {
                    final XdrOutputStream xdrOut = getXdrOut();
                    xdrOut.writeInt(op_info_database);
                    xdrOut.writeInt(getHandle());
                    xdrOut.writeInt(0); // incarnation
                    xdrOut.writeBuffer(requestItems);
                    xdrOut.writeInt(maxBufferLength);

                    xdrOut.flush();
                } catch (IOException ex) {
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ex).toSQLException();
                }
                try {
                    final GenericResponse genericResponse = readGenericResponse(null);
                    return genericResponse.getData();
                } catch (IOException ex) {
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(ex).toSQLException();
                }
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public void executeImmediate(String statementText, FbTransaction transaction) throws SQLException {
        // TODO also implement op_exec_immediate2
        try {
            if (isAttached()) {
                if (transaction == null) {
                    // TODO SQLState and/or Firebird specific error
                    throw new SQLException("executeImmediate requires a transaction when attached");
                }
                checkTransactionActive(transaction);
            } else if (transaction != null) {
                // TODO SQLState and/or Firebird specific error
                throw new SQLException("executeImmediate when not attached should have no transaction");
            }
            synchronized (getSynchronizationObject()) {
                try {
                    final XdrOutputStream xdrOut = getXdrOut();
                    xdrOut.writeInt(op_exec_immediate);

                    xdrOut.writeInt(transaction != null ? transaction.getHandle() : 0);
                    xdrOut.writeInt(getHandle());
                    xdrOut.writeInt(getConnectionDialect());
                    xdrOut.writeString(statementText, getEncoding());

                    // information request items
                    xdrOut.writeBuffer(null);
                    xdrOut.writeInt(0);
                    getXdrOut().flush();
                } catch (IOException ex) {
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ex).toSQLException();
                }
                try {
                    if (!isAttached()) {
                        processAttachOrCreateResponse(readGenericResponse(null));
                    }
                    readGenericResponse(null);
                } catch (IOException ex) {
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(ex).toSQLException();
                }
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public void releaseObject(int operation, int objectId) throws SQLException {
        checkAttached();
        synchronized (getSynchronizationObject()) {
            try {
                doReleaseObjectPacket(operation, objectId);
                getXdrOut().flush();
            } catch (IOException ex) {
                throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ex).toSQLException();
            }
            try {
                processReleaseObjectResponse(readResponse(null));
            } catch (IOException ex) {
                throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(ex).toSQLException();
            }
        }
    }

    @Override
    public FbWireAsynchronousChannel initAsynchronousChannel() throws SQLException {
        checkAttached();
        final int auxHandle;
        final int port;
        synchronized (getSynchronizationObject()) {
            try {
                final XdrOutputStream xdrOut = getXdrOut();
                xdrOut.writeInt(op_connect_request);
                xdrOut.writeInt(P_REQ_async); // Connection type (p_req_type)
                xdrOut.writeInt(getHandle()); // p_req_object
                xdrOut.writeInt(0); // p_req_partner
                xdrOut.flush();
            } catch (IOException ex) {
                throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ex).toSQLException();
            }
            try {
                final GenericResponse response = readGenericResponse(null);
                auxHandle = response.getObjectHandle();
                final byte[] data = response.getData();
                // bytes 0 - 1: sin family (ignore)
                // bytes 2 - 3: sin port (port to connect to)
                port = ((data[2] & 0xFF) << 8) + (data[3] & 0xFF);
                // remaining bytes: IP address + other info(?) (ignore, can't be trusted, and invalid in FB3 and higher; always use original hostname)
            } catch (IOException ex) {
                throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(ex).toSQLException();
            }
        }
        final FbWireAsynchronousChannel channel = protocolDescriptor.createAsynchronousChannel(this);
        channel.connect(connection.getServerName(), port, auxHandle);
        return channel;
    }

    /**
     * Sends - without flushing - the (release) operation and objectId.
     *
     * @param operation
     *         Operation
     * @param objectId
     *         Id of the object to release
     * @throws IOException
     *         For errors writing to the connection
     * @throws SQLException
     *         If the database connection is not available
     */
    protected void doReleaseObjectPacket(int operation, int objectId) throws IOException, SQLException {
        getXdrOut().writeInt(operation);
        getXdrOut().writeInt(objectId);
    }

    /**
     * Process the release object response
     *
     * @param response
     *         The response object
     */
    protected void processReleaseObjectResponse(@SuppressWarnings("UnusedParameters") Response response) {
        // Do nothing
    }

    @Override
    public BlrCalculator getBlrCalculator() {
        if (blrCalculator == null) {
            blrCalculator = protocolDescriptor.createBlrCalculator(this);
        }
        return blrCalculator;
    }

    @Override
    public void enqueueDeferredAction(DeferredAction deferredAction) {
        throw new UnsupportedOperationException("enqueueDeferredAction is not supported in the V10 protocol");
    }

    @Override
    public void authReceiveResponse(AcceptPacket acceptPacket) throws IOException, SQLException {
        wireOperations.authReceiveResponse(acceptPacket, new FbWireOperations.ProcessAttachCallback() {
            @Override
            public void processAttachResponse(GenericResponse response) {
                processAttachOrCreateResponse(response);
            }
        });
    }
}
