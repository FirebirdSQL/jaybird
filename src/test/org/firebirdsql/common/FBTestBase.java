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
package org.firebirdsql.common;

import junit.framework.TestCase;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.jca.FBManagedConnectionFactory;
import org.firebirdsql.jdbc.FirebirdConnection;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;
import org.firebirdsql.management.FBManager;
import org.firebirdsql.pool.FBWrappingDataSource;

import javax.resource.spi.ConnectionManager;
import javax.sql.PooledConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import static org.firebirdsql.common.FBTestProperties.*;

/**
 * Base class for JUnit 3 test cases which could be run against more then one GDS implementation.
 */
public abstract class FBTestBase extends TestCase {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected static final String DB_NAME = FBTestProperties.DB_NAME;
    protected static final String DB_DATASOURCE_URL = FBTestProperties.DB_DATASOURCE_URL;
    protected static final String DB_USER = FBTestProperties.DB_USER;
    protected static final String DB_PASSWORD = FBTestProperties.DB_PASSWORD;
    protected static final String DB_PATH = FBTestProperties.DB_PATH;
    protected static final String DB_SERVER_URL = FBTestProperties.DB_SERVER_URL;
    protected static final int DB_SERVER_PORT = FBTestProperties.DB_SERVER_PORT;

    protected FBManager fbManager = null;

    protected FBTestBase(String name) {
        super(name);
    }

    public static String getProperty(String property) {
        return FBTestProperties.getProperty(property);
    }

    protected String getDatabasePath() {
        return FBTestProperties.getDatabasePath();
    }

    protected String getdbpath(String name) {
        return FBTestProperties.getdbpath(name);
    }

    protected FBManagedConnectionFactory createFBManagedConnectionFactory() {
        return FBTestProperties.createFBManagedConnectionFactory();
    }

    protected FBManagedConnectionFactory createFBManagedConnectionFactory(
            ConnectionManager cm) {
        return FBTestProperties.createFBManagedConnectionFactory(cm);
    }

    protected FBManager createFBManager() {
        return FBTestProperties.createFBManager();
    }

    protected FBWrappingDataSource createFBWrappingDataSource()
            throws SQLException {
        final FBWrappingDataSource returnValue = new FBWrappingDataSource();

        returnValue.setType(getGdsType().toString());

        return returnValue;
    }

    protected FirebirdConnection getConnectionViaDriverManager() throws SQLException {
        return FBTestProperties.getConnectionViaDriverManager();
    }

    protected Properties getDefaultPropertiesForConnection() {
        return FBTestProperties.getDefaultPropertiesForConnection();
    }

    protected void executeCreateTable(Connection connection, String sql) throws SQLException {
        DdlHelper.executeCreateTable(connection, sql);
    }

    protected void executeDropTable(Connection connection, String sql) throws SQLException {
        DdlHelper.executeDropTable(connection, sql);
    }

    protected void executeDDL(Connection connection, String sql, int[] ignoreErrors) throws SQLException {
        DdlHelper.executeDDL(connection, sql, ignoreErrors);
    }

    protected String getUrl() {
        return FBTestProperties.getUrl();
    }

    protected GDSType getGdsType() {
        return FBTestProperties.getGdsType();
    }

    protected void setUp() throws Exception {
        fbManager = createFBManager();
        defaultDatabaseSetUp(fbManager);
    }

    protected void tearDown() throws Exception {
        defaultDatabaseTearDown(fbManager);
        fbManager = null;
    }

    /**
     * Helper method to quietly close statements.
     *
     * @param stmt Statement object
     */
    protected void closeQuietly(Statement stmt) {
        JdbcResourceHelper.closeQuietly(stmt);
    }

    /**
     * Helper method to quietly close connections.
     *
     * @param con Connection object
     */
    protected void closeQuietly(Connection con) {
        JdbcResourceHelper.closeQuietly(con);
    }

    /**
     * Helper method to quietly close resultsets.
     *
     * @param rs ResultSet object
     */
    protected void closeQuietly(ResultSet rs) {
        JdbcResourceHelper.closeQuietly(rs);
    }

    /**
     * Helper method to quietly close pooled connections.
     *
     * @param con PooledConnection object
     */
    protected void closeQuietly(PooledConnection con) {
        JdbcResourceHelper.closeQuietly(con);
    }
}
