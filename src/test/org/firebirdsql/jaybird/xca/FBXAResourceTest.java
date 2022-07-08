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
package org.firebirdsql.jaybird.xca;

import org.junit.Test;

import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.junit.Assert.*;

public class FBXAResourceTest extends XATestBase {

    @Test
    public void testGetXAResource() throws Exception {
        FBManagedConnectionFactory mcf = initMcf();
        FBManagedConnection mc = mcf.createManagedConnection();
        try {
            XAResource xa1 = mc.getXAResource();
            XAResource xa2 = mc.getXAResource();
            assertSame("XAResources from same mc should be identical", xa1, xa2);
        } finally {
            mc.destroy();
        }
    }

    @Test
    public void testIsSameRM() throws Exception {
        FBManagedConnectionFactory mcf1 = initMcf();
        FBManagedConnection mc1 = mcf1.createManagedConnection();
        XAResource xa1 = mc1.getXAResource();
        FBManagedConnection mc2 = mcf1.createManagedConnection();
        XAResource xa2 = mc2.getXAResource();
        FBManagedConnectionFactory mcf3 = initMcf();
        FBManagedConnection mc3 = mcf3.createManagedConnection();
        XAResource xa3 = mc3.getXAResource();
        if (xa1.isSameRM(xa2)) {
            fail("isSameRM reports no difference from same mcf");
        }
        if (xa1.isSameRM(xa3)) {
            fail("isSameRM reports no difference from different mcf");
        }
        mc1.destroy();
        mc2.destroy();
        mc3.destroy();
    }

    @Test
    public void testStartXATrans() throws Exception {
        FBManagedConnectionFactory mcf = initMcf();
        FBManagedConnection mc = mcf.createManagedConnection();
        XAResource xa = mc.getXAResource();
        Xid xid = new XidImpl();
        xa.start(xid, XAResource.TMNOFLAGS);
        assertNotNull("no db handle after start xid", mc.getGDSHelper().getCurrentDatabase());
        xa.end(xid, XAResource.TMSUCCESS);
        xa.commit(xid, true);
        mc.destroy();
    }

    @Test
    public void testRollbackXATrans() throws Exception {
        FBManagedConnectionFactory mcf = initMcf();
        FBManagedConnection mc = mcf.createManagedConnection();
        XAResource xa = mc.getXAResource();
        Xid xid = new XidImpl();
        xa.start(xid, XAResource.TMNOFLAGS);
        assertNotNull("no db handle after start xid", mc.getGDSHelper().getCurrentDatabase());
        xa.end(xid, XAResource.TMSUCCESS);
        xa.rollback(xid);
        mc.destroy();
    }

    @Test
    public void test2PCXATrans() throws Exception {
        FBManagedConnectionFactory mcf = initMcf();
        FBManagedConnection mc = mcf.createManagedConnection();
        XAResource xa = mc.getXAResource();
        Xid xid = new XidImpl();
        xa.start(xid, XAResource.TMNOFLAGS);
        assertNotNull("no db handle after start xid", mc.getGDSHelper().getCurrentDatabase());
        xa.end(xid, XAResource.TMSUCCESS);
        xa.prepare(xid);
        xa.commit(xid, false);
        mc.destroy();
    }

    @Test
    public void testRollback2PCXATrans() throws Exception {
        FBManagedConnectionFactory mcf = initMcf();
        FBManagedConnection mc = mcf.createManagedConnection();
        XAResource xa = mc.getXAResource();
        Xid xid = new XidImpl();
        xa.start(xid, XAResource.TMNOFLAGS);
        assertNotNull("no db handle after start xid", mc.getGDSHelper().getCurrentDatabase());
        xa.end(xid, XAResource.TMSUCCESS);
        xa.prepare(xid);
        xa.rollback(xid);
        mc.destroy();
    }

    @Test
    public void testDo2XATrans() throws Exception {
        FBManagedConnectionFactory mcf = initMcf();
        FBManagedConnection mc1 = mcf.createManagedConnection();
        XAResource xa1 = mc1.getXAResource();
        Xid xid1 = new XidImpl();
        xa1.start(xid1, XAResource.TMNOFLAGS);
        assertNotNull("no db handle after start xid", mc1.getGDSHelper().getCurrentDatabase());
        FBManagedConnection mc2 = mcf.createManagedConnection();
        XAResource xa2 = mc2.getXAResource();
        Xid xid2 = new XidImpl();
        xa2.start(xid2, XAResource.TMNOFLAGS);
        assertNotNull("no db handle after start xid", mc2.getGDSHelper().getCurrentDatabase());
        //commit each tr on other xares
        xa1.end(xid1, XAResource.TMSUCCESS);
        xa2.commit(xid1, true);
        xa2.end(xid2, XAResource.TMSUCCESS);
        xa1.commit(xid2, true);
        mc1.destroy();
        mc2.destroy();
    }

