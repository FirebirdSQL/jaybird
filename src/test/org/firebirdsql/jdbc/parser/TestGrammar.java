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

import static org.junit.Assert.*;

public class TestGrammar {

    private JaybirdSqlParser createParser(String testString) {
        CharStream stream = CharStreams.fromString(testString);
        
        JaybirdSqlLexer lexer = new JaybirdSqlLexer(stream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);

        return new JaybirdSqlParser(tokenStream);
    }

    @Test
    public void insert_values() {
        JaybirdSqlParser parser = createParser(
                "insert into someTable(a, \"\u0442\u0435\"\"\u0441\u0442\", aaa) values('a', -1.23, a(a,aa))");
        
        parser.statement();

        JaybirdStatementModel statementModel = parser.getStatementModel();
        assertEquals("Unexpected statement type", JaybirdStatementModel.INSERT_TYPE, statementModel.getStatementType());
        assertEquals("Unexpected table name", "someTable", statementModel.getTableName());
        assertFalse("Statement should have no returning", statementModel.hasReturning());
        assertEquals("No parser errors expected", 0, parser.getNumberOfSyntaxErrors());
    }

    @Test
    public void insert_values_quotedTable() {
        JaybirdSqlParser parser = createParser(
                "insert into \"someTable\"(a, b, c) values('a', -1.23, a(a,aa))");

        parser.statement();

        JaybirdStatementModel statementModel = parser.getStatementModel();
        assertEquals("Unexpected statement type", JaybirdStatementModel.INSERT_TYPE, statementModel.getStatementType());
        assertEquals("Unexpected table name", "\"someTable\"", statementModel.getTableName());
        assertFalse("Statement should have no returning", statementModel.hasReturning());
        assertEquals("No parser errors expected", 0, parser.getNumberOfSyntaxErrors());
    }

    @Test
    public void insert_values_withReturning() {
        JaybirdSqlParser parser = createParser(
                "insert into someTable(a, b, c) values('a', -1.23, a(a,aa)) returning id");

        parser.statement();

        JaybirdStatementModel statementModel = parser.getStatementModel();
        assertEquals("Unexpected statement type", JaybirdStatementModel.INSERT_TYPE, statementModel.getStatementType());
        assertEquals("Unexpected table name", "someTable", statementModel.getTableName());
        assertTrue("Statement should have returning", statementModel.hasReturning());
        assertEquals("No parser errors expected", 0, parser.getNumberOfSyntaxErrors());
    }

    @Test
    public void insert_values_withReturning_aliases() {
        JaybirdSqlParser parser = createParser(
                "insert into someTable(a, b, c) values('a', -1.23, a(a,aa)) returning id as \"ID\", b,c no_as");

        parser.statement();

        JaybirdStatementModel statementModel = parser.getStatementModel();
        assertEquals("Unexpected statement type", JaybirdStatementModel.INSERT_TYPE, statementModel.getStatementType());
        assertEquals("Unexpected table name", "someTable", statementModel.getTableName());
        assertTrue("Statement should have returning", statementModel.hasReturning());
        assertEquals("No parser errors expected", 0, parser.getNumberOfSyntaxErrors());
    }

    @Test
    public void insert_values_commentedOutReturning_lineComment() {
        JaybirdSqlParser parser = createParser(
                "insert into someTable(a, b, c) values('a', -1.23, a(a,aa)) -- returning id");

        parser.statement();

        JaybirdStatementModel statementModel = parser.getStatementModel();
        assertEquals("Unexpected statement type", JaybirdStatementModel.INSERT_TYPE, statementModel.getStatementType());
        assertEquals("Unexpected table name", "someTable", statementModel.getTableName());
        assertFalse("Statement should not have returning", statementModel.hasReturning());
        assertEquals("No parser errors expected", 0, parser.getNumberOfSyntaxErrors());
    }

    @Test
    public void insert_values_commentedOutReturning_blockComment() {
        JaybirdSqlParser parser = createParser(
                "insert into someTable(a, b, c) values('a', -1.23, a(a,aa)) /* returning id */");

        parser.statement();

        JaybirdStatementModel statementModel = parser.getStatementModel();
        assertEquals("Unexpected statement type", JaybirdStatementModel.INSERT_TYPE, statementModel.getStatementType());
        assertEquals("Unexpected table name", "someTable", statementModel.getTableName());
        assertFalse("Statement should not have returning", statementModel.hasReturning());
        assertEquals("No parser errors expected", 0, parser.getNumberOfSyntaxErrors());
    }

