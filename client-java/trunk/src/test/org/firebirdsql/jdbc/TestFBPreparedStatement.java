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

package org.firebirdsql.jdbc;

import org.firebirdsql.common.FBTestBase;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Calendar;
import java.util.Properties;
import java.util.TimeZone;

/**
 * Describe class <code>TestFBPreparedStatement</code> here.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
public class TestFBPreparedStatement extends FBTestBase {

    public static final String CREATE_GENERATOR = "CREATE GENERATOR test_generator";

    public static final String DROP_GENERATOR = "DROP GENERATOR test_generator";

    public static final String CREATE_TEST_BLOB_TABLE = "CREATE TABLE test_blob ("
            + "  ID INTEGER, "
            + "  OBJ_DATA BLOB, "
            + "  TS_FIELD TIMESTAMP, "
            + "  T_FIELD TIME " + ")";

    public static final String CREATE_TEST_CHARS_TABLE = ""
            + "CREATE TABLE TESTTAB (" + "ID INTEGER, "
            + "FIELD1 VARCHAR(10) NOT NULL PRIMARY KEY,"
            + "FIELD2 VARCHAR(30)," + "FIELD3 VARCHAR(20)," + "FIELD4 FLOAT,"
            + "FIELD5 CHAR," + "FIELD6 VARCHAR(5)," + "FIELD7 CHAR(1),"
            + "num_field numeric(9,2)" + ")";

    public static final String CREATE_UNRECOGNIZED_TR_TABLE = ""
            + "CREATE TABLE t1("
            + "  c1 CHAR(2) CHARACTER SET ASCII NOT NULL, "
            + "  c2 BLOB SUB_TYPE TEXT CHARACTER SET ASCII NOT NULL " + ")";

    public static final String ADD_CONSTRAINT_T1_C1 = ""
            + "ALTER TABLE t1 ADD CONSTRAINT t1_c1 PRIMARY KEY (c1)";

    public static final String INIT_T1 = ""
            + "INSERT INTO t1 VALUES ('XX', 'no more bugs')";

    public static final String DROP_TEST_BLOB_TABLE = "DROP TABLE test_blob";

    public static final String DROP_TEST_CHARS_TABLE = ""
            + "DROP TABLE TESTTAB";

    public static final String DROP_UNRECOGNIZED_TR_TABLE = ""
            + "DROP TABLE t1";

    public static final String TEST_STRING = "This is simple test string.";

    public static final String ANOTHER_TEST_STRING = "Another test string.";

    public TestFBPreparedStatement(String testName) {
        super(testName);
    }

    Connection con;

    protected void setUp() throws Exception {
        super.setUp();

        Class.forName(FBDriver.class.getName());
        con = this.getConnectionViaDriverManager();

        Statement stmt = con.createStatement();
        try {
            try {
                stmt.executeUpdate(DROP_TEST_BLOB_TABLE);
            } catch (Exception e) {
                // e.printStackTrace();
            }

            try {
                stmt.executeUpdate(DROP_UNRECOGNIZED_TR_TABLE);
            } catch (Exception e) {
                // e.printStackTrace();
            }

            try {
                stmt.executeUpdate(DROP_TEST_CHARS_TABLE);
            } catch (Exception e) {
                // ignore
            }

            try {
                stmt.executeUpdate(DROP_GENERATOR);
            } catch (Exception ex) {
            }

            stmt.executeUpdate(CREATE_TEST_BLOB_TABLE);
            stmt.executeUpdate(CREATE_UNRECOGNIZED_TR_TABLE);
            stmt.executeUpdate(ADD_CONSTRAINT_T1_C1);
            stmt.executeUpdate(INIT_T1);
            stmt.executeUpdate(CREATE_TEST_CHARS_TABLE);

            stmt.executeUpdate(CREATE_GENERATOR);

        } finally {
            stmt.close();
        }

    }

    protected void tearDown() throws Exception {
        Statement stmt = con.createStatement();
        try {
            stmt.executeUpdate(DROP_TEST_BLOB_TABLE);
            stmt.executeUpdate(DROP_TEST_CHARS_TABLE);
            stmt.executeUpdate(DROP_UNRECOGNIZED_TR_TABLE);
        } finally {
            stmt.close();
        }

        con.close();

        super.tearDown();
    }

    public void testModifyBlob() throws Exception {
        int id = 1;

        PreparedStatement insertPs = con
                .prepareStatement("INSERT INTO test_blob (id, obj_data) VALUES (?,?);");

        try {
            insertPs.setInt(1, id);
            insertPs.setBytes(2, TEST_STRING.getBytes());

            int inserted = insertPs.executeUpdate();

            assertTrue("Row should be inserted.", inserted == 1);

            checkSelectString(TEST_STRING, id);

            // Update item
            PreparedStatement updatePs = con
                    .prepareStatement("UPDATE test_blob SET obj_data=? WHERE id=?;");

            try {
                updatePs.setBytes(1, ANOTHER_TEST_STRING.getBytes());
                updatePs.setInt(2, id);
                updatePs.executeUpdate();

                updatePs.clearParameters();

                checkSelectString(ANOTHER_TEST_STRING, id);

                updatePs.setBytes(1, TEST_STRING.getBytes());
                updatePs.setInt(2, id + 1);
                int updated = updatePs.executeUpdate();

                assertTrue("No rows should be updated.", updated == 0);

                checkSelectString(ANOTHER_TEST_STRING, id);
            } finally {
                updatePs.close();
            }

        } finally {
            insertPs.close();
        }
    }

    public void testMixedExecution() throws Throwable {
        PreparedStatement ps = con
                .prepareStatement("INSERT INTO test_blob (id, obj_data) VALUES(?, NULL)");

        try {

            ps.setInt(1, 100);
            ps.execute();

            ResultSet rs = ps.executeQuery("SELECT * FROM test_blob");
            while (rs.next()) {
                // nothing
            }
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        } finally {
            ps.close();
        }

    }

    void checkSelectString(String stringToTest, int id) throws Exception {
        PreparedStatement selectPs = con
                .prepareStatement("SELECT obj_data FROM test_blob WHERE id = ?");

        try {
            selectPs.setInt(1, id);
            ResultSet rs = selectPs.executeQuery();

            assertTrue("There must be at least one row available.", rs.next());

            String result = rs.getString(1);

            assertTrue("Selected string must be equal to inserted one.",
                stringToTest.equals(result));

            assertTrue("There must be exactly one row.", !rs.next());

            rs.close();
        } finally {
            selectPs.close();
        }
    }

    public void testGenerator() throws Exception {
        PreparedStatement ps = con
                .prepareStatement("SELECT gen_id(test_generator, 1) as new_value FROM rdb$database");

        ResultSet rs = ps.executeQuery();

        assertTrue("Should get at least one row", rs.next());

        rs.getLong("new_value");

        assertFalse("should have only one row", rs.next());

        rs.close();
        ps.close();
    }

    /**
     * Test case to reproduce problem with the connection when "operation was
     * cancelled" happens. Bug is fixed, however due to workaround for this
     * problem (@see org.firebirdsql.jdbc.field.FBWorkaroundStringField) this
     * test case is no longer relevant. In order to make it execute correctly
     * one has to remove this workaround.
     * 
     * @throws Exception
     *             if something went wrong.
     */
    public void _testOpCancelled() throws Exception {
        con.setAutoCommit(true);
        PreparedStatement prep = con
                .prepareStatement("INSERT INTO TESTTAB (FIELD1, FIELD3, FIELD4, FIELD5 ) "
                        + "VALUES ( ?, ?, ?, ? )");

        try {
            for (int i = 0; i < 5; i++) {
                try {
                    if (i == 0) {
                        prep.setObject(1, "0123456789");
                        prep.setObject(2, "01234567890123456789");
                        prep.setObject(3, "1259.9");
                        prep.setObject(4, "A");
                    }
                    if (i == 1) {
                        prep.setObject(1, "0123456787");
                        prep.setObject(2, "012345678901234567890");
                        prep.setObject(3, "0.9");
                        prep.setObject(4, "B");
                    }
                    if (i == 2) {
                        prep.setObject(1, "0123456788");
                        prep.setObject(2, "Fld3-Rec3");
                        prep.setObject(3, "0.9");
                        prep.setObject(4, "B");
                    }
                    if (i == 3) {
                        prep.setObject(1, "0123456780");
                        prep.setObject(2, "Fld3-Rec4");
                        prep.setObject(3, "1299.5");
                        prep.setObject(4, "Q");
                    }
                    if (i == 4) {
                        prep.setObject(1, "0123456779");
                        prep.setObject(2, "Fld3-Rec5");
                        prep.setObject(3, "1844");
                        prep.setObject(4, "Z");
                    }
                    prep.execute();
                } catch (SQLException x) {
                    // x.printStackTrace();
                }
            }
        } finally {
            prep.close();
        }
    }

    /**
     * Test if parameters are correctly checked for their length.
     * 
     * @throws Exception
     *             if something went wrong.
     */
    public void testLongParameter() throws Exception {
        con.setAutoCommit(true);
        Statement stmt = con.createStatement();
        try {
            stmt.execute("INSERT INTO testtab(id, field1, field6) VALUES(1, '', 'a')");
        } finally {
            stmt.close();
        }

        con.setAutoCommit(false);

        PreparedStatement ps = con
                .prepareStatement("UPDATE testtab SET field6=? WHERE id = 1");
        try {
            try {
                ps.setString(1, "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
                ps.execute();
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();

            fail("No exception should be thrown.");
        } finally {
            ps.close();
        }
    }

    /**
     * Test if batch execution works correctly.
     * 
     * @throws Exception
     *             if something went wrong.
     */
    public void testBatch() throws Exception {
        Connection c = getConnectionViaDriverManager();
        try {
            Statement s = c.createStatement();
            s.executeUpdate("CREATE TABLE foo (" + "bar varchar(64) NOT NULL, "
                    + "baz varchar(8) NOT NULL, "
                    + "CONSTRAINT pk_foo PRIMARY KEY (bar, baz))");
            PreparedStatement ps = c
                    .prepareStatement("Insert into foo values (?, ?)");
            ps.setString(1, "one");
            ps.setString(2, "two");
            ps.addBatch();
            ps.executeBatch();
            ps.clearBatch();
            ps.setString(1, "one");
            ps.setString(2, "three");
            ps.addBatch();
            ps.executeBatch();
            ps.clearBatch();
        } finally {
            c.close();
        }
    }

    public void testTimestampWithCalendar() throws Exception {
        Properties props = new Properties();
        props.putAll(getDefaultPropertiesForConnection());
        props.setProperty("timestamp_uses_local_timezone", "");

        Connection connection = DriverManager.getConnection(getUrl(), props);
        try {
            PreparedStatement stmt = connection
                    .prepareStatement("INSERT INTO test_blob(id, ts_field) VALUES (?, ?)");

            try {
                Calendar calendar = Calendar.getInstance(TimeZone
                        .getTimeZone("GMT+01"));
                Calendar utcCalendar = Calendar.getInstance(TimeZone
                        .getTimeZone("UTC"));

                Timestamp ts = new Timestamp(calendar.getTime().getTime());

                stmt.setInt(1, 2);
                stmt.setTimestamp(2, ts, calendar);

                stmt.execute();

                stmt.setInt(1, 3);
                stmt.setTimestamp(2, ts, utcCalendar);

                stmt.execute();

                Statement selectStmt = connection.createStatement();
                try {
                    ResultSet rs = selectStmt
                            .executeQuery("SELECT id, CAST(ts_field AS VARCHAR(30)), ts_field FROM test_blob");

                    Timestamp ts2 = null;
                    Timestamp ts3 = null;

                    String ts2Str = null;
                    String ts3Str = null;

                    int maxLength = 22;

                    // workaround for the bug in java.sql.Timestamp in JDK 1.3
                    if ("1.3".equals(getProperty("java.specification.version")))
                        maxLength = 19;

                    while (rs.next()) {

                        switch (rs.getInt(1)) {
                            case 2:
                                ts2 = rs.getTimestamp(3);
                                ts2Str = rs.getString(2)
                                        .substring(0, maxLength);
                                break;

                            case 3:
                                ts3 = rs.getTimestamp(3);
                                ts3Str = rs.getString(2)
                                        .substring(0, maxLength);
                                break;
                        }
                    }

                    assertTrue(
                        "Timestamps 2 and 3 should differ for 3600 seconds.",
                        Math.abs(ts2.getTime() - ts3.getTime()) == 3600 * 1000);

                    assertTrue("Server should see the same timestamp", ts2
                            .toString().substring(0, maxLength).equals(ts2Str));

                    assertTrue("Server should see the same timestamp", ts3
                            .toString().substring(0, maxLength).equals(ts3Str));

                } catch (SQLException ex) {
                    ex.printStackTrace();
                    throw ex;
                } finally {
                    selectStmt.close();
                }

            } finally {
                stmt.close();
            }

        } finally {
            connection.close();
        }
    }

    public void testTimeWithCalendar() throws Exception {
        Properties props = new Properties();
        props.putAll(getDefaultPropertiesForConnection());
        props.setProperty("timestamp_uses_local_timezone", "");

        Connection connection = DriverManager.getConnection(getUrl(), props);
        try {
            PreparedStatement stmt = connection
                    .prepareStatement("INSERT INTO test_blob(id, t_field) VALUES (?, ?)");

            try {
                Calendar calendar = Calendar.getInstance(TimeZone
                        .getTimeZone("GMT+01"));
                Calendar utcCalendar = Calendar.getInstance(TimeZone
                        .getTimeZone("UTC"));

                Time t = new Time(calendar.getTime().getTime());

                stmt.setInt(1, 2);
                stmt.setTime(2, t, calendar);

                stmt.execute();

                stmt.setInt(1, 3);
                stmt.setTime(2, t, utcCalendar);

                stmt.execute();

                Statement selectStmt = connection.createStatement();
                try {
                    ResultSet rs = selectStmt
                            .executeQuery("SELECT id, CAST(t_field AS VARCHAR(30)), t_field FROM test_blob");

                    Time t2 = null;
                    Time t3 = null;

                    String t2Str = null;
                    String t3Str = null;

                    while (rs.next()) {

                        switch (rs.getInt(1)) {
                            case 2:
                                t2 = rs.getTime(3);
                                t2Str = rs.getString(2);
                                break;

                            case 3:
                                t3 = rs.getTime(3);
                                t3Str = rs.getString(2);
                                break;
                        }
                    }

                    assertTrue(
                        "Timestamps 2 and 3 should differ for 3600 seconds.",
                        Math.abs(t2.getTime() - t3.getTime()) == 3600 * 1000);

                    assertTrue("Server should see the same timestamp", t2
                            .toString().equals(t2Str.substring(0, 8)));

                    assertTrue("Server should see the same timestamp", t3
                            .toString().equals(t3Str.substring(0, 8)));

                } finally {
                    selectStmt.close();
                }

            } finally {
                stmt.close();
            }

        } finally {
            connection.close();
        }
    }

    /**
     * Test if failure in setting the parameter leaves the driver in correct
     * state (i.e. "not all params were set").
     * 
     * @throws Exception
     */
    public void testBindParameter() throws Exception {
        Connection connection = getConnectionViaDriverManager();

        try {
            connection.setAutoCommit(false);

            PreparedStatement ps = connection
                    .prepareStatement("UPDATE testtab SET field1 = ? WHERE id = ?");
            try {
                try {
                    ps.setString(1,
                        "veeeeeeeeeeeeeeeeeeeeery looooooooooooooooooooooong striiiiiiiiiiiiiiiiiiing");
                } catch (DataTruncation ex) {
                    // ignore
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }

                ps.setInt(2, 1);

                try {
                    ps.execute();
                } catch (FBMissingParameterException ex) {
                    // correct
                }

                Statement stmt = connection.createStatement();
                try {
                    stmt.execute("SELECT * FROM rdb$database");
                } catch (Throwable t) {
                    if (t instanceof SQLException) {
                        // ignore
                    } else
                        fail("Should not throw exception");
                }
            } finally {
                ps.close();
            }

        } finally {
            connection.close();
        }
    }

    /**
     * Test if failure in setting the parameter leaves the driver in correct
     * state (i.e. "not all params were set").
     * 
     * @throws Exception
     */
    public void testLikeParameter() throws Exception {
        Connection connection = getConnectionViaDriverManager();

        try {
            connection.setAutoCommit(false);

            PreparedStatement ps = connection
                    .prepareStatement("SELECT * FROM testtab WHERE field7 = ?");
            try {
                try {
                    ps.setString(1, "%a%");
                    ResultSet rs = ps.executeQuery();
                    // fail("should throw data truncation");
                } catch (DataTruncation ex) {
                    // ignore
                }

            } finally {
                ps.close();
            }

            Statement stmt = connection.createStatement();
            try {
                stmt.execute("SELECT * FROM rdb$database");
            } catch (Throwable t) {
                if (t instanceof SQLException) {
                    // ignore
                } else
                    fail("Should not throw exception");
            }

        } finally {
            connection.close();
        }
    }

    public void _testUnrecognizedTransaction() throws Exception {
        Connection connection = getConnectionViaDriverManager();
        try {
            String sql = "SELECT 1 FROM t1 WHERE c1 = ? AND c2 = ?";
            PreparedStatement ps;
            ResultSet rs;

            ps = connection.prepareStatement(sql);
            ps.setString(1, "XX");
            ps.setString(2, "bug busters");
            rs = ps.executeQuery();
            assertTrue("Should find something.", rs.next());

        } finally {
            connection.close();
        }
    }

    public void testGetExecutionPlan() throws SQLException {
        Connection conn = getConnectionViaDriverManager();
        try {
            AbstractPreparedStatement stmt = (AbstractPreparedStatement) conn
                    .prepareStatement("SELECT * FROM TESTTAB WHERE ID = 2");
            String executionPlan = stmt.getExecutionPlan();
            assertTrue("Ensure that a valid execution plan is retrieved",
                executionPlan.indexOf("TESTTAB") >= 0);
            stmt.close();
        } finally {
            conn.close();
        }
    }

    public void testGetStatementType() throws SQLException {
        Connection conn = getConnectionViaDriverManager();
        try {
            AbstractPreparedStatement stmt = (AbstractPreparedStatement) conn
                    .prepareStatement("SELECT * FROM TESTTAB");
            assertEquals(
                "TYPE_SELECT should be returned for a SELECT statement",
                FirebirdPreparedStatement.TYPE_SELECT, stmt.getStatementType());
            stmt.close();

            stmt = (AbstractPreparedStatement) conn
                    .prepareStatement("INSERT INTO testtab(id, field1, field6) VALUES(?, ?, ?)");
            assertEquals(
                "TYPE_INSERT should be returned for an INSERT statement",
                FirebirdPreparedStatement.TYPE_INSERT, stmt.getStatementType());
            stmt.close();

            stmt = (AbstractPreparedStatement) conn
                    .prepareStatement("DELETE FROM TESTTAB WHERE ID = ?");
            assertEquals(
                "TYPE_DELETE should be returned for a DELETE statement",
                FirebirdPreparedStatement.TYPE_DELETE, stmt.getStatementType());
            stmt.close();

            stmt = (AbstractPreparedStatement) conn
                    .prepareStatement("UPDATE TESTTAB SET FIELD1 = ? WHERE ID = ?");
            assertEquals(
                "TYPE_UPDATE should be returned for an UPDATE statement",
                FirebirdPreparedStatement.TYPE_UPDATE, stmt.getStatementType());
            stmt.close();

            stmt = (AbstractPreparedStatement) conn
                    .prepareStatement("INSERT INTO testtab(field1) VALUES(?) RETURNING id");
            assertEquals(
                "TYPE_EXEC_PROCEDURE should be returned for an UPDATE statement",
                FirebirdPreparedStatement.TYPE_EXEC_PROCEDURE,
                stmt.getStatementType());
            stmt.close();
        } finally {
            conn.close();
        }
    }

    public void _testLikeFullLength() throws Exception {
        Connection connection = getConnectionViaDriverManager();
        try {
            Statement stmt = connection.createStatement();
            try {
                stmt.execute("INSERT INTO testtab(field1) VALUES('abcdefghij')");
            } finally {
                stmt.close();
            }

            PreparedStatement ps = connection
                    .prepareStatement("SELECT field1 FROM testtab WHERE field1 LIKE ?");
            try {
                ps.setString(1, "%abcdefghi%");

                ResultSet rs = ps.executeQuery();
                assertTrue("Should find a record.", rs.next());
            } finally {
                ps.close();
            }
        } finally {
            connection.close();
        }
    }

    /**
     * Test if parameters are correctly checked for their length.
     * 
     * @throws Exception
     *             if something went wrong.
     */
    public void testNumeric15_2() throws Exception {

        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("sqlDialect", "1");

        Connection connection = DriverManager.getConnection(getUrl(), props);
        try {
            Statement stmt = connection.createStatement();
            try {
                stmt.execute("INSERT INTO testtab(id, field1, num_field) VALUES(1, '', 10.02)");
            } finally {
                stmt.close();
            }

            PreparedStatement ps = connection
                    .prepareStatement("SELECT num_field FROM testtab WHERE id = 1");
            try {
                ResultSet rs = ps.executeQuery();

                assertTrue(rs.next());

                float floatValue = rs.getFloat(1);
                double doubleValue = rs.getDouble(1);
                BigDecimal bigDecimalValue = rs.getBigDecimal(1);

                assertTrue(doubleValue == 10.02);

            } catch (SQLException ex) {
                ex.printStackTrace();

                fail("No exception should be thrown.");
            } finally {
                ps.close();
            }
        } finally {
            connection.close();
        }
    }

    public void testInsertReturning() throws Exception {
        Connection conn = getConnectionViaDriverManager();
        try {
            FirebirdPreparedStatement stmt = (FirebirdPreparedStatement) conn
                    .prepareStatement("INSERT INTO testtab(id, field1) VALUES(gen_id(test_generator, 1), 'a') RETURNING id");
            try {
                assertEquals(
                    "TYPE_EXEC_PROCEDURE should be returned for an INSERT...RETURNING statement",
                    FirebirdPreparedStatement.TYPE_EXEC_PROCEDURE,
                    stmt.getStatementType());

                ResultSet rs = stmt.executeQuery();

                assertTrue("Should return at least 1 row", rs.next());
                assertTrue("Generator value should be > 0 (actual value is "
                        + rs.getInt(1) + ")", rs.getInt(1) > 0);
                assertTrue("Should return exactly one row", !rs.next());
            } finally {
                stmt.close();
            }

        } finally {
            conn.close();
        }
    }

    private static final String dummySelect =
        "execute block returns(a integer) "
        + " as"
        + "     declare variable i integer;"
        + " begin"
        + "    i = 1;"
        + "    while(i < 10000) do begin"
        + "     EXECUTE STATEMENT 'SELECT ' || :i || ' FROM rdb$database' INTO :a;"
        + "     i = i + 1;"
        + "     suspend;"
        + "    end"
        + " end";
    
    public void testCancelStatement() throws Exception {
        Connection conn = getConnectionViaDriverManager();
        try {
            final Statement stmt = conn.createStatement();
            try {
                ResultSet rs = stmt.executeQuery(dummySelect);
                
                boolean hasRecord = rs.next();
                assertTrue("Should fetch at least one record", hasRecord);
                
                Thread cancelThread = new Thread(new Runnable() {
                   public void run() {
                       try {
                           Thread.sleep(20);
                           stmt.cancel();
                           Thread.sleep(100);
                       } catch(SQLException ex) {
                           fail("Cancel operation should work.");
                       } catch(InterruptedException ex) {
                           // empty
                       }
                   } 
                });
                
                cancelThread.start();
                cancelThread.join();

                
                int i = 0;
                try {
                    while(hasRecord) {
                        i = rs.getInt(1);
                        hasRecord = rs.next();
                    }
                    
                    assertEquals("Should not get the record 9999", 9999, i);
                    
                    System.err.println("Should raise an error on one of the records.");
                    fail("Should raise an error on one of the records.");
                } catch(SQLException ex) {
                    System.out.println("testCancelStatement: RS was closed on record " + i);
                    // everything is fine
                } catch(RuntimeException ex) {
                    ex.printStackTrace();
                    throw ex;
                }
                
            } finally {
                stmt.close();
            }
        } finally {
            conn.close();
        }
    }
    
    public void testParameterIsNullQuery() throws Throwable {
        Connection conn = getConnectionViaDriverManager();
        try {
            Statement stmt = conn.createStatement();
            try {
                stmt.executeUpdate("INSERT INTO testtab(id, field1, field2) VALUES (1, '1', 'a')");
                stmt.executeUpdate("INSERT INTO testtab(id, field1, field2) VALUES (2, '2', NULL)");
            } finally {
                stmt.close();
            }
            
            PreparedStatement ps = conn.prepareStatement(
                "SELECT id FROM testtab WHERE field2 = ? OR ? IS NULL ORDER BY 1");
            ResultSet rs;
            try {
                ps.setNull(1, Types.VARCHAR);
                ps.setNull(2, Types.VARCHAR);
                
                rs = ps.executeQuery();
                
                boolean hasRecord = rs.next();
                
                assertTrue("Step 1.1 - should get a record.", hasRecord);
                assertEquals("Step 1.1 - ID should be equal 2", 1, rs.getInt(1));

                hasRecord = rs.next();
                
                assertTrue("Step 1.2 - should get a record.", hasRecord);
                assertEquals("Step 1.2 - ID should be equal 2", 2, rs.getInt(1));

                ps.clearParameters();
                
                ps.setString(1, "a");
                ps.setString(2, "a");
                
                rs = ps.executeQuery();
                
                hasRecord = rs.next();
                assertTrue("Step 2.1 - should get a record.", hasRecord);
                assertEquals("Step 2.1 - ID should be equal 1", 1, rs.getInt(1));

                hasRecord = rs.next();
                assertTrue("Step 2 - should get only one record.", !hasRecord);

            } catch(Throwable ex) {
                ex.printStackTrace();
                throw ex;
            } finally {
                ps.close();
            }
            
        } finally {
            conn.close();
        }
    }
}
