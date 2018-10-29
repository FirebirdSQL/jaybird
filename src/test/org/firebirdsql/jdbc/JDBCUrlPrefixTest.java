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

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.rules.UsesDatabase;
import org.firebirdsql.gds.impl.jni.*;
import org.firebirdsql.gds.impl.nativeoo.FbOOEmbeddedGDSFactoryPlugin;
import org.firebirdsql.gds.impl.nativeoo.FbOOLocalGDSFactoryPlugin;
import org.firebirdsql.gds.impl.nativeoo.FbOONativeGDSFactoryPlugin;
import org.firebirdsql.gds.impl.oo.OOGDSFactoryPlugin;
import org.firebirdsql.gds.impl.wire.WireGDSFactoryPlugin;
import org.firebirdsql.jca.FBManagedConnectionFactory;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.firebirdsql.common.FBTestProperties.getDefaultPropertiesForConnection;
import static org.firebirdsql.common.matchers.GdsTypeMatchers.isEmbeddedType;
import static org.firebirdsql.common.matchers.GdsTypeMatchers.isOtherNativeType;
import static org.firebirdsql.common.matchers.GdsTypeMatchers.isPureJavaType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeThat;

/**
 * Tests for alternative JDBC URLs supported.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
@RunWith(Parameterized.class)
public class JDBCUrlPrefixTest {

    @ClassRule
    public static final UsesDatabase usesDatabase = UsesDatabase.usesDatabase();

    private final String urlPrefix;
    private final String expectedType;

    public JDBCUrlPrefixTest(String urlPrefix, String expectedType) {
        this.urlPrefix = urlPrefix;
        this.expectedType = expectedType;
    }

    @Parameterized.Parameters(name = "{0} => {1}")
    public static List<Object[]> parameters() {
        return Arrays.asList(
                testCase("jdbc:firebirdsql:", WireGDSFactoryPlugin.PURE_JAVA_TYPE_NAME),
                testCase("jdbc:firebirdsql:java:", WireGDSFactoryPlugin.PURE_JAVA_TYPE_NAME),
                testCase("jdbc:firebird:", WireGDSFactoryPlugin.PURE_JAVA_TYPE_NAME),
                testCase("jdbc:firebird:java:", WireGDSFactoryPlugin.PURE_JAVA_TYPE_NAME),
                testCase("jdbc:firebirdsql:oo:", OOGDSFactoryPlugin.TYPE_NAME),
                testCase("jdbc:firebird:oo:", OOGDSFactoryPlugin.TYPE_NAME),
                testCase("jdbc:firebirdsql:embedded:", EmbeddedGDSFactoryPlugin.EMBEDDED_TYPE_NAME),
                testCase("jdbc:firebird:embedded:", EmbeddedGDSFactoryPlugin.EMBEDDED_TYPE_NAME),
                testCase("jdbc:firebirdsql:fboo:embedded:", FbOOEmbeddedGDSFactoryPlugin.EMBEDDED_TYPE_NAME),
                testCase("jdbc:firebird:fboo:embedded:", FbOOEmbeddedGDSFactoryPlugin.EMBEDDED_TYPE_NAME),
                testCase("jdbc:firebirdsql:native:", NativeGDSFactoryPlugin.NATIVE_TYPE_NAME),
                testCase("jdbc:firebird:native:", NativeGDSFactoryPlugin.NATIVE_TYPE_NAME),
                testCase("jdbc:firebirdsql:fboo:native:", FbOONativeGDSFactoryPlugin.NATIVE_TYPE_NAME),
                testCase("jdbc:firebird:fboo:native:", FbOONativeGDSFactoryPlugin.NATIVE_TYPE_NAME),
                testCase("jdbc:firebirdsql:local:", LocalGDSFactoryPlugin.LOCAL_TYPE_NAME),
                testCase("jdbc:firebird:local:", LocalGDSFactoryPlugin.LOCAL_TYPE_NAME),
                testCase("jdbc:firebirdsql:fboo:local:", FbOOLocalGDSFactoryPlugin.LOCAL_TYPE_NAME),
                testCase("jdbc:firebird:fboo:local:", FbOOLocalGDSFactoryPlugin.LOCAL_TYPE_NAME)
        );
    }

    @Before
    public void checkGDSType() {
        if (isPureJavaType().matches(expectedType)) {
            assumeThat("Unexpected GDS type", FBTestProperties.GDS_TYPE, isPureJavaType());
        } else if (isEmbeddedType().matches(expectedType)) {
            assumeThat("Unexpected GDS type", FBTestProperties.GDS_TYPE, isEmbeddedType());
        } else if (isOtherNativeType().matches(expectedType)) {
            assumeThat("Unexpected GDS type", FBTestProperties.GDS_TYPE, isOtherNativeType());
        }
    }

    @Test
    public void verifyUrl() throws SQLException {
        String url = getUrl();
        try (Connection connection = DriverManager.getConnection(url, getDefaultPropertiesForConnection())) {
            assertTrue("connection isValid", connection.isValid(500));
            FBConnection fbConnection = connection.unwrap(FBConnection.class);
            FBManagedConnectionFactory fbManagedConnectionFactory =
                    (FBManagedConnectionFactory) fbConnection.getManagedConnection().getManagedConnectionFactory();
            String actualType = fbManagedConnectionFactory.getType();
            assertEquals("unexpected GDS type", expectedType, actualType);
        }
    }

    private static Object[] testCase(String urlPrefix, String expectedType) {
        return new Object[] { urlPrefix, expectedType };
    }

    private String getUrl() {
        return urlPrefix + FBTestProperties.getdbpath(FBTestProperties.DB_NAME);
    }
}
