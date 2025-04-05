// SPDX-FileCopyrightText: Copyright 2013-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.JaybirdSystemProperties;
import org.firebirdsql.jaybird.props.PropertyConstants;
import org.firebirdsql.jaybird.props.PropertyNames;
import org.firebirdsql.jaybird.props.def.ConnectionProperty;
import org.firebirdsql.jaybird.props.internal.ConnectionPropertyRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.firebirdsql.common.SystemPropertyHelper.withTemporarySystemProperty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests for {@link FbConnectionProperties}
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
class FbConnectionPropertiesTest {

    private final FbConnectionProperties info = new FbConnectionProperties();

    @Test
    void testDatabaseName() {
        assertNull(info.getDatabaseName());
        final String databaseName = "testDatabaseName";
        info.setDatabaseName(databaseName);
        assertEquals(databaseName, info.getDatabaseName());
    }

    @Test
    void testServerName() {
        assertNull(info.getServerName());
        final String serverName = "testServerName";
        info.setServerName(serverName);
        assertEquals(serverName, info.getServerName());
    }

    @Test
    void testPortNumber() {
        assertEquals(PropertyConstants.DEFAULT_PORT, info.getPortNumber());
        final int portNumber = 1234;
        info.setPortNumber(portNumber);
        assertEquals(portNumber, info.getPortNumber());
    }

    @Test
    void testUser() {
        assertNull(info.getUser());
        final String user = "testUser";
        info.setUser(user);
        assertEquals(user, info.getUser());
    }

    @Test
    void testPassword() {
        assertNull(info.getPassword());
        final String password = "testPassword";
        info.setPassword(password);
        assertEquals(password, info.getPassword());
    }

    @Test
    void testCharSet() {
        assertNull(info.getCharSet());
        final String charSet = "UTF-8";
        info.setCharSet(charSet);
        assertEquals(charSet, info.getCharSet());
        // Value of encoding should not be modified by charSet
        assertNull(info.getEncoding());
    }

    @Test
    void testEncoding() {
        assertNull(info.getEncoding());
        final String encoding = "UTF8";
        info.setEncoding(encoding);
        assertEquals(encoding, info.getEncoding());
        // Value of charSet should not be modified by encoding
        assertNull(info.getCharSet());
    }

    @Test
    void testRoleName() {
        assertNull(info.getRoleName());
        final String roleName = "ROLE1";
        info.setRoleName(roleName);
        assertEquals(roleName, info.getRoleName());
    }

    @Test
    void testSqlDialect() {
        assertEquals(PropertyConstants.DEFAULT_DIALECT, info.getSqlDialect());
        final int sqlDialect = 2;
        info.setSqlDialect(sqlDialect);
        assertEquals(sqlDialect, info.getSqlDialect());
    }

    @Test
    void testSocketBufferSize() {
        assertEquals(IConnectionProperties.DEFAULT_SOCKET_BUFFER_SIZE, info.getSocketBufferSize());
        final int socketBufferSize = 64 * 1024;
        info.setSocketBufferSize(socketBufferSize);
        assertEquals(socketBufferSize, info.getSocketBufferSize());
    }

    @Test
    void testBuffersNumber() {
        assertEquals(PropertyConstants.DEFAULT_PAGE_CACHE_SIZE, info.getPageCacheSize());
        final int buffersNumber = 2048;
        info.setPageCacheSize(buffersNumber);
        assertEquals(buffersNumber, info.getPageCacheSize());
    }

    @Test
    void testSoTimeout() {
        assertEquals(IAttachProperties.DEFAULT_SO_TIMEOUT, info.getSoTimeout());
        final int soTimeout = 4000;
        info.setSoTimeout(soTimeout);
        assertEquals(soTimeout, info.getSoTimeout());
    }

