/*   The contents of this file are subject to the Mozilla Public
 *   License Version 1.1 (the "License"); you may not use this file
 *   except in compliance with the License. You may obtain a copy of
 *   the License at http://www.mozilla.org/MPL/
 *   Alternatively, the contents of this file may be used under the
 *   terms of the GNU Lesser General Public License Version 2 or later (the
 *   "LGPL"), in which case the provisions of the GPL are applicable
 *   instead of those above. You may obtain a copy of the Licence at
 *   http://www.gnu.org/copyleft/lgpl.html
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    relevant License for more details.
 *
 *    This file was created by members of the firebird development team.
 *    All individual contributions remain the Copyright (C) of those
 *    individuals.  Contributors to this file are either listed here or
 *    can be obtained from a CVS history command.
 *
 *    All rights reserved.

 */

package org.firebirdsql.jdbc;


// imports --------------------------------------
import java.util.HashMap;
import java.util.Iterator;
import java.sql.DatabaseMetaData;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Types;
import javax.resource.spi.LocalTransaction;
import javax.resource.ResourceException;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import org.firebirdsql.gds.GDS;
import org.firebirdsql.gds.XSQLVAR;

import org.firebirdsql.logging.Logger;

/**
 *
 *   @see <related>
 *   @author David Jencks (davidjencks@earthlink.net)
 *   @version $ $
 */


/**
 * Comprehensive information about the database as a whole.
 *
 * <P>Many of the methods here return lists of information in
 * the form of <code>ResultSet</code> objects.
 * You can use the normal <code>ResultSet</code> methods such as getString and getInt
 * to retrieve the data from these <code>ResultSet</code>.  If a given form of
 * metadata is not available, these methods should throw an SQLException.
 *
 * <P>Some of these methods take arguments that are String patterns.  These
 * arguments all have names such as fooPattern.  Within a pattern String, "%"
 * means match any substring of 0 or more characters, and "_" means match
 * any one character. Only metadata entries matching the search pattern
 * are returned. If a search pattern argument is set to a null ref,
 * that argument's criteria will be dropped from the search.
 *
 * <P>An <code>SQLException</code> will be thrown if a driver does not support a meta
 * data method.  In the case of methods that return a <code>ResultSet</code>,
 * either a <code>ResultSet</code> (which may be empty) is returned or a
 * SQLException is thrown.
 */
public class FBDatabaseMetaData implements DatabaseMetaData {

   private final Logger log = Logger.getLogger(getClass());
    private static final String SPACES = "                               ";//31 spaces

    FBConnection c;

    //boolean transactionActive = false;

    LocalTransaction trans;

    HashMap statements = new HashMap();

    //PreparedStatement tables = null;

    FBDatabaseMetaData(FBConnection c) {
        this.c = c;
        //use the spi LocalTransaction that does not notify the ConnectionManager.
        trans = c.mc.getLocalTransaction();
    }

    void close() {
        try {
            Iterator i = statements.values().iterator();
            while(i.hasNext()) {
                ((PreparedStatement)i.next()).close();
            }
            statements.clear();
            /*if (tables != null) {
                tables.close();
                tables = null;
                }*/
        }
        catch (SQLException e) {
           log.warn("error in DatabaseMetaData.close", e);
        }
    }

    //----------------------------------------------------------------------
    // First, a variety of minor information about the target database.

    /**
     * Can all the procedures returned by getProcedures be called by the
     * current user?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean allProceduresAreCallable() throws SQLException {
        //returns all procedures whether or not you have execute permission
        return false;
    }


    /**
     * Can all the tables returned by getTable be SELECTed by the
     * current user?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean allTablesAreSelectable() throws SQLException {
        //returns all tables matching criteria independent of access permissions.
        return false;
    }


    /**
     * What's the url for this database?
     *
     * @return the url or null if it cannot be generated
     * @exception SQLException if a database access error occurs
     */
    public  String getURL() throws SQLException {
        return FBDriver.FIREBIRD_PROTOCOL + c.getDatabase();
    }


    /**
     * What's our user name as known to the database?
     *
     * @return our database user name
     * @exception SQLException if a database access error occurs
     */
    public  String getUserName() throws SQLException {
        return c.getUserName();
    }


    /**
     * Is the database in read-only mode?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean isReadOnly() throws SQLException {
        return false;//could be true, not yetimplemented
    }


    /**
     * Are NULL values sorted high?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean nullsAreSortedHigh() throws SQLException {
        //they always occur at end despite sort order
        return false;
    }


    /**
     * Are NULL values sorted low?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean nullsAreSortedLow() throws SQLException {
        //they always occur at end despite sort order
        return false;
    }


    /**
     * Are NULL values sorted at the start regardless of sort order?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean nullsAreSortedAtStart() throws SQLException {
        //they always occur at end despite sort order
        return false;
    }


    /**
     * Are NULL values sorted at the end regardless of sort order?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean nullsAreSortedAtEnd() throws SQLException {
        //they always occur at end despite sort order
        return true;
    }


    /**
     * What's the name of this database product?
     *
     * @return database product name
     * @exception SQLException if a database access error occurs
     */
    public  String getDatabaseProductName() throws SQLException {
        return "Firebird something == notyet impelemented";
        //throw new SQLException("Not yet implemented");
    }


    /**
     * What's the version of this database product?
     *
     * @return database version
     * @exception SQLException if a database access error occurs
     */
    public  String getDatabaseProductVersion() throws SQLException {
        return "Firebird something == notyet impelemented";
        //throw new SQLException("Not yet implemented");
    }


    /**
     * What's the name of this JDBC driver?
     *
     * @return JDBC driver name
     * @exception SQLException if a database access error occurs
     */
    public  String getDriverName() throws SQLException {
        return "firebirdsql jca/jdbc resource adapter";
    }


    /**
     * What's the version of this JDBC driver?
     *
     * @return JDBC driver version
     * @exception SQLException if a database access error occurs
     */
    public  String getDriverVersion() throws SQLException {
        return "0.1";
    }


    /**
     * What's this JDBC driver's major version number?
     *
     * @return JDBC driver major version
     */
    public  int getDriverMajorVersion() {
        return 0;
    }


    /**
     * What's this JDBC driver's minor version number?
     *
     * @return JDBC driver minor version number
     */
    public  int getDriverMinorVersion() {
        return 1;
    }


    /**
     * Does the database store tables in a local file?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean usesLocalFiles() throws SQLException {
        return false;
    }


    /**
     * Does the database use a file for each table?
     *
     * @return true if the database uses a local file for each table
     * @exception SQLException if a database access error occurs
     */
    public  boolean usesLocalFilePerTable() throws SQLException {
        return false;
    }


    /**
     * Does the database treat mixed case unquoted SQL identifiers as
     * case sensitive and as a result store them in mixed case?
     *
     * A JDBC Compliant<sup><font size=-2>TM</font></sup> driver will always return false.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsMixedCaseIdentifiers() throws SQLException {
        return false;
    }

    /**
     * 
     * @return a <code>boolean</code> value
     * @exception SQLException if an error occurs
     * @todo implement statemet pooling on the server.. then in the driver
     */
    public boolean supportsStatementPooling() throws SQLException {
        return false;
    }

    /**
     * Describe <code>locatorsUpdateCopy</code> method here.
     *
     * @return a <code>boolean</code> value
     * @exception SQLException if an error occurs
     * @todo find out what this even means
     */
    public boolean locatorsUpdateCopy() throws SQLException {
        return false;
    }


    /**
     * Does the database treat mixed case unquoted SQL identifiers as
     * case insensitive and store them in upper case?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean storesUpperCaseIdentifiers() throws SQLException {
        return true;
    }


    /**
     * Does the database treat mixed case unquoted SQL identifiers as
     * case insensitive and store them in lower case?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean storesLowerCaseIdentifiers() throws SQLException {
        return false;
    }


    /**
     * Does the database treat mixed case unquoted SQL identifiers as
     * case insensitive and store them in mixed case?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean storesMixedCaseIdentifiers() throws SQLException {
        return false;
    }


    /**
     * Does the database treat mixed case quoted SQL identifiers as
     * case sensitive and as a result store them in mixed case?
     *
     * A JDBC Compliant<sup><font size=-2>TM</font></sup> driver will always return true.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
        return true;
    }


    /**
     * Does the database treat mixed case quoted SQL identifiers as
     * case insensitive and store them in upper case?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
        return false;
    }


    /**
     * Does the database treat mixed case quoted SQL identifiers as
     * case insensitive and store them in lower case?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
        return false;
    }


    /**
     * Does the database treat mixed case quoted SQL identifiers as
     * case insensitive and store them in mixed case?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
        return false;
    }


    /**
     * What's the string used to quote SQL identifiers?
     * This returns a space " " if identifier quoting isn't supported.
     *
     * A JDBC Compliant<sup><font size=-2>TM</font></sup>
     * driver always uses a double quote character.
     *
     * @return the quoting string
     * @exception SQLException if a database access error occurs
     */
    public  String getIdentifierQuoteString() throws SQLException {
        return "\"";
    }


    /**
     * Gets a comma-separated list of all a database's SQL keywords
     * that are NOT also SQL92 keywords.
     *
     * @return the list
     * @exception SQLException if a database access error occurs
     */
    public  String getSQLKeywords() throws SQLException {
        return "NOT YET IMPLEMENTED";
        //throw new SQLException("Not yet implemented");
    }


    /**
     * Gets a comma-separated list of math functions.  These are the
     * X/Open CLI math function names used in the JDBC function escape
     * clause.
     *
     * @return the list
     * @exception SQLException if a database access error occurs
     */
    public  String getNumericFunctions() throws SQLException {
        return "";//udf's
        //throw new SQLException("Not yet implemented");
    }


    /**
     * Gets a comma-separated list of string functions.  These are the
     * X/Open CLI string function names used in the JDBC function escape
     * clause.
     *
     * @return the list
     * @exception SQLException if a database access error occurs
     */
    public  String getStringFunctions() throws SQLException {
        return "SUBSTRING";
        //throw new SQLException("Not yet implemented");
    }


    /**
     * Gets a comma-separated list of system functions.  These are the
     * X/Open CLI system function names used in the JDBC function escape
     * clause.
     *
     * @return the list
     * @exception SQLException if a database access error occurs
     */
    public  String getSystemFunctions() throws SQLException {
        return "";
        //throw new SQLException("Not yet implemented");
    }


    /**
     * Gets a comma-separated list of time and date functions.
     *
     * @return the list
     * @exception SQLException if a database access error occurs
     */
    public  String getTimeDateFunctions() throws SQLException {
        return "NOT,YET,IMPLEMENTED";
        //throw new SQLException("Not yet implemented");
    }


    /**
     * Gets the string that can be used to escape wildcard characters.
     * This is the string that can be used to escape '_' or '%' in
     * the string pattern style catalog search parameters.
     *
     * <P>The '_' character represents any single character.
     * <P>The '%' character represents any sequence of zero or
     * more characters.
     *
     * @return the string used to escape wildcard characters
     * @exception SQLException if a database access error occurs
     */
    public  String getSearchStringEscape() throws SQLException {
        return "\\";
    }


    /**
     * Gets all the "extra" characters that can be used in unquoted
     * identifier names (those beyond a-z, A-Z, 0-9 and _).
     *
     * @return the string containing the extra characters
     * @exception SQLException if a database access error occurs
     */
    public  String getExtraNameCharacters() throws SQLException {
        return "$";
    }


    //--------------------------------------------------------------------
    // Functions describing which features are supported.

    /**
     * Is "ALTER TABLE" with add column supported?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsAlterTableWithAddColumn() throws SQLException {
        return true;
    }


    /**
     * Is "ALTER TABLE" with drop column supported?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsAlterTableWithDropColumn() throws SQLException {
        return true;
    }


    /**
     * Is column aliasing supported?
     *
     * <P>If so, the SQL AS clause can be used to provide names for
     * computed columns or to provide alias names for columns as
     * required.
     *
     * A JDBC Compliant<sup><font size=-2>TM</font></sup> driver always returns true.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsColumnAliasing() throws SQLException {
        return true;
    }


    /**
     * Are concatenations between NULL and non-NULL values NULL?
     * For SQL-92 compliance, a JDBC technology-enabled driver will
     * return <code>true</code>.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean nullPlusNonNullIsNull() throws SQLException {
        return true;//I didn't check (rrokytskyy: checked, true for FB 1.0 RC2)
    }


    /**
     * Is the CONVERT function between SQL types supported?
     *
     * What is this??? my sql ref says CONVERT is a string encoding map!
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsConvert() throws SQLException {
        return false;//don't know
        //throw new SQLException("Not yet implemented");
    }


    /**
     * Is CONVERT between the given SQL types supported?
     *
     * @param fromType the type to convert from
     * @param toType the type to convert to
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @see Types
     */
    public  boolean supportsConvert(int fromType, int toType) throws SQLException {
        return false;//don't know
        //throw new SQLException("Not yet implemented");
    }


    /**
     * Are table correlation names supported?
     *
     * A JDBC Compliant<sup><font size=-2>TM</font></sup> driver always returns true.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsTableCorrelationNames() throws SQLException {
        return true;
    }


    /**
     * If table correlation names are supported, are they restricted
     * to be different from the names of the tables?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsDifferentTableCorrelationNames() throws SQLException {
        return false; //I think
    }


    /**
     * Are expressions in "ORDER BY" lists supported?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsExpressionsInOrderBy() throws SQLException {
        return false; //coming soon
    }


    /**
     * Can an "ORDER BY" clause use columns not in the SELECT statement?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsOrderByUnrelated() throws SQLException {
        return true;//I'm not sure on this one, (rrokytskyy: works on FB 1.0 RC2)
    }


    /**
     * Is some form of "GROUP BY" clause supported?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsGroupBy() throws SQLException {
        return true;
    }


    /**
     * Can a "GROUP BY" clause use columns not in the SELECT?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsGroupByUnrelated() throws SQLException {
        return false;
        //throw new SQLException("Not yet implemented");//don't know
    }


    /**
     * Can a "GROUP BY" clause add columns not in the SELECT
     * provided it specifies all the columns in the SELECT?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsGroupByBeyondSelect() throws SQLException {
        return false;
        //throw new SQLException("Not yet implemented");//dont know
    }


    /**
     * Is the escape character in "LIKE" clauses supported?
     *
     * A JDBC Compliant<sup><font size=-2>TM</font></sup> driver always returns true.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsLikeEscapeClause() throws SQLException {
        return true;
    }


    /**
     * Are multiple <code>ResultSet</code> from a single execute supported?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsMultipleResultSets() throws SQLException {
        return false;
    }


    /**
     * Can we have multiple transactions open at once (on different
     * connections)?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsMultipleTransactions() throws SQLException {
        return true;
    }


    /**
     * Can columns be defined as non-nullable?
     *
     * A JDBC Compliant<sup><font size=-2>TM</font></sup> driver always returns true.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsNonNullableColumns() throws SQLException {
        return true;
    }


    /**
     * Is the ODBC Minimum SQL grammar supported?
     *
     * All JDBC Compliant<sup><font size=-2>TM</font></sup> drivers must return true.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsMinimumSQLGrammar() throws SQLException {
        return true; //lets see what the tests say
    }


    /**
     * Is the ODBC Core SQL grammar supported?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsCoreSQLGrammar() throws SQLException {
        return true; //lets see what the tests say
    }


    /**
     * Is the ODBC Extended SQL grammar supported?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsExtendedSQLGrammar() throws SQLException {
        return true; //lets see what the tests say
    }


    /**
     * Is the ANSI92 entry level SQL grammar supported?
     *
     * All JDBC Compliant<sup><font size=-2>TM</font></sup> drivers must return true.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsANSI92EntryLevelSQL() throws SQLException {
        return true; //lets see what the tests say
    }


    /**
     * Is the ANSI92 intermediate SQL grammar supported?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsANSI92IntermediateSQL() throws SQLException {
        return true; //lets see what the tests say
    }


    /**
     * Is the ANSI92 full SQL grammar supported?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsANSI92FullSQL() throws SQLException {
        return true; //Nah, but lets see what the tests say
    }


    /**
     * Is the SQL Integrity Enhancement Facility supported?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsIntegrityEnhancementFacility() throws SQLException {
        //throw new SQLException("Not yet implemented");//wtf is this? referential integrity??
        return true; // rrokytskyy: yep, they call so foreign keys + cascade deletes
    }


    /**
     * Is some form of outer join supported?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsOuterJoins() throws SQLException {
        return true;
    }


    /**
     * Are full nested outer joins supported?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsFullOuterJoins() throws SQLException {
        return true;//lets see what the tests say
    }


    /**
     * Is there limited support for outer joins?  (This will be true
     * if supportFullOuterJoins is true.)
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsLimitedOuterJoins() throws SQLException {
        return true;
    }


    /**
     * What's the database vendor's preferred term for "schema"?
     *
     * @return the vendor term
     * @exception SQLException if a database access error occurs
     */
    public  String getSchemaTerm() throws  SQLException {
        return "";
        //throw new SQLException("No schemas");
    }


