/*
 * $Id$
 * 
 * Firebird Open Source J2EE Connector - JDBC Driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.ds;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.firebirdsql.common.FBTestBase;
import org.firebirdsql.common.SimpleFBTestBase;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.jca.TestXABase.XidImpl;
import org.firebirdsql.jdbc.FBSQLException;

/**
 * Test for XADataSource. Note behavior of XAResource (ManagedConnection) is tested in {@link org.firebirdsql.jca.TestFBXAResource}.
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.2
 */
public class TestFBXADataSource extends FBTestBase {
    
    private List connections = new ArrayList();

    public TestFBXADataSource(String name) {
        super(name);
    }

    protected FBXADataSource ds;

    public void setUp() throws Exception {
        super.setUp();

        FBXADataSource newDs = new FBXADataSource();
        newDs.setType(SimpleFBTestBase.getProperty("test.gds_type", null));
        if (getGdsType() == GDSType.getType("PURE_JAVA")
                || getGdsType() == GDSType.getType("NATIVE")) {
            newDs.setServerName(DB_SERVER_URL);
            newDs.setPortNumber(DB_SERVER_PORT);
        }
        newDs.setDatabaseName(getDatabasePath());
        newDs.setUser(DB_USER);
        newDs.setPassword(DB_PASSWORD);

        ds = newDs;
    }

