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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Random;

import static org.firebirdsql.common.DdlHelper.executeCreateTable;
import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.junit.Assert.*;

public class TestFBBlobAccess extends FBJUnit4TestBase {
    //@formatter:off
    private static final String CREATE_TABLE =
            "CREATE TABLE test_blob(" +
            "  id INTEGER, " +
            "  bin_data BLOB " +
            ")";

    private static final String CREATE_VIEW =
            "CREATE VIEW test_blob_view (id, bin_data) AS " +
            "  SELECT id, bin_data FROM test_blob";
    //@formatter:on

    private static int TEST_ROW_COUNT = 10;

    private Connection connection;

    private byte[][] testData;

    @Before
    public void setUp() throws Exception {
        connection = getConnectionViaDriverManager();

        executeCreateTable(connection, CREATE_TABLE);
        executeCreateTable(connection, CREATE_VIEW);

        Random rnd = new Random();

        testData = new byte[TEST_ROW_COUNT][0];

        for (int i = 0; i < testData.length; i++) {
            int testLength = rnd.nextInt(100) + 10;
            testData[i] = new byte[testLength];
            rnd.nextBytes(testData[i]);
        }
    }

    @After
    public void tearDown() throws Exception {
        connection.close();
    }

    /**
     * Test if byte[] are correctly stored and retrieved from database
     */
    @Test
    public void testFieldTypes() throws Exception {
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO test_blob(id, bin_data) VALUES (?, ?)")) {

            for (int i = 0; i < TEST_ROW_COUNT; i++) {
                ps.setInt(1, i);
                ps.setBytes(2, testData[i]);

                ps.executeUpdate();
            }
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, bin_data FROM test_blob")) {
            int counter = 0;

            while (rs.next()) {
                int id = rs.getInt("id");
                byte[] data = rs.getBytes("bin_data");

                assertArrayEquals("Data read from database for id " + id + " should be equal to generated one.",
                        testData[id], data);
                counter++;
            }

            assertEquals("Should read " + TEST_ROW_COUNT + " rows, read " + counter, TEST_ROW_COUNT, counter);
        }
    }

    /**
     * This method checks if we correctly handle assigning null values to blobs.
     */
    @Test
    public void testSetNull() throws Exception {
        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO test_blob(id, bin_data) VALUES(?, ?)")) {
            stmt.setInt(1, 1);
            stmt.setBytes(2, null);

            stmt.executeUpdate();

            stmt.setInt(1, 2);
            stmt.setBinaryStream(2, null, 0);

            stmt.executeUpdate();

            stmt.setInt(1, 3);
            stmt.setString(2, null);

            stmt.executeUpdate();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("select bin_data from test_blob")) {
            int counter = 0;
            while (rs.next()) {
                counter++;
                assertNull("Expected null value for bin_data", rs.getBytes(1));
            }
            assertEquals("expected three rows", 3, counter);
        }
    }

    public static final String TEST_TEXT = "Test text";

    /**
     * This test initially was created to find the bug when LONGVARCHAR column
     * is used, but it revealed the bug with blobs.
     * <p>
     * We try to execute some blob updates one by one in non-autocommit mode.
     * This tests if transaction is started before the blob is created.
     */
    @Test
    public void testSetString() throws Exception {
        connection.setAutoCommit(false);

        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO test_blob(id, bin_data) VALUES(?, ?)")) {
            stmt.setInt(1, 1);
            stmt.setString(2, TEST_TEXT);

            stmt.executeUpdate();

            connection.commit();

            stmt.setInt(1, 2);
            stmt.setString(2, TEST_TEXT);

            stmt.executeUpdate();

            connection.commit();

            stmt.setInt(1, 3);
            stmt.setString(2, TEST_TEXT);

            stmt.executeUpdate();

            connection.commit();
        }

        connection.setAutoCommit(true);

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("select bin_data from test_blob")) {
            int counter = 0;
            while (rs.next()) {
                counter++;
                assertEquals("Unexpected value for bin_data", TEST_TEXT, rs.getString(1));
            }
            assertEquals("expected three rows", 3, counter);
        }
    }

    /**
     * Test if blobs are correctly accessed via views.
     */
    @Test
    public void testViewAccess() throws Exception {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO test_blob_view(id, bin_data) VALUES (?, ?)")) {
            for (int i = 0; i < TEST_ROW_COUNT; i++) {
                ps.setInt(1, i);
                ps.setBytes(2, testData[i]);

                ps.executeUpdate();
            }
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, bin_data FROM test_blob_view")) {
            int counter = 0;

            while (rs.next()) {
                int id = rs.getInt("id");
                byte[] data = rs.getBytes("bin_data");

                assertArrayEquals("Data read from database for id " + id + " should be equal to generated one.",
                        testData[id], data);

                counter++;
            }

            assertEquals("Should read " + TEST_ROW_COUNT + " rows, read " + counter, TEST_ROW_COUNT, counter);
        }
    }

}
