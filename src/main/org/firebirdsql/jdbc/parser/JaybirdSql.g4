grammar JaybirdSql;

@parser::members {
private boolean _inReturning;
protected boolean _defaultValues;
protected JaybirdStatementModel statementModel = new JaybirdStatementModel();

protected java.util.ArrayList _errorMessages = new java.util.ArrayList();

public JaybirdStatementModel getStatementModel() {
    return statementModel;
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

public void emitErrorMessage(String msg) {
    _errorMessages.add(msg);
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
}

ALL : [Aa][Ll][Ll];
AND : [Aa][Nn][Dd];
AS : [Aa][Ss];
AVG: [Aa][Vv][Gg];

BOTH: [Bb][Oo][Tt][Hh];

CAST: [Cc][Aa][Ss][Tt];
CHARACTER: [Cc][Hh][Aa][Rr][Aa][Cc][Tt][Ee][Rr];
COUNT: [Cc][Oo][Uu][Nn][Tt];
COLLATE: [Cc][Oo][Ll][Ll][Aa][Tt][Ee];

DEFAULT: [Dd][Ee][Ff][Aa][Uu][Ll][Tt];
DELETE: [Dd][Ee][Ll][Ee][Tt][Ee];
DISTINCT: [Dd][Ii][Ss][Tt][Ii][Nn][Cc][Tt];
DB_KEY: [Dd][Bb][_][Kk][Ee][Yy];

EXTRACT: [Ee][Xx][Tt][Rr][Aa][Cc][Tt];
EXECUTE: [Ee][Xx][Ee][Cc][Uu][Tt][Ee];

FOR: [Ff][Oo][Rr];
FROM: [Ff][Rr][Oo][Mm];

GEN_ID: [Gg][Ee][Nn][_][Ii][Dd];

INSERT : [Ii][Nn][Ss][Ee][Rr][Tt];
INTO : [Ii][Nn][Tt][Oo];

LEADING: [Ll][Ee][Aa][Dd][Ii][Nn][Gg];

MATCHING: [Mm][Aa][Tt][Cc][Hh][Ii][Nn][Gg];
MINIMUM: [Mm][Ii][Nn];
MAXIMUM: [Mm][Aa][Xx];

NULL: [Nn][Uu][Ll][Ll];
NEXT: [Nn][Ee][Xx][Tt];

OR: [Oo][Rr];

PROCEDURE: [Pp][Rr][Oo][Cc][Ee][Dd][Uu][Rr][Ee];

RETURNING: [Rr][Ee][Tt][Uu][Rr][Nn][Ii][Nn][Gg];

SEGMENT: [Ss][Ee][Gg][Mm][Ee][Nn][Tt];
SELECT: [Ss][Ee][Ll][Ee][Cc][Tt];
SET: [Ss][Ee][Tt];
SUBSTRING: [Ss][Uu][Bb][Ss][Tt][Rr][Ii][Nn][Gg];
SUB_TYPE: [Ss][Uu][Bb][_][Tt][Yy][Pp][Ee];
SUM: [Ss][Uu][Mm];

TRIM: [Tt][Rr][Ii][Mm];
TRAILING: [Tt][Rr][Aa][Ii][Ll][Ii][Nn][Gg];

UNKNOWN: [Uu][Nn][Kk][Nn][Oo][Ww][Nn];
UPDATE: [Uu][Pp][Dd][Aa][Tt][Ee];

VALUE: [Vv][Aa][Ll][Uu][Ee];
VALUES: [Vv][Aa][Ll][Uu][Ee][Ss];

KW_BLOB: [Bb][Ll][Oo][Bb];
KW_BIGINT: [Bb][Ii][Gg][Ii][Nn][Tt];
KW_BOOLEAN: [Bb][Oo][Oo][Ll][Ee][Aa][Nn];
KW_CHAR: [Cc][Hh][Aa][Rr];
KW_DATE: [Dd][Aa][Tt][Ee];
KW_DECIMAL: [Dd][Ee][Cc][Ii][Mm][Aa][Ll];
KW_DOUBLE: [Dd][Oo][Uu][Bb][Ll][Ee];
KW_PRECISION: [Pp][Rr][Ee][Cc][Ii][Ss][Ii][Oo][Nn];
KW_FLOAT: [Ff][Ll][Oo][Aa][Tt];
KW_INTEGER: [Ii][Nn][Tt][Ee][Gg][Ee][Rr];
KW_INT: [Ii][Nn][Tt];
KW_NCHAR: [Nn][Cc][Hh][Aa][Rr];
KW_NUMERIC: [Nn][Uu][Mm][Ee][Rr][Ii][Cc];
KW_NVARCHAR: [Nn][Vv][Aa][Rr][Cc][Hh][Aa][Rr];
KW_SMALLINT: [Ss][Mm][Aa][Ll][Ll][Ii][Nn][Tt];
KW_TIME: [Tt][Ii][Mm][Ee];
KW_TIMESTAMP: [Tt][Ii][Mm][Ee][Ss][Tt][Aa][Mm][Pp];
KW_VARCHAR: [Vv][Aa][Rr][Cc][Hh][Aa][Rr];
KW_SIZE: [Ss][Ii][Zz][Ee];

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
        :    [Xx]'\'' (HEXIT HEXIT)* '\''
        ;

TRUTH_VALUE
        : [Tt][Rr][Uu][Ee]
        | [Ff][Aa][Ll][Ss][Ee]
        ;

GENERIC_ID
        :    ID_LETTER ( ID_LETTER | ID_NUMBER_OR_SYMBOL )*
        ;

QUOTED_ID
        : '\"' ( ID_QUOTED_UNICODE )+ '\"'
        ;

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
        | '\"\"'
        ;

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

SL_COMMENT : '--' .*? '\r'? '\n' -> skip ; // Match "--" stuff '\n'
COMMENT : '/*' .*? '*/' -> skip ; // Match "/*" stuff "*/"
WS : [ \t\r\n]+ -> skip ;