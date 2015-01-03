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
 * The class <code>FBManager</code> is a simple jmx mbean that allows you
 * to create and drop databases.  in particular, they can be created and
 * dropped using the jboss service lifecycle operations start and stop.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 * @jmx.mbean
 */
public class FBManager implements FBManagerMBean {

    private static final int DEFAULT_PORT = 3050;
    private final static Logger log = LoggerFactory.getLogger(FBManager.class);

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
        this(GDSFactory.getDefaultGDS().getType());
    }

    public FBManager(GDSType type) {
        this.type = type;
    }

    public FBManager(String type) {
        this.type = GDSType.getType(type);
    }

    //Service methods

    /**
     * @jmx.managed-operation
     */
    public void start() throws Exception {
        dbFactory = GDSFactory.getDatabaseFactoryForType(type);
        state = STARTED;
        if (isCreateOnStart()) {
            createDatabase(getFileName(), getUserName(), getPassword());
        }
    }

    /**
     * @jmx.managed-operation
     */
    public void stop() throws Exception {
        if (isDropOnStop()) {
            dropDatabase(getFileName(), getUserName(), getPassword());
        }

        dbFactory = null;
        state = STOPPED;
    }

    /**
     * @jmx.managed-attribute
     */
    public String getState() {
        return state;
    }

    /**
     * @jmx.managed-attribute
     */
    public String getName() {
        return "Firebird Database manager";
    }

    //Firebird specific methods
    //Which server are we connecting to?

    /**
     * @jmx.managed-attribute
     */
    public void setServer(final String host) {
        this.host = host;
    }

    /**
     * @jmx.managed-attribute
     */
    public String getServer() {
        return host;
    }

    /**
     * @jmx.managed-attribute
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * @jmx.managed-attribute
     */
    public int getPort() {
        return port != null ? port : DEFAULT_PORT;
    }

    /**
     * mbean get-set pair for field fileName
     * Get the value of fileName
     *
     * @return value of fileName
     * @jmx:managed-attribute
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Set the value of fileName
     *
     * @param fileName
     *         Value to assign to fileName
     * @jmx:managed-attribute
     */
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

    /**
     * mbean get-set pair for field userName
     * Get the value of userName
     *
     * @return value of userName
     * @jmx:managed-attribute
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Set the value of userName
     *
     * @param userName
     *         Value to assign to userName
     * @jmx:managed-attribute
     */
    public void setUserName(final String userName) {
        this.userName = userName;
    }

    /**
     * mbean get-set pair for field password
     * Get the value of password
     *
     * @return value of password
     * @jmx:managed-attribute
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set the value of password
     *
     * @param password
     *         Value to assign to password
     * @jmx:managed-attribute
     */
    public void setPassword(final String password) {
        this.password = password;
    }

    /**
     * Sets the dialect.
     *
     * @param dialect Database dialect (1 or 3)
     * @throws java.lang.IllegalArgumentException if value is not 1 or 3
     */
    public void setDialect(int dialect) {
        if (!(dialect == 1 || dialect == 3)) throw new IllegalArgumentException("Only dialect 1 or 3 allowed");
        this.dialect = dialect;
    }

    public int getDialect() {
        return dialect;
    }

    /**
     * Set the page size that will be used for the database. The value
     * for <code>pageSize</code> must be one of: 1024, 2048, 4096, 8192 or 16384. The
     * default value depends on the Firebird version.
     * <p>
     * Some values are not valid on all Firebird versions.
     * </p>
     *
     * @param pageSize The page size to be used in a restored database,
     *        one of 1024, 2048, 4196, 8192 or 16384
     */
    public void setPageSize(int pageSize) {
        if (pageSize != 1024 && pageSize != 2048
                && pageSize != 4096 && pageSize != 8192 && pageSize != 16384){
            throw new IllegalArgumentException(
                    "Page size must be one of 1024, 2048, 4096, 8192 or 16384");
        }
        this.pageSize = pageSize;
    }

    /**
     * @return The page size to be used when creating a database, or {@code -1} if the database default is used.
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * mbean get-set pair for field createOnStart
     * Get the value of createOnStart
     *
     * @return value of createOnStart
     * @jmx:managed-attribute
     */
    public boolean isCreateOnStart() {
        return createOnStart;
    }

    /**
     * Set the value of createOnStart
     *
     * @param createOnStart
     *         Value to assign to createOnStart
     * @jmx:managed-attribute
     */
    public void setCreateOnStart(final boolean createOnStart) {
        this.createOnStart = createOnStart;
    }

    /**
     * mbean get-set pair for field dropOnStop
     * Get the value of dropOnStop
     *
     * @return value of dropOnStop
     * @jmx:managed-attribute
     */
    public boolean isDropOnStop() {
        return dropOnStop;
    }

    /**
     * Set the value of dropOnStop
     *
     * @param dropOnStop
     *         Value to assign to dropOnStop
     * @jmx:managed-attribute
     */
    public void setDropOnStop(final boolean dropOnStop) {
        this.dropOnStop = dropOnStop;
    }

    /**
     * Get the ForceCreate value.
     *
     * @return the ForceCreate value.
     * @jmx:managed-attribute
     */
    public boolean isForceCreate() {
        return forceCreate;
    }

    /**
     * Set the ForceCreate value.
     *
     * @param forceCreate
     *         The new ForceCreate value.
     * @jmx:managed-attribute
     */
    public void setForceCreate(boolean forceCreate) {
        this.forceCreate = forceCreate;
    }

    //Meaningful management methods

    /**
     * @jmx.managed-operation
     */
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
                db.detach();
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
            FbDatabase db = dbFactory.connect(connectionProperties);
            db.createDatabase();
            db.detach();
        } catch (Exception e) {
            log.error("Exception creating database", e);
            throw e;
        }
    }

    /**
     * @jmx.managed-operation
     */
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

    public boolean isDatabaseExists(String fileName, String user, String password) throws Exception {
        try {
            IConnectionProperties connectionProperties = createDefaultConnectionProperties(user, password);
            connectionProperties.setDatabaseName(fileName);
            FbDatabase db = dbFactory.connect(connectionProperties);
            db.attach();
            db.detach();
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