    public void tearDown() throws Exception {
        Iterator iter = connections.iterator();
        while (iter.hasNext()) {
            XAConnection pc = (XAConnection) iter.next();
            closeQuietly(pc);
        }
        super.tearDown();
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
    public void testDataSource_start() throws SQLException {
        getXAConnection();
    }

    /**
     * Tests if the connection obtained from the PooledConnection can be used
     * and has expected defaults.
     * 
     * @throws SQLException
     */
    public void testConnection() throws SQLException {
        XAConnection pc = getXAConnection();

        Connection con = pc.getConnection();

        assertTrue("Autocommit should be true", con.getAutoCommit());
        assertTrue("Read-only should be false", !con.isReadOnly());
        assertEquals("Tx isolation level should be read committed.",
                Connection.TRANSACTION_READ_COMMITTED, con.getTransactionIsolation());

        Statement stmt = con.createStatement();

        try {
            ResultSet rs = stmt.executeQuery("SELECT cast(1 AS INTEGER) FROM rdb$database");

            assertTrue("Should select one row", rs.next());
            assertEquals("Selected value should be 1.", 1, rs.getInt(1));
        } finally {
            stmt.close();
        }
        con.close();
        assertTrue("Connection should report as being closed.", con.isClosed());
    }
    
    /**
     * Tests if setting autoCommit(true) when autoCommit is false throws an exception when participating in a distributed transaction (JDBC 4.0 section 12.4).
     */
    public void testInDistributed_setAutoCommit_true_notInAutoCommit() throws Exception {
        XAConnection pc = getXAConnection();
        XAResource xa = pc.getXAResource();
        Connection con = pc.getConnection();
        con.setAutoCommit(false);
        Xid xid = new XidImpl();
        try {
            xa.start(xid, XAResource.TMNOFLAGS);
            con.setAutoCommit(true);
            fail("Expected setAutoCommit true while in distributed transaction to throw an exception");
        } catch (SQLException ex) {
            // Expected
            assertEquals(FBSQLException.SQL_STATE_INVALID_TX_STATE, ex.getSQLState());
        } finally {
            xa.end(xid, XAResource.TMSUCCESS);
            xa.rollback(xid);
            con.close();
        }
    }
    
    /**
     * Tests if setting autoCommit(true) when autoCommit is true to not throw an exception when participating in a distributed transaction (JDBC 4.0 section 12.4).
     */
    public void testInDistributed_setAutoCommit_true_inAutoCommit() throws Exception {
        XAConnection pc = getXAConnection();
        XAResource xa = pc.getXAResource();
        Connection con = pc.getConnection();
        con.setAutoCommit(true);
        Xid xid = new XidImpl();
        try {
            xa.start(xid, XAResource.TMNOFLAGS);
            con.setAutoCommit(true);
            
        } catch (SQLException ex) {
            fail("Expected setAutoCommit true (while already true) while in distributed transaction to not throw an exception");
        } finally {
            xa.end(xid, XAResource.TMSUCCESS);
            xa.rollback(xid);
            con.close();
        }
    }
    
    /**
     * Test if calling commit throws an exception when participating in a distributed transaction (JDBC 4.0 section 12.4).
     */
    public void testInDistributed_commit() throws Exception {
        XAConnection pc = getXAConnection();
        XAResource xa = pc.getXAResource();
        Connection con = pc.getConnection();
        con.setAutoCommit(false);
        Xid xid = new XidImpl();
        try {
            xa.start(xid, XAResource.TMNOFLAGS);
            con.commit();
            fail("Expected setAutoCommit true while in distributed transaction to throw an exception");
        } catch (SQLException ex) {
            // Expected
            assertEquals(FBSQLException.SQL_STATE_INVALID_TX_STATE, ex.getSQLState());
        } finally {
            xa.end(xid, XAResource.TMSUCCESS);
            xa.rollback(xid);
            con.close();
        }
    }
    
    /**
     * Test if calling rollback throws an exception when participating in a distributed transaction (JDBC 4.0 section 12.4).
     */
    public void testInDistributed_rollback() throws Exception {
        XAConnection pc = getXAConnection();
        XAResource xa = pc.getXAResource();
        Connection con = pc.getConnection();
        con.setAutoCommit(false);
        Xid xid = new XidImpl();
        try {
            xa.start(xid, XAResource.TMNOFLAGS);
            con.rollback();
            fail("Expected setAutoCommit true while in distributed transaction to throw an exception");
        } catch (SQLException ex) {
            // Expected
            assertEquals(FBSQLException.SQL_STATE_INVALID_TX_STATE, ex.getSQLState());
        } finally {
            xa.end(xid, XAResource.TMSUCCESS);
            xa.rollback(xid);
            con.close();
        }
    }
    
    /**
     * Test if calling rollback for savepoint throws an exception when participating in a distributed transaction (JDBC 4.0 section 12.4).
     */
    public void testInDistributed_rollback_savepoint() throws Exception {
        XAConnection pc = getXAConnection();
        XAResource xa = pc.getXAResource();
        Connection con = pc.getConnection();
        con.setAutoCommit(false);
        Savepoint savepoint = con.setSavepoint(); // Just to create one
        con.rollback(); // Required to make sure start() works.
        Xid xid = new XidImpl();
        xa.start(xid, XAResource.TMNOFLAGS);
        try {
            con.rollback(savepoint);
            fail("Expected setAutoCommit true while in distributed transaction to throw an exception");
        } catch (SQLException ex) {
            // Expected
            assertEquals(FBSQLException.SQL_STATE_INVALID_TX_STATE, ex.getSQLState());
        } finally {
            xa.end(xid, XAResource.TMSUCCESS);
            xa.rollback(xid);
            con.close();
        }
    }
    
    /**
     * Test if calling setSavePoint (no param) throws an exception when participating in a distributed transaction (JDBC 4.0 section 12.4).
     */
    public void testInDistributed_setSavepoint() throws Exception {
        XAConnection pc = getXAConnection();
        XAResource xa = pc.getXAResource();
        Connection con = pc.getConnection();
        con.setAutoCommit(false);
        Xid xid = new XidImpl();
        try {
            xa.start(xid, XAResource.TMNOFLAGS);
            con.setSavepoint();
            fail("Expected setAutoCommit true while in distributed transaction to throw an exception");
        } catch (SQLException ex) {
            // Expected
            assertEquals(FBSQLException.SQL_STATE_INVALID_TX_STATE, ex.getSQLState());
        } finally {
            xa.end(xid, XAResource.TMSUCCESS);
            xa.rollback(xid);
            con.close();
        }
    }
    
    /**
     * Test if calling setSavePoint (named) throws an exception when participating in a distributed transaction (JDBC 4.0 section 12.4).
     */
    public void testInDistributed_setSavepoint_named() throws Exception {
        XAConnection pc = getXAConnection();
        XAResource xa = pc.getXAResource();
        Connection con = pc.getConnection();
        con.setAutoCommit(false);
        Xid xid = new XidImpl();
        try {
            xa.start(xid, XAResource.TMNOFLAGS);
            con.setSavepoint("test_sp");
            fail("Expected setAutoCommit true while in distributed transaction to throw an exception");
        } catch (SQLException ex) {
            // Expected
            assertEquals(FBSQLException.SQL_STATE_INVALID_TX_STATE, ex.getSQLState());
        } finally {
            xa.end(xid, XAResource.TMSUCCESS);
            xa.rollback(xid);
            con.close();
        }
    }
    
    /**
     * Test if a property stored with {@link FBXADataSource#setNonStandardProperty(String)} is retrievable.
     */
    public void testSetNonStandardProperty_singleParam() {
        ds.setNonStandardProperty("someProperty=someValue");
        
        assertEquals("someValue", ds.getNonStandardProperty("someProperty"));
    }
    
    /**
     * Test if a property stored with {@link FBXADataSource#setNonStandardProperty(String, String)} is retrievable.
     */
    public void testSetNonStandardProperty_twoParam() {
        ds.setNonStandardProperty("someProperty", "someValue");
        
        assertEquals("someValue", ds.getNonStandardProperty("someProperty"));
    }

}
