/*
 SPDX-FileCopyrightText: Copyright 2001-2003 David Jencks
 SPDX-FileCopyrightText: Copyright 2002-2003 Blas Rodriguez Somoza
 SPDX-FileCopyrightText: Copyright 2003 Ryan Baldwin
 SPDX-FileCopyrightText: Copyright 2003-2006 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2012-2026 Mark Rotteveel
 SPDX-License-Identifier: LGPL-2.1-or-later
*/
package org.firebirdsql.management;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.ng.FbConnectionProperties;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.FbDatabaseFactory;
import org.firebirdsql.gds.ng.IConnectionProperties;
import org.firebirdsql.jaybird.props.def.ConnectionProperty;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

import java.sql.SQLException;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * A tool for creating and dropping databases.
 * <p>
 * In particular, they can be created and dropped using the jboss service lifecycle operations start and stop.
 * </p>
 * <p>
 * See {@link FBManagerMBean} for documentation.
 * </p>
 *
 * @author David Jencks
 * @version 1.0
 */
public class FBManager implements FBManagerMBean {

    private static final System.Logger log = System.getLogger(FBManager.class.getName());

    private @Nullable FbDatabaseFactory dbFactory;
    private final IConnectionProperties connectionProperties = new FbConnectionProperties();
    private @Nullable String fileName;
    private int dialect = ISCConstants.SQL_DIALECT_CURRENT;
    private int pageSize = -1;
    private @Nullable String defaultCharacterSet;
    private @Nullable Boolean forceWrite;
    private boolean forceCreate;
    private boolean createOnStart;
    private boolean dropOnStop;
    private String state = FBManagerMBean.STOPPED;
    private GDSType type;

    public FBManager() {
        this(GDSFactory.getDefaultGDSType());
    }

    public FBManager(GDSType type) {
        this.type = requireNonNull(type, "type");
        connectionProperties.setType(type.toString());
    }

    public FBManager(String type) {
        this(GDSType.getType(type));
    }

    //Service methods

    @Override
    public synchronized void start() throws Exception {
        if (FBManagerMBean.STARTED.equals(state)) {
            throw new IllegalStateException("FBManager already started. Call stop() before starting again.");
        }
        dbFactory = GDSFactory.getDatabaseFactoryForType(type);
        state = FBManagerMBean.STARTED;
        String fileName = getFileName();
        if (isCreateOnStart() && fileName != null) {
            createDatabase(fileName, getUserName(), getPassword(), getRoleName());
        }
    }

    @Override
    public synchronized void stop() throws Exception {
        try {
            if (FBManagerMBean.STOPPED.equals(state)) {
                log.log(System.Logger.Level.WARNING, "FBManager already stopped");
                return;
            }
            String fileName = getFileName();
            if (isDropOnStop() && fileName != null) {
                dropDatabase(fileName, getUserName(), getPassword(), getRoleName());
            }
        } finally {
            dbFactory = null;
            state = FBManagerMBean.STOPPED;
        }
    }

    @Override
    public void close() throws Exception {
        stop();
    }

    @Override
    public String getState() {
        return state;
    }

    @Override
    public String getName() {
        return "Firebird Database manager";
    }

    // Firebird specific methods
    // We're redefining the getters and setters here so they can be found with bean introspection;
    // This is primarily for historic reasons, and don't add redirection for all properties.

    @Override
    public void setServerName(@Nullable String serverName) {
        FBManagerMBean.super.setServerName(serverName);
    }

    @Override
    public @Nullable String getServerName() {
        return FBManagerMBean.super.getServerName();
    }

    @Deprecated(since = "7")
    @Override
    public void setServer(@Nullable String host) {
        setServerName(host);
    }

    @Deprecated(since = "7")
    @Override
    public @Nullable String getServer() {
        return getServerName();
    }

    @Override
    public void setPortNumber(int portNumber) {
        FBManagerMBean.super.setPortNumber(portNumber);
    }

    @Override
    public int getPortNumber() {
        return FBManagerMBean.super.getPortNumber();
    }

