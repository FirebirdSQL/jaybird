/*   The contents of this file are subject to the Mozilla Public
 *   License Version 1.1 (the "License"); you may not use this file
 *   except in compliance with the License. You may obtain a copy of
 *   the License at http://www.mozilla.org/MPL/
 *   Alternatively, the contents of this file may be used under the
 *   terms of the GNU Lesser General Public License Version 2 or later (the
 *   "LGPL"), in which case the provisions of the GPL are applicable
 *   instead of those above. You may obtain a copy of the Licence at
 *   http://www.gnu.org/copyleft/lgpl.html
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    relevant License for more details.
 *
 *    This file was created by members of the firebird development team.
 *    All individual contributions remain the Copyright (C) of those
 *    individuals.  Contributors to this file are either listed here or
 *    can be obtained from a CVS history command.
 *
 *    All rights reserved.
 */

/* added by Roman Rokytskyy:
 *
 * CVS modification log:
 * $Log$
 * Revision 1.5  2002/02/02 18:58:24  d_jencks
 * converted to log4j logging and cleaned up some test problems.  If you do not wish to use log4j, you may leave out the log4j-core.jar and get no logging
 *
 * Revision 1.4  2001/11/25 23:15:55  d_jencks
 * Implemented autocommit that does not interfere with connections managed by XAResource or LocalTransaction.  Made Driver reuse ManagedConnectionFactory for same database url.
 *
 * Revision 1.3  2001/08/28 17:13:23  d_jencks
 * Improved formatting slightly, removed dos cr's
 *
 * Revision 1.2  2001/07/13 18:16:15  d_jencks
 * Implementation of jdbc 1.0 Driver contributed by Roman Rokytskyy
 *
 * Revision 1.3  2001/07/09 09:09:34  rrokytskyy
 * Switched to the FBUnmanagedConnection implementation
 *
 * Revision 1.2  2001/07/08 18:42:30  rrokytskyy
 * Implementation of main java.sql.Driver methods.
 *
 */


package org.firebirdsql.jdbc;



// imports --------------------------------------



import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import javax.security.auth.Subject;
import org.firebirdsql.jca.*;

import org.firebirdsql.logging.Logger;
import org.firebirdsql.gds.GDS;

/**
 *
 *   @see <related>
 *   @author David Jencks (davidjencks@earthlink.net)
 *   @version $ $
 */


/**
 * The interface that every driver class must implement.
 * <P>The Java SQL framework allows for multiple database drivers.
 *
 * <P>Each driver should supply a class that implements
 * the Driver interface.
 *
 * <P>The DriverManager will try to load as many drivers as it can
 * find and then for any given connection request, it will ask each
 * driver in turn to try to connect to the target URL.
 *
 * <P>It is strongly recommended that each Driver class should be
 * small and standalone so that the Driver class can be loaded and
 * queried without bringing in vast quantities of supporting code.
 *
 * <P>When a Driver class is loaded, it should create an instance of
 * itself and register it with the DriverManager. This means that a
 * user can load and register a driver by calling
 * <pre>
 *   <code>Class.forName("foo.bah.Driver")</code>
 * </pre>
 *
 * @see DriverManager
 * @see Connection
 */
public class FBDriver implements Driver {

   private final static Logger log;

    public static final String FIREBIRD_PROTOCOL = "jdbc:firebirdsql:";
    public static final String USER = "user";
    public static final String PASSWORD = "password";
    public static final String DATABASE = "database";

    /**
     * @todo implement the default subject for the
     * standard connection.
     */
    private Subject subject = null;

    private Map urlToDataSourceMap = new HashMap();

    static{
       log = Logger.getLogger(FBDriver.class);
        try{
            java.sql.DriverManager.registerDriver(new FBDriver());
        } catch(Exception ex) {
           log.error("Could not register with driver manager", ex);
        }
    }


