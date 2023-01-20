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
package org.firebirdsql.ds;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.JdbcResourceHelper;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.gds.impl.GDSServerVersion;
import org.firebirdsql.jaybird.xca.FBManagedConnectionFactory;
import org.firebirdsql.jdbc.FirebirdConnection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;

import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.firebirdsql.common.matchers.GdsTypeMatchers.isPureJavaType;
import static org.firebirdsql.common.matchers.MatcherAssume.assumeThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests for {@link FBSimpleDataSource}
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
class FBSimpleDataSourceTest {

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll();

    /**
     * Test for JDBC-314 : setting charSet connection property to (alias of) file.encoding system property makes prepare statement fail
     */
    @Test
    void testJavaCharSetIsDefaultCharSet() {
        FBSimpleDataSource ds = new FBSimpleDataSource();
        ds.setDatabaseName(FBTestProperties.DB_DATASOURCE_URL);
        ds.setUser(FBTestProperties.DB_USER);
        ds.setPassword(FBTestProperties.DB_PASSWORD);
        ds.setType(FBTestProperties.getGdsType().toString());
        ds.setCharSet(System.getProperty("file.encoding"));
        try (Connection con = ds.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM RDB$DATABASE");
            JdbcResourceHelper.closeQuietly(ps);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Preparing statement with property charSet equal to file.encoding should not fail");
        }
    }

    @Test
    void defaultDisableWireCompression() throws Exception {
        assumeThat("Test only works with pure java connections", FBTestProperties.GDS_TYPE, isPureJavaType());
        FBSimpleDataSource ds = new FBSimpleDataSource();
        ds.setDatabaseName(FBTestProperties.DB_DATASOURCE_URL);
        ds.setUser(FBTestProperties.DB_USER);
        ds.setPassword(FBTestProperties.DB_PASSWORD);
        ds.setType(FBTestProperties.getGdsType().toString());

        try (Connection connection = ds.getConnection()) {
            assertTrue(connection.isValid(0));
            GDSServerVersion serverVersion =
                    connection.unwrap(FirebirdConnection.class).getFbDatabase().getServerVersion();
            assertFalse(serverVersion.isWireCompressionUsed(), "expected wire compression not in use");
        }
    }

    @Test
    void enableWireCompression() throws Exception {
        assumeThat("Test only works with pure java connections", FBTestProperties.GDS_TYPE, isPureJavaType());
        assumeTrue(getDefaultSupportInfo().supportsWireCompression(), "Test requires wire compression");
        FBSimpleDataSource ds = new FBSimpleDataSource();
        ds.setDatabaseName(FBTestProperties.DB_DATASOURCE_URL);
        ds.setUser(FBTestProperties.DB_USER);
        ds.setPassword(FBTestProperties.DB_PASSWORD);
        ds.setType(FBTestProperties.getGdsType().toString());

        ds.setWireCompression(true);

        try (Connection connection = ds.getConnection()) {
            assertTrue(connection.isValid(0));
            GDSServerVersion serverVersion =
                    connection.unwrap(FirebirdConnection.class).getFbDatabase().getServerVersion();
            assertTrue(serverVersion.isWireCompressionUsed(), "expected wire compression in use");
        }
    }

    @Test
    void canChangeConfigAfterConnectionCreation() throws Exception {
        FBSimpleDataSource ds = new FBSimpleDataSource();
        ds.setDatabaseName(FBTestProperties.DB_DATASOURCE_URL);
        ds.setUser(FBTestProperties.DB_USER);
        ds.setPassword(FBTestProperties.DB_PASSWORD);
        ds.setType(FBTestProperties.getGdsType().toString());

        // possible before connecting
        ds.setBlobBufferSize(1024);

        try (Connection connection = ds.getConnection()) {
            assertTrue(connection.isValid(1000));
        }

        // still possible after creating a connection
        ds.setBlobBufferSize(2048);
    }

    @Test
    void cannotChangeConfigAfterConnectionCreation_usingSharedMCF() throws Exception {
        FBManagedConnectionFactory mcf = new FBManagedConnectionFactory();
        FBSimpleDataSource ds = new FBSimpleDataSource(mcf);
        ds.setDatabaseName(FBTestProperties.DB_DATASOURCE_URL);
        ds.setUser(FBTestProperties.DB_USER);
        ds.setPassword(FBTestProperties.DB_PASSWORD);
        ds.setType(FBTestProperties.getGdsType().toString());

        // possible before connecting
        ds.setBlobBufferSize(1024);

        try (Connection connection = ds.getConnection()) {
            assertTrue(connection.isValid(1000));
        }

        // not possible after creating a connection
        assertThrows(IllegalStateException.class, () -> ds.setBlobBufferSize(2048));
    }

    /**
     * Test for <a href="https://github.com/FirebirdSQL/jaybird/issues/494">jaybird#494</a>.
     */
    @Test
    void canConnectWithEmptyRoleName_494() throws Exception {
        FBSimpleDataSource ds = new FBSimpleDataSource();
        ds.setDatabaseName(FBTestProperties.DB_DATASOURCE_URL);
        ds.setUser(FBTestProperties.DB_USER);
        ds.setPassword(FBTestProperties.DB_PASSWORD);
        ds.setRoleName("");
        ds.setType(FBTestProperties.getGdsType().toString());

        try (Connection connection = ds.getConnection()) {
            assertTrue(connection.isValid(1000));
        }
    }
}
