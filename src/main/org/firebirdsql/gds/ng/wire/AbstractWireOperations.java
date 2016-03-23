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
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.impl.wire.XdrInputStream;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.WarningMessageCallback;
import org.firebirdsql.gds.ng.wire.auth.ClientAuthBlock;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLWarning;

import static org.firebirdsql.gds.ISCConstants.*;
import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.*;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public abstract class AbstractWireOperations implements FbWireOperations {

    private static final Logger log = LoggerFactory.getLogger(AbstractWireOperations.class);

    private final WireConnection<?, ?> connection;
    private final WarningMessageCallback defaultWarningMessageCallback;
    private final Object syncObject;

    protected AbstractWireOperations(WireConnection<?, ?> connection,
            WarningMessageCallback defaultWarningMessageCallback, Object syncObject) {
        this.connection = connection;
        this.defaultWarningMessageCallback = defaultWarningMessageCallback;
        this.syncObject = syncObject;
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
        boolean debug = log.isDebugEnabled();
        final FbExceptionBuilder builder = new FbExceptionBuilder();
        final XdrInputStream xdrIn = getXdrIn();
        try {
            while (true) {
                int arg = xdrIn.readInt();
                int errorCode;
                switch (arg) {
                case isc_arg_gds:
                    errorCode = xdrIn.readInt();
                    if (debug) log.debug("readStatusVector arg:isc_arg_gds int: " + errorCode);
                    if (errorCode != 0) {
                        builder.exception(errorCode);
                    }
                    break;
                case isc_arg_warning:
                    errorCode = xdrIn.readInt();
                    if (debug) log.debug("readStatusVector arg:isc_arg_warning int: " + errorCode);
                    if (errorCode != 0) {
                        builder.warning(errorCode);
                    }
                    break;
                case isc_arg_interpreted:
                case isc_arg_string:
                    String stringValue = xdrIn.readString(getEncoding());
                    if (debug) log.debug("readStatusVector string: " + stringValue);
                    builder.messageParameter(stringValue);
                    break;
                case isc_arg_sql_state:
                    String sqlState = xdrIn.readString(getEncoding());
                    if (debug) log.debug("readStatusVector sqlstate: " + sqlState);
                    builder.sqlState(sqlState);
                    break;
                case isc_arg_number:
                    int intValue = xdrIn.readInt();
                    if (debug) log.debug("readStatusVector arg:isc_arg_number int: " + intValue);
                    builder.messageParameter(intValue);
                    break;
                case isc_arg_end:
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

    @Override
    public final Response readResponse(WarningMessageCallback warningCallback) throws SQLException, IOException {
        Response response = readSingleResponse(warningCallback);
        processResponse(response);
        return response;
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
    public final Response readSingleResponse(WarningMessageCallback warningCallback) throws SQLException, IOException {
        Response response = processOperation(readNextOperation());
        processResponseWarnings(response, warningCallback);
        return response;
    }

    /**
     * Reads the next operation. Forwards call to {@link WireConnection#readNextOperation()}.
     *
     * @return next operation
     * @throws java.io.IOException
     */
    public final int readNextOperation() throws IOException {
        synchronized (syncObject) {
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
        default:
            log.warn(String.format("Unsupported or unexpected operation code %d in processOperation", operation));
            // TODO throw an exception instead
            return null;
        }
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
        if (warningCallback == null) {
            warningCallback = defaultWarningMessageCallback;
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
    public final void consumePackets(int numberOfResponses, WarningMessageCallback warningCallback) {
        while (numberOfResponses > 0) {
            numberOfResponses--;
            try {
                readResponse(warningCallback);
            } catch (Exception e) {
                // TODO Wrap in SQLWarning and register on warning callback?
                // ignoring exceptions
                log.warn("Exception in consumePackets", e);
            }
        }
    }

    @Override
    public final void writeDirect(byte[] data) throws IOException {
        connection.writeDirect(data);
    }

    protected final Object getSynchronizationObject() {
        return syncObject;
    }

    protected final void addServerKeys(byte[] serverKeys) throws SQLException {
        connection.addServerKeys(serverKeys);
    }

    protected final ClientAuthBlock getClientAuthBlock() {
        return connection.getClientAuthBlock();
    }
}
