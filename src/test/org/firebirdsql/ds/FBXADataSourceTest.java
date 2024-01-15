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
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.gds.impl.GDSServerVersion;
import org.firebirdsql.jaybird.xca.XidImpl;
import org.firebirdsql.jdbc.FirebirdConnection;
import org.firebirdsql.jdbc.SQLStateConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.firebirdsql.common.matchers.GdsTypeMatchers.isPureJavaType;
import static org.firebirdsql.common.matchers.MatcherAssume.assumeThat;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.sqlStateEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Test for XADataSource. Behavior of XAResource (FBManagedConnection) is tested in {@code org.firebirdsql.jaybird.xca.FBXAResourceTest}.
 * 
 * @author Mark Rotteveel
 * @since 2.2
 */
class FBXADataSourceTest {

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll();

    private final List<XAConnection> connections = new ArrayList<>();

    private final FBXADataSource ds = configureDefaultDbProperties(new FBXADataSource());

    @AfterEach
    void tearDown() {
        for (XAConnection pc : connections) {
            closeQuietly(pc);
        }
        connections.clear();
    }
    
    protected XAConnection getXAConnection() throws SQLException {
        XAConnection pc = ds.getXAConnection();
        connections.add(pc);
        return pc;
    }

    /**
     * Tests if the ConnectionPoolDataSource can create a PooledConnection
     */
    @Test
    void testDataSource_start() {
        assertDoesNotThrow(this::getXAConnection);
    }

    /**
     * Tests if the connection obtained from the PooledConnection can be used
     * and has expected defaults.
     */
    @Test
    void testConnection() throws SQLException {
        XAConnection pc = getXAConnection();

        Connection con = pc.getConnection();

        assertTrue(con.getAutoCommit(), "Autocommit should be true");
        assertFalse(con.isReadOnly(), "Read-only should be false");
        assertEquals(Connection.TRANSACTION_READ_COMMITTED, con.getTransactionIsolation(),
                "Tx isolation level should be read committed");

        try (Statement stmt = con.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT cast(1 AS INTEGER) FROM rdb$database");

            assertTrue(rs.next(), "Should select one row");
            assertEquals(1, rs.getInt(1), "Selected value should be 1");
        }
        con.close();
        assertTrue(con.isClosed(), "Connection should report as being closed");
    }

    /**
     * Tests if setting autoCommit(true) when autoCommit is false throws an exception when participating in a distributed transaction (JDBC 4.0 section 12.4).
     */
    @Test
    void testInDistributed_setAutoCommit_true_notInAutoCommit() throws Exception {
        XAConnection pc = getXAConnection();
        XAResource xa = pc.getXAResource();
        Xid xid = new XidImpl();
        try (Connection con = pc.getConnection()) {
            con.setAutoCommit(false);
            xa.start(xid, XAResource.TMNOFLAGS);

            SQLException exception = assertThrows(SQLException.class, () -> con.setAutoCommit(true));
            assertThat(exception, sqlStateEquals(SQLStateConstants.SQL_STATE_INVALID_TX_STATE));
        } finally {
            xa.end(xid, XAResource.TMSUCCESS);
            xa.rollback(xid);
        }
    }

    /**
     * Tests if setting autoCommit(true) when autoCommit is true throws an exception when participating in a distributed transaction (JDBC 4.0 section 12.4).
     */
    @Test
    void testInDistributed_setAutoCommit_true_inAutoCommit() throws Exception {
        XAConnection pc = getXAConnection();
        XAResource xa = pc.getXAResource();
        Xid xid = new XidImpl();
        try (Connection con = pc.getConnection()) {
            con.setAutoCommit(true);
            xa.start(xid, XAResource.TMNOFLAGS);

            SQLException exception = assertThrows(SQLException.class, () -> con.setAutoCommit(true));
            assertThat(exception, sqlStateEquals(SQLStateConstants.SQL_STATE_INVALID_TX_STATE));
        } finally {
            xa.end(xid, XAResource.TMSUCCESS);
            xa.rollback(xid);
        }
    }

