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

package org.firebirdsql.jdbc;


import java.sql.*;
import java.util.*;

import javax.resource.ResourceException;

import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.jca.*;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

/**
 * The Jaybird JDBC Driver implementation for the Firebird database.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class FBDriver implements Driver {

    private final static Logger log;

//    public static final String FIREBIRD_PROTOCOL = "jdbc:firebirdsql:";

    public static final String CHARSET = "charSet";
    public static final String USE_TRANSLATION = "useTranslation";
    public static final String USER = "user";
    public static final String USER_NAME = "user_name";
    public static final String PASSWORD = "password";
    public static final String DATABASE = "database";
    public static final String BLOB_BUFFER_LENGTH = "blob_buffer_length";
    public static final String TPB_MAPPING = "tpb_mapping";
    
    /**
     * @todo implement the default subject for the
     * standard connection.
     */

    private Map mcfToDataSourceMap = new HashMap();

    static{
       log = LoggerFactory.getLogger(FBDriver.class,false);
        try{
            DriverManager.registerDriver(new FBDriver());
        } catch(Exception ex) {
           if (log!=null) log.error("Could not register with driver manager", ex);
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
     * @param originalInfo a list of arbitrary string tag/value pairs as
     * connection arguments. Normally at least a "user" and
     * "password" property should be included.
     * @return a <code>Connection</code> object that represents a
     *         connection to the URL
     * @exception SQLException if a database access error occurs
     */
    public Connection connect(String url, Properties originalInfo)
        throws SQLException
    {
        final GDSType type = GDSFactory.getTypeForProtocol(url);

        try {
            if (originalInfo == null)
                originalInfo = new Properties();

            Properties normalizedInfo = FBDriverPropertyManager.normalize(url, originalInfo);
            
            int qMarkIndex = url.indexOf('?');
            if (qMarkIndex != -1)
                url = url.substring(0, qMarkIndex);

            Integer blobBufferLength = extractBlobBufferLength(normalizedInfo);
            

            FBManagedConnectionFactory mcf = new FBManagedConnectionFactory(type);
            
            FBConnectionRequestInfo conCri =
                FBConnectionHelper.getCri(normalizedInfo, mcf.getDefaultConnectionRequestInfo());

            //handleEncoding(info, conCri);

            FBTpbMapper tpbMapper = FBConnectionHelper.getTpbMapper(mcf.getGDS(), originalInfo);

            // extract the user
            String user = conCri.getStringProperty(ISCConstants.isc_dpb_user);

            if (user == null)
                user = conCri.getStringProperty(ISCConstants.isc_dpb_user_name);

            if (user == null)
                throw new FBSQLException(
                    "User for database connection not specified.",
                    FBSQLException.SQL_STATE_INVALID_CONN_ATTR);

            // extract the password
            String password = normalizedInfo.getProperty(PASSWORD);

            if (password == null)
                password = conCri.getStringProperty(ISCConstants.isc_dpb_password);

            if (password == null)
                throw new FBSQLException(
                    "Password for database connection not specified.",
                    FBSQLException.SQL_STATE_INVALID_CONN_ATTR);
            
            // extract the database URL

            String databaseURL = GDSFactory.getDatabasePath(type, url);

            mcf.setDatabase(databaseURL);
            mcf.setConnectionRequestInfo(conCri);
            
            if (tpbMapper != null)
                mcf.setTpbMapper(tpbMapper);
                
            if (blobBufferLength != null) 
            {
                mcf.setBlobBufferLength(blobBufferLength);                
            } // end of if ()
            mcf = mcf.canonicalize();

            FBDataSource dataSource = null;
            synchronized (mcfToDataSourceMap)
            {
                dataSource = (FBDataSource)mcfToDataSourceMap.get(mcf);
                
                if (dataSource == null) {
                    dataSource = (FBDataSource)mcf.createConnectionFactory();
                    mcfToDataSourceMap.put(mcf, dataSource);
                } // end of if ()
            }

            return dataSource.getConnection(user, password);
        } catch(ResourceException resex) {
            throw new FBSQLException(resex);
        } catch(GDSException ex) {
            throw new FBSQLException(ex);
        }
    }


    private Integer extractBlobBufferLength(Properties info) throws SQLException {
        String blobBufferLengthStr = (String)info.get(
            FBConnectionHelper.DPB_PREFIX + BLOB_BUFFER_LENGTH);
        
        if (blobBufferLengthStr == null)
            return null;
        
        try {
            return new Integer(blobBufferLengthStr);
        } catch (NumberFormatException e) {
            throw new FBSQLException("Blob buffer length " + blobBufferLengthStr
                    + " could not be converted to an integer",
                    FBSQLException.SQL_STATE_INVALID_CONN_ATTR);
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
        Set protocols = GDSFactory.getSupportedProtocols();
        
        for (Iterator iter = protocols.iterator(); iter.hasNext();) {
            String protocol = (String) iter.next();
            if (url.startsWith(protocol))
                return true;
        }
        
        return false;
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
        Properties info) throws  SQLException
    {
        /*
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
        */
        return FBDriverPropertyManager.getDriverPropertyInfo(info);
    }



    /**
     * Gets the driver's major version number. Initially this should be 1.
         * @return this driver's major version number
     */
    public int getMajorVersion() {
        return 2;
    }

    /**
     * Gets the driver's minor version number. Initially this should be 0.
         * @return this driver's minor version number
     */
    public int getMinorVersion() {
        return 0;
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
        return true;
    }
}

