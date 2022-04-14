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
package org.firebirdsql.gds.ng;

import org.firebirdsql.jaybird.props.PropertyConstants;
import org.firebirdsql.jaybird.props.def.ConnectionProperty;
import org.firebirdsql.jaybird.props.internal.ConnectionPropertyRegistry;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests for {@link FbConnectionProperties}
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
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
}
