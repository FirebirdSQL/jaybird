// $ANTLR 3.4 D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g 2011-10-30 20:15:38

package org.firebirdsql.jdbc.parser;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked"})
public class JaybirdSqlLexer extends Lexer {
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

    	protected int _mismatchCount;
    	protected java.util.ArrayList _errorMessages = new java.util.ArrayList();
    	
    	public int getMismatchCount() {
    		return _mismatchCount;
    	}
    	
    	public java.util.Collection getErrorMessages() {
    		return _errorMessages;
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


    // delegates
    // delegators
    public Lexer[] getDelegates() {
        return new Lexer[] {};
    }

    public JaybirdSqlLexer() {} 
    public JaybirdSqlLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public JaybirdSqlLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);
    }
    public String getGrammarFileName() { return "D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g"; }

    // $ANTLR start "ALL"
    public final void mALL() throws RecognitionException {
        try {
            int _type = ALL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:34:5: ( 'all' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:34:7: 'all'
            {
            match("all"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "ALL"

    // $ANTLR start "AS"
    public final void mAS() throws RecognitionException {
        try {
            int _type = AS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:35:4: ( 'as' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:35:6: 'as'
            {
            match("as"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "AS"

    // $ANTLR start "AVG"
    public final void mAVG() throws RecognitionException {
        try {
            int _type = AVG;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:36:5: ( 'avg' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:36:7: 'avg'
            {
            match("avg"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "AVG"

    // $ANTLR start "BOTH"
    public final void mBOTH() throws RecognitionException {
        try {
            int _type = BOTH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:37:6: ( 'both' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:37:8: 'both'
            {
            match("both"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "BOTH"

    // $ANTLR start "CAST"
    public final void mCAST() throws RecognitionException {
        try {
            int _type = CAST;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:38:6: ( 'cast' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:38:8: 'cast'
            {
            match("cast"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "CAST"

    // $ANTLR start "CHARACTER"
    public final void mCHARACTER() throws RecognitionException {
        try {
            int _type = CHARACTER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:39:11: ( 'character' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:39:13: 'character'
            {
            match("character"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "CHARACTER"

    // $ANTLR start "COLLATE"
    public final void mCOLLATE() throws RecognitionException {
        try {
            int _type = COLLATE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:40:9: ( 'collate' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:40:11: 'collate'
            {
            match("collate"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "COLLATE"

    // $ANTLR start "COUNT"
    public final void mCOUNT() throws RecognitionException {
        try {
            int _type = COUNT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:41:7: ( 'count' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:41:9: 'count'
            {
            match("count"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "COUNT"

    // $ANTLR start "CURRENT_DATE"
    public final void mCURRENT_DATE() throws RecognitionException {
        try {
            int _type = CURRENT_DATE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:42:14: ( 'current_date' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:42:16: 'current_date'
            {
            match("current_date"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "CURRENT_DATE"

    // $ANTLR start "CURRENT_ROLE"
    public final void mCURRENT_ROLE() throws RecognitionException {
        try {
            int _type = CURRENT_ROLE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:43:14: ( 'current_role' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:43:16: 'current_role'
            {
            match("current_role"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "CURRENT_ROLE"

    // $ANTLR start "CURRENT_TIME"
    public final void mCURRENT_TIME() throws RecognitionException {
        try {
            int _type = CURRENT_TIME;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:44:14: ( 'current_time' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:44:16: 'current_time'
            {
            match("current_time"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "CURRENT_TIME"

    // $ANTLR start "CURRENT_TIMESTAMP"
    public final void mCURRENT_TIMESTAMP() throws RecognitionException {
        try {
            int _type = CURRENT_TIMESTAMP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:45:19: ( 'current_timestamp' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:45:21: 'current_timestamp'
            {
            match("current_timestamp"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "CURRENT_TIMESTAMP"

    // $ANTLR start "CURRENT_USER"
    public final void mCURRENT_USER() throws RecognitionException {
        try {
            int _type = CURRENT_USER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:46:14: ( 'current_user' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:46:16: 'current_user'
            {
            match("current_user"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "CURRENT_USER"

    // $ANTLR start "DB_KEY"
    public final void mDB_KEY() throws RecognitionException {
        try {
            int _type = DB_KEY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:47:8: ( 'db_key' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:47:10: 'db_key'
            {
            match("db_key"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "DB_KEY"

    // $ANTLR start "DEFAULT"
    public final void mDEFAULT() throws RecognitionException {
        try {
            int _type = DEFAULT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:48:9: ( 'default' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:48:11: 'default'
            {
            match("default"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "DEFAULT"

    // $ANTLR start "DELETE"
    public final void mDELETE() throws RecognitionException {
        try {
            int _type = DELETE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:49:8: ( 'delete' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:49:10: 'delete'
            {
            match("delete"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "DELETE"

    // $ANTLR start "DISTINCT"
    public final void mDISTINCT() throws RecognitionException {
        try {
            int _type = DISTINCT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:50:10: ( 'distinct' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:50:12: 'distinct'
            {
            match("distinct"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "DISTINCT"

    // $ANTLR start "EXECUTE"
    public final void mEXECUTE() throws RecognitionException {
        try {
            int _type = EXECUTE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:51:9: ( 'execute' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:51:11: 'execute'
            {
            match("execute"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "EXECUTE"

    // $ANTLR start "EXTRACT"
    public final void mEXTRACT() throws RecognitionException {
        try {
            int _type = EXTRACT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:52:9: ( 'extract' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:52:11: 'extract'
            {
            match("extract"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "EXTRACT"

    // $ANTLR start "FOR"
    public final void mFOR() throws RecognitionException {
        try {
            int _type = FOR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:53:5: ( 'for' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:53:7: 'for'
            {
            match("for"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "FOR"

    // $ANTLR start "FROM"
    public final void mFROM() throws RecognitionException {
        try {
            int _type = FROM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:54:6: ( 'from' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:54:8: 'from'
            {
            match("from"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "FROM"

    // $ANTLR start "GEN_ID"
    public final void mGEN_ID() throws RecognitionException {
        try {
            int _type = GEN_ID;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:55:8: ( 'gen_id' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:55:10: 'gen_id'
            {
            match("gen_id"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "GEN_ID"

    // $ANTLR start "INSERT"
    public final void mINSERT() throws RecognitionException {
        try {
            int _type = INSERT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:56:8: ( 'insert' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:56:10: 'insert'
            {
            match("insert"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "INSERT"

    // $ANTLR start "INTO"
    public final void mINTO() throws RecognitionException {
        try {
            int _type = INTO;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:57:6: ( 'into' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:57:8: 'into'
            {
            match("into"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "INTO"

    // $ANTLR start "KW_BIGINT"
    public final void mKW_BIGINT() throws RecognitionException {
        try {
            int _type = KW_BIGINT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:58:11: ( 'bigint' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:58:13: 'bigint'
            {
            match("bigint"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "KW_BIGINT"

    // $ANTLR start "KW_BLOB"
    public final void mKW_BLOB() throws RecognitionException {
        try {
            int _type = KW_BLOB;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:59:9: ( 'blob' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:59:11: 'blob'
            {
            match("blob"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "KW_BLOB"

    // $ANTLR start "KW_CHAR"
    public final void mKW_CHAR() throws RecognitionException {
        try {
            int _type = KW_CHAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:60:9: ( 'char' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:60:11: 'char'
            {
            match("char"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "KW_CHAR"

    // $ANTLR start "KW_DATE"
    public final void mKW_DATE() throws RecognitionException {
        try {
            int _type = KW_DATE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:61:9: ( 'date' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:61:11: 'date'
            {
            match("date"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "KW_DATE"

    // $ANTLR start "KW_DECIMAL"
    public final void mKW_DECIMAL() throws RecognitionException {
        try {
            int _type = KW_DECIMAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:62:12: ( 'decimal' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:62:14: 'decimal'
            {
            match("decimal"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "KW_DECIMAL"

    // $ANTLR start "KW_DOUBLE"
    public final void mKW_DOUBLE() throws RecognitionException {
        try {
            int _type = KW_DOUBLE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:63:11: ( 'double' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:63:13: 'double'
            {
            match("double"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "KW_DOUBLE"

    // $ANTLR start "KW_FLOAT"
    public final void mKW_FLOAT() throws RecognitionException {
        try {
            int _type = KW_FLOAT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:64:10: ( 'float' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:64:12: 'float'
            {
            match("float"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "KW_FLOAT"

    // $ANTLR start "KW_INT"
    public final void mKW_INT() throws RecognitionException {
        try {
            int _type = KW_INT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:65:8: ( 'int' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:65:10: 'int'
            {
            match("int"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "KW_INT"

    // $ANTLR start "KW_INTEGER"
    public final void mKW_INTEGER() throws RecognitionException {
        try {
            int _type = KW_INTEGER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:66:12: ( 'integer' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:66:14: 'integer'
            {
            match("integer"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "KW_INTEGER"

    // $ANTLR start "KW_NUMERIC"
    public final void mKW_NUMERIC() throws RecognitionException {
        try {
            int _type = KW_NUMERIC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:67:12: ( 'numeric' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:67:14: 'numeric'
            {
            match("numeric"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "KW_NUMERIC"

    // $ANTLR start "KW_PRECISION"
    public final void mKW_PRECISION() throws RecognitionException {
        try {
            int _type = KW_PRECISION;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:68:14: ( 'precision' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:68:16: 'precision'
            {
            match("precision"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "KW_PRECISION"

    // $ANTLR start "KW_SIZE"
    public final void mKW_SIZE() throws RecognitionException {
        try {
            int _type = KW_SIZE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:69:9: ( 'size' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:69:11: 'size'
            {
            match("size"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "KW_SIZE"

    // $ANTLR start "KW_SMALLINT"
    public final void mKW_SMALLINT() throws RecognitionException {
        try {
            int _type = KW_SMALLINT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:70:13: ( 'smallint' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:70:15: 'smallint'
            {
            match("smallint"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "KW_SMALLINT"

    // $ANTLR start "KW_TIME"
    public final void mKW_TIME() throws RecognitionException {
        try {
            int _type = KW_TIME;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:71:9: ( 'time' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:71:11: 'time'
            {
            match("time"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "KW_TIME"

    // $ANTLR start "KW_TIMESTAMP"
    public final void mKW_TIMESTAMP() throws RecognitionException {
        try {
            int _type = KW_TIMESTAMP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:72:14: ( 'timestamp' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:72:16: 'timestamp'
            {
            match("timestamp"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "KW_TIMESTAMP"

    // $ANTLR start "KW_VARCHAR"
    public final void mKW_VARCHAR() throws RecognitionException {
        try {
            int _type = KW_VARCHAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:73:12: ( 'varchar' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:73:14: 'varchar'
            {
            match("varchar"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "KW_VARCHAR"

    // $ANTLR start "LEADING"
    public final void mLEADING() throws RecognitionException {
        try {
            int _type = LEADING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:74:9: ( 'leading' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:74:11: 'leading'
            {
            match("leading"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "LEADING"

    // $ANTLR start "MATCHING"
    public final void mMATCHING() throws RecognitionException {
        try {
            int _type = MATCHING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:75:10: ( 'matching' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:75:12: 'matching'
            {
            match("matching"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "MATCHING"

    // $ANTLR start "MAXIMUM"
    public final void mMAXIMUM() throws RecognitionException {
        try {
            int _type = MAXIMUM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:76:9: ( 'max' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:76:11: 'max'
            {
            match("max"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "MAXIMUM"

    // $ANTLR start "MINIMUM"
    public final void mMINIMUM() throws RecognitionException {
        try {
            int _type = MINIMUM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:77:9: ( 'min' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:77:11: 'min'
            {
            match("min"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "MINIMUM"

    // $ANTLR start "NEXT"
    public final void mNEXT() throws RecognitionException {
        try {
            int _type = NEXT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:78:6: ( 'next' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:78:8: 'next'
            {
            match("next"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "NEXT"

    // $ANTLR start "NULL"
    public final void mNULL() throws RecognitionException {
        try {
            int _type = NULL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:79:6: ( 'null' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:79:8: 'null'
            {
            match("null"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "NULL"

    // $ANTLR start "OR"
    public final void mOR() throws RecognitionException {
        try {
            int _type = OR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:80:4: ( 'or' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:80:6: 'or'
            {
            match("or"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "OR"

    // $ANTLR start "PROCEDURE"
    public final void mPROCEDURE() throws RecognitionException {
        try {
            int _type = PROCEDURE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:81:11: ( 'procedure' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:81:13: 'procedure'
            {
            match("procedure"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "PROCEDURE"

    // $ANTLR start "RETURNING"
    public final void mRETURNING() throws RecognitionException {
        try {
            int _type = RETURNING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:82:11: ( 'returning' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:82:13: 'returning'
            {
            match("returning"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "RETURNING"

    // $ANTLR start "SEGMENT"
    public final void mSEGMENT() throws RecognitionException {
        try {
            int _type = SEGMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:83:9: ( 'segment' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:83:11: 'segment'
            {
            match("segment"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "SEGMENT"

    // $ANTLR start "SELECT"
    public final void mSELECT() throws RecognitionException {
        try {
            int _type = SELECT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:84:8: ( 'select' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:84:10: 'select'
            {
            match("select"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "SELECT"

    // $ANTLR start "SET"
    public final void mSET() throws RecognitionException {
        try {
            int _type = SET;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:85:5: ( 'set' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:85:7: 'set'
            {
            match("set"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "SET"

    // $ANTLR start "SUBSTRING"
    public final void mSUBSTRING() throws RecognitionException {
        try {
            int _type = SUBSTRING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:86:11: ( 'substring' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:86:13: 'substring'
            {
            match("substring"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "SUBSTRING"

    // $ANTLR start "SUB_TYPE"
    public final void mSUB_TYPE() throws RecognitionException {
        try {
            int _type = SUB_TYPE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:87:10: ( 'sub_type' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:87:12: 'sub_type'
            {
            match("sub_type"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "SUB_TYPE"

    // $ANTLR start "SUM"
    public final void mSUM() throws RecognitionException {
        try {
            int _type = SUM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:88:5: ( 'sum' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:88:7: 'sum'
            {
            match("sum"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "SUM"

    // $ANTLR start "TRAILING"
    public final void mTRAILING() throws RecognitionException {
        try {
            int _type = TRAILING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:89:10: ( 'trailing' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:89:12: 'trailing'
            {
            match("trailing"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "TRAILING"

    // $ANTLR start "TRIM"
    public final void mTRIM() throws RecognitionException {
        try {
            int _type = TRIM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:90:6: ( 'trim' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:90:8: 'trim'
            {
            match("trim"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "TRIM"

    // $ANTLR start "UPDATE"
    public final void mUPDATE() throws RecognitionException {
        try {
            int _type = UPDATE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:91:8: ( 'update' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:91:10: 'update'
            {
            match("update"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "UPDATE"

    // $ANTLR start "VALUE"
    public final void mVALUE() throws RecognitionException {
        try {
            int _type = VALUE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:92:7: ( 'value' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:92:9: 'value'
            {
            match("value"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "VALUE"

    // $ANTLR start "VALUES"
    public final void mVALUES() throws RecognitionException {
        try {
            int _type = VALUES;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:93:8: ( 'values' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:93:10: 'values'
            {
            match("values"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "VALUES"

    // $ANTLR start "T__74"
    public final void mT__74() throws RecognitionException {
        try {
            int _type = T__74;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:94:7: ( '*' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:94:9: '*'
            {
            match('*'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__74"

    // $ANTLR start "T__75"
    public final void mT__75() throws RecognitionException {
        try {
            int _type = T__75;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:95:7: ( '+' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:95:9: '+'
            {
            match('+'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__75"

    // $ANTLR start "T__76"
    public final void mT__76() throws RecognitionException {
        try {
            int _type = T__76;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:96:7: ( '-' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:96:9: '-'
            {
            match('-'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__76"

    // $ANTLR start "T__77"
    public final void mT__77() throws RecognitionException {
        try {
            int _type = T__77;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:97:7: ( '.' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:97:9: '.'
            {
            match('.'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__77"

    // $ANTLR start "T__78"
    public final void mT__78() throws RecognitionException {
        try {
            int _type = T__78;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:98:7: ( '/' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:98:9: '/'
            {
            match('/'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__78"

    // $ANTLR start "T__79"
    public final void mT__79() throws RecognitionException {
        try {
            int _type = T__79;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:99:7: ( ':' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:99:9: ':'
            {
            match(':'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__79"

    // $ANTLR start "T__80"
    public final void mT__80() throws RecognitionException {
        try {
            int _type = T__80;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:100:7: ( '=' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:100:9: '='
            {
            match('='); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__80"

    // $ANTLR start "T__81"
    public final void mT__81() throws RecognitionException {
        try {
            int _type = T__81;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:101:7: ( '?' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:101:9: '?'
            {
            match('?'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__81"

    // $ANTLR start "T__82"
    public final void mT__82() throws RecognitionException {
        try {
            int _type = T__82;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:102:7: ( '[' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:102:9: '['
            {
            match('['); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__82"

    // $ANTLR start "T__83"
    public final void mT__83() throws RecognitionException {
        try {
            int _type = T__83;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:103:7: ( ']' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:103:9: ']'
            {
            match(']'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__83"

    // $ANTLR start "T__84"
    public final void mT__84() throws RecognitionException {
        try {
            int _type = T__84;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:104:7: ( '||' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:104:9: '||'
            {
            match("||"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__84"

    // $ANTLR start "LEFT_PAREN"
    public final void mLEFT_PAREN() throws RecognitionException {
        try {
            int _type = LEFT_PAREN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:559:3: ( '(' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:559:5: '('
            {
            match('('); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "LEFT_PAREN"

    // $ANTLR start "RIGHT_PAREN"
    public final void mRIGHT_PAREN() throws RecognitionException {
        try {
            int _type = RIGHT_PAREN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:563:3: ( ')' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:563:5: ')'
            {
            match(')'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "RIGHT_PAREN"

    // $ANTLR start "COMMA"
    public final void mCOMMA() throws RecognitionException {
        try {
            int _type = COMMA;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:566:8: ( ',' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:566:10: ','
            {
            match(','); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "COMMA"

    // $ANTLR start "GENERIC_ID"
    public final void mGENERIC_ID() throws RecognitionException {
        try {
            int _type = GENERIC_ID;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:570:6: ( ( 'a' .. 'z' | 'A' .. 'Z' | '_' | ':' | '$' ) ( options {greedy=true; } : 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '.' | '-' | '_' | ':' | '$' )* )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:570:8: ( 'a' .. 'z' | 'A' .. 'Z' | '_' | ':' | '$' ) ( options {greedy=true; } : 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '.' | '-' | '_' | ':' | '$' )*
            {
            if ( input.LA(1)=='$'||input.LA(1)==':'||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
                input.consume();
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;
            }


            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:571:10: ( options {greedy=true; } : 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '.' | '-' | '_' | ':' | '$' )*
            loop1:
            do {
                int alt1=9;
                switch ( input.LA(1) ) {
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
                    {
                    alt1=2;
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
                    alt1=3;
                    }
                    break;
                case '.':
                    {
                    alt1=4;
                    }
                    break;
                case '-':
                    {
                    alt1=5;
                    }
                    break;
                case '_':
                    {
                    alt1=6;
                    }
                    break;
                case ':':
                    {
                    alt1=7;
                    }
                    break;
                case '$':
                    {
                    alt1=8;
                    }
                    break;

                }

                switch (alt1) {
            	case 1 :
            	    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:571:37: 'a' .. 'z'
            	    {
            	    matchRange('a','z'); 

            	    }
            	    break;
            	case 2 :
            	    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:571:46: 'A' .. 'Z'
            	    {
            	    matchRange('A','Z'); 

            	    }
            	    break;
            	case 3 :
            	    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:571:57: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

            	    }
            	    break;
            	case 4 :
            	    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:571:68: '.'
            	    {
            	    match('.'); 

            	    }
            	    break;
            	case 5 :
            	    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:571:74: '-'
            	    {
            	    match('-'); 

            	    }
            	    break;
            	case 6 :
            	    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:571:80: '_'
            	    {
            	    match('_'); 

            	    }
            	    break;
            	case 7 :
            	    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:571:86: ':'
            	    {
            	    match(':'); 

            	    }
            	    break;
            	case 8 :
            	    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:571:91: '$'
            	    {
            	    match('$'); 

            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "GENERIC_ID"

    // $ANTLR start "QUOTED_ID"
    public final void mQUOTED_ID() throws RecognitionException {
        try {
            int _type = QUOTED_ID;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:575:3: ( '\\\"' ( '$' | '_' | '\\u00A0' .. '\\uFFFF' | '\\\"\\\"' | '0' .. '9' )+ '\\\"' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:575:5: '\\\"' ( '$' | '_' | '\\u00A0' .. '\\uFFFF' | '\\\"\\\"' | '0' .. '9' )+ '\\\"'
            {
            match('\"'); 

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:575:10: ( '$' | '_' | '\\u00A0' .. '\\uFFFF' | '\\\"\\\"' | '0' .. '9' )+
            int cnt2=0;
            loop2:
            do {
                int alt2=6;
                int LA2_0 = input.LA(1);

                if ( (LA2_0=='\"') ) {
                    int LA2_1 = input.LA(2);

                    if ( (LA2_1=='\"') ) {
                        alt2=4;
                    }


                }
                else if ( (LA2_0=='$') ) {
                    alt2=1;
                }
                else if ( (LA2_0=='_') ) {
                    alt2=2;
                }
                else if ( ((LA2_0 >= '\u00A0' && LA2_0 <= '\uFFFF')) ) {
                    alt2=3;
                }
                else if ( ((LA2_0 >= '0' && LA2_0 <= '9')) ) {
                    alt2=5;
                }


                switch (alt2) {
            	case 1 :
            	    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:575:11: '$'
            	    {
            	    match('$'); 

            	    }
            	    break;
            	case 2 :
            	    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:575:15: '_'
            	    {
            	    match('_'); 

            	    }
            	    break;
            	case 3 :
            	    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:575:19: '\\u00A0' .. '\\uFFFF'
            	    {
            	    matchRange('\u00A0','\uFFFF'); 

            	    }
            	    break;
            	case 4 :
            	    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:575:41: '\\\"\\\"'
            	    {
            	    match("\"\""); 



            	    }
            	    break;
            	case 5 :
            	    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:575:50: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

            	    }
            	    break;

            	default :
            	    if ( cnt2 >= 1 ) break loop2;
                        EarlyExitException eee =
                            new EarlyExitException(2, input);
                        throw eee;
                }
                cnt2++;
            } while (true);


            match('\"'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "QUOTED_ID"

    // $ANTLR start "INTEGER"
    public final void mINTEGER() throws RecognitionException {
        try {
            int _type = INTEGER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:582:9: ( ( '-' )? ( '0' .. '9' )+ )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:582:11: ( '-' )? ( '0' .. '9' )+
            {
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:582:11: ( '-' )?
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0=='-') ) {
                alt3=1;
            }
            switch (alt3) {
                case 1 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:582:12: '-'
                    {
                    match('-'); 

                    }
                    break;

            }


            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:582:17: ( '0' .. '9' )+
            int cnt4=0;
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( ((LA4_0 >= '0' && LA4_0 <= '9')) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:
            	    {
            	    if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
            	        input.consume();
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    if ( cnt4 >= 1 ) break loop4;
                        EarlyExitException eee =
                            new EarlyExitException(4, input);
                        throw eee;
                }
                cnt4++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "INTEGER"

    // $ANTLR start "REAL"
    public final void mREAL() throws RecognitionException {
        try {
            int _type = REAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:585:7: ( ( '-' )? ( '0' .. '9' )* '.' ( '0' .. '9' )+ )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:585:9: ( '-' )? ( '0' .. '9' )* '.' ( '0' .. '9' )+
            {
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:585:9: ( '-' )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0=='-') ) {
                alt5=1;
            }
            switch (alt5) {
                case 1 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:585:10: '-'
                    {
                    match('-'); 

                    }
                    break;

            }


            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:585:15: ( '0' .. '9' )*
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( ((LA6_0 >= '0' && LA6_0 <= '9')) ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:
            	    {
            	    if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
            	        input.consume();
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    break loop6;
                }
            } while (true);


            match('.'); 

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:585:29: ( '0' .. '9' )+
            int cnt7=0;
            loop7:
            do {
                int alt7=2;
                int LA7_0 = input.LA(1);

                if ( ((LA7_0 >= '0' && LA7_0 <= '9')) ) {
                    alt7=1;
                }


                switch (alt7) {
            	case 1 :
            	    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:
            	    {
            	    if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
            	        input.consume();
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    if ( cnt7 >= 1 ) break loop7;
                        EarlyExitException eee =
                            new EarlyExitException(7, input);
                        throw eee;
                }
                cnt7++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "REAL"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:588:5: ( ( ' ' | '\\t' | '\\n' | '\\r' )+ )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:588:7: ( ' ' | '\\t' | '\\n' | '\\r' )+
            {
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:588:7: ( ' ' | '\\t' | '\\n' | '\\r' )+
            int cnt8=0;
            loop8:
            do {
                int alt8=2;
                int LA8_0 = input.LA(1);

                if ( ((LA8_0 >= '\t' && LA8_0 <= '\n')||LA8_0=='\r'||LA8_0==' ') ) {
                    alt8=1;
                }


                switch (alt8) {
            	case 1 :
            	    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:
            	    {
            	    if ( (input.LA(1) >= '\t' && input.LA(1) <= '\n')||input.LA(1)=='\r'||input.LA(1)==' ' ) {
            	        input.consume();
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    if ( cnt8 >= 1 ) break loop8;
                        EarlyExitException eee =
                            new EarlyExitException(8, input);
                        throw eee;
                }
                cnt8++;
            } while (true);


            _channel = HIDDEN;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "WS"

    // $ANTLR start "SL_COMMENT"
    public final void mSL_COMMENT() throws RecognitionException {
        try {
            int _type = SL_COMMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:592:9: ( '--' (~ ( '\\n' | '\\r' ) )* ( '\\n' | '\\r' ( '\\n' )? ) )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:592:11: '--' (~ ( '\\n' | '\\r' ) )* ( '\\n' | '\\r' ( '\\n' )? )
            {
            match("--"); 



            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:592:16: (~ ( '\\n' | '\\r' ) )*
            loop9:
            do {
                int alt9=2;
                int LA9_0 = input.LA(1);

                if ( ((LA9_0 >= '\u0000' && LA9_0 <= '\t')||(LA9_0 >= '\u000B' && LA9_0 <= '\f')||(LA9_0 >= '\u000E' && LA9_0 <= '\uFFFF')) ) {
                    alt9=1;
                }


                switch (alt9) {
            	case 1 :
            	    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:
            	    {
            	    if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '\t')||(input.LA(1) >= '\u000B' && input.LA(1) <= '\f')||(input.LA(1) >= '\u000E' && input.LA(1) <= '\uFFFF') ) {
            	        input.consume();
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    break loop9;
                }
            } while (true);


            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:592:32: ( '\\n' | '\\r' ( '\\n' )? )
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0=='\n') ) {
                alt11=1;
            }
            else if ( (LA11_0=='\r') ) {
                alt11=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 11, 0, input);

                throw nvae;

            }
            switch (alt11) {
                case 1 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:592:33: '\\n'
                    {
                    match('\n'); 

                    }
                    break;
                case 2 :
                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:592:38: '\\r' ( '\\n' )?
                    {
                    match('\r'); 

                    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:592:42: ( '\\n' )?
                    int alt10=2;
                    int LA10_0 = input.LA(1);

                    if ( (LA10_0=='\n') ) {
                        alt10=1;
                    }
                    switch (alt10) {
                        case 1 :
                            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:592:43: '\\n'
                            {
                            match('\n'); 

                            }
                            break;

                    }


                    }
                    break;

            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "SL_COMMENT"

    // $ANTLR start "STRING"
    public final void mSTRING() throws RecognitionException {
        try {
            int _type = STRING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:596:3: ( ( '\\'' (~ '\\'' )* '\\'' ) )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:596:5: ( '\\'' (~ '\\'' )* '\\'' )
            {
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:596:5: ( '\\'' (~ '\\'' )* '\\'' )
            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:596:7: '\\'' (~ '\\'' )* '\\''
            {
            match('\''); 

            // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:596:12: (~ '\\'' )*
            loop12:
            do {
                int alt12=2;
                int LA12_0 = input.LA(1);

                if ( ((LA12_0 >= '\u0000' && LA12_0 <= '&')||(LA12_0 >= '(' && LA12_0 <= '\uFFFF')) ) {
                    alt12=1;
                }


                switch (alt12) {
            	case 1 :
            	    // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:
            	    {
            	    if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '&')||(input.LA(1) >= '(' && input.LA(1) <= '\uFFFF') ) {
            	        input.consume();
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    break loop12;
                }
            } while (true);


            match('\''); 

            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "STRING"

    public void mTokens() throws RecognitionException {
        // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:8: ( ALL | AS | AVG | BOTH | CAST | CHARACTER | COLLATE | COUNT | CURRENT_DATE | CURRENT_ROLE | CURRENT_TIME | CURRENT_TIMESTAMP | CURRENT_USER | DB_KEY | DEFAULT | DELETE | DISTINCT | EXECUTE | EXTRACT | FOR | FROM | GEN_ID | INSERT | INTO | KW_BIGINT | KW_BLOB | KW_CHAR | KW_DATE | KW_DECIMAL | KW_DOUBLE | KW_FLOAT | KW_INT | KW_INTEGER | KW_NUMERIC | KW_PRECISION | KW_SIZE | KW_SMALLINT | KW_TIME | KW_TIMESTAMP | KW_VARCHAR | LEADING | MATCHING | MAXIMUM | MINIMUM | NEXT | NULL | OR | PROCEDURE | RETURNING | SEGMENT | SELECT | SET | SUBSTRING | SUB_TYPE | SUM | TRAILING | TRIM | UPDATE | VALUE | VALUES | T__74 | T__75 | T__76 | T__77 | T__78 | T__79 | T__80 | T__81 | T__82 | T__83 | T__84 | LEFT_PAREN | RIGHT_PAREN | COMMA | GENERIC_ID | QUOTED_ID | INTEGER | REAL | WS | SL_COMMENT | STRING )
        int alt13=81;
        alt13 = dfa13.predict(input);
        switch (alt13) {
            case 1 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:10: ALL
                {
                mALL(); 


                }
                break;
            case 2 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:14: AS
                {
                mAS(); 


                }
                break;
            case 3 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:17: AVG
                {
                mAVG(); 


                }
                break;
            case 4 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:21: BOTH
                {
                mBOTH(); 


                }
                break;
            case 5 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:26: CAST
                {
                mCAST(); 


                }
                break;
            case 6 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:31: CHARACTER
                {
                mCHARACTER(); 


                }
                break;
            case 7 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:41: COLLATE
                {
                mCOLLATE(); 


                }
                break;
            case 8 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:49: COUNT
                {
                mCOUNT(); 


                }
                break;
            case 9 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:55: CURRENT_DATE
                {
                mCURRENT_DATE(); 


                }
                break;
            case 10 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:68: CURRENT_ROLE
                {
                mCURRENT_ROLE(); 


                }
                break;
            case 11 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:81: CURRENT_TIME
                {
                mCURRENT_TIME(); 


                }
                break;
            case 12 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:94: CURRENT_TIMESTAMP
                {
                mCURRENT_TIMESTAMP(); 


                }
                break;
            case 13 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:112: CURRENT_USER
                {
                mCURRENT_USER(); 


                }
                break;
            case 14 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:125: DB_KEY
                {
                mDB_KEY(); 


                }
                break;
            case 15 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:132: DEFAULT
                {
                mDEFAULT(); 


                }
                break;
            case 16 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:140: DELETE
                {
                mDELETE(); 


                }
                break;
            case 17 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:147: DISTINCT
                {
                mDISTINCT(); 


                }
                break;
            case 18 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:156: EXECUTE
                {
                mEXECUTE(); 


                }
                break;
            case 19 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:164: EXTRACT
                {
                mEXTRACT(); 


                }
                break;
            case 20 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:172: FOR
                {
                mFOR(); 


                }
                break;
            case 21 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:176: FROM
                {
                mFROM(); 


                }
                break;
            case 22 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:181: GEN_ID
                {
                mGEN_ID(); 


                }
                break;
            case 23 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:188: INSERT
                {
                mINSERT(); 


                }
                break;
            case 24 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:195: INTO
                {
                mINTO(); 


                }
                break;
            case 25 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:200: KW_BIGINT
                {
                mKW_BIGINT(); 


                }
                break;
            case 26 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:210: KW_BLOB
                {
                mKW_BLOB(); 


                }
                break;
            case 27 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:218: KW_CHAR
                {
                mKW_CHAR(); 


                }
                break;
            case 28 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:226: KW_DATE
                {
                mKW_DATE(); 


                }
                break;
            case 29 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:234: KW_DECIMAL
                {
                mKW_DECIMAL(); 


                }
                break;
            case 30 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:245: KW_DOUBLE
                {
                mKW_DOUBLE(); 


                }
                break;
            case 31 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:255: KW_FLOAT
                {
                mKW_FLOAT(); 


                }
                break;
            case 32 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:264: KW_INT
                {
                mKW_INT(); 


                }
                break;
            case 33 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:271: KW_INTEGER
                {
                mKW_INTEGER(); 


                }
                break;
            case 34 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:282: KW_NUMERIC
                {
                mKW_NUMERIC(); 


                }
                break;
            case 35 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:293: KW_PRECISION
                {
                mKW_PRECISION(); 


                }
                break;
            case 36 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:306: KW_SIZE
                {
                mKW_SIZE(); 


                }
                break;
            case 37 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:314: KW_SMALLINT
                {
                mKW_SMALLINT(); 


                }
                break;
            case 38 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:326: KW_TIME
                {
                mKW_TIME(); 


                }
                break;
            case 39 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:334: KW_TIMESTAMP
                {
                mKW_TIMESTAMP(); 


                }
                break;
            case 40 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:347: KW_VARCHAR
                {
                mKW_VARCHAR(); 


                }
                break;
            case 41 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:358: LEADING
                {
                mLEADING(); 


                }
                break;
            case 42 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:366: MATCHING
                {
                mMATCHING(); 


                }
                break;
            case 43 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:375: MAXIMUM
                {
                mMAXIMUM(); 


                }
                break;
            case 44 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:383: MINIMUM
                {
                mMINIMUM(); 


                }
                break;
            case 45 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:391: NEXT
                {
                mNEXT(); 


                }
                break;
            case 46 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:396: NULL
                {
                mNULL(); 


                }
                break;
            case 47 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:401: OR
                {
                mOR(); 


                }
                break;
            case 48 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:404: PROCEDURE
                {
                mPROCEDURE(); 


                }
                break;
            case 49 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:414: RETURNING
                {
                mRETURNING(); 


                }
                break;
            case 50 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:424: SEGMENT
                {
                mSEGMENT(); 


                }
                break;
            case 51 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:432: SELECT
                {
                mSELECT(); 


                }
                break;
            case 52 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:439: SET
                {
                mSET(); 


                }
                break;
            case 53 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:443: SUBSTRING
                {
                mSUBSTRING(); 


                }
                break;
            case 54 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:453: SUB_TYPE
                {
                mSUB_TYPE(); 


                }
                break;
            case 55 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:462: SUM
                {
                mSUM(); 


                }
                break;
            case 56 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:466: TRAILING
                {
                mTRAILING(); 


                }
                break;
            case 57 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:475: TRIM
                {
                mTRIM(); 


                }
                break;
            case 58 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:480: UPDATE
                {
                mUPDATE(); 


                }
                break;
            case 59 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:487: VALUE
                {
                mVALUE(); 


                }
                break;
            case 60 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:493: VALUES
                {
                mVALUES(); 


                }
                break;
            case 61 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:500: T__74
                {
                mT__74(); 


                }
                break;
            case 62 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:506: T__75
                {
                mT__75(); 


                }
                break;
            case 63 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:512: T__76
                {
                mT__76(); 


                }
                break;
            case 64 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:518: T__77
                {
                mT__77(); 


                }
                break;
            case 65 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:524: T__78
                {
                mT__78(); 


                }
                break;
            case 66 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:530: T__79
                {
                mT__79(); 


                }
                break;
            case 67 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:536: T__80
                {
                mT__80(); 


                }
                break;
            case 68 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:542: T__81
                {
                mT__81(); 


                }
                break;
            case 69 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:548: T__82
                {
                mT__82(); 


                }
                break;
            case 70 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:554: T__83
                {
                mT__83(); 


                }
                break;
            case 71 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:560: T__84
                {
                mT__84(); 


                }
                break;
            case 72 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:566: LEFT_PAREN
                {
                mLEFT_PAREN(); 


                }
                break;
            case 73 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:577: RIGHT_PAREN
                {
                mRIGHT_PAREN(); 


                }
                break;
            case 74 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:589: COMMA
                {
                mCOMMA(); 


                }
                break;
            case 75 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:595: GENERIC_ID
                {
                mGENERIC_ID(); 


                }
                break;
            case 76 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:606: QUOTED_ID
                {
                mQUOTED_ID(); 


                }
                break;
            case 77 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:616: INTEGER
                {
                mINTEGER(); 


                }
                break;
            case 78 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:624: REAL
                {
                mREAL(); 


                }
                break;
            case 79 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:629: WS
                {
                mWS(); 


                }
                break;
            case 80 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:632: SL_COMMENT
                {
                mSL_COMMENT(); 


                }
                break;
            case 81 :
                // D:\\Users\\rrokytskyy\\workspace\\client-java\\src\\main\\org\\firebirdsql\\jdbc\\parser\\JaybirdSql.g:1:643: STRING
                {
                mSTRING(); 


                }
                break;

        }

    }


    protected DFA13 dfa13 = new DFA13(this);
    static final String DFA13_eotS =
        "\1\uffff\22\41\2\uffff\1\114\1\116\1\uffff\1\117\12\uffff\1\120"+
        "\2\uffff\1\41\1\122\40\41\1\u0080\2\41\6\uffff\1\u0083\1\uffff\1"+
        "\u0084\21\41\1\u0096\4\41\1\u009d\11\41\1\u00a7\1\41\1\u00aa\7\41"+
        "\1\u00b2\1\u00b3\1\uffff\2\41\2\uffff\1\u00b6\1\41\1\u00b8\1\u00b9"+
        "\1\u00bb\10\41\1\u00c4\3\41\1\uffff\1\u00c8\3\41\1\u00cc\1\41\1"+
        "\uffff\1\41\1\u00cf\1\u00d0\2\41\1\u00d3\3\41\1\uffff\2\41\1\uffff"+
        "\1\u00da\1\41\1\u00dc\4\41\2\uffff\2\41\1\uffff\1\41\2\uffff\1\41"+
        "\1\uffff\1\41\1\u00e6\6\41\1\uffff\3\41\1\uffff\1\u00f0\2\41\1\uffff"+
        "\2\41\2\uffff\2\41\1\uffff\6\41\1\uffff\1\41\1\uffff\1\41\1\u0100"+
        "\4\41\1\u0105\2\41\1\uffff\1\41\1\u0109\1\41\1\u010b\2\41\1\u010e"+
        "\2\41\1\uffff\1\u0111\1\u0112\6\41\1\u0119\5\41\1\u011f\1\uffff"+
        "\3\41\1\u0123\1\uffff\1\41\1\u0125\1\41\1\uffff\1\u0127\1\uffff"+
        "\1\u0128\1\41\1\uffff\1\u012a\1\u012b\2\uffff\1\u012c\1\u012d\3"+
        "\41\1\u0131\1\uffff\4\41\1\u0136\1\uffff\1\u0137\2\41\1\uffff\1"+
        "\41\1\uffff\1\41\2\uffff\1\u013f\4\uffff\2\41\1\u0142\1\uffff\1"+
        "\41\1\u0144\1\41\1\u0146\2\uffff\1\u0147\1\41\1\u0149\4\41\1\uffff"+
        "\1\u014e\1\u014f\1\uffff\1\u0150\1\uffff\1\u0151\2\uffff\1\u0152"+
        "\1\uffff\4\41\5\uffff\4\41\1\u015b\1\u015c\1\u015e\1\u015f\2\uffff"+
        "\1\41\2\uffff\3\41\1\u0164\1\uffff";
    static final String DFA13_eofS =
        "\u0165\uffff";
    static final String DFA13_minS =
        "\1\11\1\154\1\151\2\141\1\170\1\154\1\145\1\156\1\145\1\162\1\145"+
        "\1\151\1\141\1\145\1\141\1\162\1\145\1\160\2\uffff\1\55\1\60\1\uffff"+
        "\1\44\12\uffff\1\56\2\uffff\1\154\1\44\1\147\1\164\1\147\1\157\1"+
        "\163\1\141\1\154\1\162\1\137\1\143\1\163\1\164\1\165\1\145\1\162"+
        "\2\157\1\156\1\163\1\154\1\170\1\145\1\172\1\141\1\147\1\142\1\155"+
        "\1\141\1\154\1\141\1\164\1\156\1\44\1\164\1\144\6\uffff\1\44\1\uffff"+
        "\1\44\1\150\1\151\1\142\1\164\1\162\1\154\1\156\1\162\1\153\1\141"+
        "\1\145\1\151\1\164\1\145\1\142\1\143\1\162\1\44\1\155\1\141\1\137"+
        "\1\145\1\44\1\145\1\154\1\164\2\143\1\145\1\154\1\155\1\145\1\44"+
        "\1\137\1\44\1\145\1\151\1\155\1\143\1\165\1\144\1\143\2\44\1\uffff"+
        "\1\165\1\141\2\uffff\1\44\1\156\3\44\1\141\1\164\2\145\1\165\1\164"+
        "\1\155\1\151\1\44\1\154\1\165\1\141\1\uffff\1\44\1\164\1\151\1\162"+
        "\1\44\1\147\1\uffff\1\162\2\44\1\151\1\145\1\44\1\154\1\145\1\143"+
        "\1\uffff\2\164\1\uffff\1\44\1\154\1\44\1\150\1\145\1\151\1\150\2"+
        "\uffff\1\162\1\164\1\uffff\1\164\2\uffff\1\143\1\uffff\1\164\1\44"+
        "\1\156\1\171\1\154\1\145\1\141\1\156\1\uffff\1\145\1\164\1\143\1"+
        "\uffff\1\44\1\144\1\164\1\uffff\1\145\1\151\2\uffff\1\163\1\144"+
        "\1\uffff\1\151\1\156\1\164\1\162\1\171\1\164\1\uffff\1\151\1\uffff"+
        "\1\141\1\44\1\156\1\151\1\156\1\145\1\44\1\164\1\145\1\uffff\1\164"+
        "\1\44\1\164\1\44\1\154\1\143\1\44\1\145\1\164\1\uffff\2\44\1\162"+
        "\1\143\1\151\1\165\1\156\1\164\1\44\1\151\1\160\1\141\1\156\1\162"+
        "\1\44\1\uffff\1\147\1\156\1\151\1\44\1\uffff\1\145\1\44\1\137\1"+
        "\uffff\1\44\1\uffff\1\44\1\164\1\uffff\2\44\2\uffff\2\44\1\157\1"+
        "\162\1\164\1\44\1\uffff\1\156\1\145\1\155\1\147\1\44\1\uffff\1\44"+
        "\1\147\1\156\1\uffff\1\162\1\uffff\1\144\2\uffff\1\44\4\uffff\1"+
        "\156\1\145\1\44\1\uffff\1\147\1\44\1\160\1\44\2\uffff\1\44\1\147"+
        "\1\44\1\141\1\157\1\151\1\163\1\uffff\2\44\1\uffff\1\44\1\uffff"+
        "\1\44\2\uffff\1\44\1\uffff\1\164\1\154\1\155\1\145\5\uffff\3\145"+
        "\1\162\4\44\2\uffff\1\164\2\uffff\1\141\1\155\1\160\1\44\1\uffff";
    static final String DFA13_maxS =
        "\1\174\1\166\1\157\1\165\1\157\1\170\1\162\1\145\1\156\1\165\1\162"+
        "\1\165\1\162\1\141\1\145\1\151\1\162\1\145\1\160\2\uffff\2\71\1"+
        "\uffff\1\172\12\uffff\1\71\2\uffff\1\154\1\172\1\147\1\164\1\147"+
        "\1\157\1\163\1\141\1\165\1\162\1\137\1\154\1\163\1\164\1\165\1\164"+
        "\1\162\2\157\1\156\1\164\1\155\1\170\1\157\1\172\1\141\1\164\2\155"+
        "\1\151\1\162\1\141\1\170\1\156\1\172\1\164\1\144\6\uffff\1\172\1"+
        "\uffff\1\172\1\150\1\151\1\142\1\164\1\162\1\154\1\156\1\162\1\153"+
        "\1\141\1\145\1\151\1\164\1\145\1\142\1\143\1\162\1\172\1\155\1\141"+
        "\1\137\1\145\1\172\1\145\1\154\1\164\2\143\1\145\1\154\1\155\1\145"+
        "\1\172\1\163\1\172\1\145\1\151\1\155\1\143\1\165\1\144\1\143\2\172"+
        "\1\uffff\1\165\1\141\2\uffff\1\172\1\156\3\172\1\141\1\164\2\145"+
        "\1\165\1\164\1\155\1\151\1\172\1\154\1\165\1\141\1\uffff\1\172\1"+
        "\164\1\151\1\162\1\172\1\147\1\uffff\1\162\2\172\1\151\1\145\1\172"+
        "\1\154\1\145\1\143\1\uffff\2\164\1\uffff\1\172\1\154\1\172\1\150"+
        "\1\145\1\151\1\150\2\uffff\1\162\1\164\1\uffff\1\164\2\uffff\1\143"+
        "\1\uffff\1\164\1\172\1\156\1\171\1\154\1\145\1\141\1\156\1\uffff"+
        "\1\145\1\164\1\143\1\uffff\1\172\1\144\1\164\1\uffff\1\145\1\151"+
        "\2\uffff\1\163\1\144\1\uffff\1\151\1\156\1\164\1\162\1\171\1\164"+
        "\1\uffff\1\151\1\uffff\1\141\1\172\1\156\1\151\1\156\1\145\1\172"+
        "\1\164\1\145\1\uffff\1\164\1\172\1\164\1\172\1\154\1\143\1\172\1"+
        "\145\1\164\1\uffff\2\172\1\162\1\143\1\151\1\165\1\156\1\164\1\172"+
        "\1\151\1\160\1\141\1\156\1\162\1\172\1\uffff\1\147\1\156\1\151\1"+
        "\172\1\uffff\1\145\1\172\1\137\1\uffff\1\172\1\uffff\1\172\1\164"+
        "\1\uffff\2\172\2\uffff\2\172\1\157\1\162\1\164\1\172\1\uffff\1\156"+
        "\1\145\1\155\1\147\1\172\1\uffff\1\172\1\147\1\156\1\uffff\1\162"+
        "\1\uffff\1\165\2\uffff\1\172\4\uffff\1\156\1\145\1\172\1\uffff\1"+
        "\147\1\172\1\160\1\172\2\uffff\1\172\1\147\1\172\1\141\1\157\1\151"+
        "\1\163\1\uffff\2\172\1\uffff\1\172\1\uffff\1\172\2\uffff\1\172\1"+
        "\uffff\1\164\1\154\1\155\1\145\5\uffff\3\145\1\162\4\172\2\uffff"+
        "\1\164\2\uffff\1\141\1\155\1\160\1\172\1\uffff";
    static final String DFA13_acceptS =
        "\23\uffff\1\75\1\76\2\uffff\1\101\1\uffff\1\103\1\104\1\105\1\106"+
        "\1\107\1\110\1\111\1\112\1\113\1\114\1\uffff\1\117\1\121\45\uffff"+
        "\1\120\1\77\1\116\1\100\1\102\1\115\1\uffff\1\2\55\uffff\1\57\2"+
        "\uffff\1\1\1\3\21\uffff\1\24\6\uffff\1\40\11\uffff\1\64\2\uffff"+
        "\1\67\7\uffff\1\53\1\54\2\uffff\1\4\1\uffff\1\32\1\5\1\uffff\1\33"+
        "\10\uffff\1\34\3\uffff\1\25\3\uffff\1\30\2\uffff\1\56\1\55\2\uffff"+
        "\1\44\6\uffff\1\46\1\uffff\1\71\11\uffff\1\10\11\uffff\1\37\17\uffff"+
        "\1\73\4\uffff\1\31\3\uffff\1\16\1\uffff\1\20\2\uffff\1\36\2\uffff"+
        "\1\26\1\27\6\uffff\1\63\5\uffff\1\74\3\uffff\1\72\1\uffff\1\7\1"+
        "\uffff\1\17\1\35\1\uffff\1\22\1\23\1\41\1\42\3\uffff\1\62\4\uffff"+
        "\1\50\1\51\7\uffff\1\21\2\uffff\1\45\1\uffff\1\66\1\uffff\1\70\1"+
        "\52\1\uffff\1\6\4\uffff\1\43\1\60\1\65\1\47\1\61\10\uffff\1\11\1"+
        "\12\1\uffff\1\13\1\15\4\uffff\1\14";
    static final String DFA13_specialS =
        "\u0165\uffff}>";
    static final String[] DFA13_transitionS = {
            "\2\44\2\uffff\1\44\22\uffff\1\44\1\uffff\1\42\1\uffff\1\41\2"+
            "\uffff\1\45\1\36\1\37\1\23\1\24\1\40\1\25\1\26\1\27\12\43\1"+
            "\30\2\uffff\1\31\1\uffff\1\32\1\uffff\32\41\1\33\1\uffff\1\34"+
            "\1\uffff\1\41\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\41\1\10"+
            "\2\41\1\16\1\17\1\11\1\20\1\12\1\41\1\21\1\13\1\14\1\22\1\15"+
            "\4\41\1\uffff\1\35",
            "\1\46\6\uffff\1\47\2\uffff\1\50",
            "\1\52\2\uffff\1\53\2\uffff\1\51",
            "\1\54\6\uffff\1\55\6\uffff\1\56\5\uffff\1\57",
            "\1\63\1\60\2\uffff\1\61\3\uffff\1\62\5\uffff\1\64",
            "\1\65",
            "\1\70\2\uffff\1\66\2\uffff\1\67",
            "\1\71",
            "\1\72",
            "\1\74\17\uffff\1\73",
            "\1\75",
            "\1\100\3\uffff\1\76\3\uffff\1\77\7\uffff\1\101",
            "\1\102\10\uffff\1\103",
            "\1\104",
            "\1\105",
            "\1\106\7\uffff\1\107",
            "\1\110",
            "\1\111",
            "\1\112",
            "",
            "",
            "\1\113\1\115\1\uffff\12\43",
            "\12\115",
            "",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
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
            "\1\115\1\uffff\12\43",
            "",
            "",
            "\1\121",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "\1\123",
            "\1\124",
            "\1\125",
            "\1\126",
            "\1\127",
            "\1\130",
            "\1\131\10\uffff\1\132",
            "\1\133",
            "\1\134",
            "\1\137\2\uffff\1\135\5\uffff\1\136",
            "\1\140",
            "\1\141",
            "\1\142",
            "\1\143\16\uffff\1\144",
            "\1\145",
            "\1\146",
            "\1\147",
            "\1\150",
            "\1\151\1\152",
            "\1\154\1\153",
            "\1\155",
            "\1\156\11\uffff\1\157",
            "\1\160",
            "\1\161",
            "\1\162\4\uffff\1\163\7\uffff\1\164",
            "\1\165\12\uffff\1\166",
            "\1\167",
            "\1\170\7\uffff\1\171",
            "\1\173\5\uffff\1\172",
            "\1\174",
            "\1\175\3\uffff\1\176",
            "\1\177",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "\1\u0081",
            "\1\u0082",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "\1\u0085",
            "\1\u0086",
            "\1\u0087",
            "\1\u0088",
            "\1\u0089",
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
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "\1\u0097",
            "\1\u0098",
            "\1\u0099",
            "\1\u009a",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\4\41\1\u009c\11\41\1\u009b\13\41",
            "\1\u009e",
            "\1\u009f",
            "\1\u00a0",
            "\1\u00a1",
            "\1\u00a2",
            "\1\u00a3",
            "\1\u00a4",
            "\1\u00a5",
            "\1\u00a6",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "\1\u00a9\23\uffff\1\u00a8",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "\1\u00ab",
            "\1\u00ac",
            "\1\u00ad",
            "\1\u00ae",
            "\1\u00af",
            "\1\u00b0",
            "\1\u00b1",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "",
            "\1\u00b4",
            "\1\u00b5",
            "",
            "",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "\1\u00b7",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\1\u00ba\31\41",
            "\1\u00bc",
            "\1\u00bd",
            "\1\u00be",
            "\1\u00bf",
            "\1\u00c0",
            "\1\u00c1",
            "\1\u00c2",
            "\1\u00c3",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "\1\u00c5",
            "\1\u00c6",
            "\1\u00c7",
            "",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "\1\u00c9",
            "\1\u00ca",
            "\1\u00cb",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "\1\u00cd",
            "",
            "\1\u00ce",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "\1\u00d1",
            "\1\u00d2",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "\1\u00d4",
            "\1\u00d5",
            "\1\u00d6",
            "",
            "\1\u00d7",
            "\1\u00d8",
            "",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\22\41\1\u00d9\7\41",
            "\1\u00db",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "\1\u00dd",
            "\1\u00de",
            "\1\u00df",
            "\1\u00e0",
            "",
            "",
            "\1\u00e1",
            "\1\u00e2",
            "",
            "\1\u00e3",
            "",
            "",
            "\1\u00e4",
            "",
            "\1\u00e5",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "\1\u00e7",
            "\1\u00e8",
            "\1\u00e9",
            "\1\u00ea",
            "\1\u00eb",
            "\1\u00ec",
            "",
            "\1\u00ed",
            "\1\u00ee",
            "\1\u00ef",
            "",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "\1\u00f1",
            "\1\u00f2",
            "",
            "\1\u00f3",
            "\1\u00f4",
            "",
            "",
            "\1\u00f5",
            "\1\u00f6",
            "",
            "\1\u00f7",
            "\1\u00f8",
            "\1\u00f9",
            "\1\u00fa",
            "\1\u00fb",
            "\1\u00fc",
            "",
            "\1\u00fd",
            "",
            "\1\u00fe",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\22\41\1\u00ff\7\41",
            "\1\u0101",
            "\1\u0102",
            "\1\u0103",
            "\1\u0104",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "\1\u0106",
            "\1\u0107",
            "",
            "\1\u0108",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "\1\u010a",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "\1\u010c",
            "\1\u010d",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "\1\u010f",
            "\1\u0110",
            "",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "\1\u0113",
            "\1\u0114",
            "\1\u0115",
            "\1\u0116",
            "\1\u0117",
            "\1\u0118",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "\1\u011a",
            "\1\u011b",
            "\1\u011c",
            "\1\u011d",
            "\1\u011e",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "",
            "\1\u0120",
            "\1\u0121",
            "\1\u0122",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "",
            "\1\u0124",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "\1\u0126",
            "",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "\1\u0129",
            "",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "",
            "",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "\1\u012e",
            "\1\u012f",
            "\1\u0130",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "",
            "\1\u0132",
            "\1\u0133",
            "\1\u0134",
            "\1\u0135",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "\1\u0138",
            "\1\u0139",
            "",
            "\1\u013a",
            "",
            "\1\u013b\15\uffff\1\u013c\1\uffff\1\u013d\1\u013e",
            "",
            "",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "",
            "",
            "",
            "",
            "\1\u0140",
            "\1\u0141",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "",
            "\1\u0143",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "\1\u0145",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "",
            "",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "\1\u0148",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "\1\u014a",
            "\1\u014b",
            "\1\u014c",
            "\1\u014d",
            "",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "",
            "",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "",
            "\1\u0153",
            "\1\u0154",
            "\1\u0155",
            "\1\u0156",
            "",
            "",
            "",
            "",
            "",
            "\1\u0157",
            "\1\u0158",
            "\1\u0159",
            "\1\u015a",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\22\41\1\u015d\7\41",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
            "",
            "",
            "\1\u0160",
            "",
            "",
            "\1\u0161",
            "\1\u0162",
            "\1\u0163",
            "\1\41\10\uffff\2\41\1\uffff\13\41\6\uffff\32\41\4\uffff\1\41"+
            "\1\uffff\32\41",
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
            return "1:1: Tokens : ( ALL | AS | AVG | BOTH | CAST | CHARACTER | COLLATE | COUNT | CURRENT_DATE | CURRENT_ROLE | CURRENT_TIME | CURRENT_TIMESTAMP | CURRENT_USER | DB_KEY | DEFAULT | DELETE | DISTINCT | EXECUTE | EXTRACT | FOR | FROM | GEN_ID | INSERT | INTO | KW_BIGINT | KW_BLOB | KW_CHAR | KW_DATE | KW_DECIMAL | KW_DOUBLE | KW_FLOAT | KW_INT | KW_INTEGER | KW_NUMERIC | KW_PRECISION | KW_SIZE | KW_SMALLINT | KW_TIME | KW_TIMESTAMP | KW_VARCHAR | LEADING | MATCHING | MAXIMUM | MINIMUM | NEXT | NULL | OR | PROCEDURE | RETURNING | SEGMENT | SELECT | SET | SUBSTRING | SUB_TYPE | SUM | TRAILING | TRIM | UPDATE | VALUE | VALUES | T__74 | T__75 | T__76 | T__77 | T__78 | T__79 | T__80 | T__81 | T__82 | T__83 | T__84 | LEFT_PAREN | RIGHT_PAREN | COMMA | GENERIC_ID | QUOTED_ID | INTEGER | REAL | WS | SL_COMMENT | STRING );";
        }
    }
 

}