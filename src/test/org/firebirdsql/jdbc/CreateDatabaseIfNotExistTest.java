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

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.extension.DatabaseUserExtension;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.jaybird.props.PropertyNames;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.FbAssumptions.assumeFeature;
import static org.firebirdsql.common.assertions.ResultSetAssertions.assertNextRow;
import static org.firebirdsql.common.matchers.GdsTypeMatchers.isEmbeddedType;
import static org.firebirdsql.common.matchers.MatcherAssume.assumeThat;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.errorCodeEquals;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@code createDatabaseIfNotExist} connection property.
 *
 * @author Mark Rotteveel
 */
class CreateDatabaseIfNotExistTest {

    private static final String NO_CREATE_DB_PRIVILEGE_USER = "NO_CREATE_DB_PRIVILEGE";
    private static final String NO_CREATE_DB_PRIVILEGE_PASSWORD = "password";
    private static final String CREATE_DB_TEST_NAME = getDatabasePath("createdbtest.fdb");

    // Normal test database shared to be able to create users
    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll sharedDatabase = UsesDatabaseExtension.usesDatabaseForAll();

    // No default database, non-default databases are registered for drop when needed
    @RegisterExtension
    final UsesDatabaseExtension.UsesDatabaseForEach usesDatabase = UsesDatabaseExtension.noDatabase();
    @RegisterExtension
    final DatabaseUserExtension databaseUser = DatabaseUserExtension.withDatabaseUser();

    @Test
    void noDatabaseCreatedByDefault() {
        var exception = assertThrows(SQLException.class, () -> getConnection());
        assertThat(exception, errorCodeEquals(ISCConstants.isc_io_error));
    }

    @Test
    void createDatabaseIfNotExist() throws Exception {
        try (var connection = getConnection(Map.of(PropertyNames.createDatabaseIfNotExist, "true"))) {
            assertTrue(connection.isValid(1000), "valid connection");
        }
        try (var connection = assertDoesNotThrow(() -> getConnection(),
                "Should be able to establish connection to newly created db")) {
            assertTrue(connection.isValid(1000), "valid connection");
        }
    }

    @Test
    void createWithNonPrivilegedUser_shouldFail() throws Exception {
        assumeFeature(FirebirdSupportInfo::supportsMetadataPrivileges, "Test requires CREATE DATABASE privileges");
        databaseUser.createUser(NO_CREATE_DB_PRIVILEGE_USER, NO_CREATE_DB_PRIVILEGE_PASSWORD, "Srp");
        var exception = assertThrows(SQLException.class, () ->
                getConnection(Map.of(
                        PropertyNames.user, NO_CREATE_DB_PRIVILEGE_USER,
                        PropertyNames.password, NO_CREATE_DB_PRIVILEGE_PASSWORD,
                        PropertyNames.createDatabaseIfNotExist, "true")));
        assertThat(exception, errorCodeEquals(ISCConstants.isc_no_priv));
    }