    /**
     * What's the database vendor's preferred term for "procedure"?
     *
     * @return the vendor term
     * @exception SQLException if a database access error occurs
     */
    public  String getProcedureTerm() throws SQLException {
        return "PROCEDURE";
    }


    /**
     * What's the database vendor's preferred term for "catalog"?
     *
     * @return the vendor term
     * @exception SQLException if a database access error occurs
     */
    public  String getCatalogTerm() throws  SQLException {
        return "";
        //throw new SQLException("No Catalogs");
    }


    /**
     * Does a catalog appear at the start of a qualified table name?
     * (Otherwise it appears at the end)
     *
     * @return true if it appears at the start
     * @exception SQLException if a database access error occurs
     */
    public  boolean isCatalogAtStart() throws SQLException {
        return false;
    }


    /**
     * What's the separator between catalog and table name?
     *
     * @return the separator string
     * @exception SQLException if a database access error occurs
     */
    public  String getCatalogSeparator() throws SQLException {
        return "";
        //throw new SQLException("No catalogs");
    }


    /**
     * Can a schema name be used in a data manipulation statement?
     *
    public * @return <code>true</code> if so {
        throw new SQLException("Not yet implemented");
    }
 <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsSchemasInDataManipulation() throws SQLException {
        return false;
    }


    /**
     * Can a schema name be used in a procedure call statement?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsSchemasInProcedureCalls() throws SQLException {
        return false;
    }


    /**
     * Can a schema name be used in a table definition statement?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsSchemasInTableDefinitions() throws SQLException {
        return false;
    }


    /**
     * Can a schema name be used in an index definition statement?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsSchemasInIndexDefinitions() throws SQLException {
        return false;
    }


    /**
     * Can a schema name be used in a privilege definition statement?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
        return false;
    }


    /**
     * Can a catalog name be used in a data manipulation statement?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsCatalogsInDataManipulation() throws SQLException {
        return false;
    }


    /**
     * Can a catalog name be used in a procedure call statement?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsCatalogsInProcedureCalls() throws SQLException {
        return false;
    }


    /**
     * Can a catalog name be used in a table definition statement?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsCatalogsInTableDefinitions() throws SQLException {
        return false;
    }


    /**
     * Can a catalog name be used in an index definition statement?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsCatalogsInIndexDefinitions() throws SQLException {
        return false;
    }


    /**
     * Can a catalog name be used in a privilege definition statement?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
        return false;
    }



    /**
     * Is positioned DELETE supported?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsPositionedDelete() throws SQLException {
        return true;
    }


    /**
     * Is positioned UPDATE supported?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsPositionedUpdate() throws SQLException {
        return true;
    }


    /**
     * Is SELECT for UPDATE supported?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsSelectForUpdate() throws SQLException {
        return true;
    }


    /**
     * Are stored procedure calls using the stored procedure escape
     * syntax supported?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsStoredProcedures() throws SQLException {
        return true;
    }


    /**
     * Are subqueries in comparison expressions supported?
     *
     * A JDBC Compliant<sup><font size=-2>TM</font></sup> driver always returns true.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsSubqueriesInComparisons() throws SQLException {
        return true;
    }


    /**
     * Are subqueries in 'exists' expressions supported?
     *
     * A JDBC Compliant<sup><font size=-2>TM</font></sup> driver always returns true.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsSubqueriesInExists() throws SQLException {
        return true;
    }


    /**
     * Are subqueries in 'in' statements supported?
     *
     * A JDBC Compliant<sup><font size=-2>TM</font></sup> driver always returns true.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsSubqueriesInIns() throws SQLException {
        return true;
    }


    /**
     * Are subqueries in quantified expressions supported?
     *
     * A JDBC Compliant<sup><font size=-2>TM</font></sup> driver always returns true.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsSubqueriesInQuantifieds() throws SQLException {
        return true;
    }


    /**
     * Are correlated subqueries supported?
     *
     * A JDBC Compliant<sup><font size=-2>TM</font></sup> driver always returns true.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsCorrelatedSubqueries() throws SQLException {
        return true;
    }


    /**
     * Is SQL UNION supported?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsUnion() throws SQLException {
        return true;
    }


    /**
     * Is SQL UNION ALL supported?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsUnionAll() throws SQLException {
        return false;//I think
    }


    /**
     * Can cursors remain open across commits?
     *
     * @return <code>true</code> if cursors always remain open;
     *       <code>false</code> if they might not remain open
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsOpenCursorsAcrossCommit() throws SQLException {
        return false;//only when commit retaining is executed I think
    }


    /**
     * Can cursors remain open across rollbacks?
     *
     * @return <code>true</code> if cursors always remain open;
     *       <code>false</code> if they might not remain open
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsOpenCursorsAcrossRollback() throws SQLException {
        return false;//commit retaining only.
    }


    /**
     * Can statements remain open across commits?
     *
     * @return <code>true</code> if statements always remain open;
     *       <code>false</code> if they might not remain open
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsOpenStatementsAcrossCommit() throws SQLException {
        return false;//commit retaining only.
    }


    /**
     * Can statements remain open across rollbacks?
     *
     * @return <code>true</code> if statements always remain open;
     *       <code>false</code> if they might not remain open
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsOpenStatementsAcrossRollback() throws SQLException {
        return false;//commit retaining only.
    }




    //----------------------------------------------------------------------
    // The following group of methods exposes various limitations
    // based on the target database with the current driver.
    // Unless otherwise specified, a result of zero means there is no
    // limit, or the limit is not known.

    /**
     * How many hex characters can you have in an inline binary literal?
     *
     * @return max binary literal length in hex characters;
     *      a result of zero means that there is no limit or the limit is not known
     * @exception SQLException if a database access error occurs
     */
    public  int getMaxBinaryLiteralLength() throws SQLException {
        return 0;//anyone know for sure?
    }



    /**
     * What's the max length for a character literal?
     *
     * @return max literal length;
     *      a result of zero means that there is no limit or the limit is not known
     * @exception SQLException if a database access error occurs
     */
    public  int getMaxCharLiteralLength() throws SQLException {
        return 0;
    }



    /**
     * What's the limit on column name length?
     *
     * @return max column name length;
     *      a result of zero means that there is no limit or the limit is not known
     * @exception SQLException if a database access error occurs
     */
    public  int getMaxColumnNameLength() throws SQLException {
        return 31;//I think
    }



    /**
     * What's the maximum number of columns in a "GROUP BY" clause?
     *
     * @return max number of columns;
     *      a result of zero means that there is no limit or the limit is not known
     * @exception SQLException if a database access error occurs
     */
    public  int getMaxColumnsInGroupBy() throws SQLException {
        return 0; //I don't know
    }



    /**
     * What's the maximum number of columns allowed in an index?
     *
     * @return max number of columns;
     *      a result of zero means that there is no limit or the limit is not known
     * @exception SQLException if a database access error occurs
     */
    public  int getMaxColumnsInIndex() throws SQLException {
        return 0; //I don't know
    }



    /**
     * What's the maximum number of columns in an "ORDER BY" clause?
     *
     * @return max number of columns;
     *      a result of zero means that there is no limit or the limit is not known
     * @exception SQLException if a database access error occurs
     */
    public  int getMaxColumnsInOrderBy() throws SQLException {
        return 0; //I don't know
    }



    /**
     * What's the maximum number of columns in a "SELECT" list?
     *
     * @return max number of columns;
     *      a result of zero means that there is no limit or the limit is not known
     * @exception SQLException if a database access error occurs
     */
    public  int getMaxColumnsInSelect() throws SQLException {
        return 0; //I don't know
    }



    /**
     * What's the maximum number of columns in a table?
     *
     * @return max number of columns;
     *      a result of zero means that there is no limit or the limit is not known
     * @exception SQLException if a database access error occurs
     */
    public  int getMaxColumnsInTable() throws SQLException {
        return 0; //I don't know
    }



    /**
     * How many active connections can we have at a time to this database?
     *
     * @return max number of active connections;
     *      a result of zero means that there is no limit or the limit is not known
     * @exception SQLException if a database access error occurs
     */
    public  int getMaxConnections() throws SQLException {
        return 0; //I don't know
    }



    /**
     * What's the maximum cursor name length?
     *
     * @return max cursor name length in bytes;
     *      a result of zero means that there is no limit or the limit is not known
     * @exception SQLException if a database access error occurs
     */
    public  int getMaxCursorNameLength() throws SQLException {
        return 31;
    }



    /**
     * Retrieves the maximum number of bytes for an index, including all
     * of the parts of the index.
     *
     * @return max index length in bytes, which includes the composite of all
     *      the constituent parts of the index;
     *      a result of zero means that there is no limit or the limit is not known
     * @exception SQLException if a database access error occurs
     */
    public  int getMaxIndexLength() throws SQLException {
        return 0; //I don't know
    }



    /**
     * What's the maximum length allowed for a schema name?
     *
     * @return max name length in bytes;
     *      a result of zero means that there is no limit or the limit is not known
     * @exception SQLException if a database access error occurs
     */
    public  int getMaxSchemaNameLength() throws SQLException {
        return 0; //No schemas
    }



    /**
     * What's the maximum length of a procedure name?
     *
     * @return max name length in bytes;
     *      a result of zero means that there is no limit or the limit is not known
     * @exception SQLException if a database access error occurs
     */
    public  int getMaxProcedureNameLength() throws SQLException {
        return 31;
    }



    /**
     * What's the maximum length of a catalog name?
     *
     * @return max name length in bytes;
     *      a result of zero means that there is no limit or the limit is not known
     * @exception SQLException if a database access error occurs
     */
    public  int getMaxCatalogNameLength() throws SQLException {
        return 0; //No catalogs
    }



    /**
     * What's the maximum length of a single row?
     *
     * @return max row size in bytes;
     *      a result of zero means that there is no limit or the limit is not known
     * @exception SQLException if a database access error occurs
     */
    public  int getMaxRowSize() throws SQLException {
        return 0;
    }



    /**
     * Did getMaxRowSize() include LONGVARCHAR and LONGVARBINARY
     * blobs?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
        return true;
    }



    /**
     * What's the maximum length of an SQL statement?
     *
     * @return max length in bytes;
     *      a result of zero means that there is no limit or the limit is not known
     * @exception SQLException if a database access error occurs
     */
    public  int getMaxStatementLength() throws SQLException {
        return 0;
    }



    /**
     * How many active statements can we have open at one time to this
     * database?
     *
     * @return the maximum number of statements that can be open at one time;
     *      a result of zero means that there is no limit or the limit is not known
     * @exception SQLException if a database access error occurs
     */
    public  int getMaxStatements() throws SQLException {
        return 0;
    }



    /**
     * What's the maximum length of a table name?
     *
     * @return max name length in bytes;
     *      a result of zero means that there is no limit or the limit is not known
     * @exception SQLException if a database access error occurs
     */
    public  int getMaxTableNameLength() throws SQLException {
        return 31;
    }



    /**
     * What's the maximum number of tables in a SELECT statement?
     *
     * @return the maximum number of tables allowed in a SELECT statement;
     *      a result of zero means that there is no limit or the limit is not known
     * @exception SQLException if a database access error occurs
     */
    public  int getMaxTablesInSelect() throws SQLException {
        return 0;
    }



    /**
     * What's the maximum length of a user name?
     *
     * @return max user name length  in bytes;
     *      a result of zero means that there is no limit or the limit is not known
     * @exception SQLException if a database access error occurs
     */
    public  int getMaxUserNameLength() throws SQLException {
        return 31;//I don't know??
    }



    //----------------------------------------------------------------------

    /**
     * What's the database's default transaction isolation level?  The
     * values are defined in <code>java.sql.Connection</code>.
     *
     * @return the default isolation level
     * @exception SQLException if a database access error occurs
     * @see Connection
     */
    public  int getDefaultTransactionIsolation() throws SQLException {
        return Connection.TRANSACTION_REPEATABLE_READ;//close enough to snapshot.
    }



    /**
     * Are transactions supported? If not, invoking the method
     * <code>commit</code> is a noop and the
     * isolation level is TRANSACTION_NONE.
     *
     * @return <code>true</code> if transactions are supported; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsTransactions() throws SQLException {
        return true;
    }



    /**
     * Does this database support the given transaction isolation level?
     *
     * @param level the values are defined in <code>java.sql.Connection</code>
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @see Connection
     */
    public boolean supportsTransactionIsolationLevel(int level) throws SQLException {
        switch (level) {
            case Connection.TRANSACTION_NONE: return false;
            case Connection.TRANSACTION_READ_COMMITTED: return true;//true soon
            case Connection.TRANSACTION_READ_UNCOMMITTED: return false;
            case Connection.TRANSACTION_REPEATABLE_READ: return true;//??
            case Connection.TRANSACTION_SERIALIZABLE: return true;//????
            default: return false;
        }

    }



    /**
     * Are both data definition and data manipulation statements
     * within a transaction supported?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException {
        return true;//but not on the tables you defined in the transaction!
    }


    /**
     * Are only data manipulation statements within a transaction
     * supported?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsDataManipulationTransactionsOnly() throws SQLException {
        return false;
    }


    /**
     * Does a data definition statement within a transaction force the
     * transaction to commit?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
        return false;//but you can't use the table till the transaction is committed.
    }


    /**
     * Is a data definition statement within a transaction ignored?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
        return false;
    }

    private static final String GET_PROCEDURES_START = "select null as PROCEDURE_CAT,"
        + " null as PROCEDURE_SCHEM,"
        + " RDB$PROCEDURE_NAME as PROCEDURE_NAME,"
        + " null as FUTURE1,"
        + " null as FUTURE2,"
        + " null as FUTURE3,"
        + " RDB$DESCRIPTION as REMARKS,"
        + " RDB$PROCEDURE_OUTPUTS as PROCEDURE_TYPE,"
        + " RDB$OWNER_NAME "
        + "from"
        + " RDB$PROCEDURES "
        + "where ";
    private static final String GET_PROCEDURES_END = "1 = 1 order by 1";



    /**
     * Gets a description of the stored procedures available in a
     * catalog.
     *
     * <P>Only procedure descriptions matching the schema and
     * procedure name criteria are returned.  They are ordered by
     * PROCEDURE_SCHEM, and PROCEDURE_NAME.
     *
     * <P>Each procedure description has the the following columns:
     *  <OL>
     *  <LI><B>PROCEDURE_CAT</B> String => procedure catalog (may be null)
     *  <LI><B>PROCEDURE_SCHEM</B> String => procedure schema (may be null)
     *  <LI><B>PROCEDURE_NAME</B> String => procedure name
     *  <LI> reserved for future use
     *  <LI> reserved for future use
     *  <LI> reserved for future use
     *  <LI><B>REMARKS</B> String => explanatory comment on the procedure
     *  <LI><B>PROCEDURE_TYPE</B> short => kind of procedure:
     *      <UL>
     *      <LI> procedureResultUnknown - May return a result
     *      <LI> procedureNoResult - Does not return a result
     *      <LI> procedureReturnsResult - Returns a result
     *      </UL>
     *  </OL>
     *
     * @param catalog a catalog name; "" retrieves those without a
     * catalog; null means drop catalog name from the selection criteria
     * @param schemaPattern a schema name pattern; "" retrieves those
     * without a schema
     * @param procedureNamePattern a procedure name pattern
     * @return <code>ResultSet</code> - each row is a procedure description
     * @exception SQLException if a database access error occurs
     * @see #getSearchStringEscape
     */
    public ResultSet getProcedures(String catalog, String schemaPattern,
            String procedureNamePattern) throws SQLException {
        checkCatalogAndSchema(catalog, schemaPattern);
        Clause procedureClause = new Clause("RDB$PROCEDURE_NAME", procedureNamePattern);
        String sql = GET_PROCEDURES_START;
        sql += procedureClause.getCondition();
        sql += GET_PROCEDURES_END;
        ArrayList params = new ArrayList();
        if (!procedureClause.getCondition().equals("")) {
            params.add(procedureClause.getValue());
        }
        return doQuery(sql, params);
    }



    /**
     * A possible value for column <code>PROCEDURE_TYPE</code> in the
     * <code>ResultSet</code> object returned by the method
     * <code>getProcedures</code>.
     * <p> Indicates that it is not known whether the procedure returns
     * a result.
     */
    //int procedureResultUnknown    = 0;

    /**
     * A possible value for column <code>PROCEDURE_TYPE</code> in the
     * <code>ResultSet</code> object returned by the method
     * <code>getProcedures</code>.
     * <p> Indicates that the procedure does not return
     * a result.
     */
    //int procedureNoResult     = 1;