    /**
     * Attempts to make a database connection to the given URL.
     * The driver should return "null" if it realizes it is the wrong kind
     * of driver to connect to the given URL.  This will be common, as when
     * the JDBC driver manager is asked to connect to a given URL it passes
     * the URL to each loaded driver in turn.
     *
     * <P>The driver should raise a SQLException if it is the right
     * driver to connect to the given URL, but has trouble connecting to
     * the database.
     *
     * <P>The java.util.Properties argument can be used to passed arbitrary
     * string tag/value pairs as connection arguments.
     * Normally at least "user" and "password" properties should be
     * included in the Properties.
     *
     * @param url the URL of the database to which to connect
     * @param info a list of arbitrary string tag/value pairs as
     * connection arguments. Normally at least a "user" and
     * "password" property should be included.
     * @return a <code>Connection</code> object that represents a
     *         connection to the URL
     * @exception SQLException if a database access error occurs
     */
    public Connection connect(String url, java.util.Properties info)
        throws SQLException
    {
        if (url == null || !url.startsWith(FIREBIRD_PROTOCOL)) 
        {
            return null;
        } // end of if ()
        
        try {
            FBConnectionRequestInfo conCri = 
                FBConnectionHelper.getCri(info, null);
            
            // extract the user
            String user = info.getProperty(USER);
            
            if (user == null)
                user = conCri.getStringProperty(GDS.isc_dpb_user_name);
            
            if (user == null)
                throw new SQLException(
                    "User for database connection not specified.");

            // extract the password
            String password = info.getProperty(PASSWORD);
            
            if (password == null)
                password = conCri.getStringProperty(GDS.isc_dpb_password);
            
            if (password == null)
                throw new SQLException(
                    "Password for database connection not specified.");

            // extract the database URL
            String databaseURL = url.substring(FIREBIRD_PROTOCOL.length());
            
            FBDataSource dataSource = 
                (FBDataSource)urlToDataSourceMap.get(databaseURL);
                
            if (dataSource == null) {
                FBManagedConnectionFactory factory = new FBManagedConnectionFactory();
                factory.setDatabase(databaseURL);
                
                // set connection request info 
                factory.setConnectionRequestInfo(FBConnectionHelper.getCri(
                        info, factory.getDefaultConnectionRequestInfo()));
                        
                dataSource = (FBDataSource)factory.createConnectionFactory();
                // urlToDataSourceMap.put(databaseURL, dataSource);
            } // end of if ()
            

            return dataSource.getConnection(user, password);
        } catch(javax.resource.ResourceException resex) {
            throw new SQLException(resex.getMessage());
        }
    }


    /**
     * Returns true if the driver thinks that it can open a connection
     * to the given URL.  Typically drivers will return true if they
     * understand the subprotocol specified in the URL and false if
     * they don't.
     *
     * @param url the URL of the database
     * @return true if this driver can connect to the given URL
     * @exception SQLException if a database access error occurs
     */
    public boolean acceptsURL(String url) throws  SQLException {
        return url.startsWith(FIREBIRD_PROTOCOL);
    }



    /**
     * Gets information about the possible properties for this driver.
     * <p>The getPropertyInfo method is intended to allow a generic GUI tool to
     * discover what properties it should prompt a human for in order to get
     * enough information to connect to a database.  Note that depending on
     * the values the human has supplied so far, additional values may become
     * necessary, so it may be necessary to iterate though several calls
     * to getPropertyInfo.
     *
     * @param url the URL of the database to which to connect
     * @param info a proposed list of tag/value pairs that will be sent on
     *          connect open
     * @return an array of DriverPropertyInfo objects describing possible
     *          properties.  This array may be an empty array if no properties
     *          are required.
     * @exception SQLException if a database access error occurs
     * @todo check the correctness of implementation
     * @todo convert parameters into constants
     */
    public DriverPropertyInfo[] getPropertyInfo(String url,
        java.util.Properties info) throws  SQLException
    {
        Vector properties = new Vector();
        String database = url.substring(FIREBIRD_PROTOCOL.length());
        String user = info.getProperty(USER);
        String passwd = info.getProperty(PASSWORD);

        // add non-empty database
        if ((database != null) && (database != "")) {
            DriverPropertyInfo dinfo =
                new DriverPropertyInfo(DATABASE, database);
            dinfo.required = true;
            properties.add(dinfo);
        }

        // add user if it is not null
        if (user != null) {
            DriverPropertyInfo dinfo =
                new DriverPropertyInfo(USER, user);
            dinfo.required = true;
            properties.add(dinfo);
        }

        // add password if it is not null
        if (passwd != null) {
            DriverPropertyInfo dinfo =
                new DriverPropertyInfo(PASSWORD, passwd);
            dinfo.required = true;
            properties.add(dinfo);
        }

        return (DriverPropertyInfo[])
            properties.toArray(new DriverPropertyInfo[0]);
    }



    /**
     * Gets the driver's major version number. Initially this should be 1.
         * @return this driver's major version number
     */
    public int getMajorVersion() {
        return 0;
    }

    /**
     * Gets the driver's minor version number. Initially this should be 0.
         * @return this driver's minor version number
     */
    public int getMinorVersion() {
        return 1;
    }


    /**
     * Reports whether this driver is a genuine JDBC
         * COMPLIANT<sup><font size=-2>TM</font></sup> driver.
     * A driver may only report true here if it passes the JDBC compliance
     * tests; otherwise it is required to return false.
     *
     * JDBC compliance requires full support for the JDBC API and full support
     * for SQL 92 Entry Level.  It is expected that JDBC compliant drivers will
     * be available for all the major commercial databases.
     *
     * This method is not intended to encourage the development of non-JDBC
     * compliant drivers, but is a recognition of the fact that some vendors
     * are interested in using the JDBC API and framework for lightweight
     * databases that do not support full database functionality, or for
     * special databases such as document information retrieval where a SQL
     * implementation may not be feasible.
     */
    public boolean jdbcCompliant() {
        return false; //Lets work to make it true!
    }

    /** @link dependency
     * @stereotype instantiate*/
    /*#FBConnection lnkFBConnection;*/
}

