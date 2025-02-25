// SPDX-FileCopyrightText: Copyright 2011-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.ds;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.gds.impl.GDSServerVersion;
import org.firebirdsql.jdbc.FirebirdConnection;
import org.junit.jupiter.api.Test;

import javax.sql.PooledConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.firebirdsql.common.matchers.GdsTypeMatchers.isPureJavaType;
import static org.firebirdsql.common.matchers.MatcherAssume.assumeThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests for {@link FBConnectionPoolDataSource}
 * 
 * @author Mark Rotteveel
 */
class FBConnectionPoolDataSourceTest extends FBConnectionPoolTestBase {

    /**
     * Tests if the ConnectionPoolDataSource can create a PooledConnection
     */
    @Test
    void testDataSource_start() {
        assertDoesNotThrow(this::getPooledConnection);
    }

    /**
     * Tests if the connection obtained from the PooledConnection can be used
     * and has expected defaults.
     */
    @Test
    void testConnection() throws SQLException {
        PooledConnection pc = getPooledConnection();

        Connection con = pc.getConnection();
        //noinspection TryFinallyCanBeTryWithResources
        try {
            assertTrue(con.getAutoCommit(), "Autocommit should be true");
            assertFalse(con.isReadOnly(), "Read-only should be false");
            assertEquals(Connection.TRANSACTION_READ_COMMITTED, con.getTransactionIsolation(),
                    "Tx isolation level should be read committed");

            try (Statement stmt = con.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT cast(1 AS INTEGER) FROM rdb$database");

                assertTrue(rs.next(), "Should select one row");
                assertEquals(1, rs.getInt(1), "Selected value should be 1");
            }
        } finally {
            con.close();
        }
        assertTrue(con.isClosed(), "Connection should report as being closed");
    }
    
    /**
     * Test if a property stored with {@link FBConnectionPoolDataSource#setNonStandardProperty(String)} is retrievable.
     */
    @Test
    void testSetNonStandardProperty_singleParam() {
        ds.setNonStandardProperty("someProperty=someValue");
        
        assertEquals("someValue", ds.getProperty("someProperty"));
    }

    @Test
    void enableWireCompression() throws Exception {
        assumeThat("Test only works with pure java connections", FBTestProperties.GDS_TYPE, isPureJavaType());
        assumeTrue(getDefaultSupportInfo().supportsWireCompression(), "Test requires wire compression");
        ds.setWireCompression(true);

        PooledConnection pooledConnection = ds.getPooledConnection();
        try (Connection connection = pooledConnection.getConnection()){
            assertTrue(connection.isValid(0));
            GDSServerVersion serverVersion =
                    connection.unwrap(FirebirdConnection.class).getFbDatabase().getServerVersion();
            assertTrue(serverVersion.isWireCompressionUsed(), "expected wire compression in use");
        } finally {
            pooledConnection.close();
        }
    }
}