    /**
     * Test if calling commit throws an exception when participating in a distributed transaction (JDBC 4.0 section 12.4).
     */
    @Test
    void testInDistributed_commit() throws Exception {
        XAConnection pc = getXAConnection();
        XAResource xa = pc.getXAResource();
        Xid xid = new XidImpl();
        try (Connection con = pc.getConnection()) {
            con.setAutoCommit(false);
            xa.start(xid, XAResource.TMNOFLAGS);

            SQLException exception = assertThrows(SQLException.class, con::commit);
            assertThat(exception, sqlStateEquals(SQLStateConstants.SQL_STATE_INVALID_TX_STATE));
        } finally {
            xa.end(xid, XAResource.TMSUCCESS);
            xa.rollback(xid);
        }
    }

    /**
     * Test if calling rollback throws an exception when participating in a distributed transaction (JDBC 4.0 section 12.4).
     */
    @Test
    void testInDistributed_rollback() throws Exception {
        XAConnection pc = getXAConnection();
        XAResource xa = pc.getXAResource();
        Xid xid = new XidImpl();
        try (Connection con = pc.getConnection()) {
            con.setAutoCommit(false);
            xa.start(xid, XAResource.TMNOFLAGS);

            SQLException exception = assertThrows(SQLException.class, con::rollback);
            assertThat(exception, sqlStateEquals(SQLStateConstants.SQL_STATE_INVALID_TX_STATE));
        } finally {
            xa.end(xid, XAResource.TMSUCCESS);
            xa.rollback(xid);
        }
    }

    /**
     * Test if calling rollback for savepoint throws an exception when participating in a distributed transaction (JDBC 4.0 section 12.4).
     */
    @Test
    void testInDistributed_rollback_savepoint() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsSavepoint(), "Test requires SAVEPOINT support");
        XAConnection pc = getXAConnection();
        XAResource xa = pc.getXAResource();
        try (Connection con = pc.getConnection()) {
            Xid xid = new XidImpl();
            try {
                con.setAutoCommit(false);
                Savepoint savepoint = con.setSavepoint(); // Just to create one
                con.rollback(); // Required to make sure start() works.
                xa.start(xid, XAResource.TMNOFLAGS);

                SQLException exception = assertThrows(SQLException.class, () -> con.rollback(savepoint));
                assertThat(exception, sqlStateEquals(SQLStateConstants.SQL_STATE_INVALID_TX_STATE));
            } finally {
                xa.end(xid, XAResource.TMSUCCESS);
                xa.rollback(xid);
            }
        }
    }

    /**
     * Test if calling setSavePoint (no param) throws an exception when participating in a distributed transaction (JDBC 4.0 section 12.4).
     */
    @Test
    void testInDistributed_setSavepoint() throws Exception {
        XAConnection pc = getXAConnection();
        XAResource xa = pc.getXAResource();
        Xid xid = new XidImpl();
        try (Connection con = pc.getConnection()) {
            con.setAutoCommit(false);
            xa.start(xid, XAResource.TMNOFLAGS);

            SQLException exception = assertThrows(SQLException.class, con::setSavepoint);
            assertThat(exception, sqlStateEquals(SQLStateConstants.SQL_STATE_INVALID_TX_STATE));
        } finally {
            xa.end(xid, XAResource.TMSUCCESS);
            xa.rollback(xid);
        }
    }

    /**
     * Test if calling setSavePoint (named) throws an exception when participating in a distributed transaction (JDBC 4.0 section 12.4).
     */
    @Test
    void testInDistributed_setSavepoint_named() throws Exception {
        XAConnection pc = getXAConnection();
        XAResource xa = pc.getXAResource();
        Xid xid = new XidImpl();
        try (Connection con = pc.getConnection()) {
            con.setAutoCommit(false);
            xa.start(xid, XAResource.TMNOFLAGS);

            SQLException exception = assertThrows(SQLException.class, () -> con.setSavepoint("test_sp"));
            assertThat(exception, sqlStateEquals(SQLStateConstants.SQL_STATE_INVALID_TX_STATE));
        } finally {
            xa.end(xid, XAResource.TMSUCCESS);
            xa.rollback(xid);
        }
    }

    /**
     * Test if a property stored with {@link FBXADataSource#setNonStandardProperty(String)} is retrievable.
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

        XAConnection xaConnection = ds.getXAConnection();
        try (Connection connection = xaConnection.getConnection()){
            assertTrue(connection.isValid(0));
            GDSServerVersion serverVersion =
                    connection.unwrap(FirebirdConnection.class).getFbDatabase().getServerVersion();
            assertTrue(serverVersion.isWireCompressionUsed(), "expected wire compression in use");
        } finally {
            xaConnection.close();
        }
    }

}
