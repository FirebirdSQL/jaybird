// $ANTLR 3.4 JaybirdSql.g 2015-05-02 17:09:43

package org.firebirdsql.jdbc.parser;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.antlr.runtime.tree.*;


@SuppressWarnings({"all", "warnings", "unchecked"})
public class JaybirdSqlParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "ALL", "AS", "AVG", "BOTH", "CAST", "CHARACTER", "COLLATE", "COMMA", "COUNT", "CURRENT_DATE", "CURRENT_ROLE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER", "DB_KEY", "DEFAULT", "DELETE", "DISTINCT", "EXECUTE", "EXTRACT", "FOR", "FROM", "GENERIC_ID", "GEN_ID", "INSERT", "INTEGER", "INTO", "KW_BIGINT", "KW_BLOB", "KW_CHAR", "KW_DATE", "KW_DECIMAL", "KW_DOUBLE", "KW_FLOAT", "KW_INT", "KW_INTEGER", "KW_NUMERIC", "KW_PRECISION", "KW_SIZE", "KW_SMALLINT", "KW_TIME", "KW_TIMESTAMP", "KW_VARCHAR", "LEADING", "LEFT_PAREN", "MATCHING", "MAXIMUM", "MINIMUM", "NEXT", "NULL", "OR", "PROCEDURE", "QUOTED_ID", "REAL", "RETURNING", "RIGHT_PAREN", "SEGMENT", "SELECT", "SET", "SL_COMMENT", "STRING", "SUBSTRING", "SUB_TYPE", "SUM", "TRAILING", "TRIM", "UPDATE", "VALUE", "VALUES", "WS", "'*'", "'+'", "'-'", "'.'", "'/'", "':'", "'='", "'?'", "'['", "']'", "'||'"
    };

    public static final int EOF=-1;
    public static final int T__74=74;
    public static final int T__75=75;
    public static final int T__76=76;
    public static final int T__77=77;
    public static final int T__78=78;
    public static final int T__79=79;
    public static final int T__80=80;
    public static final int T__81=81;
    public static final int T__82=82;
    public static final int T__83=83;
    public static final int T__84=84;
    public static final int ALL=4;
    public static final int AS=5;
    public static final int AVG=6;
    public static final int BOTH=7;
    public static final int CAST=8;
    public static final int CHARACTER=9;
    public static final int COLLATE=10;
    public static final int COMMA=11;
    public static final int COUNT=12;
    public static final int CURRENT_DATE=13;
    public static final int CURRENT_ROLE=14;
    public static final int CURRENT_TIME=15;
    public static final int CURRENT_TIMESTAMP=16;
    public static final int CURRENT_USER=17;
    public static final int DB_KEY=18;
    public static final int DEFAULT=19;
    public static final int DELETE=20;
    public static final int DISTINCT=21;
    public static final int EXECUTE=22;
    public static final int EXTRACT=23;
    public static final int FOR=24;
    public static final int FROM=25;
    public static final int GENERIC_ID=26;
    public static final int GEN_ID=27;
    public static final int INSERT=28;
    public static final int INTEGER=29;
    public static final int INTO=30;
    public static final int KW_BIGINT=31;
    public static final int KW_BLOB=32;
    public static final int KW_CHAR=33;
    public static final int KW_DATE=34;
    public static final int KW_DECIMAL=35;
    public static final int KW_DOUBLE=36;
    public static final int KW_FLOAT=37;
    public static final int KW_INT=38;
    public static final int KW_INTEGER=39;
    public static final int KW_NUMERIC=40;
    public static final int KW_PRECISION=41;
    public static final int KW_SIZE=42;
    public static final int KW_SMALLINT=43;
    public static final int KW_TIME=44;
    public static final int KW_TIMESTAMP=45;
    public static final int KW_VARCHAR=46;
    public static final int LEADING=47;
    public static final int LEFT_PAREN=48;
    public static final int MATCHING=49;
    public static final int MAXIMUM=50;
    public static final int MINIMUM=51;
    public static final int NEXT=52;
    public static final int NULL=53;
    public static final int OR=54;
    public static final int PROCEDURE=55;
    public static final int QUOTED_ID=56;
    public static final int REAL=57;
    public static final int RETURNING=58;
    public static final int RIGHT_PAREN=59;
    public static final int SEGMENT=60;
    public static final int SELECT=61;
    public static final int SET=62;
    public static final int SL_COMMENT=63;
    public static final int STRING=64;
    public static final int SUBSTRING=65;
    public static final int SUB_TYPE=66;
    public static final int SUM=67;
    public static final int TRAILING=68;
    public static final int TRIM=69;
    public static final int UPDATE=70;
    public static final int VALUE=71;
    public static final int VALUES=72;
    public static final int WS=73;

    // delegates
    public Parser[] getDelegates() {
        return new Parser[] {};
    }

    // delegators


    public JaybirdSqlParser(TokenStream input) {
        this(input, new RecognizerSharedState());
    }
    public JaybirdSqlParser(TokenStream input, RecognizerSharedState state) {
        super(input, state);
        this.state.ruleMemo = new HashMap[141+1];
         

    }

protected TreeAdaptor adaptor = new CommonTreeAdaptor();

