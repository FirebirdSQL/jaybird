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

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class GrammarTest {

    private StatementIdentification parseStatement(String testString) {
        StatementDetector statementDetector = new StatementDetector();
        SqlParser.withReservedWords(FirebirdReservedWords.latest())
                .withVisitor(statementDetector)
                .of(testString)
                .parse();
        return statementDetector.toStatementIdentification();
    }

    @Test
    public void insert_values() {
        StatementIdentification statementModel = parseStatement(
                "insert into someTable(a, \"\u0442\u0435\"\"\u0441\u0442\", aaa) values('a', -1.23, a(a,aa))");

        assertEquals("Unexpected statement type", LocalStatementType.INSERT, statementModel.getStatementType());
        assertEquals("Unexpected table name", "someTable", statementModel.getTableName());
        assertFalse("Statement should have no returning", statementModel.returningClauseDetected());
    }

    @Test
    public void insert_values_quotedTable() {
        StatementIdentification statementModel = parseStatement(
                "insert into \"someTable\"(a, b, c) values('a', -1.23, a(a,aa))");

        assertEquals("Unexpected statement type", LocalStatementType.INSERT, statementModel.getStatementType());
        assertEquals("Unexpected table name", "\"someTable\"", statementModel.getTableName());
        assertFalse("Statement should have no returning", statementModel.returningClauseDetected());
    }

    @Test
    public void insert_values_withReturning() {
        StatementIdentification statementModel = parseStatement(
                "insert into someTable(a, b, c) values('a', -1.23, a(a,aa)) returning id");

        assertEquals("Unexpected statement type", LocalStatementType.INSERT, statementModel.getStatementType());
        assertEquals("Unexpected table name", "someTable", statementModel.getTableName());
        assertTrue("Statement should have returning", statementModel.returningClauseDetected());
    }

    @Test
    public void insert_values_withReturning_aliases() {
        StatementIdentification statementModel = parseStatement(
                "insert into someTable(a, b, c) values('a', -1.23, a(a,aa)) returning id as \"ID\", b,c no_as");

        assertEquals("Unexpected statement type", LocalStatementType.INSERT, statementModel.getStatementType());
        assertEquals("Unexpected table name", "someTable", statementModel.getTableName());
        assertTrue("Statement should have returning", statementModel.returningClauseDetected());
    }

    @Test
    public void insert_values_commentedOutReturning_lineComment() {
        StatementIdentification statementModel = parseStatement(
                "insert into someTable(a, b, c) values('a', -1.23, a(a,aa)) -- returning id");

        assertEquals("Unexpected statement type", LocalStatementType.INSERT, statementModel.getStatementType());
        assertEquals("Unexpected table name", "someTable", statementModel.getTableName());
        assertFalse("Statement should not have returning", statementModel.returningClauseDetected());
    }

    @Test
    public void insert_values_commentedOutReturning_blockComment() {
        StatementIdentification statementModel = parseStatement(
                "insert into someTable(a, b, c) values('a', -1.23, a(a,aa)) /* returning id */");

        assertEquals("Unexpected statement type", LocalStatementType.INSERT, statementModel.getStatementType());
        assertEquals("Unexpected table name", "someTable", statementModel.getTableName());
        assertFalse("Statement should not have returning", statementModel.returningClauseDetected());
    }

    @Test
    public void insertIntoSelect() {
        StatementIdentification statementModel = parseStatement("Insert Into someTable Select * From anotherTable");

        assertEquals("Unexpected statement type", LocalStatementType.INSERT, statementModel.getStatementType());
        assertEquals("someTable", statementModel.getTableName());
        assertFalse("Statement should have no returning", statementModel.returningClauseDetected());
    }

    @Test
    public void insertIntoSelect_withReturning() {
        StatementIdentification statementModel =
                parseStatement("Insert Into someTable Select * From anotherTable returning id");

        assertEquals("Unexpected statement type", LocalStatementType.INSERT, statementModel.getStatementType());
        assertEquals("someTable", statementModel.getTableName());
        assertTrue("Statement should have returning", statementModel.returningClauseDetected());
    }

    @Test
    public void insertWithCase() {
        StatementIdentification statementModel = parseStatement(
                "Insert Into someTable ( col1, col2) values((case when a = 1 Then 2 else 3 end), 2)");

        assertEquals("Unexpected statement type", LocalStatementType.INSERT, statementModel.getStatementType());
        assertEquals("someTable", statementModel.getTableName());
        assertFalse("Statement should have no returning", statementModel.returningClauseDetected());
    }

    @Test
    public void insertReturningWithCase() {
        StatementIdentification statementModel = parseStatement(
                "Insert Into someTable ( col1, col2) values((case when a = 1 Then 2 else 3 end), 2) returning id");

        assertEquals("Unexpected statement type", LocalStatementType.INSERT, statementModel.getStatementType());
        assertEquals("someTable", statementModel.getTableName());
        assertTrue("Statement should have returning", statementModel.returningClauseDetected());
    }

    @Test
    public void insertDefaultValues() {
        StatementIdentification statementModel = parseStatement("INSERT INTO someTable DEFAULT VALUES");

        assertEquals("Unexpected statement type", LocalStatementType.INSERT, statementModel.getStatementType());
        assertEquals("someTable", statementModel.getTableName());
        assertFalse("Statement should have no returning", statementModel.returningClauseDetected());
    }

    @Test
    public void insertDefaultValues_withReturning() {
        StatementIdentification statementModel =
                parseStatement("INSERT INTO someTable DEFAULT VALUES RETURNING \"ID\"");

        assertEquals("Unexpected statement type", LocalStatementType.INSERT, statementModel.getStatementType());
        assertEquals("someTable", statementModel.getTableName());
        assertTrue("Statement should have returning", statementModel.returningClauseDetected());
    }

    @Test
    public void update() {
        StatementIdentification statementModel = parseStatement(
                "Update someTable Set col1 = 25, col2 = 'abc' Where 1=0");

        assertEquals("Unexpected statement type", LocalStatementType.UPDATE, statementModel.getStatementType());
        assertEquals("someTable", statementModel.getTableName());
        assertFalse("Statement should not have returning", statementModel.returningClauseDetected());
    }

    @Test
    public void update_quotedTableName() {
        StatementIdentification statementModel = parseStatement(
                "Update \"someTable\" Set col1 = 25, col2 = 'abc' Where 1=0");

        assertEquals("Unexpected statement type", LocalStatementType.UPDATE, statementModel.getStatementType());
        assertEquals("\"someTable\"", statementModel.getTableName());
        assertFalse("Statement should not have returning", statementModel.returningClauseDetected());
    }

    @Test
    public void update_quotedTableNameWithSpace() {
        StatementIdentification statementModel = parseStatement(
                "Update \"some Table\" Set col1 = 25, col2 = 'abc' Where 1=0");

        assertEquals("Unexpected statement type", LocalStatementType.UPDATE, statementModel.getStatementType());
        assertEquals("\"some Table\"", statementModel.getTableName());
        assertFalse("Statement should not have returning", statementModel.returningClauseDetected());
    }

    @Test
    public void update_withReturning() {
        StatementIdentification statementModel = parseStatement(
                "Update someTable Set col1 = 25, col2 = 'abc' Where 1=0 Returning col3");

        assertEquals("Unexpected statement type", LocalStatementType.UPDATE, statementModel.getStatementType());
        assertEquals("someTable", statementModel.getTableName());
        assertTrue("Statement should have returning", statementModel.returningClauseDetected());
    }

    @Test
    public void delete() {
        StatementIdentification statementModel = parseStatement(
                "DELETE FROM someTable Where 1=0");

        assertEquals("Unexpected statement type", LocalStatementType.DELETE, statementModel.getStatementType());
        assertEquals("someTable", statementModel.getTableName());
        assertFalse("Statement should not have returning", statementModel.returningClauseDetected());
    }

    @Test
    public void delete_quotedTableName() {
        StatementIdentification statementModel = parseStatement("delete from \"someTable\"");

        assertEquals("Unexpected statement type", LocalStatementType.DELETE, statementModel.getStatementType());
        assertEquals("\"someTable\"", statementModel.getTableName());
        assertFalse("Statement should not have returning", statementModel.returningClauseDetected());
    }

    @Test
    public void delete_withReturning() {
        StatementIdentification statementModel = parseStatement("Delete From someTable Returning col3");

        assertEquals("Unexpected statement type", LocalStatementType.DELETE, statementModel.getStatementType());
        assertEquals("someTable", statementModel.getTableName());
        assertTrue("Statement should have returning", statementModel.returningClauseDetected());
    }

    @Test
    public void delete_withWhere_withReturning() {
        StatementIdentification statementModel = parseStatement("Delete From someTable where 1 = 1 Returning col3");

        assertEquals("Unexpected statement type", LocalStatementType.DELETE, statementModel.getStatementType());
        assertEquals("someTable", statementModel.getTableName());
        assertTrue("Statement should have returning", statementModel.returningClauseDetected());
    }

    @Test
    public void select() {
        StatementIdentification statementModel = parseStatement("select * from RDB$DATABASE");

        assertEquals("Expected SELECT statement type", LocalStatementType.SELECT, statementModel.getStatementType());
        assertNull("Expected no table name", statementModel.getTableName());
        assertFalse("Statement should not have returning", statementModel.returningClauseDetected());
    }

    @Test
    public void insertWithQString() {
        StatementIdentification statementModel = parseStatement("insert into someTable values (Q'[a'bc]')");

        assertEquals("Unexpected statement type", LocalStatementType.INSERT, statementModel.getStatementType());
        assertEquals("someTable", statementModel.getTableName());
        assertFalse("Statement should not have returning", statementModel.returningClauseDetected());
    }

    @Test
    public void insertWithQStringWithReturning() {
        StatementIdentification statementModel =
                parseStatement("insert into someTable values (Q'[a'bc]') returning id, \"ABC\"");

        assertEquals("Unexpected statement type", LocalStatementType.INSERT, statementModel.getStatementType());
        assertEquals("someTable", statementModel.getTableName());
        assertTrue("Statement should not have returning", statementModel.returningClauseDetected());
    }

    @Test
    public void testQLiterals() {
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

        assertThat(token, instanceOf(StringLiteralToken.class));
        assertEquals(token.text(), input);
    }

    @Test
    public void merge() {
        StatementIdentification statementModel = parseStatement(
                "MERGE INTO books b\n"
                        + "  USING purchases p\n"
                        + "  ON p.title = b.title and p.type = 'bk'\n"
                        + "  WHEN MATCHED THEN\n"
                        + "    UPDATE SET b.desc = b.desc || '; ' || p.desc\n"
                        + "  WHEN NOT MATCHED THEN\n"
                        + "    INSERT (title, desc, bought) values (p.title, p.desc, p.bought)");

        assertEquals("Unexpected statement type", LocalStatementType.MERGE, statementModel.getStatementType());
        assertEquals("books", statementModel.getTableName());
        assertFalse("Statement should not have returning", statementModel.returningClauseDetected());
    }

    @Test
    public void merge_quotedTableName() {
        StatementIdentification statementModel = parseStatement(
                "MERGE INTO \"books\" b\n"
                        + "  USING purchases p\n"
                        + "  ON p.title = b.title and p.type = q'<bk>'\n"
                        + "  WHEN MATCHED THEN\n"
                        + "    UPDATE SET b.desc = b.desc || '; ' || p.desc\n"
                        + "  WHEN NOT MATCHED THEN\n"
                        + "    INSERT (title, desc, bought) values (p.title, p.desc, p.bought)");

        assertEquals("Unexpected statement type", LocalStatementType.MERGE, statementModel.getStatementType());
        assertEquals("\"books\"", statementModel.getTableName());
        assertFalse("Statement should not have returning", statementModel.returningClauseDetected());
    }

    @Test
    public void merge_quotedTableNameWithSpace() {
        StatementIdentification statementModel = parseStatement(
                "MERGE INTO \"more books\" b\n"
                        + "  USING purchases p\n"
                        + "  ON p.title = b.title and p.type = q'<bk>'\n"
                        + "  WHEN MATCHED THEN\n"
                        + "    UPDATE SET b.desc = b.desc || '; ' || p.desc\n"
                        + "  WHEN NOT MATCHED THEN\n"
                        + "    INSERT (title, desc, bought) values (p.title, p.desc, p.bought)");

        assertEquals("Unexpected statement type", LocalStatementType.MERGE, statementModel.getStatementType());
        assertEquals("\"more books\"", statementModel.getTableName());
        assertFalse("Statement should not have returning", statementModel.returningClauseDetected());
    }

    @Test
    public void merge_withReturning() {
        StatementIdentification statementModel = parseStatement(
                "MERGE INTO books b\n"
                        + "  USING purchases p\n"
                        + "  ON p.title = b.title and p.type = q'<bk>'\n"
                        + "  WHEN MATCHED THEN\n"
                        + "    UPDATE SET b.desc = b.desc || '; ' || p.desc\n"
                        + "  WHEN NOT MATCHED THEN\n"
                        + "    INSERT (title, desc, bought) values (p.title, p.desc, p.bought)\n"
                        + "  RETURNING id, \"OTHER COLUMN\"");

        assertEquals("Unexpected statement type", LocalStatementType.MERGE, statementModel.getStatementType());
        assertEquals("books", statementModel.getTableName());
        assertTrue("Statement should have returning", statementModel.returningClauseDetected());
    }

}
