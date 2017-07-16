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
package org.firebirdsql.gds.ng;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests for {@link FbConnectionProperties}
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class FbConnectionPropertiesTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private final FbConnectionProperties info = new FbConnectionProperties();

    @Test
    public void testDatabaseName() {
        assertNull(info.getDatabaseName());
        final String databaseName = "testDatabaseName";
        info.setDatabaseName(databaseName);
        assertEquals(databaseName, info.getDatabaseName());
    }

    @Test
    public void testServerName() {
        assertEquals("localhost", info.getServerName());
        final String serverName = "testServerName";
        info.setServerName(serverName);
        assertEquals(serverName, info.getServerName());
    }

    @Test
    public void testPortNumber() {
        assertEquals(IConnectionProperties.DEFAULT_PORT, info.getPortNumber());
        final int portNumber = 1234;
        info.setPortNumber(portNumber);
        assertEquals(portNumber, info.getPortNumber());
    }

    @Test
    public void testUser() {
        assertNull(info.getUser());
        final String user = "testUser";
        info.setUser(user);
        assertEquals(user, info.getUser());
    }

    @Test
    public void testPassword() {
        assertNull(info.getPassword());
        final String password = "testPassword";
        info.setPassword(password);
        assertEquals(password, info.getPassword());
    }

    @Test
    public void testCharSet() {
        assertNull(info.getCharSet());
        final String charSet = "UTF-8";
        info.setCharSet(charSet);
        assertEquals(charSet, info.getCharSet());
        // Value of encoding should not be modified by charSet
        assertNull(info.getEncoding());
    }

    @Test
    public void testEncoding() {
        assertNull(info.getEncoding());
        final String encoding = "UTF8";
        info.setEncoding(encoding);
        assertEquals(encoding, info.getEncoding());
        // Value of charSet should not be modified by encoding
        assertNull(info.getCharSet());
    }

    @Test
    public void testRoleName() {
        assertNull(info.getRoleName());
        final String roleName = "ROLE1";
        info.setRoleName(roleName);
        assertEquals(roleName, info.getRoleName());
    }

    @Test
    public void testSqlDialect() {
        assertEquals(IConnectionProperties.DEFAULT_DIALECT, info.getConnectionDialect());
        final short sqlDialect = 2;
        info.setConnectionDialect(sqlDialect);
        assertEquals(sqlDialect, info.getConnectionDialect());
    }

    @Test
    public void testSocketBufferSize() {
        assertEquals(IConnectionProperties.DEFAULT_SOCKET_BUFFER_SIZE, info.getSocketBufferSize());
        final int socketBufferSize = 64 * 1024;
        info.setSocketBufferSize(socketBufferSize);
        assertEquals(socketBufferSize, info.getSocketBufferSize());
    }

    @Test
    public void testBuffersNumber() {
        assertEquals(IConnectionProperties.DEFAULT_BUFFERS_NUMBER, info.getPageCacheSize());
        final int buffersNumber = 2048;
        info.setPageCacheSize(buffersNumber);
        assertEquals(buffersNumber, info.getPageCacheSize());
    }

    @Test
    public void testSoTimeout() {
        assertEquals(IConnectionProperties.DEFAULT_SO_TIMEOUT, info.getSoTimeout());
        final int soTimeout = 4000;
        info.setSoTimeout(soTimeout);
        assertEquals(soTimeout, info.getSoTimeout());
    }

    @Test
    public void testConnectTimeout() {
        assertEquals(IConnectionProperties.DEFAULT_CONNECT_TIMEOUT, info.getConnectTimeout());
        final int connectTimeout = 5;
        info.setConnectTimeout(connectTimeout);
        assertEquals(connectTimeout, info.getConnectTimeout());
    }

    @Test
    public void testWireCrypt() {
        assertEquals(WireCrypt.DEFAULT, info.getWireCrypt());
        final WireCrypt wireCrypt = WireCrypt.DISABLED;
        info.setWireCrypt(wireCrypt);
        assertEquals(wireCrypt, info.getWireCrypt());
    }

    @Test
    public void testWireCryptNullPointerExceptionOnNull() {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("wireCrypt");

        info.setWireCrypt(null);
    }

    @Test
    public void testCopyConstructor() throws Exception {
        info.setDatabaseName("testValue");
        info.setServerName("xyz");
        info.setPortNumber(1203);
        info.setConnectionDialect((short) 2);
        info.setConnectTimeout(15);
        info.setWireCrypt(WireCrypt.REQUIRED);

        FbConnectionProperties copy = new FbConnectionProperties(info);
        BeanInfo beanInfo = Introspector.getBeanInfo(FbConnectionProperties.class);
        for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {
            Method method = descriptor.getReadMethod();
            if (method == null) continue;
            // Compare all properties
            assertEquals(method.invoke(info), method.invoke(copy));
        }
    }

    @Test
    public void testAsImmutable() throws Exception {
        // TODO Explicitly test properties instead of using reflection
        Map<String, Object> testValues = new HashMap<>();
        int intValue = 1;
        BeanInfo beanInfo = Introspector.getBeanInfo(FbConnectionProperties.class, Object.class);
        for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {
            if ("extraDatabaseParameters".equals(descriptor.getName())) {
                // Property extraDatabaseParameters has no setter
                continue;
            }
            Method method = descriptor.getWriteMethod();
            if (method == null) {
                continue;
            }
            Class<?> parameterType = method.getParameterTypes()[0];
            if (parameterType == int.class) {
                Object value = intValue++;
                method.invoke(info, value);
                testValues.put(descriptor.getName(), value);
            } else if (parameterType == short.class) {
                Object value = (short) (intValue++);
                method.invoke(info, value);
                testValues.put(descriptor.getName(), value);
            } else if (parameterType == String.class) {
                method.invoke(info, method.getName());
                testValues.put(descriptor.getName(), method.getName());
            } else if (parameterType == boolean.class) {
                method.invoke(info, true);
                testValues.put(descriptor.getName(), true);
            } else if (parameterType == WireCrypt.class) {
                method.invoke(info, WireCrypt.REQUIRED);
                testValues.put(descriptor.getName(), WireCrypt.REQUIRED);
            } else {
                throw new IllegalStateException("Unexpected setter type: " + parameterType);
            }
        }

        IConnectionProperties immutable = info.asImmutable();
        BeanInfo immutableBean = Introspector.getBeanInfo(FbImmutableConnectionProperties.class, Object.class);
        for (PropertyDescriptor descriptor : immutableBean.getPropertyDescriptors()) {
            if (Arrays.asList("attachObjectName").contains(descriptor.getName())) {
                continue;
            }
            if ("extraDatabaseParameters".equals(descriptor.getName())) {
                // Property extraDatabaseParameters always returns a buffer
                // TODO: Add or update test(s) to include extraDatabaseParameters
                continue;
            }
            Method method = descriptor.getReadMethod();
            Object value = method.invoke(immutable);
            String propertyName = descriptor.getName();
            assertEquals(String.format("Value for property %s doesn't match expected value", propertyName), testValues.get(propertyName), value);
        }
    }
}
