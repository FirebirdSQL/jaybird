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

class FBBlobParamsTest {

    //@formatter:off
    private static final String CREATE_TABLE =
        "CREATE TABLE ClassMap(" + 
        "  oid INTEGER NOT NULL, " + 
        "  className BLOB SUB_TYPE 1, " + 
        "  mapping BLOB SUB_TYPE 1, " +
        "  codebase BLOB SUB_TYPE 1, " + 
        " PRIMARY KEY (oid) " +
        ")";
    //@formatter:on

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll(
            CREATE_TABLE);

    private Connection connection;

    @BeforeEach
    void setUp() throws Exception {
        connection = getConnectionViaDriverManager();
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("delete from ClassMap");
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    /**
     * Test if we can pass string as blob param. This test is not 100% correctly coded, but it tries to reproduce bug
     */
    @Test
    void testParams() throws Exception {
        connection.setAutoCommit(false);

        PreparedStatement ps = connection.prepareStatement(
                "SELECT ClassMap.oid,classname,mapping,codebase FROM ClassMap WHERE classname=?;");

        ps.setObject(1, PreparedStatement.class.getName());

        ResultSet rs = ps.executeQuery();
        assertDoesNotThrow(rs::next);
    }

    /**
     * Test case that reproduces problem when using UPPER function with text Blobs.
     */
    @Test
    void testUpperAndBlobParam() throws Exception {
        connection.setAutoCommit(false);

        try (Statement stmt = connection.createStatement()) {
            stmt.execute("INSERT INTO ClassMap(oid, classname) VALUES (1, 'test')");
        }

        connection.commit();

        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT oid FROM ClassMap WHERE UPPER(classname) LIKE ?")) {
            ps.setString(1, "TEST");

            ResultSet rs = ps.executeQuery();
            assertTrue(rs.next(), "Should find at least one row");
            assertEquals("1", rs.getString(1), "OID value should be correct");
            assertFalse(rs.next(), "Only one row should be selected");
        }
    }

    /**
     * Test case that reproduces problem when using equal sign with text Blobs.
     */
    @Test
    void testEqualityInBlobParam() throws Exception {
        connection.setAutoCommit(false);

        try (Statement stmt = connection.createStatement()) {
            stmt.execute("INSERT INTO ClassMap(oid, classname) VALUES (1, 'test')");
        }

        connection.commit();

        try (PreparedStatement ps = connection.prepareStatement("SELECT oid FROM ClassMap WHERE classname = ?")) {
            ps.setString(1, "test");

            ResultSet rs = ps.executeQuery();
            assertTrue(rs.next(), "Should find at least one row");
            assertEquals("1", rs.getString(1), "OID value should be correct");
            assertFalse(rs.next(), "Only one row should be selected");
        }
    }

}
