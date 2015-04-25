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
package org.firebirdsql.common;

import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.jca.FBManagedConnectionFactory;
import org.firebirdsql.jca.InternalConnectionManager;
import org.firebirdsql.jdbc.FirebirdConnection;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;
import org.firebirdsql.management.FBManager;
import org.firebirdsql.pool.AbstractFBConnectionPoolDataSource;
import org.firebirdsql.pool.FBPooledDataSourceFactory;
import org.firebirdsql.pool.FBWrappingDataSource;

import javax.resource.spi.ConnectionManager;
import javax.sql.PooledConnection;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Base class for test cases which could be run against more then a single GDS
 * implementation.
 */
public abstract class FBTestBase extends SimpleFBTestBase {

    static {
        try {
            Class.forName("org.firebirdsql.jdbc.FBDriver");
        } catch (ClassNotFoundException ex) {
            throw new ExceptionInInitializerError("No suitable driver.");
        }
    }

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected static final String DB_LC_CTYPE = getProperty("test.db.lc_ctype", "NONE");

    protected final String DB_DATASOURCE_URL = getdbpath(DB_NAME);

    protected FBTestBase(String name) {
        super(name);
    }

    // FACTORY METHODS
    //
    // These methods should be used where possible so as to create the objects
    // bound to the
    // appropriate GDS implementation.

    protected AbstractFBConnectionPoolDataSource createFBConnectionPoolDataSource()
            throws SQLException {
        final AbstractFBConnectionPoolDataSource returnValue = FBPooledDataSourceFactory
                .createFBConnectionPoolDataSource();

        returnValue.setType(getGdsType().toString());

        return returnValue;
    }

    protected FBManagedConnectionFactory createFBManagedConnectionFactory() {
        return new FBManagedConnectionFactory(getGdsType());
    }

    protected FBManagedConnectionFactory createFBManagedConnectionFactory(
            ConnectionManager cm) {
        FBManagedConnectionFactory mcf = new FBManagedConnectionFactory(
                getGdsType());
        mcf.setDefaultConnectionManager(new InternalConnectionManager());
        return mcf;
    }

    protected FBManager createFBManager() {
        return new FBManager(getGdsType());
    }

    protected FBWrappingDataSource createFBWrappingDataSource()
            throws SQLException {
        final FBWrappingDataSource returnValue = new FBWrappingDataSource();

        returnValue.setType(getGdsType().toString());

        return returnValue;
    }

    protected FirebirdConnection getConnectionViaDriverManager() throws SQLException {
        return (FirebirdConnection)DriverManager.getConnection(getUrl(),
            getDefaultPropertiesForConnection());
    }

    protected Properties getDefaultPropertiesForConnection() {
        final Properties returnValue = new Properties();

        returnValue.setProperty("user", DB_USER);
        returnValue.setProperty("password", DB_PASSWORD);
        returnValue.setProperty("lc_ctype", DB_LC_CTYPE);

        return returnValue;
    }
    
    protected void executeCreateTable(Connection connection, String sql) throws SQLException {
        executeDDL(connection, sql, new int[]{ISCConstants.isc_no_meta_update});
    }
    
    protected void executeDropTable(Connection connection, String sql) throws SQLException {
        executeDDL(connection, sql, getDropIgnoreErrors(connection));
    }
    
    private int[] getDropIgnoreErrors(Connection connection) throws SQLException {
        DatabaseMetaData dbmd = connection.getMetaData();
        if (dbmd.getDatabaseMajorVersion() < 2) {
            // Firebird 1.5 and earlier do not always return specific error codes
            return new int[] {ISCConstants.isc_dsql_error, ISCConstants.isc_no_meta_update, ISCConstants.isc_dsql_table_not_found, ISCConstants.isc_dsql_view_not_found};
        } else {
            return new int[]{ISCConstants.isc_no_meta_update, ISCConstants.isc_dsql_table_not_found, ISCConstants.isc_dsql_view_not_found};
        }
    }