    /**
     * A possible value for column <code>PROCEDURE_TYPE</code> in the
     * <code>ResultSet</code> object returned by the method
     * <code>getProcedures</code>.
     * <p> Indicates that the procedure returns
     * a result.
     */
    //int procedureReturnsResult    = 2;

    private static final String GET_PROCEDURE_COLUMNS_START = "select"
        + " null as PROCEDURE_CAT,"
        + " null as PROCEDURE_SCHEM,"
        + " P.RDB$PROCEDURE_NAME as PROCEDURE_NAME,"
        + " PP.RDB$PARAMETER_NAME as COLUMN_NAME,"
        + " PP.RDB$PARAMETER_TYPE as COLUMN_TYPE,"
        + " F.RDB$FIELD_TYPE as DATA_TYPE,"
        + " F.RDB$FIELD_SUB_TYPE as TYPE_NAME," //unlikely
        + " 0 as \"PRECISION\","//Hah!
        + " F.RDB$FIELD_SCALE as SCALE,"
        + " F.RDB$FIELD_LENGTH as \"LENGTH\","
        + " 10 as RADIX,"//who knows??
        + " F.RDB$NULL_FLAG as NULLABLE,"
        + " PP.RDB$DESCRIPTION as REMARKS "
        + "from"
        + " RDB$PROCEDURES P,"
        + " RDB$PROCEDURE_PARAMETERS PP,"
        + " RDB$FIELDS F "
        + "where ";
    private static final String GET_PROCEDURE_COLUMNS_END =  " P.RDB$PROCEDURE_NAME = PP.RDB$PROCEDURE_NAME and"
        + " PP.RDB$FIELD_SOURCE = F.RDB$FIELD_NAME "
        + "order by"
        + " P.RDB$PROCEDURE_NAME,"
        + " PP.RDB$PARAMETER_TYPE desc,"
        + " PP.RDB$PARAMETER_NUMBER";


    /**
     * Gets a description of a catalog's stored procedure parameters
     * and result columns.
     *
     * <P>Only descriptions matching the schema, procedure and
     * parameter name criteria are returned.  They are ordered by
     * PROCEDURE_SCHEM and PROCEDURE_NAME. Within this, the return value,
     * if any, is first. Next are the parameter descriptions in call
     * order. The column descriptions follow in column number order.
     *
     * <P>Each row in the <code>ResultSet</code> is a parameter description or
     * column description with the following fields:
     *  <OL>
     *  <LI><B>PROCEDURE_CAT</B> String => procedure catalog (may be null)
     *  <LI><B>PROCEDURE_SCHEM</B> String => procedure schema (may be null)
     *  <LI><B>PROCEDURE_NAME</B> String => procedure name
     *  <LI><B>COLUMN_NAME</B> String => column/parameter name
     *  <LI><B>COLUMN_TYPE</B> Short => kind of column/parameter:
     *      <UL>
     *      <LI> procedureColumnUnknown - nobody knows
     *      <LI> procedureColumnIn - IN parameter
     *      <LI> procedureColumnInOut - INOUT parameter
     *      <LI> procedureColumnOut - OUT parameter
     *      <LI> procedureColumnReturn - procedure return value
     *      <LI> procedureColumnResult - result column in <code>ResultSet</code>
     *      </UL>
     *  <LI><B>DATA_TYPE</B> short => SQL type from java.sql.Types
     *  <LI><B>TYPE_NAME</B> String => SQL type name, for a UDT type the
     *  type name is fully qualified
     *  <LI><B>PRECISION</B> int => precision
     *  <LI><B>LENGTH</B> int => length in bytes of data
     *  <LI><B>SCALE</B> short => scale
     *  <LI><B>RADIX</B> short => radix
     *  <LI><B>NULLABLE</B> short => can it contain NULL?
     *      <UL>
     *      <LI> procedureNoNulls - does not allow NULL values
     *      <LI> procedureNullable - allows NULL values
     *      <LI> procedureNullableUnknown - nullability unknown
     *      </UL>
     *  <LI><B>REMARKS</B> String => comment describing parameter/column
     *  </OL>
     *
     * <P><B>Note:</B> Some databases may not return the column
     * descriptions for a procedure. Additional columns beyond
     * REMARKS can be defined by the database.
     *
     * @param catalog a catalog name; "" retrieves those without a
     * catalog; null means drop catalog name from the selection criteria
     * @param schemaPattern a schema name pattern; "" retrieves those
     * without a schema
     * @param procedureNamePattern a procedure name pattern
     * @param columnNamePattern a column name pattern
     * @return <code>ResultSet</code> - each row describes a stored procedure parameter or
     *      column
     * @exception SQLException if a database access error occurs
     * @see #getSearchStringEscape
     */
    public ResultSet getProcedureColumns(String catalog,
            String schemaPattern,
            String procedureNamePattern,
            String columnNamePattern) throws SQLException {
        checkCatalogAndSchema(catalog, schemaPattern);
        Clause procedureClause = new Clause("P.RDB$PROCEDURE_NAME", procedureNamePattern);
        Clause columnClause = new Clause("PP.RDB$PARAMETER_NAME", columnNamePattern);
        String sql = GET_PROCEDURE_COLUMNS_START;
        sql += procedureClause.getCondition();
        sql += columnClause.getCondition();
        sql += GET_PROCEDURES_END;
        ArrayList params = new ArrayList();
        if (!procedureClause.getCondition().equals("")) {
            params.add(procedureClause.getValue());
        }
        if (!columnClause.getCondition().equals("")) {
            params.add(columnClause.getValue());
        }
        return doQuery(sql, params);
    }



    /**
     * Indicates that type of the column is unknown.
     * A possible value for the column
     * <code>COLUMN_TYPE</code>
     * in the <code>ResultSet</code>
     * returned by the method <code>getProcedureColumns</code>.
     */
    //int procedureColumnUnknown = 0;

    /**
     * Indicates that the column stores IN parameters.
     * A possible value for the column
     * <code>COLUMN_TYPE</code>
     * in the <code>ResultSet</code>
     * returned by the method <code>getProcedureColumns</code>.
     */
    //int procedureColumnIn = 1;

    /**
     * Indicates that the column stores INOUT parameters.
     * A possible value for the column
     * <code>COLUMN_TYPE</code>
     * in the <code>ResultSet</code>
     * returned by the method <code>getProcedureColumns</code>.
     */
    //int procedureColumnInOut = 2;

    /**
     * Indicates that the column stores OUT parameters.
     * A possible value for the column
     * <code>COLUMN_TYPE</code>
     * in the <code>ResultSet</code>
     * returned by the method <code>getProcedureColumns</code>.
     */
    //int procedureColumnOut = 4;
    /**
     * Indicates that the column stores return values.
     * A possible value for the column
     * <code>COLUMN_TYPE</code>
     * in the <code>ResultSet</code>
     * returned by the method <code>getProcedureColumns</code>.
     */
    //int procedureColumnReturn = 5;

    /**
     * Indicates that the column stores results.
     * A possible value for the column
     * <code>COLUMN_TYPE</code>
     * in the <code>ResultSet</code>
     * returned by the method <code>getProcedureColumns</code>.
     */
    //int procedureColumnResult = 3;

    /**
     * Indicates that <code>NULL</code> values are not allowed.
     * A possible value for the column
     * <code>NULLABLE</code>
     * in the <code>ResultSet</code>
     * returned by the method <code>getProcedureColumns</code>.
     */
    //int procedureNoNulls = 0;

    /**
     * Indicates that <code>NULL</code> values are allowed.
     * A possible value for the column
     * <code>NULLABLE</code>
     * in the <code>ResultSet</code>
     * returned by the method <code>getProcedureColumns</code>.
     */
    //int procedureNullable = 1;

    /**
     * Indicates that whether <code>NULL</code> values are allowed
     * is unknown.
     * A possible value for the column
     * <code>NULLABLE</code>
     * in the <code>ResultSet</code>
     * returned by the method <code>getProcedureColumns</code>.
     */
    //int procedureNullableUnknown = 2;

    public static final String TABLE = "TABLE";
    public static final String SYSTEM_TABLE = "SYSTEM TABLE";
    public static final String VIEW = "VIEW";
    public static final String[] ALL_TYPES = {TABLE, SYSTEM_TABLE, VIEW};

    private static final String GET_TABLES_ALL = "select null as TABLE_CAT,"
        + " null as TABLE_SCHEM,"
        + " RDB$RELATION_NAME as TABLE_NAME,"
        + " cast('" + SYSTEM_TABLE + "' as varchar(31)) as TABLE_TYPE,"
        + " RDB$DESCRIPTION as REMARKS,"
        + " RDB$OWNER_NAME as OWNER_NAME"
        + " from RDB$RELATIONS"
        + " where ? = 'T' and RDB$SYSTEM_FLAG = 1 and RDB$VIEW_SOURCE is null"
        + " union"
        + " select null as TABLE_CAT,"
        + " null as TABLE_SCHEM,"
        + " RDB$RELATION_NAME as TABLE_NAME,"
        + " cast('" + TABLE + "' as varchar(31)) as TABLE_TYPE,"
        + " RDB$DESCRIPTION as REMARKS,"
        + " RDB$OWNER_NAME as OWNER_NAME"
        + " from RDB$RELATIONS"
        + " where ? = 'T' and RDB$SYSTEM_FLAG = 0 and RDB$VIEW_SOURCE is null"
        + " union"
        + " select null as TABLE_CAT,"
        + " null as TABLE_SCHEM,"
        + " RDB$RELATION_NAME as TABLE_NAME,"
        + " cast('" + VIEW + "' as varchar(31)) as TABLE_TYPE,"
        + " RDB$DESCRIPTION as REMARKS,"
        + " RDB$OWNER_NAME as OWNER_NAME"
        + " from RDB$RELATIONS"
        + " where ? = 'T' and RDB$VIEW_SOURCE is not null";

    private static final String GET_TABLES_EXACT = "select null as TABLE_CAT,"
        + " null as TABLE_SCHEM,"
        + " RDB$RELATION_NAME as TABLE_NAME,"
        + " cast('" + SYSTEM_TABLE + "' as varchar(31)) as TABLE_TYPE,"
        + " RDB$DESCRIPTION as REMARKS,"
        + " RDB$OWNER_NAME as OWNER_NAME"
        + " from RDB$RELATIONS"
        + " where ? = 'T' and RDB$SYSTEM_FLAG = 1 and RDB$VIEW_SOURCE is null"
        + " and ? = RDB$RELATION_NAME"
        + " union"
        + " select null as TABLE_CAT,"
        + " null as TABLE_SCHEM,"
        + " RDB$RELATION_NAME as TABLE_NAME,"
        + " cast('" + TABLE + "' as varchar(31)) as TABLE_TYPE,"
        + " RDB$DESCRIPTION as REMARKS,"
        + " RDB$OWNER_NAME as OWNER_NAME"
        + " from RDB$RELATIONS"
        + " where ? = 'T' and RDB$SYSTEM_FLAG = 0 and RDB$VIEW_SOURCE is null"
        + " and ? = RDB$RELATION_NAME"
        + " union"
        + " select null as TABLE_CAT,"
        + " null as TABLE_SCHEM,"
        + " RDB$RELATION_NAME as TABLE_NAME,"
        + " cast('" + VIEW + "' as varchar(31)) as TABLE_TYPE,"
        + " RDB$DESCRIPTION as REMARKS,"
        + " RDB$OWNER_NAME as OWNER_NAME"
        + " from RDB$RELATIONS"
        + " where ? = 'T' and RDB$VIEW_SOURCE is not null"
        + " and ? = RDB$RELATION_NAME";

    private static final String GET_TABLES_LIKE = "select null as TABLE_CAT,"
        + " null as TABLE_SCHEM,"
        + " RDB$RELATION_NAME as TABLE_NAME,"
        + " cast('" + SYSTEM_TABLE + "' as varchar(31)) as TABLE_TYPE,"
        + " RDB$DESCRIPTION as REMARKS,"
        + " RDB$OWNER_NAME as OWNER_NAME"
        + " from RDB$RELATIONS"
        + " where ? = 'T' and RDB$SYSTEM_FLAG = 1 and RDB$VIEW_SOURCE is null"
        + " and RDB$RELATION_NAME || '" + SPACES + "' like ? escape '\\'"
        + " union"
        + " select null as TABLE_CAT,"
        + " null as TABLE_SCHEM,"
        + " RDB$RELATION_NAME as TABLE_NAME,"
        + " cast('" + TABLE + "' as varchar(31)) as TABLE_TYPE,"
        + " RDB$DESCRIPTION as REMARKS,"
        + " RDB$OWNER_NAME as OWNER_NAME"
        + " from RDB$RELATIONS"
        + " where ? = 'T' and RDB$SYSTEM_FLAG = 0 and RDB$VIEW_SOURCE is null"
        + " and RDB$RELATION_NAME || '" + SPACES + "' like ? escape '\\'"
        + " union"
        + " select null as TABLE_CAT,"
        + " null as TABLE_SCHEM,"
        + " RDB$RELATION_NAME as TABLE_NAME,"
        + " cast('" + VIEW + "' as varchar(31)) as TABLE_TYPE,"
        + " RDB$DESCRIPTION as REMARKS,"
        + " RDB$OWNER_NAME as OWNER_NAME"
        + " from RDB$RELATIONS"
        + " where ? = 'T' and RDB$VIEW_SOURCE is not null"
        + " and RDB$RELATION_NAME || '" + SPACES + "' like ? escape '\\'";



    /**
     * Gets a description of tables available in a catalog.
     *
     * <P>Only table descriptions matching the catalog, schema, table
     * name and type criteria are returned.  They are ordered by
     * TABLE_TYPE, TABLE_SCHEM and TABLE_NAME.
     *
     * <P>Each table description has the following columns:
     *  <OL>
     *  <LI><B>TABLE_CAT</B> String => table catalog (may be null)
     *  <LI><B>TABLE_SCHEM</B> String => table schema (may be null)
     *  <LI><B>TABLE_NAME</B> String => table name
     *  <LI><B>TABLE_TYPE</B> String => table type.  Typical types are "TABLE",
     *          "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY",
     *          "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
     *  <LI><B>REMARKS</B> String => explanatory comment on the table
     *  </OL>
     *
     * <P><B>Note:</B> Some databases may not return information for
     * all tables.
     *
     * @param catalog a catalog name; "" retrieves those without a
     * catalog; null means drop catalog name from the selection criteria
     * @param schemaPattern a schema name pattern; "" retrieves those
     * without a schema
     * @param tableNamePattern a table name pattern
     * @param types a list of table types to include; null returns all types
     * @return <code>ResultSet</code> - each row is a table description
     * @exception SQLException if a database access error occurs
     * @see #getSearchStringEscape
     */
    public ResultSet getTables(String catalog, String schemaPattern,
        String tableNamePattern, String types[]) throws SQLException {
        checkCatalogAndSchema(catalog, schemaPattern);
        if (types == null) {
            types = ALL_TYPES;
        }
        String sql = "";
        ArrayList params = new ArrayList();
        if (isAllCondition(tableNamePattern)) {
            sql = GET_TABLES_ALL;
            params.add(getWantsSystemTables(types));
            params.add(getWantsTables(types));
            params.add(getWantsViews(types));
        }
        else if (hasNoWildcards(tableNamePattern)) {
            tableNamePattern = stripQuotes(stripEscape(tableNamePattern));
            sql = (GET_TABLES_EXACT);
            params.add(getWantsSystemTables(types));
            params.add(tableNamePattern);
            params.add(getWantsTables(types));
            params.add(tableNamePattern);
            params.add(getWantsViews(types));
            params.add(tableNamePattern);
        }
        else {
            tableNamePattern = stripQuotes(tableNamePattern) + SPACES + "%";
            sql = (GET_TABLES_LIKE);
            params.add(getWantsSystemTables(types));
            params.add(tableNamePattern);
            params.add(getWantsTables(types));
            params.add(tableNamePattern);
            params.add(getWantsViews(types));
            params.add(tableNamePattern);
        }
        return doQuery(sql, params);
    }



    /**
     * Gets the schema names available in this database.  The results
     * are ordered by schema name.
     *
     * <P>The schema column is:
     *  <OL>
     *  <LI><B>TABLE_SCHEM</B> String => schema name
     *  </OL>
     *
     * @return <code>ResultSet</code> - each row has a single String column that is a
     * schema name
     * @exception SQLException if a database access error occurs
     */
    public  ResultSet getSchemas() throws SQLException {
        throw new SQLException("Not yet implemented");
    }



    /**
     * Gets the catalog names available in this database.  The results
     * are ordered by catalog name.
     *
     * <P>The catalog column is:
     *  <OL>
     *  <LI><B>TABLE_CAT</B> String => catalog name
     *  </OL>
     *
     * @return <code>ResultSet</code> - each row has a single String column that is a
     * catalog name
     * @exception SQLException if a database access error occurs
     */
    public  ResultSet getCatalogs() throws SQLException {
        throw new SQLException("Not yet implemented");
    }



