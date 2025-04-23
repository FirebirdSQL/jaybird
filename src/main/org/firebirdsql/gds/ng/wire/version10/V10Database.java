// SPDX-FileCopyrightText: Copyright 2013-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version10;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.gds.*;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.gds.ng.TransactionState;
import org.firebirdsql.gds.ng.fields.BlrCalculator;
import org.firebirdsql.gds.ng.wire.*;
import org.firebirdsql.jdbc.SQLStateConstants;

import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

import static org.firebirdsql.gds.JaybirdErrorCodes.jb_executeImmediateRequiresNoTransactionDetached;
import static org.firebirdsql.gds.JaybirdErrorCodes.jb_executeImmediateRequiresTransactionAttached;
import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.*;
import static org.firebirdsql.gds.ng.TransactionHelper.checkTransactionActive;

/**
 * {@link FbWireDatabase} implementation for the version 10 wire protocol.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public class V10Database extends AbstractFbWireDatabase implements FbWireDatabase {

    private BlrCalculator blrCalculator;

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
    public final void attach() throws SQLException {
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
    protected final void attachOrCreate(DatabaseParameterBuffer dpb, boolean create) throws SQLException {
        checkConnected();
        requireNotAttached();
        try (var ignored = withLock()) {
            try {
                sendAttachOrCreate(dpb, create);
                receiveAttachOrCreateResponse();
            } catch (SQLException e) {
                safelyDetach();
                throw e;
            }
            setAttached();
            afterAttachActions();
        }
    }

    private void sendAttachOrCreate(DatabaseParameterBuffer dpb, boolean create) throws SQLException {
        try {
            sendAttachOrCreateToBuffer(dpb, create);
            getXdrOut().flush();
        } catch (IOException e) {
            throw FbExceptionBuilder.ioWriteError(e);
        }
    }

    private void receiveAttachOrCreateResponse() throws SQLException {
        try {
            authReceiveResponse(null);
        } catch (IOException e) {
            throw FbExceptionBuilder.ioReadError(e);
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
    protected final void sendAttachOrCreateToBuffer(DatabaseParameterBuffer dpb, boolean create)
            throws SQLException, IOException {
        final int operation = create ? op_create : op_attach;
        final XdrOutputStream xdrOut = getXdrOut();

        final Encoding filenameEncoding = getFilenameEncoding(dpb);

        xdrOut.writeInt(operation); // p_operation
        xdrOut.writeInt(0); // p_atch_database
        xdrOut.writeString(connection.getAttachObjectName(), filenameEncoding); // p_atch_file

        xdrOut.writeTyped(dpb); // p_atch_dpb
    }

    /**
     * Gets the {@code Encoding} to use for the database filename.
     *
     * @param dpb
     *         Database parameter buffer
     * @return Encoding
     */
    protected Encoding getFilenameEncoding(DatabaseParameterBuffer dpb) {
        final String filenameCharset = getConnectionProperties().getProperty("filename_charset");
        if (filenameCharset != null) {
            return getEncodingFactory().getOrCreateEncodingForCharset(Charset.forName(filenameCharset));
        }
        return getEncoding();
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
    protected final void afterAttachActions() throws SQLException {
        connection.clearAuthData();
        getDatabaseInfo(getDescribeDatabaseInfoBlock(), 1024, getDatabaseInformationProcessor());
        // During connect and attach the socketTimeout might be set to the connectTimeout, now reset to 'normal' socketTimeout
        connection.resetSocketTimeout();
    }

    @Override
    @SuppressWarnings("java:S1141")
    protected final void internalDetach() throws SQLException {
        // TODO Move to wire operations as it is almost identical to service detach?
        try (LockCloseable ignored = withLock()) {
            try {
                sendDetachDisconnect();
                if (isAttached()) {
                    receiveDetachResponse();
                }
                try {
                    closeConnection();
                } catch (IOException e) {
                    throw FbExceptionBuilder.ioWriteError(e);
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

    private void sendDetachDisconnect() throws SQLException {
        try {
            XdrOutputStream xdrOut = getXdrOut();
            if (isAttached()) {
                xdrOut.writeInt(op_detach); // p_operation
                xdrOut.writeInt(0); // p_rlse_object
            }
            xdrOut.writeInt(op_disconnect); // p_operation
            xdrOut.flush();
        } catch (IOException e) {
            throw FbExceptionBuilder.ioWriteError(e);
        }
    }

    private void receiveDetachResponse() throws SQLException {
        try {
            // Consume op_detach response
            wireOperations.readResponse(null);
        } catch (IOException e) {
            throw FbExceptionBuilder.ioReadError(e);
        }
    }

    @Override
    public final void createDatabase() throws SQLException {
        try {
            final DatabaseParameterBuffer dpb = protocolDescriptor.createDatabaseParameterBuffer(connection);
            attachOrCreate(dpb, true);
        } catch (SQLException ex) {
            exceptionListenerDispatcher.errorOccurred(ex);
            throw ex;
        }
    }

    @Override
    @SuppressWarnings("java:S1141")
    public final void dropDatabase() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkAttached();
            try {
                sendDropDatabase();
                receiveDropDatabaseResponse();
            } finally {
                try {
                    closeConnection();
                } catch (IOException e) {
                    System.getLogger(getClass().getName()).log(System.Logger.Level.DEBUG,
                            "Ignored exception on connection close in dropDatabase()", e);
                }
            }
        } catch (SQLException ex) {
            exceptionListenerDispatcher.errorOccurred(ex);
            throw ex;
        }
    }

    private void sendDropDatabase() throws SQLException {
        try {
            final XdrOutputStream xdrOut = getXdrOut();
            xdrOut.writeInt(op_drop_database); // p_operation
            xdrOut.writeInt(0); // p_rlse_object
            xdrOut.flush();
        } catch (IOException e) {
            throw FbExceptionBuilder.ioWriteError(e);
        }
    }

    private void receiveDropDatabaseResponse() throws SQLException {
        try {
            readResponse(null);
        } catch (IOException e) {
            throw FbExceptionBuilder.ioReadError(e);
        }
    }

    @Override
    public final FbWireTransaction startTransaction(TransactionParameterBuffer tpb) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkAttached();
            sendStartTransaction(tpb);
            return receiveTransactionResponse(TransactionState.ACTIVE);
        } catch (SQLException ex) {
            exceptionListenerDispatcher.errorOccurred(ex);
            throw ex;
        }
    }

    @Override
    public FbTransaction startTransaction(String statementText) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkAttached();
            sendExecuteImmediate(statementText, null);
            return receiveTransactionResponse(TransactionState.ACTIVE);
        } catch (SQLException ex) {
            exceptionListenerDispatcher.errorOccurred(ex);
            throw ex;
        }
    }

    private void sendStartTransaction(TransactionParameterBuffer tpb) throws SQLException {
        try {
            final XdrOutputStream xdrOut = getXdrOut();
            xdrOut.writeInt(op_transaction); // p_operation
            xdrOut.writeInt(0); // p_sttr_database
            xdrOut.writeTyped(tpb); // p_sttr_tpb
            xdrOut.flush();
        } catch (IOException e) {
            throw FbExceptionBuilder.ioWriteError(e);
        }
    }

    private FbWireTransaction receiveTransactionResponse(TransactionState initialState) throws SQLException {
        try {
            FbWireTransaction transaction = protocolDescriptor.createTransaction(this,
                    readGenericResponse(null).objectHandle(), initialState);
            transactionAdded(transaction);
            return transaction;
        } catch (IOException e) {
            throw FbExceptionBuilder.ioReadError(e);
        }
    }

    @Override
    public final FbTransaction reconnectTransaction(long transactionId) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkAttached();
            sendReconnect(transactionId);
            return receiveTransactionResponse(TransactionState.PREPARED);
        } catch (SQLException ex) {
            exceptionListenerDispatcher.errorOccurred(ex);
            throw ex;
        }
    }

    private void sendReconnect(long transactionId) throws SQLException {
        try {
            final XdrOutputStream xdrOut = getXdrOut();
            xdrOut.writeInt(op_reconnect); // p_operation
            xdrOut.writeInt(0); // p_sttr_database
            final byte[] transactionIdBuffer = getTransactionIdBuffer(transactionId);
            xdrOut.writeBuffer(transactionIdBuffer); // p_sttr_tpb
            xdrOut.flush();
        } catch (IOException e) {
            throw FbExceptionBuilder.ioWriteError(e);
        }
    }

    @Override
    @SuppressWarnings("java:S2095")
    public final FbStatement createStatement(FbTransaction transaction) throws SQLException {
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
                // In case of abort we forcibly close the connection
                forceClose();
            } else {
                throw new SQLFeatureNotSupportedException(
                        String.format("Cancel Operation isn't supported in this version of the wire protocol (%d).",
                                protocolDescriptor.getVersion()),
                        SQLStateConstants.SQL_STATE_FEATURE_NOT_SUPPORTED);
            }
        } catch (SQLException ex) {
            exceptionListenerDispatcher.errorOccurred(ex);
            throw ex;
        }
    }

    @Override
    public final void executeImmediate(String statementText, FbTransaction transaction) throws SQLException {
        // TODO also implement op_exec_immediate2
        try {
            if (isAttached()) {
                if (transaction == null) {
                    throw FbExceptionBuilder.toException(jb_executeImmediateRequiresTransactionAttached);
                }
                checkTransactionActive(transaction);
            } else if (transaction != null) {
                throw FbExceptionBuilder.toException(jb_executeImmediateRequiresNoTransactionDetached);
            }
            try (LockCloseable ignored = withLock()) {
                sendExecuteImmediate(statementText, transaction);
                receiveExecuteImmediateResponse();
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    private void sendExecuteImmediate(String statementText, FbTransaction transaction) throws SQLException {
        try {
            final XdrOutputStream xdrOut = getXdrOut();
            xdrOut.writeInt(op_exec_immediate);

            xdrOut.writeInt(transaction != null ? transaction.getHandle() : 0);
            xdrOut.writeInt(0);
            xdrOut.writeInt(getConnectionDialect());
            xdrOut.writeString(statementText, getEncoding());

            // information request items
            xdrOut.writeBuffer(null);
            xdrOut.writeInt(0);
            getXdrOut().flush();
        } catch (IOException e) {
            throw FbExceptionBuilder.ioWriteError(e);
        }
    }

    private void receiveExecuteImmediateResponse() throws SQLException {
        try {
            if (!isAttached()) {
                readGenericResponse(null);
            }
            readGenericResponse(null);
        } catch (IOException e) {
            throw FbExceptionBuilder.ioReadError(e);
        }
    }

    @Override
    public void releaseObject(int operation, int objectId) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkAttached();
            try {
                doReleaseObjectPacket(operation, objectId);
                getXdrOut().flush();
            } catch (IOException e) {
                throw FbExceptionBuilder.ioWriteError(e);
            }
            try {
                processReleaseObjectResponse(readResponse(null));
            } catch (IOException e) {
                throw FbExceptionBuilder.ioReadError(e);
            }
        }
    }

    @Override
    public final FbWireAsynchronousChannel initAsynchronousChannel() throws SQLException {
        checkAttached();
        final int port;
        try (LockCloseable ignored = withLock()) {
            try {
                final XdrOutputStream xdrOut = getXdrOut();
                xdrOut.writeInt(op_connect_request); // p_operation
                xdrOut.writeInt(P_REQ_async); // p_req_type - Connection type
                xdrOut.writeInt(0); // p_req_object
                xdrOut.writeInt(0); // p_req_partner
                xdrOut.flush();
            } catch (IOException e) {
                throw FbExceptionBuilder.ioWriteError(e);
            }
            try {
                final GenericResponse response = readGenericResponse(null);
                final byte[] data = response.data();
                // bytes 0 - 1: sin family (ignore)
                // bytes 2 - 3: sin port (port to connect to)
                port = ((data[2] & 0xFF) << 8) + (data[3] & 0xFF);
                // remaining bytes: IP address + other info(?) (ignore, can't be trusted, and invalid in FB3 and higher; always use original hostname)
            } catch (IOException e) {
                throw FbExceptionBuilder.ioReadError(e);
            }
        }
        final FbWireAsynchronousChannel channel = protocolDescriptor.createAsynchronousChannel(this);
        channel.connect(connection.getServerName(), port);
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
    protected final void doReleaseObjectPacket(int operation, int objectId) throws IOException, SQLException {
        final XdrOutputStream xdrOut = getXdrOut();
        xdrOut.writeInt(operation); // p_operation
        xdrOut.writeInt(objectId); // p_rlse_object
    }

    /**
     * Process the release object response
     *
     * @param response
     *         The response object
     */
    protected final void processReleaseObjectResponse(@SuppressWarnings("unused") Response response) {
        // Do nothing
    }

    @Override
    public final BlrCalculator getBlrCalculator() {
        if (blrCalculator == null) {
            blrCalculator = protocolDescriptor.createBlrCalculator(this);
        }
        return blrCalculator;
    }

    @Override
    public final void authReceiveResponse(AcceptPacket acceptPacket) throws IOException, SQLException {
        wireOperations.authReceiveResponse(acceptPacket, connection.createDbCryptCallback());
    }
}
