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
import org.firebirdsql.common.extension.RequireFeatureExtension;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.*;
import java.util.Properties;

import static org.firebirdsql.common.matchers.SQLExceptionMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests configuration property {@code generatedKeysEnabled}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
class GeneratedKeysEnabledTest {

    @RegisterExtension
    @Order(1)
    static final RequireFeatureExtension requireFeature = RequireFeatureExtension
            .withFeatureCheck(FirebirdSupportInfo::supportsInsertReturning,
                    "Test requires INSERT .. RETURNING .. support")
            .build();

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll(
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

    private static final String TEST_INSERT_QUERY = "INSERT INTO TABLE_WITH_TRIGGER(TEXT) VALUES (?)";
    private static final String TEST_UPDATE_QUERY = "UPDATE TABLE_WITH_TRIGGER SET TEXT = ? WHERE ID = ?";

    @Test
    void test_generatedKeysEnabled_notSpecified_insertWorks() throws SQLException {
        Properties props = FBTestProperties.getDefaultPropertiesForConnection();
        try (Connection connection = DriverManager.getConnection(FBTestProperties.getUrl(), props);
             PreparedStatement pstmt = connection.prepareStatement(TEST_INSERT_QUERY, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, "value");
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                assertTrue(rs.next(), "expected a row");
                assertThat("non-zero id", rs.getInt("id"), greaterThan(0));
            }
        }
    }

    @Test
    void test_generatedKeysEnabled_default_insertWorks() throws SQLException {
        try (Connection connection = getConnection("default");
             PreparedStatement pstmt = connection.prepareStatement(TEST_INSERT_QUERY, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, "value");
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                assertTrue(rs.next(), "expected a row");
                assertThat("non-zero id", rs.getInt("id"), greaterThan(0));
            }
        }
    }

    @Test
    void test_generatedKeysEnabled_disabled_throwsFeatureNotSupported() throws SQLException {
        try (Connection connection = getConnection("disabled")) {
            SQLException exception = assertThrows(SQLFeatureNotSupportedException.class,
                    () -> connection.prepareStatement(TEST_INSERT_QUERY, Statement.RETURN_GENERATED_KEYS));
            assertThat(exception, allOf(
                    errorCodeEquals(JaybirdErrorCodes.jb_generatedKeysSupportNotAvailable),
                    fbMessageStartsWith(JaybirdErrorCodes.jb_generatedKeysSupportNotAvailable, GeneratedKeysSupportFactory.REASON_EXPLICITLY_DISABLED)));
        }
    }

    @Test
    void test_generatedKeysEnabled_ignored_insert_noGeneratedKeys() throws SQLException {
        try (Connection connection = getConnection("ignored");
             PreparedStatement pstmt = connection.prepareStatement(TEST_INSERT_QUERY, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, "value");
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                assertFalse(rs.next(), "expected no row");
            }
        }
    }

    @Test
    void test_generatedKeysEnabled_insert_insertWorks_updateNoGeneratedKeys() throws SQLException {
        try (Connection connection = getConnection("insert")) {
            int insertedId;
            try (PreparedStatement pstmt = connection.prepareStatement(TEST_INSERT_QUERY, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, "value");
                pstmt.executeUpdate();
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    assertTrue(rs.next(), "expected a row for insert");
                    insertedId = rs.getInt("id");
                    assertThat("non-zero id", insertedId, greaterThan(0));
                }
            }

            try (PreparedStatement pstmt = connection.prepareStatement(TEST_UPDATE_QUERY, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, "newvalue");
                pstmt.setInt(2, insertedId);
                pstmt.executeUpdate();

                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    assertFalse(rs.next(), "expected no row for update");
                }
            }
        }
    }

    @Test
    void test_generatedKeysEnabled_insert_update_insertWorks_updateWorks() throws SQLException {
        assumeTrue(FBTestProperties.getDefaultSupportInfo().supportsUpdateReturning(),
                "test requires UPDATE .. RETURNING .. support");

        try (Connection connection = getConnection("insert,update")) {
            int insertedId;
            try (PreparedStatement pstmt = connection.prepareStatement(TEST_INSERT_QUERY, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, "value");
                pstmt.executeUpdate();
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    assertTrue(rs.next(), "expected a row for insert");
                    insertedId = rs.getInt("id");
                    assertThat("non-zero id", insertedId, greaterThan(0));
                }
            }

            try (PreparedStatement pstmt = connection.prepareStatement(TEST_UPDATE_QUERY, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, "newvalue");
                pstmt.setInt(2, insertedId);
                pstmt.executeUpdate();

                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    assertTrue(rs.next(), "expected a row for update");
                    int updatedId = rs.getInt("id");
                    assertEquals(insertedId, updatedId, "same id");
                }
            }
        }
    }

    @Test
    void test_generatedKeysEnabled_default_databaseMetaData_supportsGetGeneratedKeys_true() throws SQLException {
        try (Connection connection = getConnection("default")) {
            DatabaseMetaData dbmd = connection.getMetaData();

            assertTrue(dbmd.supportsGetGeneratedKeys(), "expected supportsGetGeneratedKeys to report true");
        }
    }

    @Test
    void test_generatedKeysEnabled_insert_databaseMetaData_supportsGetGeneratedKeys_true() throws SQLException {
        try (Connection connection = getConnection("insert")) {
            DatabaseMetaData dbmd = connection.getMetaData();

            assertTrue(dbmd.supportsGetGeneratedKeys(), "expected supportsGetGeneratedKeys to report true");
        }
    }

    @Test
    void test_generatedKeysEnabled_disabled_databaseMetaData_supportsGetGeneratedKeys_false() throws SQLException {
        try (Connection connection = getConnection("disabled")) {
            DatabaseMetaData dbmd = connection.getMetaData();

            assertFalse(dbmd.supportsGetGeneratedKeys(), "expected supportsGetGeneratedKeys to report false");
        }
    }

    @Test
    void test_generatedKeysEnabled_ignored_databaseMetaData_supportsGetGeneratedKeys_false() throws SQLException {
        try (Connection connection = getConnection("ignored")) {
            DatabaseMetaData dbmd = connection.getMetaData();

            assertFalse(dbmd.supportsGetGeneratedKeys(), "expected supportsGetGeneratedKeys to report false");
        }
    }

    private Connection getConnection(String generatedKeysEnabledValue) throws SQLException {
        Properties props = FBTestProperties.getDefaultPropertiesForConnection();
        props.setProperty("generatedKeysEnabled", generatedKeysEnabledValue);
        return DriverManager.getConnection(FBTestProperties.getUrl(), props);
    }
}
