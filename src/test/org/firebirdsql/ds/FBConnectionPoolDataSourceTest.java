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
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
class FBConnectionPoolDataSourceTest extends FBConnectionPoolTestBase {

    /**
     * Tests if the ConnectionPoolDataSource can create a PooledConnection
     */
    @Test
    void testDataSource_start() throws SQLException {
        getPooledConnection();
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
    
    /**
     * Test if a property stored with {@link FBConnectionPoolDataSource#setNonStandardProperty(String, String)} is retrievable.
     */
    @SuppressWarnings("deprecation")
    @Test
    void testSetNonStandardProperty_twoParam() {
        ds.setNonStandardProperty("someProperty", "someValue");
        
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
