/*   This class is LGPL only, due to the inclusion of a 
 *Xid implementation from the JBoss project as a static inner class for testing purposes.
 *The portions before the XidImpl are usable under MPL 1.1 or LGPL
 *If we write our own xid test implementation, we can reset the license to match
 *the rest of the project.
 *Original author of non-jboss code david jencks
 *copyright 2001 all rights reserved.
 */
package org.firebirdsql.jca;

import javax.resource.spi.*;
import javax.transaction.xa.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.sql.DataSource;

//import org.firebirdsql.jca.*;
import org.firebirdsql.gds.Clumplet;
import org.firebirdsql.gds.GDS;
import org.firebirdsql.jgds.GDS_Impl;
import org.firebirdsql.management.FBManager;
import org.firebirdsql.jdbc.FBConnection;

import java.io.*;
import java.util.Properties;
import java.util.HashSet;
import java.sql.*;

//for embedded xid implementation
    import java.net.InetAddress;
    import java.net.UnknownHostException;


import junit.framework.*;

/**
 *
 *   @see <related>
 *   @author David Jencks (davidjencks@earthlink.net)
 *   @version $ $
 */



/**
 *This is a class that hands out connections.  Initial implementation uses DriverManager.getConnection,
 *future enhancements will use datasources/ managed stuff.
 */
public class TestFBResultSet extends TestXABase {
    
    
    public TestFBResultSet(String name) {
        super(name);
    }
    
    public static Test suite() {

        return new TestSuite(TestFBResultSet.class);
    }
    


    public void testUseResultSet() throws Exception {
        System.out.println();
        System.out.println("testUseResultSet");
        FBManagedConnectionFactory mcf = initMcf();
        ManagedConnection mc = mcf.createManagedConnection(null, null);
        Connection c = (Connection)mc.getConnection(null, null);
        Statement s = c.createStatement();
        XAResource xa = mc.getXAResource();
        Exception ex = null;
        Xid xid = new XidImpl();
        xa.start(xid, XAResource.TMNOFLAGS);
        try {
            s.execute("CREATE TABLE T1 ( C1 SMALLINT, C2 SMALLINT)"); 
            //s.close();
        }
        catch (Exception e) {
            ex = e;
        }
        xa.end(xid, XAResource.TMNOFLAGS);
        xa.commit(xid, true);

        xid = new XidImpl();
        xa.start(xid, XAResource.TMNOFLAGS);
        assertTrue("execute returned true for insert statement", !s.execute("insert into T1 values (1, 1)")); 
        assertTrue("executeUpdate did not return 1 for single row insert", s.executeUpdate("insert into T1 values (2, 2)") == 1); 
        assertTrue("execute returned false for select statement", s.execute("select C1, C2 from T1"));
        ResultSet rs = s.getResultSet();
        while (rs.next()) {
            System.out.println("C1: " + rs.getShort(1) + " C2: " + rs.getShort(2));
        }
        rs.close();
        //s.close();
        xa.end(xid, XAResource.TMNOFLAGS);
        xa.commit(xid, true);
        
        xid = new XidImpl();
        xa.start(xid, XAResource.TMNOFLAGS);
        s.execute("DROP TABLE T1"); 
        s.close();
        xa.end(xid, XAResource.TMNOFLAGS);
        xa.commit(xid, true);
        mc.destroy();
        if (ex != null) {
            throw ex;
        }
        
    }

