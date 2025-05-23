// SPDX-FileCopyrightText: Copyright 2013-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
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

import static org.firebirdsql.common.FBTestProperties.configureDefaultDbProperties;
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
        FBSimpleDataSource ds = configureDefaultDbProperties(new FBSimpleDataSource());
        ds.setCharSet(System.getProperty("file.encoding"));
        try (Connection con = ds.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM RDB$DATABASE");
            JdbcResourceHelper.closeQuietly(ps);
        } catch (Exception e) {
            fail("Preparing statement with property charSet equal to file.encoding should not fail", e);
        }
    }

    @Test
    void defaultDisableWireCompression() throws Exception {
        assumeThat("Test only works with pure java connections", FBTestProperties.GDS_TYPE, isPureJavaType());
        FBSimpleDataSource ds = configureDefaultDbProperties(new FBSimpleDataSource());

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
        FBSimpleDataSource ds = configureDefaultDbProperties(new FBSimpleDataSource());

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
        FBSimpleDataSource ds = configureDefaultDbProperties(new FBSimpleDataSource());

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
        FBSimpleDataSource ds = configureDefaultDbProperties(new FBSimpleDataSource(mcf));

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
        FBSimpleDataSource ds = configureDefaultDbProperties(new FBSimpleDataSource());
        ds.setRoleName("");

        try (Connection connection = ds.getConnection()) {
            assertTrue(connection.isValid(1000));
        }
    }

}