    @Test
    void testConnectTimeout() {
        assertEquals(IAttachProperties.DEFAULT_CONNECT_TIMEOUT, info.getConnectTimeout());
        final int connectTimeout = 5;
        info.setConnectTimeout(connectTimeout);
        assertEquals(connectTimeout, info.getConnectTimeout());
    }

    @Test
    void testWireCrypt() {
        assertEquals(WireCrypt.DEFAULT.name(), info.getWireCrypt());
        assertEquals(WireCrypt.DEFAULT, info.getWireCryptAsEnum());
        final WireCrypt wireCrypt = WireCrypt.DISABLED;
        info.setWireCryptAsEnum(wireCrypt);
        assertEquals(wireCrypt.name(), info.getWireCrypt());
        assertEquals(wireCrypt, info.getWireCryptAsEnum());
    }

    @Test
    void testWireCryptNullPointerExceptionOnNull() {
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> info.setWireCryptAsEnum(null))
                .withMessage("wireCrypt");
    }

    @Test
    void testDbCryptConfig() {
        assertNull(info.getDbCryptConfig());
        final String dbCryptConfig = "ABCDEF";
        info.setDbCryptConfig(dbCryptConfig);
        assertEquals(dbCryptConfig, info.getDbCryptConfig());
    }

    @Test
    void testAuthPlugins() {
        assertEquals(PropertyConstants.DEFAULT_AUTH_PLUGINS, info.getAuthPlugins());
        final String authPlugins = "XYZ,ABC";
        info.setAuthPlugins(authPlugins);
        assertEquals(authPlugins, info.getAuthPlugins());
    }

    @Test
    void testConnectionPropertyValues() {
        info.setSqlDialect(2);
        info.setConnectTimeout(15);
        info.setWireCryptAsEnum(WireCrypt.REQUIRED);
        info.setDbCryptConfig("XYZcrypt");
        info.setAuthPlugins("XXXauth");

        ConnectionPropertyRegistry registry = ConnectionPropertyRegistry.getInstance();
        Map<ConnectionProperty, Object> expected = new HashMap<>();
        expected.put(registry.getByName("sqlDialect"), 2);
        expected.put(registry.getByName("connectTimeout"), 15);
        expected.put(registry.getByName("wireCrypt"), WireCrypt.REQUIRED.name());
        expected.put(registry.getByName("dbCryptConfig"), "XYZcrypt");
        expected.put(registry.getByName("authPlugins"), "XXXauth");
        // Default value set through FbConnectionProperties()
        expected.put(registry.getByName("sessionTimeZone"), TimeZone.getDefault().getID());

        assertThat(info.connectionPropertyValues()).isEqualTo(expected);
    }

    @Test
    void testCopyConstructor() {
        info.setDatabaseName("testValue");
        info.setServerName("xyz");
        info.setPortNumber(1203);
        info.setSqlDialect(2);
        info.setConnectTimeout(15);
        info.setWireCryptAsEnum(WireCrypt.REQUIRED);
        info.setDbCryptConfig("XYZcrypt");
        info.setAuthPlugins("XXXauth");

        FbConnectionProperties copy = new FbConnectionProperties(info);
        assertThat(copy)
                .extracting("databaseName", "serverName", "portNumber")
                .isEqualTo(Arrays.<Object>asList("testValue", "xyz", 1203));
        assertThat(info.connectionPropertyValues()).isEqualTo(copy.connectionPropertyValues());
    }

    @Test
    void testAsImmutable() {
        info.setDatabaseName("testValue");
        info.setServerName("xyz");
        info.setPortNumber(1203);
        info.setSqlDialect(2);
        info.setConnectTimeout(15);
        info.setWireCryptAsEnum(WireCrypt.REQUIRED);
        info.setDbCryptConfig("XYZcrypt");
        info.setAuthPlugins("XXXauth");

        IConnectionProperties immutable = info.asImmutable();
        assertThat(immutable)
                .isInstanceOf(FbImmutableConnectionProperties.class)
                .extracting("databaseName", "serverName", "portNumber")
                .isEqualTo(Arrays.<Object>asList("testValue", "xyz", 1203));
        assertThat(info.connectionPropertyValues()).isEqualTo(immutable.connectionPropertyValues());
    }

    @Test
    void testSessionTimeZoneDefaultSpecialGmtOffsetHandling() {
        final TimeZone before = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("GMT-08:00"));
            // Need to create new instance after initializing TZ
            FbConnectionProperties info = new FbConnectionProperties();
            assertEquals("-08:00", info.getSessionTimeZone(), "Expected sessionTimeZone without GMT prefix");
        } finally {
            TimeZone.setDefault(before);
        }
    }

    @Test
    void testSessionTimeZoneNormalizationOfOffsets() {
        info.setSessionTimeZone("GMT+05:00");
        assertEquals("+05:00", info.getSessionTimeZone());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "*", "11" })
    void enableProtocolDefaultDerivedFromSystemProperty(String defaultValue) {
        try (var ignored = withTemporarySystemProperty(JaybirdSystemProperties.DEFAULT_ENABLE_PROTOCOL, defaultValue)) {
            assertEquals(defaultValue, new FbConnectionProperties().getEnableProtocol(),
                    "Unexpected enableProtocol value");
        }
    }

    @Test
    void defaultReportSQLWarningsValue() {
        assertEquals(PropertyConstants.DEFAULT_REPORT_SQL_WARNINGS, info.getReportSQLWarnings(),
                "Unexpected reportSQLWarnings value");
    }

    @ParameterizedTest
    @ValueSource(strings = { "ALL", "NONE" })
    void reportSQLWarningsDefaultDerivedFromSystemProperty(String defaultValue) {
        try (var ignored = withTemporarySystemProperty(
                JaybirdSystemProperties.DEFAULT_REPORT_SQL_WARNINGS, defaultValue)) {
            assertEquals(defaultValue, new FbConnectionProperties().getReportSQLWarnings(),
                    "Unexpected reportSQLWarnings value");
        }
    }

    @Test
    void reportSQLWarnings_invalidSystemPropertyValue_reportsALL() {
        try (var ignored = withTemporarySystemProperty(
                JaybirdSystemProperties.DEFAULT_REPORT_SQL_WARNINGS, "INVALID_VALUE")) {
            assertEquals(PropertyConstants.DEFAULT_REPORT_SQL_WARNINGS,
                    new FbConnectionProperties().getReportSQLWarnings(), "Unexpected reportSQLWarnings value");
        }
    }

    @Test
    void defaultAsyncFetchValue() {
        assertEquals(PropertyConstants.DEFAULT_ASYNC_FETCH, info.isAsyncFetch(), "Unexpected asyncFetch value");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "true", "false", "NOT_A_BOOLEAN" })
    void asyncFetchDefaultDerivedFromSystemProperty(String defaultValue) {
        try (var ignored = withTemporarySystemProperty(JaybirdSystemProperties.DEFAULT_ASYNC_FETCH, defaultValue)) {
            // Handle empty string as true, null as normal default
            boolean expectedValue = defaultValue == null
                    ? PropertyConstants.DEFAULT_ASYNC_FETCH
                    : defaultValue.isBlank() || Boolean.parseBoolean(defaultValue);
            var props = new FbConnectionProperties();
            assertEquals(expectedValue, props.isAsyncFetch(), "Unexpected asyncFetch value");

            // checking inverted value
            props.setAsyncFetch(!props.isAsyncFetch());
            assertEquals(!expectedValue, props.isAsyncFetch(), "Unexpected asyncFetch value");

            // explicitly clearing the property reverts to the default
            props.setBooleanProperty(PropertyNames.asyncFetch, null);
            assertEquals(expectedValue, props.isAsyncFetch(), "Unexpected asyncFetch value");
        }
    }

    @Test
    void defaultMaxInlineBlobSize() {
        assertEquals(PropertyConstants.DEFAULT_MAX_INLINE_BLOB_SIZE, info.getMaxInlineBlobSize(),
                "Unexpected maxInlineBlobSize value");
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            defaultValue, expectedValue
            ,             -1
            '',           -1
            -100,         0
            0,            0
            12345,        12345
            NOT_AN_INT,   -1
            """)
    void maxInlineBlobSizeDefaultDerivedFromSystemProperty(String defaultValue, int expectedValue) {
        try (var ignored = withTemporarySystemProperty(
                JaybirdSystemProperties.DEFAULT_MAX_INLINE_BLOB_SIZE, defaultValue)) {
            if (expectedValue == -1) {
                expectedValue = PropertyConstants.DEFAULT_MAX_INLINE_BLOB_SIZE;
            }
            var props = new FbConnectionProperties();
            assertEquals(expectedValue, props.getMaxInlineBlobSize(), "Unexpected maxInlineBlobSize value");

            // set to a different value
            props.setMaxInlineBlobSize(expectedValue + 10);
            assertEquals(expectedValue + 10, props.getMaxInlineBlobSize(), "Unexpected maxInlineBlobSize value");

            // explicitly clearing the property reverts to the default
            props.setIntProperty(PropertyNames.maxInlineBlobSize, null);
            assertEquals(expectedValue, props.getMaxInlineBlobSize(), "Unexpected maxInlineBlobSize value");
        }
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            setValue,    expectedValue
            # Integer.MIN_VALUE
            -0x80000000, 0
            -1,          0
            0,           0
            500,         500
            # Integer.MAX_VALUE
            0x7FFFFFFF,  0x7FFFFFFF
            """)
    void maxInlineBlobSize(int setValue, int expectedValue) {
        info.setMaxInlineBlobSize(setValue);
        assertEquals(expectedValue, info.getMaxInlineBlobSize(), "Unexpected maxInlineBlobSize value");
    }

    @Test
    void defaultMaxBlobCacheSize() {
        assertEquals(PropertyConstants.DEFAULT_MAX_BLOB_CACHE_SIZE, info.getMaxBlobCacheSize(),
                "Unexpected maxBlobCacheSize value");
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            defaultValue, expectedValue
            ,             -1
            '',           -1
            -100,         0
            0,            0
            12345,        12345
            NOT_AN_INT,   -1
            """)
    void maxBlobCacheSizeDefaultDerivedFromSystemProperty(String defaultValue, int expectedValue) {
        try (var ignored = withTemporarySystemProperty(
                JaybirdSystemProperties.DEFAULT_MAX_BLOB_CACHE_SIZE, defaultValue)) {
            if (expectedValue == -1) {
                expectedValue = PropertyConstants.DEFAULT_MAX_BLOB_CACHE_SIZE;
            }
            var props = new FbConnectionProperties();
            assertEquals(expectedValue, props.getMaxBlobCacheSize(), "Unexpected maxBlobCacheSize value");

            // set to a different value
            props.setMaxBlobCacheSize(expectedValue + 10);
            assertEquals(expectedValue + 10, props.getMaxBlobCacheSize(), "Unexpected maxBlobCacheSize value");

            // explicitly clearing the property reverts to the default
            props.setIntProperty(PropertyNames.maxBlobCacheSize, null);
            assertEquals(expectedValue, props.getMaxBlobCacheSize(), "Unexpected maxBlobCacheSize value");
        }
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            setValue,    expectedValue
            # Integer.MIN_VALUE
            -0x80000000, 0
            -1,          0
            0,           0
            500,         500
            # Integer.MAX_VALUE
            0x7FFFFFFF,  0x7FFFFFFF
            """)
    void maxBlobCacheSize(int setValue, int expectedValue) {
        info.setMaxBlobCacheSize(setValue);
        assertEquals(expectedValue, info.getMaxBlobCacheSize(), "Unexpected maxBlobCacheSize value");
    }

}
