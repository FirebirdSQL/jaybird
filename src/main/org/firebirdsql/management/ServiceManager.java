package org.firebirdsql.management;

import java.io.IOException;
import java.io.OutputStream;

import org.firebirdsql.gds.GDS;
import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.GDSFactory;
import org.firebirdsql.gds.GDSType;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ServiceParameterBuffer;
import org.firebirdsql.gds.ServiceRequestBuffer;
import org.firebirdsql.gds.isc_svc_handle;
import org.firebirdsql.jdbc.FBSQLException;


/**
 * 
 */
public class ServiceManager {

    private GDSType gdsType;
    
    private String user;
    private String password;

    private String database;
    
    private String host;
    private int port = 3050;
    
    private OutputStream logger;

    /**
     * 
     */
    protected ServiceManager(GDSType gdsType) {
        this.gdsType = gdsType;
    }

    /**
     * Set the name of the user that performs the operation.
     * 
     * @param user name of the user.
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * Get name of the user that performs the operation.
     * 
     * @return name of the user that performs the operation.
     */
    public String getUser() {
        return user;
    }

    /**
     * @param password
     *            The password to set.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return Returns the password.
     */
    public String getPassword() {
        return password;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getDatabase() {
        return database;
    }
    
    /**
     * @return Returns the host.
     */
    public String getHost() {
        return host;
    }
    /**
     * @param host The host to set.
     */
    public void setHost(String host) {
        this.host = host;
    }
    /**
     * @return Returns the port.
     */
    public int getPort() {
        return port;
    }
    /**
     * @param port The port to set.
     */
    public void setPort(int port) {
        this.port = port;
    }
    /**
     * @return Returns the out.
     */
    public OutputStream getLogger() {
        return logger;
    }
    
    /**
     * @param logger The out to set.
     */
    public void setLogger(OutputStream logger) {
        this.logger = logger;
    }

    /**
     * Get {@link GDS} implementation depending on the type specified
     * during instantiation.
     * 
     * @return instance of {@link GDS}.
     */
    protected GDS getGds() {
        return GDSFactory.getGDSForType(gdsType);
    }
    
    protected String getServiceName() {
        StringBuffer sb = new StringBuffer();
        if (getHost() != null) {
            
            sb.append(getHost());
            
            if (getPort() != 3050) {
                sb.append("/");
                sb.append(getPort());
            }
            
            sb.append(":");
        }
        sb.append("service_mgr");
        return sb.toString();
    }
    
    protected isc_svc_handle attachServiceManager(GDS gds) throws GDSException {
        ServiceParameterBuffer serviceParameterBuffer = 
            gds.newServiceParameterBuffer();
    
//        serviceParameterBuffer.addArgument(
//            ISCConstants.isc_spb_current_version);
        
        serviceParameterBuffer.addArgument(
            ISCConstants.isc_spb_user_name, getUser());
        
        serviceParameterBuffer.addArgument(
            ISCConstants.isc_spb_password, getPassword());

        serviceParameterBuffer.addArgument(
            ISCConstants.isc_spb_dummy_packet_interval, new byte[]{120, 10, 0, 0});

        final isc_svc_handle handle = gds.get_new_isc_svc_handle();
        gds.isc_service_attach(getServiceName(), handle, serviceParameterBuffer);
        
        return handle;
    }
    
    protected void detachServiceManager(GDS gds, isc_svc_handle handle) throws GDSException {
        gds.isc_service_detach(handle);
    }

    protected void queueService(GDS gds, isc_svc_handle handle) throws GDSException, FBSQLException, IOException {
        ServiceRequestBuffer infoSRB = gds.newServiceRequestBuffer(ISCConstants.isc_info_svc_to_eof);

        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        
        boolean processing = true;
        while (processing) {
            gds.isc_service_query(handle, gds.newServiceParameterBuffer() , infoSRB, buffer);
    
            switch(buffer[0]) {
                
                case ISCConstants.isc_info_svc_to_eof :
                
                    int dataLength = (buffer[1] & 0xff) | ((buffer[2] & 0xff) << 8);
                    if (dataLength == 0) {
                        if (buffer[3] != ISCConstants.isc_info_end)
                            throw new FBSQLException("Unexpected end of stream reached.");
                        else {
                            processing = false;
                            break;
                        }
                    }

                    getLogger().write(buffer, 3, dataLength);
                    break;
                    
                case ISCConstants.isc_info_truncated :
                    bufferSize = bufferSize * 2;
                    buffer = new byte[bufferSize];
                    break;
                    
                case ISCConstants.isc_info_end : 
                    processing = false;
                    break;
            }
        }
    }
}
