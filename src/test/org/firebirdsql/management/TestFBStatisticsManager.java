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
package org.firebirdsql.management;

import org.firebirdsql.common.FBJUnit4TestBase;
import org.firebirdsql.gds.impl.GDSType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.junit.Assert.*;

/**
 * Test the FBStatisticsManager class
 */
public class TestFBStatisticsManager extends FBJUnit4TestBase {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private FBStatisticsManager statManager;

    private OutputStream loggingStream;

    public static final String DEFAULT_TABLE = ""
        + "CREATE TABLE TEST ("
        + "     TESTVAL INTEGER NOT NULL"
        + ")";

    public TestFBStatisticsManager() throws ClassNotFoundException {
        Class.forName("org.firebirdsql.jdbc.FBDriver");
    }

    @Before
    public void setUp() throws Exception {
        loggingStream = new ByteArrayOutputStream();
    
        statManager = new FBStatisticsManager(getGdsType());
        if (getGdsType() == GDSType.getType("PURE_JAVA") || getGdsType() == GDSType.getType("NATIVE")) {
            statManager.setHost(DB_SERVER_URL);
            statManager.setPort(DB_SERVER_PORT);
        }
        statManager.setUser(DB_USER);
        statManager.setPassword(DB_PASSWORD);
        statManager.setDatabase(getDatabasePath());
        statManager.setLogger(loggingStream);
    }

    private void createTestTable() throws SQLException {
        createTestTable(DEFAULT_TABLE);
    }

    private void createTestTable(String tableDef) throws SQLException {
        try (Connection conn = getConnectionViaDriverManager()) {
            Statement stmt = conn.createStatement();
            stmt.execute(tableDef);
        }
    }

    @Test
    public void testGetHeaderPage() throws SQLException {
        statManager.getHeaderPage();
        String headerPage = loggingStream.toString();

        // Not a lot more we can really do to ensure that it's a real
        // header page, unfortunately :(
        assertTrue("The header page must include 'Database header page information'",
                headerPage.contains("Database header page information"));

        assertFalse("The statistics must not include data table info",
                headerPage.contains("Data pages"));
    }

    @Test
    public void testGetDatabaseStatistics() throws SQLException {
        
        createTestTable();
        statManager.getDatabaseStatistics();
        String statistics = loggingStream.toString();

        assertTrue("The database page analysis must be in the statistics",
                statistics.contains("Data pages"));

        assertFalse("System table information must not be in basic statistics",
                statistics.contains("RDB$DATABASE"));
    }

    @Test
    public void testGetStatsWithBadOptions() throws SQLException {
        try {
            statManager.getDatabaseStatistics(
                    (StatisticsManager.DATA_TABLE_STATISTICS
                     | StatisticsManager.SYSTEM_TABLE_STATISTICS
                     | StatisticsManager.INDEX_STATISTICS) * 2);
            fail("Options to getDatabaseStatistics must be a combination "
                    + "of DATA_TABLE_STATISTICS, SYSTEM_TABLE_STATISTICS "
                    + "and INDEX_STATISTICS, or 0");
        } catch (IllegalArgumentException e){
            // Ignore
        }
    }

    @Test
    public void testGetSystemStats() throws SQLException {
        statManager.getDatabaseStatistics(
                StatisticsManager.SYSTEM_TABLE_STATISTICS);
        String statistics = loggingStream.toString();
        assertTrue("Statistics with SYSTEM_TABLE_STATISTICS option must "
                    + "include system table info",
                statistics.contains("RDB$DATABASE"));
    }

    @Test
    public void testGetTableStatistics() throws SQLException {
        createTestTable();
        statManager.getTableStatistics(new String[]{"TEST"});
        String statistics = loggingStream.toString();

        System.out.println(statistics);
        
        assertTrue("The database page analysis must be in the statistics",
                statistics.contains("Data pages"));

        assertTrue("The table name must be in the statistics",
                statistics.contains("TEST"));
    }

    @Test
    public void testGetDatabaseTransactionInfo_usingServiceConfig() throws SQLException {
        createTestTable();
        try (Connection conn = getConnectionViaDriverManager()) {
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement();
            stmt.execute("select * from rdb$database");
            FBStatisticsManager.DatabaseTransactionInfo databaseTransactionInfo =
                    statManager.getDatabaseTransactionInfo();
            // The transaction values checked here might be implementation dependent
            assertEquals("oldest", 1, databaseTransactionInfo.getOldestTransaction());
            assertEquals("oldest active", 2, databaseTransactionInfo.getOldestActiveTransaction());
            assertEquals("oldest snapshot", 2, databaseTransactionInfo.getOldestSnapshotTransaction());
            assertEquals("next", 2, databaseTransactionInfo.getNextTransaction());
            assertEquals("active", 1, databaseTransactionInfo.getActiveTransactionCount());
        }
    }

    @Test
    public void testGetDatabaseTransactionInfo_noDatabaseNameSpecified() throws SQLException {
        statManager.setDatabase(null);
        expectedException.expect(SQLException.class);
        statManager.getDatabaseTransactionInfo();
    }

    @Test
    public void testGetDatabaseTransactionInfo_usingConnection() throws SQLException {
        createTestTable();
        try (Connection conn = getConnectionViaDriverManager()) {
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement();
            stmt.execute("select * from rdb$database");

            FBStatisticsManager.DatabaseTransactionInfo databaseTransactionInfo =
                    FBStatisticsManager.getDatabaseTransactionInfo(conn);
            // The transaction values checked here might be implementation dependent
            assertEquals("oldest", 1, databaseTransactionInfo.getOldestTransaction());
            assertEquals("oldest active", 2, databaseTransactionInfo.getOldestActiveTransaction());
            assertEquals("oldest snapshot", 2, databaseTransactionInfo.getOldestSnapshotTransaction());
            assertEquals("next", 2, databaseTransactionInfo.getNextTransaction());
            assertEquals("active", 1, databaseTransactionInfo.getActiveTransactionCount());
        }
    }
}
