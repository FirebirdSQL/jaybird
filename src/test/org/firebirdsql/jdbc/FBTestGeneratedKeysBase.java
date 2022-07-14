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
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
abstract class FBTestGeneratedKeysBase {

    @RegisterExtension
    @Order(1)
    static final RequireFeatureExtension requireFeature = RequireFeatureExtension
            .withFeatureCheck(FirebirdSupportInfo::supportsInsertReturning,
                    "Test requires support for INSERT ... RETURNING ...")
            .build();

    //@formatter:off
    private static final String CREATE_TABLE = "CREATE TABLE TABLE_WITH_TRIGGER (\n"
                + " ID Integer NOT NULL,\n"
                + " TEXT Varchar(200),\n"
                + " \"quote_column\" INTEGER DEFAULT 2,\n"
                + " CONSTRAINT PK_TABLE_WITH_TRIGGER_1 PRIMARY KEY (ID)\n"
                + ")";
    private static final String CREATE_SEQUENCE = "CREATE GENERATOR GEN_TABLE_WITH_TRIGGER_ID";
    private static final String INIT_SEQUENCE = "SET GENERATOR GEN_TABLE_WITH_TRIGGER_ID TO 512";
    private static final String CREATE_TRIGGER = "CREATE TRIGGER TABLE_WITH_TRIGGER_BI FOR TABLE_WITH_TRIGGER ACTIVE\n" +
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
        		"END";
    //@formatter:on

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