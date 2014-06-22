/*
 * $Id$
 * 
 * Firebird Open Source J2EE Connector - JDBC Driver
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
 * can be obtained from a CVS history command.
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
import org.firebirdsql.gds.ng.listeners.TransactionListener;
import org.firebirdsql.gds.ng.wire.*;
import org.firebirdsql.jdbc.FBDriverNotCapableException;
import org.firebirdsql.jdbc.FBSQLException;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.util.concurrent.atomic.AtomicInteger;

import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.*;
import static org.firebirdsql.gds.ng.TransactionHelper.checkTransactionActive;

/**
 * {@link FbWireDatabase} implementation for the version 10 wire protocol.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class V10Database extends AbstractFbWireDatabase implements FbWireDatabase, TransactionListener {

    private static final Logger log = LoggerFactory.getLogger(V10Database.class, false);

    private final AtomicInteger transactionCount = new AtomicInteger();
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
    public int getTransactionCount() {
        return transactionCount.get();
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
        if (attached.get()) {
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
            attached.set(true);
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

        String filenameCharset = dpb.getArgumentAsString(DatabaseParameterBufferExtension.FILENAME_CHARSET);

        xdrOut.writeInt(operation);
        xdrOut.writeInt(0); // Database object ID
        final Encoding filenameEncoding;
        if (filenameCharset == null) {
            filenameEncoding = getEncoding();
        } else {
            filenameEncoding = EncodingFactory.getDefaultInstance().getOrCreateEncodingForCharset(Charset.forName(filenameCharset));
        }
        xdrOut.writeString(connection.getDatabaseName(), filenameEncoding);

        dpb = ((DatabaseParameterBufferExtension) dpb).removeExtensionParams();
        // TODO Include ProcessID and ProcessName as in JavaGDSImpl implementation (or move that to different part?) See also Version10ProtocolDescriptor

        xdrOut.writeTyped(ISCConstants.isc_dpb_version1, (Xdrable) dpb);
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
        getDatabaseInfo(DESCRIBE_DATABASE_INFO_BLOCK, 1024, new DatabaseInformationProcessor());
        // During connect and attach the socketTimeout might be set to the connectTimeout, now reset to 'normal' socketTimeout
        connection.resetSocketTimeout();
    }

    @Override
    protected void internalDetach() throws SQLException {
        synchronized (getSynchronizationObject()) {
            if (getTransactionCount() > 0) {
                // Register open transactions as warning, we are going to detach and close the connection anyway
                // TODO: Change exception creation
                // TODO: Rollback transactions?
                FbExceptionBuilder builder = new FbExceptionBuilder();
                builder.warning(ISCConstants.isc_open_trans).messageParameter(getTransactionCount());
                getDatabaseWarningCallback().processWarning(builder.toSQLException(SQLWarning.class));
            }

            try {
                final XdrOutputStream xdrOut = getXdrOut();
                if (attached.get()) {
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
                attached.set(false);
            }
        }
    }

    /**
     * Performs {@link #detach()} suppressing any exception.
     */
    protected void safelyDetach() {
        try {
            detach();
        } catch (Exception ex) {
            // ignore, but log
            log.debug("Exception on safely detach", ex);
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
                attached.set(false);
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
        final FbWireTransaction transaction;
        synchronized (getSynchronizationObject()) {
            GenericResponse response;
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
                response = (GenericResponse) readResponse(null);
            } catch (IOException ioex) {
                throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(ioex).toSQLException();
            }
            transaction = protocolDescriptor.createTransaction(this, response.getObjectHandle(), TransactionState.ACTIVE);
        }
        transaction.addTransactionListener(this);
        transactionCount.incrementAndGet();
        return transaction;
    }

    @Override
    public FbTransaction reconnectTransaction(long transactionId) throws SQLException {
        final FbWireTransaction transaction;
        synchronized (getSynchronizationObject()) {
            GenericResponse response;
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
                response = (GenericResponse) readResponse(null);
            } catch (IOException ioex) {
                throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(ioex).toSQLException();
            }
            transaction = protocolDescriptor.createTransaction(this, response.getObjectHandle(), TransactionState.PREPARED);
        }
        transaction.addTransactionListener(this);
        transactionCount.incrementAndGet();
        return transaction;
    }

    @Override
    public FbStatement createStatement(FbTransaction transaction) throws SQLException {
        FbStatement stmt = protocolDescriptor.createStatement(this);
        stmt.setTransaction(transaction);
        return stmt;
    }

    @Override
    public void cancelOperation(int kind) throws SQLException {
        throw new SQLFeatureNotSupportedException(String.format("Cancel Operation isn't supported in this version of the wire protocol (%d).", protocolDescriptor.getVersion()), FBDriverNotCapableException.SQL_STATE_FEATURE_NOT_SUPPORTED);
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
    public GenericResponse readGenericResponse(WarningMessageCallback warningCallback) throws SQLException, IOException {
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
     * @param response
     *         Response to process
     * @throws SQLException
     *         For errors returned from the server.
     */
    protected void processResponse(Response response) throws SQLException {
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
    protected void processResponseWarnings(final Response response, WarningMessageCallback warningCallback) {
        if (warningCallback == null) {
            warningCallback = getDatabaseWarningCallback();
        }
        if (response instanceof GenericResponse) {
            GenericResponse genericResponse = (GenericResponse) response;
            SQLException exception = genericResponse.getException();
            if (exception != null && exception instanceof SQLWarning) {
                warningCallback.processWarning((SQLWarning) exception);
            }
        }
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
    public final int readNextOperation() throws IOException {
        synchronized (getSynchronizationObject()) {
            return connection.readNextOperation();
        }
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
                    // TODO Is this actually returned from server?
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
                    return builder.toSQLException();
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

    // TODO: Move iscVax* up in inheritance tree, or move to helper class

    /**
     * Info-request block for database information.
     * <p>
     * TODO Move to FbDatabase interface? Will this vary with versions of
     * Firebird?
     * </p>
     */
    // @formatter:off
    protected static final byte[] DESCRIBE_DATABASE_INFO_BLOCK = new byte[]{
            ISCConstants.isc_info_db_sql_dialect,
            ISCConstants.isc_info_firebird_version,
            ISCConstants.isc_info_ods_version,
            ISCConstants.isc_info_ods_minor_version,
            ISCConstants.isc_info_end };
    // @formatter:on

    protected class DatabaseInformationProcessor implements InfoProcessor<V10Database> {
        @Override
        public V10Database process(byte[] info) throws SQLException {
            boolean debug = log.isDebugEnabled();
            if (info.length == 0) {
                throw new SQLException("Response buffer for database information request is empty");
            }
            if (debug)
                log.debug(String.format("DatabaseInformationProcessor.process: first 2 bytes are %04X or: %02X, %02X",
                        iscVaxInteger2(info, 0), info[0], info[1]));
            int value;
            int len;
            int i = 0;
            while (info[i] != ISCConstants.isc_info_end) {
                switch (info[i++]) {
                case ISCConstants.isc_info_db_sql_dialect:
                    len = iscVaxInteger2(info, i);
                    i += 2;
                    value = iscVaxInteger(info, i, len);
                    i += len;
                    setDatabaseDialect((short) value);
                    if (debug) log.debug("isc_info_db_sql_dialect:" + value);
                    break;
                case ISCConstants.isc_info_ods_version:
                    len = iscVaxInteger2(info, i);
                    i += 2;
                    value = iscVaxInteger(info, i, len);
                    i += len;
                    setOdsMajor(value);
                    if (debug) log.debug("isc_info_ods_version:" + value);
                    break;
                case ISCConstants.isc_info_ods_minor_version:
                    len = iscVaxInteger2(info, i);
                    i += 2;
                    value = iscVaxInteger(info, i, len);
                    i += len;
                    setOdsMinor(value);
                    if (debug) log.debug("isc_info_ods_minor_version:" + value);
                    break;
                case ISCConstants.isc_info_firebird_version:
                    len = iscVaxInteger2(info, i);
                    i += 2;
                    String firebirdVersion = new String(info, i + 2, len - 2);
                    i += len;
                    setServerVersion(firebirdVersion);
                    if (debug) log.debug("isc_info_firebird_version:" + firebirdVersion);
                    break;
                case ISCConstants.isc_info_truncated:
                    if (debug) log.debug("isc_info_truncated ");
                    return V10Database.this;
                default:
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_infunk).toSQLException();
                }
            }
            return V10Database.this;
        }
    }

    @Override
    public void transactionStateChanged(FbTransaction transaction, TransactionState newState,
                                        TransactionState previousState) {
        switch (newState) {
        case COMMITTED:
        case ROLLED_BACK:
            transactionCount.decrementAndGet();
            break;
        default:
            // do nothing
            break;
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
     *         If the not connected or attached.
     */
    protected final void checkAttached() throws SQLException {
        checkConnected();
        if (!attached.get()) {
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
                if (attached.get()) {
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
