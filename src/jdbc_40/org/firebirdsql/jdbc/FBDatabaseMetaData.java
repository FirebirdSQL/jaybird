package org.firebirdsql.jdbc;

import java.sql.*;
import java.util.ArrayList;

import org.firebirdsql.gds.*;
import org.firebirdsql.gds.impl.GDSHelper;


public class FBDatabaseMetaData extends AbstractDatabaseMetaData {

    public FBDatabaseMetaData(AbstractConnection c) throws GDSException {
        super(c);
        // TODO Auto-generated constructor stub
    }

    public FBDatabaseMetaData(GDSHelper gdsHelper) {
        super(gdsHelper);
        // TODO Auto-generated constructor stub
    }
    
    //-------------------------- JDBC 4.0 -------------------------------------
    
    public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException {
        return false;
    }
    
    public boolean autoCommitFailureClosesAllResultSets() throws SQLException {
        // the holdable result sets remain open, others are closed, but this
        // happens before the statement is executed
        return false;
    }

    /**
     * Retrieves a list of the client info properties 
     * that the driver supports.  The result set contains the following columns
     * <p>
         * <ol>
     * <li><b>NAME</b> String=> The name of the client info property<br>
     * <li><b>MAX_LEN</b> int=> The maximum length of the value for the property<br>
     * <li><b>DEFAULT_VALUE</b> String=> The default value of the property<br>
     * <li><b>DESCRIPTION</b> String=> A description of the property.  This will typically 
     *                      contain information as to where this property is 
     *                      stored in the database.
     * </ol>
         * <p>
     * The <code>ResultSet</code> is sorted by the NAME column
     * <p>
     * @return  A <code>ResultSet</code> object; each row is a supported client info
         * property
     * <p>
     *  @exception SQLException if a database access error occurs
     * <p>
     * @since 1.6
     */
    public ResultSet getClientInfoProperties() throws SQLException {
        XSQLVAR[] xsqlvars = new XSQLVAR[4];

        xsqlvars[0] = new XSQLVAR();
        xsqlvars[0].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[0].sqllen = 31;
        xsqlvars[0].sqlname = "NAME";
        xsqlvars[0].relname = "UDT";

        xsqlvars[1] = new XSQLVAR();
        xsqlvars[1].sqltype = ISCConstants.SQL_LONG;
        xsqlvars[1].sqllen = 4;
        xsqlvars[1].sqlname = "MAX_LEN";
        xsqlvars[1].relname = "UDT";

        xsqlvars[2] = new XSQLVAR();
        xsqlvars[2].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[2].sqllen = 31;
        xsqlvars[2].sqlname = "DEFAULT";
        xsqlvars[2].relname = "UDT";

        xsqlvars[3] = new XSQLVAR();
        xsqlvars[3].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[3].sqllen = 31;
        xsqlvars[3].sqlname = "DESCRIPTION";
        xsqlvars[3].relname = "UDT";

        ArrayList rows = new ArrayList(0);

        return new FBResultSet(xsqlvars, rows);
    }

