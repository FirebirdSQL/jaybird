// SPDX-FileCopyrightText: Copyright 2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.firebirdsql.common.FbAssumptions.assumeFeature;
import static org.firebirdsql.common.FbAssumptions.assumeFeatureMissing;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Tests for {@link FBDatabaseMetaData#findTableSchema(String)}.
 */
class FBDatabaseMetaDataFindTableSchemaTest {

    private static final String CREATE_DEFAULT_SCHEMA_TABLE_ONE = "create table TABLE_ONE (ID integer)";
    private static final String CREATE_DEFAULT_SCHEMA_TABLE_TWO = "create table \"table_two\" (ID integer)";
    private static final String CREATE_OTHER_SCHEMA = "create schema OTHER_SCHEMA";
    private static final String CREATE_OTHER_SCHEMA_TABLE_ONE = "create table OTHER_SCHEMA.TABLE_ONE (ID integer)";
    private static final String CREATE_OTHER_SCHEMA_TABLE_THREE = "create table OTHER_SCHEMA.TABLE_THREE (ID integer)";

    private static final String NOT_FOUND_MARKER = "#NOT_FOUND#";

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll(
            getDbInitStatements());

    private static List<String> getDbInitStatements() {
        var statements = new ArrayList<>(List.of(
                CREATE_DEFAULT_SCHEMA_TABLE_ONE,
                CREATE_DEFAULT_SCHEMA_TABLE_TWO));
        if (getDefaultSupportInfo().supportsSchemas()) {
            statements.addAll(List.of(
                    CREATE_OTHER_SCHEMA,
                    CREATE_OTHER_SCHEMA_TABLE_ONE,
                    CREATE_OTHER_SCHEMA_TABLE_THREE));
        }
        return statements;
    }

    private static Connection connection;
    private static FirebirdDatabaseMetaData dbmd;
    private static PreparedStatement sessionResetStatement;

    @BeforeAll
    static void setupAll() throws Exception  {
        connection = getConnectionViaDriverManager();
        dbmd = connection.getMetaData().unwrap(FirebirdDatabaseMetaData.class);
    }

    @BeforeEach
    void setupEach() throws Exception {
        if (dbmd.supportsSchemasInDataManipulation()) {
            if (sessionResetStatement == null) {
                sessionResetStatement = connection.prepareStatement("alter session reset");
            }
            // reset search path
            sessionResetStatement.execute();
        }
    }

    @AfterAll
    static void tearDownAll() throws Exception {
        connection.close();
    }

    @ParameterizedTest
    @ValueSource(strings = { "TABLE_ONE", "table_two", "RDB$RELATIONS", "DOES_NOT_EXIST" })
    void findSchema_noTableSchemaSupport(String tableName) throws Exception {
        assumeFeatureMissing(FirebirdSupportInfo::supportsSchemas, "Test requires no schema support");

        Optional<String> schemaOpt = dbmd.findTableSchema(tableName);
        assertThat("expected schema empty string (no schema support)", schemaOpt, is(optionalWithValue("")));
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, nullValues = "<NIL>", textBlock = """
            tableName,      searchPath,            expectedSchema
            TABLE_ONE,      <NIL>,                 PUBLIC
            TABLE_ONE,      'PUBLIC,OTHER_SCHEMA', PUBLIC
            TABLE_ONE,      'OTHER_SCHEMA,PUBLIC', OTHER_SCHEMA
            TABLE_ONE,      OTHER_SCHEMA,          OTHER_SCHEMA
            table_two,      <NIL>,                 PUBLIC
            table_two,      'OTHER_SCHEMA,PUBLIC', PUBLIC
            table_two,      OTHER_SCHEMA,          #NOT_FOUND#
            TABLE_THREE,    <NIL>,                 #NOT_FOUND#
            TABLE_THREE,    'PUBLIC,OTHER_SCHEMA', OTHER_SCHEMA
            TABLE_THREE,    OTHER_SCHEMA,          OTHER_SCHEMA
            RDB$RELATIONS,  <NIL>,                 SYSTEM
            RDB$RELATIONS,  PUBLIC,                SYSTEM
            RDB$RELATIONS,  'SYSTEM,PUBLIC',       SYSTEM
            DOES_NOT_EXIST, <NIL>,                 #NOT_FOUND#
            DOES_NOT_EXIST, 'OTHER_SCHEMA,PUBLIC', #NOT_FOUND#
            """)
    void findSchema_Table_schemaSupport(String tableName, String searchPath, String expectedSchema) throws Exception {
        assumeFeature(FirebirdSupportInfo::supportsSchemas, "Test requires schema support");
        if (searchPath != null) {
            try (var stmt = connection.createStatement()) {
                stmt.execute("set search_path to " + searchPath);
            }
        }

        Optional<String> schemaOpt = dbmd.findTableSchema(tableName);
        if (NOT_FOUND_MARKER.equals(expectedSchema)) {
            assertThat("schema should not be found", schemaOpt, is(emptyOptional()));
        } else {
            assertThat("unexpected schema", schemaOpt, is(optionalWithValue(expectedSchema)));
        }
    }

}