    public void testZZUseResultSetMore() throws Exception {
        System.out.println();
        System.out.println("testUseResultSetMore");
        FBManagedConnectionFactory mcf = initMcf();
        ManagedConnection mc = mcf.createManagedConnection(null, null);
        Connection c = (Connection)mc.getConnection(null, null);
        Statement s = c.createStatement();
        XAResource xa = mc.getXAResource();
        Exception ex = null;
        Xid xid = new XidImpl();
        xa.start(xid, XAResource.TMNOFLAGS);
        try {
            s.execute("CREATE TABLE T1 ( C1 INTEGER not null primary key, C2 SMALLINT, C3 DECIMAL(18,0), C4 FLOAT, C5 DOUBLE PRECISION, C6 CHAR(10), C7 VARCHAR(20), C8 TIME, C9 DATE, C10 TIMESTAMP)"); 
            //s.close();
        }
        catch (Exception e) {
            ex = e;
        }
        xa.end(xid, XAResource.TMNOFLAGS);
        xa.commit(xid, true);

        xid = new XidImpl();
        xa.start(xid, XAResource.TMNOFLAGS);
        assertTrue("execute returned true for insert statement", 
            !s.execute("insert into T1 values (1, 1, 1, 1.0, 1.0, 'one', 'one', '8:00:03.1234', '2002-JAN-11', '2001-JAN-6:8:00:03.1223')")); 
        //s.close();
        // s.execute("insert into T1 values (2, 2,2,  2.0, 2.0, 'two', 'two')"); 
        //s.close();
        assertTrue("execute returned false for select statement", s.execute("select C1, C2, C3,  C4, C5, C6, C7, C8, C9, C10 from T1"));
        ResultSet rs = s.getResultSet();
        while (rs.next()) {
            System.out.println("C1: " + rs.getInt(1) 
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
            System.out.println("C1: " + rs.getInt("C1") 
                + " C2: " + rs.getShort("C2")
                + " C3: " + rs.getLong("C3")
                + " C4: " + rs.getFloat("C4")
                + " C5: " + rs.getDouble("C5")
                + " C6: " + rs.getString("C6")
                + " C7: " + rs.getString("C7")
                );
        }
        rs.close();
        //s.close();
        xa.end(xid, XAResource.TMNOFLAGS);
        xa.commit(xid, true);
        
        xid = new XidImpl();
        xa.start(xid, XAResource.TMNOFLAGS);
        s.execute("DROP TABLE T1"); 
        s.close();
        xa.end(xid, XAResource.TMNOFLAGS);
        xa.commit(xid, true);
        mc.destroy();
        if (ex != null) {
            throw ex;
        }
        
    }

    public void testUseResultSetWithPreparedStatement() throws Exception {
        System.out.println();
        System.out.println("testUseResultSetWithPreparedStatement");
        FBManagedConnectionFactory mcf = initMcf();
        DataSource ds = (DataSource)mcf.createConnectionFactory();
        FBConnection c = (FBConnection)ds.getConnection();
        Statement s = c.createStatement();
        LocalTransaction t = c.getLocalTransaction();
        Exception ex = null;
        t.begin();
        try {
            s.execute("CREATE TABLE T1 ( C1 INTEGER not null primary key, C2 SMALLINT, C3 DECIMAL(18,0), C4 FLOAT, C5 DOUBLE PRECISION, C6 CHAR(10), C7 VARCHAR(20))"); 
            //s.close();
        }
        catch (Exception e) {
            ex = e;
        }
        t.commit();
        
        t.begin();
        PreparedStatement p = c.prepareStatement("insert into T1 values (?, ?, ?, ?, ?, ?, ?)");
        p.setInt(1, 1);
        p.setShort(2, (short)1);
        p.setLong(3, 1);
        p.setFloat(4, (float)1.0);
        p.setDouble(5, 1.0);
        p.setString(6, "one");
        p.setString(7, "one");
        
        assertTrue("execute returned true for insert statement", !p.execute()); 
        p.setInt(1, 2);
        p.setShort(2, (short)2);
        p.setLong(3, 2);
        p.setFloat(4, (float)2.0);
        p.setDouble(5, 2.0);
        p.setString(6, "two");
        p.setString(7, "two");
        assertTrue("executeUpdate count != 1", p.executeUpdate() == 1);
        
        p.close();
        p = c.prepareStatement("select * from T1 where C1 = ?");
        p.setInt(1, 1);
        ResultSet rs = p.executeQuery();
        while (rs.next()) {
            System.out.println("C1: " + rs.getInt(1) 
                + " C2: " + rs.getShort(2)
                + " C3: " + rs.getLong(3)
                + " C4: " + rs.getFloat(4)
                + " C5: " + rs.getDouble(5)
                + " C6: " + rs.getString(6)
                + " C7: " + rs.getString(7)
                );
            System.out.println("C1: " + rs.getInt("C1") 
                + " C2: " + rs.getShort("C2")
                + " C3: " + rs.getLong("C3")
                + " C4: " + rs.getFloat("C4")
                + " C5: " + rs.getDouble("C5")
                + " C6: " + rs.getString("C6")
                + " C7: " + rs.getString("C7")
                );
        }
//        rs.close(); //should be automatic
        p.setInt(1, 2);
        rs = p.executeQuery();
        while (rs.next()) {
            System.out.println("C1: " + rs.getInt(1) 
                + " C2: " + rs.getShort(2)
                + " C3: " + rs.getLong(3)
                + " C4: " + rs.getFloat(4)
                + " C5: " + rs.getDouble(5)
                + " C6: " + rs.getString(6)
                + " C7: " + rs.getString(7)
                );
            System.out.println("C1: " + rs.getInt("C1") 
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
        System.out.println();
        System.out.println("testUsePreparedStatementAcrossTransactions");
        FBManagedConnectionFactory mcf = initMcf();
        DataSource ds = (DataSource)mcf.createConnectionFactory();
        FBConnection c = (FBConnection)ds.getConnection();
        Statement s = c.createStatement();
        LocalTransaction t = c.getLocalTransaction();
        Exception ex = null;
        t.begin();
        try {
            s.execute("DROP TABLE T1");
        }
        catch (Exception e) {
        }
        t.commit();
        t.begin();
        try {
            s.execute("CREATE TABLE T1 ( C1 INTEGER not null primary key, C2 SMALLINT, C3 DECIMAL(18,0), C4 FLOAT, C5 DOUBLE PRECISION, C6 CHAR(10), C7 VARCHAR(20))"); 
            //s.close();
        }
        catch (Exception e) {
            ex = e;
        }
        t.commit();
        
        t.begin();
        PreparedStatement p = c.prepareStatement("insert into T1 values (?, ?, ?, ?, ?, ?, ?)");
        p.setInt(1, 1);
        p.setShort(2, (short)1);
        p.setLong(3, 1);
        p.setFloat(4, (float)1.0);
        p.setDouble(5, 1.0);
        p.setString(6, "one");
        p.setString(7, "one");
        
        assertTrue("execute returned true for insert statement", !p.execute()); 
        p.setInt(1, 2);
        p.setShort(2, (short)2);
        p.setLong(3, 2);
        p.setFloat(4, (float)2.0);
        p.setDouble(5, 2.0);
        p.setString(6, "two");
        p.setString(7, "two");
        assertTrue("executeUpdate count != 1", p.executeUpdate() == 1);
        
        p.close();
        p = c.prepareStatement("select * from T1 where C1 = ?");
        p.setInt(1, 1);
        ResultSet rs = p.executeQuery();
        while (rs.next()) {
            System.out.println("C1: " + rs.getInt(1) 
                + " C2: " + rs.getShort(2)
                + " C3: " + rs.getLong(3)
                + " C4: " + rs.getFloat(4)
                + " C5: " + rs.getDouble(5)
                + " C6: " + rs.getString(6)
                + " C7: " + rs.getString(7)
                );
            System.out.println("C1: " + rs.getInt("C1") 
                + " C2: " + rs.getShort("C2")
                + " C3: " + rs.getLong("C3")
                + " C4: " + rs.getFloat("C4")
                + " C5: " + rs.getDouble("C5")
                + " C6: " + rs.getString("C6")
                + " C7: " + rs.getString("C7")
                );
        }
//        rs.close(); //should be automatic
        t.commit();
        //does prepared statemen persist across transactions?
        t.begin();
        p.setInt(1, 2);
        rs = p.executeQuery();
        while (rs.next()) {
            System.out.println("C1: " + rs.getInt(1) 
                + " C2: " + rs.getShort(2)
                + " C3: " + rs.getLong(3)
                + " C4: " + rs.getFloat(4)
                + " C5: " + rs.getDouble(5)
                + " C6: " + rs.getString(6)
                + " C7: " + rs.getString(7)
                );
            System.out.println("C1: " + rs.getInt("C1") 
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
        System.out.println();
        System.out.println("testUseResultSetWithCount");
        FBManagedConnectionFactory mcf = initMcf();
        DataSource ds = (DataSource)mcf.createConnectionFactory();
        FBConnection c = (FBConnection)ds.getConnection();
        Statement s = c.createStatement();
        LocalTransaction t = c.getLocalTransaction();
        Exception ex = null;
        t.begin();
        try {
            s.execute(" CREATE TABLE Customer (name VARCHAR(256),accounts VARCHAR(2000),id VARCHAR(256))"); 
            //s.close();
        }
        catch (Exception e) {
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
            System.out.println("count: " + rs.getInt(1) );
        }
//        rs.close(); //should be automatic
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

    public void testzzzExecutableProcedure() throws Exception {
        System.out.println();
        System.out.println("testExecutableProcedure");
        FBManagedConnectionFactory mcf = initMcf();
        DataSource ds = (DataSource)mcf.createConnectionFactory();
        FBConnection c = (FBConnection)ds.getConnection();
        Statement s = c.createStatement();
        LocalTransaction t = c.getLocalTransaction();
        Exception ex = null;
        t.begin();
        try {
            s.execute("DROP PROCEDURE testproc"); 
        }
        catch (Exception e) {
        }
        t.commit();
        t.begin();
        //try {
            s.execute(CREATE_PROCEDURE); 
            //s.close();
            //}
        /*catch (Exception e) {
            ex = e;
            }*/
        t.commit();
        
        t.begin();
        CallableStatement p = c.prepareCall("EXECUTE PROCEDURE testproc(?)");
        p.setInt(1, 5);
        
        assertTrue("execute returned false for execute procedure statement", p.execute()); 
        ResultSet rs = p.getResultSet();
        while (rs.next()) {
            System.out.println("factorial: " + rs.getInt(1) );
        }
//        rs.close(); //should be automatic
        p.close();
        t.commit();   
        
        t.begin();
        s.execute("DROP PROCEDURE testproc"); 
        s.close();
        t.commit();
        c.close();
        if (ex != null) {
            throw ex;
        }
        
    }
    

}