    protected void executeDDL(Connection connection, String sql, int[] ignoreErrors) throws SQLException {
        try {
            Statement stmt = connection.createStatement();
            try {
                stmt.execute(sql);
            } finally {
                stmt.close();
            }
        } catch(SQLException ex) {
            if (ignoreErrors == null || ignoreErrors.length == 0)
                throw ex;
            
            boolean ignoreException = false;
            
            int errorCode = ex.getErrorCode();
            Throwable current = ex;
            errorcodeloop: do {
                for (int i = 0; i < ignoreErrors.length; i++) {
                    if (ignoreErrors[i] == errorCode) {
                        ignoreException = true;
                        break errorcodeloop;
                    }
                }
                if (current instanceof GDSException) {
                    current = ((GDSException)current).getNext();
                } else {
                    current = current.getCause();
                }
                if (current == null || !(current instanceof GDSException)) {
                    break;
                } else {
                    errorCode = ((GDSException)current).getFbErrorCode();
                }
            } while (errorCode != -1);
            
            if (!ignoreException)
                throw ex;
        }
    }

    // USEFULL PROPERTY GETTERS

    protected String getUrl() {
        return gdsTypeToUrlPrefixMap.get(getGdsType()) + getdbpath(DB_NAME);
    }

    protected GDSType getGdsType() {
        final GDSType gdsType = GDSType.getType(getProperty("test.gds_type", "PURE_JAVA"));
        if (gdsType == null)
            throw new RuntimeException(
                    "Unrecoginzed value for 'test.gds_type' property.");

        return gdsType;
    }

    // STANDARD RIG

    protected void setUp() throws Exception {
        fbManager = createFBManager();

        if (getGdsType() == GDSType.getType("PURE_JAVA")
                || getGdsType() == GDSType.getType("NATIVE")
                || getGdsType() == GDSType.getType("OOREMOTE")) {
            fbManager.setServer(DB_SERVER_URL);
            fbManager.setPort(DB_SERVER_PORT);
        }
        fbManager.start();
        fbManager.setForceCreate(true);
        fbManager.createDatabase(getDatabasePath(), DB_USER, DB_PASSWORD);
    }

    protected void tearDown() throws Exception {
        fbManager.dropDatabase(getDatabasePath(), DB_USER, DB_PASSWORD);
        fbManager.stop();
        fbManager = null;
    }

    protected FBManager fbManager = null;

    private static final Map gdsTypeToUrlPrefixMap = new HashMap();
    static {
        gdsTypeToUrlPrefixMap.put(GDSType.getType("PURE_JAVA"), "jdbc:firebirdsql:");
        gdsTypeToUrlPrefixMap.put(GDSType.getType("EMBEDDED"), "jdbc:firebirdsql:embedded:");
        gdsTypeToUrlPrefixMap.put(GDSType.getType("NATIVE"), "jdbc:firebirdsql:native:");
        gdsTypeToUrlPrefixMap.put(GDSType.getType("ORACLE_MODE"), "jdbc:firebirdsql:oracle:");
        gdsTypeToUrlPrefixMap.put(GDSType.getType("LOCAL"), "jdbc:firebirdsql:local:");
        gdsTypeToUrlPrefixMap.put(GDSType.getType("NIO"), "jdbc:firebirdsql:nio:");
        gdsTypeToUrlPrefixMap.put(GDSType.getType("OOREMOTE"), "jdbc:firebirdsql:oo:");
    }
    
    /**
     * Helper method to quietly close statements.
     * 
     * @param stmt Statement object
     */
    protected void closeQuietly(Statement stmt) {
        if (stmt == null) {
            return;
        }
        try {
            stmt.close();
        } catch (SQLException ex) {
            //ignore
        }
    }
    
    /**
     * Helper method to quietly close connections.
     * 
     * @param con Connection object
     */
    protected void closeQuietly(Connection con) {
        if (con == null) {
            return;
        }
        try {
            con.close();
        } catch (SQLException ex) {
            //ignore
        }
    }
    
    /**
     * Helper method to quietly close resultsets.
     * 
     * @param rs ResultSet object
     */
    protected void closeQuietly(ResultSet rs) {
        if (rs == null) {
            return;
        }
        try {
            rs.close();
        } catch (SQLException ex) {
            //ignore
        }
    }
    
    /**
     * Helper method to quietly close pooled connections.
     * 
     * @param con PooledConnection object
     */
    protected void closeQuietly(PooledConnection con) {
        if (con == null) {
            return;
        }
        try {
            con.close();
        } catch (SQLException ex) {
            //ignore
        }
    }
}
