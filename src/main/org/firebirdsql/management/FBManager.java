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

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.ng.FbConnectionProperties;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.FbDatabaseFactory;
import org.firebirdsql.gds.ng.IConnectionProperties;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.sql.SQLException;

/**
 * A simple jmx mbean that allows you to create and drop databases.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @version 1.0
 */
public class FBManager implements FBManagerMBean {

    private static final int DEFAULT_PORT = 3050;
    private static final Logger log = LoggerFactory.getLogger(FBManager.class);

    private FbDatabaseFactory dbFactory;
    private String host = "localhost";
    private Integer port;
    private String fileName;
    private String userName;
    private String password;
    private int dialect = ISCConstants.SQL_DIALECT_CURRENT;
    private int pageSize = -1;
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
    public void start() throws Exception {
        dbFactory = GDSFactory.getDatabaseFactoryForType(type);
        state = STARTED;
        if (isCreateOnStart()) {
            createDatabase(getFileName(), getUserName(), getPassword());
        }
    }

    @Override
    public void stop() throws Exception {
        if (isDropOnStop()) {
            dropDatabase(getFileName(), getUserName(), getPassword());
        }

        dbFactory = null;
        state = STOPPED;
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
    public void setServer(final String host) {
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
    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public String getType() {
        return this.type.toString();
    }

    public void setType(String type) {
        final GDSType gdsType = GDSType.getType(type);

        if (gdsType == null)
            throw new RuntimeException("Unrecognized type '" + type + "'");

        this.type = gdsType;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public void setUserName(final String userName) {
        this.userName = userName;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(final String password) {
        this.password = password;
    }

    /**
     * Sets the dialect.
     *
     * @param dialect
     *         Database dialect (1 or 3)
     * @throws java.lang.IllegalArgumentException
     *         if value is not 1 or 3
     */
    public void setDialect(int dialect) {
        if (!(dialect == 1 || dialect == 3)) throw new IllegalArgumentException("Only dialect 1 or 3 allowed");
        this.dialect = dialect;
    }

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
    public boolean isCreateOnStart() {
        return createOnStart;
    }

    @Override
    public void setCreateOnStart(final boolean createOnStart) {
        this.createOnStart = createOnStart;
    }

    @Override
    public boolean isDropOnStop() {
        return dropOnStop;
    }

    @Override
    public void setDropOnStop(final boolean dropOnStop) {
        this.dropOnStop = dropOnStop;
    }

    /**
     * Get the ForceCreate value.
     *
     * @return the ForceCreate value.
     */
    @Override
    public boolean isForceCreate() {
        return forceCreate;
    }

    /**
     * Set the ForceCreate value.
     *
     * @param forceCreate
     *         The new ForceCreate value.
     */
    @Override
    public void setForceCreate(boolean forceCreate) {
        this.forceCreate = forceCreate;
    }

    //Meaningful management methods

    @Override
    public void createDatabase(String fileName, String user, String password) throws Exception {
        try {
            IConnectionProperties connectionProperties = createDefaultConnectionProperties(user, password);
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
            IConnectionProperties connectionProperties = createDefaultConnectionProperties(user, password);
            connectionProperties.setDatabaseName(fileName);
            connectionProperties.setConnectionDialect((short) dialect);
            if (getPageSize() != -1) {
                connectionProperties.getExtraDatabaseParameters()
                        .addArgument(ISCConstants.isc_dpb_page_size, getPageSize());
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
        try {
            IConnectionProperties connectionProperties = createDefaultConnectionProperties(user, password);
            connectionProperties.setDatabaseName(fileName);
            FbDatabase db = dbFactory.connect(connectionProperties);
            db.attach();
            db.dropDatabase();
        } catch (Exception e) {
            log.error("Exception dropping database", e);
            throw e;
        }
    }

    @Override
    public boolean isDatabaseExists(String fileName, String user, String password) throws Exception {
        try {
            IConnectionProperties connectionProperties = createDefaultConnectionProperties(user, password);
            connectionProperties.setDatabaseName(fileName);
            FbDatabase db = dbFactory.connect(connectionProperties);
            db.attach();
            db.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private IConnectionProperties createDefaultConnectionProperties(String user, String password) {
        FbConnectionProperties connectionProperties = new FbConnectionProperties();
        connectionProperties.setUser(user);
        connectionProperties.setPassword(password);
        connectionProperties.setServerName(getServer());
        connectionProperties.setPortNumber(getPort());
        return connectionProperties;
    }
}
