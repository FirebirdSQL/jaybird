/*
 * Firebird Open Source JDBC Driver
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
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.jaybird.Version;
import org.firebirdsql.jaybird.props.InvalidPropertyValueException;
import org.firebirdsql.jaybird.xca.FBManagedConnectionFactory;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Jaybird JDBC Driver implementation for the Firebird database.
 *
 * @author David Jencks
 * @author Mark Rotteveel
 */
public class FBDriver implements FirebirdDriver {

    private static final Logger log;

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

        final Map<String, String> mergedProperties = mergeProperties(url, info);
        try {
            int qMarkIndex = url.indexOf('?');
            if (qMarkIndex != -1) {
                url = url.substring(0, qMarkIndex);
            }

            FBManagedConnectionFactory mcf = new FBManagedConnectionFactory(type);
            String databaseURL = GDSFactory.getDatabasePath(type, url);

            // NOTE: occurrence of an explicit connection property may override this
            mcf.setDatabaseName(databaseURL);
            for (Map.Entry<String, String> entry : mergedProperties.entrySet()) {
                try {
                    mcf.setProperty(entry.getKey(), entry.getValue());
                } catch (InvalidPropertyValueException e) {
                    throw e.asSQLException();
                }
            }

            FBTpbMapper.processMapping(mcf, mergedProperties);

            mcf = mcf.canonicalize();

            FBDataSource dataSource = createDataSource(mcf);

            return dataSource.getConnection(mcf.getUser(), mcf.getPassword());

        } catch (GDSException e) {
            throw new FBSQLException(e);
        }
    }

    private FBDataSource createDataSource(final FBManagedConnectionFactory mcf) {
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
        if (type == null) {
            type = GDSFactory.getDefaultGDSType();
        }

        FBManagedConnectionFactory mcf = new FBManagedConnectionFactory(type, (FBConnectionProperties) properties)
                .canonicalize();
        FBDataSource dataSource = createDataSource(mcf);
        return (FirebirdConnection) dataSource.getConnection(mcf.getUser(), mcf.getPassword());
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
        return Version.JAYBIRD_MAJOR_VERSION;
    }

    @Override
    public int getMinorVersion() {
        return Version.JAYBIRD_MINOR_VERSION;
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
     * Merges the properties from the JDBC URL and properties object, normalizes them to a standard name.
     * <p>
     * If a property with the exact same name is present in both, the property specified in the JDBC url takes
     * precedence. Occurrence of multiple (distinct) aliases for the same property result in a {@link SQLException}.
     * </p>
     * <p>
     * The property name that is the result of normalization, is implementation specific behaviour, and might change in
     * a future version of Jaybird. When present, the (normalized) property `"database"` will be excluded, this also
     * might change in the future.
     * </p>
     * <p>
     * The behaviour of this method does not necessarily match the behaviour of Jaybird when processing properties
     * during {@link #connect(String, Properties)}, as this method performs additional processing that is skipped during
     * connect.
     * </p>
     *
     * @param jdbcUrl
     *         JDBC Url
     * @param connectionProperties
     *         Properties object
     * @return New map object with the merged and normalized connection properties
     * @throws SQLException
     *         For failures to extract connection properties from {@code jdbcUrl} (URL decoding errors), or presence
     *         of the same property under multiple aliases.
     * @since 4.0.1
     */
    public static Map<String, String> normalizeProperties(String jdbcUrl, Properties connectionProperties)
            throws SQLException {
        Map<String, String> mergedProperties = mergeProperties(jdbcUrl, connectionProperties);
        return FBDriverPropertyManager.normalize(mergedProperties);
    }

    /**
     * Merges the properties from the JDBC URL and properties object.
     * <p>
     * If a property with the exact same name is present in both, the property specified in the JDBC url takes
     * precedence.
     * </p>
     *
     * @param jdbcUrl
     *         JDBC Url
     * @param connectionProperties
     *         Properties object
     * @return Map with connection properties
     * @throws SQLException
     *         For failures to extract connection properties from {@code jdbcUrl} (URL decoding errors)
     */
    private static Map<String, String> mergeProperties(String jdbcUrl, Properties connectionProperties)
            throws SQLException {
        Map<String, String> mergedProperties = new HashMap<>();
        if (connectionProperties != null) {
            for (String propertyName : connectionProperties.stringPropertyNames()) {
                mergedProperties.put(propertyName, connectionProperties.getProperty(propertyName));
            }
        }

        convertUrlParams(jdbcUrl, mergedProperties);

        return mergedProperties;
    }

    /**
     * Extract properties specified as URL parameter into the specified properties object.
     *
     * @param url
     *         specified URL.
     * @param info
     *         map into which values should be extracted.
     * @throws SQLException
     *         For failures to extract connection properties from {@code url} (URL decoding errors)
     */
    private static void convertUrlParams(String url, Map<String, String> info) throws SQLException {
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
                String property = urlDecode(propertyString.substring(0, iIs), url);
                String value = urlDecode(propertyString.substring(iIs + 1), url);
                info.put(property, value);
            } else {
                info.put(urlDecode(propertyString, url), "");
            }
        }
    }

    /**
     * Decodes URL encoded value, transforming exceptions to SQLExceptions
     *
     * @param encodedValue
     *         The value to decode
     * @return The decoded value
     * @throws SQLException
     *         If decoding fails (failures of {@link URLDecoder#decode(String, java.nio.charset.Charset)}
     */
    private static String urlDecode(String encodedValue, String url) throws SQLException {
        try {
            return URLDecoder.decode(encodedValue, StandardCharsets.UTF_8);
        } catch (RuntimeException e) {
            // NOTE: The UnsupportedEncodingException shouldn't occur because UTF-8 support is required in Java
            throw new FbExceptionBuilder()
                    .nonTransientConnectionException(JaybirdErrorCodes.jb_invalidConnectionString)
                    .messageParameter(url)
                    .messageParameter(e.toString())
                    .cause(e)
                    .toSQLException();
        }
    }
}

