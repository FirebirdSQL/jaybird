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
package org.firebirdsql.ds;

import org.firebirdsql.common.FBJUnit4TestBase;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.jca.TestXABase.XidImpl;
import org.firebirdsql.jdbc.SQLStateConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.sqlStateEquals;
import static org.firebirdsql.util.FirebirdSupportInfo.supportInfoFor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

/**
 * Test for XADataSource. Note behavior of XAResource (ManagedConnection) is tested in {@link org.firebirdsql.jca.TestFBXAResource}.
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.2
 */
public class TestFBXADataSource extends FBJUnit4TestBase {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();
    
    private List<XAConnection> connections = new ArrayList<>();

    protected FBXADataSource ds;

    @Before
    public void setUp() throws Exception {
        FBXADataSource newDs = new FBXADataSource();
        newDs.setType(getProperty("test.gds_type", null));
        if (getGdsType() == GDSType.getType("PURE_JAVA")
                || getGdsType() == GDSType.getType("NATIVE")) {
            newDs.setServerName(DB_SERVER_URL);
            newDs.setPortNumber(DB_SERVER_PORT);
        }
        newDs.setDatabaseName(getDatabasePath());
        newDs.setUser(DB_USER);
        newDs.setPassword(DB_PASSWORD);
        newDs.setEncoding(DB_LC_CTYPE);

        ds = newDs;
    }

    @After
    public void tearDown() throws Exception {
        for (XAConnection pc : connections) {
            closeQuietly(pc);
        }
    }
    
    protected XAConnection getXAConnection() throws SQLException {
        XAConnection pc = ds.getXAConnection();
        connections.add(pc);
        return pc;
    }

    /**
     * Tests if the ConnectionPoolDataSource can create a PooledConnection
     * 
     * @throws SQLException
     */
    @Test
    public void testDataSource_start() throws SQLException {
        getXAConnection();
    }

    /**
     * Tests if the connection obtained from the PooledConnection can be used
     * and has expected defaults.
     * 
     * @throws SQLException
     */
    @Test
    public void testConnection() throws SQLException {
        XAConnection pc = getXAConnection();

        Connection con = pc.getConnection();

        assertTrue("Autocommit should be true", con.getAutoCommit());
        assertTrue("Read-only should be false", !con.isReadOnly());
        assertEquals("Tx isolation level should be read committed.",
                Connection.TRANSACTION_READ_COMMITTED, con.getTransactionIsolation());

        try (Statement stmt = con.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT cast(1 AS INTEGER) FROM rdb$database");

            assertTrue("Should select one row", rs.next());
            assertEquals("Selected value should be 1.", 1, rs.getInt(1));
        }
        con.close();
        assertTrue("Connection should report as being closed.", con.isClosed());
    }
    
    /**
     * Tests if setting autoCommit(true) when autoCommit is false throws an exception when participating in a distributed transaction (JDBC 4.0 section 12.4).
     */
    @Test
    public void testInDistributed_setAutoCommit_true_notInAutoCommit() throws Exception {
        XAConnection pc = getXAConnection();
        XAResource xa = pc.getXAResource();
        Xid xid = new XidImpl();
        try (Connection con = pc.getConnection()) {
            con.setAutoCommit(false);
            xa.start(xid, XAResource.TMNOFLAGS);

            expectedException.expect(SQLException.class);
            expectedException.expect(sqlStateEquals(SQLStateConstants.SQL_STATE_INVALID_TX_STATE));

            con.setAutoCommit(true);
        } finally {
            xa.end(xid, XAResource.TMSUCCESS);
            xa.rollback(xid);
        }
    }

    /**
     * Tests if setting autoCommit(true) when autoCommit is true throws an exception when participating in a distributed transaction (JDBC 4.0 section 12.4).
     */
    @Test
    public void testInDistributed_setAutoCommit_true_inAutoCommit() throws Exception {
        XAConnection pc = getXAConnection();
        XAResource xa = pc.getXAResource();
        Xid xid = new XidImpl();
        try (Connection con = pc.getConnection()) {
            con.setAutoCommit(true);
            xa.start(xid, XAResource.TMNOFLAGS);

            expectedException.expect(SQLException.class);
            expectedException.expect(sqlStateEquals(SQLStateConstants.SQL_STATE_INVALID_TX_STATE));

            con.setAutoCommit(true);
        } finally {
            xa.end(xid, XAResource.TMSUCCESS);
            xa.rollback(xid);
        }
    }
    
