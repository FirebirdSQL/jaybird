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
import org.firebirdsql.jdbc.FBDatabaseMetaData;

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
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 *   @version $ $
 */



public class TestFBDatabaseMetaData extends TestXABase {

    private FBConnection c;
    private Statement s;
    private DatabaseMetaData dmd;
    private LocalTransaction t;
    private Exception ex;


    public TestFBDatabaseMetaData(String name) {
        super(name);
    }


    public void setUp() throws Exception {
       super.setUp();
        ex = null;
        FBManagedConnectionFactory mcf = initMcf();
        DataSource ds = (DataSource)mcf.createConnectionFactory();
        c = (FBConnection)ds.getConnection();
        s = c.createStatement();
        t = c.getLocalTransaction();
        dmd = c.getMetaData();
    }

    public void tearDown() throws Exception {
        s.close();
        s = null;
        if (c.inTransaction()) {
            t.commit();
        }
        t = null;
        dmd = null;
        c.close();
        c = null;
        super.tearDown();
    }

    public void testGetTablesNull() throws Exception {

        if (log != null) log.info("testGetTablesNull");
        createTable("T1");

        ResultSet rs = dmd.getTables(null, null, "T1", null);
        int count = 0;
        while (rs.next()) {
            String name =  rs.getString(3);
            if (log != null) log.info("table name: " + name);
            count++;
            assertTrue("Didn't get back the name expected", "T1".equals(name));
        }
        assertTrue("Got more than one table name back!", count == 1);
        rs.close();


        dropTable("T1");
        if (ex != null) {
            throw ex;
        }

    }
    public void testGetTablesSystem() throws Exception {

        if (log != null) log.info("testGetTablesSystem");
        createTable("T1");

        ResultSet rs = dmd.getTables(null, null, "T1", new String[] {"SYSTEM TABLE"});
        int count = 0;
        while (rs.next()) {
            String name =  rs.getString(3);
            if (log != null) log.info("table name: " + name);
            count++;
            assertTrue("Didn't get back the name expected", "T1".equals(name));
        }
        assertTrue("Got more a table name back!", count == 0);
        rs.close();


        dropTable("T1");
        if (ex != null) {
            throw ex;
        }

    }
    public void testGetTablesTable() throws Exception {

        if (log != null) log.info("testGetTablesTable");
        createTable("T1");

        ResultSet rs = dmd.getTables(null, null, "T1", new String[] {"TABLE"});
        int count = 0;
        while (rs.next()) {
            String name =  rs.getString(3);
            if (log != null) log.info("table name: " + name);
            count++;
            assertTrue("Didn't get back the name expected", "T1".equals(name));
        }
        assertTrue("Got more a table name back!", count == 1);
        rs.close();


        dropTable("T1");
        if (ex != null) {
            throw ex;
        }

    }

    public void testGetTablesView() throws Exception {

        if (log != null) log.info("testGetTablesView");
        createTable("T1");

        ResultSet rs = dmd.getTables(null, null, "T1", new String[] {"VIEW"});
        int count = 0;
        while (rs.next()) {
            String name =  rs.getString(3);
            if (log != null) log.info("table name: " + name);
            count++;
            assertTrue("Didn't get back the name expected", "T1".equals(name));
        }
        assertTrue("Got more a table name back!", count == 0);
        rs.close();


        dropTable("T1");
        if (ex != null) {
            throw ex;
        }

    }

    public void testGetSystemTablesSystem() throws Exception {

        if (log != null) log.info("testGetSystemTablesSystem");

        ResultSet rs = dmd.getTables(null, null, "RDB$RELATIONS", new String[] {"SYSTEM TABLE"});
        int count = 0;
        while (rs.next()) {
            String name =  rs.getString(3);
            if (log != null) log.info("table name: " + name);
            count++;
            assertTrue("Didn't get back the name expected", "RDB$RELATIONS".equals(name));
        }
        assertTrue("Got more than one table name back!", count == 1);
        rs.close();


    }

    public void testGetAllSystemTablesSystem() throws Exception {

        if (log != null) log.info("testGetSystemTablesSystem");

        ResultSet rs = dmd.getTables(null, null, "%", new String[] {"SYSTEM TABLE"});
        int count = 0;
        while (rs.next()) {
            String name =  rs.getString(3);
            if (log != null) log.info("table name: " + name);
            count++;
        }
        assertTrue("# of system tables is not 32: counted: " + count, count == 32);
        rs.close();


    }


    public void testAAStringFunctions() {

        if (log != null) log.info("testAAStringFunctions");
        FBDatabaseMetaData d = (FBDatabaseMetaData)dmd;
        assertTrue("claims test\\_me has wildcards",  d.hasNoWildcards("test\\_me"));
        assertTrue("strip escape wrong", d.stripEscape("test\\_me").equals("test_me"));
        assertTrue("strip quotes wrong", d.stripQuotes("test_me").equals("TEST_ME"));
        assertTrue("strip quotes wrong: " + d.stripQuotes("\"test_me\""), d.stripQuotes("\"test_me\"").equals("test_me"));
    }

