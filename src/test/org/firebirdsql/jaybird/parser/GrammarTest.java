// SPDX-FileCopyrightText: Copyright 2021-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.firebirdsql.jaybird.parser.LocalStatementType.DELETE;
import static org.firebirdsql.jaybird.parser.LocalStatementType.INSERT;
import static org.firebirdsql.jaybird.parser.LocalStatementType.MERGE;
import static org.firebirdsql.jaybird.parser.LocalStatementType.SELECT;
import static org.firebirdsql.jaybird.parser.LocalStatementType.UPDATE;
import static org.firebirdsql.jaybird.parser.LocalStatementType.UPDATE_OR_INSERT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GrammarTest {

    private static StatementIdentification parseStatement(String testString) {
        StatementDetector statementDetector = new StatementDetector();
        SqlParser.withReservedWords(FirebirdReservedWords.latest())
                .withVisitor(statementDetector)
                .of(testString)
                .parse();
        return statementDetector.toStatementIdentification();
    }

    @Test
    void insert_values() {
        StatementIdentification statementModel = parseStatement(
                "insert into someTable(a, \"\u0442\u0435\"\"\u0441\u0442\", aaa) values('a', -1.23, a(a,aa))");

        assertEquals(INSERT, statementModel.getStatementType(), "Unexpected statement type");
        assertNull(statementModel.getSchema(), "Unexpected schema");
        assertEquals("SOMETABLE", statementModel.getTableName(), "Unexpected table name");
        assertFalse(statementModel.returningClauseDetected(), "Statement should have no returning");
    }

    @Test
    void insert_values_quotedTable() {
        StatementIdentification statementModel = parseStatement(
                "insert into \"someTable\"(a, b, c) values('a', -1.23, a(a,aa))");

        assertEquals(INSERT, statementModel.getStatementType(), "Unexpected statement type");
        assertNull(statementModel.getSchema(), "Unexpected schema");
        assertEquals("someTable", statementModel.getTableName(), "Unexpected table name");
        assertFalse(statementModel.returningClauseDetected(), "Statement should have no returning");
    }

    @Test
    void insert_values_withReturning() {
        StatementIdentification statementModel = parseStatement(
                "insert into someTable(a, b, c) values('a', -1.23, a(a,aa)) returning id");

        assertEquals(INSERT, statementModel.getStatementType(), "Unexpected statement type");
        assertNull(statementModel.getSchema(), "Unexpected schema");
        assertEquals("SOMETABLE", statementModel.getTableName(), "Unexpected table name");
        assertTrue(statementModel.returningClauseDetected(), "Statement should have returning");
    }

    @Test
    void insert_values_withReturning_aliases() {
        StatementIdentification statementModel = parseStatement(
                "insert into someTable(a, b, c) values('a', -1.23, a(a,aa)) returning id as \"ID\", b,c no_as");

        assertEquals(INSERT, statementModel.getStatementType(), "Unexpected statement type");
        assertNull(statementModel.getSchema(), "Unexpected schema");
        assertEquals("SOMETABLE", statementModel.getTableName(), "Unexpected table name");
        assertTrue(statementModel.returningClauseDetected(), "Statement should have returning");
    }

    @Test
    void insert_values_commentedOutReturning_lineComment() {
        StatementIdentification statementModel = parseStatement(
                "insert into someTable(a, b, c) values('a', -1.23, a(a,aa)) -- returning id");

        assertEquals(INSERT, statementModel.getStatementType(), "Unexpected statement type");
        assertNull(statementModel.getSchema(), "Unexpected schema");
        assertEquals("SOMETABLE", statementModel.getTableName(), "Unexpected table name");
        assertFalse(statementModel.returningClauseDetected(), "Statement should not have returning");
    }

    @Test
    void insert_values_commentedOutReturning_blockComment() {
        StatementIdentification statementModel = parseStatement(
                "insert into someTable(a, b, c) values('a', -1.23, a(a,aa)) /* returning id */");

        assertEquals(INSERT, statementModel.getStatementType(), "Unexpected statement type");
        assertNull(statementModel.getSchema(), "Unexpected schema");
        assertEquals("SOMETABLE", statementModel.getTableName(), "Unexpected table name");
        assertFalse(statementModel.returningClauseDetected(), "Statement should not have returning");
    }

    @Test
    void insertIntoSelect() {
        StatementIdentification statementModel = parseStatement("Insert Into someTable Select * From anotherTable");

        assertEquals(INSERT, statementModel.getStatementType(), "Unexpected statement type");
        assertNull(statementModel.getSchema(), "Unexpected schema");
        assertEquals("SOMETABLE", statementModel.getTableName());
        assertFalse(statementModel.returningClauseDetected(), "Statement should have no returning");
    }

    @Test
    void insertIntoSelect_withReturning() {
        StatementIdentification statementModel =
                parseStatement("Insert Into someTable Select * From anotherTable returning id");

        assertEquals(INSERT, statementModel.getStatementType(), "Unexpected statement type");
        assertNull(statementModel.getSchema(), "Unexpected schema");
        assertEquals("SOMETABLE", statementModel.getTableName());
        assertTrue(statementModel.returningClauseDetected(), "Statement should have returning");
    }

    @Test
    void insertWithCase() {
        StatementIdentification statementModel = parseStatement(
                "Insert Into someTable ( col1, col2) values((case when a = 1 Then 2 else 3 end), 2)");

        assertEquals(INSERT, statementModel.getStatementType(), "Unexpected statement type");
        assertNull(statementModel.getSchema(), "Unexpected schema");
        assertEquals("SOMETABLE", statementModel.getTableName());
        assertFalse(statementModel.returningClauseDetected(), "Statement should have no returning");
    }

    @Test
    void insertReturningWithCase() {
        StatementIdentification statementModel = parseStatement(
                "Insert Into someTable ( col1, col2) values((case when a = 1 Then 2 else 3 end), 2) returning id");

        assertEquals(INSERT, statementModel.getStatementType(), "Unexpected statement type");
        assertNull(statementModel.getSchema(), "Unexpected schema");
        assertEquals("SOMETABLE", statementModel.getTableName());
        assertTrue(statementModel.returningClauseDetected(), "Statement should have returning");
    }

    @Test
    void insertDefaultValues() {
        StatementIdentification statementModel = parseStatement("INSERT INTO someTable DEFAULT VALUES");

        assertEquals(INSERT, statementModel.getStatementType(), "Unexpected statement type");
        assertNull(statementModel.getSchema(), "Unexpected schema");
        assertEquals("SOMETABLE", statementModel.getTableName());
        assertFalse(statementModel.returningClauseDetected(), "Statement should have no returning");
    }

    @Test
    void insertDefaultValues_withReturning() {
        StatementIdentification statementModel =
                parseStatement("INSERT INTO someTable DEFAULT VALUES RETURNING \"ID\"");

        assertEquals(INSERT, statementModel.getStatementType(), "Unexpected statement type");
        assertNull(statementModel.getSchema(), "Unexpected schema");
        assertEquals("SOMETABLE", statementModel.getTableName());
        assertTrue(statementModel.returningClauseDetected(), "Statement should have returning");
    }

    @Test
    void update() {
        StatementIdentification statementModel = parseStatement(
                "Update someTable Set col1 = 25, col2 = 'abc' Where 1=0");

        assertEquals(UPDATE, statementModel.getStatementType(), "Unexpected statement type");
        assertNull(statementModel.getSchema(), "Unexpected schema");
        assertEquals("SOMETABLE", statementModel.getTableName());
        assertFalse(statementModel.returningClauseDetected(), "Statement should not have returning");
    }

    @Test
    void update_quotedTableName() {
        StatementIdentification statementModel = parseStatement(
                "Update \"someTable\" Set col1 = 25, col2 = 'abc' Where 1=0");

        assertEquals(UPDATE, statementModel.getStatementType(), "Unexpected statement type");
        assertNull(statementModel.getSchema(), "Unexpected schema");
        assertEquals("someTable", statementModel.getTableName());
        assertFalse(statementModel.returningClauseDetected(), "Statement should not have returning");
    }

    @Test
    void update_quotedTableNameWithSpace() {
        StatementIdentification statementModel = parseStatement(
                "Update \"some Table\" Set col1 = 25, col2 = 'abc' Where 1=0");

        assertEquals(UPDATE, statementModel.getStatementType(), "Unexpected statement type");
        assertNull(statementModel.getSchema(), "Unexpected schema");
        assertEquals("some Table", statementModel.getTableName());
        assertFalse(statementModel.returningClauseDetected(), "Statement should not have returning");
    }

    @Test
    void update_withReturning() {
        StatementIdentification statementModel = parseStatement(
                "Update someTable Set col1 = 25, col2 = 'abc' Where 1=0 Returning col3");

        assertEquals(UPDATE, statementModel.getStatementType(), "Unexpected statement type");
        assertNull(statementModel.getSchema(), "Unexpected schema");
        assertEquals("SOMETABLE", statementModel.getTableName());
        assertTrue(statementModel.returningClauseDetected(), "Statement should have returning");
    }

    @Test
    void delete() {
        StatementIdentification statementModel = parseStatement(
                "DELETE FROM someTable Where 1=0");

        assertEquals(DELETE, statementModel.getStatementType(), "Unexpected statement type");
        assertNull(statementModel.getSchema(), "Unexpected schema");
        assertEquals("SOMETABLE", statementModel.getTableName());
        assertFalse(statementModel.returningClauseDetected(), "Statement should not have returning");
    }

    @Test
    void delete_quotedTableName() {
        StatementIdentification statementModel = parseStatement("delete from \"someTable\"");

        assertEquals(DELETE, statementModel.getStatementType(), "Unexpected statement type");
        assertNull(statementModel.getSchema(), "Unexpected schema");
        assertEquals("someTable", statementModel.getTableName());
        assertFalse(statementModel.returningClauseDetected(), "Statement should not have returning");
    }

    @Test
    void delete_withReturning() {
        StatementIdentification statementModel = parseStatement("Delete From someTable Returning col3");

        assertEquals(DELETE, statementModel.getStatementType(), "Unexpected statement type");
        assertNull(statementModel.getSchema(), "Unexpected schema");
        assertEquals("SOMETABLE", statementModel.getTableName());
        assertTrue(statementModel.returningClauseDetected(), "Statement should have returning");
    }

    @Test
    void delete_withWhere_withReturning() {
        StatementIdentification statementModel = parseStatement("Delete From someTable where 1 = 1 Returning col3");

        assertEquals(DELETE, statementModel.getStatementType(), "Unexpected statement type");
        assertNull(statementModel.getSchema(), "Unexpected schema");
        assertEquals("SOMETABLE", statementModel.getTableName());
        assertTrue(statementModel.returningClauseDetected(), "Statement should have returning");
    }

    @Test
    void select() {
        StatementIdentification statementModel = parseStatement("select * from RDB$DATABASE");

        assertEquals(SELECT, statementModel.getStatementType(), "Expected SELECT statement type");
        assertNull(statementModel.getSchema(), "Unexpected schema");
        assertNull(statementModel.getTableName(), "Expected no table name");
        assertFalse(statementModel.returningClauseDetected(), "Statement should not have returning");
    }

    @Test
    void insertWithQString() {
        StatementIdentification statementModel = parseStatement("insert into someTable values (Q'[a'bc]')");

        assertEquals(INSERT, statementModel.getStatementType(), "Unexpected statement type");
        assertNull(statementModel.getSchema(), "Unexpected schema");
        assertEquals("SOMETABLE", statementModel.getTableName());
        assertFalse(statementModel.returningClauseDetected(), "Statement should not have returning");
    }

    @Test
    void insertWithQStringWithReturning() {
        StatementIdentification statementModel =
                parseStatement("insert into someTable values (Q'[a'bc]') returning id, \"ABC\"");

        assertEquals(INSERT, statementModel.getStatementType(), "Unexpected statement type");
        assertNull(statementModel.getSchema(), "Unexpected schema");
        assertEquals("SOMETABLE", statementModel.getTableName());
        assertTrue(statementModel.returningClauseDetected(), "Statement should not have returning");
    }

    @Test
    void testQLiterals() {
        checkQLiteralsStartEnd('#', '#');
        checkQLiteralsStartEnd('x', 'x');
        checkQLiteralsStartEnd('!', '!');
        checkQLiteralsStartEnd('(', ')');
        checkQLiteralsStartEnd(')', ')');
        checkQLiteralsStartEnd('{', '}');
        checkQLiteralsStartEnd('}', '}');
        checkQLiteralsStartEnd('[', ']');
        checkQLiteralsStartEnd(']', ']');
        checkQLiteralsStartEnd('<', '>');
        checkQLiteralsStartEnd('>', '>');
    }

    @SuppressWarnings("resource")
    private void checkQLiteralsStartEnd(char start, char end) {
        final String input = "q'" + start + "a'bc" + end + "'";
        Token token = SqlTokenizer.withReservedWords(FirebirdReservedWords.latest())
                .of(input)
                .next();
        assertThat(token).isInstanceOf(StringLiteralToken.class);
        assertEquals(token.text(), input);
    }

    @Test
    void merge() {
        StatementIdentification statementModel = parseStatement("""
                MERGE INTO books b
                  USING purchases p
                  ON p.title = b.title and p.type = 'bk'
                  WHEN MATCHED THEN
                    UPDATE SET b.desc = b.desc || '; ' || p.desc
                  WHEN NOT MATCHED THEN
                    INSERT (title, desc, bought) values (p.title, p.desc, p.bought)""");

        assertEquals(MERGE, statementModel.getStatementType(), "Unexpected statement type");
        assertNull(statementModel.getSchema(), "Unexpected schema");
        assertEquals("BOOKS", statementModel.getTableName());
        assertFalse(statementModel.returningClauseDetected(), "Statement should not have returning");
    }

    @Test
    void merge_quotedTableName() {
        StatementIdentification statementModel = parseStatement("""
                MERGE INTO "books" b
                  USING purchases p
                  ON p.title = b.title and p.type = q'<bk>'
                  WHEN MATCHED THEN
                    UPDATE SET b.desc = b.desc || '; ' || p.desc
                  WHEN NOT MATCHED THEN
                    INSERT (title, desc, bought) values (p.title, p.desc, p.bought)""");

        assertEquals(MERGE, statementModel.getStatementType(), "Unexpected statement type");
        assertNull(statementModel.getSchema(), "Unexpected schema");
        assertEquals("books", statementModel.getTableName());
        assertFalse(statementModel.returningClauseDetected(), "Statement should not have returning");
    }

    @Test
    void merge_quotedTableNameWithSpace() {
        StatementIdentification statementModel = parseStatement("""
                MERGE INTO "more books" b
                  USING purchases p
                  ON p.title = b.title and p.type = q'<bk>'
                  WHEN MATCHED THEN
                    UPDATE SET b.desc = b.desc || '; ' || p.desc
                  WHEN NOT MATCHED THEN
                    INSERT (title, desc, bought) values (p.title, p.desc, p.bought)""");

        assertEquals(MERGE, statementModel.getStatementType(), "Unexpected statement type");
        assertNull(statementModel.getSchema(), "Unexpected schema");
        assertEquals("more books", statementModel.getTableName());
        assertFalse(statementModel.returningClauseDetected(), "Statement should not have returning");
    }

    @Test
    void merge_withReturning() {
        StatementIdentification statementModel = parseStatement("""
                MERGE INTO books b
                  USING purchases p
                  ON p.title = b.title and p.type = q'<bk>'
                  WHEN MATCHED THEN
                    UPDATE SET b.desc = b.desc || '; ' || p.desc
                  WHEN NOT MATCHED THEN
                    INSERT (title, desc, bought) values (p.title, p.desc, p.bought)
                  RETURNING id, "OTHER COLUMN"
                """);

        assertEquals(MERGE, statementModel.getStatementType(), "Unexpected statement type");
        assertNull(statementModel.getSchema(), "Unexpected schema");
        assertEquals("BOOKS", statementModel.getTableName());
        assertTrue(statementModel.returningClauseDetected(), "Statement should have returning");
    }

    @ParameterizedTest
    @MethodSource("testData")
    void testParser(boolean expectedReturning, LocalStatementType expectedStatementType, String expectedSchema,
            String expectedTableName, String statementText) {
        StatementIdentification statementIdentification = parseStatement(statementText);

        assertEquals(expectedReturning, statementIdentification.returningClauseDetected(),
                "returningClauseDetected for: " + statementText);
        assertEquals(expectedStatementType, statementIdentification.getStatementType(),
                "statementType for: " + statementText);
        assertEquals(expectedSchema, statementIdentification.getSchema(), "schema for: " + statementText);
        assertEquals(expectedTableName, statementIdentification.getTableName(), "tableName for: " + statementText);
    }

    static Stream<Arguments> testData() {
        return Stream.of(
// @formatter:off
        /* 0*/  testCase(false, SELECT, null, null, "select * from rdb$database"),
        /* 1*/  testCase(false, INSERT, null, "TABLE", "insert into \"TABLE\" (x, y, z) values ('ab', ?, Q'[xyz]')"),
        /* 2*/  testCase(true, INSERT, null, "TABLE",
                        "insert into \"TABLE\" (x, y, z) values ('ab', ?, Q'[xyz]') returning id"),
        /* 3*/  testCase(false, UPDATE, null, "SOMETABLE",
                        "update sometable set x = ?, y = Q'[xy'z]' where a and b > 1"),
        /* 4*/  testCase(true, UPDATE, null, "SOMETABLE",
                        "update SOMETABLE set x = ?, y = Q'[xy'z]' where a and b > 1 returning \"A\" as a"),
        /* 5*/  testCase(false, DELETE, null, "SOMETABLE", "DELETE FROM sometable where x"),
        /* 6*/  testCase(true, DELETE, null, "SOMETABLE", """
                        DELETE FROM sometable
                          where x = (select y from "TABLE"
                            where startdate = {d'2018-05-1'})
                          returning x, a, b "A\""""),
        /* 7*/  testCase(false, UPDATE_OR_INSERT, null, "COWS", """
                        UPDATE OR INSERT INTO Cows (Name, Number, Location)
                          VALUES ('Suzy Creamcheese', 3278823, 'Green Pastures')
                          MATCHING (Number);"""),
        /* 8*/  testCase(true, UPDATE_OR_INSERT, null, "COWS", """
                        UPDATE OR INSERT INTO Cows (Name, Number, Location)
                          VALUES ('Suzy Creamcheese', 3278823, 'Green Pastures')
                          MATCHING (Number)
                          RETURNING rec_id;"""),
        /* 9*/  testCase(false, MERGE, null, "CUSTOMERS", """
                        MERGE INTO customers c
                          USING
                            (SELECT * FROM customers_delta WHERE id > 10) cd
                             ON (c.id = cd.id)
                          WHEN MATCHED THEN
                            UPDATE SET name = cd.name
                          WHEN NOT MATCHED THEN
                            INSERT (id, name)
                            VALUES (cd.id, cd.name)"""),
        /*10*/  testCase(true, MERGE, null, "CUSTOMERS", """
                        MERGE INTO customers c
                          USING
                            (SELECT * FROM customers_delta WHERE id > 10) cd
                             ON (c.id = cd.id)
                          WHEN MATCHED THEN
                            UPDATE SET name = cd.name
                          WHEN NOT MATCHED THEN
                            INSERT (id, name)
                            VALUES (cd.id, cd.name)
                          RETURNING id"""),
        /*11*/  testCase(false, MERGE, null, "CUSTOMERS", """
                        MERGE INTO customers c
                          USING
                            (SELECT * FROM customers_delta WHERE id > 10) cd
                             ON (c.id = cd.id)
                          WHEN MATCHED THEN
                            UPDATE SET name = cd.name
                          WHEN NOT MATCHED THEN
                            INSERT (id, name)
                            VALUES (cd.id, cd.name)
                         -- RETURNING id"""),
        /*12*/  testCase(true, INSERT, null, "SOMETABLE", "insert into sometable default values returning *"),
        /*13*/  testCase(true, INSERT, null, "SOMETABLE", "insert into sometable default values returning sometable.*"),
        /*14*/  testCase(true, INSERT, null, "SOMETABLE", "insert into sometable(x, y, z) values(default, 1, 2) returning *"),
        /*15*/  testCase(true, INSERT, null, "SOMETABLE",
                        "insert into sometable(x, y, z) values(default, 1, 2) returning sometable.*"),
        /*16*/  testCase(true, UPDATE, null, "SOMETABLE", "update sometable set x = ? returning *"),
        /*17*/  testCase(true, UPDATE, null, "SOMETABLE", "update sometable set x = ? returning sometable.*"),
        /*18*/  testCase(true, UPDATE, null, "SOMETABLE", "update sometable a set x = ? returning a.*"),
        /*19*/  testCase(true, UPDATE, null, "SOMETABLE", "update sometable as a set a.x = ? returning *"),
        /*20*/  testCase(true, UPDATE, null, "SOMETABLE", "update sometable as a set a.x = ? returning a.id"),
        /*21*/  testCase(true, DELETE, null, "SOMETABLE", "delete from sometable where x = ? returning *"),
        /*22*/  testCase(true, DELETE, null, "SOMETABLE", "delete from sometable a where a.x = ? returning a.*"),
        /*23*/  testCase(true, DELETE, null, "SOMETABLE", "delete from sometable a where a.x = ? returning *"),
        /*24*/  testCase(true, DELETE, null, "SOMETABLE", "delete from sometable as a where a.x = ? returning a.id"),
        /*25*/  testCase(true, DELETE, null, "SOMETABLE", "delete from sometable as a where a.x = ? returning *"),
        /*26*/  testCase(true, MERGE, null, "CUSTOMERS", """
                        MERGE INTO customers
                          USING
                            (SELECT * FROM customers_delta WHERE id > 10) cd
                             ON (c.id = cd.id)
                          WHEN MATCHED THEN
                            UPDATE SET name = cd.name
                          WHEN NOT MATCHED THEN
                            INSERT (id, name)
                            VALUES (cd.id, cd.name)
                          RETURNING *"""),
        /*27*/  testCase(true, MERGE, null, "CUSTOMERS", """
                        MERGE INTO customers as c
                          USING
                            (SELECT * FROM customers_delta WHERE id > 10) cd
                             ON (c.id = cd.id)
                          WHEN MATCHED THEN
                            UPDATE SET name = cd.name
                          WHEN NOT MATCHED THEN
                            INSERT (id, name)
                            VALUES (cd.id, cd.name)
                          RETURNING *"""),
        /*28*/  testCase(true, MERGE, null, "CUSTOMERS", """
                        MERGE INTO customers c
                          USING
                            (SELECT * FROM customers_delta WHERE id > 10) cd
                             ON (c.id = cd.id)
                          WHEN MATCHED THEN
                            UPDATE SET name = cd.name
                          WHEN NOT MATCHED THEN
                            INSERT (id, name)
                            VALUES (cd.id, cd.name)
                          RETURNING c.*"""),
        /*29*/  testCase(true, MERGE, null, "CUSTOMERS", """
                        MERGE INTO customers c
                          USING
                            (SELECT * FROM customers_delta WHERE id > 10) cd
                             ON (c.id = cd.id)
                          WHEN MATCHED THEN
                            UPDATE SET name = cd.name
                          WHEN NOT MATCHED THEN
                            INSERT (id, name)
                            VALUES (cd.id, cd.name)
                          RETURNING c.id"""),
        // cases with schema
        /*30*/  testCase(false, INSERT, "other", "table",
                        "insert into \"other\".\"table\" (x, y, z) values ('ab', ?, Q'[xyz]')"),
        /*31*/  testCase(false, UPDATE, "PUBLIC", "SOMETABLE",
                        "update public.sometable set x = ?, y = Q'[xy'z]' where a and b > 1"),
        /*32*/  testCase(false, DELETE, "OTHER", "SOMETABLE", "DELETE FROM other.sometable where x"),
        /*33*/  testCase(false, UPDATE_OR_INSERT, "PUBLIC", "COWS", """
                        UPDATE OR INSERT INTO public.Cows as x (Name, Number, Location)
                          VALUES ('Suzy Creamcheese', 3278823, 'Green Pastures')
                          MATCHING (x.Number);"""),
        /*34*/  testCase(false, MERGE, "OTHER", "CUSTOMERS", """
                        MERGE INTO "OTHER".customers c
                          USING
                            (SELECT * FROM customers_delta WHERE id > 10) cd
                             ON (c.id = cd.id)
                          WHEN MATCHED THEN
                            UPDATE SET name = cd.name
                          WHEN NOT MATCHED THEN
                            INSERT (id, name)
                            VALUES (cd.id, cd.name)"""),
        /*35*/  testCase(true, INSERT, "PUBLIC", "SOMETABLE",
                        "insert into public.sometable default values returning id, val"),
        /*36*/  testCase(true, UPDATE, "OTHER", "SOMETABLE",
                        "update OTHER.SOMETABLE set x = ?, y = Q'[xy'z]' where a and b > 1 returning \"A\" as a"),
        /*37*/  testCase(true, DELETE, "PUBLIC", "SOMETABLE", """
                        DELETE FROM PUBLIC.sometable
                          where x = (select y from "TABLE"
                            where startdate = {d'2018-05-1'})
                          returning x, a, b "A\""""),
        /*38*/  testCase(true, MERGE, "with\"quote", "CUSTOMERS", """
                        MERGE INTO "with""quote".customers
                          USING
                            (SELECT * FROM customers_delta WHERE id > 10) cd
                             ON (c.id = cd.id)
                          WHEN MATCHED THEN
                            UPDATE SET name = cd.name
                          WHEN NOT MATCHED THEN
                            INSERT (id, name)
                            VALUES (cd.id, cd.name)
                          RETURNING *""")
// @formatter:on
        );
    }

    private static Arguments testCase(boolean expectedReturning, LocalStatementType expectedStatementType,
            String expectedSchema, String expectedTableName, String statementText) {
        return Arguments.of(expectedReturning, expectedStatementType, expectedSchema, expectedTableName, statementText);
    }

}
