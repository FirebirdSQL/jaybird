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
package org.firebirdsql.jdbc.parser;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.firebirdsql.jdbc.parser.JaybirdStatementModel.*;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class GrammarParameterizedTest {

    // TODO consider moving tests from TestGrammar to here

    private final boolean expectedReturning;
    private final int expectedStatementType;
    private final String expectedTableName;
    private final String statementText;

    public GrammarParameterizedTest(boolean expectedReturning, int expectedStatementType, String expectedTableName,
            String statementText) {
        this.expectedReturning = expectedReturning;
        this.expectedStatementType = expectedStatementType;
        this.expectedTableName = expectedTableName;
        this.statementText = statementText;
    }

    @Test
    public void testParser() {
        JaybirdSqlParser parser = createParser(statementText);
        parser.statement();
        JaybirdStatementModel statementModel = parser.getStatementModel();

        assertEquals("hasReturning for: " + statementText, expectedReturning, statementModel.hasReturning());
        assertEquals("statementType for: " + statementText, expectedStatementType, statementModel.getStatementType());
        assertEquals("tableName for: " + statementText, expectedTableName, statementModel.getTableName());
        if (expectedStatementType != UNDETECTED_TYPE) {
            // dubious check; may fail
            assertEquals("syntax errors", 0, parser.getNumberOfSyntaxErrors());
        }
    }

    @Parameterized.Parameters
    public static Collection<Object[]> testData() {
        return Arrays.asList(
// @formatter:off
    /*  0 */    testCase(false, UNDETECTED_TYPE, null, "select * from rdb$database"),
    /*  1 */    testCase(false, INSERT_TYPE, "\"TABLE\"", "insert into \"TABLE\" (x, y, z) values ('ab', ?, Q'[xyz]')"),
    /*  2 */    testCase(true, INSERT_TYPE, "\"TABLE\"", "insert into \"TABLE\" (x, y, z) values ('ab', ?, Q'[xyz]') returning id"),
    /*  3 */    testCase(false, UPDATE_TYPE, "sometable", "update sometable set x = ?, y = Q'[xy'z]' where a and b > 1"),
    /*  4 */    testCase(true, UPDATE_TYPE, "SOMETABLE", "update SOMETABLE set x = ?, y = Q'[xy'z]' where a and b > 1 returning \"A\" as a"),
    /*  5 */    testCase(false, DELETE_TYPE, "sometable", "DELETE FROM sometable where x"),
    /*  6 */    testCase(true, DELETE_TYPE, "sometable", "DELETE FROM sometable where x = (select y from \"TABLE\" where startdate = {d'2018-05-1'}) returning x, a, b \"A\""),
    /*  7 */    testCase(false, UPDATE_OR_INSERT_TYPE, "Cows", "UPDATE OR INSERT INTO Cows (Name, Number, Location)\n"
                        + "  VALUES ('Suzy Creamcheese', 3278823, 'Green Pastures')\n"
                        + "  MATCHING (Number);"),
    /*  8 */    testCase(true, UPDATE_OR_INSERT_TYPE, "Cows", "UPDATE OR INSERT INTO Cows (Name, Number, Location)\n"
                        + "  VALUES ('Suzy Creamcheese', 3278823, 'Green Pastures')\n"
                        + "  MATCHING (Number)\n"
                        + "  RETURNING rec_id;"),
    /*  9 */    testCase(false, MERGE_TYPE, "customers", "MERGE INTO customers c\n"
                        + "  USING\n"
                        + "    (SELECT * FROM customers_delta WHERE id > 10) cd\n"
                        + "     ON (c.id = cd.id)\n"
                        + "  WHEN MATCHED THEN\n"
                        + "    UPDATE SET name = cd.name\n"
                        + "  WHEN NOT MATCHED THEN\n"
                        + "    INSERT (id, name)\n"
                        + "    VALUES (cd.id, cd.name)"),
    /* 10 */    testCase(true, MERGE_TYPE, "customers", "MERGE INTO customers c\n"
                        + "  USING\n"
                        + "    (SELECT * FROM customers_delta WHERE id > 10) cd\n"
                        + "     ON (c.id = cd.id)\n"
                        + "  WHEN MATCHED THEN\n"
                        + "    UPDATE SET name = cd.name\n"
                        + "  WHEN NOT MATCHED THEN\n"
                        + "    INSERT (id, name)\n"
                        + "    VALUES (cd.id, cd.name)\n"
                        + "  RETURNING id"),
    /* 11 */    testCase(false, MERGE_TYPE, "customers", "MERGE INTO customers c\n"
                        + "  USING\n"
                        + "    (SELECT * FROM customers_delta WHERE id > 10) cd\n"
                        + "     ON (c.id = cd.id)\n"
                        + "  WHEN MATCHED THEN\n"
                        + "    UPDATE SET name = cd.name\n"
                        + "  WHEN NOT MATCHED THEN\n"
                        + "    INSERT (id, name)\n"
                        + "    VALUES (cd.id, cd.name)\n"
                        + " -- RETURNING id"),
    /* 12 */    testCase(true, INSERT_TYPE, "sometable", "insert into sometable default values returning *"),
    /* 13 */    testCase(true, INSERT_TYPE, "sometable", "insert into sometable default values returning sometable.*"),
    /* 14 */    testCase(true, INSERT_TYPE, "sometable", "insert into sometable(x, y, z) values(default, 1, 2) returning *"),
    /* 15 */    testCase(true, INSERT_TYPE, "sometable", "insert into sometable(x, y, z) values(default, 1, 2) returning sometable.*"),
    /* 16 */    testCase(true, UPDATE_TYPE, "sometable", "update sometable set x = ? returning *"),
    /* 17 */    testCase(true, UPDATE_TYPE, "sometable", "update sometable set x = ? returning sometable.*"),
    /* 18 */    testCase(true, UPDATE_TYPE, "sometable", "update sometable a set x = ? returning a.*"),
    /* 19 */    testCase(true, UPDATE_TYPE, "sometable", "update sometable as a set a.x = ? returning *"),
    /* 20 */    testCase(true, UPDATE_TYPE, "sometable", "update sometable as a set a.x = ? returning a.id"),
    /* 21 */    testCase(true, DELETE_TYPE, "sometable", "delete from sometable where x = ? returning *"),
    /* 22 */    testCase(true, DELETE_TYPE, "sometable", "delete from sometable a where a.x = ? returning a.*"),
    /* 23 */    testCase(true, DELETE_TYPE, "sometable", "delete from sometable a where a.x = ? returning *"),
    /* 24 */    testCase(true, DELETE_TYPE, "sometable", "delete from sometable as a where a.x = ? returning a.id"),
    /* 25 */    testCase(true, DELETE_TYPE, "sometable", "delete from sometable as a where a.x = ? returning *"),
    /* 26 */    testCase(true, MERGE_TYPE, "customers", "MERGE INTO customers\n"
                        + "  USING\n"
                        + "    (SELECT * FROM customers_delta WHERE id > 10) cd\n"
                        + "     ON (c.id = cd.id)\n"
                        + "  WHEN MATCHED THEN\n"
                        + "    UPDATE SET name = cd.name\n"
                        + "  WHEN NOT MATCHED THEN\n"
                        + "    INSERT (id, name)\n"
                        + "    VALUES (cd.id, cd.name)\n"
                        + "  RETURNING *"),
    /* 27 */    testCase(true, MERGE_TYPE, "customers", "MERGE INTO customers as c\n"
                        + "  USING\n"
                        + "    (SELECT * FROM customers_delta WHERE id > 10) cd\n"
                        + "     ON (c.id = cd.id)\n"
                        + "  WHEN MATCHED THEN\n"
                        + "    UPDATE SET name = cd.name\n"
                        + "  WHEN NOT MATCHED THEN\n"
                        + "    INSERT (id, name)\n"
                        + "    VALUES (cd.id, cd.name)\n"
                        + "  RETURNING *"),
    /* 28 */    testCase(true, MERGE_TYPE, "customers", "MERGE INTO customers c\n"
                        + "  USING\n"
                        + "    (SELECT * FROM customers_delta WHERE id > 10) cd\n"
                        + "     ON (c.id = cd.id)\n"
                        + "  WHEN MATCHED THEN\n"
                        + "    UPDATE SET name = cd.name\n"
                        + "  WHEN NOT MATCHED THEN\n"
                        + "    INSERT (id, name)\n"
                        + "    VALUES (cd.id, cd.name)\n"
                        + "  RETURNING c.*"),
    /* 29 */    testCase(true, MERGE_TYPE, "customers", "MERGE INTO customers c\n"
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

    private static Object[] testCase(boolean expectedReturning, int expectedStatementType, String expectedTableName,
            String statementText) {
        return new Object[] { expectedReturning, expectedStatementType, expectedTableName, statementText };
    }

    private JaybirdSqlParser createParser(String testString) {
        CharStream stream = CharStreams.fromString(testString);

        JaybirdSqlLexer lexer = new JaybirdSqlLexer(stream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);

        return new JaybirdSqlParser(tokenStream);
    }
}