    /**
     * Gets the table types available in this database.  The results
     * are ordered by table type.
     *
     * <P>The table type is:
     *  <OL>
     *  <LI><B>TABLE_TYPE</B> String => table type.  Typical types are "TABLE",
     *          "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY",
     *          "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
     *  </OL>
     *
     * @return <code>ResultSet</code> - each row has a single String column that is a
     * table type
     * @exception SQLException if a database access error occurs
     */
    public  ResultSet getTableTypes() throws SQLException {
        /*
        XSQLVAR[] xsqlvars = new XSQLVAR[1];

        xsqlvars[0] = new XSQLVAR();
        xsqlvars[0].sqltype = GDS.SQL_VARYING;
        xsqlvars[0].sqllen = 31;
        xsqlvars[0].sqlind = -1;
        xsqlvars[0].sqlname = "TABLE_TYPE";
        xsqlvars[0].relname = "TABLETYPES";

        ArrayList rows = new ArrayList(ALL_TYPES.length);
        for(int i = 0; i < ALL_TYPES.length; i++)
          rows.add(new Object[] {ALL_TYPES[i]});

        return new FBResultSet(xsqlvars, rows);
        */
        throw new SQLException("Not yet implemented");
    }


    private static final String GET_COLUMNS_START = "select " +
        " RF.RDB$RELATION_NAME as RELATION_NAME," +
        " RF.RDB$FIELD_NAME as FIELD_NAME," +
        " F.RDB$FIELD_TYPE as FIELD_TYPE," +
        " F.RDB$FIELD_SUB_TYPE as FIELD_SUB_TYPE," +
        " F.RDB$FIELD_PRECISION as FIELD_PRECISION," +
        " F.RDB$FIELD_SCALE as FIELD_SCALE," +
        " F.RDB$FIELD_LENGTH as FIELD_LENGTH," +
        " RF.RDB$DESCRIPTION," +
        " RF.RDB$DEFAULT_SOURCE," +
        " RF.RDB$FIELD_POSITION as FIELD_POSITION, " +
        " RF.RDB$NULL_FLAG as NULL_FLAG " +
        "from" +
        " RDB$RELATION_FIELDS RF," +
        " RDB$FIELDS F " +
        "where ";

    public static final String GET_COLUMNS_END = " RF.RDB$FIELD_SOURCE = F.RDB$FIELD_NAME " +
        "order by 1, 10";

    /**
     * Gets a description of table columns available in
     * the specified catalog.
     *
     * <P>Only column descriptions matching the catalog, schema, table
     * and column name criteria are returned.  They are ordered by
     * TABLE_SCHEM, TABLE_NAME and ORDINAL_POSITION.
     *
     * <P>Each column description has the following columns:
     *  <OL>
     *  <LI><B>TABLE_CAT</B> String => table catalog (may be null)
     *  <LI><B>TABLE_SCHEM</B> String => table schema (may be null)
     *  <LI><B>TABLE_NAME</B> String => table name
     *  <LI><B>COLUMN_NAME</B> String => column name
     *  <LI><B>DATA_TYPE</B> short => SQL type from java.sql.Types
     *  <LI><B>TYPE_NAME</B> String => Data source dependent type name,
     *  for a UDT the type name is fully qualified
     *  <LI><B>COLUMN_SIZE</B> int => column size.  For char or date
     *      types this is the maximum number of characters, for numeric or
     *      decimal types this is precision.
     *  <LI><B>BUFFER_LENGTH</B> is not used.
     *  <LI><B>DECIMAL_DIGITS</B> int => the number of fractional digits
     *  <LI><B>NUM_PREC_RADIX</B> int => Radix (typically either 10 or 2)
     *  <LI><B>NULLABLE</B> int => is NULL allowed?
     *      <UL>
     *      <LI> columnNoNulls - might not allow NULL values
     *      <LI> columnNullable - definitely allows NULL values
     *      <LI> columnNullableUnknown - nullability unknown
     *      </UL>
     *  <LI><B>REMARKS</B> String => comment describing column (may be null)
     *  <LI><B>COLUMN_DEF</B> String => default value (may be null)
     *  <LI><B>SQL_DATA_TYPE</B> int => unused
     *  <LI><B>SQL_DATETIME_SUB</B> int => unused
     *  <LI><B>CHAR_OCTET_LENGTH</B> int => for char types the
     *       maximum number of bytes in the column
     *  <LI><B>ORDINAL_POSITION</B> int => index of column in table
     *      (starting at 1)
     *  <LI><B>IS_NULLABLE</B> String => "NO" means column definitely
     *      does not allow NULL values; "YES" means the column might
     *      allow NULL values.  An empty string means nobody knows.
     *  </OL>
     *
     * @param catalog a catalog name; "" retrieves those without a
     * catalog; null means drop catalog name from the selection criteria
     * @param schemaPattern a schema name pattern; "" retrieves those
     * without a schema
     * @param tableNamePattern a table name pattern
     * @param columnNamePattern a column name pattern
     * @return <code>ResultSet</code> - each row is a column description
     * @exception SQLException if a database access error occurs
     * @see #getSearchStringEscape
     */
    public ResultSet getColumns(String catalog, String schemaPattern,
        String tableNamePattern, String columnNamePattern) throws SQLException {
        checkCatalogAndSchema(catalog, schemaPattern);
        Clause tableClause = new Clause("RF.RDB$RELATION_NAME", tableNamePattern);
        Clause columnClause = new Clause("RF.RDB$FIELD_NAME", columnNamePattern);
        String sql = GET_COLUMNS_START;
        sql += tableClause.getCondition();
        sql += columnClause.getCondition();
        sql += GET_COLUMNS_END;
        ArrayList params = new ArrayList();
        if (!tableClause.getCondition().equals("")) {
            params.add(tableClause.getValue());
        }
        if (!columnClause.getCondition().equals("")) {
            params.add(columnClause.getValue());
        }

        ResultSet rs = doQuery(sql, params);

        XSQLVAR[] xsqlvars = new XSQLVAR[18];

        xsqlvars[0] = new XSQLVAR();
        xsqlvars[0].sqltype = GDS.SQL_VARYING;
        xsqlvars[0].sqllen = 31;
        xsqlvars[0].sqlind = -1;
        xsqlvars[0].sqlname = "TABLE_CAT";
        xsqlvars[0].relname = "COLUMNINFO";

        xsqlvars[1] = new XSQLVAR();
        xsqlvars[1].sqltype = GDS.SQL_VARYING;
        xsqlvars[1].sqllen = 31;
        xsqlvars[1].sqlind = -1;
        xsqlvars[1].sqlname = "TABLE_SCHEM";
        xsqlvars[1].relname = "COLUMNINFO";

        xsqlvars[2] = new XSQLVAR();
        xsqlvars[2].sqltype = GDS.SQL_VARYING;
        xsqlvars[2].sqllen = 31;
        xsqlvars[2].sqlind = 0;
        xsqlvars[2].sqlname = "TABLE_NAME";
        xsqlvars[2].relname = "COLUMNINFO";

        xsqlvars[3] = new XSQLVAR();
        xsqlvars[3].sqltype = GDS.SQL_VARYING;
        xsqlvars[3].sqllen = 31;
        xsqlvars[3].sqlind = 0;
        xsqlvars[3].sqlname = "COLUMN_NAME";
        xsqlvars[3].relname = "COLUMNINFO";

        xsqlvars[4] = new XSQLVAR();
        xsqlvars[4].sqltype = GDS.SQL_SHORT;
        xsqlvars[4].sqlname = "DATA_TYPE";
        xsqlvars[4].relname = "COLUMNINFO";

        xsqlvars[5] = new XSQLVAR();
        xsqlvars[5].sqltype = GDS.SQL_VARYING | 1;
        xsqlvars[5].sqllen = 31;
        xsqlvars[5].sqlind = 0;
        xsqlvars[5].sqlname = "TYPE_NAME";
        xsqlvars[5].relname = "COLUMNINFO";

        xsqlvars[6] = new XSQLVAR();
        xsqlvars[6].sqltype = GDS.SQL_LONG;
        xsqlvars[6].sqlname = "COLUMN_SIZE";
        xsqlvars[6].relname = "COLUMNINFO";

        xsqlvars[7] = new XSQLVAR();
        xsqlvars[7].sqltype = GDS.SQL_SHORT;
        xsqlvars[7].sqlname = "BUFFER_LENGTH";
        xsqlvars[7].relname = "COLUMNINFO";

        xsqlvars[8] = new XSQLVAR();
        xsqlvars[8].sqltype = GDS.SQL_LONG;
        xsqlvars[8].sqlname = "DECIMAL_DIGITS";
        xsqlvars[8].relname = "COLUMNINFO";

        xsqlvars[9] = new XSQLVAR();
        xsqlvars[9].sqltype = GDS.SQL_LONG;
        xsqlvars[9].sqlname = "NUM_PREC_RADIX";
        xsqlvars[9].relname = "COLUMNINFO";

        xsqlvars[10] = new XSQLVAR();
        xsqlvars[10].sqltype = GDS.SQL_LONG;
        xsqlvars[10].sqlname = "NULLABLE";
        xsqlvars[10].relname = "COLUMNINFO";

        xsqlvars[11] = new XSQLVAR();
        xsqlvars[11].sqltype = GDS.SQL_VARYING | 1;
        xsqlvars[11].sqllen = 31;
        xsqlvars[11].sqlind = 0;
        xsqlvars[11].sqlname = "REMARKS";
        xsqlvars[11].relname = "COLUMNINFO";

        xsqlvars[12] = new XSQLVAR();
        xsqlvars[12].sqltype = GDS.SQL_VARYING | 1;
        xsqlvars[12].sqllen = 31;
        xsqlvars[12].sqlind = 0;
        xsqlvars[12].sqlname = "COLUMN_DEF";
        xsqlvars[12].relname = "COLUMNINFO";

        xsqlvars[13] = new XSQLVAR();
        xsqlvars[13].sqltype = GDS.SQL_LONG;
        xsqlvars[13].sqlname = "SQL_DATA_TYPE";
        xsqlvars[13].relname = "COLUMNINFO";

        xsqlvars[14] = new XSQLVAR();
        xsqlvars[14].sqltype = GDS.SQL_LONG;
        xsqlvars[14].sqlname = "SQL_DATETIME_SUB";
        xsqlvars[14].relname = "COLUMNINFO";

        xsqlvars[15] = new XSQLVAR();
        xsqlvars[15].sqltype = GDS.SQL_LONG;
        xsqlvars[15].sqlname = "CHAR_OCTET_LENGTH";
        xsqlvars[15].relname = "COLUMNINFO";

        xsqlvars[16] = new XSQLVAR();
        xsqlvars[16].sqltype = GDS.SQL_LONG;
        xsqlvars[16].sqlname = "ORDINAL_POSITION";
        xsqlvars[16].relname = "COLUMNINFO";

        xsqlvars[17] = new XSQLVAR();
        xsqlvars[17].sqltype = GDS.SQL_VARYING;
        xsqlvars[17].sqllen = 3;
        xsqlvars[17].sqlind = 0;
        xsqlvars[17].sqlname = "IS_NULLABLE";
        xsqlvars[17].relname = "COLUMNINFO";

        ArrayList rows = new ArrayList();
        while (rs.next()) {
            Object[] row = new Object[18];
            row[0] = null;
            row[1] = null;
            row[2] = rs.getString("RELATION_NAME").trim();
            row[3] = rs.getString("FIELD_NAME").trim();

            short fieldType = rs.getShort("FIELD_TYPE");
            short fieldSubType = rs.getShort("FIELD_SUB_TYPE");
            short fieldScale = rs.getShort("FIELD_SCALE");
            int dataType = getDataType(fieldType, fieldSubType, fieldScale);

            row[4] = new Short((short) dataType);

            row[5] = null;

            if (dataType == java.sql.Types.DECIMAL ||
                dataType == java.sql.Types.NUMERIC)
            {
                row[6] = new Integer(rs.getShort("FIELD_PRECISION"));
            } else {
                row[6] = new Integer(rs.getShort("FIELD_LENGTH"));
            }

            row[7] = new Short((short) 0);
            row[8] = new Integer(fieldScale * (-1));
            row[9] = new Integer(10);

            short nullFlag = rs.getShort("NULL_FLAG");
            row[10] = (nullFlag == 1) ? new Integer(columnNoNulls) :
                                        new Integer(columnNullable);

            row[11] = null;
            row[12] = null;
            row[13] = null;
            row[14] = null;
            row[15] = new Integer(0);
            row[16] = new Integer(rs.getShort("FIELD_POSITION") + 1);
            row[17] = (nullFlag == 1) ? "NO" : "YES";

            rows.add(row);
        }
        rows.add(null);
        //return new FBResultSet(xsqlvars, rows);
        return new FBResultSet(xsqlvars, rows);
    }

    private int getDataType (short fieldType, short fieldSubType, short fieldScale) {
        if (fieldScale < 0) {
            switch (fieldType) {
                case 7:
                case 8:
                case 16:
                case 27:
                    if (fieldSubType == 2)
                        return java.sql.Types.DECIMAL;
                    else
                        return java.sql.Types.NUMERIC;
                default:
                    break;
            }
        }

        switch (fieldType) {
            case 7:
                return java.sql.Types.SMALLINT;
            case 8:
                return java.sql.Types.INTEGER;
            case 27:
            case 11:
                return java.sql.Types.DOUBLE;
            case 10:
                return java.sql.Types.FLOAT;
            case 14:
                return java.sql.Types.CHAR;
            case 37:
                return java.sql.Types.VARCHAR;
            case 35:
                return java.sql.Types.TIMESTAMP;
            case 13:
                return java.sql.Types.TIME;
            case 12:
                return java.sql.Types.DATE;
            case 16:
                if (fieldSubType == 2)
                    return java.sql.Types.DECIMAL;
                else
                    return java.sql.Types.NUMERIC;
            case 261:
                if (fieldSubType < 0)
                    return java.sql.Types.BLOB;
                if (fieldSubType == 1)
                    return java.sql.Types.LONGVARCHAR;
                else
                    return java.sql.Types.LONGVARBINARY;
            case 9:
                return java.sql.Types.OTHER;
            default:
                return java.sql.Types.NULL;
        }
    }


    /**
     * Indicates that the column might not allow NULL values.
     * A possible value for the column
     * <code>NULLABLE</code>
     * in the <code>ResultSet</code> returned by the method
     * <code>getColumns</code>.
     */
    //int columnNoNulls = 0;

    /**
     * Indicates that the column definitely allows <code>NULL</code> values.
     * A possible value for the column
     * <code>NULLABLE</code>
     * in the <code>ResultSet</code> returned by the method
     * <code>getColumns</code>.
     */
    //int columnNullable = 1;

    /**
     * Indicates that the nullability of columns is unknown.
     * A possible value for the column
     * <code>NULLABLE</code>
     * in the <code>ResultSet</code> returned by the method
     * <code>getColumns</code>.
     */
    //int columnNullableUnknown = 2;

    private static final String GET_COLUMN_PRIVILEGES_START = "select "
        + "RF.RDB$RELATION_NAME, "
        + "RF.RDB$FIELD_NAME, "
        + "UP.RDB$GRANTOR, "
        + "UP.RDB$USER, "
        + "UP.RDB$PRIVILEGE, "
        + "UP.RDB$GRANT_OPTION "
        + "from "
        + "RDB$RELATION_FIELDS RF, "
        + "RDB$FIELDS F, "
        + "RDB$USER_PRIVILEGES UP "
        + "where "
        + "RF.RDB$RELATION_NAME = UP.RDB$RELATION_NAME and "
        + "RF.RDB$FIELD_SOURCE = F.RDB$FIELD_NAME  and "
        + "(UP.RDB$FIELD_NAME is null or "
        + "UP.RDB$FIELD_NAME = RF.RDB$FIELD_NAME) and "
        + "UP.RDB$RELATION_NAME = ? and ((";
    private static final String GET_COLUMN_PRIVILEGES_END = " UP.RDB$OBJECT_TYPE = 0) or "
        + "(RF.RDB$FIELD_NAME is null and UP.RDB$OBJECT_TYPE = 0)) "
        + "order by 1, 2, 5 ";

