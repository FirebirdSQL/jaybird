package org.firebirdsql.jdbc.parser;

import org.antlr.runtime.*;
import org.antlr.runtime.tree.CommonTree;

import junit.framework.TestCase;

public class TestGrammar extends TestCase {
	
	// TODO Add more testcases for grammar

    protected JaybirdSqlParser createParser(String testString) {
        CharStream stream = new CaseInsensitiveStream(testString);
        
        JaybirdSqlLexer lexer = new JaybirdSqlLexer(stream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
    
        return new JaybirdSqlParser(tokenStream);
    }
    
    public void testGrammar() throws Exception {
        
        JaybirdSqlParser parser = createParser(
            "insert into someTable(a, \"\u0442\u0435\"\"\u0441\u0442\", aaa) " +
            "values('a', -1.23, a(a,aa))");
        
        CommonTree tree = (CommonTree)parser.statement().getTree();
        
        assertEquals("someTable", parser.getStatementModel().getTableName());
        assertTrue(parser.getStatementModel().getColumns().contains("\"\u0442\u0435\"\"\u0441\u0442\""));
        
        System.out.println(tree.toStringTree());
        System.out.println(parser.getStatementModel().getTableName());
        System.out.println(parser.getStatementModel().getColumns());
    }
    
    public void testInsertIntoSelect() throws Exception {
        JaybirdSqlParser parser = createParser(
            "Insert Into someTable " +
            "Select * From anotherTable");
        
        parser.statement().getTree();
        assertEquals("someTable", parser.getStatementModel().getTableName());        
    }
    
    public void testInsertWithCase() throws Exception {
        JaybirdSqlParser parser = createParser(
            "Insert Into someTable ( col1, col2) " +
            "values((case when a = 1 Then 2 else 3 end), 2)");
        
        parser.statement().getTree();
        assertEquals("someTable", parser.getStatementModel().getTableName());
        assertTrue(parser.getMismatchCount() > 0);
    }
    
    public void testUpdate() throws Exception {
        JaybirdSqlParser parser = createParser(
            "Update someTable Set " +
            "col1 = 25, col2 = 'abc' Where 1=0 Returning col3");
        
        parser.statement().getTree();
        assertEquals("someTable", parser.getStatementModel().getTableName());
        assertEquals(0, parser.getMismatchCount());
    }
}
