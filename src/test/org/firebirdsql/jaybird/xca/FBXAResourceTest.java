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

import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

import static org.firebirdsql.common.FBTestProperties.createDefaultMcf;
import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.*;

class FBXAResourceTest {

    // TODO Ideally, we'd like to share the database for all tests, but testRecover() fails in NATIVE when we do that
    //  investigate why this happens
    @RegisterExtension
    final UsesDatabaseExtension.UsesDatabaseForEach usesDatabase = UsesDatabaseExtension.usesDatabase();

    @Test
    void testGetXAResource() throws Exception {
        FBManagedConnectionFactory mcf = createDefaultMcf();
        FBManagedConnection mc = mcf.createManagedConnection();
        try {
            XAResource xa1 = mc.getXAResource();
            XAResource xa2 = mc.getXAResource();
            assertSame(xa1, xa2, "XAResources from same mc should be identical");
        } finally {
            mc.destroy();
        }
    }

    @Test
    void testIsSameRM() throws Exception {
        FBManagedConnectionFactory mcf1 = createDefaultMcf();
        FBManagedConnection mc1 = mcf1.createManagedConnection();
        XAResource xa1 = mc1.getXAResource();
        FBManagedConnection mc2 = mcf1.createManagedConnection();
        XAResource xa2 = mc2.getXAResource();
        FBManagedConnectionFactory mcf3 = createDefaultMcf();
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
    void testStartXATrans() throws Exception {
        FBManagedConnectionFactory mcf = createDefaultMcf();
        FBManagedConnection mc = mcf.createManagedConnection();
        XAResource xa = mc.getXAResource();
        Xid xid = new XidImpl();
        xa.start(xid, XAResource.TMNOFLAGS);
        assertNotNull(mc.getGDSHelper().getCurrentDatabase(), "no db handle after start xid");
        xa.end(xid, XAResource.TMSUCCESS);
        xa.commit(xid, true);
        mc.destroy();
    }

    @Test
    void testRollbackXATrans() throws Exception {
        FBManagedConnectionFactory mcf = createDefaultMcf();
        FBManagedConnection mc = mcf.createManagedConnection();
        XAResource xa = mc.getXAResource();
        Xid xid = new XidImpl();
        xa.start(xid, XAResource.TMNOFLAGS);
        assertNotNull(mc.getGDSHelper().getCurrentDatabase(), "no db handle after start xid");
        xa.end(xid, XAResource.TMSUCCESS);
        xa.rollback(xid);
        mc.destroy();
    }

    @Test
    void test2PCXATrans() throws Exception {
        FBManagedConnectionFactory mcf = createDefaultMcf();
        FBManagedConnection mc = mcf.createManagedConnection();
        XAResource xa = mc.getXAResource();
        Xid xid = new XidImpl();
        xa.start(xid, XAResource.TMNOFLAGS);
        assertNotNull(mc.getGDSHelper().getCurrentDatabase(), "no db handle after start xid");
        xa.end(xid, XAResource.TMSUCCESS);
        xa.prepare(xid);
        xa.commit(xid, false);
        mc.destroy();
    }

    @Test
    void testRollback2PCXATrans() throws Exception {
        FBManagedConnectionFactory mcf = createDefaultMcf();
        FBManagedConnection mc = mcf.createManagedConnection();
        XAResource xa = mc.getXAResource();
        Xid xid = new XidImpl();
        xa.start(xid, XAResource.TMNOFLAGS);
        assertNotNull(mc.getGDSHelper().getCurrentDatabase(), "no db handle after start xid");
        xa.end(xid, XAResource.TMSUCCESS);
        xa.prepare(xid);
        xa.rollback(xid);
        mc.destroy();
    }

    @Test
    void testDo2XATrans() throws Exception {
        FBManagedConnectionFactory mcf = createDefaultMcf();
        FBManagedConnection mc1 = mcf.createManagedConnection();
        XAResource xa1 = mc1.getXAResource();
        Xid xid1 = new XidImpl();
        xa1.start(xid1, XAResource.TMNOFLAGS);
        assertNotNull(mc1.getGDSHelper().getCurrentDatabase(), "no db handle after start xid");
        FBManagedConnection mc2 = mcf.createManagedConnection();
        XAResource xa2 = mc2.getXAResource();
        Xid xid2 = new XidImpl();
        xa2.start(xid2, XAResource.TMNOFLAGS);
        assertNotNull(mc2.getGDSHelper().getCurrentDatabase(), "no db handle after start xid");
        //commit each tr on other xares
        xa1.end(xid1, XAResource.TMSUCCESS);
        xa2.commit(xid1, true);
        xa2.end(xid2, XAResource.TMSUCCESS);
        xa1.commit(xid2, true);
        mc1.destroy();
        mc2.destroy();
    }

    @Test
    void testRecover() throws Exception {
        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement()) {
            try {
                stmt.execute("DROP TABLE test_reconnect");
            } catch (SQLException ex) {
                // empty
            }

            stmt.execute("CREATE TABLE test_reconnect(id INTEGER)");
        }

        FBManagedConnectionFactory mcf = createDefaultMcf();

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

        FBManagedConnectionFactory mcf2 = createDefaultMcf();

        FBManagedConnection mc2 = mcf2.createManagedConnection();
        try {
            XAResource xa2 = mc2.getXAResource();

            Xid xid2 = new XidImpl();
            xa2.start(xid2, XAResource.TMNOFLAGS);

            Xid[] xids = xa2.recover(XAResource.TMSTARTRSCAN | XAResource.TMENDRSCAN);

            xa2.end(xid2, XAResource.TMSUCCESS);
            xa2.commit(xid2, true);

            assertNotNull(xids, "Should recover non-null array");
            assertTrue(xids.length > 0, "Should recover at least one transaction");

            assertThat(Arrays.asList(xids), hasItem(xid1));

            xa2.commit(xid1, false);
        } finally {
            mc2.destroy();
        }

        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM test_reconnect");
            assertTrue(rs.next(), "Should find at least one row.");
            assertEquals(1, rs.getInt(1), "Should read correct value");
            assertFalse(rs.next(), "Should select only one row");
        }
    }

    /**
     * Test that use of multiple statements in distributed transactions does not close result sets.
     * <p>
     * See <a href="http://tracker.firebirdsql.org/browse/JDBC-344">JDBC-344</a>
     * </p>
     */
    @Test
    void testXAMultipleStatements() throws Throwable {
        FBManagedConnectionFactory mcf = createDefaultMcf();
        FBManagedConnection mc = mcf.createManagedConnection();
        try {
            XAResource xa = mc.getXAResource();
            Connection con = mc.getConnection();
            Xid xid = new XidImpl();
            xa.start(xid, XAResource.TMNOFLAGS);

            try (Statement stmt1 = con.createStatement();
                 Statement stmt2 = con.createStatement()) {
                ResultSet rs1 = stmt1.executeQuery("SELECT RDB$CHARACTER_SET_NAME FROM RDB$CHARACTER_SETS");
                assertTrue(rs1.next(), "Expected rs1 row 1");
                assertNotNull(rs1.getString(1), "Expected rs1 value for row 1, column 1");
                ResultSet rs2 = stmt2.executeQuery("SELECT 1 FROM RDB$DATABASE");
                assertTrue(rs2.next(), "Expected rs2 row 1");
                assertEquals(1, rs2.getInt(1), "Expected value 1 for rs2 row 1, column 1");
                assertFalse(rs1.isClosed(), "Expected rs1 to be open as the resultset shouldn't have been closed by interleaved execution of stmt2");

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
    void testCloseConnectionDuringXA() throws Throwable {
        FBManagedConnectionFactory mcf = createDefaultMcf();
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
