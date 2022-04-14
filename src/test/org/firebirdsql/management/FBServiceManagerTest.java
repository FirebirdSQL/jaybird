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
package org.firebirdsql.management;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.gds.impl.GDSServerVersion;
import org.firebirdsql.jaybird.props.PropertyConstants;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static org.firebirdsql.common.FBTestProperties.DB_SERVER_PORT;
import static org.firebirdsql.common.FBTestProperties.DB_SERVER_URL;
import static org.firebirdsql.common.FBTestProperties.GDS_TYPE;
import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.firebirdsql.common.matchers.GdsTypeMatchers.isEmbeddedType;
import static org.firebirdsql.common.matchers.GdsTypeMatchers.isOtherNativeType;
import static org.firebirdsql.jaybird.props.PropertyConstants.DEFAULT_SERVICE_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Tests for {@link org.firebirdsql.management.FBServiceManager}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
class FBServiceManagerTest {

    // NOTE Some of these tests may fail when using a Firebird 3.0 or earlier client library with the NATIVE protocol

    @ParameterizedTest
    @MethodSource
    void testGetServerVersion(String serverName, Integer portNumber, String serviceName) throws Exception {
        final FBServiceManager fbServiceManager = new FBServiceManager(FBTestProperties.getGdsType());
        fbServiceManager.setServerName(serverName);
        if (portNumber != null) fbServiceManager.setPortNumber(portNumber);
        fbServiceManager.setServiceName(serviceName);
        fbServiceManager.setUser(FBTestProperties.DB_USER);
        fbServiceManager.setPassword(FBTestProperties.DB_PASSWORD);

        final GDSServerVersion serverVersion = fbServiceManager.getServerVersion();

        assertThat(serverVersion, allOf(
                notNullValue(),
                not(equalTo(GDSServerVersion.INVALID_VERSION))));
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> testGetServerVersion() {
        List<Arguments> arguments = new ArrayList<>();
        arguments.add(Arguments.of(DB_SERVER_URL, DB_SERVER_PORT, null));
        arguments.add(Arguments.of(DB_SERVER_URL, DB_SERVER_PORT, DEFAULT_SERVICE_NAME));

        final String gdsTypeName = GDS_TYPE;
        if (isEmbeddedType().matches(gdsTypeName)) {
            arguments.add(Arguments.of(null, null, null));
            arguments.add(Arguments.of(null, null, DEFAULT_SERVICE_NAME));
        } else {
            final String serverName = DB_SERVER_URL;
            final String ipv6SafeServerName = serverName.indexOf(':') != -1 ? '[' + serverName + ']' : serverName;
            final List<String> urlFormats = new ArrayList<>();
            urlFormats.add("%1$s/%2$d:");
            urlFormats.add("%1$s/%2$d:%3$s");
            urlFormats.add("//%1$s:%2$d");
            urlFormats.add("//%1$s:%2$d/");
            urlFormats.add("//%1$s:%2$d/%3$s");
            if (DB_SERVER_PORT == PropertyConstants.DEFAULT_PORT) {
                urlFormats.add("%1$s:");
                urlFormats.add("%1$s:%3$s");
                urlFormats.add("//%1$s");
                urlFormats.add("//%1$s/");
                urlFormats.add("//%1$s/%3$s");
                if (serverName.equals("localhost")) {
                    // no hostname + port:
                    urlFormats.add("%3$s");
                }
                if (isOtherNativeType().matches(gdsTypeName)) {
                    urlFormats.add("%1$s");
                }
            }

            if (isOtherNativeType().matches(gdsTypeName)) {
                urlFormats.add("%1$s/%2$d");
                // NOTE: This test assumes a Firebird 3.0 or higher client library is used
                urlFormats.add("inet://%1$s:%2$d/%3$s");
                urlFormats.add("inet://%1$s:%2$d/");
                urlFormats.add("inet://%1$s:%2$d");
                // Not testing inet4/inet6
                if (getDefaultSupportInfo().isWindows() && isWindowsSystem()) {
                    // NOTE: This assumes the default WNET service name is used
                    urlFormats.add("wnet://%1$s/%3$s");
                    urlFormats.add("wnet://%1$s/");
                    urlFormats.add("wnet://%1$s");
                    urlFormats.add("\\\\%4$s\\%3$s");
                    urlFormats.add("\\\\%4$s\\");
                    urlFormats.add("\\\\%4$s");
                    if (serverName.equals("localhost") || serverName.equals("127.0.0.1")) {
                        urlFormats.add("wnet://%3$s");
                        urlFormats.add("wnet://");
                        urlFormats.add("xnet://%3$s");
                        urlFormats.add("xnet://");
                    }
                }
            }
            
            urlFormats.stream()
                    .map(urlFormat ->
                            String.format(urlFormat, ipv6SafeServerName, DB_SERVER_PORT, DEFAULT_SERVICE_NAME, serverName))
                    .map(url -> Arguments.of(null, null, url))
                    .forEach(arguments::add);
        }

        return arguments.stream();
    }

    private static boolean isWindowsSystem() {
        return System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win");
    }
}