    /**
     * Test if calling commit throws an exception when participating in a distributed transaction (JDBC 4.0 section 12.4).
     */
    @Test
    public void testInDistributed_commit() throws Exception {
        XAConnection pc = getXAConnection();
        XAResource xa = pc.getXAResource();
        Xid xid = new XidImpl();
        try (Connection con = pc.getConnection()) {
            con.setAutoCommit(false);
            xa.start(xid, XAResource.TMNOFLAGS);

            expectedException.expect(SQLException.class);
            expectedException.expect(sqlStateEquals(SQLStateConstants.SQL_STATE_INVALID_TX_STATE));

            con.commit();
        } finally {
            xa.end(xid, XAResource.TMSUCCESS);
            xa.rollback(xid);
        }
    }
    
    /**
     * Test if calling rollback throws an exception when participating in a distributed transaction (JDBC 4.0 section 12.4).
     */
    @Test
    public void testInDistributed_rollback() throws Exception {
        XAConnection pc = getXAConnection();
        XAResource xa = pc.getXAResource();
        Xid xid = new XidImpl();
        try (Connection con = pc.getConnection()) {
            con.setAutoCommit(false);
            xa.start(xid, XAResource.TMNOFLAGS);

            expectedException.expect(SQLException.class);
            expectedException.expect(sqlStateEquals(SQLStateConstants.SQL_STATE_INVALID_TX_STATE));

            con.rollback();
        } finally {
            xa.end(xid, XAResource.TMSUCCESS);
            xa.rollback(xid);
        }
    }
    
    /**
     * Test if calling rollback for savepoint throws an exception when participating in a distributed transaction (JDBC 4.0 section 12.4).
     */
    @Test
    public void testInDistributed_rollback_savepoint() throws Exception {
        XAConnection pc = getXAConnection();
        XAResource xa = pc.getXAResource();
        try (Connection con = pc.getConnection()) {
            assumeTrue("Test requires SAVEPOINT support", supportInfoFor(con).supportsSavepoint());
            Xid xid = new XidImpl();
            try {
                con.setAutoCommit(false);
                Savepoint savepoint = con.setSavepoint(); // Just to create one
                con.rollback(); // Required to make sure start() works.
                xa.start(xid, XAResource.TMNOFLAGS);

                expectedException.expect(SQLException.class);
                expectedException.expect(sqlStateEquals(SQLStateConstants.SQL_STATE_INVALID_TX_STATE));

                con.rollback(savepoint);
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
    public void testInDistributed_setSavepoint() throws Exception {
        XAConnection pc = getXAConnection();
        XAResource xa = pc.getXAResource();
        Xid xid = new XidImpl();
        try (Connection con = pc.getConnection()) {
            con.setAutoCommit(false);
            xa.start(xid, XAResource.TMNOFLAGS);

            expectedException.expect(SQLException.class);
            expectedException.expect(sqlStateEquals(SQLStateConstants.SQL_STATE_INVALID_TX_STATE));

            con.setSavepoint();
        } finally {
            xa.end(xid, XAResource.TMSUCCESS);
            xa.rollback(xid);
        }
    }
    
    /**
     * Test if calling setSavePoint (named) throws an exception when participating in a distributed transaction (JDBC 4.0 section 12.4).
     */
    @Test
    public void testInDistributed_setSavepoint_named() throws Exception {
        XAConnection pc = getXAConnection();
        XAResource xa = pc.getXAResource();
        Xid xid = new XidImpl();
        try (Connection con = pc.getConnection()) {
            con.setAutoCommit(false);
            xa.start(xid, XAResource.TMNOFLAGS);

            expectedException.expect(SQLException.class);
            expectedException.expect(sqlStateEquals(SQLStateConstants.SQL_STATE_INVALID_TX_STATE));

            con.setSavepoint("test_sp");
        } finally {
            xa.end(xid, XAResource.TMSUCCESS);
            xa.rollback(xid);
        }
    }
    
    /**
     * Test if a property stored with {@link FBXADataSource#setNonStandardProperty(String)} is retrievable.
     */
    @Test
    public void testSetNonStandardProperty_singleParam() {
        ds.setNonStandardProperty("someProperty=someValue");
        
        assertEquals("someValue", ds.getNonStandardProperty("someProperty"));
    }
    
    /**
     * Test if a property stored with {@link FBXADataSource#setNonStandardProperty(String, String)} is retrievable.
     */
    @Test
    public void testSetNonStandardProperty_twoParam() {
        ds.setNonStandardProperty("someProperty", "someValue");
        
        assertEquals("someValue", ds.getNonStandardProperty("someProperty"));
    }

}