    /**
     * Gets a description of the access rights for a table's columns.
     *
     * <P>Only privileges matching the column name criteria are
     * returned.  They are ordered by COLUMN_NAME and PRIVILEGE.
     *
     * <P>Each privilige description has the following columns:
     *  <OL>
     *  <LI><B>TABLE_CAT</B> String => table catalog (may be null)
     *  <LI><B>TABLE_SCHEM</B> String => table schema (may be null)
     *  <LI><B>TABLE_NAME</B> String => table name
     *  <LI><B>COLUMN_NAME</B> String => column name
     *  <LI><B>GRANTOR</B> => grantor of access (may be null)
     *  <LI><B>GRANTEE</B> String => grantee of access
     *  <LI><B>PRIVILEGE</B> String => name of access (SELECT,
     *      INSERT, UPDATE, REFRENCES, ...)
     *  <LI><B>IS_GRANTABLE</B> String => "YES" if grantee is permitted
     *      to grant to others; "NO" if not; null if unknown
     *  </OL>
     *
     * @param catalog a catalog name; "" retrieves those without a
     * catalog; null means drop catalog name from the selection criteria
     * @param schema a schema name; "" retrieves those without a schema
     * @param table a table name
     * @param columnNamePattern a column name pattern
     * @return <code>ResultSet</code> - each row is a column privilege description
     * @exception SQLException if a database access error occurs
     * @see #getSearchStringEscape
     */
    public ResultSet getColumnPrivileges(String catalog, String schema,
        String table, String columnNamePattern) throws SQLException {
        checkCatalogAndSchema(catalog, schema);
        Clause columnClause = new Clause("RF.RDB$FIELD_NAME", columnNamePattern);
        String sql = GET_COLUMN_PRIVILEGES_START;
        sql += columnClause.getCondition();
        sql += GET_COLUMN_PRIVILEGES_END;
        ArrayList params = new ArrayList();
        params.add(table);
        if (!columnClause.getCondition().equals("")) {
            params.add(columnClause.getValue());
        }
        return doQuery(sql, params);
    }



    private static final String GET_TABLE_PRIVILEGES_START = "select"
        + " RDB$RELATION_NAME,"
        + " RDB$GRANTOR,"
        + " RDB$USER,"
        + " RDB$PRIVILEGE,"
        + " RDB$GRANT_OPTION "
        + "from"
        + " RDB$USER_PRIVILEGES "
        + "where ";
    private static final String GET_TABLE_PRIVILEGES_END = " RDB$OBJECT_TYPE = 0 and"
        + " RDB$FIELD_NAME is null "
        + "order by 1, 4";

    /**
     * Gets a description of the access rights for each table available
     * in a catalog. Note that a table privilege applies to one or
     * more columns in the table. It would be wrong to assume that
     * this priviledge applies to all columns (this may be true for
     * some systems but is not true for all.)
     *
     * <P>Only privileges matching the schema and table name
     * criteria are returned.  They are ordered by TABLE_SCHEM,
     * TABLE_NAME, and PRIVILEGE.
     *
     * <P>Each privilige description has the following columns:
     *  <OL>
     *  <LI><B>TABLE_CAT</B> String => table catalog (may be null)
     *  <LI><B>TABLE_SCHEM</B> String => table schema (may be null)
     *  <LI><B>TABLE_NAME</B> String => table name
     *  <LI><B>GRANTOR</B> => grantor of access (may be null)
     *  <LI><B>GRANTEE</B> String => grantee of access
     *  <LI><B>PRIVILEGE</B> String => name of access (SELECT,
     *      INSERT, UPDATE, REFRENCES, ...)
     *  <LI><B>IS_GRANTABLE</B> String => "YES" if grantee is permitted
     *      to grant to others; "NO" if not; null if unknown
     *  </OL>
     *
     * @param catalog a catalog name; "" retrieves those without a
     * catalog; null means drop catalog name from the selection criteria
     * @param schemaPattern a schema name pattern; "" retrieves those
     * without a schema
     * @param tableNamePattern a table name pattern
     * @return <code>ResultSet</code> - each row is a table privilege description
     * @exception SQLException if a database access error occurs
     * @see #getSearchStringEscape
     */
    public ResultSet getTablePrivileges(String catalog, String schemaPattern,
                String tableNamePattern) throws SQLException {
        checkCatalogAndSchema(catalog, schemaPattern);
        Clause tableClause = new Clause("RDB$RELATION_NAME", tableNamePattern);
        String sql = GET_TABLE_PRIVILEGES_START;
        sql += tableClause.getCondition();
        sql += GET_TABLE_PRIVILEGES_END;
        ArrayList params = new ArrayList();
        if (!tableClause.getCondition().equals("")) {
            params.add(tableClause.getValue());
        }
        return doQuery(sql, params);
    }



    /**
     * Gets a description of a table's optimal set of columns that
     * uniquely identifies a row. They are ordered by SCOPE.
     *
     * <P>Each column description has the following columns:
     *  <OL>
     *  <LI><B>SCOPE</B> short => actual scope of result
     *      <UL>
     *      <LI> bestRowTemporary - very temporary, while using row
     *      <LI> bestRowTransaction - valid for remainder of current transaction
     *      <LI> bestRowSession - valid for remainder of current session
     *      </UL>
     *  <LI><B>COLUMN_NAME</B> String => column name
     *  <LI><B>DATA_TYPE</B> short => SQL data type from java.sql.Types
     *  <LI><B>TYPE_NAME</B> String => Data source dependent type name,
     *  for a UDT the type name is fully qualified
     *  <LI><B>COLUMN_SIZE</B> int => precision
     *  <LI><B>BUFFER_LENGTH</B> int => not used
     *  <LI><B>DECIMAL_DIGITS</B> short  => scale
     *  <LI><B>PSEUDO_COLUMN</B> short => is this a pseudo column
     *      like an Oracle ROWID
     *      <UL>
     *      <LI> bestRowUnknown - may or may not be pseudo column
     *      <LI> bestRowNotPseudo - is NOT a pseudo column
     *      <LI> bestRowPseudo - is a pseudo column
     *      </UL>
     *  </OL>
     *
     * @param catalog a catalog name; "" retrieves those without a
     * catalog; null means drop catalog name from the selection criteria
     * @param schema a schema name; "" retrieves those without a schema
     * @param table a table name
     * @param scope the scope of interest; use same values as SCOPE
     * @param nullable include columns that are nullable?
     * @return <code>ResultSet</code> - each row is a column description
     * @exception SQLException if a database access error occurs
     */
    public ResultSet getBestRowIdentifier(String catalog, String schema,
        String table, int scope, boolean nullable) throws SQLException {
        throw new SQLException("Not yet implemented");
    }



    /**
     * Indicates that the scope of the best row identifier is
     * very temporary, lasting only while the
     * row is being used.
     * A possible value for the column
     * <code>SCOPE</code>
     * in the <code>ResultSet</code> object
     * returned by the method <code>getBestRowIdentifier</code>.
     */
    int bestRowTemporary   = 0;

    /**
     * Indicates that the scope of the best row identifier is
     * the remainder of the current transaction.
     * A possible value for the column
     * <code>SCOPE</code>
     * in the <code>ResultSet</code> object
     * returned by the method <code>getBestRowIdentifier</code>.
     */
    int bestRowTransaction = 1;

    /**
     * Indicates that the scope of the best row identifier is
     * the remainder of the current session.
     * A possible value for the column
     * <code>SCOPE</code>
     * in the <code>ResultSet</code> object
     * returned by the method <code>getBestRowIdentifier</code>.
     */
    int bestRowSession     = 2;

    /**
     * Indicates that the best row identifier may or may not be a pseudo column.
     * A possible value for the column
     * <code>PSEUDO_COLUMN</code>
     * in the <code>ResultSet</code> object
     * returned by the method <code>getBestRowIdentifier</code>.
     */
    int bestRowUnknown  = 0;

    /**
     * Indicates that the best row identifier is NOT a pseudo column.
     * A possible value for the column
     * <code>PSEUDO_COLUMN</code>
     * in the <code>ResultSet</code> object
     * returned by the method <code>getBestRowIdentifier</code>.
     */
    int bestRowNotPseudo    = 1;

    /**
     * Indicates that the best row identifier is a pseudo column.
     * A possible value for the column
     * <code>PSEUDO_COLUMN</code>
     * in the <code>ResultSet</code> object
     * returned by the method <code>getBestRowIdentifier</code>.
     */
    int bestRowPseudo   = 2;

    /**
     * Gets a description of a table's columns that are automatically
     * updated when any value in a row is updated.  They are
     * unordered.
     *
     * <P>Each column description has the following columns:
     *  <OL>
     *  <LI><B>SCOPE</B> short => is not used
     *  <LI><B>COLUMN_NAME</B> String => column name
     *  <LI><B>DATA_TYPE</B> short => SQL data type from java.sql.Types
     *  <LI><B>TYPE_NAME</B> String => Data source dependent type name
     *  <LI><B>COLUMN_SIZE</B> int => precision
     *  <LI><B>BUFFER_LENGTH</B> int => length of column value in bytes
     *  <LI><B>DECIMAL_DIGITS</B> short  => scale
     *  <LI><B>PSEUDO_COLUMN</B> short => is this a pseudo column
     *      like an Oracle ROWID
     *      <UL>
     *      <LI> versionColumnUnknown - may or may not be pseudo column
     *      <LI> versionColumnNotPseudo - is NOT a pseudo column
     *      <LI> versionColumnPseudo - is a pseudo column
     *      </UL>
     *  </OL>
     *
     * @param catalog a catalog name; "" retrieves those without a
     * catalog; null means drop catalog name from the selection criteria
     * @param schema a schema name; "" retrieves those without a schema
     * @param table a table name
     * @return <code>ResultSet</code> - each row is a column description
     * @exception SQLException if a database access error occurs
     */
    public ResultSet getVersionColumns(String catalog, String schema,
                String table) throws SQLException {
        throw new SQLException("Not yet implemented");
    }



    /**
     * Indicates that this version column may or may not be a pseudo column.
     * A possible value for the column
     * <code>PSEUDO_COLUMN</code>
     * in the <code>ResultSet</code> object
     * returned by the method <code>getVersionColumns</code>.
     */
    int versionColumnUnknown    = 0;

    /**
     * Indicates that this version column is NOT a pseudo column.
     * A possible value for the column
     * <code>PSEUDO_COLUMN</code>
     * in the <code>ResultSet</code> object
     * returned by the method <code>getVersionColumns</code>.
     */
    int versionColumnNotPseudo  = 1;

    /**
     * Indicates that this version column is a pseudo column.
     * A possible value for the column
     * <code>PSEUDO_COLUMN</code>
     * in the <code>ResultSet</code> object
     * returned by the method <code>getVersionColumns</code>.
     */
    int versionColumnPseudo = 2;

    private static final String GET_PRIMARY_KEYS = "select "
        + " null as TABLE_CAT, "
        + " null as TABLE_SCHEM, "
        + "RC.RDB$RELATION_NAME as TABLE_NAME, "
        + "ISGMT.RDB$FIELD_NAME as COLUMN_NAME, "
        + "CAST ((ISGMT.RDB$FIELD_POSITION + 1) as SMALLINT) as KEY_SEQ, "
        + "RC.RDB$CONSTRAINT_NAME as PK_NAME "
        + "from "
        + "RDB$RELATION_CONSTRAINTS RC, "
        + "RDB$INDEX_SEGMENTS ISGMT "
        + "where "
        + "RC.RDB$RELATION_NAME = ? and "
        + "RC.RDB$INDEX_NAME = ISGMT.RDB$INDEX_NAME and "
        + "RC.RDB$CONSTRAINT_TYPE = 'PRIMARY KEY' "
        + "order by ISGMT.RDB$FIELD_NAME ";

    /**
     * Gets a description of a table's primary key columns.  They
     * are ordered by COLUMN_NAME.
     *
     * <P>Each primary key column description has the following columns:
     *  <OL>
     *  <LI><B>TABLE_CAT</B> String => table catalog (may be null)
     *  <LI><B>TABLE_SCHEM</B> String => table schema (may be null)
     *  <LI><B>TABLE_NAME</B> String => table name
     *  <LI><B>COLUMN_NAME</B> String => column name
     *  <LI><B>KEY_SEQ</B> short => sequence number within primary key
     *  <LI><B>PK_NAME</B> String => primary key name (may be null)
     *  </OL>
     *
     * @param catalog a catalog name; "" retrieves those without a
     * catalog; null means drop catalog name from the selection criteria
     * @param schema a schema name; "" retrieves those
     * without a schema
     * @param table a table name
     * @return <code>ResultSet</code> - each row is a primary key column description
     * @exception SQLException if a database access error occurs
     */
    public ResultSet getPrimaryKeys(String catalog, String schema,
                String table) throws SQLException {
        checkCatalogAndSchema(catalog, schema);
        String sql = GET_PRIMARY_KEYS;
        ArrayList params = new ArrayList();
        params.add(table);

        ResultSet rs = doQuery(sql, params);

        XSQLVAR[] xsqlvars = new XSQLVAR[6];

        xsqlvars[0] = new XSQLVAR();
        xsqlvars[0].sqltype = GDS.SQL_VARYING;
        xsqlvars[0].sqllen = 31;
        xsqlvars[0].sqlind = -1;
        xsqlvars[0].sqlname = "TABLE_CAT";
        xsqlvars[0].relname = "COLUMNINFO";

        xsqlvars[1] = new XSQLVAR();
        xsqlvars[1].sqltype = GDS.SQL_VARYING;
        xsqlvars[1].sqllen = 31;
        xsqlvars[1].sqlind = -1;
        xsqlvars[1].sqlname = "TABLE_SCHEM";
        xsqlvars[1].relname = "COLUMNINFO";

        xsqlvars[2] = new XSQLVAR();
        xsqlvars[2].sqltype = GDS.SQL_VARYING;
        xsqlvars[2].sqllen = 31;
        xsqlvars[2].sqlind = 0;
        xsqlvars[2].sqlname = "TABLE_NAME";
        xsqlvars[2].relname = "COLUMNINFO";

        xsqlvars[3] = new XSQLVAR();
        xsqlvars[3].sqltype = GDS.SQL_VARYING;
        xsqlvars[3].sqllen = 31;
        xsqlvars[3].sqlind = 0;
        xsqlvars[3].sqlname = "COLUMN_NAME";
        xsqlvars[3].relname = "COLUMNINFO";

        xsqlvars[4] = new XSQLVAR();
        xsqlvars[4].sqltype = GDS.SQL_SHORT;
        xsqlvars[4].sqlname = "KEY_SEQ";
        xsqlvars[4].relname = "COLUMNINFO";

        xsqlvars[5] = new XSQLVAR();
        xsqlvars[5].sqltype = GDS.SQL_VARYING;
        xsqlvars[5].sqllen = 31;
        xsqlvars[5].sqlind = 0;
        xsqlvars[5].sqlname = "PK_NAME";
        xsqlvars[5].relname = "COLUMNINFO";

        ArrayList rows = new ArrayList();
        while (rs.next()) {
            Object[] row = new Object[6];
            row[0] = null;
            row[1] = null;
            row[2] = rs.getString("TABLE_NAME").trim();
            row[3] = rs.getString("COLUMN_NAME").trim();
            row[4] = new Short(rs.getShort("KEY_SEQ"));
            row[5] = rs.getString("PK_NAME");

            rows.add(row);
        }
        rows.add(null);
        // return new FBResultSet(xsqlvars, rows);
        return new FBResultSet(xsqlvars, rows);
    }



    /**
     * Gets a description of the primary key columns that are
     * referenced by a table's foreign key columns (the primary keys
     * imported by a table).  They are ordered by PKTABLE_CAT,
     * PKTABLE_SCHEM, PKTABLE_NAME, and KEY_SEQ.
     *
     * <P>Each primary key column description has the following columns:
     *  <OL>
     *  <LI><B>PKTABLE_CAT</B> String => primary key table catalog
     *      being imported (may be null)
     *  <LI><B>PKTABLE_SCHEM</B> String => primary key table schema
     *      being imported (may be null)
     *  <LI><B>PKTABLE_NAME</B> String => primary key table name
     *      being imported
     *  <LI><B>PKCOLUMN_NAME</B> String => primary key column name
     *      being imported
     *  <LI><B>FKTABLE_CAT</B> String => foreign key table catalog (may be null)
     *  <LI><B>FKTABLE_SCHEM</B> String => foreign key table schema (may be null)
     *  <LI><B>FKTABLE_NAME</B> String => foreign key table name
     *  <LI><B>FKCOLUMN_NAME</B> String => foreign key column name
     *  <LI><B>KEY_SEQ</B> short => sequence number within foreign key
     *  <LI><B>UPDATE_RULE</B> short => What happens to
     *       foreign key when primary is updated:
     *      <UL>
     *      <LI> importedNoAction - do not allow update of primary
     *               key if it has been imported
     *      <LI> importedKeyCascade - change imported key to agree
     *               with primary key update
     *      <LI> importedKeySetNull - change imported key to NULL if
     *               its primary key has been updated
     *      <LI> importedKeySetDefault - change imported key to default values
     *               if its primary key has been updated
     *      <LI> importedKeyRestrict - same as importedKeyNoAction
     *                                 (for ODBC 2.x compatibility)
     *      </UL>
     *  <LI><B>DELETE_RULE</B> short => What happens to
     *      the foreign key when primary is deleted.
     *      <UL>
     *      <LI> importedKeyNoAction - do not allow delete of primary
     *               key if it has been imported
     *      <LI> importedKeyCascade - delete rows that import a deleted key
     *      <LI> importedKeySetNull - change imported key to NULL if
     *               its primary key has been deleted
     *      <LI> importedKeyRestrict - same as importedKeyNoAction
     *                                 (for ODBC 2.x compatibility)
     *      <LI> importedKeySetDefault - change imported key to default if
     *               its primary key has been deleted
     *      </UL>
     *  <LI><B>FK_NAME</B> String => foreign key name (may be null)
     *  <LI><B>PK_NAME</B> String => primary key name (may be null)
     *  <LI><B>DEFERRABILITY</B> short => can the evaluation of foreign key
     *      constraints be deferred until commit
     *      <UL>
     *      <LI> importedKeyInitiallyDeferred - see SQL92 for definition
     *      <LI> importedKeyInitiallyImmediate - see SQL92 for definition
     *      <LI> importedKeyNotDeferrable - see SQL92 for definition
     *      </UL>
     *  </OL>
     *
     * @param catalog a catalog name; "" retrieves those without a
     * catalog; null means drop catalog name from the selection criteria
     * @param schema a schema name; "" retrieves those
     * without a schema
     * @param table a table name
     * @return <code>ResultSet</code> - each row is a primary key column description
     * @exception SQLException if a database access error occurs
     * @see #getExportedKeys
     */
    public ResultSet getImportedKeys(String catalog, String schema,
                String table) throws SQLException {
        throw new SQLException("Not yet implemented");
    }



