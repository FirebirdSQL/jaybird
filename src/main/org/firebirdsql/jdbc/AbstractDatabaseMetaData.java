/*
 * Firebird Open Source J2ee connector - jdbc driver
 *
 * Distributable under LGPL license.
 * You may obtain a copy of the License at http://www.gnu.org/copyleft/lgpl.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * LGPL License for more details.
 *
 * This file was created by members of the firebird development team.
 * All individual contributions remain the Copyright (C) of those
 * individuals.  Contributors to this file are either listed here or
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */

package org.firebirdsql.jdbc;


import java.sql.*;
import java.util.*;

import org.firebirdsql.gds.*;
import org.firebirdsql.gds.impl.AbstractGDS;
import org.firebirdsql.gds.impl.AbstractIscDbHandle;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

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
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public abstract class AbstractDatabaseMetaData implements FirebirdDatabaseMetaData {

    private final static Logger log = LoggerFactory.getLogger(FBDatabaseMetaData.class,false);
    public static final String SPACES = "                               ";//31 spaces

    private GDSHelper gdsHelper;
    private AbstractConnection connection;

    HashMap statements = new HashMap();

    //PreparedStatement tables = null;

    protected AbstractDatabaseMetaData(GDSHelper gdsHelper) {
        this.gdsHelper = gdsHelper;
    }
    
    protected AbstractDatabaseMetaData(AbstractConnection c) throws GDSException {
        this.gdsHelper = c.getGDSHelper();
        this.connection = c;
    }

    protected void close() {
        try {
            Iterator i = statements.values().iterator();
            while(i.hasNext()) {
                AbstractStatement stmt = (AbstractPreparedStatement)i.next();
                if (!stmt.isClosed())
                    stmt.close();
            }
            statements.clear();
            /*if (tables != null) {
                tables.close();
                tables = null;
                }*/
        }
        catch (SQLException e) {
           if (log!=null) log.warn("error in DatabaseMetaData.close", e);
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
        AbstractGDS gds = ((AbstractGDS) connection.getInternalAPIHandler());
        
        return GDSFactory.getJdbcUrl(gds.getType(), connection.mc.getDatabase());
    }


    /**
     * What's our user name as known to the database?
     * 
     * @return our database user name
     * @exception SQLException
     *                if a database access error occurs
     */
    public  String getUserName() throws SQLException {
        return gdsHelper.getUserName();
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
        // in Firebird 1.5.x NULLs are always sorted at the end
        // in Firebird 2.0.x NULLs are sorted low
        if (getDatabaseMajorVersion() == 1) {
            return false;
        } else
        if (getDatabaseMajorVersion() == 2) {
            return false;
        } else
            throw new FBDriverNotCapableException();
    }


    /**
     * Are NULL values sorted low?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean nullsAreSortedLow() throws SQLException {
        // in Firebird 1.5.x NULLs are always sorted at the end
        // in Firebird 2.0.x NULLs are sorted low
        if (getDatabaseMajorVersion() == 1) {
            return false;
        } else
        if (getDatabaseMajorVersion() == 2) {
            return true;
        } else
            throw new FBDriverNotCapableException();
    }


    /**
     * Are NULL values sorted at the start regardless of sort order?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean nullsAreSortedAtStart() throws SQLException {
        // in Firebird 1.5.x NULLs are always sorted at the end
        // in Firebird 2.0.x NULLs are sorted low
        if (getDatabaseMajorVersion() == 1) {
            return false;
        } else
        if (getDatabaseMajorVersion() == 2) {
            return false;
        } else
            throw new FBDriverNotCapableException();
    }


    /**
     * Are NULL values sorted at the end regardless of sort order?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean nullsAreSortedAtEnd() throws SQLException {
        // in Firebird 1.5.x NULLs are always sorted at the end
        // in Firebird 2.0.x NULLs are sorted low
        if (getDatabaseMajorVersion() == 1) {
            return true;
        } else
        if (getDatabaseMajorVersion() == 2) {
            return false;
        } else
            throw new FBDriverNotCapableException();
    }


    /**
     * What's the name of this database product?
     *
     * @return database product name
     * @exception SQLException if a database access error occurs
     */
    public  String getDatabaseProductName() throws SQLException {
        return gdsHelper.getDatabaseProductName();
    }


    /**
     * What's the version of this database product?
     *
     * @return database version
     * @exception SQLException if a database access error occurs
     */
    public  String getDatabaseProductVersion() throws SQLException {
        return gdsHelper.getDatabaseProductVersion();
    }


    /**
     * What's the name of this JDBC driver?
     *
     * @return JDBC driver name
     * @exception SQLException if a database access error occurs
     */
    public  String getDriverName() throws SQLException {
        return "Jaybird JCA/JDBC driver";
    }


    /**
     * What's the version of this JDBC driver?
     *
     * @return JDBC driver version
     * @exception SQLException if a database access error occurs
     */
    public  String getDriverVersion() throws SQLException {
        return "2.1";
    }


    /**
     * What's this JDBC driver's major version number?
     *
     * @return JDBC driver major version
     */
    public  int getDriverMajorVersion() {
        return 2;
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
     * TODO implement statemet pooling on the server.. then in the driver
     */
    public boolean supportsStatementPooling() throws SQLException {
        return false;
    }

    /**
     * Describe <code>locatorsUpdateCopy</code> method here.
     *
     * @return a <code>boolean</code> value
     * @exception SQLException if an error occurs
     * TODO find out what this even means
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
     * Describe constant <code>fbSQLKeywords</code> here.
     * Derived from firebird2/src/dsql/keywords.cpp.
     * Exclusions from list of sql-92 keywords in SQL Instant Reference,
     * Martin Gruber (1993) Sybex.
     *
     */
    private final static String fbSQLKeywords =
    //"ACTION," +
    "ACTIVE," +
    //"ADD," +
    "ADMIN," +
    "AFTER," +
    //"ALL," +
    //"ALTER," + 
    //"AND," + 
    //"ANY," + 
    //"AS," + 
    //"ASC," +    /* Alias of ASCENDING */
    "ASCENDING," +
    //"AT," +
    "AUTO," +
    //"AVG," +
    "BASE_NAME," +
    "BEFORE," +
    //"BEGIN," +
    //"BETWEEN," +
    "BIGINT," +
    "BLOB," +
    "BREAK," + 
    //"BY," +
    "CACHE," +
    //"CASCADE," +
    //"CASE," +
    //"CAST," +
    //"CHAR," +
    //"CHARACTER," +
    //"CHECK," +
    "CHECK_POINT_LENGTH," +
    //"COALESCE," +
    //"COLLATE," +
    //"COLUMN," +
    //"COMMIT," +
    //"COMMITTED," +
    "COMPUTED," +
    "CONDITIONAL," +
    "CONNECTION_ID," +
    //"CONSTRAINT," +
    "CONTAINING," +
    //"COUNT," +
    //"CREATE," +
    "CSTRING," +
    //"CURRENT," +
    //"CURRENT_DATE," +
    "CURRENT_ROLE," +
    //"CURRENT_TIME," +
    //"CURRENT_TIMESTAMP," +
    //"CURRENT_USER," +
    //"CURSOR," +
    "DATABASE," +
    //"DATE," +
    //"DAY," +
    "DEBUG," +
    //"DEC," +
    //"DECIMAL," +
    //"DECLARE," +
    //"DEFAULT," + 
    //"DELETE," + 
    //"DESC," +    /* Alias of DESCENDING */
    "DESCENDING," +
    //"DESCRIPTOR," +
    //"DISTINCT," +
    "DO," +
    //"DOMAIN," +
    //"DOUBLE," +
    //"DROP," +
    //"ELSE," +
    //"END," +
    "ENTRY_POINT," +
    //"ESCAPE," +
    //"EXCEPTION," +
    //"EXECUTE," +
    //"EXISTS," +
    "EXIT," +
    //"EXTERNAL," +
    //"EXTRACT," +
    "FILE," +
    "FILTER," +
    //"FIRST," +
    //"FLOAT," +
    //"FOR," +
    //"FOREIGN," +
    "FREE_IT," +
    //"FROM," +
    //"FULL," +
    "FUNCTION," +
    "GDSCODE," +
    "GENERATOR," +
    "GEN_ID," +
    //"GRANT," +
    //"GROUP," +
    "GROUP_COMMIT_WAIT_TIME," +
    //"HAVING," +
    //"HOUR," +
    "IF," +
    //"IN," +
    "INACTIVE," +
    "INDEX," +
    //"INNER," +
    "INPUT_TYPE," +
    //"INSERT," +
    //"INT," +
    //"INTEGER," +
    //"INTO," +
    //"IS," +
    //"ISOLATION," +
    //"JOIN," +
    //"KEY," +
    //"LAST," +
    //"LEFT," +
    //"LENGTH," +
    //"LEVEL," +
    //"LIKE," +
    "LOGFILE," +
    "LOG_BUFFER_SIZE," +
    "LONG," +
    "MANUAL," +
    //"MAX," +
    "MAXIMUM_SEGMENT," +
    "MERGE," +
    "MESSAGE," +
    //"MIN," +
    //"MINUTE," +
    "MODULE_NAME," +
    //"MONTH," +
    //"NAMES," +
    //"NATIONAL," +
    //"NATURAL," +
    //"NCHAR," +
    //"NO," +
    //"NOT," +
    //"NULLIF," +
    //"NULL," +
    "NULLS," +
    "LOCK," +
    //"NUMERIC," +
    "NUM_LOG_BUFFERS," +
    //"OF," +
    //"ON," +
    //"ONLY," +
    //"OPTION," +
    //"OR," +
    //"ORDER," +
    //"OUTER," +
    "OUTPUT_TYPE," +
    "OVERFLOW," +
    "PAGE," +
    "PAGES," +
    "PAGE_SIZE," +
    "PARAMETER," +
    "PASSWORD," +
    "PLAN," +
    //"POSITION," +
    "POST_EVENT," +
    //"PRECISION," +
    //"PRIMARY," +
    //"PRIVILEGES," +
    //"PROCEDURE," +
    "PROTECTED," +
    "RAW_PARTITIONS," +
    "RDB$DB_KEY," +
    //"READ," +
    //"REAL," +
    "RECORD_VERSION," +
    "RECREATE," +
    //"REFERENCES," + 
    "RESERV," +    /* Alias of RESERVING */
    "RESERVING," +
    //"RESTRICT," +
    "RETAIN," +
    "RETURNING_VALUES," +
    "RETURNS," +
    //"REVOKE," +
    //"RIGHT," +
    "ROLE," + 
    //"ROLLBACK," + 
    "ROWS_AFFECTED," +
    "SAVEPOINT," +
    //"SCHEMA," +    /* Alias of DATABASE */
    //"SECOND," +
    "SEGMENT," +
    //"SELECT," +
    //"SET," +
    "SHADOW," +
    "SHARED," +
    "SINGULAR," +
    //"SIZE," +
    "SKIP," +
    //"SMALLINT," +
    "SNAPSHOT," +
    //"SOME," +
    "SORT," +
    //"SQLCODE," +
    "STABILITY," + 
    "STARTING," + 
    "STARTS," +    /* Alias of STARTING */
    "STATISTICS," +
    //"SUBSTRING," +
    "SUB_TYPE," +
    //"SUM," +
    "SUSPEND," +
    //"TABLE," +
    //"THEN," +
    //"TIME," +
    //"TIMESTAMP," +
    //"TO," +
    //"TRANSACTION," +
    "TRANSACTION_ID," +
    "TRIGGER," +
    //"TYPE," +
    //"UNCOMMITTED," +
    //"UNION," +
    //"UNIQUE," +
    //"UPDATE," +
    //"UPPER," +
    //"USER," +
    //"USING," +
    //"VALUE," +
    //"VALUES," +
    //"VARCHAR," +
    "VARIABLE," +
    //"VARYING," +
    //"VIEW," +
    "WAIT," +
    "WEEKDAY," +
    //"WHEN," +
    //"WHERE," +
    "WHILE," +
    //"WITH," +
    //"WORK," +
    //"WRITE," + 
    //"YEAR," + 
    "YEARDAY"; 


    /**
     * Gets a comma-separated list of all a database's SQL keywords
     * that are NOT also SQL92 keywords.
     *
     * @return the list
     * @exception SQLException if a database access error occurs
     */
    public  String getSQLKeywords() throws SQLException {
        return fbSQLKeywords;
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
        return "SUBSTRING,UCASE,CONCAT";
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
    }


    /**
     * Gets a comma-separated list of time and date functions.
     *
     * @return the list
     * @exception SQLException if a database access error occurs
     */
    public  String getTimeDateFunctions() throws SQLException {
        return "CURDATE,CURTIME,DAYOFYEAR,HOUR,MINUTE,MONTH,NOW,SECOND,YEAR";
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
     * Retrieves whether concatenations between NULL and non-NULL values 
     * equal NULL. For SQL-92 compliance, a JDBC technology-enabled driver will
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
    }


    /**
     * Retrieves whether CONVERT between the given SQL types supported.
     *
     * @param fromType the type to convert from
     * @param toType the type to convert to
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @see Types
     */
    public  boolean supportsConvert(int fromType, int toType) throws SQLException {
        return false;//don't know
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
        return false; //lets see what the tests say
    }


    /**
     * Is the ANSI92 full SQL grammar supported?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsANSI92FullSQL() throws SQLException {
        return false; //Nah, but lets see what the tests say
    }


    /**
     * Is the SQL Integrity Enhancement Facility supported?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsIntegrityEnhancementFacility() throws SQLException {
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
     * @return the vendor term, always <code>null</code> because
     * schemas are not supported by database server (see JDBC CTS
     * for details).
     * @exception SQLException if a database access error occurs
     */
    public  String getSchemaTerm() throws  SQLException {
        return null;
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
     * @return the vendor term, always <code>null</code> because
     * catalogs are not supported by database server (see JDBC CTS
     * for details).
     *
     * @exception SQLException if a database access error occurs
     */
    public  String getCatalogTerm() throws  SQLException {
        return null;
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
     * @return the separator string, always <code>null</code> because
     * catalogs are not supported by database server (see JDBC CTS
     * for details).

     * @exception SQLException if a database access error occurs
     */
    public  String getCatalogSeparator() throws SQLException {
        return null;
    }


    /**
     * Can a schema name be used in a data manipulation statement?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     *
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
        return true;//commit retaining only.
    }


    /**
     * Can statements remain open across rollbacks?
     *
     * @return <code>true</code> if statements always remain open;
     *       <code>false</code> if they might not remain open
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsOpenStatementsAcrossRollback() throws SQLException {
        return true;//commit retaining only.
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
        return 32767;
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
        return 32767; //I don't know
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
        if (getDatabaseMajorVersion() >= 1 && getDatabaseMinorVersion() >= 5)
            return 65531;
        else 
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
        return Connection.TRANSACTION_READ_COMMITTED;//close enough to snapshot.
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

    private static final String GET_PROCEDURES_START = "select "
        + " RDB$PROCEDURE_NAME as PROCEDURE_NAME,"
        + " RDB$DESCRIPTION as REMARKS,"
        + " RDB$PROCEDURE_OUTPUTS as PROCEDURE_TYPE "
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

        XSQLVAR[] xsqlvars = new XSQLVAR[8];

        xsqlvars[0] = new XSQLVAR();
        xsqlvars[0].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[0].sqllen = 31;
        xsqlvars[0].sqlname = "PROCEDURE_CAT";
        xsqlvars[0].relname = "RDB$PROCEDURES";

        xsqlvars[1] = new XSQLVAR();
        xsqlvars[1].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[1].sqllen = 31;
        xsqlvars[1].sqlname = "PROCEDURE_SCHEM";
        xsqlvars[1].relname = "RDB$PROCEDURES";

        xsqlvars[2] = new XSQLVAR();
        xsqlvars[2].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[2].sqllen = 31;
        xsqlvars[2].sqlname = "PROCEDURE_NAME";
        xsqlvars[2].relname = "RDB$PROCEDURES";

        xsqlvars[3] = new XSQLVAR();
        xsqlvars[3].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[3].sqllen = 31;
        xsqlvars[3].sqlname = "FUTURE1";
        xsqlvars[3].relname = "RDB$PROCEDURES";

        xsqlvars[4] = new XSQLVAR();
        xsqlvars[4].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[4].sqllen = 31;
        xsqlvars[4].sqlname = "FUTURE2";
        xsqlvars[4].relname = "RDB$PROCEDURES";

        xsqlvars[5] = new XSQLVAR();
        xsqlvars[5].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[5].sqllen = 31;
        xsqlvars[5].sqlname = "FUTURE3";
        xsqlvars[5].relname = "RDB$PROCEDURES";

        xsqlvars[6] = new XSQLVAR();
        xsqlvars[6].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[6].sqllen = 80; // gets updated if there are longer remarks.
        xsqlvars[6].sqlname = "REMARKS";
        xsqlvars[6].relname = "RDB$PROCEDURES";

        xsqlvars[7] = new XSQLVAR();
        xsqlvars[7].sqltype = ISCConstants.SQL_SHORT;
        xsqlvars[7].sqlname = "PROCEDURE_TYPE";
        xsqlvars[7].relname = "RDB$PROCEDURES";

        Clause procedureClause = new Clause("RDB$PROCEDURE_NAME", procedureNamePattern);
        
        String sql = GET_PROCEDURES_START;
        sql += procedureClause.getCondition();
        sql += GET_PROCEDURES_END;
        
        // check the original case identifiers first
        ArrayList params = new ArrayList();
        if (!procedureClause.getCondition().equals("")) {
            params.add(procedureClause.getOriginalCaseValue());
        }

        ResultSet rs = doQuery(sql, params);
        ArrayList rows = new ArrayList();
        
        // if nothing found, check the uppercased identifiers
        if (!rs.next()) {
            params.clear();
            if (!procedureClause.getCondition().equals("")) {
                params.add(procedureClause.getValue());
            }
            
            rs = doQuery(sql, params);
            
            // if nothing found, return an empty result set
            if (!rs.next())
                return new FBResultSet(xsqlvars, rows);
        }
        
        do {
            byte[][] row = new byte[8][];
            row[0] = null;
            row[1] = null;
            row[2] = getBytes(rs.getString("PROCEDURE_NAME").trim());
            row[3] = null;
            row[4] = null;
            row[5] = null;
            String remarks = rs.getString("REMARKS");
            row[6] = getBytes(remarks);
            if (remarks != null && remarks.length() > xsqlvars[6].sqllen)
                xsqlvars[6].sqllen = remarks.length();
            short procedureType = rs.getShort("PROCEDURE_TYPE");
            row[7] = (procedureType == 0) ? xsqlvars[0].encodeShort((short)procedureNoResult) : xsqlvars[0].encodeShort((short)procedureReturnsResult);
            rows.add(row);
        } while (rs.next());
        
        return new FBResultSet(xsqlvars, rows);
    }


    private static final String GET_PROCEDURE_COLUMNS_START = "select"
        + " PP.RDB$PROCEDURE_NAME as PROCEDURE_NAME,"
        + " PP.RDB$PARAMETER_NAME as COLUMN_NAME,"
        + " PP.RDB$PARAMETER_TYPE as COLUMN_TYPE,"
        + " F.RDB$FIELD_TYPE as FIELD_TYPE,"
        + " F.RDB$FIELD_SUB_TYPE as FIELD_SUB_TYPE,"
        + " F.RDB$FIELD_PRECISION as FIELD_PRECISION,"
        + " F.RDB$FIELD_SCALE as FIELD_SCALE,"
        + " F.RDB$FIELD_LENGTH as FIELD_LENGTH,"
        + " F.RDB$NULL_FLAG as NULL_FLAG,"
        + " PP.RDB$DESCRIPTION as REMARKS "
        + "from"
        + " RDB$PROCEDURE_PARAMETERS PP,"
        + " RDB$FIELDS F "
        + "where ";
    private static final String GET_PROCEDURE_COLUMNS_END =  " PP.RDB$FIELD_SOURCE = F.RDB$FIELD_NAME "
        + "order by"
        + " PP.RDB$PROCEDURE_NAME,"
        + " PP.RDB$PARAMETER_TYPE desc,"
        + " PP.RDB$PARAMETER_NUMBER ";

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

        XSQLVAR[] xsqlvars = new XSQLVAR[13];

        xsqlvars[0] = new XSQLVAR();
        xsqlvars[0].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[0].sqllen = 31;
        xsqlvars[0].sqlname = "PROCEDURE_CAT";
        xsqlvars[0].relname = "COLUMNINFO";

        xsqlvars[1] = new XSQLVAR();
        xsqlvars[1].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[1].sqllen = 31;
        xsqlvars[1].sqlname = "PROCEDURE_SCHEM";
        xsqlvars[1].relname = "COLUMNINFO";

        xsqlvars[2] = new XSQLVAR();
        xsqlvars[2].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[2].sqllen = 31;
        xsqlvars[2].sqlname = "PROCEDURE_NAME";
        xsqlvars[2].relname = "COLUMNINFO";

        xsqlvars[3] = new XSQLVAR();
        xsqlvars[3].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[3].sqllen = 31;
        xsqlvars[3].sqlname = "COLUMN_NAME";
        xsqlvars[3].relname = "COLUMNINFO";

        xsqlvars[4] = new XSQLVAR();
        xsqlvars[4].sqltype = ISCConstants.SQL_SHORT;
        xsqlvars[4].sqlname = "COLUMN_TYPE";
        xsqlvars[4].relname = "COLUMNINFO";

        xsqlvars[5] = new XSQLVAR();
        xsqlvars[5].sqltype = ISCConstants.SQL_SHORT;
        xsqlvars[5].sqlname = "DATA_TYPE";
        xsqlvars[5].relname = "COLUMNINFO";

        xsqlvars[6] = new XSQLVAR();
        xsqlvars[6].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[6].sqllen = 31;
        xsqlvars[6].sqlname = "TYPE_NAME";
        xsqlvars[6].relname = "COLUMNINFO";

        xsqlvars[7] = new XSQLVAR();
        xsqlvars[7].sqltype = ISCConstants.SQL_LONG;
        xsqlvars[7].sqlname = "PRECISION";
        xsqlvars[7].relname = "COLUMNINFO";

        xsqlvars[8] = new XSQLVAR();
        xsqlvars[8].sqltype = ISCConstants.SQL_LONG;
        xsqlvars[8].sqlname = "LENGTH";
        xsqlvars[8].relname = "COLUMNINFO";

        xsqlvars[9] = new XSQLVAR();
        xsqlvars[9].sqltype = ISCConstants.SQL_SHORT;
        xsqlvars[9].sqlname = "SCALE";
        xsqlvars[9].relname = "COLUMNINFO";

        xsqlvars[10] = new XSQLVAR();
        xsqlvars[10].sqltype = ISCConstants.SQL_SHORT;
        xsqlvars[10].sqlname = "RADIX";
        xsqlvars[10].relname = "COLUMNINFO";

        xsqlvars[11] = new XSQLVAR();
        xsqlvars[11].sqltype = ISCConstants.SQL_SHORT;
        xsqlvars[11].sqlname = "NULLABLE";
        xsqlvars[11].relname = "COLUMNINFO";

        xsqlvars[12] = new XSQLVAR();
        xsqlvars[12].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[12].sqllen = 80; // gets updated if we get a longer description
        xsqlvars[12].sqlname = "REMARKS";
        xsqlvars[12].relname = "COLUMNINFO";

        Clause procedureClause = new Clause("PP.RDB$PROCEDURE_NAME", procedureNamePattern);
        Clause columnClause = new Clause("PP.RDB$PARAMETER_NAME", columnNamePattern);
        
        String sql = GET_PROCEDURE_COLUMNS_START;
        sql += procedureClause.getCondition();
        sql += columnClause.getCondition();
        sql += GET_PROCEDURE_COLUMNS_END;
        
        // check the original case identifiers first
        ArrayList params = new ArrayList();
        if (!procedureClause.getCondition().equals("")) {
            params.add(procedureClause.getOriginalCaseValue());
        }
        if (!columnClause.getCondition().equals("")) {
            params.add(columnClause.getOriginalCaseValue());
        }

        ResultSet rs = doQuery(sql, params);
        ArrayList rows = new ArrayList();
        
        // if nothing found, check the uppercased identifiers
        if (!rs.next()) {
            params.clear();
            if (!procedureClause.getCondition().equals("")) {
                params.add(procedureClause.getValue());
            }
            if (!columnClause.getCondition().equals("")) {
                params.add(columnClause.getValue());
            }
            
            rs = doQuery(sql, params);
            
            // if nothing found, return an empty result set
            if (!rs.next())
                return new FBResultSet(xsqlvars, rows);
        }

        do {
            byte[][] row = new byte[13][];
            row[0] = null;
            row[1] = null;
            row[2] = getBytes(rs.getString("PROCEDURE_NAME").trim());
            row[3] = getBytes(rs.getString("COLUMN_NAME").trim());

            short columnType = rs.getShort("COLUMN_TYPE");
            row[4] = (columnType == 0) ? xsqlvars[0].encodeShort((short)procedureColumnIn) : xsqlvars[0].encodeShort((short)procedureColumnOut);

            short fieldType = rs.getShort("FIELD_TYPE");
            short fieldSubType = rs.getShort("FIELD_SUB_TYPE");
            short fieldScale = rs.getShort("FIELD_SCALE");
            int dataType = getDataType(fieldType, fieldSubType, fieldScale);

            row[5] = xsqlvars[0].encodeShort((short) dataType);

            row[6] = getBytes(getDataTypeName(fieldType, fieldSubType, fieldScale));

            row[7] = null;
            if (dataType == Types.DECIMAL ||
                dataType == Types.NUMERIC)
            {
                row[7] = xsqlvars[0].encodeInt(rs.getShort("FIELD_PRECISION"));
            } else {
                row[7] = xsqlvars[0].encodeInt(rs.getShort("FIELD_LENGTH"));
            }

            row[8] = xsqlvars[0].encodeInt(rs.getShort("FIELD_LENGTH"));
            row[9] = xsqlvars[0].encodeShort((short)(fieldScale * (-1)));
            row[10] = xsqlvars[0].encodeShort((short)10); // RADIX

            short nullFlag = rs.getShort("NULL_FLAG");
            row[11] = (nullFlag == 1) ? xsqlvars[0].encodeShort((short)procedureNoNulls) :
                                        xsqlvars[0].encodeShort((short)procedureNullable);

            String remarks = rs.getString("REMARKS");
            row[12] = getBytes(remarks);
            if (remarks != null && remarks.length() > xsqlvars[12].sqllen)
                xsqlvars[12].sqllen = remarks.length();

            rows.add(row);
        } while (rs.next());
        return new FBResultSet(xsqlvars, rows);
    }

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
        + " where ? = 'T' and RDB$VIEW_SOURCE is not null "
        + " order by 3 ";

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
        + " and RDB$RELATION_NAME || '" + SPACES + "' like ? escape '\\' "
        + " order by 3 ";

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

        if (tableNamePattern == null || "".equals(tableNamePattern))
            tableNamePattern = "%";

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
            tableNamePattern = stripQuotes(stripEscape(tableNamePattern), true);
            sql = (GET_TABLES_EXACT);
            params.add(getWantsSystemTables(types));
            params.add(tableNamePattern);
            params.add(getWantsTables(types));
            params.add(tableNamePattern);
            params.add(getWantsViews(types));
            params.add(tableNamePattern);
        }
        else {
            tableNamePattern = stripQuotes(tableNamePattern, true) + SPACES + "%";
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
        return getSchemas(null, null);
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
        XSQLVAR[] xsqlvars = new XSQLVAR[1];

        xsqlvars[0] = new XSQLVAR();
        xsqlvars[0].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[0].sqllen = 31;
        xsqlvars[0].sqlname = "TABLE_CAT";
        xsqlvars[0].relname = "TABLECATALOGS";

        ArrayList rows = new ArrayList(0);

        return new FBResultSet(xsqlvars, rows);
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
        XSQLVAR[] xsqlvars = new XSQLVAR[1];

        xsqlvars[0] = new XSQLVAR();
        xsqlvars[0].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[0].sqllen = 31;
        xsqlvars[0].sqlname = "TABLE_TYPE";
        xsqlvars[0].relname = "TABLETYPES";

        ArrayList rows = new ArrayList(ALL_TYPES.length);
        for(int i = 0; i < ALL_TYPES.length; i++)
          rows.add(new byte[][] {getBytes(ALL_TYPES[i])});

        return new FBResultSet(xsqlvars, rows);
    }


    private static final String GET_COLUMNS_START = "select " +
        " RF.RDB$RELATION_NAME as RELATION_NAME," +
        " RF.RDB$FIELD_NAME as FIELD_NAME," +
        " F.RDB$FIELD_TYPE as FIELD_TYPE," +
        " F.RDB$FIELD_SUB_TYPE as FIELD_SUB_TYPE," +
        " F.RDB$FIELD_PRECISION as FIELD_PRECISION," +
        " F.RDB$FIELD_SCALE as FIELD_SCALE," +
        " F.RDB$FIELD_LENGTH as FIELD_LENGTH," +
        " F.RDB$CHARACTER_LENGTH as CHAR_LEN," +
        " RF.RDB$DESCRIPTION AS REMARKS," +
        " RF.RDB$DEFAULT_SOURCE as DEFAULT_SOURCE," +
        " RF.RDB$FIELD_POSITION as FIELD_POSITION, " +
        " RF.RDB$NULL_FLAG as NULL_FLAG, " +
        " F.RDB$NULL_FLAG as SOURCE_NULL_FLAG " +
        "from" +
        " RDB$RELATION_FIELDS RF," +
        " RDB$FIELDS F " +
        "where ";

    public static final String GET_COLUMNS_END = " RF.RDB$FIELD_SOURCE = F.RDB$FIELD_NAME " +
        "order by 1, 11";

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

        // ResultSet rs = doQuery(sql, params);

        XSQLVAR[] xsqlvars = new XSQLVAR[18];

        xsqlvars[0] = new XSQLVAR();
        xsqlvars[0].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[0].sqllen = 31;
        xsqlvars[0].sqlname = "TABLE_CAT";
        xsqlvars[0].relname = "COLUMNINFO";

        xsqlvars[1] = new XSQLVAR();
        xsqlvars[1].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[1].sqllen = 31;
        xsqlvars[1].sqlname = "TABLE_SCHEM";
        xsqlvars[1].relname = "COLUMNINFO";

        xsqlvars[2] = new XSQLVAR();
        xsqlvars[2].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[2].sqllen = 31;
        xsqlvars[2].sqlname = "TABLE_NAME";
        xsqlvars[2].relname = "COLUMNINFO";

        xsqlvars[3] = new XSQLVAR();
        xsqlvars[3].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[3].sqllen = 31;
        xsqlvars[3].sqlname = "COLUMN_NAME";
        xsqlvars[3].relname = "COLUMNINFO";

        xsqlvars[4] = new XSQLVAR();
        xsqlvars[4].sqltype = ISCConstants.SQL_SHORT;
        xsqlvars[4].sqlname = "DATA_TYPE";
        xsqlvars[4].relname = "COLUMNINFO";

        xsqlvars[5] = new XSQLVAR();
        xsqlvars[5].sqltype = ISCConstants.SQL_VARYING | 1;
        xsqlvars[5].sqllen = 31;
        xsqlvars[5].sqlname = "TYPE_NAME";
        xsqlvars[5].relname = "COLUMNINFO";

        xsqlvars[6] = new XSQLVAR();
        xsqlvars[6].sqltype = ISCConstants.SQL_LONG;
        xsqlvars[6].sqlname = "COLUMN_SIZE";
        xsqlvars[6].relname = "COLUMNINFO";

        xsqlvars[7] = new XSQLVAR();
        xsqlvars[7].sqltype = ISCConstants.SQL_SHORT;
        xsqlvars[7].sqlname = "BUFFER_LENGTH";
        xsqlvars[7].relname = "COLUMNINFO";

        xsqlvars[8] = new XSQLVAR();
        xsqlvars[8].sqltype = ISCConstants.SQL_LONG;
        xsqlvars[8].sqlname = "DECIMAL_DIGITS";
        xsqlvars[8].relname = "COLUMNINFO";

        xsqlvars[9] = new XSQLVAR();
        xsqlvars[9].sqltype = ISCConstants.SQL_LONG;
        xsqlvars[9].sqlname = "NUM_PREC_RADIX";
        xsqlvars[9].relname = "COLUMNINFO";

        xsqlvars[10] = new XSQLVAR();
        xsqlvars[10].sqltype = ISCConstants.SQL_LONG;
        xsqlvars[10].sqlname = "NULLABLE";
        xsqlvars[10].relname = "COLUMNINFO";

        xsqlvars[11] = new XSQLVAR();
        xsqlvars[11].sqltype = ISCConstants.SQL_VARYING | 1;
        xsqlvars[11].sqllen = 80;
        xsqlvars[11].sqlname = "REMARKS";
        xsqlvars[11].relname = "COLUMNINFO";

        xsqlvars[12] = new XSQLVAR();
        xsqlvars[12].sqltype = ISCConstants.SQL_VARYING | 1;
        xsqlvars[12].sqllen = 31;
        xsqlvars[12].sqlname = "COLUMN_DEF";
        xsqlvars[12].relname = "COLUMNINFO";

        xsqlvars[13] = new XSQLVAR();
        xsqlvars[13].sqltype = ISCConstants.SQL_LONG;
        xsqlvars[13].sqlname = "SQL_DATA_TYPE";
        xsqlvars[13].relname = "COLUMNINFO";

        xsqlvars[14] = new XSQLVAR();
        xsqlvars[14].sqltype = ISCConstants.SQL_LONG;
        xsqlvars[14].sqlname = "SQL_DATETIME_SUB";
        xsqlvars[14].relname = "COLUMNINFO";

        xsqlvars[15] = new XSQLVAR();
        xsqlvars[15].sqltype = ISCConstants.SQL_LONG;
        xsqlvars[15].sqlname = "CHAR_OCTET_LENGTH";
        xsqlvars[15].relname = "COLUMNINFO";

        xsqlvars[16] = new XSQLVAR();
        xsqlvars[16].sqltype = ISCConstants.SQL_LONG;
        xsqlvars[16].sqlname = "ORDINAL_POSITION";
        xsqlvars[16].relname = "COLUMNINFO";

        xsqlvars[17] = new XSQLVAR();
        xsqlvars[17].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[17].sqllen = 3;
        xsqlvars[17].sqlname = "IS_NULLABLE";
        xsqlvars[17].relname = "COLUMNINFO";

        Clause tableClause = new Clause("RF.RDB$RELATION_NAME", tableNamePattern);
        Clause columnClause = new Clause("RF.RDB$FIELD_NAME", columnNamePattern);
        
        String sql = GET_COLUMNS_START;
        sql += tableClause.getCondition();
        sql += columnClause.getCondition();
        sql += GET_COLUMNS_END;
        
        ArrayList params = new ArrayList();
        
        // check first original case values
        if (!tableClause.getCondition().equals("")) {
            params.add(tableClause.getOriginalCaseValue());
        }
        if (!columnClause.getCondition().equals("")) {
            params.add(columnClause.getOriginalCaseValue());
        }
        
        ResultSet rs = doQuery(sql, params);
        ArrayList rows = new ArrayList();

        // if no direct match happened, check the uppercased match
        if (!rs.next()) {
            params.clear();
            if (!tableClause.getCondition().equals("")) {
                params.add(tableClause.getValue());
            }
            if (!columnClause.getCondition().equals("")) {
                params.add(columnClause.getValue());
            }
            rs = doQuery(sql, params);
            
            // open the second result set and check whether we have rows
            // if no rows are available, we have to exit now, otherwise the 
            // following do/while loop will throw SQLException that the
            // result set is not positioned on a row
            if (!rs.next())
                return new FBResultSet(xsqlvars, rows);
        }

        do {
            byte[][] row = new byte[18][];
            row[0] = null;
            row[1] = null;
            row[2] = getBytes(rs.getString("RELATION_NAME").trim());
            row[3] = getBytes(rs.getString("FIELD_NAME").trim());

            short fieldType = rs.getShort("FIELD_TYPE");
            short fieldSubType = rs.getShort("FIELD_SUB_TYPE");
            short fieldScale = rs.getShort("FIELD_SCALE");
            int dataType = getDataType(fieldType, fieldSubType, fieldScale);

            row[4] = xsqlvars[0].encodeShort((short) dataType);
            row[5] = getBytes(getDataTypeName(fieldType, fieldSubType, fieldScale));

            switch (dataType){
                case Types.DECIMAL:
                case Types.NUMERIC:
                   row[6] = xsqlvars[0].encodeInt(rs.getShort("FIELD_PRECISION"));
                   row[8] = xsqlvars[0].encodeInt(fieldScale * (-1));
                   row[15] = xsqlvars[0].encodeInt(0);
                   break;
                case Types.CHAR:
                case Types.VARCHAR:
                   row[6] = xsqlvars[0].encodeInt(rs.getShort("CHAR_LEN"));
                   row[8] = xsqlvars[0].encodeInt(fieldScale * (-1));
                   row[15] = xsqlvars[0].encodeInt(rs.getShort("FIELD_LENGTH"));
                   break;
                case Types.FLOAT:
                   row[6] = xsqlvars[0].encodeInt(7);
                   row[8] = xsqlvars[0].encodeInt(7);
                   row[15] = xsqlvars[0].encodeInt(0);
                   break;
                case Types.DOUBLE:
                   row[6] = xsqlvars[0].encodeInt(15);
                   row[8] = xsqlvars[0].encodeInt(15);
                   row[15] = xsqlvars[0].encodeInt(0);
                   break;
                case Types.INTEGER:
                   row[6] = xsqlvars[0].encodeInt(10);
                   row[8] = xsqlvars[0].encodeInt(fieldScale * (-1));
                   row[15] = xsqlvars[0].encodeInt(0);
                   break;
                case Types.SMALLINT:
                   row[6] = xsqlvars[0].encodeInt(5);
                   row[8] = xsqlvars[0].encodeInt(fieldScale * (-1));
                   row[15] = xsqlvars[0].encodeInt(0);
                   break;
                case Types.DATE:
                   row[6] = xsqlvars[0].encodeInt(10);
                   row[8] = xsqlvars[0].encodeInt(fieldScale * (-1));
                   row[15] = xsqlvars[0].encodeInt(0);
                   break;
                case Types.TIME:
                   row[6] = xsqlvars[0].encodeInt(8);
                   row[8] = xsqlvars[0].encodeInt(fieldScale * (-1));
                   row[15] = xsqlvars[0].encodeInt(0);
                   break;
                case Types.TIMESTAMP:
                   row[6] = xsqlvars[0].encodeInt(19);
                   row[8] = xsqlvars[0].encodeInt(fieldScale * (-1));
                   row[15] = xsqlvars[0].encodeInt(0);
                   break;
                default:
                   row[6] = xsqlvars[0].encodeInt(0);
                   row[8] = xsqlvars[0].encodeInt(fieldScale * (-1));
                   row[15] = xsqlvars[0].encodeInt(0);
               }

            row[7] = xsqlvars[0].encodeShort((short) 0);
            row[9] = xsqlvars[0].encodeInt(10);

            short nullFlag = rs.getShort("NULL_FLAG");
            short sourceNullFlag = rs.getShort("SOURCE_NULL_FLAG");
            row[10] = (nullFlag == 1 || sourceNullFlag == 1) ? 
                    xsqlvars[0].encodeInt(columnNoNulls) :
                    xsqlvars[0].encodeInt(columnNullable);

            String remarks = rs.getString("REMARKS");  
            row[11] = getBytes(remarks);             
            if (remarks != null && remarks.length() > xsqlvars[11].sqllen)               
                xsqlvars[11].sqllen = remarks.length();
            
            String column_def = rs.getString("DEFAULT_SOURCE");
            if (column_def!=null) {
                String defaultValue = column_def.trim();
                
                int defaultPos = defaultValue.toUpperCase().indexOf("DEFAULT");
                if (defaultPos >= 0)
                    defaultValue = defaultValue.substring(7).trim();
                
            	row[12] = getBytes(defaultValue);
            } else
            	row[12] = null;
            
            row[13] = null;
            row[14] = null;
            row[16] = xsqlvars[0].encodeInt(rs.getShort("FIELD_POSITION") + 1);
            row[17] = (nullFlag == 1 || sourceNullFlag == 1) ? 
                        getBytes("NO") : getBytes("YES");

            rows.add(row);
        } while (rs.next());
        
        return new FBResultSet(xsqlvars, rows);
    }

    private static final short smallint_type = 7;
    private static final short integer_type = 8;
    private static final short quad_type = 9;
    private static final short float_type = 10;
    private static final short d_float_type = 11;
    private static final short date_type = 12;
    private static final short time_type = 13;
    private static final short char_type = 14;
    private static final short int64_type = 16;
    private static final short double_type = 27;
    private static final short timestamp_type = 35;
    private static final short varchar_type = 37;
//    private static final short cstring_type = 40;
    private static final short blob_type = 261;

    private int getDataType (short fieldType, short fieldSubType, short fieldScale) {
        if (fieldScale < 0) {
            switch (fieldType) {
                case smallint_type:
                case integer_type:
                case int64_type:
                case double_type:
                    // NOTE: can't be BIGINT because of scale
                    if (fieldSubType == 2)
                        return Types.DECIMAL;
                    else
                        return Types.NUMERIC;
                default:
                    break;
            }
        }

        switch (fieldType) {
            case smallint_type:
                if (fieldSubType == 1)
                    return Types.NUMERIC;
                else if (fieldSubType == 2)
                    return Types.DECIMAL;
                else
                    return Types.SMALLINT;
            case integer_type:
                if (fieldSubType == 1)
                    return Types.NUMERIC;
                else if (fieldSubType == 2)
                    return Types.DECIMAL;
                else
                    return Types.INTEGER;
            case double_type:
            case d_float_type:
                return Types.DOUBLE;
            case float_type:
                return Types.FLOAT;
            case char_type:
                return Types.CHAR;
            case varchar_type:
                return Types.VARCHAR;
            case timestamp_type:
                return Types.TIMESTAMP;
            case time_type:
                return Types.TIME;
            case date_type:
                return Types.DATE;
            case int64_type:
                //This might need some help for long mapping
                if (fieldSubType == 1)
                    return Types.NUMERIC;
                else if (fieldSubType == 2)
                    return Types.DECIMAL;
                else
                    return Types.BIGINT;
            case blob_type:
                if (fieldSubType < 0)
                    return Types.BLOB;
                else if (fieldSubType == 0)
                    return Types.LONGVARBINARY;
                else if (fieldSubType == 1)
                    return Types.LONGVARCHAR;
                else
                    return Types.OTHER;
            case quad_type:
                return Types.OTHER;
            default:
                return Types.NULL;
        }
    }

    static String getDataTypeName(short sqltype, short sqlsubtype, short sqlscale) {
        if (sqlscale < 0) {
            switch (sqltype) {
                case smallint_type:
                case integer_type:
                case int64_type:
                case double_type:
                    // NOTE: can't be BIGINT because of scale
                    if (sqlsubtype == 2)
                        return "DECIMAL";
                    else
                        return "NUMERIC";
                default:
                    break;
            }
        }

        switch (sqltype) {
            case smallint_type:
                return "SMALLINT";
            case integer_type:
                return "INTEGER";
            case double_type:
            case d_float_type:
                return "DOUBLE PRECISION";
            case float_type:
                return "FLOAT";
            case char_type:
                return "CHAR";
            case varchar_type:
                return "VARCHAR";
            case timestamp_type:
                return "TIMESTAMP";
            case time_type:
                return "TIME";
            case date_type:
                return "DATE";
            case int64_type:
                //this might need some help for long mapping
                if (sqlsubtype == 1)
                    return "NUMERIC";
                else if (sqlsubtype == 2)
                    return "DECIMAL";
                else
                    return "BIGINT";
            case blob_type:
                if (sqlsubtype < 0)
                    return "BLOB SUB_TYPE <0";
                else if (sqlsubtype == 0)
                    return "BLOB SUB_TYPE 0";
                else if (sqlsubtype == 1)
                    return "BLOB SUB_TYPE 1";
                else
                    return "BLOB SUB_TYPE " + sqlsubtype;
            case quad_type:
                return "ARRAY";
            default:
                return "NULL";
        }
    }

    private static final String GET_COLUMN_PRIVILEGES_START = "select "
        + "null as TABLE_CAT,"
        + "null as TABLE_SCHEM,"
        + "RF.RDB$RELATION_NAME as TABLE_NAME, "
        + "RF.RDB$FIELD_NAME as COLUMN_NAME, "
        + "UP.RDB$GRANTOR as GRANTOR, "
        + "UP.RDB$USER as GRANTEE, "
        + "UP.RDB$PRIVILEGE as PRIVILEGE, "
        + "UP.RDB$GRANT_OPTION as IS_GRANTABLE "
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
        + "order by 4,7 ";

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

        XSQLVAR[] xsqlvars = new XSQLVAR[8];

        xsqlvars[0] = new XSQLVAR();
        xsqlvars[0].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[0].sqllen = 31;
        xsqlvars[0].sqlname = "TABLE_CAT";
        xsqlvars[0].relname = "COLUMNPRIV";

        xsqlvars[1] = new XSQLVAR();
        xsqlvars[1].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[1].sqllen = 31;
        xsqlvars[1].sqlname = "TABLE_SCHEM";
        xsqlvars[1].relname = "COLUMNPRIV";

        xsqlvars[2] = new XSQLVAR();
        xsqlvars[2].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[2].sqllen = 31;
        xsqlvars[2].sqlname = "TABLE_NAME";
        xsqlvars[2].relname = "COLUMNPRIV";

        xsqlvars[3] = new XSQLVAR();
        xsqlvars[3].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[3].sqllen = 31;
        xsqlvars[3].sqlname = "COLUMN_NAME";
        xsqlvars[3].relname = "COLUMNPRIV";

        xsqlvars[4] = new XSQLVAR();
        xsqlvars[4].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[4].sqllen = 31;
        xsqlvars[4].sqlname = "GRANTOR";
        xsqlvars[4].relname = "COLUMNPRIV";

        xsqlvars[5] = new XSQLVAR();
        xsqlvars[5].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[5].sqllen = 31;
        xsqlvars[5].sqlname = "GRANTEE";
        xsqlvars[5].relname = "COLUMNPRIV";

        xsqlvars[6] = new XSQLVAR();
        xsqlvars[6].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[6].sqllen = 31;
        xsqlvars[6].sqlname = "PRIVILEGE";
        xsqlvars[6].relname = "COLUMNPRIV";

        xsqlvars[7] = new XSQLVAR();
        xsqlvars[7].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[7].sqllen = 31;
        xsqlvars[7].sqlname = "IS_GRANTABLE";
        xsqlvars[7].relname = "COLUMNPRIV";

        Clause columnClause = new Clause("RF.RDB$FIELD_NAME", columnNamePattern);
        
        String sql = GET_COLUMN_PRIVILEGES_START;
        sql += columnClause.getCondition();
        sql += GET_COLUMN_PRIVILEGES_END;
        
        ArrayList params = new ArrayList();
        
        // check the original case first
        table = stripQuotes(stripEscape(table), false);
        params.add(table);
        if (!columnClause.getCondition().equals("")) {
            params.add(columnClause.getOriginalCaseValue());
        }
        
        ArrayList rows = new ArrayList();
        ResultSet rs = doQuery(sql, params);
        
        // if nothing was found, check the uppercased identifiers
        if (!rs.next()) {
            params.clear();
            if (!columnClause.getCondition().equals("")) {
                params.add(stripQuotes(stripEscape(table), true));
                params.add(columnClause.getValue());
            }
            
            rs = doQuery(sql, params);
            
            // return empty result set 
            if (!rs.next())
                return new FBResultSet(xsqlvars, rows);
        }

        do {
            byte[][] row = new byte[8][];
            row[0] = null;
            row[1] = null;
            row[2] = getBytes(rs.getString("TABLE_NAME"));
            row[3] = getBytes(rs.getString("COLUMN_NAME"));
            row[4] = getBytes(rs.getString("GRANTOR"));
            row[5] = getBytes(rs.getString("GRANTEE"));
            String privilege = rs.getString("PRIVILEGE");
            if (privilege.equals("A"))
                row[6] = getBytes("ALL");
            else if (privilege.equals("S"))
                row[6] = getBytes("SELECT");
            else if (privilege.equals("D"))
                row[6] = getBytes("DELETE");
            else if (privilege.equals("I"))
                row[6] = getBytes("INSERT");
            else if (privilege.equals("U"))
                row[6] = getBytes("UPDATE");
            else if (privilege.equals("R"))
                row[6] = getBytes("REFERENCE");
            else if (privilege.equals("M"))
                row[6] = getBytes("MEMBEROF");
            int isGrantable = rs.getShort("IS_GRANTABLE");
            if (isGrantable==0)
                row[7] = getBytes("NO");
            else
                row[7] = getBytes("YES");

            rows.add(row);
        } while(rs.next());
        
        return new FBResultSet(xsqlvars, rows);
    }



    private static final String GET_TABLE_PRIVILEGES_START = "select"
        + " null as TABLE_CAT, "
        + " null as TABLE_SCHEM,"
        + " RDB$RELATION_NAME as TABLE_NAME,"
        + " RDB$GRANTOR as GRANTOR, "
        + " RDB$USER as GRANTEE, "
        + " RDB$PRIVILEGE as PRIVILEGE, "
        + " RDB$GRANT_OPTION as IS_GRANTABLE "
        + "from"
        + " RDB$USER_PRIVILEGES "
        + "where ";
    private static final String GET_TABLE_PRIVILEGES_END = " RDB$OBJECT_TYPE = 0 and"
        + " RDB$FIELD_NAME is null "
        + "order by 3, 6";

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
        tableNamePattern = stripQuotes(stripEscape(tableNamePattern), true);

        XSQLVAR[] xsqlvars = new XSQLVAR[7];

        xsqlvars[0] = new XSQLVAR();
        xsqlvars[0].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[0].sqllen = 31;
        xsqlvars[0].sqlname = "TABLE_CAT";
        xsqlvars[0].relname = "TABLEPRIV";

        xsqlvars[1] = new XSQLVAR();
        xsqlvars[1].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[1].sqllen = 31;
        xsqlvars[1].sqlname = "TABLE_SCHEM";
        xsqlvars[1].relname = "TABLEPRIV";

        xsqlvars[2] = new XSQLVAR();
        xsqlvars[2].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[2].sqllen = 31;
        xsqlvars[2].sqlname = "TABLE_NAME";
        xsqlvars[2].relname = "TABLEPRIV";

        xsqlvars[3] = new XSQLVAR();
        xsqlvars[3].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[3].sqllen = 31;
        xsqlvars[3].sqlname = "GRANTOR";
        xsqlvars[3].relname = "TABLEPRIV";

        xsqlvars[4] = new XSQLVAR();
        xsqlvars[4].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[4].sqllen = 31;
        xsqlvars[4].sqlname = "GRANTEE";
        xsqlvars[4].relname = "TABLEPRIV";

        xsqlvars[5] = new XSQLVAR();
        xsqlvars[5].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[5].sqllen = 31;
        xsqlvars[5].sqlname = "PRIVILEGE";
        xsqlvars[5].relname = "TABLEPRIV";

        xsqlvars[6] = new XSQLVAR();
        xsqlvars[6].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[6].sqllen = 31;
        xsqlvars[6].sqlname = "IS_GRANTABLE";
        xsqlvars[6].relname = "TABLEPRIV";

        Clause tableClause = new Clause("RDB$RELATION_NAME", tableNamePattern);
        
        String sql = GET_TABLE_PRIVILEGES_START;
        sql += tableClause.getCondition();
        sql += GET_TABLE_PRIVILEGES_END;
        
        // check the original case identifiers first
        ArrayList params = new ArrayList();
        if (!tableClause.getCondition().equals("")) {
            params.add(tableClause.getOriginalCaseValue());
        }

        ArrayList rows = new ArrayList();
        ResultSet rs = doQuery(sql, params);
        
        // if nothing found, check the uppercased identifiers
        if (!rs.next()) {
            params.clear();
            if (!tableClause.getCondition().equals("")) {
                params.add(tableClause.getValue());
            }
            
            rs = doQuery(sql, params);
            
            // if nothing found, return an empty result set
            if (!rs.next())
                return new FBResultSet(xsqlvars, rows);
            
        }
        
        do {
            byte[][] row = new byte[7][];
            row[0] = null;
            row[1] = null;
            row[2] = getBytes(rs.getString("TABLE_NAME"));
            row[3] = getBytes(rs.getString("GRANTOR"));
            row[4] = getBytes(rs.getString("GRANTEE"));
            String privilege = rs.getString("PRIVILEGE");
            if (privilege.equals("A"))
                row[5] = getBytes("ALL");
            else if (privilege.equals("S"))
                row[5] = getBytes("SELECT");
            else if (privilege.equals("D"))
                row[5] = getBytes("DELETE");
            else if (privilege.equals("I"))
                row[5] = getBytes("INSERT");
            else if (privilege.equals("U"))
                row[5] = getBytes("UPDATE");
            else if (privilege.equals("R"))
                row[5] = getBytes("REFERENCE");
            else if (privilege.equals("M"))
                row[5] = getBytes("MEMBEROF");
            int isGrantable = rs.getShort("IS_GRANTABLE");
            if (isGrantable==0)
                row[6] = getBytes("NO");
            else
                row[6] = getBytes("YES");

            rows.add(row);
        } while (rs.next());
        
        return new FBResultSet(xsqlvars, rows);
    }

    private static final String GET_BEST_ROW_IDENT = "" +
        "select " +
        "    rf.rdb$field_name as column_name, " +
        "    f.rdb$field_type as field_type, " +
        "    f.rdb$field_sub_type as field_sub_type, " +
        "    f.rdb$field_scale as field_scale, " +
        "    f.rdb$field_precision as field_precision " +
        "from " +
        "    rdb$relation_constraints rc," +
        "    rdb$index_segments idx," +
        "    rdb$relation_fields rf," +
        "    rdb$fields f " +
        "where " +
        "    rc.rdb$relation_name = ? " +
        "and " +
        "    rc.rdb$constraint_type = 'PRIMARY KEY' " +
        "and " +
        "    idx.rdb$index_name = rc.rdb$index_name " +
        "and " +
        "    rf.rdb$field_name = idx.rdb$field_name " +
        "and " +
        "    rf.rdb$relation_name = ? " +
        "and " +
        "    f.rdb$field_name = rf.rdb$field_source"
        ;

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
        XSQLVAR[] xsqlvars = new XSQLVAR[8];

        xsqlvars[0] = new XSQLVAR();
        xsqlvars[0].sqltype = ISCConstants.SQL_SHORT;
        xsqlvars[0].sqlname = "SCOPE";
        xsqlvars[0].relname = "ROWIDENTIFIER";

        xsqlvars[1] = new XSQLVAR();
        xsqlvars[1].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[1].sqllen = 31;
        xsqlvars[1].sqlname = "COLUMN_NAME";
        xsqlvars[1].relname = "ROWIDENTIFIER";

        xsqlvars[2] = new XSQLVAR();
        xsqlvars[2].sqltype = ISCConstants.SQL_SHORT;
        xsqlvars[2].sqlname = "DATA_TYPE";
        xsqlvars[2].relname = "ROWIDENTIFIER";

        xsqlvars[3] = new XSQLVAR();
        xsqlvars[3].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[3].sqllen = 31;
        xsqlvars[3].sqlname = "TYPE_NAME";
        xsqlvars[3].relname = "ROWIDENTIFIER";

        xsqlvars[4] = new XSQLVAR();
        xsqlvars[4].sqltype = ISCConstants.SQL_LONG;
        xsqlvars[4].sqlname = "COLUMN_SIZE";
        xsqlvars[4].relname = "ROWIDENTIFIER";

        xsqlvars[5] = new XSQLVAR();
        xsqlvars[5].sqltype = ISCConstants.SQL_LONG;
        xsqlvars[5].sqlname = "BUFFER_LENGTH";
        xsqlvars[5].relname = "ROWIDENTIFIER";

        xsqlvars[6] = new XSQLVAR();
        xsqlvars[6].sqltype = ISCConstants.SQL_SHORT;
        xsqlvars[6].sqlname = "DECIMAL_DIGITS";
        xsqlvars[6].relname = "ROWIDENTIFIER";

        xsqlvars[7] = new XSQLVAR();
        xsqlvars[7].sqltype = ISCConstants.SQL_SHORT;
        xsqlvars[7].sqlname = "PSEUDO_COLUMN";
        xsqlvars[7].relname = "ROWIDENTIFIER";

        ResultSet tables = getTables(catalog, schema, table, null);
        
        if (!tables.next())
            return new FBResultSet(xsqlvars, new ArrayList());
        
        ArrayList rows = getPrimaryKeyIdentifier(tables.getString(3), scope, xsqlvars);

        // if no primary key exists, add RDB$DB_KEY as pseudo-column
        if (rows.size() == 0) {
            byte[][] row = new byte[8][];
            row[0] = xsqlvars[0].encodeShort((short)scope);
            row[1] = getBytes("RDB$DB_KEY");
            row[2] = xsqlvars[0].encodeShort((short)getDataType(char_type, (short)0, (short)0));
            row[3] = getBytes(getDataTypeName(char_type, (short)0, (short)0));
            row[4] = xsqlvars[0].encodeInt(0);
            row[5] = null;
            row[6] = xsqlvars[0].encodeShort((short)0);
            row[7] = xsqlvars[0].encodeShort((short)bestRowPseudo);

            rows.add(row);
        }
        
        return new FBResultSet(xsqlvars, rows);
    }


    /**
     * Get primary key of the table as best row identifier.
     * 
     * @param table name of the table.
     * @param scope scope, we just include it in the result set.
     * @param xsqlvars array of {@link XSQLVAR} instances describing result set.
     * 
     * @return list of result set values, when size is 0, no primary key has 
     * been defined for a table.
     * 
     * @throws SQLException if something went wrong.
     */
    private ArrayList getPrimaryKeyIdentifier(String table, int scope, XSQLVAR[] xsqlvars) throws SQLException {
        ArrayList rows = new ArrayList(0);

        ArrayList params = new ArrayList(2);
        params.add(table);
        params.add(table);
        
        ResultSet rs = doQuery(GET_BEST_ROW_IDENT, params);
        
        while (rs.next()) {
            byte[][] row = new byte[8][];
            row[0] = xsqlvars[0].encodeShort((short)scope);
            row[1] = getBytes(rs.getString("COLUMN_NAME").trim());
            row[2] = xsqlvars[0].encodeShort((short)getDataType(rs.getShort("FIELD_TYPE"), 
                rs.getShort("FIELD_SUB_TYPE"), rs.getShort("FIELD_SCALE")));
            row[3] = getBytes(getDataTypeName(rs.getShort("FIELD_TYPE"), 
                rs.getShort("FIELD_SUB_TYPE"), rs.getShort("FIELD_SCALE")));
            row[4] = xsqlvars[0].encodeInt(rs.getInt("FIELD_PRECISION"));
            row[5] = null;
            row[6] = xsqlvars[0].encodeShort(rs.getShort("FIELD_SCALE"));
            row[7] = xsqlvars[0].encodeShort((short)bestRowNotPseudo);

            rows.add(row);
        }
        return rows;
    }

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
        XSQLVAR[] xsqlvars = new XSQLVAR[8];

        xsqlvars[0] = new XSQLVAR();
        xsqlvars[0].sqltype = ISCConstants.SQL_SHORT;
        xsqlvars[0].sqlname = "SCOPE";
        xsqlvars[0].relname = "VERSIONCOL";

        xsqlvars[1] = new XSQLVAR();
        xsqlvars[1].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[1].sqllen = 31;
        xsqlvars[1].sqlname = "COLUMN_NAME";
        xsqlvars[1].relname = "VERSIONCOL";

        xsqlvars[2] = new XSQLVAR();
        xsqlvars[2].sqltype = ISCConstants.SQL_SHORT;
        xsqlvars[2].sqlname = "DATA_TYPE";
        xsqlvars[2].relname = "VERSIONCOL";

        xsqlvars[3] = new XSQLVAR();
        xsqlvars[3].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[3].sqllen = 31;
        xsqlvars[3].sqlname = "TYPE_NAME";
        xsqlvars[3].relname = "VERSIONCOL";

        xsqlvars[4] = new XSQLVAR();
        xsqlvars[4].sqltype = ISCConstants.SQL_LONG;
        xsqlvars[4].sqlname = "COLUMN_SIZE";
        xsqlvars[4].relname = "VERSIONCOL";

        xsqlvars[5] = new XSQLVAR();
        xsqlvars[5].sqltype = ISCConstants.SQL_LONG;
        xsqlvars[5].sqlname = "BUFFER_LENGTH";
        xsqlvars[5].relname = "VERSIONCOL";

        xsqlvars[6] = new XSQLVAR();
        xsqlvars[6].sqltype = ISCConstants.SQL_SHORT;
        xsqlvars[6].sqlname = "DECIMAL_DIGITS";
        xsqlvars[6].relname = "VERSIONCOL";

        xsqlvars[7] = new XSQLVAR();
        xsqlvars[7].sqltype = ISCConstants.SQL_SHORT;
        xsqlvars[7].sqlname = "PSEUDO_COLUMN";
        xsqlvars[7].relname = "VERSIONCOL";

        ArrayList rows = new ArrayList(0);

        return new FBResultSet(xsqlvars, rows);
    }


    private static final String GET_PRIMARY_KEYS_START = "select "
        + " null as TABLE_CAT, "
        + " null as TABLE_SCHEM, "
        + "RC.RDB$RELATION_NAME as TABLE_NAME, "
        + "ISGMT.RDB$FIELD_NAME as COLUMN_NAME, "
        + "CAST ((ISGMT.RDB$FIELD_POSITION + 1) as SMALLINT) as KEY_SEQ, "
        + "RC.RDB$CONSTRAINT_NAME as PK_NAME "
        + "from "
        + "RDB$RELATION_CONSTRAINTS RC, "
        + "RDB$INDEX_SEGMENTS ISGMT "
        + "where ";

    private static final String GET_PRIMARY_KEYS_END =
        "RC.RDB$INDEX_NAME = ISGMT.RDB$INDEX_NAME and "
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

        XSQLVAR[] xsqlvars = new XSQLVAR[6];

        xsqlvars[0] = new XSQLVAR();
        xsqlvars[0].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[0].sqllen = 31;
        xsqlvars[0].sqlname = "TABLE_CAT";
        xsqlvars[0].relname = "COLUMNINFO";

        xsqlvars[1] = new XSQLVAR();
        xsqlvars[1].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[1].sqllen = 31;
        xsqlvars[1].sqlname = "TABLE_SCHEM";
        xsqlvars[1].relname = "COLUMNINFO";

        xsqlvars[2] = new XSQLVAR();
        xsqlvars[2].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[2].sqllen = 31;
        xsqlvars[2].sqlname = "TABLE_NAME";
        xsqlvars[2].relname = "COLUMNINFO";

        xsqlvars[3] = new XSQLVAR();
        xsqlvars[3].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[3].sqllen = 31;
        xsqlvars[3].sqlname = "COLUMN_NAME";
        xsqlvars[3].relname = "COLUMNINFO";

        xsqlvars[4] = new XSQLVAR();
        xsqlvars[4].sqltype = ISCConstants.SQL_SHORT;
        xsqlvars[4].sqlname = "KEY_SEQ";
        xsqlvars[4].relname = "COLUMNINFO";

        xsqlvars[5] = new XSQLVAR();
        xsqlvars[5].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[5].sqllen = 31;
        xsqlvars[5].sqlname = "PK_NAME";
        xsqlvars[5].relname = "COLUMNINFO";

        Clause tableClause = new Clause("RC.RDB$RELATION_NAME", table);

        String sql = GET_PRIMARY_KEYS_START;
        sql += tableClause.getCondition();
        sql += GET_PRIMARY_KEYS_END;
        
        // check the original case identifiers
        ArrayList params = new ArrayList();
        if (!tableClause.getCondition().equals("")) {
            params.add(tableClause.getOriginalCaseValue());
        }
        
        ArrayList rows = new ArrayList();
        ResultSet rs = doQuery(sql, params);
        
        // if nothing found, check the uppercased identifier
        if (!rs.next()) {
            params.clear();
            if (!tableClause.getCondition().equals("")) {
                params.add(tableClause.getValue());
            }
            
            rs = doQuery(sql, params);
            
            // if nothing found, return empty result set
            if (!rs.next()) 
                return new FBResultSet(xsqlvars, rows);
        }
        
        do {
            byte[][] row = new byte[6][];
            row[0] = null;
            row[1] = null;
            row[2] = getBytes(rs.getString("TABLE_NAME").trim());
            row[3] = getBytes(rs.getString("COLUMN_NAME").trim());
            row[4] = xsqlvars[0].encodeShort(rs.getShort("KEY_SEQ"));
            row[5] = getBytes(rs.getString("PK_NAME"));

            rows.add(row);
        } while(rs.next());
        
        return new FBResultSet(xsqlvars, rows);
    }


    private static final String GET_IMPORTED_KEYS_START = "select"
    +" null as PKTABLE_CAT "
    +" ,null as PKTABLE_SCHEM "
    +" ,PK.RDB$RELATION_NAME as PKTABLE_NAME "
    +" ,ISP.RDB$FIELD_NAME as PKCOLUMN_NAME "
    +" ,null as FKTABLE_CAT "
    +" ,null as FKTABLE_SCHEM "
    +" ,FK.RDB$RELATION_NAME as FKTABLE_NAME "
    +" ,ISF.RDB$FIELD_NAME as FKCOLUMN_NAME "
    +" ,CAST ((ISP.RDB$FIELD_POSITION + 1) as SMALLINT) as KEY_SEQ "
    +" ,RC.RDB$UPDATE_RULE as UPDATE_RULE "
    +" ,RC.RDB$DELETE_RULE as DELETE_RULE "
    +" ,PK.RDB$CONSTRAINT_NAME as PK_NAME "
    +" ,FK.RDB$CONSTRAINT_NAME as FK_NAME "
    +" ,null as DEFERRABILITY "
    +" from "
    +" RDB$RELATION_CONSTRAINTS PK "
    +" ,RDB$RELATION_CONSTRAINTS FK "
    +" ,RDB$REF_CONSTRAINTS RC "
    +" ,RDB$INDEX_SEGMENTS ISP "
    +" ,RDB$INDEX_SEGMENTS ISF "
    +" WHERE ";

    private static final String GET_IMPORTED_KEYS_END =
    " FK.RDB$CONSTRAINT_NAME = RC.RDB$CONSTRAINT_NAME "
    +" and PK.RDB$CONSTRAINT_NAME = RC.RDB$CONST_NAME_UQ "
    +" and ISP.RDB$INDEX_NAME = PK.RDB$INDEX_NAME "
    +" and ISF.RDB$INDEX_NAME = FK.RDB$INDEX_NAME "
    +" and ISP.RDB$FIELD_POSITION = ISF.RDB$FIELD_POSITION "
    +" order by 3, 9 ";

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
        checkCatalogAndSchema(catalog, schema);

        XSQLVAR[] xsqlvars = new XSQLVAR[14];

        xsqlvars[0] = new XSQLVAR();
        xsqlvars[0].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[0].sqllen = 31;
        xsqlvars[0].sqlname = "PKTABLE_CAT";
        xsqlvars[0].relname = "COLUMNINFO";

        xsqlvars[1] = new XSQLVAR();
        xsqlvars[1].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[1].sqllen = 31;
        xsqlvars[1].sqlname = "PKTABLE_SCHEM";
        xsqlvars[1].relname = "COLUMNINFO";

        xsqlvars[2] = new XSQLVAR();
        xsqlvars[2].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[2].sqllen = 31;
        xsqlvars[2].sqlname = "PKTABLE_NAME";
        xsqlvars[2].relname = "COLUMNINFO";

        xsqlvars[3] = new XSQLVAR();
        xsqlvars[3].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[3].sqllen = 31;
        xsqlvars[3].sqlname = "PKCOLUMN_NAME";
        xsqlvars[3].relname = "COLUMNINFO";

        xsqlvars[4] = new XSQLVAR();
        xsqlvars[4].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[4].sqllen = 31;
        xsqlvars[4].sqlname = "FKTABLE_CAT";
        xsqlvars[4].relname = "COLUMNINFO";

        xsqlvars[5] = new XSQLVAR();
        xsqlvars[5].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[5].sqllen = 31;
        xsqlvars[5].sqlname = "FKTABLE_SCHEM";
        xsqlvars[5].relname = "COLUMNINFO";

        xsqlvars[6] = new XSQLVAR();
        xsqlvars[6].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[6].sqllen = 31;
        xsqlvars[6].sqlname = "FKTABLE_NAME";
        xsqlvars[6].relname = "COLUMNINFO";

        xsqlvars[7] = new XSQLVAR();
        xsqlvars[7].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[7].sqllen = 31;
        xsqlvars[7].sqlname = "FKCOLUMN_NAME";
        xsqlvars[7].relname = "COLUMNINFO";

        xsqlvars[8] = new XSQLVAR();
        xsqlvars[8].sqltype = ISCConstants.SQL_SHORT;
        xsqlvars[8].sqlname = "KEY_SEQ";
        xsqlvars[8].relname = "COLUMNINFO";

        xsqlvars[9] = new XSQLVAR();
        xsqlvars[9].sqltype = ISCConstants.SQL_SHORT;
        xsqlvars[9].sqlname = "UPDATE_RULE";
        xsqlvars[9].relname = "COLUMNINFO";

        xsqlvars[10] = new XSQLVAR();
        xsqlvars[10].sqltype = ISCConstants.SQL_SHORT;
        xsqlvars[10].sqlname = "DELETE_RULE";
        xsqlvars[10].relname = "COLUMNINFO";

        xsqlvars[11] = new XSQLVAR();
        xsqlvars[11].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[11].sqllen = 31;
        xsqlvars[11].sqlname = "FK_NAME";
        xsqlvars[11].relname = "COLUMNINFO";

        xsqlvars[12] = new XSQLVAR();
        xsqlvars[12].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[12].sqllen = 31;
        xsqlvars[12].sqlname = "PK_NAME";
        xsqlvars[12].relname = "COLUMNINFO";

        xsqlvars[13] = new XSQLVAR();
        xsqlvars[13].sqltype = ISCConstants.SQL_SHORT;
        xsqlvars[13].sqlname = "DEFERRABILITY";
        xsqlvars[13].relname = "COLUMNINFO";

        Clause tableClause = new Clause("FK.RDB$RELATION_NAME", table);
        
        String sql = GET_IMPORTED_KEYS_START;
        sql += tableClause.getCondition();
        sql += GET_IMPORTED_KEYS_END;

        // check the original case identifiers first
        ArrayList params = new ArrayList();
        if (!tableClause.getCondition().equals("")) {
            params.add(tableClause.getOriginalCaseValue());
        }
        
        ArrayList rows = new ArrayList();
        ResultSet rs = doQuery(sql, params);
        
        // if nothing found, check the uppercased identifiers
        if (!rs.next()) {
            params.clear();
            if (!tableClause.getCondition().equals("")) {
                params.add(tableClause.getValue());
            }
            
            rs = doQuery(sql, params);
            
            // if nothing found, return an empty result set
            if (!rs.next())
                return new FBResultSet(xsqlvars, rows);
        }
        
        do {
            byte[][] row = new byte[14][];
            row[0] = null;
            row[1] = null;
            row[2] = getBytes(rs.getString("PKTABLE_NAME").trim());
            row[3] = getBytes(rs.getString("PKCOLUMN_NAME").trim());
            row[4] = null;
            row[5] = null;
            row[6] = getBytes(rs.getString("FKTABLE_NAME").trim());
            row[7] = getBytes(rs.getString("FKCOLUMN_NAME").trim());
            row[8] = xsqlvars[0].encodeShort(rs.getShort("KEY_SEQ"));
            String updateRule = rs.getString("UPDATE_RULE");
            if (updateRule.equals("NO ACTION") || updateRule.equals("RESTRICT"))
                row[9] = xsqlvars[0].encodeShort((short) DatabaseMetaData.importedKeyNoAction);
            else if (updateRule.equals("CASCADE"))
                row[9] = xsqlvars[0].encodeShort((short) DatabaseMetaData.importedKeyCascade);
            else if (updateRule.equals("SET NULL"))
                row[9] = xsqlvars[0].encodeShort((short) DatabaseMetaData.importedKeySetNull);
            else if (updateRule.equals("SET DEFAULT"))
                row[9] = xsqlvars[0].encodeShort((short) DatabaseMetaData.importedKeySetDefault);
            String deleteRule = rs.getString("DELETE_RULE");
            if (deleteRule.equals("NO ACTION") || deleteRule.equals("RESTRICT"))
                row[10] = xsqlvars[0].encodeShort((short) DatabaseMetaData.importedKeyNoAction);
            else if (deleteRule.equals("CASCADE"))
                row[10] = xsqlvars[0].encodeShort((short) DatabaseMetaData.importedKeyCascade);
            else if (deleteRule.equals("SET NULL"))
                row[10] = xsqlvars[0].encodeShort((short) DatabaseMetaData.importedKeySetNull);
            else if (deleteRule.equals("SET DEFAULT"))
                row[10] = xsqlvars[0].encodeShort((short) DatabaseMetaData.importedKeySetDefault);
            row[11] = getBytes(rs.getString("FK_NAME"));
            row[12] = getBytes(rs.getString("PK_NAME"));
            row[13] = xsqlvars[0].encodeShort((short) DatabaseMetaData.importedKeyNotDeferrable);
            rows.add(row);
        } while (rs.next());
        
        return new FBResultSet(xsqlvars, rows);
    }


    private static final String GET_EXPORTED_KEYS_START = "select"
    +" null as PKTABLE_CAT "
    +" ,null as PKTABLE_SCHEM "
    +" ,PK.RDB$RELATION_NAME as PKTABLE_NAME "
    +" ,ISP.RDB$FIELD_NAME as PKCOLUMN_NAME "
    +" ,null as FKTABLE_CAT "
    +" ,null as FKTABLE_SCHEM "
    +" ,FK.RDB$RELATION_NAME as FKTABLE_NAME "
    +" ,ISF.RDB$FIELD_NAME as FKCOLUMN_NAME "
    +" ,CAST ((ISP.RDB$FIELD_POSITION + 1) as SMALLINT) as KEY_SEQ "
    +" ,RC.RDB$UPDATE_RULE as UPDATE_RULE "
    +" ,RC.RDB$DELETE_RULE as DELETE_RULE "
    +" ,PK.RDB$CONSTRAINT_NAME as PK_NAME "
    +" ,FK.RDB$CONSTRAINT_NAME as FK_NAME "
    +" ,null as DEFERRABILITY "
    +" from "
    +" RDB$RELATION_CONSTRAINTS PK "
    +" ,RDB$RELATION_CONSTRAINTS FK "
    +" ,RDB$REF_CONSTRAINTS RC "
    +" ,RDB$INDEX_SEGMENTS ISP "
    +" ,RDB$INDEX_SEGMENTS ISF "
    +" WHERE ";

    private static final String GET_EXPORTED_KEYS_END =
    " FK.RDB$CONSTRAINT_NAME = RC.RDB$CONSTRAINT_NAME "
    +" and PK.RDB$CONSTRAINT_NAME = RC.RDB$CONST_NAME_UQ "
    +" and ISP.RDB$INDEX_NAME = PK.RDB$INDEX_NAME "
    +" and ISF.RDB$INDEX_NAME = FK.RDB$INDEX_NAME "
    +" and ISP.RDB$FIELD_POSITION = ISF.RDB$FIELD_POSITION "
    +" order by 7, 9 ";

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
        checkCatalogAndSchema(catalog, schema);

        XSQLVAR[] xsqlvars = new XSQLVAR[14];

        xsqlvars[0] = new XSQLVAR();
        xsqlvars[0].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[0].sqllen = 31;
        xsqlvars[0].sqlname = "PKTABLE_CAT";
        xsqlvars[0].relname = "COLUMNINFO";

        xsqlvars[1] = new XSQLVAR();
        xsqlvars[1].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[1].sqllen = 31;
        xsqlvars[1].sqlname = "PKTABLE_SCHEM";
        xsqlvars[1].relname = "COLUMNINFO";

        xsqlvars[2] = new XSQLVAR();
        xsqlvars[2].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[2].sqllen = 31;
        xsqlvars[2].sqlname = "PKTABLE_NAME";
        xsqlvars[2].relname = "COLUMNINFO";

        xsqlvars[3] = new XSQLVAR();
        xsqlvars[3].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[3].sqllen = 31;
        xsqlvars[3].sqlname = "PKCOLUMN_NAME";
        xsqlvars[3].relname = "COLUMNINFO";

        xsqlvars[4] = new XSQLVAR();
        xsqlvars[4].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[4].sqllen = 31;
        xsqlvars[4].sqlname = "FKTABLE_CAT";
        xsqlvars[4].relname = "COLUMNINFO";

        xsqlvars[5] = new XSQLVAR();
        xsqlvars[5].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[5].sqllen = 31;
        xsqlvars[5].sqlname = "FKTABLE_SCHEM";
        xsqlvars[5].relname = "COLUMNINFO";

        xsqlvars[6] = new XSQLVAR();
        xsqlvars[6].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[6].sqllen = 31;
        xsqlvars[6].sqlname = "FKTABLE_NAME";
        xsqlvars[6].relname = "COLUMNINFO";

        xsqlvars[7] = new XSQLVAR();
        xsqlvars[7].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[7].sqllen = 31;
        xsqlvars[7].sqlname = "FKCOLUMN_NAME";
        xsqlvars[7].relname = "COLUMNINFO";

        xsqlvars[8] = new XSQLVAR();
        xsqlvars[8].sqltype = ISCConstants.SQL_SHORT;
        xsqlvars[8].sqlname = "KEY_SEQ";
        xsqlvars[8].relname = "COLUMNINFO";

        xsqlvars[9] = new XSQLVAR();
        xsqlvars[9].sqltype = ISCConstants.SQL_SHORT;
        xsqlvars[9].sqlname = "UPDATE_RULE";
        xsqlvars[9].relname = "COLUMNINFO";

        xsqlvars[10] = new XSQLVAR();
        xsqlvars[10].sqltype = ISCConstants.SQL_SHORT;
        xsqlvars[10].sqlname = "DELETE_RULE";
        xsqlvars[10].relname = "COLUMNINFO";

        xsqlvars[11] = new XSQLVAR();
        xsqlvars[11].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[11].sqllen = 31;
        xsqlvars[11].sqlname = "FK_NAME";
        xsqlvars[11].relname = "COLUMNINFO";

        xsqlvars[12] = new XSQLVAR();
        xsqlvars[12].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[12].sqllen = 31;
        xsqlvars[12].sqlname = "PK_NAME";
        xsqlvars[12].relname = "COLUMNINFO";

        xsqlvars[13] = new XSQLVAR();
        xsqlvars[13].sqltype = ISCConstants.SQL_SHORT;
        xsqlvars[13].sqlname = "DEFERRABILITY";
        xsqlvars[13].relname = "COLUMNINFO";

        Clause tableClause = new Clause("PK.RDB$RELATION_NAME", table);

        String sql = GET_EXPORTED_KEYS_START;
        sql += tableClause.getCondition();
        sql += GET_EXPORTED_KEYS_END;
        
        // check the original case identifiers first
        ArrayList params = new ArrayList();
        if (!tableClause.getCondition().equals("")) {
            params.add(tableClause.getOriginalCaseValue());
        }

        ArrayList rows = new ArrayList();
        ResultSet rs = doQuery(sql, params);
        
        // if nothing found, check the uppercased identifiers
        if (!rs.next()) {
            params.clear();
            if (!tableClause.getCondition().equals("")) {
                params.add(tableClause.getValue());
            }
            
            rs = doQuery(sql, params);
            
            // if nothing found, return an empty result set
            if (!rs.next())
                return new FBResultSet(xsqlvars, rows);
        }

        do {
            byte[][] row = new byte[14][];
            row[0] = null;
            row[1] = null;
            row[2] = getBytes(rs.getString("PKTABLE_NAME").trim());
            row[3] = getBytes(rs.getString("PKCOLUMN_NAME").trim());
            row[4] = null;
            row[5] = null;
            row[6] = getBytes(rs.getString("FKTABLE_NAME").trim());
            row[7] = getBytes(rs.getString("FKCOLUMN_NAME").trim());
            row[8] = xsqlvars[0].encodeShort(rs.getShort("KEY_SEQ"));
            String updateRule = rs.getString("UPDATE_RULE");
            if (updateRule.equals("NO ACTION") || updateRule.equals("RESTRICT"))
                row[9] = xsqlvars[0].encodeShort((short) DatabaseMetaData.importedKeyNoAction);
            else if (updateRule.equals("CASCADE"))
                row[9] = xsqlvars[0].encodeShort((short) DatabaseMetaData.importedKeyCascade);
            else if (updateRule.equals("SET NULL"))
                row[9] = xsqlvars[0].encodeShort((short) DatabaseMetaData.importedKeySetNull);
            else if (updateRule.equals("SET DEFAULT"))
                row[9] = xsqlvars[0].encodeShort((short) DatabaseMetaData.importedKeySetDefault);
            String deleteRule = rs.getString("DELETE_RULE");
            if (deleteRule.equals("NO ACTION") || deleteRule.equals("RESTRICT"))
                row[10] = xsqlvars[0].encodeShort((short) DatabaseMetaData.importedKeyNoAction);
            else if (deleteRule.equals("CASCADE"))
                row[10] = xsqlvars[0].encodeShort((short) DatabaseMetaData.importedKeyCascade);
            else if (deleteRule.equals("SET NULL"))
                row[10] = xsqlvars[0].encodeShort((short) DatabaseMetaData.importedKeySetNull);
            else if (deleteRule.equals("SET DEFAULT"))
                row[10] = xsqlvars[0].encodeShort((short) DatabaseMetaData.importedKeySetDefault);

            row[11] = getBytes(rs.getString("FK_NAME"));
            row[12] = getBytes(rs.getString("PK_NAME"));
            row[13] = xsqlvars[0].encodeShort((short) DatabaseMetaData.importedKeyNotDeferrable);

            rows.add(row);
        } while(rs.next());
        
        return new FBResultSet(xsqlvars, rows);
    }



    private static final String GET_CROSS_KEYS_START = "select"
    +" null as PKTABLE_CAT "
    +" ,null as PKTABLE_SCHEM "
    +" ,PK.RDB$RELATION_NAME as PKTABLE_NAME "
    +" ,ISP.RDB$FIELD_NAME as PKCOLUMN_NAME "
    +" ,null as FKTABLE_CAT "
    +" ,null as FKTABLE_SCHEM "
    +" ,FK.RDB$RELATION_NAME as FKTABLE_NAME "
    +" ,ISF.RDB$FIELD_NAME as FKCOLUMN_NAME "
    +" ,CAST ((ISP.RDB$FIELD_POSITION + 1) as SMALLINT) as KEY_SEQ "
    +" ,RC.RDB$UPDATE_RULE as UPDATE_RULE "
    +" ,RC.RDB$DELETE_RULE as DELETE_RULE "
    +" ,PK.RDB$CONSTRAINT_NAME as PK_NAME "
    +" ,FK.RDB$CONSTRAINT_NAME as FK_NAME "
    +" ,null as DEFERRABILITY "
    +" from "
    +" RDB$RELATION_CONSTRAINTS PK "
    +" ,RDB$RELATION_CONSTRAINTS FK "
    +" ,RDB$REF_CONSTRAINTS RC "
    +" ,RDB$INDEX_SEGMENTS ISP "
    +" ,RDB$INDEX_SEGMENTS ISF "
    +" WHERE ";

    private static final String GET_CROSS_KEYS_END =
    " FK.RDB$CONSTRAINT_NAME = RC.RDB$CONSTRAINT_NAME "
    +" and PK.RDB$CONSTRAINT_NAME = RC.RDB$CONST_NAME_UQ "
    +" and ISP.RDB$INDEX_NAME = PK.RDB$INDEX_NAME "
    +" and ISF.RDB$INDEX_NAME = FK.RDB$INDEX_NAME "
    +" and ISP.RDB$FIELD_POSITION = ISF.RDB$FIELD_POSITION "
    +" order by 7, 9 ";

    /**
     * Gets a description of the foreign key columns in the foreign key
     * table that reference the primary key columns of the primary key
     * table (describe how one table imports another's key). This
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
        checkCatalogAndSchema(primaryCatalog, primarySchema);
        checkCatalogAndSchema(foreignCatalog, foreignSchema);

        XSQLVAR[] xsqlvars = new XSQLVAR[14];

        xsqlvars[0] = new XSQLVAR();
        xsqlvars[0].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[0].sqllen = 31;
        xsqlvars[0].sqlname = "PKTABLE_CAT";
        xsqlvars[0].relname = "COLUMNINFO";

        xsqlvars[1] = new XSQLVAR();
        xsqlvars[1].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[1].sqllen = 31;
        xsqlvars[1].sqlname = "PKTABLE_SCHEM";
        xsqlvars[1].relname = "COLUMNINFO";

        xsqlvars[2] = new XSQLVAR();
        xsqlvars[2].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[2].sqllen = 31;
        xsqlvars[2].sqlname = "PKTABLE_NAME";
        xsqlvars[2].relname = "COLUMNINFO";

        xsqlvars[3] = new XSQLVAR();
        xsqlvars[3].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[3].sqllen = 31;
        xsqlvars[3].sqlname = "PKCOLUMN_NAME";
        xsqlvars[3].relname = "COLUMNINFO";

        xsqlvars[4] = new XSQLVAR();
        xsqlvars[4].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[4].sqllen = 31;
        xsqlvars[4].sqlname = "FKTABLE_CAT";
        xsqlvars[4].relname = "COLUMNINFO";

        xsqlvars[5] = new XSQLVAR();
        xsqlvars[5].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[5].sqllen = 31;
        xsqlvars[5].sqlname = "FKTABLE_SCHEM";
        xsqlvars[5].relname = "COLUMNINFO";

        xsqlvars[6] = new XSQLVAR();
        xsqlvars[6].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[6].sqllen = 31;
        xsqlvars[6].sqlname = "FKTABLE_NAME";
        xsqlvars[6].relname = "COLUMNINFO";

        xsqlvars[7] = new XSQLVAR();
        xsqlvars[7].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[7].sqllen = 31;
        xsqlvars[7].sqlname = "FKCOLUMN_NAME";
        xsqlvars[7].relname = "COLUMNINFO";

        xsqlvars[8] = new XSQLVAR();
        xsqlvars[8].sqltype = ISCConstants.SQL_SHORT;
        xsqlvars[8].sqlname = "KEY_SEQ";
        xsqlvars[8].relname = "COLUMNINFO";

        xsqlvars[9] = new XSQLVAR();
        xsqlvars[9].sqltype = ISCConstants.SQL_SHORT;
        xsqlvars[9].sqlname = "UPDATE_RULE";
        xsqlvars[9].relname = "COLUMNINFO";

        xsqlvars[10] = new XSQLVAR();
        xsqlvars[10].sqltype = ISCConstants.SQL_SHORT;
        xsqlvars[10].sqlname = "DELETE_RULE";
        xsqlvars[10].relname = "COLUMNINFO";

        xsqlvars[11] = new XSQLVAR();
        xsqlvars[11].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[11].sqllen = 31;
        xsqlvars[11].sqlname = "FK_NAME";
        xsqlvars[11].relname = "COLUMNINFO";

        xsqlvars[12] = new XSQLVAR();
        xsqlvars[12].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[12].sqllen = 31;
        xsqlvars[12].sqlname = "PK_NAME";
        xsqlvars[12].relname = "COLUMNINFO";

        xsqlvars[13] = new XSQLVAR();
        xsqlvars[13].sqltype = ISCConstants.SQL_SHORT;
        xsqlvars[13].sqlname = "DEFERRABILITY";
        xsqlvars[13].relname = "COLUMNINFO";

        Clause primaryTableClause = new Clause("PK.RDB$RELATION_NAME", primaryTable);
        Clause foreignTableClause = new Clause("FK.RDB$RELATION_NAME", foreignTable);
        
        String sql = GET_CROSS_KEYS_START;
        sql += primaryTableClause.getCondition();
        sql += foreignTableClause.getCondition();
        sql += GET_CROSS_KEYS_END;
        
        ArrayList params = new ArrayList();
        
        // check the original case first
        if (!primaryTableClause.getCondition().equals("")) {
            params.add(primaryTableClause.getOriginalCaseValue());
        }
        if (!foreignTableClause.getCondition().equals("")) {
            params.add(foreignTableClause.getOriginalCaseValue());
        }

        ArrayList rows = new ArrayList();
        ResultSet rs = doQuery(sql, params);

        // if nothing found, check the uppercased identifiers
        if (!rs.next()) {
            params.clear();
            if (!primaryTableClause.getCondition().equals("")) {
                params.add(primaryTableClause.getValue());
            }
            if (!foreignTableClause.getCondition().equals("")) {
                params.add(foreignTableClause.getValue());
            }

            rs = doQuery(sql, params);
            
            // return empty result set if nothing found
            if (!rs.next())
                return new FBResultSet(xsqlvars, rows);
        }
        
        do {
            byte[][] row = new byte[14][];
            row[0] = null;
            row[1] = null;
            row[2] = getBytes(rs.getString("PKTABLE_NAME").trim());
            row[3] = getBytes(rs.getString("PKCOLUMN_NAME").trim());
            row[4] = null;
            row[5] = null;
            row[6] = getBytes(rs.getString("FKTABLE_NAME").trim());
            row[7] = getBytes(rs.getString("FKCOLUMN_NAME").trim());
            row[8] = xsqlvars[0].encodeShort(rs.getShort("KEY_SEQ"));
            String updateRule = rs.getString("UPDATE_RULE");
            if (updateRule.equals("NO ACTION") || updateRule.equals("RESTRICT"))
                row[9] = xsqlvars[0].encodeShort((short) DatabaseMetaData.importedKeyNoAction);
            else if (updateRule.equals("CASCADE"))
                row[9] = xsqlvars[0].encodeShort((short) DatabaseMetaData.importedKeyCascade);
            else if (updateRule.equals("SET NULL"))
                row[9] = xsqlvars[0].encodeShort((short) DatabaseMetaData.importedKeySetNull);
            else if (updateRule.equals("SET DEFAULT"))
                row[9] = xsqlvars[0].encodeShort((short) DatabaseMetaData.importedKeySetDefault);
            String deleteRule = rs.getString("DELETE_RULE");
            if (deleteRule.equals("NO ACTION") || deleteRule.equals("RESTRICT"))
                row[10] = xsqlvars[0].encodeShort((short) DatabaseMetaData.importedKeyNoAction);
            else if (deleteRule.equals("CASCADE"))
                row[10] = xsqlvars[0].encodeShort((short) DatabaseMetaData.importedKeyCascade);
            else if (deleteRule.equals("SET NULL"))
                row[10] = xsqlvars[0].encodeShort((short) DatabaseMetaData.importedKeySetNull);
            else if (deleteRule.equals("SET DEFAULT"))
                row[10] = xsqlvars[0].encodeShort((short) DatabaseMetaData.importedKeySetDefault);
            row[11] = getBytes(rs.getString("FK_NAME"));
            row[12] = getBytes(rs.getString("PK_NAME"));
            row[13] = xsqlvars[0].encodeShort((short) DatabaseMetaData.importedKeyNotDeferrable);

            rows.add(row);
        } while(rs.next());
        return new FBResultSet(xsqlvars, rows);
    }


    /**
     * Simple convertor function to convert integer values to Short objects.
     * Used in {@link #getTypeInfo()} for values of {@link java.sql.Types} class.
     *
     * @param value integer value to convert
     * @return instance of java.lang.Short representing the value
     */
    private byte[] createShort(int value) throws SQLException {
        if (value > Short.MAX_VALUE)
            throw new FBSQLException("Cannot convert integer to short.",
                    FBSQLException.SQL_STATE_INVALID_ARG_VALUE);
        
        return new XSQLVAR().encodeShort((short)value);
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

		final XSQLVAR anXSQLVAR = new XSQLVAR();

        byte[] shortZero = anXSQLVAR.encodeShort((short)0);
        byte[] CASESENSITIVE = getBytes("T");
        byte[] CASEINSENSITIVE = getBytes("F");
        byte[] UNSIGNED = getBytes("T");
        byte[] SIGNED = getBytes("F");
        byte[] FIXEDSCALE = getBytes("T");
        byte[] VARIABLESCALE = getBytes("F");
        byte[] NOTAUTOINC = getBytes("F");
        byte[] BINARY = anXSQLVAR.encodeInt(2);
        byte[] PREDNONE = anXSQLVAR.encodeShort((short) DatabaseMetaData.typePredNone);
        byte[] PREDBASIC = anXSQLVAR.encodeShort((short) DatabaseMetaData.typePredBasic);
        byte[] SEARCHABLE = anXSQLVAR.encodeShort((short) DatabaseMetaData.typeSearchable);
        byte[] NULLABLE = anXSQLVAR.encodeShort((short) DatabaseMetaData.typeNullable);
        //need to construct xsqlvar[] for ResultSetMetaData.
        XSQLVAR[] xsqlvars = new XSQLVAR[18];

        xsqlvars[0] = new XSQLVAR();
        xsqlvars[0].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[0].sqllen = 31;
        xsqlvars[0].sqlname = "TYPE_NAME";
        xsqlvars[0].relname = "TYPEINFO";

        xsqlvars[1] = new XSQLVAR();
        xsqlvars[1].sqltype = ISCConstants.SQL_SHORT;
        xsqlvars[1].sqlname = "DATA_TYPE";
        xsqlvars[1].relname = "TYPEINFO";

        xsqlvars[2] = new XSQLVAR();
        xsqlvars[2].sqltype = ISCConstants.SQL_LONG;
        xsqlvars[2].sqlname = "PRECISION";
        xsqlvars[2].relname = "TYPEINFO";

        xsqlvars[3] = new XSQLVAR();
        xsqlvars[3].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[3].sqllen = 1;
        xsqlvars[3].sqlname = "LITERAL_PREFIX";
        xsqlvars[3].relname = "TYPEINFO";

        xsqlvars[4] = new XSQLVAR();
        xsqlvars[4].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[4].sqllen = 1;
        xsqlvars[4].sqlname = "LITERAL_SUFFIX";
        xsqlvars[4].relname = "TYPEINFO";

        xsqlvars[5] = new XSQLVAR();
        xsqlvars[5].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[5].sqllen = 31;
        xsqlvars[5].sqlname = "CREATE_PARAMS";
        xsqlvars[5].relname = "TYPEINFO";

        xsqlvars[6] = new XSQLVAR();
        xsqlvars[6].sqltype = ISCConstants.SQL_SHORT;
        xsqlvars[6].sqlname = "NULLABLE";
        xsqlvars[6].relname = "TYPEINFO";

        xsqlvars[7] = new XSQLVAR();
        xsqlvars[7].sqltype = ISCConstants.SQL_TEXT;
        xsqlvars[7].sqllen = 1;
        xsqlvars[7].sqlname = "CASE_SENSITIVE";
        xsqlvars[7].relname = "TYPEINFO";

        xsqlvars[8] = new XSQLVAR();
        xsqlvars[8].sqltype = ISCConstants.SQL_SHORT;
        xsqlvars[8].sqlname = "SEARCHABLE";
        xsqlvars[8].relname = "TYPEINFO";

        xsqlvars[9] = new XSQLVAR();
        xsqlvars[9].sqltype = ISCConstants.SQL_TEXT;
        xsqlvars[9].sqllen = 1;
        xsqlvars[9].sqlname = "UNSIGNED_ATTRIBUTE";
        xsqlvars[9].relname = "TYPEINFO";

        xsqlvars[10] = new XSQLVAR();
        xsqlvars[10].sqltype = ISCConstants.SQL_TEXT;
        xsqlvars[10].sqllen = 1;
        xsqlvars[10].sqlname = "FIXED_PREC_SCALE";
        xsqlvars[10].relname = "TYPEINFO";

        xsqlvars[11] = new XSQLVAR();
        xsqlvars[11].sqltype = ISCConstants.SQL_TEXT;
        xsqlvars[11].sqllen = 1;
        xsqlvars[11].sqlname = "AUTO_INCREMENT";
        xsqlvars[11].relname = "TYPEINFO";

        xsqlvars[12] = new XSQLVAR();
        xsqlvars[12].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[12].sqllen = 31;
        xsqlvars[12].sqlname = "LOCAL_TYPE_NAME";
        xsqlvars[12].relname = "TYPEINFO";

        xsqlvars[13] = new XSQLVAR();
        xsqlvars[13].sqltype = ISCConstants.SQL_SHORT;
        xsqlvars[13].sqlname = "MINIMUM_SCALE";
        xsqlvars[13].relname = "TYPEINFO";

        xsqlvars[14] = new XSQLVAR();
        xsqlvars[14].sqltype = ISCConstants.SQL_SHORT;
        xsqlvars[14].sqlname = "MAXIMUM_SCALE";
        xsqlvars[14].relname = "TYPEINFO";

        xsqlvars[15] = new XSQLVAR();
        xsqlvars[15].sqltype = ISCConstants.SQL_LONG;
        xsqlvars[15].sqlname = "SQL_DATA_TYPE";
        xsqlvars[15].relname = "TYPEINFO";

        xsqlvars[16] = new XSQLVAR();
        xsqlvars[16].sqltype = ISCConstants.SQL_LONG;
        xsqlvars[16].sqlname = "SQL_DATETIME_SUB";
        xsqlvars[16].relname = "TYPEINFO";

        xsqlvars[17] = new XSQLVAR();
        xsqlvars[17].sqltype = ISCConstants.SQL_LONG;
        xsqlvars[17].sqlname = "NUM_PREC_RADIX";
        xsqlvars[17].relname = "TYPEINFO";

        //dialect 3 only
        ArrayList rows = new ArrayList();

        //BIGINT=-5
        rows.add(new byte[][] {getBytes("BIGINT"), createShort(Types.BIGINT)
            , anXSQLVAR.encodeInt(64), null, null, null,
            NULLABLE, CASEINSENSITIVE, SEARCHABLE, SIGNED, FIXEDSCALE,
            NOTAUTOINC, null, shortZero, shortZero, anXSQLVAR.encodeInt(ISCConstants.SQL_INT64), null, BINARY});
        
        //LONGVARBINARY=-4
        rows.add(new byte[][] {getBytes("BLOB SUB_TYPE 0"), createShort(Types.LONGVARBINARY)
            , anXSQLVAR.encodeInt(0), null, null, null,
            NULLABLE, CASESENSITIVE, PREDNONE, UNSIGNED, FIXEDSCALE,
            NOTAUTOINC, null, shortZero, shortZero, anXSQLVAR.encodeInt(ISCConstants.SQL_BLOB), null, BINARY});

        //LONGVARCHAR=-1
        rows.add(new byte[][] {getBytes("BLOB SUB_TYPE 1"), createShort(Types.LONGVARCHAR)
            , anXSQLVAR.encodeInt(0), null, null, null,
            NULLABLE, CASESENSITIVE, PREDNONE, UNSIGNED, FIXEDSCALE,
            NOTAUTOINC, null, shortZero, shortZero, anXSQLVAR.encodeInt(ISCConstants.SQL_BLOB), null, BINARY});

        //CHAR=1
        rows.add(new byte[][] {getBytes("CHAR"), createShort(Types.CHAR)
            , anXSQLVAR.encodeInt(32664), getBytes("'"), getBytes("'"), getBytes("length"),
            NULLABLE, CASESENSITIVE, SEARCHABLE, UNSIGNED, FIXEDSCALE,
            NOTAUTOINC, null, shortZero, shortZero, anXSQLVAR.encodeInt(ISCConstants.SQL_TEXT), null, BINARY});

        //NUMERIC=2
        rows.add(new byte[][] {getBytes("NUMERIC"), createShort(Types.NUMERIC)
            , anXSQLVAR.encodeInt(18), null, null, getBytes("precision,scale"),
            NULLABLE, CASEINSENSITIVE, SEARCHABLE, SIGNED, FIXEDSCALE,
            NOTAUTOINC, null, shortZero, createShort(18), anXSQLVAR.encodeInt(ISCConstants.SQL_INT64), null, BINARY});

        //DECIMAL=3
        rows.add(new byte[][] {getBytes("DECIMAL"), createShort(Types.DECIMAL)
            , anXSQLVAR.encodeInt(18), null, null, getBytes("precision,scale"),
            NULLABLE, CASEINSENSITIVE, SEARCHABLE, SIGNED, FIXEDSCALE,
            NOTAUTOINC, null, shortZero, createShort(18), anXSQLVAR.encodeInt(ISCConstants.SQL_INT64), null, BINARY});

        //INTEGER=4
        rows.add(new byte[][] {getBytes("INTEGER"), createShort(Types.INTEGER)
            , anXSQLVAR.encodeInt(32), null, null, null,
            NULLABLE, CASEINSENSITIVE, SEARCHABLE, SIGNED, FIXEDSCALE,
            NOTAUTOINC, null, shortZero, shortZero, anXSQLVAR.encodeInt(ISCConstants.SQL_LONG), null, BINARY});

        //SMALLINT=5
        rows.add(new byte[][] {getBytes("SMALLINT"), createShort(Types.SMALLINT)
            , anXSQLVAR.encodeInt(16), null, null, null,
            NULLABLE, CASEINSENSITIVE, SEARCHABLE, SIGNED, FIXEDSCALE,
            NOTAUTOINC, null, shortZero, shortZero, anXSQLVAR.encodeInt(ISCConstants.SQL_SHORT), null, BINARY});

        //FLOAT=6
        rows.add(new byte[][] {getBytes("FLOAT"), createShort(Types.FLOAT)
            , anXSQLVAR.encodeInt(7), null, null, null,
            NULLABLE, CASEINSENSITIVE, SEARCHABLE, SIGNED, VARIABLESCALE,
            NOTAUTOINC, null, createShort(0), createShort(7), anXSQLVAR.encodeInt(ISCConstants.SQL_FLOAT), null, BINARY});

        //DOUBLE=8
        rows.add(new byte[][] {getBytes("DOUBLE PRECISION"), createShort(Types.DOUBLE)
            , anXSQLVAR.encodeInt(15), null, null, null,
            NULLABLE, CASEINSENSITIVE, SEARCHABLE, SIGNED, VARIABLESCALE,
            NOTAUTOINC, null, createShort(0), createShort(15), anXSQLVAR.encodeInt(ISCConstants.SQL_DOUBLE), null, BINARY});

        //VARCHAR=12
        rows.add(new byte[][] {getBytes("VARCHAR"), createShort(Types.VARCHAR)
            , anXSQLVAR.encodeInt(32664), getBytes("'"), getBytes("'"), getBytes("length"),
            NULLABLE, CASESENSITIVE, SEARCHABLE, UNSIGNED, FIXEDSCALE,
            NOTAUTOINC, null, shortZero, shortZero, anXSQLVAR.encodeInt(ISCConstants.SQL_VARYING), null, BINARY});

        //DATE=91
        rows.add(new byte[][] {getBytes("DATE"), createShort(Types.DATE)
            , anXSQLVAR.encodeInt(0), null, null, null,
            NULLABLE, CASEINSENSITIVE, SEARCHABLE, UNSIGNED, FIXEDSCALE,
            NOTAUTOINC, null, shortZero, shortZero, anXSQLVAR.encodeInt(ISCConstants.SQL_TYPE_DATE), null, BINARY});

        //TIME=92
        rows.add(new byte[][] {getBytes("TIME"), createShort(Types.TIME)
            , anXSQLVAR.encodeInt(0), null, null, null,
            NULLABLE, CASEINSENSITIVE, SEARCHABLE, UNSIGNED, FIXEDSCALE,
            NOTAUTOINC, null, shortZero, shortZero, anXSQLVAR.encodeInt(ISCConstants.SQL_TYPE_TIME), null, BINARY});

        //TIMESTAMP=93
        rows.add(new byte[][] {getBytes("TIMESTAMP"), createShort(Types.TIMESTAMP)
            , anXSQLVAR.encodeInt(0), null, null, null,
            NULLABLE, CASEINSENSITIVE, SEARCHABLE, UNSIGNED, FIXEDSCALE,
            NOTAUTOINC, null, shortZero, shortZero, anXSQLVAR.encodeInt(ISCConstants.SQL_TIMESTAMP), null, BINARY});

        //OTHER=1111
        rows.add(new byte[][] {getBytes("ARRAY"), createShort(Types.OTHER)
            , anXSQLVAR.encodeInt(0), null, null, null,
            NULLABLE, CASESENSITIVE, PREDNONE, UNSIGNED, FIXEDSCALE,
            NOTAUTOINC, null, shortZero, shortZero, anXSQLVAR.encodeInt(ISCConstants.SQL_ARRAY), null, BINARY});

        //BLOB=2004
        rows.add(new byte[][] {getBytes("BLOB SUB_TYPE <0 "), createShort(Types.BLOB)
            , anXSQLVAR.encodeInt(0), null, null, null,
            NULLABLE, CASESENSITIVE, PREDNONE, UNSIGNED, FIXEDSCALE,
            NOTAUTOINC, null, shortZero, shortZero, anXSQLVAR.encodeInt(ISCConstants.SQL_BLOB), null, BINARY});

        return new FBResultSet(xsqlvars, rows);

    }

    /**
     * A possible value for column <code>SEARCHABLE</code> in the
     * <code>ResultSet</code> object returned by the method
     * <code>getTypeInfo</code>.
     * <p>Indicates that all <code>WHERE</code> search clauses can be
     * based on this type.
     */
    //int typeSearchable  = 3;

    private static final String GET_INDEX_INFO_COLUMN_LIST = ""
        + "  NULL as TABLE_CAT "
        + ", NULL as TABLE_SCHEM "
        + ", ind.RDB$RELATION_NAME AS TABLE_NAME "
        + ", ind.RDB$UNIQUE_FLAG AS NON_UNIQUE "
        + ", NULL as INDEX_QUALIFIER "
        + ", ind.RDB$INDEX_NAME as INDEX_NAME "
        + ", NULL as ITYPE "
        + ", ise.rdb$field_position+1 as ORDINAL_POSITION "
        + ", ise.rdb$field_name as COLUMN_NAME "
        + ", ind.RDB$INDEX_TYPE as ASC_OR_DESC "
        + ", 0 as CARDINALITY "
        + ", 0 as IPAGES "
        + ", null as FILTER_CONDITION "
        ;
    
    private static final String GET_INDEX_INFO = "" 
        + "SELECT"
        +   GET_INDEX_INFO_COLUMN_LIST
        + "FROM "
        + "  rdb$indices ind, "
        + "  rdb$index_segments ise "
        + "WHERE "
        + "  ind.rdb$index_name = ise.rdb$index_name "
        + "AND " 
        + "  ind.rdb$relation_name = ? "
        + "ORDER BY 4, 6, 8"
        ;
    
    private static final String GET_INDEX_INFO_UPPER = ""
        + "SELECT"
        +   GET_INDEX_INFO_COLUMN_LIST
        + "FROM "
        + "  rdb$indices ind, "
        + "  rdb$index_segments ise "
        + "WHERE "
        + "  ind.rdb$index_name = ise.rdb$index_name "
        + "AND " 
        + "  UPPER(ind.rdb$relation_name) = ? "
        + "ORDER BY 4, 6, 8"
        ;

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
        checkCatalogAndSchema(catalog, schema);

        XSQLVAR[] xsqlvars = new XSQLVAR[13];

        xsqlvars[0] = new XSQLVAR();
        xsqlvars[0].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[0].sqllen = 31;
        xsqlvars[0].sqlname = "TABLE_CAT";
        xsqlvars[0].relname = "INDEXINFO";

        xsqlvars[1] = new XSQLVAR();
        xsqlvars[1].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[1].sqllen = 31;
        xsqlvars[1].sqlname = "TABLE_SCHEM";
        xsqlvars[1].relname = "INDEXINFO";

        xsqlvars[2] = new XSQLVAR();
        xsqlvars[2].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[2].sqllen = 31;
        xsqlvars[2].sqlname = "TABLE_NAME";
        xsqlvars[2].relname = "INDEXINFO";

        xsqlvars[3] = new XSQLVAR();
        xsqlvars[3].sqltype = ISCConstants.SQL_TEXT;
        xsqlvars[3].sqllen = 1;
        xsqlvars[3].sqlname = "NON_UNIQUE";
        xsqlvars[3].relname = "INDEXINFO";

        xsqlvars[4] = new XSQLVAR();
        xsqlvars[4].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[4].sqllen = 31;
        xsqlvars[4].sqlname = "INDEX_QUALIFIER";
        xsqlvars[4].relname = "INDEXINFO";

        xsqlvars[5] = new XSQLVAR();
        xsqlvars[5].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[5].sqllen = 31;
        xsqlvars[5].sqlname = "INDEX_NAME";
        xsqlvars[5].relname = "INDEXINFO";

        xsqlvars[6] = new XSQLVAR();
        xsqlvars[6].sqltype = ISCConstants.SQL_SHORT;
        xsqlvars[6].sqlname = "TYPE";
        xsqlvars[6].relname = "INDEXINFO";

        xsqlvars[7] = new XSQLVAR();
        xsqlvars[7].sqltype = ISCConstants.SQL_SHORT;
        xsqlvars[7].sqlname = "ORDINAL_POSITION";
        xsqlvars[7].relname = "INDEXINFO";

        xsqlvars[8] = new XSQLVAR();
        xsqlvars[8].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[8].sqllen = 31;
        xsqlvars[8].sqlname = "COLUMN_NAME";
        xsqlvars[8].relname = "INDEXINFO";

        xsqlvars[9] = new XSQLVAR();
        xsqlvars[9].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[9].sqllen = 31;
        xsqlvars[9].sqlname = "ASC_OR_DESC";
        xsqlvars[9].relname = "INDEXINFO";

        xsqlvars[10] = new XSQLVAR();
        xsqlvars[10].sqltype = ISCConstants.SQL_LONG;
        xsqlvars[10].sqlname = "CARDINALITY";
        xsqlvars[10].relname = "INDEXINFO";

        xsqlvars[11] = new XSQLVAR();
        xsqlvars[11].sqltype = ISCConstants.SQL_LONG;
        xsqlvars[11].sqlname = "PAGES";
        xsqlvars[11].relname = "INDEXINFO";

        xsqlvars[12] = new XSQLVAR();
        xsqlvars[12].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[12].sqllen = 31;
        xsqlvars[12].sqlname = "FILTER_CONDITION";
        xsqlvars[12].relname = "INDEXINFO";

        ArrayList rows = new ArrayList();

        ArrayList params = new ArrayList();
        params.add(table);

        ResultSet rs = doQuery(GET_INDEX_INFO, params);

        // if no direct match happened, check the uppercased match
        if (!rs.next()) {
            params.clear();
            params.add(table.toUpperCase());
            rs = doQuery(GET_INDEX_INFO_UPPER, params);
            
            // open the second result set and check whether we have rows
            // if no rows are available, we have to exit now, otherwise the 
            // following do/while loop will throw SQLException that the
            // result set is not positioned on a row
            if (!rs.next())
                return new FBResultSet(xsqlvars, rows);
        }
        
        do {
            byte[][] row = new byte[13][];
            row[0] = null;
            row[1] = null;
            row[2] = getBytes(rs.getString("TABLE_NAME").trim());
            int nonUnique = rs.getInt("NON_UNIQUE");
            if (nonUnique==0)
                row[3] = getBytes("T");
            else
                row[3] = getBytes("F");
            row[4] = null;
            row[5] = getBytes(rs.getString("INDEX_NAME").trim());
            row[6] = xsqlvars[0].encodeShort((short) DatabaseMetaData.tableIndexOther);
            row[7] = xsqlvars[0].encodeShort(rs.getShort("ORDINAL_POSITION"));
            row[8] = getBytes(rs.getString("COLUMN_NAME").trim());
            int index_type = rs.getInt("ASC_OR_DESC");
            if (index_type == 1)
                row[9] = getBytes("D");
            else
                row[9] = getBytes("A");
            row[10] = xsqlvars[0].encodeInt(0);
            row[11] = xsqlvars[0].encodeInt(0);
            row[12] = null;

            rows.add(row);
            
        } while (rs.next());
        
        return new FBResultSet(xsqlvars, rows);
    }

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
        switch (type){
            case ResultSet.TYPE_FORWARD_ONLY:
            case ResultSet.TYPE_SCROLL_INSENSITIVE :
            case ResultSet.TYPE_SCROLL_SENSITIVE :
                return true;
            default:
                return false;
        }
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
        switch(type) {
            case ResultSet.TYPE_FORWARD_ONLY:
            case ResultSet.TYPE_SCROLL_INSENSITIVE :
            case ResultSet.TYPE_SCROLL_SENSITIVE :
                return concurrency == ResultSet.CONCUR_READ_ONLY || 
                    concurrency == ResultSet.CONCUR_UPDATABLE;
            default:
                return false;
        }
    }



    /**
     *
     * Indicates whether a result set's own updates are visible.
     *
     * @param type result set type, i.e. ResultSet.TYPE_XXX
     * @return <code>true</code> if updates are visible for the result set type;
     *        <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
    public boolean ownUpdatesAreVisible(int type) throws SQLException {
        return ResultSet.TYPE_SCROLL_INSENSITIVE == type ||
            ResultSet.TYPE_SCROLL_SENSITIVE == type;
    }



    /**
     *
     * Indicates whether a result set's own deletes are visible.
     *
     * @param type result set type, i.e. ResultSet.TYPE_XXX
     * @return <code>true</code> if deletes are visible for the result set type;
     *        <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
    public boolean ownDeletesAreVisible(int type) throws SQLException {
        return ResultSet.TYPE_SCROLL_INSENSITIVE == type ||
            ResultSet.TYPE_SCROLL_SENSITIVE == type;
    }



    /**
     *
     * Indicates whether a result set's own inserts are visible.
     *
     * @param type result set type, i.e. ResultSet.TYPE_XXX
     * @return <code>true</code> if inserts are visible for the result set type;
     *        <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
    public boolean ownInsertsAreVisible(int type) throws SQLException {
        return ResultSet.TYPE_SCROLL_INSENSITIVE == type ||
            ResultSet.TYPE_SCROLL_SENSITIVE == type;
    }



    /**
     *
     * Indicates whether updates made by others are visible.
     *
     * @param type result set type, i.e. ResultSet.TYPE_XXX
     * @return <code>true</code> if updates made by others
     * are visible for the result set type;
     *        <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
    public boolean othersUpdatesAreVisible(int type) throws SQLException {
        return false;
    }



    /**
     *
     * Indicates whether deletes made by others are visible.
     *
     * @param type result set type, i.e. ResultSet.TYPE_XXX
     * @return <code>true</code> if deletes made by others
     * are visible for the result set type;
     *        <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
    public boolean othersDeletesAreVisible(int type) throws SQLException {
        return false;
    }



    /**
     *
     * Indicates whether inserts made by others are visible.
     *
     * @param type result set type, i.e. ResultSet.TYPE_XXX
     * @return true if updates are visible for the result set type
     * @return <code>true</code> if inserts made by others
     * are visible for the result set type;
     *        <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
    public boolean othersInsertsAreVisible(int type) throws SQLException {
        return false;
    }



    /**
     *
     * Indicates whether or not a visible row update can be detected by
     * calling the method <code>ResultSet.rowUpdated</code>.
     *
     * @param type result set type, i.e. ResultSet.TYPE_XXX
     * @return <code>true</code> if changes are detected by the result set type;
     *         <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
    public boolean updatesAreDetected(int type) throws SQLException {
        return false;
    }



    /**
     *
     * Indicates whether or not a visible row delete can be detected by
     * calling ResultSet.rowDeleted().  If deletesAreDetected()
     * returns false, then deleted rows are removed from the result set.
     *
     * @param type result set type, i.e. ResultSet.TYPE_XXX
     * @return true if changes are detected by the resultset type
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
    public boolean deletesAreDetected(int type) throws SQLException {
        return false;
    }



    /**
     *
     * Indicates whether or not a visible row insert can be detected
     * by calling ResultSet.rowInserted().
     *
     * @param type result set type, i.e. ResultSet.TYPE_XXX
     * @return true if changes are detected by the resultset type
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
    public boolean insertsAreDetected(int type) throws SQLException {
        return false;
    }



    /**
     *
     * Indicates whether the driver supports batch updates.
     * @return true if the driver supports batch updates; false otherwise
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
    public boolean supportsBatchUpdates() throws SQLException {
        return true;
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
        XSQLVAR[] xsqlvars = new XSQLVAR[6];

        xsqlvars[0] = new XSQLVAR();
        xsqlvars[0].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[0].sqllen = 31;
        xsqlvars[0].sqlname = "TYPE_CAT";
        xsqlvars[0].relname = "UDT";

        xsqlvars[1] = new XSQLVAR();
        xsqlvars[1].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[1].sqllen = 31;
        xsqlvars[1].sqlname = "TYPE_SCHEM";
        xsqlvars[1].relname = "UDT";

        xsqlvars[2] = new XSQLVAR();
        xsqlvars[2].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[2].sqllen = 31;
        xsqlvars[2].sqlname = "TYPE_NAME";
        xsqlvars[2].relname = "UDT";

        xsqlvars[3] = new XSQLVAR();
        xsqlvars[3].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[3].sqllen = 31;
        xsqlvars[3].sqlname = "CLASS_NAME";
        xsqlvars[3].relname = "UDT";

        xsqlvars[4] = new XSQLVAR();
        xsqlvars[4].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[4].sqllen = 31;
        xsqlvars[4].sqlname = "DATA_TYPE";
        xsqlvars[4].relname = "UDT";

        xsqlvars[5] = new XSQLVAR();
        xsqlvars[5].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[5].sqllen = 31;
        xsqlvars[5].sqlname = "REMARKS";
        xsqlvars[5].relname = "UDT";

        ArrayList rows = new ArrayList(0);

        return new FBResultSet(xsqlvars, rows);
    }



    /**
     * Retrieves the connection that produced this metadata object.
     *
     * @return the connection that produced this metadata object
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
    public Connection getConnection() throws SQLException {
        return connection;
    }

    //jdbc 3 methods


    /**
     * <b>This operation is not supported</b>
     *
     * @param param1 <description>
     * @param param2 <description>
     * @param param3 <description>
     * @param param4 <description>
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public ResultSet getAttributes(String param1, String param2, String param3, String param4) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    /**
     * Retrieves whether this database supports savepoints.
     *
     * @return true if savepoints are supported; false otherwise 
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsSavepoints() throws SQLException {
        return getDatabaseMajorVersion() >= 1 && getDatabaseMinorVersion() >= 5;
    }

    /**
     * Retrieve whether this database supports named parameters.
     *
     * @return true if named parameters are supported, false otherwise 
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsNamedParameters() throws SQLException {
        return false;
    }

    /**
     * Retrieves whether it is possible to have multiple <code>ResultSet</code>
     * objects returned from a <code>CallableStatement</code> object 
     * simultaneously.
     *
     * @return true if multiple open ResultSets are supported, false otherwise
     * @exception SQLException if a database access error occurs 
     */
    public boolean supportsMultipleOpenResults() throws SQLException {
        return false;
    }

    /**
     * Retrieves whether auto-generated keys can be retrieved after creation.
     *
     * @return true if auto-generated keys can be retrieved, false otherwise
     * @exception SQLException if a database access error occurs 
     */
    public boolean supportsGetGeneratedKeys() throws SQLException {
        return false;
    }


    /**
     *
     * Retrieves a description of the user-defined type (UDT) hierarchies 
     * defined in a particular schema in this database. Only the immediate 
     * super type/sub type relationship is modeled.
     * <P>
     * Only supertype information for UDTs matching the catalog, 
     * schema, and type name is returned. The type name parameter
     * may be a fully-qualified name. When the UDT name supplied is a 
     * fully-qualified name, the catalog and schemaPattern parameters are 
     * ignored. 
     * <P>
     * If a UDT does not have a direct super type, it is not listed here.
     * A row of the <code>ResultSet</code> object returned by this method
     * describes the designated UDT and a direct supertype. A row has the 
     * following columns:
     *  <OL>
     *  <LI><B>TYPE_CAT</B> String => the UDT's catalog (may be 
     *  <code>null</code>)
     *  <LI><B>TYPE_SCHEM</B> String => UDT's schema (may be <code>null</code>)
     *  <LI><B>TYPE_NAME</B> String => type name of the UDT
     *  <LI><B>SUPERTYPE_CAT</B> String => the direct super type's catalog 
     *                           (may be <code>null</code>)
     *  <LI><B>SUPERTYPE_SCHEM</B> String => the direct super type's schema 
     *                             (may be <code>null</code>)
     *  <LI><B>SUPERTYPE_NAME</B> String => the direct super type's name
     *  </OL>
     *
     * <P><B>Note:</B> If the driver does not support type hierarchies, an 
     * empty result set is returned.
     *
     * @param catalog a catalog name; "" retrieves those without a catalog;
     *        <code>null</code> means drop catalog name from the selection 
     *        criteria
     * @param schemaPattern a schema name pattern; "" retrieves those 
     *        without a schema
     * @param tableNamePattern a UDT name pattern; may be a fully-qualified
     *        name
     * @return a <code>ResultSet</code> object in which a row gives information
     *         about the designated UDT
     * @throws SQLException if a database access error occurs
     */
    public ResultSet getSuperTypes(String catalog, String schemaPattern, String tableNamePattern) throws SQLException {
        XSQLVAR[] xsqlvars = new XSQLVAR[6];

        xsqlvars[0] = new XSQLVAR();
        xsqlvars[0].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[0].sqllen = 31;
        xsqlvars[0].sqlname = "TYPE_CAT";
        xsqlvars[0].relname = "SUPERTYPES";

        xsqlvars[1] = new XSQLVAR();
        xsqlvars[1].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[1].sqllen = 31;
        xsqlvars[1].sqlname = "TYPE_SCHEM";
        xsqlvars[1].relname = "SUPERTYPES";

        xsqlvars[2] = new XSQLVAR();
        xsqlvars[2].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[2].sqllen = 31;
        xsqlvars[2].sqlname = "TYPE_NAME";
        xsqlvars[2].relname = "SUPERTYPES";

        xsqlvars[3].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[3].sqllen = 31;
        xsqlvars[3].sqlname = "SUPERTYPE_CAT";
        xsqlvars[3].relname = "SUPERTYPES";

        xsqlvars[4] = new XSQLVAR();
        xsqlvars[4].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[4].sqllen = 31;
        xsqlvars[4].sqlname = "SUPERTYPE_SCHEM";
        xsqlvars[4].relname = "SUPERTYPES";

        xsqlvars[5] = new XSQLVAR();
        xsqlvars[5].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[5].sqllen = 31;
        xsqlvars[5].sqlname = "SUPERTYPE_NAME";
        xsqlvars[5].relname = "SUPERTYPES";

        ArrayList rows = new ArrayList(0);

        return new FBResultSet(xsqlvars, rows);
    }

    /**
     * Retrieves a description of the table hierarchies defined in a particular 
     * schema in this database.
     *
     * <P>Only supertable information for tables matching the catalog, schema
     * and table name are returned. The table name parameter may be a fully-
     * qualified name, in which case, the catalog and schemaPattern parameters
     * are ignored. If a table does not have a super table, it is not listed 
     * here. Supertables have to be defined in the same catalog and schema as 
     * the sub tables. Therefore, the type description does not need to include
     * this information for the supertable.
     *
     * <P>Each type description has the following columns:
     *  <OL>
     *  <LI><B>TABLE_CAT</B> String => the type's catalog (may be 
     *  <code>null</code>)
     *  <LI><B>TABLE_SCHEM</B> String => type's schema (may be 
     *  <code>null</code>)
     *  <LI><B>TABLE_NAME</B> String => type name
     *  <LI><B>SUPERTABLE_NAME</B> String => the direct super type's name
     *  </OL>
     *
     * <P><B>Note:</B> If the driver does not support type hierarchies, an 
     * empty result set is returned.
     *
     * @param catalog a catalog name; "" retrieves those without a catalog;
     *        <code>null</code> means drop catalog name from the selection 
     *        criteria
     * @param schemaPattern a schema name pattern; "" retrieves those 
     *        without a schema
     * @param tableNamePattern a table name pattern; may be a fully-qualified
     *        name
     * @return a <code>ResultSet</code> object in which each row is a type 
     *         description
     * @throws SQLException if a database access error occurs
     */
    public ResultSet getSuperTables(String catalog, String schemaPattern, String tableNamePattern) throws SQLException {
        
        XSQLVAR[] xsqlvars = new XSQLVAR[4];

        xsqlvars[0] = new XSQLVAR();
        xsqlvars[0].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[0].sqllen = 31;
        xsqlvars[0].sqlname = "TABLE_CAT";
        xsqlvars[0].relname = "SUPERTABLES";

        xsqlvars[1] = new XSQLVAR();
        xsqlvars[1].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[1].sqllen = 31;
        xsqlvars[1].sqlname = "TABLE_SCHEM";
        xsqlvars[1].relname = "SUPERTABLES";

        xsqlvars[2] = new XSQLVAR();
        xsqlvars[2].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[2].sqllen = 31;
        xsqlvars[2].sqlname = "TABLE_NAME";
        xsqlvars[2].relname = "SUPERTABLES";

        xsqlvars[3] = new XSQLVAR();
        xsqlvars[3].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[3].sqllen = 31;
        xsqlvars[3].sqlname = "SUPERTABLE_NAME";
        xsqlvars[3].relname = "SUPERTABLES";

        ArrayList rows = new ArrayList(0);

        return new FBResultSet(xsqlvars, rows);
    }

    /**
     * Retrieves whether this database supports the given results holdability.
     *
     * @param holdability one of the following constants: 
     * <code>ResultSet.HOLD_CURSORS_OVER_COMMIT</code> or 
     * <code>ResultSet.CLOSE_CURSORS_AT_COMMIT</code>
     * @return <code>true</code> if the holdability is supported, 
     *         <code>false</code> otherwise 
     * @exception SQLException if a database access error occurs 
     */
    public boolean supportsResultSetHoldability(int holdability) throws SQLException {
        return holdability == FirebirdResultSet.CLOSE_CURSORS_AT_COMMIT ||
            holdability == FirebirdResultSet.HOLD_CURSORS_OVER_COMMIT;
    }

    /**
     * Retrieves the default holdability of this <code>ResultSet</code>.
     *
     * @return the default holdability
     * @exception SQLException if a database access error occurs 
     */
    public int getResultSetHoldability() throws SQLException {
        return FirebirdResultSet.CLOSE_CURSORS_AT_COMMIT; 
    }

    /**
     * Get the major version number of the database.
     *
     * @return The major version number 
     * @exception SQLException if a database access error occurs 
     */
    public int getDatabaseMajorVersion() throws SQLException {
        return ((AbstractIscDbHandle)gdsHelper.getIscDBHandle()).getDatabaseProductMajorVersion();
    }

    /**
     * Get the minor version number of the database.
     * @return The minor version number 
     * @exception SQLException if a database access error occurs 
     */
    public int getDatabaseMinorVersion() throws SQLException {
        return ((AbstractIscDbHandle)gdsHelper.getIscDBHandle()).getDatabaseProductMinorVersion();
    }
    
    /**
     * Get the major version of the ODS (On-Disk Structure) of the database.
     * @return The major version number
     * @exception SQLException if a database access error occurs
     */
    public int getOdsMajorVersion() throws SQLException {
    	return ((AbstractIscDbHandle)gdsHelper.getIscDBHandle()).getODSMajorVersion();
    }
    
    /**
     * Get the minor version of the ODS (On-Disk Structure) of the database.
     * @return The minor version number
     * @exception SQLException if a database access error occurs
     */
    public int getOdsMinorVersion() throws SQLException {
    	return ((AbstractIscDbHandle)gdsHelper.getIscDBHandle()).getODSMinorVersion();
    }

    /**
     * Get the JDBC major version for this driver.
     *
     * @return the JDBC major version 
     * @exception SQLException should never be thrown in this implementation
     */
    public int getJDBCMajorVersion() throws SQLException {
        return 3;
    }

    /**
     * Get the JDBC minor version for this driver
     *
     * @return the JDBC minor version
     * @exception SQLException should never be thrown in this implementation
     */
    public int getJDBCMinorVersion() throws SQLException {
        return 0;
    }

    /**
     * Indicates whether the SQLSTATEs returned by SQLException.getSQLState is 
     * X/Open (now known as Open Group) SQL CLI or SQL99
     *
     * @return the type of SQLSTATEs
     * @exception SQLException should never be thrown in this implementation 
     */
    public int getSQLStateType() throws SQLException {
        // return sqlStateXOpen;
        return 1; // same value as sqlStateXOpen, but makes JDK 1.3 happy.
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
    
    public boolean isAllCondition(String pattern) {
        if ("%".equals(pattern)) {
            //asks for everything, no condition needed
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Determine if there are no SQL wildcard characters ('%' or '_') in the 
     * given pattern.
     *
     * @param pattern The pattern to be checked for wildcards
     * @return <code>true</code> if there are no wildcards in the pattern, 
     *         <code>false</code> otherwise
     */
    public boolean hasNoWildcards(String pattern) {
        if (pattern == null)
            return true;

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

    /**
     * Strips all backslash-escapes from a string.
     *
     * @param pattern The string to be stripped
     * @return pattern with all backslash-escapes removed
     */
    public String stripEscape(String pattern) {
        StringBuffer stripped = new StringBuffer(pattern.length());
        for (int pos = 0; pos < pattern.length(); pos++) {
            if (pattern.charAt(pos) != '\\') {
                stripped.append(pattern.charAt(pos));
            }
        }
        return stripped.toString();
    }

    protected String getWantsSystemTables(String[] types) {
        for (int i = 0; i < types.length; i++) {
            if (SYSTEM_TABLE.equals(types[i])) {
                return "T";
            }
        }
        return "F";
    }

    protected String getWantsTables(String[] types) {
        for (int i = 0; i < types.length; i++) {
            if (TABLE.equals(types[i])) {
                return "T";
            }
        }
        return "F";
    }

    protected String getWantsViews(String[] types) {
        for (int i = 0; i < types.length; i++) {
            if (VIEW.equals(types[i])) {
                return "T";
            }
        }
        return "F";
    }

    /**
     * Strips a leading and trailing quote (double or single) from a string.
     *
     * @param pattern the string to be stripped
     * @return a copy of <code>pattern</code> with leading and trailing quote 
     * removed
     */
    public String stripQuotes(String pattern, boolean uppercase) {
        if ((pattern.length() >= 2)
            && (pattern.charAt(0) == '\"')
            && (pattern.charAt(pattern.length() - 1) == '\"'))
        {
            return pattern.substring(1, pattern.length() - 1);
        }
        else {
            if (uppercase)
                return pattern.toUpperCase();
            else
                return pattern;
        }
    }

     /*
         * (non-Javadoc)
         * 
         * @see org.firebirdsql.jdbc.FirebirdDatabaseMetaData#getProcedureSourceCode(java.lang.String)
         */
    public String getProcedureSourceCode(String procedureName)
            throws SQLException {
        String sResult = null;
        String sql = "Select RDB$PROCEDURE_SOURCE From RDB$PROCEDURES Where "
                + "RDB$PROCEDURE_NAME = ?";
        ArrayList params = new ArrayList();
        params.add(procedureName);
        ResultSet rs = doQuery(sql, params);
        if (rs.next()) sResult = rs.getString(1);
        rs.close();

        return sResult;
    } // public String getProcedureSourceCode(String procedureName) throws
        // SQLException

    /*
     * (non-Javadoc)
     * 
     * @see org.firebirdsql.jdbc.FirebirdDatabaseMetaData#getTriggerSourceCode(java.lang.String)
     */
    public String getTriggerSourceCode(String triggerName) throws SQLException {
        String sResult = null;
        String sql = "Select RDB$TRIGGER_SOURCE From RDB$TRIGGERS Where "
                + "RDB$TRIGGER_NAME = ?";
        ArrayList params = new ArrayList();
        params.add(triggerName);
        ResultSet rs = doQuery(sql, params);
        if (rs.next()) sResult = rs.getString(1);
        rs.close();

        return sResult;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.firebirdsql.jdbc.FirebirdDatabaseMetaData#getViewSourceCode(java.lang.String)
     */
    public String getViewSourceCode(String viewName) throws SQLException {
        String sResult = null;
        String sql = "Select RDB$VIEW_SOURCE From RDB$RELATIONS Where "
                + "RDB$RELATION_NAME = ?";
        ArrayList params = new ArrayList();
        params.add(viewName);
        ResultSet rs = doQuery(sql, params);
        if (rs.next()) sResult = rs.getString(1);
        rs.close();

        return sResult;
    }

    
    private void checkCatalogAndSchema(String catalog, String schema) throws SQLException {
        /*
        // we ignore incorrect catalog and schema specification as
        // suggested by Thomas Kellerer in JDBC Forum 
        
        if (catalog != null && !catalog.equals("") && !catalog.equals("%")) {
            throw new SQLException("Catalogs not supported");
        }
        if (schema != null && (!schema.equals("")) && (!schema.equals("%"))) {
            throw new SQLException("Schemas not supported");
        }
        */
    }

    private class Clause {
        private String condition = "";
        private String value;
        private String originalCaseValue;

        public Clause (String columnName, String pattern) {
            if (pattern == null) {
                return;
            }
            else if (isAllCondition(pattern)) {
                //do nothing to tableCondition
                return;
            }
            else if (hasNoWildcards(pattern)) {
                value = stripQuotes(stripEscape(pattern), true);
                originalCaseValue = stripQuotes(stripEscape(pattern), false);
                condition = columnName + " = ? and ";
            }
            else {
                value = stripQuotes(pattern, true) + SPACES + "%";
                originalCaseValue = stripQuotes(pattern, false) + SPACES + "%";
                condition = columnName + " || '" + SPACES + "' like ? escape '\\' and ";
            }
        }

        public String getCondition() {
            return condition;
        }

        public String getValue() {
            return value;
        }
        
        public String getOriginalCaseValue() {
            return originalCaseValue;
        }
    }


    protected byte[] getBytes(String value){
        if (value !=null)
            return value.getBytes();
        else
            return null;
    }
    
    private AbstractPreparedStatement getStatement(String sql) throws SQLException {
        
        AbstractPreparedStatement s = 
            (AbstractPreparedStatement)statements.get(sql);
        
        if (s != null && s.isClosed()) {
            statements.remove(sql);
            s = null;
        }
        
        if (s != null) 
            return s;
        
        if (connection == null) {
            InternalTransactionCoordinator.MetaDataTransactionCoordinator metaDataTransactionCoordinator = 
                new InternalTransactionCoordinator.MetaDataTransactionCoordinator();
            
            s = FBStatementFactory.createPreparedStatement(gdsHelper, sql,
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY,
                    FirebirdResultSet.CLOSE_CURSORS_AT_COMMIT, 
                    metaDataTransactionCoordinator, metaDataTransactionCoordinator,
                    true, true);
        } else {
            s = (AbstractPreparedStatement)connection.prepareMetaDataStatement(
                sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        }
            
        statements.put(sql, s);
        
        return s;
    }

    /**
     * Execute an sql query with a given set of parameters.
     * 
     * @param sql
     *            The sql statement to be used for the query
     * @param params
     *            The parameters to be used in the query
     * @throws SQLException
     *             if a database access error occurs
     */
    public ResultSet doQuery(String sql, List params)
            throws SQLException {
        
        AbstractPreparedStatement s = getStatement(sql);
        
        for (int i = 0; i < params.size(); i++)
            s.setStringForced(i + 1, (String) params.get(i));

        return s.executeMetaDataQuery();
    }
}