    @Test
    public void insertIntoSelect() {
        JaybirdSqlParser parser = createParser("Insert Into someTable Select * From anotherTable");
        
        parser.statement();

        JaybirdStatementModel statementModel = parser.getStatementModel();
        assertEquals("Unexpected statement type", JaybirdStatementModel.INSERT_TYPE, statementModel.getStatementType());
        assertEquals("someTable", statementModel.getTableName());
        assertFalse("Statement should have no returning", statementModel.hasReturning());
        assertEquals("No parser errors expected", 0, parser.getNumberOfSyntaxErrors());
    }

    @Test
    public void insertIntoSelect_withReturning() {
        JaybirdSqlParser parser = createParser("Insert Into someTable Select * From anotherTable returning id");

        parser.statement();

        JaybirdStatementModel statementModel = parser.getStatementModel();
        assertEquals("Unexpected statement type", JaybirdStatementModel.INSERT_TYPE, statementModel.getStatementType());
        assertEquals("someTable", statementModel.getTableName());
        assertTrue("Statement should have returning", statementModel.hasReturning());
        assertEquals("No parser errors expected", 0, parser.getNumberOfSyntaxErrors());
    }

    @Test
    public void insertWithCase() {
        JaybirdSqlParser parser = createParser(
                "Insert Into someTable ( col1, col2) values((case when a = 1 Then 2 else 3 end), 2)");

        parser.statement();

        JaybirdStatementModel statementModel = parser.getStatementModel();
        assertEquals("Unexpected statement type", JaybirdStatementModel.INSERT_TYPE, statementModel.getStatementType());
        assertEquals("someTable", statementModel.getTableName());
        assertFalse("Statement should have no returning", statementModel.hasReturning());
        assertEquals(0, parser.getNumberOfSyntaxErrors());
    }

    @Test
    public void insertReturningWithCase() {
        JaybirdSqlParser parser = createParser(
                "Insert Into someTable ( col1, col2) values((case when a = 1 Then 2 else 3 end), 2) returning id");

        parser.statement();

        JaybirdStatementModel statementModel = parser.getStatementModel();
        assertEquals("Unexpected statement type", JaybirdStatementModel.INSERT_TYPE, statementModel.getStatementType());
        assertEquals("someTable", statementModel.getTableName());
        assertTrue("Statement should have returning", statementModel.hasReturning());
        assertTrue("Statement should have returning", statementModel.hasReturning());
        assertEquals(0, parser.getNumberOfSyntaxErrors());
    }

    @Test
    public void insertDefaultValues() {
        JaybirdSqlParser parser = createParser("INSERT INTO someTable DEFAULT VALUES");

        parser.statement();

        JaybirdStatementModel statementModel = parser.getStatementModel();
        assertEquals("Unexpected statement type", JaybirdStatementModel.INSERT_TYPE, statementModel.getStatementType());
        assertEquals("someTable", statementModel.getTableName());
        assertFalse("Statement should have no returning", statementModel.hasReturning());
        assertEquals("No parser errors expected", 0, parser.getNumberOfSyntaxErrors());
    }

    @Test
    public void insertDefaultValues_withReturning() {
        JaybirdSqlParser parser = createParser("INSERT INTO someTable DEFAULT VALUES RETURNING \"ID\"");

        parser.statement();

        JaybirdStatementModel statementModel = parser.getStatementModel();
        assertEquals("Unexpected statement type", JaybirdStatementModel.INSERT_TYPE, statementModel.getStatementType());
        assertEquals("someTable", statementModel.getTableName());
        assertTrue("Statement should have returning", statementModel.hasReturning());
        assertEquals("No parser errors expected", 0, parser.getNumberOfSyntaxErrors());
    }

    @Test
    public void update() {
        JaybirdSqlParser parser = createParser(
                "Update someTable Set col1 = 25, col2 = 'abc' Where 1=0");

        parser.statement();

        JaybirdStatementModel statementModel = parser.getStatementModel();
        assertEquals("Unexpected statement type", JaybirdStatementModel.UPDATE_TYPE, statementModel.getStatementType());
        assertEquals("someTable", statementModel.getTableName());
        assertFalse("Statement should not have returning", statementModel.hasReturning());
        assertEquals("No parser errors expected", 0, parser.getNumberOfSyntaxErrors());
    }

    @Test
    public void update_quotedTableName() {
        JaybirdSqlParser parser = createParser(
                "Update \"someTable\" Set col1 = 25, col2 = 'abc' Where 1=0");

        parser.statement();

        JaybirdStatementModel statementModel = parser.getStatementModel();
        assertEquals("Unexpected statement type", JaybirdStatementModel.UPDATE_TYPE, statementModel.getStatementType());
        assertEquals("\"someTable\"", statementModel.getTableName());
        assertFalse("Statement should not have returning", statementModel.hasReturning());
        assertEquals("No parser errors expected", 0, parser.getNumberOfSyntaxErrors());
    }