    /**
     * Retrieves a description of the given catalog's system or user 
     * function parameters and return type.
     *
     * <P>Only descriptions matching the schema,  function and
     * parameter name criteria are returned. They are ordered by
     * <code>FUNCTION_CAT</code>, <code>FUNCTION_SCHEM</code>,
     * <code>FUNCTION_NAME</code> and 
     * <code>SPECIFIC_ NAME</code>. Within this, the return value,
     * if any, is first. Next are the parameter descriptions in call
     * order. The column descriptions follow in column number order.
     *
     * <P>Each row in the <code>ResultSet</code> 
     * is a parameter description, column description or
     * return type description with the following fields:
     *  <OL>
     *  <LI><B>FUNCTION_CAT</B> String => function catalog (may be <code>null</code>)
     *  <LI><B>FUNCTION_SCHEM</B> String => function schema (may be <code>null</code>)
     *  <LI><B>FUNCTION_NAME</B> String => function name.  This is the name 
     * used to invoke the function
     *  <LI><B>COLUMN_NAME</B> String => column/parameter name 
     *  <LI><B>COLUMN_TYPE</B> Short => kind of column/parameter:
     *      <UL>
     *      <LI> functionColumnUnknown - nobody knows
     *      <LI> functionColumnIn - IN parameter
     *      <LI> functionColumnInOut - INOUT parameter
     *      <LI> functionColumnOut - OUT parameter
     *      <LI> functionColumnReturn - function return value
     *      <LI> functionColumnResult - Indicates that the parameter or column
     *  is a column in the <code>ResultSet</code>
     *      </UL>
     *  <LI><B>DATA_TYPE</B> int => SQL type from java.sql.Types
     *  <LI><B>TYPE_NAME</B> String => SQL type name, for a UDT type the
     *  type name is fully qualified
     *  <LI><B>PRECISION</B> int => precision
     *  <LI><B>LENGTH</B> int => length in bytes of data
     *  <LI><B>SCALE</B> short => scale -  null is returned for data types where  
     * SCALE is not applicable.
     *  <LI><B>RADIX</B> short => radix
     *  <LI><B>NULLABLE</B> short => can it contain NULL.
     *      <UL>
     *      <LI> functionNoNulls - does not allow NULL values
     *      <LI> functionNullable - allows NULL values
     *      <LI> functionNullableUnknown - nullability unknown
     *      </UL>
     *  <LI><B>REMARKS</B> String => comment describing column/parameter
     *  <LI><B>CHAR_OCTET_LENGTH</B> int  => the maximum length of binary 
     * and character based parameters or columns.  For any other datatype the returned value 
     * is a NULL
     *  <LI><B>ORDINAL_POSITION</B> int  => the ordinal position, starting 
     * from 1, for the input and output parameters. A value of 0
     * is returned if this row describes the function's return value. 
     * For result set columns, it is the
     * ordinal position of the column in the result set starting from 1.  
     *  <LI><B>IS_NULLABLE</B> String  => ISO rules are used to determine 
     * the nullability for a parameter or column.
     *       <UL>
     *       <LI> YES           --- if the parameter or column can include NULLs
     *       <LI> NO            --- if the parameter or column  cannot include NULLs
     *       <LI> empty string  --- if the nullability for the 
     * parameter  or column is unknown
     *       </UL>
     *  <LI><B>SPECIFIC_NAME</B> String  => the name which uniquely identifies 
     * this function within its schema.  This is a user specified, or DBMS
     * generated, name that may be different then the <code>FUNCTION_NAME</code> 
     * for example with overload functions
     *  </OL>
     * 
     * <p>The PRECISION column represents the specified column size for the given 
     * parameter or column. 
     * For numeric data, this is the maximum precision.  For character data, this is the length in characters. 
     * For datetime datatypes, this is the length in characters of the String representation (assuming the 
     * maximum allowed precision of the fractional seconds component). For binary data, this is the length in bytes.  For the ROWID datatype, 
     * this is the length in bytes. Null is returned for data types where the
     * column size is not applicable.
     * @param catalog a catalog name; must match the catalog name as it
     *        is stored in the database; "" retrieves those without a catalog;
     *        <code>null</code> means that the catalog name should not be used to narrow
     *        the search
     * @param schemaPattern a schema name pattern; must match the schema name
     *        as it is stored in the database; "" retrieves those without a schema;
     *        <code>null</code> means that the schema name should not be used to narrow
     *        the search
     * @param functionNamePattern a procedure name pattern; must match the
     *        function name as it is stored in the database 
     * @param columnNamePattern a parameter name pattern; must match the 
     * parameter or column name as it is stored in the database 
     * @return <code>ResultSet</code> - each row describes a 
     * user function parameter, column  or return type
     *
     * @exception SQLException if a database access error occurs
     * @see #getSearchStringEscape 
     * @since 1.6
     */
    public ResultSet getFunctionColumns(String catalog, String schemaPattern, String functionNamePattern, String columnNamePattern) throws SQLException {
        // FIXME implement this method
        throw new FBDriverNotCapableException();
    }

