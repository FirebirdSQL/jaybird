package org.firebirdsql.jdbc.parser;
import org.antlr.runtime.*;
import org.antlr.runtime.tree.CommonTree;
import org.firebirdsql.jdbc.parser.*;

import junit.framework.TestCase;



public class TestGrammar extends TestCase {

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
        
        assertTrue("someTable".equals(parser.getStatementModel().getTableName()));
        assertTrue(parser.getStatementModel().getColumns().contains("\"\u0442\u0435\"\"\u0441\u0442\""));
        // assertTrue("'a'".equals(parser._values.get(0)));
        
        System.out.println(tree.toStringTree());
        System.out.println(parser.getStatementModel().getTableName());
        System.out.println(parser.getStatementModel().getColumns());
    }
    
    public void testInsertIntoSelect() throws Exception {
        JaybirdSqlParser parser = createParser(
            "Insert Into someTable " +
            "Select * From anotherTable");
        
        CommonTree tree = (CommonTree)parser.statement().getTree();
        assertTrue("someTable".equals(parser.getStatementModel().getTableName()));        
    }
    
    public void testInsertWithCase() throws Exception {
        JaybirdSqlParser parser = createParser(
            "Insert Into someTable ( col1, col2) " +
            "values((case when a = 1 Then 2 else 3 end), 2)");
        
        CommonTree tree = (CommonTree)parser.statement().getTree();
        assertTrue("someTable".equals(parser.getStatementModel().getTableName()));
        assertTrue(parser.getMismatchCount() > 0);
    }
    
    public void testUpdate() throws Exception {
        JaybirdSqlParser parser = createParser(
            "Update someTable Set " +
            "col1 = 25, col2 = 'abc' Where 1=0 Returning col3");
        
        CommonTree tree = (CommonTree)parser.statement().getTree();
        assertTrue("someTable".equals(parser.getStatementModel().getTableName()));
        assertTrue(parser.getMismatchCount() == 0);
        
    }
    
//    public void testANTRLv3Parser() throws Exception {
//        CharStream stream = new ANTLRFileStream("./src/grammar/JaybirdTest.g3");
//        
//        ANTLRv3Lexer lexer = new ANTLRv3Lexer(stream);
//        ANTLRv3Parser parser = new ANTLRv3Parser(new CommonTokenStream(lexer));
//        
//        CommonTree tree = (CommonTree)parser.grammarDef().getTree();
//        
//        System.out.println(tree.toStringTree());
//    }
}