    @Test
    public void update_quotedTableNameWithSpace() {
        JaybirdSqlParser parser = createParser(
                "Update \"some Table\" Set col1 = 25, col2 = 'abc' Where 1=0");

        parser.statement();

        JaybirdStatementModel statementModel = parser.getStatementModel();
        assertEquals("Unexpected statement type", JaybirdStatementModel.UPDATE_TYPE, statementModel.getStatementType());
        assertEquals("\"some Table\"", statementModel.getTableName());
        assertFalse("Statement should not have returning", statementModel.hasReturning());
        assertEquals("No parser errors expected", 0, parser.getNumberOfSyntaxErrors());
    }

    @Test
    public void update_withReturning() {
        JaybirdSqlParser parser = createParser(
                "Update someTable Set col1 = 25, col2 = 'abc' Where 1=0 Returning col3");

        parser.statement();

        JaybirdStatementModel statementModel = parser.getStatementModel();
        assertEquals("Unexpected statement type", JaybirdStatementModel.UPDATE_TYPE, statementModel.getStatementType());
        assertEquals("someTable", statementModel.getTableName());
        assertTrue("Statement should have returning", statementModel.hasReturning());
        assertEquals("No parser errors expected", 0, parser.getNumberOfSyntaxErrors());
    }

    @Test
    public void delete() {
        JaybirdSqlParser parser = createParser(
                "DELETE FROM someTable Where 1=0");

        parser.statement();

        JaybirdStatementModel statementModel = parser.getStatementModel();
        assertEquals("Unexpected statement type", JaybirdStatementModel.DELETE_TYPE, statementModel.getStatementType());
        assertEquals("someTable", statementModel.getTableName());
        assertFalse("Statement should not have returning", statementModel.hasReturning());
        assertEquals("No parser errors expected", 0, parser.getNumberOfSyntaxErrors());
    }

    @Test
    public void delete_quotedTableName() {
        JaybirdSqlParser parser = createParser("delete from \"someTable\"");

        parser.statement();

        JaybirdStatementModel statementModel = parser.getStatementModel();
        assertEquals("Unexpected statement type", JaybirdStatementModel.DELETE_TYPE, statementModel.getStatementType());
        assertEquals("\"someTable\"", statementModel.getTableName());
        assertFalse("Statement should not have returning", statementModel.hasReturning());
        assertEquals("No parser errors expected", 0, parser.getNumberOfSyntaxErrors());
    }

    @Test
    public void delete_withReturning() {
        JaybirdSqlParser parser = createParser("Delete From someTable Returning col3");

        parser.statement();

        JaybirdStatementModel statementModel = parser.getStatementModel();
        assertEquals("Unexpected statement type", JaybirdStatementModel.DELETE_TYPE, statementModel.getStatementType());
        assertEquals("someTable", statementModel.getTableName());
        assertTrue("Statement should have returning", statementModel.hasReturning());
        assertEquals("No parser errors expected", 0, parser.getNumberOfSyntaxErrors());
    }

    @Test
    public void delete_withWhere_withReturning() {
        JaybirdSqlParser parser = createParser("Delete From someTable where 1 = 1 Returning col3");

        parser.statement();

        JaybirdStatementModel statementModel = parser.getStatementModel();
        assertEquals("Unexpected statement type", JaybirdStatementModel.DELETE_TYPE, statementModel.getStatementType());
        assertEquals("someTable", statementModel.getTableName());
        assertTrue("Statement should have returning", statementModel.hasReturning());
        assertEquals("No parser errors expected", 0, parser.getNumberOfSyntaxErrors());
    }

    @Test
    public void select() {
        JaybirdSqlParser parser = createParser("select * from RDB$DATABASE");

        parser.statement();

        JaybirdStatementModel statementModel = parser.getStatementModel();
        assertEquals("Expected default statement type (0)", JaybirdStatementModel.UNDETECTED_TYPE, statementModel.getStatementType());
        assertNull("Expected no table name", statementModel.getTableName());
        assertFalse("Statement should not have returning", statementModel.hasReturning());
        assertTrue("Expected parser error", parser.getNumberOfSyntaxErrors() > 0);
    }

    @Test
    public void insertWithQString() {
        JaybirdSqlParser parser = createParser("insert into someTable values (Q'[a'bc]')");

        parser.statement();

        JaybirdStatementModel statementModel = parser.getStatementModel();
        assertEquals("Unexpected statement type", JaybirdStatementModel.INSERT_TYPE, statementModel.getStatementType());
        assertEquals("someTable", statementModel.getTableName());
        assertFalse("Statement should not have returning", statementModel.hasReturning());
        assertEquals("No parser errors expected", 0, parser.getNumberOfSyntaxErrors());
    }