    /**
     * A possible value for the columns <code>UPDATE_RULE</code>
     * and <code>DELETE_RULE</code> in the
     * <code>ResultSet</code> objects returned by the methods
     * <code>getImportedKeys</code>,  <code>getExportedKeys</code>,
     * and <code>getCrossReference</code>.
     * <P>For the column <code>UPDATE_RULE</code>,
     * it indicates that
     * when the primary key is updated, the foreign key (imported key)
     * is changed to agree with it.
     * <P>For the column <code>DELETE_RULE</code>,
     * it indicates that
     * when the primary key is deleted, rows that imported that key
     * are deleted.
     */
    int importedKeyCascade  = 0;

    /**
     * A possible value for the columns <code>UPDATE_RULE</code>
     * and <code>DELETE_RULE</code> in the
     * <code>ResultSet</code> objects returned by the methods
     * <code>getImportedKeys</code>,  <code>getExportedKeys</code>,
     * and <code>getCrossReference</code>.
     * <P>For the column <code>UPDATE_RULE</code>, it indicates that
     * a primary key may not be updated if it has been imported by
     * another table as a foreign key.
     * <P>For the column <code>DELETE_RULE</code>, it indicates that
     * a primary key may not be deleted if it has been imported by
     * another table as a foreign key.
     */
    int importedKeyRestrict = 1;

    /**
     * A possible value for the columns <code>UPDATE_RULE</code>
     * and <code>DELETE_RULE</code> in the
     * <code>ResultSet</code> objects returned by the methods
     * <code>getImportedKeys</code>,  <code>getExportedKeys</code>,
     * and <code>getCrossReference</code>.
     * <P>For the columns <code>UPDATE_RULE</code>
     * and <code>DELETE_RULE</code>,
     * it indicates that
     * when the primary key is updated or deleted, the foreign key (imported key)
     * is changed to <code>NULL</code>.
     */
    int importedKeySetNull  = 2;

    /**
     * A possible value for the columns <code>UPDATE_RULE</code>
     * and <code>DELETE_RULE</code> in the
     * <code>ResultSet</code> objects returned by the methods
     * <code>getImportedKeys</code>,  <code>getExportedKeys</code>,
     * and <code>getCrossReference</code>.
     * <P>For the columns <code>UPDATE_RULE</code>
     * and <code>DELETE_RULE</code>,
     * it indicates that
     * if the primary key has been imported, it cannot be updated or deleted.
     */
    int importedKeyNoAction = 3;

    /**
     * A possible value for the columns <code>UPDATE_RULE</code>
     * and <code>DELETE_RULE</code> in the
     * <code>ResultSet</code> objects returned by the methods
     * <code>getImportedKeys</code>,  <code>getExportedKeys</code>,
     * and <code>getCrossReference</code>.
     * <P>For the columns <code>UPDATE_RULE</code>
     * and <code>DELETE_RULE</code>,
     * it indicates that
     * if the primary key is updated or deleted, the foreign key (imported key)
     * is set to the default value.
     */
    int importedKeySetDefault  = 4;

    /**
     * A possible value for the column <code>DEFERRABILITY</code>
     * in the
     * <code>ResultSet</code> objects returned by the methods
     * <code>getImportedKeys</code>,  <code>getExportedKeys</code>,
     * and <code>getCrossReference</code>.
     * <P>Indicates deferrability.  See SQL-92 for a definition.
     */
    int importedKeyInitiallyDeferred  = 5;

    /**
     * A possible value for the column <code>DEFERRABILITY</code>
     * in the
     * <code>ResultSet</code> objects returned by the methods
     * <code>getImportedKeys</code>,  <code>getExportedKeys</code>,
     * and <code>getCrossReference</code>.
     * <P>Indicates deferrability.  See SQL-92 for a definition.
     */
    int importedKeyInitiallyImmediate  = 6;

    /**
     * A possible value for the column <code>DEFERRABILITY</code>
     * in the
     * <code>ResultSet</code> objects returned by the methods
     * <code>getImportedKeys</code>,  <code>getExportedKeys</code>,
     * and <code>getCrossReference</code>.
     * <P>Indicates deferrability.  See SQL-92 for a definition.
     */
    int importedKeyNotDeferrable  = 7;

    /**
     * Gets a description of the foreign key columns that reference a
     * table's primary key columns (the foreign keys exported by a
     * table).  They are ordered by FKTABLE_CAT, FKTABLE_SCHEM,
     * FKTABLE_NAME, and KEY_SEQ.
     *
     * <P>Each foreign key column description has the following columns:
     *  <OL>
     *  <LI><B>PKTABLE_CAT</B> String => primary key table catalog (may be null)
     *  <LI><B>PKTABLE_SCHEM</B> String => primary key table schema (may be null)
     *  <LI><B>PKTABLE_NAME</B> String => primary key table name
     *  <LI><B>PKCOLUMN_NAME</B> String => primary key column name
     *  <LI><B>FKTABLE_CAT</B> String => foreign key table catalog (may be null)
     *      being exported (may be null)
     *  <LI><B>FKTABLE_SCHEM</B> String => foreign key table schema (may be null)
     *      being exported (may be null)
     *  <LI><B>FKTABLE_NAME</B> String => foreign key table name
     *      being exported
     *  <LI><B>FKCOLUMN_NAME</B> String => foreign key column name
     *      being exported
     *  <LI><B>KEY_SEQ</B> short => sequence number within foreign key
     *  <LI><B>UPDATE_RULE</B> short => What happens to
     *       foreign key when primary is updated:
     *      <UL>
     *      <LI> importedNoAction - do not allow update of primary
     *               key if it has been imported
     *      <LI> importedKeyCascade - change imported key to agree
     *               with primary key update
     *      <LI> importedKeySetNull - change imported key to NULL if
     *               its primary key has been updated
     *      <LI> importedKeySetDefault - change imported key to default values
     *               if its primary key has been updated
     *      <LI> importedKeyRestrict - same as importedKeyNoAction
     *                                 (for ODBC 2.x compatibility)
     *      </UL>
     *  <LI><B>DELETE_RULE</B> short => What happens to
     *      the foreign key when primary is deleted.
     *      <UL>
     *      <LI> importedKeyNoAction - do not allow delete of primary
     *               key if it has been imported
     *      <LI> importedKeyCascade - delete rows that import a deleted key
     *      <LI> importedKeySetNull - change imported key to NULL if
     *               its primary key has been deleted
     *      <LI> importedKeyRestrict - same as importedKeyNoAction
     *                                 (for ODBC 2.x compatibility)
     *      <LI> importedKeySetDefault - change imported key to default if
     *               its primary key has been deleted
     *      </UL>
     *  <LI><B>FK_NAME</B> String => foreign key name (may be null)
     *  <LI><B>PK_NAME</B> String => primary key name (may be null)
     *  <LI><B>DEFERRABILITY</B> short => can the evaluation of foreign key
     *      constraints be deferred until commit
     *      <UL>
     *      <LI> importedKeyInitiallyDeferred - see SQL92 for definition
     *      <LI> importedKeyInitiallyImmediate - see SQL92 for definition
     *      <LI> importedKeyNotDeferrable - see SQL92 for definition
     *      </UL>
     *  </OL>
     *
     * @param catalog a catalog name; "" retrieves those without a
     * catalog; null means drop catalog name from the selection criteria
     * @param schema a schema name; "" retrieves those
     * without a schema
     * @param table a table name
     * @return <code>ResultSet</code> - each row is a foreign key column description
     * @exception SQLException if a database access error occurs
     * @see #getImportedKeys
     */
    public ResultSet getExportedKeys(String catalog, String schema,
                String table) throws SQLException {
        throw new SQLException("Not yet implemented");
    }



    /**
     * Gets a description of the foreign key columns in the foreign key
     * table that reference the primary key columns of the primary key
     * table (describe how one table imports another's key.) This
     * should normally return a single foreign key/primary key pair
     * (most tables only import a foreign key from a table once.)  They
     * are ordered by FKTABLE_CAT, FKTABLE_SCHEM, FKTABLE_NAME, and
     * KEY_SEQ.
     *
     * <P>Each foreign key column description has the following columns:
     *  <OL>
     *  <LI><B>PKTABLE_CAT</B> String => primary key table catalog (may be null)
     *  <LI><B>PKTABLE_SCHEM</B> String => primary key table schema (may be null)
     *  <LI><B>PKTABLE_NAME</B> String => primary key table name
     *  <LI><B>PKCOLUMN_NAME</B> String => primary key column name
     *  <LI><B>FKTABLE_CAT</B> String => foreign key table catalog (may be null)
     *      being exported (may be null)
     *  <LI><B>FKTABLE_SCHEM</B> String => foreign key table schema (may be null)
     *      being exported (may be null)
     *  <LI><B>FKTABLE_NAME</B> String => foreign key table name
     *      being exported
     *  <LI><B>FKCOLUMN_NAME</B> String => foreign key column name
     *      being exported
     *  <LI><B>KEY_SEQ</B> short => sequence number within foreign key
     *  <LI><B>UPDATE_RULE</B> short => What happens to
     *       foreign key when primary is updated:
     *      <UL>
     *      <LI> importedNoAction - do not allow update of primary
     *               key if it has been imported
     *      <LI> importedKeyCascade - change imported key to agree
     *               with primary key update
     *      <LI> importedKeySetNull - change imported key to NULL if
     *               its primary key has been updated
     *      <LI> importedKeySetDefault - change imported key to default values
     *               if its primary key has been updated
     *      <LI> importedKeyRestrict - same as importedKeyNoAction
     *                                 (for ODBC 2.x compatibility)
     *      </UL>
     *  <LI><B>DELETE_RULE</B> short => What happens to
     *      the foreign key when primary is deleted.
     *      <UL>
     *      <LI> importedKeyNoAction - do not allow delete of primary
     *               key if it has been imported
     *      <LI> importedKeyCascade - delete rows that import a deleted key
     *      <LI> importedKeySetNull - change imported key to NULL if
     *               its primary key has been deleted
     *      <LI> importedKeyRestrict - same as importedKeyNoAction
     *                                 (for ODBC 2.x compatibility)
     *      <LI> importedKeySetDefault - change imported key to default if
     *               its primary key has been deleted
     *      </UL>
     *  <LI><B>FK_NAME</B> String => foreign key name (may be null)
     *  <LI><B>PK_NAME</B> String => primary key name (may be null)
     *  <LI><B>DEFERRABILITY</B> short => can the evaluation of foreign key
     *      constraints be deferred until commit
     *      <UL>
     *      <LI> importedKeyInitiallyDeferred - see SQL92 for definition
     *      <LI> importedKeyInitiallyImmediate - see SQL92 for definition
     *      <LI> importedKeyNotDeferrable - see SQL92 for definition
     *      </UL>
     *  </OL>
     *
     * @param primaryCatalog a catalog name; "" retrieves those without a
     * catalog; null means drop catalog name from the selection criteria
     * @param primarySchema a schema name; "" retrieves those
     * without a schema
     * @param primaryTable the table name that exports the key
     * @param foreignCatalog a catalog name; "" retrieves those without a
     * catalog; null means drop catalog name from the selection criteria
     * @param foreignSchema a schema name; "" retrieves those
     * without a schema
     * @param foreignTable the table name that imports the key
     * @return <code>ResultSet</code> - each row is a foreign key column description
     * @exception SQLException if a database access error occurs
     * @see #getImportedKeys
     */
    public ResultSet getCrossReference(
        String primaryCatalog, String primarySchema, String primaryTable,
        String foreignCatalog, String foreignSchema, String foreignTable
        ) throws SQLException {
        throw new SQLException("Not yet implemented");
    }

    private static final Short shortZero = new Short((short)0);
    private static final String CASESENSITIVE = "T";
    private static final String CASEINSENSITIVE = "F";
    private static final String UNSIGNED = "T";
    private static final String SIGNED = "F";
    private static final String FIXEDSCALE = "T";
    private static final String VARIABLESCALE = "F";
    private static final String NOTAUTOINC = "F";
    private static final Short PREDNONE = new Short((short)0);//typePredNone);
    private static final Short PREDBASIC = new Short((short)2);//typePredBasic);
    private static final Short SEARCHABLE = new Short((short)3);//typeSearchable);
    private static final Short NULLABLE = new Short((short)1);//typeNullable);
    private static final Integer BINARY = new Integer(2);

    /**
     * Simple convertor function to convert integer values to Short objects.
     * Used in {@link #getTypeInfo()} for values of {@link java.sql.Types} class.
     *
     * @param value integer value to convert
     * @return instance of java.lang.Short representing the value
     */
    private Short createShort(int value) throws SQLException {
        if (value > Short.MAX_VALUE)
            throw new SQLException("Cannot convert integer to short.");
        return new Short((short)value);
    }

