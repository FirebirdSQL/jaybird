/*
 SPDX-FileCopyrightText: Copyright 2004-2005 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2004-2005 Steven Jardine
 SPDX-FileCopyrightText: Copyright 2005 Gabriel Reid
 SPDX-FileCopyrightText: Copyright 2012-2026 Mark Rotteveel
 SPDX-License-Identifier: LGPL-2.1-or-later
*/
package org.firebirdsql.management;

import org.firebirdsql.gds.ServiceRequestBuffer;
import org.firebirdsql.gds.impl.DbAttachInfo;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSServerVersion;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.ng.*;
import org.firebirdsql.jaybird.props.PropertyConstants;
import org.firebirdsql.jaybird.props.PropertyNames;
import org.firebirdsql.jaybird.props.def.ConnectionProperty;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Map;

import static org.firebirdsql.gds.ISCConstants.isc_info_end;
import static org.firebirdsql.gds.ISCConstants.isc_info_svc_to_eof;
import static org.firebirdsql.gds.ISCConstants.isc_info_truncated;
import static org.firebirdsql.gds.ISCConstants.isc_infunk;
import static org.firebirdsql.jaybird.fb.constants.SpbItems.isc_spb_dbname;
import static org.firebirdsql.jaybird.fb.constants.SpbItems.isc_spb_options;
import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger2;

/**
 * An implementation of the basic Firebird Service API functionality.
 *
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 */
public class FBServiceManager implements ServiceManager {

    private final IServiceProperties serviceProperties = new FbServiceProperties();
    private final FbDatabaseFactory dbFactory;
    private @Nullable String database;
    private @Nullable OutputStream logger;

