/*
 SPDX-FileCopyrightText: Copyright 2001-2002 David Jencks
 SPDX-FileCopyrightText: Copyright 2002-2005 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2002-2003 Blas Rodriguez Somoza
 SPDX-FileCopyrightText: Copyright 2002 Mark O'Donohue
 SPDX-FileCopyrightText: Copyright 2003 Ryan Baldwin
 SPDX-FileCopyrightText: Copyright 2011-2024 Mark Rotteveel
 SPDX-License-Identifier: LGPL-2.1-or-later
*/
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.jaybird.Version;
import org.firebirdsql.jaybird.props.InvalidPropertyValueException;
import org.firebirdsql.jaybird.xca.FBManagedConnectionFactory;

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
 * <p>
 * Although this class is formally public API of Jaybird, any method not defined by the {@link Driver} and
 * {@link FirebirdDriver} interfaces is considered internal API and may change between point releases.
 * </p>
 *
 * @author David Jencks
 * @author Mark Rotteveel
 */
public class FBDriver implements FirebirdDriver {

    private final Map<FBConnectionProperties, Reference<FBDataSource>> mcfToDataSourceMap =
            new ConcurrentHashMap<>();
    private final ReferenceQueue<FBDataSource> dataSourceReferenceQueue = new ReferenceQueue<>();
    private final Object createDataSourceLock = new Object();

    static {
        try {
            DriverManager.registerDriver(new FBDriver());
        } catch (Exception ex) {
            System.getLogger(FBDriver.class.getName())
                    .log(System.Logger.Level.ERROR, "Could not register with driver manager", ex);
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
            throw FbExceptionBuilder.forNonTransientConnectionException(JaybirdErrorCodes.jb_invalidConnectionString)
                    .messageParameter(url, e)
                    .cause(e)
                    .toSQLException();
        }
    }
}

