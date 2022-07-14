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

import org.firebirdsql.common.DataGenerator;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.junit.jupiter.api.Assertions.*;

class FBBlobAccessTest {
    
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

    private static final int TEST_ROW_COUNT = 10;

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll(
            CREATE_TABLE,
            CREATE_VIEW);

    private Connection connection;

    private byte[][] testData;

    @BeforeEach
    void setUp() throws Exception {
        connection = getConnectionViaDriverManager();
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("delete from test_blob");
        }

        testData = new byte[TEST_ROW_COUNT][0];

        for (int i = 0; i < testData.length; i++) {
            int testLength = DataGenerator.generateRandom(10, 110);
            testData[i] = DataGenerator.createRandomBytes(testLength);
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    /**
     * Test if byte[] are correctly stored and retrieved from database
     */
    @Test
    void testFieldTypes() throws Exception {
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO test_blob(id, bin_data) VALUES (?, ?)")) {
            for (int i = 0; i < TEST_ROW_COUNT; i++) {
                ps.setInt(1, i);
                ps.setBytes(2, testData[i]);

                ps.execute();
            }
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, bin_data FROM test_blob")) {
            int counter = 0;

            while (rs.next()) {
                int id = rs.getInt("id");
                byte[] data = rs.getBytes("bin_data");

                assertArrayEquals(testData[id], data,
                        () -> "Data read from database for id " + id + " should be equal to generated one");
                counter++;
            }

            assertEquals(TEST_ROW_COUNT, counter, "Should read " + TEST_ROW_COUNT + " rows, read " + counter);
        }
    }

    /**
     * This method checks if we correctly handle assigning null values to blobs.
     */
    @Test
    void testSetNull() throws Exception {
        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO test_blob(id, bin_data) VALUES(?, ?)")) {
            stmt.setInt(1, 1);
            stmt.setBytes(2, null);

            stmt.execute();

            stmt.setInt(1, 2);
            stmt.setBinaryStream(2, null, 0);

            stmt.execute();

            stmt.setInt(1, 3);
            stmt.setString(2, null);

            stmt.execute();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("select bin_data from test_blob")) {
            int counter = 0;
            while (rs.next()) {
                counter++;
                assertNull(rs.getBytes(1), "Expected null value for bin_data");
            }
            assertEquals(3, counter, "expected three rows");
        }
    }

    private static final String TEST_TEXT = "Test text";

    /**
     * This test initially was created to find the bug when LONGVARCHAR column
     * is used, but it revealed the bug with blobs.
     * <p>
     * We try to execute some blob updates one by one in non-autocommit mode.
     * This tests if transaction is started before the blob is created.
     */
    @Test
    void testSetString() throws Exception {
        connection.setAutoCommit(false);

        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO test_blob(id, bin_data) VALUES(?, ?)")) {
            stmt.setInt(1, 1);
            stmt.setString(2, TEST_TEXT);

            stmt.execute();

            connection.commit();

            stmt.setInt(1, 2);
            stmt.setString(2, TEST_TEXT);

            stmt.execute();

            connection.commit();

            stmt.setInt(1, 3);
            stmt.setString(2, TEST_TEXT);

            stmt.execute();

            connection.commit();
        }

        connection.setAutoCommit(true);

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("select bin_data from test_blob")) {
            int counter = 0;
            while (rs.next()) {
                counter++;
                assertEquals(TEST_TEXT, rs.getString(1), "Unexpected value for bin_data");
            }
            assertEquals(3, counter, "expected three rows");
        }
    }

    /**
     * Test if blobs are correctly accessed via views.
     */
    @Test
    void testViewAccess() throws Exception {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO test_blob_view(id, bin_data) VALUES (?, ?)")) {
            for (int i = 0; i < TEST_ROW_COUNT; i++) {
                ps.setInt(1, i);
                ps.setBytes(2, testData[i]);

                ps.execute();
            }
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, bin_data FROM test_blob_view")) {
            int counter = 0;

            while (rs.next()) {
                int id = rs.getInt("id");
                byte[] data = rs.getBytes("bin_data");

                assertArrayEquals(testData[id], data,
                        () -> "Data read from database for id " + id + " should be equal to generated one");

                counter++;
            }

            assertEquals(TEST_ROW_COUNT, counter, "Should read " + TEST_ROW_COUNT + " rows, read " + counter);
        }
    }

}