    public static final int BUFFER_SIZE = 1024; //1K

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
        serviceProperties.setType(gdsType.toString());
        dbFactory = GDSFactory.getDatabaseFactoryForType(gdsType);
    }

    @Override
    public final void setType(@Nullable String type) {
        throw new IllegalStateException("Type must be specified on construction");
    }

    // NOTE: we're redirecting the default implementations of the interface here to ensure the
    //  service manager can be introspected as a JavaBean (default methods are not returned by the introspector)

    @Override
    public void setCharSet(@Nullable String charSet) {
        ServiceManager.super.setCharSet(charSet);
    }

    @Override
    public @Nullable String getCharSet() {
        return ServiceManager.super.getCharSet();
    }

    @Override
    public void setUser(@Nullable String user) {
        ServiceManager.super.setUser(user);
    }

    @Override
    public @Nullable String getUser() {
        return ServiceManager.super.getUser();
    }

    @Override
    public void setPassword(@Nullable String password) {
        ServiceManager.super.setPassword(password);
    }

    @Override
    public @Nullable String getPassword() {
        return ServiceManager.super.getPassword();
    }

    @Override
    public @Nullable String getServerName() {
        return ServiceManager.super.getServerName();
    }

    @Override
    public void setServerName(@Nullable String serverName) {
        ServiceManager.super.setServerName(serverName);
    }

    @Override
    public int getPortNumber() {
        return ServiceManager.super.getPortNumber();
    }

    @Override
    public void setPortNumber(int portNumber) {
        ServiceManager.super.setPortNumber(portNumber);
    }

    @Override
    public String getServiceName() {
        return ServiceManager.super.getServiceName();
    }

    @Override
    public void setServiceName(@Nullable String serviceName) {
        ServiceManager.super.setServiceName(serviceName);
    }

    /**
     * {@inheritDoc}
     * <p>
     * NOTE: The {@link #setDatabase(String)} property will also set this property, so in general this property doesn't
     * need to be set explicitly.
     * </p>
     */
    @Override
    public void setExpectedDb(@Nullable String expectedDb) {
        ServiceManager.super.setExpectedDb(expectedDb);
    }

    @Override
    public @Nullable String getExpectedDb() {
        return ServiceManager.super.getExpectedDb();
    }

    @Override
    @NullUnmarked
    public void setDatabase(String database) {
        this.database = database;
        setExpectedDb(database);
    }

    @Override
    public @Nullable String getDatabase() {
        return database;
    }

    @Override
    public String getWireCrypt() {
        return ServiceManager.super.getWireCrypt();
    }

    @Override
    public void setWireCrypt(@Nullable String wireCrypt) {
        ServiceManager.super.setWireCrypt(wireCrypt);
    }

    @Override
    public WireCrypt getWireCryptAsEnum() {
        return serviceProperties.getWireCryptAsEnum();
    }

    @Override
    public void setWireCryptAsEnum(WireCrypt wireCrypt) {
        serviceProperties.setWireCryptAsEnum(wireCrypt);
    }

    @Override
    public @Nullable String getDbCryptConfig() {
        return ServiceManager.super.getDbCryptConfig();
    }

    @Override
    public void setDbCryptConfig(@Nullable String dbCryptConfig) {
        ServiceManager.super.setDbCryptConfig(dbCryptConfig);
    }

    @Override
    public String getAuthPlugins() {
        return ServiceManager.super.getAuthPlugins();
    }

    @Override
    public void setAuthPlugins(@Nullable String authPlugins) {
        ServiceManager.super.setAuthPlugins(authPlugins);
    }

    @Override
    public boolean isWireCompression() {
        return ServiceManager.super.isWireCompression();
    }

    @Override
    public void setWireCompression(boolean wireCompression) {
        ServiceManager.super.setWireCompression(wireCompression);
    }

    @Override
    public @Nullable String getProperty(String name) {
        return serviceProperties.getProperty(name);
    }

    @Override
    public void setProperty(String name, @Nullable String value) {
        if (PropertyNames.type.equals(name)) {
            // Triggers exception
            setType(value);
        }
        serviceProperties.setProperty(name, value);
    }

    @Override
    public @Nullable Integer getIntProperty(String name) {
        return serviceProperties.getIntProperty(name);
    }

    @Override
    public void setIntProperty(String name, @Nullable Integer value) {
        serviceProperties.setIntProperty(name, value);
    }

    @Override
    public @Nullable Boolean getBooleanProperty(String name) {
        return serviceProperties.getBooleanProperty(name);
    }

    @Override
    public void setBooleanProperty(String name, @Nullable Boolean value) {
        serviceProperties.setBooleanProperty(name, value);
    }

    @Override
    public Map<ConnectionProperty, Object> connectionPropertyValues() {
        return serviceProperties.connectionPropertyValues();
    }

    /**
     * @return Returns the out.
     */
    public synchronized @Nullable OutputStream getLogger() {
        return logger;
    }

    /**
     * @param logger
     *         The out to set.
     */
    public synchronized void setLogger(@Nullable OutputStream logger) {
        this.logger = logger;
    }

    public FbService attachServiceManager() throws SQLException {
        FbService fbService = dbFactory.serviceConnect(serviceProperties);
        fbService.attach();
        return fbService;
    }

    protected FbDatabase attachDatabase() throws SQLException {
        if (database == null) {
            throw new SQLException("Property database needs to be set.");
        }
        FbConnectionProperties connectionProperties = new FbConnectionProperties();
        createDatabaseAttachInfo().copyTo(connectionProperties);
        connectionProperties.setUser(serviceProperties.getUser());
        connectionProperties.setPassword(serviceProperties.getPassword());
        connectionProperties.setRoleName(serviceProperties.getRoleName());
        connectionProperties.setEnableProtocol(serviceProperties.getEnableProtocol());
        connectionProperties.setAuthPlugins(serviceProperties.getAuthPlugins());
        connectionProperties.setWireCrypt(serviceProperties.getWireCrypt());
        FbDatabase fbDatabase = dbFactory.connect(connectionProperties);
        fbDatabase.attach();
        return fbDatabase;
    }

    private DbAttachInfo createDatabaseAttachInfo() {
        // NOTE: If it turns out we need to tweak this for specific protocol implementations, this may need to move to
        // FbDatabaseFactory (e.g. as a default method to be overridden by implementations)
        final String serverName = serviceProperties.getServerName();
        final String serviceAttachObjectName = serviceProperties.getAttachObjectName();
        if (serverName != null || serviceAttachObjectName == null || serviceAttachObjectName.isEmpty()) {
            return new DbAttachInfo(serverName, serviceProperties.getPortNumber(), database);
        }
        String databaseAttachObjectName;
        if (serviceAttachObjectName.equals(PropertyConstants.DEFAULT_SERVICE_NAME)) {
            databaseAttachObjectName = database;
        } else if (serviceAttachObjectName.endsWith(PropertyConstants.DEFAULT_SERVICE_NAME)) {
            databaseAttachObjectName = serviceAttachObjectName
                    .substring(0, serviceAttachObjectName.length() - 11 /* service_mgr */) + database;
        } else if (serviceAttachObjectName.endsWith("/") || serviceAttachObjectName.endsWith(":")) {
            // e.g. //localhost/ or inet://localhost/ or localhost:
            databaseAttachObjectName = serviceAttachObjectName + database;
        } else {
            // e.g. //localhost or inet://localhost
            // NOTE: This is probably the most error-prone conversion, but making this more robust increases complexity
            // significantly, so instead we'll just let Firebird fail if this guess is wrong
            databaseAttachObjectName = serviceAttachObjectName + '/' + database;
        }
        return new DbAttachInfo(null, serviceProperties.getPortNumber(), databaseAttachObjectName);
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
            case isc_info_svc_to_eof -> {
                int dataLength = iscVaxInteger2(buffer, 1);
                if (dataLength == 0) {
                    if (buffer[3] != isc_info_end) {
                        throw new SQLException("Unexpected end of stream reached.");
                    }
                    processing = false;
                } else if (currentLogger != null) {
                    currentLogger.write(buffer, 3, dataLength);
                }
            }
            case isc_info_truncated -> bufferSize = bufferSize * 2;
            case isc_info_end -> processing = false;
            default -> throw FbExceptionBuilder.toException(isc_infunk);
            }
        }
    }

    /**
     * Execute a Services API operation in the database. All output from the operation is sent to
     * this {@code ServiceManager}'s logger.
     *
     * @param service
     *         service instance to execute on
     * @param srb
     *         The buffer containing the task request
     * @throws SQLException
     *         if a database access error occurs or incorrect parameters are supplied
     */
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
            srb.addArgument(isc_spb_dbname, getDatabase());
        }
        srb.addArgument(isc_spb_options, options);
        return srb;
    }

    @Override
    public GDSServerVersion getServerVersion() throws SQLException {
        try (FbService service = attachServiceManager()) {
            return service.getServerVersion();
        }
    }
}
