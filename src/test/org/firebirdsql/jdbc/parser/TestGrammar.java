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

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.*;

public class TestGrammar {
	
	// TODO Add more testcases for grammar

    protected JaybirdSqlParser createParser(String testString) {
        CharStream stream = new ANTLRInputStream(testString);
        
        JaybirdSqlLexer lexer = new JaybirdSqlLexer(stream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
    
        return new JaybirdSqlParser(tokenStream);
    }

    @Test
    public void insert_values() throws Exception {
        JaybirdSqlParser parser = createParser(
                "insert into someTable(a, \"\u0442\u0435\"\"\u0441\u0442\", aaa) values('a', -1.23, a(a,aa))");
        
        parser.statement();

        JaybirdStatementModel statementModel = parser.getStatementModel();
        assertEquals("Unexpected statement type", JaybirdStatementModel.INSERT_TYPE, statementModel.getStatementType());
        assertEquals("Unexpected table name", "someTable", statementModel.getTableName());
        assertThat("Column missing", statementModel.getColumns(), hasItem("\"\u0442\u0435\"\"\u0441\u0442\""));
        assertFalse("Statement should have no returning", statementModel.hasReturning());
    }

    @Test
    public void insert_values_quotedTable() throws Exception {
        JaybirdSqlParser parser = createParser(
                "insert into \"someTable\"(a, b, c) values('a', -1.23, a(a,aa))");

        parser.statement();

        JaybirdStatementModel statementModel = parser.getStatementModel();
        assertEquals("Unexpected statement type", JaybirdStatementModel.INSERT_TYPE, statementModel.getStatementType());
        assertEquals("Unexpected table name", "\"someTable\"", statementModel.getTableName());
        assertFalse("Statement should have no returning", statementModel.hasReturning());
    }

    @Test
    public void insert_values_withReturning() throws Exception {
        JaybirdSqlParser parser = createParser(
                "insert into someTable(a, b, c) values('a', -1.23, a(a,aa)) returning id");

        parser.statement();

        JaybirdStatementModel statementModel = parser.getStatementModel();
        assertEquals("Unexpected statement type", JaybirdStatementModel.INSERT_TYPE, statementModel.getStatementType());
        assertEquals("Unexpected table name", "someTable", statementModel.getTableName());
        assertTrue("Statement should have returning", statementModel.hasReturning());
    }

    @Test
    public void insertIntoSelect() throws Exception {
        JaybirdSqlParser parser = createParser("Insert Into someTable Select * From anotherTable");
        
        parser.statement();

        JaybirdStatementModel statementModel = parser.getStatementModel();
        assertEquals("Unexpected statement type", JaybirdStatementModel.INSERT_TYPE, statementModel.getStatementType());
        assertEquals("someTable", statementModel.getTableName());
        assertFalse("Statement should have no returning", statementModel.hasReturning());
    }

    @Test
    public void insertIntoSelect_withReturning() throws Exception {
        JaybirdSqlParser parser = createParser("Insert Into someTable Select * From anotherTable returning id");

        parser.statement();

        JaybirdStatementModel statementModel = parser.getStatementModel();
        assertEquals("Unexpected statement type", JaybirdStatementModel.INSERT_TYPE, statementModel.getStatementType());
        assertEquals("someTable", statementModel.getTableName());
        assertTrue("Statement should have returning", statementModel.hasReturning());
        assertThat("Should contain column id", statementModel.getReturningColumns(), hasItem("id"));
    }

    @Test
    public void insertWithCase() throws Exception {
        JaybirdSqlParser parser = createParser(
                "Insert Into someTable ( col1, col2) values((case when a = 1 Then 2 else 3 end), 2)");

        parser.statement();

        JaybirdStatementModel statementModel = parser.getStatementModel();
        assertEquals("Unexpected statement type", JaybirdStatementModel.INSERT_TYPE, statementModel.getStatementType());
        assertEquals("someTable", statementModel.getTableName());
        assertTrue(parser.getNumberOfSyntaxErrors() > 0);
        assertFalse("Statement should have no returning", statementModel.hasReturning());
    }

    @Test
    public void insertDefaultValues() throws Exception {
        JaybirdSqlParser parser = createParser("INSERT INTO someTable DEFAULT VALUES");

        parser.statement();

        JaybirdStatementModel statementModel = parser.getStatementModel();
        assertEquals("Unexpected statement type", JaybirdStatementModel.INSERT_TYPE, statementModel.getStatementType());
        assertTrue("Expected default values", statementModel.isDefaultValues());
        assertEquals("someTable", statementModel.getTableName());
        assertFalse("Statement should have no returning", statementModel.hasReturning());
    }

    @Test
    public void insertDefaultValues_withReturning() throws Exception {
        JaybirdSqlParser parser = createParser("INSERT INTO someTable DEFAULT VALUES RETURNING \"ID\"");

        parser.statement();

        JaybirdStatementModel statementModel = parser.getStatementModel();
        assertEquals("Unexpected statement type", JaybirdStatementModel.INSERT_TYPE, statementModel.getStatementType());
        assertTrue("Expected default values", statementModel.isDefaultValues());
        assertEquals("someTable", statementModel.getTableName());
        assertTrue("Statement should have returning", statementModel.hasReturning());
        assertThat("Should contain column \"ID\"", statementModel.getReturningColumns(), hasItem("\"ID\""));
    }

    @Test
    public void update() throws Exception {
        JaybirdSqlParser parser = createParser(
                "Update someTable Set col1 = 25, col2 = 'abc' Where 1=0");

        parser.statement();

        JaybirdStatementModel statementModel = parser.getStatementModel();
        assertEquals("Unexpected statement type", JaybirdStatementModel.UPDATE_TYPE, statementModel.getStatementType());
        assertEquals("someTable", statementModel.getTableName());
    }

    @Test
    public void update_quotedTableName() throws Exception {
        JaybirdSqlParser parser = createParser(
                "Update \"someTable\" Set col1 = 25, col2 = 'abc' Where 1=0");

        parser.statement();

        JaybirdStatementModel statementModel = parser.getStatementModel();
        assertEquals("Unexpected statement type", JaybirdStatementModel.UPDATE_TYPE, statementModel.getStatementType());
        assertEquals("\"someTable\"", statementModel.getTableName());
    }

    @Test
    public void update_withReturning() throws Exception {
        JaybirdSqlParser parser = createParser(
                "Update someTable Set col1 = 25, col2 = 'abc' Where 1=0 Returning col3");

        parser.statement();

        JaybirdStatementModel statementModel = parser.getStatementModel();
        assertEquals("Unexpected statement type", JaybirdStatementModel.UPDATE_TYPE, statementModel.getStatementType());
        assertEquals("someTable", statementModel.getTableName());
        assertTrue("Statement should have returning", statementModel.hasReturning());
        assertEquals(Collections.singletonList("col3"), statementModel.getReturningColumns());
    }

    @Test
    public void delete() throws Exception {
        JaybirdSqlParser parser = createParser(
                "DELETE FROM someTable Where 1=0");

        parser.statement();

        JaybirdStatementModel statementModel = parser.getStatementModel();
        assertEquals("Unexpected statement type", JaybirdStatementModel.DELETE_TYPE, statementModel.getStatementType());
        assertEquals("someTable", statementModel.getTableName());
        assertEquals(0, parser.getNumberOfSyntaxErrors());
    }

    @Test
    public void delete_quotedTableName() throws Exception {
        JaybirdSqlParser parser = createParser("delete from \"someTable\"");

        parser.statement();

        JaybirdStatementModel statementModel = parser.getStatementModel();
        assertEquals("Unexpected statement type", JaybirdStatementModel.DELETE_TYPE, statementModel.getStatementType());
        assertEquals("\"someTable\"", statementModel.getTableName());
        assertEquals(0, parser.getNumberOfSyntaxErrors());
    }

    @Test
    public void delete_withReturning() throws Exception {
        JaybirdSqlParser parser = createParser("Delete From someTable Returning col3");

        parser.statement();

        JaybirdStatementModel statementModel = parser.getStatementModel();
        assertEquals("Unexpected statement type", JaybirdStatementModel.DELETE_TYPE, statementModel.getStatementType());
        assertEquals("someTable", statementModel.getTableName());
        assertTrue("Statement should have returning", statementModel.hasReturning());
        assertEquals(Collections.singletonList("col3"), statementModel.getReturningColumns());
        assertEquals(0, parser.getNumberOfSyntaxErrors());
    }

    @Test
    public void delete_withWhere_withReturning() throws Exception {
        JaybirdSqlParser parser = createParser("Delete From someTable where 1 = 1 Returning col3");

        parser.statement();

        JaybirdStatementModel statementModel = parser.getStatementModel();
        assertEquals("Unexpected statement type", JaybirdStatementModel.DELETE_TYPE, statementModel.getStatementType());
        assertEquals("someTable", statementModel.getTableName());
        assertTrue("Statement should have returning", statementModel.hasReturning());
        assertEquals(Collections.singletonList("col3"), statementModel.getReturningColumns());
        assertEquals(0, parser.getNumberOfSyntaxErrors());
    }

    @Test
    public void select() throws Exception {
        JaybirdSqlParser parser = createParser("select * from RDB$DATABASE");

        parser.statement();

        JaybirdStatementModel statementModel = parser.getStatementModel();
        assertEquals("Expected default statement type (0)", JaybirdStatementModel.UNDETECTED_TYPE, statementModel.getStatementType());
        assertNull("Expected no table name", statementModel.getTableName());
    }
}
