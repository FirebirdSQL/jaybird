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

import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.DatabaseParameterBufferExtension;
import org.firebirdsql.gds.impl.wire.DatabaseParameterBufferImp;
import org.firebirdsql.gds.impl.wire.XdrInputStream;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.impl.wire.Xdrable;
import org.firebirdsql.gds.ng.*;
import org.firebirdsql.gds.ng.wire.*;
import org.firebirdsql.jdbc.FBDriverNotCapableException;
import org.firebirdsql.jdbc.FBSQLException;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.*;

/**
 * {@link FbWireDatabase} implementation for the version 10 wire protocol.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.3
 */
public class V10Database implements FbWireDatabase, TransactionEventListener {

    private static final Logger log = LoggerFactory.getLogger(V10Database.class, false);

    // TODO Eliminate DatabaseParameterBuffer parameter from various methods

    private final Object syncObject = new Object();
    private final XdrStreamHolder xdrStreamHolder;
    private final AtomicBoolean attached = new AtomicBoolean();
    private final AtomicInteger transactionCount = new AtomicInteger();
    private final ProtocolDescriptor protocolDescriptor;
    private final WireConnection connection;
    /**
     * Callback for warnings. Only change using {@link #setWarningMessageCallback(org.firebirdsql.gds.ng.WarningMessageCallback)}.
     * <p>
     * Should never be null.
     * </p>
     */
    private WarningMessageCallback warningCallback = WarningMessageCallback.DUMMY;
    private int handle;
    private short databaseDialect;
    private int odsMajor;
    private int odsMinor;
    private String versionString;

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
        if (connection == null) throw new IllegalArgumentException("parameter connection should be non-null");
        if (descriptor == null) throw new IllegalArgumentException("parameter descriptor should be non-null");
        this.connection = connection;
        xdrStreamHolder = new XdrStreamHolder(connection);
        protocolDescriptor = descriptor;
    }

    @Override
    public short getDatabaseDialect() {
        return databaseDialect;
    }

    @Override
    public short getConnectionDialect() {
        return connection.getConnectionDialect();
    }

    /**
     * Sets the dialect of the database.
     * <p>
     * This method should only be called by this instance.
     * </p>
     *
     * @param dialect
     *         Dialect of the database/connection
     */
    protected void setDatabaseDialect(short dialect) {
        this.databaseDialect = dialect;
    }

    @Override
    public int getHandle() {
        return handle;
    }

    @Override
    public int getOdsMajor() {
        return odsMajor;
    }

    /**
     * Sets the ODS (On Disk Structure) major version of the database associated
     * with this connection.
     * <p>
     * This method should only be called by this instance.
     * </p>
     *
     * @param odsMajor
     *         ODS major version
     */
    protected void setOdsMajor(int odsMajor) {
        this.odsMajor = odsMajor;
    }

    @Override
    public int getOdsMinor() {
        return odsMinor;
    }

    /**
     * Sets the ODS (On Disk Structure) minor version of the database associated
     * with this connection.
     * <p>
     * This method should only be called by this instance.
     * </p>
     *
     * @param odsMinor
     *         The ODS minor version
     */
    protected void setOdsMinor(int odsMinor) {
        this.odsMinor = odsMinor;
    }

    @Override
    public String getVersionString() {
        return versionString;
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
    protected void setVersionString(String versionString) {
        this.versionString = versionString;
    }

    @Override
    public int getTransactionCount() {
        return transactionCount.get();
    }

    @Override
    public void setWarningMessageCallback(WarningMessageCallback callback) {
        synchronized (getSynchronizationObject()) {
            warningCallback = callback != null ? callback : WarningMessageCallback.DUMMY;
        }
    }

    @Override
    public final Object getSynchronizationObject() {
        return syncObject;
    }

    @Override
    public final XdrInputStream getXdrIn() throws SQLException {
        return xdrStreamHolder.getXdrIn();
    }

    @Override
    public final XdrOutputStream getXdrOut() throws SQLException {
        return xdrStreamHolder.getXdrOut();
    }

    @Override
    public boolean isAttached() {
        return attached.get() && connection.isConnected();
    }

    @Override
    public void attach(DatabaseParameterBuffer dpb) throws SQLException {
        attachOrCreate(dpb, false);
    }

    protected DatabaseParameterBuffer generateDatabaseParameterBufferFromConnectionProperties() {
        final IConnectionProperties connectionProperties = connection.getConnectionProperties();
        final DatabaseParameterBufferImp dpb = new DatabaseParameterBufferImp();
        throw new UnsupportedOperationException();
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
                    processAttachOrCreateResponse(readGenericResponse());
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
        xdrOut.writeString(connection.getDatabaseName(), filenameCharset);

        dpb = ((DatabaseParameterBufferExtension) dpb).removeExtensionParams();
        // TODO Include ProcessID and ProcessName as in JavaGDSImpl implementation (or move that to different part?)

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
    public void detach() throws SQLException {
        checkConnected();
        synchronized (getSynchronizationObject()) {
            if (getTransactionCount() > 0) {
                // Register open transactions as warning, we are going to detach and close the connection anyway
                // TODO: Change exception creation
                // TODO: Rollback transactions?
                FbExceptionBuilder builder = new FbExceptionBuilder();
                builder.warning(ISCConstants.isc_open_trans).messageParameter(getTransactionCount());
                warningCallback.processWarning(builder.toSQLException(SQLWarning.class));
            }

            final XdrOutputStream xdrOut = getXdrOut();
            try {
                if (attached.get()) {
                    xdrOut.writeInt(op_detach);
                    xdrOut.writeInt(getHandle());
                }
                xdrOut.writeInt(op_disconnect);
                xdrOut.flush();

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
                setWarningMessageCallback(null);
            }
        }
    }

    @Override
    public void createDatabase(DatabaseParameterBuffer dpb) throws SQLException {
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
                    readResponse();
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

    // TODO Rename to startTransaction(?), or leave calling beginTransaction to caller; and eliminate tpb use?

    @Override
    public FbTransaction createTransaction(TransactionParameterBuffer tpb) throws SQLException {
        FbTransaction transaction = protocolDescriptor.createTransaction(this);
        transaction.beginTransaction(tpb);
        return transaction;
    }

    @Override
    public FbStatement createStatement() throws SQLException {
        return protocolDescriptor.createStatement(this);
    }

    @Override
    public FbStatement createStatement(FbTransaction transaction) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void cancelOperation(int kind) throws SQLException {
        throw new SQLFeatureNotSupportedException("Cancel Operation isn't supported on Firebird 2.1 and earlier.", FBDriverNotCapableException.SQL_STATE_FEATURE_NOT_SUPPORTED);
    }

    @Override
    public <T> T getDatabaseInfo(byte[] requestItems, int bufferLength, InfoProcessor<T> infoProcessor)
            throws SQLException {
        byte[] responseBuffer = getDatabaseInfo(requestItems, bufferLength);
        return infoProcessor.process(responseBuffer);
    }

    public byte[] getDatabaseInfo(byte[] requestItems, int maxBufferLength) throws SQLException {
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
                GenericResponse genericResponse = readGenericResponse();
                byte[] data = genericResponse.getData();
                int responseLength = Math.min(maxBufferLength, data.length);
                // TODO Can't we just return data?
                final byte[] responseBuffer = new byte[responseLength];
                System.arraycopy(data, 0, responseBuffer, 0, responseLength);
                return responseBuffer;
            } catch (IOException ex) {
                throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(ex).toSQLException();
            }
        }
    }

    @Override
    public Response readResponse() throws SQLException, IOException {
        Response response = readSingleResponse();
        if (response instanceof GenericResponse) {
            processResponse(response);
        }
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
                processReleaseObjectResponse(readResponse());
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
    public GenericResponse readGenericResponse() throws SQLException, IOException {
        return (GenericResponse) readResponse();
    }

    /**
     * Reads the response from the server.
     *
     * @return Response
     * @throws SQLException
     *         For errors returned from the server, or when attempting to
     *         read
     * @throws IOException
     *         For errors reading the response from the connection.
     */
    protected Response readSingleResponse() throws SQLException, IOException {
        Response response = processOperation(getXdrIn().readNextOperation());
        processResponseWarnings(response);
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
    protected void processResponseWarnings(Response response) {
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
            // TODO: Throw exception instead?
            return null;
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
        FbExceptionBuilder builder = new FbExceptionBuilder();
        try {
            final XdrInputStream xdrIn = getXdrIn();
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
                    String stringValue = xdrIn.readString();
                    if (debug) log.debug("readStatusVector string: " + stringValue);
                    builder.messageParameter(stringValue);
                    break;
                case ISCConstants.isc_arg_sql_state:
                    // TODO Is this actually returned from server?
                    String sqlState = xdrIn.readString();
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
     * Reads Vax style integers from the supplied buffer, starting at
     * <code>startPosition</code> and reading for <code>length</code> bytes.
     * <p>
     * This method is useful for lengths up to 4 bytes (ie normal Java integers
     * (<code>int</code>). For larger lengths the values read will overflow. Use
     * {@link #iscVaxLong(byte[], int, int)} for reading values with length up
     * to 8 bytes.
     * </p>
     *
     * @param buffer
     *         The byte array from which the integer is to be retrieved
     * @param startPosition
     *         The offset starting position from which to start retrieving
     *         byte values
     * @return The integer value retrieved from the bytes
     * @see #iscVaxLong(byte[], int, int)
     * @see #iscVaxInteger2(byte[], int)
     */
    @Override
    public int iscVaxInteger(final byte[] buffer, final int startPosition, int length) {
        int value = 0;
        int shift = 0;

        int index = startPosition;
        while (--length >= 0) {
            value += (buffer[index++] & 0xff) << shift;
            shift += 8;
        }
        return value;
    }

    /**
     * Reads Vax style integers from the supplied buffer, starting at
     * <code>startPosition</code> and reading for <code>length</code> bytes.
     * <p>
     * This method is useful for lengths up to 8 bytes (ie normal Java longs (
     * <code>long</code>). For larger lengths the values read will overflow.
     * </p>
     *
     * @param buffer
     *         The byte array from which the integer is to be retrieved
     * @param startPosition
     *         The offset starting position from which to start retrieving
     *         byte values
     * @return The integer value retrieved from the bytes
     * @see #iscVaxLong(byte[], int, int)
     * @see #iscVaxInteger2(byte[], int)
     */
    @Override
    public long iscVaxLong(final byte[] buffer, final int startPosition, int length) {
        long value = 0;
        int shift = 0;

        int index = startPosition;
        while (--length >= 0) {
            value += (buffer[index++] & 0xffL) << shift;
            shift += 8;
        }
        return value;
    }

    /**
     * Implementation of {@link #iscVaxInteger(byte[], int, int)} specifically
     * for two-byte integers.
     * <p>
     * Use of this method has a small performance benefit over generic
     * {@link #iscVaxInteger(byte[], int, int)}
     * </p>
     *
     * @param buffer
     *         The byte array from which the integer is to be retrieved
     * @param startPosition
     *         The offset starting position from which to start retrieving
     *         byte values
     * @return The integer value retrieved from the bytes
     * @see #iscVaxInteger(byte[], int, int)
     * @see #iscVaxLong(byte[], int, int)
     */
    @Override
    public int iscVaxInteger2(final byte[] buffer, final int startPosition) {
        return (buffer[startPosition] & 0xff) | ((buffer[startPosition + 1] & 0xff) << 8);
    }

    /**
     * Info-request block for database information.
     * <p>
     * TODO Move to FbDatabase interface? Will this vary with versions of
     * Firebird?
     * </p>
     */
    // @formatter:off
    protected static final byte[] DESCRIBE_DATABASE_INFO_BLOCK = new byte[] {
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
                    setVersionString(firebirdVersion);
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

    // TODO Current behavior is based on implementation in .NET Provider; this probably needs to be modified
    @Override
    public void transactionStateChanged(FbTransaction transaction, TransactionState newState,
                                        TransactionState previousState) {
        if (newState != previousState) {
            switch (newState) {
            case ACTIVE:
                transactionCount.incrementAndGet();
                break;
            case NO_TRANSACTION:
                transactionCount.decrementAndGet();
                break;
            default:
                // do nothing
                break;
            }
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
