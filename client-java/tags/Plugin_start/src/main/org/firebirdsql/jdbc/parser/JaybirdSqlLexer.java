// $ANTLR 3.0.1 D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3 2008-06-20 22:29:26

package org.firebirdsql.jdbc.parser;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
public class JaybirdSqlLexer extends Lexer {
    public static final int CAST=8;
    public static final int KW_TIMESTAMP=61;
    public static final int T79=79;
    public static final int T78=78;
    public static final int KW_CHAR=50;
    public static final int CURRENT_USER=12;
    public static final int LETTER=73;
    public static final int EXECUTE=22;
    public static final int KW_SIZE=64;
    public static final int SUB_TYPE=41;
    public static final int CURRENT_TIME=15;
    public static final int UPDATE=45;
    public static final int FOR=23;
    public static final int COUNT=10;
    public static final int KW_FLOAT=55;
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
    public static final int KW_INT=57;
    public static final int KW_SMALLINT=59;
    public static final int KW_DECIMAL=52;
    public static final int NULL=32;
    public static final int DEFAULT=17;
    public static final int VALUES=47;
    public static final int TRAILING=44;
    public static final int SET=39;
    public static final int DB_KEY=20;
    public static final int DELETE=18;
    public static final int GEN_ID=25;
    public static final int VALUE=46;
    public static final int Tokens=89;
    public static final int KW_DOUBLE=53;
    public static final int PROCEDURE=35;
    public static final int LEADING=28;
    public static final int SUBSTRING=40;
    public static final int T88=88;
    public static final int REAL=71;
    public static final int MATCHING=29;
    public static final int KW_DATE=51;
    public static final int T84=84;
    public static final int WS=75;
    public static final int T85=85;
    public static final int T86=86;
    public static final int T87=87;
    public static final int CURRENT_ROLE=13;
    public static final int OR=34;
    public static final int GENERIC_ID=65;
    public static final int SL_COMMENT=76;
    public static final int TRIM=43;
    public static final int KW_INTEGER=56;
    public static final int LEFT_PAREN=67;
    public static final int MAXIMUM=31;
    public static final int T81=81;
    public static final int FROM=24;
    public static final int T80=80;
    public static final int T83=83;
    public static final int DISTINCT=19;
    public static final int T82=82;
    public static final int CURRENT_DATE=14;
    public static final int STRING=69;
    
    	protected int _mismatchCount;
    	protected java.util.ArrayList _errorMessages = new java.util.ArrayList();
    	
    	public int getMismatchCount() {
    		return _mismatchCount;
    	}
    	
