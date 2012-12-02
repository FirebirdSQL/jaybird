/*
 * $Id$
 * Firebird Open Source J2ee connector - jdbc driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.common;

import java.io.File;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.resource.spi.ConnectionManager;

import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.jca.FBManagedConnectionFactory;
import org.firebirdsql.jca.InternalConnectionManager;
import org.firebirdsql.jdbc.FBDriver;
import org.firebirdsql.jdbc.FirebirdConnection;
import org.firebirdsql.management.FBManager;

/**
 * Helper class for test properties (database user, password, paths etc)
 */
public final class FBTestProperties {
    
    static {
        // TODO: Technically not needed with JDBC 4.0 autoloading
        try {
            Class.forName(FBDriver.class.getName());
        } catch (ClassNotFoundException ex) {
            throw new ExceptionInInitializerError("No suitable driver.");
        }
    }
    
    private static ResourceBundle testDefaults = ResourceBundle.getBundle("unit_test_defaults");

    public static String getProperty(String property) {
        return getProperty(property, null);
    }

    public static String getProperty(String property, String defaultValue) {
        try {
            return System.getProperty(property, testDefaults.getString(property));
        } catch (MissingResourceException ex) {
            return System.getProperty(property, defaultValue);
        }
    }

    /**
     * Default name of database file to use for the test case.
     */
    public static final String DB_NAME = "fbtest.fdb";
    public static final String DB_USER = getProperty("test.user", "sysdba");
    public static final String DB_PASSWORD = getProperty("test.password", "masterkey");
    public static final String DB_PATH = getProperty("test.db.dir", "");
    public static final String DB_SERVER_URL = getProperty("test.db.host", "localhost");
    public static final int DB_SERVER_PORT = Integer.parseInt(getProperty("test.db.port", "3050"));
    public static final String DB_LC_CTYPE = getProperty("test.db.lc_ctype", "NONE");
    public static final String DB_DATASOURCE_URL = getdbpath(DB_NAME);

    public static String getDatabasePath() {
        return getDatabasePath(DB_NAME);
    }

    public static String getDatabasePath(String name) {
        if (!"127.0.0.1".equals(DB_SERVER_URL) && !"localhost".equals(DB_SERVER_URL))
            return DB_PATH + "/" + name;
        else
            return new File(DB_PATH, name).getAbsolutePath();
    }

    /**
     * Builds a firebird database connection string for the supplied database
     * file.
     * 
     * @param name
     * @return
     */
    public static String getdbpath(String name) {
        final String gdsType = getProperty("test.gds_type", null);
        if ("EMBEDDED".equalsIgnoreCase(gdsType) || "LOCAL".equalsIgnoreCase(gdsType)) {
            return new File(DB_PATH, name).getAbsolutePath();
        } else {
            return DB_SERVER_URL + "/" + DB_SERVER_PORT + ":" + getDatabasePath(name);
        }
    }

    /**
     * @return Default database connection properties for this testrun
     */
    public static Properties getDefaultPropertiesForConnection() {
        final Properties returnValue = new Properties();

        returnValue.setProperty("user", DB_USER);
        returnValue.setProperty("password", DB_PASSWORD);
        returnValue.setProperty("lc_ctype", DB_LC_CTYPE);

        return returnValue;
    }

    /**
     * @return {@link GDSType} for this testrun
     */
    public static GDSType getGdsType() {
        final GDSType gdsType = GDSType.getType(getProperty("test.gds_type", "PURE_JAVA"));
        if (gdsType == null) {
            throw new RuntimeException("Unrecognized value for 'test.gds_type' property.");
        }
        return gdsType;
    }

    private static final Map<GDSType, String> gdsTypeToUrlPrefixMap = new HashMap<GDSType, String>();
    static {
        gdsTypeToUrlPrefixMap.put(GDSType.getType("PURE_JAVA"), "jdbc:firebirdsql:");
        gdsTypeToUrlPrefixMap.put(GDSType.getType("EMBEDDED"), "jdbc:firebirdsql:embedded:");
        gdsTypeToUrlPrefixMap.put(GDSType.getType("NATIVE"), "jdbc:firebirdsql:native:");
        gdsTypeToUrlPrefixMap.put(GDSType.getType("ORACLE_MODE"), "jdbc:firebirdsql:oracle:");
        gdsTypeToUrlPrefixMap.put(GDSType.getType("LOCAL"), "jdbc:firebirdsql:local:");
        gdsTypeToUrlPrefixMap.put(GDSType.getType("NIO"), "jdbc:firebirdsql:nio:");
    }

    /**
     * @return JDBC URL (without parameters) for this testrun
     */
    public static String getUrl() {
        return gdsTypeToUrlPrefixMap.get(getGdsType()) + getdbpath(DB_NAME);
    }

    // FACTORY METHODS
    //
    // These methods should be used where possible so as to create the objects
    // bound to the
    // appropriate GDS implementation.

    public static FBManagedConnectionFactory createFBManagedConnectionFactory() {
        return new FBManagedConnectionFactory(getGdsType());
    }

    public static FBManagedConnectionFactory createFBManagedConnectionFactory(ConnectionManager cm) {
        FBManagedConnectionFactory mcf = new FBManagedConnectionFactory(getGdsType());
        mcf.setDefaultConnectionManager(new InternalConnectionManager());
        return mcf;
    }

    public static FBManager createFBManager() {
        return new FBManager(getGdsType());
    }

    public static FirebirdConnection getConnectionViaDriverManager() throws SQLException {
        return (FirebirdConnection) DriverManager.getConnection(getUrl(),
                getDefaultPropertiesForConnection());
    }

    /**
     * Creates the default test database.
     * 
     * @return Configured FBManager instance used for creation of the database
     * @throws Exception
     */
    public static FBManager defaultDatabaseSetUp() throws Exception {
        FBManager fbManager = createFBManager();

        if (getGdsType() == GDSType.getType("PURE_JAVA")
                || getGdsType() == GDSType.getType("NATIVE")) {
            fbManager.setServer(DB_SERVER_URL);
            fbManager.setPort(DB_SERVER_PORT);
        }
        fbManager.start();
        fbManager.setForceCreate(true);
        fbManager.createDatabase(getDatabasePath(), DB_USER, DB_PASSWORD);

        return fbManager;
    }

    /**
     * Deletes the default test database using the supplied FBManager (in
     * general this should be the same instance as was used for creating it).
     * 
     * @param fbManager
     *            FBManager instance
     * @throws Exception
     */
    public static void defaultDatabaseTearDown(FBManager fbManager) throws Exception {
        fbManager.dropDatabase(getDatabasePath(), DB_USER, DB_PASSWORD);
        fbManager.stop();
    }

    private FBTestProperties() {
    }
}
