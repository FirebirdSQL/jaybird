package org.firebirdsql.nativeoo.gds.ng;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.ServiceParameterBuffer;
import org.firebirdsql.gds.ServiceRequestBuffer;
import org.firebirdsql.gds.impl.ServiceParameterBufferImp;
import org.firebirdsql.gds.impl.ServiceRequestBufferImp;
import org.firebirdsql.gds.ng.AbstractFbService;
import org.firebirdsql.gds.ng.FbAttachment;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.ParameterConverter;
import org.firebirdsql.jna.fbclient.FbClientLibrary;
import org.firebirdsql.nativeoo.gds.ng.FbInterface.*;

import java.sql.SQLException;

/**
 * Implementation of {@link FbInterface.IService} for native client access using OO API.
 *
 * @author <a href="mailto:vasiliy.yashkov@red-soft.ru">Vasiliy Yashkov</a>
 * @since 4.0
 */
public class IServiceImpl extends AbstractFbService<IServiceConnectionImpl> implements FbAttachment {

    // TODO Find out if there are any exception from JNA that we need to be prepared to handle.

    private static final ParameterConverter<?, IServiceConnectionImpl> PARAMETER_CONVERTER = new IParameterConverterImpl();
    private final FbClientLibrary clientLibrary;
    private final IMaster master;
    private final IProvider provider;
    private final IStatus status;
    private IService service;

    public IServiceImpl(IServiceConnectionImpl connection) {
        super(connection, connection.createDatatypeCoder());
        clientLibrary = connection.getClientLibrary();
        master = clientLibrary.fb_get_master_interface();
        status = master.getStatus();
        provider = master.getDispatcher();
    }

    @Override
    public ServiceParameterBuffer createServiceParameterBuffer() {
        // TODO When Firebird 3, use UTF-8; implement similar mechanism as ProtocolDescriptor of wire?
        return new ServiceParameterBufferImp(ServiceParameterBufferImp.SpbMetaData.SPB_VERSION_2, getEncoding());
    }

    @Override
    public ServiceRequestBuffer createServiceRequestBuffer() {
        // TODO When Firebird 3, use UTF-8; implement similar mechanism as ProtocolDescriptor of wire?
        return new ServiceRequestBufferImp(ServiceRequestBufferImp.SrbMetaData.SRB_VERSION_2, getEncoding());
    }

    @Override
    protected void checkConnected() throws SQLException {
        if (!isAttached()) {
            throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_notAttachedToDatabase)
                    .toFlatSQLException();
        }
    }

    @Override
    public byte[] getServiceInfo(ServiceParameterBuffer serviceParameterBuffer,
                                 ServiceRequestBuffer serviceRequestBuffer, int maxBufferLength) throws SQLException {
        checkConnected();
        try {
            final byte[] serviceParameterBufferBytes = serviceParameterBuffer == null ? null
                    : serviceParameterBuffer.toBytesWithType();
            final byte[] serviceRequestBufferBytes =
                    serviceRequestBuffer == null ? null : serviceRequestBuffer.toBytes();
            final byte[] responseBuffer = new byte[maxBufferLength];
            synchronized (getSynchronizationObject()) {
                service.query(status, (serviceParameterBufferBytes != null ? serviceParameterBufferBytes.length
                                : 0), serviceParameterBufferBytes,
                        (serviceRequestBufferBytes != null ? serviceRequestBufferBytes.length
                                : 0), serviceRequestBufferBytes,
                        maxBufferLength, responseBuffer);
            }
            return responseBuffer;
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public void startServiceAction(ServiceRequestBuffer serviceRequestBuffer) throws SQLException {
        checkConnected();
        try {
            final byte[] serviceRequestBufferBytes = serviceRequestBuffer == null
                    ? null
                    : serviceRequestBuffer.toBytes();
            synchronized (getSynchronizationObject()) {
                service.start(status, (serviceRequestBufferBytes != null ? serviceRequestBufferBytes.length : 0),
                        serviceRequestBufferBytes);
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
                    service = provider.attachServiceManager(status, connection.getAttachUrl(), spbArray.length, spbArray);
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

    @Override
    public int getHandle() {
        throw new UnsupportedOperationException( "Native OO API not support service handle" );
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
        checkConnected();
        synchronized (getSynchronizationObject()) {
            try {
                service.detach(status);
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
