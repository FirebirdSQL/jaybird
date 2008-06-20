lexer grammar JaybirdSql;
@members {
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
}
@header {
package org.firebirdsql.jdbc.parser;
}

ALL : 'all' ;
AS : 'as' ;
AVG : 'avg' ;
BOTH : 'both' ;
CAST : 'cast' ;
CHARACTER : 'character' ;
COUNT : 'count' ;
COLLATE : 'collate' ;
CURRENT_USER : 'current_user' ;
CURRENT_ROLE : 'current_role' ;
CURRENT_DATE : 'current_date' ;
CURRENT_TIME : 'current_time' ;
CURRENT_TIMESTAMP : 'current_timestamp' ;
DEFAULT : 'default' ;
DELETE : 'delete' ;
DISTINCT : 'distinct' ;
DB_KEY : 'db_key' ;
EXTRACT : 'extract' ;
EXECUTE : 'execute' ;
FOR : 'for' ;
FROM : 'from' ;
GEN_ID : 'gen_id' ;
INSERT : 'insert' ;
INTO : 'into' ;
LEADING : 'leading' ;
MATCHING : 'matching' ;
MINIMUM : 'min' ;
MAXIMUM : 'max' ;
NULL : 'null' ;
NEXT : 'next' ;
OR : 'or' ;
PROCEDURE : 'procedure' ;
RETURNING : 'returning' ;
SEGMENT : 'segment' ;
SELECT : 'select' ;
SET : 'set' ;
SUBSTRING : 'substring' ;
SUB_TYPE : 'sub_type' ;
SUM : 'sum' ;
TRIM : 'trim' ;
TRAILING : 'trailing' ;
UPDATE : 'update' ;
VALUE : 'value' ;
VALUES : 'values' ;
KW_BLOB : 'blob' ;
KW_BIGINT : 'bigint' ;
KW_CHAR : 'char' ;
KW_DATE : 'date' ;
KW_DECIMAL : 'decimal' ;
KW_DOUBLE : 'double' ;
KW_PRECISION : 'precision' ;
KW_FLOAT : 'float' ;
KW_INTEGER : 'integer' ;
KW_INT : 'int' ;
KW_NUMERIC : 'numeric' ;
KW_SMALLINT : 'smallint' ;
KW_TIME : 'time' ;
KW_TIMESTAMP : 'timestamp' ;
KW_VARCHAR : 'varchar' ;
KW_SET : 'set' ;
KW_SIZE : 'size' ;
T78 : '=' ;
T79 : '.' ;
T80 : '+' ;
T81 : '-' ;
T82 : '*' ;
T83 : '/' ;
T84 : '||' ;
T85 : '?' ;
T86 : '[' ;
T87 : ']' ;
T88 : ':' ;

// $ANTLR src "D:\projects\Firebird\client-java.22.head\src\main\org\firebirdsql\jdbc\parser\JaybirdSql.g3" 600
LEFT_PAREN 
		:	'('
		;
	
// $ANTLR src "D:\projects\Firebird\client-java.22.head\src\main\org\firebirdsql\jdbc\parser\JaybirdSql.g3" 604
RIGHT_PAREN 
		:	')'
		;

// $ANTLR src "D:\projects\Firebird\client-java.22.head\src\main\org\firebirdsql\jdbc\parser\JaybirdSql.g3" 608
COMMA 	:	','
		;

// $ANTLR src "D:\projects\Firebird\client-java.22.head\src\main\org\firebirdsql\jdbc\parser\JaybirdSql.g3" 611
GENERIC_ID 
    	:	( LETTER | '_' | ':' | '$') 
        	( options {greedy=true;} : LETTER | '0'..'9' | '.' | '-' | '_' | ':' |'$' )*
		;
	
// $ANTLR src "D:\projects\Firebird\client-java.22.head\src\main\org\firebirdsql\jdbc\parser\JaybirdSql.g3" 616
QUOTED_ID
		: '\"' ((ESCqd)=>ESCqd | ~'\"')* '\"'
		;
	
// $ANTLR src "D:\projects\Firebird\client-java.22.head\src\main\org\firebirdsql\jdbc\parser\JaybirdSql.g3" 620
LETTER	:	'a'..'z' 
		|	'A'..'Z'
		;

// $ANTLR src "D:\projects\Firebird\client-java.22.head\src\main\org\firebirdsql\jdbc\parser\JaybirdSql.g3" 624
INTEGER :	('-')?('0'..'9')+
		;

// $ANTLR src "D:\projects\Firebird\client-java.22.head\src\main\org\firebirdsql\jdbc\parser\JaybirdSql.g3" 627
REAL 	:	('-')?('0'..'9')*'.'('0'..'9')+
		;

// $ANTLR src "D:\projects\Firebird\client-java.22.head\src\main\org\firebirdsql\jdbc\parser\JaybirdSql.g3" 630
WS		:	(' '|'\t'|'\n'|'\r')+  {$channel = HIDDEN;}
		;	
	
// $ANTLR src "D:\projects\Firebird\client-java.22.head\src\main\org\firebirdsql\jdbc\parser\JaybirdSql.g3" 633
SL_COMMENT
        :	'--' (~('\n'|'\r'))* ('\n'|'\r'('\n')?)
        ;

// $ANTLR src "D:\projects\Firebird\client-java.22.head\src\main\org\firebirdsql\jdbc\parser\JaybirdSql.g3" 637
STRING 	
		:	( '\'' ((ESCqs)=>ESCqs | ~'\'')* '\'')
		;
		
// $ANTLR src "D:\projects\Firebird\client-java.22.head\src\main\org\firebirdsql\jdbc\parser\JaybirdSql.g3" 641
ESCqs	:	'\'' '\'';
// $ANTLR src "D:\projects\Firebird\client-java.22.head\src\main\org\firebirdsql\jdbc\parser\JaybirdSql.g3" 642
ESCqd	:	'\"' '\"';
