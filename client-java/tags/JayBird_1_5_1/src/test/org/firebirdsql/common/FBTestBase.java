/*
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

import org.firebirdsql.gds.GDSType;
import org.firebirdsql.gds.GDSFactory;
import org.firebirdsql.jca.FBManagedConnectionFactory;
import org.firebirdsql.jca.FBConnectionRequestInfo;
import org.firebirdsql.management.FBManager;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;
import org.firebirdsql.pool.FBWrappingDataSource;
import org.firebirdsql.pool.FBConnectionPoolDataSource;
import org.firebirdsql.jdbc.FBDriver;

import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;

/**
 * Base class for test cases which could be run against more then a single GDS implementation.
 */
public class FBTestBase extends SimpleFBTestBase
    {
    protected final Logger log = LoggerFactory.getLogger(getClass(),true);

    /**
     *
     */
    protected static final String DB_LC_CTYPE    = System.getProperty("test.db.lc_ctype", "NONE");

     /**
     *
     */
    protected final String DB_DATASOURCE_URL = getdbpath(DB_NAME);



    protected FBTestBase(String name)
        {
        super(name);
        }

    // FACTORY METHODS
    //
    // These methods should be used where possible so as to create the objects bound to the
    // appropriate GDS implementation.

    /**
     *
     * @return
     * @throws SQLException
     */
    protected FBConnectionPoolDataSource createFBConnectionPoolDataSource() throws SQLException
        {
        final FBConnectionPoolDataSource returnValue = new FBConnectionPoolDataSource();

        returnValue.setType(getGdsType().toString());

        return returnValue;
        }

    /**
     *
     * @return
     */
    protected FBManagedConnectionFactory createFBManagedConnectionFactory()
        {
        return new FBManagedConnectionFactory(getGdsType());
        }

    /**
     *
     * @return
     */
    protected FBManager createFBManager()
        {
        return new FBManager(getGdsType());
        }

    /**
     *
     * @return
     * @throws SQLException
     */
    protected FBWrappingDataSource createFBWrappingDataSource() throws SQLException
        {
        final FBWrappingDataSource returnValue = new FBWrappingDataSource();

        returnValue.setType(getGdsType().toString());

        return returnValue;
        }

    /**
     *
     * @return
     */
    protected FBConnectionRequestInfo createFBConnectionRequestInfo()
        {
        return FBConnectionRequestInfo.newInstance(GDSFactory.getGDSForType(getGdsType()));
        }

    /**
     *
     * @return
     * @throws SQLException
     */
    protected Connection getConnectionViaDriverManager() throws SQLException
        {
        try {
            Class.forName(FBDriver.class.getName());
        } catch(ClassNotFoundException ex) {
            throw new SQLException("No suitable driver.");
        }
        
        return DriverManager.getConnection(getUrl(), getDefaultPropertiesForConnection());
        }

    /**
    *
    */
    protected Properties getDefaultPropertiesForConnection()
        {
        final Properties returnValue = new Properties();

        returnValue.setProperty(FBDriver.USER, DB_USER);
        returnValue.setProperty(FBDriver.PASSWORD, DB_PASSWORD);
        returnValue.setProperty("lc_ctype", DB_LC_CTYPE);

        return returnValue;
        }


    // USEFULL PROPERTY GETTERS

    protected String getUrl()
        {
        return gdsTypeToUrlPrefixMap.get(getGdsType()) + getdbpath(DB_NAME);
        }

    protected GDSType getGdsType()
        {
        final GDSType gdsType = GDSType.getType(System.getProperty("test.gds_type"));
        if( gdsType == null )
            throw new RuntimeException("Unrecoginzed value for 'test.gds_type' property.");

        return gdsType;
        }


    // STANDARD RIG

    protected void setUp() throws Exception
        {
        try
            {
            fbManager = createFBManager();

            fbManager.setServer(DB_SERVER_URL);
            fbManager.setPort(DB_SERVER_PORT);
            fbManager.start();
            fbManager.createDatabase(DB_PATH + "/" + DB_NAME, DB_USER, DB_PASSWORD);
            }
        catch (Exception e)
            {
            if (log!=null) log.warn("exception in setup of " + getName() + ": ", e);
            } // end of try-catch
        }

    protected void tearDown() throws Exception
        {
        try
            {
            fbManager.dropDatabase(DB_DATASOURCE_URL, DB_USER, DB_PASSWORD);
            fbManager.stop();
            fbManager = null;
            }
        catch (Exception e)
            {
            if (log!=null) log.warn("exception in teardown of " + getName() + ": ", e);
            } // end of try-catch
        }


    private FBManager fbManager = null;

    private static final Map gdsTypeToUrlPrefixMap = new HashMap();
    static
        {
        gdsTypeToUrlPrefixMap.put(GDSType.PURE_JAVA,       "jdbc:firebirdsql:");
        gdsTypeToUrlPrefixMap.put(GDSType.NATIVE_EMBEDDED, "jdbc:firebirdsql:embedded:");
        gdsTypeToUrlPrefixMap.put(GDSType.NATIVE,          "jdbc:firebirdsql:native:");
        }
    }
