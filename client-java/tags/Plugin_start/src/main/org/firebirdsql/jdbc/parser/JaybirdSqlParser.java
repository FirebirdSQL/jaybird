// $ANTLR 3.0.1 D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3 2008-06-20 22:29:25

package org.firebirdsql.jdbc.parser;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.antlr.runtime.tree.*;

public class JaybirdSqlParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "ALL", "AS", "AVG", "BOTH", "CAST", "CHARACTER", "COUNT", "COLLATE", "CURRENT_USER", "CURRENT_ROLE", "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "DEFAULT", "DELETE", "DISTINCT", "DB_KEY", "EXTRACT", "EXECUTE", "FOR", "FROM", "GEN_ID", "INSERT", "INTO", "LEADING", "MATCHING", "MINIMUM", "MAXIMUM", "NULL", "NEXT", "OR", "PROCEDURE", "RETURNING", "SEGMENT", "SELECT", "SET", "SUBSTRING", "SUB_TYPE", "SUM", "TRIM", "TRAILING", "UPDATE", "VALUE", "VALUES", "KW_BLOB", "KW_BIGINT", "KW_CHAR", "KW_DATE", "KW_DECIMAL", "KW_DOUBLE", "KW_PRECISION", "KW_FLOAT", "KW_INTEGER", "KW_INT", "KW_NUMERIC", "KW_SMALLINT", "KW_TIME", "KW_TIMESTAMP", "KW_VARCHAR", "KW_SET", "KW_SIZE", "GENERIC_ID", "QUOTED_ID", "LEFT_PAREN", "RIGHT_PAREN", "STRING", "INTEGER", "REAL", "COMMA", "LETTER", "ESCqd", "WS", "SL_COMMENT", "ESCqs", "'='", "'.'", "'+'", "'-'", "'*'", "'/'", "'||'", "'?'", "'['", "']'", "':'"
    };
    public static final int CAST=8;
    public static final int KW_TIMESTAMP=61;
    public static final int KW_CHAR=50;
    public static final int CURRENT_USER=12;
    public static final int LETTER=73;
    public static final int KW_SIZE=64;
    public static final int EXECUTE=22;
    public static final int CURRENT_TIME=15;
    public static final int SUB_TYPE=41;
    public static final int UPDATE=45;
    public static final int FOR=23;
    public static final int KW_FLOAT=55;
    public static final int COUNT=10;
    public static final int SUM=42;
    public static final int EOF=-1;
    public static final int QUOTED_ID=66;
    public static final int CHARACTER=9;
    public static final int KW_BIGINT=49;
    public static final int ESCqs=77;
    public static final int AS=5;
    public static final int RIGHT_PAREN=68;
    public static final int KW_PRECISION=54;
    public static final int KW_TIME=60;
    public static final int INSERT=26;
    public static final int COMMA=72;
    public static final int KW_NUMERIC=58;
    public static final int MINIMUM=30;
    public static final int RETURNING=36;
    public static final int AVG=6;
    public static final int ALL=4;
    public static final int KW_SET=63;
    public static final int BOTH=7;
    public static final int NEXT=33;
    public static final int EXTRACT=21;
    public static final int CURRENT_TIMESTAMP=16;
    public static final int SELECT=38;
    public static final int INTO=27;
    public static final int SEGMENT=37;
    public static final int ESCqd=74;
    public static final int COLLATE=11;
    public static final int INTEGER=70;
    public static final int KW_VARCHAR=62;
    public static final int KW_BLOB=48;
    public static final int KW_SMALLINT=59;
    public static final int KW_INT=57;
    public static final int KW_DECIMAL=52;
    public static final int NULL=32;
    public static final int DEFAULT=17;
    public static final int VALUES=47;
    public static final int TRAILING=44;
    public static final int DB_KEY=20;
    public static final int SET=39;
    public static final int DELETE=18;
    public static final int GEN_ID=25;
    public static final int VALUE=46;
    public static final int KW_DOUBLE=53;
    public static final int PROCEDURE=35;
    public static final int LEADING=28;
    public static final int SUBSTRING=40;
    public static final int REAL=71;
    public static final int KW_DATE=51;
    public static final int MATCHING=29;
    public static final int WS=75;
    public static final int CURRENT_ROLE=13;
    public static final int SL_COMMENT=76;
    public static final int GENERIC_ID=65;
    public static final int OR=34;
    public static final int TRIM=43;
    public static final int LEFT_PAREN=67;
    public static final int KW_INTEGER=56;
    public static final int MAXIMUM=31;
    public static final int FROM=24;
    public static final int DISTINCT=19;
    public static final int CURRENT_DATE=14;
    public static final int STRING=69;

        public JaybirdSqlParser(TokenStream input) {
            super(input);
            ruleMemo = new HashMap[139+1];
         }
        
    protected TreeAdaptor adaptor = new CommonTreeAdaptor();

    public void setTreeAdaptor(TreeAdaptor adaptor) {
        this.adaptor = adaptor;
    }
    public TreeAdaptor getTreeAdaptor() {
        return adaptor;
    }

    public String[] getTokenNames() { return tokenNames; }
    public String getGrammarFileName() { return "D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3"; }

    
    	private boolean _inReturning;
    	//protected boolean _hasReturning;
    	//protected String _tableName;
    	//protected String _selectClause;
    	protected boolean _defaultValues;
    	//protected java.util.ArrayList _columns = new java.util.ArrayList();
    	//protected java.util.ArrayList _returningColumns = new java.util.ArrayList();
    	//protected java.util.ArrayList _values = new java.util.ArrayList();
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
    	
    	protected void mismatch(IntStream input, int ttype, BitSet follow)	
    		throws RecognitionException {
    		
    		_mismatchCount++;
    		
    		super.mismatch(input, ttype, follow);
    	}
    	 
    	public void emitErrorMessage(String msg) {
    		_errorMessages.add(msg);
    	}
    	
    	public void recoverFromMismatchedToken(IntStream input,
    										   RecognitionException e,
    										   int ttype,
    										   BitSet follow)
    		throws RecognitionException
    	{
    		// if next token is what we are looking for then "delete" this token
    		if ( input.LA(2)==ttype ) {
    			reportError(e);
    			beginResync();
    			input.consume(); // simply delete extra token
    			endResync();
    			input.consume(); // move past ttype token as if all were ok
    			return;
    		}
    		if ( !recoverFromMismatchedElement(input,e,follow) ) {
    			throw e;
    		}
    	}


    public static class statement_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start statement
    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:224:1: statement : ( insertStatement | deleteStatement | updateStatement | updateOrInsertStatement );
    public final statement_return statement() throws RecognitionException {
        statement_return retval = new statement_return();
        retval.start = input.LT(1);
        int statement_StartIndex = input.index();
        CommonTree root_0 = null;

        insertStatement_return insertStatement1 = null;

        deleteStatement_return deleteStatement2 = null;

        updateStatement_return updateStatement3 = null;

        updateOrInsertStatement_return updateOrInsertStatement4 = null;



        try {
            if ( backtracking>0 && alreadyParsedRule(input, 1) ) { return retval; }
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:225:3: ( insertStatement | deleteStatement | updateStatement | updateOrInsertStatement )
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
                else if ( ((LA1_3>=GENERIC_ID && LA1_3<=QUOTED_ID)) ) {
                    alt1=3;
                }
                else {
                    if (backtracking>0) {failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("224:1: statement : ( insertStatement | deleteStatement | updateStatement | updateOrInsertStatement );", 1, 3, input);

                    throw nvae;
                }
                }
                break;
            default:
                if (backtracking>0) {failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("224:1: statement : ( insertStatement | deleteStatement | updateStatement | updateOrInsertStatement );", 1, 0, input);

                throw nvae;
            }

            switch (alt1) {
                case 1 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:225:5: insertStatement
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_insertStatement_in_statement480);
                    insertStatement1=insertStatement();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, insertStatement1.getTree());

                    }
                    break;
                case 2 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:226:5: deleteStatement
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_deleteStatement_in_statement486);
                    deleteStatement2=deleteStatement();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, deleteStatement2.getTree());

                    }
                    break;
                case 3 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:227:5: updateStatement
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_updateStatement_in_statement492);
                    updateStatement3=updateStatement();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, updateStatement3.getTree());

                    }
                    break;
                case 4 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:229:5: updateOrInsertStatement
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_updateOrInsertStatement_in_statement499);
                    updateOrInsertStatement4=updateOrInsertStatement();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, updateOrInsertStatement4.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 1, statement_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end statement

    public static class deleteStatement_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start deleteStatement
    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:249:1: deleteStatement : DELETE FROM tableName ;
    public final deleteStatement_return deleteStatement() throws RecognitionException {
        deleteStatement_return retval = new deleteStatement_return();
        retval.start = input.LT(1);
        int deleteStatement_StartIndex = input.index();
        CommonTree root_0 = null;

        Token DELETE5=null;
        Token FROM6=null;
        tableName_return tableName7 = null;


        CommonTree DELETE5_tree=null;
        CommonTree FROM6_tree=null;

        try {
            if ( backtracking>0 && alreadyParsedRule(input, 2) ) { return retval; }
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:249:17: ( DELETE FROM tableName )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:250:4: DELETE FROM tableName
            {
            root_0 = (CommonTree)adaptor.nil();

            DELETE5=(Token)input.LT(1);
            match(input,DELETE,FOLLOW_DELETE_in_deleteStatement516); if (failed) return retval;
            if ( backtracking==0 ) {
            DELETE5_tree = (CommonTree)adaptor.create(DELETE5);
            adaptor.addChild(root_0, DELETE5_tree);
            }
            FROM6=(Token)input.LT(1);
            match(input,FROM,FOLLOW_FROM_in_deleteStatement518); if (failed) return retval;
            if ( backtracking==0 ) {
            FROM6_tree = (CommonTree)adaptor.create(FROM6);
            adaptor.addChild(root_0, FROM6_tree);
            }
            pushFollow(FOLLOW_tableName_in_deleteStatement520);
            tableName7=tableName();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, tableName7.getTree());
            if ( backtracking==0 ) {
              
              				statementModel.setStatementType(JaybirdStatementModel.DELETE_TYPE);
              			
            }

            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 2, deleteStatement_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end deleteStatement

    public static class updateStatement_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start updateStatement
    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:273:1: updateStatement : UPDATE tableName SET assignments ;
    public final updateStatement_return updateStatement() throws RecognitionException {
        updateStatement_return retval = new updateStatement_return();
        retval.start = input.LT(1);
        int updateStatement_StartIndex = input.index();
        CommonTree root_0 = null;

        Token UPDATE8=null;
        Token SET10=null;
        tableName_return tableName9 = null;

        assignments_return assignments11 = null;


        CommonTree UPDATE8_tree=null;
        CommonTree SET10_tree=null;

        try {
            if ( backtracking>0 && alreadyParsedRule(input, 3) ) { return retval; }
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:273:17: ( UPDATE tableName SET assignments )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:274:4: UPDATE tableName SET assignments
            {
            root_0 = (CommonTree)adaptor.nil();

            UPDATE8=(Token)input.LT(1);
            match(input,UPDATE,FOLLOW_UPDATE_in_updateStatement544); if (failed) return retval;
            if ( backtracking==0 ) {
            UPDATE8_tree = (CommonTree)adaptor.create(UPDATE8);
            adaptor.addChild(root_0, UPDATE8_tree);
            }
            pushFollow(FOLLOW_tableName_in_updateStatement546);
            tableName9=tableName();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, tableName9.getTree());
            SET10=(Token)input.LT(1);
            match(input,SET,FOLLOW_SET_in_updateStatement548); if (failed) return retval;
            if ( backtracking==0 ) {
            SET10_tree = (CommonTree)adaptor.create(SET10);
            adaptor.addChild(root_0, SET10_tree);
            }
            pushFollow(FOLLOW_assignments_in_updateStatement550);
            assignments11=assignments();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, assignments11.getTree());
            if ( backtracking==0 ) {
              
              				statementModel.setStatementType(JaybirdStatementModel.UPDATE_TYPE);
              			
            }

            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 3, updateStatement_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end updateStatement

    public static class assignments_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start assignments
    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:280:1: assignments : assignment ( ',' assignment )* ;
    public final assignments_return assignments() throws RecognitionException {
        assignments_return retval = new assignments_return();
        retval.start = input.LT(1);
        int assignments_StartIndex = input.index();
        CommonTree root_0 = null;

        Token char_literal13=null;
        assignment_return assignment12 = null;

        assignment_return assignment14 = null;


        CommonTree char_literal13_tree=null;

        try {
            if ( backtracking>0 && alreadyParsedRule(input, 4) ) { return retval; }
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:280:13: ( assignment ( ',' assignment )* )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:280:15: assignment ( ',' assignment )*
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_assignment_in_assignments568);
            assignment12=assignment();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, assignment12.getTree());
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:280:26: ( ',' assignment )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( (LA2_0==COMMA) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:280:27: ',' assignment
            	    {
            	    char_literal13=(Token)input.LT(1);
            	    match(input,COMMA,FOLLOW_COMMA_in_assignments571); if (failed) return retval;
            	    if ( backtracking==0 ) {
            	    char_literal13_tree = (CommonTree)adaptor.create(char_literal13);
            	    adaptor.addChild(root_0, char_literal13_tree);
            	    }
            	    pushFollow(FOLLOW_assignment_in_assignments573);
            	    assignment14=assignment();
            	    _fsp--;
            	    if (failed) return retval;
            	    if ( backtracking==0 ) adaptor.addChild(root_0, assignment14.getTree());

            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 4, assignments_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end assignments

    public static class assignment_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start assignment
    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:283:1: assignment : columnName '=' value ;
    public final assignment_return assignment() throws RecognitionException {
        assignment_return retval = new assignment_return();
        retval.start = input.LT(1);
        int assignment_StartIndex = input.index();
        CommonTree root_0 = null;

        Token char_literal16=null;
        columnName_return columnName15 = null;

        value_return value17 = null;


        CommonTree char_literal16_tree=null;

        try {
            if ( backtracking>0 && alreadyParsedRule(input, 5) ) { return retval; }
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:283:12: ( columnName '=' value )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:283:14: columnName '=' value
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_columnName_in_assignment588);
            columnName15=columnName();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, columnName15.getTree());
            char_literal16=(Token)input.LT(1);
            match(input,78,FOLLOW_78_in_assignment590); if (failed) return retval;
            if ( backtracking==0 ) {
            char_literal16_tree = (CommonTree)adaptor.create(char_literal16);
            adaptor.addChild(root_0, char_literal16_tree);
            }
            pushFollow(FOLLOW_value_in_assignment592);
            value17=value();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, value17.getTree());

            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 5, assignment_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end assignment

    public static class updateOrInsertStatement_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start updateOrInsertStatement
    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:299:1: updateOrInsertStatement : UPDATE OR INSERT INTO tableName ( insertColumns )? insertValues ( matchingClause )? ( returningClause )? ;
    public final updateOrInsertStatement_return updateOrInsertStatement() throws RecognitionException {
        updateOrInsertStatement_return retval = new updateOrInsertStatement_return();
        retval.start = input.LT(1);
        int updateOrInsertStatement_StartIndex = input.index();
        CommonTree root_0 = null;

        Token UPDATE18=null;
        Token OR19=null;
        Token INSERT20=null;
        Token INTO21=null;
        tableName_return tableName22 = null;

        insertColumns_return insertColumns23 = null;

        insertValues_return insertValues24 = null;

        matchingClause_return matchingClause25 = null;

        returningClause_return returningClause26 = null;


        CommonTree UPDATE18_tree=null;
        CommonTree OR19_tree=null;
        CommonTree INSERT20_tree=null;
        CommonTree INTO21_tree=null;

        try {
            if ( backtracking>0 && alreadyParsedRule(input, 6) ) { return retval; }
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:300:3: ( UPDATE OR INSERT INTO tableName ( insertColumns )? insertValues ( matchingClause )? ( returningClause )? )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:300:5: UPDATE OR INSERT INTO tableName ( insertColumns )? insertValues ( matchingClause )? ( returningClause )?
            {
            root_0 = (CommonTree)adaptor.nil();

            UPDATE18=(Token)input.LT(1);
            match(input,UPDATE,FOLLOW_UPDATE_in_updateOrInsertStatement609); if (failed) return retval;
            if ( backtracking==0 ) {
            UPDATE18_tree = (CommonTree)adaptor.create(UPDATE18);
            adaptor.addChild(root_0, UPDATE18_tree);
            }
            OR19=(Token)input.LT(1);
            match(input,OR,FOLLOW_OR_in_updateOrInsertStatement611); if (failed) return retval;
            if ( backtracking==0 ) {
            OR19_tree = (CommonTree)adaptor.create(OR19);
            adaptor.addChild(root_0, OR19_tree);
            }
            INSERT20=(Token)input.LT(1);
            match(input,INSERT,FOLLOW_INSERT_in_updateOrInsertStatement613); if (failed) return retval;
            if ( backtracking==0 ) {
            INSERT20_tree = (CommonTree)adaptor.create(INSERT20);
            adaptor.addChild(root_0, INSERT20_tree);
            }
            INTO21=(Token)input.LT(1);
            match(input,INTO,FOLLOW_INTO_in_updateOrInsertStatement615); if (failed) return retval;
            if ( backtracking==0 ) {
            INTO21_tree = (CommonTree)adaptor.create(INTO21);
            adaptor.addChild(root_0, INTO21_tree);
            }
            pushFollow(FOLLOW_tableName_in_updateOrInsertStatement617);
            tableName22=tableName();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, tableName22.getTree());
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:300:37: ( insertColumns )?
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==LEFT_PAREN) ) {
                alt3=1;
            }
            switch (alt3) {
                case 1 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:0:0: insertColumns
                    {
                    pushFollow(FOLLOW_insertColumns_in_updateOrInsertStatement619);
                    insertColumns23=insertColumns();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, insertColumns23.getTree());

                    }
                    break;

            }

            pushFollow(FOLLOW_insertValues_in_updateOrInsertStatement627);
            insertValues24=insertValues();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, insertValues24.getTree());
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:301:18: ( matchingClause )?
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0==MATCHING) ) {
                alt4=1;
            }
            switch (alt4) {
                case 1 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:0:0: matchingClause
                    {
                    pushFollow(FOLLOW_matchingClause_in_updateOrInsertStatement629);
                    matchingClause25=matchingClause();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, matchingClause25.getTree());

                    }
                    break;

            }

            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:301:34: ( returningClause )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==RETURNING) ) {
                alt5=1;
            }
            switch (alt5) {
                case 1 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:0:0: returningClause
                    {
                    pushFollow(FOLLOW_returningClause_in_updateOrInsertStatement632);
                    returningClause26=returningClause();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, returningClause26.getTree());

                    }
                    break;

            }

            if ( backtracking==0 ) {
              
              				statementModel.setStatementType(JaybirdStatementModel.UPDATE_OR_INSERT_TYPE);
              			
            }

            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 6, updateOrInsertStatement_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end updateOrInsertStatement

    public static class matchingClause_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start matchingClause
    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:307:1: matchingClause : MATCHING columnList ;
    public final matchingClause_return matchingClause() throws RecognitionException {
        matchingClause_return retval = new matchingClause_return();
        retval.start = input.LT(1);
        int matchingClause_StartIndex = input.index();
        CommonTree root_0 = null;

        Token MATCHING27=null;
        columnList_return columnList28 = null;


        CommonTree MATCHING27_tree=null;

        try {
            if ( backtracking>0 && alreadyParsedRule(input, 7) ) { return retval; }
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:307:16: ( MATCHING columnList )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:307:18: MATCHING columnList
            {
            root_0 = (CommonTree)adaptor.nil();

            MATCHING27=(Token)input.LT(1);
            match(input,MATCHING,FOLLOW_MATCHING_in_matchingClause651); if (failed) return retval;
            if ( backtracking==0 ) {
            MATCHING27_tree = (CommonTree)adaptor.create(MATCHING27);
            adaptor.addChild(root_0, MATCHING27_tree);
            }
            pushFollow(FOLLOW_columnList_in_matchingClause653);
            columnList28=columnList();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, columnList28.getTree());

            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 7, matchingClause_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end matchingClause

    public static class insertStatement_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start insertStatement
    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:318:1: insertStatement : INSERT INTO tableName ( insertColumns )? ( insertValues ( returningClause )? | selectClause | defaultValuesClause ( returningClause )? ) ;
    public final insertStatement_return insertStatement() throws RecognitionException {
        insertStatement_return retval = new insertStatement_return();
        retval.start = input.LT(1);
        int insertStatement_StartIndex = input.index();
        CommonTree root_0 = null;

        Token INSERT29=null;
        Token INTO30=null;
        tableName_return tableName31 = null;

        insertColumns_return insertColumns32 = null;

        insertValues_return insertValues33 = null;

        returningClause_return returningClause34 = null;

        selectClause_return selectClause35 = null;

        defaultValuesClause_return defaultValuesClause36 = null;

        returningClause_return returningClause37 = null;


        CommonTree INSERT29_tree=null;
        CommonTree INTO30_tree=null;

        try {
            if ( backtracking>0 && alreadyParsedRule(input, 8) ) { return retval; }
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:319:3: ( INSERT INTO tableName ( insertColumns )? ( insertValues ( returningClause )? | selectClause | defaultValuesClause ( returningClause )? ) )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:319:6: INSERT INTO tableName ( insertColumns )? ( insertValues ( returningClause )? | selectClause | defaultValuesClause ( returningClause )? )
            {
            root_0 = (CommonTree)adaptor.nil();

            INSERT29=(Token)input.LT(1);
            match(input,INSERT,FOLLOW_INSERT_in_insertStatement670); if (failed) return retval;
            if ( backtracking==0 ) {
            INSERT29_tree = (CommonTree)adaptor.create(INSERT29);
            adaptor.addChild(root_0, INSERT29_tree);
            }
            INTO30=(Token)input.LT(1);
            match(input,INTO,FOLLOW_INTO_in_insertStatement672); if (failed) return retval;
            if ( backtracking==0 ) {
            INTO30_tree = (CommonTree)adaptor.create(INTO30);
            adaptor.addChild(root_0, INTO30_tree);
            }
            pushFollow(FOLLOW_tableName_in_insertStatement674);
            tableName31=tableName();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, tableName31.getTree());
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:319:28: ( insertColumns )?
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0==LEFT_PAREN) ) {
                alt6=1;
            }
            switch (alt6) {
                case 1 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:0:0: insertColumns
                    {
                    pushFollow(FOLLOW_insertColumns_in_insertStatement676);
                    insertColumns32=insertColumns();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, insertColumns32.getTree());

                    }
                    break;

            }

            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:320:6: ( insertValues ( returningClause )? | selectClause | defaultValuesClause ( returningClause )? )
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
                if (backtracking>0) {failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("320:6: ( insertValues ( returningClause )? | selectClause | defaultValuesClause ( returningClause )? )", 9, 0, input);

                throw nvae;
            }

            switch (alt9) {
                case 1 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:320:8: insertValues ( returningClause )?
                    {
                    pushFollow(FOLLOW_insertValues_in_insertStatement686);
                    insertValues33=insertValues();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, insertValues33.getTree());
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:320:21: ( returningClause )?
                    int alt7=2;
                    int LA7_0 = input.LA(1);

                    if ( (LA7_0==RETURNING) ) {
                        alt7=1;
                    }
                    switch (alt7) {
                        case 1 :
                            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:0:0: returningClause
                            {
                            pushFollow(FOLLOW_returningClause_in_insertStatement688);
                            returningClause34=returningClause();
                            _fsp--;
                            if (failed) return retval;
                            if ( backtracking==0 ) adaptor.addChild(root_0, returningClause34.getTree());

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:321:8: selectClause
                    {
                    pushFollow(FOLLOW_selectClause_in_insertStatement698);
                    selectClause35=selectClause();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, selectClause35.getTree());

                    }
                    break;
                case 3 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:322:8: defaultValuesClause ( returningClause )?
                    {
                    pushFollow(FOLLOW_defaultValuesClause_in_insertStatement707);
                    defaultValuesClause36=defaultValuesClause();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, defaultValuesClause36.getTree());
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:322:28: ( returningClause )?
                    int alt8=2;
                    int LA8_0 = input.LA(1);

                    if ( (LA8_0==RETURNING) ) {
                        alt8=1;
                    }
                    switch (alt8) {
                        case 1 :
                            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:0:0: returningClause
                            {
                            pushFollow(FOLLOW_returningClause_in_insertStatement709);
                            returningClause37=returningClause();
                            _fsp--;
                            if (failed) return retval;
                            if ( backtracking==0 ) adaptor.addChild(root_0, returningClause37.getTree());

                            }
                            break;

                    }


                    }
                    break;

            }

            if ( backtracking==0 ) {
              
              				statementModel.setStatementType(JaybirdStatementModel.INSERT_TYPE);
              			
            }

            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 8, insertStatement_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end insertStatement

    public static class insertColumns_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start insertColumns
    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:329:1: insertColumns : '(' columnList ')' ;
    public final insertColumns_return insertColumns() throws RecognitionException {
        insertColumns_return retval = new insertColumns_return();
        retval.start = input.LT(1);
        int insertColumns_StartIndex = input.index();
        CommonTree root_0 = null;

        Token char_literal38=null;
        Token char_literal40=null;
        columnList_return columnList39 = null;


        CommonTree char_literal38_tree=null;
        CommonTree char_literal40_tree=null;

        try {
            if ( backtracking>0 && alreadyParsedRule(input, 9) ) { return retval; }
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:330:3: ( '(' columnList ')' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:330:5: '(' columnList ')'
            {
            root_0 = (CommonTree)adaptor.nil();

            char_literal38=(Token)input.LT(1);
            match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_insertColumns736); if (failed) return retval;
            if ( backtracking==0 ) {
            char_literal38_tree = (CommonTree)adaptor.create(char_literal38);
            adaptor.addChild(root_0, char_literal38_tree);
            }
            pushFollow(FOLLOW_columnList_in_insertColumns738);
            columnList39=columnList();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, columnList39.getTree());
            char_literal40=(Token)input.LT(1);
            match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_insertColumns740); if (failed) return retval;
            if ( backtracking==0 ) {
            char_literal40_tree = (CommonTree)adaptor.create(char_literal40);
            adaptor.addChild(root_0, char_literal40_tree);
            }
            if ( backtracking==0 ) {
              
              				_inReturning = false;
              			
            }

            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 9, insertColumns_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end insertColumns

    public static class insertValues_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start insertValues
    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:336:1: insertValues : VALUES '(' valueList ')' ;
    public final insertValues_return insertValues() throws RecognitionException {
        insertValues_return retval = new insertValues_return();
        retval.start = input.LT(1);
        int insertValues_StartIndex = input.index();
        CommonTree root_0 = null;

        Token VALUES41=null;
        Token char_literal42=null;
        Token char_literal44=null;
        valueList_return valueList43 = null;


        CommonTree VALUES41_tree=null;
        CommonTree char_literal42_tree=null;
        CommonTree char_literal44_tree=null;

        try {
            if ( backtracking>0 && alreadyParsedRule(input, 10) ) { return retval; }
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:337:3: ( VALUES '(' valueList ')' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:337:5: VALUES '(' valueList ')'
            {
            root_0 = (CommonTree)adaptor.nil();

            VALUES41=(Token)input.LT(1);
            match(input,VALUES,FOLLOW_VALUES_in_insertValues759); if (failed) return retval;
            if ( backtracking==0 ) {
            VALUES41_tree = (CommonTree)adaptor.create(VALUES41);
            adaptor.addChild(root_0, VALUES41_tree);
            }
            char_literal42=(Token)input.LT(1);
            match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_insertValues761); if (failed) return retval;
            if ( backtracking==0 ) {
            char_literal42_tree = (CommonTree)adaptor.create(char_literal42);
            adaptor.addChild(root_0, char_literal42_tree);
            }
            pushFollow(FOLLOW_valueList_in_insertValues763);
            valueList43=valueList();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, valueList43.getTree());
            char_literal44=(Token)input.LT(1);
            match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_insertValues765); if (failed) return retval;
            if ( backtracking==0 ) {
            char_literal44_tree = (CommonTree)adaptor.create(char_literal44);
            adaptor.addChild(root_0, char_literal44_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 10, insertValues_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end insertValues

    public static class returningClause_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start returningClause
    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:340:1: returningClause : RETURNING columnList ;
    public final returningClause_return returningClause() throws RecognitionException {
        returningClause_return retval = new returningClause_return();
        retval.start = input.LT(1);
        int returningClause_StartIndex = input.index();
        CommonTree root_0 = null;

        Token RETURNING45=null;
        columnList_return columnList46 = null;


        CommonTree RETURNING45_tree=null;

        try {
            if ( backtracking>0 && alreadyParsedRule(input, 11) ) { return retval; }
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:341:3: ( RETURNING columnList )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:341:5: RETURNING columnList
            {
            root_0 = (CommonTree)adaptor.nil();

            RETURNING45=(Token)input.LT(1);
            match(input,RETURNING,FOLLOW_RETURNING_in_returningClause780); if (failed) return retval;
            if ( backtracking==0 ) {
            RETURNING45_tree = (CommonTree)adaptor.create(RETURNING45);
            adaptor.addChild(root_0, RETURNING45_tree);
            }
            pushFollow(FOLLOW_columnList_in_returningClause782);
            columnList46=columnList();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, columnList46.getTree());
            if ( backtracking==0 ) {
              
              				_inReturning = true;
              			
            }

            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 11, returningClause_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end returningClause

    public static class defaultValuesClause_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start defaultValuesClause
    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:347:1: defaultValuesClause : DEFAULT VALUES ;
    public final defaultValuesClause_return defaultValuesClause() throws RecognitionException {
        defaultValuesClause_return retval = new defaultValuesClause_return();
        retval.start = input.LT(1);
        int defaultValuesClause_StartIndex = input.index();
        CommonTree root_0 = null;

        Token DEFAULT47=null;
        Token VALUES48=null;

        CommonTree DEFAULT47_tree=null;
        CommonTree VALUES48_tree=null;

        try {
            if ( backtracking>0 && alreadyParsedRule(input, 12) ) { return retval; }
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:348:3: ( DEFAULT VALUES )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:348:5: DEFAULT VALUES
            {
            root_0 = (CommonTree)adaptor.nil();

            DEFAULT47=(Token)input.LT(1);
            match(input,DEFAULT,FOLLOW_DEFAULT_in_defaultValuesClause801); if (failed) return retval;
            if ( backtracking==0 ) {
            DEFAULT47_tree = (CommonTree)adaptor.create(DEFAULT47);
            adaptor.addChild(root_0, DEFAULT47_tree);
            }
            VALUES48=(Token)input.LT(1);
            match(input,VALUES,FOLLOW_VALUES_in_defaultValuesClause803); if (failed) return retval;
            if ( backtracking==0 ) {
            VALUES48_tree = (CommonTree)adaptor.create(VALUES48);
            adaptor.addChild(root_0, VALUES48_tree);
            }
            if ( backtracking==0 ) {
              
              				statementModel.setDefaultValues(true);
              			
            }

            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 12, defaultValuesClause_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end defaultValuesClause

    public static class simpleIdentifier_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start simpleIdentifier
    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:354:1: simpleIdentifier : ( GENERIC_ID | QUOTED_ID );
    public final simpleIdentifier_return simpleIdentifier() throws RecognitionException {
        simpleIdentifier_return retval = new simpleIdentifier_return();
        retval.start = input.LT(1);
        int simpleIdentifier_StartIndex = input.index();
        CommonTree root_0 = null;

        Token set49=null;

        CommonTree set49_tree=null;

        try {
            if ( backtracking>0 && alreadyParsedRule(input, 13) ) { return retval; }
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:355:3: ( GENERIC_ID | QUOTED_ID )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:
            {
            root_0 = (CommonTree)adaptor.nil();

            set49=(Token)input.LT(1);
            if ( (input.LA(1)>=GENERIC_ID && input.LA(1)<=QUOTED_ID) ) {
                input.consume();
                if ( backtracking==0 ) adaptor.addChild(root_0, adaptor.create(set49));
                errorRecovery=false;failed=false;
            }
            else {
                if (backtracking>0) {failed=true; return retval;}
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recoverFromMismatchedSet(input,mse,FOLLOW_set_in_simpleIdentifier0);    throw mse;
            }


            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 13, simpleIdentifier_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end simpleIdentifier

    public static class fullIdentifier_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start fullIdentifier
    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:359:1: fullIdentifier : simpleIdentifier '.' simpleIdentifier ;
    public final fullIdentifier_return fullIdentifier() throws RecognitionException {
        fullIdentifier_return retval = new fullIdentifier_return();
        retval.start = input.LT(1);
        int fullIdentifier_StartIndex = input.index();
        CommonTree root_0 = null;

        Token char_literal51=null;
        simpleIdentifier_return simpleIdentifier50 = null;

        simpleIdentifier_return simpleIdentifier52 = null;


        CommonTree char_literal51_tree=null;

        try {
            if ( backtracking>0 && alreadyParsedRule(input, 14) ) { return retval; }
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:360:3: ( simpleIdentifier '.' simpleIdentifier )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:360:5: simpleIdentifier '.' simpleIdentifier
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_simpleIdentifier_in_fullIdentifier843);
            simpleIdentifier50=simpleIdentifier();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, simpleIdentifier50.getTree());
            char_literal51=(Token)input.LT(1);
            match(input,79,FOLLOW_79_in_fullIdentifier845); if (failed) return retval;
            if ( backtracking==0 ) {
            char_literal51_tree = (CommonTree)adaptor.create(char_literal51);
            adaptor.addChild(root_0, char_literal51_tree);
            }
            pushFollow(FOLLOW_simpleIdentifier_in_fullIdentifier847);
            simpleIdentifier52=simpleIdentifier();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, simpleIdentifier52.getTree());

            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 14, fullIdentifier_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end fullIdentifier

    public static class tableName_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start tableName
    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:363:1: tableName : t= simpleIdentifier ;
    public final tableName_return tableName() throws RecognitionException {
        tableName_return retval = new tableName_return();
        retval.start = input.LT(1);
        int tableName_StartIndex = input.index();
        CommonTree root_0 = null;

        simpleIdentifier_return t = null;



        try {
            if ( backtracking>0 && alreadyParsedRule(input, 15) ) { return retval; }
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:364:3: (t= simpleIdentifier )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:364:5: t= simpleIdentifier
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_simpleIdentifier_in_tableName866);
            t=simpleIdentifier();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, t.getTree());
            if ( backtracking==0 ) {
              
              				statementModel.setTableName(input.toString(t.start,t.stop));
              			
            }

            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 15, tableName_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end tableName

    public static class columnList_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start columnList
    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:370:1: columnList : columnName ( ',' columnName )* ;
    public final columnList_return columnList() throws RecognitionException {
        columnList_return retval = new columnList_return();
        retval.start = input.LT(1);
        int columnList_StartIndex = input.index();
        CommonTree root_0 = null;

        Token char_literal54=null;
        columnName_return columnName53 = null;

        columnName_return columnName55 = null;


        CommonTree char_literal54_tree=null;

        try {
            if ( backtracking>0 && alreadyParsedRule(input, 16) ) { return retval; }
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:371:3: ( columnName ( ',' columnName )* )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:371:5: columnName ( ',' columnName )*
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_columnName_in_columnList887);
            columnName53=columnName();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, columnName53.getTree());
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:371:16: ( ',' columnName )*
            loop10:
            do {
                int alt10=2;
                int LA10_0 = input.LA(1);

                if ( (LA10_0==COMMA) ) {
                    alt10=1;
                }


                switch (alt10) {
            	case 1 :
            	    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:371:17: ',' columnName
            	    {
            	    char_literal54=(Token)input.LT(1);
            	    match(input,COMMA,FOLLOW_COMMA_in_columnList890); if (failed) return retval;
            	    if ( backtracking==0 ) {
            	    char_literal54_tree = (CommonTree)adaptor.create(char_literal54);
            	    adaptor.addChild(root_0, char_literal54_tree);
            	    }
            	    pushFollow(FOLLOW_columnName_in_columnList892);
            	    columnName55=columnName();
            	    _fsp--;
            	    if (failed) return retval;
            	    if ( backtracking==0 ) adaptor.addChild(root_0, columnName55.getTree());

            	    }
            	    break;

            	default :
            	    break loop10;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 16, columnList_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end columnList

    public static class columnName_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start columnName
    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:374:1: columnName : (si= simpleIdentifier | fi= fullIdentifier );
    public final columnName_return columnName() throws RecognitionException {
        columnName_return retval = new columnName_return();
        retval.start = input.LT(1);
        int columnName_StartIndex = input.index();
        CommonTree root_0 = null;

        simpleIdentifier_return si = null;

        fullIdentifier_return fi = null;



        try {
            if ( backtracking>0 && alreadyParsedRule(input, 17) ) { return retval; }
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:375:3: (si= simpleIdentifier | fi= fullIdentifier )
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( ((LA11_0>=GENERIC_ID && LA11_0<=QUOTED_ID)) ) {
                int LA11_1 = input.LA(2);

                if ( (LA11_1==79) ) {
                    alt11=2;
                }
                else if ( (LA11_1==EOF||LA11_1==RETURNING||LA11_1==RIGHT_PAREN||LA11_1==COMMA||LA11_1==78) ) {
                    alt11=1;
                }
                else {
                    if (backtracking>0) {failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("374:1: columnName : (si= simpleIdentifier | fi= fullIdentifier );", 11, 1, input);

                    throw nvae;
                }
            }
            else {
                if (backtracking>0) {failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("374:1: columnName : (si= simpleIdentifier | fi= fullIdentifier );", 11, 0, input);

                throw nvae;
            }
            switch (alt11) {
                case 1 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:375:5: si= simpleIdentifier
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_simpleIdentifier_in_columnName911);
                    si=simpleIdentifier();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, si.getTree());
                    if ( backtracking==0 ) {
                      
                      				if (_inReturning)
                      					statementModel.addReturningColumn(input.toString(si.start,si.stop));
                      				else
                      					statementModel.addColumn(input.toString(si.start,si.stop));
                      			
                    }

                    }
                    break;
                case 2 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:383:5: fi= fullIdentifier
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_fullIdentifier_in_columnName931);
                    fi=fullIdentifier();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, fi.getTree());
                    if ( backtracking==0 ) {
                      
                      				if (_inReturning)
                      					statementModel.addReturningColumn(input.toString(fi.start,fi.stop));
                      				else
                      					statementModel.addColumn(input.toString(fi.start,fi.stop));
                      			
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 17, columnName_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end columnName

    public static class valueList_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start valueList
    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:392:1: valueList : value ( ',' value )* ;
    public final valueList_return valueList() throws RecognitionException {
        valueList_return retval = new valueList_return();
        retval.start = input.LT(1);
        int valueList_StartIndex = input.index();
        CommonTree root_0 = null;

        Token char_literal57=null;
        value_return value56 = null;

        value_return value58 = null;


        CommonTree char_literal57_tree=null;

        try {
            if ( backtracking>0 && alreadyParsedRule(input, 18) ) { return retval; }
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:393:3: ( value ( ',' value )* )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:393:5: value ( ',' value )*
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_value_in_valueList950);
            value56=value();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, value56.getTree());
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:393:11: ( ',' value )*
            loop12:
            do {
                int alt12=2;
                int LA12_0 = input.LA(1);

                if ( (LA12_0==COMMA) ) {
                    alt12=1;
                }


                switch (alt12) {
            	case 1 :
            	    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:393:12: ',' value
            	    {
            	    char_literal57=(Token)input.LT(1);
            	    match(input,COMMA,FOLLOW_COMMA_in_valueList953); if (failed) return retval;
            	    if ( backtracking==0 ) {
            	    char_literal57_tree = (CommonTree)adaptor.create(char_literal57);
            	    adaptor.addChild(root_0, char_literal57_tree);
            	    }
            	    pushFollow(FOLLOW_value_in_valueList955);
            	    value58=value();
            	    _fsp--;
            	    if (failed) return retval;
            	    if ( backtracking==0 ) adaptor.addChild(root_0, value58.getTree());

            	    }
            	    break;

            	default :
            	    break loop12;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 18, valueList_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end valueList

    public static class value_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start value
    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:427:1: value : ( simpleValue | simpleValue '+' simpleValue | simpleValue '-' simpleValue | simpleValue '*' simpleValue | simpleValue '/' simpleValue | simpleValue '||' simpleValue | '+' simpleValue | '-' simpleValue | LEFT_PAREN simpleValue RIGHT_PAREN | simpleValue COLLATE simpleIdentifier | parameter | CURRENT_USER | CURRENT_ROLE | CURRENT_DATE | CURRENT_TIME | CURRENT_TIMESTAMP | nullValue | function | nextValueExpression | castExpression | arrayElement | DB_KEY | simpleIdentifier '.' DB_KEY );
    public final value_return value() throws RecognitionException {
        value_return retval = new value_return();
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
        simpleValue_return simpleValue59 = null;

        simpleValue_return simpleValue60 = null;

        simpleValue_return simpleValue62 = null;

        simpleValue_return simpleValue63 = null;

        simpleValue_return simpleValue65 = null;

        simpleValue_return simpleValue66 = null;

        simpleValue_return simpleValue68 = null;

        simpleValue_return simpleValue69 = null;

        simpleValue_return simpleValue71 = null;

        simpleValue_return simpleValue72 = null;

        simpleValue_return simpleValue74 = null;

        simpleValue_return simpleValue76 = null;

        simpleValue_return simpleValue78 = null;

        simpleValue_return simpleValue80 = null;

        simpleValue_return simpleValue82 = null;

        simpleIdentifier_return simpleIdentifier84 = null;

        parameter_return parameter85 = null;

        nullValue_return nullValue91 = null;

        function_return function92 = null;

        nextValueExpression_return nextValueExpression93 = null;

        castExpression_return castExpression94 = null;

        arrayElement_return arrayElement95 = null;

        simpleIdentifier_return simpleIdentifier97 = null;


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
            if ( backtracking>0 && alreadyParsedRule(input, 19) ) { return retval; }
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:428:3: ( simpleValue | simpleValue '+' simpleValue | simpleValue '-' simpleValue | simpleValue '*' simpleValue | simpleValue '/' simpleValue | simpleValue '||' simpleValue | '+' simpleValue | '-' simpleValue | LEFT_PAREN simpleValue RIGHT_PAREN | simpleValue COLLATE simpleIdentifier | parameter | CURRENT_USER | CURRENT_ROLE | CURRENT_DATE | CURRENT_TIME | CURRENT_TIMESTAMP | nullValue | function | nextValueExpression | castExpression | arrayElement | DB_KEY | simpleIdentifier '.' DB_KEY )
            int alt13=23;
            switch ( input.LA(1) ) {
            case GENERIC_ID:
                {
                switch ( input.LA(2) ) {
                case COLLATE:
                    {
                    alt13=10;
                    }
                    break;
                case 83:
                    {
                    alt13=5;
                    }
                    break;
                case EOF:
                case AS:
                case FOR:
                case FROM:
                case RIGHT_PAREN:
                case COMMA:
                case 87:
                    {
                    alt13=1;
                    }
                    break;
                case 81:
                    {
                    alt13=3;
                    }
                    break;
                case 80:
                    {
                    alt13=2;
                    }
                    break;
                case 84:
                    {
                    alt13=6;
                    }
                    break;
                case 79:
                    {
                    alt13=23;
                    }
                    break;
                case 86:
                    {
                    alt13=21;
                    }
                    break;
                case LEFT_PAREN:
                    {
                    alt13=18;
                    }
                    break;
                case 82:
                    {
                    alt13=4;
                    }
                    break;
                default:
                    if (backtracking>0) {failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("427:1: value : ( simpleValue | simpleValue '+' simpleValue | simpleValue '-' simpleValue | simpleValue '*' simpleValue | simpleValue '/' simpleValue | simpleValue '||' simpleValue | '+' simpleValue | '-' simpleValue | LEFT_PAREN simpleValue RIGHT_PAREN | simpleValue COLLATE simpleIdentifier | parameter | CURRENT_USER | CURRENT_ROLE | CURRENT_DATE | CURRENT_TIME | CURRENT_TIMESTAMP | nullValue | function | nextValueExpression | castExpression | arrayElement | DB_KEY | simpleIdentifier '.' DB_KEY );", 13, 1, input);

                    throw nvae;
                }

                }
                break;
            case 80:
                {
                alt13=7;
                }
                break;
            case 81:
                {
                alt13=8;
                }
                break;
            case LEFT_PAREN:
                {
                alt13=9;
                }
                break;
            case 85:
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
            case STRING:
            case INTEGER:
            case REAL:
                {
                switch ( input.LA(2) ) {
                case COLLATE:
                    {
                    alt13=10;
                    }
                    break;
                case 83:
                    {
                    alt13=5;
                    }
                    break;
                case EOF:
                case AS:
                case FOR:
                case FROM:
                case RIGHT_PAREN:
                case COMMA:
                case 87:
                    {
                    alt13=1;
                    }
                    break;
                case 81:
                    {
                    alt13=3;
                    }
                    break;
                case 80:
                    {
                    alt13=2;
                    }
                    break;
                case 84:
                    {
                    alt13=6;
                    }
                    break;
                case 82:
                    {
                    alt13=4;
                    }
                    break;
                default:
                    if (backtracking>0) {failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("427:1: value : ( simpleValue | simpleValue '+' simpleValue | simpleValue '-' simpleValue | simpleValue '*' simpleValue | simpleValue '/' simpleValue | simpleValue '||' simpleValue | '+' simpleValue | '-' simpleValue | LEFT_PAREN simpleValue RIGHT_PAREN | simpleValue COLLATE simpleIdentifier | parameter | CURRENT_USER | CURRENT_ROLE | CURRENT_DATE | CURRENT_TIME | CURRENT_TIMESTAMP | nullValue | function | nextValueExpression | castExpression | arrayElement | DB_KEY | simpleIdentifier '.' DB_KEY );", 13, 12, input);

                    throw nvae;
                }

                }
                break;
            case QUOTED_ID:
                {
                switch ( input.LA(2) ) {
                case 79:
                    {
                    alt13=23;
                    }
                    break;
                case 86:
                    {
                    alt13=21;
                    }
                    break;
                case LEFT_PAREN:
                    {
                    alt13=18;
                    }
                    break;
                default:
                    if (backtracking>0) {failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("427:1: value : ( simpleValue | simpleValue '+' simpleValue | simpleValue '-' simpleValue | simpleValue '*' simpleValue | simpleValue '/' simpleValue | simpleValue '||' simpleValue | '+' simpleValue | '-' simpleValue | LEFT_PAREN simpleValue RIGHT_PAREN | simpleValue COLLATE simpleIdentifier | parameter | CURRENT_USER | CURRENT_ROLE | CURRENT_DATE | CURRENT_TIME | CURRENT_TIMESTAMP | nullValue | function | nextValueExpression | castExpression | arrayElement | DB_KEY | simpleIdentifier '.' DB_KEY );", 13, 13, input);

                    throw nvae;
                }

                }
                break;
            case AVG:
            case COUNT:
            case EXTRACT:
            case MINIMUM:
            case MAXIMUM:
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
                if (backtracking>0) {failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("427:1: value : ( simpleValue | simpleValue '+' simpleValue | simpleValue '-' simpleValue | simpleValue '*' simpleValue | simpleValue '/' simpleValue | simpleValue '||' simpleValue | '+' simpleValue | '-' simpleValue | LEFT_PAREN simpleValue RIGHT_PAREN | simpleValue COLLATE simpleIdentifier | parameter | CURRENT_USER | CURRENT_ROLE | CURRENT_DATE | CURRENT_TIME | CURRENT_TIMESTAMP | nullValue | function | nextValueExpression | castExpression | arrayElement | DB_KEY | simpleIdentifier '.' DB_KEY );", 13, 0, input);

                throw nvae;
            }

            switch (alt13) {
                case 1 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:428:5: simpleValue
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_simpleValue_in_value973);
                    simpleValue59=simpleValue();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, simpleValue59.getTree());

                    }
                    break;
                case 2 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:429:5: simpleValue '+' simpleValue
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_simpleValue_in_value979);
                    simpleValue60=simpleValue();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, simpleValue60.getTree());
                    char_literal61=(Token)input.LT(1);
                    match(input,80,FOLLOW_80_in_value981); if (failed) return retval;
                    if ( backtracking==0 ) {
                    char_literal61_tree = (CommonTree)adaptor.create(char_literal61);
                    adaptor.addChild(root_0, char_literal61_tree);
                    }
                    pushFollow(FOLLOW_simpleValue_in_value983);
                    simpleValue62=simpleValue();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, simpleValue62.getTree());

                    }
                    break;
                case 3 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:430:5: simpleValue '-' simpleValue
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_simpleValue_in_value989);
                    simpleValue63=simpleValue();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, simpleValue63.getTree());
                    char_literal64=(Token)input.LT(1);
                    match(input,81,FOLLOW_81_in_value991); if (failed) return retval;
                    if ( backtracking==0 ) {
                    char_literal64_tree = (CommonTree)adaptor.create(char_literal64);
                    adaptor.addChild(root_0, char_literal64_tree);
                    }
                    pushFollow(FOLLOW_simpleValue_in_value993);
                    simpleValue65=simpleValue();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, simpleValue65.getTree());

                    }
                    break;
                case 4 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:431:5: simpleValue '*' simpleValue
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_simpleValue_in_value999);
                    simpleValue66=simpleValue();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, simpleValue66.getTree());
                    char_literal67=(Token)input.LT(1);
                    match(input,82,FOLLOW_82_in_value1001); if (failed) return retval;
                    if ( backtracking==0 ) {
                    char_literal67_tree = (CommonTree)adaptor.create(char_literal67);
                    adaptor.addChild(root_0, char_literal67_tree);
                    }
                    pushFollow(FOLLOW_simpleValue_in_value1003);
                    simpleValue68=simpleValue();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, simpleValue68.getTree());

                    }
                    break;
                case 5 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:432:5: simpleValue '/' simpleValue
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_simpleValue_in_value1009);
                    simpleValue69=simpleValue();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, simpleValue69.getTree());
                    char_literal70=(Token)input.LT(1);
                    match(input,83,FOLLOW_83_in_value1011); if (failed) return retval;
                    if ( backtracking==0 ) {
                    char_literal70_tree = (CommonTree)adaptor.create(char_literal70);
                    adaptor.addChild(root_0, char_literal70_tree);
                    }
                    pushFollow(FOLLOW_simpleValue_in_value1013);
                    simpleValue71=simpleValue();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, simpleValue71.getTree());

                    }
                    break;
                case 6 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:433:5: simpleValue '||' simpleValue
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_simpleValue_in_value1019);
                    simpleValue72=simpleValue();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, simpleValue72.getTree());
                    string_literal73=(Token)input.LT(1);
                    match(input,84,FOLLOW_84_in_value1021); if (failed) return retval;
                    if ( backtracking==0 ) {
                    string_literal73_tree = (CommonTree)adaptor.create(string_literal73);
                    adaptor.addChild(root_0, string_literal73_tree);
                    }
                    pushFollow(FOLLOW_simpleValue_in_value1023);
                    simpleValue74=simpleValue();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, simpleValue74.getTree());

                    }
                    break;
                case 7 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:434:5: '+' simpleValue
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    char_literal75=(Token)input.LT(1);
                    match(input,80,FOLLOW_80_in_value1029); if (failed) return retval;
                    if ( backtracking==0 ) {
                    char_literal75_tree = (CommonTree)adaptor.create(char_literal75);
                    adaptor.addChild(root_0, char_literal75_tree);
                    }
                    pushFollow(FOLLOW_simpleValue_in_value1031);
                    simpleValue76=simpleValue();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, simpleValue76.getTree());

                    }
                    break;
                case 8 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:435:5: '-' simpleValue
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    char_literal77=(Token)input.LT(1);
                    match(input,81,FOLLOW_81_in_value1037); if (failed) return retval;
                    if ( backtracking==0 ) {
                    char_literal77_tree = (CommonTree)adaptor.create(char_literal77);
                    adaptor.addChild(root_0, char_literal77_tree);
                    }
                    pushFollow(FOLLOW_simpleValue_in_value1039);
                    simpleValue78=simpleValue();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, simpleValue78.getTree());

                    }
                    break;
                case 9 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:437:5: LEFT_PAREN simpleValue RIGHT_PAREN
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    LEFT_PAREN79=(Token)input.LT(1);
                    match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_value1048); if (failed) return retval;
                    if ( backtracking==0 ) {
                    LEFT_PAREN79_tree = (CommonTree)adaptor.create(LEFT_PAREN79);
                    adaptor.addChild(root_0, LEFT_PAREN79_tree);
                    }
                    pushFollow(FOLLOW_simpleValue_in_value1050);
                    simpleValue80=simpleValue();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, simpleValue80.getTree());
                    RIGHT_PAREN81=(Token)input.LT(1);
                    match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_value1052); if (failed) return retval;
                    if ( backtracking==0 ) {
                    RIGHT_PAREN81_tree = (CommonTree)adaptor.create(RIGHT_PAREN81);
                    adaptor.addChild(root_0, RIGHT_PAREN81_tree);
                    }

                    }
                    break;
                case 10 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:439:5: simpleValue COLLATE simpleIdentifier
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_simpleValue_in_value1061);
                    simpleValue82=simpleValue();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, simpleValue82.getTree());
                    COLLATE83=(Token)input.LT(1);
                    match(input,COLLATE,FOLLOW_COLLATE_in_value1063); if (failed) return retval;
                    if ( backtracking==0 ) {
                    COLLATE83_tree = (CommonTree)adaptor.create(COLLATE83);
                    adaptor.addChild(root_0, COLLATE83_tree);
                    }
                    pushFollow(FOLLOW_simpleIdentifier_in_value1065);
                    simpleIdentifier84=simpleIdentifier();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, simpleIdentifier84.getTree());

                    }
                    break;
                case 11 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:441:5: parameter
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_parameter_in_value1073);
                    parameter85=parameter();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, parameter85.getTree());

                    }
                    break;
                case 12 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:443:5: CURRENT_USER
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    CURRENT_USER86=(Token)input.LT(1);
                    match(input,CURRENT_USER,FOLLOW_CURRENT_USER_in_value1082); if (failed) return retval;
                    if ( backtracking==0 ) {
                    CURRENT_USER86_tree = (CommonTree)adaptor.create(CURRENT_USER86);
                    adaptor.addChild(root_0, CURRENT_USER86_tree);
                    }

                    }
                    break;
                case 13 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:444:5: CURRENT_ROLE
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    CURRENT_ROLE87=(Token)input.LT(1);
                    match(input,CURRENT_ROLE,FOLLOW_CURRENT_ROLE_in_value1088); if (failed) return retval;
                    if ( backtracking==0 ) {
                    CURRENT_ROLE87_tree = (CommonTree)adaptor.create(CURRENT_ROLE87);
                    adaptor.addChild(root_0, CURRENT_ROLE87_tree);
                    }

                    }
                    break;
                case 14 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:445:5: CURRENT_DATE
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    CURRENT_DATE88=(Token)input.LT(1);
                    match(input,CURRENT_DATE,FOLLOW_CURRENT_DATE_in_value1094); if (failed) return retval;
                    if ( backtracking==0 ) {
                    CURRENT_DATE88_tree = (CommonTree)adaptor.create(CURRENT_DATE88);
                    adaptor.addChild(root_0, CURRENT_DATE88_tree);
                    }

                    }
                    break;
                case 15 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:446:5: CURRENT_TIME
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    CURRENT_TIME89=(Token)input.LT(1);
                    match(input,CURRENT_TIME,FOLLOW_CURRENT_TIME_in_value1100); if (failed) return retval;
                    if ( backtracking==0 ) {
                    CURRENT_TIME89_tree = (CommonTree)adaptor.create(CURRENT_TIME89);
                    adaptor.addChild(root_0, CURRENT_TIME89_tree);
                    }

                    }
                    break;
                case 16 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:447:5: CURRENT_TIMESTAMP
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    CURRENT_TIMESTAMP90=(Token)input.LT(1);
                    match(input,CURRENT_TIMESTAMP,FOLLOW_CURRENT_TIMESTAMP_in_value1106); if (failed) return retval;
                    if ( backtracking==0 ) {
                    CURRENT_TIMESTAMP90_tree = (CommonTree)adaptor.create(CURRENT_TIMESTAMP90);
                    adaptor.addChild(root_0, CURRENT_TIMESTAMP90_tree);
                    }

                    }
                    break;
                case 17 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:449:5: nullValue
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_nullValue_in_value1115);
                    nullValue91=nullValue();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, nullValue91.getTree());

                    }
                    break;
                case 18 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:451:5: function
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_function_in_value1124);
                    function92=function();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, function92.getTree());

                    }
                    break;
                case 19 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:452:5: nextValueExpression
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_nextValueExpression_in_value1130);
                    nextValueExpression93=nextValueExpression();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, nextValueExpression93.getTree());

                    }
                    break;
                case 20 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:453:5: castExpression
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_castExpression_in_value1136);
                    castExpression94=castExpression();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, castExpression94.getTree());

                    }
                    break;
                case 21 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:456:5: arrayElement
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_arrayElement_in_value1146);
                    arrayElement95=arrayElement();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, arrayElement95.getTree());

                    }
                    break;
                case 22 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:458:5: DB_KEY
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    DB_KEY96=(Token)input.LT(1);
                    match(input,DB_KEY,FOLLOW_DB_KEY_in_value1155); if (failed) return retval;
                    if ( backtracking==0 ) {
                    DB_KEY96_tree = (CommonTree)adaptor.create(DB_KEY96);
                    adaptor.addChild(root_0, DB_KEY96_tree);
                    }

                    }
                    break;
                case 23 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:459:5: simpleIdentifier '.' DB_KEY
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_simpleIdentifier_in_value1161);
                    simpleIdentifier97=simpleIdentifier();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, simpleIdentifier97.getTree());
                    char_literal98=(Token)input.LT(1);
                    match(input,79,FOLLOW_79_in_value1163); if (failed) return retval;
                    if ( backtracking==0 ) {
                    char_literal98_tree = (CommonTree)adaptor.create(char_literal98);
                    adaptor.addChild(root_0, char_literal98_tree);
                    }
                    DB_KEY99=(Token)input.LT(1);
                    match(input,DB_KEY,FOLLOW_DB_KEY_in_value1165); if (failed) return retval;
                    if ( backtracking==0 ) {
                    DB_KEY99_tree = (CommonTree)adaptor.create(DB_KEY99);
                    adaptor.addChild(root_0, DB_KEY99_tree);
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 19, value_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end value

    public static class parameter_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start parameter
    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:462:1: parameter : '?' ;
    public final parameter_return parameter() throws RecognitionException {
        parameter_return retval = new parameter_return();
        retval.start = input.LT(1);
        int parameter_StartIndex = input.index();
        CommonTree root_0 = null;

        Token char_literal100=null;

        CommonTree char_literal100_tree=null;

        try {
            if ( backtracking>0 && alreadyParsedRule(input, 20) ) { return retval; }
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:463:3: ( '?' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:463:5: '?'
            {
            root_0 = (CommonTree)adaptor.nil();

            char_literal100=(Token)input.LT(1);
            match(input,85,FOLLOW_85_in_parameter1179); if (failed) return retval;
            if ( backtracking==0 ) {
            char_literal100_tree = (CommonTree)adaptor.create(char_literal100);
            adaptor.addChild(root_0, char_literal100_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 20, parameter_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end parameter

    public static class nullValue_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start nullValue
    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:466:1: nullValue : NULL ;
    public final nullValue_return nullValue() throws RecognitionException {
        nullValue_return retval = new nullValue_return();
        retval.start = input.LT(1);
        int nullValue_StartIndex = input.index();
        CommonTree root_0 = null;

        Token NULL101=null;

        CommonTree NULL101_tree=null;

        try {
            if ( backtracking>0 && alreadyParsedRule(input, 21) ) { return retval; }
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:467:3: ( NULL )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:467:5: NULL
            {
            root_0 = (CommonTree)adaptor.nil();

            NULL101=(Token)input.LT(1);
            match(input,NULL,FOLLOW_NULL_in_nullValue1192); if (failed) return retval;
            if ( backtracking==0 ) {
            NULL101_tree = (CommonTree)adaptor.create(NULL101);
            adaptor.addChild(root_0, NULL101_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 21, nullValue_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end nullValue

    public static class simpleValue_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start simpleValue
    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:470:1: simpleValue : ( GENERIC_ID | STRING | INTEGER | REAL );
    public final simpleValue_return simpleValue() throws RecognitionException {
        simpleValue_return retval = new simpleValue_return();
        retval.start = input.LT(1);
        int simpleValue_StartIndex = input.index();
        CommonTree root_0 = null;

        Token set102=null;

        CommonTree set102_tree=null;

        try {
            if ( backtracking>0 && alreadyParsedRule(input, 22) ) { return retval; }
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:471:3: ( GENERIC_ID | STRING | INTEGER | REAL )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:
            {
            root_0 = (CommonTree)adaptor.nil();

            set102=(Token)input.LT(1);
            if ( input.LA(1)==GENERIC_ID||(input.LA(1)>=STRING && input.LA(1)<=REAL) ) {
                input.consume();
                if ( backtracking==0 ) adaptor.addChild(root_0, adaptor.create(set102));
                errorRecovery=false;failed=false;
            }
            else {
                if (backtracking>0) {failed=true; return retval;}
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recoverFromMismatchedSet(input,mse,FOLLOW_set_in_simpleValue0);    throw mse;
            }


            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 22, simpleValue_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end simpleValue

    public static class nextValueExpression_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start nextValueExpression
    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:477:1: nextValueExpression : ( NEXT VALUE FOR simpleIdentifier | GEN_ID '(' simpleIdentifier ',' INTEGER ')' );
    public final nextValueExpression_return nextValueExpression() throws RecognitionException {
        nextValueExpression_return retval = new nextValueExpression_return();
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
        simpleIdentifier_return simpleIdentifier106 = null;

        simpleIdentifier_return simpleIdentifier109 = null;


        CommonTree NEXT103_tree=null;
        CommonTree VALUE104_tree=null;
        CommonTree FOR105_tree=null;
        CommonTree GEN_ID107_tree=null;
        CommonTree char_literal108_tree=null;
        CommonTree char_literal110_tree=null;
        CommonTree INTEGER111_tree=null;
        CommonTree char_literal112_tree=null;

        try {
            if ( backtracking>0 && alreadyParsedRule(input, 23) ) { return retval; }
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:478:3: ( NEXT VALUE FOR simpleIdentifier | GEN_ID '(' simpleIdentifier ',' INTEGER ')' )
            int alt14=2;
            int LA14_0 = input.LA(1);

            if ( (LA14_0==NEXT) ) {
                alt14=1;
            }
            else if ( (LA14_0==GEN_ID) ) {
                alt14=2;
            }
            else {
                if (backtracking>0) {failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("477:1: nextValueExpression : ( NEXT VALUE FOR simpleIdentifier | GEN_ID '(' simpleIdentifier ',' INTEGER ')' );", 14, 0, input);

                throw nvae;
            }
            switch (alt14) {
                case 1 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:478:5: NEXT VALUE FOR simpleIdentifier
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    NEXT103=(Token)input.LT(1);
                    match(input,NEXT,FOLLOW_NEXT_in_nextValueExpression1241); if (failed) return retval;
                    if ( backtracking==0 ) {
                    NEXT103_tree = (CommonTree)adaptor.create(NEXT103);
                    adaptor.addChild(root_0, NEXT103_tree);
                    }
                    VALUE104=(Token)input.LT(1);
                    match(input,VALUE,FOLLOW_VALUE_in_nextValueExpression1243); if (failed) return retval;
                    if ( backtracking==0 ) {
                    VALUE104_tree = (CommonTree)adaptor.create(VALUE104);
                    adaptor.addChild(root_0, VALUE104_tree);
                    }
                    FOR105=(Token)input.LT(1);
                    match(input,FOR,FOLLOW_FOR_in_nextValueExpression1245); if (failed) return retval;
                    if ( backtracking==0 ) {
                    FOR105_tree = (CommonTree)adaptor.create(FOR105);
                    adaptor.addChild(root_0, FOR105_tree);
                    }
                    pushFollow(FOLLOW_simpleIdentifier_in_nextValueExpression1247);
                    simpleIdentifier106=simpleIdentifier();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, simpleIdentifier106.getTree());

                    }
                    break;
                case 2 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:479:5: GEN_ID '(' simpleIdentifier ',' INTEGER ')'
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    GEN_ID107=(Token)input.LT(1);
                    match(input,GEN_ID,FOLLOW_GEN_ID_in_nextValueExpression1253); if (failed) return retval;
                    if ( backtracking==0 ) {
                    GEN_ID107_tree = (CommonTree)adaptor.create(GEN_ID107);
                    adaptor.addChild(root_0, GEN_ID107_tree);
                    }
                    char_literal108=(Token)input.LT(1);
                    match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_nextValueExpression1255); if (failed) return retval;
                    if ( backtracking==0 ) {
                    char_literal108_tree = (CommonTree)adaptor.create(char_literal108);
                    adaptor.addChild(root_0, char_literal108_tree);
                    }
                    pushFollow(FOLLOW_simpleIdentifier_in_nextValueExpression1257);
                    simpleIdentifier109=simpleIdentifier();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, simpleIdentifier109.getTree());
                    char_literal110=(Token)input.LT(1);
                    match(input,COMMA,FOLLOW_COMMA_in_nextValueExpression1259); if (failed) return retval;
                    if ( backtracking==0 ) {
                    char_literal110_tree = (CommonTree)adaptor.create(char_literal110);
                    adaptor.addChild(root_0, char_literal110_tree);
                    }
                    INTEGER111=(Token)input.LT(1);
                    match(input,INTEGER,FOLLOW_INTEGER_in_nextValueExpression1261); if (failed) return retval;
                    if ( backtracking==0 ) {
                    INTEGER111_tree = (CommonTree)adaptor.create(INTEGER111);
                    adaptor.addChild(root_0, INTEGER111_tree);
                    }
                    char_literal112=(Token)input.LT(1);
                    match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_nextValueExpression1263); if (failed) return retval;
                    if ( backtracking==0 ) {
                    char_literal112_tree = (CommonTree)adaptor.create(char_literal112);
                    adaptor.addChild(root_0, char_literal112_tree);
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 23, nextValueExpression_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end nextValueExpression

    public static class castExpression_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start castExpression
    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:482:1: castExpression : CAST '(' value AS dataTypeDescriptor ')' ;
    public final castExpression_return castExpression() throws RecognitionException {
        castExpression_return retval = new castExpression_return();
        retval.start = input.LT(1);
        int castExpression_StartIndex = input.index();
        CommonTree root_0 = null;

        Token CAST113=null;
        Token char_literal114=null;
        Token AS116=null;
        Token char_literal118=null;
        value_return value115 = null;

        dataTypeDescriptor_return dataTypeDescriptor117 = null;


        CommonTree CAST113_tree=null;
        CommonTree char_literal114_tree=null;
        CommonTree AS116_tree=null;
        CommonTree char_literal118_tree=null;

        try {
            if ( backtracking>0 && alreadyParsedRule(input, 24) ) { return retval; }
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:483:3: ( CAST '(' value AS dataTypeDescriptor ')' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:483:5: CAST '(' value AS dataTypeDescriptor ')'
            {
            root_0 = (CommonTree)adaptor.nil();

            CAST113=(Token)input.LT(1);
            match(input,CAST,FOLLOW_CAST_in_castExpression1278); if (failed) return retval;
            if ( backtracking==0 ) {
            CAST113_tree = (CommonTree)adaptor.create(CAST113);
            adaptor.addChild(root_0, CAST113_tree);
            }
            char_literal114=(Token)input.LT(1);
            match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_castExpression1280); if (failed) return retval;
            if ( backtracking==0 ) {
            char_literal114_tree = (CommonTree)adaptor.create(char_literal114);
            adaptor.addChild(root_0, char_literal114_tree);
            }
            pushFollow(FOLLOW_value_in_castExpression1282);
            value115=value();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, value115.getTree());
            AS116=(Token)input.LT(1);
            match(input,AS,FOLLOW_AS_in_castExpression1284); if (failed) return retval;
            if ( backtracking==0 ) {
            AS116_tree = (CommonTree)adaptor.create(AS116);
            adaptor.addChild(root_0, AS116_tree);
            }
            pushFollow(FOLLOW_dataTypeDescriptor_in_castExpression1286);
            dataTypeDescriptor117=dataTypeDescriptor();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, dataTypeDescriptor117.getTree());
            char_literal118=(Token)input.LT(1);
            match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_castExpression1288); if (failed) return retval;
            if ( backtracking==0 ) {
            char_literal118_tree = (CommonTree)adaptor.create(char_literal118);
            adaptor.addChild(root_0, char_literal118_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 24, castExpression_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end castExpression

    public static class dataTypeDescriptor_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start dataTypeDescriptor
    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:486:1: dataTypeDescriptor : ( nonArrayType | arrayType );
    public final dataTypeDescriptor_return dataTypeDescriptor() throws RecognitionException {
        dataTypeDescriptor_return retval = new dataTypeDescriptor_return();
        retval.start = input.LT(1);
        int dataTypeDescriptor_StartIndex = input.index();
        CommonTree root_0 = null;

        nonArrayType_return nonArrayType119 = null;

        arrayType_return arrayType120 = null;



        try {
            if ( backtracking>0 && alreadyParsedRule(input, 25) ) { return retval; }
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:487:3: ( nonArrayType | arrayType )
            int alt15=2;
            switch ( input.LA(1) ) {
            case KW_BIGINT:
                {
                int LA15_1 = input.LA(2);

                if ( (LA15_1==86) ) {
                    alt15=2;
                }
                else if ( (LA15_1==RIGHT_PAREN) ) {
                    alt15=1;
                }
                else {
                    if (backtracking>0) {failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("486:1: dataTypeDescriptor : ( nonArrayType | arrayType );", 15, 1, input);

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
                else if ( (LA15_2==86) ) {
                    alt15=2;
                }
                else {
                    if (backtracking>0) {failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("486:1: dataTypeDescriptor : ( nonArrayType | arrayType );", 15, 2, input);

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
                                    else if ( (LA15_26==86) ) {
                                        alt15=2;
                                    }
                                    else {
                                        if (backtracking>0) {failed=true; return retval;}
                                        NoViableAltException nvae =
                                            new NoViableAltException("486:1: dataTypeDescriptor : ( nonArrayType | arrayType );", 15, 26, input);

                                        throw nvae;
                                    }
                                }
                                else {
                                    if (backtracking>0) {failed=true; return retval;}
                                    NoViableAltException nvae =
                                        new NoViableAltException("486:1: dataTypeDescriptor : ( nonArrayType | arrayType );", 15, 31, input);

                                    throw nvae;
                                }
                            }
                            else {
                                if (backtracking>0) {failed=true; return retval;}
                                NoViableAltException nvae =
                                    new NoViableAltException("486:1: dataTypeDescriptor : ( nonArrayType | arrayType );", 15, 25, input);

                                throw nvae;
                            }
                        }
                        else if ( (LA15_21==RIGHT_PAREN) ) {
                            int LA15_26 = input.LA(5);

                            if ( (LA15_26==RIGHT_PAREN) ) {
                                alt15=1;
                            }
                            else if ( (LA15_26==86) ) {
                                alt15=2;
                            }
                            else {
                                if (backtracking>0) {failed=true; return retval;}
                                NoViableAltException nvae =
                                    new NoViableAltException("486:1: dataTypeDescriptor : ( nonArrayType | arrayType );", 15, 26, input);

                                throw nvae;
                            }
                        }
                        else {
                            if (backtracking>0) {failed=true; return retval;}
                            NoViableAltException nvae =
                                new NoViableAltException("486:1: dataTypeDescriptor : ( nonArrayType | arrayType );", 15, 21, input);

                            throw nvae;
                        }
                    }
                    else {
                        if (backtracking>0) {failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("486:1: dataTypeDescriptor : ( nonArrayType | arrayType );", 15, 16, input);

                        throw nvae;
                    }
                }
                else {
                    if (backtracking>0) {failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("486:1: dataTypeDescriptor : ( nonArrayType | arrayType );", 15, 3, input);

                    throw nvae;
                }
                }
                break;
            case KW_DOUBLE:
                {
                int LA15_4 = input.LA(2);

                if ( (LA15_4==KW_PRECISION) ) {
                    int LA15_17 = input.LA(3);

                    if ( (LA15_17==86) ) {
                        alt15=2;
                    }
                    else if ( (LA15_17==RIGHT_PAREN) ) {
                        alt15=1;
                    }
                    else {
                        if (backtracking>0) {failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("486:1: dataTypeDescriptor : ( nonArrayType | arrayType );", 15, 17, input);

                        throw nvae;
                    }
                }
                else {
                    if (backtracking>0) {failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("486:1: dataTypeDescriptor : ( nonArrayType | arrayType );", 15, 4, input);

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
                else if ( (LA15_5==86) ) {
                    alt15=2;
                }
                else {
                    if (backtracking>0) {failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("486:1: dataTypeDescriptor : ( nonArrayType | arrayType );", 15, 5, input);

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
                else if ( (LA15_6==86) ) {
                    alt15=2;
                }
                else {
                    if (backtracking>0) {failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("486:1: dataTypeDescriptor : ( nonArrayType | arrayType );", 15, 6, input);

                    throw nvae;
                }
                }
                break;
            case KW_INT:
                {
                int LA15_7 = input.LA(2);

                if ( (LA15_7==86) ) {
                    alt15=2;
                }
                else if ( (LA15_7==RIGHT_PAREN) ) {
                    alt15=1;
                }
                else {
                    if (backtracking>0) {failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("486:1: dataTypeDescriptor : ( nonArrayType | arrayType );", 15, 7, input);

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

                                    if ( (LA15_28==86) ) {
                                        alt15=2;
                                    }
                                    else if ( (LA15_28==RIGHT_PAREN) ) {
                                        alt15=1;
                                    }
                                    else {
                                        if (backtracking>0) {failed=true; return retval;}
                                        NoViableAltException nvae =
                                            new NoViableAltException("486:1: dataTypeDescriptor : ( nonArrayType | arrayType );", 15, 28, input);

                                        throw nvae;
                                    }
                                }
                                else {
                                    if (backtracking>0) {failed=true; return retval;}
                                    NoViableAltException nvae =
                                        new NoViableAltException("486:1: dataTypeDescriptor : ( nonArrayType | arrayType );", 15, 32, input);

                                    throw nvae;
                                }
                            }
                            else {
                                if (backtracking>0) {failed=true; return retval;}
                                NoViableAltException nvae =
                                    new NoViableAltException("486:1: dataTypeDescriptor : ( nonArrayType | arrayType );", 15, 27, input);

                                throw nvae;
                            }
                        }
                        else if ( (LA15_22==RIGHT_PAREN) ) {
                            int LA15_28 = input.LA(5);

                            if ( (LA15_28==86) ) {
                                alt15=2;
                            }
                            else if ( (LA15_28==RIGHT_PAREN) ) {
                                alt15=1;
                            }
                            else {
                                if (backtracking>0) {failed=true; return retval;}
                                NoViableAltException nvae =
                                    new NoViableAltException("486:1: dataTypeDescriptor : ( nonArrayType | arrayType );", 15, 28, input);

                                throw nvae;
                            }
                        }
                        else {
                            if (backtracking>0) {failed=true; return retval;}
                            NoViableAltException nvae =
                                new NoViableAltException("486:1: dataTypeDescriptor : ( nonArrayType | arrayType );", 15, 22, input);

                            throw nvae;
                        }
                    }
                    else {
                        if (backtracking>0) {failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("486:1: dataTypeDescriptor : ( nonArrayType | arrayType );", 15, 18, input);

                        throw nvae;
                    }
                }
                else {
                    if (backtracking>0) {failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("486:1: dataTypeDescriptor : ( nonArrayType | arrayType );", 15, 8, input);

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
                else if ( (LA15_9==86) ) {
                    alt15=2;
                }
                else {
                    if (backtracking>0) {failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("486:1: dataTypeDescriptor : ( nonArrayType | arrayType );", 15, 9, input);

                    throw nvae;
                }
                }
                break;
            case KW_TIME:
                {
                int LA15_10 = input.LA(2);

                if ( (LA15_10==86) ) {
                    alt15=2;
                }
                else if ( (LA15_10==RIGHT_PAREN) ) {
                    alt15=1;
                }
                else {
                    if (backtracking>0) {failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("486:1: dataTypeDescriptor : ( nonArrayType | arrayType );", 15, 10, input);

                    throw nvae;
                }
                }
                break;
            case KW_TIMESTAMP:
                {
                int LA15_11 = input.LA(2);

                if ( (LA15_11==86) ) {
                    alt15=2;
                }
                else if ( (LA15_11==RIGHT_PAREN) ) {
                    alt15=1;
                }
                else {
                    if (backtracking>0) {failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("486:1: dataTypeDescriptor : ( nonArrayType | arrayType );", 15, 11, input);

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
                            else if ( (LA15_29==86) ) {
                                alt15=2;
                            }
                            else {
                                if (backtracking>0) {failed=true; return retval;}
                                NoViableAltException nvae =
                                    new NoViableAltException("486:1: dataTypeDescriptor : ( nonArrayType | arrayType );", 15, 29, input);

                                throw nvae;
                            }
                        }
                        else {
                            if (backtracking>0) {failed=true; return retval;}
                            NoViableAltException nvae =
                                new NoViableAltException("486:1: dataTypeDescriptor : ( nonArrayType | arrayType );", 15, 23, input);

                            throw nvae;
                        }
                    }
                    else {
                        if (backtracking>0) {failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("486:1: dataTypeDescriptor : ( nonArrayType | arrayType );", 15, 19, input);

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
                case 86:
                    {
                    alt15=2;
                    }
                    break;
                default:
                    if (backtracking>0) {failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("486:1: dataTypeDescriptor : ( nonArrayType | arrayType );", 15, 12, input);

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
                            else if ( (LA15_30==86) ) {
                                alt15=2;
                            }
                            else {
                                if (backtracking>0) {failed=true; return retval;}
                                NoViableAltException nvae =
                                    new NoViableAltException("486:1: dataTypeDescriptor : ( nonArrayType | arrayType );", 15, 30, input);

                                throw nvae;
                            }
                        }
                        else {
                            if (backtracking>0) {failed=true; return retval;}
                            NoViableAltException nvae =
                                new NoViableAltException("486:1: dataTypeDescriptor : ( nonArrayType | arrayType );", 15, 24, input);

                            throw nvae;
                        }
                    }
                    else {
                        if (backtracking>0) {failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("486:1: dataTypeDescriptor : ( nonArrayType | arrayType );", 15, 20, input);

                        throw nvae;
                    }
                }
                else {
                    if (backtracking>0) {failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("486:1: dataTypeDescriptor : ( nonArrayType | arrayType );", 15, 13, input);

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
                if (backtracking>0) {failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("486:1: dataTypeDescriptor : ( nonArrayType | arrayType );", 15, 0, input);

                throw nvae;
            }

            switch (alt15) {
                case 1 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:487:5: nonArrayType
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_nonArrayType_in_dataTypeDescriptor1303);
                    nonArrayType119=nonArrayType();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, nonArrayType119.getTree());

                    }
                    break;
                case 2 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:488:5: arrayType
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_arrayType_in_dataTypeDescriptor1309);
                    arrayType120=arrayType();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, arrayType120.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 25, dataTypeDescriptor_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end dataTypeDescriptor

    public static class nonArrayType_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start nonArrayType
    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:491:1: nonArrayType : ( simpleType | blobType );
    public final nonArrayType_return nonArrayType() throws RecognitionException {
        nonArrayType_return retval = new nonArrayType_return();
        retval.start = input.LT(1);
        int nonArrayType_StartIndex = input.index();
        CommonTree root_0 = null;

        simpleType_return simpleType121 = null;

        blobType_return blobType122 = null;



        try {
            if ( backtracking>0 && alreadyParsedRule(input, 26) ) { return retval; }
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:492:3: ( simpleType | blobType )
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( ((LA16_0>=KW_BIGINT && LA16_0<=KW_DOUBLE)||(LA16_0>=KW_FLOAT && LA16_0<=KW_VARCHAR)) ) {
                alt16=1;
            }
            else if ( (LA16_0==KW_BLOB) ) {
                alt16=2;
            }
            else {
                if (backtracking>0) {failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("491:1: nonArrayType : ( simpleType | blobType );", 16, 0, input);

                throw nvae;
            }
            switch (alt16) {
                case 1 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:492:5: simpleType
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_simpleType_in_nonArrayType1324);
                    simpleType121=simpleType();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, simpleType121.getTree());

                    }
                    break;
                case 2 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:493:5: blobType
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_blobType_in_nonArrayType1330);
                    blobType122=blobType();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, blobType122.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 26, nonArrayType_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end nonArrayType

    public static class simpleType_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start simpleType
    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:496:1: simpleType : ( nonCharType | charType );
    public final simpleType_return simpleType() throws RecognitionException {
        simpleType_return retval = new simpleType_return();
        retval.start = input.LT(1);
        int simpleType_StartIndex = input.index();
        CommonTree root_0 = null;

        nonCharType_return nonCharType123 = null;

        charType_return charType124 = null;



        try {
            if ( backtracking>0 && alreadyParsedRule(input, 27) ) { return retval; }
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:497:3: ( nonCharType | charType )
            int alt17=2;
            int LA17_0 = input.LA(1);

            if ( (LA17_0==KW_BIGINT||(LA17_0>=KW_DATE && LA17_0<=KW_DOUBLE)||(LA17_0>=KW_FLOAT && LA17_0<=KW_TIMESTAMP)) ) {
                alt17=1;
            }
            else if ( (LA17_0==KW_CHAR||LA17_0==KW_VARCHAR) ) {
                alt17=2;
            }
            else {
                if (backtracking>0) {failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("496:1: simpleType : ( nonCharType | charType );", 17, 0, input);

                throw nvae;
            }
            switch (alt17) {
                case 1 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:497:5: nonCharType
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_nonCharType_in_simpleType1343);
                    nonCharType123=nonCharType();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, nonCharType123.getTree());

                    }
                    break;
                case 2 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:498:5: charType
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_charType_in_simpleType1349);
                    charType124=charType();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, charType124.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 27, simpleType_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end simpleType

    public static class charType_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start charType
    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:501:1: charType : ( nonCharSetCharType | charSetCharType );
    public final charType_return charType() throws RecognitionException {
        charType_return retval = new charType_return();
        retval.start = input.LT(1);
        int charType_StartIndex = input.index();
        CommonTree root_0 = null;

        nonCharSetCharType_return nonCharSetCharType125 = null;

        charSetCharType_return charSetCharType126 = null;



        try {
            if ( backtracking>0 && alreadyParsedRule(input, 28) ) { return retval; }
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:502:3: ( nonCharSetCharType | charSetCharType )
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

                            if ( (LA18_9==CHARACTER) ) {
                                alt18=2;
                            }
                            else if ( (LA18_9==EOF||LA18_9==RIGHT_PAREN) ) {
                                alt18=1;
                            }
                            else {
                                if (backtracking>0) {failed=true; return retval;}
                                NoViableAltException nvae =
                                    new NoViableAltException("501:1: charType : ( nonCharSetCharType | charSetCharType );", 18, 9, input);

                                throw nvae;
                            }
                        }
                        else {
                            if (backtracking>0) {failed=true; return retval;}
                            NoViableAltException nvae =
                                new NoViableAltException("501:1: charType : ( nonCharSetCharType | charSetCharType );", 18, 7, input);

                            throw nvae;
                        }
                    }
                    else {
                        if (backtracking>0) {failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("501:1: charType : ( nonCharSetCharType | charSetCharType );", 18, 3, input);

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
                    if (backtracking>0) {failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("501:1: charType : ( nonCharSetCharType | charSetCharType );", 18, 1, input);

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
                                if (backtracking>0) {failed=true; return retval;}
                                NoViableAltException nvae =
                                    new NoViableAltException("501:1: charType : ( nonCharSetCharType | charSetCharType );", 18, 10, input);

                                throw nvae;
                            }
                        }
                        else {
                            if (backtracking>0) {failed=true; return retval;}
                            NoViableAltException nvae =
                                new NoViableAltException("501:1: charType : ( nonCharSetCharType | charSetCharType );", 18, 8, input);

                            throw nvae;
                        }
                    }
                    else {
                        if (backtracking>0) {failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("501:1: charType : ( nonCharSetCharType | charSetCharType );", 18, 6, input);

                        throw nvae;
                    }
                }
                else {
                    if (backtracking>0) {failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("501:1: charType : ( nonCharSetCharType | charSetCharType );", 18, 2, input);

                    throw nvae;
                }
            }
            else {
                if (backtracking>0) {failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("501:1: charType : ( nonCharSetCharType | charSetCharType );", 18, 0, input);

                throw nvae;
            }
            switch (alt18) {
                case 1 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:502:5: nonCharSetCharType
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_nonCharSetCharType_in_charType1364);
                    nonCharSetCharType125=nonCharSetCharType();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, nonCharSetCharType125.getTree());

                    }
                    break;
                case 2 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:503:5: charSetCharType
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_charSetCharType_in_charType1370);
                    charSetCharType126=charSetCharType();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, charSetCharType126.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 28, charType_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end charType

    public static class nonCharSetCharType_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start nonCharSetCharType
    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:506:1: nonCharSetCharType : ( KW_CHAR ( '(' INTEGER ')' )? | KW_VARCHAR '(' INTEGER ')' );
    public final nonCharSetCharType_return nonCharSetCharType() throws RecognitionException {
        nonCharSetCharType_return retval = new nonCharSetCharType_return();
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
            if ( backtracking>0 && alreadyParsedRule(input, 29) ) { return retval; }
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:507:3: ( KW_CHAR ( '(' INTEGER ')' )? | KW_VARCHAR '(' INTEGER ')' )
            int alt20=2;
            int LA20_0 = input.LA(1);

            if ( (LA20_0==KW_CHAR) ) {
                alt20=1;
            }
            else if ( (LA20_0==KW_VARCHAR) ) {
                alt20=2;
            }
            else {
                if (backtracking>0) {failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("506:1: nonCharSetCharType : ( KW_CHAR ( '(' INTEGER ')' )? | KW_VARCHAR '(' INTEGER ')' );", 20, 0, input);

                throw nvae;
            }
            switch (alt20) {
                case 1 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:507:5: KW_CHAR ( '(' INTEGER ')' )?
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    KW_CHAR127=(Token)input.LT(1);
                    match(input,KW_CHAR,FOLLOW_KW_CHAR_in_nonCharSetCharType1383); if (failed) return retval;
                    if ( backtracking==0 ) {
                    KW_CHAR127_tree = (CommonTree)adaptor.create(KW_CHAR127);
                    adaptor.addChild(root_0, KW_CHAR127_tree);
                    }
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:507:13: ( '(' INTEGER ')' )?
                    int alt19=2;
                    int LA19_0 = input.LA(1);

                    if ( (LA19_0==LEFT_PAREN) ) {
                        alt19=1;
                    }
                    switch (alt19) {
                        case 1 :
                            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:507:14: '(' INTEGER ')'
                            {
                            char_literal128=(Token)input.LT(1);
                            match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_nonCharSetCharType1386); if (failed) return retval;
                            if ( backtracking==0 ) {
                            char_literal128_tree = (CommonTree)adaptor.create(char_literal128);
                            adaptor.addChild(root_0, char_literal128_tree);
                            }
                            INTEGER129=(Token)input.LT(1);
                            match(input,INTEGER,FOLLOW_INTEGER_in_nonCharSetCharType1388); if (failed) return retval;
                            if ( backtracking==0 ) {
                            INTEGER129_tree = (CommonTree)adaptor.create(INTEGER129);
                            adaptor.addChild(root_0, INTEGER129_tree);
                            }
                            char_literal130=(Token)input.LT(1);
                            match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_nonCharSetCharType1390); if (failed) return retval;
                            if ( backtracking==0 ) {
                            char_literal130_tree = (CommonTree)adaptor.create(char_literal130);
                            adaptor.addChild(root_0, char_literal130_tree);
                            }

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:508:5: KW_VARCHAR '(' INTEGER ')'
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    KW_VARCHAR131=(Token)input.LT(1);
                    match(input,KW_VARCHAR,FOLLOW_KW_VARCHAR_in_nonCharSetCharType1398); if (failed) return retval;
                    if ( backtracking==0 ) {
                    KW_VARCHAR131_tree = (CommonTree)adaptor.create(KW_VARCHAR131);
                    adaptor.addChild(root_0, KW_VARCHAR131_tree);
                    }
                    char_literal132=(Token)input.LT(1);
                    match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_nonCharSetCharType1400); if (failed) return retval;
                    if ( backtracking==0 ) {
                    char_literal132_tree = (CommonTree)adaptor.create(char_literal132);
                    adaptor.addChild(root_0, char_literal132_tree);
                    }
                    INTEGER133=(Token)input.LT(1);
                    match(input,INTEGER,FOLLOW_INTEGER_in_nonCharSetCharType1402); if (failed) return retval;
                    if ( backtracking==0 ) {
                    INTEGER133_tree = (CommonTree)adaptor.create(INTEGER133);
                    adaptor.addChild(root_0, INTEGER133_tree);
                    }
                    char_literal134=(Token)input.LT(1);
                    match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_nonCharSetCharType1404); if (failed) return retval;
                    if ( backtracking==0 ) {
                    char_literal134_tree = (CommonTree)adaptor.create(char_literal134);
                    adaptor.addChild(root_0, char_literal134_tree);
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 29, nonCharSetCharType_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end nonCharSetCharType

    public static class charSetCharType_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start charSetCharType
    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:511:1: charSetCharType : nonCharSetCharType charSetClause ;
    public final charSetCharType_return charSetCharType() throws RecognitionException {
        charSetCharType_return retval = new charSetCharType_return();
        retval.start = input.LT(1);
        int charSetCharType_StartIndex = input.index();
        CommonTree root_0 = null;

        nonCharSetCharType_return nonCharSetCharType135 = null;

        charSetClause_return charSetClause136 = null;



        try {
            if ( backtracking>0 && alreadyParsedRule(input, 30) ) { return retval; }
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:512:3: ( nonCharSetCharType charSetClause )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:512:5: nonCharSetCharType charSetClause
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_nonCharSetCharType_in_charSetCharType1417);
            nonCharSetCharType135=nonCharSetCharType();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, nonCharSetCharType135.getTree());
            pushFollow(FOLLOW_charSetClause_in_charSetCharType1419);
            charSetClause136=charSetClause();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, charSetClause136.getTree());

            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 30, charSetCharType_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end charSetCharType

    public static class nonCharType_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start nonCharType
    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:515:1: nonCharType : ( KW_BIGINT | KW_DATE | KW_DECIMAL '(' INTEGER ( ',' INTEGER )? ')' | KW_DOUBLE KW_PRECISION | KW_FLOAT | KW_INTEGER | KW_INT | KW_NUMERIC '(' INTEGER ( ',' INTEGER )? ')' | KW_SMALLINT | KW_TIME | KW_TIMESTAMP );
    public final nonCharType_return nonCharType() throws RecognitionException {
        nonCharType_return retval = new nonCharType_return();
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
            if ( backtracking>0 && alreadyParsedRule(input, 31) ) { return retval; }
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:516:3: ( KW_BIGINT | KW_DATE | KW_DECIMAL '(' INTEGER ( ',' INTEGER )? ')' | KW_DOUBLE KW_PRECISION | KW_FLOAT | KW_INTEGER | KW_INT | KW_NUMERIC '(' INTEGER ( ',' INTEGER )? ')' | KW_SMALLINT | KW_TIME | KW_TIMESTAMP )
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
                if (backtracking>0) {failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("515:1: nonCharType : ( KW_BIGINT | KW_DATE | KW_DECIMAL '(' INTEGER ( ',' INTEGER )? ')' | KW_DOUBLE KW_PRECISION | KW_FLOAT | KW_INTEGER | KW_INT | KW_NUMERIC '(' INTEGER ( ',' INTEGER )? ')' | KW_SMALLINT | KW_TIME | KW_TIMESTAMP );", 23, 0, input);

                throw nvae;
            }

            switch (alt23) {
                case 1 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:516:5: KW_BIGINT
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    KW_BIGINT137=(Token)input.LT(1);
                    match(input,KW_BIGINT,FOLLOW_KW_BIGINT_in_nonCharType1432); if (failed) return retval;
                    if ( backtracking==0 ) {
                    KW_BIGINT137_tree = (CommonTree)adaptor.create(KW_BIGINT137);
                    adaptor.addChild(root_0, KW_BIGINT137_tree);
                    }

                    }
                    break;
                case 2 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:517:5: KW_DATE
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    KW_DATE138=(Token)input.LT(1);
                    match(input,KW_DATE,FOLLOW_KW_DATE_in_nonCharType1438); if (failed) return retval;
                    if ( backtracking==0 ) {
                    KW_DATE138_tree = (CommonTree)adaptor.create(KW_DATE138);
                    adaptor.addChild(root_0, KW_DATE138_tree);
                    }

                    }
                    break;
                case 3 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:518:5: KW_DECIMAL '(' INTEGER ( ',' INTEGER )? ')'
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    KW_DECIMAL139=(Token)input.LT(1);
                    match(input,KW_DECIMAL,FOLLOW_KW_DECIMAL_in_nonCharType1444); if (failed) return retval;
                    if ( backtracking==0 ) {
                    KW_DECIMAL139_tree = (CommonTree)adaptor.create(KW_DECIMAL139);
                    adaptor.addChild(root_0, KW_DECIMAL139_tree);
                    }
                    char_literal140=(Token)input.LT(1);
                    match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_nonCharType1446); if (failed) return retval;
                    if ( backtracking==0 ) {
                    char_literal140_tree = (CommonTree)adaptor.create(char_literal140);
                    adaptor.addChild(root_0, char_literal140_tree);
                    }
                    INTEGER141=(Token)input.LT(1);
                    match(input,INTEGER,FOLLOW_INTEGER_in_nonCharType1448); if (failed) return retval;
                    if ( backtracking==0 ) {
                    INTEGER141_tree = (CommonTree)adaptor.create(INTEGER141);
                    adaptor.addChild(root_0, INTEGER141_tree);
                    }
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:518:28: ( ',' INTEGER )?
                    int alt21=2;
                    int LA21_0 = input.LA(1);

                    if ( (LA21_0==COMMA) ) {
                        alt21=1;
                    }
                    switch (alt21) {
                        case 1 :
                            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:518:29: ',' INTEGER
                            {
                            char_literal142=(Token)input.LT(1);
                            match(input,COMMA,FOLLOW_COMMA_in_nonCharType1451); if (failed) return retval;
                            if ( backtracking==0 ) {
                            char_literal142_tree = (CommonTree)adaptor.create(char_literal142);
                            adaptor.addChild(root_0, char_literal142_tree);
                            }
                            INTEGER143=(Token)input.LT(1);
                            match(input,INTEGER,FOLLOW_INTEGER_in_nonCharType1453); if (failed) return retval;
                            if ( backtracking==0 ) {
                            INTEGER143_tree = (CommonTree)adaptor.create(INTEGER143);
                            adaptor.addChild(root_0, INTEGER143_tree);
                            }

                            }
                            break;

                    }

                    char_literal144=(Token)input.LT(1);
                    match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_nonCharType1457); if (failed) return retval;
                    if ( backtracking==0 ) {
                    char_literal144_tree = (CommonTree)adaptor.create(char_literal144);
                    adaptor.addChild(root_0, char_literal144_tree);
                    }

                    }
                    break;
                case 4 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:519:5: KW_DOUBLE KW_PRECISION
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    KW_DOUBLE145=(Token)input.LT(1);
                    match(input,KW_DOUBLE,FOLLOW_KW_DOUBLE_in_nonCharType1463); if (failed) return retval;
                    if ( backtracking==0 ) {
                    KW_DOUBLE145_tree = (CommonTree)adaptor.create(KW_DOUBLE145);
                    adaptor.addChild(root_0, KW_DOUBLE145_tree);
                    }
                    KW_PRECISION146=(Token)input.LT(1);
                    match(input,KW_PRECISION,FOLLOW_KW_PRECISION_in_nonCharType1465); if (failed) return retval;
                    if ( backtracking==0 ) {
                    KW_PRECISION146_tree = (CommonTree)adaptor.create(KW_PRECISION146);
                    adaptor.addChild(root_0, KW_PRECISION146_tree);
                    }

                    }
                    break;
                case 5 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:520:5: KW_FLOAT
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    KW_FLOAT147=(Token)input.LT(1);
                    match(input,KW_FLOAT,FOLLOW_KW_FLOAT_in_nonCharType1471); if (failed) return retval;
                    if ( backtracking==0 ) {
                    KW_FLOAT147_tree = (CommonTree)adaptor.create(KW_FLOAT147);
                    adaptor.addChild(root_0, KW_FLOAT147_tree);
                    }

                    }
                    break;
                case 6 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:521:5: KW_INTEGER
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    KW_INTEGER148=(Token)input.LT(1);
                    match(input,KW_INTEGER,FOLLOW_KW_INTEGER_in_nonCharType1477); if (failed) return retval;
                    if ( backtracking==0 ) {
                    KW_INTEGER148_tree = (CommonTree)adaptor.create(KW_INTEGER148);
                    adaptor.addChild(root_0, KW_INTEGER148_tree);
                    }

                    }
                    break;
                case 7 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:522:5: KW_INT
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    KW_INT149=(Token)input.LT(1);
                    match(input,KW_INT,FOLLOW_KW_INT_in_nonCharType1483); if (failed) return retval;
                    if ( backtracking==0 ) {
                    KW_INT149_tree = (CommonTree)adaptor.create(KW_INT149);
                    adaptor.addChild(root_0, KW_INT149_tree);
                    }

                    }
                    break;
                case 8 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:523:5: KW_NUMERIC '(' INTEGER ( ',' INTEGER )? ')'
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    KW_NUMERIC150=(Token)input.LT(1);
                    match(input,KW_NUMERIC,FOLLOW_KW_NUMERIC_in_nonCharType1489); if (failed) return retval;
                    if ( backtracking==0 ) {
                    KW_NUMERIC150_tree = (CommonTree)adaptor.create(KW_NUMERIC150);
                    adaptor.addChild(root_0, KW_NUMERIC150_tree);
                    }
                    char_literal151=(Token)input.LT(1);
                    match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_nonCharType1491); if (failed) return retval;
                    if ( backtracking==0 ) {
                    char_literal151_tree = (CommonTree)adaptor.create(char_literal151);
                    adaptor.addChild(root_0, char_literal151_tree);
                    }
                    INTEGER152=(Token)input.LT(1);
                    match(input,INTEGER,FOLLOW_INTEGER_in_nonCharType1493); if (failed) return retval;
                    if ( backtracking==0 ) {
                    INTEGER152_tree = (CommonTree)adaptor.create(INTEGER152);
                    adaptor.addChild(root_0, INTEGER152_tree);
                    }
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:523:28: ( ',' INTEGER )?
                    int alt22=2;
                    int LA22_0 = input.LA(1);

                    if ( (LA22_0==COMMA) ) {
                        alt22=1;
                    }
                    switch (alt22) {
                        case 1 :
                            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:523:29: ',' INTEGER
                            {
                            char_literal153=(Token)input.LT(1);
                            match(input,COMMA,FOLLOW_COMMA_in_nonCharType1496); if (failed) return retval;
                            if ( backtracking==0 ) {
                            char_literal153_tree = (CommonTree)adaptor.create(char_literal153);
                            adaptor.addChild(root_0, char_literal153_tree);
                            }
                            INTEGER154=(Token)input.LT(1);
                            match(input,INTEGER,FOLLOW_INTEGER_in_nonCharType1498); if (failed) return retval;
                            if ( backtracking==0 ) {
                            INTEGER154_tree = (CommonTree)adaptor.create(INTEGER154);
                            adaptor.addChild(root_0, INTEGER154_tree);
                            }

                            }
                            break;

                    }

                    char_literal155=(Token)input.LT(1);
                    match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_nonCharType1502); if (failed) return retval;
                    if ( backtracking==0 ) {
                    char_literal155_tree = (CommonTree)adaptor.create(char_literal155);
                    adaptor.addChild(root_0, char_literal155_tree);
                    }

                    }
                    break;
                case 9 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:524:5: KW_SMALLINT
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    KW_SMALLINT156=(Token)input.LT(1);
                    match(input,KW_SMALLINT,FOLLOW_KW_SMALLINT_in_nonCharType1508); if (failed) return retval;
                    if ( backtracking==0 ) {
                    KW_SMALLINT156_tree = (CommonTree)adaptor.create(KW_SMALLINT156);
                    adaptor.addChild(root_0, KW_SMALLINT156_tree);
                    }

                    }
                    break;
                case 10 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:525:5: KW_TIME
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    KW_TIME157=(Token)input.LT(1);
                    match(input,KW_TIME,FOLLOW_KW_TIME_in_nonCharType1514); if (failed) return retval;
                    if ( backtracking==0 ) {
                    KW_TIME157_tree = (CommonTree)adaptor.create(KW_TIME157);
                    adaptor.addChild(root_0, KW_TIME157_tree);
                    }

                    }
                    break;
                case 11 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:526:5: KW_TIMESTAMP
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    KW_TIMESTAMP158=(Token)input.LT(1);
                    match(input,KW_TIMESTAMP,FOLLOW_KW_TIMESTAMP_in_nonCharType1520); if (failed) return retval;
                    if ( backtracking==0 ) {
                    KW_TIMESTAMP158_tree = (CommonTree)adaptor.create(KW_TIMESTAMP158);
                    adaptor.addChild(root_0, KW_TIMESTAMP158_tree);
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 31, nonCharType_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end nonCharType

    public static class blobType_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start blobType
    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:529:1: blobType : ( KW_BLOB ( blobSubtype )? ( blobSegSize )? ( charSetClause )? | KW_BLOB '(' ( INTEGER )? ( ',' INTEGER )? ')' );
    public final blobType_return blobType() throws RecognitionException {
        blobType_return retval = new blobType_return();
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
        blobSubtype_return blobSubtype160 = null;

        blobSegSize_return blobSegSize161 = null;

        charSetClause_return charSetClause162 = null;


        CommonTree KW_BLOB159_tree=null;
        CommonTree KW_BLOB163_tree=null;
        CommonTree char_literal164_tree=null;
        CommonTree INTEGER165_tree=null;
        CommonTree char_literal166_tree=null;
        CommonTree INTEGER167_tree=null;
        CommonTree char_literal168_tree=null;

        try {
            if ( backtracking>0 && alreadyParsedRule(input, 32) ) { return retval; }
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:530:3: ( KW_BLOB ( blobSubtype )? ( blobSegSize )? ( charSetClause )? | KW_BLOB '(' ( INTEGER )? ( ',' INTEGER )? ')' )
            int alt29=2;
            int LA29_0 = input.LA(1);

            if ( (LA29_0==KW_BLOB) ) {
                int LA29_1 = input.LA(2);

                if ( (LA29_1==LEFT_PAREN) ) {
                    alt29=2;
                }
                else if ( (LA29_1==EOF||LA29_1==CHARACTER||LA29_1==SEGMENT||LA29_1==SUB_TYPE||LA29_1==RIGHT_PAREN) ) {
                    alt29=1;
                }
                else {
                    if (backtracking>0) {failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("529:1: blobType : ( KW_BLOB ( blobSubtype )? ( blobSegSize )? ( charSetClause )? | KW_BLOB '(' ( INTEGER )? ( ',' INTEGER )? ')' );", 29, 1, input);

                    throw nvae;
                }
            }
            else {
                if (backtracking>0) {failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("529:1: blobType : ( KW_BLOB ( blobSubtype )? ( blobSegSize )? ( charSetClause )? | KW_BLOB '(' ( INTEGER )? ( ',' INTEGER )? ')' );", 29, 0, input);

                throw nvae;
            }
            switch (alt29) {
                case 1 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:530:5: KW_BLOB ( blobSubtype )? ( blobSegSize )? ( charSetClause )?
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    KW_BLOB159=(Token)input.LT(1);
                    match(input,KW_BLOB,FOLLOW_KW_BLOB_in_blobType1534); if (failed) return retval;
                    if ( backtracking==0 ) {
                    KW_BLOB159_tree = (CommonTree)adaptor.create(KW_BLOB159);
                    adaptor.addChild(root_0, KW_BLOB159_tree);
                    }
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:530:13: ( blobSubtype )?
                    int alt24=2;
                    int LA24_0 = input.LA(1);

                    if ( (LA24_0==SUB_TYPE) ) {
                        alt24=1;
                    }
                    switch (alt24) {
                        case 1 :
                            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:0:0: blobSubtype
                            {
                            pushFollow(FOLLOW_blobSubtype_in_blobType1536);
                            blobSubtype160=blobSubtype();
                            _fsp--;
                            if (failed) return retval;
                            if ( backtracking==0 ) adaptor.addChild(root_0, blobSubtype160.getTree());

                            }
                            break;

                    }

                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:530:26: ( blobSegSize )?
                    int alt25=2;
                    int LA25_0 = input.LA(1);

                    if ( (LA25_0==SEGMENT) ) {
                        alt25=1;
                    }
                    switch (alt25) {
                        case 1 :
                            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:0:0: blobSegSize
                            {
                            pushFollow(FOLLOW_blobSegSize_in_blobType1539);
                            blobSegSize161=blobSegSize();
                            _fsp--;
                            if (failed) return retval;
                            if ( backtracking==0 ) adaptor.addChild(root_0, blobSegSize161.getTree());

                            }
                            break;

                    }

                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:530:39: ( charSetClause )?
                    int alt26=2;
                    int LA26_0 = input.LA(1);

                    if ( (LA26_0==CHARACTER) ) {
                        alt26=1;
                    }
                    switch (alt26) {
                        case 1 :
                            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:0:0: charSetClause
                            {
                            pushFollow(FOLLOW_charSetClause_in_blobType1542);
                            charSetClause162=charSetClause();
                            _fsp--;
                            if (failed) return retval;
                            if ( backtracking==0 ) adaptor.addChild(root_0, charSetClause162.getTree());

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:532:5: KW_BLOB '(' ( INTEGER )? ( ',' INTEGER )? ')'
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    KW_BLOB163=(Token)input.LT(1);
                    match(input,KW_BLOB,FOLLOW_KW_BLOB_in_blobType1554); if (failed) return retval;
                    if ( backtracking==0 ) {
                    KW_BLOB163_tree = (CommonTree)adaptor.create(KW_BLOB163);
                    adaptor.addChild(root_0, KW_BLOB163_tree);
                    }
                    char_literal164=(Token)input.LT(1);
                    match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_blobType1556); if (failed) return retval;
                    if ( backtracking==0 ) {
                    char_literal164_tree = (CommonTree)adaptor.create(char_literal164);
                    adaptor.addChild(root_0, char_literal164_tree);
                    }
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:532:17: ( INTEGER )?
                    int alt27=2;
                    int LA27_0 = input.LA(1);

                    if ( (LA27_0==INTEGER) ) {
                        alt27=1;
                    }
                    switch (alt27) {
                        case 1 :
                            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:0:0: INTEGER
                            {
                            INTEGER165=(Token)input.LT(1);
                            match(input,INTEGER,FOLLOW_INTEGER_in_blobType1558); if (failed) return retval;
                            if ( backtracking==0 ) {
                            INTEGER165_tree = (CommonTree)adaptor.create(INTEGER165);
                            adaptor.addChild(root_0, INTEGER165_tree);
                            }

                            }
                            break;

                    }

                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:532:26: ( ',' INTEGER )?
                    int alt28=2;
                    int LA28_0 = input.LA(1);

                    if ( (LA28_0==COMMA) ) {
                        alt28=1;
                    }
                    switch (alt28) {
                        case 1 :
                            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:532:27: ',' INTEGER
                            {
                            char_literal166=(Token)input.LT(1);
                            match(input,COMMA,FOLLOW_COMMA_in_blobType1562); if (failed) return retval;
                            if ( backtracking==0 ) {
                            char_literal166_tree = (CommonTree)adaptor.create(char_literal166);
                            adaptor.addChild(root_0, char_literal166_tree);
                            }
                            INTEGER167=(Token)input.LT(1);
                            match(input,INTEGER,FOLLOW_INTEGER_in_blobType1564); if (failed) return retval;
                            if ( backtracking==0 ) {
                            INTEGER167_tree = (CommonTree)adaptor.create(INTEGER167);
                            adaptor.addChild(root_0, INTEGER167_tree);
                            }

                            }
                            break;

                    }

                    char_literal168=(Token)input.LT(1);
                    match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_blobType1568); if (failed) return retval;
                    if ( backtracking==0 ) {
                    char_literal168_tree = (CommonTree)adaptor.create(char_literal168);
                    adaptor.addChild(root_0, char_literal168_tree);
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 32, blobType_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end blobType

    public static class blobSubtype_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start blobSubtype
    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:535:1: blobSubtype : ( SUB_TYPE INTEGER | SUB_TYPE GENERIC_ID );
    public final blobSubtype_return blobSubtype() throws RecognitionException {
        blobSubtype_return retval = new blobSubtype_return();
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
            if ( backtracking>0 && alreadyParsedRule(input, 33) ) { return retval; }
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:536:3: ( SUB_TYPE INTEGER | SUB_TYPE GENERIC_ID )
            int alt30=2;
            int LA30_0 = input.LA(1);

            if ( (LA30_0==SUB_TYPE) ) {
                int LA30_1 = input.LA(2);

                if ( (LA30_1==GENERIC_ID) ) {
                    alt30=2;
                }
                else if ( (LA30_1==INTEGER) ) {
                    alt30=1;
                }
                else {
                    if (backtracking>0) {failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("535:1: blobSubtype : ( SUB_TYPE INTEGER | SUB_TYPE GENERIC_ID );", 30, 1, input);

                    throw nvae;
                }
            }
            else {
                if (backtracking>0) {failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("535:1: blobSubtype : ( SUB_TYPE INTEGER | SUB_TYPE GENERIC_ID );", 30, 0, input);

                throw nvae;
            }
            switch (alt30) {
                case 1 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:536:5: SUB_TYPE INTEGER
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    SUB_TYPE169=(Token)input.LT(1);
                    match(input,SUB_TYPE,FOLLOW_SUB_TYPE_in_blobSubtype1583); if (failed) return retval;
                    if ( backtracking==0 ) {
                    SUB_TYPE169_tree = (CommonTree)adaptor.create(SUB_TYPE169);
                    adaptor.addChild(root_0, SUB_TYPE169_tree);
                    }
                    INTEGER170=(Token)input.LT(1);
                    match(input,INTEGER,FOLLOW_INTEGER_in_blobSubtype1585); if (failed) return retval;
                    if ( backtracking==0 ) {
                    INTEGER170_tree = (CommonTree)adaptor.create(INTEGER170);
                    adaptor.addChild(root_0, INTEGER170_tree);
                    }

                    }
                    break;
                case 2 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:537:5: SUB_TYPE GENERIC_ID
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    SUB_TYPE171=(Token)input.LT(1);
                    match(input,SUB_TYPE,FOLLOW_SUB_TYPE_in_blobSubtype1591); if (failed) return retval;
                    if ( backtracking==0 ) {
                    SUB_TYPE171_tree = (CommonTree)adaptor.create(SUB_TYPE171);
                    adaptor.addChild(root_0, SUB_TYPE171_tree);
                    }
                    GENERIC_ID172=(Token)input.LT(1);
                    match(input,GENERIC_ID,FOLLOW_GENERIC_ID_in_blobSubtype1593); if (failed) return retval;
                    if ( backtracking==0 ) {
                    GENERIC_ID172_tree = (CommonTree)adaptor.create(GENERIC_ID172);
                    adaptor.addChild(root_0, GENERIC_ID172_tree);
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 33, blobSubtype_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end blobSubtype

    public static class blobSegSize_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start blobSegSize
    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:540:1: blobSegSize : SEGMENT KW_SIZE INTEGER ;
    public final blobSegSize_return blobSegSize() throws RecognitionException {
        blobSegSize_return retval = new blobSegSize_return();
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
            if ( backtracking>0 && alreadyParsedRule(input, 34) ) { return retval; }
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:541:3: ( SEGMENT KW_SIZE INTEGER )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:541:5: SEGMENT KW_SIZE INTEGER
            {
            root_0 = (CommonTree)adaptor.nil();

            SEGMENT173=(Token)input.LT(1);
            match(input,SEGMENT,FOLLOW_SEGMENT_in_blobSegSize1608); if (failed) return retval;
            if ( backtracking==0 ) {
            SEGMENT173_tree = (CommonTree)adaptor.create(SEGMENT173);
            adaptor.addChild(root_0, SEGMENT173_tree);
            }
            KW_SIZE174=(Token)input.LT(1);
            match(input,KW_SIZE,FOLLOW_KW_SIZE_in_blobSegSize1610); if (failed) return retval;
            if ( backtracking==0 ) {
            KW_SIZE174_tree = (CommonTree)adaptor.create(KW_SIZE174);
            adaptor.addChild(root_0, KW_SIZE174_tree);
            }
            INTEGER175=(Token)input.LT(1);
            match(input,INTEGER,FOLLOW_INTEGER_in_blobSegSize1612); if (failed) return retval;
            if ( backtracking==0 ) {
            INTEGER175_tree = (CommonTree)adaptor.create(INTEGER175);
            adaptor.addChild(root_0, INTEGER175_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 34, blobSegSize_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end blobSegSize

    public static class charSetClause_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start charSetClause
    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:544:1: charSetClause : CHARACTER KW_SET GENERIC_ID ;
    public final charSetClause_return charSetClause() throws RecognitionException {
        charSetClause_return retval = new charSetClause_return();
        retval.start = input.LT(1);
        int charSetClause_StartIndex = input.index();
        CommonTree root_0 = null;

        Token CHARACTER176=null;
        Token KW_SET177=null;
        Token GENERIC_ID178=null;

        CommonTree CHARACTER176_tree=null;
        CommonTree KW_SET177_tree=null;
        CommonTree GENERIC_ID178_tree=null;

        try {
            if ( backtracking>0 && alreadyParsedRule(input, 35) ) { return retval; }
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:545:3: ( CHARACTER KW_SET GENERIC_ID )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:545:5: CHARACTER KW_SET GENERIC_ID
            {
            root_0 = (CommonTree)adaptor.nil();

            CHARACTER176=(Token)input.LT(1);
            match(input,CHARACTER,FOLLOW_CHARACTER_in_charSetClause1627); if (failed) return retval;
            if ( backtracking==0 ) {
            CHARACTER176_tree = (CommonTree)adaptor.create(CHARACTER176);
            adaptor.addChild(root_0, CHARACTER176_tree);
            }
            KW_SET177=(Token)input.LT(1);
            match(input,KW_SET,FOLLOW_KW_SET_in_charSetClause1629); if (failed) return retval;
            if ( backtracking==0 ) {
            KW_SET177_tree = (CommonTree)adaptor.create(KW_SET177);
            adaptor.addChild(root_0, KW_SET177_tree);
            }
            GENERIC_ID178=(Token)input.LT(1);
            match(input,GENERIC_ID,FOLLOW_GENERIC_ID_in_charSetClause1631); if (failed) return retval;
            if ( backtracking==0 ) {
            GENERIC_ID178_tree = (CommonTree)adaptor.create(GENERIC_ID178);
            adaptor.addChild(root_0, GENERIC_ID178_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 35, charSetClause_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end charSetClause

    public static class arrayType_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start arrayType
    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:548:1: arrayType : ( nonCharSetCharType '[' arraySpec ']' ( charSetClause )? | nonCharType '[' arraySpec ']' );
    public final arrayType_return arrayType() throws RecognitionException {
        arrayType_return retval = new arrayType_return();
        retval.start = input.LT(1);
        int arrayType_StartIndex = input.index();
        CommonTree root_0 = null;

        Token char_literal180=null;
        Token char_literal182=null;
        Token char_literal185=null;
        Token char_literal187=null;
        nonCharSetCharType_return nonCharSetCharType179 = null;

        arraySpec_return arraySpec181 = null;

        charSetClause_return charSetClause183 = null;

        nonCharType_return nonCharType184 = null;

        arraySpec_return arraySpec186 = null;


        CommonTree char_literal180_tree=null;
        CommonTree char_literal182_tree=null;
        CommonTree char_literal185_tree=null;
        CommonTree char_literal187_tree=null;

        try {
            if ( backtracking>0 && alreadyParsedRule(input, 36) ) { return retval; }
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:549:3: ( nonCharSetCharType '[' arraySpec ']' ( charSetClause )? | nonCharType '[' arraySpec ']' )
            int alt32=2;
            int LA32_0 = input.LA(1);

            if ( (LA32_0==KW_CHAR||LA32_0==KW_VARCHAR) ) {
                alt32=1;
            }
            else if ( (LA32_0==KW_BIGINT||(LA32_0>=KW_DATE && LA32_0<=KW_DOUBLE)||(LA32_0>=KW_FLOAT && LA32_0<=KW_TIMESTAMP)) ) {
                alt32=2;
            }
            else {
                if (backtracking>0) {failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("548:1: arrayType : ( nonCharSetCharType '[' arraySpec ']' ( charSetClause )? | nonCharType '[' arraySpec ']' );", 32, 0, input);

                throw nvae;
            }
            switch (alt32) {
                case 1 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:549:5: nonCharSetCharType '[' arraySpec ']' ( charSetClause )?
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_nonCharSetCharType_in_arrayType1644);
                    nonCharSetCharType179=nonCharSetCharType();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, nonCharSetCharType179.getTree());
                    char_literal180=(Token)input.LT(1);
                    match(input,86,FOLLOW_86_in_arrayType1646); if (failed) return retval;
                    if ( backtracking==0 ) {
                    char_literal180_tree = (CommonTree)adaptor.create(char_literal180);
                    adaptor.addChild(root_0, char_literal180_tree);
                    }
                    pushFollow(FOLLOW_arraySpec_in_arrayType1648);
                    arraySpec181=arraySpec();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, arraySpec181.getTree());
                    char_literal182=(Token)input.LT(1);
                    match(input,87,FOLLOW_87_in_arrayType1650); if (failed) return retval;
                    if ( backtracking==0 ) {
                    char_literal182_tree = (CommonTree)adaptor.create(char_literal182);
                    adaptor.addChild(root_0, char_literal182_tree);
                    }
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:549:42: ( charSetClause )?
                    int alt31=2;
                    int LA31_0 = input.LA(1);

                    if ( (LA31_0==CHARACTER) ) {
                        alt31=1;
                    }
                    switch (alt31) {
                        case 1 :
                            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:0:0: charSetClause
                            {
                            pushFollow(FOLLOW_charSetClause_in_arrayType1652);
                            charSetClause183=charSetClause();
                            _fsp--;
                            if (failed) return retval;
                            if ( backtracking==0 ) adaptor.addChild(root_0, charSetClause183.getTree());

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:550:5: nonCharType '[' arraySpec ']'
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_nonCharType_in_arrayType1659);
                    nonCharType184=nonCharType();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, nonCharType184.getTree());
                    char_literal185=(Token)input.LT(1);
                    match(input,86,FOLLOW_86_in_arrayType1661); if (failed) return retval;
                    if ( backtracking==0 ) {
                    char_literal185_tree = (CommonTree)adaptor.create(char_literal185);
                    adaptor.addChild(root_0, char_literal185_tree);
                    }
                    pushFollow(FOLLOW_arraySpec_in_arrayType1663);
                    arraySpec186=arraySpec();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, arraySpec186.getTree());
                    char_literal187=(Token)input.LT(1);
                    match(input,87,FOLLOW_87_in_arrayType1665); if (failed) return retval;
                    if ( backtracking==0 ) {
                    char_literal187_tree = (CommonTree)adaptor.create(char_literal187);
                    adaptor.addChild(root_0, char_literal187_tree);
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 36, arrayType_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end arrayType

    public static class arraySpec_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start arraySpec
    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:553:1: arraySpec : arrayRange ( ',' arrayRange )? ;
    public final arraySpec_return arraySpec() throws RecognitionException {
        arraySpec_return retval = new arraySpec_return();
        retval.start = input.LT(1);
        int arraySpec_StartIndex = input.index();
        CommonTree root_0 = null;

        Token char_literal189=null;
        arrayRange_return arrayRange188 = null;

        arrayRange_return arrayRange190 = null;


        CommonTree char_literal189_tree=null;

        try {
            if ( backtracking>0 && alreadyParsedRule(input, 37) ) { return retval; }
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:554:3: ( arrayRange ( ',' arrayRange )? )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:554:5: arrayRange ( ',' arrayRange )?
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_arrayRange_in_arraySpec1680);
            arrayRange188=arrayRange();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, arrayRange188.getTree());
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:554:16: ( ',' arrayRange )?
            int alt33=2;
            int LA33_0 = input.LA(1);

            if ( (LA33_0==COMMA) ) {
                alt33=1;
            }
            switch (alt33) {
                case 1 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:554:17: ',' arrayRange
                    {
                    char_literal189=(Token)input.LT(1);
                    match(input,COMMA,FOLLOW_COMMA_in_arraySpec1683); if (failed) return retval;
                    if ( backtracking==0 ) {
                    char_literal189_tree = (CommonTree)adaptor.create(char_literal189);
                    adaptor.addChild(root_0, char_literal189_tree);
                    }
                    pushFollow(FOLLOW_arrayRange_in_arraySpec1685);
                    arrayRange190=arrayRange();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, arrayRange190.getTree());

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 37, arraySpec_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end arraySpec

    public static class arrayRange_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start arrayRange
    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:557:1: arrayRange : INTEGER ( ':' INTEGER ) ;
    public final arrayRange_return arrayRange() throws RecognitionException {
        arrayRange_return retval = new arrayRange_return();
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
            if ( backtracking>0 && alreadyParsedRule(input, 38) ) { return retval; }
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:558:3: ( INTEGER ( ':' INTEGER ) )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:558:5: INTEGER ( ':' INTEGER )
            {
            root_0 = (CommonTree)adaptor.nil();

            INTEGER191=(Token)input.LT(1);
            match(input,INTEGER,FOLLOW_INTEGER_in_arrayRange1702); if (failed) return retval;
            if ( backtracking==0 ) {
            INTEGER191_tree = (CommonTree)adaptor.create(INTEGER191);
            adaptor.addChild(root_0, INTEGER191_tree);
            }
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:558:13: ( ':' INTEGER )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:558:14: ':' INTEGER
            {
            char_literal192=(Token)input.LT(1);
            match(input,88,FOLLOW_88_in_arrayRange1705); if (failed) return retval;
            if ( backtracking==0 ) {
            char_literal192_tree = (CommonTree)adaptor.create(char_literal192);
            adaptor.addChild(root_0, char_literal192_tree);
            }
            INTEGER193=(Token)input.LT(1);
            match(input,INTEGER,FOLLOW_INTEGER_in_arrayRange1707); if (failed) return retval;
            if ( backtracking==0 ) {
            INTEGER193_tree = (CommonTree)adaptor.create(INTEGER193);
            adaptor.addChild(root_0, INTEGER193_tree);
            }

            }


            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 38, arrayRange_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end arrayRange

    public static class arrayElement_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start arrayElement
    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:561:1: arrayElement : simpleIdentifier '[' valueList ']' ;
    public final arrayElement_return arrayElement() throws RecognitionException {
        arrayElement_return retval = new arrayElement_return();
        retval.start = input.LT(1);
        int arrayElement_StartIndex = input.index();
        CommonTree root_0 = null;

        Token char_literal195=null;
        Token char_literal197=null;
        simpleIdentifier_return simpleIdentifier194 = null;

        valueList_return valueList196 = null;


        CommonTree char_literal195_tree=null;
        CommonTree char_literal197_tree=null;

        try {
            if ( backtracking>0 && alreadyParsedRule(input, 39) ) { return retval; }
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:562:3: ( simpleIdentifier '[' valueList ']' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:562:5: simpleIdentifier '[' valueList ']'
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_simpleIdentifier_in_arrayElement1723);
            simpleIdentifier194=simpleIdentifier();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, simpleIdentifier194.getTree());
            char_literal195=(Token)input.LT(1);
            match(input,86,FOLLOW_86_in_arrayElement1725); if (failed) return retval;
            if ( backtracking==0 ) {
            char_literal195_tree = (CommonTree)adaptor.create(char_literal195);
            adaptor.addChild(root_0, char_literal195_tree);
            }
            pushFollow(FOLLOW_valueList_in_arrayElement1727);
            valueList196=valueList();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, valueList196.getTree());
            char_literal197=(Token)input.LT(1);
            match(input,87,FOLLOW_87_in_arrayElement1729); if (failed) return retval;
            if ( backtracking==0 ) {
            char_literal197_tree = (CommonTree)adaptor.create(char_literal197);
            adaptor.addChild(root_0, char_literal197_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 39, arrayElement_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end arrayElement

    public static class function_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start function
    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:565:1: function : ( simpleIdentifier '(' valueList ')' | simpleIdentifier '(' ')' | substringFunction | trimFunction | extractFunction | SUM '(' ( ALL | DISTINCT )? value ')' | COUNT '(' ( ALL | DISTINCT )? value ')' | AVG '(' ( ALL | DISTINCT )? value ')' | MINIMUM '(' ( ALL | DISTINCT )? value ')' | MAXIMUM '(' ( ALL | DISTINCT )? value ')' );
    public final function_return function() throws RecognitionException {
        function_return retval = new function_return();
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
        simpleIdentifier_return simpleIdentifier198 = null;

        valueList_return valueList200 = null;

        simpleIdentifier_return simpleIdentifier202 = null;

        substringFunction_return substringFunction205 = null;

        trimFunction_return trimFunction206 = null;

        extractFunction_return extractFunction207 = null;

        value_return value211 = null;

        value_return value216 = null;

        value_return value221 = null;

        value_return value226 = null;

        value_return value231 = null;


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
            if ( backtracking>0 && alreadyParsedRule(input, 40) ) { return retval; }
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:566:3: ( simpleIdentifier '(' valueList ')' | simpleIdentifier '(' ')' | substringFunction | trimFunction | extractFunction | SUM '(' ( ALL | DISTINCT )? value ')' | COUNT '(' ( ALL | DISTINCT )? value ')' | AVG '(' ( ALL | DISTINCT )? value ')' | MINIMUM '(' ( ALL | DISTINCT )? value ')' | MAXIMUM '(' ( ALL | DISTINCT )? value ')' )
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
                    else if ( (LA39_10==AVG||LA39_10==CAST||LA39_10==COUNT||(LA39_10>=CURRENT_USER && LA39_10<=CURRENT_TIMESTAMP)||(LA39_10>=DB_KEY && LA39_10<=EXTRACT)||LA39_10==GEN_ID||(LA39_10>=MINIMUM && LA39_10<=NEXT)||LA39_10==SUBSTRING||(LA39_10>=SUM && LA39_10<=TRIM)||(LA39_10>=GENERIC_ID && LA39_10<=LEFT_PAREN)||(LA39_10>=STRING && LA39_10<=REAL)||(LA39_10>=80 && LA39_10<=81)||LA39_10==85) ) {
                        alt39=1;
                    }
                    else {
                        if (backtracking>0) {failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("565:1: function : ( simpleIdentifier '(' valueList ')' | simpleIdentifier '(' ')' | substringFunction | trimFunction | extractFunction | SUM '(' ( ALL | DISTINCT )? value ')' | COUNT '(' ( ALL | DISTINCT )? value ')' | AVG '(' ( ALL | DISTINCT )? value ')' | MINIMUM '(' ( ALL | DISTINCT )? value ')' | MAXIMUM '(' ( ALL | DISTINCT )? value ')' );", 39, 10, input);

                        throw nvae;
                    }
                }
                else {
                    if (backtracking>0) {failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("565:1: function : ( simpleIdentifier '(' valueList ')' | simpleIdentifier '(' ')' | substringFunction | trimFunction | extractFunction | SUM '(' ( ALL | DISTINCT )? value ')' | COUNT '(' ( ALL | DISTINCT )? value ')' | AVG '(' ( ALL | DISTINCT )? value ')' | MINIMUM '(' ( ALL | DISTINCT )? value ')' | MAXIMUM '(' ( ALL | DISTINCT )? value ')' );", 39, 1, input);

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
                if (backtracking>0) {failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("565:1: function : ( simpleIdentifier '(' valueList ')' | simpleIdentifier '(' ')' | substringFunction | trimFunction | extractFunction | SUM '(' ( ALL | DISTINCT )? value ')' | COUNT '(' ( ALL | DISTINCT )? value ')' | AVG '(' ( ALL | DISTINCT )? value ')' | MINIMUM '(' ( ALL | DISTINCT )? value ')' | MAXIMUM '(' ( ALL | DISTINCT )? value ')' );", 39, 0, input);

                throw nvae;
            }

            switch (alt39) {
                case 1 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:566:5: simpleIdentifier '(' valueList ')'
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_simpleIdentifier_in_function1742);
                    simpleIdentifier198=simpleIdentifier();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, simpleIdentifier198.getTree());
                    char_literal199=(Token)input.LT(1);
                    match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_function1744); if (failed) return retval;
                    if ( backtracking==0 ) {
                    char_literal199_tree = (CommonTree)adaptor.create(char_literal199);
                    adaptor.addChild(root_0, char_literal199_tree);
                    }
                    pushFollow(FOLLOW_valueList_in_function1746);
                    valueList200=valueList();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, valueList200.getTree());
                    char_literal201=(Token)input.LT(1);
                    match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_function1748); if (failed) return retval;
                    if ( backtracking==0 ) {
                    char_literal201_tree = (CommonTree)adaptor.create(char_literal201);
                    adaptor.addChild(root_0, char_literal201_tree);
                    }

                    }
                    break;
                case 2 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:567:5: simpleIdentifier '(' ')'
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_simpleIdentifier_in_function1754);
                    simpleIdentifier202=simpleIdentifier();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, simpleIdentifier202.getTree());
                    char_literal203=(Token)input.LT(1);
                    match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_function1756); if (failed) return retval;
                    if ( backtracking==0 ) {
                    char_literal203_tree = (CommonTree)adaptor.create(char_literal203);
                    adaptor.addChild(root_0, char_literal203_tree);
                    }
                    char_literal204=(Token)input.LT(1);
                    match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_function1758); if (failed) return retval;
                    if ( backtracking==0 ) {
                    char_literal204_tree = (CommonTree)adaptor.create(char_literal204);
                    adaptor.addChild(root_0, char_literal204_tree);
                    }

                    }
                    break;
                case 3 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:568:5: substringFunction
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_substringFunction_in_function1764);
                    substringFunction205=substringFunction();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, substringFunction205.getTree());

                    }
                    break;
                case 4 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:569:5: trimFunction
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_trimFunction_in_function1770);
                    trimFunction206=trimFunction();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, trimFunction206.getTree());

                    }
                    break;
                case 5 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:570:5: extractFunction
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_extractFunction_in_function1776);
                    extractFunction207=extractFunction();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, extractFunction207.getTree());

                    }
                    break;
                case 6 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:571:5: SUM '(' ( ALL | DISTINCT )? value ')'
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    SUM208=(Token)input.LT(1);
                    match(input,SUM,FOLLOW_SUM_in_function1782); if (failed) return retval;
                    if ( backtracking==0 ) {
                    SUM208_tree = (CommonTree)adaptor.create(SUM208);
                    adaptor.addChild(root_0, SUM208_tree);
                    }
                    char_literal209=(Token)input.LT(1);
                    match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_function1785); if (failed) return retval;
                    if ( backtracking==0 ) {
                    char_literal209_tree = (CommonTree)adaptor.create(char_literal209);
                    adaptor.addChild(root_0, char_literal209_tree);
                    }
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:571:14: ( ALL | DISTINCT )?
                    int alt34=2;
                    int LA34_0 = input.LA(1);

                    if ( (LA34_0==ALL||LA34_0==DISTINCT) ) {
                        alt34=1;
                    }
                    switch (alt34) {
                        case 1 :
                            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:
                            {
                            set210=(Token)input.LT(1);
                            if ( input.LA(1)==ALL||input.LA(1)==DISTINCT ) {
                                input.consume();
                                if ( backtracking==0 ) adaptor.addChild(root_0, adaptor.create(set210));
                                errorRecovery=false;failed=false;
                            }
                            else {
                                if (backtracking>0) {failed=true; return retval;}
                                MismatchedSetException mse =
                                    new MismatchedSetException(null,input);
                                recoverFromMismatchedSet(input,mse,FOLLOW_set_in_function1787);    throw mse;
                            }


                            }
                            break;

                    }

                    pushFollow(FOLLOW_value_in_function1794);
                    value211=value();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, value211.getTree());
                    char_literal212=(Token)input.LT(1);
                    match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_function1796); if (failed) return retval;
                    if ( backtracking==0 ) {
                    char_literal212_tree = (CommonTree)adaptor.create(char_literal212);
                    adaptor.addChild(root_0, char_literal212_tree);
                    }

                    }
                    break;
                case 7 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:572:5: COUNT '(' ( ALL | DISTINCT )? value ')'
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    COUNT213=(Token)input.LT(1);
                    match(input,COUNT,FOLLOW_COUNT_in_function1802); if (failed) return retval;
                    if ( backtracking==0 ) {
                    COUNT213_tree = (CommonTree)adaptor.create(COUNT213);
                    adaptor.addChild(root_0, COUNT213_tree);
                    }
                    char_literal214=(Token)input.LT(1);
                    match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_function1805); if (failed) return retval;
                    if ( backtracking==0 ) {
                    char_literal214_tree = (CommonTree)adaptor.create(char_literal214);
                    adaptor.addChild(root_0, char_literal214_tree);
                    }
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:572:16: ( ALL | DISTINCT )?
                    int alt35=2;
                    int LA35_0 = input.LA(1);

                    if ( (LA35_0==ALL||LA35_0==DISTINCT) ) {
                        alt35=1;
                    }
                    switch (alt35) {
                        case 1 :
                            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:
                            {
                            set215=(Token)input.LT(1);
                            if ( input.LA(1)==ALL||input.LA(1)==DISTINCT ) {
                                input.consume();
                                if ( backtracking==0 ) adaptor.addChild(root_0, adaptor.create(set215));
                                errorRecovery=false;failed=false;
                            }
                            else {
                                if (backtracking>0) {failed=true; return retval;}
                                MismatchedSetException mse =
                                    new MismatchedSetException(null,input);
                                recoverFromMismatchedSet(input,mse,FOLLOW_set_in_function1807);    throw mse;
                            }


                            }
                            break;

                    }

                    pushFollow(FOLLOW_value_in_function1814);
                    value216=value();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, value216.getTree());
                    char_literal217=(Token)input.LT(1);
                    match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_function1816); if (failed) return retval;
                    if ( backtracking==0 ) {
                    char_literal217_tree = (CommonTree)adaptor.create(char_literal217);
                    adaptor.addChild(root_0, char_literal217_tree);
                    }

                    }
                    break;
                case 8 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:573:5: AVG '(' ( ALL | DISTINCT )? value ')'
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    AVG218=(Token)input.LT(1);
                    match(input,AVG,FOLLOW_AVG_in_function1822); if (failed) return retval;
                    if ( backtracking==0 ) {
                    AVG218_tree = (CommonTree)adaptor.create(AVG218);
                    adaptor.addChild(root_0, AVG218_tree);
                    }
                    char_literal219=(Token)input.LT(1);
                    match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_function1825); if (failed) return retval;
                    if ( backtracking==0 ) {
                    char_literal219_tree = (CommonTree)adaptor.create(char_literal219);
                    adaptor.addChild(root_0, char_literal219_tree);
                    }
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:573:14: ( ALL | DISTINCT )?
                    int alt36=2;
                    int LA36_0 = input.LA(1);

                    if ( (LA36_0==ALL||LA36_0==DISTINCT) ) {
                        alt36=1;
                    }
                    switch (alt36) {
                        case 1 :
                            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:
                            {
                            set220=(Token)input.LT(1);
                            if ( input.LA(1)==ALL||input.LA(1)==DISTINCT ) {
                                input.consume();
                                if ( backtracking==0 ) adaptor.addChild(root_0, adaptor.create(set220));
                                errorRecovery=false;failed=false;
                            }
                            else {
                                if (backtracking>0) {failed=true; return retval;}
                                MismatchedSetException mse =
                                    new MismatchedSetException(null,input);
                                recoverFromMismatchedSet(input,mse,FOLLOW_set_in_function1827);    throw mse;
                            }


                            }
                            break;

                    }

                    pushFollow(FOLLOW_value_in_function1834);
                    value221=value();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, value221.getTree());
                    char_literal222=(Token)input.LT(1);
                    match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_function1836); if (failed) return retval;
                    if ( backtracking==0 ) {
                    char_literal222_tree = (CommonTree)adaptor.create(char_literal222);
                    adaptor.addChild(root_0, char_literal222_tree);
                    }

                    }
                    break;
                case 9 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:574:5: MINIMUM '(' ( ALL | DISTINCT )? value ')'
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    MINIMUM223=(Token)input.LT(1);
                    match(input,MINIMUM,FOLLOW_MINIMUM_in_function1842); if (failed) return retval;
                    if ( backtracking==0 ) {
                    MINIMUM223_tree = (CommonTree)adaptor.create(MINIMUM223);
                    adaptor.addChild(root_0, MINIMUM223_tree);
                    }
                    char_literal224=(Token)input.LT(1);
                    match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_function1844); if (failed) return retval;
                    if ( backtracking==0 ) {
                    char_literal224_tree = (CommonTree)adaptor.create(char_literal224);
                    adaptor.addChild(root_0, char_literal224_tree);
                    }
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:574:17: ( ALL | DISTINCT )?
                    int alt37=2;
                    int LA37_0 = input.LA(1);

                    if ( (LA37_0==ALL||LA37_0==DISTINCT) ) {
                        alt37=1;
                    }
                    switch (alt37) {
                        case 1 :
                            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:
                            {
                            set225=(Token)input.LT(1);
                            if ( input.LA(1)==ALL||input.LA(1)==DISTINCT ) {
                                input.consume();
                                if ( backtracking==0 ) adaptor.addChild(root_0, adaptor.create(set225));
                                errorRecovery=false;failed=false;
                            }
                            else {
                                if (backtracking>0) {failed=true; return retval;}
                                MismatchedSetException mse =
                                    new MismatchedSetException(null,input);
                                recoverFromMismatchedSet(input,mse,FOLLOW_set_in_function1846);    throw mse;
                            }


                            }
                            break;

                    }

                    pushFollow(FOLLOW_value_in_function1853);
                    value226=value();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, value226.getTree());
                    char_literal227=(Token)input.LT(1);
                    match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_function1855); if (failed) return retval;
                    if ( backtracking==0 ) {
                    char_literal227_tree = (CommonTree)adaptor.create(char_literal227);
                    adaptor.addChild(root_0, char_literal227_tree);
                    }

                    }
                    break;
                case 10 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:575:5: MAXIMUM '(' ( ALL | DISTINCT )? value ')'
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    MAXIMUM228=(Token)input.LT(1);
                    match(input,MAXIMUM,FOLLOW_MAXIMUM_in_function1861); if (failed) return retval;
                    if ( backtracking==0 ) {
                    MAXIMUM228_tree = (CommonTree)adaptor.create(MAXIMUM228);
                    adaptor.addChild(root_0, MAXIMUM228_tree);
                    }
                    char_literal229=(Token)input.LT(1);
                    match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_function1863); if (failed) return retval;
                    if ( backtracking==0 ) {
                    char_literal229_tree = (CommonTree)adaptor.create(char_literal229);
                    adaptor.addChild(root_0, char_literal229_tree);
                    }
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:575:17: ( ALL | DISTINCT )?
                    int alt38=2;
                    int LA38_0 = input.LA(1);

                    if ( (LA38_0==ALL||LA38_0==DISTINCT) ) {
                        alt38=1;
                    }
                    switch (alt38) {
                        case 1 :
                            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:
                            {
                            set230=(Token)input.LT(1);
                            if ( input.LA(1)==ALL||input.LA(1)==DISTINCT ) {
                                input.consume();
                                if ( backtracking==0 ) adaptor.addChild(root_0, adaptor.create(set230));
                                errorRecovery=false;failed=false;
                            }
                            else {
                                if (backtracking>0) {failed=true; return retval;}
                                MismatchedSetException mse =
                                    new MismatchedSetException(null,input);
                                recoverFromMismatchedSet(input,mse,FOLLOW_set_in_function1865);    throw mse;
                            }


                            }
                            break;

                    }

                    pushFollow(FOLLOW_value_in_function1872);
                    value231=value();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, value231.getTree());
                    char_literal232=(Token)input.LT(1);
                    match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_function1874); if (failed) return retval;
                    if ( backtracking==0 ) {
                    char_literal232_tree = (CommonTree)adaptor.create(char_literal232);
                    adaptor.addChild(root_0, char_literal232_tree);
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 40, function_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end function

    public static class substringFunction_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start substringFunction
    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:578:1: substringFunction : SUBSTRING '(' value FROM value ( FOR INTEGER )? ')' ;
    public final substringFunction_return substringFunction() throws RecognitionException {
        substringFunction_return retval = new substringFunction_return();
        retval.start = input.LT(1);
        int substringFunction_StartIndex = input.index();
        CommonTree root_0 = null;

        Token SUBSTRING233=null;
        Token char_literal234=null;
        Token FROM236=null;
        Token FOR238=null;
        Token INTEGER239=null;
        Token char_literal240=null;
        value_return value235 = null;

        value_return value237 = null;


        CommonTree SUBSTRING233_tree=null;
        CommonTree char_literal234_tree=null;
        CommonTree FROM236_tree=null;
        CommonTree FOR238_tree=null;
        CommonTree INTEGER239_tree=null;
        CommonTree char_literal240_tree=null;

        try {
            if ( backtracking>0 && alreadyParsedRule(input, 41) ) { return retval; }
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:579:3: ( SUBSTRING '(' value FROM value ( FOR INTEGER )? ')' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:579:5: SUBSTRING '(' value FROM value ( FOR INTEGER )? ')'
            {
            root_0 = (CommonTree)adaptor.nil();

            SUBSTRING233=(Token)input.LT(1);
            match(input,SUBSTRING,FOLLOW_SUBSTRING_in_substringFunction1891); if (failed) return retval;
            if ( backtracking==0 ) {
            SUBSTRING233_tree = (CommonTree)adaptor.create(SUBSTRING233);
            adaptor.addChild(root_0, SUBSTRING233_tree);
            }
            char_literal234=(Token)input.LT(1);
            match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_substringFunction1893); if (failed) return retval;
            if ( backtracking==0 ) {
            char_literal234_tree = (CommonTree)adaptor.create(char_literal234);
            adaptor.addChild(root_0, char_literal234_tree);
            }
            pushFollow(FOLLOW_value_in_substringFunction1895);
            value235=value();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, value235.getTree());
            FROM236=(Token)input.LT(1);
            match(input,FROM,FOLLOW_FROM_in_substringFunction1897); if (failed) return retval;
            if ( backtracking==0 ) {
            FROM236_tree = (CommonTree)adaptor.create(FROM236);
            adaptor.addChild(root_0, FROM236_tree);
            }
            pushFollow(FOLLOW_value_in_substringFunction1899);
            value237=value();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, value237.getTree());
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:579:36: ( FOR INTEGER )?
            int alt40=2;
            int LA40_0 = input.LA(1);

            if ( (LA40_0==FOR) ) {
                alt40=1;
            }
            switch (alt40) {
                case 1 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:579:37: FOR INTEGER
                    {
                    FOR238=(Token)input.LT(1);
                    match(input,FOR,FOLLOW_FOR_in_substringFunction1902); if (failed) return retval;
                    if ( backtracking==0 ) {
                    FOR238_tree = (CommonTree)adaptor.create(FOR238);
                    adaptor.addChild(root_0, FOR238_tree);
                    }
                    INTEGER239=(Token)input.LT(1);
                    match(input,INTEGER,FOLLOW_INTEGER_in_substringFunction1904); if (failed) return retval;
                    if ( backtracking==0 ) {
                    INTEGER239_tree = (CommonTree)adaptor.create(INTEGER239);
                    adaptor.addChild(root_0, INTEGER239_tree);
                    }

                    }
                    break;

            }

            char_literal240=(Token)input.LT(1);
            match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_substringFunction1908); if (failed) return retval;
            if ( backtracking==0 ) {
            char_literal240_tree = (CommonTree)adaptor.create(char_literal240);
            adaptor.addChild(root_0, char_literal240_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 41, substringFunction_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end substringFunction

    public static class trimFunction_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start trimFunction
    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:582:1: trimFunction : TRIM '(' ( trimSpecification )? value ( FROM value )? ')' ;
    public final trimFunction_return trimFunction() throws RecognitionException {
        trimFunction_return retval = new trimFunction_return();
        retval.start = input.LT(1);
        int trimFunction_StartIndex = input.index();
        CommonTree root_0 = null;

        Token TRIM241=null;
        Token char_literal242=null;
        Token FROM245=null;
        Token char_literal247=null;
        trimSpecification_return trimSpecification243 = null;

        value_return value244 = null;

        value_return value246 = null;


        CommonTree TRIM241_tree=null;
        CommonTree char_literal242_tree=null;
        CommonTree FROM245_tree=null;
        CommonTree char_literal247_tree=null;

        try {
            if ( backtracking>0 && alreadyParsedRule(input, 42) ) { return retval; }
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:583:3: ( TRIM '(' ( trimSpecification )? value ( FROM value )? ')' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:583:5: TRIM '(' ( trimSpecification )? value ( FROM value )? ')'
            {
            root_0 = (CommonTree)adaptor.nil();

            TRIM241=(Token)input.LT(1);
            match(input,TRIM,FOLLOW_TRIM_in_trimFunction1923); if (failed) return retval;
            if ( backtracking==0 ) {
            TRIM241_tree = (CommonTree)adaptor.create(TRIM241);
            adaptor.addChild(root_0, TRIM241_tree);
            }
            char_literal242=(Token)input.LT(1);
            match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_trimFunction1925); if (failed) return retval;
            if ( backtracking==0 ) {
            char_literal242_tree = (CommonTree)adaptor.create(char_literal242);
            adaptor.addChild(root_0, char_literal242_tree);
            }
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:583:14: ( trimSpecification )?
            int alt41=2;
            int LA41_0 = input.LA(1);

            if ( (LA41_0==BOTH||LA41_0==LEADING||LA41_0==TRAILING) ) {
                alt41=1;
            }
            switch (alt41) {
                case 1 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:583:15: trimSpecification
                    {
                    pushFollow(FOLLOW_trimSpecification_in_trimFunction1928);
                    trimSpecification243=trimSpecification();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, trimSpecification243.getTree());

                    }
                    break;

            }

            pushFollow(FOLLOW_value_in_trimFunction1932);
            value244=value();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, value244.getTree());
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:583:41: ( FROM value )?
            int alt42=2;
            int LA42_0 = input.LA(1);

            if ( (LA42_0==FROM) ) {
                alt42=1;
            }
            switch (alt42) {
                case 1 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:583:42: FROM value
                    {
                    FROM245=(Token)input.LT(1);
                    match(input,FROM,FOLLOW_FROM_in_trimFunction1935); if (failed) return retval;
                    if ( backtracking==0 ) {
                    FROM245_tree = (CommonTree)adaptor.create(FROM245);
                    adaptor.addChild(root_0, FROM245_tree);
                    }
                    pushFollow(FOLLOW_value_in_trimFunction1937);
                    value246=value();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, value246.getTree());

                    }
                    break;

            }

            char_literal247=(Token)input.LT(1);
            match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_trimFunction1941); if (failed) return retval;
            if ( backtracking==0 ) {
            char_literal247_tree = (CommonTree)adaptor.create(char_literal247);
            adaptor.addChild(root_0, char_literal247_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 42, trimFunction_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end trimFunction

    public static class extractFunction_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start extractFunction
    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:586:1: extractFunction : EXTRACT '(' value FROM value ')' ;
    public final extractFunction_return extractFunction() throws RecognitionException {
        extractFunction_return retval = new extractFunction_return();
        retval.start = input.LT(1);
        int extractFunction_StartIndex = input.index();
        CommonTree root_0 = null;

        Token EXTRACT248=null;
        Token char_literal249=null;
        Token FROM251=null;
        Token char_literal253=null;
        value_return value250 = null;

        value_return value252 = null;


        CommonTree EXTRACT248_tree=null;
        CommonTree char_literal249_tree=null;
        CommonTree FROM251_tree=null;
        CommonTree char_literal253_tree=null;

        try {
            if ( backtracking>0 && alreadyParsedRule(input, 43) ) { return retval; }
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:587:3: ( EXTRACT '(' value FROM value ')' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:587:5: EXTRACT '(' value FROM value ')'
            {
            root_0 = (CommonTree)adaptor.nil();

            EXTRACT248=(Token)input.LT(1);
            match(input,EXTRACT,FOLLOW_EXTRACT_in_extractFunction1956); if (failed) return retval;
            if ( backtracking==0 ) {
            EXTRACT248_tree = (CommonTree)adaptor.create(EXTRACT248);
            adaptor.addChild(root_0, EXTRACT248_tree);
            }
            char_literal249=(Token)input.LT(1);
            match(input,LEFT_PAREN,FOLLOW_LEFT_PAREN_in_extractFunction1958); if (failed) return retval;
            if ( backtracking==0 ) {
            char_literal249_tree = (CommonTree)adaptor.create(char_literal249);
            adaptor.addChild(root_0, char_literal249_tree);
            }
            pushFollow(FOLLOW_value_in_extractFunction1960);
            value250=value();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, value250.getTree());
            FROM251=(Token)input.LT(1);
            match(input,FROM,FOLLOW_FROM_in_extractFunction1962); if (failed) return retval;
            if ( backtracking==0 ) {
            FROM251_tree = (CommonTree)adaptor.create(FROM251);
            adaptor.addChild(root_0, FROM251_tree);
            }
            pushFollow(FOLLOW_value_in_extractFunction1964);
            value252=value();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, value252.getTree());
            char_literal253=(Token)input.LT(1);
            match(input,RIGHT_PAREN,FOLLOW_RIGHT_PAREN_in_extractFunction1966); if (failed) return retval;
            if ( backtracking==0 ) {
            char_literal253_tree = (CommonTree)adaptor.create(char_literal253);
            adaptor.addChild(root_0, char_literal253_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 43, extractFunction_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end extractFunction

    public static class trimSpecification_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start trimSpecification
    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:590:1: trimSpecification : ( BOTH | TRAILING | LEADING );
    public final trimSpecification_return trimSpecification() throws RecognitionException {
        trimSpecification_return retval = new trimSpecification_return();
        retval.start = input.LT(1);
        int trimSpecification_StartIndex = input.index();
        CommonTree root_0 = null;

        Token set254=null;

        CommonTree set254_tree=null;

        try {
            if ( backtracking>0 && alreadyParsedRule(input, 44) ) { return retval; }
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:591:3: ( BOTH | TRAILING | LEADING )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:
            {
            root_0 = (CommonTree)adaptor.nil();

            set254=(Token)input.LT(1);
            if ( input.LA(1)==BOTH||input.LA(1)==LEADING||input.LA(1)==TRAILING ) {
                input.consume();
                if ( backtracking==0 ) adaptor.addChild(root_0, adaptor.create(set254));
                errorRecovery=false;failed=false;
            }
            else {
                if (backtracking>0) {failed=true; return retval;}
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recoverFromMismatchedSet(input,mse,FOLLOW_set_in_trimSpecification0);    throw mse;
            }


            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 44, trimSpecification_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end trimSpecification

    public static class selectClause_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start selectClause
    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:596:1: selectClause : SELECT ;
    public final selectClause_return selectClause() throws RecognitionException {
        selectClause_return retval = new selectClause_return();
        retval.start = input.LT(1);
        int selectClause_StartIndex = input.index();
        CommonTree root_0 = null;

        Token SELECT255=null;

        CommonTree SELECT255_tree=null;

        try {
            if ( backtracking>0 && alreadyParsedRule(input, 45) ) { return retval; }
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:597:3: ( SELECT )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:597:5: SELECT
            {
            root_0 = (CommonTree)adaptor.nil();

            SELECT255=(Token)input.LT(1);
            match(input,SELECT,FOLLOW_SELECT_in_selectClause2010); if (failed) return retval;
            if ( backtracking==0 ) {
            SELECT255_tree = (CommonTree)adaptor.create(SELECT255);
            adaptor.addChild(root_0, SELECT255_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 45, selectClause_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end selectClause


 

    public static final BitSet FOLLOW_insertStatement_in_statement480 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_deleteStatement_in_statement486 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_updateStatement_in_statement492 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_updateOrInsertStatement_in_statement499 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DELETE_in_deleteStatement516 = new BitSet(new long[]{0x0000000001000000L});
    public static final BitSet FOLLOW_FROM_in_deleteStatement518 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000006L});
    public static final BitSet FOLLOW_tableName_in_deleteStatement520 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_UPDATE_in_updateStatement544 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000006L});
    public static final BitSet FOLLOW_tableName_in_updateStatement546 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_SET_in_updateStatement548 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000006L});
    public static final BitSet FOLLOW_assignments_in_updateStatement550 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignment_in_assignments568 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_assignments571 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000006L});
    public static final BitSet FOLLOW_assignment_in_assignments573 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000100L});
    public static final BitSet FOLLOW_columnName_in_assignment588 = new BitSet(new long[]{0x0000000000000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_78_in_assignment590 = new BitSet(new long[]{0x00000D03C231F540L,0x00000000002300EEL});
    public static final BitSet FOLLOW_value_in_assignment592 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_UPDATE_in_updateOrInsertStatement609 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_OR_in_updateOrInsertStatement611 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_INSERT_in_updateOrInsertStatement613 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_INTO_in_updateOrInsertStatement615 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000006L});
    public static final BitSet FOLLOW_tableName_in_updateOrInsertStatement617 = new BitSet(new long[]{0x0000800000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_insertColumns_in_updateOrInsertStatement619 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_insertValues_in_updateOrInsertStatement627 = new BitSet(new long[]{0x0000001020000002L});
    public static final BitSet FOLLOW_matchingClause_in_updateOrInsertStatement629 = new BitSet(new long[]{0x0000001000000002L});
    public static final BitSet FOLLOW_returningClause_in_updateOrInsertStatement632 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MATCHING_in_matchingClause651 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000006L});
    public static final BitSet FOLLOW_columnList_in_matchingClause653 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INSERT_in_insertStatement670 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_INTO_in_insertStatement672 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000006L});
    public static final BitSet FOLLOW_tableName_in_insertStatement674 = new BitSet(new long[]{0x0000804000020000L,0x0000000000000008L});
    public static final BitSet FOLLOW_insertColumns_in_insertStatement676 = new BitSet(new long[]{0x0000804000020000L});
    public static final BitSet FOLLOW_insertValues_in_insertStatement686 = new BitSet(new long[]{0x0000001000000002L});
    public static final BitSet FOLLOW_returningClause_in_insertStatement688 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selectClause_in_insertStatement698 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_defaultValuesClause_in_insertStatement707 = new BitSet(new long[]{0x0000001000000002L});
    public static final BitSet FOLLOW_returningClause_in_insertStatement709 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_insertColumns736 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000006L});
    public static final BitSet FOLLOW_columnList_in_insertColumns738 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_insertColumns740 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VALUES_in_insertValues759 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_insertValues761 = new BitSet(new long[]{0x00000D03C231F540L,0x00000000002300EEL});
    public static final BitSet FOLLOW_valueList_in_insertValues763 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_insertValues765 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RETURNING_in_returningClause780 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000006L});
    public static final BitSet FOLLOW_columnList_in_returningClause782 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DEFAULT_in_defaultValuesClause801 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_VALUES_in_defaultValuesClause803 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_simpleIdentifier0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simpleIdentifier_in_fullIdentifier843 = new BitSet(new long[]{0x0000000000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_79_in_fullIdentifier845 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000006L});
    public static final BitSet FOLLOW_simpleIdentifier_in_fullIdentifier847 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simpleIdentifier_in_tableName866 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnName_in_columnList887 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_columnList890 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000006L});
    public static final BitSet FOLLOW_columnName_in_columnList892 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000100L});
    public static final BitSet FOLLOW_simpleIdentifier_in_columnName911 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fullIdentifier_in_columnName931 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_value_in_valueList950 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_valueList953 = new BitSet(new long[]{0x00000D03C231F540L,0x00000000002300EEL});
    public static final BitSet FOLLOW_value_in_valueList955 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000100L});
    public static final BitSet FOLLOW_simpleValue_in_value973 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simpleValue_in_value979 = new BitSet(new long[]{0x0000000000000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_80_in_value981 = new BitSet(new long[]{0x0000000000000000L,0x00000000000000E2L});
    public static final BitSet FOLLOW_simpleValue_in_value983 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simpleValue_in_value989 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020000L});
    public static final BitSet FOLLOW_81_in_value991 = new BitSet(new long[]{0x0000000000000000L,0x00000000000000E2L});
    public static final BitSet FOLLOW_simpleValue_in_value993 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simpleValue_in_value999 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_82_in_value1001 = new BitSet(new long[]{0x0000000000000000L,0x00000000000000E2L});
    public static final BitSet FOLLOW_simpleValue_in_value1003 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simpleValue_in_value1009 = new BitSet(new long[]{0x0000000000000000L,0x0000000000080000L});
    public static final BitSet FOLLOW_83_in_value1011 = new BitSet(new long[]{0x0000000000000000L,0x00000000000000E2L});
    public static final BitSet FOLLOW_simpleValue_in_value1013 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simpleValue_in_value1019 = new BitSet(new long[]{0x0000000000000000L,0x0000000000100000L});
    public static final BitSet FOLLOW_84_in_value1021 = new BitSet(new long[]{0x0000000000000000L,0x00000000000000E2L});
    public static final BitSet FOLLOW_simpleValue_in_value1023 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_80_in_value1029 = new BitSet(new long[]{0x0000000000000000L,0x00000000000000E2L});
    public static final BitSet FOLLOW_simpleValue_in_value1031 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_81_in_value1037 = new BitSet(new long[]{0x0000000000000000L,0x00000000000000E2L});
    public static final BitSet FOLLOW_simpleValue_in_value1039 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_value1048 = new BitSet(new long[]{0x0000000000000000L,0x00000000000000E2L});
    public static final BitSet FOLLOW_simpleValue_in_value1050 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_value1052 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simpleValue_in_value1061 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_COLLATE_in_value1063 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000006L});
    public static final BitSet FOLLOW_simpleIdentifier_in_value1065 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_parameter_in_value1073 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CURRENT_USER_in_value1082 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CURRENT_ROLE_in_value1088 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CURRENT_DATE_in_value1094 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CURRENT_TIME_in_value1100 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CURRENT_TIMESTAMP_in_value1106 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nullValue_in_value1115 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_function_in_value1124 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nextValueExpression_in_value1130 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_castExpression_in_value1136 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_arrayElement_in_value1146 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DB_KEY_in_value1155 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simpleIdentifier_in_value1161 = new BitSet(new long[]{0x0000000000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_79_in_value1163 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_DB_KEY_in_value1165 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_85_in_parameter1179 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NULL_in_nullValue1192 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_simpleValue0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NEXT_in_nextValueExpression1241 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_VALUE_in_nextValueExpression1243 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_FOR_in_nextValueExpression1245 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000006L});
    public static final BitSet FOLLOW_simpleIdentifier_in_nextValueExpression1247 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GEN_ID_in_nextValueExpression1253 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_nextValueExpression1255 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000006L});
    public static final BitSet FOLLOW_simpleIdentifier_in_nextValueExpression1257 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_nextValueExpression1259 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_INTEGER_in_nextValueExpression1261 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_nextValueExpression1263 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CAST_in_castExpression1278 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_castExpression1280 = new BitSet(new long[]{0x00000D03C231F540L,0x00000000002300EEL});
    public static final BitSet FOLLOW_value_in_castExpression1282 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_AS_in_castExpression1284 = new BitSet(new long[]{0x7FBF000000000000L});
    public static final BitSet FOLLOW_dataTypeDescriptor_in_castExpression1286 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_castExpression1288 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nonArrayType_in_dataTypeDescriptor1303 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_arrayType_in_dataTypeDescriptor1309 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simpleType_in_nonArrayType1324 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_blobType_in_nonArrayType1330 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nonCharType_in_simpleType1343 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_charType_in_simpleType1349 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nonCharSetCharType_in_charType1364 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_charSetCharType_in_charType1370 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KW_CHAR_in_nonCharSetCharType1383 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000008L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_nonCharSetCharType1386 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_INTEGER_in_nonCharSetCharType1388 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_nonCharSetCharType1390 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KW_VARCHAR_in_nonCharSetCharType1398 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_nonCharSetCharType1400 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_INTEGER_in_nonCharSetCharType1402 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_nonCharSetCharType1404 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nonCharSetCharType_in_charSetCharType1417 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_charSetClause_in_charSetCharType1419 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KW_BIGINT_in_nonCharType1432 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KW_DATE_in_nonCharType1438 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KW_DECIMAL_in_nonCharType1444 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_nonCharType1446 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_INTEGER_in_nonCharType1448 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000110L});
    public static final BitSet FOLLOW_COMMA_in_nonCharType1451 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_INTEGER_in_nonCharType1453 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_nonCharType1457 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KW_DOUBLE_in_nonCharType1463 = new BitSet(new long[]{0x0040000000000000L});
    public static final BitSet FOLLOW_KW_PRECISION_in_nonCharType1465 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KW_FLOAT_in_nonCharType1471 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KW_INTEGER_in_nonCharType1477 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KW_INT_in_nonCharType1483 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KW_NUMERIC_in_nonCharType1489 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_nonCharType1491 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_INTEGER_in_nonCharType1493 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000110L});
    public static final BitSet FOLLOW_COMMA_in_nonCharType1496 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_INTEGER_in_nonCharType1498 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_nonCharType1502 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KW_SMALLINT_in_nonCharType1508 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KW_TIME_in_nonCharType1514 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KW_TIMESTAMP_in_nonCharType1520 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KW_BLOB_in_blobType1534 = new BitSet(new long[]{0x0000022000000202L});
    public static final BitSet FOLLOW_blobSubtype_in_blobType1536 = new BitSet(new long[]{0x0000002000000202L});
    public static final BitSet FOLLOW_blobSegSize_in_blobType1539 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_charSetClause_in_blobType1542 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KW_BLOB_in_blobType1554 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_blobType1556 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000150L});
    public static final BitSet FOLLOW_INTEGER_in_blobType1558 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000110L});
    public static final BitSet FOLLOW_COMMA_in_blobType1562 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_INTEGER_in_blobType1564 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_blobType1568 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SUB_TYPE_in_blobSubtype1583 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_INTEGER_in_blobSubtype1585 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SUB_TYPE_in_blobSubtype1591 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_GENERIC_ID_in_blobSubtype1593 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SEGMENT_in_blobSegSize1608 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_KW_SIZE_in_blobSegSize1610 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_INTEGER_in_blobSegSize1612 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CHARACTER_in_charSetClause1627 = new BitSet(new long[]{0x8000000000000000L});
    public static final BitSet FOLLOW_KW_SET_in_charSetClause1629 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_GENERIC_ID_in_charSetClause1631 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nonCharSetCharType_in_arrayType1644 = new BitSet(new long[]{0x0000000000000000L,0x0000000000400000L});
    public static final BitSet FOLLOW_86_in_arrayType1646 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_arraySpec_in_arrayType1648 = new BitSet(new long[]{0x0000000000000000L,0x0000000000800000L});
    public static final BitSet FOLLOW_87_in_arrayType1650 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_charSetClause_in_arrayType1652 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nonCharType_in_arrayType1659 = new BitSet(new long[]{0x0000000000000000L,0x0000000000400000L});
    public static final BitSet FOLLOW_86_in_arrayType1661 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_arraySpec_in_arrayType1663 = new BitSet(new long[]{0x0000000000000000L,0x0000000000800000L});
    public static final BitSet FOLLOW_87_in_arrayType1665 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_arrayRange_in_arraySpec1680 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_arraySpec1683 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_arrayRange_in_arraySpec1685 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INTEGER_in_arrayRange1702 = new BitSet(new long[]{0x0000000000000000L,0x0000000001000000L});
    public static final BitSet FOLLOW_88_in_arrayRange1705 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_INTEGER_in_arrayRange1707 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simpleIdentifier_in_arrayElement1723 = new BitSet(new long[]{0x0000000000000000L,0x0000000000400000L});
    public static final BitSet FOLLOW_86_in_arrayElement1725 = new BitSet(new long[]{0x00000D03C231F540L,0x00000000002300EEL});
    public static final BitSet FOLLOW_valueList_in_arrayElement1727 = new BitSet(new long[]{0x0000000000000000L,0x0000000000800000L});
    public static final BitSet FOLLOW_87_in_arrayElement1729 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simpleIdentifier_in_function1742 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_function1744 = new BitSet(new long[]{0x00000D03C231F540L,0x00000000002300EEL});
    public static final BitSet FOLLOW_valueList_in_function1746 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_function1748 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simpleIdentifier_in_function1754 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_function1756 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_function1758 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_substringFunction_in_function1764 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_trimFunction_in_function1770 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_extractFunction_in_function1776 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SUM_in_function1782 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_function1785 = new BitSet(new long[]{0x00000D03C239F550L,0x00000000002300EEL});
    public static final BitSet FOLLOW_set_in_function1787 = new BitSet(new long[]{0x00000D03C231F540L,0x00000000002300EEL});
    public static final BitSet FOLLOW_value_in_function1794 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_function1796 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COUNT_in_function1802 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_function1805 = new BitSet(new long[]{0x00000D03C239F550L,0x00000000002300EEL});
    public static final BitSet FOLLOW_set_in_function1807 = new BitSet(new long[]{0x00000D03C231F540L,0x00000000002300EEL});
    public static final BitSet FOLLOW_value_in_function1814 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_function1816 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AVG_in_function1822 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_function1825 = new BitSet(new long[]{0x00000D03C239F550L,0x00000000002300EEL});
    public static final BitSet FOLLOW_set_in_function1827 = new BitSet(new long[]{0x00000D03C231F540L,0x00000000002300EEL});
    public static final BitSet FOLLOW_value_in_function1834 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_function1836 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINIMUM_in_function1842 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_function1844 = new BitSet(new long[]{0x00000D03C239F550L,0x00000000002300EEL});
    public static final BitSet FOLLOW_set_in_function1846 = new BitSet(new long[]{0x00000D03C231F540L,0x00000000002300EEL});
    public static final BitSet FOLLOW_value_in_function1853 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_function1855 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MAXIMUM_in_function1861 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_function1863 = new BitSet(new long[]{0x00000D03C239F550L,0x00000000002300EEL});
    public static final BitSet FOLLOW_set_in_function1865 = new BitSet(new long[]{0x00000D03C231F540L,0x00000000002300EEL});
    public static final BitSet FOLLOW_value_in_function1872 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_function1874 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SUBSTRING_in_substringFunction1891 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_substringFunction1893 = new BitSet(new long[]{0x00000D03C231F540L,0x00000000002300EEL});
    public static final BitSet FOLLOW_value_in_substringFunction1895 = new BitSet(new long[]{0x0000000001000000L});
    public static final BitSet FOLLOW_FROM_in_substringFunction1897 = new BitSet(new long[]{0x00000D03C231F540L,0x00000000002300EEL});
    public static final BitSet FOLLOW_value_in_substringFunction1899 = new BitSet(new long[]{0x0000000000800000L,0x0000000000000010L});
    public static final BitSet FOLLOW_FOR_in_substringFunction1902 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_INTEGER_in_substringFunction1904 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_substringFunction1908 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRIM_in_trimFunction1923 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_trimFunction1925 = new BitSet(new long[]{0x00001D03D231F5C0L,0x00000000002300EEL});
    public static final BitSet FOLLOW_trimSpecification_in_trimFunction1928 = new BitSet(new long[]{0x00000D03C231F540L,0x00000000002300EEL});
    public static final BitSet FOLLOW_value_in_trimFunction1932 = new BitSet(new long[]{0x0000000001000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_FROM_in_trimFunction1935 = new BitSet(new long[]{0x00000D03C231F540L,0x00000000002300EEL});
    public static final BitSet FOLLOW_value_in_trimFunction1937 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_trimFunction1941 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EXTRACT_in_extractFunction1956 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_LEFT_PAREN_in_extractFunction1958 = new BitSet(new long[]{0x00000D03C231F540L,0x00000000002300EEL});
    public static final BitSet FOLLOW_value_in_extractFunction1960 = new BitSet(new long[]{0x0000000001000000L});
    public static final BitSet FOLLOW_FROM_in_extractFunction1962 = new BitSet(new long[]{0x00000D03C231F540L,0x00000000002300EEL});
    public static final BitSet FOLLOW_value_in_extractFunction1964 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RIGHT_PAREN_in_extractFunction1966 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_trimSpecification0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SELECT_in_selectClause2010 = new BitSet(new long[]{0x0000000000000002L});

}