    /**
     * Gets a description of all the standard SQL types supported by
     * this database. They are ordered by DATA_TYPE and then by how
     * closely the data type maps to the corresponding JDBC SQL type.
     *
     * <P>Each type description has the following columns:
     *  <OL>
     *  <LI><B>TYPE_NAME</B> String => Type name
     *  <LI><B>DATA_TYPE</B> short => SQL data type from java.sql.Types
     *  <LI><B>PRECISION</B> int => maximum precision
     *  <LI><B>LITERAL_PREFIX</B> String => prefix used to quote a literal
     *      (may be null)
     *  <LI><B>LITERAL_SUFFIX</B> String => suffix used to quote a literal
            (may be null)
     *  <LI><B>CREATE_PARAMS</B> String => parameters used in creating
     *      the type (may be null)
     *  <LI><B>NULLABLE</B> short => can you use NULL for this type?
     *      <UL>
     *      <LI> typeNoNulls - does not allow NULL values
     *      <LI> typeNullable - allows NULL values
     *      <LI> typeNullableUnknown - nullability unknown
     *      </UL>
     *  <LI><B>CASE_SENSITIVE</B> boolean=> is it case sensitive?
     *  <LI><B>SEARCHABLE</B> short => can you use "WHERE" based on this type:
     *      <UL>
     *      <LI> typePredNone - No support
     *      <LI> typePredChar - Only supported with WHERE .. LIKE
     *      <LI> typePredBasic - Supported except for WHERE .. LIKE
     *      <LI> typeSearchable - Supported for all WHERE ..
     *      </UL>
     *  <LI><B>UNSIGNED_ATTRIBUTE</B> boolean => is it unsigned?
     *  <LI><B>FIXED_PREC_SCALE</B> boolean => can it be a money value?
     *  <LI><B>AUTO_INCREMENT</B> boolean => can it be used for an
     *      auto-increment value?
     *  <LI><B>LOCAL_TYPE_NAME</B> String => localized version of type name
     *      (may be null)
     *  <LI><B>MINIMUM_SCALE</B> short => minimum scale supported
     *  <LI><B>MAXIMUM_SCALE</B> short => maximum scale supported
     *  <LI><B>SQL_DATA_TYPE</B> int => unused
     *  <LI><B>SQL_DATETIME_SUB</B> int => unused
     *  <LI><B>NUM_PREC_RADIX</B> int => usually 2 or 10
     *  </OL>
     *
     * @return <code>ResultSet</code> - each row is an SQL type description
     * @exception SQLException if a database access error occurs
     */
    public  ResultSet getTypeInfo() throws SQLException {
        //need to construct xsqlvar[] for ResultSetMetaData.
        XSQLVAR[] xsqlvars = new XSQLVAR[18];

        xsqlvars[0] = new XSQLVAR();
        xsqlvars[0].sqltype = GDS.SQL_VARYING;
        xsqlvars[0].sqllen = 31;
        xsqlvars[0].sqlind = 0;
        xsqlvars[0].sqlname = "TYPE_NAME";
        xsqlvars[0].relname = "TYPEINFO";

        xsqlvars[1] = new XSQLVAR();
        xsqlvars[1].sqltype = GDS.SQL_SHORT;
        xsqlvars[1].sqlname = "DATA_TYPE";
        xsqlvars[1].relname = "TYPEINFO";

        xsqlvars[2] = new XSQLVAR();
        xsqlvars[2].sqltype = GDS.SQL_LONG;
        xsqlvars[2].sqlname = "PRECISION";
        xsqlvars[2].relname = "TYPEINFO";

        xsqlvars[3] = new XSQLVAR();
        xsqlvars[3].sqltype = GDS.SQL_VARYING;
        xsqlvars[3].sqllen = 1;
        xsqlvars[3].sqlind = -1;
        xsqlvars[3].sqlname = "LITERAL_PREFIX";
        xsqlvars[3].relname = "TYPEINFO";

        xsqlvars[4] = new XSQLVAR();
        xsqlvars[4].sqltype = GDS.SQL_VARYING;
        xsqlvars[4].sqllen = 1;
        xsqlvars[4].sqlind = -1;
        xsqlvars[4].sqlname = "LITERAL_SUFFIX";
        xsqlvars[4].relname = "TYPEINFO";

        xsqlvars[5] = new XSQLVAR();
        xsqlvars[5].sqltype = GDS.SQL_VARYING;
        xsqlvars[5].sqllen = 31;
        xsqlvars[5].sqlind = -1;
        xsqlvars[5].sqlname = "CREATE_PARAMS";
        xsqlvars[5].relname = "TYPEINFO";

        xsqlvars[6] = new XSQLVAR();
        xsqlvars[6].sqltype = GDS.SQL_SHORT;
        xsqlvars[6].sqlname = "NULLABLE";
        xsqlvars[6].relname = "TYPEINFO";

        xsqlvars[7] = new XSQLVAR();
        xsqlvars[7].sqltype = GDS.SQL_TEXT;
        xsqlvars[7].sqllen = 1;
        xsqlvars[7].sqlname = "CASE_SENSITIVE";
        xsqlvars[7].relname = "TYPEINFO";

        xsqlvars[8] = new XSQLVAR();
        xsqlvars[8].sqltype = GDS.SQL_SHORT;
        xsqlvars[8].sqlname = "SEARCHABLE";
        xsqlvars[8].relname = "TYPEINFO";

        xsqlvars[9] = new XSQLVAR();
        xsqlvars[9].sqltype = GDS.SQL_TEXT;
        xsqlvars[9].sqllen = 1;
        xsqlvars[9].sqlname = "UNSIGNED_ATTRIBUTE";
        xsqlvars[9].relname = "TYPEINFO";

        xsqlvars[10] = new XSQLVAR();
        xsqlvars[10].sqltype = GDS.SQL_TEXT;
        xsqlvars[10].sqllen = 1;
        xsqlvars[10].sqlname = "FIXED_PREC_SCALE";
        xsqlvars[10].relname = "TYPEINFO";

        xsqlvars[11] = new XSQLVAR();
        xsqlvars[11].sqltype = GDS.SQL_TEXT;
        xsqlvars[11].sqllen = 1;
        xsqlvars[11].sqlname = "AUTO_INCREMENT";
        xsqlvars[11].relname = "TYPEINFO";

        xsqlvars[12] = new XSQLVAR();
        xsqlvars[12].sqltype = GDS.SQL_VARYING;
        xsqlvars[12].sqllen = 31;
        xsqlvars[12].sqlind = -1;
        xsqlvars[12].sqlname = "LOCAL_TYPE_NAME";
        xsqlvars[12].relname = "TYPEINFO";

        xsqlvars[13] = new XSQLVAR();
        xsqlvars[13].sqltype = GDS.SQL_SHORT;
        xsqlvars[13].sqlname = "MINIMUM_SCALE";
        xsqlvars[13].relname = "TYPEINFO";

        xsqlvars[14] = new XSQLVAR();
        xsqlvars[14].sqltype = GDS.SQL_SHORT;
        xsqlvars[14].sqlname = "MAXIMUM_SCALE";
        xsqlvars[14].relname = "TYPEINFO";

        xsqlvars[15] = new XSQLVAR();
        xsqlvars[15].sqltype = GDS.SQL_LONG;
        xsqlvars[15].sqlname = "SQL_DATA_TYPE";
        xsqlvars[15].relname = "TYPEINFO";

        xsqlvars[16] = new XSQLVAR();
        xsqlvars[16].sqltype = GDS.SQL_LONG;
        xsqlvars[16].sqlname = "SQL_DATETIME_SUB";
        xsqlvars[16].relname = "TYPEINFO";

        xsqlvars[17] = new XSQLVAR();
        xsqlvars[17].sqltype = GDS.SQL_LONG;
        xsqlvars[17].sqlname = "NUM_PREC_RADIX";
        xsqlvars[17].relname = "TYPEINFO";

        //dialect 3 only
        ArrayList rows = new ArrayList();
        /*
        rows.add(new Object[] {"SMALLINT", createShort(Types.BIT), new Integer(1), null, null, null,
            NULLABLE, CASEINSENSITIVE, PREDBASIC, UNSIGNED, FIXEDSCALE,
            NOTAUTOINC, null, shortZero, shortZero, new Integer(GDS.SQL_SHORT), null, BINARY});
        */

        /*
        rows.add(new Object[] {"TINYINT", new Short((short)-6), new Integer(8), null, null, null,
            NULLABLE, CASEINSENSITIVE, PREDBASIC, SIGNED, FIXEDSCALE,
            NOTAUTOINC, null, shortZero, shortZero, new Integer(GDS.SQL_SHORT), null, BINARY});
        */

        rows.add(new Object[] {"NUMERIC", createShort(Types.BIGINT), new Integer(18), null, null, null,
            NULLABLE, CASEINSENSITIVE, PREDBASIC, SIGNED, FIXEDSCALE,
            NOTAUTOINC, null, shortZero, shortZero, new Integer(GDS.SQL_INT64), null, BINARY});

        rows.add(new Object[] {"BLOB", createShort(Types.LONGVARBINARY), new Integer(0), null, null, null,
            NULLABLE, CASESENSITIVE, PREDNONE, UNSIGNED, FIXEDSCALE,
            NOTAUTOINC, null, shortZero, shortZero, new Integer(GDS.SQL_BLOB), null, BINARY});

        rows.add(new Object[] {"BLOB", createShort(Types.VARBINARY), new Integer(0), null, null, null,
            NULLABLE, CASESENSITIVE, PREDNONE, UNSIGNED, FIXEDSCALE,
            NOTAUTOINC, null, shortZero, shortZero, new Integer(GDS.SQL_BLOB), null, BINARY});

        rows.add(new Object[] {"BLOB", createShort(Types.BINARY), new Integer(0), null, null, null,
            NULLABLE, CASESENSITIVE, PREDNONE, UNSIGNED, FIXEDSCALE,
            NOTAUTOINC, null, shortZero, shortZero, new Integer(GDS.SQL_BLOB), null, BINARY});

        /*
                   //??should this be varchar???
        rows.add(new Object[] {"LONGVARCHAR", new Short((short)-2), new Integer(0), null, null, null,
            NULLABLE, CASESENSITIVE, PREDNONE, UNSIGNED, FIXEDSCALE,
            NOTAUTOINC, null, shortZero, shortZero, new Integer(GDS.SQL_BLOB), null, BINARY});
        */

        rows.add(new Object[] {"CHAR", createShort(Types.CHAR), new Integer(0), "'", "'", null,
            NULLABLE, CASESENSITIVE, SEARCHABLE, UNSIGNED, FIXEDSCALE,
            NOTAUTOINC, null, shortZero, shortZero, new Integer(GDS.SQL_TEXT), null, BINARY});

        rows.add(new Object[] {"NUMERIC", createShort(Types.NUMERIC), new Integer(18), null, null, null,
            NULLABLE, CASEINSENSITIVE, SEARCHABLE, SIGNED, FIXEDSCALE,
            NOTAUTOINC, null, shortZero, createShort(18), new Integer(GDS.SQL_INT64), null, BINARY});

        rows.add(new Object[] {"DECIMAL", createShort(Types.DECIMAL), new Integer(18), null, null, null,
            NULLABLE, CASEINSENSITIVE, SEARCHABLE, SIGNED, FIXEDSCALE,
            NOTAUTOINC, null, shortZero, createShort(18), new Integer(GDS.SQL_INT64), null, BINARY});

        rows.add(new Object[] {"INTEGER", createShort(Types.INTEGER), new Integer(32), null, null, null,
            NULLABLE, CASEINSENSITIVE, SEARCHABLE, SIGNED, FIXEDSCALE,
            NOTAUTOINC, null, shortZero, shortZero, new Integer(GDS.SQL_LONG), null, BINARY});

        rows.add(new Object[] {"SMALLINT", createShort(Types.SMALLINT), new Integer(16), null, null, null,
            NULLABLE, CASEINSENSITIVE, SEARCHABLE, SIGNED, FIXEDSCALE,
            NOTAUTOINC, null, shortZero, shortZero, new Integer(GDS.SQL_SHORT), null, BINARY});

        rows.add(new Object[] {"FLOAT", createShort(Types.FLOAT), new Integer(7), null, null, null,
            NULLABLE, CASEINSENSITIVE, SEARCHABLE, SIGNED, VARIABLESCALE,
            NOTAUTOINC, null, createShort(0), createShort(7), new Integer(GDS.SQL_FLOAT), null, BINARY});

        /*
        rows.add(new Object[] {"REAL", createShort((short)7), new Integer(32), null, null, null,
            NULLABLE, CASEINSENSITIVE, SEARCHABLE, SIGNED, VARIABLESCALE,
            NOTAUTOINC, null, new Short((short)-38), new Short((short)38), new Integer(GDS.SQL_FLOAT), null, BINARY});
        */

        rows.add(new Object[] {"DOUBLE PRECISION", createShort(Types.DOUBLE), new Integer(15), null, null, null,
            NULLABLE, CASEINSENSITIVE, SEARCHABLE, SIGNED, VARIABLESCALE,
            NOTAUTOINC, null, createShort(0), createShort(15), new Integer(GDS.SQL_DOUBLE), null, BINARY});

        rows.add(new Object[] {"VARCHAR", createShort(Types.VARCHAR), new Integer(0), "'", "'", null,
            NULLABLE, CASESENSITIVE, SEARCHABLE, UNSIGNED, FIXEDSCALE,
            NOTAUTOINC, null, shortZero, shortZero, new Integer(GDS.SQL_VARYING), null, BINARY});

        rows.add(new Object[] {"DATE", createShort(Types.DATE), new Integer(0), null, null, null,
            NULLABLE, CASEINSENSITIVE, SEARCHABLE, UNSIGNED, FIXEDSCALE,
            NOTAUTOINC, null, shortZero, shortZero, new Integer(GDS.SQL_TYPE_DATE), null, BINARY});

        rows.add(new Object[] {"TIME", createShort(Types.TIME), new Integer(0), null, null, null,
            NULLABLE, CASEINSENSITIVE, SEARCHABLE, UNSIGNED, FIXEDSCALE,
            NOTAUTOINC, null, shortZero, shortZero, new Integer(GDS.SQL_TYPE_TIME), null, BINARY});

        rows.add(new Object[] {"TIMESTAMP", createShort(Types.TIMESTAMP), new Integer(0), null, null, null,
            NULLABLE, CASEINSENSITIVE, SEARCHABLE, UNSIGNED, FIXEDSCALE,
            NOTAUTOINC, null, shortZero, shortZero, new Integer(GDS.SQL_TIMESTAMP), null, BINARY});

                 /*rows.add(new Object[] {"OTHER", new Short((short)1111), new Integer(0), null, null, null,
            NULLABLE, CASESENSITIVE, PREDNONE, UNSIGNED, FIXEDSCALE,
            NOTAUTOINC, null, shortZero, shortZero, new Integer(GDS.SQL_BLOB), null, BINARY});

        rows.add(new Object[] {"JAVA_OBJECT", new Short((short)2000), new Integer(0), null, null, null,
            NULLABLE, CASESENSITIVE, PREDNONE, UNSIGNED, FIXEDSCALE,
            NOTAUTOINC, null, shortZero, shortZero, new Integer(GDS.SQL_BLOB), null, BINARY});

        rows.add(new Object[] {"DISTINCT", new Short((short)2001), new Integer(0), null, null, null,
            NULLABLE, CASESENSITIVE, PREDNONE, UNSIGNED, FIXEDSCALE,
            NOTAUTOINC, null, shortZero, shortZero, new Integer(GDS.SQL_BLOB), null, BINARY});

        rows.add(new Object[] {"STRUCT", new Short((short)2002), new Integer(0), null, null, null,
            NULLABLE, CASESENSITIVE, PREDNONE, UNSIGNED, FIXEDSCALE,
            NOTAUTOINC, null, shortZero, shortZero, new Integer(GDS.SQL_BLOB), null, BINARY});

        rows.add(new Object[] {"ARRAY", new Short((short)2003), new Integer(0), null, null, null,
            NULLABLE, CASESENSITIVE, PREDNONE, UNSIGNED, FIXEDSCALE,
            NOTAUTOINC, null, shortZero, shortZero, new Integer(GDS.SQL_BLOB), null, BINARY});
                 */
        rows.add(new Object[] {"BLOB", createShort(Types.BLOB), new Integer(0), null, null, null,
            NULLABLE, CASESENSITIVE, PREDNONE, UNSIGNED, FIXEDSCALE,
            NOTAUTOINC, null, shortZero, shortZero, new Integer(GDS.SQL_BLOB), null, BINARY});

        /*
        rows.add(new Object[] {"CLOB", new Short((short)2005), new Integer(0), null, null, null,
            NULLABLE, CASESENSITIVE, PREDNONE, UNSIGNED, FIXEDSCALE,
            NOTAUTOINC, null, shortZero, shortZero, new Integer(GDS.SQL_BLOB), null, BINARY});
        */

                 /*rows.add(new Object[] {"REF", new Short((short)2006), new Integer(0), null, null, null,
            NULLABLE, CASESENSITIVE, PREDNONE, UNSIGNED, FIXEDSCALE,
            NOTAUTOINC, null, shortZero, shortZero, new Integer(GDS.SQL_BLOB), null, BINARY});*/

        //rows.add(null);
        // return new FBResultSet(xsqlvars, rows);
        return new FBResultSet(xsqlvars, rows);

    }



    /**
     * A possible value for column <code>NULLABLE</code> in the
     * <code>ResultSet</code> object returned by the method
     * <code>getTypeInfo</code>.
     * <p>Indicates that a <code>NULL</code> value is NOT allowed for this
     * data type.
     */
    //int typeNoNulls = 0;

    /**
     * A possible value for column <code>NULLABLE</code> in the
     * <code>ResultSet</code> object returned by the method
     * <code>getTypeInfo</code>.
     * <p>Indicates that a <code>NULL</code> value is allowed for this
     * data type.
     */
    //int typeNullable = 1;

    /**
     * A possible value for column <code>NULLABLE</code> in the
     * <code>ResultSet</code> object returned by the method
     * <code>getTypeInfo</code>.
     * <p>Indicates that it is not known whether a <code>NULL</code> value
     * is allowed for this data type.
     */
    //int typeNullableUnknown = 2;

    /**
     * A possible value for column <code>SEARCHABLE</code> in the
     * <code>ResultSet</code> object returned by the method
     * <code>getTypeInfo</code>.
     * <p>Indicates that <code>WHERE</code> search clauses are not supported
     * for this type.
     */
    //int typePredNone = 0;

    /**
     * A possible value for column <code>SEARCHABLE</code> in the
     * <code>ResultSet</code> object returned by the method
     * <code>getTypeInfo</code>.
     * <p>Indicates that the only <code>WHERE</code> search clause that can
     * be based on this type is <code>WHERE . . .LIKE</code>.
     */
    //int typePredChar = 1;

    /**
     * A possible value for column <code>SEARCHABLE</code> in the
     * <code>ResultSet</code> object returned by the method
     * <code>getTypeInfo</code>.
     * <p>Indicates that one can base all <code>WHERE</code> search clauses
     * except <code>WHERE . . .LIKE</code> on this data type.
     */
    //int typePredBasic = 2;

    /**
     * A possible value for column <code>SEARCHABLE</code> in the
     * <code>ResultSet</code> object returned by the method
     * <code>getTypeInfo</code>.
     * <p>Indicates that all <code>WHERE</code> search clauses can be
     * based on this type.
     */
    //int typeSearchable  = 3;

