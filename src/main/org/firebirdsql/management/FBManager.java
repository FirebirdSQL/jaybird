/*
 SPDX-FileCopyrightText: Copyright 2001-2003 David Jencks
 SPDX-FileCopyrightText: Copyright 2002-2003 Blas Rodriguez Somoza
 SPDX-FileCopyrightText: Copyright 2003 Ryan Baldwin
 SPDX-FileCopyrightText: Copyright 2003-2006 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2012-2024 Mark Rotteveel
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

import java.sql.SQLException;

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

    private static final int DEFAULT_PORT = 3050;
    private static final System.Logger log = System.getLogger(FBManager.class.getName());

    private FbDatabaseFactory dbFactory;
    private String host = "localhost";
    private Integer port;
    private String fileName;
    private String userName;
    private String password;
    private String roleName;
    private String enableProtocol;
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
    }

    public FBManager(String type) {
        this.type = GDSType.getType(type);
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
                log.log(System.Logger.Level.WARNING, "FBManager already stopped");
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

    //Firebird specific methods
    //Which server are we connecting to?

    @Override
    public void setServer(String host) {
        this.host = host;
    }

    @Override
    public String getServer() {
        return host;
    }

    @Override
    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public int getPort() {
        return port != null ? port : DEFAULT_PORT;
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
        return this.type.toString();
    }

    @Override
    public void setType(String type) {
        GDSType gdsType = GDSType.getType(type);

        if (gdsType == null) {
            throw new IllegalArgumentException("Unrecognized type '" + type + "'");
        }

        this.type = gdsType;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getRoleName() {
        return roleName;
    }

    @Override
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    @Override
    public void setEnableProtocol(String enableProtocol) {
        this.enableProtocol = enableProtocol;
    }

    @Override
    public String getEnableProtocol() {
        return enableProtocol;
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
            db.attach();

            // if forceCreate is set, drop the database correctly
            // otherwise exit, database already exists
            if (forceCreate)
                db.dropDatabase();
            else {
                db.close();
                return; //database exists, don't wipe it out.
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
            db.attach();
            db.dropDatabase();
        } catch (Exception e) {
            log.log(System.Logger.Level.ERROR, "Exception dropping database", e);
            throw e;
        }
    }

    @Override
    public synchronized boolean isDatabaseExists(String fileName, String user, String password) throws Exception {
        checkStarted();
        try {
            IConnectionProperties connectionProperties = createDefaultConnectionProperties(user, password, null);
            connectionProperties.setDatabaseName(fileName);
            FbDatabase db = dbFactory.connect(connectionProperties);
            db.attach();
            db.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private IConnectionProperties createDefaultConnectionProperties(String user, String password, String roleName) {
        FbConnectionProperties connectionProperties = new FbConnectionProperties();
        connectionProperties.setUser(user);
        connectionProperties.setPassword(password);
        connectionProperties.setRoleName(roleName);
        connectionProperties.setServerName(getServer());
        connectionProperties.setPortNumber(getPort());
        connectionProperties.setEnableProtocol(getEnableProtocol());
        return connectionProperties;
    }

    private synchronized void checkStarted() {
        if (!STARTED.equals(state)) {
            throw new IllegalStateException("FBManager has not been started. Call start() before use.");
        }
    }
}