    @Test
    public void testRecover() throws Exception {
        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement()) {
            try {
                stmt.execute("DROP TABLE test_reconnect");
            } catch (SQLException ex) {
                // empty
            }

            stmt.execute("CREATE TABLE test_reconnect(id INTEGER)");
        }

        FBManagedConnectionFactory mcf = initMcf();

        Xid xid1 = new XidImpl();

        FBManagedConnection mc1 = mcf.createManagedConnection();
        try {
            XAResource xa1 = mc1.getXAResource();

            xa1.start(xid1, XAResource.TMNOFLAGS);

            Connection fbc1 = mc1.getConnection();
            try (Statement fbstmt1 = fbc1.createStatement()) {
                fbstmt1.execute("INSERT INTO test_reconnect(id) VALUES(1)");
            }

            xa1.end(xid1, XAResource.TMSUCCESS);
            xa1.prepare(xid1);
        } finally {
            // kill connection after prepare.
            mc1.destroy();
        }

        FBManagedConnectionFactory mcf2 = initMcf();

        FBManagedConnection mc2 = mcf2.createManagedConnection();
        try {
            XAResource xa2 = mc2.getXAResource();

            Xid xid2 = new XidImpl();
            xa2.start(xid2, XAResource.TMNOFLAGS);

            Xid[] xids = xa2.recover(XAResource.TMSTARTRSCAN | XAResource.TMENDRSCAN);

            xa2.end(xid2, XAResource.TMSUCCESS);
            xa2.commit(xid2, true);

            assertNotNull("Should recover non-null array", xids);
            assertTrue("Should recover at least one transaction", xids.length > 0);

            boolean found = false;
            for (Xid xid : xids) {
                if (xid.equals(xid1)) {
                    found = true;
                    break;
                }
            }

            assertTrue("Should find our transaction", found);

            xa2.commit(xid1, false);
        } finally {
            mc2.destroy();
        }

        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM test_reconnect");
            assertTrue("Should find at least one row.", rs.next());
            assertEquals("Should read correct value", 1, rs.getInt(1));
            assertFalse("Should select only one row", rs.next());
        }
    }

    /**
     * Test that use of multiple statements in distributed transactions does not close result sets.
     * <p>
     * See <a href="http://tracker.firebirdsql.org/browse/JDBC-344">JDBC-344</a>
     * </p>
     */
    @Test
    public void testXAMultipleStatements() throws Throwable {
        FBManagedConnectionFactory mcf = initMcf();
        FBManagedConnection mc = mcf.createManagedConnection();
        try {
            XAResource xa = mc.getXAResource();
            Connection con = mc.getConnection();
            Xid xid = new XidImpl();
            xa.start(xid, XAResource.TMNOFLAGS);

            try (Statement stmt1 = con.createStatement();
                 Statement stmt2 = con.createStatement()) {
                ResultSet rs1 = stmt1.executeQuery("SELECT RDB$CHARACTER_SET_NAME FROM RDB$CHARACTER_SETS");
                assertTrue("Expected rs1 row 1", rs1.next());
                assertNotNull("Expected rs1 value for row 1, column 1", rs1.getString(1));
                ResultSet rs2 = stmt2.executeQuery("SELECT 1 FROM RDB$DATABASE");
                assertTrue("Expected rs2 row 1", rs2.next());
                assertEquals("Expected value 1 for rs2 row 1, column 1", 1, rs2.getInt(1));
                assertFalse("Expected rs1 to be open as the resultset shouldn't have been closed by interleaved execution of stmt2", rs1.isClosed());

                rs1.close();
                rs2.close();
                xa.end(xid, XAResource.TMSUCCESS);
                xa.commit(xid, true);
            } catch (Throwable t) {
                xa.end(xid, XAResource.TMSUCCESS);
                xa.rollback(xid);
                throw t;
            }
        } finally {
            mc.destroy();
        }
    }

    /**
     * Tests whether a connection obtained from a managed connection during a distributed transaction can be closed.
     * <p>
     * See <a href="http://tracker.firebirdsql.org/browse/JDBC-362">JDBC-362</a>.
     * </p>
     */
    @Test
    public void testCloseConnectionDuringXA() throws Throwable {
        FBManagedConnectionFactory mcf = initMcf();
        FBManagedConnection mc = mcf.createManagedConnection();
        try {
            XAResource xa = mc.getXAResource();
            Connection con = mc.getConnection();
            Xid xid = new XidImpl();
            xa.start(xid, XAResource.TMNOFLAGS);

            try {
                con.close();

                xa.end(xid, XAResource.TMSUCCESS);
                xa.commit(xid, true);
            } catch (Throwable t) {
                xa.end(xid, XAResource.TMSUCCESS);
                xa.rollback(xid);
                throw t;
            }
        } finally {
            mc.destroy();
        }
    }
}
