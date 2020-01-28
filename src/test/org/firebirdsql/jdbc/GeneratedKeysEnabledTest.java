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

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.rules.UsesDatabase;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.*;
import java.util.Properties;

import static org.firebirdsql.common.matchers.SQLExceptionMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/**
 * Tests configuration property {@code generatedKeysEnabled}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class GeneratedKeysEnabledTest {

    @ClassRule
    public static final UsesDatabase usesDatabase = UsesDatabase.usesDatabase(
            "CREATE TABLE TABLE_WITH_TRIGGER (\n"
                    + " ID Integer NOT NULL,\n"
                    + " TEXT Varchar(200),\n"
                    + " CONSTRAINT PK_TABLE_WITH_TRIGGER_1 PRIMARY KEY (ID)\n"
                    + ")",
            "CREATE GENERATOR GEN_TABLE_WITH_TRIGGER_ID",
            "CREATE TRIGGER TABLE_WITH_TRIGGER_BI FOR TABLE_WITH_TRIGGER ACTIVE\n" +
                    "BEFORE INSERT POSITION 0\n" +
                    "AS\n" +
                    "DECLARE VARIABLE tmp DECIMAL(18,0);\n" +
                    "BEGIN\n" +
                    "  IF (NEW.ID IS NULL) THEN\n" +
                    "    NEW.ID = GEN_ID(GEN_TABLE_WITH_TRIGGER_ID, 1);\n" +
                    "  ELSE\n" +
                    "  BEGIN\n" +
                    "    tmp = GEN_ID(GEN_TABLE_WITH_TRIGGER_ID, 0);\n" +
                    "    if (tmp < new.ID) then\n" +
                    "      tmp = GEN_ID(GEN_TABLE_WITH_TRIGGER_ID, new.ID-tmp);\n" +
                    "  END\n" +
                    "END"
    );

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();
    
    private static final String TEST_INSERT_QUERY = "INSERT INTO TABLE_WITH_TRIGGER(TEXT) VALUES (?)";
    private static final String TEST_UPDATE_QUERY = "UPDATE TABLE_WITH_TRIGGER SET TEXT = ? WHERE ID = ?";

    @BeforeClass
    public static void checkReturningSupport() {
        assumeTrue("test requires INSERT .. RETURNING .. support",
                FBTestProperties.getDefaultSupportInfo().supportsInsertReturning());
    }

    @Test
    public void test_generatedKeysEnabled_notSpecified_insertWorks() throws SQLException {
        Properties props = FBTestProperties.getDefaultPropertiesForConnection();
        try (Connection connection = DriverManager.getConnection(FBTestProperties.getUrl(), props);
             PreparedStatement pstmt = connection.prepareStatement(TEST_INSERT_QUERY, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, "value");
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                assertTrue("expected a row", rs.next());
                assertThat("non-zero id", rs.getInt("id"), greaterThan(0));
            }
        }
    }

    @Test
    public void test_generatedKeysEnabled_default_insertWorks() throws SQLException {
        try (Connection connection = getConnection("default");
             PreparedStatement pstmt = connection.prepareStatement(TEST_INSERT_QUERY, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, "value");
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                assertTrue("expected a row", rs.next());
                assertThat("non-zero id", rs.getInt("id"), greaterThan(0));
            }
        }
    }

    @Test
    public void test_generatedKeysEnabled_disabled_throwsFeatureNotSupported() throws SQLException {
        try (Connection connection = getConnection("disabled")) {
            expectedException.expect(allOf(
                    isA(SQLFeatureNotSupportedException.class),
                    errorCodeEquals(JaybirdErrorCodes.jb_generatedKeysSupportNotAvailable),
                    fbMessageStartsWith(JaybirdErrorCodes.jb_generatedKeysSupportNotAvailable, GeneratedKeysSupportFactory.REASON_EXPLICITLY_DISABLED)
            ));
            connection.prepareStatement(TEST_INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);
        }
    }

    @Test
    public void test_generatedKeysEnabled_ignored_insert_noGeneratedKeys() throws SQLException {
        try (Connection connection = getConnection("ignored");
             PreparedStatement pstmt = connection.prepareStatement(TEST_INSERT_QUERY, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, "value");
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                assertFalse("expected no row", rs.next());
            }
        }
    }

    @Test
    public void test_generatedKeysEnabled_insert_insertWorks_updateNoGeneratedKeys() throws SQLException {
        try (Connection connection = getConnection("insert")) {
            int insertedId;
            try (PreparedStatement pstmt = connection.prepareStatement(TEST_INSERT_QUERY, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, "value");
                pstmt.executeUpdate();
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    assertTrue("expected a row for insert", rs.next());
                    insertedId = rs.getInt("id");
                    assertThat("non-zero id", insertedId, greaterThan(0));
                }
            }

            try (PreparedStatement pstmt = connection.prepareStatement(TEST_UPDATE_QUERY, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, "newvalue");
                pstmt.setInt(2, insertedId);
                pstmt.executeUpdate();

                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    assertFalse("expected no row for update", rs.next());
                }
            }
        }
    }

    @Test
    public void test_generatedKeysEnabled_insert_update_insertWorks_updateWorks() throws SQLException {
        assumeTrue("test requires UPDATE .. RETURNING .. support",
                FBTestProperties.getDefaultSupportInfo().supportsUpdateReturning());

        try (Connection connection = getConnection("insert,update")) {
            int insertedId;
            try (PreparedStatement pstmt = connection.prepareStatement(TEST_INSERT_QUERY, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, "value");
                pstmt.executeUpdate();
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    assertTrue("expected a row for insert", rs.next());
                    insertedId = rs.getInt("id");
                    assertThat("non-zero id", insertedId, greaterThan(0));
                }
            }

            try (PreparedStatement pstmt = connection.prepareStatement(TEST_UPDATE_QUERY, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, "newvalue");
                pstmt.setInt(2, insertedId);
                pstmt.executeUpdate();

                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    assertTrue("expected a row for update", rs.next());
                    int updatedId = rs.getInt("id");
                    assertEquals("same id", insertedId, updatedId);
                }
            }
        }
    }

    @Test
    public void test_generatedKeysEnabled_default_databaseMetaData_supportsGetGeneratedKeys_true() throws SQLException {
        try (Connection connection = getConnection("default")) {
            DatabaseMetaData dbmd = connection.getMetaData();

            assertTrue("expected supportsGetGeneratedKeys to report true", dbmd.supportsGetGeneratedKeys());
        }
    }

    @Test
    public void test_generatedKeysEnabled_insert_databaseMetaData_supportsGetGeneratedKeys_true() throws SQLException {
        try (Connection connection = getConnection("insert")) {
            DatabaseMetaData dbmd = connection.getMetaData();

            assertTrue("expected supportsGetGeneratedKeys to report true", dbmd.supportsGetGeneratedKeys());
        }
    }

    @Test
    public void test_generatedKeysEnabled_disabled_databaseMetaData_supportsGetGeneratedKeys_false() throws SQLException {
        try (Connection connection = getConnection("disabled")) {
            DatabaseMetaData dbmd = connection.getMetaData();

            assertFalse("expected supportsGetGeneratedKeys to report false", dbmd.supportsGetGeneratedKeys());
        }
    }

    @Test
    public void test_generatedKeysEnabled_ignored_databaseMetaData_supportsGetGeneratedKeys_false() throws SQLException {
        try (Connection connection = getConnection("ignored")) {
            DatabaseMetaData dbmd = connection.getMetaData();

            assertFalse("expected supportsGetGeneratedKeys to report false", dbmd.supportsGetGeneratedKeys());
        }
    }

    private Connection getConnection(String generatedKeysEnabledValue) throws SQLException {
        Properties props = FBTestProperties.getDefaultPropertiesForConnection();
        props.setProperty("generatedKeysEnabled", generatedKeysEnabledValue);
        return DriverManager.getConnection(FBTestProperties.getUrl(), props);
    }
}
