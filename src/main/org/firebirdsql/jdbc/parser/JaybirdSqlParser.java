// $ANTLR 3.4 D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g 2011-10-30 20:15:37

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
        this.state.ruleMemo = new HashMap[139+1];
         

    }

protected TreeAdaptor adaptor = new CommonTreeAdaptor();

public void setTreeAdaptor(TreeAdaptor adaptor) {
    this.adaptor = adaptor;
}
public TreeAdaptor getTreeAdaptor() {
    return adaptor;
}
    public String[] getTokenNames() { return JaybirdSqlParser.tokenNames; }
    public String getGrammarFileName() { return "D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g"; }


    	private boolean _inReturning;
    	protected boolean _defaultValues;
    	protected JaybirdStatementModel statementModel = new JaybirdStatementModel();
    	
    	protected int _mismatchCount;
    	protected java.util.ArrayList _errorMessages = new java.util.ArrayList();
    	
    	
    	public boolean hasReturning() {
    		return statementModel.getReturningColumns().size() == 0;
    	}
    	
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
    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:185:1: statement : ( insertStatement | deleteStatement | updateStatement | updateOrInsertStatement );
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

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:186:3: ( insertStatement | deleteStatement | updateStatement | updateOrInsertStatement )
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
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:186:5: insertStatement
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_insertStatement_in_statement474);
                    insertStatement1=insertStatement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, insertStatement1.getTree());

                    }
                    break;
                case 2 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:187:5: deleteStatement
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_deleteStatement_in_statement480);
                    deleteStatement2=deleteStatement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, deleteStatement2.getTree());

                    }
                    break;
                case 3 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:188:5: updateStatement
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_updateStatement_in_statement486);
                    updateStatement3=updateStatement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, updateStatement3.getTree());

                    }
                    break;
                case 4 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:190:5: updateOrInsertStatement
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_updateOrInsertStatement_in_statement493);
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
    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:210:1: deleteStatement : DELETE FROM tableName ;
    public final JaybirdSqlParser.deleteStatement_return deleteStatement() throws RecognitionException {
        JaybirdSqlParser.deleteStatement_return retval = new JaybirdSqlParser.deleteStatement_return();
        retval.start = input.LT(1);

        int deleteStatement_StartIndex = input.index();

        CommonTree root_0 = null;

        Token DELETE5=null;
        Token FROM6=null;
        JaybirdSqlParser.tableName_return tableName7 =null;


        CommonTree DELETE5_tree=null;
        CommonTree FROM6_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 2) ) { return retval; }

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:210:17: ( DELETE FROM tableName )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:211:4: DELETE FROM tableName
            {
            root_0 = (CommonTree)adaptor.nil();


            DELETE5=(Token)match(input,DELETE,FOLLOW_DELETE_in_deleteStatement510); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            DELETE5_tree = 
            (CommonTree)adaptor.create(DELETE5)
            ;
            adaptor.addChild(root_0, DELETE5_tree);
            }

            FROM6=(Token)match(input,FROM,FOLLOW_FROM_in_deleteStatement512); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            FROM6_tree = 
            (CommonTree)adaptor.create(FROM6)
            ;
            adaptor.addChild(root_0, FROM6_tree);
            }

            pushFollow(FOLLOW_tableName_in_deleteStatement514);
            tableName7=tableName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, tableName7.getTree());

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
    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:234:1: updateStatement : UPDATE tableName SET assignments ;
    public final JaybirdSqlParser.updateStatement_return updateStatement() throws RecognitionException {
        JaybirdSqlParser.updateStatement_return retval = new JaybirdSqlParser.updateStatement_return();
        retval.start = input.LT(1);

        int updateStatement_StartIndex = input.index();

        CommonTree root_0 = null;

        Token UPDATE8=null;
        Token SET10=null;
        JaybirdSqlParser.tableName_return tableName9 =null;

        JaybirdSqlParser.assignments_return assignments11 =null;


        CommonTree UPDATE8_tree=null;
        CommonTree SET10_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 3) ) { return retval; }

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:234:17: ( UPDATE tableName SET assignments )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:235:4: UPDATE tableName SET assignments
            {
            root_0 = (CommonTree)adaptor.nil();


            UPDATE8=(Token)match(input,UPDATE,FOLLOW_UPDATE_in_updateStatement538); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            UPDATE8_tree = 
            (CommonTree)adaptor.create(UPDATE8)
            ;
            adaptor.addChild(root_0, UPDATE8_tree);
            }

            pushFollow(FOLLOW_tableName_in_updateStatement540);
            tableName9=tableName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, tableName9.getTree());

            SET10=(Token)match(input,SET,FOLLOW_SET_in_updateStatement542); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            SET10_tree = 
            (CommonTree)adaptor.create(SET10)
            ;
            adaptor.addChild(root_0, SET10_tree);
            }

            pushFollow(FOLLOW_assignments_in_updateStatement544);
            assignments11=assignments();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, assignments11.getTree());

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
    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:241:1: assignments : assignment ( ',' assignment )* ;
    public final JaybirdSqlParser.assignments_return assignments() throws RecognitionException {
        JaybirdSqlParser.assignments_return retval = new JaybirdSqlParser.assignments_return();
        retval.start = input.LT(1);

        int assignments_StartIndex = input.index();

        CommonTree root_0 = null;

        Token char_literal13=null;
        JaybirdSqlParser.assignment_return assignment12 =null;

        JaybirdSqlParser.assignment_return assignment14 =null;


        CommonTree char_literal13_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 4) ) { return retval; }

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:241:13: ( assignment ( ',' assignment )* )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:241:15: assignment ( ',' assignment )*
            {
            root_0 = (CommonTree)adaptor.nil();


            pushFollow(FOLLOW_assignment_in_assignments562);
            assignment12=assignment();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, assignment12.getTree());

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:241:26: ( ',' assignment )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( (LA2_0==COMMA) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:241:27: ',' assignment
            	    {
            	    char_literal13=(Token)match(input,COMMA,FOLLOW_COMMA_in_assignments565); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    char_literal13_tree = 
            	    (CommonTree)adaptor.create(char_literal13)
            	    ;
            	    adaptor.addChild(root_0, char_literal13_tree);
            	    }

            	    pushFollow(FOLLOW_assignment_in_assignments567);
            	    assignment14=assignment();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, assignment14.getTree());

            	    }
            	    break;

            	default :
            	    break loop2;
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
    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:244:1: assignment : columnName '=' value ;
    public final JaybirdSqlParser.assignment_return assignment() throws RecognitionException {
        JaybirdSqlParser.assignment_return retval = new JaybirdSqlParser.assignment_return();
        retval.start = input.LT(1);

        int assignment_StartIndex = input.index();

        CommonTree root_0 = null;

        Token char_literal16=null;
        JaybirdSqlParser.columnName_return columnName15 =null;

        JaybirdSqlParser.value_return value17 =null;


        CommonTree char_literal16_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 5) ) { return retval; }

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:244:12: ( columnName '=' value )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:244:14: columnName '=' value
            {
            root_0 = (CommonTree)adaptor.nil();


            pushFollow(FOLLOW_columnName_in_assignment582);
            columnName15=columnName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, columnName15.getTree());

            char_literal16=(Token)match(input,80,FOLLOW_80_in_assignment584); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            char_literal16_tree = 
            (CommonTree)adaptor.create(char_literal16)
            ;
            adaptor.addChild(root_0, char_literal16_tree);
            }

            pushFollow(FOLLOW_value_in_assignment586);
            value17=value();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, value17.getTree());

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
    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:260:1: updateOrInsertStatement : UPDATE OR INSERT INTO tableName ( insertColumns )? insertValues ( matchingClause )? ( returningClause )? ;
    public final JaybirdSqlParser.updateOrInsertStatement_return updateOrInsertStatement() throws RecognitionException {
        JaybirdSqlParser.updateOrInsertStatement_return retval = new JaybirdSqlParser.updateOrInsertStatement_return();
        retval.start = input.LT(1);

        int updateOrInsertStatement_StartIndex = input.index();

        CommonTree root_0 = null;

        Token UPDATE18=null;
        Token OR19=null;
        Token INSERT20=null;
        Token INTO21=null;
        JaybirdSqlParser.tableName_return tableName22 =null;

        JaybirdSqlParser.insertColumns_return insertColumns23 =null;

        JaybirdSqlParser.insertValues_return insertValues24 =null;

        JaybirdSqlParser.matchingClause_return matchingClause25 =null;

        JaybirdSqlParser.returningClause_return returningClause26 =null;


        CommonTree UPDATE18_tree=null;
        CommonTree OR19_tree=null;
        CommonTree INSERT20_tree=null;
        CommonTree INTO21_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 6) ) { return retval; }

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:261:3: ( UPDATE OR INSERT INTO tableName ( insertColumns )? insertValues ( matchingClause )? ( returningClause )? )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:261:5: UPDATE OR INSERT INTO tableName ( insertColumns )? insertValues ( matchingClause )? ( returningClause )?
            {
            root_0 = (CommonTree)adaptor.nil();


            UPDATE18=(Token)match(input,UPDATE,FOLLOW_UPDATE_in_updateOrInsertStatement603); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            UPDATE18_tree = 
            (CommonTree)adaptor.create(UPDATE18)
            ;
            adaptor.addChild(root_0, UPDATE18_tree);
            }

            OR19=(Token)match(input,OR,FOLLOW_OR_in_updateOrInsertStatement605); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            OR19_tree = 
            (CommonTree)adaptor.create(OR19)
            ;
            adaptor.addChild(root_0, OR19_tree);
            }

            INSERT20=(Token)match(input,INSERT,FOLLOW_INSERT_in_updateOrInsertStatement607); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            INSERT20_tree = 
            (CommonTree)adaptor.create(INSERT20)
            ;
            adaptor.addChild(root_0, INSERT20_tree);
            }

            INTO21=(Token)match(input,INTO,FOLLOW_INTO_in_updateOrInsertStatement609); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            INTO21_tree = 
            (CommonTree)adaptor.create(INTO21)
            ;
            adaptor.addChild(root_0, INTO21_tree);
            }

            pushFollow(FOLLOW_tableName_in_updateOrInsertStatement611);
            tableName22=tableName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, tableName22.getTree());

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:261:37: ( insertColumns )?
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==LEFT_PAREN) ) {
                alt3=1;
            }
            switch (alt3) {
                case 1 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:261:37: insertColumns
                    {
                    pushFollow(FOLLOW_insertColumns_in_updateOrInsertStatement613);
                    insertColumns23=insertColumns();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, insertColumns23.getTree());

                    }
                    break;

            }


            pushFollow(FOLLOW_insertValues_in_updateOrInsertStatement621);
            insertValues24=insertValues();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, insertValues24.getTree());

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:262:18: ( matchingClause )?
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0==MATCHING) ) {
                alt4=1;
            }
            switch (alt4) {
                case 1 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:262:18: matchingClause
                    {
                    pushFollow(FOLLOW_matchingClause_in_updateOrInsertStatement623);
                    matchingClause25=matchingClause();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, matchingClause25.getTree());

                    }
                    break;

            }


            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:262:34: ( returningClause )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==RETURNING) ) {
                alt5=1;
            }
            switch (alt5) {
                case 1 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:262:34: returningClause
                    {
                    pushFollow(FOLLOW_returningClause_in_updateOrInsertStatement626);
                    returningClause26=returningClause();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, returningClause26.getTree());

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
    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:268:1: matchingClause : MATCHING columnList ;
    public final JaybirdSqlParser.matchingClause_return matchingClause() throws RecognitionException {
        JaybirdSqlParser.matchingClause_return retval = new JaybirdSqlParser.matchingClause_return();
        retval.start = input.LT(1);

        int matchingClause_StartIndex = input.index();

        CommonTree root_0 = null;

        Token MATCHING27=null;
        JaybirdSqlParser.columnList_return columnList28 =null;


        CommonTree MATCHING27_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 7) ) { return retval; }

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:268:16: ( MATCHING columnList )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:268:18: MATCHING columnList
            {
            root_0 = (CommonTree)adaptor.nil();


            MATCHING27=(Token)match(input,MATCHING,FOLLOW_MATCHING_in_matchingClause645); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            MATCHING27_tree = 
            (CommonTree)adaptor.create(MATCHING27)
            ;
            adaptor.addChild(root_0, MATCHING27_tree);
            }

            pushFollow(FOLLOW_columnList_in_matchingClause647);
            columnList28=columnList();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, columnList28.getTree());

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
    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:279:1: insertStatement : INSERT INTO tableName ( insertColumns )? ( insertValues ( returningClause )? | selectClause | defaultValuesClause ( returningClause )? ) ;
    public final JaybirdSqlParser.insertStatement_return insertStatement() throws RecognitionException {
        JaybirdSqlParser.insertStatement_return retval = new JaybirdSqlParser.insertStatement_return();
        retval.start = input.LT(1);

        int insertStatement_StartIndex = input.index();

        CommonTree root_0 = null;

        Token INSERT29=null;
        Token INTO30=null;
        JaybirdSqlParser.tableName_return tableName31 =null;

        JaybirdSqlParser.insertColumns_return insertColumns32 =null;

        JaybirdSqlParser.insertValues_return insertValues33 =null;

        JaybirdSqlParser.returningClause_return returningClause34 =null;

        JaybirdSqlParser.selectClause_return selectClause35 =null;

        JaybirdSqlParser.defaultValuesClause_return defaultValuesClause36 =null;

        JaybirdSqlParser.returningClause_return returningClause37 =null;


        CommonTree INSERT29_tree=null;
        CommonTree INTO30_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 8) ) { return retval; }

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:280:3: ( INSERT INTO tableName ( insertColumns )? ( insertValues ( returningClause )? | selectClause | defaultValuesClause ( returningClause )? ) )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:280:6: INSERT INTO tableName ( insertColumns )? ( insertValues ( returningClause )? | selectClause | defaultValuesClause ( returningClause )? )
            {
            root_0 = (CommonTree)adaptor.nil();


            INSERT29=(Token)match(input,INSERT,FOLLOW_INSERT_in_insertStatement664); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            INSERT29_tree = 
            (CommonTree)adaptor.create(INSERT29)
            ;
            adaptor.addChild(root_0, INSERT29_tree);
            }

            INTO30=(Token)match(input,INTO,FOLLOW_INTO_in_insertStatement666); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            INTO30_tree = 
            (CommonTree)adaptor.create(INTO30)
            ;
            adaptor.addChild(root_0, INTO30_tree);
            }

            pushFollow(FOLLOW_tableName_in_insertStatement668);
            tableName31=tableName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, tableName31.getTree());

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:280:28: ( insertColumns )?
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0==LEFT_PAREN) ) {
                alt6=1;
            }
            switch (alt6) {
                case 1 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:280:28: insertColumns
                    {
                    pushFollow(FOLLOW_insertColumns_in_insertStatement670);
                    insertColumns32=insertColumns();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, insertColumns32.getTree());

                    }
                    break;

            }


            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:281:6: ( insertValues ( returningClause )? | selectClause | defaultValuesClause ( returningClause )? )
            int alt9=3;
            switch ( input.LA(1) ) {
            case VALUES:
                {
                alt9=1;
                }
                break;
            case SELECT:
                {
                alt9=2;
                }
                break;
            case DEFAULT:
                {
                alt9=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 9, 0, input);

                throw nvae;

            }

            switch (alt9) {
                case 1 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:281:8: insertValues ( returningClause )?
                    {
                    pushFollow(FOLLOW_insertValues_in_insertStatement680);
                    insertValues33=insertValues();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, insertValues33.getTree());

                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:281:21: ( returningClause )?
                    int alt7=2;
                    int LA7_0 = input.LA(1);

                    if ( (LA7_0==RETURNING) ) {
                        alt7=1;
                    }
                    switch (alt7) {
                        case 1 :
                            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:281:21: returningClause
                            {
                            pushFollow(FOLLOW_returningClause_in_insertStatement682);
                            returningClause34=returningClause();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) adaptor.addChild(root_0, returningClause34.getTree());

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:282:8: selectClause
                    {
                    pushFollow(FOLLOW_selectClause_in_insertStatement692);
                    selectClause35=selectClause();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, selectClause35.getTree());

                    }
                    break;
                case 3 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:283:8: defaultValuesClause ( returningClause )?
                    {
                    pushFollow(FOLLOW_defaultValuesClause_in_insertStatement701);
                    defaultValuesClause36=defaultValuesClause();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, defaultValuesClause36.getTree());

                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:283:28: ( returningClause )?
                    int alt8=2;
                    int LA8_0 = input.LA(1);

                    if ( (LA8_0==RETURNING) ) {
                        alt8=1;
                    }
                    switch (alt8) {
                        case 1 :
                            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:283:28: returningClause
                            {
                            pushFollow(FOLLOW_returningClause_in_insertStatement703);
                            returningClause37=returningClause();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) adaptor.addChild(root_0, returningClause37.getTree());

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
    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:290:1: insertColumns : '(' columnList ')' ;
    public final JaybirdSqlParser.insertColumns_return insertColumns() throws RecognitionException {
        JaybirdSqlParser.insertColumns_return retval = new JaybirdSqlParser.insertColumns_return();
        retval.start = input.LT(1);

        int insertColumns_StartIndex = input.index();

        CommonTree root_0 = null;

        Token char_literal38=null;
        Token char_literal40=null;
        JaybirdSqlParser.columnList_return columnList39 =null;


        CommonTree char_literal38_tree=null;
        CommonTree char_literal40_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 9) ) { return retval; }

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:291:3: ( '(' columnList ')' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:291:5: '(' columnList ')'
            {
            root_0 = (CommonTree)adaptor.nil();


            char_literal38=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_insertColumns730); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            char_literal38_tree = 
            (CommonTree)adaptor.create(char_literal38)
            ;
            adaptor.addChild(root_0, char_literal38_tree);
            }

            pushFollow(FOLLOW_columnList_in_insertColumns732);
            columnList39=columnList();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, columnList39.getTree());

            char_literal40=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_insertColumns734); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            char_literal40_tree = 
            (CommonTree)adaptor.create(char_literal40)
            ;
            adaptor.addChild(root_0, char_literal40_tree);
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
    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:297:1: insertValues : VALUES '(' valueList ')' ;
    public final JaybirdSqlParser.insertValues_return insertValues() throws RecognitionException {
        JaybirdSqlParser.insertValues_return retval = new JaybirdSqlParser.insertValues_return();
        retval.start = input.LT(1);

        int insertValues_StartIndex = input.index();

        CommonTree root_0 = null;

        Token VALUES41=null;
        Token char_literal42=null;
        Token char_literal44=null;
        JaybirdSqlParser.valueList_return valueList43 =null;


        CommonTree VALUES41_tree=null;
        CommonTree char_literal42_tree=null;
        CommonTree char_literal44_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 10) ) { return retval; }

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:298:3: ( VALUES '(' valueList ')' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:298:5: VALUES '(' valueList ')'
            {
            root_0 = (CommonTree)adaptor.nil();


            VALUES41=(Token)match(input,VALUES,FOLLOW_VALUES_in_insertValues753); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            VALUES41_tree = 
            (CommonTree)adaptor.create(VALUES41)
            ;
            adaptor.addChild(root_0, VALUES41_tree);
            }

            char_literal42=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_insertValues755); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            char_literal42_tree = 
            (CommonTree)adaptor.create(char_literal42)
            ;
            adaptor.addChild(root_0, char_literal42_tree);
            }

            pushFollow(FOLLOW_valueList_in_insertValues757);
            valueList43=valueList();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, valueList43.getTree());

            char_literal44=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_insertValues759); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            char_literal44_tree = 
            (CommonTree)adaptor.create(char_literal44)
            ;
            adaptor.addChild(root_0, char_literal44_tree);
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
    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:301:1: returningClause : RETURNING columnList ;
    public final JaybirdSqlParser.returningClause_return returningClause() throws RecognitionException {
        JaybirdSqlParser.returningClause_return retval = new JaybirdSqlParser.returningClause_return();
        retval.start = input.LT(1);

        int returningClause_StartIndex = input.index();

        CommonTree root_0 = null;

        Token RETURNING45=null;
        JaybirdSqlParser.columnList_return columnList46 =null;


        CommonTree RETURNING45_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 11) ) { return retval; }

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:302:3: ( RETURNING columnList )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:302:5: RETURNING columnList
            {
            root_0 = (CommonTree)adaptor.nil();


            RETURNING45=(Token)match(input,RETURNING,FOLLOW_RETURNING_in_returningClause774); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RETURNING45_tree = 
            (CommonTree)adaptor.create(RETURNING45)
            ;
            adaptor.addChild(root_0, RETURNING45_tree);
            }

            if ( state.backtracking==0 ) {_inReturning = true;}

            pushFollow(FOLLOW_columnList_in_returningClause778);
            columnList46=columnList();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, columnList46.getTree());

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
    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:305:1: defaultValuesClause : DEFAULT VALUES ;
    public final JaybirdSqlParser.defaultValuesClause_return defaultValuesClause() throws RecognitionException {
        JaybirdSqlParser.defaultValuesClause_return retval = new JaybirdSqlParser.defaultValuesClause_return();
        retval.start = input.LT(1);

        int defaultValuesClause_StartIndex = input.index();

        CommonTree root_0 = null;

        Token DEFAULT47=null;
        Token VALUES48=null;

        CommonTree DEFAULT47_tree=null;
        CommonTree VALUES48_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 12) ) { return retval; }

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:306:3: ( DEFAULT VALUES )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:306:5: DEFAULT VALUES
            {
            root_0 = (CommonTree)adaptor.nil();


            DEFAULT47=(Token)match(input,DEFAULT,FOLLOW_DEFAULT_in_defaultValuesClause793); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            DEFAULT47_tree = 
            (CommonTree)adaptor.create(DEFAULT47)
            ;
            adaptor.addChild(root_0, DEFAULT47_tree);
            }

            VALUES48=(Token)match(input,VALUES,FOLLOW_VALUES_in_defaultValuesClause795); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            VALUES48_tree = 
            (CommonTree)adaptor.create(VALUES48)
            ;
            adaptor.addChild(root_0, VALUES48_tree);
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
    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:312:1: simpleIdentifier : ( GENERIC_ID | QUOTED_ID );
    public final JaybirdSqlParser.simpleIdentifier_return simpleIdentifier() throws RecognitionException {
        JaybirdSqlParser.simpleIdentifier_return retval = new JaybirdSqlParser.simpleIdentifier_return();
        retval.start = input.LT(1);

        int simpleIdentifier_StartIndex = input.index();

        CommonTree root_0 = null;

        Token set49=null;

        CommonTree set49_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 13) ) { return retval; }

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:313:3: ( GENERIC_ID | QUOTED_ID )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:
            {
            root_0 = (CommonTree)adaptor.nil();


            set49=(Token)input.LT(1);

            if ( input.LA(1)==GENERIC_ID||input.LA(1)==QUOTED_ID ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, 
                (CommonTree)adaptor.create(set49)
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
    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:317:1: fullIdentifier : simpleIdentifier '.' simpleIdentifier ;
    public final JaybirdSqlParser.fullIdentifier_return fullIdentifier() throws RecognitionException {
        JaybirdSqlParser.fullIdentifier_return retval = new JaybirdSqlParser.fullIdentifier_return();
        retval.start = input.LT(1);

        int fullIdentifier_StartIndex = input.index();

        CommonTree root_0 = null;

        Token char_literal51=null;
        JaybirdSqlParser.simpleIdentifier_return simpleIdentifier50 =null;

        JaybirdSqlParser.simpleIdentifier_return simpleIdentifier52 =null;


        CommonTree char_literal51_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 14) ) { return retval; }

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:318:3: ( simpleIdentifier '.' simpleIdentifier )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:318:5: simpleIdentifier '.' simpleIdentifier
            {
            root_0 = (CommonTree)adaptor.nil();


            pushFollow(FOLLOW_simpleIdentifier_in_fullIdentifier835);
            simpleIdentifier50=simpleIdentifier();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleIdentifier50.getTree());

            char_literal51=(Token)match(input,77,FOLLOW_77_in_fullIdentifier837); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            char_literal51_tree = 
            (CommonTree)adaptor.create(char_literal51)
            ;
            adaptor.addChild(root_0, char_literal51_tree);
            }

            pushFollow(FOLLOW_simpleIdentifier_in_fullIdentifier839);
            simpleIdentifier52=simpleIdentifier();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleIdentifier52.getTree());

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
    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:321:1: tableName : t= simpleIdentifier ;
    public final JaybirdSqlParser.tableName_return tableName() throws RecognitionException {
        JaybirdSqlParser.tableName_return retval = new JaybirdSqlParser.tableName_return();
        retval.start = input.LT(1);

        int tableName_StartIndex = input.index();

        CommonTree root_0 = null;

        JaybirdSqlParser.simpleIdentifier_return t =null;



        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 15) ) { return retval; }

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:322:3: (t= simpleIdentifier )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:322:5: t= simpleIdentifier
            {
            root_0 = (CommonTree)adaptor.nil();


            pushFollow(FOLLOW_simpleIdentifier_in_tableName858);
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
    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:328:1: columnList : columnName ( ',' columnName )* ;
    public final JaybirdSqlParser.columnList_return columnList() throws RecognitionException {
        JaybirdSqlParser.columnList_return retval = new JaybirdSqlParser.columnList_return();
        retval.start = input.LT(1);

        int columnList_StartIndex = input.index();

        CommonTree root_0 = null;

        Token char_literal54=null;
        JaybirdSqlParser.columnName_return columnName53 =null;

        JaybirdSqlParser.columnName_return columnName55 =null;


        CommonTree char_literal54_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 16) ) { return retval; }

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:329:3: ( columnName ( ',' columnName )* )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:329:5: columnName ( ',' columnName )*
            {
            root_0 = (CommonTree)adaptor.nil();


            pushFollow(FOLLOW_columnName_in_columnList879);
            columnName53=columnName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, columnName53.getTree());

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:329:16: ( ',' columnName )*
            loop10:
            do {
                int alt10=2;
                int LA10_0 = input.LA(1);

                if ( (LA10_0==COMMA) ) {
                    alt10=1;
                }


                switch (alt10) {
            	case 1 :
            	    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:329:17: ',' columnName
            	    {
            	    char_literal54=(Token)match(input,COMMA,FOLLOW_COMMA_in_columnList882); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    char_literal54_tree = 
            	    (CommonTree)adaptor.create(char_literal54)
            	    ;
            	    adaptor.addChild(root_0, char_literal54_tree);
            	    }

            	    pushFollow(FOLLOW_columnName_in_columnList884);
            	    columnName55=columnName();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, columnName55.getTree());

            	    }
            	    break;

            	default :
            	    break loop10;
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
    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:332:1: columnName : (si= simpleIdentifier |fi= fullIdentifier );
    public final JaybirdSqlParser.columnName_return columnName() throws RecognitionException {
        JaybirdSqlParser.columnName_return retval = new JaybirdSqlParser.columnName_return();
        retval.start = input.LT(1);

        int columnName_StartIndex = input.index();

        CommonTree root_0 = null;

        JaybirdSqlParser.simpleIdentifier_return si =null;

        JaybirdSqlParser.fullIdentifier_return fi =null;



        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 17) ) { return retval; }

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:333:3: (si= simpleIdentifier |fi= fullIdentifier )
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0==GENERIC_ID||LA11_0==QUOTED_ID) ) {
                int LA11_1 = input.LA(2);

                if ( (LA11_1==EOF||LA11_1==COMMA||(LA11_1 >= RETURNING && LA11_1 <= RIGHT_PAREN)||LA11_1==80) ) {
                    alt11=1;
                }
                else if ( (LA11_1==77) ) {
                    alt11=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 11, 1, input);

                    throw nvae;

                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 11, 0, input);

                throw nvae;

            }
            switch (alt11) {
                case 1 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:333:5: si= simpleIdentifier
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_simpleIdentifier_in_columnName903);
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
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:341:5: fi= fullIdentifier
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_fullIdentifier_in_columnName923);
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
    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:350:1: valueList : value ( ',' value )* ;
    public final JaybirdSqlParser.valueList_return valueList() throws RecognitionException {
        JaybirdSqlParser.valueList_return retval = new JaybirdSqlParser.valueList_return();
        retval.start = input.LT(1);

        int valueList_StartIndex = input.index();

        CommonTree root_0 = null;

        Token char_literal57=null;
        JaybirdSqlParser.value_return value56 =null;

        JaybirdSqlParser.value_return value58 =null;


        CommonTree char_literal57_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 18) ) { return retval; }

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:351:3: ( value ( ',' value )* )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:351:5: value ( ',' value )*
            {
            root_0 = (CommonTree)adaptor.nil();


            pushFollow(FOLLOW_value_in_valueList942);
            value56=value();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, value56.getTree());

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:351:11: ( ',' value )*
            loop12:
            do {
                int alt12=2;
                int LA12_0 = input.LA(1);

                if ( (LA12_0==COMMA) ) {
                    alt12=1;
                }


                switch (alt12) {
            	case 1 :
            	    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:351:12: ',' value
            	    {
            	    char_literal57=(Token)match(input,COMMA,FOLLOW_COMMA_in_valueList945); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    char_literal57_tree = 
            	    (CommonTree)adaptor.create(char_literal57)
            	    ;
            	    adaptor.addChild(root_0, char_literal57_tree);
            	    }

            	    pushFollow(FOLLOW_value_in_valueList947);
            	    value58=value();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, value58.getTree());

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
    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:385:1: value : ( simpleValue | simpleValue '+' simpleValue | simpleValue '-' simpleValue | simpleValue '*' simpleValue | simpleValue '/' simpleValue | simpleValue '||' simpleValue | '+' simpleValue | '-' simpleValue | LEFT_PAREN simpleValue RIGHT_PAREN | simpleValue COLLATE simpleIdentifier | parameter | CURRENT_USER | CURRENT_ROLE | CURRENT_DATE | CURRENT_TIME | CURRENT_TIMESTAMP | nullValue | function | nextValueExpression | castExpression | arrayElement | DB_KEY | simpleIdentifier '.' DB_KEY );
    public final JaybirdSqlParser.value_return value() throws RecognitionException {
        JaybirdSqlParser.value_return retval = new JaybirdSqlParser.value_return();
        retval.start = input.LT(1);

        int value_StartIndex = input.index();

        CommonTree root_0 = null;

        Token char_literal61=null;
        Token char_literal64=null;
        Token char_literal67=null;
        Token char_literal70=null;
        Token string_literal73=null;
        Token char_literal75=null;
        Token char_literal77=null;
        Token LEFT_PAREN79=null;
        Token RIGHT_PAREN81=null;
        Token COLLATE83=null;
        Token CURRENT_USER86=null;
        Token CURRENT_ROLE87=null;
        Token CURRENT_DATE88=null;
        Token CURRENT_TIME89=null;
        Token CURRENT_TIMESTAMP90=null;
        Token DB_KEY96=null;
        Token char_literal98=null;
        Token DB_KEY99=null;
        JaybirdSqlParser.simpleValue_return simpleValue59 =null;

        JaybirdSqlParser.simpleValue_return simpleValue60 =null;

        JaybirdSqlParser.simpleValue_return simpleValue62 =null;

        JaybirdSqlParser.simpleValue_return simpleValue63 =null;

        JaybirdSqlParser.simpleValue_return simpleValue65 =null;

        JaybirdSqlParser.simpleValue_return simpleValue66 =null;

        JaybirdSqlParser.simpleValue_return simpleValue68 =null;

        JaybirdSqlParser.simpleValue_return simpleValue69 =null;

        JaybirdSqlParser.simpleValue_return simpleValue71 =null;

        JaybirdSqlParser.simpleValue_return simpleValue72 =null;

        JaybirdSqlParser.simpleValue_return simpleValue74 =null;

        JaybirdSqlParser.simpleValue_return simpleValue76 =null;

        JaybirdSqlParser.simpleValue_return simpleValue78 =null;

        JaybirdSqlParser.simpleValue_return simpleValue80 =null;

        JaybirdSqlParser.simpleValue_return simpleValue82 =null;

        JaybirdSqlParser.simpleIdentifier_return simpleIdentifier84 =null;

        JaybirdSqlParser.parameter_return parameter85 =null;

        JaybirdSqlParser.nullValue_return nullValue91 =null;

        JaybirdSqlParser.function_return function92 =null;

        JaybirdSqlParser.nextValueExpression_return nextValueExpression93 =null;

        JaybirdSqlParser.castExpression_return castExpression94 =null;

        JaybirdSqlParser.arrayElement_return arrayElement95 =null;

        JaybirdSqlParser.simpleIdentifier_return simpleIdentifier97 =null;


        CommonTree char_literal61_tree=null;
        CommonTree char_literal64_tree=null;
        CommonTree char_literal67_tree=null;
        CommonTree char_literal70_tree=null;
        CommonTree string_literal73_tree=null;
        CommonTree char_literal75_tree=null;
        CommonTree char_literal77_tree=null;
        CommonTree LEFT_PAREN79_tree=null;
        CommonTree RIGHT_PAREN81_tree=null;
        CommonTree COLLATE83_tree=null;
        CommonTree CURRENT_USER86_tree=null;
        CommonTree CURRENT_ROLE87_tree=null;
        CommonTree CURRENT_DATE88_tree=null;
        CommonTree CURRENT_TIME89_tree=null;
        CommonTree CURRENT_TIMESTAMP90_tree=null;
        CommonTree DB_KEY96_tree=null;
        CommonTree char_literal98_tree=null;
        CommonTree DB_KEY99_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 19) ) { return retval; }

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:386:3: ( simpleValue | simpleValue '+' simpleValue | simpleValue '-' simpleValue | simpleValue '*' simpleValue | simpleValue '/' simpleValue | simpleValue '||' simpleValue | '+' simpleValue | '-' simpleValue | LEFT_PAREN simpleValue RIGHT_PAREN | simpleValue COLLATE simpleIdentifier | parameter | CURRENT_USER | CURRENT_ROLE | CURRENT_DATE | CURRENT_TIME | CURRENT_TIMESTAMP | nullValue | function | nextValueExpression | castExpression | arrayElement | DB_KEY | simpleIdentifier '.' DB_KEY )
            int alt13=23;
            switch ( input.LA(1) ) {
            case GENERIC_ID:
                {
                switch ( input.LA(2) ) {
                case EOF:
                case AS:
                case COMMA:
                case FOR:
                case FROM:
                case RIGHT_PAREN:
                case 83:
                    {
                    alt13=1;
                    }
                    break;
                case 75:
                    {
                    alt13=2;
                    }
                    break;
                case 76:
                    {
                    alt13=3;
                    }
                    break;
                case 74:
                    {
                    alt13=4;
                    }
                    break;
                case 78:
                    {
                    alt13=5;
                    }
                    break;
                case 84:
                    {
                    alt13=6;
                    }
                    break;
                case COLLATE:
                    {
                    alt13=10;
                    }
                    break;
                case LEFT_PAREN:
                    {
                    alt13=18;
                    }
                    break;
                case 82:
                    {
                    alt13=21;
                    }
                    break;
                case 77:
                    {
                    alt13=23;
                    }
                    break;
                default:
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 13, 1, input);

                    throw nvae;

                }

                }
                break;
            case 75:
                {
                alt13=7;
                }
                break;
            case 76:
                {
                alt13=8;
                }
                break;
            case LEFT_PAREN:
                {
                alt13=9;
                }
                break;
            case 81:
                {
                alt13=11;
                }
                break;
            case CURRENT_USER:
                {
                alt13=12;
                }
                break;
            case CURRENT_ROLE:
                {
                alt13=13;
                }
                break;
            case CURRENT_DATE:
                {
                alt13=14;
                }
                break;
            case CURRENT_TIME:
                {
                alt13=15;
                }
                break;
            case CURRENT_TIMESTAMP:
                {
                alt13=16;
                }
                break;
            case NULL:
                {
                alt13=17;
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
                case RIGHT_PAREN:
                case 83:
                    {
                    alt13=1;
                    }
                    break;
                case 75:
                    {
                    alt13=2;
                    }
                    break;
                case 76:
                    {
                    alt13=3;
                    }
                    break;
                case 74:
                    {
                    alt13=4;
                    }
                    break;
                case 78:
                    {
                    alt13=5;
                    }
                    break;
                case 84:
                    {
                    alt13=6;
                    }
                    break;
                case COLLATE:
                    {
                    alt13=10;
                    }
                    break;
                default:
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 13, 12, input);

                    throw nvae;

                }

                }
                break;
            case QUOTED_ID:
                {
                switch ( input.LA(2) ) {
                case LEFT_PAREN:
                    {
                    alt13=18;
                    }
                    break;
                case 82:
                    {
                    alt13=21;
                    }
                    break;
                case 77:
                    {
                    alt13=23;
                    }
                    break;
                default:
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 13, 13, input);

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
                alt13=18;
                }
                break;
            case GEN_ID:
            case NEXT:
                {
                alt13=19;
                }
                break;
            case CAST:
                {
                alt13=20;
                }
                break;
            case DB_KEY:
                {
                alt13=22;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 13, 0, input);

                throw nvae;

            }

            switch (alt13) {
                case 1 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:386:5: simpleValue
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_simpleValue_in_value965);
                    simpleValue59=simpleValue();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleValue59.getTree());

                    }
                    break;
                case 2 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:387:5: simpleValue '+' simpleValue
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_simpleValue_in_value971);
                    simpleValue60=simpleValue();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleValue60.getTree());

                    char_literal61=(Token)match(input,75,FOLLOW_75_in_value973); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal61_tree = 
                    (CommonTree)adaptor.create(char_literal61)
                    ;
                    adaptor.addChild(root_0, char_literal61_tree);
                    }

                    pushFollow(FOLLOW_simpleValue_in_value975);
                    simpleValue62=simpleValue();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleValue62.getTree());

                    }
                    break;
                case 3 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:388:5: simpleValue '-' simpleValue
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_simpleValue_in_value981);
                    simpleValue63=simpleValue();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleValue63.getTree());

                    char_literal64=(Token)match(input,76,FOLLOW_76_in_value983); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal64_tree = 
                    (CommonTree)adaptor.create(char_literal64)
                    ;
                    adaptor.addChild(root_0, char_literal64_tree);
                    }

                    pushFollow(FOLLOW_simpleValue_in_value985);
                    simpleValue65=simpleValue();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleValue65.getTree());

                    }
                    break;
                case 4 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:389:5: simpleValue '*' simpleValue
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_simpleValue_in_value991);
                    simpleValue66=simpleValue();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleValue66.getTree());

                    char_literal67=(Token)match(input,74,FOLLOW_74_in_value993); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal67_tree = 
                    (CommonTree)adaptor.create(char_literal67)
                    ;
                    adaptor.addChild(root_0, char_literal67_tree);
                    }

                    pushFollow(FOLLOW_simpleValue_in_value995);
                    simpleValue68=simpleValue();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleValue68.getTree());

                    }
                    break;
                case 5 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:390:5: simpleValue '/' simpleValue
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_simpleValue_in_value1001);
                    simpleValue69=simpleValue();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleValue69.getTree());

                    char_literal70=(Token)match(input,78,FOLLOW_78_in_value1003); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal70_tree = 
                    (CommonTree)adaptor.create(char_literal70)
                    ;
                    adaptor.addChild(root_0, char_literal70_tree);
                    }

                    pushFollow(FOLLOW_simpleValue_in_value1005);
                    simpleValue71=simpleValue();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleValue71.getTree());

                    }
                    break;
                case 6 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:391:5: simpleValue '||' simpleValue
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_simpleValue_in_value1011);
                    simpleValue72=simpleValue();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleValue72.getTree());

                    string_literal73=(Token)match(input,84,FOLLOW_84_in_value1013); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    string_literal73_tree = 
                    (CommonTree)adaptor.create(string_literal73)
                    ;
                    adaptor.addChild(root_0, string_literal73_tree);
                    }

                    pushFollow(FOLLOW_simpleValue_in_value1015);
                    simpleValue74=simpleValue();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleValue74.getTree());

                    }
                    break;
                case 7 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:392:5: '+' simpleValue
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    char_literal75=(Token)match(input,75,FOLLOW_75_in_value1021); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal75_tree = 
                    (CommonTree)adaptor.create(char_literal75)
                    ;
                    adaptor.addChild(root_0, char_literal75_tree);
                    }

                    pushFollow(FOLLOW_simpleValue_in_value1023);
                    simpleValue76=simpleValue();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleValue76.getTree());

                    }
                    break;
                case 8 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:393:5: '-' simpleValue
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    char_literal77=(Token)match(input,76,FOLLOW_76_in_value1029); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal77_tree = 
                    (CommonTree)adaptor.create(char_literal77)
                    ;
                    adaptor.addChild(root_0, char_literal77_tree);
                    }

                    pushFollow(FOLLOW_simpleValue_in_value1031);
                    simpleValue78=simpleValue();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleValue78.getTree());

                    }
                    break;
                case 9 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:395:5: LEFT_PAREN simpleValue RIGHT_PAREN
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    LEFT_PAREN79=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_value1040); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LEFT_PAREN79_tree = 
                    (CommonTree)adaptor.create(LEFT_PAREN79)
                    ;
                    adaptor.addChild(root_0, LEFT_PAREN79_tree);
                    }

                    pushFollow(FOLLOW_simpleValue_in_value1042);
                    simpleValue80=simpleValue();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleValue80.getTree());

                    RIGHT_PAREN81=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_value1044); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RIGHT_PAREN81_tree = 
                    (CommonTree)adaptor.create(RIGHT_PAREN81)
                    ;
                    adaptor.addChild(root_0, RIGHT_PAREN81_tree);
                    }

                    }
                    break;
                case 10 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:397:5: simpleValue COLLATE simpleIdentifier
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_simpleValue_in_value1053);
                    simpleValue82=simpleValue();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleValue82.getTree());

                    COLLATE83=(Token)match(input,COLLATE,FOLLOW_COLLATE_in_value1055); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COLLATE83_tree = 
                    (CommonTree)adaptor.create(COLLATE83)
                    ;
                    adaptor.addChild(root_0, COLLATE83_tree);
                    }

                    pushFollow(FOLLOW_simpleIdentifier_in_value1057);
                    simpleIdentifier84=simpleIdentifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleIdentifier84.getTree());

                    }
                    break;
                case 11 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:399:5: parameter
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_parameter_in_value1065);
                    parameter85=parameter();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, parameter85.getTree());

                    }
                    break;
                case 12 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:401:5: CURRENT_USER
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    CURRENT_USER86=(Token)match(input,CURRENT_USER,FOLLOW_CURRENT_USER_in_value1074); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    CURRENT_USER86_tree = 
                    (CommonTree)adaptor.create(CURRENT_USER86)
                    ;
                    adaptor.addChild(root_0, CURRENT_USER86_tree);
                    }

                    }
                    break;
                case 13 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:402:5: CURRENT_ROLE
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    CURRENT_ROLE87=(Token)match(input,CURRENT_ROLE,FOLLOW_CURRENT_ROLE_in_value1080); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    CURRENT_ROLE87_tree = 
                    (CommonTree)adaptor.create(CURRENT_ROLE87)
                    ;
                    adaptor.addChild(root_0, CURRENT_ROLE87_tree);
                    }

                    }
                    break;
                case 14 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:403:5: CURRENT_DATE
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    CURRENT_DATE88=(Token)match(input,CURRENT_DATE,FOLLOW_CURRENT_DATE_in_value1086); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    CURRENT_DATE88_tree = 
                    (CommonTree)adaptor.create(CURRENT_DATE88)
                    ;
                    adaptor.addChild(root_0, CURRENT_DATE88_tree);
                    }

                    }
                    break;
                case 15 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:404:5: CURRENT_TIME
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    CURRENT_TIME89=(Token)match(input,CURRENT_TIME,FOLLOW_CURRENT_TIME_in_value1092); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    CURRENT_TIME89_tree = 
                    (CommonTree)adaptor.create(CURRENT_TIME89)
                    ;
                    adaptor.addChild(root_0, CURRENT_TIME89_tree);
                    }

                    }
                    break;
                case 16 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:405:5: CURRENT_TIMESTAMP
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    CURRENT_TIMESTAMP90=(Token)match(input,CURRENT_TIMESTAMP,FOLLOW_CURRENT_TIMESTAMP_in_value1098); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    CURRENT_TIMESTAMP90_tree = 
                    (CommonTree)adaptor.create(CURRENT_TIMESTAMP90)
                    ;
                    adaptor.addChild(root_0, CURRENT_TIMESTAMP90_tree);
                    }

                    }
                    break;
                case 17 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:407:5: nullValue
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_nullValue_in_value1107);
                    nullValue91=nullValue();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, nullValue91.getTree());

                    }
                    break;
                case 18 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:409:5: function
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_function_in_value1116);
                    function92=function();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, function92.getTree());

                    }
                    break;
                case 19 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:410:5: nextValueExpression
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_nextValueExpression_in_value1122);
                    nextValueExpression93=nextValueExpression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, nextValueExpression93.getTree());

                    }
                    break;
                case 20 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:411:5: castExpression
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_castExpression_in_value1128);
                    castExpression94=castExpression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, castExpression94.getTree());

                    }
                    break;
                case 21 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:414:5: arrayElement
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_arrayElement_in_value1138);
                    arrayElement95=arrayElement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, arrayElement95.getTree());

                    }
                    break;
                case 22 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:416:5: DB_KEY
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    DB_KEY96=(Token)match(input,DB_KEY,FOLLOW_DB_KEY_in_value1147); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    DB_KEY96_tree = 
                    (CommonTree)adaptor.create(DB_KEY96)
                    ;
                    adaptor.addChild(root_0, DB_KEY96_tree);
                    }

                    }
                    break;
                case 23 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:417:5: simpleIdentifier '.' DB_KEY
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_simpleIdentifier_in_value1153);
                    simpleIdentifier97=simpleIdentifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleIdentifier97.getTree());

                    char_literal98=(Token)match(input,77,FOLLOW_77_in_value1155); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal98_tree = 
                    (CommonTree)adaptor.create(char_literal98)
                    ;
                    adaptor.addChild(root_0, char_literal98_tree);
                    }

                    DB_KEY99=(Token)match(input,DB_KEY,FOLLOW_DB_KEY_in_value1157); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    DB_KEY99_tree = 
                    (CommonTree)adaptor.create(DB_KEY99)
                    ;
                    adaptor.addChild(root_0, DB_KEY99_tree);
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
    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:420:1: parameter : '?' ;
    public final JaybirdSqlParser.parameter_return parameter() throws RecognitionException {
        JaybirdSqlParser.parameter_return retval = new JaybirdSqlParser.parameter_return();
        retval.start = input.LT(1);

        int parameter_StartIndex = input.index();

        CommonTree root_0 = null;

        Token char_literal100=null;

        CommonTree char_literal100_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 20) ) { return retval; }

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:421:3: ( '?' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:421:5: '?'
            {
            root_0 = (CommonTree)adaptor.nil();


            char_literal100=(Token)match(input,81,FOLLOW_81_in_parameter1171); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            char_literal100_tree = 
            (CommonTree)adaptor.create(char_literal100)
            ;
            adaptor.addChild(root_0, char_literal100_tree);
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
    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:424:1: nullValue : NULL ;
    public final JaybirdSqlParser.nullValue_return nullValue() throws RecognitionException {
        JaybirdSqlParser.nullValue_return retval = new JaybirdSqlParser.nullValue_return();
        retval.start = input.LT(1);

        int nullValue_StartIndex = input.index();

        CommonTree root_0 = null;

        Token NULL101=null;

        CommonTree NULL101_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 21) ) { return retval; }

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:425:3: ( NULL )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:425:5: NULL
            {
            root_0 = (CommonTree)adaptor.nil();


            NULL101=(Token)match(input,NULL,FOLLOW_NULL_in_nullValue1184); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            NULL101_tree = 
            (CommonTree)adaptor.create(NULL101)
            ;
            adaptor.addChild(root_0, NULL101_tree);
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
    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:428:1: simpleValue : ( GENERIC_ID | STRING | INTEGER | REAL );
    public final JaybirdSqlParser.simpleValue_return simpleValue() throws RecognitionException {
        JaybirdSqlParser.simpleValue_return retval = new JaybirdSqlParser.simpleValue_return();
        retval.start = input.LT(1);

        int simpleValue_StartIndex = input.index();

        CommonTree root_0 = null;

        Token set102=null;

        CommonTree set102_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 22) ) { return retval; }

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:429:3: ( GENERIC_ID | STRING | INTEGER | REAL )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:
            {
            root_0 = (CommonTree)adaptor.nil();


            set102=(Token)input.LT(1);

            if ( input.LA(1)==GENERIC_ID||input.LA(1)==INTEGER||input.LA(1)==REAL||input.LA(1)==STRING ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, 
                (CommonTree)adaptor.create(set102)
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
    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:435:1: nextValueExpression : ( NEXT VALUE FOR simpleIdentifier | GEN_ID '(' simpleIdentifier ',' INTEGER ')' );
    public final JaybirdSqlParser.nextValueExpression_return nextValueExpression() throws RecognitionException {
        JaybirdSqlParser.nextValueExpression_return retval = new JaybirdSqlParser.nextValueExpression_return();
        retval.start = input.LT(1);

        int nextValueExpression_StartIndex = input.index();

        CommonTree root_0 = null;

        Token NEXT103=null;
        Token VALUE104=null;
        Token FOR105=null;
        Token GEN_ID107=null;
        Token char_literal108=null;
        Token char_literal110=null;
        Token INTEGER111=null;
        Token char_literal112=null;
        JaybirdSqlParser.simpleIdentifier_return simpleIdentifier106 =null;

        JaybirdSqlParser.simpleIdentifier_return simpleIdentifier109 =null;


        CommonTree NEXT103_tree=null;
        CommonTree VALUE104_tree=null;
        CommonTree FOR105_tree=null;
        CommonTree GEN_ID107_tree=null;
        CommonTree char_literal108_tree=null;
        CommonTree char_literal110_tree=null;
        CommonTree INTEGER111_tree=null;
        CommonTree char_literal112_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 23) ) { return retval; }

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:436:3: ( NEXT VALUE FOR simpleIdentifier | GEN_ID '(' simpleIdentifier ',' INTEGER ')' )
            int alt14=2;
            int LA14_0 = input.LA(1);

            if ( (LA14_0==NEXT) ) {
                alt14=1;
            }
            else if ( (LA14_0==GEN_ID) ) {
                alt14=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 14, 0, input);

                throw nvae;

            }
            switch (alt14) {
                case 1 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:436:5: NEXT VALUE FOR simpleIdentifier
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    NEXT103=(Token)match(input,NEXT,FOLLOW_NEXT_in_nextValueExpression1233); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    NEXT103_tree = 
                    (CommonTree)adaptor.create(NEXT103)
                    ;
                    adaptor.addChild(root_0, NEXT103_tree);
                    }

                    VALUE104=(Token)match(input,VALUE,FOLLOW_VALUE_in_nextValueExpression1235); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    VALUE104_tree = 
                    (CommonTree)adaptor.create(VALUE104)
                    ;
                    adaptor.addChild(root_0, VALUE104_tree);
                    }

                    FOR105=(Token)match(input,FOR,FOLLOW_FOR_in_nextValueExpression1237); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    FOR105_tree = 
                    (CommonTree)adaptor.create(FOR105)
                    ;
                    adaptor.addChild(root_0, FOR105_tree);
                    }

                    pushFollow(FOLLOW_simpleIdentifier_in_nextValueExpression1239);
                    simpleIdentifier106=simpleIdentifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleIdentifier106.getTree());

                    }
                    break;
                case 2 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:437:5: GEN_ID '(' simpleIdentifier ',' INTEGER ')'
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    GEN_ID107=(Token)match(input,GEN_ID,FOLLOW_GEN_ID_in_nextValueExpression1245); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    GEN_ID107_tree = 
                    (CommonTree)adaptor.create(GEN_ID107)
                    ;
                    adaptor.addChild(root_0, GEN_ID107_tree);
                    }

                    char_literal108=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_nextValueExpression1247); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal108_tree = 
                    (CommonTree)adaptor.create(char_literal108)
                    ;
                    adaptor.addChild(root_0, char_literal108_tree);
                    }

                    pushFollow(FOLLOW_simpleIdentifier_in_nextValueExpression1249);
                    simpleIdentifier109=simpleIdentifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleIdentifier109.getTree());

                    char_literal110=(Token)match(input,COMMA,FOLLOW_COMMA_in_nextValueExpression1251); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal110_tree = 
                    (CommonTree)adaptor.create(char_literal110)
                    ;
                    adaptor.addChild(root_0, char_literal110_tree);
                    }

                    INTEGER111=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_nextValueExpression1253); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    INTEGER111_tree = 
                    (CommonTree)adaptor.create(INTEGER111)
                    ;
                    adaptor.addChild(root_0, INTEGER111_tree);
                    }

                    char_literal112=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_nextValueExpression1255); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal112_tree = 
                    (CommonTree)adaptor.create(char_literal112)
                    ;
                    adaptor.addChild(root_0, char_literal112_tree);
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
    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:440:1: castExpression : CAST '(' value AS dataTypeDescriptor ')' ;
    public final JaybirdSqlParser.castExpression_return castExpression() throws RecognitionException {
        JaybirdSqlParser.castExpression_return retval = new JaybirdSqlParser.castExpression_return();
        retval.start = input.LT(1);

        int castExpression_StartIndex = input.index();

        CommonTree root_0 = null;

        Token CAST113=null;
        Token char_literal114=null;
        Token AS116=null;
        Token char_literal118=null;
        JaybirdSqlParser.value_return value115 =null;

        JaybirdSqlParser.dataTypeDescriptor_return dataTypeDescriptor117 =null;


        CommonTree CAST113_tree=null;
        CommonTree char_literal114_tree=null;
        CommonTree AS116_tree=null;
        CommonTree char_literal118_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 24) ) { return retval; }

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:441:3: ( CAST '(' value AS dataTypeDescriptor ')' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:441:5: CAST '(' value AS dataTypeDescriptor ')'
            {
            root_0 = (CommonTree)adaptor.nil();


            CAST113=(Token)match(input,CAST,FOLLOW_CAST_in_castExpression1270); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            CAST113_tree = 
            (CommonTree)adaptor.create(CAST113)
            ;
            adaptor.addChild(root_0, CAST113_tree);
            }

            char_literal114=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_castExpression1272); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            char_literal114_tree = 
            (CommonTree)adaptor.create(char_literal114)
            ;
            adaptor.addChild(root_0, char_literal114_tree);
            }

            pushFollow(FOLLOW_value_in_castExpression1274);
            value115=value();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, value115.getTree());

            AS116=(Token)match(input,AS,FOLLOW_AS_in_castExpression1276); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            AS116_tree = 
            (CommonTree)adaptor.create(AS116)
            ;
            adaptor.addChild(root_0, AS116_tree);
            }

            pushFollow(FOLLOW_dataTypeDescriptor_in_castExpression1278);
            dataTypeDescriptor117=dataTypeDescriptor();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, dataTypeDescriptor117.getTree());

            char_literal118=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_castExpression1280); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            char_literal118_tree = 
            (CommonTree)adaptor.create(char_literal118)
            ;
            adaptor.addChild(root_0, char_literal118_tree);
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
    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:444:1: dataTypeDescriptor : ( nonArrayType | arrayType );
    public final JaybirdSqlParser.dataTypeDescriptor_return dataTypeDescriptor() throws RecognitionException {
        JaybirdSqlParser.dataTypeDescriptor_return retval = new JaybirdSqlParser.dataTypeDescriptor_return();
        retval.start = input.LT(1);

        int dataTypeDescriptor_StartIndex = input.index();

        CommonTree root_0 = null;

        JaybirdSqlParser.nonArrayType_return nonArrayType119 =null;

        JaybirdSqlParser.arrayType_return arrayType120 =null;



        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 25) ) { return retval; }

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:445:3: ( nonArrayType | arrayType )
            int alt15=2;
            switch ( input.LA(1) ) {
            case KW_BIGINT:
                {
                int LA15_1 = input.LA(2);

                if ( (LA15_1==RIGHT_PAREN) ) {
                    alt15=1;
                }
                else if ( (LA15_1==82) ) {
                    alt15=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 15, 1, input);

                    throw nvae;

                }
                }
                break;
            case KW_DATE:
                {
                int LA15_2 = input.LA(2);

                if ( (LA15_2==RIGHT_PAREN) ) {
                    alt15=1;
                }
                else if ( (LA15_2==82) ) {
                    alt15=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 15, 2, input);

                    throw nvae;

                }
                }
                break;
            case KW_DECIMAL:
                {
                int LA15_3 = input.LA(2);

                if ( (LA15_3==LEFT_PAREN) ) {
                    int LA15_16 = input.LA(3);

                    if ( (LA15_16==INTEGER) ) {
                        int LA15_21 = input.LA(4);

                        if ( (LA15_21==COMMA) ) {
                            int LA15_25 = input.LA(5);

                            if ( (LA15_25==INTEGER) ) {
                                int LA15_31 = input.LA(6);

                                if ( (LA15_31==RIGHT_PAREN) ) {
                                    int LA15_26 = input.LA(7);

                                    if ( (LA15_26==RIGHT_PAREN) ) {
                                        alt15=1;
                                    }
                                    else if ( (LA15_26==82) ) {
                                        alt15=2;
                                    }
                                    else {
                                        if (state.backtracking>0) {state.failed=true; return retval;}
                                        NoViableAltException nvae =
                                            new NoViableAltException("", 15, 26, input);

                                        throw nvae;

                                    }
                                }
                                else {
                                    if (state.backtracking>0) {state.failed=true; return retval;}
                                    NoViableAltException nvae =
                                        new NoViableAltException("", 15, 31, input);

                                    throw nvae;

                                }
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return retval;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 15, 25, input);

                                throw nvae;

                            }
                        }
                        else if ( (LA15_21==RIGHT_PAREN) ) {
                            int LA15_26 = input.LA(5);

                            if ( (LA15_26==RIGHT_PAREN) ) {
                                alt15=1;
                            }
                            else if ( (LA15_26==82) ) {
                                alt15=2;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return retval;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 15, 26, input);

                                throw nvae;

                            }
                        }
                        else {
                            if (state.backtracking>0) {state.failed=true; return retval;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 15, 21, input);

                            throw nvae;

                        }
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 15, 16, input);

                        throw nvae;

                    }
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 15, 3, input);

                    throw nvae;

                }
                }
                break;
            case KW_DOUBLE:
                {
                int LA15_4 = input.LA(2);

                if ( (LA15_4==KW_PRECISION) ) {
                    int LA15_17 = input.LA(3);

                    if ( (LA15_17==RIGHT_PAREN) ) {
                        alt15=1;
                    }
                    else if ( (LA15_17==82) ) {
                        alt15=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 15, 17, input);

                        throw nvae;

                    }
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 15, 4, input);

                    throw nvae;

                }
                }
                break;
            case KW_FLOAT:
                {
                int LA15_5 = input.LA(2);

                if ( (LA15_5==RIGHT_PAREN) ) {
                    alt15=1;
                }
                else if ( (LA15_5==82) ) {
                    alt15=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 15, 5, input);

                    throw nvae;

                }
                }
                break;
            case KW_INTEGER:
                {
                int LA15_6 = input.LA(2);

                if ( (LA15_6==RIGHT_PAREN) ) {
                    alt15=1;
                }
                else if ( (LA15_6==82) ) {
                    alt15=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 15, 6, input);

                    throw nvae;

                }
                }
                break;
            case KW_INT:
                {
                int LA15_7 = input.LA(2);

                if ( (LA15_7==RIGHT_PAREN) ) {
                    alt15=1;
                }
                else if ( (LA15_7==82) ) {
                    alt15=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 15, 7, input);

                    throw nvae;

                }
                }
                break;
            case KW_NUMERIC:
                {
                int LA15_8 = input.LA(2);

                if ( (LA15_8==LEFT_PAREN) ) {
                    int LA15_18 = input.LA(3);

                    if ( (LA15_18==INTEGER) ) {
                        int LA15_22 = input.LA(4);

                        if ( (LA15_22==COMMA) ) {
                            int LA15_27 = input.LA(5);

                            if ( (LA15_27==INTEGER) ) {
                                int LA15_32 = input.LA(6);

                                if ( (LA15_32==RIGHT_PAREN) ) {
                                    int LA15_28 = input.LA(7);

                                    if ( (LA15_28==RIGHT_PAREN) ) {
                                        alt15=1;
                                    }
                                    else if ( (LA15_28==82) ) {
                                        alt15=2;
                                    }
                                    else {
                                        if (state.backtracking>0) {state.failed=true; return retval;}
                                        NoViableAltException nvae =
                                            new NoViableAltException("", 15, 28, input);

                                        throw nvae;

                                    }
                                }
                                else {
                                    if (state.backtracking>0) {state.failed=true; return retval;}
                                    NoViableAltException nvae =
                                        new NoViableAltException("", 15, 32, input);

                                    throw nvae;

                                }
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return retval;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 15, 27, input);

                                throw nvae;

                            }
                        }
                        else if ( (LA15_22==RIGHT_PAREN) ) {
                            int LA15_28 = input.LA(5);

                            if ( (LA15_28==RIGHT_PAREN) ) {
                                alt15=1;
                            }
                            else if ( (LA15_28==82) ) {
                                alt15=2;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return retval;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 15, 28, input);

                                throw nvae;

                            }
                        }
                        else {
                            if (state.backtracking>0) {state.failed=true; return retval;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 15, 22, input);

                            throw nvae;

                        }
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 15, 18, input);

                        throw nvae;

                    }
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 15, 8, input);

                    throw nvae;

                }
                }
                break;
            case KW_SMALLINT:
                {
                int LA15_9 = input.LA(2);

                if ( (LA15_9==RIGHT_PAREN) ) {
                    alt15=1;
                }
                else if ( (LA15_9==82) ) {
                    alt15=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 15, 9, input);

                    throw nvae;

                }
                }
                break;
            case KW_TIME:
                {
                int LA15_10 = input.LA(2);

                if ( (LA15_10==RIGHT_PAREN) ) {
                    alt15=1;
                }
                else if ( (LA15_10==82) ) {
                    alt15=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 15, 10, input);

                    throw nvae;

                }
                }
                break;
            case KW_TIMESTAMP:
                {
                int LA15_11 = input.LA(2);

                if ( (LA15_11==RIGHT_PAREN) ) {
                    alt15=1;
                }
                else if ( (LA15_11==82) ) {
                    alt15=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 15, 11, input);

                    throw nvae;

                }
                }
                break;
            case KW_CHAR:
                {
                switch ( input.LA(2) ) {
                case LEFT_PAREN:
                    {
                    int LA15_19 = input.LA(3);

                    if ( (LA15_19==INTEGER) ) {
                        int LA15_23 = input.LA(4);

                        if ( (LA15_23==RIGHT_PAREN) ) {
                            int LA15_29 = input.LA(5);

                            if ( (LA15_29==CHARACTER||LA15_29==RIGHT_PAREN) ) {
                                alt15=1;
                            }
                            else if ( (LA15_29==82) ) {
                                alt15=2;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return retval;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 15, 29, input);

                                throw nvae;

                            }
                        }
                        else {
                            if (state.backtracking>0) {state.failed=true; return retval;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 15, 23, input);

                            throw nvae;

                        }
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 15, 19, input);

                        throw nvae;

                    }
                    }
                    break;
                case CHARACTER:
                case RIGHT_PAREN:
                    {
                    alt15=1;
                    }
                    break;
                case 82:
                    {
                    alt15=2;
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
            case KW_VARCHAR:
                {
                int LA15_13 = input.LA(2);

                if ( (LA15_13==LEFT_PAREN) ) {
                    int LA15_20 = input.LA(3);

                    if ( (LA15_20==INTEGER) ) {
                        int LA15_24 = input.LA(4);

                        if ( (LA15_24==RIGHT_PAREN) ) {
                            int LA15_30 = input.LA(5);

                            if ( (LA15_30==CHARACTER||LA15_30==RIGHT_PAREN) ) {
                                alt15=1;
                            }
                            else if ( (LA15_30==82) ) {
                                alt15=2;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return retval;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 15, 30, input);

                                throw nvae;

                            }
                        }
                        else {
                            if (state.backtracking>0) {state.failed=true; return retval;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 15, 24, input);

                            throw nvae;

                        }
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 15, 20, input);

                        throw nvae;

                    }
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 15, 13, input);

                    throw nvae;

                }
                }
                break;
            case KW_BLOB:
                {
                alt15=1;
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
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:445:5: nonArrayType
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_nonArrayType_in_dataTypeDescriptor1295);
                    nonArrayType119=nonArrayType();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, nonArrayType119.getTree());

                    }
                    break;
                case 2 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:446:5: arrayType
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_arrayType_in_dataTypeDescriptor1301);
                    arrayType120=arrayType();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, arrayType120.getTree());

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
    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:449:1: nonArrayType : ( simpleType | blobType );
    public final JaybirdSqlParser.nonArrayType_return nonArrayType() throws RecognitionException {
        JaybirdSqlParser.nonArrayType_return retval = new JaybirdSqlParser.nonArrayType_return();
        retval.start = input.LT(1);

        int nonArrayType_StartIndex = input.index();

        CommonTree root_0 = null;

        JaybirdSqlParser.simpleType_return simpleType121 =null;

        JaybirdSqlParser.blobType_return blobType122 =null;



        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 26) ) { return retval; }

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:450:3: ( simpleType | blobType )
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( (LA16_0==KW_BIGINT||(LA16_0 >= KW_CHAR && LA16_0 <= KW_NUMERIC)||(LA16_0 >= KW_SMALLINT && LA16_0 <= KW_VARCHAR)) ) {
                alt16=1;
            }
            else if ( (LA16_0==KW_BLOB) ) {
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
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:450:5: simpleType
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_simpleType_in_nonArrayType1316);
                    simpleType121=simpleType();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleType121.getTree());

                    }
                    break;
                case 2 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:451:5: blobType
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_blobType_in_nonArrayType1322);
                    blobType122=blobType();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, blobType122.getTree());

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
    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:454:1: simpleType : ( nonCharType | charType );
    public final JaybirdSqlParser.simpleType_return simpleType() throws RecognitionException {
        JaybirdSqlParser.simpleType_return retval = new JaybirdSqlParser.simpleType_return();
        retval.start = input.LT(1);

        int simpleType_StartIndex = input.index();

        CommonTree root_0 = null;

        JaybirdSqlParser.nonCharType_return nonCharType123 =null;

        JaybirdSqlParser.charType_return charType124 =null;



        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 27) ) { return retval; }

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:455:3: ( nonCharType | charType )
            int alt17=2;
            int LA17_0 = input.LA(1);

            if ( (LA17_0==KW_BIGINT||(LA17_0 >= KW_DATE && LA17_0 <= KW_NUMERIC)||(LA17_0 >= KW_SMALLINT && LA17_0 <= KW_TIMESTAMP)) ) {
                alt17=1;
            }
            else if ( (LA17_0==KW_CHAR||LA17_0==KW_VARCHAR) ) {
                alt17=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 17, 0, input);

                throw nvae;

            }
            switch (alt17) {
                case 1 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:455:5: nonCharType
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_nonCharType_in_simpleType1335);
                    nonCharType123=nonCharType();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, nonCharType123.getTree());

                    }
                    break;
                case 2 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:456:5: charType
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_charType_in_simpleType1341);
                    charType124=charType();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, charType124.getTree());

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
    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:459:1: charType : ( nonCharSetCharType | charSetCharType );
    public final JaybirdSqlParser.charType_return charType() throws RecognitionException {
        JaybirdSqlParser.charType_return retval = new JaybirdSqlParser.charType_return();
        retval.start = input.LT(1);

        int charType_StartIndex = input.index();

        CommonTree root_0 = null;

        JaybirdSqlParser.nonCharSetCharType_return nonCharSetCharType125 =null;

        JaybirdSqlParser.charSetCharType_return charSetCharType126 =null;



        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 28) ) { return retval; }

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:460:3: ( nonCharSetCharType | charSetCharType )
            int alt18=2;
            int LA18_0 = input.LA(1);

            if ( (LA18_0==KW_CHAR) ) {
                switch ( input.LA(2) ) {
                case LEFT_PAREN:
                    {
                    int LA18_3 = input.LA(3);

                    if ( (LA18_3==INTEGER) ) {
                        int LA18_7 = input.LA(4);

                        if ( (LA18_7==RIGHT_PAREN) ) {
                            int LA18_9 = input.LA(5);

                            if ( (LA18_9==EOF||LA18_9==RIGHT_PAREN) ) {
                                alt18=1;
                            }
                            else if ( (LA18_9==CHARACTER) ) {
                                alt18=2;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return retval;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 18, 9, input);

                                throw nvae;

                            }
                        }
                        else {
                            if (state.backtracking>0) {state.failed=true; return retval;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 18, 7, input);

                            throw nvae;

                        }
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 18, 3, input);

                        throw nvae;

                    }
                    }
                    break;
                case EOF:
                case RIGHT_PAREN:
                    {
                    alt18=1;
                    }
                    break;
                case CHARACTER:
                    {
                    alt18=2;
                    }
                    break;
                default:
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 18, 1, input);

                    throw nvae;

                }

            }
            else if ( (LA18_0==KW_VARCHAR) ) {
                int LA18_2 = input.LA(2);

                if ( (LA18_2==LEFT_PAREN) ) {
                    int LA18_6 = input.LA(3);

                    if ( (LA18_6==INTEGER) ) {
                        int LA18_8 = input.LA(4);

                        if ( (LA18_8==RIGHT_PAREN) ) {
                            int LA18_10 = input.LA(5);

                            if ( (LA18_10==EOF||LA18_10==RIGHT_PAREN) ) {
                                alt18=1;
                            }
                            else if ( (LA18_10==CHARACTER) ) {
                                alt18=2;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return retval;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 18, 10, input);

                                throw nvae;

                            }
                        }
                        else {
                            if (state.backtracking>0) {state.failed=true; return retval;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 18, 8, input);

                            throw nvae;

                        }
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 18, 6, input);

                        throw nvae;

                    }
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 18, 2, input);

                    throw nvae;

                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 18, 0, input);

                throw nvae;

            }
            switch (alt18) {
                case 1 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:460:5: nonCharSetCharType
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_nonCharSetCharType_in_charType1356);
                    nonCharSetCharType125=nonCharSetCharType();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, nonCharSetCharType125.getTree());

                    }
                    break;
                case 2 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:461:5: charSetCharType
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_charSetCharType_in_charType1362);
                    charSetCharType126=charSetCharType();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, charSetCharType126.getTree());

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
    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:464:1: nonCharSetCharType : ( KW_CHAR ( '(' INTEGER ')' )? | KW_VARCHAR '(' INTEGER ')' );
    public final JaybirdSqlParser.nonCharSetCharType_return nonCharSetCharType() throws RecognitionException {
        JaybirdSqlParser.nonCharSetCharType_return retval = new JaybirdSqlParser.nonCharSetCharType_return();
        retval.start = input.LT(1);

        int nonCharSetCharType_StartIndex = input.index();

        CommonTree root_0 = null;

        Token KW_CHAR127=null;
        Token char_literal128=null;
        Token INTEGER129=null;
        Token char_literal130=null;
        Token KW_VARCHAR131=null;
        Token char_literal132=null;
        Token INTEGER133=null;
        Token char_literal134=null;

        CommonTree KW_CHAR127_tree=null;
        CommonTree char_literal128_tree=null;
        CommonTree INTEGER129_tree=null;
        CommonTree char_literal130_tree=null;
        CommonTree KW_VARCHAR131_tree=null;
        CommonTree char_literal132_tree=null;
        CommonTree INTEGER133_tree=null;
        CommonTree char_literal134_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 29) ) { return retval; }

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:465:3: ( KW_CHAR ( '(' INTEGER ')' )? | KW_VARCHAR '(' INTEGER ')' )
            int alt20=2;
            int LA20_0 = input.LA(1);

            if ( (LA20_0==KW_CHAR) ) {
                alt20=1;
            }
            else if ( (LA20_0==KW_VARCHAR) ) {
                alt20=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 20, 0, input);

                throw nvae;

            }
            switch (alt20) {
                case 1 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:465:5: KW_CHAR ( '(' INTEGER ')' )?
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    KW_CHAR127=(Token)match(input,KW_CHAR,FOLLOW_KW_CHAR_in_nonCharSetCharType1375); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    KW_CHAR127_tree = 
                    (CommonTree)adaptor.create(KW_CHAR127)
                    ;
                    adaptor.addChild(root_0, KW_CHAR127_tree);
                    }

                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:465:13: ( '(' INTEGER ')' )?
                    int alt19=2;
                    int LA19_0 = input.LA(1);

                    if ( (LA19_0==LEFT_PAREN) ) {
                        alt19=1;
                    }
                    switch (alt19) {
                        case 1 :
                            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:465:14: '(' INTEGER ')'
                            {
                            char_literal128=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_nonCharSetCharType1378); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            char_literal128_tree = 
                            (CommonTree)adaptor.create(char_literal128)
                            ;
                            adaptor.addChild(root_0, char_literal128_tree);
                            }

                            INTEGER129=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_nonCharSetCharType1380); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            INTEGER129_tree = 
                            (CommonTree)adaptor.create(INTEGER129)
                            ;
                            adaptor.addChild(root_0, INTEGER129_tree);
                            }

                            char_literal130=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_nonCharSetCharType1382); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            char_literal130_tree = 
                            (CommonTree)adaptor.create(char_literal130)
                            ;
                            adaptor.addChild(root_0, char_literal130_tree);
                            }

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:466:5: KW_VARCHAR '(' INTEGER ')'
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    KW_VARCHAR131=(Token)match(input,KW_VARCHAR,FOLLOW_KW_VARCHAR_in_nonCharSetCharType1390); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    KW_VARCHAR131_tree = 
                    (CommonTree)adaptor.create(KW_VARCHAR131)
                    ;
                    adaptor.addChild(root_0, KW_VARCHAR131_tree);
                    }

                    char_literal132=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_nonCharSetCharType1392); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal132_tree = 
                    (CommonTree)adaptor.create(char_literal132)
                    ;
                    adaptor.addChild(root_0, char_literal132_tree);
                    }

                    INTEGER133=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_nonCharSetCharType1394); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    INTEGER133_tree = 
                    (CommonTree)adaptor.create(INTEGER133)
                    ;
                    adaptor.addChild(root_0, INTEGER133_tree);
                    }

                    char_literal134=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_nonCharSetCharType1396); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal134_tree = 
                    (CommonTree)adaptor.create(char_literal134)
                    ;
                    adaptor.addChild(root_0, char_literal134_tree);
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
    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:469:1: charSetCharType : nonCharSetCharType charSetClause ;
    public final JaybirdSqlParser.charSetCharType_return charSetCharType() throws RecognitionException {
        JaybirdSqlParser.charSetCharType_return retval = new JaybirdSqlParser.charSetCharType_return();
        retval.start = input.LT(1);

        int charSetCharType_StartIndex = input.index();

        CommonTree root_0 = null;

        JaybirdSqlParser.nonCharSetCharType_return nonCharSetCharType135 =null;

        JaybirdSqlParser.charSetClause_return charSetClause136 =null;



        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 30) ) { return retval; }

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:470:3: ( nonCharSetCharType charSetClause )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:470:5: nonCharSetCharType charSetClause
            {
            root_0 = (CommonTree)adaptor.nil();


            pushFollow(FOLLOW_nonCharSetCharType_in_charSetCharType1409);
            nonCharSetCharType135=nonCharSetCharType();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, nonCharSetCharType135.getTree());

            pushFollow(FOLLOW_charSetClause_in_charSetCharType1411);
            charSetClause136=charSetClause();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, charSetClause136.getTree());

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
    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:473:1: nonCharType : ( KW_BIGINT | KW_DATE | KW_DECIMAL '(' INTEGER ( ',' INTEGER )? ')' | KW_DOUBLE KW_PRECISION | KW_FLOAT | KW_INTEGER | KW_INT | KW_NUMERIC '(' INTEGER ( ',' INTEGER )? ')' | KW_SMALLINT | KW_TIME | KW_TIMESTAMP );
    public final JaybirdSqlParser.nonCharType_return nonCharType() throws RecognitionException {
        JaybirdSqlParser.nonCharType_return retval = new JaybirdSqlParser.nonCharType_return();
        retval.start = input.LT(1);

        int nonCharType_StartIndex = input.index();

        CommonTree root_0 = null;

        Token KW_BIGINT137=null;
        Token KW_DATE138=null;
        Token KW_DECIMAL139=null;
        Token char_literal140=null;
        Token INTEGER141=null;
        Token char_literal142=null;
        Token INTEGER143=null;
        Token char_literal144=null;
        Token KW_DOUBLE145=null;
        Token KW_PRECISION146=null;
        Token KW_FLOAT147=null;
        Token KW_INTEGER148=null;
        Token KW_INT149=null;
        Token KW_NUMERIC150=null;
        Token char_literal151=null;
        Token INTEGER152=null;
        Token char_literal153=null;
        Token INTEGER154=null;
        Token char_literal155=null;
        Token KW_SMALLINT156=null;
        Token KW_TIME157=null;
        Token KW_TIMESTAMP158=null;

        CommonTree KW_BIGINT137_tree=null;
        CommonTree KW_DATE138_tree=null;
        CommonTree KW_DECIMAL139_tree=null;
        CommonTree char_literal140_tree=null;
        CommonTree INTEGER141_tree=null;
        CommonTree char_literal142_tree=null;
        CommonTree INTEGER143_tree=null;
        CommonTree char_literal144_tree=null;
        CommonTree KW_DOUBLE145_tree=null;
        CommonTree KW_PRECISION146_tree=null;
        CommonTree KW_FLOAT147_tree=null;
        CommonTree KW_INTEGER148_tree=null;
        CommonTree KW_INT149_tree=null;
        CommonTree KW_NUMERIC150_tree=null;
        CommonTree char_literal151_tree=null;
        CommonTree INTEGER152_tree=null;
        CommonTree char_literal153_tree=null;
        CommonTree INTEGER154_tree=null;
        CommonTree char_literal155_tree=null;
        CommonTree KW_SMALLINT156_tree=null;
        CommonTree KW_TIME157_tree=null;
        CommonTree KW_TIMESTAMP158_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 31) ) { return retval; }

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:474:3: ( KW_BIGINT | KW_DATE | KW_DECIMAL '(' INTEGER ( ',' INTEGER )? ')' | KW_DOUBLE KW_PRECISION | KW_FLOAT | KW_INTEGER | KW_INT | KW_NUMERIC '(' INTEGER ( ',' INTEGER )? ')' | KW_SMALLINT | KW_TIME | KW_TIMESTAMP )
            int alt23=11;
            switch ( input.LA(1) ) {
            case KW_BIGINT:
                {
                alt23=1;
                }
                break;
            case KW_DATE:
                {
                alt23=2;
                }
                break;
            case KW_DECIMAL:
                {
                alt23=3;
                }
                break;
            case KW_DOUBLE:
                {
                alt23=4;
                }
                break;
            case KW_FLOAT:
                {
                alt23=5;
                }
                break;
            case KW_INTEGER:
                {
                alt23=6;
                }
                break;
            case KW_INT:
                {
                alt23=7;
                }
                break;
            case KW_NUMERIC:
                {
                alt23=8;
                }
                break;
            case KW_SMALLINT:
                {
                alt23=9;
                }
                break;
            case KW_TIME:
                {
                alt23=10;
                }
                break;
            case KW_TIMESTAMP:
                {
                alt23=11;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 23, 0, input);

                throw nvae;

            }

            switch (alt23) {
                case 1 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:474:5: KW_BIGINT
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    KW_BIGINT137=(Token)match(input,KW_BIGINT,FOLLOW_KW_BIGINT_in_nonCharType1424); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    KW_BIGINT137_tree = 
                    (CommonTree)adaptor.create(KW_BIGINT137)
                    ;
                    adaptor.addChild(root_0, KW_BIGINT137_tree);
                    }

                    }
                    break;
                case 2 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:475:5: KW_DATE
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    KW_DATE138=(Token)match(input,KW_DATE,FOLLOW_KW_DATE_in_nonCharType1430); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    KW_DATE138_tree = 
                    (CommonTree)adaptor.create(KW_DATE138)
                    ;
                    adaptor.addChild(root_0, KW_DATE138_tree);
                    }

                    }
                    break;
                case 3 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:476:5: KW_DECIMAL '(' INTEGER ( ',' INTEGER )? ')'
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    KW_DECIMAL139=(Token)match(input,KW_DECIMAL,FOLLOW_KW_DECIMAL_in_nonCharType1436); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    KW_DECIMAL139_tree = 
                    (CommonTree)adaptor.create(KW_DECIMAL139)
                    ;
                    adaptor.addChild(root_0, KW_DECIMAL139_tree);
                    }

                    char_literal140=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_nonCharType1438); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal140_tree = 
                    (CommonTree)adaptor.create(char_literal140)
                    ;
                    adaptor.addChild(root_0, char_literal140_tree);
                    }

                    INTEGER141=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_nonCharType1440); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    INTEGER141_tree = 
                    (CommonTree)adaptor.create(INTEGER141)
                    ;
                    adaptor.addChild(root_0, INTEGER141_tree);
                    }

                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:476:28: ( ',' INTEGER )?
                    int alt21=2;
                    int LA21_0 = input.LA(1);

                    if ( (LA21_0==COMMA) ) {
                        alt21=1;
                    }
                    switch (alt21) {
                        case 1 :
                            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:476:29: ',' INTEGER
                            {
                            char_literal142=(Token)match(input,COMMA,FOLLOW_COMMA_in_nonCharType1443); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            char_literal142_tree = 
                            (CommonTree)adaptor.create(char_literal142)
                            ;
                            adaptor.addChild(root_0, char_literal142_tree);
                            }

                            INTEGER143=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_nonCharType1445); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            INTEGER143_tree = 
                            (CommonTree)adaptor.create(INTEGER143)
                            ;
                            adaptor.addChild(root_0, INTEGER143_tree);
                            }

                            }
                            break;

                    }


                    char_literal144=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_nonCharType1449); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal144_tree = 
                    (CommonTree)adaptor.create(char_literal144)
                    ;
                    adaptor.addChild(root_0, char_literal144_tree);
                    }

                    }
                    break;
                case 4 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:477:5: KW_DOUBLE KW_PRECISION
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    KW_DOUBLE145=(Token)match(input,KW_DOUBLE,FOLLOW_KW_DOUBLE_in_nonCharType1455); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    KW_DOUBLE145_tree = 
                    (CommonTree)adaptor.create(KW_DOUBLE145)
                    ;
                    adaptor.addChild(root_0, KW_DOUBLE145_tree);
                    }

                    KW_PRECISION146=(Token)match(input,KW_PRECISION,FOLLOW_KW_PRECISION_in_nonCharType1457); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    KW_PRECISION146_tree = 
                    (CommonTree)adaptor.create(KW_PRECISION146)
                    ;
                    adaptor.addChild(root_0, KW_PRECISION146_tree);
                    }

                    }
                    break;
                case 5 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:478:5: KW_FLOAT
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    KW_FLOAT147=(Token)match(input,KW_FLOAT,FOLLOW_KW_FLOAT_in_nonCharType1463); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    KW_FLOAT147_tree = 
                    (CommonTree)adaptor.create(KW_FLOAT147)
                    ;
                    adaptor.addChild(root_0, KW_FLOAT147_tree);
                    }

                    }
                    break;
                case 6 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:479:5: KW_INTEGER
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    KW_INTEGER148=(Token)match(input,KW_INTEGER,FOLLOW_KW_INTEGER_in_nonCharType1469); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    KW_INTEGER148_tree = 
                    (CommonTree)adaptor.create(KW_INTEGER148)
                    ;
                    adaptor.addChild(root_0, KW_INTEGER148_tree);
                    }

                    }
                    break;
                case 7 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:480:5: KW_INT
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    KW_INT149=(Token)match(input,KW_INT,FOLLOW_KW_INT_in_nonCharType1475); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    KW_INT149_tree = 
                    (CommonTree)adaptor.create(KW_INT149)
                    ;
                    adaptor.addChild(root_0, KW_INT149_tree);
                    }

                    }
                    break;
                case 8 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:481:5: KW_NUMERIC '(' INTEGER ( ',' INTEGER )? ')'
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    KW_NUMERIC150=(Token)match(input,KW_NUMERIC,FOLLOW_KW_NUMERIC_in_nonCharType1481); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    KW_NUMERIC150_tree = 
                    (CommonTree)adaptor.create(KW_NUMERIC150)
                    ;
                    adaptor.addChild(root_0, KW_NUMERIC150_tree);
                    }

                    char_literal151=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_nonCharType1483); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal151_tree = 
                    (CommonTree)adaptor.create(char_literal151)
                    ;
                    adaptor.addChild(root_0, char_literal151_tree);
                    }

                    INTEGER152=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_nonCharType1485); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    INTEGER152_tree = 
                    (CommonTree)adaptor.create(INTEGER152)
                    ;
                    adaptor.addChild(root_0, INTEGER152_tree);
                    }

                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:481:28: ( ',' INTEGER )?
                    int alt22=2;
                    int LA22_0 = input.LA(1);

                    if ( (LA22_0==COMMA) ) {
                        alt22=1;
                    }
                    switch (alt22) {
                        case 1 :
                            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:481:29: ',' INTEGER
                            {
                            char_literal153=(Token)match(input,COMMA,FOLLOW_COMMA_in_nonCharType1488); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            char_literal153_tree = 
                            (CommonTree)adaptor.create(char_literal153)
                            ;
                            adaptor.addChild(root_0, char_literal153_tree);
                            }

                            INTEGER154=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_nonCharType1490); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            INTEGER154_tree = 
                            (CommonTree)adaptor.create(INTEGER154)
                            ;
                            adaptor.addChild(root_0, INTEGER154_tree);
                            }

                            }
                            break;

                    }


                    char_literal155=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_nonCharType1494); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal155_tree = 
                    (CommonTree)adaptor.create(char_literal155)
                    ;
                    adaptor.addChild(root_0, char_literal155_tree);
                    }

                    }
                    break;
                case 9 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:482:5: KW_SMALLINT
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    KW_SMALLINT156=(Token)match(input,KW_SMALLINT,FOLLOW_KW_SMALLINT_in_nonCharType1500); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    KW_SMALLINT156_tree = 
                    (CommonTree)adaptor.create(KW_SMALLINT156)
                    ;
                    adaptor.addChild(root_0, KW_SMALLINT156_tree);
                    }

                    }
                    break;
                case 10 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:483:5: KW_TIME
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    KW_TIME157=(Token)match(input,KW_TIME,FOLLOW_KW_TIME_in_nonCharType1506); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    KW_TIME157_tree = 
                    (CommonTree)adaptor.create(KW_TIME157)
                    ;
                    adaptor.addChild(root_0, KW_TIME157_tree);
                    }

                    }
                    break;
                case 11 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:484:5: KW_TIMESTAMP
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    KW_TIMESTAMP158=(Token)match(input,KW_TIMESTAMP,FOLLOW_KW_TIMESTAMP_in_nonCharType1512); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    KW_TIMESTAMP158_tree = 
                    (CommonTree)adaptor.create(KW_TIMESTAMP158)
                    ;
                    adaptor.addChild(root_0, KW_TIMESTAMP158_tree);
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
    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:487:1: blobType : ( KW_BLOB ( blobSubtype )? ( blobSegSize )? ( charSetClause )? | KW_BLOB '(' ( INTEGER )? ( ',' INTEGER )? ')' );
    public final JaybirdSqlParser.blobType_return blobType() throws RecognitionException {
        JaybirdSqlParser.blobType_return retval = new JaybirdSqlParser.blobType_return();
        retval.start = input.LT(1);

        int blobType_StartIndex = input.index();

        CommonTree root_0 = null;

        Token KW_BLOB159=null;
        Token KW_BLOB163=null;
        Token char_literal164=null;
        Token INTEGER165=null;
        Token char_literal166=null;
        Token INTEGER167=null;
        Token char_literal168=null;
        JaybirdSqlParser.blobSubtype_return blobSubtype160 =null;

        JaybirdSqlParser.blobSegSize_return blobSegSize161 =null;

        JaybirdSqlParser.charSetClause_return charSetClause162 =null;


        CommonTree KW_BLOB159_tree=null;
        CommonTree KW_BLOB163_tree=null;
        CommonTree char_literal164_tree=null;
        CommonTree INTEGER165_tree=null;
        CommonTree char_literal166_tree=null;
        CommonTree INTEGER167_tree=null;
        CommonTree char_literal168_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 32) ) { return retval; }

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:488:3: ( KW_BLOB ( blobSubtype )? ( blobSegSize )? ( charSetClause )? | KW_BLOB '(' ( INTEGER )? ( ',' INTEGER )? ')' )
            int alt29=2;
            int LA29_0 = input.LA(1);

            if ( (LA29_0==KW_BLOB) ) {
                int LA29_1 = input.LA(2);

                if ( (LA29_1==LEFT_PAREN) ) {
                    alt29=2;
                }
                else if ( (LA29_1==EOF||LA29_1==CHARACTER||(LA29_1 >= RIGHT_PAREN && LA29_1 <= SEGMENT)||LA29_1==SUB_TYPE) ) {
                    alt29=1;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 29, 1, input);

                    throw nvae;

                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 29, 0, input);

                throw nvae;

            }
            switch (alt29) {
                case 1 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:488:5: KW_BLOB ( blobSubtype )? ( blobSegSize )? ( charSetClause )?
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    KW_BLOB159=(Token)match(input,KW_BLOB,FOLLOW_KW_BLOB_in_blobType1526); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    KW_BLOB159_tree = 
                    (CommonTree)adaptor.create(KW_BLOB159)
                    ;
                    adaptor.addChild(root_0, KW_BLOB159_tree);
                    }

                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:488:13: ( blobSubtype )?
                    int alt24=2;
                    int LA24_0 = input.LA(1);

                    if ( (LA24_0==SUB_TYPE) ) {
                        alt24=1;
                    }
                    switch (alt24) {
                        case 1 :
                            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:488:13: blobSubtype
                            {
                            pushFollow(FOLLOW_blobSubtype_in_blobType1528);
                            blobSubtype160=blobSubtype();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) adaptor.addChild(root_0, blobSubtype160.getTree());

                            }
                            break;

                    }


                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:488:26: ( blobSegSize )?
                    int alt25=2;
                    int LA25_0 = input.LA(1);

                    if ( (LA25_0==SEGMENT) ) {
                        alt25=1;
                    }
                    switch (alt25) {
                        case 1 :
                            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:488:26: blobSegSize
                            {
                            pushFollow(FOLLOW_blobSegSize_in_blobType1531);
                            blobSegSize161=blobSegSize();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) adaptor.addChild(root_0, blobSegSize161.getTree());

                            }
                            break;

                    }


                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:488:39: ( charSetClause )?
                    int alt26=2;
                    int LA26_0 = input.LA(1);

                    if ( (LA26_0==CHARACTER) ) {
                        alt26=1;
                    }
                    switch (alt26) {
                        case 1 :
                            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:488:39: charSetClause
                            {
                            pushFollow(FOLLOW_charSetClause_in_blobType1534);
                            charSetClause162=charSetClause();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) adaptor.addChild(root_0, charSetClause162.getTree());

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:490:5: KW_BLOB '(' ( INTEGER )? ( ',' INTEGER )? ')'
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    KW_BLOB163=(Token)match(input,KW_BLOB,FOLLOW_KW_BLOB_in_blobType1546); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    KW_BLOB163_tree = 
                    (CommonTree)adaptor.create(KW_BLOB163)
                    ;
                    adaptor.addChild(root_0, KW_BLOB163_tree);
                    }

                    char_literal164=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_blobType1548); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal164_tree = 
                    (CommonTree)adaptor.create(char_literal164)
                    ;
                    adaptor.addChild(root_0, char_literal164_tree);
                    }

                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:490:17: ( INTEGER )?
                    int alt27=2;
                    int LA27_0 = input.LA(1);

                    if ( (LA27_0==INTEGER) ) {
                        alt27=1;
                    }
                    switch (alt27) {
                        case 1 :
                            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:490:17: INTEGER
                            {
                            INTEGER165=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_blobType1550); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            INTEGER165_tree = 
                            (CommonTree)adaptor.create(INTEGER165)
                            ;
                            adaptor.addChild(root_0, INTEGER165_tree);
                            }

                            }
                            break;

                    }


                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:490:26: ( ',' INTEGER )?
                    int alt28=2;
                    int LA28_0 = input.LA(1);

                    if ( (LA28_0==COMMA) ) {
                        alt28=1;
                    }
                    switch (alt28) {
                        case 1 :
                            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:490:27: ',' INTEGER
                            {
                            char_literal166=(Token)match(input,COMMA,FOLLOW_COMMA_in_blobType1554); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            char_literal166_tree = 
                            (CommonTree)adaptor.create(char_literal166)
                            ;
                            adaptor.addChild(root_0, char_literal166_tree);
                            }

                            INTEGER167=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_blobType1556); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            INTEGER167_tree = 
                            (CommonTree)adaptor.create(INTEGER167)
                            ;
                            adaptor.addChild(root_0, INTEGER167_tree);
                            }

                            }
                            break;

                    }


                    char_literal168=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_blobType1560); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal168_tree = 
                    (CommonTree)adaptor.create(char_literal168)
                    ;
                    adaptor.addChild(root_0, char_literal168_tree);
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
    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:493:1: blobSubtype : ( SUB_TYPE INTEGER | SUB_TYPE GENERIC_ID );
    public final JaybirdSqlParser.blobSubtype_return blobSubtype() throws RecognitionException {
        JaybirdSqlParser.blobSubtype_return retval = new JaybirdSqlParser.blobSubtype_return();
        retval.start = input.LT(1);

        int blobSubtype_StartIndex = input.index();

        CommonTree root_0 = null;

        Token SUB_TYPE169=null;
        Token INTEGER170=null;
        Token SUB_TYPE171=null;
        Token GENERIC_ID172=null;

        CommonTree SUB_TYPE169_tree=null;
        CommonTree INTEGER170_tree=null;
        CommonTree SUB_TYPE171_tree=null;
        CommonTree GENERIC_ID172_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 33) ) { return retval; }

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:494:3: ( SUB_TYPE INTEGER | SUB_TYPE GENERIC_ID )
            int alt30=2;
            int LA30_0 = input.LA(1);

            if ( (LA30_0==SUB_TYPE) ) {
                int LA30_1 = input.LA(2);

                if ( (LA30_1==INTEGER) ) {
                    alt30=1;
                }
                else if ( (LA30_1==GENERIC_ID) ) {
                    alt30=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 30, 1, input);

                    throw nvae;

                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 30, 0, input);

                throw nvae;

            }
            switch (alt30) {
                case 1 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:494:5: SUB_TYPE INTEGER
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    SUB_TYPE169=(Token)match(input,SUB_TYPE,FOLLOW_SUB_TYPE_in_blobSubtype1575); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    SUB_TYPE169_tree = 
                    (CommonTree)adaptor.create(SUB_TYPE169)
                    ;
                    adaptor.addChild(root_0, SUB_TYPE169_tree);
                    }

                    INTEGER170=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_blobSubtype1577); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    INTEGER170_tree = 
                    (CommonTree)adaptor.create(INTEGER170)
                    ;
                    adaptor.addChild(root_0, INTEGER170_tree);
                    }

                    }
                    break;
                case 2 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:495:5: SUB_TYPE GENERIC_ID
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    SUB_TYPE171=(Token)match(input,SUB_TYPE,FOLLOW_SUB_TYPE_in_blobSubtype1583); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    SUB_TYPE171_tree = 
                    (CommonTree)adaptor.create(SUB_TYPE171)
                    ;
                    adaptor.addChild(root_0, SUB_TYPE171_tree);
                    }

                    GENERIC_ID172=(Token)match(input,GENERIC_ID,FOLLOW_GENERIC_ID_in_blobSubtype1585); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    GENERIC_ID172_tree = 
                    (CommonTree)adaptor.create(GENERIC_ID172)
                    ;
                    adaptor.addChild(root_0, GENERIC_ID172_tree);
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
    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:498:1: blobSegSize : SEGMENT KW_SIZE INTEGER ;
    public final JaybirdSqlParser.blobSegSize_return blobSegSize() throws RecognitionException {
        JaybirdSqlParser.blobSegSize_return retval = new JaybirdSqlParser.blobSegSize_return();
        retval.start = input.LT(1);

        int blobSegSize_StartIndex = input.index();

        CommonTree root_0 = null;

        Token SEGMENT173=null;
        Token KW_SIZE174=null;
        Token INTEGER175=null;

        CommonTree SEGMENT173_tree=null;
        CommonTree KW_SIZE174_tree=null;
        CommonTree INTEGER175_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 34) ) { return retval; }

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:499:3: ( SEGMENT KW_SIZE INTEGER )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:499:5: SEGMENT KW_SIZE INTEGER
            {
            root_0 = (CommonTree)adaptor.nil();


            SEGMENT173=(Token)match(input,SEGMENT,FOLLOW_SEGMENT_in_blobSegSize1600); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            SEGMENT173_tree = 
            (CommonTree)adaptor.create(SEGMENT173)
            ;
            adaptor.addChild(root_0, SEGMENT173_tree);
            }

            KW_SIZE174=(Token)match(input,KW_SIZE,FOLLOW_KW_SIZE_in_blobSegSize1602); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            KW_SIZE174_tree = 
            (CommonTree)adaptor.create(KW_SIZE174)
            ;
            adaptor.addChild(root_0, KW_SIZE174_tree);
            }

            INTEGER175=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_blobSegSize1604); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            INTEGER175_tree = 
            (CommonTree)adaptor.create(INTEGER175)
            ;
            adaptor.addChild(root_0, INTEGER175_tree);
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
    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:502:1: charSetClause : CHARACTER SET GENERIC_ID ;
    public final JaybirdSqlParser.charSetClause_return charSetClause() throws RecognitionException {
        JaybirdSqlParser.charSetClause_return retval = new JaybirdSqlParser.charSetClause_return();
        retval.start = input.LT(1);

        int charSetClause_StartIndex = input.index();

        CommonTree root_0 = null;

        Token CHARACTER176=null;
        Token SET177=null;
        Token GENERIC_ID178=null;

        CommonTree CHARACTER176_tree=null;
        CommonTree SET177_tree=null;
        CommonTree GENERIC_ID178_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 35) ) { return retval; }

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:503:3: ( CHARACTER SET GENERIC_ID )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:503:5: CHARACTER SET GENERIC_ID
            {
            root_0 = (CommonTree)adaptor.nil();


            CHARACTER176=(Token)match(input,CHARACTER,FOLLOW_CHARACTER_in_charSetClause1619); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            CHARACTER176_tree = 
            (CommonTree)adaptor.create(CHARACTER176)
            ;
            adaptor.addChild(root_0, CHARACTER176_tree);
            }

            SET177=(Token)match(input,SET,FOLLOW_SET_in_charSetClause1621); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            SET177_tree = 
            (CommonTree)adaptor.create(SET177)
            ;
            adaptor.addChild(root_0, SET177_tree);
            }

            GENERIC_ID178=(Token)match(input,GENERIC_ID,FOLLOW_GENERIC_ID_in_charSetClause1623); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            GENERIC_ID178_tree = 
            (CommonTree)adaptor.create(GENERIC_ID178)
            ;
            adaptor.addChild(root_0, GENERIC_ID178_tree);
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
    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:506:1: arrayType : ( nonCharSetCharType '[' arraySpec ']' ( charSetClause )? | nonCharType '[' arraySpec ']' );
    public final JaybirdSqlParser.arrayType_return arrayType() throws RecognitionException {
        JaybirdSqlParser.arrayType_return retval = new JaybirdSqlParser.arrayType_return();
        retval.start = input.LT(1);

        int arrayType_StartIndex = input.index();

        CommonTree root_0 = null;

        Token char_literal180=null;
        Token char_literal182=null;
        Token char_literal185=null;
        Token char_literal187=null;
        JaybirdSqlParser.nonCharSetCharType_return nonCharSetCharType179 =null;

        JaybirdSqlParser.arraySpec_return arraySpec181 =null;

        JaybirdSqlParser.charSetClause_return charSetClause183 =null;

        JaybirdSqlParser.nonCharType_return nonCharType184 =null;

        JaybirdSqlParser.arraySpec_return arraySpec186 =null;


        CommonTree char_literal180_tree=null;
        CommonTree char_literal182_tree=null;
        CommonTree char_literal185_tree=null;
        CommonTree char_literal187_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 36) ) { return retval; }

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:507:3: ( nonCharSetCharType '[' arraySpec ']' ( charSetClause )? | nonCharType '[' arraySpec ']' )
            int alt32=2;
            int LA32_0 = input.LA(1);

            if ( (LA32_0==KW_CHAR||LA32_0==KW_VARCHAR) ) {
                alt32=1;
            }
            else if ( (LA32_0==KW_BIGINT||(LA32_0 >= KW_DATE && LA32_0 <= KW_NUMERIC)||(LA32_0 >= KW_SMALLINT && LA32_0 <= KW_TIMESTAMP)) ) {
                alt32=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 32, 0, input);

                throw nvae;

            }
            switch (alt32) {
                case 1 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:507:5: nonCharSetCharType '[' arraySpec ']' ( charSetClause )?
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_nonCharSetCharType_in_arrayType1636);
                    nonCharSetCharType179=nonCharSetCharType();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, nonCharSetCharType179.getTree());

                    char_literal180=(Token)match(input,82,FOLLOW_82_in_arrayType1638); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal180_tree = 
                    (CommonTree)adaptor.create(char_literal180)
                    ;
                    adaptor.addChild(root_0, char_literal180_tree);
                    }

                    pushFollow(FOLLOW_arraySpec_in_arrayType1640);
                    arraySpec181=arraySpec();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, arraySpec181.getTree());

                    char_literal182=(Token)match(input,83,FOLLOW_83_in_arrayType1642); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal182_tree = 
                    (CommonTree)adaptor.create(char_literal182)
                    ;
                    adaptor.addChild(root_0, char_literal182_tree);
                    }

                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:507:42: ( charSetClause )?
                    int alt31=2;
                    int LA31_0 = input.LA(1);

                    if ( (LA31_0==CHARACTER) ) {
                        alt31=1;
                    }
                    switch (alt31) {
                        case 1 :
                            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:507:42: charSetClause
                            {
                            pushFollow(FOLLOW_charSetClause_in_arrayType1644);
                            charSetClause183=charSetClause();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) adaptor.addChild(root_0, charSetClause183.getTree());

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:508:5: nonCharType '[' arraySpec ']'
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_nonCharType_in_arrayType1651);
                    nonCharType184=nonCharType();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, nonCharType184.getTree());

                    char_literal185=(Token)match(input,82,FOLLOW_82_in_arrayType1653); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal185_tree = 
                    (CommonTree)adaptor.create(char_literal185)
                    ;
                    adaptor.addChild(root_0, char_literal185_tree);
                    }

                    pushFollow(FOLLOW_arraySpec_in_arrayType1655);
                    arraySpec186=arraySpec();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, arraySpec186.getTree());

                    char_literal187=(Token)match(input,83,FOLLOW_83_in_arrayType1657); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal187_tree = 
                    (CommonTree)adaptor.create(char_literal187)
                    ;
                    adaptor.addChild(root_0, char_literal187_tree);
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
    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:511:1: arraySpec : arrayRange ( ',' arrayRange )? ;
    public final JaybirdSqlParser.arraySpec_return arraySpec() throws RecognitionException {
        JaybirdSqlParser.arraySpec_return retval = new JaybirdSqlParser.arraySpec_return();
        retval.start = input.LT(1);

        int arraySpec_StartIndex = input.index();

        CommonTree root_0 = null;

        Token char_literal189=null;
        JaybirdSqlParser.arrayRange_return arrayRange188 =null;

        JaybirdSqlParser.arrayRange_return arrayRange190 =null;


        CommonTree char_literal189_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 37) ) { return retval; }

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:512:3: ( arrayRange ( ',' arrayRange )? )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:512:5: arrayRange ( ',' arrayRange )?
            {
            root_0 = (CommonTree)adaptor.nil();


            pushFollow(FOLLOW_arrayRange_in_arraySpec1672);
            arrayRange188=arrayRange();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, arrayRange188.getTree());

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:512:16: ( ',' arrayRange )?
            int alt33=2;
            int LA33_0 = input.LA(1);

            if ( (LA33_0==COMMA) ) {
                alt33=1;
            }
            switch (alt33) {
                case 1 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:512:17: ',' arrayRange
                    {
                    char_literal189=(Token)match(input,COMMA,FOLLOW_COMMA_in_arraySpec1675); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal189_tree = 
                    (CommonTree)adaptor.create(char_literal189)
                    ;
                    adaptor.addChild(root_0, char_literal189_tree);
                    }

                    pushFollow(FOLLOW_arrayRange_in_arraySpec1677);
                    arrayRange190=arrayRange();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, arrayRange190.getTree());

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
    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:515:1: arrayRange : INTEGER ( ':' INTEGER ) ;
    public final JaybirdSqlParser.arrayRange_return arrayRange() throws RecognitionException {
        JaybirdSqlParser.arrayRange_return retval = new JaybirdSqlParser.arrayRange_return();
        retval.start = input.LT(1);

        int arrayRange_StartIndex = input.index();

        CommonTree root_0 = null;

        Token INTEGER191=null;
        Token char_literal192=null;
        Token INTEGER193=null;

        CommonTree INTEGER191_tree=null;
        CommonTree char_literal192_tree=null;
        CommonTree INTEGER193_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 38) ) { return retval; }

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:516:3: ( INTEGER ( ':' INTEGER ) )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:516:5: INTEGER ( ':' INTEGER )
            {
            root_0 = (CommonTree)adaptor.nil();


            INTEGER191=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_arrayRange1694); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            INTEGER191_tree = 
            (CommonTree)adaptor.create(INTEGER191)
            ;
            adaptor.addChild(root_0, INTEGER191_tree);
            }

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:516:13: ( ':' INTEGER )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:516:14: ':' INTEGER
            {
            char_literal192=(Token)match(input,79,FOLLOW_79_in_arrayRange1697); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            char_literal192_tree = 
            (CommonTree)adaptor.create(char_literal192)
            ;
            adaptor.addChild(root_0, char_literal192_tree);
            }

            INTEGER193=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_arrayRange1699); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            INTEGER193_tree = 
            (CommonTree)adaptor.create(INTEGER193)
            ;
            adaptor.addChild(root_0, INTEGER193_tree);
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
    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:519:1: arrayElement : simpleIdentifier '[' valueList ']' ;
    public final JaybirdSqlParser.arrayElement_return arrayElement() throws RecognitionException {
        JaybirdSqlParser.arrayElement_return retval = new JaybirdSqlParser.arrayElement_return();
        retval.start = input.LT(1);

        int arrayElement_StartIndex = input.index();

        CommonTree root_0 = null;

        Token char_literal195=null;
        Token char_literal197=null;
        JaybirdSqlParser.simpleIdentifier_return simpleIdentifier194 =null;

        JaybirdSqlParser.valueList_return valueList196 =null;


        CommonTree char_literal195_tree=null;
        CommonTree char_literal197_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 39) ) { return retval; }

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:520:3: ( simpleIdentifier '[' valueList ']' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:520:5: simpleIdentifier '[' valueList ']'
            {
            root_0 = (CommonTree)adaptor.nil();


            pushFollow(FOLLOW_simpleIdentifier_in_arrayElement1715);
            simpleIdentifier194=simpleIdentifier();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleIdentifier194.getTree());

            char_literal195=(Token)match(input,82,FOLLOW_82_in_arrayElement1717); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            char_literal195_tree = 
            (CommonTree)adaptor.create(char_literal195)
            ;
            adaptor.addChild(root_0, char_literal195_tree);
            }

            pushFollow(FOLLOW_valueList_in_arrayElement1719);
            valueList196=valueList();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, valueList196.getTree());

            char_literal197=(Token)match(input,83,FOLLOW_83_in_arrayElement1721); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            char_literal197_tree = 
            (CommonTree)adaptor.create(char_literal197)
            ;
            adaptor.addChild(root_0, char_literal197_tree);
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
    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:523:1: function : ( simpleIdentifier '(' valueList ')' | simpleIdentifier '(' ')' | substringFunction | trimFunction | extractFunction | SUM '(' ( ALL | DISTINCT )? value ')' | COUNT '(' ( ALL | DISTINCT )? value ')' | AVG '(' ( ALL | DISTINCT )? value ')' | MINIMUM '(' ( ALL | DISTINCT )? value ')' | MAXIMUM '(' ( ALL | DISTINCT )? value ')' );
    public final JaybirdSqlParser.function_return function() throws RecognitionException {
        JaybirdSqlParser.function_return retval = new JaybirdSqlParser.function_return();
        retval.start = input.LT(1);

        int function_StartIndex = input.index();

        CommonTree root_0 = null;

        Token char_literal199=null;
        Token char_literal201=null;
        Token char_literal203=null;
        Token char_literal204=null;
        Token SUM208=null;
        Token char_literal209=null;
        Token set210=null;
        Token char_literal212=null;
        Token COUNT213=null;
        Token char_literal214=null;
        Token set215=null;
        Token char_literal217=null;
        Token AVG218=null;
        Token char_literal219=null;
        Token set220=null;
        Token char_literal222=null;
        Token MINIMUM223=null;
        Token char_literal224=null;
        Token set225=null;
        Token char_literal227=null;
        Token MAXIMUM228=null;
        Token char_literal229=null;
        Token set230=null;
        Token char_literal232=null;
        JaybirdSqlParser.simpleIdentifier_return simpleIdentifier198 =null;

        JaybirdSqlParser.valueList_return valueList200 =null;

        JaybirdSqlParser.simpleIdentifier_return simpleIdentifier202 =null;

        JaybirdSqlParser.substringFunction_return substringFunction205 =null;

        JaybirdSqlParser.trimFunction_return trimFunction206 =null;

        JaybirdSqlParser.extractFunction_return extractFunction207 =null;

        JaybirdSqlParser.value_return value211 =null;

        JaybirdSqlParser.value_return value216 =null;

        JaybirdSqlParser.value_return value221 =null;

        JaybirdSqlParser.value_return value226 =null;

        JaybirdSqlParser.value_return value231 =null;


        CommonTree char_literal199_tree=null;
        CommonTree char_literal201_tree=null;
        CommonTree char_literal203_tree=null;
        CommonTree char_literal204_tree=null;
        CommonTree SUM208_tree=null;
        CommonTree char_literal209_tree=null;
        CommonTree set210_tree=null;
        CommonTree char_literal212_tree=null;
        CommonTree COUNT213_tree=null;
        CommonTree char_literal214_tree=null;
        CommonTree set215_tree=null;
        CommonTree char_literal217_tree=null;
        CommonTree AVG218_tree=null;
        CommonTree char_literal219_tree=null;
        CommonTree set220_tree=null;
        CommonTree char_literal222_tree=null;
        CommonTree MINIMUM223_tree=null;
        CommonTree char_literal224_tree=null;
        CommonTree set225_tree=null;
        CommonTree char_literal227_tree=null;
        CommonTree MAXIMUM228_tree=null;
        CommonTree char_literal229_tree=null;
        CommonTree set230_tree=null;
        CommonTree char_literal232_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 40) ) { return retval; }

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:524:3: ( simpleIdentifier '(' valueList ')' | simpleIdentifier '(' ')' | substringFunction | trimFunction | extractFunction | SUM '(' ( ALL | DISTINCT )? value ')' | COUNT '(' ( ALL | DISTINCT )? value ')' | AVG '(' ( ALL | DISTINCT )? value ')' | MINIMUM '(' ( ALL | DISTINCT )? value ')' | MAXIMUM '(' ( ALL | DISTINCT )? value ')' )
            int alt39=10;
            switch ( input.LA(1) ) {
            case GENERIC_ID:
            case QUOTED_ID:
                {
                int LA39_1 = input.LA(2);

                if ( (LA39_1==LEFT_PAREN) ) {
                    int LA39_10 = input.LA(3);

                    if ( (LA39_10==RIGHT_PAREN) ) {
                        alt39=2;
                    }
                    else if ( (LA39_10==AVG||LA39_10==CAST||(LA39_10 >= COUNT && LA39_10 <= DB_KEY)||LA39_10==EXTRACT||(LA39_10 >= GENERIC_ID && LA39_10 <= GEN_ID)||LA39_10==INTEGER||LA39_10==LEFT_PAREN||(LA39_10 >= MAXIMUM && LA39_10 <= NULL)||(LA39_10 >= QUOTED_ID && LA39_10 <= REAL)||(LA39_10 >= STRING && LA39_10 <= SUBSTRING)||LA39_10==SUM||LA39_10==TRIM||(LA39_10 >= 75 && LA39_10 <= 76)||LA39_10==81) ) {
                        alt39=1;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 39, 10, input);

                        throw nvae;

                    }
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 39, 1, input);

                    throw nvae;

                }
                }
                break;
            case SUBSTRING:
                {
                alt39=3;
                }
                break;
            case TRIM:
                {
                alt39=4;
                }
                break;
            case EXTRACT:
                {
                alt39=5;
                }
                break;
            case SUM:
                {
                alt39=6;
                }
                break;
            case COUNT:
                {
                alt39=7;
                }
                break;
            case AVG:
                {
                alt39=8;
                }
                break;
            case MINIMUM:
                {
                alt39=9;
                }
                break;
            case MAXIMUM:
                {
                alt39=10;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 39, 0, input);

                throw nvae;

            }

            switch (alt39) {
                case 1 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:524:5: simpleIdentifier '(' valueList ')'
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_simpleIdentifier_in_function1734);
                    simpleIdentifier198=simpleIdentifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleIdentifier198.getTree());

                    char_literal199=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_function1736); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal199_tree = 
                    (CommonTree)adaptor.create(char_literal199)
                    ;
                    adaptor.addChild(root_0, char_literal199_tree);
                    }

                    pushFollow(FOLLOW_valueList_in_function1738);
                    valueList200=valueList();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, valueList200.getTree());

                    char_literal201=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_function1740); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal201_tree = 
                    (CommonTree)adaptor.create(char_literal201)
                    ;
                    adaptor.addChild(root_0, char_literal201_tree);
                    }

                    }
                    break;
                case 2 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:525:5: simpleIdentifier '(' ')'
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_simpleIdentifier_in_function1746);
                    simpleIdentifier202=simpleIdentifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleIdentifier202.getTree());

                    char_literal203=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_function1748); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal203_tree = 
                    (CommonTree)adaptor.create(char_literal203)
                    ;
                    adaptor.addChild(root_0, char_literal203_tree);
                    }

                    char_literal204=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_function1750); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal204_tree = 
                    (CommonTree)adaptor.create(char_literal204)
                    ;
                    adaptor.addChild(root_0, char_literal204_tree);
                    }

                    }
                    break;
                case 3 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:526:5: substringFunction
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_substringFunction_in_function1756);
                    substringFunction205=substringFunction();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, substringFunction205.getTree());

                    }
                    break;
                case 4 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:527:5: trimFunction
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_trimFunction_in_function1762);
                    trimFunction206=trimFunction();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, trimFunction206.getTree());

                    }
                    break;
                case 5 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:528:5: extractFunction
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_extractFunction_in_function1768);
                    extractFunction207=extractFunction();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, extractFunction207.getTree());

                    }
                    break;
                case 6 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:529:5: SUM '(' ( ALL | DISTINCT )? value ')'
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    SUM208=(Token)match(input,SUM,FOLLOW_SUM_in_function1774); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    SUM208_tree = 
                    (CommonTree)adaptor.create(SUM208)
                    ;
                    adaptor.addChild(root_0, SUM208_tree);
                    }

                    char_literal209=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_function1777); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal209_tree = 
                    (CommonTree)adaptor.create(char_literal209)
                    ;
                    adaptor.addChild(root_0, char_literal209_tree);
                    }

                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:529:14: ( ALL | DISTINCT )?
                    int alt34=2;
                    int LA34_0 = input.LA(1);

                    if ( (LA34_0==ALL||LA34_0==DISTINCT) ) {
                        alt34=1;
                    }
                    switch (alt34) {
                        case 1 :
                            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:
                            {
                            set210=(Token)input.LT(1);

                            if ( input.LA(1)==ALL||input.LA(1)==DISTINCT ) {
                                input.consume();
                                if ( state.backtracking==0 ) adaptor.addChild(root_0, 
                                (CommonTree)adaptor.create(set210)
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


                    pushFollow(FOLLOW_value_in_function1786);
                    value211=value();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, value211.getTree());

                    char_literal212=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_function1788); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal212_tree = 
                    (CommonTree)adaptor.create(char_literal212)
                    ;
                    adaptor.addChild(root_0, char_literal212_tree);
                    }

                    }
                    break;
                case 7 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:530:5: COUNT '(' ( ALL | DISTINCT )? value ')'
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    COUNT213=(Token)match(input,COUNT,FOLLOW_COUNT_in_function1794); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COUNT213_tree = 
                    (CommonTree)adaptor.create(COUNT213)
                    ;
                    adaptor.addChild(root_0, COUNT213_tree);
                    }

                    char_literal214=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_function1797); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal214_tree = 
                    (CommonTree)adaptor.create(char_literal214)
                    ;
                    adaptor.addChild(root_0, char_literal214_tree);
                    }

                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:530:16: ( ALL | DISTINCT )?
                    int alt35=2;
                    int LA35_0 = input.LA(1);

                    if ( (LA35_0==ALL||LA35_0==DISTINCT) ) {
                        alt35=1;
                    }
                    switch (alt35) {
                        case 1 :
                            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:
                            {
                            set215=(Token)input.LT(1);

                            if ( input.LA(1)==ALL||input.LA(1)==DISTINCT ) {
                                input.consume();
                                if ( state.backtracking==0 ) adaptor.addChild(root_0, 
                                (CommonTree)adaptor.create(set215)
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


                    pushFollow(FOLLOW_value_in_function1806);
                    value216=value();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, value216.getTree());

                    char_literal217=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_function1808); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal217_tree = 
                    (CommonTree)adaptor.create(char_literal217)
                    ;
                    adaptor.addChild(root_0, char_literal217_tree);
                    }

                    }
                    break;
                case 8 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:531:5: AVG '(' ( ALL | DISTINCT )? value ')'
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    AVG218=(Token)match(input,AVG,FOLLOW_AVG_in_function1814); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    AVG218_tree = 
                    (CommonTree)adaptor.create(AVG218)
                    ;
                    adaptor.addChild(root_0, AVG218_tree);
                    }

                    char_literal219=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_function1817); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal219_tree = 
                    (CommonTree)adaptor.create(char_literal219)
                    ;
                    adaptor.addChild(root_0, char_literal219_tree);
                    }

                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:531:14: ( ALL | DISTINCT )?
                    int alt36=2;
                    int LA36_0 = input.LA(1);

                    if ( (LA36_0==ALL||LA36_0==DISTINCT) ) {
                        alt36=1;
                    }
                    switch (alt36) {
                        case 1 :
                            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:
                            {
                            set220=(Token)input.LT(1);

                            if ( input.LA(1)==ALL||input.LA(1)==DISTINCT ) {
                                input.consume();
                                if ( state.backtracking==0 ) adaptor.addChild(root_0, 
                                (CommonTree)adaptor.create(set220)
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


                    pushFollow(FOLLOW_value_in_function1826);
                    value221=value();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, value221.getTree());

                    char_literal222=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_function1828); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal222_tree = 
                    (CommonTree)adaptor.create(char_literal222)
                    ;
                    adaptor.addChild(root_0, char_literal222_tree);
                    }

                    }
                    break;
                case 9 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:532:5: MINIMUM '(' ( ALL | DISTINCT )? value ')'
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    MINIMUM223=(Token)match(input,MINIMUM,FOLLOW_MINIMUM_in_function1834); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    MINIMUM223_tree = 
                    (CommonTree)adaptor.create(MINIMUM223)
                    ;
                    adaptor.addChild(root_0, MINIMUM223_tree);
                    }

                    char_literal224=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_function1836); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal224_tree = 
                    (CommonTree)adaptor.create(char_literal224)
                    ;
                    adaptor.addChild(root_0, char_literal224_tree);
                    }

                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:532:17: ( ALL | DISTINCT )?
                    int alt37=2;
                    int LA37_0 = input.LA(1);

                    if ( (LA37_0==ALL||LA37_0==DISTINCT) ) {
                        alt37=1;
                    }
                    switch (alt37) {
                        case 1 :
                            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:
                            {
                            set225=(Token)input.LT(1);

                            if ( input.LA(1)==ALL||input.LA(1)==DISTINCT ) {
                                input.consume();
                                if ( state.backtracking==0 ) adaptor.addChild(root_0, 
                                (CommonTree)adaptor.create(set225)
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


                    pushFollow(FOLLOW_value_in_function1845);
                    value226=value();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, value226.getTree());

                    char_literal227=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_function1847); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal227_tree = 
                    (CommonTree)adaptor.create(char_literal227)
                    ;
                    adaptor.addChild(root_0, char_literal227_tree);
                    }

                    }
                    break;
                case 10 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:533:5: MAXIMUM '(' ( ALL | DISTINCT )? value ')'
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    MAXIMUM228=(Token)match(input,MAXIMUM,FOLLOW_MAXIMUM_in_function1853); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    MAXIMUM228_tree = 
                    (CommonTree)adaptor.create(MAXIMUM228)
                    ;
                    adaptor.addChild(root_0, MAXIMUM228_tree);
                    }

                    char_literal229=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_function1855); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal229_tree = 
                    (CommonTree)adaptor.create(char_literal229)
                    ;
                    adaptor.addChild(root_0, char_literal229_tree);
                    }

                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:533:17: ( ALL | DISTINCT )?
                    int alt38=2;
                    int LA38_0 = input.LA(1);

                    if ( (LA38_0==ALL||LA38_0==DISTINCT) ) {
                        alt38=1;
                    }
                    switch (alt38) {
                        case 1 :
                            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:
                            {
                            set230=(Token)input.LT(1);

                            if ( input.LA(1)==ALL||input.LA(1)==DISTINCT ) {
                                input.consume();
                                if ( state.backtracking==0 ) adaptor.addChild(root_0, 
                                (CommonTree)adaptor.create(set230)
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


                    pushFollow(FOLLOW_value_in_function1864);
                    value231=value();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, value231.getTree());

                    char_literal232=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_function1866); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal232_tree = 
                    (CommonTree)adaptor.create(char_literal232)
                    ;
                    adaptor.addChild(root_0, char_literal232_tree);
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
    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:536:1: substringFunction : SUBSTRING '(' value FROM value ( FOR INTEGER )? ')' ;
    public final JaybirdSqlParser.substringFunction_return substringFunction() throws RecognitionException {
        JaybirdSqlParser.substringFunction_return retval = new JaybirdSqlParser.substringFunction_return();
        retval.start = input.LT(1);

        int substringFunction_StartIndex = input.index();

        CommonTree root_0 = null;

        Token SUBSTRING233=null;
        Token char_literal234=null;
        Token FROM236=null;
        Token FOR238=null;
        Token INTEGER239=null;
        Token char_literal240=null;
        JaybirdSqlParser.value_return value235 =null;

        JaybirdSqlParser.value_return value237 =null;


        CommonTree SUBSTRING233_tree=null;
        CommonTree char_literal234_tree=null;
        CommonTree FROM236_tree=null;
        CommonTree FOR238_tree=null;
        CommonTree INTEGER239_tree=null;
        CommonTree char_literal240_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 41) ) { return retval; }

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:537:3: ( SUBSTRING '(' value FROM value ( FOR INTEGER )? ')' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:537:5: SUBSTRING '(' value FROM value ( FOR INTEGER )? ')'
            {
            root_0 = (CommonTree)adaptor.nil();


            SUBSTRING233=(Token)match(input,SUBSTRING,FOLLOW_SUBSTRING_in_substringFunction1883); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            SUBSTRING233_tree = 
            (CommonTree)adaptor.create(SUBSTRING233)
            ;
            adaptor.addChild(root_0, SUBSTRING233_tree);
            }

            char_literal234=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_substringFunction1885); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            char_literal234_tree = 
            (CommonTree)adaptor.create(char_literal234)
            ;
            adaptor.addChild(root_0, char_literal234_tree);
            }

            pushFollow(FOLLOW_value_in_substringFunction1887);
            value235=value();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, value235.getTree());

            FROM236=(Token)match(input,FROM,FOLLOW_FROM_in_substringFunction1889); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            FROM236_tree = 
            (CommonTree)adaptor.create(FROM236)
            ;
            adaptor.addChild(root_0, FROM236_tree);
            }

            pushFollow(FOLLOW_value_in_substringFunction1891);
            value237=value();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, value237.getTree());

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:537:36: ( FOR INTEGER )?
            int alt40=2;
            int LA40_0 = input.LA(1);

            if ( (LA40_0==FOR) ) {
                alt40=1;
            }
            switch (alt40) {
                case 1 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:537:37: FOR INTEGER
                    {
                    FOR238=(Token)match(input,FOR,FOLLOW_FOR_in_substringFunction1894); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    FOR238_tree = 
                    (CommonTree)adaptor.create(FOR238)
                    ;
                    adaptor.addChild(root_0, FOR238_tree);
                    }

                    INTEGER239=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_substringFunction1896); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    INTEGER239_tree = 
                    (CommonTree)adaptor.create(INTEGER239)
                    ;
                    adaptor.addChild(root_0, INTEGER239_tree);
                    }

                    }
                    break;

            }


            char_literal240=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_substringFunction1900); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            char_literal240_tree = 
            (CommonTree)adaptor.create(char_literal240)
            ;
            adaptor.addChild(root_0, char_literal240_tree);
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
    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:540:1: trimFunction : TRIM '(' ( trimSpecification )? value ( FROM value )? ')' ;
    public final JaybirdSqlParser.trimFunction_return trimFunction() throws RecognitionException {
        JaybirdSqlParser.trimFunction_return retval = new JaybirdSqlParser.trimFunction_return();
        retval.start = input.LT(1);

        int trimFunction_StartIndex = input.index();

        CommonTree root_0 = null;

        Token TRIM241=null;
        Token char_literal242=null;
        Token FROM245=null;
        Token char_literal247=null;
        JaybirdSqlParser.trimSpecification_return trimSpecification243 =null;

        JaybirdSqlParser.value_return value244 =null;

        JaybirdSqlParser.value_return value246 =null;


        CommonTree TRIM241_tree=null;
        CommonTree char_literal242_tree=null;
        CommonTree FROM245_tree=null;
        CommonTree char_literal247_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 42) ) { return retval; }

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:541:3: ( TRIM '(' ( trimSpecification )? value ( FROM value )? ')' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:541:5: TRIM '(' ( trimSpecification )? value ( FROM value )? ')'
            {
            root_0 = (CommonTree)adaptor.nil();


            TRIM241=(Token)match(input,TRIM,FOLLOW_TRIM_in_trimFunction1915); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            TRIM241_tree = 
            (CommonTree)adaptor.create(TRIM241)
            ;
            adaptor.addChild(root_0, TRIM241_tree);
            }

            char_literal242=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_trimFunction1917); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            char_literal242_tree = 
            (CommonTree)adaptor.create(char_literal242)
            ;
            adaptor.addChild(root_0, char_literal242_tree);
            }

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:541:14: ( trimSpecification )?
            int alt41=2;
            int LA41_0 = input.LA(1);

            if ( (LA41_0==BOTH||LA41_0==LEADING||LA41_0==TRAILING) ) {
                alt41=1;
            }
            switch (alt41) {
                case 1 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:541:15: trimSpecification
                    {
                    pushFollow(FOLLOW_trimSpecification_in_trimFunction1920);
                    trimSpecification243=trimSpecification();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, trimSpecification243.getTree());

                    }
                    break;

            }


            pushFollow(FOLLOW_value_in_trimFunction1924);
            value244=value();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, value244.getTree());

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:541:41: ( FROM value )?
            int alt42=2;
            int LA42_0 = input.LA(1);

            if ( (LA42_0==FROM) ) {
                alt42=1;
            }
            switch (alt42) {
                case 1 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:541:42: FROM value
                    {
                    FROM245=(Token)match(input,FROM,FOLLOW_FROM_in_trimFunction1927); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    FROM245_tree = 
                    (CommonTree)adaptor.create(FROM245)
                    ;
                    adaptor.addChild(root_0, FROM245_tree);
                    }

                    pushFollow(FOLLOW_value_in_trimFunction1929);
                    value246=value();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, value246.getTree());

                    }
                    break;

            }


            char_literal247=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_trimFunction1933); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            char_literal247_tree = 
            (CommonTree)adaptor.create(char_literal247)
            ;
            adaptor.addChild(root_0, char_literal247_tree);
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
    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:544:1: extractFunction : EXTRACT '(' value FROM value ')' ;
    public final JaybirdSqlParser.extractFunction_return extractFunction() throws RecognitionException {
        JaybirdSqlParser.extractFunction_return retval = new JaybirdSqlParser.extractFunction_return();
        retval.start = input.LT(1);

        int extractFunction_StartIndex = input.index();

        CommonTree root_0 = null;

        Token EXTRACT248=null;
        Token char_literal249=null;
        Token FROM251=null;
        Token char_literal253=null;
        JaybirdSqlParser.value_return value250 =null;

        JaybirdSqlParser.value_return value252 =null;


        CommonTree EXTRACT248_tree=null;
        CommonTree char_literal249_tree=null;
        CommonTree FROM251_tree=null;
        CommonTree char_literal253_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 43) ) { return retval; }

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:545:3: ( EXTRACT '(' value FROM value ')' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:545:5: EXTRACT '(' value FROM value ')'
            {
            root_0 = (CommonTree)adaptor.nil();


            EXTRACT248=(Token)match(input,EXTRACT,FOLLOW_EXTRACT_in_extractFunction1948); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            EXTRACT248_tree = 
            (CommonTree)adaptor.create(EXTRACT248)
            ;
            adaptor.addChild(root_0, EXTRACT248_tree);
            }

            char_literal249=(Token)match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_extractFunction1950); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            char_literal249_tree = 
            (CommonTree)adaptor.create(char_literal249)
            ;
            adaptor.addChild(root_0, char_literal249_tree);
            }

            pushFollow(FOLLOW_value_in_extractFunction1952);
            value250=value();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, value250.getTree());

            FROM251=(Token)match(input,FROM,FOLLOW_FROM_in_extractFunction1954); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            FROM251_tree = 
            (CommonTree)adaptor.create(FROM251)
            ;
            adaptor.addChild(root_0, FROM251_tree);
            }

            pushFollow(FOLLOW_value_in_extractFunction1956);
            value252=value();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, value252.getTree());

            char_literal253=(Token)match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_extractFunction1958); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            char_literal253_tree = 
            (CommonTree)adaptor.create(char_literal253)
            ;
            adaptor.addChild(root_0, char_literal253_tree);
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
    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:548:1: trimSpecification : ( BOTH | TRAILING | LEADING );
    public final JaybirdSqlParser.trimSpecification_return trimSpecification() throws RecognitionException {
        JaybirdSqlParser.trimSpecification_return retval = new JaybirdSqlParser.trimSpecification_return();
        retval.start = input.LT(1);

        int trimSpecification_StartIndex = input.index();

        CommonTree root_0 = null;

        Token set254=null;

        CommonTree set254_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 44) ) { return retval; }

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:549:3: ( BOTH | TRAILING | LEADING )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:
            {
            root_0 = (CommonTree)adaptor.nil();


            set254=(Token)input.LT(1);

            if ( input.LA(1)==BOTH||input.LA(1)==LEADING||input.LA(1)==TRAILING ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, 
                (CommonTree)adaptor.create(set254)
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
    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:554:1: selectClause : SELECT ;
    public final JaybirdSqlParser.selectClause_return selectClause() throws RecognitionException {
        JaybirdSqlParser.selectClause_return retval = new JaybirdSqlParser.selectClause_return();
        retval.start = input.LT(1);

        int selectClause_StartIndex = input.index();

        CommonTree root_0 = null;

        Token SELECT255=null;

        CommonTree SELECT255_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 45) ) { return retval; }

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:555:3: ( SELECT )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:555:5: SELECT
            {
            root_0 = (CommonTree)adaptor.nil();


            SELECT255=(Token)match(input,SELECT,FOLLOW_SELECT_in_selectClause2002); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            SELECT255_tree = 
            (CommonTree)adaptor.create(SELECT255)
            ;
            adaptor.addChild(root_0, SELECT255_tree);
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


 

    public static final BitSet FOLLOW_insertStatement_in_statement474 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_deleteStatement_in_statement480 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_updateStatement_in_statement486 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_updateOrInsertStatement_in_statement493 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DELETE_in_deleteStatement510 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_FROM_in_deleteStatement512 = new BitSet(new long[]{0x0100000004000000L});
    public static final BitSet FOLLOW_tableName_in_deleteStatement514 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_UPDATE_in_updateStatement538 = new BitSet(new long[]{0x0100000004000000L});
    public static final BitSet FOLLOW_tableName_in_updateStatement540 = new BitSet(new long[]{0x4000000000000000L});
    public static final BitSet FOLLOW_SET_in_updateStatement542 = new BitSet(new long[]{0x0100000004000000L});
    public static final BitSet FOLLOW_assignments_in_updateStatement544 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignment_in_assignments562 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_COMMA_in_assignments565 = new BitSet(new long[]{0x0100000004000000L});
    public static final BitSet FOLLOW_assignment_in_assignments567 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_columnName_in_assignment582 = new BitSet(new long[]{0x0000000000000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_80_in_assignment584 = new BitSet(new long[]{0x033D00002C87F140L,0x000000000002182BL});
    public static final BitSet FOLLOW_value_in_assignment586 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_UPDATE_in_updateOrInsertStatement603 = new BitSet(new long[]{0x0040000000000000L});
    public static final BitSet FOLLOW_OR_in_updateOrInsertStatement605 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_INSERT_in_updateOrInsertStatement607 = new BitSet(new long[]{0x0000000040000000L});
    public static final BitSet FOLLOW_INTO_in_updateOrInsertStatement609 = new BitSet(new long[]{0x0100000004000000L});
    public static final BitSet FOLLOW_tableName_in_updateOrInsertStatement611 = new BitSet(new long[]{0x0001000000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_insertColumns_in_updateOrInsertStatement613 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_insertValues_in_updateOrInsertStatement621 = new BitSet(new long[]{0x0402000000000002L});
    public static final BitSet FOLLOW_matchingClause_in_updateOrInsertStatement623 = new BitSet(new long[]{0x0400000000000002L});
    public static final BitSet FOLLOW_returningClause_in_updateOrInsertStatement626 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MATCHING_in_matchingClause645 = new BitSet(new long[]{0x0100000004000000L});
    public static final BitSet FOLLOW_columnList_in_matchingClause647 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INSERT_in_insertStatement664 = new BitSet(new long[]{0x0000000040000000L});
    public static final BitSet FOLLOW_INTO_in_insertStatement666 = new BitSet(new long[]{0x0100000004000000L});
    public static final BitSet FOLLOW_tableName_in_insertStatement668 = new BitSet(new long[]{0x2001000000080000L,0x0000000000000100L});
    public static final BitSet FOLLOW_insertColumns_in_insertStatement670 = new BitSet(new long[]{0x2000000000080000L,0x0000000000000100L});
    public static final BitSet FOLLOW_insertValues_in_insertStatement680 = new BitSet(new long[]{0x0400000000000002L});
    public static final BitSet FOLLOW_returningClause_in_insertStatement682 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selectClause_in_insertStatement692 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_defaultValuesClause_in_insertStatement701 = new BitSet(new long[]{0x0400000000000002L});
    public static final BitSet FOLLOW_returningClause_in_insertStatement703 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_insertColumns730 = new BitSet(new long[]{0x0100000004000000L});
    public static final BitSet FOLLOW_columnList_in_insertColumns732 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_insertColumns734 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VALUES_in_insertValues753 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_insertValues755 = new BitSet(new long[]{0x033D00002C87F140L,0x000000000002182BL});
    public static final BitSet FOLLOW_valueList_in_insertValues757 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_insertValues759 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RETURNING_in_returningClause774 = new BitSet(new long[]{0x0100000004000000L});
    public static final BitSet FOLLOW_columnList_in_returningClause778 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DEFAULT_in_defaultValuesClause793 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_VALUES_in_defaultValuesClause795 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simpleIdentifier_in_fullIdentifier835 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_77_in_fullIdentifier837 = new BitSet(new long[]{0x0100000004000000L});
    public static final BitSet FOLLOW_simpleIdentifier_in_fullIdentifier839 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simpleIdentifier_in_tableName858 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnName_in_columnList879 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_COMMA_in_columnList882 = new BitSet(new long[]{0x0100000004000000L});
    public static final BitSet FOLLOW_columnName_in_columnList884 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_simpleIdentifier_in_columnName903 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fullIdentifier_in_columnName923 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_value_in_valueList942 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_COMMA_in_valueList945 = new BitSet(new long[]{0x033D00002C87F140L,0x000000000002182BL});
    public static final BitSet FOLLOW_value_in_valueList947 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_simpleValue_in_value965 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simpleValue_in_value971 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_75_in_value973 = new BitSet(new long[]{0x0200000024000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_simpleValue_in_value975 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simpleValue_in_value981 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001000L});
    public static final BitSet FOLLOW_76_in_value983 = new BitSet(new long[]{0x0200000024000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_simpleValue_in_value985 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simpleValue_in_value991 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_74_in_value993 = new BitSet(new long[]{0x0200000024000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_simpleValue_in_value995 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simpleValue_in_value1001 = new BitSet(new long[]{0x0000000000000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_78_in_value1003 = new BitSet(new long[]{0x0200000024000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_simpleValue_in_value1005 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simpleValue_in_value1011 = new BitSet(new long[]{0x0000000000000000L,0x0000000000100000L});
    public static final BitSet FOLLOW_84_in_value1013 = new BitSet(new long[]{0x0200000024000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_simpleValue_in_value1015 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_75_in_value1021 = new BitSet(new long[]{0x0200000024000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_simpleValue_in_value1023 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_76_in_value1029 = new BitSet(new long[]{0x0200000024000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_simpleValue_in_value1031 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_value1040 = new BitSet(new long[]{0x0200000024000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_simpleValue_in_value1042 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_value1044 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simpleValue_in_value1053 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_COLLATE_in_value1055 = new BitSet(new long[]{0x0100000004000000L});
    public static final BitSet FOLLOW_simpleIdentifier_in_value1057 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_parameter_in_value1065 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CURRENT_USER_in_value1074 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CURRENT_ROLE_in_value1080 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CURRENT_DATE_in_value1086 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CURRENT_TIME_in_value1092 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CURRENT_TIMESTAMP_in_value1098 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nullValue_in_value1107 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_function_in_value1116 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nextValueExpression_in_value1122 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_castExpression_in_value1128 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_arrayElement_in_value1138 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DB_KEY_in_value1147 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simpleIdentifier_in_value1153 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_77_in_value1155 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_DB_KEY_in_value1157 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_81_in_parameter1171 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NULL_in_nullValue1184 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NEXT_in_nextValueExpression1233 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L});
    public static final BitSet FOLLOW_VALUE_in_nextValueExpression1235 = new BitSet(new long[]{0x0000000001000000L});
    public static final BitSet FOLLOW_FOR_in_nextValueExpression1237 = new BitSet(new long[]{0x0100000004000000L});
    public static final BitSet FOLLOW_simpleIdentifier_in_nextValueExpression1239 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GEN_ID_in_nextValueExpression1245 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_nextValueExpression1247 = new BitSet(new long[]{0x0100000004000000L});
    public static final BitSet FOLLOW_simpleIdentifier_in_nextValueExpression1249 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_COMMA_in_nextValueExpression1251 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INTEGER_in_nextValueExpression1253 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_nextValueExpression1255 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CAST_in_castExpression1270 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_castExpression1272 = new BitSet(new long[]{0x033D00002C87F140L,0x000000000002182BL});
    public static final BitSet FOLLOW_value_in_castExpression1274 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_AS_in_castExpression1276 = new BitSet(new long[]{0x000079FF80000000L});
    public static final BitSet FOLLOW_dataTypeDescriptor_in_castExpression1278 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_castExpression1280 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nonArrayType_in_dataTypeDescriptor1295 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_arrayType_in_dataTypeDescriptor1301 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simpleType_in_nonArrayType1316 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_blobType_in_nonArrayType1322 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nonCharType_in_simpleType1335 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_charType_in_simpleType1341 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nonCharSetCharType_in_charType1356 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_charSetCharType_in_charType1362 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KW_CHAR_in_nonCharSetCharType1375 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_nonCharSetCharType1378 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INTEGER_in_nonCharSetCharType1380 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_nonCharSetCharType1382 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KW_VARCHAR_in_nonCharSetCharType1390 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_nonCharSetCharType1392 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INTEGER_in_nonCharSetCharType1394 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_nonCharSetCharType1396 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nonCharSetCharType_in_charSetCharType1409 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_charSetClause_in_charSetCharType1411 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KW_BIGINT_in_nonCharType1424 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KW_DATE_in_nonCharType1430 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KW_DECIMAL_in_nonCharType1436 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_nonCharType1438 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INTEGER_in_nonCharType1440 = new BitSet(new long[]{0x0800000000000800L});
    public static final BitSet FOLLOW_COMMA_in_nonCharType1443 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INTEGER_in_nonCharType1445 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_nonCharType1449 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KW_DOUBLE_in_nonCharType1455 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_KW_PRECISION_in_nonCharType1457 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KW_FLOAT_in_nonCharType1463 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KW_INTEGER_in_nonCharType1469 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KW_INT_in_nonCharType1475 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KW_NUMERIC_in_nonCharType1481 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_nonCharType1483 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INTEGER_in_nonCharType1485 = new BitSet(new long[]{0x0800000000000800L});
    public static final BitSet FOLLOW_COMMA_in_nonCharType1488 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INTEGER_in_nonCharType1490 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_nonCharType1494 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KW_SMALLINT_in_nonCharType1500 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KW_TIME_in_nonCharType1506 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KW_TIMESTAMP_in_nonCharType1512 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KW_BLOB_in_blobType1526 = new BitSet(new long[]{0x1000000000000202L,0x0000000000000004L});
    public static final BitSet FOLLOW_blobSubtype_in_blobType1528 = new BitSet(new long[]{0x1000000000000202L});
    public static final BitSet FOLLOW_blobSegSize_in_blobType1531 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_charSetClause_in_blobType1534 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KW_BLOB_in_blobType1546 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_blobType1548 = new BitSet(new long[]{0x0800000020000800L});
    public static final BitSet FOLLOW_INTEGER_in_blobType1550 = new BitSet(new long[]{0x0800000000000800L});
    public static final BitSet FOLLOW_COMMA_in_blobType1554 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INTEGER_in_blobType1556 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_blobType1560 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SUB_TYPE_in_blobSubtype1575 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INTEGER_in_blobSubtype1577 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SUB_TYPE_in_blobSubtype1583 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_GENERIC_ID_in_blobSubtype1585 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SEGMENT_in_blobSegSize1600 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_KW_SIZE_in_blobSegSize1602 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INTEGER_in_blobSegSize1604 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CHARACTER_in_charSetClause1619 = new BitSet(new long[]{0x4000000000000000L});
    public static final BitSet FOLLOW_SET_in_charSetClause1621 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_GENERIC_ID_in_charSetClause1623 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nonCharSetCharType_in_arrayType1636 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_82_in_arrayType1638 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_arraySpec_in_arrayType1640 = new BitSet(new long[]{0x0000000000000000L,0x0000000000080000L});
    public static final BitSet FOLLOW_83_in_arrayType1642 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_charSetClause_in_arrayType1644 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nonCharType_in_arrayType1651 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_82_in_arrayType1653 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_arraySpec_in_arrayType1655 = new BitSet(new long[]{0x0000000000000000L,0x0000000000080000L});
    public static final BitSet FOLLOW_83_in_arrayType1657 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_arrayRange_in_arraySpec1672 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_COMMA_in_arraySpec1675 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_arrayRange_in_arraySpec1677 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INTEGER_in_arrayRange1694 = new BitSet(new long[]{0x0000000000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_79_in_arrayRange1697 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INTEGER_in_arrayRange1699 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simpleIdentifier_in_arrayElement1715 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_82_in_arrayElement1717 = new BitSet(new long[]{0x033D00002C87F140L,0x000000000002182BL});
    public static final BitSet FOLLOW_valueList_in_arrayElement1719 = new BitSet(new long[]{0x0000000000000000L,0x0000000000080000L});
    public static final BitSet FOLLOW_83_in_arrayElement1721 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simpleIdentifier_in_function1734 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_function1736 = new BitSet(new long[]{0x033D00002C87F140L,0x000000000002182BL});
    public static final BitSet FOLLOW_valueList_in_function1738 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_function1740 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simpleIdentifier_in_function1746 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_function1748 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_function1750 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_substringFunction_in_function1756 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_trimFunction_in_function1762 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_extractFunction_in_function1768 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SUM_in_function1774 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_function1777 = new BitSet(new long[]{0x033D00002CA7F150L,0x000000000002182BL});
    public static final BitSet FOLLOW_value_in_function1786 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_function1788 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COUNT_in_function1794 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_function1797 = new BitSet(new long[]{0x033D00002CA7F150L,0x000000000002182BL});
    public static final BitSet FOLLOW_value_in_function1806 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_function1808 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AVG_in_function1814 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_function1817 = new BitSet(new long[]{0x033D00002CA7F150L,0x000000000002182BL});
    public static final BitSet FOLLOW_value_in_function1826 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_function1828 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINIMUM_in_function1834 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_function1836 = new BitSet(new long[]{0x033D00002CA7F150L,0x000000000002182BL});
    public static final BitSet FOLLOW_value_in_function1845 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_function1847 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MAXIMUM_in_function1853 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_function1855 = new BitSet(new long[]{0x033D00002CA7F150L,0x000000000002182BL});
    public static final BitSet FOLLOW_value_in_function1864 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_function1866 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SUBSTRING_in_substringFunction1883 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_substringFunction1885 = new BitSet(new long[]{0x033D00002C87F140L,0x000000000002182BL});
    public static final BitSet FOLLOW_value_in_substringFunction1887 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_FROM_in_substringFunction1889 = new BitSet(new long[]{0x033D00002C87F140L,0x000000000002182BL});
    public static final BitSet FOLLOW_value_in_substringFunction1891 = new BitSet(new long[]{0x0800000001000000L});
    public static final BitSet FOLLOW_FOR_in_substringFunction1894 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INTEGER_in_substringFunction1896 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_substringFunction1900 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRIM_in_trimFunction1915 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_trimFunction1917 = new BitSet(new long[]{0x033D80002C87F1C0L,0x000000000002183BL});
    public static final BitSet FOLLOW_trimSpecification_in_trimFunction1920 = new BitSet(new long[]{0x033D00002C87F140L,0x000000000002182BL});
    public static final BitSet FOLLOW_value_in_trimFunction1924 = new BitSet(new long[]{0x0800000002000000L});
    public static final BitSet FOLLOW_FROM_in_trimFunction1927 = new BitSet(new long[]{0x033D00002C87F140L,0x000000000002182BL});
    public static final BitSet FOLLOW_value_in_trimFunction1929 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_trimFunction1933 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EXTRACT_in_extractFunction1948 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_extractFunction1950 = new BitSet(new long[]{0x033D00002C87F140L,0x000000000002182BL});
    public static final BitSet FOLLOW_value_in_extractFunction1952 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_FROM_in_extractFunction1954 = new BitSet(new long[]{0x033D00002C87F140L,0x000000000002182BL});
    public static final BitSet FOLLOW_value_in_extractFunction1956 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_extractFunction1958 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SELECT_in_selectClause2002 = new BitSet(new long[]{0x0000000000000002L});

}