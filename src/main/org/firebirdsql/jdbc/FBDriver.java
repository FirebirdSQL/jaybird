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
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Jaybird JDBC Driver implementation for the Firebird database.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class FBDriver implements FirebirdDriver {

    private final static Logger log;

    public static final String CHARSET = "charSet";
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

    private final Map<FBConnectionProperties, Reference<FBDataSource>> mcfToDataSourceMap =
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

    @Override
    public Connection connect(String url, final Properties info) throws SQLException {
        if (url == null) {
            throw new SQLException("url is null");
        }

        final GDSType type = GDSFactory.getTypeForProtocol(url);
        if (type == null) {
            return null;
        }

        final Properties mergedProperties = mergeProperties(url, info);
        final Map<String, String> normalizedInfo = FBDriverPropertyManager.normalize(mergedProperties);
        try {
            int qMarkIndex = url.indexOf('?');
            if (qMarkIndex != -1) {
                url = url.substring(0, qMarkIndex);
            }

            FBManagedConnectionFactory mcf = new FBManagedConnectionFactory(type);
            String databaseURL = GDSFactory.getDatabasePath(type, url);

            mcf.setDatabase(databaseURL);
            for (Map.Entry<String, String> entry : normalizedInfo.entrySet()) {
                mcf.setNonStandardProperty(entry.getKey(), entry.getValue());
            }

            FBTpbMapper.processMapping(mcf, mergedProperties);

            mcf = mcf.canonicalize();

            FBDataSource dataSource = createDataSource(mcf);

            return dataSource.getConnection(mcf.getUserName(), mcf.getPassword());

        } catch (ResourceException | GDSException resex) {
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
        final Reference<FBDataSource> dataSourceReference = mcfToDataSourceMap.get(cacheKey);
        return dataSourceReference != null ? dataSourceReference.get() : null;
    }

    @Override
    public FirebirdConnection connect(FirebirdConnectionProperties properties) throws SQLException {
        GDSType type = GDSType.getType(properties.getType());

        if (type == null)
            type = GDSFactory.getDefaultGDSType();
        try {
            FBManagedConnectionFactory mcf = new FBManagedConnectionFactory(type);

            mcf = mcf.canonicalize();

            FBDataSource dataSource = createDataSource(mcf);

            return (FirebirdConnection) dataSource.getConnection(mcf.getUserName(), mcf.getPassword());
        } catch (ResourceException ex) {
            throw new FBSQLException(ex);
        }
    }

    @Override
    public FirebirdConnectionProperties newConnectionProperties() {
        return new FBConnectionProperties();
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        if (url == null) {
            throw new SQLException("url is null");
        }

        for (String protocol : GDSFactory.getSupportedProtocols()) {
            if (url.startsWith(protocol))
                return true;
        }

        return false;
    }

     // TODO check the correctness of implementation
     // TODO convert parameters into constants
     @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return FBDriverPropertyManager.getDriverPropertyInfo(info);
    }

    @Override
    public int getMajorVersion() {
        return 4;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public boolean jdbcCompliant() {
        return true;
    }

    @Override
    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new FBDriverNotCapableException("Method getParentLogger() not supported");
    }

    /**
     * Merges the properties from the JDBC URL and properties object.
     * <p>
     * If a property is present in both, the property specified in the JDBC url takes precedence.
     * </p>
     *
     * @param jdbcUrl JDBC Url
     * @param connectionProperties Properties object
     * @return Map with connection properties
     */
    private static Properties mergeProperties(String jdbcUrl, Properties connectionProperties) {
        Properties mergedProperties = new Properties();
        if (connectionProperties != null) {
            for (String propertyName : connectionProperties.stringPropertyNames()) {
                mergedProperties.setProperty(propertyName, connectionProperties.getProperty(propertyName));
            }
        }

        convertUrlParams(jdbcUrl, mergedProperties);

        return mergedProperties;
    }

    /**
     * Extract properties specified as URL parameter into the specified map of properties.
     *
     * @param url
     *         specified URL.
     * @param info
     *         instance of {@link Map} into which values should
     *         be extracted.
     */
    private static void convertUrlParams(String url, Properties info) {
        if (url == null) {
            return;
        }

        int iQuestionMark = url.indexOf("?");
        if (iQuestionMark == -1) {
            return;
        }

        String propString = url.substring(iQuestionMark + 1);

        StringTokenizer st = new StringTokenizer(propString, "&;");
        while (st.hasMoreTokens()) {
            String propertyString = st.nextToken();
            int iIs = propertyString.indexOf("=");
            if (iIs > -1) {
                String property = propertyString.substring(0, iIs);
                String value = propertyString.substring(iIs + 1);
                info.setProperty(property, value);
            } else {
                info.setProperty(propertyString, "");
            }
        }
    }
}

