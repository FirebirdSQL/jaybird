/*
* Firebird Open Source J2ee connector - jdbc driver
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
package org.firebirdsql.jca;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.firebirdsql.jdbc.AbstractConnection;

import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.sql.DataSource;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.sql.*;

/**
 * Describe class <code>TestFBResultSet</code> here.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class TestFBResultSet extends TestXABase {

    public TestFBResultSet(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(TestFBResultSet.class);
    }

    public void testUseResultSet() throws Exception {
        FBManagedConnectionFactory mcf = initMcf();
        ManagedConnection mc = mcf.createManagedConnection(null, null);
        Connection c = (Connection) mc.getConnection(null, null);
        Statement s = c.createStatement();
        XAResource xa = mc.getXAResource();
        Exception ex = null;
        Xid xid = new XidImpl();
        xa.start(xid, XAResource.TMNOFLAGS);
        try {
            s.execute("CREATE TABLE T1 ( C1 SMALLINT, C2 SMALLINT)");
        } catch (Exception e) {
            ex = e;
        }
        xa.end(xid, XAResource.TMSUCCESS);
        xa.commit(xid, true);

        xid = new XidImpl();
        xa.start(xid, XAResource.TMNOFLAGS);
        assertFalse("execute returned true for insert statement", s.execute("insert into T1 values (1, 1)"));
        assertEquals("executeUpdate did not return 1 for single row insert", 1, s.executeUpdate("insert into T1 values (2, 2)"));
        assertTrue("execute returned false for select statement", s.execute("select C1, C2 from T1"));
        ResultSet rs = s.getResultSet();
        while (rs.next()) {
            if (log != null) log.info("C1: " + rs.getShort(1) + " C2: " + rs.getShort(2));
        }
        rs.close();
        xa.end(xid, XAResource.TMSUCCESS);
        xa.commit(xid, true);

        xid = new XidImpl();
        xa.start(xid, XAResource.TMNOFLAGS);
        s.execute("DROP TABLE T1");
        s.close();
        xa.end(xid, XAResource.TMSUCCESS);
        xa.commit(xid, true);
        mc.destroy();
        if (ex != null) {
            throw ex;
        }
    }

    public void testUseResultSetMore() throws Exception {
        FBManagedConnectionFactory mcf = initMcf();
        ManagedConnection mc = mcf.createManagedConnection(null, null);
        Connection c = (Connection) mc.getConnection(null, null);
        Statement s = c.createStatement();
        XAResource xa = mc.getXAResource();
        Exception ex = null;
        Xid xid = new XidImpl();
        xa.start(xid, XAResource.TMNOFLAGS);
        try {
            s.execute("CREATE TABLE T1 ( C1 INTEGER not null primary key, C2 SMALLINT, C3 DECIMAL(18,0), C4 FLOAT, C5 DOUBLE PRECISION, C6 CHAR(10), C7 VARCHAR(20), C8 TIME, C9 DATE, C10 TIMESTAMP)");
        } catch (Exception e) {
            ex = e;
        }
        xa.end(xid, XAResource.TMSUCCESS);
        xa.commit(xid, true);

        xid = new XidImpl();
        xa.start(xid, XAResource.TMNOFLAGS);
        assertFalse("execute returned true for insert statement",
                s.execute("insert into T1 values (1, 1, 1, 1.0, 1.0, 'one', 'one', '8:00:03.1234', '2002-JAN-11', '2001-JAN-6:8:00:03.1223')"));
        assertTrue("execute returned false for select statement", s.execute("select C1, C2, C3,  C4, C5, C6, C7, C8, C9, C10 from T1"));
        ResultSet rs = s.getResultSet();
        while (rs.next()) {
            if (log != null) log.info("C1: " + rs.getInt(1)
                            + " C2: " + rs.getShort(2)
                            + " C3: " + rs.getLong(3)
                            + " C4: " + rs.getFloat(4)
                            + " C5: " + rs.getDouble(5)
                            + " C6: " + rs.getString(6)
                            + " C7: " + rs.getString(7)
                            + " C8: " + rs.getTime(8)
                            + " C9: " + rs.getDate(9)
                            + " C10: " + rs.getTimestamp(10)
            );
            if (log != null) log.info("C1: " + rs.getInt("C1")
                            + " C2: " + rs.getShort("C2")
                            + " C3: " + rs.getLong("C3")
                            + " C4: " + rs.getFloat("C4")
                            + " C5: " + rs.getDouble("C5")
                            + " C6: " + rs.getString("C6")
                            + " C7: " + rs.getString("C7")
            );
        }
        rs.close();
        xa.end(xid, XAResource.TMSUCCESS);
        xa.commit(xid, true);

        xid = new XidImpl();
        xa.start(xid, XAResource.TMNOFLAGS);
        s.execute("DROP TABLE T1");
        s.close();
        xa.end(xid, XAResource.TMSUCCESS);
        xa.commit(xid, true);
        mc.destroy();
        if (ex != null) {
            throw ex;
        }
    }

    public void testUseResultSetWithPreparedStatement() throws Exception {
        FBManagedConnectionFactory mcf = initMcf();
        DataSource ds = (DataSource) mcf.createConnectionFactory();
        AbstractConnection c = (AbstractConnection) ds.getConnection();
        Statement s = c.createStatement();
        LocalTransaction t = c.getLocalTransaction();
        Exception ex = null;
        t.begin();
        try {
            s.execute("CREATE TABLE T1 ( C1 INTEGER not null primary key, C2 SMALLINT, C3 DECIMAL(18,0), C4 FLOAT, C5 DOUBLE PRECISION, C6 CHAR(10), C7 VARCHAR(20))");
        } catch (Exception e) {
            ex = e;
        }
        t.commit();

        t.begin();
        PreparedStatement p = c.prepareStatement("insert into T1 values (?, ?, ?, ?, ?, ?, ?)");
        p.setInt(1, 1);
        p.setShort(2, (short) 1);
        p.setLong(3, 1);
        p.setFloat(4, (float) 1.0);
        p.setDouble(5, 1.0);
        p.setString(6, "one");
        p.setString(7, "one");

        assertFalse("execute returned true for insert statement", p.execute());
        p.setInt(1, 2);
        p.setShort(2, (short) 2);
        p.setLong(3, 2);
        p.setFloat(4, (float) 2.0);
        p.setDouble(5, 2.0);
        p.setString(6, "two");
        p.setString(7, "two");
        assertTrue("executeUpdate count != 1", p.executeUpdate() == 1);

        p.close();
        p = c.prepareStatement("select * from T1 where C1 = ?");
        p.setInt(1, 1);
        ResultSet rs = p.executeQuery();
        while (rs.next()) {
            if (log != null) log.info("C1: " + rs.getInt(1)
                            + " C2: " + rs.getShort(2)
                            + " C3: " + rs.getLong(3)
                            + " C4: " + rs.getFloat(4)
                            + " C5: " + rs.getDouble(5)
                            + " C6: " + rs.getString(6)
                            + " C7: " + rs.getString(7)
            );
            if (log != null) log.info("C1: " + rs.getInt("C1")
                            + " C2: " + rs.getShort("C2")
                            + " C3: " + rs.getLong("C3")
                            + " C4: " + rs.getFloat("C4")
                            + " C5: " + rs.getDouble("C5")
                            + " C6: " + rs.getString("C6")
                            + " C7: " + rs.getString("C7")
            );
        }
        p.setInt(1, 2);
        rs = p.executeQuery();
        while (rs.next()) {
            if (log != null) log.info("C1: " + rs.getInt(1)
                            + " C2: " + rs.getShort(2)
                            + " C3: " + rs.getLong(3)
                            + " C4: " + rs.getFloat(4)
                            + " C5: " + rs.getDouble(5)
                            + " C6: " + rs.getString(6)
                            + " C7: " + rs.getString(7)
            );
            if (log != null) log.info("C1: " + rs.getInt("C1")
                            + " C2: " + rs.getShort("C2")
                            + " C3: " + rs.getLong("C3")
                            + " C4: " + rs.getFloat("C4")
                            + " C5: " + rs.getDouble("C5")
                            + " C6: " + rs.getString("C6")
                            + " C7: " + rs.getString("C7")
            );
        }
        p.close();
        t.commit();

        t.begin();
        s.execute("DROP TABLE T1");
        s.close();
        t.commit();
        c.close();
        if (ex != null) {
            throw ex;
        }
    }

    public void testUsePreparedStatementAcrossTransactions() throws Exception {
        FBManagedConnectionFactory mcf = initMcf();
        DataSource ds = (DataSource) mcf.createConnectionFactory();
        AbstractConnection c = (AbstractConnection) ds.getConnection();
        Statement s = c.createStatement();
        LocalTransaction t = c.getLocalTransaction();
        Exception ex = null;
        t.begin();
        try {
            s.execute("DROP TABLE T1");
        } catch (Exception e) {
        }
        t.commit();
        t.begin();
        try {
            s.execute("CREATE TABLE T1 ( C1 INTEGER not null primary key, C2 SMALLINT, C3 DECIMAL(18,0), C4 FLOAT, C5 DOUBLE PRECISION, C6 CHAR(10), C7 VARCHAR(20))");
        } catch (Exception e) {
            ex = e;
        }
        t.commit();

        t.begin();
        PreparedStatement p = c.prepareStatement("insert into T1 values (?, ?, ?, ?, ?, ?, ?)");
        p.setInt(1, 1);
        p.setShort(2, (short) 1);
        p.setLong(3, 1);
        p.setFloat(4, (float) 1.0);
        p.setDouble(5, 1.0);
        p.setString(6, "one");
        p.setString(7, "one");

        assertFalse("execute returned true for insert statement", p.execute());
        p.setInt(1, 2);
        p.setShort(2, (short) 2);
        p.setLong(3, 2);
        p.setFloat(4, (float) 2.0);
        p.setDouble(5, 2.0);
        p.setString(6, "two");
        p.setString(7, "two");
        assertEquals("executeUpdate count != 1", 1, p.executeUpdate());

        p.close();
        p = c.prepareStatement("select * from T1 where C1 = ?");
        p.setInt(1, 1);
        ResultSet rs = p.executeQuery();
        while (rs.next()) {
            if (log != null) log.info("C1: " + rs.getInt(1)
                            + " C2: " + rs.getShort(2)
                            + " C3: " + rs.getLong(3)
                            + " C4: " + rs.getFloat(4)
                            + " C5: " + rs.getDouble(5)
                            + " C6: " + rs.getString(6)
                            + " C7: " + rs.getString(7)
            );
            if (log != null) log.info("C1: " + rs.getInt("C1")
                            + " C2: " + rs.getShort("C2")
                            + " C3: " + rs.getLong("C3")
                            + " C4: " + rs.getFloat("C4")
                            + " C5: " + rs.getDouble("C5")
                            + " C6: " + rs.getString("C6")
                            + " C7: " + rs.getString("C7")
            );
        }
        t.commit();
        //does prepared statement persist across transactions?
        t.begin();
        p.setInt(1, 2);
        rs = p.executeQuery();
        while (rs.next()) {
            if (log != null) log.info("C1: " + rs.getInt(1)
                            + " C2: " + rs.getShort(2)
                            + " C3: " + rs.getLong(3)
                            + " C4: " + rs.getFloat(4)
                            + " C5: " + rs.getDouble(5)
                            + " C6: " + rs.getString(6)
                            + " C7: " + rs.getString(7)
            );
            if (log != null) log.info("C1: " + rs.getInt("C1")
                            + " C2: " + rs.getShort("C2")
                            + " C3: " + rs.getLong("C3")
                            + " C4: " + rs.getFloat("C4")
                            + " C5: " + rs.getDouble("C5")
                            + " C6: " + rs.getString("C6")
                            + " C7: " + rs.getString("C7")
            );
        }
        p.close();
        t.commit();

        t.begin();
        s.execute("DROP TABLE T1");
        s.close();
        t.commit();
        c.close();
        if (ex != null) {
            throw ex;
        }
    }

    public void testUseResultSetWithCount() throws Exception {
        FBManagedConnectionFactory mcf = initMcf();
        DataSource ds = (DataSource) mcf.createConnectionFactory();
        AbstractConnection c = (AbstractConnection) ds.getConnection();
        Statement s = c.createStatement();
        LocalTransaction t = c.getLocalTransaction();
        Exception ex = null;
        t.begin();
        try {
            s.execute(" CREATE TABLE Customer (name VARCHAR(256),accounts VARCHAR(2000),id VARCHAR(256))");
        } catch (Exception e) {
            ex = e;
        }
        t.commit();

        t.begin();
        PreparedStatement p = c.prepareStatement("SELECT COUNT(*) FROM Customer WHERE id=? AND name=?");
        p.setString(1, "1");
        p.setString(2, "First Customer");

        assertTrue("execute returned false for insert statement", p.execute());
        ResultSet rs = p.getResultSet();
        while (rs.next()) {
            if (log != null) log.info("count: " + rs.getInt(1));
        }
        p.close();
        t.commit();

        t.begin();
        s.execute("DROP TABLE Customer");
        s.close();
        t.commit();
        c.close();
        if (ex != null) {
            throw ex;
        }
    }

    public static final String CREATE_PROCEDURE =
            "CREATE PROCEDURE testproc(number INTEGER) RETURNS (result INTEGER) AS BEGIN     result = number; END";

    public void testExecutableProcedure() throws Exception {
        FBManagedConnectionFactory mcf = initMcf();
        DataSource ds = (DataSource) mcf.createConnectionFactory();
        AbstractConnection c = (AbstractConnection) ds.getConnection();
        Statement s = c.createStatement();
        LocalTransaction t = c.getLocalTransaction();
        t.begin();
        try {
            s.execute("DROP PROCEDURE testproc");
        } catch (Exception e) {
        }
        t.commit();
        t.begin();
        s.execute(CREATE_PROCEDURE);
        t.commit();

        t.begin();
        CallableStatement p = c.prepareCall("EXECUTE PROCEDURE testproc(?)");
        p.setInt(1, 5);

        assertTrue("execute returned false for execute procedure statement", p.execute());
        assertEquals("wrong answer from sp invocation", 5, p.getInt(1));
        p.close();
        t.commit();

        t.begin();
        s.execute("DROP PROCEDURE testproc");
        s.close();
        t.commit();
        c.close();
    }
}
