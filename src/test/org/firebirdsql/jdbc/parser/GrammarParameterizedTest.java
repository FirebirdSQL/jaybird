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
package org.firebirdsql.jdbc.parser;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.firebirdsql.jdbc.parser.DmlStatementType.DELETE;
import static org.firebirdsql.jdbc.parser.DmlStatementType.INSERT;
import static org.firebirdsql.jdbc.parser.DmlStatementType.MERGE;
import static org.firebirdsql.jdbc.parser.DmlStatementType.OTHER;
import static org.firebirdsql.jdbc.parser.DmlStatementType.UPDATE;
import static org.firebirdsql.jdbc.parser.DmlStatementType.UPDATE_OR_INSERT;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GrammarParameterizedTest {

    // TODO consider moving tests from TestGrammar to here

    @ParameterizedTest
    @MethodSource("testData")
    void testParser(boolean expectedReturning, DmlStatementType expectedStatementType, String expectedTableName,
            String statementText) {
        StatementIdentification statementIdentification = parseStatement(statementText);

        assertEquals(expectedReturning, statementIdentification.returningClauseDetected(),
                "returningClauseDetected for: " + statementText);
        assertEquals(expectedStatementType, statementIdentification.getDmlStatementType(),
                "dmlStatementType for: " + statementText);
        assertEquals(expectedTableName, statementIdentification.getTableName(), "tableName for: " + statementText);
    }

    static Stream<Arguments> testData() {
        return Stream.of(
// @formatter:off
    /*  0 */    testCase(false, OTHER, null, "select * from rdb$database"),
    /*  1 */    testCase(false, INSERT, "\"TABLE\"", "insert into \"TABLE\" (x, y, z) values ('ab', ?, Q'[xyz]')"),
    /*  2 */    testCase(true, INSERT, "\"TABLE\"", "insert into \"TABLE\" (x, y, z) values ('ab', ?, Q'[xyz]') returning id"),
    /*  3 */    testCase(false, UPDATE, "sometable", "update sometable set x = ?, y = Q'[xy'z]' where a and b > 1"),
    /*  4 */    testCase(true, UPDATE, "SOMETABLE", "update SOMETABLE set x = ?, y = Q'[xy'z]' where a and b > 1 returning \"A\" as a"),
    /*  5 */    testCase(false, DELETE, "sometable", "DELETE FROM sometable where x"),
    /*  6 */    testCase(true, DELETE, "sometable", "DELETE FROM sometable where x = (select y from \"TABLE\" where startdate = {d'2018-05-1'}) returning x, a, b \"A\""),
    /*  7 */    testCase(false, UPDATE_OR_INSERT, "Cows", "UPDATE OR INSERT INTO Cows (Name, Number, Location)\n"
                        + "  VALUES ('Suzy Creamcheese', 3278823, 'Green Pastures')\n"
                        + "  MATCHING (Number);"),
    /*  8 */    testCase(true, UPDATE_OR_INSERT, "Cows", "UPDATE OR INSERT INTO Cows (Name, Number, Location)\n"
                        + "  VALUES ('Suzy Creamcheese', 3278823, 'Green Pastures')\n"
                        + "  MATCHING (Number)\n"
                        + "  RETURNING rec_id;"),
    /*  9 */    testCase(false, MERGE, "customers", "MERGE INTO customers c\n"
                        + "  USING\n"
                        + "    (SELECT * FROM customers_delta WHERE id > 10) cd\n"
                        + "     ON (c.id = cd.id)\n"
                        + "  WHEN MATCHED THEN\n"
                        + "    UPDATE SET name = cd.name\n"
                        + "  WHEN NOT MATCHED THEN\n"
                        + "    INSERT (id, name)\n"
                        + "    VALUES (cd.id, cd.name)"),
    /* 10 */    testCase(true, MERGE, "customers", "MERGE INTO customers c\n"
                        + "  USING\n"
                        + "    (SELECT * FROM customers_delta WHERE id > 10) cd\n"
                        + "     ON (c.id = cd.id)\n"
                        + "  WHEN MATCHED THEN\n"
                        + "    UPDATE SET name = cd.name\n"
                        + "  WHEN NOT MATCHED THEN\n"
                        + "    INSERT (id, name)\n"
                        + "    VALUES (cd.id, cd.name)\n"
                        + "  RETURNING id"),
    /* 11 */    testCase(false, MERGE, "customers", "MERGE INTO customers c\n"
                        + "  USING\n"
                        + "    (SELECT * FROM customers_delta WHERE id > 10) cd\n"
                        + "     ON (c.id = cd.id)\n"
                        + "  WHEN MATCHED THEN\n"
                        + "    UPDATE SET name = cd.name\n"
                        + "  WHEN NOT MATCHED THEN\n"
                        + "    INSERT (id, name)\n"
                        + "    VALUES (cd.id, cd.name)\n"
                        + " -- RETURNING id"),
    /* 12 */    testCase(true, INSERT, "sometable", "insert into sometable default values returning *"),
    /* 13 */    testCase(true, INSERT, "sometable", "insert into sometable default values returning sometable.*"),
    /* 14 */    testCase(true, INSERT, "sometable", "insert into sometable(x, y, z) values(default, 1, 2) returning *"),
    /* 15 */    testCase(true, INSERT, "sometable", "insert into sometable(x, y, z) values(default, 1, 2) returning sometable.*"),
    /* 16 */    testCase(true, UPDATE, "sometable", "update sometable set x = ? returning *"),
    /* 17 */    testCase(true, UPDATE, "sometable", "update sometable set x = ? returning sometable.*"),
    /* 18 */    testCase(true, UPDATE, "sometable", "update sometable a set x = ? returning a.*"),
    /* 19 */    testCase(true, UPDATE, "sometable", "update sometable as a set a.x = ? returning *"),
    /* 20 */    testCase(true, UPDATE, "sometable", "update sometable as a set a.x = ? returning a.id"),
    /* 21 */    testCase(true, DELETE, "sometable", "delete from sometable where x = ? returning *"),
    /* 22 */    testCase(true, DELETE, "sometable", "delete from sometable a where a.x = ? returning a.*"),
    /* 23 */    testCase(true, DELETE, "sometable", "delete from sometable a where a.x = ? returning *"),
    /* 24 */    testCase(true, DELETE, "sometable", "delete from sometable as a where a.x = ? returning a.id"),
    /* 25 */    testCase(true, DELETE, "sometable", "delete from sometable as a where a.x = ? returning *"),
    /* 26 */    testCase(true, MERGE, "customers", "MERGE INTO customers\n"
                        + "  USING\n"
                        + "    (SELECT * FROM customers_delta WHERE id > 10) cd\n"
                        + "     ON (c.id = cd.id)\n"
                        + "  WHEN MATCHED THEN\n"
                        + "    UPDATE SET name = cd.name\n"
                        + "  WHEN NOT MATCHED THEN\n"
                        + "    INSERT (id, name)\n"
                        + "    VALUES (cd.id, cd.name)\n"
                        + "  RETURNING *"),
    /* 27 */    testCase(true, MERGE, "customers", "MERGE INTO customers as c\n"
                        + "  USING\n"
                        + "    (SELECT * FROM customers_delta WHERE id > 10) cd\n"
                        + "     ON (c.id = cd.id)\n"
                        + "  WHEN MATCHED THEN\n"
                        + "    UPDATE SET name = cd.name\n"
                        + "  WHEN NOT MATCHED THEN\n"
                        + "    INSERT (id, name)\n"
                        + "    VALUES (cd.id, cd.name)\n"
                        + "  RETURNING *"),
    /* 28 */    testCase(true, MERGE, "customers", "MERGE INTO customers c\n"
                        + "  USING\n"
                        + "    (SELECT * FROM customers_delta WHERE id > 10) cd\n"
                        + "     ON (c.id = cd.id)\n"
                        + "  WHEN MATCHED THEN\n"
                        + "    UPDATE SET name = cd.name\n"
                        + "  WHEN NOT MATCHED THEN\n"
                        + "    INSERT (id, name)\n"
                        + "    VALUES (cd.id, cd.name)\n"
                        + "  RETURNING c.*"),
    /* 29 */    testCase(true, MERGE, "customers", "MERGE INTO customers c\n"
                        + "  USING\n"
                        + "    (SELECT * FROM customers_delta WHERE id > 10) cd\n"
                        + "     ON (c.id = cd.id)\n"
                        + "  WHEN MATCHED THEN\n"
                        + "    UPDATE SET name = cd.name\n"
                        + "  WHEN NOT MATCHED THEN\n"
                        + "    INSERT (id, name)\n"
                        + "    VALUES (cd.id, cd.name)\n"
                        + "  RETURNING c.id")
// @formatter:on
        );
    }

    private static Arguments testCase(boolean expectedReturning, DmlStatementType expectedStatementType,
            String expectedTableName, String statementText) {
        return Arguments.of(expectedReturning, expectedStatementType, expectedTableName, statementText);
    }

    private StatementIdentification parseStatement(String testString) {
        DmlStatementDetector dmlStatementDetector = new DmlStatementDetector();
        SqlParser.withReservedWords(FirebirdReservedWords.latest())
                .withVisitor(dmlStatementDetector)
                .of(testString)
                .parse();
        return dmlStatementDetector.toStatementIdentification();
    }
}
