// SPDX-FileCopyrightText: Copyright 2018-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.gds.impl.jni.EmbeddedGDSFactoryPlugin;
import org.firebirdsql.gds.impl.jni.NativeGDSFactoryPlugin;
import org.firebirdsql.gds.impl.wire.WireGDSFactoryPlugin;
import org.firebirdsql.jaybird.xca.FBManagedConnectionFactory;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.stream.Stream;

import static org.firebirdsql.common.FBTestProperties.getDefaultPropertiesForConnection;
import static org.firebirdsql.common.matchers.GdsTypeMatchers.isEmbeddedType;
import static org.firebirdsql.common.matchers.GdsTypeMatchers.isOtherNativeType;
import static org.firebirdsql.common.matchers.GdsTypeMatchers.isPureJavaType;
import static org.firebirdsql.common.matchers.MatcherAssume.assumeThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for alternative JDBC URLs supported.
 *
 * @author Mark Rotteveel
 */
class JDBCUrlPrefixTest {

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll();

    @SuppressWarnings("deprecation")
    static Stream<Arguments> parameters() {
        return Stream.of(
                testCase("jdbc:firebirdsql:", WireGDSFactoryPlugin.PURE_JAVA_TYPE_NAME),
                testCase("jdbc:firebirdsql:java:", WireGDSFactoryPlugin.PURE_JAVA_TYPE_NAME),
                testCase("jdbc:firebird:", WireGDSFactoryPlugin.PURE_JAVA_TYPE_NAME),
                testCase("jdbc:firebird:java:", WireGDSFactoryPlugin.PURE_JAVA_TYPE_NAME),
                testCase("jdbc:firebirdsql:embedded:", EmbeddedGDSFactoryPlugin.EMBEDDED_TYPE_NAME),
                testCase("jdbc:firebird:embedded:", EmbeddedGDSFactoryPlugin.EMBEDDED_TYPE_NAME),
                testCase("jdbc:firebirdsql:native:", NativeGDSFactoryPlugin.NATIVE_TYPE_NAME),
                testCase("jdbc:firebird:native:", NativeGDSFactoryPlugin.NATIVE_TYPE_NAME),
                // For backwards compatibility
                testCase("jdbc:firebird:local:", NativeGDSFactoryPlugin.NATIVE_TYPE_NAME),
                testCase("jdbc:firebird:local:", NativeGDSFactoryPlugin.NATIVE_TYPE_NAME));
    }

    void checkGDSType(String expectedType) {
        if (isPureJavaType().matches(expectedType)) {
            assumeThat("Unexpected GDS type", FBTestProperties.GDS_TYPE, isPureJavaType());
        } else if (isEmbeddedType().matches(expectedType)) {
            assumeThat("Unexpected GDS type", FBTestProperties.GDS_TYPE, isEmbeddedType());
        } else if (isOtherNativeType().matches(expectedType)) {
            assumeThat("Unexpected GDS type", FBTestProperties.GDS_TYPE, isOtherNativeType());
        }
    }

    @ParameterizedTest(name = "{0} => {1}")
    @MethodSource("parameters")
    void verifyUrl(String urlPrefix, String expectedType) throws SQLException {
        checkGDSType(expectedType);
        String url = getUrl(urlPrefix);
        try (Connection connection = DriverManager.getConnection(url, getDefaultPropertiesForConnection())) {
            assertTrue(connection.isValid(500), "connection isValid");
            FBConnection fbConnection = connection.unwrap(FBConnection.class);
            FBManagedConnectionFactory fbManagedConnectionFactory =
                    fbConnection.getManagedConnection().getManagedConnectionFactory();
            String actualType = fbManagedConnectionFactory.getType();
            assertEquals(expectedType, actualType, "unexpected GDS type");
        }
    }

    private static Arguments testCase(String urlPrefix, String expectedType) {
        return Arguments.of(urlPrefix, expectedType);
    }

    private String getUrl(String urlPrefix) {
        return urlPrefix + FBTestProperties.getdbpath(FBTestProperties.DB_NAME);
    }
}
