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
package org.firebirdsql.jaybird.parser;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GrammarTest {

    private StatementIdentification parseStatement(String testString) {
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

        assertEquals(StatementType.INSERT, statementModel.getStatementType(), "Unexpected statement type");
        assertEquals("someTable", statementModel.getTableName(), "Unexpected table name");
        assertFalse(statementModel.returningClauseDetected(), "Statement should have no returning");
    }

    @Test
    void insert_values_quotedTable() {
        StatementIdentification statementModel = parseStatement(
                "insert into \"someTable\"(a, b, c) values('a', -1.23, a(a,aa))");

        assertEquals(StatementType.INSERT, statementModel.getStatementType(), "Unexpected statement type");
        assertEquals("\"someTable\"", statementModel.getTableName(), "Unexpected table name");
        assertFalse(statementModel.returningClauseDetected(), "Statement should have no returning");
    }

    @Test
    void insert_values_withReturning() {
        StatementIdentification statementModel = parseStatement(
                "insert into someTable(a, b, c) values('a', -1.23, a(a,aa)) returning id");

        assertEquals(StatementType.INSERT, statementModel.getStatementType(), "Unexpected statement type");
        assertEquals("someTable", statementModel.getTableName(), "Unexpected table name");
        assertTrue(statementModel.returningClauseDetected(), "Statement should have returning");
    }

    @Test
    void insert_values_withReturning_aliases() {
        StatementIdentification statementModel = parseStatement(
                "insert into someTable(a, b, c) values('a', -1.23, a(a,aa)) returning id as \"ID\", b,c no_as");

        assertEquals(StatementType.INSERT, statementModel.getStatementType(), "Unexpected statement type");
        assertEquals("someTable", statementModel.getTableName(), "Unexpected table name");
        assertTrue(statementModel.returningClauseDetected(), "Statement should have returning");
    }

    @Test
    void insert_values_commentedOutReturning_lineComment() {
        StatementIdentification statementModel = parseStatement(
                "insert into someTable(a, b, c) values('a', -1.23, a(a,aa)) -- returning id");

        assertEquals(StatementType.INSERT, statementModel.getStatementType(), "Unexpected statement type");
        assertEquals("someTable", statementModel.getTableName(), "Unexpected table name");
        assertFalse(statementModel.returningClauseDetected(), "Statement should not have returning");
    }

    @Test
    void insert_values_commentedOutReturning_blockComment() {
        StatementIdentification statementModel = parseStatement(
                "insert into someTable(a, b, c) values('a', -1.23, a(a,aa)) /* returning id */");

        assertEquals(StatementType.INSERT, statementModel.getStatementType(), "Unexpected statement type");
        assertEquals("someTable", statementModel.getTableName(), "Unexpected table name");
        assertFalse(statementModel.returningClauseDetected(), "Statement should not have returning");
    }

    @Test
    void insertIntoSelect() {
        StatementIdentification statementModel = parseStatement("Insert Into someTable Select * From anotherTable");

        assertEquals(StatementType.INSERT, statementModel.getStatementType(), "Unexpected statement type");
        assertEquals("someTable", statementModel.getTableName());
        assertFalse(statementModel.returningClauseDetected(), "Statement should have no returning");
    }

    @Test
    void insertIntoSelect_withReturning() {
        StatementIdentification statementModel =
                parseStatement("Insert Into someTable Select * From anotherTable returning id");

        assertEquals(StatementType.INSERT, statementModel.getStatementType(), "Unexpected statement type");
        assertEquals("someTable", statementModel.getTableName());
        assertTrue(statementModel.returningClauseDetected(), "Statement should have returning");
    }

    @Test
    void insertWithCase() {
        StatementIdentification statementModel = parseStatement(
                "Insert Into someTable ( col1, col2) values((case when a = 1 Then 2 else 3 end), 2)");

        assertEquals(StatementType.INSERT, statementModel.getStatementType(), "Unexpected statement type");
        assertEquals("someTable", statementModel.getTableName());
        assertFalse(statementModel.returningClauseDetected(), "Statement should have no returning");
    }

    @Test
    void insertReturningWithCase() {
        StatementIdentification statementModel = parseStatement(
                "Insert Into someTable ( col1, col2) values((case when a = 1 Then 2 else 3 end), 2) returning id");

        assertEquals(StatementType.INSERT, statementModel.getStatementType(), "Unexpected statement type");
        assertEquals("someTable", statementModel.getTableName());
        assertTrue(statementModel.returningClauseDetected(), "Statement should have returning");
    }

    @Test
    void insertDefaultValues() {
        StatementIdentification statementModel = parseStatement("INSERT INTO someTable DEFAULT VALUES");

        assertEquals(StatementType.INSERT, statementModel.getStatementType(), "Unexpected statement type");
        assertEquals("someTable", statementModel.getTableName());
        assertFalse(statementModel.returningClauseDetected(), "Statement should have no returning");
    }

    @Test
    void insertDefaultValues_withReturning() {
        StatementIdentification statementModel =
                parseStatement("INSERT INTO someTable DEFAULT VALUES RETURNING \"ID\"");

        assertEquals(StatementType.INSERT, statementModel.getStatementType(), "Unexpected statement type");
        assertEquals("someTable", statementModel.getTableName());
        assertTrue(statementModel.returningClauseDetected(), "Statement should have returning");
    }

    @Test
    void update() {
        StatementIdentification statementModel = parseStatement(
                "Update someTable Set col1 = 25, col2 = 'abc' Where 1=0");

        assertEquals(StatementType.UPDATE, statementModel.getStatementType(), "Unexpected statement type");
        assertEquals("someTable", statementModel.getTableName());
        assertFalse(statementModel.returningClauseDetected(), "Statement should not have returning");
    }

    @Test
    void update_quotedTableName() {
        StatementIdentification statementModel = parseStatement(
                "Update \"someTable\" Set col1 = 25, col2 = 'abc' Where 1=0");

        assertEquals(StatementType.UPDATE, statementModel.getStatementType(), "Unexpected statement type");
        assertEquals("\"someTable\"", statementModel.getTableName());
        assertFalse(statementModel.returningClauseDetected(), "Statement should not have returning");
    }

    @Test
    void update_quotedTableNameWithSpace() {
        StatementIdentification statementModel = parseStatement(
                "Update \"some Table\" Set col1 = 25, col2 = 'abc' Where 1=0");

        assertEquals(StatementType.UPDATE, statementModel.getStatementType(), "Unexpected statement type");
        assertEquals("\"some Table\"", statementModel.getTableName());
        assertFalse(statementModel.returningClauseDetected(), "Statement should not have returning");
    }

    @Test
    void update_withReturning() {
        StatementIdentification statementModel = parseStatement(
                "Update someTable Set col1 = 25, col2 = 'abc' Where 1=0 Returning col3");

        assertEquals(StatementType.UPDATE, statementModel.getStatementType(), "Unexpected statement type");
        assertEquals("someTable", statementModel.getTableName());
        assertTrue(statementModel.returningClauseDetected(), "Statement should have returning");
    }

    @Test
    void delete() {
        StatementIdentification statementModel = parseStatement(
                "DELETE FROM someTable Where 1=0");

        assertEquals(StatementType.DELETE, statementModel.getStatementType(), "Unexpected statement type");
        assertEquals("someTable", statementModel.getTableName());
        assertFalse(statementModel.returningClauseDetected(), "Statement should not have returning");
    }

    @Test
    void delete_quotedTableName() {
        StatementIdentification statementModel = parseStatement("delete from \"someTable\"");

        assertEquals(StatementType.DELETE, statementModel.getStatementType(), "Unexpected statement type");
        assertEquals("\"someTable\"", statementModel.getTableName());
        assertFalse(statementModel.returningClauseDetected(), "Statement should not have returning");
    }

    @Test
    void delete_withReturning() {
        StatementIdentification statementModel = parseStatement("Delete From someTable Returning col3");

        assertEquals(StatementType.DELETE, statementModel.getStatementType(), "Unexpected statement type");
        assertEquals("someTable", statementModel.getTableName());
        assertTrue(statementModel.returningClauseDetected(), "Statement should have returning");
    }

    @Test
    void delete_withWhere_withReturning() {
        StatementIdentification statementModel = parseStatement("Delete From someTable where 1 = 1 Returning col3");

        assertEquals(StatementType.DELETE, statementModel.getStatementType(), "Unexpected statement type");
        assertEquals("someTable", statementModel.getTableName());
        assertTrue(statementModel.returningClauseDetected(), "Statement should have returning");
    }

    @Test
    void select() {
        StatementIdentification statementModel = parseStatement("select * from RDB$DATABASE");

        assertEquals(StatementType.SELECT, statementModel.getStatementType(), "Expected SELECT statement type");
        assertNull(statementModel.getTableName(), "Expected no table name");
        assertFalse(statementModel.returningClauseDetected(), "Statement should not have returning");
    }

    @Test
    void insertWithQString() {
        StatementIdentification statementModel = parseStatement("insert into someTable values (Q'[a'bc]')");

        assertEquals(StatementType.INSERT, statementModel.getStatementType(), "Unexpected statement type");
        assertEquals("someTable", statementModel.getTableName());
        assertFalse(statementModel.returningClauseDetected(), "Statement should not have returning");
    }

    @Test
    void insertWithQStringWithReturning() {
        StatementIdentification statementModel =
                parseStatement("insert into someTable values (Q'[a'bc]') returning id, \"ABC\"");

        assertEquals(StatementType.INSERT, statementModel.getStatementType(), "Unexpected statement type");
        assertEquals("someTable", statementModel.getTableName());
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
        StatementIdentification statementModel = parseStatement(
                "MERGE INTO books b\n"
                        + "  USING purchases p\n"
                        + "  ON p.title = b.title and p.type = 'bk'\n"
                        + "  WHEN MATCHED THEN\n"
                        + "    UPDATE SET b.desc = b.desc || '; ' || p.desc\n"
                        + "  WHEN NOT MATCHED THEN\n"
                        + "    INSERT (title, desc, bought) values (p.title, p.desc, p.bought)");

        assertEquals(StatementType.MERGE, statementModel.getStatementType(), "Unexpected statement type");
        assertEquals("books", statementModel.getTableName());
        assertFalse(statementModel.returningClauseDetected(), "Statement should not have returning");
    }

    @Test
    void merge_quotedTableName() {
        StatementIdentification statementModel = parseStatement(
                "MERGE INTO \"books\" b\n"
                        + "  USING purchases p\n"
                        + "  ON p.title = b.title and p.type = q'<bk>'\n"
                        + "  WHEN MATCHED THEN\n"
                        + "    UPDATE SET b.desc = b.desc || '; ' || p.desc\n"
                        + "  WHEN NOT MATCHED THEN\n"
                        + "    INSERT (title, desc, bought) values (p.title, p.desc, p.bought)");

        assertEquals(StatementType.MERGE, statementModel.getStatementType(), "Unexpected statement type");
        assertEquals("\"books\"", statementModel.getTableName());
        assertFalse(statementModel.returningClauseDetected(), "Statement should not have returning");
    }

    @Test
    void merge_quotedTableNameWithSpace() {
        StatementIdentification statementModel = parseStatement(
                "MERGE INTO \"more books\" b\n"
                        + "  USING purchases p\n"
                        + "  ON p.title = b.title and p.type = q'<bk>'\n"
                        + "  WHEN MATCHED THEN\n"
                        + "    UPDATE SET b.desc = b.desc || '; ' || p.desc\n"
                        + "  WHEN NOT MATCHED THEN\n"
                        + "    INSERT (title, desc, bought) values (p.title, p.desc, p.bought)");

        assertEquals(StatementType.MERGE, statementModel.getStatementType(), "Unexpected statement type");
        assertEquals("\"more books\"", statementModel.getTableName());
        assertFalse(statementModel.returningClauseDetected(), "Statement should not have returning");
    }

    @Test
    void merge_withReturning() {
        StatementIdentification statementModel = parseStatement(
                "MERGE INTO books b\n"
                        + "  USING purchases p\n"
                        + "  ON p.title = b.title and p.type = q'<bk>'\n"
                        + "  WHEN MATCHED THEN\n"
                        + "    UPDATE SET b.desc = b.desc || '; ' || p.desc\n"
                        + "  WHEN NOT MATCHED THEN\n"
                        + "    INSERT (title, desc, bought) values (p.title, p.desc, p.bought)\n"
                        + "  RETURNING id, \"OTHER COLUMN\"");

        assertEquals(StatementType.MERGE, statementModel.getStatementType(), "Unexpected statement type");
        assertEquals("books", statementModel.getTableName());
        assertTrue(statementModel.returningClauseDetected(), "Statement should have returning");
    }

}
