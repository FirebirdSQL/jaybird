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
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.impl.wire.XdrInputStream;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.IAttachProperties;
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.gds.ng.WarningMessageCallback;
import org.firebirdsql.gds.ng.dbcrypt.DbCryptCallback;
import org.firebirdsql.gds.ng.wire.auth.ClientAuthBlock;
import org.firebirdsql.gds.ng.wire.crypt.KnownServerKey;
import org.firebirdsql.jdbc.FBDriverNotCapableException;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.firebirdsql.gds.ISCConstants.*;
import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.*;

/**
 * @author Mark Rotteveel
 * @since 3.0
 */
public abstract class AbstractWireOperations implements FbWireOperations {

    private static final Logger log = LoggerFactory.getLogger(AbstractWireOperations.class);

    private final WireConnection<?, ?> connection;
    private final WarningMessageCallback defaultWarningMessageCallback;

    protected AbstractWireOperations(WireConnection<?, ?> connection,
            WarningMessageCallback defaultWarningMessageCallback) {
        this.connection = requireNonNull(connection, "connection");
        this.defaultWarningMessageCallback =
                requireNonNull(defaultWarningMessageCallback, "defaultWarningMessageCallback");
    }

    @Override
    public final XdrStreamAccess getXdrStreamAccess() {
        return connection.getXdrStreamAccess();
    }