public void setTreeAdaptor(TreeAdaptor adaptor) {
    this.adaptor = adaptor;
}
public TreeAdaptor getTreeAdaptor() {
    return adaptor;
}
    public String[] getTokenNames() { return JaybirdSqlParser.tokenNames; }
    public String getGrammarFileName() { return "JaybirdSql.g"; }


        private boolean _inReturning;
        protected boolean _defaultValues;
        protected JaybirdStatementModel statementModel = new JaybirdStatementModel();
        
        protected int _mismatchCount;
        protected java.util.ArrayList _errorMessages = new java.util.ArrayList();
        
        public JaybirdStatementModel getStatementModel() {
            return statementModel;
        }
        
        public int getMismatchCount() {
            return _mismatchCount;
        }
        
        public java.util.Collection getErrorMessages() {
            return _errorMessages;
        }
        
        public String getColumn(int index) {
            return (String)statementModel.getColumns().get(index);
        }
        
        public String getValue(int index) {
            return (String)statementModel.getValues().get(index);
        }
        
        public String getTableName() {
            return statementModel.getTableName();
        }
        
        public boolean mismatchIsUnwantedToken(IntStream input, int ttype) {
            boolean result = super.mismatchIsUnwantedToken(input, ttype);
            _mismatchCount++;
            return result;
        }

        public boolean mismatchIsMissingToken(IntStream input, BitSet follow) {
            boolean result = super.mismatchIsMissingToken(input, follow);
            _mismatchCount++;
            return result;
        }

        public void emitErrorMessage(String msg) {
            _errorMessages.add(msg);
        }


    public static class statement_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "statement"
    // JaybirdSql.g:180:1: statement : ( insertStatement | deleteStatement | updateStatement | updateOrInsertStatement );
    public final JaybirdSqlParser.statement_return statement() throws RecognitionException {
        JaybirdSqlParser.statement_return retval = new JaybirdSqlParser.statement_return();
        retval.start = input.LT(1);

        int statement_StartIndex = input.index();

        CommonTree root_0 = null;

        JaybirdSqlParser.insertStatement_return insertStatement1 =null;

        JaybirdSqlParser.deleteStatement_return deleteStatement2 =null;

        JaybirdSqlParser.updateStatement_return updateStatement3 =null;

        JaybirdSqlParser.updateOrInsertStatement_return updateOrInsertStatement4 =null;



        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 1) ) { return retval; }

            // JaybirdSql.g:181:9: ( insertStatement | deleteStatement | updateStatement | updateOrInsertStatement )
            int alt1=4;
            switch ( input.LA(1) ) {
            case INSERT:
                {
                alt1=1;
                }
                break;
            case DELETE:
                {
                alt1=2;
                }
                break;
            case UPDATE:
                {
                int LA1_3 = input.LA(2);

                if ( (LA1_3==OR) ) {
                    alt1=4;
                }
                else if ( (LA1_3==GENERIC_ID||LA1_3==QUOTED_ID) ) {
                    alt1=3;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 1, 3, input);

                    throw nvae;

                }
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 1, 0, input);

                throw nvae;

            }

            switch (alt1) {
                case 1 :
                    // JaybirdSql.g:181:14: insertStatement
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_insertStatement_in_statement708);
                    insertStatement1=insertStatement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, insertStatement1.getTree());

                    }
                    break;
                case 2 :
                    // JaybirdSql.g:182:14: deleteStatement
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_deleteStatement_in_statement723);
                    deleteStatement2=deleteStatement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, deleteStatement2.getTree());

                    }
                    break;
                case 3 :
                    // JaybirdSql.g:183:14: updateStatement
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_updateStatement_in_statement738);
                    updateStatement3=updateStatement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, updateStatement3.getTree());

                    }
                    break;
                case 4 :
                    // JaybirdSql.g:185:14: updateOrInsertStatement
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_updateOrInsertStatement_in_statement754);
                    updateOrInsertStatement4=updateOrInsertStatement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, updateOrInsertStatement4.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
            if ( state.backtracking>0 ) { memoize(input, 1, statement_StartIndex); }

        }
        return retval;
    }
    // $ANTLR end "statement"


    public static class deleteStatement_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "deleteStatement"
    // JaybirdSql.g:205:1: deleteStatement : DELETE FROM tableName ( returningClause )? ;
    public final JaybirdSqlParser.deleteStatement_return deleteStatement() throws RecognitionException {
        JaybirdSqlParser.deleteStatement_return retval = new JaybirdSqlParser.deleteStatement_return();
        retval.start = input.LT(1);

        int deleteStatement_StartIndex = input.index();

        CommonTree root_0 = null;

        Token DELETE5=null;
        Token FROM6=null;
        JaybirdSqlParser.tableName_return tableName7 =null;

        JaybirdSqlParser.returningClause_return returningClause8 =null;


        CommonTree DELETE5_tree=null;
        CommonTree FROM6_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 2) ) { return retval; }

            // JaybirdSql.g:205:20: ( DELETE FROM tableName ( returningClause )? )
            // JaybirdSql.g:206:13: DELETE FROM tableName ( returningClause )?
            {
            root_0 = (CommonTree)adaptor.nil();


            DELETE5=(Token)match(input,DELETE,FOLLOW_DELETE_in_deleteStatement789); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            DELETE5_tree = 
            (CommonTree)adaptor.create(DELETE5)
            ;
            adaptor.addChild(root_0, DELETE5_tree);
            }

            FROM6=(Token)match(input,FROM,FOLLOW_FROM_in_deleteStatement791); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            FROM6_tree = 
            (CommonTree)adaptor.create(FROM6)
            ;
            adaptor.addChild(root_0, FROM6_tree);
            }

            pushFollow(FOLLOW_tableName_in_deleteStatement793);
            tableName7=tableName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, tableName7.getTree());

            // JaybirdSql.g:206:71: ( returningClause )?
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0==RETURNING) ) {
                alt2=1;
            }
            switch (alt2) {
                case 1 :
                    // JaybirdSql.g:206:71: returningClause
                    {
                    pushFollow(FOLLOW_returningClause_in_deleteStatement797);
                    returningClause8=returningClause();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, returningClause8.getTree());

                    }
                    break;

            }


            if ( state.backtracking==0 ) {
                            statementModel.setStatementType(JaybirdStatementModel.DELETE_TYPE);
                        }

            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
            if ( state.backtracking>0 ) { memoize(input, 2, deleteStatement_StartIndex); }

        }
        return retval;
    }
    // $ANTLR end "deleteStatement"


    public static class updateStatement_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "updateStatement"
    // JaybirdSql.g:229:1: updateStatement : UPDATE tableName SET assignments ( returningClause )? ;
    public final JaybirdSqlParser.updateStatement_return updateStatement() throws RecognitionException {
        JaybirdSqlParser.updateStatement_return retval = new JaybirdSqlParser.updateStatement_return();
        retval.start = input.LT(1);

        int updateStatement_StartIndex = input.index();

        CommonTree root_0 = null;

        Token UPDATE9=null;
        Token SET11=null;
        JaybirdSqlParser.tableName_return tableName10 =null;

        JaybirdSqlParser.assignments_return assignments12 =null;

        JaybirdSqlParser.returningClause_return returningClause13 =null;


        CommonTree UPDATE9_tree=null;
        CommonTree SET11_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 3) ) { return retval; }

            // JaybirdSql.g:229:20: ( UPDATE tableName SET assignments ( returningClause )? )
            // JaybirdSql.g:230:13: UPDATE tableName SET assignments ( returningClause )?
            {
            root_0 = (CommonTree)adaptor.nil();


            UPDATE9=(Token)match(input,UPDATE,FOLLOW_UPDATE_in_updateStatement855); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            UPDATE9_tree = 
            (CommonTree)adaptor.create(UPDATE9)
            ;
            adaptor.addChild(root_0, UPDATE9_tree);
            }

            pushFollow(FOLLOW_tableName_in_updateStatement857);
            tableName10=tableName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, tableName10.getTree());

            SET11=(Token)match(input,SET,FOLLOW_SET_in_updateStatement859); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            SET11_tree = 
            (CommonTree)adaptor.create(SET11)
            ;
            adaptor.addChild(root_0, SET11_tree);
            }

            pushFollow(FOLLOW_assignments_in_updateStatement861);
            assignments12=assignments();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, assignments12.getTree());

            // JaybirdSql.g:230:82: ( returningClause )?
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==RETURNING) ) {
                alt3=1;
            }
            switch (alt3) {
                case 1 :
                    // JaybirdSql.g:230:82: returningClause
                    {
                    pushFollow(FOLLOW_returningClause_in_updateStatement865);
                    returningClause13=returningClause();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, returningClause13.getTree());

                    }
                    break;

            }


            if ( state.backtracking==0 ) {
                            statementModel.setStatementType(JaybirdStatementModel.UPDATE_TYPE);
                        }

            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
            if ( state.backtracking>0 ) { memoize(input, 3, updateStatement_StartIndex); }

        }
        return retval;
    }
    // $ANTLR end "updateStatement"


    public static class assignments_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "assignments"
    // JaybirdSql.g:236:1: assignments : assignment ( ',' assignment )* ;
    public final JaybirdSqlParser.assignments_return assignments() throws RecognitionException {
        JaybirdSqlParser.assignments_return retval = new JaybirdSqlParser.assignments_return();
        retval.start = input.LT(1);

        int assignments_StartIndex = input.index();

        CommonTree root_0 = null;

        Token char_literal15=null;
        JaybirdSqlParser.assignment_return assignment14 =null;

        JaybirdSqlParser.assignment_return assignment16 =null;


        CommonTree char_literal15_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 4) ) { return retval; }

            // JaybirdSql.g:236:16: ( assignment ( ',' assignment )* )
            // JaybirdSql.g:236:21: assignment ( ',' assignment )*
            {
            root_0 = (CommonTree)adaptor.nil();


            pushFollow(FOLLOW_assignment_in_assignments911);
            assignment14=assignment();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, assignment14.getTree());

            // JaybirdSql.g:236:32: ( ',' assignment )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0==COMMA) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // JaybirdSql.g:236:33: ',' assignment
            	    {
            	    char_literal15=(Token)match(input,COMMA,FOLLOW_COMMA_in_assignments914); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    char_literal15_tree = 
            	    (CommonTree)adaptor.create(char_literal15)
            	    ;
            	    adaptor.addChild(root_0, char_literal15_tree);
            	    }

            	    pushFollow(FOLLOW_assignment_in_assignments916);
            	    assignment16=assignment();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, assignment16.getTree());

            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
            if ( state.backtracking>0 ) { memoize(input, 4, assignments_StartIndex); }

        }
        return retval;
    }
    // $ANTLR end "assignments"


    public static class assignment_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "assignment"
    // JaybirdSql.g:239:1: assignment : columnName '=' value ;
    public final JaybirdSqlParser.assignment_return assignment() throws RecognitionException {
        JaybirdSqlParser.assignment_return retval = new JaybirdSqlParser.assignment_return();
        retval.start = input.LT(1);

        int assignment_StartIndex = input.index();

        CommonTree root_0 = null;

        Token char_literal18=null;
        JaybirdSqlParser.columnName_return columnName17 =null;

        JaybirdSqlParser.value_return value19 =null;


        CommonTree char_literal18_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 5) ) { return retval; }

            // JaybirdSql.g:239:15: ( columnName '=' value )
            // JaybirdSql.g:239:20: columnName '=' value
            {
            root_0 = (CommonTree)adaptor.nil();


            pushFollow(FOLLOW_columnName_in_assignment949);
            columnName17=columnName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, columnName17.getTree());

            char_literal18=(Token)match(input,80,FOLLOW_80_in_assignment951); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            char_literal18_tree = 
            (CommonTree)adaptor.create(char_literal18)
            ;
            adaptor.addChild(root_0, char_literal18_tree);
            }

            pushFollow(FOLLOW_value_in_assignment953);
            value19=value();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, value19.getTree());

            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
            if ( state.backtracking>0 ) { memoize(input, 5, assignment_StartIndex); }

        }
        return retval;
    }
    // $ANTLR end "assignment"


    public static class updateOrInsertStatement_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "updateOrInsertStatement"
    // JaybirdSql.g:255:1: updateOrInsertStatement : UPDATE OR INSERT INTO tableName ( insertColumns )? insertValues ( matchingClause )? ( returningClause )? ;
    public final JaybirdSqlParser.updateOrInsertStatement_return updateOrInsertStatement() throws RecognitionException {
        JaybirdSqlParser.updateOrInsertStatement_return retval = new JaybirdSqlParser.updateOrInsertStatement_return();
        retval.start = input.LT(1);

        int updateOrInsertStatement_StartIndex = input.index();

        CommonTree root_0 = null;

        Token UPDATE20=null;
        Token OR21=null;
        Token INSERT22=null;
        Token INTO23=null;
        JaybirdSqlParser.tableName_return tableName24 =null;

        JaybirdSqlParser.insertColumns_return insertColumns25 =null;

        JaybirdSqlParser.insertValues_return insertValues26 =null;

        JaybirdSqlParser.matchingClause_return matchingClause27 =null;

        JaybirdSqlParser.returningClause_return returningClause28 =null;


        CommonTree UPDATE20_tree=null;
        CommonTree OR21_tree=null;
        CommonTree INSERT22_tree=null;
        CommonTree INTO23_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 6) ) { return retval; }

            // JaybirdSql.g:256:9: ( UPDATE OR INSERT INTO tableName ( insertColumns )? insertValues ( matchingClause )? ( returningClause )? )
            // JaybirdSql.g:256:14: UPDATE OR INSERT INTO tableName ( insertColumns )? insertValues ( matchingClause )? ( returningClause )?
            {
            root_0 = (CommonTree)adaptor.nil();


            UPDATE20=(Token)match(input,UPDATE,FOLLOW_UPDATE_in_updateOrInsertStatement991); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            UPDATE20_tree = 
            (CommonTree)adaptor.create(UPDATE20)
            ;
            adaptor.addChild(root_0, UPDATE20_tree);
            }

            OR21=(Token)match(input,OR,FOLLOW_OR_in_updateOrInsertStatement993); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            OR21_tree = 
            (CommonTree)adaptor.create(OR21)
            ;
            adaptor.addChild(root_0, OR21_tree);
            }

            INSERT22=(Token)match(input,INSERT,FOLLOW_INSERT_in_updateOrInsertStatement995); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            INSERT22_tree = 
            (CommonTree)adaptor.create(INSERT22)
            ;
            adaptor.addChild(root_0, INSERT22_tree);
            }

            INTO23=(Token)match(input,INTO,FOLLOW_INTO_in_updateOrInsertStatement997); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            INTO23_tree = 
            (CommonTree)adaptor.create(INTO23)
            ;
            adaptor.addChild(root_0, INTO23_tree);
            }

            pushFollow(FOLLOW_tableName_in_updateOrInsertStatement999);
            tableName24=tableName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, tableName24.getTree());

            // JaybirdSql.g:256:46: ( insertColumns )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==LEFT_PAREN) ) {
                alt5=1;
            }
            switch (alt5) {
                case 1 :
                    // JaybirdSql.g:256:46: insertColumns
                    {
                    pushFollow(FOLLOW_insertColumns_in_updateOrInsertStatement1001);
                    insertColumns25=insertColumns();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, insertColumns25.getTree());

                    }
                    break;

            }


            pushFollow(FOLLOW_insertValues_in_updateOrInsertStatement1021);
            insertValues26=insertValues();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, insertValues26.getTree());

            // JaybirdSql.g:257:30: ( matchingClause )?
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0==MATCHING) ) {
                alt6=1;
            }
            switch (alt6) {
                case 1 :
                    // JaybirdSql.g:257:30: matchingClause
                    {
                    pushFollow(FOLLOW_matchingClause_in_updateOrInsertStatement1023);
                    matchingClause27=matchingClause();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, matchingClause27.getTree());

                    }
                    break;

            }


            // JaybirdSql.g:257:46: ( returningClause )?
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0==RETURNING) ) {
                alt7=1;
            }
            switch (alt7) {
                case 1 :
                    // JaybirdSql.g:257:46: returningClause
                    {
                    pushFollow(FOLLOW_returningClause_in_updateOrInsertStatement1026);
                    returningClause28=returningClause();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, returningClause28.getTree());

                    }
                    break;

            }


            if ( state.backtracking==0 ) {
                            statementModel.setStatementType(JaybirdStatementModel.UPDATE_OR_INSERT_TYPE);
                        }

            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
            if ( state.backtracking>0 ) { memoize(input, 6, updateOrInsertStatement_StartIndex); }

        }
        return retval;
    }
    // $ANTLR end "updateOrInsertStatement"


    public static class matchingClause_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "matchingClause"
    // JaybirdSql.g:263:1: matchingClause : MATCHING columnList ;
    public final JaybirdSqlParser.matchingClause_return matchingClause() throws RecognitionException {
        JaybirdSqlParser.matchingClause_return retval = new JaybirdSqlParser.matchingClause_return();
        retval.start = input.LT(1);

        int matchingClause_StartIndex = input.index();

        CommonTree root_0 = null;

        Token MATCHING29=null;
        JaybirdSqlParser.columnList_return columnList30 =null;


        CommonTree MATCHING29_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 7) ) { return retval; }

            // JaybirdSql.g:263:19: ( MATCHING columnList )
            // JaybirdSql.g:263:24: MATCHING columnList
            {
            root_0 = (CommonTree)adaptor.nil();


            MATCHING29=(Token)match(input,MATCHING,FOLLOW_MATCHING_in_matchingClause1072); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            MATCHING29_tree = 
            (CommonTree)adaptor.create(MATCHING29)
            ;
            adaptor.addChild(root_0, MATCHING29_tree);
            }

            pushFollow(FOLLOW_columnList_in_matchingClause1074);
            columnList30=columnList();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, columnList30.getTree());

            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
            if ( state.backtracking>0 ) { memoize(input, 7, matchingClause_StartIndex); }

        }
        return retval;
    }
    // $ANTLR end "matchingClause"


    public static class insertStatement_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "insertStatement"
    // JaybirdSql.g:274:1: insertStatement : INSERT INTO tableName ( insertColumns )? ( insertValues ( returningClause )? | selectClause | defaultValuesClause ( returningClause )? ) ;
    public final JaybirdSqlParser.insertStatement_return insertStatement() throws RecognitionException {
        JaybirdSqlParser.insertStatement_return retval = new JaybirdSqlParser.insertStatement_return();
        retval.start = input.LT(1);

        int insertStatement_StartIndex = input.index();

        CommonTree root_0 = null;

        Token INSERT31=null;
        Token INTO32=null;
        JaybirdSqlParser.tableName_return tableName33 =null;

        JaybirdSqlParser.insertColumns_return insertColumns34 =null;

        JaybirdSqlParser.insertValues_return insertValues35 =null;

        JaybirdSqlParser.returningClause_return returningClause36 =null;

        JaybirdSqlParser.selectClause_return selectClause37 =null;

        JaybirdSqlParser.defaultValuesClause_return defaultValuesClause38 =null;

        JaybirdSqlParser.returningClause_return returningClause39 =null;


        CommonTree INSERT31_tree=null;
        CommonTree INTO32_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 8) ) { return retval; }

            // JaybirdSql.g:275:9: ( INSERT INTO tableName ( insertColumns )? ( insertValues ( returningClause )? | selectClause | defaultValuesClause ( returningClause )? ) )
            // JaybirdSql.g:275:15: INSERT INTO tableName ( insertColumns )? ( insertValues ( returningClause )? | selectClause | defaultValuesClause ( returningClause )? )
            {
            root_0 = (CommonTree)adaptor.nil();


            INSERT31=(Token)match(input,INSERT,FOLLOW_INSERT_in_insertStatement1106); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            INSERT31_tree = 
            (CommonTree)adaptor.create(INSERT31)
            ;
            adaptor.addChild(root_0, INSERT31_tree);
            }

            INTO32=(Token)match(input,INTO,FOLLOW_INTO_in_insertStatement1108); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            INTO32_tree = 
            (CommonTree)adaptor.create(INTO32)
            ;
            adaptor.addChild(root_0, INTO32_tree);
            }

            pushFollow(FOLLOW_tableName_in_insertStatement1110);
            tableName33=tableName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, tableName33.getTree());

            // JaybirdSql.g:275:37: ( insertColumns )?
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0==LEFT_PAREN) ) {
                alt8=1;
            }
            switch (alt8) {
                case 1 :
                    // JaybirdSql.g:275:37: insertColumns
                    {
                    pushFollow(FOLLOW_insertColumns_in_insertStatement1112);
                    insertColumns34=insertColumns();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, insertColumns34.getTree());

                    }
                    break;

            }


            // JaybirdSql.g:276:21: ( insertValues ( returningClause )? | selectClause | defaultValuesClause ( returningClause )? )
            int alt11=3;
            switch ( input.LA(1) ) {
            case VALUES:
                {
                alt11=1;
                }
                break;
            case SELECT:
                {
                alt11=2;
                }
                break;
            case DEFAULT:
                {
                alt11=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 11, 0, input);

                throw nvae;

            }

            switch (alt11) {
                case 1 :
                    // JaybirdSql.g:276:26: insertValues ( returningClause )?
                    {
                    pushFollow(FOLLOW_insertValues_in_insertStatement1140);
                    insertValues35=insertValues();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, insertValues35.getTree());

                    // JaybirdSql.g:276:39: ( returningClause )?
                    int alt9=2;
                    int LA9_0 = input.LA(1);

                    if ( (LA9_0==RETURNING) ) {
                        alt9=1;
                    }
                    switch (alt9) {
                        case 1 :
                            // JaybirdSql.g:276:39: returningClause
                            {
                            pushFollow(FOLLOW_returningClause_in_insertStatement1142);
                            returningClause36=returningClause();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) adaptor.addChild(root_0, returningClause36.getTree());

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // JaybirdSql.g:277:26: selectClause
                    {
                    pushFollow(FOLLOW_selectClause_in_insertStatement1170);
                    selectClause37=selectClause();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, selectClause37.getTree());

                    }
                    break;
                case 3 :
                    // JaybirdSql.g:278:26: defaultValuesClause ( returningClause )?
                    {
                    pushFollow(FOLLOW_defaultValuesClause_in_insertStatement1197);
                    defaultValuesClause38=defaultValuesClause();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, defaultValuesClause38.getTree());

                    // JaybirdSql.g:278:46: ( returningClause )?
                    int alt10=2;
                    int LA10_0 = input.LA(1);

                    if ( (LA10_0==RETURNING) ) {
                        alt10=1;
                    }
                    switch (alt10) {
                        case 1 :
                            // JaybirdSql.g:278:46: returningClause
                            {
                            pushFollow(FOLLOW_returningClause_in_insertStatement1199);
                            returningClause39=returningClause();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) adaptor.addChild(root_0, returningClause39.getTree());

                            }
                            break;

                    }


                    }
                    break;

            }


            if ( state.backtracking==0 ) {
                            statementModel.setStatementType(JaybirdStatementModel.INSERT_TYPE);
                        }

            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
            if ( state.backtracking>0 ) { memoize(input, 8, insertStatement_StartIndex); }

        }
        return retval;
    }
    // $ANTLR end "insertStatement"


    public static class insertColumns_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "insertColumns"
    // JaybirdSql.g:285:1: insertColumns : '(' columnList ')' ;
    public final JaybirdSqlParser.insertColumns_return insertColumns() throws RecognitionException {
        JaybirdSqlParser.insertColumns_return retval = new JaybirdSqlParser.insertColumns_return();
        retval.start = input.LT(1);

        int insertColumns_StartIndex = input.index();

        CommonTree root_0 = null;

        Token char_literal40=null;
        Token char_literal42=null;
        JaybirdSqlParser.columnList_return columnList41 =null;


        CommonTree char_literal40_tree=null;
        CommonTree char_literal42_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 9) ) { return retval; }

            // JaybirdSql.g:286:9: ( '(' columnList ')' )
            // JaybirdSql.g:286:14: '(' columnList ')'
            {
            root_0 = (CommonTree)adaptor.nil();


            char_literal40=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_insertColumns1265); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            char_literal40_tree = 
            (CommonTree)adaptor.create(char_literal40)
            ;
            adaptor.addChild(root_0, char_literal40_tree);
            }

            pushFollow(FOLLOW_columnList_in_insertColumns1267);
            columnList41=columnList();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, columnList41.getTree());

            char_literal42=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_insertColumns1269); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            char_literal42_tree = 
            (CommonTree)adaptor.create(char_literal42)
            ;
            adaptor.addChild(root_0, char_literal42_tree);
            }

            if ( state.backtracking==0 ) {
                            _inReturning = false;
                        }

            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
            if ( state.backtracking>0 ) { memoize(input, 9, insertColumns_StartIndex); }

        }
        return retval;
    }
    // $ANTLR end "insertColumns"


    public static class insertValues_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "insertValues"
    // JaybirdSql.g:292:1: insertValues : VALUES '(' valueList ')' ;
    public final JaybirdSqlParser.insertValues_return insertValues() throws RecognitionException {
        JaybirdSqlParser.insertValues_return retval = new JaybirdSqlParser.insertValues_return();
        retval.start = input.LT(1);

        int insertValues_StartIndex = input.index();

        CommonTree root_0 = null;

        Token VALUES43=null;
        Token char_literal44=null;
        Token char_literal46=null;
        JaybirdSqlParser.valueList_return valueList45 =null;


        CommonTree VALUES43_tree=null;
        CommonTree char_literal44_tree=null;
        CommonTree char_literal46_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 10) ) { return retval; }

            // JaybirdSql.g:293:9: ( VALUES '(' valueList ')' )
            // JaybirdSql.g:293:14: VALUES '(' valueList ')'
            {
            root_0 = (CommonTree)adaptor.nil();


            VALUES43=(Token)match(input,VALUES,FOLLOW_VALUES_in_insertValues1312); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            VALUES43_tree = 
            (CommonTree)adaptor.create(VALUES43)
            ;
            adaptor.addChild(root_0, VALUES43_tree);
            }

            char_literal44=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_insertValues1314); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            char_literal44_tree = 
            (CommonTree)adaptor.create(char_literal44)
            ;
            adaptor.addChild(root_0, char_literal44_tree);
            }

            pushFollow(FOLLOW_valueList_in_insertValues1316);
            valueList45=valueList();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, valueList45.getTree());

            char_literal46=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_insertValues1318); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            char_literal46_tree = 
            (CommonTree)adaptor.create(char_literal46)
            ;
            adaptor.addChild(root_0, char_literal46_tree);
            }

            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
            if ( state.backtracking>0 ) { memoize(input, 10, insertValues_StartIndex); }

        }
        return retval;
    }
    // $ANTLR end "insertValues"


    public static class returningClause_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "returningClause"
    // JaybirdSql.g:296:1: returningClause : RETURNING columnList ;
    public final JaybirdSqlParser.returningClause_return returningClause() throws RecognitionException {
        JaybirdSqlParser.returningClause_return retval = new JaybirdSqlParser.returningClause_return();
        retval.start = input.LT(1);

        int returningClause_StartIndex = input.index();

        CommonTree root_0 = null;

        Token RETURNING47=null;
        JaybirdSqlParser.columnList_return columnList48 =null;


        CommonTree RETURNING47_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 11) ) { return retval; }

            // JaybirdSql.g:297:9: ( RETURNING columnList )
            // JaybirdSql.g:297:14: RETURNING columnList
            {
            root_0 = (CommonTree)adaptor.nil();


            RETURNING47=(Token)match(input,RETURNING,FOLLOW_RETURNING_in_returningClause1354); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RETURNING47_tree = 
            (CommonTree)adaptor.create(RETURNING47)
            ;
            adaptor.addChild(root_0, RETURNING47_tree);
            }

            if ( state.backtracking==0 ) {_inReturning = true;}

            pushFollow(FOLLOW_columnList_in_returningClause1358);
            columnList48=columnList();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, columnList48.getTree());

            if ( state.backtracking==0 ) {_inReturning = true;}

            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
            if ( state.backtracking>0 ) { memoize(input, 11, returningClause_StartIndex); }

        }
        return retval;
    }
    // $ANTLR end "returningClause"


    public static class defaultValuesClause_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "defaultValuesClause"
    // JaybirdSql.g:300:1: defaultValuesClause : DEFAULT VALUES ;
    public final JaybirdSqlParser.defaultValuesClause_return defaultValuesClause() throws RecognitionException {
        JaybirdSqlParser.defaultValuesClause_return retval = new JaybirdSqlParser.defaultValuesClause_return();
        retval.start = input.LT(1);

        int defaultValuesClause_StartIndex = input.index();

        CommonTree root_0 = null;

        Token DEFAULT49=null;
        Token VALUES50=null;

        CommonTree DEFAULT49_tree=null;
        CommonTree VALUES50_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 12) ) { return retval; }

            // JaybirdSql.g:301:9: ( DEFAULT VALUES )
            // JaybirdSql.g:301:14: DEFAULT VALUES
            {
            root_0 = (CommonTree)adaptor.nil();


            DEFAULT49=(Token)match(input,DEFAULT,FOLLOW_DEFAULT_in_defaultValuesClause1388); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            DEFAULT49_tree = 
            (CommonTree)adaptor.create(DEFAULT49)
            ;
            adaptor.addChild(root_0, DEFAULT49_tree);
            }

            VALUES50=(Token)match(input,VALUES,FOLLOW_VALUES_in_defaultValuesClause1390); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            VALUES50_tree = 
            (CommonTree)adaptor.create(VALUES50)
            ;
            adaptor.addChild(root_0, VALUES50_tree);
            }

            if ( state.backtracking==0 ) {
                            statementModel.setDefaultValues(true);
                        }

            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
            if ( state.backtracking>0 ) { memoize(input, 12, defaultValuesClause_StartIndex); }

        }
        return retval;
    }
    // $ANTLR end "defaultValuesClause"


    public static class simpleIdentifier_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "simpleIdentifier"
    // JaybirdSql.g:307:1: simpleIdentifier : ( GENERIC_ID | QUOTED_ID );
    public final JaybirdSqlParser.simpleIdentifier_return simpleIdentifier() throws RecognitionException {
        JaybirdSqlParser.simpleIdentifier_return retval = new JaybirdSqlParser.simpleIdentifier_return();
        retval.start = input.LT(1);

        int simpleIdentifier_StartIndex = input.index();

        CommonTree root_0 = null;

        Token set51=null;

        CommonTree set51_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 13) ) { return retval; }

            // JaybirdSql.g:308:9: ( GENERIC_ID | QUOTED_ID )
            // JaybirdSql.g:
            {
            root_0 = (CommonTree)adaptor.nil();


            set51=(Token)input.LT(1);

            if ( input.LA(1)==GENERIC_ID||input.LA(1)==QUOTED_ID ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, 
                (CommonTree)adaptor.create(set51)
                );
                state.errorRecovery=false;
                state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
            if ( state.backtracking>0 ) { memoize(input, 13, simpleIdentifier_StartIndex); }

        }
        return retval;
    }
    // $ANTLR end "simpleIdentifier"


    public static class fullIdentifier_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "fullIdentifier"
    // JaybirdSql.g:312:1: fullIdentifier : simpleIdentifier '.' simpleIdentifier ;
    public final JaybirdSqlParser.fullIdentifier_return fullIdentifier() throws RecognitionException {
        JaybirdSqlParser.fullIdentifier_return retval = new JaybirdSqlParser.fullIdentifier_return();
        retval.start = input.LT(1);

        int fullIdentifier_StartIndex = input.index();

        CommonTree root_0 = null;

        Token char_literal53=null;
        JaybirdSqlParser.simpleIdentifier_return simpleIdentifier52 =null;

        JaybirdSqlParser.simpleIdentifier_return simpleIdentifier54 =null;


        CommonTree char_literal53_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 14) ) { return retval; }

            // JaybirdSql.g:313:9: ( simpleIdentifier '.' simpleIdentifier )
            // JaybirdSql.g:313:14: simpleIdentifier '.' simpleIdentifier
            {
            root_0 = (CommonTree)adaptor.nil();


            pushFollow(FOLLOW_simpleIdentifier_in_fullIdentifier1481);
            simpleIdentifier52=simpleIdentifier();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleIdentifier52.getTree());

            char_literal53=(Token)match(input,77,FOLLOW_77_in_fullIdentifier1483); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            char_literal53_tree = 
            (CommonTree)adaptor.create(char_literal53)
            ;
            adaptor.addChild(root_0, char_literal53_tree);
            }

            pushFollow(FOLLOW_simpleIdentifier_in_fullIdentifier1485);
            simpleIdentifier54=simpleIdentifier();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleIdentifier54.getTree());

            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
            if ( state.backtracking>0 ) { memoize(input, 14, fullIdentifier_StartIndex); }

        }
        return retval;
    }
    // $ANTLR end "fullIdentifier"


    public static class tableName_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "tableName"
    // JaybirdSql.g:316:1: tableName : t= simpleIdentifier ;
    public final JaybirdSqlParser.tableName_return tableName() throws RecognitionException {
        JaybirdSqlParser.tableName_return retval = new JaybirdSqlParser.tableName_return();
        retval.start = input.LT(1);

        int tableName_StartIndex = input.index();

        CommonTree root_0 = null;

        JaybirdSqlParser.simpleIdentifier_return t =null;



        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 15) ) { return retval; }

            // JaybirdSql.g:317:9: (t= simpleIdentifier )
            // JaybirdSql.g:317:14: t= simpleIdentifier
            {
            root_0 = (CommonTree)adaptor.nil();


            pushFollow(FOLLOW_simpleIdentifier_in_tableName1522);
            t=simpleIdentifier();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, t.getTree());

            if ( state.backtracking==0 ) {
                            statementModel.setTableName((t!=null?input.toString(t.start,t.stop):null));
                        }

            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
            if ( state.backtracking>0 ) { memoize(input, 15, tableName_StartIndex); }

        }
        return retval;
    }
    // $ANTLR end "tableName"


    public static class columnList_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "columnList"
    // JaybirdSql.g:323:1: columnList : columnName ( ',' columnName )* ;
    public final JaybirdSqlParser.columnList_return columnList() throws RecognitionException {
        JaybirdSqlParser.columnList_return retval = new JaybirdSqlParser.columnList_return();
        retval.start = input.LT(1);

        int columnList_StartIndex = input.index();

        CommonTree root_0 = null;

        Token char_literal56=null;
        JaybirdSqlParser.columnName_return columnName55 =null;

        JaybirdSqlParser.columnName_return columnName57 =null;


        CommonTree char_literal56_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 16) ) { return retval; }

            // JaybirdSql.g:324:9: ( columnName ( ',' columnName )* )
            // JaybirdSql.g:324:14: columnName ( ',' columnName )*
            {
            root_0 = (CommonTree)adaptor.nil();


            pushFollow(FOLLOW_columnName_in_columnList1570);
            columnName55=columnName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, columnName55.getTree());

            // JaybirdSql.g:324:25: ( ',' columnName )*
            loop12:
            do {
                int alt12=2;
                int LA12_0 = input.LA(1);

                if ( (LA12_0==COMMA) ) {
                    alt12=1;
                }


                switch (alt12) {
            	case 1 :
            	    // JaybirdSql.g:324:26: ',' columnName
            	    {
            	    char_literal56=(Token)match(input,COMMA,FOLLOW_COMMA_in_columnList1573); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    char_literal56_tree = 
            	    (CommonTree)adaptor.create(char_literal56)
            	    ;
            	    adaptor.addChild(root_0, char_literal56_tree);
            	    }

            	    pushFollow(FOLLOW_columnName_in_columnList1575);
            	    columnName57=columnName();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, columnName57.getTree());

            	    }
            	    break;

            	default :
            	    break loop12;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
            if ( state.backtracking>0 ) { memoize(input, 16, columnList_StartIndex); }

        }
        return retval;
    }
    // $ANTLR end "columnList"


    public static class columnName_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "columnName"
    // JaybirdSql.g:327:1: columnName : (si= simpleIdentifier |fi= fullIdentifier );
    public final JaybirdSqlParser.columnName_return columnName() throws RecognitionException {
        JaybirdSqlParser.columnName_return retval = new JaybirdSqlParser.columnName_return();
        retval.start = input.LT(1);

        int columnName_StartIndex = input.index();

        CommonTree root_0 = null;

        JaybirdSqlParser.simpleIdentifier_return si =null;

        JaybirdSqlParser.fullIdentifier_return fi =null;



        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 17) ) { return retval; }

            // JaybirdSql.g:328:9: (si= simpleIdentifier |fi= fullIdentifier )
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( (LA13_0==GENERIC_ID||LA13_0==QUOTED_ID) ) {
                int LA13_1 = input.LA(2);

                if ( (LA13_1==EOF||LA13_1==COMMA||(LA13_1 >= RETURNING && LA13_1 <= RIGHT_PAREN)||LA13_1==80) ) {
                    alt13=1;
                }
                else if ( (LA13_1==77) ) {
                    alt13=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 13, 1, input);

                    throw nvae;

                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 13, 0, input);

                throw nvae;

            }
            switch (alt13) {
                case 1 :
                    // JaybirdSql.g:328:14: si= simpleIdentifier
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_simpleIdentifier_in_columnName1609);
                    si=simpleIdentifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, si.getTree());

                    if ( state.backtracking==0 ) {
                                    if (_inReturning)
                                        statementModel.addReturningColumn((si!=null?input.toString(si.start,si.stop):null));
                                    else
                                        statementModel.addColumn((si!=null?input.toString(si.start,si.stop):null));
                                }

                    }
                    break;
                case 2 :
                    // JaybirdSql.g:336:14: fi= fullIdentifier
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_fullIdentifier_in_columnName1656);
                    fi=fullIdentifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, fi.getTree());

                    if ( state.backtracking==0 ) {
                                    if (_inReturning)
                                        statementModel.addReturningColumn((fi!=null?input.toString(fi.start,fi.stop):null));
                                    else
                                        statementModel.addColumn((fi!=null?input.toString(fi.start,fi.stop):null));
                                }

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
            if ( state.backtracking>0 ) { memoize(input, 17, columnName_StartIndex); }

        }
        return retval;
    }
    // $ANTLR end "columnName"


    public static class valueList_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "valueList"
    // JaybirdSql.g:345:1: valueList : value ( ',' value )* ;
    public final JaybirdSqlParser.valueList_return valueList() throws RecognitionException {
        JaybirdSqlParser.valueList_return retval = new JaybirdSqlParser.valueList_return();
        retval.start = input.LT(1);

        int valueList_StartIndex = input.index();

        CommonTree root_0 = null;

        Token char_literal59=null;
        JaybirdSqlParser.value_return value58 =null;

        JaybirdSqlParser.value_return value60 =null;


        CommonTree char_literal59_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 18) ) { return retval; }

            // JaybirdSql.g:346:9: ( value ( ',' value )* )
            // JaybirdSql.g:346:14: value ( ',' value )*
            {
            root_0 = (CommonTree)adaptor.nil();


            pushFollow(FOLLOW_value_in_valueList1699);
            value58=value();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, value58.getTree());

            // JaybirdSql.g:346:20: ( ',' value )*
            loop14:
            do {
                int alt14=2;
                int LA14_0 = input.LA(1);

                if ( (LA14_0==COMMA) ) {
                    alt14=1;
                }


                switch (alt14) {
            	case 1 :
            	    // JaybirdSql.g:346:21: ',' value
            	    {
            	    char_literal59=(Token)match(input,COMMA,FOLLOW_COMMA_in_valueList1702); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    char_literal59_tree = 
            	    (CommonTree)adaptor.create(char_literal59)
            	    ;
            	    adaptor.addChild(root_0, char_literal59_tree);
            	    }

            	    pushFollow(FOLLOW_value_in_valueList1704);
            	    value60=value();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, value60.getTree());

            	    }
            	    break;

            	default :
            	    break loop14;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
            if ( state.backtracking>0 ) { memoize(input, 18, valueList_StartIndex); }

        }
        return retval;
    }
    // $ANTLR end "valueList"


    public static class value_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "value"
    // JaybirdSql.g:380:1: value : ( simpleValue | simpleValue '+' simpleValue | simpleValue '-' simpleValue | simpleValue '*' simpleValue | simpleValue '/' simpleValue | simpleValue '||' simpleValue | '+' simpleValue | '-' simpleValue | LEFT_PAREN simpleValue RIGHT_PAREN | simpleValue COLLATE simpleIdentifier | parameter | CURRENT_USER | CURRENT_ROLE | CURRENT_DATE | CURRENT_TIME | CURRENT_TIMESTAMP | nullValue | function | nextValueExpression | castExpression | arrayElement | DB_KEY | simpleIdentifier '.' DB_KEY );
    public final JaybirdSqlParser.value_return value() throws RecognitionException {
        JaybirdSqlParser.value_return retval = new JaybirdSqlParser.value_return();
        retval.start = input.LT(1);

        int value_StartIndex = input.index();

        CommonTree root_0 = null;

        Token char_literal63=null;
        Token char_literal66=null;
        Token char_literal69=null;
        Token char_literal72=null;
        Token string_literal75=null;
        Token char_literal77=null;
        Token char_literal79=null;
        Token LEFT_PAREN81=null;
        Token RIGHT_PAREN83=null;
        Token COLLATE85=null;
        Token CURRENT_USER88=null;
        Token CURRENT_ROLE89=null;
        Token CURRENT_DATE90=null;
        Token CURRENT_TIME91=null;
        Token CURRENT_TIMESTAMP92=null;
        Token DB_KEY98=null;
        Token char_literal100=null;
        Token DB_KEY101=null;
        JaybirdSqlParser.simpleValue_return simpleValue61 =null;

        JaybirdSqlParser.simpleValue_return simpleValue62 =null;

        JaybirdSqlParser.simpleValue_return simpleValue64 =null;

        JaybirdSqlParser.simpleValue_return simpleValue65 =null;

        JaybirdSqlParser.simpleValue_return simpleValue67 =null;

        JaybirdSqlParser.simpleValue_return simpleValue68 =null;

        JaybirdSqlParser.simpleValue_return simpleValue70 =null;

        JaybirdSqlParser.simpleValue_return simpleValue71 =null;

        JaybirdSqlParser.simpleValue_return simpleValue73 =null;

        JaybirdSqlParser.simpleValue_return simpleValue74 =null;

        JaybirdSqlParser.simpleValue_return simpleValue76 =null;

        JaybirdSqlParser.simpleValue_return simpleValue78 =null;

        JaybirdSqlParser.simpleValue_return simpleValue80 =null;

        JaybirdSqlParser.simpleValue_return simpleValue82 =null;

        JaybirdSqlParser.simpleValue_return simpleValue84 =null;

        JaybirdSqlParser.simpleIdentifier_return simpleIdentifier86 =null;

        JaybirdSqlParser.parameter_return parameter87 =null;

        JaybirdSqlParser.nullValue_return nullValue93 =null;

        JaybirdSqlParser.function_return function94 =null;

        JaybirdSqlParser.nextValueExpression_return nextValueExpression95 =null;

        JaybirdSqlParser.castExpression_return castExpression96 =null;

        JaybirdSqlParser.arrayElement_return arrayElement97 =null;

        JaybirdSqlParser.simpleIdentifier_return simpleIdentifier99 =null;


        CommonTree char_literal63_tree=null;
        CommonTree char_literal66_tree=null;
        CommonTree char_literal69_tree=null;
        CommonTree char_literal72_tree=null;
        CommonTree string_literal75_tree=null;
        CommonTree char_literal77_tree=null;
        CommonTree char_literal79_tree=null;
        CommonTree LEFT_PAREN81_tree=null;
        CommonTree RIGHT_PAREN83_tree=null;
        CommonTree COLLATE85_tree=null;
        CommonTree CURRENT_USER88_tree=null;
        CommonTree CURRENT_ROLE89_tree=null;
        CommonTree CURRENT_DATE90_tree=null;
        CommonTree CURRENT_TIME91_tree=null;
        CommonTree CURRENT_TIMESTAMP92_tree=null;
        CommonTree DB_KEY98_tree=null;
        CommonTree char_literal100_tree=null;
        CommonTree DB_KEY101_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 19) ) { return retval; }

            // JaybirdSql.g:381:9: ( simpleValue | simpleValue '+' simpleValue | simpleValue '-' simpleValue | simpleValue '*' simpleValue | simpleValue '/' simpleValue | simpleValue '||' simpleValue | '+' simpleValue | '-' simpleValue | LEFT_PAREN simpleValue RIGHT_PAREN | simpleValue COLLATE simpleIdentifier | parameter | CURRENT_USER | CURRENT_ROLE | CURRENT_DATE | CURRENT_TIME | CURRENT_TIMESTAMP | nullValue | function | nextValueExpression | castExpression | arrayElement | DB_KEY | simpleIdentifier '.' DB_KEY )
            int alt15=23;
            switch ( input.LA(1) ) {
            case GENERIC_ID:
                {
                switch ( input.LA(2) ) {
                case EOF:
                case AS:
                case COMMA:
                case FOR:
                case FROM:
                case RETURNING:
                case RIGHT_PAREN:
                case 83:
                    {
                    alt15=1;
                    }
                    break;
                case 75:
                    {
                    alt15=2;
                    }
                    break;
                case 76:
                    {
                    alt15=3;
                    }
                    break;
                case 74:
                    {
                    alt15=4;
                    }
                    break;
                case 78:
                    {
                    alt15=5;
                    }
                    break;
                case 84:
                    {
                    alt15=6;
                    }
                    break;
                case COLLATE:
                    {
                    alt15=10;
                    }
                    break;
                case LEFT_PAREN:
                    {
                    alt15=18;
                    }
                    break;
                case 82:
                    {
                    alt15=21;
                    }
                    break;
                case 77:
                    {
                    alt15=23;
                    }
                    break;
                default:
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 15, 1, input);

                    throw nvae;

                }

                }
                break;
            case 75:
                {
                alt15=7;
                }
                break;
            case 76:
                {
                alt15=8;
                }
                break;
            case LEFT_PAREN:
                {
                alt15=9;
                }
                break;
            case 81:
                {
                alt15=11;
                }
                break;
            case CURRENT_USER:
                {
                alt15=12;
                }
                break;
            case CURRENT_ROLE:
                {
                alt15=13;
                }
                break;
            case CURRENT_DATE:
                {
                alt15=14;
                }
                break;
            case CURRENT_TIME:
                {
                alt15=15;
                }
                break;
            case CURRENT_TIMESTAMP:
                {
                alt15=16;
                }
                break;
            case NULL:
                {
                alt15=17;
                }
                break;
            case INTEGER:
            case REAL:
            case STRING:
                {
                switch ( input.LA(2) ) {
                case EOF:
                case AS:
                case COMMA:
                case FOR:
                case FROM:
                case RETURNING:
                case RIGHT_PAREN:
                case 83:
                    {
                    alt15=1;
                    }
                    break;
                case 75:
                    {
                    alt15=2;
                    }
                    break;
                case 76:
                    {
                    alt15=3;
                    }
                    break;
                case 74:
                    {
                    alt15=4;
                    }
                    break;
                case 78:
                    {
                    alt15=5;
                    }
                    break;
                case 84:
                    {
                    alt15=6;
                    }
                    break;
                case COLLATE:
                    {
                    alt15=10;
                    }
                    break;
                default:
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 15, 12, input);

                    throw nvae;

                }

                }
                break;
            case QUOTED_ID:
                {
                switch ( input.LA(2) ) {
                case LEFT_PAREN:
                    {
                    alt15=18;
                    }
                    break;
                case 82:
                    {
                    alt15=21;
                    }
                    break;
                case 77:
                    {
                    alt15=23;
                    }
                    break;
                default:
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 15, 13, input);

                    throw nvae;

                }

                }
                break;
            case AVG:
            case COUNT:
            case EXTRACT:
            case MAXIMUM:
            case MINIMUM:
            case SUBSTRING:
            case SUM:
            case TRIM:
                {
                alt15=18;
                }
                break;
            case GEN_ID:
            case NEXT:
                {
                alt15=19;
                }
                break;
            case CAST:
                {
                alt15=20;
                }
                break;
            case DB_KEY:
                {
                alt15=22;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 15, 0, input);

                throw nvae;

            }

            switch (alt15) {
                case 1 :
                    // JaybirdSql.g:381:14: simpleValue
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_simpleValue_in_value1740);
                    simpleValue61=simpleValue();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleValue61.getTree());

                    }
                    break;
                case 2 :
                    // JaybirdSql.g:382:14: simpleValue '+' simpleValue
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_simpleValue_in_value1755);
                    simpleValue62=simpleValue();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleValue62.getTree());

                    char_literal63=(Token)match(input,75,FOLLOW_75_in_value1757); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal63_tree = 
                    (CommonTree)adaptor.create(char_literal63)
                    ;
                    adaptor.addChild(root_0, char_literal63_tree);
                    }

                    pushFollow(FOLLOW_simpleValue_in_value1759);
                    simpleValue64=simpleValue();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleValue64.getTree());

                    }
                    break;
                case 3 :
                    // JaybirdSql.g:383:14: simpleValue '-' simpleValue
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_simpleValue_in_value1774);
                    simpleValue65=simpleValue();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleValue65.getTree());

                    char_literal66=(Token)match(input,76,FOLLOW_76_in_value1776); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal66_tree = 
                    (CommonTree)adaptor.create(char_literal66)
                    ;
                    adaptor.addChild(root_0, char_literal66_tree);
                    }

                    pushFollow(FOLLOW_simpleValue_in_value1778);
                    simpleValue67=simpleValue();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleValue67.getTree());

                    }
                    break;
                case 4 :
                    // JaybirdSql.g:384:14: simpleValue '*' simpleValue
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_simpleValue_in_value1793);
                    simpleValue68=simpleValue();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleValue68.getTree());

                    char_literal69=(Token)match(input,74,FOLLOW_74_in_value1795); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal69_tree = 
                    (CommonTree)adaptor.create(char_literal69)
                    ;
                    adaptor.addChild(root_0, char_literal69_tree);
                    }

                    pushFollow(FOLLOW_simpleValue_in_value1797);
                    simpleValue70=simpleValue();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleValue70.getTree());

                    }
                    break;
                case 5 :
                    // JaybirdSql.g:385:14: simpleValue '/' simpleValue
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_simpleValue_in_value1812);
                    simpleValue71=simpleValue();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleValue71.getTree());

                    char_literal72=(Token)match(input,78,FOLLOW_78_in_value1817); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal72_tree = 
                    (CommonTree)adaptor.create(char_literal72)
                    ;
                    adaptor.addChild(root_0, char_literal72_tree);
                    }

                    pushFollow(FOLLOW_simpleValue_in_value1819);
                    simpleValue73=simpleValue();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleValue73.getTree());

                    }
                    break;
                case 6 :
                    // JaybirdSql.g:386:14: simpleValue '||' simpleValue
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_simpleValue_in_value1834);
                    simpleValue74=simpleValue();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleValue74.getTree());

                    string_literal75=(Token)match(input,84,FOLLOW_84_in_value1836); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    string_literal75_tree = 
                    (CommonTree)adaptor.create(string_literal75)
                    ;
                    adaptor.addChild(root_0, string_literal75_tree);
                    }

                    pushFollow(FOLLOW_simpleValue_in_value1838);
                    simpleValue76=simpleValue();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleValue76.getTree());

                    }
                    break;
                case 7 :
                    // JaybirdSql.g:387:14: '+' simpleValue
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    char_literal77=(Token)match(input,75,FOLLOW_75_in_value1853); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal77_tree = 
                    (CommonTree)adaptor.create(char_literal77)
                    ;
                    adaptor.addChild(root_0, char_literal77_tree);
                    }

                    pushFollow(FOLLOW_simpleValue_in_value1855);
                    simpleValue78=simpleValue();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleValue78.getTree());

                    }
                    break;
                case 8 :
                    // JaybirdSql.g:388:14: '-' simpleValue
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    char_literal79=(Token)match(input,76,FOLLOW_76_in_value1870); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal79_tree = 
                    (CommonTree)adaptor.create(char_literal79)
                    ;
                    adaptor.addChild(root_0, char_literal79_tree);
                    }

                    pushFollow(FOLLOW_simpleValue_in_value1872);
                    simpleValue80=simpleValue();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleValue80.getTree());

                    }
                    break;
                case 9 :
                    // JaybirdSql.g:390:14: LEFT_PAREN simpleValue RIGHT_PAREN
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    LEFT_PAREN81=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_value1896); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LEFT_PAREN81_tree = 
                    (CommonTree)adaptor.create(LEFT_PAREN81)
                    ;
                    adaptor.addChild(root_0, LEFT_PAREN81_tree);
                    }

                    pushFollow(FOLLOW_simpleValue_in_value1898);
                    simpleValue82=simpleValue();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleValue82.getTree());

                    RIGHT_PAREN83=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_value1900); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RIGHT_PAREN83_tree = 
                    (CommonTree)adaptor.create(RIGHT_PAREN83)
                    ;
                    adaptor.addChild(root_0, RIGHT_PAREN83_tree);
                    }

                    }
                    break;
                case 10 :
                    // JaybirdSql.g:392:14: simpleValue COLLATE simpleIdentifier
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_simpleValue_in_value1924);
                    simpleValue84=simpleValue();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleValue84.getTree());

                    COLLATE85=(Token)match(input,COLLATE,FOLLOW_COLLATE_in_value1926); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COLLATE85_tree = 
                    (CommonTree)adaptor.create(COLLATE85)
                    ;
                    adaptor.addChild(root_0, COLLATE85_tree);
                    }

                    pushFollow(FOLLOW_simpleIdentifier_in_value1931);
                    simpleIdentifier86=simpleIdentifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleIdentifier86.getTree());

                    }
                    break;
                case 11 :
                    // JaybirdSql.g:394:14: parameter
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_parameter_in_value1951);
                    parameter87=parameter();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, parameter87.getTree());

                    }
                    break;
                case 12 :
                    // JaybirdSql.g:396:14: CURRENT_USER
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    CURRENT_USER88=(Token)match(input,CURRENT_USER,FOLLOW_CURRENT_USER_in_value1975); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    CURRENT_USER88_tree = 
                    (CommonTree)adaptor.create(CURRENT_USER88)
                    ;
                    adaptor.addChild(root_0, CURRENT_USER88_tree);
                    }

                    }
                    break;
                case 13 :
                    // JaybirdSql.g:397:14: CURRENT_ROLE
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    CURRENT_ROLE89=(Token)match(input,CURRENT_ROLE,FOLLOW_CURRENT_ROLE_in_value1990); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    CURRENT_ROLE89_tree = 
                    (CommonTree)adaptor.create(CURRENT_ROLE89)
                    ;
                    adaptor.addChild(root_0, CURRENT_ROLE89_tree);
                    }

                    }
                    break;
                case 14 :
                    // JaybirdSql.g:398:14: CURRENT_DATE
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    CURRENT_DATE90=(Token)match(input,CURRENT_DATE,FOLLOW_CURRENT_DATE_in_value2005); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    CURRENT_DATE90_tree = 
                    (CommonTree)adaptor.create(CURRENT_DATE90)
                    ;
                    adaptor.addChild(root_0, CURRENT_DATE90_tree);
                    }

                    }
                    break;
                case 15 :
                    // JaybirdSql.g:399:14: CURRENT_TIME
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    CURRENT_TIME91=(Token)match(input,CURRENT_TIME,FOLLOW_CURRENT_TIME_in_value2020); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    CURRENT_TIME91_tree = 
                    (CommonTree)adaptor.create(CURRENT_TIME91)
                    ;
                    adaptor.addChild(root_0, CURRENT_TIME91_tree);
                    }

                    }
                    break;
                case 16 :
                    // JaybirdSql.g:400:14: CURRENT_TIMESTAMP
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    CURRENT_TIMESTAMP92=(Token)match(input,CURRENT_TIMESTAMP,FOLLOW_CURRENT_TIMESTAMP_in_value2035); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    CURRENT_TIMESTAMP92_tree = 
                    (CommonTree)adaptor.create(CURRENT_TIMESTAMP92)
                    ;
                    adaptor.addChild(root_0, CURRENT_TIMESTAMP92_tree);
                    }

                    }
                    break;
                case 17 :
                    // JaybirdSql.g:402:14: nullValue
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_nullValue_in_value2059);
                    nullValue93=nullValue();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, nullValue93.getTree());

                    }
                    break;
                case 18 :
                    // JaybirdSql.g:404:14: function
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_function_in_value2083);
                    function94=function();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, function94.getTree());

                    }
                    break;
                case 19 :
                    // JaybirdSql.g:405:14: nextValueExpression
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_nextValueExpression_in_value2098);
                    nextValueExpression95=nextValueExpression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, nextValueExpression95.getTree());

                    }
                    break;
                case 20 :
                    // JaybirdSql.g:406:14: castExpression
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_castExpression_in_value2113);
                    castExpression96=castExpression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, castExpression96.getTree());

                    }
                    break;
                case 21 :
                    // JaybirdSql.g:409:14: arrayElement
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_arrayElement_in_value2138);
                    arrayElement97=arrayElement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, arrayElement97.getTree());

                    }
                    break;
                case 22 :
                    // JaybirdSql.g:411:14: DB_KEY
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    DB_KEY98=(Token)match(input,DB_KEY,FOLLOW_DB_KEY_in_value2162); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    DB_KEY98_tree = 
                    (CommonTree)adaptor.create(DB_KEY98)
                    ;
                    adaptor.addChild(root_0, DB_KEY98_tree);
                    }

                    }
                    break;
                case 23 :
                    // JaybirdSql.g:412:14: simpleIdentifier '.' DB_KEY
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_simpleIdentifier_in_value2177);
                    simpleIdentifier99=simpleIdentifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleIdentifier99.getTree());

                    char_literal100=(Token)match(input,77,FOLLOW_77_in_value2179); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal100_tree = 
                    (CommonTree)adaptor.create(char_literal100)
                    ;
                    adaptor.addChild(root_0, char_literal100_tree);
                    }

                    DB_KEY101=(Token)match(input,DB_KEY,FOLLOW_DB_KEY_in_value2181); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    DB_KEY101_tree = 
                    (CommonTree)adaptor.create(DB_KEY101)
                    ;
                    adaptor.addChild(root_0, DB_KEY101_tree);
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
            if ( state.backtracking>0 ) { memoize(input, 19, value_StartIndex); }

        }
        return retval;
    }
    // $ANTLR end "value"


    public static class parameter_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "parameter"
    // JaybirdSql.g:415:1: parameter : '?' ;
    public final JaybirdSqlParser.parameter_return parameter() throws RecognitionException {
        JaybirdSqlParser.parameter_return retval = new JaybirdSqlParser.parameter_return();
        retval.start = input.LT(1);

        int parameter_StartIndex = input.index();

        CommonTree root_0 = null;

        Token char_literal102=null;

        CommonTree char_literal102_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 20) ) { return retval; }

            // JaybirdSql.g:416:9: ( '?' )
            // JaybirdSql.g:416:14: '?'
            {
            root_0 = (CommonTree)adaptor.nil();


            char_literal102=(Token)match(input,81,FOLLOW_81_in_parameter2213); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            char_literal102_tree = 
            (CommonTree)adaptor.create(char_literal102)
            ;
            adaptor.addChild(root_0, char_literal102_tree);
            }

            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
            if ( state.backtracking>0 ) { memoize(input, 20, parameter_StartIndex); }

        }
        return retval;
    }
    // $ANTLR end "parameter"


    public static class nullValue_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "nullValue"
    // JaybirdSql.g:419:1: nullValue : NULL ;
    public final JaybirdSqlParser.nullValue_return nullValue() throws RecognitionException {
        JaybirdSqlParser.nullValue_return retval = new JaybirdSqlParser.nullValue_return();
        retval.start = input.LT(1);

        int nullValue_StartIndex = input.index();

        CommonTree root_0 = null;

        Token NULL103=null;

        CommonTree NULL103_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 21) ) { return retval; }

            // JaybirdSql.g:420:9: ( NULL )
            // JaybirdSql.g:420:14: NULL
            {
            root_0 = (CommonTree)adaptor.nil();


            NULL103=(Token)match(input,NULL,FOLLOW_NULL_in_nullValue2241); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            NULL103_tree = 
            (CommonTree)adaptor.create(NULL103)
            ;
            adaptor.addChild(root_0, NULL103_tree);
            }

            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
            if ( state.backtracking>0 ) { memoize(input, 21, nullValue_StartIndex); }

        }
        return retval;
    }
    // $ANTLR end "nullValue"


    public static class simpleValue_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "simpleValue"
    // JaybirdSql.g:423:1: simpleValue : ( GENERIC_ID | STRING | INTEGER | REAL );
    public final JaybirdSqlParser.simpleValue_return simpleValue() throws RecognitionException {
        JaybirdSqlParser.simpleValue_return retval = new JaybirdSqlParser.simpleValue_return();
        retval.start = input.LT(1);

        int simpleValue_StartIndex = input.index();

        CommonTree root_0 = null;

        Token set104=null;

        CommonTree set104_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 22) ) { return retval; }

            // JaybirdSql.g:424:9: ( GENERIC_ID | STRING | INTEGER | REAL )
            // JaybirdSql.g:
            {
            root_0 = (CommonTree)adaptor.nil();


            set104=(Token)input.LT(1);

            if ( input.LA(1)==GENERIC_ID||input.LA(1)==INTEGER||input.LA(1)==REAL||input.LA(1)==STRING ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, 
                (CommonTree)adaptor.create(set104)
                );
                state.errorRecovery=false;
                state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
            if ( state.backtracking>0 ) { memoize(input, 22, simpleValue_StartIndex); }

        }
        return retval;
    }
    // $ANTLR end "simpleValue"


    public static class nextValueExpression_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "nextValueExpression"
    // JaybirdSql.g:430:1: nextValueExpression : ( NEXT VALUE FOR simpleIdentifier | GEN_ID '(' simpleIdentifier ',' INTEGER ')' );
    public final JaybirdSqlParser.nextValueExpression_return nextValueExpression() throws RecognitionException {
        JaybirdSqlParser.nextValueExpression_return retval = new JaybirdSqlParser.nextValueExpression_return();
        retval.start = input.LT(1);

        int nextValueExpression_StartIndex = input.index();

        CommonTree root_0 = null;

        Token NEXT105=null;
        Token VALUE106=null;
        Token FOR107=null;
        Token GEN_ID109=null;
        Token char_literal110=null;
        Token char_literal112=null;
        Token INTEGER113=null;
        Token char_literal114=null;
        JaybirdSqlParser.simpleIdentifier_return simpleIdentifier108 =null;

        JaybirdSqlParser.simpleIdentifier_return simpleIdentifier111 =null;


        CommonTree NEXT105_tree=null;
        CommonTree VALUE106_tree=null;
        CommonTree FOR107_tree=null;
        CommonTree GEN_ID109_tree=null;
        CommonTree char_literal110_tree=null;
        CommonTree char_literal112_tree=null;
        CommonTree INTEGER113_tree=null;
        CommonTree char_literal114_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 23) ) { return retval; }

            // JaybirdSql.g:431:9: ( NEXT VALUE FOR simpleIdentifier | GEN_ID '(' simpleIdentifier ',' INTEGER ')' )
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( (LA16_0==NEXT) ) {
                alt16=1;
            }
            else if ( (LA16_0==GEN_ID) ) {
                alt16=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 16, 0, input);

                throw nvae;

            }
            switch (alt16) {
                case 1 :
                    // JaybirdSql.g:431:14: NEXT VALUE FOR simpleIdentifier
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    NEXT105=(Token)match(input,NEXT,FOLLOW_NEXT_in_nextValueExpression2359); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    NEXT105_tree = 
                    (CommonTree)adaptor.create(NEXT105)
                    ;
                    adaptor.addChild(root_0, NEXT105_tree);
                    }

                    VALUE106=(Token)match(input,VALUE,FOLLOW_VALUE_in_nextValueExpression2361); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    VALUE106_tree = 
                    (CommonTree)adaptor.create(VALUE106)
                    ;
                    adaptor.addChild(root_0, VALUE106_tree);
                    }

                    FOR107=(Token)match(input,FOR,FOLLOW_FOR_in_nextValueExpression2363); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    FOR107_tree = 
                    (CommonTree)adaptor.create(FOR107)
                    ;
                    adaptor.addChild(root_0, FOR107_tree);
                    }

                    pushFollow(FOLLOW_simpleIdentifier_in_nextValueExpression2365);
                    simpleIdentifier108=simpleIdentifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleIdentifier108.getTree());

                    }
                    break;
                case 2 :
                    // JaybirdSql.g:432:14: GEN_ID '(' simpleIdentifier ',' INTEGER ')'
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    GEN_ID109=(Token)match(input,GEN_ID,FOLLOW_GEN_ID_in_nextValueExpression2380); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    GEN_ID109_tree = 
                    (CommonTree)adaptor.create(GEN_ID109)
                    ;
                    adaptor.addChild(root_0, GEN_ID109_tree);
                    }

                    char_literal110=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_nextValueExpression2382); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal110_tree = 
                    (CommonTree)adaptor.create(char_literal110)
                    ;
                    adaptor.addChild(root_0, char_literal110_tree);
                    }

                    pushFollow(FOLLOW_simpleIdentifier_in_nextValueExpression2384);
                    simpleIdentifier111=simpleIdentifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleIdentifier111.getTree());

                    char_literal112=(Token)match(input,COMMA,FOLLOW_COMMA_in_nextValueExpression2386); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal112_tree = 
                    (CommonTree)adaptor.create(char_literal112)
                    ;
                    adaptor.addChild(root_0, char_literal112_tree);
                    }

                    INTEGER113=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_nextValueExpression2388); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    INTEGER113_tree = 
                    (CommonTree)adaptor.create(INTEGER113)
                    ;
                    adaptor.addChild(root_0, INTEGER113_tree);
                    }

                    char_literal114=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_nextValueExpression2390); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal114_tree = 
                    (CommonTree)adaptor.create(char_literal114)
                    ;
                    adaptor.addChild(root_0, char_literal114_tree);
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
            if ( state.backtracking>0 ) { memoize(input, 23, nextValueExpression_StartIndex); }

        }
        return retval;
    }
    // $ANTLR end "nextValueExpression"


    public static class castExpression_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "castExpression"
    // JaybirdSql.g:435:1: castExpression : CAST '(' value AS dataTypeDescriptor ')' ;
    public final JaybirdSqlParser.castExpression_return castExpression() throws RecognitionException {
        JaybirdSqlParser.castExpression_return retval = new JaybirdSqlParser.castExpression_return();
        retval.start = input.LT(1);

        int castExpression_StartIndex = input.index();

        CommonTree root_0 = null;

        Token CAST115=null;
        Token char_literal116=null;
        Token AS118=null;
        Token char_literal120=null;
        JaybirdSqlParser.value_return value117 =null;

        JaybirdSqlParser.dataTypeDescriptor_return dataTypeDescriptor119 =null;


        CommonTree CAST115_tree=null;
        CommonTree char_literal116_tree=null;
        CommonTree AS118_tree=null;
        CommonTree char_literal120_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 24) ) { return retval; }

            // JaybirdSql.g:436:9: ( CAST '(' value AS dataTypeDescriptor ')' )
            // JaybirdSql.g:436:14: CAST '(' value AS dataTypeDescriptor ')'
            {
            root_0 = (CommonTree)adaptor.nil();


            CAST115=(Token)match(input,CAST,FOLLOW_CAST_in_castExpression2426); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            CAST115_tree = 
            (CommonTree)adaptor.create(CAST115)
            ;
            adaptor.addChild(root_0, CAST115_tree);
            }

            char_literal116=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_castExpression2428); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            char_literal116_tree = 
            (CommonTree)adaptor.create(char_literal116)
            ;
            adaptor.addChild(root_0, char_literal116_tree);
            }

            pushFollow(FOLLOW_value_in_castExpression2430);
            value117=value();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, value117.getTree());

            AS118=(Token)match(input,AS,FOLLOW_AS_in_castExpression2432); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            AS118_tree = 
            (CommonTree)adaptor.create(AS118)
            ;
            adaptor.addChild(root_0, AS118_tree);
            }

            pushFollow(FOLLOW_dataTypeDescriptor_in_castExpression2434);
            dataTypeDescriptor119=dataTypeDescriptor();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, dataTypeDescriptor119.getTree());

            char_literal120=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_castExpression2436); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            char_literal120_tree = 
            (CommonTree)adaptor.create(char_literal120)
            ;
            adaptor.addChild(root_0, char_literal120_tree);
            }

            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
            if ( state.backtracking>0 ) { memoize(input, 24, castExpression_StartIndex); }

        }
        return retval;
    }
    // $ANTLR end "castExpression"


    public static class dataTypeDescriptor_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "dataTypeDescriptor"
    // JaybirdSql.g:439:1: dataTypeDescriptor : ( nonArrayType | arrayType );
    public final JaybirdSqlParser.dataTypeDescriptor_return dataTypeDescriptor() throws RecognitionException {
        JaybirdSqlParser.dataTypeDescriptor_return retval = new JaybirdSqlParser.dataTypeDescriptor_return();
        retval.start = input.LT(1);

        int dataTypeDescriptor_StartIndex = input.index();

        CommonTree root_0 = null;

        JaybirdSqlParser.nonArrayType_return nonArrayType121 =null;

        JaybirdSqlParser.arrayType_return arrayType122 =null;



        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 25) ) { return retval; }

            // JaybirdSql.g:440:9: ( nonArrayType | arrayType )
            int alt17=2;
            switch ( input.LA(1) ) {
            case KW_BIGINT:
                {
                int LA17_1 = input.LA(2);

                if ( (LA17_1==RIGHT_PAREN) ) {
                    alt17=1;
                }
                else if ( (LA17_1==82) ) {
                    alt17=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 17, 1, input);

                    throw nvae;

                }
                }
                break;
            case KW_DATE:
                {
                int LA17_2 = input.LA(2);

                if ( (LA17_2==RIGHT_PAREN) ) {
                    alt17=1;
                }
                else if ( (LA17_2==82) ) {
                    alt17=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 17, 2, input);

                    throw nvae;

                }
                }
                break;
            case KW_DECIMAL:
                {
                int LA17_3 = input.LA(2);

                if ( (LA17_3==LEFT_PAREN) ) {
                    int LA17_16 = input.LA(3);

                    if ( (LA17_16==INTEGER) ) {
                        int LA17_21 = input.LA(4);

                        if ( (LA17_21==COMMA) ) {
                            int LA17_25 = input.LA(5);

                            if ( (LA17_25==INTEGER) ) {
                                int LA17_31 = input.LA(6);

                                if ( (LA17_31==RIGHT_PAREN) ) {
                                    int LA17_26 = input.LA(7);

                                    if ( (LA17_26==RIGHT_PAREN) ) {
                                        alt17=1;
                                    }
                                    else if ( (LA17_26==82) ) {
                                        alt17=2;
                                    }
                                    else {
                                        if (state.backtracking>0) {state.failed=true; return retval;}
                                        NoViableAltException nvae =
                                            new NoViableAltException("", 17, 26, input);

                                        throw nvae;

                                    }
                                }
                                else {
                                    if (state.backtracking>0) {state.failed=true; return retval;}
                                    NoViableAltException nvae =
                                        new NoViableAltException("", 17, 31, input);

                                    throw nvae;

                                }
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return retval;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 17, 25, input);

                                throw nvae;

                            }
                        }
                        else if ( (LA17_21==RIGHT_PAREN) ) {
                            int LA17_26 = input.LA(5);

                            if ( (LA17_26==RIGHT_PAREN) ) {
                                alt17=1;
                            }
                            else if ( (LA17_26==82) ) {
                                alt17=2;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return retval;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 17, 26, input);

                                throw nvae;

                            }
                        }
                        else {
                            if (state.backtracking>0) {state.failed=true; return retval;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 17, 21, input);

                            throw nvae;

                        }
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 17, 16, input);

                        throw nvae;

                    }
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 17, 3, input);

                    throw nvae;

                }
                }
                break;
            case KW_DOUBLE:
                {
                int LA17_4 = input.LA(2);

                if ( (LA17_4==KW_PRECISION) ) {
                    int LA17_17 = input.LA(3);

                    if ( (LA17_17==RIGHT_PAREN) ) {
                        alt17=1;
                    }
                    else if ( (LA17_17==82) ) {
                        alt17=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 17, 17, input);

                        throw nvae;

                    }
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 17, 4, input);

                    throw nvae;

                }
                }
                break;
            case KW_FLOAT:
                {
                int LA17_5 = input.LA(2);

                if ( (LA17_5==RIGHT_PAREN) ) {
                    alt17=1;
                }
                else if ( (LA17_5==82) ) {
                    alt17=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 17, 5, input);

                    throw nvae;

                }
                }
                break;
            case KW_INTEGER:
                {
                int LA17_6 = input.LA(2);

                if ( (LA17_6==RIGHT_PAREN) ) {
                    alt17=1;
                }
                else if ( (LA17_6==82) ) {
                    alt17=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 17, 6, input);

                    throw nvae;

                }
                }
                break;
            case KW_INT:
                {
                int LA17_7 = input.LA(2);

                if ( (LA17_7==RIGHT_PAREN) ) {
                    alt17=1;
                }
                else if ( (LA17_7==82) ) {
                    alt17=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 17, 7, input);

                    throw nvae;

                }
                }
                break;
            case KW_NUMERIC:
                {
                int LA17_8 = input.LA(2);

                if ( (LA17_8==LEFT_PAREN) ) {
                    int LA17_18 = input.LA(3);

                    if ( (LA17_18==INTEGER) ) {
                        int LA17_22 = input.LA(4);

                        if ( (LA17_22==COMMA) ) {
                            int LA17_27 = input.LA(5);

                            if ( (LA17_27==INTEGER) ) {
                                int LA17_32 = input.LA(6);

                                if ( (LA17_32==RIGHT_PAREN) ) {
                                    int LA17_28 = input.LA(7);

                                    if ( (LA17_28==RIGHT_PAREN) ) {
                                        alt17=1;
                                    }
                                    else if ( (LA17_28==82) ) {
                                        alt17=2;
                                    }
                                    else {
                                        if (state.backtracking>0) {state.failed=true; return retval;}
                                        NoViableAltException nvae =
                                            new NoViableAltException("", 17, 28, input);

                                        throw nvae;

                                    }
                                }
                                else {
                                    if (state.backtracking>0) {state.failed=true; return retval;}
                                    NoViableAltException nvae =
                                        new NoViableAltException("", 17, 32, input);

                                    throw nvae;

                                }
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return retval;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 17, 27, input);

                                throw nvae;

                            }
                        }
                        else if ( (LA17_22==RIGHT_PAREN) ) {
                            int LA17_28 = input.LA(5);

                            if ( (LA17_28==RIGHT_PAREN) ) {
                                alt17=1;
                            }
                            else if ( (LA17_28==82) ) {
                                alt17=2;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return retval;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 17, 28, input);

                                throw nvae;

                            }
                        }
                        else {
                            if (state.backtracking>0) {state.failed=true; return retval;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 17, 22, input);

                            throw nvae;

                        }
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 17, 18, input);

                        throw nvae;

                    }
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 17, 8, input);

                    throw nvae;

                }
                }
                break;
            case KW_SMALLINT:
                {
                int LA17_9 = input.LA(2);

                if ( (LA17_9==RIGHT_PAREN) ) {
                    alt17=1;
                }
                else if ( (LA17_9==82) ) {
                    alt17=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 17, 9, input);

                    throw nvae;

                }
                }
                break;
            case KW_TIME:
                {
                int LA17_10 = input.LA(2);

                if ( (LA17_10==RIGHT_PAREN) ) {
                    alt17=1;
                }
                else if ( (LA17_10==82) ) {
                    alt17=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 17, 10, input);

                    throw nvae;

                }
                }
                break;
            case KW_TIMESTAMP:
                {
                int LA17_11 = input.LA(2);

                if ( (LA17_11==RIGHT_PAREN) ) {
                    alt17=1;
                }
                else if ( (LA17_11==82) ) {
                    alt17=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 17, 11, input);

                    throw nvae;

                }
                }
                break;
            case KW_CHAR:
                {
                switch ( input.LA(2) ) {
                case LEFT_PAREN:
                    {
                    int LA17_19 = input.LA(3);

                    if ( (LA17_19==INTEGER) ) {
                        int LA17_23 = input.LA(4);

                        if ( (LA17_23==RIGHT_PAREN) ) {
                            int LA17_29 = input.LA(5);

                            if ( (LA17_29==CHARACTER||LA17_29==RIGHT_PAREN) ) {
                                alt17=1;
                            }
                            else if ( (LA17_29==82) ) {
                                alt17=2;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return retval;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 17, 29, input);

                                throw nvae;

                            }
                        }
                        else {
                            if (state.backtracking>0) {state.failed=true; return retval;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 17, 23, input);

                            throw nvae;

                        }
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 17, 19, input);

                        throw nvae;

                    }
                    }
                    break;
                case CHARACTER:
                case RIGHT_PAREN:
                    {
                    alt17=1;
                    }
                    break;
                case 82:
                    {
                    alt17=2;
                    }
                    break;
                default:
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 17, 12, input);

                    throw nvae;

                }

                }
                break;
            case KW_VARCHAR:
                {
                int LA17_13 = input.LA(2);

                if ( (LA17_13==LEFT_PAREN) ) {
                    int LA17_20 = input.LA(3);

                    if ( (LA17_20==INTEGER) ) {
                        int LA17_24 = input.LA(4);

                        if ( (LA17_24==RIGHT_PAREN) ) {
                            int LA17_30 = input.LA(5);

                            if ( (LA17_30==CHARACTER||LA17_30==RIGHT_PAREN) ) {
                                alt17=1;
                            }
                            else if ( (LA17_30==82) ) {
                                alt17=2;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return retval;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 17, 30, input);

                                throw nvae;

                            }
                        }
                        else {
                            if (state.backtracking>0) {state.failed=true; return retval;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 17, 24, input);

                            throw nvae;

                        }
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 17, 20, input);

                        throw nvae;

                    }
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 17, 13, input);

                    throw nvae;

                }
                }
                break;
            case KW_BLOB:
                {
                alt17=1;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 17, 0, input);

                throw nvae;

            }

            switch (alt17) {
                case 1 :
                    // JaybirdSql.g:440:14: nonArrayType
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_nonArrayType_in_dataTypeDescriptor2472);
                    nonArrayType121=nonArrayType();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, nonArrayType121.getTree());

                    }
                    break;
                case 2 :
                    // JaybirdSql.g:441:14: arrayType
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_arrayType_in_dataTypeDescriptor2487);
                    arrayType122=arrayType();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, arrayType122.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
            if ( state.backtracking>0 ) { memoize(input, 25, dataTypeDescriptor_StartIndex); }

        }
        return retval;
    }
    // $ANTLR end "dataTypeDescriptor"


    public static class nonArrayType_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "nonArrayType"
    // JaybirdSql.g:444:1: nonArrayType : ( simpleType | blobType );
    public final JaybirdSqlParser.nonArrayType_return nonArrayType() throws RecognitionException {
        JaybirdSqlParser.nonArrayType_return retval = new JaybirdSqlParser.nonArrayType_return();
        retval.start = input.LT(1);

        int nonArrayType_StartIndex = input.index();

        CommonTree root_0 = null;

        JaybirdSqlParser.simpleType_return simpleType123 =null;

        JaybirdSqlParser.blobType_return blobType124 =null;



        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 26) ) { return retval; }

            // JaybirdSql.g:445:9: ( simpleType | blobType )
            int alt18=2;
            int LA18_0 = input.LA(1);

            if ( (LA18_0==KW_BIGINT||(LA18_0 >= KW_CHAR && LA18_0 <= KW_NUMERIC)||(LA18_0 >= KW_SMALLINT && LA18_0 <= KW_VARCHAR)) ) {
                alt18=1;
            }
            else if ( (LA18_0==KW_BLOB) ) {
                alt18=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 18, 0, input);

                throw nvae;

            }
            switch (alt18) {
                case 1 :
                    // JaybirdSql.g:445:14: simpleType
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_simpleType_in_nonArrayType2523);
                    simpleType123=simpleType();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleType123.getTree());

                    }
                    break;
                case 2 :
                    // JaybirdSql.g:446:14: blobType
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_blobType_in_nonArrayType2538);
                    blobType124=blobType();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, blobType124.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
            if ( state.backtracking>0 ) { memoize(input, 26, nonArrayType_StartIndex); }

        }
        return retval;
    }
    // $ANTLR end "nonArrayType"


    public static class simpleType_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "simpleType"
    // JaybirdSql.g:449:1: simpleType : ( nonCharType | charType );
    public final JaybirdSqlParser.simpleType_return simpleType() throws RecognitionException {
        JaybirdSqlParser.simpleType_return retval = new JaybirdSqlParser.simpleType_return();
        retval.start = input.LT(1);

        int simpleType_StartIndex = input.index();

        CommonTree root_0 = null;

        JaybirdSqlParser.nonCharType_return nonCharType125 =null;

        JaybirdSqlParser.charType_return charType126 =null;



        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 27) ) { return retval; }

            // JaybirdSql.g:450:9: ( nonCharType | charType )
            int alt19=2;
            int LA19_0 = input.LA(1);

            if ( (LA19_0==KW_BIGINT||(LA19_0 >= KW_DATE && LA19_0 <= KW_NUMERIC)||(LA19_0 >= KW_SMALLINT && LA19_0 <= KW_TIMESTAMP)) ) {
                alt19=1;
            }
            else if ( (LA19_0==KW_CHAR||LA19_0==KW_VARCHAR) ) {
                alt19=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 19, 0, input);

                throw nvae;

            }
            switch (alt19) {
                case 1 :
                    // JaybirdSql.g:450:14: nonCharType
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_nonCharType_in_simpleType2566);
                    nonCharType125=nonCharType();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, nonCharType125.getTree());

                    }
                    break;
                case 2 :
                    // JaybirdSql.g:451:14: charType
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_charType_in_simpleType2581);
                    charType126=charType();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, charType126.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
            if ( state.backtracking>0 ) { memoize(input, 27, simpleType_StartIndex); }

        }
        return retval;
    }
    // $ANTLR end "simpleType"


    public static class charType_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "charType"
    // JaybirdSql.g:454:1: charType : ( nonCharSetCharType | charSetCharType );
    public final JaybirdSqlParser.charType_return charType() throws RecognitionException {
        JaybirdSqlParser.charType_return retval = new JaybirdSqlParser.charType_return();
        retval.start = input.LT(1);

        int charType_StartIndex = input.index();

        CommonTree root_0 = null;

        JaybirdSqlParser.nonCharSetCharType_return nonCharSetCharType127 =null;

        JaybirdSqlParser.charSetCharType_return charSetCharType128 =null;



        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 28) ) { return retval; }

            // JaybirdSql.g:455:9: ( nonCharSetCharType | charSetCharType )
            int alt20=2;
            int LA20_0 = input.LA(1);

            if ( (LA20_0==KW_CHAR) ) {
                switch ( input.LA(2) ) {
                case LEFT_PAREN:
                    {
                    int LA20_3 = input.LA(3);

                    if ( (LA20_3==INTEGER) ) {
                        int LA20_7 = input.LA(4);

                        if ( (LA20_7==RIGHT_PAREN) ) {
                            int LA20_9 = input.LA(5);

                            if ( (LA20_9==EOF||LA20_9==RIGHT_PAREN) ) {
                                alt20=1;
                            }
                            else if ( (LA20_9==CHARACTER) ) {
                                alt20=2;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return retval;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 20, 9, input);

                                throw nvae;

                            }
                        }
                        else {
                            if (state.backtracking>0) {state.failed=true; return retval;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 20, 7, input);

                            throw nvae;

                        }
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 20, 3, input);

                        throw nvae;

                    }
                    }
                    break;
                case EOF:
                case RIGHT_PAREN:
                    {
                    alt20=1;
                    }
                    break;
                case CHARACTER:
                    {
                    alt20=2;
                    }
                    break;
                default:
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 20, 1, input);

                    throw nvae;

                }

            }
            else if ( (LA20_0==KW_VARCHAR) ) {
                int LA20_2 = input.LA(2);

                if ( (LA20_2==LEFT_PAREN) ) {
                    int LA20_6 = input.LA(3);

                    if ( (LA20_6==INTEGER) ) {
                        int LA20_8 = input.LA(4);

                        if ( (LA20_8==RIGHT_PAREN) ) {
                            int LA20_10 = input.LA(5);

                            if ( (LA20_10==EOF||LA20_10==RIGHT_PAREN) ) {
                                alt20=1;
                            }
                            else if ( (LA20_10==CHARACTER) ) {
                                alt20=2;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return retval;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 20, 10, input);

                                throw nvae;

                            }
                        }
                        else {
                            if (state.backtracking>0) {state.failed=true; return retval;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 20, 8, input);

                            throw nvae;

                        }
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 20, 6, input);

                        throw nvae;

                    }
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 20, 2, input);

                    throw nvae;

                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 20, 0, input);

                throw nvae;

            }
            switch (alt20) {
                case 1 :
                    // JaybirdSql.g:455:14: nonCharSetCharType
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_nonCharSetCharType_in_charType2617);
                    nonCharSetCharType127=nonCharSetCharType();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, nonCharSetCharType127.getTree());

                    }
                    break;
                case 2 :
                    // JaybirdSql.g:456:14: charSetCharType
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_charSetCharType_in_charType2632);
                    charSetCharType128=charSetCharType();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, charSetCharType128.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
            if ( state.backtracking>0 ) { memoize(input, 28, charType_StartIndex); }

        }
        return retval;
    }
    // $ANTLR end "charType"


    public static class nonCharSetCharType_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "nonCharSetCharType"
    // JaybirdSql.g:459:1: nonCharSetCharType : ( KW_CHAR ( '(' INTEGER ')' )? | KW_VARCHAR '(' INTEGER ')' );
    public final JaybirdSqlParser.nonCharSetCharType_return nonCharSetCharType() throws RecognitionException {
        JaybirdSqlParser.nonCharSetCharType_return retval = new JaybirdSqlParser.nonCharSetCharType_return();
        retval.start = input.LT(1);

        int nonCharSetCharType_StartIndex = input.index();

        CommonTree root_0 = null;

        Token KW_CHAR129=null;
        Token char_literal130=null;
        Token INTEGER131=null;
        Token char_literal132=null;
        Token KW_VARCHAR133=null;
        Token char_literal134=null;
        Token INTEGER135=null;
        Token char_literal136=null;

        CommonTree KW_CHAR129_tree=null;
        CommonTree char_literal130_tree=null;
        CommonTree INTEGER131_tree=null;
        CommonTree char_literal132_tree=null;
        CommonTree KW_VARCHAR133_tree=null;
        CommonTree char_literal134_tree=null;
        CommonTree INTEGER135_tree=null;
        CommonTree char_literal136_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 29) ) { return retval; }

            // JaybirdSql.g:460:9: ( KW_CHAR ( '(' INTEGER ')' )? | KW_VARCHAR '(' INTEGER ')' )
            int alt22=2;
            int LA22_0 = input.LA(1);

            if ( (LA22_0==KW_CHAR) ) {
                alt22=1;
            }
            else if ( (LA22_0==KW_VARCHAR) ) {
                alt22=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 22, 0, input);

                throw nvae;

            }
            switch (alt22) {
                case 1 :
                    // JaybirdSql.g:460:14: KW_CHAR ( '(' INTEGER ')' )?
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    KW_CHAR129=(Token)match(input,KW_CHAR,FOLLOW_KW_CHAR_in_nonCharSetCharType2660); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    KW_CHAR129_tree = 
                    (CommonTree)adaptor.create(KW_CHAR129)
                    ;
                    adaptor.addChild(root_0, KW_CHAR129_tree);
                    }

                    // JaybirdSql.g:460:22: ( '(' INTEGER ')' )?
                    int alt21=2;
                    int LA21_0 = input.LA(1);

                    if ( (LA21_0==LEFT_PAREN) ) {
                        alt21=1;
                    }
                    switch (alt21) {
                        case 1 :
                            // JaybirdSql.g:460:23: '(' INTEGER ')'
                            {
                            char_literal130=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_nonCharSetCharType2663); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            char_literal130_tree = 
                            (CommonTree)adaptor.create(char_literal130)
                            ;
                            adaptor.addChild(root_0, char_literal130_tree);
                            }

                            INTEGER131=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_nonCharSetCharType2665); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            INTEGER131_tree = 
                            (CommonTree)adaptor.create(INTEGER131)
                            ;
                            adaptor.addChild(root_0, INTEGER131_tree);
                            }

                            char_literal132=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_nonCharSetCharType2667); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            char_literal132_tree = 
                            (CommonTree)adaptor.create(char_literal132)
                            ;
                            adaptor.addChild(root_0, char_literal132_tree);
                            }

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // JaybirdSql.g:461:14: KW_VARCHAR '(' INTEGER ')'
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    KW_VARCHAR133=(Token)match(input,KW_VARCHAR,FOLLOW_KW_VARCHAR_in_nonCharSetCharType2684); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    KW_VARCHAR133_tree = 
                    (CommonTree)adaptor.create(KW_VARCHAR133)
                    ;
                    adaptor.addChild(root_0, KW_VARCHAR133_tree);
                    }

                    char_literal134=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_nonCharSetCharType2686); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal134_tree = 
                    (CommonTree)adaptor.create(char_literal134)
                    ;
                    adaptor.addChild(root_0, char_literal134_tree);
                    }

                    INTEGER135=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_nonCharSetCharType2688); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    INTEGER135_tree = 
                    (CommonTree)adaptor.create(INTEGER135)
                    ;
                    adaptor.addChild(root_0, INTEGER135_tree);
                    }

                    char_literal136=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_nonCharSetCharType2690); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal136_tree = 
                    (CommonTree)adaptor.create(char_literal136)
                    ;
                    adaptor.addChild(root_0, char_literal136_tree);
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
            if ( state.backtracking>0 ) { memoize(input, 29, nonCharSetCharType_StartIndex); }

        }
        return retval;
    }
    // $ANTLR end "nonCharSetCharType"


    public static class charSetCharType_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "charSetCharType"
    // JaybirdSql.g:464:1: charSetCharType : nonCharSetCharType charSetClause ;
    public final JaybirdSqlParser.charSetCharType_return charSetCharType() throws RecognitionException {
        JaybirdSqlParser.charSetCharType_return retval = new JaybirdSqlParser.charSetCharType_return();
        retval.start = input.LT(1);

        int charSetCharType_StartIndex = input.index();

        CommonTree root_0 = null;

        JaybirdSqlParser.nonCharSetCharType_return nonCharSetCharType137 =null;

        JaybirdSqlParser.charSetClause_return charSetClause138 =null;



        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 30) ) { return retval; }

            // JaybirdSql.g:465:9: ( nonCharSetCharType charSetClause )
            // JaybirdSql.g:465:14: nonCharSetCharType charSetClause
            {
            root_0 = (CommonTree)adaptor.nil();


            pushFollow(FOLLOW_nonCharSetCharType_in_charSetCharType2718);
            nonCharSetCharType137=nonCharSetCharType();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, nonCharSetCharType137.getTree());

            pushFollow(FOLLOW_charSetClause_in_charSetCharType2720);
            charSetClause138=charSetClause();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, charSetClause138.getTree());

            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
            if ( state.backtracking>0 ) { memoize(input, 30, charSetCharType_StartIndex); }

        }
        return retval;
    }
    // $ANTLR end "charSetCharType"


    public static class nonCharType_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "nonCharType"
    // JaybirdSql.g:468:1: nonCharType : ( KW_BIGINT | KW_DATE | KW_DECIMAL '(' INTEGER ( ',' INTEGER )? ')' | KW_DOUBLE KW_PRECISION | KW_FLOAT | KW_INTEGER | KW_INT | KW_NUMERIC '(' INTEGER ( ',' INTEGER )? ')' | KW_SMALLINT | KW_TIME | KW_TIMESTAMP );
    public final JaybirdSqlParser.nonCharType_return nonCharType() throws RecognitionException {
        JaybirdSqlParser.nonCharType_return retval = new JaybirdSqlParser.nonCharType_return();
        retval.start = input.LT(1);

        int nonCharType_StartIndex = input.index();

        CommonTree root_0 = null;

        Token KW_BIGINT139=null;
        Token KW_DATE140=null;
        Token KW_DECIMAL141=null;
        Token char_literal142=null;
        Token INTEGER143=null;
        Token char_literal144=null;
        Token INTEGER145=null;
        Token char_literal146=null;
        Token KW_DOUBLE147=null;
        Token KW_PRECISION148=null;
        Token KW_FLOAT149=null;
        Token KW_INTEGER150=null;
        Token KW_INT151=null;
        Token KW_NUMERIC152=null;
        Token char_literal153=null;
        Token INTEGER154=null;
        Token char_literal155=null;
        Token INTEGER156=null;
        Token char_literal157=null;
        Token KW_SMALLINT158=null;
        Token KW_TIME159=null;
        Token KW_TIMESTAMP160=null;

        CommonTree KW_BIGINT139_tree=null;
        CommonTree KW_DATE140_tree=null;
        CommonTree KW_DECIMAL141_tree=null;
        CommonTree char_literal142_tree=null;
        CommonTree INTEGER143_tree=null;
        CommonTree char_literal144_tree=null;
        CommonTree INTEGER145_tree=null;
        CommonTree char_literal146_tree=null;
        CommonTree KW_DOUBLE147_tree=null;
        CommonTree KW_PRECISION148_tree=null;
        CommonTree KW_FLOAT149_tree=null;
        CommonTree KW_INTEGER150_tree=null;
        CommonTree KW_INT151_tree=null;
        CommonTree KW_NUMERIC152_tree=null;
        CommonTree char_literal153_tree=null;
        CommonTree INTEGER154_tree=null;
        CommonTree char_literal155_tree=null;
        CommonTree INTEGER156_tree=null;
        CommonTree char_literal157_tree=null;
        CommonTree KW_SMALLINT158_tree=null;
        CommonTree KW_TIME159_tree=null;
        CommonTree KW_TIMESTAMP160_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 31) ) { return retval; }

            // JaybirdSql.g:469:9: ( KW_BIGINT | KW_DATE | KW_DECIMAL '(' INTEGER ( ',' INTEGER )? ')' | KW_DOUBLE KW_PRECISION | KW_FLOAT | KW_INTEGER | KW_INT | KW_NUMERIC '(' INTEGER ( ',' INTEGER )? ')' | KW_SMALLINT | KW_TIME | KW_TIMESTAMP )
            int alt25=11;
            switch ( input.LA(1) ) {
            case KW_BIGINT:
                {
                alt25=1;
                }
                break;
            case KW_DATE:
                {
                alt25=2;
                }
                break;
            case KW_DECIMAL:
                {
                alt25=3;
                }
                break;
            case KW_DOUBLE:
                {
                alt25=4;
                }
                break;
            case KW_FLOAT:
                {
                alt25=5;
                }
                break;
            case KW_INTEGER:
                {
                alt25=6;
                }
                break;
            case KW_INT:
                {
                alt25=7;
                }
                break;
            case KW_NUMERIC:
                {
                alt25=8;
                }
                break;
            case KW_SMALLINT:
                {
                alt25=9;
                }
                break;
            case KW_TIME:
                {
                alt25=10;
                }
                break;
            case KW_TIMESTAMP:
                {
                alt25=11;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 25, 0, input);

                throw nvae;

            }

            switch (alt25) {
                case 1 :
                    // JaybirdSql.g:469:14: KW_BIGINT
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    KW_BIGINT139=(Token)match(input,KW_BIGINT,FOLLOW_KW_BIGINT_in_nonCharType2748); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    KW_BIGINT139_tree = 
                    (CommonTree)adaptor.create(KW_BIGINT139)
                    ;
                    adaptor.addChild(root_0, KW_BIGINT139_tree);
                    }

                    }
                    break;
                case 2 :
                    // JaybirdSql.g:470:14: KW_DATE
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    KW_DATE140=(Token)match(input,KW_DATE,FOLLOW_KW_DATE_in_nonCharType2763); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    KW_DATE140_tree = 
                    (CommonTree)adaptor.create(KW_DATE140)
                    ;
                    adaptor.addChild(root_0, KW_DATE140_tree);
                    }

                    }
                    break;
                case 3 :
                    // JaybirdSql.g:471:14: KW_DECIMAL '(' INTEGER ( ',' INTEGER )? ')'
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    KW_DECIMAL141=(Token)match(input,KW_DECIMAL,FOLLOW_KW_DECIMAL_in_nonCharType2778); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    KW_DECIMAL141_tree = 
                    (CommonTree)adaptor.create(KW_DECIMAL141)
                    ;
                    adaptor.addChild(root_0, KW_DECIMAL141_tree);
                    }

                    char_literal142=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_nonCharType2780); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal142_tree = 
                    (CommonTree)adaptor.create(char_literal142)
                    ;
                    adaptor.addChild(root_0, char_literal142_tree);
                    }

                    INTEGER143=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_nonCharType2782); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    INTEGER143_tree = 
                    (CommonTree)adaptor.create(INTEGER143)
                    ;
                    adaptor.addChild(root_0, INTEGER143_tree);
                    }

                    // JaybirdSql.g:471:37: ( ',' INTEGER )?
                    int alt23=2;
                    int LA23_0 = input.LA(1);

                    if ( (LA23_0==COMMA) ) {
                        alt23=1;
                    }
                    switch (alt23) {
                        case 1 :
                            // JaybirdSql.g:471:38: ',' INTEGER
                            {
                            char_literal144=(Token)match(input,COMMA,FOLLOW_COMMA_in_nonCharType2785); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            char_literal144_tree = 
                            (CommonTree)adaptor.create(char_literal144)
                            ;
                            adaptor.addChild(root_0, char_literal144_tree);
                            }

                            INTEGER145=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_nonCharType2787); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            INTEGER145_tree = 
                            (CommonTree)adaptor.create(INTEGER145)
                            ;
                            adaptor.addChild(root_0, INTEGER145_tree);
                            }

                            }
                            break;

                    }


                    char_literal146=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_nonCharType2791); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal146_tree = 
                    (CommonTree)adaptor.create(char_literal146)
                    ;
                    adaptor.addChild(root_0, char_literal146_tree);
                    }

                    }
                    break;
                case 4 :
                    // JaybirdSql.g:472:14: KW_DOUBLE KW_PRECISION
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    KW_DOUBLE147=(Token)match(input,KW_DOUBLE,FOLLOW_KW_DOUBLE_in_nonCharType2806); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    KW_DOUBLE147_tree = 
                    (CommonTree)adaptor.create(KW_DOUBLE147)
                    ;
                    adaptor.addChild(root_0, KW_DOUBLE147_tree);
                    }

                    KW_PRECISION148=(Token)match(input,KW_PRECISION,FOLLOW_KW_PRECISION_in_nonCharType2808); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    KW_PRECISION148_tree = 
                    (CommonTree)adaptor.create(KW_PRECISION148)
                    ;
                    adaptor.addChild(root_0, KW_PRECISION148_tree);
                    }

                    }
                    break;
                case 5 :
                    // JaybirdSql.g:473:14: KW_FLOAT
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    KW_FLOAT149=(Token)match(input,KW_FLOAT,FOLLOW_KW_FLOAT_in_nonCharType2823); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    KW_FLOAT149_tree = 
                    (CommonTree)adaptor.create(KW_FLOAT149)
                    ;
                    adaptor.addChild(root_0, KW_FLOAT149_tree);
                    }

                    }
                    break;
                case 6 :
                    // JaybirdSql.g:474:14: KW_INTEGER
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    KW_INTEGER150=(Token)match(input,KW_INTEGER,FOLLOW_KW_INTEGER_in_nonCharType2838); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    KW_INTEGER150_tree = 
                    (CommonTree)adaptor.create(KW_INTEGER150)
                    ;
                    adaptor.addChild(root_0, KW_INTEGER150_tree);
                    }

                    }
                    break;
                case 7 :
                    // JaybirdSql.g:475:14: KW_INT
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    KW_INT151=(Token)match(input,KW_INT,FOLLOW_KW_INT_in_nonCharType2853); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    KW_INT151_tree = 
                    (CommonTree)adaptor.create(KW_INT151)
                    ;
                    adaptor.addChild(root_0, KW_INT151_tree);
                    }

                    }
                    break;
                case 8 :
                    // JaybirdSql.g:476:14: KW_NUMERIC '(' INTEGER ( ',' INTEGER )? ')'
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    KW_NUMERIC152=(Token)match(input,KW_NUMERIC,FOLLOW_KW_NUMERIC_in_nonCharType2868); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    KW_NUMERIC152_tree = 
                    (CommonTree)adaptor.create(KW_NUMERIC152)
                    ;
                    adaptor.addChild(root_0, KW_NUMERIC152_tree);
                    }

                    char_literal153=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_nonCharType2870); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal153_tree = 
                    (CommonTree)adaptor.create(char_literal153)
                    ;
                    adaptor.addChild(root_0, char_literal153_tree);
                    }

                    INTEGER154=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_nonCharType2872); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    INTEGER154_tree = 
                    (CommonTree)adaptor.create(INTEGER154)
                    ;
                    adaptor.addChild(root_0, INTEGER154_tree);
                    }

                    // JaybirdSql.g:476:37: ( ',' INTEGER )?
                    int alt24=2;
                    int LA24_0 = input.LA(1);

                    if ( (LA24_0==COMMA) ) {
                        alt24=1;
                    }
                    switch (alt24) {
                        case 1 :
                            // JaybirdSql.g:476:38: ',' INTEGER
                            {
                            char_literal155=(Token)match(input,COMMA,FOLLOW_COMMA_in_nonCharType2875); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            char_literal155_tree = 
                            (CommonTree)adaptor.create(char_literal155)
                            ;
                            adaptor.addChild(root_0, char_literal155_tree);
                            }

                            INTEGER156=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_nonCharType2877); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            INTEGER156_tree = 
                            (CommonTree)adaptor.create(INTEGER156)
                            ;
                            adaptor.addChild(root_0, INTEGER156_tree);
                            }

                            }
                            break;

                    }


                    char_literal157=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_nonCharType2881); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal157_tree = 
                    (CommonTree)adaptor.create(char_literal157)
                    ;
                    adaptor.addChild(root_0, char_literal157_tree);
                    }

                    }
                    break;
                case 9 :
                    // JaybirdSql.g:477:14: KW_SMALLINT
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    KW_SMALLINT158=(Token)match(input,KW_SMALLINT,FOLLOW_KW_SMALLINT_in_nonCharType2896); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    KW_SMALLINT158_tree = 
                    (CommonTree)adaptor.create(KW_SMALLINT158)
                    ;
                    adaptor.addChild(root_0, KW_SMALLINT158_tree);
                    }

                    }
                    break;
                case 10 :
                    // JaybirdSql.g:478:14: KW_TIME
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    KW_TIME159=(Token)match(input,KW_TIME,FOLLOW_KW_TIME_in_nonCharType2911); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    KW_TIME159_tree = 
                    (CommonTree)adaptor.create(KW_TIME159)
                    ;
                    adaptor.addChild(root_0, KW_TIME159_tree);
                    }

                    }
                    break;
                case 11 :
                    // JaybirdSql.g:479:14: KW_TIMESTAMP
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    KW_TIMESTAMP160=(Token)match(input,KW_TIMESTAMP,FOLLOW_KW_TIMESTAMP_in_nonCharType2926); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    KW_TIMESTAMP160_tree = 
                    (CommonTree)adaptor.create(KW_TIMESTAMP160)
                    ;
                    adaptor.addChild(root_0, KW_TIMESTAMP160_tree);
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
            if ( state.backtracking>0 ) { memoize(input, 31, nonCharType_StartIndex); }

        }
        return retval;
    }
    // $ANTLR end "nonCharType"


    public static class blobType_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "blobType"
    // JaybirdSql.g:482:1: blobType : ( KW_BLOB ( blobSubtype )? ( blobSegSize )? ( charSetClause )? | KW_BLOB '(' ( INTEGER )? ( ',' INTEGER )? ')' );
    public final JaybirdSqlParser.blobType_return blobType() throws RecognitionException {
        JaybirdSqlParser.blobType_return retval = new JaybirdSqlParser.blobType_return();
        retval.start = input.LT(1);

        int blobType_StartIndex = input.index();

        CommonTree root_0 = null;

        Token KW_BLOB161=null;
        Token KW_BLOB165=null;
        Token char_literal166=null;
        Token INTEGER167=null;
        Token char_literal168=null;
        Token INTEGER169=null;
        Token char_literal170=null;
        JaybirdSqlParser.blobSubtype_return blobSubtype162 =null;

        JaybirdSqlParser.blobSegSize_return blobSegSize163 =null;

        JaybirdSqlParser.charSetClause_return charSetClause164 =null;


        CommonTree KW_BLOB161_tree=null;
        CommonTree KW_BLOB165_tree=null;
        CommonTree char_literal166_tree=null;
        CommonTree INTEGER167_tree=null;
        CommonTree char_literal168_tree=null;
        CommonTree INTEGER169_tree=null;
        CommonTree char_literal170_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 32) ) { return retval; }

            // JaybirdSql.g:483:9: ( KW_BLOB ( blobSubtype )? ( blobSegSize )? ( charSetClause )? | KW_BLOB '(' ( INTEGER )? ( ',' INTEGER )? ')' )
            int alt31=2;
            int LA31_0 = input.LA(1);

            if ( (LA31_0==KW_BLOB) ) {
                int LA31_1 = input.LA(2);

                if ( (LA31_1==LEFT_PAREN) ) {
                    alt31=2;
                }
                else if ( (LA31_1==EOF||LA31_1==CHARACTER||(LA31_1 >= RIGHT_PAREN && LA31_1 <= SEGMENT)||LA31_1==SUB_TYPE) ) {
                    alt31=1;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 31, 1, input);

                    throw nvae;

                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 31, 0, input);

                throw nvae;

            }
            switch (alt31) {
                case 1 :
                    // JaybirdSql.g:483:14: KW_BLOB ( blobSubtype )? ( blobSegSize )? ( charSetClause )?
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    KW_BLOB161=(Token)match(input,KW_BLOB,FOLLOW_KW_BLOB_in_blobType2955); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    KW_BLOB161_tree = 
                    (CommonTree)adaptor.create(KW_BLOB161)
                    ;
                    adaptor.addChild(root_0, KW_BLOB161_tree);
                    }

                    // JaybirdSql.g:483:22: ( blobSubtype )?
                    int alt26=2;
                    int LA26_0 = input.LA(1);

                    if ( (LA26_0==SUB_TYPE) ) {
                        alt26=1;
                    }
                    switch (alt26) {
                        case 1 :
                            // JaybirdSql.g:483:22: blobSubtype
                            {
                            pushFollow(FOLLOW_blobSubtype_in_blobType2957);
                            blobSubtype162=blobSubtype();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) adaptor.addChild(root_0, blobSubtype162.getTree());

                            }
                            break;

                    }


                    // JaybirdSql.g:483:35: ( blobSegSize )?
                    int alt27=2;
                    int LA27_0 = input.LA(1);

                    if ( (LA27_0==SEGMENT) ) {
                        alt27=1;
                    }
                    switch (alt27) {
                        case 1 :
                            // JaybirdSql.g:483:35: blobSegSize
                            {
                            pushFollow(FOLLOW_blobSegSize_in_blobType2960);
                            blobSegSize163=blobSegSize();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) adaptor.addChild(root_0, blobSegSize163.getTree());

                            }
                            break;

                    }


                    // JaybirdSql.g:483:48: ( charSetClause )?
                    int alt28=2;
                    int LA28_0 = input.LA(1);

                    if ( (LA28_0==CHARACTER) ) {
                        alt28=1;
                    }
                    switch (alt28) {
                        case 1 :
                            // JaybirdSql.g:483:48: charSetClause
                            {
                            pushFollow(FOLLOW_charSetClause_in_blobType2963);
                            charSetClause164=charSetClause();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) adaptor.addChild(root_0, charSetClause164.getTree());

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // JaybirdSql.g:485:14: KW_BLOB '(' ( INTEGER )? ( ',' INTEGER )? ')'
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    KW_BLOB165=(Token)match(input,KW_BLOB,FOLLOW_KW_BLOB_in_blobType2993); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    KW_BLOB165_tree = 
                    (CommonTree)adaptor.create(KW_BLOB165)
                    ;
                    adaptor.addChild(root_0, KW_BLOB165_tree);
                    }

                    char_literal166=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_blobType2995); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal166_tree = 
                    (CommonTree)adaptor.create(char_literal166)
                    ;
                    adaptor.addChild(root_0, char_literal166_tree);
                    }

                    // JaybirdSql.g:485:26: ( INTEGER )?
                    int alt29=2;
                    int LA29_0 = input.LA(1);

                    if ( (LA29_0==INTEGER) ) {
                        alt29=1;
                    }
                    switch (alt29) {
                        case 1 :
                            // JaybirdSql.g:485:26: INTEGER
                            {
                            INTEGER167=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_blobType2997); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            INTEGER167_tree = 
                            (CommonTree)adaptor.create(INTEGER167)
                            ;
                            adaptor.addChild(root_0, INTEGER167_tree);
                            }

                            }
                            break;

                    }


                    // JaybirdSql.g:485:35: ( ',' INTEGER )?
                    int alt30=2;
                    int LA30_0 = input.LA(1);

                    if ( (LA30_0==COMMA) ) {
                        alt30=1;
                    }
                    switch (alt30) {
                        case 1 :
                            // JaybirdSql.g:485:36: ',' INTEGER
                            {
                            char_literal168=(Token)match(input,COMMA,FOLLOW_COMMA_in_blobType3001); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            char_literal168_tree = 
                            (CommonTree)adaptor.create(char_literal168)
                            ;
                            adaptor.addChild(root_0, char_literal168_tree);
                            }

                            INTEGER169=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_blobType3003); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            INTEGER169_tree = 
                            (CommonTree)adaptor.create(INTEGER169)
                            ;
                            adaptor.addChild(root_0, INTEGER169_tree);
                            }

                            }
                            break;

                    }


                    char_literal170=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_blobType3007); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal170_tree = 
                    (CommonTree)adaptor.create(char_literal170)
                    ;
                    adaptor.addChild(root_0, char_literal170_tree);
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
            if ( state.backtracking>0 ) { memoize(input, 32, blobType_StartIndex); }

        }
        return retval;
    }
    // $ANTLR end "blobType"


    public static class blobSubtype_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "blobSubtype"
    // JaybirdSql.g:488:1: blobSubtype : ( SUB_TYPE INTEGER | SUB_TYPE GENERIC_ID );
    public final JaybirdSqlParser.blobSubtype_return blobSubtype() throws RecognitionException {
        JaybirdSqlParser.blobSubtype_return retval = new JaybirdSqlParser.blobSubtype_return();
        retval.start = input.LT(1);

        int blobSubtype_StartIndex = input.index();

        CommonTree root_0 = null;

        Token SUB_TYPE171=null;
        Token INTEGER172=null;
        Token SUB_TYPE173=null;
        Token GENERIC_ID174=null;

        CommonTree SUB_TYPE171_tree=null;
        CommonTree INTEGER172_tree=null;
        CommonTree SUB_TYPE173_tree=null;
        CommonTree GENERIC_ID174_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 33) ) { return retval; }

            // JaybirdSql.g:489:9: ( SUB_TYPE INTEGER | SUB_TYPE GENERIC_ID )
            int alt32=2;
            int LA32_0 = input.LA(1);

            if ( (LA32_0==SUB_TYPE) ) {
                int LA32_1 = input.LA(2);

                if ( (LA32_1==INTEGER) ) {
                    alt32=1;
                }
                else if ( (LA32_1==GENERIC_ID) ) {
                    alt32=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 32, 1, input);

                    throw nvae;

                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 32, 0, input);

                throw nvae;

            }
            switch (alt32) {
                case 1 :
                    // JaybirdSql.g:489:14: SUB_TYPE INTEGER
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    SUB_TYPE171=(Token)match(input,SUB_TYPE,FOLLOW_SUB_TYPE_in_blobSubtype3043); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    SUB_TYPE171_tree = 
                    (CommonTree)adaptor.create(SUB_TYPE171)
                    ;
                    adaptor.addChild(root_0, SUB_TYPE171_tree);
                    }

                    INTEGER172=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_blobSubtype3045); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    INTEGER172_tree = 
                    (CommonTree)adaptor.create(INTEGER172)
                    ;
                    adaptor.addChild(root_0, INTEGER172_tree);
                    }

                    }
                    break;
                case 2 :
                    // JaybirdSql.g:490:14: SUB_TYPE GENERIC_ID
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    SUB_TYPE173=(Token)match(input,SUB_TYPE,FOLLOW_SUB_TYPE_in_blobSubtype3060); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    SUB_TYPE173_tree = 
                    (CommonTree)adaptor.create(SUB_TYPE173)
                    ;
                    adaptor.addChild(root_0, SUB_TYPE173_tree);
                    }

                    GENERIC_ID174=(Token)match(input,GENERIC_ID,FOLLOW_GENERIC_ID_in_blobSubtype3062); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    GENERIC_ID174_tree = 
                    (CommonTree)adaptor.create(GENERIC_ID174)
                    ;
                    adaptor.addChild(root_0, GENERIC_ID174_tree);
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
            if ( state.backtracking>0 ) { memoize(input, 33, blobSubtype_StartIndex); }

        }
        return retval;
    }
    // $ANTLR end "blobSubtype"


    public static class blobSegSize_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "blobSegSize"
    // JaybirdSql.g:493:1: blobSegSize : SEGMENT KW_SIZE INTEGER ;
    public final JaybirdSqlParser.blobSegSize_return blobSegSize() throws RecognitionException {
        JaybirdSqlParser.blobSegSize_return retval = new JaybirdSqlParser.blobSegSize_return();
        retval.start = input.LT(1);

        int blobSegSize_StartIndex = input.index();

        CommonTree root_0 = null;

        Token SEGMENT175=null;
        Token KW_SIZE176=null;
        Token INTEGER177=null;

        CommonTree SEGMENT175_tree=null;
        CommonTree KW_SIZE176_tree=null;
        CommonTree INTEGER177_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 34) ) { return retval; }

            // JaybirdSql.g:494:9: ( SEGMENT KW_SIZE INTEGER )
            // JaybirdSql.g:494:14: SEGMENT KW_SIZE INTEGER
            {
            root_0 = (CommonTree)adaptor.nil();


            SEGMENT175=(Token)match(input,SEGMENT,FOLLOW_SEGMENT_in_blobSegSize3098); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            SEGMENT175_tree = 
            (CommonTree)adaptor.create(SEGMENT175)
            ;
            adaptor.addChild(root_0, SEGMENT175_tree);
            }

            KW_SIZE176=(Token)match(input,KW_SIZE,FOLLOW_KW_SIZE_in_blobSegSize3100); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            KW_SIZE176_tree = 
            (CommonTree)adaptor.create(KW_SIZE176)
            ;
            adaptor.addChild(root_0, KW_SIZE176_tree);
            }

            INTEGER177=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_blobSegSize3102); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            INTEGER177_tree = 
            (CommonTree)adaptor.create(INTEGER177)
            ;
            adaptor.addChild(root_0, INTEGER177_tree);
            }

            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
            if ( state.backtracking>0 ) { memoize(input, 34, blobSegSize_StartIndex); }

        }
        return retval;
    }
    // $ANTLR end "blobSegSize"


    public static class charSetClause_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "charSetClause"
    // JaybirdSql.g:497:1: charSetClause : CHARACTER SET GENERIC_ID ;
    public final JaybirdSqlParser.charSetClause_return charSetClause() throws RecognitionException {
        JaybirdSqlParser.charSetClause_return retval = new JaybirdSqlParser.charSetClause_return();
        retval.start = input.LT(1);

        int charSetClause_StartIndex = input.index();

        CommonTree root_0 = null;

        Token CHARACTER178=null;
        Token SET179=null;
        Token GENERIC_ID180=null;

        CommonTree CHARACTER178_tree=null;
        CommonTree SET179_tree=null;
        CommonTree GENERIC_ID180_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 35) ) { return retval; }

            // JaybirdSql.g:498:9: ( CHARACTER SET GENERIC_ID )
            // JaybirdSql.g:498:14: CHARACTER SET GENERIC_ID
            {
            root_0 = (CommonTree)adaptor.nil();


            CHARACTER178=(Token)match(input,CHARACTER,FOLLOW_CHARACTER_in_charSetClause3138); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            CHARACTER178_tree = 
            (CommonTree)adaptor.create(CHARACTER178)
            ;
            adaptor.addChild(root_0, CHARACTER178_tree);
            }

            SET179=(Token)match(input,SET,FOLLOW_SET_in_charSetClause3140); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            SET179_tree = 
            (CommonTree)adaptor.create(SET179)
            ;
            adaptor.addChild(root_0, SET179_tree);
            }

            GENERIC_ID180=(Token)match(input,GENERIC_ID,FOLLOW_GENERIC_ID_in_charSetClause3142); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            GENERIC_ID180_tree = 
            (CommonTree)adaptor.create(GENERIC_ID180)
            ;
            adaptor.addChild(root_0, GENERIC_ID180_tree);
            }

            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
            if ( state.backtracking>0 ) { memoize(input, 35, charSetClause_StartIndex); }

        }
        return retval;
    }
    // $ANTLR end "charSetClause"


    public static class arrayType_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "arrayType"
    // JaybirdSql.g:501:1: arrayType : ( nonCharSetCharType '[' arraySpec ']' ( charSetClause )? | nonCharType '[' arraySpec ']' );
    public final JaybirdSqlParser.arrayType_return arrayType() throws RecognitionException {
        JaybirdSqlParser.arrayType_return retval = new JaybirdSqlParser.arrayType_return();
        retval.start = input.LT(1);

        int arrayType_StartIndex = input.index();

        CommonTree root_0 = null;

        Token char_literal182=null;
        Token char_literal184=null;
        Token char_literal187=null;
        Token char_literal189=null;
        JaybirdSqlParser.nonCharSetCharType_return nonCharSetCharType181 =null;

        JaybirdSqlParser.arraySpec_return arraySpec183 =null;

        JaybirdSqlParser.charSetClause_return charSetClause185 =null;

        JaybirdSqlParser.nonCharType_return nonCharType186 =null;

        JaybirdSqlParser.arraySpec_return arraySpec188 =null;


        CommonTree char_literal182_tree=null;
        CommonTree char_literal184_tree=null;
        CommonTree char_literal187_tree=null;
        CommonTree char_literal189_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 36) ) { return retval; }

            // JaybirdSql.g:502:9: ( nonCharSetCharType '[' arraySpec ']' ( charSetClause )? | nonCharType '[' arraySpec ']' )
            int alt34=2;
            int LA34_0 = input.LA(1);

            if ( (LA34_0==KW_CHAR||LA34_0==KW_VARCHAR) ) {
                alt34=1;
            }
            else if ( (LA34_0==KW_BIGINT||(LA34_0 >= KW_DATE && LA34_0 <= KW_NUMERIC)||(LA34_0 >= KW_SMALLINT && LA34_0 <= KW_TIMESTAMP)) ) {
                alt34=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 34, 0, input);

                throw nvae;

            }
            switch (alt34) {
                case 1 :
                    // JaybirdSql.g:502:14: nonCharSetCharType '[' arraySpec ']' ( charSetClause )?
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_nonCharSetCharType_in_arrayType3170);
                    nonCharSetCharType181=nonCharSetCharType();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, nonCharSetCharType181.getTree());

                    char_literal182=(Token)match(input,82,FOLLOW_82_in_arrayType3172); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal182_tree = 
                    (CommonTree)adaptor.create(char_literal182)
                    ;
                    adaptor.addChild(root_0, char_literal182_tree);
                    }

                    pushFollow(FOLLOW_arraySpec_in_arrayType3174);
                    arraySpec183=arraySpec();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, arraySpec183.getTree());

                    char_literal184=(Token)match(input,83,FOLLOW_83_in_arrayType3176); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal184_tree = 
                    (CommonTree)adaptor.create(char_literal184)
                    ;
                    adaptor.addChild(root_0, char_literal184_tree);
                    }

                    // JaybirdSql.g:502:51: ( charSetClause )?
                    int alt33=2;
                    int LA33_0 = input.LA(1);

                    if ( (LA33_0==CHARACTER) ) {
                        alt33=1;
                    }
                    switch (alt33) {
                        case 1 :
                            // JaybirdSql.g:502:51: charSetClause
                            {
                            pushFollow(FOLLOW_charSetClause_in_arrayType3178);
                            charSetClause185=charSetClause();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) adaptor.addChild(root_0, charSetClause185.getTree());

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // JaybirdSql.g:503:14: nonCharType '[' arraySpec ']'
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_nonCharType_in_arrayType3194);
                    nonCharType186=nonCharType();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, nonCharType186.getTree());

                    char_literal187=(Token)match(input,82,FOLLOW_82_in_arrayType3196); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal187_tree = 
                    (CommonTree)adaptor.create(char_literal187)
                    ;
                    adaptor.addChild(root_0, char_literal187_tree);
                    }

                    pushFollow(FOLLOW_arraySpec_in_arrayType3198);
                    arraySpec188=arraySpec();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, arraySpec188.getTree());

                    char_literal189=(Token)match(input,83,FOLLOW_83_in_arrayType3200); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal189_tree = 
                    (CommonTree)adaptor.create(char_literal189)
                    ;
                    adaptor.addChild(root_0, char_literal189_tree);
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
            if ( state.backtracking>0 ) { memoize(input, 36, arrayType_StartIndex); }

        }
        return retval;
    }
    // $ANTLR end "arrayType"


    public static class arraySpec_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "arraySpec"
    // JaybirdSql.g:506:1: arraySpec : arrayRange ( ',' arrayRange )? ;
    public final JaybirdSqlParser.arraySpec_return arraySpec() throws RecognitionException {
        JaybirdSqlParser.arraySpec_return retval = new JaybirdSqlParser.arraySpec_return();
        retval.start = input.LT(1);

        int arraySpec_StartIndex = input.index();

        CommonTree root_0 = null;

        Token char_literal191=null;
        JaybirdSqlParser.arrayRange_return arrayRange190 =null;

        JaybirdSqlParser.arrayRange_return arrayRange192 =null;


        CommonTree char_literal191_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 37) ) { return retval; }

            // JaybirdSql.g:507:9: ( arrayRange ( ',' arrayRange )? )
            // JaybirdSql.g:507:14: arrayRange ( ',' arrayRange )?
            {
            root_0 = (CommonTree)adaptor.nil();


            pushFollow(FOLLOW_arrayRange_in_arraySpec3236);
            arrayRange190=arrayRange();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, arrayRange190.getTree());

            // JaybirdSql.g:507:25: ( ',' arrayRange )?
            int alt35=2;
            int LA35_0 = input.LA(1);

            if ( (LA35_0==COMMA) ) {
                alt35=1;
            }
            switch (alt35) {
                case 1 :
                    // JaybirdSql.g:507:26: ',' arrayRange
                    {
                    char_literal191=(Token)match(input,COMMA,FOLLOW_COMMA_in_arraySpec3239); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal191_tree = 
                    (CommonTree)adaptor.create(char_literal191)
                    ;
                    adaptor.addChild(root_0, char_literal191_tree);
                    }

                    pushFollow(FOLLOW_arrayRange_in_arraySpec3241);
                    arrayRange192=arrayRange();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, arrayRange192.getTree());

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
            if ( state.backtracking>0 ) { memoize(input, 37, arraySpec_StartIndex); }

        }
        return retval;
    }
    // $ANTLR end "arraySpec"


    public static class arrayRange_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "arrayRange"
    // JaybirdSql.g:510:1: arrayRange : INTEGER ( ':' INTEGER ) ;
    public final JaybirdSqlParser.arrayRange_return arrayRange() throws RecognitionException {
        JaybirdSqlParser.arrayRange_return retval = new JaybirdSqlParser.arrayRange_return();
        retval.start = input.LT(1);

        int arrayRange_StartIndex = input.index();

        CommonTree root_0 = null;

        Token INTEGER193=null;
        Token char_literal194=null;
        Token INTEGER195=null;

        CommonTree INTEGER193_tree=null;
        CommonTree char_literal194_tree=null;
        CommonTree INTEGER195_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 38) ) { return retval; }

            // JaybirdSql.g:511:9: ( INTEGER ( ':' INTEGER ) )
            // JaybirdSql.g:511:14: INTEGER ( ':' INTEGER )
            {
            root_0 = (CommonTree)adaptor.nil();


            INTEGER193=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_arrayRange3279); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            INTEGER193_tree = 
            (CommonTree)adaptor.create(INTEGER193)
            ;
            adaptor.addChild(root_0, INTEGER193_tree);
            }

            // JaybirdSql.g:511:22: ( ':' INTEGER )
            // JaybirdSql.g:511:23: ':' INTEGER
            {
            char_literal194=(Token)match(input,79,FOLLOW_79_in_arrayRange3282); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            char_literal194_tree = 
            (CommonTree)adaptor.create(char_literal194)
            ;
            adaptor.addChild(root_0, char_literal194_tree);
            }

            INTEGER195=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_arrayRange3284); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            INTEGER195_tree = 
            (CommonTree)adaptor.create(INTEGER195)
            ;
            adaptor.addChild(root_0, INTEGER195_tree);
            }

            }


            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
            if ( state.backtracking>0 ) { memoize(input, 38, arrayRange_StartIndex); }

        }
        return retval;
    }
    // $ANTLR end "arrayRange"


    public static class arrayElement_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "arrayElement"
    // JaybirdSql.g:514:1: arrayElement : simpleIdentifier '[' valueList ']' ;
    public final JaybirdSqlParser.arrayElement_return arrayElement() throws RecognitionException {
        JaybirdSqlParser.arrayElement_return retval = new JaybirdSqlParser.arrayElement_return();
        retval.start = input.LT(1);

        int arrayElement_StartIndex = input.index();

        CommonTree root_0 = null;

        Token char_literal197=null;
        Token char_literal199=null;
        JaybirdSqlParser.simpleIdentifier_return simpleIdentifier196 =null;

        JaybirdSqlParser.valueList_return valueList198 =null;


        CommonTree char_literal197_tree=null;
        CommonTree char_literal199_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 39) ) { return retval; }

            // JaybirdSql.g:515:9: ( simpleIdentifier '[' valueList ']' )
            // JaybirdSql.g:515:14: simpleIdentifier '[' valueList ']'
            {
            root_0 = (CommonTree)adaptor.nil();


            pushFollow(FOLLOW_simpleIdentifier_in_arrayElement3321);
            simpleIdentifier196=simpleIdentifier();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleIdentifier196.getTree());

            char_literal197=(Token)match(input,82,FOLLOW_82_in_arrayElement3323); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            char_literal197_tree = 
            (CommonTree)adaptor.create(char_literal197)
            ;
            adaptor.addChild(root_0, char_literal197_tree);
            }

            pushFollow(FOLLOW_valueList_in_arrayElement3325);
            valueList198=valueList();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, valueList198.getTree());

            char_literal199=(Token)match(input,83,FOLLOW_83_in_arrayElement3327); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            char_literal199_tree = 
            (CommonTree)adaptor.create(char_literal199)
            ;
            adaptor.addChild(root_0, char_literal199_tree);
            }

            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
            if ( state.backtracking>0 ) { memoize(input, 39, arrayElement_StartIndex); }

        }
        return retval;
    }
    // $ANTLR end "arrayElement"


    public static class function_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "function"
    // JaybirdSql.g:518:1: function : ( simpleIdentifier '(' valueList ')' | simpleIdentifier '(' ')' | substringFunction | trimFunction | extractFunction | SUM '(' ( ALL | DISTINCT )? value ')' | COUNT '(' ( ALL | DISTINCT )? value ')' | AVG '(' ( ALL | DISTINCT )? value ')' | MINIMUM '(' ( ALL | DISTINCT )? value ')' | MAXIMUM '(' ( ALL | DISTINCT )? value ')' );
    public final JaybirdSqlParser.function_return function() throws RecognitionException {
        JaybirdSqlParser.function_return retval = new JaybirdSqlParser.function_return();
        retval.start = input.LT(1);

        int function_StartIndex = input.index();

        CommonTree root_0 = null;

        Token char_literal201=null;
        Token char_literal203=null;
        Token char_literal205=null;
        Token char_literal206=null;
        Token SUM210=null;
        Token char_literal211=null;
        Token set212=null;
        Token char_literal214=null;
        Token COUNT215=null;
        Token char_literal216=null;
        Token set217=null;
        Token char_literal219=null;
        Token AVG220=null;
        Token char_literal221=null;
        Token set222=null;
        Token char_literal224=null;
        Token MINIMUM225=null;
        Token char_literal226=null;
        Token set227=null;
        Token char_literal229=null;
        Token MAXIMUM230=null;
        Token char_literal231=null;
        Token set232=null;
        Token char_literal234=null;
        JaybirdSqlParser.simpleIdentifier_return simpleIdentifier200 =null;

        JaybirdSqlParser.valueList_return valueList202 =null;

        JaybirdSqlParser.simpleIdentifier_return simpleIdentifier204 =null;

        JaybirdSqlParser.substringFunction_return substringFunction207 =null;

        JaybirdSqlParser.trimFunction_return trimFunction208 =null;

        JaybirdSqlParser.extractFunction_return extractFunction209 =null;

        JaybirdSqlParser.value_return value213 =null;

        JaybirdSqlParser.value_return value218 =null;

        JaybirdSqlParser.value_return value223 =null;

        JaybirdSqlParser.value_return value228 =null;

        JaybirdSqlParser.value_return value233 =null;


        CommonTree char_literal201_tree=null;
        CommonTree char_literal203_tree=null;
        CommonTree char_literal205_tree=null;
        CommonTree char_literal206_tree=null;
        CommonTree SUM210_tree=null;
        CommonTree char_literal211_tree=null;
        CommonTree set212_tree=null;
        CommonTree char_literal214_tree=null;
        CommonTree COUNT215_tree=null;
        CommonTree char_literal216_tree=null;
        CommonTree set217_tree=null;
        CommonTree char_literal219_tree=null;
        CommonTree AVG220_tree=null;
        CommonTree char_literal221_tree=null;
        CommonTree set222_tree=null;
        CommonTree char_literal224_tree=null;
        CommonTree MINIMUM225_tree=null;
        CommonTree char_literal226_tree=null;
        CommonTree set227_tree=null;
        CommonTree char_literal229_tree=null;
        CommonTree MAXIMUM230_tree=null;
        CommonTree char_literal231_tree=null;
        CommonTree set232_tree=null;
        CommonTree char_literal234_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 40) ) { return retval; }

            // JaybirdSql.g:519:9: ( simpleIdentifier '(' valueList ')' | simpleIdentifier '(' ')' | substringFunction | trimFunction | extractFunction | SUM '(' ( ALL | DISTINCT )? value ')' | COUNT '(' ( ALL | DISTINCT )? value ')' | AVG '(' ( ALL | DISTINCT )? value ')' | MINIMUM '(' ( ALL | DISTINCT )? value ')' | MAXIMUM '(' ( ALL | DISTINCT )? value ')' )
            int alt41=10;
            switch ( input.LA(1) ) {
            case GENERIC_ID:
            case QUOTED_ID:
                {
                int LA41_1 = input.LA(2);

                if ( (LA41_1==LEFT_PAREN) ) {
                    int LA41_10 = input.LA(3);

                    if ( (LA41_10==RIGHT_PAREN) ) {
                        alt41=2;
                    }
                    else if ( (LA41_10==AVG||LA41_10==CAST||(LA41_10 >= COUNT && LA41_10 <= DB_KEY)||LA41_10==EXTRACT||(LA41_10 >= GENERIC_ID && LA41_10 <= GEN_ID)||LA41_10==INTEGER||LA41_10==LEFT_PAREN||(LA41_10 >= MAXIMUM && LA41_10 <= NULL)||(LA41_10 >= QUOTED_ID && LA41_10 <= REAL)||(LA41_10 >= STRING && LA41_10 <= SUBSTRING)||LA41_10==SUM||LA41_10==TRIM||(LA41_10 >= 75 && LA41_10 <= 76)||LA41_10==81) ) {
                        alt41=1;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 41, 10, input);

                        throw nvae;

                    }
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 41, 1, input);

                    throw nvae;

                }
                }
                break;
            case SUBSTRING:
                {
                alt41=3;
                }
                break;
            case TRIM:
                {
                alt41=4;
                }
                break;
            case EXTRACT:
                {
                alt41=5;
                }
                break;
            case SUM:
                {
                alt41=6;
                }
                break;
            case COUNT:
                {
                alt41=7;
                }
                break;
            case AVG:
                {
                alt41=8;
                }
                break;
            case MINIMUM:
                {
                alt41=9;
                }
                break;
            case MAXIMUM:
                {
                alt41=10;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 41, 0, input);

                throw nvae;

            }

            switch (alt41) {
                case 1 :
                    // JaybirdSql.g:519:14: simpleIdentifier '(' valueList ')'
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_simpleIdentifier_in_function3355);
                    simpleIdentifier200=simpleIdentifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleIdentifier200.getTree());

                    char_literal201=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_function3357); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal201_tree = 
                    (CommonTree)adaptor.create(char_literal201)
                    ;
                    adaptor.addChild(root_0, char_literal201_tree);
                    }

                    pushFollow(FOLLOW_valueList_in_function3359);
                    valueList202=valueList();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, valueList202.getTree());

                    char_literal203=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_function3361); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal203_tree = 
                    (CommonTree)adaptor.create(char_literal203)
                    ;
                    adaptor.addChild(root_0, char_literal203_tree);
                    }

                    }
                    break;
                case 2 :
                    // JaybirdSql.g:520:14: simpleIdentifier '(' ')'
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_simpleIdentifier_in_function3376);
                    simpleIdentifier204=simpleIdentifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleIdentifier204.getTree());

                    char_literal205=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_function3378); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal205_tree = 
                    (CommonTree)adaptor.create(char_literal205)
                    ;
                    adaptor.addChild(root_0, char_literal205_tree);
                    }

                    char_literal206=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_function3380); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal206_tree = 
                    (CommonTree)adaptor.create(char_literal206)
                    ;
                    adaptor.addChild(root_0, char_literal206_tree);
                    }

                    }
                    break;
                case 3 :
                    // JaybirdSql.g:521:14: substringFunction
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_substringFunction_in_function3395);
                    substringFunction207=substringFunction();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, substringFunction207.getTree());

                    }
                    break;
                case 4 :
                    // JaybirdSql.g:522:14: trimFunction
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_trimFunction_in_function3410);
                    trimFunction208=trimFunction();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, trimFunction208.getTree());

                    }
                    break;
                case 5 :
                    // JaybirdSql.g:523:14: extractFunction
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_extractFunction_in_function3425);
                    extractFunction209=extractFunction();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, extractFunction209.getTree());

                    }
                    break;
                case 6 :
                    // JaybirdSql.g:524:14: SUM '(' ( ALL | DISTINCT )? value ')'
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    SUM210=(Token)match(input,SUM,FOLLOW_SUM_in_function3440); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    SUM210_tree = 
                    (CommonTree)adaptor.create(SUM210)
                    ;
                    adaptor.addChild(root_0, SUM210_tree);
                    }

                    char_literal211=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_function3446); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal211_tree = 
                    (CommonTree)adaptor.create(char_literal211)
                    ;
                    adaptor.addChild(root_0, char_literal211_tree);
                    }

                    // JaybirdSql.g:524:26: ( ALL | DISTINCT )?
                    int alt36=2;
                    int LA36_0 = input.LA(1);

                    if ( (LA36_0==ALL||LA36_0==DISTINCT) ) {
                        alt36=1;
                    }
                    switch (alt36) {
                        case 1 :
                            // JaybirdSql.g:
                            {
                            set212=(Token)input.LT(1);

                            if ( input.LA(1)==ALL||input.LA(1)==DISTINCT ) {
                                input.consume();
                                if ( state.backtracking==0 ) adaptor.addChild(root_0, 
                                (CommonTree)adaptor.create(set212)
                                );
                                state.errorRecovery=false;
                                state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return retval;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                throw mse;
                            }


                            }
                            break;

                    }


                    pushFollow(FOLLOW_value_in_function3455);
                    value213=value();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, value213.getTree());

                    char_literal214=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_function3457); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal214_tree = 
                    (CommonTree)adaptor.create(char_literal214)
                    ;
                    adaptor.addChild(root_0, char_literal214_tree);
                    }

                    }
                    break;
                case 7 :
                    // JaybirdSql.g:525:14: COUNT '(' ( ALL | DISTINCT )? value ')'
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    COUNT215=(Token)match(input,COUNT,FOLLOW_COUNT_in_function3472); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COUNT215_tree = 
                    (CommonTree)adaptor.create(COUNT215)
                    ;
                    adaptor.addChild(root_0, COUNT215_tree);
                    }

                    char_literal216=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_function3478); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal216_tree = 
                    (CommonTree)adaptor.create(char_literal216)
                    ;
                    adaptor.addChild(root_0, char_literal216_tree);
                    }

                    // JaybirdSql.g:525:28: ( ALL | DISTINCT )?
                    int alt37=2;
                    int LA37_0 = input.LA(1);

                    if ( (LA37_0==ALL||LA37_0==DISTINCT) ) {
                        alt37=1;
                    }
                    switch (alt37) {
                        case 1 :
                            // JaybirdSql.g:
                            {
                            set217=(Token)input.LT(1);

                            if ( input.LA(1)==ALL||input.LA(1)==DISTINCT ) {
                                input.consume();
                                if ( state.backtracking==0 ) adaptor.addChild(root_0, 
                                (CommonTree)adaptor.create(set217)
                                );
                                state.errorRecovery=false;
                                state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return retval;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                throw mse;
                            }


                            }
                            break;

                    }


                    pushFollow(FOLLOW_value_in_function3487);
                    value218=value();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, value218.getTree());

                    char_literal219=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_function3489); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal219_tree = 
                    (CommonTree)adaptor.create(char_literal219)
                    ;
                    adaptor.addChild(root_0, char_literal219_tree);
                    }

                    }
                    break;
                case 8 :
                    // JaybirdSql.g:526:14: AVG '(' ( ALL | DISTINCT )? value ')'
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    AVG220=(Token)match(input,AVG,FOLLOW_AVG_in_function3504); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    AVG220_tree = 
                    (CommonTree)adaptor.create(AVG220)
                    ;
                    adaptor.addChild(root_0, AVG220_tree);
                    }

                    char_literal221=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_function3513); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal221_tree = 
                    (CommonTree)adaptor.create(char_literal221)
                    ;
                    adaptor.addChild(root_0, char_literal221_tree);
                    }

                    // JaybirdSql.g:526:29: ( ALL | DISTINCT )?
                    int alt38=2;
                    int LA38_0 = input.LA(1);

                    if ( (LA38_0==ALL||LA38_0==DISTINCT) ) {
                        alt38=1;
                    }
                    switch (alt38) {
                        case 1 :
                            // JaybirdSql.g:
                            {
                            set222=(Token)input.LT(1);

                            if ( input.LA(1)==ALL||input.LA(1)==DISTINCT ) {
                                input.consume();
                                if ( state.backtracking==0 ) adaptor.addChild(root_0, 
                                (CommonTree)adaptor.create(set222)
                                );
                                state.errorRecovery=false;
                                state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return retval;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                throw mse;
                            }


                            }
                            break;

                    }


                    pushFollow(FOLLOW_value_in_function3522);
                    value223=value();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, value223.getTree());

                    char_literal224=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_function3524); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal224_tree = 
                    (CommonTree)adaptor.create(char_literal224)
                    ;
                    adaptor.addChild(root_0, char_literal224_tree);
                    }

                    }
                    break;
                case 9 :
                    // JaybirdSql.g:527:14: MINIMUM '(' ( ALL | DISTINCT )? value ')'
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    MINIMUM225=(Token)match(input,MINIMUM,FOLLOW_MINIMUM_in_function3539); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    MINIMUM225_tree = 
                    (CommonTree)adaptor.create(MINIMUM225)
                    ;
                    adaptor.addChild(root_0, MINIMUM225_tree);
                    }

                    char_literal226=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_function3544); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal226_tree = 
                    (CommonTree)adaptor.create(char_literal226)
                    ;
                    adaptor.addChild(root_0, char_literal226_tree);
                    }

                    // JaybirdSql.g:527:29: ( ALL | DISTINCT )?
                    int alt39=2;
                    int LA39_0 = input.LA(1);

                    if ( (LA39_0==ALL||LA39_0==DISTINCT) ) {
                        alt39=1;
                    }
                    switch (alt39) {
                        case 1 :
                            // JaybirdSql.g:
                            {
                            set227=(Token)input.LT(1);

                            if ( input.LA(1)==ALL||input.LA(1)==DISTINCT ) {
                                input.consume();
                                if ( state.backtracking==0 ) adaptor.addChild(root_0, 
                                (CommonTree)adaptor.create(set227)
                                );
                                state.errorRecovery=false;
                                state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return retval;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                throw mse;
                            }


                            }
                            break;

                    }


                    pushFollow(FOLLOW_value_in_function3553);
                    value228=value();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, value228.getTree());

                    char_literal229=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_function3555); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal229_tree = 
                    (CommonTree)adaptor.create(char_literal229)
                    ;
                    adaptor.addChild(root_0, char_literal229_tree);
                    }

                    }
                    break;
                case 10 :
                    // JaybirdSql.g:528:14: MAXIMUM '(' ( ALL | DISTINCT )? value ')'
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    MAXIMUM230=(Token)match(input,MAXIMUM,FOLLOW_MAXIMUM_in_function3570); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    MAXIMUM230_tree = 
                    (CommonTree)adaptor.create(MAXIMUM230)
                    ;
                    adaptor.addChild(root_0, MAXIMUM230_tree);
                    }

                    char_literal231=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_function3572); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal231_tree = 
                    (CommonTree)adaptor.create(char_literal231)
                    ;
                    adaptor.addChild(root_0, char_literal231_tree);
                    }

                    // JaybirdSql.g:528:26: ( ALL | DISTINCT )?
                    int alt40=2;
                    int LA40_0 = input.LA(1);

                    if ( (LA40_0==ALL||LA40_0==DISTINCT) ) {
                        alt40=1;
                    }
                    switch (alt40) {
                        case 1 :
                            // JaybirdSql.g:
                            {
                            set232=(Token)input.LT(1);

                            if ( input.LA(1)==ALL||input.LA(1)==DISTINCT ) {
                                input.consume();
                                if ( state.backtracking==0 ) adaptor.addChild(root_0, 
                                (CommonTree)adaptor.create(set232)
                                );
                                state.errorRecovery=false;
                                state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return retval;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                throw mse;
                            }


                            }
                            break;

                    }


                    pushFollow(FOLLOW_value_in_function3581);
                    value233=value();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, value233.getTree());

                    char_literal234=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_function3583); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal234_tree = 
                    (CommonTree)adaptor.create(char_literal234)
                    ;
                    adaptor.addChild(root_0, char_literal234_tree);
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
            if ( state.backtracking>0 ) { memoize(input, 40, function_StartIndex); }

        }
        return retval;
    }
    // $ANTLR end "function"


    public static class substringFunction_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "substringFunction"
    // JaybirdSql.g:531:1: substringFunction : SUBSTRING '(' value FROM value ( FOR INTEGER )? ')' ;
    public final JaybirdSqlParser.substringFunction_return substringFunction() throws RecognitionException {
        JaybirdSqlParser.substringFunction_return retval = new JaybirdSqlParser.substringFunction_return();
        retval.start = input.LT(1);

        int substringFunction_StartIndex = input.index();

        CommonTree root_0 = null;

        Token SUBSTRING235=null;
        Token char_literal236=null;
        Token FROM238=null;
        Token FOR240=null;
        Token INTEGER241=null;
        Token char_literal242=null;
        JaybirdSqlParser.value_return value237 =null;

        JaybirdSqlParser.value_return value239 =null;


        CommonTree SUBSTRING235_tree=null;
        CommonTree char_literal236_tree=null;
        CommonTree FROM238_tree=null;
        CommonTree FOR240_tree=null;
        CommonTree INTEGER241_tree=null;
        CommonTree char_literal242_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 41) ) { return retval; }

            // JaybirdSql.g:532:9: ( SUBSTRING '(' value FROM value ( FOR INTEGER )? ')' )
            // JaybirdSql.g:532:14: SUBSTRING '(' value FROM value ( FOR INTEGER )? ')'
            {
            root_0 = (CommonTree)adaptor.nil();


            SUBSTRING235=(Token)match(input,SUBSTRING,FOLLOW_SUBSTRING_in_substringFunction3621); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            SUBSTRING235_tree = 
            (CommonTree)adaptor.create(SUBSTRING235)
            ;
            adaptor.addChild(root_0, SUBSTRING235_tree);
            }

            char_literal236=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_substringFunction3623); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            char_literal236_tree = 
            (CommonTree)adaptor.create(char_literal236)
            ;
            adaptor.addChild(root_0, char_literal236_tree);
            }

            pushFollow(FOLLOW_value_in_substringFunction3625);
            value237=value();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, value237.getTree());

            FROM238=(Token)match(input,FROM,FOLLOW_FROM_in_substringFunction3627); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            FROM238_tree = 
            (CommonTree)adaptor.create(FROM238)
            ;
            adaptor.addChild(root_0, FROM238_tree);
            }

            pushFollow(FOLLOW_value_in_substringFunction3629);
            value239=value();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, value239.getTree());

            // JaybirdSql.g:532:45: ( FOR INTEGER )?
            int alt42=2;
            int LA42_0 = input.LA(1);

            if ( (LA42_0==FOR) ) {
                alt42=1;
            }
            switch (alt42) {
                case 1 :
                    // JaybirdSql.g:532:46: FOR INTEGER
                    {
                    FOR240=(Token)match(input,FOR,FOLLOW_FOR_in_substringFunction3632); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    FOR240_tree = 
                    (CommonTree)adaptor.create(FOR240)
                    ;
                    adaptor.addChild(root_0, FOR240_tree);
                    }

                    INTEGER241=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_substringFunction3634); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    INTEGER241_tree = 
                    (CommonTree)adaptor.create(INTEGER241)
                    ;
                    adaptor.addChild(root_0, INTEGER241_tree);
                    }

                    }
                    break;

            }


            char_literal242=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_substringFunction3638); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            char_literal242_tree = 
            (CommonTree)adaptor.create(char_literal242)
            ;
            adaptor.addChild(root_0, char_literal242_tree);
            }

            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
            if ( state.backtracking>0 ) { memoize(input, 41, substringFunction_StartIndex); }

        }
        return retval;
    }
    // $ANTLR end "substringFunction"


    public static class trimFunction_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "trimFunction"
    // JaybirdSql.g:535:1: trimFunction : TRIM '(' ( trimSpecification )? value ( FROM value )? ')' ;
    public final JaybirdSqlParser.trimFunction_return trimFunction() throws RecognitionException {
        JaybirdSqlParser.trimFunction_return retval = new JaybirdSqlParser.trimFunction_return();
        retval.start = input.LT(1);

        int trimFunction_StartIndex = input.index();

        CommonTree root_0 = null;

        Token TRIM243=null;
        Token char_literal244=null;
        Token FROM247=null;
        Token char_literal249=null;
        JaybirdSqlParser.trimSpecification_return trimSpecification245 =null;

        JaybirdSqlParser.value_return value246 =null;

        JaybirdSqlParser.value_return value248 =null;


        CommonTree TRIM243_tree=null;
        CommonTree char_literal244_tree=null;
        CommonTree FROM247_tree=null;
        CommonTree char_literal249_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 42) ) { return retval; }

            // JaybirdSql.g:536:9: ( TRIM '(' ( trimSpecification )? value ( FROM value )? ')' )
            // JaybirdSql.g:536:14: TRIM '(' ( trimSpecification )? value ( FROM value )? ')'
            {
            root_0 = (CommonTree)adaptor.nil();


            TRIM243=(Token)match(input,TRIM,FOLLOW_TRIM_in_trimFunction3674); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            TRIM243_tree = 
            (CommonTree)adaptor.create(TRIM243)
            ;
            adaptor.addChild(root_0, TRIM243_tree);
            }

            char_literal244=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_trimFunction3676); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            char_literal244_tree = 
            (CommonTree)adaptor.create(char_literal244)
            ;
            adaptor.addChild(root_0, char_literal244_tree);
            }

            // JaybirdSql.g:536:23: ( trimSpecification )?
            int alt43=2;
            int LA43_0 = input.LA(1);

            if ( (LA43_0==BOTH||LA43_0==LEADING||LA43_0==TRAILING) ) {
                alt43=1;
            }
            switch (alt43) {
                case 1 :
                    // JaybirdSql.g:536:24: trimSpecification
                    {
                    pushFollow(FOLLOW_trimSpecification_in_trimFunction3679);
                    trimSpecification245=trimSpecification();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, trimSpecification245.getTree());

                    }
                    break;

            }


            pushFollow(FOLLOW_value_in_trimFunction3683);
            value246=value();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, value246.getTree());

            // JaybirdSql.g:536:50: ( FROM value )?
            int alt44=2;
            int LA44_0 = input.LA(1);

            if ( (LA44_0==FROM) ) {
                alt44=1;
            }
            switch (alt44) {
                case 1 :
                    // JaybirdSql.g:536:51: FROM value
                    {
                    FROM247=(Token)match(input,FROM,FOLLOW_FROM_in_trimFunction3686); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    FROM247_tree = 
                    (CommonTree)adaptor.create(FROM247)
                    ;
                    adaptor.addChild(root_0, FROM247_tree);
                    }

                    pushFollow(FOLLOW_value_in_trimFunction3688);
                    value248=value();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, value248.getTree());

                    }
                    break;

            }


            char_literal249=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_trimFunction3692); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            char_literal249_tree = 
            (CommonTree)adaptor.create(char_literal249)
            ;
            adaptor.addChild(root_0, char_literal249_tree);
            }

            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
            if ( state.backtracking>0 ) { memoize(input, 42, trimFunction_StartIndex); }

        }
        return retval;
    }
    // $ANTLR end "trimFunction"


    public static class extractFunction_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "extractFunction"
    // JaybirdSql.g:539:1: extractFunction : EXTRACT '(' value FROM value ')' ;
    public final JaybirdSqlParser.extractFunction_return extractFunction() throws RecognitionException {
        JaybirdSqlParser.extractFunction_return retval = new JaybirdSqlParser.extractFunction_return();
        retval.start = input.LT(1);

        int extractFunction_StartIndex = input.index();

        CommonTree root_0 = null;

        Token EXTRACT250=null;
        Token char_literal251=null;
        Token FROM253=null;
        Token char_literal255=null;
        JaybirdSqlParser.value_return value252 =null;

        JaybirdSqlParser.value_return value254 =null;


        CommonTree EXTRACT250_tree=null;
        CommonTree char_literal251_tree=null;
        CommonTree FROM253_tree=null;
        CommonTree char_literal255_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 43) ) { return retval; }

            // JaybirdSql.g:540:9: ( EXTRACT '(' value FROM value ')' )
            // JaybirdSql.g:540:14: EXTRACT '(' value FROM value ')'
            {
            root_0 = (CommonTree)adaptor.nil();


            EXTRACT250=(Token)match(input,EXTRACT,FOLLOW_EXTRACT_in_extractFunction3728); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            EXTRACT250_tree = 
            (CommonTree)adaptor.create(EXTRACT250)
            ;
            adaptor.addChild(root_0, EXTRACT250_tree);
            }

            char_literal251=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_extractFunction3730); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            char_literal251_tree = 
            (CommonTree)adaptor.create(char_literal251)
            ;
            adaptor.addChild(root_0, char_literal251_tree);
            }

            pushFollow(FOLLOW_value_in_extractFunction3732);
            value252=value();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, value252.getTree());

            FROM253=(Token)match(input,FROM,FOLLOW_FROM_in_extractFunction3734); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            FROM253_tree = 
            (CommonTree)adaptor.create(FROM253)
            ;
            adaptor.addChild(root_0, FROM253_tree);
            }

            pushFollow(FOLLOW_value_in_extractFunction3736);
            value254=value();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, value254.getTree());

            char_literal255=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_extractFunction3738); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            char_literal255_tree = 
            (CommonTree)adaptor.create(char_literal255)
            ;
            adaptor.addChild(root_0, char_literal255_tree);
            }

            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
            if ( state.backtracking>0 ) { memoize(input, 43, extractFunction_StartIndex); }

        }
        return retval;
    }
    // $ANTLR end "extractFunction"


    public static class trimSpecification_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "trimSpecification"
    // JaybirdSql.g:543:1: trimSpecification : ( BOTH | TRAILING | LEADING );
    public final JaybirdSqlParser.trimSpecification_return trimSpecification() throws RecognitionException {
        JaybirdSqlParser.trimSpecification_return retval = new JaybirdSqlParser.trimSpecification_return();
        retval.start = input.LT(1);

        int trimSpecification_StartIndex = input.index();

        CommonTree root_0 = null;

        Token set256=null;

        CommonTree set256_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 44) ) { return retval; }

            // JaybirdSql.g:544:9: ( BOTH | TRAILING | LEADING )
            // JaybirdSql.g:
            {
            root_0 = (CommonTree)adaptor.nil();


            set256=(Token)input.LT(1);

            if ( input.LA(1)==BOTH||input.LA(1)==LEADING||input.LA(1)==TRAILING ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, 
                (CommonTree)adaptor.create(set256)
                );
                state.errorRecovery=false;
                state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
            if ( state.backtracking>0 ) { memoize(input, 44, trimSpecification_StartIndex); }

        }
        return retval;
    }
    // $ANTLR end "trimSpecification"


    public static class selectClause_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "selectClause"
    // JaybirdSql.g:549:1: selectClause : SELECT ;
    public final JaybirdSqlParser.selectClause_return selectClause() throws RecognitionException {
        JaybirdSqlParser.selectClause_return retval = new JaybirdSqlParser.selectClause_return();
        retval.start = input.LT(1);

        int selectClause_StartIndex = input.index();

        CommonTree root_0 = null;

        Token SELECT257=null;

        CommonTree SELECT257_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 45) ) { return retval; }

            // JaybirdSql.g:550:9: ( SELECT )
            // JaybirdSql.g:550:14: SELECT
            {
            root_0 = (CommonTree)adaptor.nil();


            SELECT257=(Token)match(input,SELECT,FOLLOW_SELECT_in_selectClause3842); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            SELECT257_tree = 
            (CommonTree)adaptor.create(SELECT257)
            ;
            adaptor.addChild(root_0, SELECT257_tree);
            }

            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
            if ( state.backtracking>0 ) { memoize(input, 45, selectClause_StartIndex); }

        }
        return retval;
    }
    // $ANTLR end "selectClause"

    // Delegated rules


 

    public static final BitSet FOLLOW_insertStatement_in_statement708 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_deleteStatement_in_statement723 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_updateStatement_in_statement738 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_updateOrInsertStatement_in_statement754 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DELETE_in_deleteStatement789 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_FROM_in_deleteStatement791 = new BitSet(new long[]{0x0100000004000000L});
    public static final BitSet FOLLOW_tableName_in_deleteStatement793 = new BitSet(new long[]{0x0400000000000002L});
    public static final BitSet FOLLOW_returningClause_in_deleteStatement797 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_UPDATE_in_updateStatement855 = new BitSet(new long[]{0x0100000004000000L});
    public static final BitSet FOLLOW_tableName_in_updateStatement857 = new BitSet(new long[]{0x4000000000000000L});
    public static final BitSet FOLLOW_SET_in_updateStatement859 = new BitSet(new long[]{0x0100000004000000L});
    public static final BitSet FOLLOW_assignments_in_updateStatement861 = new BitSet(new long[]{0x0400000000000002L});
    public static final BitSet FOLLOW_returningClause_in_updateStatement865 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignment_in_assignments911 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_COMMA_in_assignments914 = new BitSet(new long[]{0x0100000004000000L});
    public static final BitSet FOLLOW_assignment_in_assignments916 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_columnName_in_assignment949 = new BitSet(new long[]{0x0000000000000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_80_in_assignment951 = new BitSet(new long[]{0x033D00002C87F140L,0x000000000002182BL});
    public static final BitSet FOLLOW_value_in_assignment953 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_UPDATE_in_updateOrInsertStatement991 = new BitSet(new long[]{0x0040000000000000L});
    public static final BitSet FOLLOW_OR_in_updateOrInsertStatement993 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_INSERT_in_updateOrInsertStatement995 = new BitSet(new long[]{0x0000000040000000L});
    public static final BitSet FOLLOW_INTO_in_updateOrInsertStatement997 = new BitSet(new long[]{0x0100000004000000L});
    public static final BitSet FOLLOW_tableName_in_updateOrInsertStatement999 = new BitSet(new long[]{0x0001000000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_insertColumns_in_updateOrInsertStatement1001 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_insertValues_in_updateOrInsertStatement1021 = new BitSet(new long[]{0x0402000000000002L});
    public static final BitSet FOLLOW_matchingClause_in_updateOrInsertStatement1023 = new BitSet(new long[]{0x0400000000000002L});
    public static final BitSet FOLLOW_returningClause_in_updateOrInsertStatement1026 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MATCHING_in_matchingClause1072 = new BitSet(new long[]{0x0100000004000000L});
    public static final BitSet FOLLOW_columnList_in_matchingClause1074 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INSERT_in_insertStatement1106 = new BitSet(new long[]{0x0000000040000000L});
    public static final BitSet FOLLOW_INTO_in_insertStatement1108 = new BitSet(new long[]{0x0100000004000000L});
    public static final BitSet FOLLOW_tableName_in_insertStatement1110 = new BitSet(new long[]{0x2001000000080000L,0x0000000000000100L});
    public static final BitSet FOLLOW_insertColumns_in_insertStatement1112 = new BitSet(new long[]{0x2000000000080000L,0x0000000000000100L});
    public static final BitSet FOLLOW_insertValues_in_insertStatement1140 = new BitSet(new long[]{0x0400000000000002L});
    public static final BitSet FOLLOW_returningClause_in_insertStatement1142 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selectClause_in_insertStatement1170 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_defaultValuesClause_in_insertStatement1197 = new BitSet(new long[]{0x0400000000000002L});
    public static final BitSet FOLLOW_returningClause_in_insertStatement1199 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_insertColumns1265 = new BitSet(new long[]{0x0100000004000000L});
    public static final BitSet FOLLOW_columnList_in_insertColumns1267 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_insertColumns1269 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VALUES_in_insertValues1312 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_insertValues1314 = new BitSet(new long[]{0x033D00002C87F140L,0x000000000002182BL});
    public static final BitSet FOLLOW_valueList_in_insertValues1316 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_insertValues1318 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RETURNING_in_returningClause1354 = new BitSet(new long[]{0x0100000004000000L});
    public static final BitSet FOLLOW_columnList_in_returningClause1358 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DEFAULT_in_defaultValuesClause1388 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_VALUES_in_defaultValuesClause1390 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simpleIdentifier_in_fullIdentifier1481 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_77_in_fullIdentifier1483 = new BitSet(new long[]{0x0100000004000000L});
    public static final BitSet FOLLOW_simpleIdentifier_in_fullIdentifier1485 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simpleIdentifier_in_tableName1522 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnName_in_columnList1570 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_COMMA_in_columnList1573 = new BitSet(new long[]{0x0100000004000000L});
    public static final BitSet FOLLOW_columnName_in_columnList1575 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_simpleIdentifier_in_columnName1609 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fullIdentifier_in_columnName1656 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_value_in_valueList1699 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_COMMA_in_valueList1702 = new BitSet(new long[]{0x033D00002C87F140L,0x000000000002182BL});
    public static final BitSet FOLLOW_value_in_valueList1704 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_simpleValue_in_value1740 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simpleValue_in_value1755 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_75_in_value1757 = new BitSet(new long[]{0x0200000024000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_simpleValue_in_value1759 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simpleValue_in_value1774 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001000L});
    public static final BitSet FOLLOW_76_in_value1776 = new BitSet(new long[]{0x0200000024000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_simpleValue_in_value1778 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simpleValue_in_value1793 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_74_in_value1795 = new BitSet(new long[]{0x0200000024000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_simpleValue_in_value1797 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simpleValue_in_value1812 = new BitSet(new long[]{0x0000000000000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_78_in_value1817 = new BitSet(new long[]{0x0200000024000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_simpleValue_in_value1819 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simpleValue_in_value1834 = new BitSet(new long[]{0x0000000000000000L,0x0000000000100000L});
    public static final BitSet FOLLOW_84_in_value1836 = new BitSet(new long[]{0x0200000024000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_simpleValue_in_value1838 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_75_in_value1853 = new BitSet(new long[]{0x0200000024000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_simpleValue_in_value1855 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_76_in_value1870 = new BitSet(new long[]{0x0200000024000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_simpleValue_in_value1872 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_value1896 = new BitSet(new long[]{0x0200000024000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_simpleValue_in_value1898 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_value1900 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simpleValue_in_value1924 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_COLLATE_in_value1926 = new BitSet(new long[]{0x0100000004000000L});
    public static final BitSet FOLLOW_simpleIdentifier_in_value1931 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_parameter_in_value1951 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CURRENT_USER_in_value1975 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CURRENT_ROLE_in_value1990 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CURRENT_DATE_in_value2005 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CURRENT_TIME_in_value2020 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CURRENT_TIMESTAMP_in_value2035 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nullValue_in_value2059 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_function_in_value2083 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nextValueExpression_in_value2098 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_castExpression_in_value2113 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_arrayElement_in_value2138 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DB_KEY_in_value2162 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simpleIdentifier_in_value2177 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_77_in_value2179 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_DB_KEY_in_value2181 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_81_in_parameter2213 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NULL_in_nullValue2241 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NEXT_in_nextValueExpression2359 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L});
    public static final BitSet FOLLOW_VALUE_in_nextValueExpression2361 = new BitSet(new long[]{0x0000000001000000L});
    public static final BitSet FOLLOW_FOR_in_nextValueExpression2363 = new BitSet(new long[]{0x0100000004000000L});
    public static final BitSet FOLLOW_simpleIdentifier_in_nextValueExpression2365 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GEN_ID_in_nextValueExpression2380 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_nextValueExpression2382 = new BitSet(new long[]{0x0100000004000000L});
    public static final BitSet FOLLOW_simpleIdentifier_in_nextValueExpression2384 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_COMMA_in_nextValueExpression2386 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INTEGER_in_nextValueExpression2388 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_nextValueExpression2390 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CAST_in_castExpression2426 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_castExpression2428 = new BitSet(new long[]{0x033D00002C87F140L,0x000000000002182BL});
    public static final BitSet FOLLOW_value_in_castExpression2430 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_AS_in_castExpression2432 = new BitSet(new long[]{0x000079FF80000000L});
    public static final BitSet FOLLOW_dataTypeDescriptor_in_castExpression2434 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_castExpression2436 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nonArrayType_in_dataTypeDescriptor2472 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_arrayType_in_dataTypeDescriptor2487 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simpleType_in_nonArrayType2523 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_blobType_in_nonArrayType2538 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nonCharType_in_simpleType2566 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_charType_in_simpleType2581 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nonCharSetCharType_in_charType2617 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_charSetCharType_in_charType2632 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KW_CHAR_in_nonCharSetCharType2660 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_nonCharSetCharType2663 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INTEGER_in_nonCharSetCharType2665 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_nonCharSetCharType2667 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KW_VARCHAR_in_nonCharSetCharType2684 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_nonCharSetCharType2686 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INTEGER_in_nonCharSetCharType2688 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_nonCharSetCharType2690 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nonCharSetCharType_in_charSetCharType2718 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_charSetClause_in_charSetCharType2720 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KW_BIGINT_in_nonCharType2748 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KW_DATE_in_nonCharType2763 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KW_DECIMAL_in_nonCharType2778 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_nonCharType2780 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INTEGER_in_nonCharType2782 = new BitSet(new long[]{0x0800000000000800L});
    public static final BitSet FOLLOW_COMMA_in_nonCharType2785 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INTEGER_in_nonCharType2787 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_nonCharType2791 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KW_DOUBLE_in_nonCharType2806 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_KW_PRECISION_in_nonCharType2808 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KW_FLOAT_in_nonCharType2823 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KW_INTEGER_in_nonCharType2838 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KW_INT_in_nonCharType2853 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KW_NUMERIC_in_nonCharType2868 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_nonCharType2870 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INTEGER_in_nonCharType2872 = new BitSet(new long[]{0x0800000000000800L});
    public static final BitSet FOLLOW_COMMA_in_nonCharType2875 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INTEGER_in_nonCharType2877 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_nonCharType2881 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KW_SMALLINT_in_nonCharType2896 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KW_TIME_in_nonCharType2911 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KW_TIMESTAMP_in_nonCharType2926 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KW_BLOB_in_blobType2955 = new BitSet(new long[]{0x1000000000000202L,0x0000000000000004L});
    public static final BitSet FOLLOW_blobSubtype_in_blobType2957 = new BitSet(new long[]{0x1000000000000202L});
    public static final BitSet FOLLOW_blobSegSize_in_blobType2960 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_charSetClause_in_blobType2963 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KW_BLOB_in_blobType2993 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_blobType2995 = new BitSet(new long[]{0x0800000020000800L});
    public static final BitSet FOLLOW_INTEGER_in_blobType2997 = new BitSet(new long[]{0x0800000000000800L});
    public static final BitSet FOLLOW_COMMA_in_blobType3001 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INTEGER_in_blobType3003 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_blobType3007 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SUB_TYPE_in_blobSubtype3043 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INTEGER_in_blobSubtype3045 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SUB_TYPE_in_blobSubtype3060 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_GENERIC_ID_in_blobSubtype3062 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SEGMENT_in_blobSegSize3098 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_KW_SIZE_in_blobSegSize3100 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INTEGER_in_blobSegSize3102 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CHARACTER_in_charSetClause3138 = new BitSet(new long[]{0x4000000000000000L});
    public static final BitSet FOLLOW_SET_in_charSetClause3140 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_GENERIC_ID_in_charSetClause3142 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nonCharSetCharType_in_arrayType3170 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_82_in_arrayType3172 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_arraySpec_in_arrayType3174 = new BitSet(new long[]{0x0000000000000000L,0x0000000000080000L});
    public static final BitSet FOLLOW_83_in_arrayType3176 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_charSetClause_in_arrayType3178 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nonCharType_in_arrayType3194 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_82_in_arrayType3196 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_arraySpec_in_arrayType3198 = new BitSet(new long[]{0x0000000000000000L,0x0000000000080000L});
    public static final BitSet FOLLOW_83_in_arrayType3200 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_arrayRange_in_arraySpec3236 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_COMMA_in_arraySpec3239 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_arrayRange_in_arraySpec3241 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INTEGER_in_arrayRange3279 = new BitSet(new long[]{0x0000000000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_79_in_arrayRange3282 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INTEGER_in_arrayRange3284 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simpleIdentifier_in_arrayElement3321 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_82_in_arrayElement3323 = new BitSet(new long[]{0x033D00002C87F140L,0x000000000002182BL});
    public static final BitSet FOLLOW_valueList_in_arrayElement3325 = new BitSet(new long[]{0x0000000000000000L,0x0000000000080000L});
    public static final BitSet FOLLOW_83_in_arrayElement3327 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simpleIdentifier_in_function3355 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_function3357 = new BitSet(new long[]{0x033D00002C87F140L,0x000000000002182BL});
    public static final BitSet FOLLOW_valueList_in_function3359 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_function3361 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simpleIdentifier_in_function3376 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_function3378 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_function3380 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_substringFunction_in_function3395 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_trimFunction_in_function3410 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_extractFunction_in_function3425 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SUM_in_function3440 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_function3446 = new BitSet(new long[]{0x033D00002CA7F150L,0x000000000002182BL});
    public static final BitSet FOLLOW_value_in_function3455 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_function3457 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COUNT_in_function3472 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_function3478 = new BitSet(new long[]{0x033D00002CA7F150L,0x000000000002182BL});
    public static final BitSet FOLLOW_value_in_function3487 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_function3489 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AVG_in_function3504 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_function3513 = new BitSet(new long[]{0x033D00002CA7F150L,0x000000000002182BL});
    public static final BitSet FOLLOW_value_in_function3522 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_function3524 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINIMUM_in_function3539 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_function3544 = new BitSet(new long[]{0x033D00002CA7F150L,0x000000000002182BL});
    public static final BitSet FOLLOW_value_in_function3553 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_function3555 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MAXIMUM_in_function3570 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_function3572 = new BitSet(new long[]{0x033D00002CA7F150L,0x000000000002182BL});
    public static final BitSet FOLLOW_value_in_function3581 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_function3583 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SUBSTRING_in_substringFunction3621 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_substringFunction3623 = new BitSet(new long[]{0x033D00002C87F140L,0x000000000002182BL});
    public static final BitSet FOLLOW_value_in_substringFunction3625 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_FROM_in_substringFunction3627 = new BitSet(new long[]{0x033D00002C87F140L,0x000000000002182BL});
    public static final BitSet FOLLOW_value_in_substringFunction3629 = new BitSet(new long[]{0x0800000001000000L});
    public static final BitSet FOLLOW_FOR_in_substringFunction3632 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INTEGER_in_substringFunction3634 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_substringFunction3638 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRIM_in_trimFunction3674 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_trimFunction3676 = new BitSet(new long[]{0x033D80002C87F1C0L,0x000000000002183BL});
    public static final BitSet FOLLOW_trimSpecification_in_trimFunction3679 = new BitSet(new long[]{0x033D00002C87F140L,0x000000000002182BL});
    public static final BitSet FOLLOW_value_in_trimFunction3683 = new BitSet(new long[]{0x0800000002000000L});
    public static final BitSet FOLLOW_FROM_in_trimFunction3686 = new BitSet(new long[]{0x033D00002C87F140L,0x000000000002182BL});
    public static final BitSet FOLLOW_value_in_trimFunction3688 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_trimFunction3692 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EXTRACT_in_extractFunction3728 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_extractFunction3730 = new BitSet(new long[]{0x033D00002C87F140L,0x000000000002182BL});
    public static final BitSet FOLLOW_value_in_extractFunction3732 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_FROM_in_extractFunction3734 = new BitSet(new long[]{0x033D00002C87F140L,0x000000000002182BL});
    public static final BitSet FOLLOW_value_in_extractFunction3736 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_extractFunction3738 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SELECT_in_selectClause3842 = new BitSet(new long[]{0x0000000000000002L});

}