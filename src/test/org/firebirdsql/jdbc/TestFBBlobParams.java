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
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.firebirdsql.common.DdlHelper.executeCreateTable;
import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.junit.Assert.*;

public class TestFBBlobParams extends FBJUnit4TestBase {
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

    @Before
    public void setUp() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            executeCreateTable(connection, CREATE_TABLE);
        }
    }

    /**
     * Test if we can pass string as blob param. This test is not 100% correctly coded, but it tries to reproduce bug
     */
    @Test
    public void testParams() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            connection.setAutoCommit(false);

            PreparedStatement ps = connection.prepareStatement(
                    "SELECT ClassMap.oid,classname,mapping,codebase FROM ClassMap WHERE classname=?;");

            ps.setObject(1, PreparedStatement.class.getName());

            ResultSet rs = ps.executeQuery();
            rs.next();
        }
    }

    /**
     * Test case that reproduces problem when using UPPER function with text Blobs.
     */
    @Test
    public void testUpperAndBlobParam() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            connection.setAutoCommit(false);

            try (Statement stmt = connection.createStatement()) {
                stmt.execute("INSERT INTO ClassMap(oid, classname) VALUES (1, 'test')");
            }

            connection.commit();

            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT oid FROM ClassMap WHERE UPPER(classname) LIKE ?")) {
                ps.setString(1, "TEST");

                ResultSet rs = ps.executeQuery();
                assertTrue("Should find at least one row.", rs.next());
                assertEquals("OID value should be correct.", "1", rs.getString(1));
                assertFalse("Only one row should be selected", rs.next());
            }
        }
    }

    /**
     * Test case that reproduces problem when using equal sign with text Blobs.
     */
    @Test
    public void testEqualityInBlobParam() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            connection.setAutoCommit(false);

            try (Statement stmt = connection.createStatement()) {
                stmt.execute("INSERT INTO ClassMap(oid, classname) VALUES (1, 'test')");
            }

            connection.commit();

            try (PreparedStatement ps = connection.prepareStatement("SELECT oid FROM ClassMap WHERE classname = ?")) {
                ps.setString(1, "test");

                ResultSet rs = ps.executeQuery();
                assertTrue("Should find at least one row.", rs.next());
                assertEquals("OID value should be correct.", "1", rs.getString(1));
                assertFalse("Only one row should be selected", rs.next());
            }
        }
    }

}
