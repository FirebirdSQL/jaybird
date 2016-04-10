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
package org.firebirdsql.management;

import org.firebirdsql.gds.ServiceRequestBuffer;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSServerVersion;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.ng.FbDatabaseFactory;
import org.firebirdsql.gds.ng.FbService;
import org.firebirdsql.gds.ng.FbServiceProperties;
import org.firebirdsql.gds.ng.IServiceProperties;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;

import static org.firebirdsql.gds.ISCConstants.*;
import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger2;

/**
 * An implementation of the basic Firebird Service API functionality.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class FBServiceManager implements ServiceManager {

    private final IServiceProperties serviceProperties = new FbServiceProperties();
    private FbDatabaseFactory dbFactory;
    private String database;
    private OutputStream logger;

    public final static int BUFFER_SIZE = 1024; //1K

    /**
     * Create a new instance of <code>FBServiceManager</code> based on
     * the default GDSType.
     */
    public FBServiceManager() {
        this(GDSFactory.getDefaultGDSType());
    }

    /**
     * Create a new instance of <code>FBServiceManager</code> based on
     * a given GDSType.
     *
     * @param gdsType
     *         type must be PURE_JAVA, EMBEDDED, or NATIVE
     */
    public FBServiceManager(String gdsType) {
        this(GDSType.getType(gdsType));
    }

    /**
     * Create a new instance of <code>FBServiceManager</code> based on
     * a given GDSType.
     *
     * @param gdsType
     *         The GDS implementation type to use
     */
    public FBServiceManager(GDSType gdsType) {
        dbFactory = GDSFactory.getDatabaseFactoryForType(gdsType);
    }

    @Override
    public void setCharSet(String charSet) {
        serviceProperties.setCharSet(charSet);
    }

    @Override
    public String getCharSet() {
        return serviceProperties.getCharSet();
    }

    /**
     * Set the name of the user that performs the operation.
     *
     * @param user
     *         name of the user.
     */
    public void setUser(String user) {
        serviceProperties.setUser(user);
    }

    /**
     * Get name of the user that performs the operation.
     *
     * @return name of the user that performs the operation.
     */
    public String getUser() {
        return serviceProperties.getUser();
    }

    /**
     * @param password
     *         The password to set.
     */
    public void setPassword(String password) {
        serviceProperties.setPassword(password);
    }

    /**
     * @return Returns the password.
     */
    public String getPassword() {
        return serviceProperties.getPassword();
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
        return serviceProperties.getServerName();
    }

    /**
     * @param host
     *         The host to set.
     */
    public void setHost(String host) {
        serviceProperties.setServerName(host);
    }

    /**
     * @return Returns the port.
     */
    public int getPort() {
        return serviceProperties.getPortNumber();
    }

    /**
     * @param port
     *         The port to set.
     */
    public void setPort(int port) {
        serviceProperties.setPortNumber(port);
    }

    /**
     * @return Returns the out.
     */
    public synchronized OutputStream getLogger() {
        return logger;
    }

    /**
     * @param logger
     *         The out to set.
     */
    public synchronized void setLogger(OutputStream logger) {
        this.logger = logger;
    }

    public String getServiceName() {
        StringBuilder sb = new StringBuilder();
        if (getHost() != null) {

            sb.append(getHost());

            if (getPort() != 3050) {
                sb.append('/');
                sb.append(getPort());
            }

            sb.append(':');
        }
        sb.append("service_mgr");
        return sb.toString();
    }

    public FbService attachServiceManager() throws SQLException {
        FbService fbService = dbFactory.serviceConnect(serviceProperties);
        fbService.attach();
        return fbService;
    }

    public void queueService(FbService service) throws SQLException, IOException {
        OutputStream currentLogger = getLogger();

        ServiceRequestBuffer infoSRB = service.createServiceRequestBuffer();
        infoSRB.addArgument(isc_info_svc_to_eof);

        int bufferSize = BUFFER_SIZE;

        boolean processing = true;
        while (processing) {
            byte[] buffer = service.getServiceInfo(null, infoSRB, bufferSize);

            switch (buffer[0]) {
            case isc_info_svc_to_eof:

                int dataLength = iscVaxInteger2(buffer, 1);
                if (dataLength == 0) {
                    if (buffer[3] != isc_info_end)
                        throw new SQLException("Unexpected end of stream reached.");
                    else {
                        processing = false;
                        break;
                    }
                }

                if (currentLogger != null) {
                    currentLogger.write(buffer, 3, dataLength);
                }

                break;

            case isc_info_truncated:
                bufferSize = bufferSize * 2;
                break;

            case isc_info_end:
                processing = false;
                break;
            }
        }
    }

    /**
     * Execute a Services API operation in the database. All output from the
     * operation is sent to this <code>ServiceManager</code>'s logger.
     *
     * @param srb
     *         The buffer containing the task request
     * @throws SQLException
     *         if a database access error occurs or
     *         incorrect parameters are supplied
     * @deprecated Use {@link #executeServicesOperation(FbService, ServiceRequestBuffer)}.
     */
    @SuppressWarnings("unused")
    @Deprecated
    protected void executeServicesOperation(ServiceRequestBuffer srb) throws SQLException {
        try (FbService service = attachServiceManager()) {
            service.startServiceAction(srb);
            queueService(service);
        } catch (IOException ioe) {
            throw new SQLException(ioe);
        }
    }

    protected final void executeServicesOperation(FbService service, ServiceRequestBuffer srb) throws SQLException {
        try {
            service.startServiceAction(srb);
            queueService(service);
        } catch (IOException ioe) {
            throw new SQLException(ioe);
        }
    }

    protected ServiceRequestBuffer createRequestBuffer(FbService service, int operation, int options) {
        ServiceRequestBuffer srb = service.createServiceRequestBuffer();
        srb.addArgument(operation);
        if (getDatabase() != null) {
            srb.addArgument(isc_spb_dbname, getDatabase(), service.getEncoding());
        }
        srb.addArgument(isc_spb_options, options);
        return srb;
    }

    /**
     * Obtains the server version through a service call.
     *
     * @return Parsed server version, or {@link org.firebirdsql.gds.impl.GDSServerVersion#INVALID_VERSION} if parsing
     * failed.
     * @throws SQLException
     *         For errors connecting to the service manager.
     */
    public GDSServerVersion getServerVersion() throws SQLException {
        try (FbService service = attachServiceManager()) {
            return service.getServerVersion();
        }
    }
}
