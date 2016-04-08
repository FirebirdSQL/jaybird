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

import org.firebirdsql.common.FBJUnit4TestBase;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.impl.GDSType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.*;
import java.util.Properties;

import static org.firebirdsql.common.DdlHelper.executeCreateTable;
import static org.firebirdsql.common.FBTestProperties.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for updatable result sets with a dialect 1 database.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.2
 */
public class TestResultSetDialect1 extends FBJUnit4TestBase {

    //@formatter:off
    public static final String CREATE_TABLE_STATEMENT =
            "CREATE TABLE test_table(" +
            "  id INTEGER NOT NULL PRIMARY KEY, " +
            "  str VARCHAR(10) " +
            ")";

    public static final String SELECT_TEST_TABLE = "SELECT id, str FROM test_table";

    public static final String INSERT_INTO_TABLE_STATEMENT = "INSERT INTO test_table (id, str) VALUES(?, ?)";
    //@formatter:on

    private Connection connection;

    @Before
    @Override
    public void basicSetUp() throws Exception {
        fbManager = createFBManager();

        if (getGdsType() == GDSType.getType("PURE_JAVA")
                || getGdsType() == GDSType.getType("NATIVE")) {
            fbManager.setServer(DB_SERVER_URL);
            fbManager.setPort(DB_SERVER_PORT);
        }
        fbManager.setDialect(ISCConstants.SQL_DIALECT_V5);
        fbManager.start();
        fbManager.setForceCreate(true);
        fbManager.createDatabase(getDatabasePath(), DB_USER, DB_PASSWORD);

        final Properties properties = getDefaultPropertiesForConnection();
        properties.setProperty("sql_dialect", String.valueOf(ISCConstants.SQL_DIALECT_V5));
        connection = DriverManager.getConnection(getUrl(), properties);

        executeCreateTable(connection, CREATE_TABLE_STATEMENT);
    }

    @After
    @Override
    public void basicTearDown() throws Exception {
        try {
            connection.close();
        } finally {
            fbManager.dropDatabase(getDatabasePath(), DB_USER, DB_PASSWORD);
            fbManager.stop();
            fbManager = null;
        }
    }

    /**
     * Tests whether updating a row in dialect 1 works
     * <p>
     * Rationale: {@link org.firebirdsql.jdbc.FBRowUpdater} quotes object names in dialect 3, but this shouldn't
     * happen in dialect 1.
     * </p>
     */
    @Test
    public void testUpdateRow() throws Exception {
        createTestData(1);

        try (Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            try (ResultSet rs1 = stmt.executeQuery(SELECT_TEST_TABLE)) {
                assertTrue("Expected a row", rs1.next());
                rs1.updateString(2, "newString1");
                rs1.updateRow();
            }

            try (ResultSet rs2 = stmt.executeQuery(SELECT_TEST_TABLE)) {
                assertTrue("Expected a row", rs2.next());
                assertEquals("newString1", rs2.getString(2));
            }
        }
    }

    /**
     * Tests whether inserting a row in dialect 1 works
     * <p>
     * Rationale: {@link org.firebirdsql.jdbc.FBRowUpdater} quotes object names in dialect 3, but this shouldn't
     * happen in dialect 1.
     * </p>
     */
    @Test
    public void testInsertRow() throws Exception {
        createTestData(1);

        try (Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            try (ResultSet rs1 = stmt.executeQuery(SELECT_TEST_TABLE)) {
                rs1.moveToInsertRow();
                rs1.updateInt(1, 2);
                rs1.updateString(2, "newString2");
                rs1.insertRow();
            }

            try (ResultSet rs2 = stmt.executeQuery(SELECT_TEST_TABLE + " WHERE id = 2")) {
                assertTrue("Expected a row", rs2.next());
                assertEquals("newString2", rs2.getString(2));
            }
        }
    }

    /**
     * Tests whether deleting a row in dialect 1 works
     * <p>
     * Rationale: {@link org.firebirdsql.jdbc.FBRowUpdater} quotes object names in dialect 3, but this shouldn't
     * happen in dialect 1.
     * </p>
     */
    @Test
    public void testDeleteRow() throws Exception {
        createTestData(1);

        try (Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            try (ResultSet rs1 = stmt.executeQuery(SELECT_TEST_TABLE)) {
                assertTrue("Expected a row", rs1.next());
                rs1.deleteRow();
            }

            try (ResultSet rs2 = stmt.executeQuery(SELECT_TEST_TABLE)) {
                assertFalse("Expected no row", rs2.next());
            }
        }
    }

    private void createTestData(int recordCount) throws Exception {
        connection.setAutoCommit(false);
        try (PreparedStatement ps = connection.prepareStatement(INSERT_INTO_TABLE_STATEMENT)) {
            for (int i = 0; i < recordCount; i++) {
                ps.setInt(1, i);
                ps.setString(2, "oldString" + i);
                ps.executeUpdate();
            }
        }
        connection.setAutoCommit(true);
    }
}
