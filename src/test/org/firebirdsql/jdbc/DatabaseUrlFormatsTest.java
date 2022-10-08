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

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.ds.FBSimpleDataSource;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.jaybird.props.PropertyConstants;
import org.firebirdsql.jaybird.props.PropertyNames;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.firebirdsql.common.FBTestProperties.DB_PASSWORD;
import static org.firebirdsql.common.FBTestProperties.DB_SERVER_PORT;
import static org.firebirdsql.common.FBTestProperties.DB_SERVER_URL;
import static org.firebirdsql.common.FBTestProperties.DB_USER;
import static org.firebirdsql.common.FBTestProperties.GDS_TYPE;
import static org.firebirdsql.common.FBTestProperties.getDatabasePath;
import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.firebirdsql.common.matchers.GdsTypeMatchers.isEmbeddedType;
import static org.firebirdsql.common.matchers.GdsTypeMatchers.isOtherNativeType;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the various formats of database URLs and database coordinates
 */
class DatabaseUrlFormatsTest {

    @RegisterExtension
    static UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll();

    @ParameterizedTest
    @MethodSource
    void testConnectionWithDriverManager(String url) throws Exception {
        try (Connection connection = DriverManager.getConnection(url, DB_USER, FBTestProperties.DB_PASSWORD)) {
            assertTrue(connection.isValid(1000));
        }
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> testConnectionWithDriverManager() {
        final List<String> urlPrefixes = Arrays.asList(
                GDSFactory.getPlugin(FBTestProperties.getGdsType()).getSupportedProtocols());
        final List<String> urls = urlsWithoutProtocolPrefix();

        return urlPrefixes.stream()
                .flatMap(urlPrefix -> urls.stream()
                        .map(url -> urlPrefix + url))
                .map(Arguments::of);
    }

    /**
     * Tests with empty URL (for example only {@code "jdbc:firebird:"}) and {@code databaseName} property.
     */
    @ParameterizedTest
    @MethodSource
    void testConnectionWithEmptyUrl(String url) throws Exception {
        Properties properties = FBTestProperties.getDefaultPropertiesForConnection();
        properties.put(PropertyNames.databaseName, getDatabasePath());

        try (Connection connection = DriverManager.getConnection(url, properties)) {
            assertTrue(connection.isValid(1000));
        }
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> testConnectionWithEmptyUrl() {
        return Arrays.stream(GDSFactory.getPlugin(FBTestProperties.getGdsType()).getSupportedProtocols())
                .map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource
    void testConnectionWithSimpleDataSource(String serverName, Integer portNumber, String databaseName)
            throws Exception {
        FBSimpleDataSource dataSource = new FBSimpleDataSource(FBTestProperties.getGdsType());
        dataSource.setUser(DB_USER);
        dataSource.setPassword(DB_PASSWORD);
        dataSource.setServerName(serverName);
        if (portNumber != null) dataSource.setPortNumber(portNumber);
        dataSource.setDatabaseName(databaseName);

        try (Connection connection = dataSource.getConnection()) {
            assertTrue(connection.isValid(1000));
        }
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> testConnectionWithSimpleDataSource() {
        return Stream.concat(
                urlsWithoutProtocolPrefix().stream()
                        // URLS with a question mark are only for JDBC url test
                        .filter(url -> url.indexOf('?') == -1)
                        .map(url -> Arguments.of(null, null, url)),
                Stream.of(Arguments.of(DB_SERVER_URL, DB_SERVER_PORT, FBTestProperties.getDatabasePath())));
    }

    private static List<String> urlsWithoutProtocolPrefix() {
        final String databasePath = getDatabasePath();
        final String serverName = DB_SERVER_URL;
        final String ipv6SafeServerName = serverName.indexOf(':') != -1 ? '[' + serverName + ']' : serverName;
        final int portNumber = DB_SERVER_PORT;
        final String gdsTypeName = GDS_TYPE;
        final List<String> urlFormats = new ArrayList<>();
        if (isEmbeddedType().matches(gdsTypeName)) {
            // file path only
            urlFormats.add("%3$s");
            urlFormats.add("nopath?databaseName=%3$s");
        } else {
            urlFormats.add("///%3$s");
            urlFormats.add("%1$s/%2$d:%3$s");
            urlFormats.add("//%1$s:%2$d/%3$s");
            if (portNumber == PropertyConstants.DEFAULT_PORT) {
                urlFormats.add("%1$s:%3$s");
                urlFormats.add("//%1$s/%3$s");
                if (serverName.equals("localhost")) {
                    // no hostname + port:
                    urlFormats.add("%3$s");
                }
            }

            // Cases with a question mark only work for JDBC url tests, not for data source databaseName
            // Everything in properties
            urlFormats.add("?serverName=%4$s&portNumber=%2$d&databaseName=%3$s");
            urlFormats.add("?databaseName=%1$s/%2$d:%3$s");
            urlFormats.add("?databaseName=//%1$s:%2$d/%3$s");
            // property specifies serverNamed and portNumber
            urlFormats.add("%3$s?serverName=%4$s&portNumber=%2$d");
            // Property overrides URL
            urlFormats.add("//doesnotexist:1234/nopath?databaseName=%1$s/%2$d:%3$s");
            urlFormats.add("doesnotexist/1234:nopath?databaseName=//%1$s:%2$d/%3$s");

            if (isOtherNativeType().matches(gdsTypeName)) {
                // NOTE: This test assumes a Firebird 3.0 or higher client library is used
                urlFormats.add("inet://%1$s:%2$d/%3$s");
                // Not testing inet4/inet6
                FirebirdSupportInfo supportInfo = getDefaultSupportInfo();
                if (supportInfo.isWindows() && isWindowsSystem()) {
                    if (supportInfo.supportsWnet()) {
                        // NOTE: This assumes the default WNET service name is used
                        urlFormats.add("wnet://%1$s/%3$s");
                        urlFormats.add("\\\\%4$s\\%3$s");
                        if (serverName.equals("localhost") || serverName.equals("127.0.0.1")) {
                            urlFormats.add("wnet://%3$s");
                        }
                    }
                    if (serverName.equals("localhost") || serverName.equals("127.0.0.1")) {
                        urlFormats.add("xnet://%3$s");
                    }
                }
            }
        }

        return urlFormats.stream()
                .map(urlFormat -> String.format(urlFormat, ipv6SafeServerName, portNumber, databasePath, serverName))
                .collect(Collectors.toList());
    }

    private static boolean isWindowsSystem() {
        return System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win");
    }

}
