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
package org.firebirdsql.gds.ng.jna;

import com.sun.jna.ptr.IntByReference;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ServiceParameterBuffer;
import org.firebirdsql.gds.ServiceRequestBuffer;
import org.firebirdsql.gds.ng.AbstractFbService;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.ParameterConverter;
import org.firebirdsql.gds.ng.WarningMessageCallback;
import org.firebirdsql.jdbc.SQLStateConstants;
import org.firebirdsql.jna.fbclient.FbClientLibrary;
import org.firebirdsql.jna.fbclient.ISC_STATUS;

import java.nio.ByteBuffer;
import java.sql.SQLException;

/**
 * Implementation of {@link org.firebirdsql.gds.ng.FbService} for native client access.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public final class JnaService extends AbstractFbService<JnaServiceConnection> implements JnaAttachment {

    // TODO Find out if there are any exception from JNA that we need to be prepared to handle.

    private static final ParameterConverter<?, JnaServiceConnection> PARAMETER_CONVERTER = new JnaParameterConverter();
    public static final int STATUS_VECTOR_SIZE = 20;

    private final FbClientLibrary clientLibrary;
    private final IntByReference handle = new IntByReference(0);
    private final ISC_STATUS[] statusVector = new ISC_STATUS[STATUS_VECTOR_SIZE];

    public JnaService(JnaServiceConnection connection) {
        super(connection, connection.createDatatypeCoder());
        clientLibrary = connection.getClientLibrary();
    }

    @Override
    protected void checkConnected() throws SQLException {
        if (!isAttached()) {
            // TODO Update message / externalize
            throw new SQLException("The connection is not attached to a database",
                    SQLStateConstants.SQL_STATE_CONNECTION_ERROR);
        }
    }

    @Override
    public byte[] getServiceInfo(ServiceParameterBuffer serviceParameterBuffer,
            ServiceRequestBuffer serviceRequestBuffer, int maxBufferLength) throws SQLException {
        try {
            final byte[] serviceParameterBufferBytes = serviceParameterBuffer == null ? null
                    : serviceParameterBuffer.toBytesWithType();
            final byte[] serviceRequestBufferBytes =
                    serviceRequestBuffer == null ? null : serviceRequestBuffer.toBytes();
            final ByteBuffer responseBuffer = ByteBuffer.allocateDirect(maxBufferLength);
            synchronized (getSynchronizationObject()) {
                clientLibrary.isc_service_query(statusVector, handle, new IntByReference(0),
                        (short) (serviceParameterBufferBytes != null ? serviceParameterBufferBytes.length
                                : 0), serviceParameterBufferBytes,
                        (short) (serviceRequestBufferBytes != null ? serviceRequestBufferBytes.length
                                : 0), serviceRequestBufferBytes,
                        (short) maxBufferLength, responseBuffer);
                processStatusVector();
            }
            byte[] responseArray = new byte[maxBufferLength];
            responseBuffer.get(responseArray);
            return responseArray;
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public void startServiceAction(ServiceRequestBuffer serviceRequestBuffer) throws SQLException {
        try {
            final byte[] serviceRequestBufferBytes = serviceRequestBuffer == null
                    ? null
                    : serviceRequestBuffer.toBytes();
            synchronized (getSynchronizationObject()) {
                clientLibrary.isc_service_start(statusVector, handle, new IntByReference(0),
                        (short) (serviceRequestBufferBytes != null ? serviceRequestBufferBytes.length : 0),
                        serviceRequestBufferBytes);
                processStatusVector();
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public void attach() throws SQLException {
        try {
            if (isAttached()) {
                throw new SQLException("Already attached to a service");
            }
            final ServiceParameterBuffer spb = PARAMETER_CONVERTER.toServiceParameterBuffer(connection);
            final byte[] serviceName = getEncoding().encodeToCharset(connection.getAttachUrl());
            final byte[] spbArray = spb.toBytesWithType();

            synchronized (getSynchronizationObject()) {
                try {
                    clientLibrary.isc_service_attach(statusVector, (short) serviceName.length, serviceName, handle,
                            (short) spbArray.length, spbArray);
                    processStatusVector();
                } catch (SQLException ex) {
                    safelyDetach();
                    throw ex;
                } catch (Exception ex) {
                    safelyDetach();
                    // TODO Replace with specific error (eg native client error)
                    throw new FbExceptionBuilder()
                            .exception(ISCConstants.isc_network_error)
                            .messageParameter(connection.getServerName())
                            .cause(ex)
                            .toSQLException();
                }
                setAttached();
                afterAttachActions();
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    /**
     * Additional tasks to execute directly after attach operation.
     * <p>
     * Implementation retrieves service information like server version.
     * </p>
     *
     * @throws SQLException
     *         For errors reading or writing database information.
     */
    protected void afterAttachActions() throws SQLException {
        getServiceInfo(null, getDescribeServiceRequestBuffer(), 1024, getServiceInformationProcessor());
    }

    @Override
    protected void internalDetach() throws SQLException {
        synchronized (getSynchronizationObject()) {
            try {
                clientLibrary.isc_service_detach(statusVector, handle);
                processStatusVector();
            } catch (SQLException ex) {
                throw ex;
            } catch (Exception ex) {
                // TODO Replace with specific error (eg native client error)
                throw new FbExceptionBuilder()
                        .exception(ISCConstants.isc_network_error)
                        .messageParameter(connection.getServerName())
                        .cause(ex)
                        .toSQLException();
            } finally {
                setDetached();
            }
        }
    }

    @Override
    public int getHandle() {
        return handle.getValue();
    }

    public IntByReference getJnaHandle() {
        return handle;
    }

    private void processStatusVector() throws SQLException {
        processStatusVector(statusVector, getServiceWarningCallback());
    }

    public void processStatusVector(ISC_STATUS[] statusVector, WarningMessageCallback warningMessageCallback)
            throws SQLException {
        if (warningMessageCallback == null) {
            warningMessageCallback = getServiceWarningCallback();
        }
        connection.processStatusVector(statusVector, warningMessageCallback);
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (isAttached()) {
                safelyDetach();
            }
        } finally {
            super.finalize();
        }
    }
}
