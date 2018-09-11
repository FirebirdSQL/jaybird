grammar JaybirdSql;

@parser::members {
private boolean _inReturning;
protected boolean _defaultValues;
protected JaybirdStatementModel statementModel = new JaybirdStatementModel();

public JaybirdStatementModel getStatementModel() {
    return statementModel;
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

}

@lexer::members {

protected java.util.ArrayList _errorMessages = new java.util.ArrayList();

public java.util.Collection getErrorMessages() {
    return _errorMessages;
}

public void emitErrorMessage(String msg) {
    _errorMessages.add(msg);
}

boolean ahead(String text) {
    for (int i = 0; i < text.length(); i++) {
        if (_input.LA(i + 1) != text.charAt(i)) {
            return false;
        }
    }
    return true;
}
}

ALL : A L L;
AND : A N D;
AS : A S;
AVG: A V G;

BOTH: B O T H;

CAST: C A S T;
CHARACTER: C H A R A C T E R;
COUNT: C O U N T;
COLLATE: C O L L A T E;

DEFAULT: D E F A U L T;
DELETE: D E L E T E;
DISTINCT: D I S T I N C T;
DB_KEY: D B '_' K E Y;

EXTRACT: E X T R A C T;
EXECUTE: E X E C U T E;

FOR: F O R;
FROM: F R O M;

GEN_ID: G E N '_' I D;

INSERT: I N S E R T;
INTO: I N T O;

LEADING: L E A D I N G;

MATCHING: M A T C H I N G;
MINIMUM: M I N;
MAXIMUM: M A X;

NULL: N U L L;
NEXT: N E X T;

OR: O R;

PROCEDURE: P R O C E D U R E;

RETURNING: R E T U R N I N G;

SEGMENT: S E G M E N T;
SELECT: S E L E C T;
SET: S E T;
SUBSTRING: S U B S T R I N G;
SUB_TYPE: S U B '_' T Y P E;
SUM: S U M;

TRIM: T R I M;
TRAILING: T R A I L I N G;

UNKNOWN: U N K N O W N;
UPDATE: U P D A T E;

VALUE: V A L U E;
VALUES: V A L U E S;

KW_BLOB: B L O B;
KW_BIGINT: B I G I N T;
KW_BOOLEAN: B O O L E A N;
KW_CHAR: C H A R;
KW_DATE: D A T E;
KW_DECIMAL: D E C I M A L;
KW_DOUBLE: D O U B L E;
KW_PRECISION: P R E C I S I O N;
KW_FLOAT: F L O A T;
KW_INTEGER: I N T E G E R;
KW_INT: I N T;
KW_NCHAR: N C H A R;
KW_NUMERIC: N U M E R I C;
KW_NVARCHAR: N V A R C H A R;
KW_SMALLINT: S M A L L I N T;
KW_TIME: T I M E;
KW_TIMESTAMP: T I M E S T A M P;
KW_VARCHAR: V A R C H A R;
KW_SIZE: S I Z E;

LEFT_PAREN
        :    '('
        ;

RIGHT_PAREN
        :    ')'
        ;

COMMA   :    ','
        ;

INTEGER :    ('-')?DIGIT+
        |    ('-')?'0'[Xx](HEXIT HEXIT)+
        ;

NUMERIC :    ('-')?'.'DIGIT+
        |    ('-')?DIGIT+'.'DIGIT*
        ;

REAL    :    ('-')?(DIGIT+ | NUMERIC)+[Ee]('-')?('0'..'9')
        ;

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

TRUTH_VALUE
        : T R U E
        | F A L S E
        ;

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
//        |    executeStatement
        |    updateOrInsertStatement
        ;

/* DELETE statement

delete        : delete_searched
        | delete_positioned
        ;

delete_searched    : KW_DELETE FROM table_name where_clause
        plan_clause order_clause rows_clause returning_clause
            { $$ = make_node (nod_delete, (int) e_del_count,
                $3, $4, $5, $6, $7, NULL, $8); }
        ;

delete_positioned : KW_DELETE FROM table_name cursor_clause
            { $$ = make_node (nod_delete, (int) e_del_count,
                $3, NULL, NULL, NULL, NULL, $4, NULL); }
*/

deleteStatement    :
            DELETE FROM tableName .*? /* other parts ignored by parser */ returningClause?
            {
                statementModel.setStatementType(JaybirdStatementModel.DELETE_TYPE);
            }
        ;

/* UPDATE statement

update        : update_searched
        | update_positioned
        ;

update_searched    : UPDATE table_name SET assignments where_clause
        plan_clause order_clause rows_clause returning_clause
            { $$ = make_node (nod_update, (int) e_upd_count,
                $2, make_list ($4), $5, $6, $7, $8, NULL, $9, NULL); }
              ;

update_positioned : UPDATE table_name SET assignments cursor_clause
            { $$ = make_node (nod_update, (int) e_upd_count,
                $2, make_list ($4), NULL, NULL, NULL, NULL, $5, NULL, NULL); }
*/

updateStatement    :
            UPDATE tableName SET assignments .*? /* other parts ignored by parser */ returningClause?
            {
                statementModel.setStatementType(JaybirdStatementModel.UPDATE_TYPE);
            }
        ;

assignments    :    assignment (',' assignment)*
        ;

assignment    :    columnName '=' value
        ;

/* UPDATE OR INSERT statement

update_or_insert
    :    UPDATE OR INSERT INTO simple_table_name ins_column_parens_opt
            VALUES '(' value_list ')'
            update_or_insert_matching_opt
            returning_clause
        {
            $$ = make_node (nod_update_or_insert, (int) e_upi_count,
                $5, make_list ($6), make_list ($9), $11, $12);
        }
    ;
*/
updateOrInsertStatement
        :    UPDATE OR INSERT INTO tableName insertColumns?
                insertValues matchingClause? returningClause?
            {
                statementModel.setStatementType(JaybirdStatementModel.UPDATE_OR_INSERT_TYPE);
            }
        ;

matchingClause
        :    MATCHING columnList
        ;

/*
insert
        :    INSERT INTO simple_table_name ins_column_parens_opt
              VALUES '(' value_list ')' returning_clause
            | INSERT INTO simple_table_name ins_column_parens_opt select_expr returning_clause
            | INSERT INTO simple_table_name DEFAULT VALUES returning_clause
            ;
*/
insertStatement
        :     INSERT INTO tableName insertColumns?
                    (    insertValues
                    |    selectClause .*? /* other parts ignored by parser */
                    |    defaultValuesClause
                    ) returningClause?
            {
                statementModel.setStatementType(JaybirdStatementModel.INSERT_TYPE);
            }
        ;

insertColumns
        :    '(' columnList ')'
            {
                _inReturning = false;
            }
        ;

insertValues
        :    VALUES '(' valueList ')'
        ;

returningClause
        :    RETURNING {_inReturning = true;} columnList {_inReturning = true;}
        ;

defaultValuesClause
        :    DEFAULT VALUES
            {
                statementModel.setDefaultValues(true);
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

columnList
        :    columnName (',' columnName)*
        ;

columnName
        :    si = simpleIdentifier
            {
                if (_inReturning)
                    statementModel.addReturningColumn($si.text);
                else
                    statementModel.addColumn($si.text);
            }

        |    fi = fullIdentifier
            {
                if (_inReturning)
                    statementModel.addReturningColumn($fi.text);
                else
                    statementModel.addColumn($fi.text);
            }
        ;

valueList
        :    value (',' value)*
        ;

/*
value    : column_name
        | array_element
        | function
        | u_constant
        | parameter
        | variable
        | cast_specification
        | case_expression
        | next_value_expression
        | udf
        | '-' value
        | '+' value
        | value '+' value
        | value CONCATENATE value
        | value COLLATE symbol_collation_name
        | value '-' value
        | value '*' value
        | value '/' value
        | '(' value ')'
        | '(' column_singleton ')'
        | current_user
        | current_role
        | internal_info
        | DB_KEY
        | symbol_table_alias_name '.' DB_KEY
        | KW_VALUE
        | datetime_value_expression
+        | null_value
        ;
*/
value
        :    simpleValue
        |    value '+' value
        |    value '-' value
        |    value '*' value
        |    value '/' value
        |    value '||' value
        |    '+' simpleValue
        |    '-' simpleValue

        |    LEFT_PAREN value RIGHT_PAREN

        |    value COLLATE simpleIdentifier

        |    parameter

        |    nullValue

        |    function
        |    nextValueExpression
        |    castExpression
//        |    caseExpression

        |    arrayElement

        |    DB_KEY
        |    simpleIdentifier '.' DB_KEY
        |    simpleIdentifier
        ;

parameter
        :    '?'
        ;

nullValue
        :    NULL
        |    UNKNOWN
        ;

simpleValue
        :    TRUTH_VALUE
        |    STRING
        |    BINARY_STRING
        |    Q_STRING
        |    INTEGER
        |    NUMERIC
        |    REAL
        ;

nextValueExpression
        :    NEXT VALUE FOR simpleIdentifier
        |    GEN_ID '(' simpleIdentifier ',' INTEGER ')'
        ;

castExpression
        :    CAST '(' value AS dataTypeDescriptor ')'
        ;

dataTypeDescriptor
        :    nonArrayType
        |    arrayType
        ;

nonArrayType
        :    simpleType
        |    blobType
        ;

simpleType
        :    nonCharType
        |    charType
        ;

charType
        :    nonCharSetCharType
        |    charSetCharType
        ;

nonCharSetCharType
        :    (KW_CHAR | KW_NCHAR) ('(' INTEGER ')')?
        |    (KW_VARCHAR | KW_NVARCHAR) '(' INTEGER ')'
        ;

charSetCharType
        :    nonCharSetCharType charSetClause
        ;

nonCharType
        :    KW_BIGINT
        |    KW_DATE
        |    KW_DECIMAL ('(' INTEGER (',' INTEGER)? ')')?
        |    KW_DOUBLE KW_PRECISION
        |    KW_FLOAT
        |    KW_INTEGER
        |    KW_INT
        |    KW_NUMERIC ('(' INTEGER (',' INTEGER)? ')')?
        |    KW_SMALLINT
        |    KW_TIME
        |    KW_TIMESTAMP
        |    KW_BOOLEAN
        ;

blobType
        :    KW_BLOB blobSubtype? blobSegSize? charSetClause?
        |    KW_BLOB '(' INTEGER (',' INTEGER)? ')'
        ;

blobSubtype
        :    SUB_TYPE INTEGER
        |    SUB_TYPE GENERIC_ID
        ;

blobSegSize
        :    SEGMENT KW_SIZE INTEGER
        ;

charSetClause
        :    CHARACTER SET GENERIC_ID
        ;

arrayType
        :    nonCharSetCharType '[' arraySpec ']' charSetClause?
        |    nonCharType '[' arraySpec ']'
        ;

arraySpec
        :    arrayRange (',' arrayRange)?
        ;

arrayRange
        :    INTEGER (':' INTEGER)
        ;

arrayElement
        :    simpleIdentifier '[' valueList ']'
        ;

function
        :    simpleIdentifier '(' valueList ')'
        |    simpleIdentifier '(' ')'
        |    substringFunction
        |    trimFunction
        |    extractFunction
        |    SUM     '(' (ALL|DISTINCT)? value ')'
        |    COUNT     '(' (ALL|DISTINCT)? value ')'
        |    AVG        '(' (ALL|DISTINCT)? value ')'
        |    MINIMUM    '(' (ALL|DISTINCT)? value ')'
        |    MAXIMUM '(' (ALL|DISTINCT)? value ')'
        ;

substringFunction
        :    SUBSTRING '(' value FROM value (FOR INTEGER)? ')'
        ;

trimFunction
        :    TRIM '(' (trimSpecification)? value (FROM value)? ')'
        ;

extractFunction
        :    EXTRACT '(' value FROM value ')'
        ;

trimSpecification
        :    BOTH
        |    TRAILING
        |    LEADING
        ;

selectClause
        :    SELECT
        ;

SL_COMMENT : '--' ( ~('\r' | '\n') )* (NEWLINE|EOF) -> skip ; // Match "--" stuff '\n'
COMMENT : '/*' .*? '*/' -> skip ; // Match "/*" stuff "*/"
WS : [ \t\r\n]+ -> skip ;