    @Test
    public void insertWithQStringWithReturning() {
        JaybirdSqlParser parser = createParser("insert into someTable values (Q'[a'bc]') returning id, \"ABC\"");

        parser.statement();

        JaybirdStatementModel statementModel = parser.getStatementModel();
        assertEquals("Unexpected statement type", JaybirdStatementModel.INSERT_TYPE, statementModel.getStatementType());
        assertEquals("someTable", statementModel.getTableName());
        assertTrue("Statement should not have returning", statementModel.hasReturning());
        assertEquals("No parser errors expected", 0, parser.getNumberOfSyntaxErrors());
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
        JaybirdSqlParser parser = createParser(input);

        JaybirdSqlParser.SimpleValueContext simpleValueContext = parser.simpleValue();
        
        assertEquals("Expected no syntax errors", 0, parser.getNumberOfSyntaxErrors());
        assertSame("Expected single node", simpleValueContext.start, simpleValueContext.stop);
        assertEquals("Expected node type Q_STRING", JaybirdSqlLexer.Q_STRING, simpleValueContext.start.getType());
    }

    @Test
    public void merge() {
        JaybirdSqlParser parser = createParser(
                "MERGE INTO books b\n"
                        + "  USING purchases p\n"
                        + "  ON p.title = b.title and p.type = 'bk'\n"
                        + "  WHEN MATCHED THEN\n"
                        + "    UPDATE SET b.desc = b.desc || '; ' || p.desc\n"
                        + "  WHEN NOT MATCHED THEN\n"
                        + "    INSERT (title, desc, bought) values (p.title, p.desc, p.bought)");

        parser.statement();

        JaybirdStatementModel statementModel = parser.getStatementModel();
        assertEquals("Unexpected statement type", JaybirdStatementModel.MERGE_TYPE, statementModel.getStatementType());
        assertEquals("books", statementModel.getTableName());
        assertFalse("Statement should not have returning", statementModel.hasReturning());
        assertEquals("No parser errors expected", 0, parser.getNumberOfSyntaxErrors());
    }

    @Test
    public void merge_quotedTableName() {
        JaybirdSqlParser parser = createParser(
                "MERGE INTO \"books\" b\n"
                        + "  USING purchases p\n"
                        + "  ON p.title = b.title and p.type = q'<bk>'\n"
                        + "  WHEN MATCHED THEN\n"
                        + "    UPDATE SET b.desc = b.desc || '; ' || p.desc\n"
                        + "  WHEN NOT MATCHED THEN\n"
                        + "    INSERT (title, desc, bought) values (p.title, p.desc, p.bought)");

        parser.statement();

        JaybirdStatementModel statementModel = parser.getStatementModel();
        assertEquals("Unexpected statement type", JaybirdStatementModel.MERGE_TYPE, statementModel.getStatementType());
        assertEquals("\"books\"", statementModel.getTableName());
        assertEquals("No parser errors expected", 0, parser.getNumberOfSyntaxErrors());
    }

    @Test
    public void merge_quotedTableNameWithSpace() {
        JaybirdSqlParser parser = createParser(
                "MERGE INTO \"more books\" b\n"
                        + "  USING purchases p\n"
                        + "  ON p.title = b.title and p.type = q'<bk>'\n"
                        + "  WHEN MATCHED THEN\n"
                        + "    UPDATE SET b.desc = b.desc || '; ' || p.desc\n"
                        + "  WHEN NOT MATCHED THEN\n"
                        + "    INSERT (title, desc, bought) values (p.title, p.desc, p.bought)");

        parser.statement();

        JaybirdStatementModel statementModel = parser.getStatementModel();
        assertEquals("Unexpected statement type", JaybirdStatementModel.MERGE_TYPE, statementModel.getStatementType());
        assertEquals("\"more books\"", statementModel.getTableName());
        assertEquals("No parser errors expected", 0, parser.getNumberOfSyntaxErrors());
    }

    @Test
    public void merge_withReturning() {
        JaybirdSqlParser parser = createParser(
                "MERGE INTO books b\n"
                        + "  USING purchases p\n"
                        + "  ON p.title = b.title and p.type = q'<bk>'\n"
                        + "  WHEN MATCHED THEN\n"
                        + "    UPDATE SET b.desc = b.desc || '; ' || p.desc\n"
                        + "  WHEN NOT MATCHED THEN\n"
                        + "    INSERT (title, desc, bought) values (p.title, p.desc, p.bought)\n"
                        + "  RETURNING id, \"OTHER COLUMN\"");

        parser.statement();

        JaybirdStatementModel statementModel = parser.getStatementModel();
        assertEquals("Unexpected statement type", JaybirdStatementModel.MERGE_TYPE, statementModel.getStatementType());
        assertEquals("books", statementModel.getTableName());
        assertTrue("Statement should have returning", statementModel.hasReturning());
        assertEquals("No parser errors expected", 0, parser.getNumberOfSyntaxErrors());
    }

}
