/*
 * $Id$
 *
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

import org.firebirdsql.gds.*;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSServerVersion;
import org.firebirdsql.gds.impl.GDSServerVersionException;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.jdbc.FBSQLException;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;

/**
 * An implementation of the basic Firebird Service API functionality.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class FBServiceManager implements ServiceManager {

    private String user;
    private String password;
    private String database;
    private String host;
    private int port = 3050;

    private OutputStream logger;

    private GDS gds;

    public final static int BUFFER_SIZE = 1024; //1K

    /**
     * Create a new instance of <code>FBServiceManager</code> based on
     * the default GDSType.
     */
    public FBServiceManager() {
        this.gds = GDSFactory.getGDSForType(GDSFactory.getDefaultGDSType());
    }

    /**
     * Create a new instance of <code>FBServiceManager</code> based on
     * a given GDSType.
     *
     * @param gdsType
     *         type must be PURE_JAVA, EMBEDDED, or NATIVE
     */
    public FBServiceManager(String gdsType) {
        this.gds = GDSFactory.getGDSForType(GDSType.getType(gdsType));
    }

    /**
     * Create a new instance of <code>FBServiceManager</code> based on
     * a given GDSType.
     *
     * @param gdsType
     *         The GDS implementation type to use
     */
    public FBServiceManager(GDSType gdsType) {
        this.gds = GDSFactory.getGDSForType(gdsType);
    }

    /**
     * Set the name of the user that performs the operation.
     *
     * @param user
     *         name of the user.
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
     *         The password to set.
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
     * @param host
     *         The host to set.
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
     * @param port
     *         The port to set.
     */
    public void setPort(int port) {
        this.port = port;
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

    /**
     * Get {@link GDS} implementation depending on the type specified
     * during instantiation.
     *
     * @return instance of {@link GDS}.
     */
    public GDS getGds() {
        return gds;
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

    public IscSvcHandle attachServiceManager(GDS gds) throws GDSException {
        ServiceParameterBuffer serviceParameterBuffer =
                gds.createServiceParameterBuffer();

        if (getUser() != null)
            serviceParameterBuffer.addArgument(
                    ISCConstants.isc_spb_user_name, getUser());

        if (getPassword() != null)
            serviceParameterBuffer.addArgument(
                    ISCConstants.isc_spb_password, getPassword());

        serviceParameterBuffer.addArgument(
                ISCConstants.isc_spb_dummy_packet_interval, new byte[] { 120, 10, 0, 0 });

        final IscSvcHandle handle = gds.createIscSvcHandle();
        gds.iscServiceAttach(getServiceName(), handle, serviceParameterBuffer);

        return handle;
    }

    public void detachServiceManager(GDS gds, IscSvcHandle handle) throws GDSException {
        gds.iscServiceDetach(handle);
    }

    public void queueService(GDS gds, IscSvcHandle handle) throws GDSException, FBSQLException, IOException {

        OutputStream currentLogger = getLogger();

        ServiceRequestBuffer infoSRB = gds.createServiceRequestBuffer(ISCConstants.isc_info_svc_to_eof);

        int bufferSize = BUFFER_SIZE;
        byte[] buffer = new byte[bufferSize];

        boolean processing = true;
        while (processing) {
            gds.iscServiceQuery(handle, gds.createServiceParameterBuffer(), infoSRB, buffer);

            switch (buffer[0]) {

            case ISCConstants.isc_info_svc_to_eof:

                int dataLength = (buffer[1] & 0xff) | ((buffer[2] & 0xff) << 8);
                if (dataLength == 0) {
                    if (buffer[3] != ISCConstants.isc_info_end)
                        throw new FBSQLException("Unexpected end of stream reached.");
                    else {
                        processing = false;
                        break;
                    }
                }

                if (currentLogger != null)
                    currentLogger.write(buffer, 3, dataLength);

                break;

            case ISCConstants.isc_info_truncated:
                bufferSize = bufferSize * 2;
                buffer = new byte[bufferSize];
                break;

            case ISCConstants.isc_info_end:
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
     * @throws FBSQLException
     *         if a database access error occurs or
     *         incorrect parameters are supplied
     */
    protected void executeServicesOperation(ServiceRequestBuffer srb)
            throws FBSQLException {

        try {
            IscSvcHandle svcHandle = attachServiceManager(gds);
            try {
                gds.iscServiceStart(svcHandle, srb);
                queueService(gds, svcHandle);
            } finally {
                detachServiceManager(gds, svcHandle);
            }
        } catch (GDSException gdse) {
            throw new FBSQLException(gdse);
        } catch (IOException ioe) {
            throw new FBSQLException(ioe);
        }
    }

    /**
     * Build up a request buffer for the specified operation.
     *
     * @param operation
     *         The isc_action_svc_* operation
     * @param options
     *         The options bitmask for the request buffer
     */
    protected ServiceRequestBuffer createRequestBuffer(int operation,
            int options) {
        ServiceRequestBuffer srb = gds.createServiceRequestBuffer(operation);
        if (getDatabase() != null) {
            srb.addArgument(ISCConstants.isc_spb_dbname, getDatabase());
        }
        srb.addArgument(ISCConstants.isc_spb_options, options);
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
        final ServiceParameterBuffer spb = gds.createServiceParameterBuffer();
        final ServiceRequestBuffer srb = gds.createServiceRequestBuffer(ISCConstants.isc_info_svc_server_version);
        byte[] displayBuffer = new byte[256];
        try {
            IscSvcHandle svcHandle = attachServiceManager(gds);
            try {
                gds.iscServiceQuery(svcHandle, spb, srb, displayBuffer);
            } finally {
                detachServiceManager(gds, svcHandle);
            }

            // Assuming the specified buffer is long enough, not checking for truncation
            int length = getGds().iscVaxInteger(displayBuffer, 1, 2);
            String value = new String(displayBuffer, 3, length);
            try {
                return GDSServerVersion.parseRawVersion(value);
            } catch (GDSServerVersionException ex) {
                return GDSServerVersion.INVALID_VERSION;
            }
        } catch (GDSException gdse) {
            throw new FBSQLException(gdse);
        }
    }
}