    /**
     * Gets a description of a table's indices and statistics. They are
     * ordered by NON_UNIQUE, TYPE, INDEX_NAME, and ORDINAL_POSITION.
     *
     * <P>Each index column description has the following columns:
     *  <OL>
     *  <LI><B>TABLE_CAT</B> String => table catalog (may be null)
     *  <LI><B>TABLE_SCHEM</B> String => table schema (may be null)
     *  <LI><B>TABLE_NAME</B> String => table name
     *  <LI><B>NON_UNIQUE</B> boolean => Can index values be non-unique?
     *      false when TYPE is tableIndexStatistic
     *  <LI><B>INDEX_QUALIFIER</B> String => index catalog (may be null);
     *      null when TYPE is tableIndexStatistic
     *  <LI><B>INDEX_NAME</B> String => index name; null when TYPE is
     *      tableIndexStatistic
     *  <LI><B>TYPE</B> short => index type:
     *      <UL>
     *      <LI> tableIndexStatistic - this identifies table statistics that are
     *           returned in conjuction with a table's index descriptions
     *      <LI> tableIndexClustered - this is a clustered index
     *      <LI> tableIndexHashed - this is a hashed index
     *      <LI> tableIndexOther - this is some other style of index
     *      </UL>
     *  <LI><B>ORDINAL_POSITION</B> short => column sequence number
     *      within index; zero when TYPE is tableIndexStatistic
     *  <LI><B>COLUMN_NAME</B> String => column name; null when TYPE is
     *      tableIndexStatistic
     *  <LI><B>ASC_OR_DESC</B> String => column sort sequence, "A" => ascending,
     *      "D" => descending, may be null if sort sequence is not supported;
     *      null when TYPE is tableIndexStatistic
     *  <LI><B>CARDINALITY</B> int => When TYPE is tableIndexStatistic, then
     *      this is the number of rows in the table; otherwise, it is the
     *      number of unique values in the index.
     *  <LI><B>PAGES</B> int => When TYPE is  tableIndexStatisic then
     *      this is the number of pages used for the table, otherwise it
     *      is the number of pages used for the current index.
     *  <LI><B>FILTER_CONDITION</B> String => Filter condition, if any.
     *      (may be null)
     *  </OL>
     *
     * @param catalog a catalog name; "" retrieves those without a
     * catalog; null means drop catalog name from the selection criteria
     * @param schema a schema name; "" retrieves those without a schema
     * @param table a table name
     * @param unique when true, return only indices for unique values;
     *     when false, return indices regardless of whether unique or not
     * @param approximate when true, result is allowed to reflect approximate
     *     or out of data values; when false, results are requested to be
     *     accurate
     * @return <code>ResultSet</code> - each row is an index column description
     * @exception SQLException if a database access error occurs
     */
    public ResultSet getIndexInfo(String catalog, String schema, String table,
        boolean unique, boolean approximate) throws SQLException {
        throw new SQLException("Not yet implemented");
    }



    /**
     * A possible value for column <code>TYPE</code> in the
     * <code>ResultSet</code> object returned by the method
     * <code>getIndexInfo</code>.
     * <P>Indicates that this column contains table statistics that
     * are returned in conjunction with a table's index descriptions.
     */
    short tableIndexStatistic = 0;

    /**
     * A possible value for column <code>TYPE</code> in the
     * <code>ResultSet</code> object returned by the method
     * <code>getIndexInfo</code>.
     * <P>Indicates that this table index is a clustered index.
     */
    short tableIndexClustered = 1;

    /**
     * A possible value for column <code>TYPE</code> in the
     * <code>ResultSet</code> object returned by the method
     * <code>getIndexInfo</code>.
     * <P>Indicates that this table index is a hashed index.
     */
    short tableIndexHashed    = 2;

    /**
     * A possible value for column <code>TYPE</code> in the
     * <code>ResultSet</code> object returned by the method
     * <code>getIndexInfo</code>.
     * <P>Indicates that this table index is not a clustered
     * index, a hashed index, or table statistics;
     * it is something other than these.
     */
    short tableIndexOther     = 3;

    //--------------------------JDBC 2.0-----------------------------

    /**
     * Does the database support the given result set type?
     *
     * @param type defined in <code>java.sql.ResultSet</code>
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @see Connection
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
    public boolean supportsResultSetType(int type) throws SQLException {
        throw new SQLException("Not yet implemented");
    }



    /**
     * Does the database support the concurrency type in combination
     * with the given result set type?
     *
     * @param type defined in <code>java.sql.ResultSet</code>
     * @param concurrency type defined in <code>java.sql.ResultSet</code>
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @see Connection
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
    public boolean supportsResultSetConcurrency(int type, int concurrency) throws SQLException {
        throw new SQLException("Not yet implemented");
    }



    /**
     *
     * Indicates whether a result set's own updates are visible.
     *
     * @param result set type, i.e. ResultSet.TYPE_XXX
     * @return <code>true</code> if updates are visible for the result set type;
     *        <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
    public boolean ownUpdatesAreVisible(int type) throws SQLException {
        throw new SQLException("Not yet implemented");
    }



    /**
     *
     * Indicates whether a result set's own deletes are visible.
     *
     * @param result set type, i.e. ResultSet.TYPE_XXX
     * @return <code>true</code> if deletes are visible for the result set type;
     *        <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
    public boolean ownDeletesAreVisible(int type) throws SQLException {
        throw new SQLException("Not yet implemented");
    }



    /**
     *
     * Indicates whether a result set's own inserts are visible.
     *
     * @param result set type, i.e. ResultSet.TYPE_XXX
     * @return <code>true</code> if inserts are visible for the result set type;
     *        <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
    public boolean ownInsertsAreVisible(int type) throws SQLException {
        throw new SQLException("Not yet implemented");
    }



    /**
     *
     * Indicates whether updates made by others are visible.
     *
     * @param result set type, i.e. ResultSet.TYPE_XXX
     * @return <code>true</code> if updates made by others
     * are visible for the result set type;
     *        <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
    public boolean othersUpdatesAreVisible(int type) throws SQLException {
        throw new SQLException("Not yet implemented");
    }



    /**
     *
     * Indicates whether deletes made by others are visible.
     *
     * @param result set type, i.e. ResultSet.TYPE_XXX
     * @return <code>true</code> if deletes made by others
     * are visible for the result set type;
     *        <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
    public boolean othersDeletesAreVisible(int type) throws SQLException {
        throw new SQLException("Not yet implemented");
    }



    /**
     *
     * Indicates whether inserts made by others are visible.
     *
     * @param result set type, i.e. ResultSet.TYPE_XXX
     * @return true if updates are visible for the result set type
     * @return <code>true</code> if inserts made by others
     * are visible for the result set type;
     *        <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
    public boolean othersInsertsAreVisible(int type) throws SQLException {
        throw new SQLException("Not yet implemented");
    }



    /**
     *
     * Indicates whether or not a visible row update can be detected by
     * calling the method <code>ResultSet.rowUpdated</code>.
     *
     * @param result set type, i.e. ResultSet.TYPE_XXX
     * @return <code>true</code> if changes are detected by the result set type;
     *         <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
    public boolean updatesAreDetected(int type) throws SQLException {
        throw new SQLException("Not yet implemented");
    }



    /**
     *
     * Indicates whether or not a visible row delete can be detected by
     * calling ResultSet.rowDeleted().  If deletesAreDetected()
     * returns false, then deleted rows are removed from the result set.
     *
     * @param result set type, i.e. ResultSet.TYPE_XXX
     * @return true if changes are detected by the resultset type
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
    public boolean deletesAreDetected(int type) throws SQLException {
        throw new SQLException("Not yet implemented");
    }



    /**
     *
     * Indicates whether or not a visible row insert can be detected
     * by calling ResultSet.rowInserted().
     *
     * @param result set type, i.e. ResultSet.TYPE_XXX
     * @return true if changes are detected by the resultset type
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
    public boolean insertsAreDetected(int type) throws SQLException {
        throw new SQLException("Not yet implemented");
    }



    /**
     *
     * Indicates whether the driver supports batch updates.
     * @return true if the driver supports batch updates; false otherwise
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
    public boolean supportsBatchUpdates() throws SQLException {
        return false;
    }



    /**
     *
     * Gets a description of the user-defined types defined in a particular
     * schema.  Schema-specific UDTs may have type JAVA_OBJECT, STRUCT,
     * or DISTINCT.
     *
     * <P>Only types matching the catalog, schema, type name and type
     * criteria are returned.  They are ordered by DATA_TYPE, TYPE_SCHEM
     * and TYPE_NAME.  The type name parameter may be a fully-qualified
     * name.  In this case, the catalog and schemaPattern parameters are
     * ignored.
     *
     * <P>Each type description has the following columns:
     *  <OL>
     *  <LI><B>TYPE_CAT</B> String => the type's catalog (may be null)
     *  <LI><B>TYPE_SCHEM</B> String => type's schema (may be null)
     *  <LI><B>TYPE_NAME</B> String => type name
     *  <LI><B>CLASS_NAME</B> String => Java class name
     *  <LI><B>DATA_TYPE</B> String => type value defined in java.sql.Types.
     *  One of JAVA_OBJECT, STRUCT, or DISTINCT
     *  <LI><B>REMARKS</B> String => explanatory comment on the type
     *  </OL>
     *
     * <P><B>Note:</B> If the driver does not support UDTs, an empty
     * result set is returned.
     *
     * @param catalog a catalog name; "" retrieves those without a
     * catalog; null means drop catalog name from the selection criteria
     * @param schemaPattern a schema name pattern; "" retrieves those
     * without a schema
     * @param typeNamePattern a type name pattern; may be a fully-qualified
     * name
     * @param types a list of user-named types to include (JAVA_OBJECT,
     * STRUCT, or DISTINCT); null returns all types
     * @return <code>ResultSet</code> - each row is a type description
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
    public ResultSet getUDTs(String catalog, String schemaPattern,
              String typeNamePattern, int[] types) throws SQLException {
        throw new SQLException("Not yet implemented");
    }



    /**
     * Retrieves the connection that produced this metadata object.
     *
     * @return the connection that produced this metadata object
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
    public Connection getConnection() throws SQLException {
        return c;
    }

    //jdbc 3 methods


    /**
     *
     * @param param1 <description>
     * @param param2 <description>
     * @param param3 <description>
     * @param param4 <description>
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public ResultSet getAttributes(String param1, String param2, String param3, String param4) throws SQLException {
        // TODO: implement this java.sql.DatabaseMetaData method
        throw new SQLException("not yet supported");
    }

    /**
     *
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public boolean supportsSavepoints() throws SQLException {
        // TODO: implement this java.sql.DatabaseMetaData method
        throw new SQLException("not yet supported");
    }

    /**
     *
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public boolean supportsNamedParameters() throws SQLException {
        return false;
    }

    /**
     *
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public boolean supportsMultipleOpenResults() throws SQLException {
        return false;
    }

    /**
     *
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public boolean supportsGetGeneratedKeys() throws SQLException {
        // TODO: implement this java.sql.DatabaseMetaData method
        throw new SQLException("not yet supported");
    }


    /**
     *
     * @param param1 <description>
     * @param param2 <description>
     * @param param3 <description>
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public ResultSet getSuperTypes(String param1, String param2, String param3) throws SQLException {
        // TODO: implement this java.sql.DatabaseMetaData method
        throw new SQLException("not yet supported");
    }

    /**
     *
     * @param param1 <description>
     * @param param2 <description>
     * @param param3 <description>
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public ResultSet getSuperTables(String param1, String param2, String param3) throws SQLException {
        // TODO: implement this java.sql.DatabaseMetaData method
        throw new SQLException("not yet supported");
    }

    /**
     *
     * @param param1 <description>
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public boolean supportsResultSetHoldability(int param1) throws SQLException {
        // TODO: implement this java.sql.DatabaseMetaData method
        throw new SQLException("not yet supported");
    }

    /**
     *
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public int getResultSetHoldability() throws SQLException {
        // TODO: implement this java.sql.DatabaseMetaData method
        throw new SQLException("not yet supported");
    }

    /**
     *
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public int getDatabaseMajorVersion() throws SQLException {
        // TODO: implement this java.sql.DatabaseMetaData method
        throw new SQLException("not yet supported");
    }

    /**
     *
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public int getDatabaseMinorVersion() throws SQLException {
        // TODO: implement this java.sql.DatabaseMetaData method
        throw new SQLException("not yet supported");
    }

    /**
     *
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public int getJDBCMajorVersion() throws SQLException {
        // TODO: implement this java.sql.DatabaseMetaData method
        throw new SQLException("not yet supported");
    }

    /**
     *
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public int getJDBCMinorVersion() throws SQLException {
        // TODO: implement this java.sql.DatabaseMetaData method
        throw new SQLException("not yet supported");
    }

    /**
     *
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public int getSQLStateType() throws SQLException {
        // TODO: implement this java.sql.DatabaseMetaData method
        throw new SQLException("not yet supported");
    }

    //private
    private boolean isAllCondition(String pattern) {
        if ("%".equals(pattern)) {
            //asks for everything, no condition needed
            return true;
        }
        else {
            return false;
        }
    }


    public boolean hasNoWildcards(String pattern) {
        int scannedTo = 0;
        int pos;
        while ((pos = pattern.indexOf('%', scannedTo)) < pattern.length()) {
            if (pos == -1) {
                break;
            }
            if ((pos == 0) || (pattern.charAt(pos - 1) != '\\')) {
                return false;
            }
            scannedTo = ++pos;
        }
        scannedTo = 0;
        while ((pos = pattern.indexOf('_', scannedTo)) < pattern.length()) {
            if (pos == -1) {
                break;
            }
            if ((pos == 0) || (pattern.charAt(pos - 1) != '\\')) {
                return false;
            }
            scannedTo = ++pos;
        }
        return true;
    }

    public String stripEscape(String pattern) {
        StringBuffer stripped = new StringBuffer(pattern.length());
        for (int pos = 0; pos < pattern.length(); pos++) {
            if (pattern.charAt(pos) != '\\') {
                stripped.append(pattern.charAt(pos));
            }
        }
        return stripped.toString();
    }

    private String getWantsSystemTables(String[] types) {
        for (int i = 0; i < types.length; i++) {
            if (SYSTEM_TABLE.equals(types[i])) {
                return "T";
            }
        }
        return "F";
    }

    private String getWantsTables(String[] types) {
        for (int i = 0; i < types.length; i++) {
            if (TABLE.equals(types[i])) {
                return "T";
            }
        }
        return "F";
    }

    private String getWantsViews(String[] types) {
        for (int i = 0; i < types.length; i++) {
            if (VIEW.equals(types[i])) {
                return "T";
            }
        }
        return "F";
    }

    public String stripQuotes(String pattern) {
        if ((pattern.length() >= 2)
            && (pattern.charAt(0) == '\"')
            && (pattern.charAt(pattern.length() - 1) == '\"'))
        {
            return pattern.substring(1, pattern.length() - 1);
        }
        else {
            return pattern.toUpperCase();
        }
    }

    private PreparedStatement getStatement(String sql) throws SQLException {
        PreparedStatement s = (PreparedStatement)statements.get(sql);
        if (s == null) {
            s = c.prepareStatement(sql);
            statements.put(sql, s);
        }
        return s;
    }

    private ResultSet doQuery(String sql, List params) throws SQLException {
        boolean ourTransaction = false;
        if (!c.inTransaction()) {
            try {
                trans.begin();
                ourTransaction = true;
            }
            catch (ResourceException re) {
                throw new SQLException("couldn't work with local transaction: " + re);
            }
        }
        PreparedStatement s = getStatement(sql);
        for (int i = 0; i < params.size(); i++) {
            s.setString(i + 1, (String)params.get(i));
        }
        ResultSet rs = null;
        try {
            s.execute();
            rs = ((FBStatement)s).getCachedResultSet(true); //trim strings
        }
        finally {
            if (ourTransaction) {
                try {
                    trans.commit();
                }
                catch (ResourceException re) {
                    throw new SQLException("couldn't work with local transaction: " + re);
                }
            }
        }
        return rs;
    }

    private void checkCatalogAndSchema(String catalog, String schema) throws SQLException {
        if (catalog != null) {
            throw new SQLException("Catalogs not supported");
        }
        if (schema != null) {
            throw new SQLException("Schemas not supported");
        }
    }

    private class Clause {
        private String condition = "";
        private String value;

        public Clause (String columnName, String pattern) {
            if (pattern == null) {
                return;
            }
            else if (isAllCondition(pattern)) {
                //do nothing to tableCondition
                return;
            }
            else if (hasNoWildcards(pattern)) {
                value = stripQuotes(stripEscape(pattern));
                condition = columnName + " = ? and ";
            }
            else {
                value = stripQuotes(pattern) + SPACES + "%";
                condition = columnName + " || '" + SPACES + "' like ? escape '\\' and ";
            }
        }

        public String getCondition() {
            return condition;
        }

        public String getValue() {
            return value;
        }
    }

}




