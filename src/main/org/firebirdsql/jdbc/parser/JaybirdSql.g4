grammar JaybirdSql;

/* This grammar is extemely limited and only intended to identify DML statement types and whether or not they have
 * a RETURNING clause. It is not generally usable outside of Jaybird.
 */

@parser::members {
protected JaybirdStatementModel statementModel = new JaybirdStatementModel();

public JaybirdStatementModel getStatementModel() {
    return statementModel;
}
}

@lexer::members {
boolean ahead(String text) {
    for (int i = 0; i < text.length(); i++) {
        if (_input.LA(i + 1) != text.charAt(i)) {
            return false;
        }
    }
    return true;
}
}

AS: A S;

DELETE: D E L E T E;

FROM: F R O M;

INSERT: I N S E R T;
INTO: I N T O;

MERGE: M E R G E;

OR: O R;

RETURNING: R E T U R N I N G;

SET: S E T;

UPDATE: U P D A T E;

LEFT_PAREN
        :    '('
        ;

RIGHT_PAREN
        :    ')'
        ;

COMMA   :    ','
        ;

/* String token definitions exist only to support parsing */

STRING  :    '\'' (~'\''|'\'\'')* '\''
        ;

BINARY_STRING
        :    X '\'' (HEXIT HEXIT)* '\''
        ;

fragment QS_OTHER_CH: ~('<' | '{' | '[' | '(' | ' ' | '\t' | '\n' | '\r');
Q_STRING : Q ['] ( QUOTED_TEXT ) ['];
fragment QUOTED_TEXT
    : '<' .*? '>'
    | '{' .*? '}'
    | '[' .*? ']'
    | '(' .*? ')'
    | QS_OTHER_CH ({!ahead(getText().charAt(2) + "'")}? .)* ({ahead(getText().charAt(2) + "'")}? .) ;

GENERIC_ID
        :    ID_LETTER ( ID_LETTER | ID_NUMBER_OR_SYMBOL )*
        ;

QUOTED_ID
        : '"' ( ID_QUOTED_UNICODE )+ '"'
        ;

fragment A: [aA];
fragment B: [bB];
fragment C: [cC];
fragment D: [dD];
fragment E: [eE];
fragment F: [fF];
fragment G: [gG];
fragment H: [hH];
fragment I: [iI];
fragment J: [jJ];
fragment K: [kK];
fragment L: [lL];
fragment M: [mM];
fragment N: [nN];
fragment O: [oO];
fragment P: [pP];
fragment Q: [qQ];
fragment R: [rR];
fragment S: [sS];
fragment T: [tT];
fragment U: [uU];
fragment V: [vV];
fragment W: [wW];
fragment X: [xX];
fragment Y: [yY];
fragment Z: [zZ];

fragment HEXIT
        : DIGIT
        | [a-fA-F]
        ;

fragment DIGIT
        : [0-9]
        ;

fragment ID_LETTER
        : [a-zA-Z]
        ;
fragment ID_NUMBER_OR_SYMBOL
        : DIGIT | '_' | '$'
        ;

fragment ID_QUOTED_UNICODE
        : '\u0000' .. '\u0021'
        | '\u0023' .. '\uFFFF'
        | '""'
        ;

fragment NEWLINE: '\r'? '\n';

statement
        :    insertStatement
        |    deleteStatement
        |    updateStatement
        |    updateOrInsertStatement
        |    mergeStatement
        ;

/* DELETE statement */

deleteStatement    :
            DELETE FROM tableName .*? /* other parts ignored by parser */ returningClause? ';'?
            {
                statementModel.setStatementType(JaybirdStatementModel.DELETE_TYPE);
            }
        ;

/* UPDATE statement */

updateStatement    :
            UPDATE tableName alias? SET .*? /* other parts ignored by parser */ returningClause? ';'?
            {
                statementModel.setStatementType(JaybirdStatementModel.UPDATE_TYPE);
            }
        ;

/* UPDATE OR INSERT statement */

updateOrInsertStatement
        :    UPDATE OR INSERT INTO tableName .*? /* other parts ignored by parser */ returningClause? ';'?
            {
                statementModel.setStatementType(JaybirdStatementModel.UPDATE_OR_INSERT_TYPE);
            }
        ;

/* INSERT statement */

insertStatement
        :     INSERT INTO tableName .*? /* other parts ignored by parser */ returningClause? ';'?
            {
                statementModel.setStatementType(JaybirdStatementModel.INSERT_TYPE);
            }
        ;

 /* MERGE statement */

mergeStatement
        :    MERGE INTO tableName .*? /* other parts ignored by parser */ returningClause? ';'?
            {
                statementModel.setStatementType(JaybirdStatementModel.MERGE_TYPE);
            }
        ;

returningClause
        :   RETURNING (simpleIdentifier '.')? '*'
           {
               statementModel.setHasReturning();
           }
        |   RETURNING returningColumnList
           {
               statementModel.setHasReturning();
           }
        ;

simpleIdentifier
        :    GENERIC_ID
        |    QUOTED_ID
        ;

fullIdentifier
        :    simpleIdentifier '.' simpleIdentifier
        ;

tableName
        :    t = simpleIdentifier
            {
                statementModel.setTableName($t.text);
            }
        ;

returningColumnList
        :    columnName alias? (',' columnName alias?)*
        ;

columnName
        :    simpleIdentifier
        |    fullIdentifier
        ;

alias
        :    AS? simpleIdentifier
        ;

// Incomplete simple value definition; primary use is to test Q_STRING parsing (which is present to support identifying tokens)
simpleValue
        :/*  TRUTH_VALUE
        |*/  STRING
        |    BINARY_STRING
        |    Q_STRING
      /*|    INTEGER
        |    NUMERIC
        |    REAL    */
        ;

SL_COMMENT : '--' ( ~('\r' | '\n') )* (NEWLINE|EOF) -> skip ; // Match comment "--" stuff '\n'
COMMENT : '/*' .*? '*/' -> skip ; // Match comment "/*" stuff "*/"
WS : [ \t\r\n]+ -> skip ;
// 'match any character not matched by another lexer rule' (see ANTLR4 The Complete Reference section 15.6)
OTHER : . -> skip ;