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

import java.sql.Connection;
import java.sql.Statement;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;

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
            CREATE_TABLE,
            CREATE_SEQUENCE,
            CREATE_TRIGGER);

    Connection con;

    @BeforeEach
    void setUp() throws Exception {
        con = getConnectionViaDriverManager();
        try (Statement stmt = con.createStatement()) {
            stmt.execute("delete from TABLE_WITH_TRIGGER");
            stmt.execute(INIT_SEQUENCE);
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        con.close();
    }
}