    @Deprecated(since = "7")
    @Override
    public void setPort(int port) {
        setPortNumber(port);
    }

    @Deprecated(since = "7")
    @Override
    public int getPort() {
        return getPortNumber();
    }

    @Override
    public @Nullable String getFileName() {
        return fileName;
    }

    @Override
    @NullUnmarked
    @SuppressWarnings("NullableProblems") /* Intentional use of @NullUnmarked to leave nullability as questionable */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String getType() {
        return type.toString();
    }

    @Override
    public void setType(@Nullable String type) {
        GDSType gdsType = GDSType.getType(type);

        if (gdsType == null) {
            throw new IllegalArgumentException("Unrecognized type '" + type + "'");
        }

        this.type = gdsType;
        connectionProperties.setType(gdsType.toString());
    }

    @Override
    public @Nullable String getUser() {
        return FBManagerMBean.super.getUser();
    }

    @Override
    public void setUser(@Nullable String user) {
        FBManagerMBean.super.setUser(user);
    }

    @Deprecated(since = "7")
    @Override
    public @Nullable String getUserName() {
        return getUser();
    }

    @Deprecated(since = "7")
    @Override
    public void setUserName(@Nullable String userName) {
        setUser(userName);
    }

    @Override
    public @Nullable String getPassword() {
        return FBManagerMBean.super.getPassword();
    }

    @Override
    public void setPassword(@Nullable String password) {
        FBManagerMBean.super.setPassword(password);
    }

    @Override
    public @Nullable String getRoleName() {
        return FBManagerMBean.super.getRoleName();
    }

    @Override
    public void setRoleName(@Nullable String roleName) {
        FBManagerMBean.super.setRoleName(roleName);
    }

    @Override
    public String getAuthPlugins() {
        return FBManagerMBean.super.getAuthPlugins();
    }

    @Override
    public void setAuthPlugins(@Nullable String authPlugins) {
        FBManagerMBean.super.setAuthPlugins(authPlugins);
    }

    @Override
    public void setEnableProtocol(@Nullable String enableProtocol) {
       FBManagerMBean.super.setEnableProtocol(enableProtocol);
    }

    @Override
    public @Nullable String getEnableProtocol() {
        return FBManagerMBean.super.getEnableProtocol();
    }

    @Override
    public void setDialect(int dialect) {
        if (!(dialect == 1 || dialect == 3)) throw new IllegalArgumentException("Only dialect 1 or 3 allowed");
        this.dialect = dialect;
    }

    @Override
    public int getDialect() {
        return dialect;
    }

    @Override
    public void setPageSize(int pageSize) {
        this.pageSize = PageSizeConstants.requireValidPageSize(pageSize);
    }

    @Override
    public int getPageSize() {
        return pageSize;
    }

    @Override
    public void setDefaultCharacterSet(@Nullable String firebirdCharsetName) {
        this.defaultCharacterSet = firebirdCharsetName;
    }

    @Override
    public @Nullable String getDefaultCharacterSet() {
        return defaultCharacterSet;
    }

    @Override
    public void setForceWrite(@Nullable Boolean forceWrite) {
        this.forceWrite = forceWrite;
    }

    @Override
    public @Nullable Boolean getForceWrite() {
        return forceWrite;
    }

    @Override
    public boolean isCreateOnStart() {
        return createOnStart;
    }

    @Override
    public void setCreateOnStart(boolean createOnStart) {
        this.createOnStart = createOnStart;
    }

    @Override
    public boolean isDropOnStop() {
        return dropOnStop;
    }

    @Override
    public void setDropOnStop(boolean dropOnStop) {
        this.dropOnStop = dropOnStop;
    }

    @Override
    public boolean isForceCreate() {
        return forceCreate;
    }

    @Override
    public void setForceCreate(boolean forceCreate) {
        this.forceCreate = forceCreate;
    }

    //Meaningful management methods

    @Override
    public void createDatabase(String fileName, @Nullable String user, @Nullable String password) throws Exception {
        createDatabase(fileName, user, password, null);
    }

