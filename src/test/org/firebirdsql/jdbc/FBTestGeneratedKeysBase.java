// SPDX-FileCopyrightText: Copyright 2011-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import org.firebirdsql.common.extension.RequireFeatureExtension;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.provider.Arguments;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;

/**
 * Test base for tests of retrieval of auto generated keys.
 * <p>
 * Defines the basic table structure for the test.
 * </p>
 *
 * @author Mark Rotteveel
 */
abstract class FBTestGeneratedKeysBase {

    @RegisterExtension
    @Order(1)
    static final RequireFeatureExtension requireFeature = RequireFeatureExtension
            .withFeatureCheck(FirebirdSupportInfo::supportsInsertReturning,
                    "Test requires support for INSERT ... RETURNING ...")
            .build();

    private static final String CREATE_TABLE = """
            CREATE TABLE TABLE_WITH_TRIGGER (
             ID Integer NOT NULL,
             TEXT Varchar(200),
             "quote_column" INTEGER DEFAULT 2,
             CONSTRAINT PK_TABLE_WITH_TRIGGER_1 PRIMARY KEY (ID)
            )""";
    private static final String CREATE_OTHER_SCHEMA = "create schema OTHER_SCHEMA";
    private static final String CREATE_TABLE_SAME_NAME_PUBLIC = """
            create table PUBLIC.SAME_NAME (
              ID_IN_PUBLIC integer generated always as identity constraint PK_SAME_NAME primary key,
              TEXT_IN_PUBLIC varchar(200)
            )""";
    private static final String CREATE_TABLE_SAME_NAME_OTHER_SCHEMA = """
            create table OTHER_SCHEMA.SAME_NAME (
              ID_IN_OTHER_SCHEMA integer generated always as identity constraint PK_SAME_NAME primary key,
              TEXT_IN_OTHER_SCHEMA varchar(200)
            )""";
    private static final String CREATE_SEQUENCE = "CREATE GENERATOR GEN_TABLE_WITH_TRIGGER_ID";
    private static final String INIT_SEQUENCE = "SET GENERATOR GEN_TABLE_WITH_TRIGGER_ID TO 512";
    private static final String CREATE_TRIGGER = """
            CREATE TRIGGER TABLE_WITH_TRIGGER_BI FOR TABLE_WITH_TRIGGER ACTIVE
            BEFORE INSERT POSITION 0
            AS
            DECLARE VARIABLE tmp DECIMAL(18,0);
            BEGIN
              IF (NEW.ID IS NULL) THEN
                NEW.ID = GEN_ID(GEN_TABLE_WITH_TRIGGER_ID, 1);
              ELSE
              BEGIN
                tmp = GEN_ID(GEN_TABLE_WITH_TRIGGER_ID, 0);
                if (tmp < new.ID) then
                  tmp = GEN_ID(GEN_TABLE_WITH_TRIGGER_ID, new.ID-tmp);
              END
            END""";
    static final String ADD_BLOB_COLUMN = "alter table TABLE_WITH_TRIGGER add BLOB_COLUMN blob sub_type text";
    static final String DROP_BLOB_COLUMN = "alter table TABLE_WITH_TRIGGER drop BLOB_COLUMN";

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll(
            getDbInitStatements());

    Connection con;

    private static List<String> getDbInitStatements() {
        var stmts = new ArrayList<>(List.of(
                CREATE_TABLE,
                CREATE_SEQUENCE,
                CREATE_TRIGGER));
        if (getDefaultSupportInfo().supportsSchemas()) {
            stmts.addAll(List.of(
                    CREATE_OTHER_SCHEMA,
                    CREATE_TABLE_SAME_NAME_PUBLIC,
                    CREATE_TABLE_SAME_NAME_OTHER_SCHEMA
            ));
        }

        return stmts;
    }

    @BeforeEach
    void setUp() throws Exception {
        con = getConnectionViaDriverManager();
        try (var stmt = con.createStatement()) {
            stmt.execute("delete from TABLE_WITH_TRIGGER");
            stmt.execute(INIT_SEQUENCE);
            if (getDefaultSupportInfo().supportsSchemas()) {
                // Reset schema search path
                stmt.execute("ALTER SESSION RESET");
            }
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        con.close();
    }

    static Stream<Arguments> withOrWithoutSchema() {
        if (getDefaultSupportInfo().supportsSchemas()) {
            return Stream.of(Arguments.of(true), Arguments.of(false));
        }
        return Stream.of(Arguments.of(false));
    }

}