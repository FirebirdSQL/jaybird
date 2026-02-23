// SPDX-FileCopyrightText: Copyright 2021-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.management;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.gds.impl.GDSServerVersion;
import org.firebirdsql.gds.ng.WireCrypt;
import org.firebirdsql.jaybird.props.PropertyConstants;
import org.firebirdsql.jaybird.props.PropertyNames;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

import static org.firebirdsql.common.FBTestProperties.DB_SERVER_PORT;
import static org.firebirdsql.common.FBTestProperties.DB_SERVER_URL;
import static org.firebirdsql.common.FBTestProperties.GDS_TYPE;
import static org.firebirdsql.common.FBTestProperties.configureServiceManager;
import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.firebirdsql.common.FBTestProperties.isLocalhost;
import static org.firebirdsql.common.FBTestProperties.supportsNativeModernUrls;
import static org.firebirdsql.common.matchers.GdsTypeMatchers.isEmbeddedType;
import static org.firebirdsql.common.matchers.GdsTypeMatchers.isOtherNativeType;
import static org.firebirdsql.jaybird.props.PropertyConstants.DEFAULT_SERVICE_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link org.firebirdsql.management.FBServiceManager}.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
class FBServiceManagerTest {

    // NOTE Some of these tests may fail when using a Firebird 3.0 or earlier client library with the NATIVE protocol

    @ParameterizedTest
    @MethodSource
    void testGetServerVersion(String serverName, Integer portNumber, String serviceName) throws Exception {
        final FBServiceManager fbServiceManager =
                configureServiceManager(new FBServiceManager(FBTestProperties.getGdsType()));
        fbServiceManager.setServerName(serverName);
        if (portNumber == null) {
            fbServiceManager.setProperty(PropertyNames.portNumber, null);
        } else {
            fbServiceManager.setPortNumber(portNumber);
        }
        fbServiceManager.setServiceName(serviceName);

        final GDSServerVersion serverVersion = fbServiceManager.getServerVersion();

        assertThat(serverVersion, allOf(
                notNullValue(),
                not(equalTo(GDSServerVersion.INVALID_VERSION))));
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> testGetServerVersion() {
        FirebirdSupportInfo supportInfo = getDefaultSupportInfo();
        final boolean supportsNamelessServiceManager = supportInfo.supportsNamelessServiceManager();
        List<Arguments> arguments = new ArrayList<>();
        if (supportsNamelessServiceManager) {
            arguments.add(Arguments.of(DB_SERVER_URL, DB_SERVER_PORT, null));
        }
        arguments.add(Arguments.of(DB_SERVER_URL, DB_SERVER_PORT, DEFAULT_SERVICE_NAME));

        final String gdsTypeName = GDS_TYPE;
        if (isEmbeddedType().matches(gdsTypeName)) {
            if (supportsNamelessServiceManager) {
                arguments.add(Arguments.of(null, null, null));
            }
            arguments.add(Arguments.of(null, null, DEFAULT_SERVICE_NAME));
        } else {
            final String serverName = DB_SERVER_URL;
            final boolean localhost = isLocalhost();
            final String ipv6SafeServerName = serverName.indexOf(':') != -1 ? '[' + serverName + ']' : serverName;
            final List<String> urlFormats = new ArrayList<>();
            urlFormats.add("%1$s/%2$d:%3$s");
            if (supportsNamelessServiceManager) {
                urlFormats.add("%1$s/%2$d:");
                urlFormats.add("//%1$s:%2$d");
                urlFormats.add("//%1$s:%2$d/");
            }
            urlFormats.add("//%1$s:%2$d/%3$s");
            if (DB_SERVER_PORT == PropertyConstants.DEFAULT_PORT) {
                urlFormats.add("%1$s:%3$s");
                if (supportsNamelessServiceManager) {
                    urlFormats.add("%1$s:");
                    urlFormats.add("//%1$s");
                    urlFormats.add("//%1$s/");
                }
                urlFormats.add("//%1$s/%3$s");
                if (localhost) {
                    // no hostname + port:
                    urlFormats.add("%3$s");
                }
                if (isOtherNativeType().matches(gdsTypeName) && supportsNamelessServiceManager) {
                    urlFormats.add("%1$s");
                }
            }

            if (isOtherNativeType().matches(gdsTypeName)) {
                if (supportsNamelessServiceManager) {
                    urlFormats.add("%1$s/%2$d");
                }
                final boolean supportsNativeModernUrls = supportsNativeModernUrls();
                if (supportsNativeModernUrls) {
                    urlFormats.add("inet://%1$s:%2$d/%3$s");
                    urlFormats.add("inet://%1$s:%2$d/");
                    urlFormats.add("inet://%1$s:%2$d");
                }
                // Not testing inet4/inet6
                if (supportInfo.isWindows() && isWindowsSystem()) {
                    if (supportInfo.supportsWnet()) {
                        // NOTE: This assumes the default WNET service name is used
                        if (supportsNativeModernUrls) {
                            urlFormats.add("wnet://%1$s/%3$s");
                            urlFormats.add("wnet://%1$s/");
                            urlFormats.add("wnet://%1$s");
                            if (localhost) {
                                urlFormats.add("wnet://%3$s");
                                urlFormats.add("wnet://");
                            }
                        }
                        urlFormats.add("\\\\%4$s\\%3$s");
                        if (supportsNamelessServiceManager) {
                            urlFormats.add("\\\\%4$s\\");
                            urlFormats.add("\\\\%4$s");
                        }
                    }
                    if (supportsNativeModernUrls && localhost) {
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

    @ParameterizedTest
    @MethodSource
    void testBeanProperties(PropertyDescriptor beanProperty) throws Exception {
        var serviceManager = new FBServiceManager(FBTestProperties.getGdsType());
        Method readMethod = beanProperty.getReadMethod();
        final Object originalValue = readMethod.invoke(serviceManager);

        final Object testValue = generateTestValue(beanProperty, originalValue);
        Method writeMethod = beanProperty.getWriteMethod();
        writeMethod.invoke(serviceManager, testValue);

        assertEquals(testValue, readMethod.invoke(serviceManager),
                "Unexpected value read back from property " + beanProperty.getName());
    }

    static Stream<Arguments> testBeanProperties() throws IntrospectionException {
        final var excludedProperties = Set.of("type", "logger", "serverVersion");
        BeanInfo serviceManagerBeanInfo = Introspector.getBeanInfo(FBServiceManager.class, Object.class);
        return Stream.of(serviceManagerBeanInfo.getPropertyDescriptors())
                .filter(property -> !excludedProperties.contains(property.getName()))
                .map(Arguments::of);
    }

    private static Object generateTestValue(PropertyDescriptor beanProperty, Object originalValue) {
        String propertyName = beanProperty.getName();
        if ("wireCrypt".equals(propertyName)) {
            return "ENABLED".equals(originalValue) ? "DISABLED" : "ENABLED";
        }
        return switch (beanProperty.getPropertyType().getName()) {
            case "java.lang.String" -> "testValue " + propertyName;
            case "boolean", "java.lang.Boolean" -> originalValue == null || !((boolean) originalValue);
            case "int", "java.lang.Integer" -> originalValue == null ? 1 : ((int) originalValue) + 1;
            case "org.firebirdsql.gds.ng.WireCrypt" ->
                    originalValue == WireCrypt.ENABLED ? WireCrypt.DISABLED : WireCrypt.ENABLED;
            default -> throw new IllegalStateException("Property: %s has unsupported type: %s"
                    .formatted(propertyName, beanProperty.getPropertyType()));
        };
    }

}