    /**
     * Retrieves a description of the  system and user functions available 
     * in the given catalog.
     * <P>
     * Only system and user function descriptions matching the schema and
     * function name criteria are returned.  They are ordered by
     * <code>FUNCTION_CAT</code>, <code>FUNCTION_SCHEM</code>,
     * <code>FUNCTION_NAME</code> and 
     * <code>SPECIFIC_ NAME</code>.
     *
     * <P>Each function description has the the following columns:
     *  <OL>
     *  <LI><B>FUNCTION_CAT</B> String => function catalog (may be <code>null</code>)
     *  <LI><B>FUNCTION_SCHEM</B> String => function schema (may be <code>null</code>)
     *  <LI><B>FUNCTION_NAME</B> String => function name.  This is the name 
     * used to invoke the function
     *  <LI><B>REMARKS</B> String => explanatory comment on the function
     * <LI><B>FUNCTION_TYPE</B> short => kind of function:
     *      <UL>
     *      <LI>functionResultUnknown - Cannot determine if a return value
     *       or table will be returned
     *      <LI> functionNoTable- Does not return a table
     *      <LI> functionReturnsTable - Returns a table
     *      </UL>
     *  <LI><B>SPECIFIC_NAME</B> String  => the name which uniquely identifies 
     *  this function within its schema.  This is a user specified, or DBMS
     * generated, name that may be different then the <code>FUNCTION_NAME</code> 
     * for example with overload functions
     *  </OL>
     * <p>
     * A user may not have permission to execute any of the functions that are
     * returned by <code>getFunctions</code>
     *
     * @param catalog a catalog name; must match the catalog name as it
     *        is stored in the database; "" retrieves those without a catalog;
     *        <code>null</code> means that the catalog name should not be used to narrow
     *        the search
     * @param schemaPattern a schema name pattern; must match the schema name
     *        as it is stored in the database; "" retrieves those without a schema;
     *        <code>null</code> means that the schema name should not be used to narrow
     *        the search
     * @param functionNamePattern a function name pattern; must match the
     *        function name as it is stored in the database 
     * @return <code>ResultSet</code> - each row is a function description 
     * @exception SQLException if a database access error occurs
     * @see #getSearchStringEscape 
     * @since 1.6
     */
    public ResultSet getFunctions(String catalog, String schemaPattern, String functionNamePattern) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    /**
     * Indicates whether or not this data source supports the SQL <code>ROWID</code> type,
     * and if so  the lifetime for which a <code>RowId</code> object remains valid. 
     * <p>
     * The returned int values have the following relationship: 
     * <pre>
     *     ROWID_UNSUPPORTED < ROWID_VALID_OTHER < ROWID_VALID_TRANSACTION
     *         < ROWID_VALID_SESSION < ROWID_VALID_FOREVER
     * </pre>
     * so conditional logic such as 
     * <pre>
     *     if (metadata.getRowIdLifetime() > DatabaseMetaData.ROWID_VALID_TRANSACTION)
     * </pre>
     * can be used. Valid Forever means valid across all Sessions, and valid for 
     * a Session means valid across all its contained Transactions. 
     *
     * @return the status indicating the lifetime of a <code>RowId</code>
     * @throws SQLException if a database access error occurs
     * @since 1.6
     */
    public RowIdLifetime getRowIdLifetime() throws SQLException {
        throw new FBDriverNotCapableException();
    }

    /**
     * Retrieves the schema names available in this database.  The results
     * are ordered by <code>TABLE_CATALOG</code> and 
     * <code>TABLE_SCHEM</code>.
     *
     * <P>The schema columns are:
     *  <OL>
     *  <LI><B>TABLE_SCHEM</B> String => schema name
     *  <LI><B>TABLE_CATALOG</B> String => catalog name (may be <code>null</code>)
     *  </OL>
     *
     *
     * @param catalog a catalog name; must match the catalog name as it is stored
     * in the database;"" retrieves those without a catalog; null means catalog
     * name should not be used to narrow down the search.
     * @param schemaPattern a schema name; must match the schema name as it is
     * stored in the database; null means
     * schema name should not be used to narrow down the search.
     * @return a <code>ResultSet</code> object in which each row is a
     *         schema description
     * @exception SQLException if a database access error occurs
     * @see #getSearchStringEscape 
     * @since 1.6
     */
    public ResultSet getSchemas(String catalog, String schemaPattern) throws SQLException {
        XSQLVAR[] xsqlvars = new XSQLVAR[2];

        xsqlvars[0] = new XSQLVAR();
        xsqlvars[0].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[0].sqllen = 31;
        xsqlvars[0].sqlname = "TABLE_SCHEM";
        xsqlvars[0].relname = "TABLESCHEMAS";
        
        xsqlvars[1] = new XSQLVAR();
        xsqlvars[1].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[1].sqllen = 31;
        xsqlvars[1].sqlname = "TABLE_CATALOG";
        xsqlvars[1].relname = "TABLESCHEMAS";

        ArrayList rows = new ArrayList(0);

        return new FBResultSet(xsqlvars, rows);
    }

    public boolean isWrapperFor(Class arg0) throws SQLException {
        return arg0 != null && arg0.isAssignableFrom(FBDatabaseMetaData.class);
    }

    public Object unwrap(Class arg0) throws SQLException {
        if (!isWrapperFor(arg0))
            throw new FBSQLException("No compatible class found.");
        
        return this;
    }
    
    
    

}