    @Override
    public synchronized void createDatabase(String fileName, @Nullable String user, @Nullable String password,
            @Nullable String roleName) throws Exception {
        checkStarted();
        assert dbFactory != null;
        try {
            IConnectionProperties connectionProperties = createDefaultConnectionProperties(user, password, roleName);
            connectionProperties.setDatabaseName(fileName);
            try (FbDatabase db = dbFactory.connect(connectionProperties)) {
                db.attach();
                if (forceCreate) {
                    db.dropDatabase();
                } else {
                    // database exists, don't wipe it out
                    return;
                }
            }
        } catch (SQLException e) {
            // we ignore it
        }

        try {
            IConnectionProperties connectionProperties = createDefaultConnectionProperties(user, password, roleName);
            connectionProperties.setDatabaseName(fileName);
            connectionProperties.setSqlDialect(dialect);
            if (getPageSize() != -1) {
                connectionProperties.setIntProperty("page_size", getPageSize());
            }
            if (getDefaultCharacterSet() != null) {
                connectionProperties.setProperty("set_db_charset", getDefaultCharacterSet());
            }
            if (forceWrite != null) {
                connectionProperties.setBooleanProperty("force_write", forceWrite);
            }

            try (FbDatabase db = dbFactory.connect(connectionProperties)) {
                db.createDatabase();
            }
        } catch (Exception e) {
            log.log(System.Logger.Level.ERROR, "Exception creating database", e);
            throw e;
        }
    }

    @Override
    public void dropDatabase(String fileName, @Nullable String user, @Nullable String password) throws Exception {
        dropDatabase(fileName, user, password, null);
    }

    @Override
    public synchronized void dropDatabase(String fileName, @Nullable String user, @Nullable String password,
            @Nullable String roleName) throws Exception {
        checkStarted();
        assert dbFactory != null;
        try {
            IConnectionProperties connectionProperties = createDefaultConnectionProperties(user, password, roleName);
            connectionProperties.setDatabaseName(fileName);
            try (FbDatabase db = dbFactory.connect(connectionProperties)) {
                db.attach();
                db.dropDatabase();
            }
        } catch (Exception e) {
            log.log(System.Logger.Level.ERROR, "Exception dropping database", e);
            throw e;
        }
    }

    @Override
    public synchronized boolean isDatabaseExists(String fileName, @Nullable String user, @Nullable String password)
            throws Exception {
        checkStarted();
        assert dbFactory != null;
        try {
            IConnectionProperties connectionProperties = createDefaultConnectionProperties(user, password, null);
            connectionProperties.setDatabaseName(fileName);
            try (FbDatabase db = dbFactory.connect(connectionProperties)) {
                db.attach();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private IConnectionProperties createDefaultConnectionProperties(@Nullable String user, @Nullable String password,
            @Nullable String roleName) {
        IConnectionProperties connectionProperties = this.connectionProperties.asNewMutable();
        connectionProperties.setUser(user);
        connectionProperties.setPassword(password);
        connectionProperties.setRoleName(roleName);
        return connectionProperties;
    }

    private synchronized void checkStarted() {
        if (!FBManagerMBean.STARTED.equals(state)) {
            throw new IllegalStateException("FBManager has not been started. Call start() before use.");
        }
    }

    @Override
    public final @Nullable String getProperty(String name) {
        return connectionProperties.getProperty(name);
    }

    @Override
    public final void setProperty(String name, @Nullable String value) {
        connectionProperties.setProperty(name, value);
    }

    @Override
    public final @Nullable Integer getIntProperty(String name) {
        return connectionProperties.getIntProperty(name);
    }

    @Override
    public final void setIntProperty(String name, @Nullable Integer value) {
        connectionProperties.setIntProperty(name, value);
    }

    @Override
    public final @Nullable Boolean getBooleanProperty(String name) {
        return connectionProperties.getBooleanProperty(name);
    }

    @Override
    public final void setBooleanProperty(String name, @Nullable Boolean value) {
        connectionProperties.setBooleanProperty(name, value);
    }

    @Override
    public final Map<ConnectionProperty, Object> connectionPropertyValues() {
        return connectionProperties.connectionPropertyValues();
    }
    
}