    	public java.util.Collection getErrorMessages() {
    		return _errorMessages;
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

    public JaybirdSqlLexer() {;} 
    public JaybirdSqlLexer(CharStream input) {
        super(input);
        ruleMemo = new HashMap[88+1];
     }
    public String getGrammarFileName() { return "D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3"; }

    // $ANTLR start ALL
    public final void mALL() throws RecognitionException {
        try {
            int _type = ALL;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:50:5: ( 'all' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:50:7: 'all'
            {
            match("all"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end ALL

    // $ANTLR start AS
    public final void mAS() throws RecognitionException {
        try {
            int _type = AS;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:51:4: ( 'as' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:51:6: 'as'
            {
            match("as"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end AS

    // $ANTLR start AVG
    public final void mAVG() throws RecognitionException {
        try {
            int _type = AVG;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:52:5: ( 'avg' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:52:7: 'avg'
            {
            match("avg"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end AVG

    // $ANTLR start BOTH
    public final void mBOTH() throws RecognitionException {
        try {
            int _type = BOTH;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:53:6: ( 'both' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:53:8: 'both'
            {
            match("both"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end BOTH

    // $ANTLR start CAST
    public final void mCAST() throws RecognitionException {
        try {
            int _type = CAST;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:54:6: ( 'cast' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:54:8: 'cast'
            {
            match("cast"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end CAST

    // $ANTLR start CHARACTER
    public final void mCHARACTER() throws RecognitionException {
        try {
            int _type = CHARACTER;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:55:11: ( 'character' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:55:13: 'character'
            {
            match("character"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end CHARACTER

    // $ANTLR start COUNT
    public final void mCOUNT() throws RecognitionException {
        try {
            int _type = COUNT;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:56:7: ( 'count' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:56:9: 'count'
            {
            match("count"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end COUNT

    // $ANTLR start COLLATE
    public final void mCOLLATE() throws RecognitionException {
        try {
            int _type = COLLATE;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:57:9: ( 'collate' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:57:11: 'collate'
            {
            match("collate"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end COLLATE

    // $ANTLR start CURRENT_USER
    public final void mCURRENT_USER() throws RecognitionException {
        try {
            int _type = CURRENT_USER;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:58:14: ( 'current_user' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:58:16: 'current_user'
            {
            match("current_user"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end CURRENT_USER

    // $ANTLR start CURRENT_ROLE
    public final void mCURRENT_ROLE() throws RecognitionException {
        try {
            int _type = CURRENT_ROLE;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:59:14: ( 'current_role' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:59:16: 'current_role'
            {
            match("current_role"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end CURRENT_ROLE

    // $ANTLR start CURRENT_DATE
    public final void mCURRENT_DATE() throws RecognitionException {
        try {
            int _type = CURRENT_DATE;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:60:14: ( 'current_date' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:60:16: 'current_date'
            {
            match("current_date"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end CURRENT_DATE

    // $ANTLR start CURRENT_TIME
    public final void mCURRENT_TIME() throws RecognitionException {
        try {
            int _type = CURRENT_TIME;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:61:14: ( 'current_time' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:61:16: 'current_time'
            {
            match("current_time"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end CURRENT_TIME

    // $ANTLR start CURRENT_TIMESTAMP
    public final void mCURRENT_TIMESTAMP() throws RecognitionException {
        try {
            int _type = CURRENT_TIMESTAMP;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:62:19: ( 'current_timestamp' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:62:21: 'current_timestamp'
            {
            match("current_timestamp"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end CURRENT_TIMESTAMP

    // $ANTLR start DEFAULT
    public final void mDEFAULT() throws RecognitionException {
        try {
            int _type = DEFAULT;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:63:9: ( 'default' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:63:11: 'default'
            {
            match("default"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end DEFAULT

    // $ANTLR start DELETE
    public final void mDELETE() throws RecognitionException {
        try {
            int _type = DELETE;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:64:8: ( 'delete' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:64:10: 'delete'
            {
            match("delete"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end DELETE

    // $ANTLR start DISTINCT
    public final void mDISTINCT() throws RecognitionException {
        try {
            int _type = DISTINCT;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:65:10: ( 'distinct' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:65:12: 'distinct'
            {
            match("distinct"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end DISTINCT

    // $ANTLR start DB_KEY
    public final void mDB_KEY() throws RecognitionException {
        try {
            int _type = DB_KEY;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:66:8: ( 'db_key' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:66:10: 'db_key'
            {
            match("db_key"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end DB_KEY

    // $ANTLR start EXTRACT
    public final void mEXTRACT() throws RecognitionException {
        try {
            int _type = EXTRACT;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:67:9: ( 'extract' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:67:11: 'extract'
            {
            match("extract"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end EXTRACT

    // $ANTLR start EXECUTE
    public final void mEXECUTE() throws RecognitionException {
        try {
            int _type = EXECUTE;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:68:9: ( 'execute' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:68:11: 'execute'
            {
            match("execute"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end EXECUTE

    // $ANTLR start FOR
    public final void mFOR() throws RecognitionException {
        try {
            int _type = FOR;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:69:5: ( 'for' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:69:7: 'for'
            {
            match("for"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end FOR

    // $ANTLR start FROM
    public final void mFROM() throws RecognitionException {
        try {
            int _type = FROM;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:70:6: ( 'from' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:70:8: 'from'
            {
            match("from"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end FROM

    // $ANTLR start GEN_ID
    public final void mGEN_ID() throws RecognitionException {
        try {
            int _type = GEN_ID;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:71:8: ( 'gen_id' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:71:10: 'gen_id'
            {
            match("gen_id"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end GEN_ID

    // $ANTLR start INSERT
    public final void mINSERT() throws RecognitionException {
        try {
            int _type = INSERT;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:72:8: ( 'insert' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:72:10: 'insert'
            {
            match("insert"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end INSERT

    // $ANTLR start INTO
    public final void mINTO() throws RecognitionException {
        try {
            int _type = INTO;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:73:6: ( 'into' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:73:8: 'into'
            {
            match("into"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end INTO

    // $ANTLR start LEADING
    public final void mLEADING() throws RecognitionException {
        try {
            int _type = LEADING;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:74:9: ( 'leading' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:74:11: 'leading'
            {
            match("leading"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end LEADING

    // $ANTLR start MATCHING
    public final void mMATCHING() throws RecognitionException {
        try {
            int _type = MATCHING;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:75:10: ( 'matching' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:75:12: 'matching'
            {
            match("matching"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end MATCHING

    // $ANTLR start MINIMUM
    public final void mMINIMUM() throws RecognitionException {
        try {
            int _type = MINIMUM;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:76:9: ( 'min' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:76:11: 'min'
            {
            match("min"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end MINIMUM

    // $ANTLR start MAXIMUM
    public final void mMAXIMUM() throws RecognitionException {
        try {
            int _type = MAXIMUM;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:77:9: ( 'max' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:77:11: 'max'
            {
            match("max"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end MAXIMUM

    // $ANTLR start NULL
    public final void mNULL() throws RecognitionException {
        try {
            int _type = NULL;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:78:6: ( 'null' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:78:8: 'null'
            {
            match("null"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end NULL

    // $ANTLR start NEXT
    public final void mNEXT() throws RecognitionException {
        try {
            int _type = NEXT;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:79:6: ( 'next' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:79:8: 'next'
            {
            match("next"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end NEXT

    // $ANTLR start OR
    public final void mOR() throws RecognitionException {
        try {
            int _type = OR;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:80:4: ( 'or' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:80:6: 'or'
            {
            match("or"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end OR

    // $ANTLR start PROCEDURE
    public final void mPROCEDURE() throws RecognitionException {
        try {
            int _type = PROCEDURE;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:81:11: ( 'procedure' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:81:13: 'procedure'
            {
            match("procedure"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end PROCEDURE

    // $ANTLR start RETURNING
    public final void mRETURNING() throws RecognitionException {
        try {
            int _type = RETURNING;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:82:11: ( 'returning' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:82:13: 'returning'
            {
            match("returning"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end RETURNING

    // $ANTLR start SEGMENT
    public final void mSEGMENT() throws RecognitionException {
        try {
            int _type = SEGMENT;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:83:9: ( 'segment' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:83:11: 'segment'
            {
            match("segment"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end SEGMENT

    // $ANTLR start SELECT
    public final void mSELECT() throws RecognitionException {
        try {
            int _type = SELECT;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:84:8: ( 'select' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:84:10: 'select'
            {
            match("select"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end SELECT

    // $ANTLR start SET
    public final void mSET() throws RecognitionException {
        try {
            int _type = SET;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:85:5: ( 'set' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:85:7: 'set'
            {
            match("set"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end SET

    // $ANTLR start SUBSTRING
    public final void mSUBSTRING() throws RecognitionException {
        try {
            int _type = SUBSTRING;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:86:11: ( 'substring' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:86:13: 'substring'
            {
            match("substring"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end SUBSTRING

    // $ANTLR start SUB_TYPE
    public final void mSUB_TYPE() throws RecognitionException {
        try {
            int _type = SUB_TYPE;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:87:10: ( 'sub_type' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:87:12: 'sub_type'
            {
            match("sub_type"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end SUB_TYPE

    // $ANTLR start SUM
    public final void mSUM() throws RecognitionException {
        try {
            int _type = SUM;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:88:5: ( 'sum' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:88:7: 'sum'
            {
            match("sum"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end SUM

    // $ANTLR start TRIM
    public final void mTRIM() throws RecognitionException {
        try {
            int _type = TRIM;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:89:6: ( 'trim' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:89:8: 'trim'
            {
            match("trim"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end TRIM

    // $ANTLR start TRAILING
    public final void mTRAILING() throws RecognitionException {
        try {
            int _type = TRAILING;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:90:10: ( 'trailing' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:90:12: 'trailing'
            {
            match("trailing"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end TRAILING

    // $ANTLR start UPDATE
    public final void mUPDATE() throws RecognitionException {
        try {
            int _type = UPDATE;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:91:8: ( 'update' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:91:10: 'update'
            {
            match("update"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end UPDATE

    // $ANTLR start VALUE
    public final void mVALUE() throws RecognitionException {
        try {
            int _type = VALUE;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:92:7: ( 'value' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:92:9: 'value'
            {
            match("value"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end VALUE

    // $ANTLR start VALUES
    public final void mVALUES() throws RecognitionException {
        try {
            int _type = VALUES;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:93:8: ( 'values' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:93:10: 'values'
            {
            match("values"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end VALUES

    // $ANTLR start KW_BLOB
    public final void mKW_BLOB() throws RecognitionException {
        try {
            int _type = KW_BLOB;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:94:9: ( 'blob' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:94:11: 'blob'
            {
            match("blob"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end KW_BLOB

    // $ANTLR start KW_BIGINT
    public final void mKW_BIGINT() throws RecognitionException {
        try {
            int _type = KW_BIGINT;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:95:11: ( 'bigint' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:95:13: 'bigint'
            {
            match("bigint"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end KW_BIGINT

    // $ANTLR start KW_CHAR
    public final void mKW_CHAR() throws RecognitionException {
        try {
            int _type = KW_CHAR;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:96:9: ( 'char' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:96:11: 'char'
            {
            match("char"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end KW_CHAR

    // $ANTLR start KW_DATE
    public final void mKW_DATE() throws RecognitionException {
        try {
            int _type = KW_DATE;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:97:9: ( 'date' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:97:11: 'date'
            {
            match("date"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end KW_DATE

    // $ANTLR start KW_DECIMAL
    public final void mKW_DECIMAL() throws RecognitionException {
        try {
            int _type = KW_DECIMAL;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:98:12: ( 'decimal' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:98:14: 'decimal'
            {
            match("decimal"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end KW_DECIMAL

    // $ANTLR start KW_DOUBLE
    public final void mKW_DOUBLE() throws RecognitionException {
        try {
            int _type = KW_DOUBLE;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:99:11: ( 'double' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:99:13: 'double'
            {
            match("double"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end KW_DOUBLE

    // $ANTLR start KW_PRECISION
    public final void mKW_PRECISION() throws RecognitionException {
        try {
            int _type = KW_PRECISION;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:100:14: ( 'precision' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:100:16: 'precision'
            {
            match("precision"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end KW_PRECISION

    // $ANTLR start KW_FLOAT
    public final void mKW_FLOAT() throws RecognitionException {
        try {
            int _type = KW_FLOAT;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:101:10: ( 'float' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:101:12: 'float'
            {
            match("float"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end KW_FLOAT

    // $ANTLR start KW_INTEGER
    public final void mKW_INTEGER() throws RecognitionException {
        try {
            int _type = KW_INTEGER;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:102:12: ( 'integer' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:102:14: 'integer'
            {
            match("integer"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end KW_INTEGER

    // $ANTLR start KW_INT
    public final void mKW_INT() throws RecognitionException {
        try {
            int _type = KW_INT;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:103:8: ( 'int' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:103:10: 'int'
            {
            match("int"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end KW_INT

    // $ANTLR start KW_NUMERIC
    public final void mKW_NUMERIC() throws RecognitionException {
        try {
            int _type = KW_NUMERIC;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:104:12: ( 'numeric' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:104:14: 'numeric'
            {
            match("numeric"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end KW_NUMERIC

    // $ANTLR start KW_SMALLINT
    public final void mKW_SMALLINT() throws RecognitionException {
        try {
            int _type = KW_SMALLINT;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:105:13: ( 'smallint' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:105:15: 'smallint'
            {
            match("smallint"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end KW_SMALLINT

    // $ANTLR start KW_TIME
    public final void mKW_TIME() throws RecognitionException {
        try {
            int _type = KW_TIME;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:106:9: ( 'time' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:106:11: 'time'
            {
            match("time"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end KW_TIME

    // $ANTLR start KW_TIMESTAMP
    public final void mKW_TIMESTAMP() throws RecognitionException {
        try {
            int _type = KW_TIMESTAMP;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:107:14: ( 'timestamp' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:107:16: 'timestamp'
            {
            match("timestamp"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end KW_TIMESTAMP

    // $ANTLR start KW_VARCHAR
    public final void mKW_VARCHAR() throws RecognitionException {
        try {
            int _type = KW_VARCHAR;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:108:12: ( 'varchar' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:108:14: 'varchar'
            {
            match("varchar"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end KW_VARCHAR

    // $ANTLR start KW_SET
    public final void mKW_SET() throws RecognitionException {
        try {
            int _type = KW_SET;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:109:8: ( 'set' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:109:10: 'set'
            {
            match("set"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end KW_SET

    // $ANTLR start KW_SIZE
    public final void mKW_SIZE() throws RecognitionException {
        try {
            int _type = KW_SIZE;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:110:9: ( 'size' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:110:11: 'size'
            {
            match("size"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end KW_SIZE

    // $ANTLR start T78
    public final void mT78() throws RecognitionException {
        try {
            int _type = T78;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:111:5: ( '=' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:111:7: '='
            {
            match('='); if (failed) return ;

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T78

    // $ANTLR start T79
    public final void mT79() throws RecognitionException {
        try {
            int _type = T79;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:112:5: ( '.' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:112:7: '.'
            {
            match('.'); if (failed) return ;

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T79

    // $ANTLR start T80
    public final void mT80() throws RecognitionException {
        try {
            int _type = T80;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:113:5: ( '+' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:113:7: '+'
            {
            match('+'); if (failed) return ;

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T80

    // $ANTLR start T81
    public final void mT81() throws RecognitionException {
        try {
            int _type = T81;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:114:5: ( '-' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:114:7: '-'
            {
            match('-'); if (failed) return ;

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T81

    // $ANTLR start T82
    public final void mT82() throws RecognitionException {
        try {
            int _type = T82;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:115:5: ( '*' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:115:7: '*'
            {
            match('*'); if (failed) return ;

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T82

    // $ANTLR start T83
    public final void mT83() throws RecognitionException {
        try {
            int _type = T83;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:116:5: ( '/' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:116:7: '/'
            {
            match('/'); if (failed) return ;

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T83

    // $ANTLR start T84
    public final void mT84() throws RecognitionException {
        try {
            int _type = T84;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:117:5: ( '||' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:117:7: '||'
            {
            match("||"); if (failed) return ;


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T84

    // $ANTLR start T85
    public final void mT85() throws RecognitionException {
        try {
            int _type = T85;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:118:5: ( '?' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:118:7: '?'
            {
            match('?'); if (failed) return ;

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T85

    // $ANTLR start T86
    public final void mT86() throws RecognitionException {
        try {
            int _type = T86;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:119:5: ( '[' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:119:7: '['
            {
            match('['); if (failed) return ;

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T86

    // $ANTLR start T87
    public final void mT87() throws RecognitionException {
        try {
            int _type = T87;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:120:5: ( ']' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:120:7: ']'
            {
            match(']'); if (failed) return ;

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T87

    // $ANTLR start T88
    public final void mT88() throws RecognitionException {
        try {
            int _type = T88;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:121:5: ( ':' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:121:7: ':'
            {
            match(':'); if (failed) return ;

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T88

    // $ANTLR start LEFT_PAREN
    public final void mLEFT_PAREN() throws RecognitionException {
        try {
            int _type = LEFT_PAREN;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:601:3: ( '(' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:601:5: '('
            {
            match('('); if (failed) return ;

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end LEFT_PAREN

    // $ANTLR start RIGHT_PAREN
    public final void mRIGHT_PAREN() throws RecognitionException {
        try {
            int _type = RIGHT_PAREN;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:605:3: ( ')' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:605:5: ')'
            {
            match(')'); if (failed) return ;

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end RIGHT_PAREN

    // $ANTLR start COMMA
    public final void mCOMMA() throws RecognitionException {
        try {
            int _type = COMMA;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:608:8: ( ',' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:608:10: ','
            {
            match(','); if (failed) return ;

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end COMMA

    // $ANTLR start GENERIC_ID
    public final void mGENERIC_ID() throws RecognitionException {
        try {
            int _type = GENERIC_ID;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:612:6: ( ( LETTER | '_' | ':' | '$' ) ( options {greedy=true; } : LETTER | '0' .. '9' | '.' | '-' | '_' | ':' | '$' )* )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:612:8: ( LETTER | '_' | ':' | '$' ) ( options {greedy=true; } : LETTER | '0' .. '9' | '.' | '-' | '_' | ':' | '$' )*
            {
            if ( input.LA(1)=='$'||input.LA(1)==':'||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();
            failed=false;
            }
            else {
                if (backtracking>0) {failed=true; return ;}
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:613:10: ( options {greedy=true; } : LETTER | '0' .. '9' | '.' | '-' | '_' | ':' | '$' )*
            loop1:
            do {
                int alt1=8;
                switch ( input.LA(1) ) {
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                case 'G':
                case 'H':
                case 'I':
                case 'J':
                case 'K':
                case 'L':
                case 'M':
                case 'N':
                case 'O':
                case 'P':
                case 'Q':
                case 'R':
                case 'S':
                case 'T':
                case 'U':
                case 'V':
                case 'W':
                case 'X':
                case 'Y':
                case 'Z':
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                case 'g':
                case 'h':
                case 'i':
                case 'j':
                case 'k':
                case 'l':
                case 'm':
                case 'n':
                case 'o':
                case 'p':
                case 'q':
                case 'r':
                case 's':
                case 't':
                case 'u':
                case 'v':
                case 'w':
                case 'x':
                case 'y':
                case 'z':
                    {
                    alt1=1;
                    }
                    break;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    {
                    alt1=2;
                    }
                    break;
                case '.':
                    {
                    alt1=3;
                    }
                    break;
                case '-':
                    {
                    alt1=4;
                    }
                    break;
                case '_':
                    {
                    alt1=5;
                    }
                    break;
                case ':':
                    {
                    alt1=6;
                    }
                    break;
                case '$':
                    {
                    alt1=7;
                    }
                    break;

                }

                switch (alt1) {
            	case 1 :
            	    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:613:37: LETTER
            	    {
            	    mLETTER(); if (failed) return ;

            	    }
            	    break;
            	case 2 :
            	    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:613:46: '0' .. '9'
            	    {
            	    matchRange('0','9'); if (failed) return ;

            	    }
            	    break;
            	case 3 :
            	    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:613:57: '.'
            	    {
            	    match('.'); if (failed) return ;

            	    }
            	    break;
            	case 4 :
            	    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:613:63: '-'
            	    {
            	    match('-'); if (failed) return ;

            	    }
            	    break;
            	case 5 :
            	    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:613:69: '_'
            	    {
            	    match('_'); if (failed) return ;

            	    }
            	    break;
            	case 6 :
            	    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:613:75: ':'
            	    {
            	    match(':'); if (failed) return ;

            	    }
            	    break;
            	case 7 :
            	    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:613:80: '$'
            	    {
            	    match('$'); if (failed) return ;

            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end GENERIC_ID

    // $ANTLR start QUOTED_ID
    public final void mQUOTED_ID() throws RecognitionException {
        try {
            int _type = QUOTED_ID;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:617:3: ( '\\\"' ( ( ESCqd )=> ESCqd | ~ '\\\"' )* '\\\"' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:617:5: '\\\"' ( ( ESCqd )=> ESCqd | ~ '\\\"' )* '\\\"'
            {
            match('\"'); if (failed) return ;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:617:10: ( ( ESCqd )=> ESCqd | ~ '\\\"' )*
            loop2:
            do {
                int alt2=3;
                int LA2_0 = input.LA(1);

                if ( (LA2_0=='\"') ) {
                    int LA2_1 = input.LA(2);

                    if ( (LA2_1=='\"') && (synpred1())) {
                        alt2=1;
                    }


                }
                else if ( ((LA2_0>='\u0000' && LA2_0<='!')||(LA2_0>='#' && LA2_0<='\uFFFE')) ) {
                    alt2=2;
                }


                switch (alt2) {
            	case 1 :
            	    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:617:11: ( ESCqd )=> ESCqd
            	    {
            	    mESCqd(); if (failed) return ;

            	    }
            	    break;
            	case 2 :
            	    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:617:28: ~ '\\\"'
            	    {
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='!')||(input.LA(1)>='#' && input.LA(1)<='\uFFFE') ) {
            	        input.consume();
            	    failed=false;
            	    }
            	    else {
            	        if (backtracking>0) {failed=true; return ;}
            	        MismatchedSetException mse =
            	            new MismatchedSetException(null,input);
            	        recover(mse);    throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);

            match('\"'); if (failed) return ;

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end QUOTED_ID

    // $ANTLR start LETTER
    public final void mLETTER() throws RecognitionException {
        try {
            int _type = LETTER;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:620:8: ( 'a' .. 'z' | 'A' .. 'Z' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:
            {
            if ( (input.LA(1)>='A' && input.LA(1)<='Z')||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();
            failed=false;
            }
            else {
                if (backtracking>0) {failed=true; return ;}
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end LETTER

    // $ANTLR start INTEGER
    public final void mINTEGER() throws RecognitionException {
        try {
            int _type = INTEGER;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:624:9: ( ( '-' )? ( '0' .. '9' )+ )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:624:11: ( '-' )? ( '0' .. '9' )+
            {
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:624:11: ( '-' )?
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0=='-') ) {
                alt3=1;
            }
            switch (alt3) {
                case 1 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:624:12: '-'
                    {
                    match('-'); if (failed) return ;

                    }
                    break;

            }

            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:624:17: ( '0' .. '9' )+
            int cnt4=0;
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( ((LA4_0>='0' && LA4_0<='9')) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:624:18: '0' .. '9'
            	    {
            	    matchRange('0','9'); if (failed) return ;

            	    }
            	    break;

            	default :
            	    if ( cnt4 >= 1 ) break loop4;
            	    if (backtracking>0) {failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(4, input);
                        throw eee;
                }
                cnt4++;
            } while (true);


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end INTEGER

    // $ANTLR start REAL
    public final void mREAL() throws RecognitionException {
        try {
            int _type = REAL;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:627:7: ( ( '-' )? ( '0' .. '9' )* '.' ( '0' .. '9' )+ )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:627:9: ( '-' )? ( '0' .. '9' )* '.' ( '0' .. '9' )+
            {
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:627:9: ( '-' )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0=='-') ) {
                alt5=1;
            }
            switch (alt5) {
                case 1 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:627:10: '-'
                    {
                    match('-'); if (failed) return ;

                    }
                    break;

            }

            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:627:15: ( '0' .. '9' )*
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( ((LA6_0>='0' && LA6_0<='9')) ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:627:16: '0' .. '9'
            	    {
            	    matchRange('0','9'); if (failed) return ;

            	    }
            	    break;

            	default :
            	    break loop6;
                }
            } while (true);

            match('.'); if (failed) return ;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:627:29: ( '0' .. '9' )+
            int cnt7=0;
            loop7:
            do {
                int alt7=2;
                int LA7_0 = input.LA(1);

                if ( ((LA7_0>='0' && LA7_0<='9')) ) {
                    alt7=1;
                }


                switch (alt7) {
            	case 1 :
            	    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:627:30: '0' .. '9'
            	    {
            	    matchRange('0','9'); if (failed) return ;

            	    }
            	    break;

            	default :
            	    if ( cnt7 >= 1 ) break loop7;
            	    if (backtracking>0) {failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(7, input);
                        throw eee;
                }
                cnt7++;
            } while (true);


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end REAL

    // $ANTLR start WS
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:630:5: ( ( ' ' | '\\t' | '\\n' | '\\r' )+ )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:630:7: ( ' ' | '\\t' | '\\n' | '\\r' )+
            {
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:630:7: ( ' ' | '\\t' | '\\n' | '\\r' )+
            int cnt8=0;
            loop8:
            do {
                int alt8=2;
                int LA8_0 = input.LA(1);

                if ( ((LA8_0>='\t' && LA8_0<='\n')||LA8_0=='\r'||LA8_0==' ') ) {
                    alt8=1;
                }


                switch (alt8) {
            	case 1 :
            	    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:
            	    {
            	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||input.LA(1)=='\r'||input.LA(1)==' ' ) {
            	        input.consume();
            	    failed=false;
            	    }
            	    else {
            	        if (backtracking>0) {failed=true; return ;}
            	        MismatchedSetException mse =
            	            new MismatchedSetException(null,input);
            	        recover(mse);    throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    if ( cnt8 >= 1 ) break loop8;
            	    if (backtracking>0) {failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(8, input);
                        throw eee;
                }
                cnt8++;
            } while (true);

            if ( backtracking==0 ) {
              channel = HIDDEN;
            }

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end WS

    // $ANTLR start SL_COMMENT
    public final void mSL_COMMENT() throws RecognitionException {
        try {
            int _type = SL_COMMENT;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:634:9: ( '--' (~ ( '\\n' | '\\r' ) )* ( '\\n' | '\\r' ( '\\n' )? ) )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:634:11: '--' (~ ( '\\n' | '\\r' ) )* ( '\\n' | '\\r' ( '\\n' )? )
            {
            match("--"); if (failed) return ;

            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:634:16: (~ ( '\\n' | '\\r' ) )*
            loop9:
            do {
                int alt9=2;
                int LA9_0 = input.LA(1);

                if ( ((LA9_0>='\u0000' && LA9_0<='\t')||(LA9_0>='\u000B' && LA9_0<='\f')||(LA9_0>='\u000E' && LA9_0<='\uFFFE')) ) {
                    alt9=1;
                }


                switch (alt9) {
            	case 1 :
            	    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:634:17: ~ ( '\\n' | '\\r' )
            	    {
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='\t')||(input.LA(1)>='\u000B' && input.LA(1)<='\f')||(input.LA(1)>='\u000E' && input.LA(1)<='\uFFFE') ) {
            	        input.consume();
            	    failed=false;
            	    }
            	    else {
            	        if (backtracking>0) {failed=true; return ;}
            	        MismatchedSetException mse =
            	            new MismatchedSetException(null,input);
            	        recover(mse);    throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    break loop9;
                }
            } while (true);

            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:634:32: ( '\\n' | '\\r' ( '\\n' )? )
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0=='\n') ) {
                alt11=1;
            }
            else if ( (LA11_0=='\r') ) {
                alt11=2;
            }
            else {
                if (backtracking>0) {failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("634:32: ( '\\n' | '\\r' ( '\\n' )? )", 11, 0, input);

                throw nvae;
            }
            switch (alt11) {
                case 1 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:634:33: '\\n'
                    {
                    match('\n'); if (failed) return ;

                    }
                    break;
                case 2 :
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:634:38: '\\r' ( '\\n' )?
                    {
                    match('\r'); if (failed) return ;
                    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:634:42: ( '\\n' )?
                    int alt10=2;
                    int LA10_0 = input.LA(1);

                    if ( (LA10_0=='\n') ) {
                        alt10=1;
                    }
                    switch (alt10) {
                        case 1 :
                            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:634:43: '\\n'
                            {
                            match('\n'); if (failed) return ;

                            }
                            break;

                    }


                    }
                    break;

            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end SL_COMMENT

    // $ANTLR start STRING
    public final void mSTRING() throws RecognitionException {
        try {
            int _type = STRING;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:638:3: ( ( '\\'' ( ( ESCqs )=> ESCqs | ~ '\\'' )* '\\'' ) )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:638:5: ( '\\'' ( ( ESCqs )=> ESCqs | ~ '\\'' )* '\\'' )
            {
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:638:5: ( '\\'' ( ( ESCqs )=> ESCqs | ~ '\\'' )* '\\'' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:638:7: '\\'' ( ( ESCqs )=> ESCqs | ~ '\\'' )* '\\''
            {
            match('\''); if (failed) return ;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:638:12: ( ( ESCqs )=> ESCqs | ~ '\\'' )*
            loop12:
            do {
                int alt12=3;
                int LA12_0 = input.LA(1);

                if ( (LA12_0=='\'') ) {
                    int LA12_1 = input.LA(2);

                    if ( (LA12_1=='\'') && (synpred2())) {
                        alt12=1;
                    }


                }
                else if ( ((LA12_0>='\u0000' && LA12_0<='&')||(LA12_0>='(' && LA12_0<='\uFFFE')) ) {
                    alt12=2;
                }


                switch (alt12) {
            	case 1 :
            	    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:638:13: ( ESCqs )=> ESCqs
            	    {
            	    mESCqs(); if (failed) return ;

            	    }
            	    break;
            	case 2 :
            	    // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:638:30: ~ '\\''
            	    {
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='&')||(input.LA(1)>='(' && input.LA(1)<='\uFFFE') ) {
            	        input.consume();
            	    failed=false;
            	    }
            	    else {
            	        if (backtracking>0) {failed=true; return ;}
            	        MismatchedSetException mse =
            	            new MismatchedSetException(null,input);
            	        recover(mse);    throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    break loop12;
                }
            } while (true);

            match('\''); if (failed) return ;

            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end STRING

    // $ANTLR start ESCqs
    public final void mESCqs() throws RecognitionException {
        try {
            int _type = ESCqs;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:641:7: ( '\\'' '\\'' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:641:9: '\\'' '\\''
            {
            match('\''); if (failed) return ;
            match('\''); if (failed) return ;

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end ESCqs

    // $ANTLR start ESCqd
    public final void mESCqd() throws RecognitionException {
        try {
            int _type = ESCqd;
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:642:7: ( '\\\"' '\\\"' )
            // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:642:9: '\\\"' '\\\"'
            {
            match('\"'); if (failed) return ;
            match('\"'); if (failed) return ;

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end ESCqd

    public void mTokens() throws RecognitionException {
        // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:8: ( ALL | AS | AVG | BOTH | CAST | CHARACTER | COUNT | COLLATE | CURRENT_USER | CURRENT_ROLE | CURRENT_DATE | CURRENT_TIME | CURRENT_TIMESTAMP | DEFAULT | DELETE | DISTINCT | DB_KEY | EXTRACT | EXECUTE | FOR | FROM | GEN_ID | INSERT | INTO | LEADING | MATCHING | MINIMUM | MAXIMUM | NULL | NEXT | OR | PROCEDURE | RETURNING | SEGMENT | SELECT | SET | SUBSTRING | SUB_TYPE | SUM | TRIM | TRAILING | UPDATE | VALUE | VALUES | KW_BLOB | KW_BIGINT | KW_CHAR | KW_DATE | KW_DECIMAL | KW_DOUBLE | KW_PRECISION | KW_FLOAT | KW_INTEGER | KW_INT | KW_NUMERIC | KW_SMALLINT | KW_TIME | KW_TIMESTAMP | KW_VARCHAR | KW_SET | KW_SIZE | T78 | T79 | T80 | T81 | T82 | T83 | T84 | T85 | T86 | T87 | T88 | LEFT_PAREN | RIGHT_PAREN | COMMA | GENERIC_ID | QUOTED_ID | LETTER | INTEGER | REAL | WS | SL_COMMENT | STRING | ESCqs | ESCqd )
        int alt13=85;
        alt13 = dfa13.predict(input);
        switch (alt13) {
            case 1 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:10: ALL
                {
                mALL(); if (failed) return ;

                }
                break;
            case 2 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:14: AS
                {
                mAS(); if (failed) return ;

                }
                break;
            case 3 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:17: AVG
                {
                mAVG(); if (failed) return ;

                }
                break;
            case 4 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:21: BOTH
                {
                mBOTH(); if (failed) return ;

                }
                break;
            case 5 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:26: CAST
                {
                mCAST(); if (failed) return ;

                }
                break;
            case 6 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:31: CHARACTER
                {
                mCHARACTER(); if (failed) return ;

                }
                break;
            case 7 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:41: COUNT
                {
                mCOUNT(); if (failed) return ;

                }
                break;
            case 8 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:47: COLLATE
                {
                mCOLLATE(); if (failed) return ;

                }
                break;
            case 9 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:55: CURRENT_USER
                {
                mCURRENT_USER(); if (failed) return ;

                }
                break;
            case 10 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:68: CURRENT_ROLE
                {
                mCURRENT_ROLE(); if (failed) return ;

                }
                break;
            case 11 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:81: CURRENT_DATE
                {
                mCURRENT_DATE(); if (failed) return ;

                }
                break;
            case 12 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:94: CURRENT_TIME
                {
                mCURRENT_TIME(); if (failed) return ;

                }
                break;
            case 13 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:107: CURRENT_TIMESTAMP
                {
                mCURRENT_TIMESTAMP(); if (failed) return ;

                }
                break;
            case 14 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:125: DEFAULT
                {
                mDEFAULT(); if (failed) return ;

                }
                break;
            case 15 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:133: DELETE
                {
                mDELETE(); if (failed) return ;

                }
                break;
            case 16 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:140: DISTINCT
                {
                mDISTINCT(); if (failed) return ;

                }
                break;
            case 17 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:149: DB_KEY
                {
                mDB_KEY(); if (failed) return ;

                }
                break;
            case 18 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:156: EXTRACT
                {
                mEXTRACT(); if (failed) return ;

                }
                break;
            case 19 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:164: EXECUTE
                {
                mEXECUTE(); if (failed) return ;

                }
                break;
            case 20 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:172: FOR
                {
                mFOR(); if (failed) return ;

                }
                break;
            case 21 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:176: FROM
                {
                mFROM(); if (failed) return ;

                }
                break;
            case 22 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:181: GEN_ID
                {
                mGEN_ID(); if (failed) return ;

                }
                break;
            case 23 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:188: INSERT
                {
                mINSERT(); if (failed) return ;

                }
                break;
            case 24 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:195: INTO
                {
                mINTO(); if (failed) return ;

                }
                break;
            case 25 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:200: LEADING
                {
                mLEADING(); if (failed) return ;

                }
                break;
            case 26 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:208: MATCHING
                {
                mMATCHING(); if (failed) return ;

                }
                break;
            case 27 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:217: MINIMUM
                {
                mMINIMUM(); if (failed) return ;

                }
                break;
            case 28 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:225: MAXIMUM
                {
                mMAXIMUM(); if (failed) return ;

                }
                break;
            case 29 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:233: NULL
                {
                mNULL(); if (failed) return ;

                }
                break;
            case 30 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:238: NEXT
                {
                mNEXT(); if (failed) return ;

                }
                break;
            case 31 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:243: OR
                {
                mOR(); if (failed) return ;

                }
                break;
            case 32 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:246: PROCEDURE
                {
                mPROCEDURE(); if (failed) return ;

                }
                break;
            case 33 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:256: RETURNING
                {
                mRETURNING(); if (failed) return ;

                }
                break;
            case 34 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:266: SEGMENT
                {
                mSEGMENT(); if (failed) return ;

                }
                break;
            case 35 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:274: SELECT
                {
                mSELECT(); if (failed) return ;

                }
                break;
            case 36 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:281: SET
                {
                mSET(); if (failed) return ;

                }
                break;
            case 37 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:285: SUBSTRING
                {
                mSUBSTRING(); if (failed) return ;

                }
                break;
            case 38 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:295: SUB_TYPE
                {
                mSUB_TYPE(); if (failed) return ;

                }
                break;
            case 39 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:304: SUM
                {
                mSUM(); if (failed) return ;

                }
                break;
            case 40 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:308: TRIM
                {
                mTRIM(); if (failed) return ;

                }
                break;
            case 41 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:313: TRAILING
                {
                mTRAILING(); if (failed) return ;

                }
                break;
            case 42 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:322: UPDATE
                {
                mUPDATE(); if (failed) return ;

                }
                break;
            case 43 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:329: VALUE
                {
                mVALUE(); if (failed) return ;

                }
                break;
            case 44 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:335: VALUES
                {
                mVALUES(); if (failed) return ;

                }
                break;
            case 45 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:342: KW_BLOB
                {
                mKW_BLOB(); if (failed) return ;

                }
                break;
            case 46 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:350: KW_BIGINT
                {
                mKW_BIGINT(); if (failed) return ;

                }
                break;
            case 47 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:360: KW_CHAR
                {
                mKW_CHAR(); if (failed) return ;

                }
                break;
            case 48 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:368: KW_DATE
                {
                mKW_DATE(); if (failed) return ;

                }
                break;
            case 49 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:376: KW_DECIMAL
                {
                mKW_DECIMAL(); if (failed) return ;

                }
                break;
            case 50 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:387: KW_DOUBLE
                {
                mKW_DOUBLE(); if (failed) return ;

                }
                break;
            case 51 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:397: KW_PRECISION
                {
                mKW_PRECISION(); if (failed) return ;

                }
                break;
            case 52 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:410: KW_FLOAT
                {
                mKW_FLOAT(); if (failed) return ;

                }
                break;
            case 53 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:419: KW_INTEGER
                {
                mKW_INTEGER(); if (failed) return ;

                }
                break;
            case 54 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:430: KW_INT
                {
                mKW_INT(); if (failed) return ;

                }
                break;
            case 55 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:437: KW_NUMERIC
                {
                mKW_NUMERIC(); if (failed) return ;

                }
                break;
            case 56 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:448: KW_SMALLINT
                {
                mKW_SMALLINT(); if (failed) return ;

                }
                break;
            case 57 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:460: KW_TIME
                {
                mKW_TIME(); if (failed) return ;

                }
                break;
            case 58 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:468: KW_TIMESTAMP
                {
                mKW_TIMESTAMP(); if (failed) return ;

                }
                break;
            case 59 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:481: KW_VARCHAR
                {
                mKW_VARCHAR(); if (failed) return ;

                }
                break;
            case 60 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:492: KW_SET
                {
                mKW_SET(); if (failed) return ;

                }
                break;
            case 61 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:499: KW_SIZE
                {
                mKW_SIZE(); if (failed) return ;

                }
                break;
            case 62 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:507: T78
                {
                mT78(); if (failed) return ;

                }
                break;
            case 63 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:511: T79
                {
                mT79(); if (failed) return ;

                }
                break;
            case 64 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:515: T80
                {
                mT80(); if (failed) return ;

                }
                break;
            case 65 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:519: T81
                {
                mT81(); if (failed) return ;

                }
                break;
            case 66 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:523: T82
                {
                mT82(); if (failed) return ;

                }
                break;
            case 67 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:527: T83
                {
                mT83(); if (failed) return ;

                }
                break;
            case 68 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:531: T84
                {
                mT84(); if (failed) return ;

                }
                break;
            case 69 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:535: T85
                {
                mT85(); if (failed) return ;

                }
                break;
            case 70 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:539: T86
                {
                mT86(); if (failed) return ;

                }
                break;
            case 71 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:543: T87
                {
                mT87(); if (failed) return ;

                }
                break;
            case 72 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:547: T88
                {
                mT88(); if (failed) return ;

                }
                break;
            case 73 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:551: LEFT_PAREN
                {
                mLEFT_PAREN(); if (failed) return ;

                }
                break;
            case 74 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:562: RIGHT_PAREN
                {
                mRIGHT_PAREN(); if (failed) return ;

                }
                break;
            case 75 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:574: COMMA
                {
                mCOMMA(); if (failed) return ;

                }
                break;
            case 76 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:580: GENERIC_ID
                {
                mGENERIC_ID(); if (failed) return ;

                }
                break;
            case 77 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:591: QUOTED_ID
                {
                mQUOTED_ID(); if (failed) return ;

                }
                break;
            case 78 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:601: LETTER
                {
                mLETTER(); if (failed) return ;

                }
                break;
            case 79 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:608: INTEGER
                {
                mINTEGER(); if (failed) return ;

                }
                break;
            case 80 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:616: REAL
                {
                mREAL(); if (failed) return ;

                }
                break;
            case 81 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:621: WS
                {
                mWS(); if (failed) return ;

                }
                break;
            case 82 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:624: SL_COMMENT
                {
                mSL_COMMENT(); if (failed) return ;

                }
                break;
            case 83 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:635: STRING
                {
                mSTRING(); if (failed) return ;

                }
                break;
            case 84 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:642: ESCqs
                {
                mESCqs(); if (failed) return ;

                }
                break;
            case 85 :
                // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:1:648: ESCqd
                {
                mESCqd(); if (failed) return ;

                }
                break;

        }

    }

    // $ANTLR start synpred1
    public final void synpred1_fragment() throws RecognitionException {   
        // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:617:11: ( ESCqd )
        // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:617:12: ESCqd
        {
        mESCqd(); if (failed) return ;

        }
    }
    // $ANTLR end synpred1

    // $ANTLR start synpred2
    public final void synpred2_fragment() throws RecognitionException {   
        // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:638:13: ( ESCqs )
        // D:\\projects\\Firebird\\client-java.22.head\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g3:638:14: ESCqs
        {
        mESCqs(); if (failed) return ;

        }
    }
    // $ANTLR end synpred2

    public final boolean synpred1() {
        backtracking++;
        int start = input.mark();
        try {
            synpred1_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !failed;
        input.rewind(start);
        backtracking--;
        failed=false;
        return success;
    }
    public final boolean synpred2() {
        backtracking++;
        int start = input.mark();
        try {
            synpred2_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !failed;
        input.rewind(start);
        backtracking--;
        failed=false;
        return success;
    }


    protected DFA13 dfa13 = new DFA13(this);
    static final String DFA13_eotS =
        "\1\uffff\22\43\1\uffff\1\115\1\uffff\1\117\6\uffff\1\120\6\uffff"+
        "\1\123\2\uffff\1\126\31\43\1\167\12\43\13\uffff\1\u0088\1\u0089"+
        "\22\43\1\u009c\2\43\1\u00a1\2\43\1\u00a4\1\43\1\u00a6\3\43\1\uffff"+
        "\5\43\1\u00af\3\43\1\u00b4\6\43\2\uffff\1\u00bb\1\43\1\u00bd\3\43"+
        "\1\u00c2\1\u00c3\4\43\1\u00c8\5\43\1\uffff\1\u00ce\1\43\1\u00d0"+
        "\1\43\1\uffff\2\43\1\uffff\1\43\1\uffff\1\43\1\u00d6\1\u00d7\5\43"+
        "\1\uffff\1\43\1\u00de\2\43\1\uffff\1\u00e2\1\u00e3\4\43\1\uffff"+
        "\1\43\1\uffff\1\43\1\u00ea\2\43\2\uffff\4\43\1\uffff\4\43\1\u00f5"+
        "\1\uffff\1\43\1\uffff\5\43\2\uffff\6\43\1\uffff\3\43\2\uffff\2\43"+
        "\1\u0108\1\43\1\u010a\1\43\1\uffff\2\43\1\u010e\2\43\1\u0111\1\43"+
        "\1\u0113\2\43\1\uffff\1\u0116\1\43\1\u0118\7\43\1\u0120\5\43\1\u0126"+
        "\1\u0127\1\uffff\1\43\1\uffff\1\u0129\2\43\1\uffff\1\u012c\1\u012d"+
        "\1\uffff\1\43\1\uffff\1\u012f\1\u0130\1\uffff\1\u0131\1\uffff\1"+
        "\u0132\1\43\1\u0134\4\43\1\uffff\1\u0139\4\43\2\uffff\1\u013e\1"+
        "\uffff\2\43\2\uffff\1\u0144\4\uffff\1\u0145\1\uffff\3\43\1\u0149"+
        "\1\uffff\1\u014a\2\43\1\u014d\1\uffff\4\43\1\u0152\2\uffff\1\u0153"+
        "\1\u0154\1\u0155\2\uffff\1\u0156\1\u0157\1\uffff\4\43\6\uffff\4"+
        "\43\1\u0161\1\u0162\1\u0163\1\u0164\1\43\4\uffff\3\43\1\u0169\1"+
        "\uffff";
    static final String DFA13_eofS =
        "\u016a\uffff";
    static final String DFA13_minS =
        "\1\11\1\154\1\151\2\141\1\170\1\154\1\145\1\156\1\145\1\141\1\145"+
        "\2\162\2\145\1\151\1\160\1\141\1\uffff\1\60\1\uffff\1\55\6\uffff"+
        "\1\44\4\uffff\1\0\1\uffff\1\56\1\uffff\1\0\1\44\1\154\1\147\1\164"+
        "\1\147\1\157\1\154\1\162\1\141\1\163\1\165\1\143\1\164\1\163\1\137"+
        "\1\145\1\157\1\162\1\157\1\156\1\163\1\141\1\164\1\156\1\154\1\170"+
        "\1\44\1\145\1\164\1\141\1\147\1\172\1\142\1\155\1\141\1\144\1\154"+
        "\13\uffff\2\44\1\150\1\151\1\142\1\154\1\156\2\162\1\164\1\142\1"+
        "\151\1\141\2\145\1\164\1\153\1\162\1\143\1\141\1\44\1\155\1\137"+
        "\1\44\1\145\1\144\1\44\1\143\1\44\1\145\1\154\1\164\1\uffff\2\143"+
        "\1\165\1\154\1\145\1\44\1\155\1\145\1\137\1\44\1\145\1\155\1\151"+
        "\1\141\1\165\1\143\2\uffff\1\44\1\156\1\44\1\141\1\164\1\145\2\44"+
        "\1\154\1\155\1\165\1\164\1\44\1\151\1\145\1\141\1\165\1\164\1\uffff"+
        "\1\44\1\151\1\44\1\147\1\uffff\1\162\1\151\1\uffff\1\150\1\uffff"+
        "\1\162\2\44\1\151\1\145\1\162\1\154\1\143\1\uffff\1\145\1\44\2\164"+
        "\1\uffff\2\44\1\154\1\164\1\145\1\150\1\uffff\1\164\1\uffff\1\164"+
        "\1\44\1\156\1\143\2\uffff\1\145\1\141\1\154\1\145\1\uffff\1\156"+
        "\1\171\1\143\1\164\1\44\1\uffff\1\144\1\uffff\1\145\1\164\1\156"+
        "\2\151\2\uffff\1\163\1\144\1\156\1\151\1\164\1\156\1\uffff\1\171"+
        "\1\162\1\164\2\uffff\1\151\1\145\1\44\1\141\1\44\1\145\1\uffff\2"+
        "\164\1\44\1\154\1\164\1\44\1\143\1\44\1\164\1\145\1\uffff\1\44\1"+
        "\162\1\44\1\147\1\156\1\143\1\151\1\165\1\151\1\156\1\44\1\164\1"+
        "\160\1\151\1\141\1\156\2\44\1\uffff\1\162\1\uffff\1\44\1\137\1\145"+
        "\1\uffff\2\44\1\uffff\1\164\1\uffff\2\44\1\uffff\1\44\1\uffff\1"+
        "\44\1\147\1\44\1\157\1\162\1\156\1\164\1\uffff\1\44\1\145\1\156"+
        "\1\155\1\147\2\uffff\1\44\1\uffff\1\144\1\162\2\uffff\1\44\4\uffff"+
        "\1\44\1\uffff\1\156\1\145\1\147\1\44\1\uffff\1\44\1\147\1\160\1"+
        "\44\1\uffff\1\151\1\163\1\157\1\141\1\44\2\uffff\3\44\2\uffff\2"+
        "\44\1\uffff\1\155\1\145\1\154\1\164\6\uffff\1\145\1\162\2\145\4"+
        "\44\1\164\4\uffff\1\141\1\155\1\160\1\44\1\uffff";
    static final String DFA13_maxS =
        "\1\174\1\166\1\157\1\165\1\157\1\170\1\162\1\145\1\156\1\145\1\151"+
        "\1\165\2\162\1\145\1\165\1\162\1\160\1\141\1\uffff\1\71\1\uffff"+
        "\1\71\6\uffff\1\172\4\uffff\1\ufffe\1\uffff\1\71\1\uffff\1\ufffe"+
        "\1\172\1\154\1\147\1\164\1\147\1\157\1\165\1\162\1\141\1\163\1\165"+
        "\1\154\1\164\1\163\1\137\1\164\1\157\1\162\1\157\1\156\1\164\1\141"+
        "\1\170\1\156\1\155\1\170\1\172\1\157\1\164\1\141\1\164\1\172\2\155"+
        "\1\151\1\144\1\162\13\uffff\2\172\1\150\1\151\1\142\1\154\1\156"+
        "\2\162\1\164\1\142\1\151\1\141\2\145\1\164\1\153\1\162\1\143\1\141"+
        "\1\172\1\155\1\137\1\172\1\145\1\144\1\172\1\143\1\172\1\145\1\154"+
        "\1\164\1\uffff\2\143\1\165\1\154\1\145\1\172\1\155\1\145\1\163\1"+
        "\172\1\145\1\155\1\151\1\141\1\165\1\143\2\uffff\1\172\1\156\1\172"+
        "\1\141\1\164\1\145\2\172\1\154\1\155\1\165\1\164\1\172\1\151\1\145"+
        "\1\141\1\165\1\164\1\uffff\1\172\1\151\1\172\1\147\1\uffff\1\162"+
        "\1\151\1\uffff\1\150\1\uffff\1\162\2\172\1\151\1\145\1\162\1\154"+
        "\1\143\1\uffff\1\145\1\172\2\164\1\uffff\2\172\1\154\1\164\1\145"+
        "\1\150\1\uffff\1\164\1\uffff\1\164\1\172\1\156\1\143\2\uffff\1\145"+
        "\1\141\1\154\1\145\1\uffff\1\156\1\171\1\143\1\164\1\172\1\uffff"+
        "\1\144\1\uffff\1\145\1\164\1\156\2\151\2\uffff\1\163\1\144\1\156"+
        "\1\151\1\164\1\156\1\uffff\1\171\1\162\1\164\2\uffff\1\151\1\145"+
        "\1\172\1\141\1\172\1\145\1\uffff\2\164\1\172\1\154\1\164\1\172\1"+
        "\143\1\172\1\164\1\145\1\uffff\1\172\1\162\1\172\1\147\1\156\1\143"+
        "\1\151\1\165\1\151\1\156\1\172\1\164\1\160\1\151\1\141\1\156\2\172"+
        "\1\uffff\1\162\1\uffff\1\172\1\137\1\145\1\uffff\2\172\1\uffff\1"+
        "\164\1\uffff\2\172\1\uffff\1\172\1\uffff\1\172\1\147\1\172\1\157"+
        "\1\162\1\156\1\164\1\uffff\1\172\1\145\1\156\1\155\1\147\2\uffff"+
        "\1\172\1\uffff\1\165\1\162\2\uffff\1\172\4\uffff\1\172\1\uffff\1"+
        "\156\1\145\1\147\1\172\1\uffff\1\172\1\147\1\160\1\172\1\uffff\1"+
        "\151\1\163\1\157\1\141\1\172\2\uffff\3\172\2\uffff\2\172\1\uffff"+
        "\1\155\1\145\1\154\1\164\6\uffff\1\145\1\162\2\145\4\172\1\164\4"+
        "\uffff\1\141\1\155\1\160\1\172\1\uffff";
    static final String DFA13_acceptS =
        "\23\uffff\1\76\1\uffff\1\100\1\uffff\1\102\1\103\1\104\1\105\1\106"+
        "\1\107\1\uffff\1\111\1\112\1\113\1\114\1\uffff\1\114\1\uffff\1\121"+
        "\46\uffff\1\120\1\77\1\122\1\101\1\110\2\115\1\117\2\123\1\2\40"+
        "\uffff\1\37\20\uffff\1\1\1\3\22\uffff\1\24\4\uffff\1\66\2\uffff"+
        "\1\34\1\uffff\1\33\10\uffff\1\44\4\uffff\1\47\6\uffff\1\4\1\uffff"+
        "\1\55\4\uffff\1\57\1\5\4\uffff\1\60\5\uffff\1\25\1\uffff\1\30\5"+
        "\uffff\1\35\1\36\6\uffff\1\75\3\uffff\1\71\1\50\6\uffff\1\7\12\uffff"+
        "\1\64\22\uffff\1\53\1\uffff\1\56\3\uffff\1\62\2\uffff\1\17\1\uffff"+
        "\1\21\2\uffff\1\26\1\uffff\1\27\7\uffff\1\43\5\uffff\1\52\1\54\1"+
        "\uffff\1\10\2\uffff\1\61\1\16\1\uffff\1\22\1\23\1\65\1\31\1\uffff"+
        "\1\67\4\uffff\1\42\4\uffff\1\73\5\uffff\1\20\1\32\3\uffff\1\70\1"+
        "\46\2\uffff\1\51\4\uffff\1\6\1\63\1\40\1\41\1\45\1\72\11\uffff\1"+
        "\14\1\11\1\12\1\13\4\uffff\1\15";
    static final String DFA13_specialS =
        "\u016a\uffff}>";
    static final String[] DFA13_transitionS = {
            "\2\45\2\uffff\1\45\22\uffff\1\45\1\uffff\1\42\1\uffff\1\43\2"+
            "\uffff\1\46\1\36\1\37\1\27\1\25\1\40\1\26\1\24\1\30\12\44\1"+
            "\35\2\uffff\1\23\1\uffff\1\32\1\uffff\32\41\1\33\1\uffff\1\34"+
            "\1\uffff\1\43\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\41\1\10"+
            "\2\41\1\11\1\12\1\13\1\14\1\15\1\41\1\16\1\17\1\20\1\21\1\22"+
            "\4\41\1\uffff\1\31",
            "\1\50\6\uffff\1\47\2\uffff\1\51",
            "\1\53\2\uffff\1\54\2\uffff\1\52",
            "\1\60\6\uffff\1\57\6\uffff\1\55\5\uffff\1\56",
            "\1\63\1\65\2\uffff\1\62\3\uffff\1\64\5\uffff\1\61",
            "\1\66",
            "\1\67\2\uffff\1\70\2\uffff\1\71",
            "\1\72",
            "\1\73",
            "\1\74",
            "\1\75\7\uffff\1\76",
            "\1\100\17\uffff\1\77",
            "\1\101",
            "\1\102",
            "\1\103",
            "\1\105\3\uffff\1\106\3\uffff\1\104\7\uffff\1\107",
            "\1\110\10\uffff\1\111",
            "\1\112",
            "\1\113",
            "",
            "\12\114",
            "",
            "\1\116\1\114\1\uffff\12\44",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "",
            "",
            "",
            "",
            "\42\122\1\121\uffdc\122",
            "",
            "\1\114\1\uffff\12\44",
            "",
            "\47\125\1\124\uffd7\125",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\127",
            "\1\130",
            "\1\131",
            "\1\132",
            "\1\133",
            "\1\134\10\uffff\1\135",
            "\1\136",
            "\1\137",
            "\1\140",
            "\1\141",
            "\1\142\2\uffff\1\143\5\uffff\1\144",
            "\1\145",
            "\1\146",
            "\1\147",
            "\1\151\16\uffff\1\150",
            "\1\152",
            "\1\153",
            "\1\154",
            "\1\155",
            "\1\157\1\156",
            "\1\160",
            "\1\162\3\uffff\1\161",
            "\1\163",
            "\1\165\1\164",
            "\1\166",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\170\11\uffff\1\171",
            "\1\172",
            "\1\173",
            "\1\176\4\uffff\1\174\7\uffff\1\175",
            "\1\177",
            "\1\u0080\12\uffff\1\u0081",
            "\1\u0082",
            "\1\u0084\7\uffff\1\u0083",
            "\1\u0085",
            "\1\u0086\5\uffff\1\u0087",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u008a",
            "\1\u008b",
            "\1\u008c",
            "\1\u008d",
            "\1\u008e",
            "\1\u008f",
            "\1\u0090",
            "\1\u0091",
            "\1\u0092",
            "\1\u0093",
            "\1\u0094",
            "\1\u0095",
            "\1\u0096",
            "\1\u0097",
            "\1\u0098",
            "\1\u0099",
            "\1\u009a",
            "\1\u009b",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u009d",
            "\1\u009e",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\4\43\1\u00a0\11\43\1\u009f\13\43",
            "\1\u00a2",
            "\1\u00a3",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u00a5",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u00a7",
            "\1\u00a8",
            "\1\u00a9",
            "",
            "\1\u00aa",
            "\1\u00ab",
            "\1\u00ac",
            "\1\u00ad",
            "\1\u00ae",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u00b0",
            "\1\u00b1",
            "\1\u00b2\23\uffff\1\u00b3",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u00b5",
            "\1\u00b6",
            "\1\u00b7",
            "\1\u00b8",
            "\1\u00b9",
            "\1\u00ba",
            "",
            "",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u00bc",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u00be",
            "\1\u00bf",
            "\1\u00c0",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\1\u00c1\31\43",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u00c4",
            "\1\u00c5",
            "\1\u00c6",
            "\1\u00c7",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u00c9",
            "\1\u00ca",
            "\1\u00cb",
            "\1\u00cc",
            "\1\u00cd",
            "",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u00cf",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u00d1",
            "",
            "\1\u00d2",
            "\1\u00d3",
            "",
            "\1\u00d4",
            "",
            "\1\u00d5",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u00d8",
            "\1\u00d9",
            "\1\u00da",
            "\1\u00db",
            "\1\u00dc",
            "",
            "\1\u00dd",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u00df",
            "\1\u00e0",
            "",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\22\43\1\u00e1\7\43",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u00e4",
            "\1\u00e5",
            "\1\u00e6",
            "\1\u00e7",
            "",
            "\1\u00e8",
            "",
            "\1\u00e9",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u00eb",
            "\1\u00ec",
            "",
            "",
            "\1\u00ed",
            "\1\u00ee",
            "\1\u00ef",
            "\1\u00f0",
            "",
            "\1\u00f1",
            "\1\u00f2",
            "\1\u00f3",
            "\1\u00f4",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "",
            "\1\u00f6",
            "",
            "\1\u00f7",
            "\1\u00f8",
            "\1\u00f9",
            "\1\u00fa",
            "\1\u00fb",
            "",
            "",
            "\1\u00fc",
            "\1\u00fd",
            "\1\u00fe",
            "\1\u00ff",
            "\1\u0100",
            "\1\u0101",
            "",
            "\1\u0102",
            "\1\u0103",
            "\1\u0104",
            "",
            "",
            "\1\u0105",
            "\1\u0106",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\22\43\1\u0107\7\43",
            "\1\u0109",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u010b",
            "",
            "\1\u010c",
            "\1\u010d",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u010f",
            "\1\u0110",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u0112",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u0114",
            "\1\u0115",
            "",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u0117",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u0119",
            "\1\u011a",
            "\1\u011b",
            "\1\u011c",
            "\1\u011d",
            "\1\u011e",
            "\1\u011f",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u0121",
            "\1\u0122",
            "\1\u0123",
            "\1\u0124",
            "\1\u0125",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "",
            "\1\u0128",
            "",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u012a",
            "\1\u012b",
            "",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "",
            "\1\u012e",
            "",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u0133",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u0135",
            "\1\u0136",
            "\1\u0137",
            "\1\u0138",
            "",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u013a",
            "\1\u013b",
            "\1\u013c",
            "\1\u013d",
            "",
            "",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "",
            "\1\u0142\15\uffff\1\u0141\1\uffff\1\u013f\1\u0140",
            "\1\u0143",
            "",
            "",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "",
            "",
            "",
            "",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "",
            "\1\u0146",
            "\1\u0147",
            "\1\u0148",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u014b",
            "\1\u014c",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "",
            "\1\u014e",
            "\1\u014f",
            "\1\u0150",
            "\1\u0151",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "",
            "",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "",
            "",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "",
            "\1\u0158",
            "\1\u0159",
            "\1\u015a",
            "\1\u015b",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\u015c",
            "\1\u015d",
            "\1\u015e",
            "\1\u015f",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\22\43\1\u0160\7\43",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u0165",
            "",
            "",
            "",
            "",
            "\1\u0166",
            "\1\u0167",
            "\1\u0168",
            "\1\43\10\uffff\2\43\1\uffff\13\43\6\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            ""
    };

    static final short[] DFA13_eot = DFA.unpackEncodedString(DFA13_eotS);
    static final short[] DFA13_eof = DFA.unpackEncodedString(DFA13_eofS);
    static final char[] DFA13_min = DFA.unpackEncodedStringToUnsignedChars(DFA13_minS);
    static final char[] DFA13_max = DFA.unpackEncodedStringToUnsignedChars(DFA13_maxS);
    static final short[] DFA13_accept = DFA.unpackEncodedString(DFA13_acceptS);
    static final short[] DFA13_special = DFA.unpackEncodedString(DFA13_specialS);
    static final short[][] DFA13_transition;

    static {
        int numStates = DFA13_transitionS.length;
        DFA13_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA13_transition[i] = DFA.unpackEncodedString(DFA13_transitionS[i]);
        }
    }

    class DFA13 extends DFA {

        public DFA13(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 13;
            this.eot = DFA13_eot;
            this.eof = DFA13_eof;
            this.min = DFA13_min;
            this.max = DFA13_max;
            this.accept = DFA13_accept;
            this.special = DFA13_special;
            this.transition = DFA13_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( ALL | AS | AVG | BOTH | CAST | CHARACTER | COUNT | COLLATE | CURRENT_USER | CURRENT_ROLE | CURRENT_DATE | CURRENT_TIME | CURRENT_TIMESTAMP | DEFAULT | DELETE | DISTINCT | DB_KEY | EXTRACT | EXECUTE | FOR | FROM | GEN_ID | INSERT | INTO | LEADING | MATCHING | MINIMUM | MAXIMUM | NULL | NEXT | OR | PROCEDURE | RETURNING | SEGMENT | SELECT | SET | SUBSTRING | SUB_TYPE | SUM | TRIM | TRAILING | UPDATE | VALUE | VALUES | KW_BLOB | KW_BIGINT | KW_CHAR | KW_DATE | KW_DECIMAL | KW_DOUBLE | KW_PRECISION | KW_FLOAT | KW_INTEGER | KW_INT | KW_NUMERIC | KW_SMALLINT | KW_TIME | KW_TIMESTAMP | KW_VARCHAR | KW_SET | KW_SIZE | T78 | T79 | T80 | T81 | T82 | T83 | T84 | T85 | T86 | T87 | T88 | LEFT_PAREN | RIGHT_PAREN | COMMA | GENERIC_ID | QUOTED_ID | LETTER | INTEGER | REAL | WS | SL_COMMENT | STRING | ESCqs | ESCqd );";
        }
    }
 

}