    public void testGetTablesWildcardQuote() throws Exception {

        if (log != null) log.info("testGetTablesWildcardQuote");
        createTable("test_me");
        createTable("test__me");
        createTable("\"test_ me\"");
        createTable("\"test_ me too\"");
        createTable("\"test_me too\"");

        ResultSet rs = dmd.getTables(null, null, "test%m_", new String[] {"TABLE"});
        int count = 0;
        while (rs.next()) {
            String name =  rs.getString(3);
            if (log != null) log.info("table name: " + name);
            assertTrue("wrong name found: " + name, "TEST_ME".equals(name) || "TEST__ME".equals(name));
            count++;
        }
        assertTrue("more than one table found: " + count, count == 2);
        rs.close();

        rs = dmd.getTables(null, null, "test\\_me", new String[] {"TABLE"});
        count = 0;
        while (rs.next()) {
            String name =  rs.getString(3);
            if (log != null) log.info("table name: " + name);
            assertTrue("wrong name found: " + name, "TEST_ME".equals(name));
            count++;
        }
        assertTrue("more than one table found: " + count, count == 1);
        rs.close();

        rs = dmd.getTables(null, null, "\"test\\_ me\"", new String[] {"TABLE"});
        count = 0;
        while (rs.next()) {
            String name =  rs.getString(3);
            if (log != null) log.info("table name: " + name);
            assertTrue("wrong name found: " + name, "test_ me".equals(name));
            count++;
        }
        assertTrue("more than one table found: " + count, count == 1);
        rs.close();

        rs = dmd.getTables(null, null, "\"test\\_ me%\"", new String[] {"TABLE"});
        count = 0;
        while (rs.next()) {
            String name =  rs.getString(3);
            if (log != null) log.info("table name: " + name);
            assertTrue("wrong name found: " + name, "test_ me".equals(name) || "test_ me too".equals(name));
            count++;
        }
        assertTrue("more than one table found: " + count, count == 2);
        rs.close();

        rs = dmd.getTables(null, null, "RDB_RELATIONS", new String[] {"SYSTEM TABLE"});
        count = 0;
        while (rs.next()) {
            String name =  rs.getString(3);
            if (log != null) log.info("table name: " + name);
            assertTrue("wrong name found: " + name, "RDB$RELATIONS".equals(name));
            count++;
        }
        assertTrue("more than one table found: " + count, count == 1);
        rs.close();

        dropTable("test_me");
        dropTable("test__me");
        dropTable("\"test_ me\"");
        dropTable("\"test_ me too\"");
        dropTable("\"test_me too\"");

        if (ex != null) {
            throw ex;
        }

    }
    public void testGetColumnsWildcardQuote() throws Exception {

        if (log != null) log.info("testGetColumnsWildcardQuote");
        createTable("test_me");
        createTable("test__me");
        createTable("\"test_ me\"");
        createTable("\"test_ me too\"");
        createTable("\"test_me too\"");

        ResultSet rs = dmd.getColumns(null, null, "test%m_", "\"my\\_ column2\"");
        int count = 0;
        while (rs.next()) {
            String name =  rs.getString(3);
            String column = rs.getString(4);
            if (log != null) log.info("table name: " + name);
            assertTrue("wrong name found: " + name, "TEST_ME".equals(name) || "TEST__ME".equals(name));
            assertTrue("wrong column found: " + column, "my_ column2".equals(column));
            count++;
        }
        assertTrue("more than one table found: " + count, count == 2);
        rs.close();


        dropTable("test_me");
        dropTable("test__me");
        dropTable("\"test_ me\"");
        dropTable("\"test_ me too\"");
        dropTable("\"test_me too\"");

        if (ex != null) {
            throw ex;
        }

    }


    public void testGetProcedures() throws Exception {

        if (log != null) log.info("testGetProcedures");

        ResultSet rs = dmd.getProcedures(null, null, "%");
        assertTrue("No resultset returned from getProcedures", rs != null);
    }

    public void testGetProcedureColumns() throws Exception {

        if (log != null) log.info("testGetProcedureColumns");

        ResultSet rs = dmd.getProcedureColumns(null, null, "%", "%");
        assertTrue("No resultset returned from getProcedureColumns", rs != null);
    }

    public void testGetColumnPrivileges() throws Exception {

        if (log != null) log.info("testGetColumnPrivileges");

        ResultSet rs = dmd.getColumnPrivileges(null, null, "RDB$RELATIONS", "%");
        assertTrue("No resultset returned from getProcedureColumns", rs != null);
    }

    public void testGetTablePrivileges() throws Exception {
        if (log != null) log.info("testGetTablePrivileges");

        ResultSet rs = dmd.getTablePrivileges(null, null, "%");
        assertTrue("No resultset returned from getTablePrivileges", rs != null);
    }

    public void testGetTypeInfo() throws Exception {

        if (log != null) log.info("testGetTypeInfo");

        ResultSet rs = dmd.getTypeInfo();
        assertTrue("No resultset returned from getTablePrivileges", rs != null);
        int count = 0;
        String out = "";
        while (rs.next()) {
            count++;
            for (int i = 1; i <= 18; i++) {
                Object o = rs.getObject(i);
                if (o == null) {
                    o = "null";
                }
                out += o.toString();
            }
            out += System.getProperty("line.separator");
        }
        if (log != null) log.info("getTablePrivileges returned: " + out);
        assertTrue("Not enough TypeInfo rows fetched: " + count, count >= 12);
    }


    private void createTable(String tableName) throws Exception {
        dropTable(tableName);
        t.begin();
        try {
            s.execute("CREATE TABLE " + tableName + " ( C1 INTEGER not null primary key, C2 SMALLINT, C3 DECIMAL(18,0), C4 FLOAT, C5 DOUBLE PRECISION, \"my column1\" CHAR(10), \"my_ column2\" VARCHAR(20))");
        }
        catch (Exception e) {
            ex = e;
        }
        t.commit();
    }

    private void dropTable(String tableName) throws Exception {
        t.begin();
        try {
            s.execute("drop table " + tableName);
        }
        catch (Exception e) {
        }
        t.commit();
    }

}

