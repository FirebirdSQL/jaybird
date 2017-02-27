grammar JaybirdSql;

options {
    output=AST;
    ASTLabelType=CommonTree;
    backtrack = true;
    memoize=true;
}

tokens {
    ALL='all';
    AS='as';
    AVG='avg';

    BOTH='both';

    CAST='cast';
    CHARACTER='character';
    COUNT='count';
    COLLATE='collate';

    CURRENT_USER='current_user';
    CURRENT_ROLE='current_role';

    CURRENT_DATE='current_date';
    CURRENT_TIME='current_time';
    CURRENT_TIMESTAMP='current_timestamp';

    DEFAULT='default';
    DELETE='delete';
    DISTINCT='distinct';
    DB_KEY='db_key';
    
    EXTRACT='extract';
    EXECUTE='execute';

    FOR='for';
    FROM='from';

    GEN_ID='gen_id';

    INSERT='insert';
    INTO='into';
    
    LEADING='leading';

    MATCHING='matching';
    MINIMUM='min';
    MAXIMUM='max';
    
    NULL='null';
    NEXT='next';
    
    OR='or';
    
    PROCEDURE='procedure';
    
    RETURNING='returning';
    
    SEGMENT='segment';
    SELECT='select';
    SET='set';
    SUBSTRING='substring';
    SUB_TYPE='sub_type';
    SUM='sum';
    
    TRIM='trim';
    TRAILING='trailing';

    UPDATE='update';

    VALUE='value';
    VALUES='values';
    
    KW_BLOB='blob';
    KW_BIGINT='bigint';
    KW_CHAR='char';
    KW_DATE='date';
    KW_DECIMAL='decimal';
    KW_DOUBLE='double';
    KW_PRECISION='precision';
    KW_FLOAT='float';
    KW_INTEGER='integer';
    KW_INT='int';
    KW_NUMERIC='numeric';
    KW_SMALLINT='smallint';
    KW_TIME='time';
    KW_TIMESTAMP='timestamp';
    KW_VARCHAR='varchar';

    KW_SIZE='size';    
}

@parser::header {
package org.firebirdsql.jdbc.parser;
}

@lexer::header {
package org.firebirdsql.jdbc.parser;
}

@parser::members{
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
}

@lexer::members{
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
}

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
            DELETE FROM tableName /* other parts ignored by parser */ returningClause?
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
            UPDATE tableName SET assignments /* other parts ignored by parser */ returningClause?
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
        
matchingClause    :    MATCHING columnList
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
                    (    insertValues returningClause?
                    |    selectClause
                    |    defaultValuesClause returningClause?
                    )
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
        |    simpleValue '+' simpleValue
        |    simpleValue '-' simpleValue
        |    simpleValue '*' simpleValue
        |    simpleValue    '/' simpleValue
        |    simpleValue '||' simpleValue
        |    '+' simpleValue
        |    '-' simpleValue
        
        |    LEFT_PAREN simpleValue RIGHT_PAREN
        
        |    simpleValue COLLATE    simpleIdentifier
    
        |    parameter
        
        |    CURRENT_USER
        |    CURRENT_ROLE
        |    CURRENT_DATE
        |    CURRENT_TIME
        |    CURRENT_TIMESTAMP
        
        |    nullValue
        
        |    function
        |    nextValueExpression
        |    castExpression
//        |    caseExpression
        
        |    arrayElement
        
        |    DB_KEY
        |    simpleIdentifier '.' DB_KEY
        ;
    
parameter
        :    '?'
        ;

nullValue
        :    NULL
        ;
        
simpleValue     
        :    GENERIC_ID
        |    STRING
        |    INTEGER
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
        :    KW_CHAR ('(' INTEGER ')')?
        |    KW_VARCHAR '(' INTEGER ')'
        ;

charSetCharType
        :    nonCharSetCharType charSetClause
        ;

nonCharType
        :    KW_BIGINT
        |    KW_DATE
        |    KW_DECIMAL '(' INTEGER (',' INTEGER)? ')'
        |    KW_DOUBLE KW_PRECISION
        |    KW_FLOAT
        |    KW_INTEGER
        |    KW_INT
        |    KW_NUMERIC '(' INTEGER (',' INTEGER)? ')'
        |    KW_SMALLINT
        |    KW_TIME
        |    KW_TIMESTAMP
        ; 

blobType
        :    KW_BLOB blobSubtype? blobSegSize? charSetClause?
            /* BUG: it allows BLOB(), but we can live with it */
        |    KW_BLOB '(' INTEGER? (',' INTEGER)? ')'
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
        
LEFT_PAREN 
        :    '('
        ;
    
RIGHT_PAREN 
        :    ')'
        ;

COMMA     :    ','
        ;

GENERIC_ID 
        :    ( 'a'..'z'|'A'..'Z' | '_' | ':' | '$') 
            ( options {greedy=true;} : 'a'..'z'|'A'..'Z' | '0'..'9' | '.' | '-' | '_' | ':' |'$' )*
        ;
    
QUOTED_ID
        : '\"' ( '\u0000' .. '\u0021' | '\u0023' .. '\uFFFF' | '\"\"' )+ '\"'
        ;
    
// LETTER    :    'a'..'z' 
//         |    'A'..'Z'
//         ;

INTEGER :    ('-')?('0'..'9')+
        ;

REAL     :    ('-')?('0'..'9')*'.'('0'..'9')+
        ;

WS        :    (' '|'\t'|'\n'|'\r')+  {$channel = HIDDEN;}
        ;    
    
SL_COMMENT
        :    '--' (~('\n'|'\r'))* ('\n'|'\r'('\n')?)
        ;

STRING     
        :    ( '\'' (~'\'')* '\'')
        ;