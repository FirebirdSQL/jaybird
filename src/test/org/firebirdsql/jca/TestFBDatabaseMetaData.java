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

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;

import javax.resource.spi.LocalTransaction;
import javax.sql.DataSource;

import org.firebirdsql.jdbc.FBConnection;
import org.firebirdsql.jdbc.FBDatabaseMetaData;


/**
 * Describe class <code>TestFBDatabaseMetaData</code> here.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
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
	    // expanded tests added by Jeremy Williams 2002.

        if (log != null) log.info("testGetProcedures");
	createProcedure("testproc1", true);
	createProcedure("testproc2", false);
        ResultSet rs = dmd.getProcedures(null, null, "%");
        assertTrue("No resultset returned from getProcedures", rs != null);
	boolean gotproc1 = false;
	boolean gotproc2 = false;
	while (rs.next()) {
		String name = rs.getString(3);
		String lit_name = rs.getString("PROCEDURE_NAME");
		assertTrue("result set from getProcedures schema mismatch: field 3 should be PROCEDURE_NAME",
			   name.equals(lit_name));
		String remarks = rs.getString(7);
		String lit_remarks = rs.getString("REMARKS");
		if (remarks == null || lit_remarks == null)
			if (remarks != null || lit_remarks != null)
				assertTrue("result set from getProcedures schema mismatch only one of field 7 or 'REMARKS' returned null", false);
			else
				assertTrue("all OK on the western front", true);
		else
			assertTrue ("result set from getProcedures schema mismatch: field 7 should be REMARKS",
			   remarks.equals(lit_remarks));
	        short type = rs.getShort(8);
		short lit_type = rs.getShort("PROCEDURE_TYPE");
		assertTrue("result set from getProcedures schema mismatch: field 8 should be PROCEDURE_TYPE",
			   type == lit_type);
		if (log !=null) log.info(" got procedure " + name);
	        if (name.equals("TESTPROC1")){
			assertTrue("result set from getProcedures had duplicate entry for TESTPROC1",
				!gotproc1);
			gotproc1 = true;
			assertTrue("result set from getProcedures had wrong procedure type for TESTPROC1 " +
				"(should be procedureReturnsResult)", type == DatabaseMetaData.procedureReturnsResult);
			assertTrue("result set from getProcedures did not return a value for REMARKS.", remarks != null);
			assertTrue("result set from getProcedures did not return correct REMARKS section.", remarks.equals("Test description"));
		}
		else if (name.equals("TESTPROC2")) {
			assertTrue("result set from getProcedures had duplicate entry for TESTPROC2",
				!gotproc2);
			gotproc2 = true;
			assertTrue("result set from getProcedures had wrong procedure type for TESTPROC2 " +
				"(should be procedureNoResult)", type == DatabaseMetaData.procedureNoResult);
		}
		else
		  assertTrue("result set from getProcedures returned unknown procedure " + name, false);
	}
	assertTrue ("result set from getProcedures did not return procedure testproc1", gotproc1);
	assertTrue ("result set from getProcedures did not return procedure testproc2", gotproc2);
	rs.close();
	dropProcedure("testproc1");
	dropProcedure("testproc2");
	if (ex != null)
	   throw ex;
    }

    public void testGetProcedureColumns() throws Exception {

        if (log != null) log.info("testGetProcedureColumns");
	createProcedure("testproc1", true);
	createProcedure("testproc2", false);

        ResultSet rs = dmd.getProcedureColumns(null, null, "%", "%");
        assertTrue("No resultset returned from getProcedureColumns", rs != null);
	int rownum = 0;
	while (rs.next()) {
		rownum++;
		String procname = rs.getString(3);
		String colname = rs.getString(4);
		short coltype = rs.getShort(5);
		short datatype = rs.getShort(6);
		String typename = rs.getString(7);
		int precision = rs.getInt(8);
		int length = rs.getInt(9);
		short scale = rs.getShort(10);
		short radix = rs.getShort(11);
		short nullable = rs.getShort(12);
		String remarks = rs.getString(13);
        if (log != null)
		  log.info ("row " + (new Integer(rownum)).toString() + 
            "proc " + procname + " field " + colname);

		// per JDBC 2.0 spec, there is a very specific order these
		// rows should come back, so if field names don't match
		// what I'm expecting, in the order I expect them, there
		// is a bug.
		switch (rownum) {
	          case 4:
		    assertTrue("wrong pr name.", procname.equals("TESTPROC1")); 
		    assertTrue("wrong f name.", colname.equals("IN1"));
		    assertTrue("wrong c type.", coltype ==
				    DatabaseMetaData.procedureColumnIn);
		    assertTrue("wrong d type.", datatype == Types.INTEGER);
		    assertTrue("wrong t name.", typename.equals("INTEGER"));
		    assertTrue("wrong radix.", radix == 10);
		    assertTrue("wrong nullable.", nullable == 
				    DatabaseMetaData.procedureNullable);
		    assertTrue("wrong comment.", remarks == null);
		    break;
		  case 5:
		    assertTrue("wrong pr name", procname.equals("TESTPROC1"));
		    assertTrue("wrong f name", colname.equals("IN2"));
		    break;
		  case 1:
		    assertTrue("wrong pr name", procname.equals("TESTPROC1"));
		    assertTrue("wrong f name", colname.equals("OUT1"));
		    assertTrue("wrong c type", coltype == 
				    DatabaseMetaData.procedureColumnOut);
		    break;
		  case 2:
		    assertTrue("wrong pr name", procname.equals("TESTPROC1"));
		    assertTrue("wrong f name", colname.equals("OUT2"));
		    break;
		  case 3:
		    assertTrue("wrong pr name", procname.equals("TESTPROC1"));
		    assertTrue("wrong f name", colname.equals("OUT3"));
		    break;
		  case 6:
		    assertTrue("wrong pr name", procname.equals("TESTPROC2"));
		    assertTrue("wrong f name", colname.equals("INP"));
		    break;
		  default:
		    assertTrue("stray field returned from getProcedureColumns.",
				    false);
		} // end-switch
		

	} // end-while
	dropProcedure("testproc1");
	dropProcedure("testproc2");
	if (ex != null)
	   throw ex;
    }

    public void testGetColumnPrivileges() throws Exception {

        if (log != null) log.info("testGetColumnPrivileges");

        ResultSet rs = dmd.getColumnPrivileges(null, null, "RDB$RELATIONS", "%");
        assertTrue("No resultset returned from getColumnPrivileges", rs != null);
    }

    public void testGetTablePrivileges() throws Exception {
        if (log != null) log.info("testGetTablePrivileges");

        ResultSet rs = dmd.getTablePrivileges(null, null, "%");
        assertTrue("No resultset returned from getTablePrivileges", rs != null);
    }

    public void testGetTypeInfo() throws Exception {

        if (log != null) log.info("testGetTypeInfo");

        ResultSet rs = dmd.getTypeInfo();
        assertTrue("No resultset returned from getTypeInfo", rs != null);
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
        if (log != null) log.info("getTypeInfoblePrivileges returned: " + out);
        assertTrue("Not enough TypeInfo rows fetched: " + count, count >= 15);
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

    private void createProcedure(String procedureName, boolean returnsData) throws Exception {
	dropProcedure(procedureName);
	t.begin();
	try {
	    if (returnsData) {
	      s.execute("CREATE PROCEDURE " + procedureName + "(IN1 INTEGER, IN2 FLOAT)" +
			    "RETURNS (OUT1 VARCHAR(20), OUT2 DOUBLE PRECISION, OUT3 INTEGER) AS "+
			    "DECLARE VARIABLE X INTEGER;"+
			    "BEGIN" +
			    " OUT1 = 'Out String';" +
			    " OUT2 = 45;" +
			    " OUT3 = IN1;" +
			    "END");
	      s.execute("UPDATE RDB$PROCEDURES SET RDB$DESCRIPTION='Test description' WHERE RDB$PROCEDURE_NAME='" + procedureName.toUpperCase() + "'");
	    }
	    else
	      s.execute("CREATE PROCEDURE " + procedureName + " (INP INTEGER) AS BEGIN exit; END");
	}
	catch (Exception e) {
		if (log != null) log.warn("error creating procedure: " + e.getMessage());
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

    private void dropProcedure(String procedureName) throws Exception {
	t.begin();
	try {
	    s.execute("DROP PROCEDURE " + procedureName);
	}
	catch (Exception e) {
	}
	t.commit();
    }

}

