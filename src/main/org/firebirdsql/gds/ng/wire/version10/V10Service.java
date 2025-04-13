// SPDX-FileCopyrightText: Copyright 2015-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version10;

import org.firebirdsql.gds.ServiceParameterBuffer;
import org.firebirdsql.gds.ServiceRequestBuffer;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.gds.ng.dbcrypt.DbCryptCallback;
import org.firebirdsql.gds.ng.wire.*;

import java.io.IOException;
import java.sql.SQLException;

import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.*;

/**
 * {@link FbWireService} implementation for the V10 wire protocol
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public class V10Service extends AbstractFbWireService implements FbWireService {

    public V10Service(WireServiceConnection connection, ProtocolDescriptor descriptor) {
        super(connection, descriptor);
    }

    @Override
    @SuppressWarnings("java:S1141")
    public void attach() throws SQLException {
        try {
            checkConnected();
            requireNotAttached();
            try (var ignored = withLock()) {
                try {
                    sendAttach();
                    receiveAttachResponse();
                } catch (SQLException e) {
                    safelyDetach();
                    throw e;
                }
                setAttached();
                afterAttachActions();
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    private void sendAttach() throws SQLException {
        try {
            sendAttachToBuffer(protocolDescriptor.createAttachServiceParameterBuffer(connection));
            getXdrOut().flush();
        } catch (IOException e) {
            throw FbExceptionBuilder.ioWriteError(e);
        }
    }

    private void receiveAttachResponse() throws SQLException {
        try {
            authReceiveResponse(null);
        } catch (IOException e) {
            throw FbExceptionBuilder.ioReadError(e);
        }
    }

    /**
     * Processes the response from the server to the attach or create operation.
     *
     * @param genericResponse
     *         GenericResponse received from the server.
     */
    @SuppressWarnings("unused")
    protected void processAttachResponse(GenericResponse genericResponse) {
        // nothing to do
    }

    protected void afterAttachActions() throws SQLException {
        getServiceInfo(null, getDescribeServiceRequestBuffer(), 1024, getServiceInformationProcessor());
        // During connect and attach the socketTimeout might be set to the connectTimeout, now reset to 'normal' socketTimeout
        connection.resetSocketTimeout();
    }

    /**
     * Sends the buffer for op_service_attach
     *
     * @param spb
     *         Service parameter buffer
     * @throws SQLException
     *         If the connection is not open
     * @throws IOException
     *         For errors writing to the connection
     */
    protected void sendAttachToBuffer(ServiceParameterBuffer spb) throws SQLException, IOException {
        final XdrOutputStream xdrOut = getXdrOut();
        xdrOut.writeInt(op_service_attach); // p_operation
        xdrOut.writeInt(0); // p_atch_database - Service object ID
        xdrOut.writeString(connection.getAttachObjectName(), getEncoding()); // p_atch_file
        xdrOut.writeTyped(spb); // p_atch_dpb
    }

    @Override
    @SuppressWarnings("java:S1141")
    protected void internalDetach() throws SQLException {
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
                xdrOut.writeInt(op_service_detach);  // p_operation
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
            // Consume op_service_detach response
            wireOperations.readResponse(null);
        } catch (IOException e) {
            throw FbExceptionBuilder.ioReadError(e);
        }
    }

    @Override
    public byte[] getServiceInfo(ServiceParameterBuffer serviceParameterBuffer,
            ServiceRequestBuffer serviceRequestBuffer, int maxBufferLength) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkAttached();
            sendServiceInfo(serviceParameterBuffer, serviceRequestBuffer, maxBufferLength);
            return receiveServiceInfoResponse();
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    private void sendServiceInfo(ServiceParameterBuffer serviceParameterBuffer,
            ServiceRequestBuffer serviceRequestBuffer, int maxBufferLength) throws SQLException {
        try {
            XdrOutputStream xdrOut = getXdrOut();
            xdrOut.writeInt(op_service_info); // p_operation
            xdrOut.writeInt(0); // p_info_object
            xdrOut.writeInt(0); // p_info_incarnation
            xdrOut.writeBuffer(serviceParameterBuffer != null ? serviceParameterBuffer.toBytes() : null); // p_info_items
            xdrOut.writeBuffer(serviceRequestBuffer.toBytes()); // p_info_recv_items
            xdrOut.writeInt(maxBufferLength); // p_info_buffer_length

            xdrOut.flush();
        } catch (IOException e) {
            throw FbExceptionBuilder.ioWriteError(e);
        }
    }

    private byte[] receiveServiceInfoResponse() throws SQLException {
        try {
            return readGenericResponse(null).data();
        } catch (IOException e) {
            throw FbExceptionBuilder.ioReadError(e);
        }
    }

    @Override
    public void startServiceAction(ServiceRequestBuffer serviceRequestBuffer) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkAttached();
            sendServiceStart(serviceRequestBuffer);
            receiveServiceStartResponse();
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    private void sendServiceStart(ServiceRequestBuffer serviceRequestBuffer) throws SQLException {
        try {
            final XdrOutputStream xdrOut = getXdrOut();
            xdrOut.writeInt(op_service_start); // p_operation
            xdrOut.writeInt(0); // p_info_object
            xdrOut.writeInt(0); // p_info_incarnation
            xdrOut.writeBuffer(serviceRequestBuffer.toBytes()); // p_info_items

            xdrOut.flush();
        } catch (IOException e) {
            throw FbExceptionBuilder.ioWriteError(e);
        }
    }

    private void receiveServiceStartResponse() throws SQLException {
        try {
            readGenericResponse(null);
        } catch (IOException e) {
            throw FbExceptionBuilder.ioReadError(e);
        }
    }

    @Override
    public final void authReceiveResponse(AcceptPacket acceptPacket) throws IOException, SQLException {
        final DbCryptCallback dbCryptCallback = connection.createDbCryptCallback();
        wireOperations.authReceiveResponse(acceptPacket, dbCryptCallback, V10Service.this::processAttachResponse);
    }
}
