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
package org.firebirdsql.jdbc;

import org.firebirdsql.common.extension.RequireFeatureExtension;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.*;
import java.util.Properties;

import static java.lang.String.format;
import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.message;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for savepoint handling.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
class FBSavepointTest {

    @RegisterExtension
    @Order(1)
    static final RequireFeatureExtension requireFeature = RequireFeatureExtension
            .withFeatureCheck(FirebirdSupportInfo::supportsSavepoint, "Test requires SAVEPOINT support")
            .build();

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll(
            "CREATE TABLE test_svpt(id INTEGER)");

    private Connection connection;

    @BeforeEach
    void setUp() throws Exception {
        connection = getConnectionViaDriverManager();
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("delete from test_svpt");
        }
        // Most tests require auto commit disabled
        connection.setAutoCommit(false);
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    @Test
    void testSavePointCreationNotAllowedInAutoCommit() throws Exception {
        connection.setAutoCommit(true);

        SQLException exception = assertThrows(SQLException.class, connection::setSavepoint);
        assertThat(exception, message(equalTo("Connection.setSavepoint() method cannot be used in auto-commit mode.")));
    }

    @Test
    void testGetSavePointIdOnUnnamedSavePoint() throws Exception {
        Savepoint savepoint1 = connection.setSavepoint();
        Savepoint savepoint2 = connection.setSavepoint();

        assertEquals(0, savepoint1.getSavepointId());
        assertEquals(1, savepoint2.getSavepointId());
    }

    @Test
    void getSavePointNameOnUnnamedSavePointThrowsException() throws Exception {
        Savepoint savepoint = connection.setSavepoint();

        SQLException exception = assertThrows(SQLException.class, savepoint::getSavepointName);
        assertThat(exception, message(equalTo("Savepoint is unnamed.")));
    }

    /**
     * Test if basic savepoint handling works.
     */
    @Test
    void testBasicSavepoint() throws Exception {
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
    void getSavePointNameOnNamedSavePoint() throws Exception {
        final String name = "named";
        Savepoint savepoint = connection.setSavepoint(name);

        assertEquals(name, savepoint.getSavepointName());
    }

    @Test
    void getSavePointIdOnNamedSavePointThrowsException() throws Exception {
        Savepoint savepoint = connection.setSavepoint("named");

        SQLException exception = assertThrows(SQLException.class, savepoint::getSavepointId);
        assertThat(exception, message(equalTo("Savepoint is named.")));
    }

    /**
     * Test if named savepoint handling works.
     */
    @Test
    void testNamedSavepoint() throws Exception {
        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO test_svpt VALUES(?)")) {
            stmt.setInt(1, 1);
            stmt.execute();

            Savepoint svpt1 = connection.setSavepoint("test");

            stmt.setInt(1, 2);
            stmt.execute();

            checkRowCount(2);

            Savepoint svpt2 = connection.setSavepoint("test");

            assertEquals(svpt1, svpt2, "Savepoints should be equal.");

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
    void testSavepointRelease() throws Exception {
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
    void testNamedSavePointInDialect1() throws Exception {
        connection.close();
        Properties info = getDefaultPropertiesForConnection();
        info.setProperty("sql_dialect", "1");
        connection = DriverManager.getConnection(getUrl(), info);
        connection.setAutoCommit(false);

        connection.setSavepoint("named");
    }

    @Test
    void testNamedSavePointRequiresQuoting() throws Exception {
        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO test_svpt VALUES(?)")) {
            stmt.setInt(1, 1);
            stmt.execute();

            Savepoint svpt1 = connection.setSavepoint("test\" 1");

            stmt.setInt(1, 2);
            stmt.execute();

            checkRowCount(2);

            Savepoint svpt2 = connection.setSavepoint("test\" 1");

            assertEquals(svpt1, svpt2, "Savepoints should be equal");

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
        assertThrows(SQLException.class, () -> connection.rollback(savepoint), "Rollback of savepoint should not work");
        assertThrows(SQLException.class, () -> connection.releaseSavepoint(savepoint),
                "Release of savepoint should not work");
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

            assertEquals(testRowCount, count,
                    () -> format("Incorrect result set, expecting %d rows, obtained %d.", testRowCount, count));
        }
    }
}
