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
package org.firebirdsql.jdbc;

import org.firebirdsql.common.rules.UsesDatabase;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.sql.*;
import java.util.Properties;

import static org.firebirdsql.common.DdlHelper.executeCreateTable;
import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.message;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

/**
 * Tests for savepoint handling.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class TestFBSavepoint {

    @ClassRule
    public static final UsesDatabase usesDatabase = UsesDatabase.noDatabase();

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private Connection connection;

    @BeforeClass
    public static void initDatabase() throws Exception {
        assumeTrue("Test requires SAVEPOINT support", getDefaultSupportInfo().supportsSavepoint());
        usesDatabase.createDefaultDatabase();

        try (Connection connection = getConnectionViaDriverManager()) {
            executeCreateTable(connection, "CREATE TABLE test_svpt(id INTEGER)");
        }
    }

    @Before
    public void setUp() throws Exception {
        connection = getConnectionViaDriverManager();
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("delete from test_svpt");
        }
        // Most tests require auto commit disabled
        connection.setAutoCommit(false);
    }

    @After
    public void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void testSavePointCreationNotAllowedInAutoCommit() throws Exception {
        connection.setAutoCommit(true);

        expectedException.expect(allOf(
                isA(SQLException.class),
                message(equalTo("Connection.setSavepoint() method cannot be used in auto-commit mode."))));

        connection.setSavepoint();
    }

    @Test
    public void testGetSavePointIdOnUnnamedSavePoint() throws Exception {
        Savepoint savepoint1 = connection.setSavepoint();
        Savepoint savepoint2 = connection.setSavepoint();

        assertEquals(0, savepoint1.getSavepointId());
        assertEquals(1, savepoint2.getSavepointId());
    }

    @Test
    public void getSavePointNameOnUnnamedSavePointThrowsException() throws Exception {
        Savepoint savepoint = connection.setSavepoint();

        expectedException.expect(allOf(
                isA(SQLException.class),
                message(equalTo("Savepoint is unnamed."))));

        savepoint.getSavepointName();
    }

    /**
     * Test if basic savepoint handling works.
     */
    @Test
    public void testBasicSavepoint() throws Exception {
        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO test_svpt VALUES(?)")) {
            stmt.setInt(1, 1);
            stmt.execute();

            Savepoint svpt1 = connection.setSavepoint();

            stmt.setInt(1, 2);
            stmt.execute();

            checkRowCount(2);

            connection.rollback(svpt1);

            checkRowCount(1);

            connection.commit();

            checkRowCount(1);
        }
    }

    @Test
    public void getSavePointNameOnNamedSavePoint() throws Exception {
        final String name = "named";
        Savepoint savepoint = connection.setSavepoint(name);

        assertEquals(name, savepoint.getSavepointName());
    }

    @Test
    public void getSavePointIdOnNamedSavePointThrowsException() throws Exception {
        Savepoint savepoint = connection.setSavepoint("named");

        expectedException.expect(allOf(
                isA(SQLException.class),
                message(equalTo("Savepoint is named."))));

        savepoint.getSavepointId();
    }

    /**
     * Test if named savepoint handling works.
     */
    @Test
    public void testNamedSavepoint() throws Exception {
        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO test_svpt VALUES(?)")) {
            stmt.setInt(1, 1);
            stmt.execute();

            Savepoint svpt1 = connection.setSavepoint("test");

            stmt.setInt(1, 2);
            stmt.execute();

            checkRowCount(2);

            Savepoint svpt2 = connection.setSavepoint("test");

            assertEquals("Savepoints should be equal.", svpt1, svpt2);

            stmt.setInt(1, 3);
            stmt.execute();

            checkRowCount(3);

            connection.rollback(svpt1);

            checkRowCount(2);

            connection.commit();

            checkRowCount(2);
        }
    }

    /**
     * Test if savepoints are released correctly.
     */
    @Test
    public void testSavepointRelease() throws Exception {
        Savepoint svpt1 = connection.setSavepoint("test");

        connection.releaseSavepoint(svpt1);

        checkInvalidSavepoint(svpt1);

        Savepoint svpt2 = connection.setSavepoint();

        connection.releaseSavepoint(svpt2);

        checkInvalidSavepoint(svpt2);

        Savepoint svpt3 = connection.setSavepoint();

        connection.commit();

        checkInvalidSavepoint(svpt3);
    }

    @Test
    public void testNamedSavePointInDialect1() throws Exception {
        connection.close();
        Properties info = getDefaultPropertiesForConnection();
        info.setProperty("sql_dialect", "1");
        connection = DriverManager.getConnection(getUrl(), info);
        connection.setAutoCommit(false);

        connection.setSavepoint("named");
    }

    @Test
    public void testNamedSavePointRequiresQuoting() throws Exception {
        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO test_svpt VALUES(?)")) {
            stmt.setInt(1, 1);
            stmt.execute();

            Savepoint svpt1 = connection.setSavepoint("test\" 1");

            stmt.setInt(1, 2);
            stmt.execute();

            checkRowCount(2);

            Savepoint svpt2 = connection.setSavepoint("test\" 1");

            assertEquals("Savepoints should be equal.", svpt1, svpt2);

            stmt.setInt(1, 3);
            stmt.execute();

            checkRowCount(3);

            connection.rollback(svpt1);

            checkRowCount(2);

            connection.commit();

            checkRowCount(2);
        }
    }

    private void checkInvalidSavepoint(Savepoint savepoint) {
        try {
            connection.rollback(savepoint);
            fail("Released savepoint should not work.");
        } catch (SQLException ex) {
            // everything is fine
        }

        try {
            connection.releaseSavepoint(savepoint);
            fail("Released savepoint should not work.");
        } catch (SQLException ex) {
            // everything is fine
        }
    }

    /**
     * Check if table contains correct number of rows.
     *
     * @param testRowCount
     *         expected number of rows.
     * @throws SQLException
     *         if something went wrong when counting rows.
     */
    private void checkRowCount(int testRowCount) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT count(*) FROM test_svpt");

            int count = rs.next() ? rs.getInt(1) : 0;

            assertEquals("Incorrect result set, expecting " + testRowCount +
                    " rows, obtained " + count + ".", testRowCount, count);

        }
    }
}
