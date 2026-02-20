// SPDX-FileCopyrightText: Copyright 2015-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.impl.wire.XdrInputStream;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.IAttachProperties;
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.gds.ng.WarningMessageCallback;
import org.firebirdsql.gds.ng.dbcrypt.DbCryptCallback;
import org.firebirdsql.gds.ng.wire.auth.ClientAuthBlock;
import org.firebirdsql.gds.ng.wire.crypt.KnownServerKey;
import org.firebirdsql.jdbc.FBDriverNotCapableException;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static org.firebirdsql.gds.ISCConstants.*;
import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.*;

/**
 * @author Mark Rotteveel
 * @since 3.0
 */
public abstract class AbstractWireOperations implements FbWireOperations {

    private static final System.Logger log = System.getLogger(AbstractWireOperations.class.getName());

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
    public final @Nullable SQLException readStatusVector() throws SQLException {
        return readStatusVector(getXdrIn());
    }

    /**
     * Process the status vector from {@code xdrIn} and returns the associated {@link SQLException} instance.
     *
     * @param xdrIn
     *         XDR input stream to read from
     * @return SQLException from the status vector, or {@code null} if there is no exception
     * @throws SQLException
     *         for errors reading or processing the status vector
     * @see FbWireOperations#readStatusVector()
     */
    protected final @Nullable SQLException readStatusVector(XdrInputStream xdrIn) throws SQLException {
        final FbExceptionBuilder builder = new FbExceptionBuilder();
        try {
            while (true) {
                int arg = xdrIn.readInt();
                switch (arg) {
                case isc_arg_gds -> {
                    int errorCode = xdrIn.readInt();
                    if (errorCode != 0) {
                        builder.exception(errorCode);
                    }
                }
                case isc_arg_warning -> {
                    int errorCode = xdrIn.readInt();
                    if (errorCode != 0) {
                        builder.warning(errorCode);
                    }
                }
                case isc_arg_interpreted, isc_arg_string -> builder.messageParameter(xdrIn.readString(getEncoding()));
                case isc_arg_sql_state -> builder.sqlState(xdrIn.readString(getEncoding()));
                case isc_arg_end -> {
                    if (builder.isEmpty()) {
                        return null;
                    }
                    return builder.toFlatSQLException();
                }
                default -> builder.messageParameter(xdrIn.readInt());
                }
            }
        } catch (IOException e) {
            throw FbExceptionBuilder.ioReadError(e);
        }
    }

    @Override
    public final Response readResponse(@Nullable WarningMessageCallback warningCallback)
            throws SQLException, IOException {
        return readOperationResponse(readNextOperation(), warningCallback);
    }

    @Override
    public final Response readOperationResponse(int operationCode, @Nullable WarningMessageCallback warningCallback)
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
        return switch (operation) {
            case op_response ->
                    new GenericResponse(
                            xdrIn.readInt(), // p_resp_object
                            xdrIn.readLong(), // p_resp_blob_id
                            xdrIn.readBuffer(), // p_resp_data
                            readStatusVector()); // p_resp_status_vector
            case op_fetch_response -> new FetchResponse(
                    xdrIn.readInt(), // p_sqldata_status
                    xdrIn.readInt()); // p_sqldata_messages
            case op_sql_response -> new SqlResponse(
                    xdrIn.readInt()); // p_sqldata_messages
            case op_batch_cs -> readBatchCompletionResponse(xdrIn);
            case op_inline_blob -> readInlineBlobResponse(xdrIn);
            default ->
                    throw FbExceptionBuilder.forNonTransientException(JaybirdErrorCodes.jb_unexpectedOperationCode)
                            .messageParameter(operation)
                            .messageParameter("processOperation")
                            .toSQLException();
        };
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
     * @since 7
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
        if (response instanceof GenericResponse genericResponse) {
            SQLException exception = genericResponse.exception();
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
    public final void processResponseWarnings(Response response, @Nullable WarningMessageCallback warningCallback) {
        if (response instanceof GenericResponse genericResponse
                && genericResponse.exception() instanceof SQLWarning warning) {
            requireNonNullElse(warningCallback, defaultWarningMessageCallback).processWarning(warning);
        }
    }

    @Override
    public final GenericResponse readGenericResponse(@Nullable WarningMessageCallback warningCallback)
            throws SQLException, IOException {
        return (GenericResponse) readResponse(warningCallback);
    }

    @Override
    public final SqlResponse readSqlResponse(@Nullable WarningMessageCallback warningCallback)
            throws SQLException, IOException {
        return (SqlResponse) readResponse(warningCallback);
    }

    @Override
    public void handleCryptKeyCallback(DbCryptCallback dbCryptCallback) throws IOException, SQLException {
        throw new FBDriverNotCapableException("Crypt key callbacks not supported in this protocol version");
    }

    @Override
    public final void consumePackets(int numberOfResponses, @Nullable WarningMessageCallback warningCallback) {
        while (numberOfResponses-- > 0) {
            try {
                readResponse(warningCallback);
            } catch (Exception e) {
                requireNonNullElse(warningCallback, defaultWarningMessageCallback).processWarning(new SQLWarning(e));
                // ignoring exceptions
                log.log(System.Logger.Level.WARNING, "Exception in consumePackets; see debug level for stacktrace");
                log.log(System.Logger.Level.DEBUG, "Exception in consumePackets", e);
            }
        }
    }

    @Override
    public void setNetworkTimeout(int milliseconds) throws SQLException {
        if (milliseconds < 0) {
            throw FbExceptionBuilder.toException(JaybirdErrorCodes.jb_invalidTimeout);
        }
        connection.setSoTimeout(milliseconds);
    }

    protected final LockCloseable withLock() {
        return connection.withLockProxy();
    }

    protected final void addServerKeys(byte @Nullable [] serverKeys) throws SQLException {
        connection.addServerKeys(serverKeys);
    }

    protected final void clearServerKeys() {
        connection.clearServerKeys();
    }

    protected final @Nullable ClientAuthBlock getClientAuthBlock() {
        return connection.getClientAuthBlock();
    }

    /**
     * @return Immutable attach properties
     */
    @SuppressWarnings("java:S1452")
    protected final IAttachProperties<?> getAttachProperties() {
        return connection.getAttachProperties().asImmutable();
    }

    protected final List<KnownServerKey.PluginSpecificData> getPluginSpecificData() {
        return connection.getPluginSpecificData();
    }

    @SuppressWarnings("java:S1452")
    protected final WireConnection<?, ?> getConnection() {
        return connection;
    }

    protected final WarningMessageCallback getDefaultWarningMessageCallback() {
        return defaultWarningMessageCallback;
    }
}