    @Test
    void createOverrideNonPrivilegedUserWithPrivilegedUser() throws Exception {
        databaseUser.createUser(NO_CREATE_DB_PRIVILEGE_USER, NO_CREATE_DB_PRIVILEGE_PASSWORD, "Srp");
        try (var connection = getConnection(Map.of(
                PropertyNames.user, NO_CREATE_DB_PRIVILEGE_USER,
                PropertyNames.password, NO_CREATE_DB_PRIVILEGE_PASSWORD,
                PropertyNames.user + "@create", DB_USER,
                PropertyNames.password + "@create", DB_PASSWORD,
                PropertyNames.createDatabaseIfNotExist, "true"));
             var stmt = connection.createStatement();
             var rs = stmt.executeQuery("select current_user from RDB$DATABASE")) {
            assertNextRow(rs);
            // NOTE: We're assuming use of unquoted usernames
            assertEquals(DB_USER.toUpperCase(Locale.ROOT), rs.getString(1).trim(),
                    "Unexpected user for newly created database");
        }
        try (var connection = assertDoesNotThrow(() -> getConnection(),
                "Should be able to establish connection to newly created db")) {
            assertTrue(connection.isValid(1000), "valid connection");
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { PropertyNames.serverName + "@create", "host@create" })
    void cannotOverrideServerName(String overrideServerNameProperty) throws Exception {
        assumeThat("We don't test overriding serverName on Embedded", FBTestProperties.GDS_TYPE, not(isEmbeddedType()));
        try (var connection = assertDoesNotThrow(() -> getConnection(Map.of(
                        PropertyNames.createDatabaseIfNotExist, "true",
                        PropertyNames.serverName, DB_SERVER_URL,
                        PropertyNames.portNumber, String.valueOf(DB_SERVER_PORT),
                        PropertyNames.databaseName, CREATE_DB_TEST_NAME,
                        overrideServerNameProperty, "doesnotexist")),
                "Connection should not fail as serverName is not overridable")) {
            assertTrue(connection.isValid(1000), "valid connection");
        }
        try (var connection = assertDoesNotThrow(() -> getConnection(),
                "Should be able to establish connection to newly created db")) {
            assertTrue(connection.isValid(1000), "valid connection");
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { PropertyNames.portNumber + "@create", "port@create" })
    void cannotOverridePortNumber(String overridePortNumberProperty) throws Exception {
        assumeThat("We don't test overriding portNumber on Embedded", FBTestProperties.GDS_TYPE, not(isEmbeddedType()));
        try (var connection = assertDoesNotThrow(() -> getConnection(Map.of(
                        PropertyNames.createDatabaseIfNotExist, "true",
                        PropertyNames.serverName, DB_SERVER_URL,
                        PropertyNames.portNumber, String.valueOf(DB_SERVER_PORT),
                        PropertyNames.databaseName, CREATE_DB_TEST_NAME,
                        // We're making an assumption this port is not in use (or not in use by Firebird)
                        overridePortNumberProperty, String.valueOf(DB_SERVER_PORT + 1))),
                "Connection should not fail as portNumber is not overridable")) {
            assertTrue(connection.isValid(1000), "valid connection");
        }
        try (var connection = assertDoesNotThrow(() -> getConnection(),
                "Should be able to establish connection to newly created db")) {
            assertTrue(connection.isValid(1000), "valid connection");
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { PropertyNames.attachObjectName + "@create", PropertyNames.databaseName + "@create",
            PropertyNames.serviceName + "@create", "database@create" })
    void cannotOverrideDatabaseName(String overrideDatabaseNameProperty) throws Exception {
        String overrideDatabasePath = getDatabasePath("alternate_db.fdb");
        usesDatabase.addDatabase(overrideDatabasePath);
        try (var connection = getConnection(Map.of(
                PropertyNames.createDatabaseIfNotExist, "true",
                PropertyNames.serverName, DB_SERVER_URL,
                PropertyNames.portNumber, String.valueOf(DB_SERVER_PORT),
                PropertyNames.databaseName, CREATE_DB_TEST_NAME,
                overrideDatabaseNameProperty, overrideDatabasePath))) {
            assertTrue(connection.isValid(1000), "valid connection");
        }
        try (var connection = assertDoesNotThrow(() -> getConnection(),
                "Should be able to establish connection to newly created db as databaseName is not overridable")) {
            // overridden database path will not exist, so we can remove it again
            usesDatabase.removeDatabase(overrideDatabasePath);
            assertTrue(connection.isValid(1000), "valid connection");
        }
    }

    private Connection getConnection() throws SQLException {
        return getConnection(Map.of());
    }

    private Connection getConnection(Map<String, String> properties) throws SQLException {
        var connection =
                DriverManager.getConnection(getUrl(CREATE_DB_TEST_NAME), getPropertiesForConnection(properties));
        if (Boolean.parseBoolean(properties.get(PropertyNames.createDatabaseIfNotExist))) {
            usesDatabase.addDatabase(CREATE_DB_TEST_NAME);
        }
        return connection;
    }

}
