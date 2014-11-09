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
package org.firebirdsql.gds.ng.wire.version10;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.DatabaseParameterBufferExtension;
import org.firebirdsql.gds.impl.wire.XdrInputStream;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.impl.wire.Xdrable;
import org.firebirdsql.gds.ng.*;
import org.firebirdsql.gds.ng.fields.BlrCalculator;
import org.firebirdsql.gds.ng.wire.*;
import org.firebirdsql.jdbc.FBDriverNotCapableException;
import org.firebirdsql.jdbc.FBSQLException;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLNonTransientConnectionException;

import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.*;
import static org.firebirdsql.gds.ng.TransactionHelper.checkTransactionActive;

/**
 * {@link FbWireDatabase} implementation for the version 10 wire protocol.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class V10Database extends AbstractFbWireDatabase implements FbWireDatabase {

    private static final Logger log = LoggerFactory.getLogger(V10Database.class, false);

    private int handle;
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
    protected V10Database(WireConnection connection, ProtocolDescriptor descriptor) {
        super(connection, descriptor);
    }

    @Override
    public int getHandle() {
        return handle;
    }

    @Override
    public void attach() throws SQLException {
        final DatabaseParameterBuffer dpb = protocolDescriptor.createDatabaseParameterBuffer(connection);
        attachOrCreate(dpb, false);
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
                    processAttachOrCreateResponse(readGenericResponse(null));
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
        xdrOut.writeString(connection.getDatabaseName(), filenameEncoding);

        dpb = ((DatabaseParameterBufferExtension) dpb).removeExtensionParams();

        xdrOut.writeTyped(ISCConstants.isc_dpb_version1, (Xdrable) dpb);
    }

    /**
     * Gets the {@code Encoding} to use for the database filename.
     *
     * @param dpb
     *         Database parameter buffer
     * @return Encoding
     */
    protected Encoding getFilenameEncoding(DatabaseParameterBuffer dpb) {
        String filenameCharset = dpb.getArgumentAsString(DatabaseParameterBufferExtension.FILENAME_CHARSET);
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
     * Implementation retrieves database information like dialect ODS and server
     * version.
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
        synchronized (getSynchronizationObject()) {
            try {
                final XdrOutputStream xdrOut = getXdrOut();
                if (isAttached()) {
                    xdrOut.writeInt(op_detach);
                    xdrOut.writeInt(getHandle());
                }
                xdrOut.writeInt(op_disconnect);
                xdrOut.flush();

                // TODO Read response to op_detach?
                // TODO closeEventManager() (not yet implemented)

                closeConnection();
            } catch (IOException ex) {
                try {
                    closeConnection();
                } catch (Exception ex2) {
                    // ignore
                }
                throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ex).toSQLException();
            } finally {
                setDetached();
            }
        }
    }

    /**
     * Closes the WireConnection associated with this connection.
     *
     * @throws IOException
     *         For errors closing the connection.
     */
    protected void closeConnection() throws IOException {
        if (!connection.isConnected()) return;
        synchronized (getSynchronizationObject()) {
            try {
                connection.disconnect();
            } finally {
                setDetached();
            }
        }
    }

    @Override
    public void createDatabase(DatabaseParameterBuffer dpb) throws SQLException {
        // TODO Handle create database similar to attach (don't pass dpb in)?
        attachOrCreate(dpb, true);
    }

    @Override
    public void dropDatabase() throws SQLException {
        checkAttached();
        synchronized (getSynchronizationObject()) {
            try {
                try {
                    final XdrOutputStream xdrOut = getXdrOut();
                    xdrOut.writeInt(op_drop_database);
                    xdrOut.writeInt(getHandle());
                    xdrOut.flush();
                } catch (IOException ioex) {
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ioex).toSQLException();
                }
                try {
                    readResponse(null);
                } catch (IOException ioex) {
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(ioex).toSQLException();
                }
            } finally {
                try {
                    closeConnection();
                } catch (IOException e) {
                    log.debug("Ignored exception on connection close in dropDatabase()", e);
                }
            }
        }
    }

    @Override
    public FbWireTransaction startTransaction(TransactionParameterBuffer tpb) throws SQLException {
        checkAttached();
        synchronized (getSynchronizationObject()) {
            try {
                final XdrOutputStream xdrOut = getXdrOut();
                xdrOut.writeInt(op_transaction);
                xdrOut.writeInt(getHandle());
                xdrOut.writeTyped(ISCConstants.isc_tpb_version3, (Xdrable) tpb);
                xdrOut.flush();
            } catch (IOException ioex) {
                throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ioex).toSQLException();
            }
            try {
                final GenericResponse response = (GenericResponse) readResponse(null);
                final FbWireTransaction transaction = protocolDescriptor.createTransaction(this,
                        response.getObjectHandle(), TransactionState.ACTIVE);
                transactionAdded(transaction);
                return transaction;
            } catch (IOException ioex) {
                throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(ioex).toSQLException();
            }
        }

    }

    @Override
    public FbTransaction reconnectTransaction(long transactionId) throws SQLException {
        checkAttached();
        synchronized (getSynchronizationObject()) {
            try {
                final XdrOutputStream xdrOut = getXdrOut();
                xdrOut.writeInt(op_reconnect);
                xdrOut.writeInt(getHandle());
                // TODO: Only sending integer, why long?
                byte[] buf = new byte[4];
                // Note: This uses a atypical encoding (as this is actually a TPB without a type)
                for (int i = 0; i < 4; i++) {
                    buf[i] = (byte) (transactionId >>> (i * 8));
                }
                xdrOut.writeBuffer(buf);
                xdrOut.flush();
            } catch (IOException ioex) {
                throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ioex).toSQLException();
            }

            try {
                final GenericResponse response = (GenericResponse) readResponse(null);
                final FbWireTransaction transaction = protocolDescriptor.createTransaction(this,
                        response.getObjectHandle(), TransactionState.PREPARED);
                transactionAdded(transaction);
                return transaction;
            } catch (IOException ioex) {
                throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(ioex).toSQLException();
            }
        }
    }

    @Override
    public FbStatement createStatement(FbTransaction transaction) throws SQLException {
        checkAttached();
        FbStatement stmt = protocolDescriptor.createStatement(this);
        stmt.setTransaction(transaction);
        return stmt;
    }

    @Override
    public void cancelOperation(int kind) throws SQLException {
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
    }

    @Override
    public byte[] getDatabaseInfo(byte[] requestItems, int maxBufferLength) throws SQLException {
        // TODO Write common info request implementation shared for db, sql, transaction and blob?
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
                GenericResponse genericResponse = readGenericResponse(null);
                return genericResponse.getData();
            } catch (IOException ex) {
                throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(ex).toSQLException();
            }
        }
    }

    @Override
    public void executeImmediate(String statementText, FbTransaction transaction) throws SQLException {
        // TODO also implement op_exec_immediate2
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
    }

    @Override
    public Response readResponse(WarningMessageCallback warningCallback) throws SQLException, IOException {
        Response response = readSingleResponse(warningCallback);
        processResponse(response);
        return response;
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
    protected void processReleaseObjectResponse(Response response) {
        // Do nothing
    }

    @Override
    public GenericResponse readGenericResponse(
            WarningMessageCallback warningCallback) throws SQLException, IOException {
        return (GenericResponse) readResponse(warningCallback);
    }

    @Override
    public SqlResponse readSqlResponse(WarningMessageCallback warningCallback) throws SQLException, IOException {
        return (SqlResponse) readResponse(warningCallback);
    }

    @Override
    public BlrCalculator getBlrCalculator() {
        if (blrCalculator == null) {
            blrCalculator = protocolDescriptor.createBlrCalculator(this);
        }
        return blrCalculator;
    }

    /**
     * Reads the response from the server.
     *
     * @param warningCallback
     *         Callback object for signalling warnings, <code>null</code> to register warning on the default callback
     * @return Response
     * @throws SQLException
     *         For errors returned from the server, or when attempting to
     *         read
     * @throws IOException
     *         For errors reading the response from the connection.
     */
    protected Response readSingleResponse(WarningMessageCallback warningCallback) throws SQLException, IOException {
        Response response = processOperation(readNextOperation());
        processResponseWarnings(response, warningCallback);
        return response;
    }

    /**
     * Reads the response based on the specified operation.
     *
     * @param operation
     *         Database operation
     * @return Response object for the operation
     * @throws SQLException
     *         For errors reading the response from the connection.
     * @throws IOException
     *         For errors reading the response from the connection.
     */
    protected Response processOperation(int operation) throws SQLException, IOException {
        final XdrInputStream xdrIn = getXdrIn();
        switch (operation) {
        case op_response:
            return new GenericResponse(xdrIn.readInt(), xdrIn.readLong(), xdrIn.readBuffer(), readStatusVector());
        case op_fetch_response:
            return new FetchResponse(xdrIn.readInt(), xdrIn.readInt());
        case op_sql_response:
            return new SqlResponse(xdrIn.readInt());
        default:
            return null;
        }
    }

    @Override
    public void enqueueDeferredAction(DeferredAction deferredAction) {
        throw new UnsupportedOperationException("enqueueDeferredAction is not supported in the V10 protocol");
    }

    @Override
    protected void processDeferredActions() {
        // does nothing in V10 protocol
    }

    /**
     * Process the status vector and returns the associated {@link SQLException}
     * instance.
     * <p>
     * NOTE: This method <b>returns</b> the SQLException read from the
     * status vector, and only <b>throws</b> SQLException when an error occurs
     * processing the status ector.
     * </p>
     *
     * @return SQLException from the status vector
     * @throws SQLException
     *         for errors reading or processing the status vector
     */
    protected SQLException readStatusVector() throws SQLException {
        boolean debug = log.isDebugEnabled();
        final FbExceptionBuilder builder = new FbExceptionBuilder();
        final XdrInputStream xdrIn = getXdrIn();
        try {
            while (true) {
                int arg = xdrIn.readInt();
                int errorCode;
                switch (arg) {
                case ISCConstants.isc_arg_gds:
                    errorCode = xdrIn.readInt();
                    if (debug) log.debug("readStatusVector arg:isc_arg_gds int: " + errorCode);
                    if (errorCode != 0) {
                        builder.exception(errorCode);
                    }
                    break;
                case ISCConstants.isc_arg_warning:
                    errorCode = xdrIn.readInt();
                    if (debug) log.debug("readStatusVector arg:isc_arg_warning int: " + errorCode);
                    if (errorCode != 0) {
                        builder.warning(errorCode);
                    }
                    break;
                case ISCConstants.isc_arg_interpreted:
                case ISCConstants.isc_arg_string:
                    String stringValue = xdrIn.readString(getEncoding());
                    if (debug) log.debug("readStatusVector string: " + stringValue);
                    builder.messageParameter(stringValue);
                    break;
                case ISCConstants.isc_arg_sql_state:
                    String sqlState = xdrIn.readString(getEncoding());
                    if (debug) log.debug("readStatusVector sqlstate: " + sqlState);
                    builder.sqlState(sqlState);
                    break;
                case ISCConstants.isc_arg_number:
                    int intValue = xdrIn.readInt();
                    if (debug) log.debug("readStatusVector arg:isc_arg_number int: " + intValue);
                    builder.messageParameter(intValue);
                    break;
                case ISCConstants.isc_arg_end:
                    return builder.toFlatSQLException();
                default:
                    int e = xdrIn.readInt();
                    if (debug) log.debug("readStatusVector arg: " + arg + " int: " + e);
                    builder.messageParameter(e);
                    break;
                }
            }
        } catch (IOException ioe) {
            throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(ioe).toSQLException();
        }
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
            // TODO Update message / externalize + Check if SQL State right
            throw new SQLException("The connection is not attached to a database", FBSQLException.SQL_STATE_CONNECTION_ERROR);
        }
    }

    /**
     * Checks if a physical connection to the server is established.
     *
     * @throws SQLException
     *         If not connected.
     */
    @Override
    protected final void checkConnected() throws SQLException {
        if (!connection.isConnected()) {
            // TODO Update message / externalize
            throw new SQLException("No connection established to the database server", FBSQLException.SQL_STATE_CONNECTION_CLOSED);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (connection.isConnected()) {
                if (isAttached()) {
                    safelyDetach();
                } else {
                    closeConnection();
                }
            }
        } finally {
            super.finalize();
        }
    }
}
