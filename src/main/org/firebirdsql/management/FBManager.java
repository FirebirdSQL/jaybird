/*
 * Firebird Open Source JDBC Driver
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

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.ng.FbConnectionProperties;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.FbDatabaseFactory;
import org.firebirdsql.gds.ng.IConnectionProperties;
import org.firebirdsql.jaybird.props.def.ConnectionProperty;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.sql.SQLException;
import java.util.Map;

/**
 * A tool for creating and dropping databases.
 * <p>
 * In particular, they can be created and dropped using the jboss service lifecycle operations start and stop.
 * </p>
 * <p>
 * See {@link FBManagerMBean} for documentation.
 * </p>
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class FBManager implements FBManagerMBean {

    private static final Logger log = LoggerFactory.getLogger(FBManager.class);

    private FbDatabaseFactory dbFactory;
    private final IConnectionProperties connectionProperties = new FbConnectionProperties();
    private String fileName;
    private int dialect = ISCConstants.SQL_DIALECT_CURRENT;
    private int pageSize = -1;
    private String defaultCharacterSet;
    private Boolean forceWrite;
    private boolean forceCreate;
    private boolean createOnStart;
    private boolean dropOnStop;
    private String state = STOPPED;
    private static final String STOPPED = "Stopped";
    private static final String STARTED = "Started";
    private GDSType type;

    public FBManager() {
        this(GDSFactory.getDefaultGDSType());
    }

    public FBManager(GDSType type) {
        this.type = type;
        connectionProperties.setType(type.toString());
    }

    public FBManager(String type) {
        this(GDSType.getType(type));
    }

    //Service methods

    @Override
    public synchronized void start() throws Exception {
        if (STARTED.equals(state)) {
            throw new IllegalStateException("FBManager already started. Call stop() before starting again.");
        }
        dbFactory = GDSFactory.getDatabaseFactoryForType(type);
        state = STARTED;
        String fileName = getFileName();
        if (isCreateOnStart() && fileName != null) {
            createDatabase(fileName, getUserName(), getPassword(), getRoleName());
        }
    }

    @Override
    public synchronized void stop() throws Exception {
        try {
            if (STOPPED.equals(state)) {
                log.warn("FBManager already stopped.");
                return;
            }
            String fileName = getFileName();
            if (isDropOnStop() && fileName != null) {
                dropDatabase(fileName, getUserName(), getPassword(), getRoleName());
            }
        } finally {
            dbFactory = null;
            state = STOPPED;
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
    public void setServerName(String serverName) {
        FBManagerMBean.super.setServerName(serverName);
    }

    @Override
    public String getServerName() {
        return FBManagerMBean.super.getServerName();
    }

    @Override
    public void setServer(String host) {
        setServerName(host);
    }

    @Override
    public String getServer() {
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

    @Override
    public void setPort(int port) {
        setPortNumber(port);
    }

    @Override
    public int getPort() {
        return getPortNumber();
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String getType() {
        return type.toString();
    }

    @Override
    public void setType(String type) {
        GDSType gdsType = GDSType.getType(type);

        if (gdsType == null)
            throw new RuntimeException("Unrecognized type '" + type + "'");

        this.type = gdsType;
        connectionProperties.setType(gdsType.toString());
    }

    @Override
    public String getUser() {
        return FBManagerMBean.super.getUser();
    }

    @Override
    public void setUser(String user) {
        FBManagerMBean.super.setUser(user);
    }

    @Override
    public String getUserName() {
        return getUser();
    }

    @Override
    public void setUserName(String userName) {
        setUser(userName);
    }

    @Override
    public String getPassword() {
        return FBManagerMBean.super.getPassword();
    }

    @Override
    public void setPassword(String password) {
        FBManagerMBean.super.setPassword(password);
    }

    @Override
    public String getRoleName() {
        return FBManagerMBean.super.getRoleName();
    }

    @Override
    public void setRoleName(String roleName) {
        FBManagerMBean.super.setRoleName(roleName);
    }

    @Override
    public String getAuthPlugins() {
        return FBManagerMBean.super.getAuthPlugins();
    }

    @Override
    public void setAuthPlugins(String authPlugins) {
        FBManagerMBean.super.setAuthPlugins(authPlugins);
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
    public void setDefaultCharacterSet(String firebirdCharsetName) {
        this.defaultCharacterSet = firebirdCharsetName;
    }

    @Override
    public String getDefaultCharacterSet() {
        return defaultCharacterSet;
    }

    @Override
    public void setForceWrite(Boolean forceWrite) {
        this.forceWrite = forceWrite;
    }

    @Override
    public Boolean getForceWrite() {
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
    public void createDatabase(String fileName, String user, String password) throws Exception {
        createDatabase(fileName, user, password, null);
    }

    @Override
    public synchronized void createDatabase(String fileName, String user, String password, String roleName)
            throws Exception {
        checkStarted();
        try {
            IConnectionProperties connectionProperties = createDefaultConnectionProperties(user, password, roleName);
            connectionProperties.setDatabaseName(fileName);
            FbDatabase db = dbFactory.connect(connectionProperties);
            try {
                db.attach();
                if (forceCreate) {
                    db.dropDatabase();
                } else {
                    // database exists, don't wipe it out
                    return;
                }
            } finally {
                if (db.isAttached()) db.close();
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
            log.error("Exception creating database", e);
            throw e;
        }
    }

    @Override
    public void dropDatabase(String fileName, String user, String password) throws Exception {
        dropDatabase(fileName, user, password, null);
    }

    @Override
    public synchronized void dropDatabase(String fileName, String user, String password, String roleName)
            throws Exception {
        checkStarted();
        try {
            IConnectionProperties connectionProperties = createDefaultConnectionProperties(user, password, roleName);
            connectionProperties.setDatabaseName(fileName);
            FbDatabase db = dbFactory.connect(connectionProperties);
            try {
                db.attach();
                db.dropDatabase();
            } finally {
                if (db.isAttached()) db.close();
            }
        } catch (Exception e) {
            log.error("Exception dropping database", e);
            throw e;
        }
    }

    @Override
    public synchronized boolean isDatabaseExists(String fileName, String user, String password) throws Exception {
        checkStarted();
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

    private IConnectionProperties createDefaultConnectionProperties(String user, String password, String roleName) {
        IConnectionProperties connectionProperties = this.connectionProperties.asNewMutable();
        connectionProperties.setUser(user);
        connectionProperties.setPassword(password);
        connectionProperties.setRoleName(roleName);
        return connectionProperties;
    }

    private synchronized void checkStarted() {
        if (!STARTED.equals(state)) {
            throw new IllegalStateException("FBManager has not been started. Call start() before use.");
        }
    }

    @Override
    public final String getProperty(String name) {
        return connectionProperties.getProperty(name);
    }

    @Override
    public final void setProperty(String name, String value) {
        connectionProperties.setProperty(name, value);
    }

    @Override
    public final Integer getIntProperty(String name) {
        return connectionProperties.getIntProperty(name);
    }

    @Override
    public final void setIntProperty(String name, Integer value) {
        connectionProperties.setIntProperty(name, value);
    }

    @Override
    public final Boolean getBooleanProperty(String name) {
        return connectionProperties.getBooleanProperty(name);
    }

    @Override
    public final void setBooleanProperty(String name, Boolean value) {
        connectionProperties.setBooleanProperty(name, value);
    }

    @Override
    public final Map<ConnectionProperty, Object> connectionPropertyValues() {
        return connectionProperties.connectionPropertyValues();
    }
    
}