    protected final Encoding getEncoding() {
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
    public final SQLException readStatusVector() throws SQLException {
        return readStatusVector(getXdrIn());
    }

    /**
     * Process the status vector from {@code xdrIn} and returns the associated {@link SQLException} instance.
     *
     * @param xdrIn
     *         XDR input stream to read from
     * @return SQLException from the status vector
     * @throws SQLException
     *         for errors reading or processing the status vector
     * @see FbWireOperations#readStatusVector()
     */
    protected final SQLException readStatusVector(XdrInputStream xdrIn) throws SQLException {
        final boolean debug = log.isDebugEnabled();
        final FbExceptionBuilder builder = new FbExceptionBuilder();
        try {
            while (true) {
                int arg = xdrIn.readInt();
                int errorCode;
                switch (arg) {
                case isc_arg_gds:
                    errorCode = xdrIn.readInt();
                    if (debug) log.debugf("readStatusVector arg:isc_arg_gds int: %d", errorCode);
                    if (errorCode != 0) {
                        builder.exception(errorCode);
                    }
                    break;
                case isc_arg_warning:
                    errorCode = xdrIn.readInt();
                    if (debug) log.debugf("readStatusVector arg:isc_arg_warning int: %d", errorCode);
                    if (errorCode != 0) {
                        builder.warning(errorCode);
                    }
                    break;
                case isc_arg_interpreted:
                case isc_arg_string:
                    String stringValue = xdrIn.readString(getEncoding());
                    log.debugf("readStatusVector string: %s", stringValue);
                    builder.messageParameter(stringValue);
                    break;
                case isc_arg_sql_state:
                    String sqlState = xdrIn.readString(getEncoding());
                    log.debugf("readStatusVector sqlstate: %s", sqlState);
                    builder.sqlState(sqlState);
                    break;
                case isc_arg_number:
                    int intValue = xdrIn.readInt();
                    if (debug) log.debugf("readStatusVector arg:isc_arg_number int: %d", intValue);
                    builder.messageParameter(intValue);
                    break;
                case isc_arg_end:
                    if (builder.isEmpty()) {
                        return null;
                    }
                    return builder.toFlatSQLException();
                default:
                    int e = xdrIn.readInt();
                    if (debug) log.debugf("readStatusVector arg: %d int: %d", arg, e);
                    builder.messageParameter(e);
                    break;
                }
            }
        } catch (IOException ioe) {
            throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(ioe).toSQLException();
        }
    }

    @Override
    public final Response readResponse(WarningMessageCallback warningCallback) throws SQLException, IOException {
        return readOperationResponse(readNextOperation(), warningCallback);
    }

    @Override
    public final Response readOperationResponse(int operationCode, WarningMessageCallback warningCallback)
            throws SQLException, IOException {
        Response response = processOperation(operationCode);
        processResponseWarnings(response, warningCallback);
        processResponse(response);
        return response;
    }

    @Override
    public final int readNextOperation() throws SQLException, IOException {
        try (LockCloseable ignored = withLock()) {
            processDeferredActions();
            return connection.readNextOperation();
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
    protected final Response processOperation(int operation) throws SQLException, IOException {
        final XdrInputStream xdrIn = getXdrIn();
        switch (operation) {
        case op_response:
            return new GenericResponse(xdrIn.readInt(), xdrIn.readLong(), xdrIn.readBuffer(), readStatusVector());
        case op_fetch_response:
            return new FetchResponse(xdrIn.readInt(), xdrIn.readInt());
        case op_sql_response:
            return new SqlResponse(xdrIn.readInt());
        case op_batch_cs:
            return readBatchCompletionResponse(xdrIn);
        case op_inline_blob:
            return readInlineBlobResponse(xdrIn);
        default:
            throw new FbExceptionBuilder().nonTransientException(JaybirdErrorCodes.jb_unexpectedOperationCode)
                    .messageParameter(operation)
                    .messageParameter("processOperation")
                    .toSQLException();
        }
    }

    /**
     * Reads the batch completion response ({@code op_batch_cs}) without reading the operation code itself.
     *
     * @param xdrIn
     *         XDR input stream to read
     * @return batch completion response
     * @throws SQLException
     *         for errors reading the response from the connection
     * @throws java.sql.SQLFeatureNotSupportedException
     *         when the protocol version does not support this response
     * @throws IOException
     *         for errors reading the response from the connection
     * @since 5
     */
    protected BatchCompletionResponse readBatchCompletionResponse(XdrInputStream xdrIn)
            throws SQLException, IOException {
        throw new FBDriverNotCapableException("Reading batch completion response not supported by " + this);
    }

    /**
     * Reads the inline blob response ({@code op_inline_blob}) without reading the operation code itself.
     *
     * @param xdrIn
     *         XDR input stream to read
     * @return inline blob response
     * @throws SQLException
     *         for errors reading the response from the connection
     * @throws java.sql.SQLFeatureNotSupportedException
     *         when the protocol version does not support this response
     * @throws IOException
     *         for errors reading the response from the connection
     * @since 5.0.8
     */
    protected InlineBlobResponse readInlineBlobResponse(XdrInputStream xdrIn) throws SQLException, IOException {
        throw new FBDriverNotCapableException("Reading inline blob response not supported by " + this);
    }

    /**
     * @param response
     *         Response to process
     * @throws java.sql.SQLException
     *         For errors returned from the server.
     */
    public final void processResponse(Response response) throws SQLException {
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
    public final void processResponseWarnings(final Response response, WarningMessageCallback warningCallback) {
        if (response instanceof GenericResponse) {
            GenericResponse genericResponse = (GenericResponse) response;
            SQLException exception = genericResponse.getException();
            if (exception instanceof SQLWarning) {
                orDefault(warningCallback).processWarning((SQLWarning) exception);
            }
        }
    }

    private WarningMessageCallback orDefault(WarningMessageCallback warningMessageCallback) {
        return warningMessageCallback != null ? warningMessageCallback : defaultWarningMessageCallback;
    }

    @Override
    public final GenericResponse readGenericResponse(WarningMessageCallback warningCallback)
            throws SQLException, IOException {
        return (GenericResponse) readResponse(warningCallback);
    }

    @Override
    public final SqlResponse readSqlResponse(WarningMessageCallback warningCallback) throws SQLException, IOException {
        return (SqlResponse) readResponse(warningCallback);
    }

    @Override
    public void handleCryptKeyCallback(DbCryptCallback dbCryptCallback) throws IOException, SQLException {
        throw new FBDriverNotCapableException("Crypt key callbacks not supported in this protocol version");
    }

    @Override
    public final void consumePackets(int numberOfResponses, WarningMessageCallback warningCallback) {
        while (numberOfResponses-- > 0) {
            try {
                readResponse(warningCallback);
            } catch (Exception e) {
                orDefault(warningCallback).processWarning(new SQLWarning(e));
                // ignoring exceptions
                log.warnDebug("Exception in consumePackets", e);
            }
        }
    }

    @Override
    public final void writeDirect(byte[] data) throws IOException {
        connection.writeDirect(data);
    }

    @Override
    public void setNetworkTimeout(int milliseconds) throws SQLException {
        if (milliseconds < 0) {
            throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_invalidTimeout).toSQLException();
        }
        connection.setSoTimeout(milliseconds);
    }

    protected final LockCloseable withLock() {
        return connection.withLockProxy();
    }

    protected final void addServerKeys(byte[] serverKeys) throws SQLException {
        connection.addServerKeys(serverKeys);
    }

    protected final void clearServerKeys() {
        connection.clearServerKeys();
    }

    protected final ClientAuthBlock getClientAuthBlock() {
        return connection.getClientAuthBlock();
    }

    /**
     * @return Immutable attach properties
     */
    protected final IAttachProperties<?> getAttachProperties() {
        return connection.getAttachProperties().asImmutable();
    }

    protected final List<KnownServerKey.PluginSpecificData> getPluginSpecificData() {
        return connection.getPluginSpecificData();
    }

    protected final WireConnection<?, ?> getConnection() {
        return connection;
    }

    protected final WarningMessageCallback getDefaultWarningMessageCallback() {
        return defaultWarningMessageCallback;
    }
}
