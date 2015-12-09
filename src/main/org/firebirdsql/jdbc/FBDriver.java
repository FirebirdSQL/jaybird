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
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.jca.FBManagedConnectionFactory;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import javax.resource.ResourceException;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Jaybird JDBC Driver implementation for the Firebird database.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class FBDriver implements FirebirdDriver {

    private final static Logger log;

    public static final String CHARSET = "charSet";
    public static final String USE_TRANSLATION = "useTranslation";
    public static final String USER = "user";
    public static final String USER_NAME = "user_name";
    public static final String PASSWORD = "password";
    public static final String DATABASE = "database";
    public static final String BLOB_BUFFER_LENGTH = "blob_buffer_length";
    public static final String TPB_MAPPING = "tpb_mapping";
    
    /*
     * @todo implement the default subject for the
     * standard connection.
     */

    private final Map<FBConnectionProperties, SoftReference<FBDataSource>> mcfToDataSourceMap =
            new ConcurrentHashMap<>();
    private final ReferenceQueue<FBDataSource> dataSourceReferenceQueue = new ReferenceQueue<>();
    private final Object createDataSourceLock = new Object();

    static {
        log = LoggerFactory.getLogger(FBDriver.class);
        try {
            DriverManager.registerDriver(new FBDriver());
        } catch (Exception ex) {
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
     * @param originalInfo a list of arbitrary string tag/value pairs as
     * connection arguments. Normally at least a "user" and
     * "password" property should be included.
     * @return a <code>Connection</code> object that represents a
     *         connection to the URL
     * @exception SQLException if a database access error occurs
     */
    public Connection connect(String url, Properties originalInfo) throws SQLException {
        if (url == null) {
            throw new SQLException("url is null");
        }

        final GDSType type = GDSFactory.getTypeForProtocol(url);
        
        if (type == null)
            return null;

        try {
            if (originalInfo == null)
                originalInfo = new Properties();

            Map<String, String> normalizedInfo = FBDriverPropertyManager.normalize(url, originalInfo);
            
            int qMarkIndex = url.indexOf('?');
            if (qMarkIndex != -1)
                url = url.substring(0, qMarkIndex);

            FBManagedConnectionFactory mcf = new FBManagedConnectionFactory(type);
            
            String databaseURL = GDSFactory.getDatabasePath(type, url);

            mcf.setDatabase(databaseURL);
            for (Map.Entry<String, String> entry : normalizedInfo.entrySet()) {
                mcf.setNonStandardProperty(entry.getKey(), entry.getValue());
            }

            FBTpbMapper.processMapping(mcf, originalInfo);
            
            mcf = mcf.canonicalize();

            FBDataSource dataSource = createDataSource(mcf);

            return dataSource.getConnection(mcf.getUserName(), mcf.getPassword());
            
        } catch(ResourceException | GDSException resex) {
            throw new FBSQLException(resex);
        }
    }

    private FBDataSource createDataSource(final FBManagedConnectionFactory mcf) throws ResourceException {
        final FBConnectionProperties cacheKey = mcf.getCacheKey();
        FBDataSource dataSource = dataSourceFromCache(cacheKey);
        if (dataSource != null) return dataSource;
        synchronized (createDataSourceLock) {
            // Obtain again
            dataSource = dataSourceFromCache(cacheKey);
            if (dataSource == null) {
                dataSource = (FBDataSource) mcf.createConnectionFactory();
                mcfToDataSourceMap.put(cacheKey, new SoftReference<>(dataSource, dataSourceReferenceQueue));
            }
        }
        cleanDataSourceCache();
        return dataSource;
    }

    /**
     * Removes cleared references from the {@link #mcfToDataSourceMap} cache.
     */
    private void cleanDataSourceCache() {
        Reference<? extends FBDataSource> reference;
        while ((reference = dataSourceReferenceQueue.poll()) != null) {
            mcfToDataSourceMap.values().remove(reference);
        }
    }

    private FBDataSource dataSourceFromCache(final FBConnectionProperties cacheKey) {
        final SoftReference<FBDataSource> dataSourceReference = mcfToDataSourceMap.get(cacheKey);
        return dataSourceReference != null ? dataSourceReference.get() : null;
    }
    
    public FirebirdConnection connect(FirebirdConnectionProperties properties) throws SQLException {
        GDSType type = GDSType.getType(properties.getType());
        
        if (type == null)
            type = GDSFactory.getDefaultGDSType();
        try {
            FBManagedConnectionFactory mcf = new FBManagedConnectionFactory(type);
    
            mcf = mcf.canonicalize();
    
            FBDataSource dataSource = createDataSource(mcf);
    
            return (FirebirdConnection)dataSource.getConnection(mcf.getUserName(), mcf.getPassword());
        } catch(ResourceException ex) {
            throw new FBSQLException(ex);
        }
    }

    public FirebirdConnectionProperties newConnectionProperties() {
        return new  FBConnectionProperties();
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
        if (url == null) {
            throw new SQLException("url is null");
        }

        for (String protocol : GDSFactory.getSupportedProtocols()) {
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
     * TODO check the correctness of implementation
     * TODO convert parameters into constants
     */
    public DriverPropertyInfo[] getPropertyInfo(String url,
        Properties info) throws  SQLException {
        
        return FBDriverPropertyManager.getDriverPropertyInfo(info);
    }

    /**
     * Gets the driver's major version number. Initially this should be 1.
         * @return this driver's major version number
     */
    public int getMajorVersion() {
        return 3;
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

    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new FBDriverNotCapableException();
    }
}

