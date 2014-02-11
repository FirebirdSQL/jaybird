/*
 * $Id$
 * 
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

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.*;
import java.util.*;

import org.firebirdsql.gds.*;
import org.firebirdsql.gds.impl.AbstractGDS;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.jdbc.escape.FBEscapedFunctionHelper;
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
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class FBDatabaseMetaData implements FirebirdDatabaseMetaData {

    private final static Logger log = LoggerFactory.getLogger(FBDatabaseMetaData.class,false);
    private static final String SPACES_31 = "                               "; // 31 spaces
    private static final String SPACES_15 = "               "; // 15 spaces

    private static final int DRIVER_MAJOR_VERSION = 3;
    private static final int DRIVER_MINOR_VERSION = 0;
    private static final String DRIVER_VERSION = DRIVER_MAJOR_VERSION + "." + DRIVER_MINOR_VERSION;

    private static final int SUBTYPE_NUMERIC = 1;
    private static final int SUBTYPE_DECIMAL = 2;

    private static final byte[] TRUE_BYTES = getBytes("T");
    private static final byte[] FALSE_BYTES = getBytes("F");
    private static final byte[] YES_BYTES = getBytes("YES");
    private static final byte[] NO_BYTES = getBytes("NO");
    private static final byte[] EMPTY_STRING_BYTES = getBytes("");
    private static final byte[] CASESENSITIVE = TRUE_BYTES;
    private static final byte[] CASEINSENSITIVE = FALSE_BYTES;
    private static final byte[] UNSIGNED = TRUE_BYTES;
    private static final byte[] SIGNED = FALSE_BYTES;
    private static final byte[] FIXEDSCALE = TRUE_BYTES;
    private static final byte[] VARIABLESCALE = FALSE_BYTES;
    private static final byte[] NOTAUTOINC = FALSE_BYTES;
    // TODO in implementation short and int are encoded identical, remove distinction?
    private static final byte[] INT_ZERO = createInt(0);
    private static final byte[] SHORT_ZERO = createShort(0);
    private static final byte[] SHORT_ONE = createShort(1);
    private static final byte[] RADIX_BINARY = createInt(2);
    private static final byte[] RADIX_TEN = createInt(10);
    // TODO in implementation short and int are encoded identical, remove distinction?
    private static final byte[] RADIX_TEN_SHORT = createShort(10);
    private static final byte[] RADIX_BINARY_SHORT = createShort(2);
    private static final byte[] TYPE_PRED_NONE = createShort(DatabaseMetaData.typePredNone);
    private static final byte[] TYPE_PRED_BASIC = createShort(DatabaseMetaData.typePredBasic);
    private static final byte[] TYPE_SEARCHABLE = createShort(DatabaseMetaData.typeSearchable);
    private static final byte[] TYPE_NULLABLE = createShort(DatabaseMetaData.typeNullable);
    private static final byte[] PROCEDURE_NO_RESULT = createShort(DatabaseMetaData.procedureNoResult);
    private static final byte[] PROCEDURE_RETURNS_RESULT = createShort(DatabaseMetaData.procedureReturnsResult);
    private static final byte[] PROCEDURE_NO_NULLS = createShort(DatabaseMetaData.procedureNoNulls);
    private static final byte[] PROCEDURE_NULLABLE = createShort(DatabaseMetaData.procedureNullable);
    private static final byte[] PROCEDURE_COLUMN_IN = createShort(DatabaseMetaData.procedureColumnIn);
    private static final byte[] PROCEDURE_COLUMN_OUT = createShort(DatabaseMetaData.procedureColumnOut);
    private static final byte[] FLOAT_PRECISION = createInt(7);
    private static final byte[] DOUBLE_PRECISION = createInt(15);
    private static final byte[] BIGINT_PRECISION = createInt(19);
    private static final byte[] INTEGER_PRECISION = createInt(10);
    private static final byte[] SMALLINT_PRECISION = createInt(5);
    private static final byte[] DATE_PRECISION = createInt(10);
    private static final byte[] TIME_PRECISION = createInt(8);
    private static final byte[] TIMESTAMP_PRECISION = createInt(19);
    private static final byte[] NUMERIC_PRECISION = createInt(18);
    private static final byte[] DECIMAL_PRECISION = createInt(18);
    private static final byte[] BOOLEAN_PRECISION = createInt(1);
    private static final byte[] COLUMN_NO_NULLS = createInt(DatabaseMetaData.columnNoNulls);
    private static final byte[] COLUMN_NULLABLE = createInt(DatabaseMetaData.columnNullable);
    private static final byte[] IMPORTED_KEY_NO_ACTION = createShort(DatabaseMetaData.importedKeyNoAction);
    private static final byte[] IMPORTED_KEY_CASCADE = createShort(DatabaseMetaData.importedKeyCascade);
    private static final byte[] IMPORTED_KEY_SET_NULL = createShort(DatabaseMetaData.importedKeySetNull);
    private static final byte[] IMPORTED_KEY_SET_DEFAULT = createShort(DatabaseMetaData.importedKeySetDefault);
    private static final byte[] IMPORTED_KEY_NOT_DEFERRABLE = createShort(DatabaseMetaData.importedKeyNotDeferrable);
    private static final byte[] TABLE_INDEX_OTHER = createShort(DatabaseMetaData.tableIndexOther);
    private static final byte[] ASC_BYTES = getBytes("A");
    private static final byte[] DESC_BYTES = getBytes("D");

    private GDSHelper gdsHelper;
    private FBConnection connection;

    protected final Map<String, FBPreparedStatement> statements = new HashMap<String, FBPreparedStatement>();

    protected FBDatabaseMetaData(GDSHelper gdsHelper) {
        this.gdsHelper = gdsHelper;
    }
    
    protected FBDatabaseMetaData(FBConnection c) throws GDSException {
        this.gdsHelper = c.getGDSHelper();
        this.connection = c;
    }

    protected void close() {
        try {
            for (FBStatement stmt : statements.values()) {
                if (!stmt.isClosed())
                    stmt.close();
            }
            statements.clear();
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
        return false;
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
        return gdsHelper.compareToVersion(2, 0) >= 0;
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
        return false;
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
        return gdsHelper.compareToVersion(2, 0) < 0;
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
        return DRIVER_VERSION;
    }

    /**
     * What's this JDBC driver's major version number?
     *
     * @return JDBC driver major version
     */
    public  int getDriverMajorVersion() {
        return DRIVER_MAJOR_VERSION;
    }

    /**
     * What's this JDBC driver's minor version number?
     *
     * @return JDBC driver minor version number
     */
    public  int getDriverMinorVersion() {
        return DRIVER_MINOR_VERSION;
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
     * TODO implement statement pooling on the server.. then in the driver
     */
    public boolean supportsStatementPooling() throws SQLException {
        return false;
    }

    public boolean locatorsUpdateCopy() throws SQLException {
        // Firebird creates a new blob when making changes
        return true;
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
     * TODO Review list of keywords
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
     * NOTE: Some of the functions listed may only work on Firebird 2.1 or higher, or when equivalent UDFs
     * are installed.
     * <p>
     * {@inheritDoc}
     * </p>
     */
    public String getNumericFunctions() throws SQLException {
        return collectionToCommaSeperatedList(FBEscapedFunctionHelper.getSupportedNumericFunctions());
    }
    
    private static String collectionToCommaSeperatedList(Collection<String> collection) {
        StringBuilder sb = new StringBuilder();
        for (String item : collection) {
            sb.append(item);
            sb.append(',');
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    /**
     * NOTE: Some of the functions listed may only work on Firebird 2.1 or higher, or when equivalent UDFs
     * are installed.
     * <p>
     * {@inheritDoc}
     * </p>
     */
    public String getStringFunctions() throws SQLException {
        return collectionToCommaSeperatedList(FBEscapedFunctionHelper.getSupportedStringFunctions());
    }

    /**
     * NOTE: Some of the functions listed may only work on Firebird 2.1 or higher, or when equivalent UDFs
     * are installed.
     * <p>
     * {@inheritDoc}
     * </p>
     */
    public String getSystemFunctions() throws SQLException {
        return collectionToCommaSeperatedList(FBEscapedFunctionHelper.getSupportedSystemFunctions());
    }

    /**
     * NOTE: Some of the functions listed may only work on Firebird 2.1 or higher, or when equivalent UDFs
     * are installed.
     * <p>
     * {@inheritDoc}
     * </p>
     */
    public String getTimeDateFunctions() throws SQLException {
        return collectionToCommaSeperatedList(FBEscapedFunctionHelper.getSupportedTimeDateFunctions());
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
        return true;
    }

    /**
     * Is the CONVERT function between SQL types supported?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean supportsConvert() throws SQLException {
        // TODO: Set true after JDBC-294 has been done
        return false;   // Support is broken right now
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
        // TODO: implement actual mapping with JDBC-294
        return false;   // Support is broken right now
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
        return true;
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

    public  boolean supportsOuterJoins() throws SQLException {
        return true;
    }

    public  boolean supportsFullOuterJoins() throws SQLException {
        return true;
    }

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
        return true;
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
        return 31;
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
        return 32767; // Depends on datatypes and sizes, at most 64 kbyte excluding blobs (but including blob ids)
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
        if (gdsHelper.compareToVersion(2, 0) < 0) {
            return 252; // See http://www.firebirdsql.org/en/firebird-technical-specifications/
        } else {
            return 0; // 1/4 of page size, maybe retrieve page size and use that?
        }
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
        if (gdsHelper.compareToVersion(1, 5) >= 0)
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
        return false; // Blob sizes are not included in rowsize 
    }

    /**
     * What's the maximum length of an SQL statement?
     *
     * @return max length in bytes;
     *      a result of zero means that there is no limit or the limit is not known
     * @exception SQLException if a database access error occurs
     */
    public  int getMaxStatementLength() throws SQLException {
        // TODO 2GB for Firebird 3 (test if we don't need to change anything else to support this)
        return 65536;
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
        + "cast(RDB$PROCEDURE_NAME as varchar(31)) as PROCEDURE_NAME,"
        + "RDB$DESCRIPTION as REMARKS,"
        + "RDB$PROCEDURE_OUTPUTS as PROCEDURE_TYPE "
        + "from "
        + "RDB$PROCEDURES "
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
     *  <LI><B>SPECIFIC_NAME</B> String => The name which uniquely identifies this procedure within its schema.
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

        // TODO null or "" are not according to spec
        if (procedureNamePattern == null || procedureNamePattern.equals("")) {
            procedureNamePattern = "%";
        }

        XSQLVAR[] xsqlvars = new XSQLVAR[9];

        xsqlvars[0] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "PROCEDURE_CAT", "PROCEDURES");
        xsqlvars[1] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "PROCEDURE_SCHEM", "ROCEDURES");
        xsqlvars[2] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "PROCEDURE_NAME", "PROCEDURES");
        xsqlvars[3] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "FUTURE1", "PROCEDURES");
        xsqlvars[4] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "FUTURE2", "PROCEDURES");
        xsqlvars[5] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "FUTURE3", "PROCEDURES");
        xsqlvars[6] = new XSQLVAR(ISCConstants.SQL_VARYING, 80 /*gets updated if there are longer remarks*/, "REMARKS", "PROCEDURES");
        xsqlvars[7] = new XSQLVAR(ISCConstants.SQL_SHORT, 0, "PROCEDURE_TYPE", "PROCEDURES");
        xsqlvars[8] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "SPECIFIC_NAME", "PROCEDURES");

        Clause procedureClause = new Clause("RDB$PROCEDURE_NAME", procedureNamePattern);
        
        String sql = GET_PROCEDURES_START;
        sql += procedureClause.getCondition();
        sql += GET_PROCEDURES_END;
        
        // check the original case identifiers first
        List<String> params = new ArrayList<String>();
        if (!procedureClause.getCondition().equals("")) {
            params.add(procedureClause.getOriginalCaseValue());
        }

        ResultSet rs = doQuery(sql, params);

        
        // if nothing found, check the uppercased identifiers
        if (!rs.next()) {
            rs.close();
            params.clear();
            if (!procedureClause.getCondition().equals("")) {
                params.add(procedureClause.getValue());
            }
            
            rs = doQuery(sql, params);
            
            // if nothing found, return an empty result set
            if (!rs.next()) {
                rs.close();
                return new FBResultSet(xsqlvars, Collections.<byte[][]>emptyList());
            }
        }

        ArrayList<byte[][]> rows = new ArrayList<byte[][]>();
        do {
            byte[][] row = new byte[9][];
            row[0] = null;
            row[1] = null;
            row[2] = getBytes(rs.getString("PROCEDURE_NAME"));
            row[3] = null;
            row[4] = null;
            row[5] = null;
            String remarks = rs.getString("REMARKS");
            row[6] = getBytes(remarks);
            if (remarks != null && remarks.length() > xsqlvars[6].sqllen)
                xsqlvars[6].sqllen = remarks.length();
            short procedureType = rs.getShort("PROCEDURE_TYPE");
            row[7] = procedureType == 0 ? PROCEDURE_NO_RESULT : PROCEDURE_RETURNS_RESULT;
            row[8] = row[2];
            rows.add(row);
        } while (rs.next());
        rs.close();
        return new FBResultSet(xsqlvars, rows);
    }

    private static final String GET_PROCEDURE_COLUMNS_START = "select "
        + "cast(PP.RDB$PROCEDURE_NAME as varchar(31)) as PROCEDURE_NAME,"
        + "cast(PP.RDB$PARAMETER_NAME as varchar(31)) as COLUMN_NAME,"
        + "PP.RDB$PARAMETER_TYPE as COLUMN_TYPE,"
        + "F.RDB$FIELD_TYPE as FIELD_TYPE,"
        + "F.RDB$FIELD_SUB_TYPE as FIELD_SUB_TYPE,"
        + "F.RDB$FIELD_PRECISION as FIELD_PRECISION,"
        + "F.RDB$FIELD_SCALE as FIELD_SCALE,"
        + "F.RDB$FIELD_LENGTH as FIELD_LENGTH,"
        + "F.RDB$NULL_FLAG as NULL_FLAG,"
        + "PP.RDB$DESCRIPTION as REMARKS,"
        + "F.RDB$CHARACTER_LENGTH AS CHAR_LEN,"
        + "PP.RDB$PARAMETER_NUMBER + 1 AS PARAMETER_NUMBER "
        + "from "
        + "RDB$PROCEDURE_PARAMETERS PP,"
        + "RDB$FIELDS F "
        + "where ";
    private static final String GET_PROCEDURE_COLUMNS_END = " PP.RDB$FIELD_SOURCE = F.RDB$FIELD_NAME "
        + "order by "
        + "PP.RDB$PROCEDURE_NAME,"
        + "PP.RDB$PARAMETER_TYPE desc,"
        + "PP.RDB$PARAMETER_NUMBER ";

    /**
     * Retrieves a description of the given catalog's stored procedure parameter
     * and result columns.
     *
     * <P>Only descriptions matching the schema, procedure and
     * parameter name criteria are returned.  They are ordered by
     * PROCEDURE_CAT, PROCEDURE_SCHEM, PROCEDURE_NAME and SPECIFIC_NAME. Within this, the return value,
     * if any, is first. Next are the parameter descriptions in call
     * order. The column descriptions follow in column number order.
     *
     * <P>Each row in the <code>ResultSet</code> is a parameter description or
     * column description with the following fields:
     *  <OL>
     *  <LI><B>PROCEDURE_CAT</B> String => procedure catalog (may be <code>null</code>)
     *  <LI><B>PROCEDURE_SCHEM</B> String => procedure schema (may be <code>null</code>)
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
     *      <LI> procedureNoNulls - does not allow NULL values
     *      <LI> procedureNullable - allows NULL values
     *      <LI> procedureNullableUnknown - nullability unknown
     *      </UL>
     *  <LI><B>REMARKS</B> String => comment describing parameter/column
     *  <LI><B>COLUMN_DEF</B> String => default value for the column, which should be interpreted as a string when the value is enclosed in single quotes (may be <code>null</code>)
     *      <UL>
     *      <LI> The string NULL (not enclosed in quotes) - if NULL was specified as the default value
     *      <LI> TRUNCATE (not enclosed in quotes)        - if the specified default value cannot be represented without truncation
     *      <LI> NULL                                     - if a default value was not specified
     *      </UL>
     *  <LI><B>SQL_DATA_TYPE</B> int  => reserved for future use
     *  <LI><B>SQL_DATETIME_SUB</B> int  => reserved for future use
     *  <LI><B>CHAR_OCTET_LENGTH</B> int  => the maximum length of binary and character based columns.  For any other datatype the returned value is a 
     * NULL
     *  <LI><B>ORDINAL_POSITION</B> int  => the ordinal position, starting from 1, for the input and output parameters for a procedure. A value of 0
     *is returned if this row describes the procedure's return value.  For result set columns, it is the
     *ordinal position of the column in the result set starting from 1.  If there are
     *multiple result sets, the column ordinal positions are implementation
     * defined.
     *  <LI><B>IS_NULLABLE</B> String  => ISO rules are used to determine the nullability for a column.
     *       <UL>
     *       <LI> YES           --- if the parameter can include NULLs
     *       <LI> NO            --- if the parameter cannot include NULLs
     *       <LI> empty string  --- if the nullability for the 
     * parameter is unknown
     *       </UL>
     *  <LI><B>SPECIFIC_NAME</B> String  => the name which uniquely identifies this procedure within its schema.
     *  </OL>
     *
     * <P><B>Note:</B> Some databases may not return the column
     * descriptions for a procedure. 
     * 
     * <p>The PRECISION column represents the specified column size for the given column. 
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
     * @param procedureNamePattern a procedure name pattern; must match the
     *        procedure name as it is stored in the database 
     * @param columnNamePattern a column name pattern; must match the column name
     *        as it is stored in the database 
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

        XSQLVAR[] xsqlvars = new XSQLVAR[20];

        xsqlvars[0] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "PROCEDURE_CAT", "COLUMNINFO");
        xsqlvars[1] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "PROCEDURE_SCHEM", "COLUMNINFO");
        xsqlvars[2] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "PROCEDURE_NAME", "COLUMNINFO");
        xsqlvars[3] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "COLUMN_NAME", "COLUMNINFO");
        xsqlvars[4] = new XSQLVAR(ISCConstants.SQL_SHORT, 0, "COLUMN_TYPE", "COLUMNINFO");
        xsqlvars[5] = new XSQLVAR(ISCConstants.SQL_LONG, 0, "DATA_TYPE", "COLUMNINFO");
        xsqlvars[6] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "TYPE_NAME", "COLUMNINFO");
        xsqlvars[7] = new XSQLVAR(ISCConstants.SQL_LONG, 0, "PRECISION", "COLUMNINFO");
        xsqlvars[8] = new XSQLVAR(ISCConstants.SQL_LONG, 0, "LENGTH", "COLUMNINFO");
        xsqlvars[9] = new XSQLVAR(ISCConstants.SQL_SHORT, 0, "SCALE", "COLUMNINFO");
        xsqlvars[10] = new XSQLVAR(ISCConstants.SQL_SHORT, 0, "RADIX", "COLUMNINFO");
        xsqlvars[11] = new XSQLVAR(ISCConstants.SQL_SHORT, 0, "NULLABLE", "COLUMNINFO");
        xsqlvars[12] = new XSQLVAR(ISCConstants.SQL_VARYING, 80 /*gets updated if we get a longer description*/, "REMARKS", "COLUMNINFO");
        xsqlvars[13] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "COLUMN_DEF", "COLUMNINFO");
        xsqlvars[14] = new XSQLVAR(ISCConstants.SQL_LONG, 0, "SQL_DATA_TYPE", "COLUMNINFO");
        xsqlvars[15] = new XSQLVAR(ISCConstants.SQL_LONG, 0, "SQL_DATETIME_SUB", "COLUMNINFO");
        xsqlvars[16] = new XSQLVAR(ISCConstants.SQL_LONG, 0, "CHAR_OCTET_LENGTH", "COLUMNINFO");
        xsqlvars[17] = new XSQLVAR(ISCConstants.SQL_LONG, 0, "ORDINAL_POSITION", "COLUMNINFO");
        xsqlvars[18] = new XSQLVAR(ISCConstants.SQL_VARYING, 3, "IS_NULLABLE", "COLUMNINFO");
        xsqlvars[19] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "SPECIFIC_NAME", "COLUMNINFO");

        Clause procedureClause = new Clause("PP.RDB$PROCEDURE_NAME", procedureNamePattern);
        Clause columnClause = new Clause("PP.RDB$PARAMETER_NAME", columnNamePattern);
        
        String sql = GET_PROCEDURE_COLUMNS_START;
        sql += procedureClause.getCondition();
        sql += columnClause.getCondition();
        sql += GET_PROCEDURE_COLUMNS_END;
        
        // check the original case identifiers first
        List<String> params = new ArrayList<String>();
        if (!procedureClause.getCondition().equals("")) {
            params.add(procedureClause.getOriginalCaseValue());
        }
        if (!columnClause.getCondition().equals("")) {
            params.add(columnClause.getOriginalCaseValue());
        }

        ResultSet rs = doQuery(sql, params);
        List<byte[][]> rows = new ArrayList<byte[][]>();
        
        // if nothing found, check the uppercased identifiers
        if (!rs.next()) {
            rs.close();
            params.clear();
            if (!procedureClause.getCondition().equals("")) {
                params.add(procedureClause.getValue());
            }
            if (!columnClause.getCondition().equals("")) {
                params.add(columnClause.getValue());
            }
            
            rs = doQuery(sql, params);
            
            // if nothing found, return an empty result set
            if (!rs.next()) {
                rs.close();
                return new FBResultSet(xsqlvars, rows);
            }
        }

        do {
            byte[][] row = new byte[20][];
            row[0] = null;
            row[1] = null;
            row[2] = getBytes(rs.getString("PROCEDURE_NAME"));
            row[3] = getBytes(rs.getString("COLUMN_NAME"));

            short columnType = rs.getShort("COLUMN_TYPE");
            // TODO: Unsure if procedureColumnOut is correct, maybe procedureColumnResult, or need ODS dependent use of RDB$PROCEDURE_TYPE to decide on selectable or executable?
            // TODO: ResultSet columns should not be first according to JDBC 4.1 description
            row[4] = columnType == 0 ? PROCEDURE_COLUMN_IN : PROCEDURE_COLUMN_OUT;

            short fieldType = rs.getShort("FIELD_TYPE");
            short fieldSubType = rs.getShort("FIELD_SUB_TYPE");
            short fieldScale = rs.getShort("FIELD_SCALE");
            int dataType = getDataType(fieldType, fieldSubType, fieldScale);

            row[5] = createInt(dataType);
            row[6] = getBytes(getDataTypeName(fieldType, fieldSubType, fieldScale));
            
            row[8] = createInt(rs.getShort("FIELD_LENGTH"));
            
            // Defaults: some are overridden in the switch
            row[7] = null;
            row[9] = null;
            row[10] = RADIX_TEN_SHORT;
            row[16] = null;
            switch (dataType){
                case Types.DECIMAL:
                case Types.NUMERIC:
                   row[7] = createInt(rs.getShort("FIELD_PRECISION"));
                   row[9] = createShort(-1 * fieldScale);
                   break;
                case Types.CHAR:
                case Types.VARCHAR:
                   short charLen = rs.getShort("CHAR_LEN");
                   if (!rs.wasNull()) {
                       row[7] = createInt(charLen);
                   } else {
                       row[7] = row[8];
                   }
                   row[16] = row[8];
                   break;
                case Types.FLOAT:
                   row[7] = FLOAT_PRECISION;
                   break;
                case Types.DOUBLE:
                   row[7] = DOUBLE_PRECISION;
                   break;
                case Types.BIGINT:
                    row[7] = BIGINT_PRECISION;
                    row[9] = SHORT_ZERO;
                    break;
                case Types.INTEGER:
                   row[7] = INTEGER_PRECISION;
                   row[9] = SHORT_ZERO;
                   break;
                case Types.SMALLINT:
                   row[7] = SMALLINT_PRECISION;
                   row[9] = SHORT_ZERO;
                   break;
                case Types.DATE:
                   row[7] = DATE_PRECISION;
                   break;
                case Types.TIME:
                   row[7] = TIME_PRECISION;
                   break;
                case Types.TIMESTAMP:
                   row[7] = TIMESTAMP_PRECISION;
                   break;
                case Types.BOOLEAN:
                    row[7] = BOOLEAN_PRECISION;
                    row[10] = RADIX_BINARY_SHORT;
                default:
                   row[7] = null;
            }
            
            // TODO: Find out what the difference is with NULL_FLAG in RDB$PROCEDURE_PARAMETERS (might be ODS dependent) 
            short nullFlag = rs.getShort("NULL_FLAG");
            row[11] = nullFlag == 1 ? PROCEDURE_NO_NULLS : PROCEDURE_NULLABLE;

            String remarks = rs.getString("REMARKS");
            row[12] = getBytes(remarks);
            if (remarks != null && remarks.length() > xsqlvars[12].sqllen)
                xsqlvars[12].sqllen = remarks.length();
            // TODO: Need to write ODS version dependent method to retrieve some of the info
            row[13] = null; // TODO From 2.0 defaults for procedure parameters
            row[14] = null;
            row[15] = null;
            // TODO: Find correct value for ORDINAL_POSITION (+ order of columns and intent, see JDBC-229)
            row[17] = createInt(rs.getInt("PARAMETER_NUMBER"));
            // TODO: Find out if there is a conceptual difference with NULLABLE (idx 11)
            row[18] = (nullFlag == 1) ? NO_BYTES : YES_BYTES;
            row[19] = row[2];

            rows.add(row);
        } while (rs.next());
        rs.close();
        return new FBResultSet(xsqlvars, rows);
    }

    // TODO: Include GLOBAL TEMPORARY
    public static final String TABLE = "TABLE";
    public static final String SYSTEM_TABLE = "SYSTEM TABLE";
    public static final String VIEW = "VIEW";
    public static final String[] ALL_TYPES = {TABLE, SYSTEM_TABLE, VIEW};

    private static final String TABLE_COLUMNS_FORMAT =
            " select cast(null as varchar(31)) as TABLE_CAT,"
            + "cast(null as varchar(31)) as TABLE_SCHEM,"
            + "cast(RDB$RELATION_NAME as varchar(31)) as TABLE_NAME,"
            + "cast('%s' as varchar(31)) as TABLE_TYPE,"
            + "RDB$DESCRIPTION as REMARKS,"
            + "cast(null as varchar(31)) as TYPE_CAT,"
            + "cast(null as varchar(31)) as TYPE_SCHEM,"
            + "cast(null as varchar(31)) as TYPE_NAME,"
            + "cast(null as varchar(31)) as SELF_REFERENCING_COL_NAME,"
            + "cast(null as varchar(31)) as REF_GENERATION,"
            + "cast(RDB$OWNER_NAME as varchar(31)) as OWNER_NAME "
            + "from RDB$RELATIONS";
    
    private static final String TABLE_COLUMNS_SYSTEM =
            String.format(TABLE_COLUMNS_FORMAT, SYSTEM_TABLE);
    
    private static final String TABLE_COLUMNS_NORMAL =
            String.format(TABLE_COLUMNS_FORMAT, TABLE);
    
    private static final String TABLE_COLUMNS_VIEW =
            String.format(TABLE_COLUMNS_FORMAT, VIEW);

    private static final String GET_TABLES_ALL = 
          TABLE_COLUMNS_SYSTEM
        + " where ? = 'T' and RDB$SYSTEM_FLAG = 1 and RDB$VIEW_SOURCE is null"
        + " union"
        + TABLE_COLUMNS_NORMAL
        + " where ? = 'T' and RDB$SYSTEM_FLAG = 0 and RDB$VIEW_SOURCE is null"
        + " union"
        + TABLE_COLUMNS_VIEW
        + " where ? = 'T' and RDB$VIEW_SOURCE is not null "
        + " order by 3 ";

    private static final String GET_TABLES_EXACT = 
          TABLE_COLUMNS_SYSTEM
        + " where ? = 'T' and RDB$SYSTEM_FLAG = 1 and RDB$VIEW_SOURCE is null"
        + " and ? = RDB$RELATION_NAME"
        + " union"
        + TABLE_COLUMNS_NORMAL
        + " where ? = 'T' and RDB$SYSTEM_FLAG = 0 and RDB$VIEW_SOURCE is null"
        + " and ? = RDB$RELATION_NAME"
        + " union"
        + TABLE_COLUMNS_VIEW
        + " where ? = 'T' and RDB$VIEW_SOURCE is not null"
        + " and ? = RDB$RELATION_NAME";

    private static final String GET_TABLES_LIKE = 
          TABLE_COLUMNS_SYSTEM
        + " where ? = 'T' and RDB$SYSTEM_FLAG = 1 and RDB$VIEW_SOURCE is null"
        + " and RDB$RELATION_NAME || '" + SPACES_31 + "' like ? escape '\\'"
        + " union"
        + TABLE_COLUMNS_NORMAL
        + " where ? = 'T' and RDB$SYSTEM_FLAG = 0 and RDB$VIEW_SOURCE is null"
        + " and RDB$RELATION_NAME || '" + SPACES_31 + "' like ? escape '\\'"
        + " union"
        + TABLE_COLUMNS_VIEW
        + " where ? = 'T' and RDB$VIEW_SOURCE is not null"
        + " and RDB$RELATION_NAME || '" + SPACES_31 + "' like ? escape '\\' "
        + " order by 3 ";

    /**
     * Retrieves a description of the tables available in the given catalog.
     * Only table descriptions matching the catalog, schema, table
     * name and type criteria are returned.  They are ordered by
     * <code>TABLE_TYPE</code>, <code>TABLE_CAT</code>, 
     * <code>TABLE_SCHEM</code> and <code>TABLE_NAME</code>.
     * <P>
     * Each table description has the following columns:
     *  <OL>
     *  <LI><B>TABLE_CAT</B> String => table catalog (may be <code>null</code>)
     *  <LI><B>TABLE_SCHEM</B> String => table schema (may be <code>null</code>)
     *  <LI><B>TABLE_NAME</B> String => table name
     *  <LI><B>TABLE_TYPE</B> String => table type.  Typical types are "TABLE",
     *          "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", 
     *          "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
     *  <LI><B>REMARKS</B> String => explanatory comment on the table
     *  <LI><B>TYPE_CAT</B> String => the types catalog (may be <code>null</code>)
     *  <LI><B>TYPE_SCHEM</B> String => the types schema (may be <code>null</code>)
     *  <LI><B>TYPE_NAME</B> String => type name (may be <code>null</code>)
     *  <LI><B>SELF_REFERENCING_COL_NAME</B> String => name of the designated 
     *                  "identifier" column of a typed table (may be <code>null</code>)
     *  <LI><B>REF_GENERATION</B> String => specifies how values in 
     *                  SELF_REFERENCING_COL_NAME are created. Values are
     *                  "SYSTEM", "USER", "DERIVED". (may be <code>null</code>)
     *  <LI><B>OWNER_NAME</B> String => Username of the owner of the table (Jaybird-specific) 
     *  </OL>
     *
     * <P><B>Note:</B> Some databases may not return information for
     * all tables.
     *
     * @param catalog a catalog name; must match the catalog name as it
     *        is stored in the database; "" retrieves those without a catalog;
     *        <code>null</code> means that the catalog name should not be used to narrow
     *        the search
     * @param schemaPattern a schema name pattern; must match the schema name
     *        as it is stored in the database; "" retrieves those without a schema;
     *        <code>null</code> means that the schema name should not be used to narrow
     *        the search
     * @param tableNamePattern a table name pattern; must match the
     *        table name as it is stored in the database 
     * @param types a list of table types, which must be from the list of table types 
     *         returned from {@link #getTableTypes},to include; <code>null</code> returns
     * all types
     * @return <code>ResultSet</code> - each row is a table description
     * @exception SQLException if a database access error occurs
     * @see #getSearchStringEscape 
     */
    public ResultSet getTables(String catalog, String schemaPattern,
        String tableNamePattern, String types[]) throws SQLException {

        // TODO null or "" are invalid according to JDBC spec
        if (tableNamePattern == null || "".equals(tableNamePattern))
            tableNamePattern = "%";

        checkCatalogAndSchema(catalog, schemaPattern);
        if (types == null) {
            types = ALL_TYPES;
        }
        String sql;
        List<String> params = new ArrayList<String>();
        if (isAllCondition(tableNamePattern)) {
            sql = GET_TABLES_ALL;
            params.add(getWantsSystemTables(types));
            params.add(getWantsTables(types));
            params.add(getWantsViews(types));
        }
        else if (hasNoWildcards(tableNamePattern)) {
            tableNamePattern = stripQuotes(stripEscape(tableNamePattern), true);
            sql = GET_TABLES_EXACT;
            params.add(getWantsSystemTables(types));
            params.add(tableNamePattern);
            params.add(getWantsTables(types));
            params.add(tableNamePattern);
            params.add(getWantsViews(types));
            params.add(tableNamePattern);
        }
        else {
            // TODO Usages of 1) uppercase and 2) SPACES_31 + % might be wrong
            // See also comment in Clause for explanation
            tableNamePattern = stripQuotes(tableNamePattern, true) + SPACES_15 + "%";
            sql = GET_TABLES_LIKE;
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
     * <P>The schema columns are:
     *  <OL>
     *  <LI><B>TABLE_SCHEM</B> String => schema name
     *  <LI><B>TABLE_CATALOG</B> String => catalog name (may be <code>null</code>)
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

        xsqlvars[0] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "TABLE_CAT", "TABLECATALOGS");

        List<byte[][]> rows = Collections.emptyList();

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

        xsqlvars[0] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "TABLE_TYPE", "TABLETYPES");

        List<byte[][]> rows = new ArrayList<byte[][]>(ALL_TYPES.length);
        for(int i = 0; i < ALL_TYPES.length; i++)
          rows.add(new byte[][] {getBytes(ALL_TYPES[i])});

        return new FBResultSet(xsqlvars, rows);
    }

    private static final String GET_COLUMNS_START =
            "SELECT cast(RF.RDB$RELATION_NAME as varchar(31)) AS RELATION_NAME," +
            "cast(RF.RDB$FIELD_NAME as varchar(31)) AS FIELD_NAME," +
            "F.RDB$FIELD_TYPE AS FIELD_TYPE," +
            "F.RDB$FIELD_SUB_TYPE AS FIELD_SUB_TYPE," +
            "F.RDB$FIELD_PRECISION AS FIELD_PRECISION," +
            "F.RDB$FIELD_SCALE AS FIELD_SCALE," +
            "F.RDB$FIELD_LENGTH AS FIELD_LENGTH," +
            "F.RDB$CHARACTER_LENGTH AS CHAR_LEN," +
            "RF.RDB$DESCRIPTION AS REMARKS," +
            "RF.RDB$DEFAULT_SOURCE AS DEFAULT_SOURCE," +
            "RF.RDB$FIELD_POSITION + 1 AS FIELD_POSITION," +
            "RF.RDB$NULL_FLAG AS NULL_FLAG," +
            "F.RDB$NULL_FLAG AS SOURCE_NULL_FLAG," +
            "F.RDB$COMPUTED_BLR AS COMPUTED_BLR " +
            "FROM RDB$RELATION_FIELDS RF," +
            "RDB$FIELDS F " +
            "WHERE ";

    public static final String GET_COLUMNS_END = " RF.RDB$FIELD_SOURCE = F.RDB$FIELD_NAME " +
        "order by 1, 11";

    /**
     * Retrieves a description of table columns available in
     * the specified catalog.
     *
     * <P>Only column descriptions matching the catalog, schema, table
     * and column name criteria are returned.  They are ordered by
     * <code>TABLE_CAT</code>,<code>TABLE_SCHEM</code>,
     * <code>TABLE_NAME</code>, and <code>ORDINAL_POSITION</code>.
     *
     * <P>Each column description has the following columns:
     *  <OL>
     *  <LI><B>TABLE_CAT</B> String => table catalog (may be <code>null</code>)
     *  <LI><B>TABLE_SCHEM</B> String => table schema (may be <code>null</code>)
     *  <LI><B>TABLE_NAME</B> String => table name
     *  <LI><B>COLUMN_NAME</B> String => column name
     *  <LI><B>DATA_TYPE</B> int => SQL type from java.sql.Types
     *  <LI><B>TYPE_NAME</B> String => Data source dependent type name,
     *  for a UDT the type name is fully qualified
     *  <LI><B>COLUMN_SIZE</B> int => column size.
     *  <LI><B>BUFFER_LENGTH</B> is not used.
     *  <LI><B>DECIMAL_DIGITS</B> int => the number of fractional digits. Null is returned for data types where
     * DECIMAL_DIGITS is not applicable.
     *  <LI><B>NUM_PREC_RADIX</B> int => Radix (typically either 10 or 2)
     *  <LI><B>NULLABLE</B> int => is NULL allowed.
     *      <UL>
     *      <LI> columnNoNulls - might not allow <code>NULL</code> values
     *      <LI> columnNullable - definitely allows <code>NULL</code> values
     *      <LI> columnNullableUnknown - nullability unknown
     *      </UL>
     *  <LI><B>REMARKS</B> String => comment describing column (may be <code>null</code>)
     *  <LI><B>COLUMN_DEF</B> String => default value for the column, which should be interpreted as a string when the value is enclosed in single quotes (may be <code>null</code>)
     *  <LI><B>SQL_DATA_TYPE</B> int => unused
     *  <LI><B>SQL_DATETIME_SUB</B> int => unused
     *  <LI><B>CHAR_OCTET_LENGTH</B> int => for char types the
     *       maximum number of bytes in the column
     *  <LI><B>ORDINAL_POSITION</B> int => index of column in table
     *      (starting at 1)
     *  <LI><B>IS_NULLABLE</B> String  => ISO rules are used to determine the nullability for a column.
     *       <UL>
     *       <LI> YES           --- if the column can include NULLs
     *       <LI> NO            --- if the column cannot include NULLs
     *       <LI> empty string  --- if the nullability for the
     * column is unknown
     *       </UL>
     *  <LI><B>SCOPE_CATALOG</B> String => catalog of table that is the scope
     *      of a reference attribute (<code>null</code> if DATA_TYPE isn't REF)
     *  <LI><B>SCOPE_SCHEMA</B> String => schema of table that is the scope
     *      of a reference attribute (<code>null</code> if the DATA_TYPE isn't REF)
     *  <LI><B>SCOPE_TABLE</B> String => table name that this the scope
     *      of a reference attribute (<code>null</code> if the DATA_TYPE isn't REF)
     *  <LI><B>SOURCE_DATA_TYPE</B> short => source type of a distinct type or user-generated
     *      Ref type, SQL type from java.sql.Types (<code>null</code> if DATA_TYPE
     *      isn't DISTINCT or user-generated REF)
     *   <LI><B>IS_AUTOINCREMENT</B> String  => Indicates whether this column is auto incremented
     *       <UL>
     *       <LI> YES           --- if the column is auto incremented
     *       <LI> NO            --- if the column is not auto incremented
     *       <LI> empty string  --- if it cannot be determined whether the column is auto incremented
     *       </UL>
     *   <LI><B>IS_GENERATEDCOLUMN</B> String  => Indicates whether this is a generated column
     *       <UL>
     *       <LI> YES           --- if this a generated column
     *       <LI> NO            --- if this not a generated column
     *       <LI> empty string  --- if it cannot be determined whether this is a generated column
     *       </UL>
     *  </OL>
     *
     * <p>The COLUMN_SIZE column specifies the column size for the given column.
     * For numeric data, this is the maximum precision.  For character data, this is the length in characters.
     * For datetime datatypes, this is the length in characters of the String representation (assuming the
     * maximum allowed precision of the fractional seconds component). For binary data, this is the length in bytes.  For the ROWID datatype,
     * this is the length in bytes. Null is returned for data types where the
     * column size is not applicable.
     *
     * @param catalog a catalog name; must match the catalog name as it
     *        is stored in the database; "" retrieves those without a catalog;
     *        <code>null</code> means that the catalog name should not be used to narrow
     *        the search
     * @param schemaPattern a schema name pattern; must match the schema name
     *        as it is stored in the database; "" retrieves those without a schema;
     *        <code>null</code> means that the schema name should not be used to narrow
     *        the search
     * @param tableNamePattern a table name pattern; must match the
     *        table name as it is stored in the database
     * @param columnNamePattern a column name pattern; must match the column
     *        name as it is stored in the database
     * @return <code>ResultSet</code> - each row is a column description
     * @exception SQLException if a database access error occurs
     * @see #getSearchStringEscape
     */
    public ResultSet getColumns(String catalog, String schemaPattern,
        String tableNamePattern, String columnNamePattern) throws SQLException {
        checkCatalogAndSchema(catalog, schemaPattern);

        XSQLVAR[] xsqlvars = new XSQLVAR[24];

        xsqlvars[0] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "TABLE_CAT", "COLUMNINFO");
        xsqlvars[1] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "TABLE_SCHEM", "COLUMNINFO");
        xsqlvars[2] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "TABLE_NAME", "COLUMNINFO");
        xsqlvars[3] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "COLUMN_NAME", "COLUMNINFO");
        xsqlvars[4] = new XSQLVAR(ISCConstants.SQL_LONG, 0, "DATA_TYPE", "COLUMNINFO");
        xsqlvars[5] = new XSQLVAR(ISCConstants.SQL_VARYING | 1, 31, "TYPE_NAME", "COLUMNINFO");
        xsqlvars[6] = new XSQLVAR(ISCConstants.SQL_LONG, 0, "COLUMN_SIZE", "COLUMNINFO");
        xsqlvars[7] = new XSQLVAR(ISCConstants.SQL_LONG, 0, "BUFFER_LENGTH", "COLUMNINFO");
        xsqlvars[8] = new XSQLVAR(ISCConstants.SQL_LONG, 0, "DECIMAL_DIGITS", "COLUMNINFO");
        xsqlvars[9] = new XSQLVAR(ISCConstants.SQL_LONG, 0, "NUM_PREC_RADIX", "COLUMNINFO");
        xsqlvars[10] = new XSQLVAR(ISCConstants.SQL_LONG, 0, "NULLABLE", "COLUMNINFO");
        xsqlvars[11] = new XSQLVAR(ISCConstants.SQL_VARYING | 1, 80, "REMARKS", "COLUMNINFO");
        xsqlvars[12] = new XSQLVAR(ISCConstants.SQL_VARYING | 1, 31, "COLUMN_DEF", "COLUMNINFO");
        xsqlvars[13] = new XSQLVAR(ISCConstants.SQL_LONG, 0, "SQL_DATA_TYPE", "COLUMNINFO");
        xsqlvars[14] = new XSQLVAR(ISCConstants.SQL_LONG, 0, "SQL_DATETIME_SUB", "COLUMNINFO");
        xsqlvars[15] = new XSQLVAR(ISCConstants.SQL_LONG, 0, "CHAR_OCTET_LENGTH", "COLUMNINFO");
        xsqlvars[16] = new XSQLVAR(ISCConstants.SQL_LONG, 0, "ORDINAL_POSITION", "COLUMNINFO");
        xsqlvars[17] = new XSQLVAR(ISCConstants.SQL_VARYING, 3, "IS_NULLABLE", "COLUMNINFO");
        final String scopeCatalog;
        if (getJDBCMajorVersion() > 4 || getJDBCMajorVersion() == 4 && getJDBCMinorVersion() >= 1) {
            scopeCatalog = "SCOPE_CATALOG";
        } else {
            scopeCatalog = "SCOPE_CATLOG";
        }
        xsqlvars[18] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, scopeCatalog, "COLUMNINFO");
        xsqlvars[19] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "SCOPE_SCHEMA", "COLUMNINFO");
        xsqlvars[20] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "SCOPE_TABLE", "COLUMNINFO");
        xsqlvars[21] = new XSQLVAR(ISCConstants.SQL_SHORT, 0, "SOURCE_DATA_TYPE", "COLUMNINFO");
        xsqlvars[22] = new XSQLVAR(ISCConstants.SQL_VARYING, 3, "IS_AUTOINCREMENT", "COLUMNINFO");
        xsqlvars[23] = new XSQLVAR(ISCConstants.SQL_VARYING, 3, "IS_GENERATEDCOLUMN", "COLUMNINFO");

        Clause tableClause = new Clause("RF.RDB$RELATION_NAME", tableNamePattern);
        Clause columnClause = new Clause("RF.RDB$FIELD_NAME", columnNamePattern);
        
        String sql = GET_COLUMNS_START;
        sql += tableClause.getCondition();
        sql += columnClause.getCondition();
        sql += GET_COLUMNS_END;
        
        List<String> params = new ArrayList<String>();
        
        // check first original case values
        if (!tableClause.getCondition().equals("")) {
            params.add(tableClause.getOriginalCaseValue());
        }
        if (!columnClause.getCondition().equals("")) {
            params.add(columnClause.getOriginalCaseValue());
        }
        
        ResultSet rs = doQuery(sql, params);
        List<byte[][]> rows = new ArrayList<byte[][]>();

        // if no direct match happened, check the uppercased match
        if (!rs.next()) {
            rs.close();
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
            if (!rs.next()) {
                rs.close();
                return new FBResultSet(xsqlvars, rows);
            }
        }

        do {
            byte[][] row = new byte[24][];
            row[0] = null;
            row[1] = null;
            row[2] = getBytes(rs.getString("RELATION_NAME"));
            row[3] = getBytes(rs.getString("FIELD_NAME"));

            short fieldType = rs.getShort("FIELD_TYPE");
            short fieldSubType = rs.getShort("FIELD_SUB_TYPE");
            short fieldScale = rs.getShort("FIELD_SCALE");
            int dataType = getDataType(fieldType, fieldSubType, fieldScale);

            row[4] = createInt(dataType);
            row[5] = getBytes(getDataTypeName(fieldType, fieldSubType, fieldScale));
            
            row[7] = null;
            
            // Defaults: some are overridden in the switch
            row[8] = null;
            row[9] = RADIX_TEN;
            row[15] = null;
            switch (dataType){
                case Types.DECIMAL:
                case Types.NUMERIC:
                   row[6] = createInt(rs.getShort("FIELD_PRECISION"));
                   row[8] = createInt(fieldScale * (-1));
                   break;
                case Types.CHAR:
                case Types.VARCHAR:
                    row[15] = createInt(rs.getShort("FIELD_LENGTH"));
                   short charLen = rs.getShort("CHAR_LEN");
                   if (!rs.wasNull()) {
                       row[6] = createInt(charLen);
                   } else {
                       row[6] = row[15];
                   }
                   break;
                case Types.FLOAT:
                   row[6] = FLOAT_PRECISION;
                   break;
                case Types.DOUBLE:
                   row[6] = DOUBLE_PRECISION;
                   break;
                case Types.BIGINT:
                    row[6] = BIGINT_PRECISION;
                    row[8] = INT_ZERO;
                    break;
                case Types.INTEGER:
                   row[6] = INTEGER_PRECISION;
                   row[8] = INT_ZERO;
                   break;
                case Types.SMALLINT:
                   row[6] = SMALLINT_PRECISION;
                   row[8] = INT_ZERO;
                   break;
                case Types.DATE:
                   row[6] = DATE_PRECISION;
                   break;
                case Types.TIME:
                   row[6] = TIME_PRECISION;
                   break;
                case Types.TIMESTAMP:
                   row[6] = TIMESTAMP_PRECISION;
                   break;
                case Types.BOOLEAN:
                    row[6] = BOOLEAN_PRECISION;
                    row[9] = RADIX_BINARY;
                    break;
                default:
                   row[6] = null;
            }

            short nullFlag = rs.getShort("NULL_FLAG");
            short sourceNullFlag = rs.getShort("SOURCE_NULL_FLAG");
            row[10] = (nullFlag == 1 || sourceNullFlag == 1) ?
                    COLUMN_NO_NULLS :
                    COLUMN_NULLABLE;

            String remarks = rs.getString("REMARKS");  
            row[11] = getBytes(remarks);             
            if (remarks != null && remarks.length() > xsqlvars[11].sqllen)               
                xsqlvars[11].sqllen = remarks.length();
            
            String column_def = rs.getString("DEFAULT_SOURCE");
            if (column_def != null) {
                int defaultPos = column_def.toUpperCase().indexOf("DEFAULT");
                if (defaultPos >= 0)
                    column_def = column_def.substring(7).trim();
                
            	row[12] = getBytes(column_def);
            } else
            	row[12] = null;
            
            row[13] = null;
            row[14] = null;
            row[16] = createInt(rs.getInt("FIELD_POSITION"));
            row[17] = (nullFlag == 1 || sourceNullFlag == 1) ? NO_BYTES : YES_BYTES;
            row[18] = null;
            row[19] = null;
            row[20] = null;
            row[21] = null;
            switch (dataType) {
            case Types.INTEGER:
            case Types.TINYINT:
            case Types.BIGINT:
            case Types.SMALLINT:
                // Could be autoincrement, but we simply don't know
                row[22] = EMPTY_STRING_BYTES;
                break;
            case Types.NUMERIC:
            case Types.DECIMAL:
            	if (fieldScale == 0) {
            		// Could be autoincrement, but we simply don't know
                    row[22] = EMPTY_STRING_BYTES;
            	} else {
            		// Scaled NUMERIC/DECIMAL: definitely not autoincrement
            		row[22] = NO_BYTES;
            	}
            	break;
            default:
                // All other types are never autoincrement
                row[22] = NO_BYTES;
            }
            // Retrieving COMPUTED_BLR to check if it was NULL or not
            rs.getString("COMPUTED_BLR");
            row[23] = rs.wasNull() ? NO_BYTES : YES_BYTES;

            rows.add(row);
        } while (rs.next());
        rs.close();
        return new FBResultSet(xsqlvars, rows);
    }

    private static final int smallint_type = 7;
    private static final int integer_type = 8;
    private static final int quad_type = 9;
    private static final int float_type = 10;
    private static final int d_float_type = 11;
    private static final int date_type = 12;
    private static final int time_type = 13;
    private static final int char_type = 14;
    private static final int int64_type = 16;
    private static final int double_type = 27;
    private static final int timestamp_type = 35;
    private static final int varchar_type = 37;
//  private static final int cstring_type = 40;
    private static final int blob_type = 261;
    private static final short boolean_type = 23;

    private static int getDataType(int fieldType, int fieldSubType, int fieldScale) {
        switch (fieldType) {
            case smallint_type:
                if (fieldSubType == SUBTYPE_NUMERIC || (fieldSubType == 0 && fieldScale < 0))
                    return Types.NUMERIC;
                else if (fieldSubType == SUBTYPE_DECIMAL)
                    return Types.DECIMAL;
                else
                    return Types.SMALLINT;
            case integer_type:
                if (fieldSubType == SUBTYPE_NUMERIC || (fieldSubType == 0 && fieldScale < 0))
                    return Types.NUMERIC;
                else if (fieldSubType == SUBTYPE_DECIMAL)
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
                if (fieldSubType == SUBTYPE_NUMERIC || (fieldSubType == 0 && fieldScale < 0))
                    return Types.NUMERIC;
                else if (fieldSubType == SUBTYPE_DECIMAL)
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
            case boolean_type:
                return Types.BOOLEAN;
            default:
                return Types.NULL;
        }
    }

    private static String getDataTypeName(int sqltype, int sqlsubtype, int sqlscale) {
        switch (sqltype) {
            case smallint_type:
                if (sqlsubtype == SUBTYPE_NUMERIC || (sqlsubtype == 0 && sqlscale < 0))
                    return "NUMERIC";
                else if (sqlsubtype == SUBTYPE_DECIMAL)
                    return "DECIMAL";
                else
                    return "SMALLINT";
            case integer_type:
                if (sqlsubtype == SUBTYPE_NUMERIC || (sqlsubtype == 0 && sqlscale < 0))
                    return "NUMERIC";
                else if (sqlsubtype == SUBTYPE_DECIMAL)
                    return "DECIMAL";
                else
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
                if (sqlsubtype == SUBTYPE_NUMERIC || (sqlsubtype == 0 && sqlscale < 0))
                    return "NUMERIC";
                else if (sqlsubtype == SUBTYPE_DECIMAL)
                    return "DECIMAL";
                else
                    return "BIGINT";
            case blob_type:
                if (sqlsubtype < 0)
                    // TODO Include actual subtype?
                    return "BLOB SUB_TYPE <0";
                else if (sqlsubtype == 0)
                    return "BLOB SUB_TYPE 0";
                else if (sqlsubtype == 1)
                    return "BLOB SUB_TYPE 1";
                else
                    return "BLOB SUB_TYPE " + sqlsubtype;
            case quad_type:
                return "ARRAY";
            case boolean_type:
                return "BOOLEAN";
            default:
                return "NULL";
        }
    }

    private static final String GET_COLUMN_PRIVILEGES_START = "select "
        /*+ "null as TABLE_CAT,"
        + "null as TABLE_SCHEM,"*/
        + "cast(RF.RDB$RELATION_NAME as varchar(31)) as TABLE_NAME,"
        + "cast(RF.RDB$FIELD_NAME as varchar(31)) as COLUMN_NAME,"
        + "cast(UP.RDB$GRANTOR as varchar(31)) as GRANTOR,"
        + "cast(UP.RDB$USER as varchar(31)) as GRANTEE,"
        + "cast(UP.RDB$PRIVILEGE as varchar(6)) as PRIVILEGE,"
        + "UP.RDB$GRANT_OPTION as IS_GRANTABLE "
        + "from "
        + "RDB$RELATION_FIELDS RF,"
        + "RDB$FIELDS F,"
        + "RDB$USER_PRIVILEGES UP "
        + "where "
        + "RF.RDB$RELATION_NAME = UP.RDB$RELATION_NAME and "
        + "RF.RDB$FIELD_SOURCE = F.RDB$FIELD_NAME  and "
        + "(UP.RDB$FIELD_NAME is null or "
        + "UP.RDB$FIELD_NAME = RF.RDB$FIELD_NAME) and "
        + "CAST(UP.RDB$RELATION_NAME AS VARCHAR(40)) = ? and ((";
    private static final String GET_COLUMN_PRIVILEGES_END = " UP.RDB$OBJECT_TYPE = 0) or "
        + "(RF.RDB$FIELD_NAME is null and UP.RDB$OBJECT_TYPE = 0)) "
        + "order by 2,5 ";

    private static final Map<String, byte[]> PRIVILEGE_MAPPING;
    static {
        Map<String, byte[]> tempMapping = new HashMap<String, byte[]>(7);
        tempMapping.put("A", getBytes("ALL"));
        tempMapping.put("S", getBytes("SELECT"));
        tempMapping.put("D", getBytes("DELETE"));
        tempMapping.put("I", getBytes("INSERT"));
        tempMapping.put("U", getBytes("UPDATE"));
        tempMapping.put("R", getBytes("REFERENCE")); // TODO: JDBC apidoc specifies REFRENCES (yes: typo and + S)
        tempMapping.put("M", getBytes("MEMBEROF"));
        PRIVILEGE_MAPPING = Collections.unmodifiableMap(tempMapping);
    }

    /**
     * Maps the (one character) Firebird privilege to the equivalent JDBC privilege.
     *
     * @param firebirdPrivilege Firebird privilege
     * @return JDBC privilege encoded as byte array
     */
    private static byte[] mapPrivilege(String firebirdPrivilege) {
        return PRIVILEGE_MAPPING.get(firebirdPrivilege);
    }

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

        xsqlvars[0] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "TABLE_CAT", "COLUMNPRIV");
        xsqlvars[1] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "TABLE_SCHEM", "COLUMNPRIV");
        xsqlvars[2] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "TABLE_NAME", "COLUMNPRIV");
        xsqlvars[3] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "COLUMN_NAME", "COLUMNPRIV");
        xsqlvars[4] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "GRANTOR", "COLUMNPRIV");
        xsqlvars[5] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "GRANTEE", "COLUMNPRIV");
        xsqlvars[6] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "PRIVILEGE", "COLUMNPRIV");
        xsqlvars[7] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "IS_GRANTABLE", "COLUMNPRIV");

        Clause columnClause = new Clause("RF.RDB$FIELD_NAME", columnNamePattern);
        
        String sql = GET_COLUMN_PRIVILEGES_START;
        sql += columnClause.getCondition();
        sql += GET_COLUMN_PRIVILEGES_END;
        
        List<String> params = new ArrayList<String>();
        
        // check the original case first
        table = stripQuotes(stripEscape(table), false);
        params.add(table);
        if (!columnClause.getCondition().equals("")) {
            params.add(columnClause.getOriginalCaseValue());
        }
        

        ResultSet rs = doQuery(sql, params);
        
        // if nothing was found, check the uppercased identifiers
        if (!rs.next()) {
            rs.close();
            params.clear();
            if (!columnClause.getCondition().equals("")) {
                params.add(stripQuotes(stripEscape(table), true));
                params.add(columnClause.getValue());
            }
            
            rs = doQuery(sql, params);
            
            // return empty result set 
            if (!rs.next()) {
                rs.close();
                return new FBResultSet(xsqlvars, Collections.<byte[][]>emptyList());
            }
        }

        List<byte[][]> rows = new ArrayList<byte[][]>();
        do {
            byte[][] row = new byte[8][];
            row[0] = null;
            row[1] = null;
            row[2] = getBytes(rs.getString("TABLE_NAME"));
            row[3] = getBytes(rs.getString("COLUMN_NAME"));
            row[4] = getBytes(rs.getString("GRANTOR"));
            row[5] = getBytes(rs.getString("GRANTEE"));
            row[6] = mapPrivilege(rs.getString("PRIVILEGE"));
            int isGrantable = rs.getShort("IS_GRANTABLE");
            row[7] = isGrantable == 0 ? NO_BYTES : YES_BYTES;

            rows.add(row);
        } while(rs.next());
        rs.close();
        return new FBResultSet(xsqlvars, rows);
    }

    private static final String GET_TABLE_PRIVILEGES_START = "select "
        /*+ " null as TABLE_CAT, "
        + " null as TABLE_SCHEM,"*/
        + "cast(RDB$RELATION_NAME as varchar(31)) as TABLE_NAME,"
        + "cast(RDB$GRANTOR as varchar(31)) as GRANTOR,"
        + "cast(RDB$USER as varchar(31)) as GRANTEE,"
        + "cast(RDB$PRIVILEGE as varchar(6)) as PRIVILEGE,"
        + "RDB$GRANT_OPTION as IS_GRANTABLE "
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
        tableNamePattern = stripQuotes(stripEscape(tableNamePattern), true);

        XSQLVAR[] xsqlvars = buildTablePrivilegeRSMetaData();

        Clause tableClause = new Clause("RDB$RELATION_NAME", tableNamePattern);
        
        String sql = GET_TABLE_PRIVILEGES_START;
        sql += tableClause.getCondition();
        sql += GET_TABLE_PRIVILEGES_END;
        
        // check the original case identifiers first
        List<String> params = new ArrayList<String>();
        if (!tableClause.getCondition().equals("")) {
            params.add(tableClause.getOriginalCaseValue());
        }

        ResultSet rs = doQuery(sql, params);
        
        // if nothing found, check the uppercased identifiers
        if (!rs.next()) {
            rs.close();
            params.clear();
            if (!tableClause.getCondition().equals("")) {
                params.add(tableClause.getValue());
            }
            
            rs = doQuery(sql, params);
            
            // if nothing found, return an empty result set
            if (!rs.next()) {
                rs.close();
                return new FBResultSet(xsqlvars, Collections.<byte[][]>emptyList());
            }
        }
        
        return processTablePrivileges(xsqlvars, rs);
    }
    
    protected final XSQLVAR[] buildTablePrivilegeRSMetaData() {
        XSQLVAR[] xsqlvars = new XSQLVAR[7];

        xsqlvars[0] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "TABLE_CAT", "TABLEPRIV");
        xsqlvars[1] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "TABLE_SCHEM", "TABLEPRIV");
        xsqlvars[2] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "TABLE_NAME", "TABLEPRIV");
        xsqlvars[3] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "GRANTOR", "TABLEPRIV");
        xsqlvars[4] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "GRANTEE", "TABLEPRIV");
        xsqlvars[5] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "PRIVILEGE", "TABLEPRIV");
        xsqlvars[6] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "IS_GRANTABLE", "TABLEPRIV");
        
        return xsqlvars;
    }
    
    protected final FBResultSet processTablePrivileges(XSQLVAR[] xsqlvars, ResultSet fbTablePrivileges) throws SQLException {
        List<byte[][]> rows = new ArrayList<byte[][]>();
        
        do {
            byte[][] row = new byte[7][];
            row[0] = null;
            row[1] = null;
            row[2] = getBytes(fbTablePrivileges.getString("TABLE_NAME"));
            row[3] = getBytes(fbTablePrivileges.getString("GRANTOR"));
            row[4] = getBytes(fbTablePrivileges.getString("GRANTEE"));
            row[5] = mapPrivilege(fbTablePrivileges.getString("PRIVILEGE"));
            int isGrantable = fbTablePrivileges.getShort("IS_GRANTABLE");
            row[6] = isGrantable == 0 ? NO_BYTES : YES_BYTES;

            rows.add(row);
        } while (fbTablePrivileges.next());
        fbTablePrivileges.close();
        return new FBResultSet(xsqlvars, rows);
    }

    private static final String GET_BEST_ROW_IDENT =
            "SELECT " +
            "CAST(rf.rdb$field_name AS varchar(31)) AS column_name, " +
            "f.rdb$field_type AS field_type, " +
            "f.rdb$field_sub_type AS field_sub_type, " +
            "f.rdb$field_scale AS field_scale, " +
            "f.rdb$field_precision AS field_precision " +
            "FROM rdb$relation_constraints rc " +
            "INNER JOIN rdb$index_segments idx ON idx.rdb$index_name = rc.rdb$index_name " +
            "INNER JOIN rdb$relation_fields rf ON rf.rdb$field_name = idx.rdb$field_name " +
            "    AND rf.rdb$relation_name = rc.rdb$relation_name " +
            "INNER JOIN rdb$fields f ON f.rdb$field_name = rf.rdb$field_source " +
            "WHERE " +
            "CAST(rc.rdb$relation_name AS VARCHAR(40)) = ? " +
            "AND rc.rdb$constraint_type = 'PRIMARY KEY'";

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

        xsqlvars[0] = new XSQLVAR(ISCConstants.SQL_SHORT, 0, "SCOPE", "ROWIDENTIFIER");
        xsqlvars[1] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "COLUMN_NAME", "ROWIDENTIFIER");
        xsqlvars[2] = new XSQLVAR(ISCConstants.SQL_SHORT, 0, "DATA_TYPE", "ROWIDENTIFIER");
        xsqlvars[3] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "TYPE_NAME", "ROWIDENTIFIER");
        xsqlvars[4] = new XSQLVAR(ISCConstants.SQL_LONG, 0, "COLUMN_SIZE", "ROWIDENTIFIER");
        xsqlvars[5] = new XSQLVAR(ISCConstants.SQL_LONG, 0, "BUFFER_LENGTH", "ROWIDENTIFIER");
        xsqlvars[6] = new XSQLVAR(ISCConstants.SQL_SHORT, 0, "DECIMAL_DIGITS", "ROWIDENTIFIER");
        xsqlvars[7] = new XSQLVAR(ISCConstants.SQL_SHORT, 0, "PSEUDO_COLUMN", "ROWIDENTIFIER");

        ResultSet tables = null;
        List<byte[][]> rows = null;
        try {
            // Check if table exists, need to escape as getTables takes a pattern
            String quoteLikeTable = table != null ? table.replaceAll("([_%])", "\\\\$1") : null;
            tables = getTables(catalog, schema, quoteLikeTable, null);
            if (!tables.next())
                return new FBResultSet(xsqlvars, Collections.<byte[][]>emptyList());
            rows = getPrimaryKeyIdentifier(tables.getString(3), scope, xsqlvars);
        } finally {
            if (tables != null) tables.close();
        }

        // if no primary key exists, add RDB$DB_KEY as pseudo-column
        if (rows.size() == 0) {
            byte[][] row = new byte[8][];
            row[0] = createShort(scope);
            row[1] = getBytes("RDB$DB_KEY");
            row[2] = createShort(getDataType(char_type, 0, 0));
            row[3] = getBytes(getDataTypeName(char_type, 0, 0));
            row[4] = createInt(0);
            row[5] = null;
            row[6] = createShort(0);
            row[7] = createShort(bestRowPseudo);

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
    private List<byte[][]> getPrimaryKeyIdentifier(String table, int scope, XSQLVAR[] xsqlvars) throws SQLException {
        List<byte[][]> rows = new ArrayList<byte[][]>();

        List<String> params = new ArrayList<String>(1);
        params.add(table);

        ResultSet rs = doQuery(GET_BEST_ROW_IDENT, params);
        
        while (rs.next()) {
            byte[][] row = new byte[8][];
            row[0] = createShort(scope);
            row[1] = getBytes(rs.getString("COLUMN_NAME"));
            short fieldType = rs.getShort("FIELD_TYPE");
            short fieldSubType = rs.getShort("FIELD_SUB_TYPE");
            short fieldScale = rs.getShort("FIELD_SCALE");
            row[2] = createShort(getDataType(fieldType, fieldSubType, fieldScale));
            row[3] = getBytes(getDataTypeName(fieldType, fieldSubType, fieldScale));
            row[4] = createInt(rs.getInt("FIELD_PRECISION"));
            row[5] = null;
            row[6] = createShort(fieldScale);
            row[7] = createShort(bestRowNotPseudo);

            rows.add(row);
        }
        rs.close();
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

        xsqlvars[0] = new XSQLVAR(ISCConstants.SQL_SHORT, 0, "SCOPE", "VERSIONCOL");
        xsqlvars[1] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "COLUMN_NAME", "VERSIONCOL");
        xsqlvars[2] = new XSQLVAR(ISCConstants.SQL_SHORT, 0, "DATA_TYPE", "VERSIONCOL");
        xsqlvars[3] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "TYPE_NAME", "VERSIONCOL");
        xsqlvars[4] = new XSQLVAR(ISCConstants.SQL_LONG, 0, "COLUMN_SIZE", "VERSIONCOL");
        xsqlvars[5] = new XSQLVAR(ISCConstants.SQL_LONG, 0, "BUFFER_LENGTH", "VERSIONCOL");
        xsqlvars[6] = new XSQLVAR(ISCConstants.SQL_SHORT, 0, "DECIMAL_DIGITS", "VERSIONCOL");
        xsqlvars[7] = new XSQLVAR(ISCConstants.SQL_SHORT, 0, "PSEUDO_COLUMN", "VERSIONCOL");

        List<byte[][]> rows = Collections.emptyList();

        return new FBResultSet(xsqlvars, rows);
    }

    private static final String GET_PRIMARY_KEYS = "select "
        /*+ " null as TABLE_CAT, "
        + " null as TABLE_SCHEM, "*/
        + "cast(RC.RDB$RELATION_NAME as varchar(31)) as TABLE_NAME,"
        + "cast(ISGMT.RDB$FIELD_NAME as varchar(31)) as COLUMN_NAME,"
        + "CAST((ISGMT.RDB$FIELD_POSITION + 1) as SMALLINT) as KEY_SEQ,"
        + "cast(RC.RDB$CONSTRAINT_NAME as varchar(31)) as PK_NAME "
        + "from "
        + "RDB$RELATION_CONSTRAINTS RC "
        + "INNER JOIN RDB$INDEX_SEGMENTS ISGMT ON RC.RDB$INDEX_NAME = ISGMT.RDB$INDEX_NAME "
        + "where CAST(RC.RDB$RELATION_NAME AS VARCHAR(40)) = ? and "
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

        xsqlvars[0] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "TABLE_CAT", "COLUMNINFO");
        xsqlvars[1] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "TABLE_SCHEM", "COLUMNINFO");
        xsqlvars[2] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "TABLE_NAME", "COLUMNINFO");
        xsqlvars[3] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "COLUMN_NAME", "COLUMNINFO");
        xsqlvars[4] = new XSQLVAR(ISCConstants.SQL_SHORT, 0, "KEY_SEQ", "COLUMNINFO");
        xsqlvars[5] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "PK_NAME", "COLUMNINFO");

        // check the original case identifiers
        List<String> params = new ArrayList<String>();
        params.add(stripQuotes(stripEscape(table), false));
        
        ResultSet rs = doQuery(GET_PRIMARY_KEYS, params);
        
        // if nothing found, check the uppercased identifier
        if (!rs.next()) {
            rs.close();
            params.clear();
            params.add(stripQuotes(stripEscape(table), true));

            rs = doQuery(GET_PRIMARY_KEYS, params);
            
            // if nothing found, return empty result set
            if (!rs.next()) {
                rs.close();
                return new FBResultSet(xsqlvars, Collections.<byte[][]>emptyList());
            }
        }

        List<byte[][]> rows = new ArrayList<byte[][]>();
        do {
            byte[][] row = new byte[6][];
            row[0] = null;
            row[1] = null;
            row[2] = getBytes(rs.getString("TABLE_NAME"));
            row[3] = getBytes(rs.getString("COLUMN_NAME"));
            row[4] = createShort(rs.getShort("KEY_SEQ"));
            row[5] = getBytes(rs.getString("PK_NAME"));

            rows.add(row);
        } while(rs.next());
        rs.close();
        return new FBResultSet(xsqlvars, rows);
    }

    private static final String GET_IMPORTED_KEYS = "select "
    /*+" null as PKTABLE_CAT "
    +" ,null as PKTABLE_SCHEM "*/
    +"cast(PK.RDB$RELATION_NAME as varchar(31)) as PKTABLE_NAME"
    +",cast(ISP.RDB$FIELD_NAME as varchar(31)) as PKCOLUMN_NAME"
    /*+" ,null as FKTABLE_CAT "
    +" ,null as FKTABLE_SCHEM "*/
    +",cast(FK.RDB$RELATION_NAME as varchar(31)) as FKTABLE_NAME"
    +",cast(ISF.RDB$FIELD_NAME as varchar(31)) as FKCOLUMN_NAME"
    +",CAST((ISP.RDB$FIELD_POSITION + 1) as SMALLINT) as KEY_SEQ"
    +",cast(RC.RDB$UPDATE_RULE as varchar(11)) as UPDATE_RULE"
    +",cast(RC.RDB$DELETE_RULE as varchar(11)) as DELETE_RULE"
    +",cast(PK.RDB$CONSTRAINT_NAME as varchar(31)) as PK_NAME"
    +",cast(FK.RDB$CONSTRAINT_NAME as varchar(31)) as FK_NAME "
    /*+" ,null as DEFERRABILITY "*/
    +"from "
    +"RDB$RELATION_CONSTRAINTS PK"
    +",RDB$RELATION_CONSTRAINTS FK"
    +",RDB$REF_CONSTRAINTS RC"
    +",RDB$INDEX_SEGMENTS ISP"
    +",RDB$INDEX_SEGMENTS ISF "
    +"WHERE CAST(FK.RDB$RELATION_NAME AS VARCHAR(40)) = ? and "
    +" FK.RDB$CONSTRAINT_NAME = RC.RDB$CONSTRAINT_NAME "
    +"and PK.RDB$CONSTRAINT_NAME = RC.RDB$CONST_NAME_UQ "
    +"and ISP.RDB$INDEX_NAME = PK.RDB$INDEX_NAME "
    +"and ISF.RDB$INDEX_NAME = FK.RDB$INDEX_NAME "
    +"and ISP.RDB$FIELD_POSITION = ISF.RDB$FIELD_POSITION "
    +"order by 1, 5 ";

    private static final Map<String, byte[]> ACTION_MAPPING;
    static {
        Map<String, byte[]> tempMap = new HashMap<String, byte[]>();
        tempMap.put("NO ACTION", IMPORTED_KEY_NO_ACTION);
        tempMap.put("RESTRICT", IMPORTED_KEY_NO_ACTION);
        tempMap.put("CASCADE", IMPORTED_KEY_CASCADE);
        tempMap.put("SET NULL", IMPORTED_KEY_SET_NULL);
        tempMap.put("SET DEFAULT", IMPORTED_KEY_SET_DEFAULT);
        ACTION_MAPPING = Collections.unmodifiableMap(tempMap);
    }

    /**
     * Maps the Firebird action name to the equivalent JDBC action.
     *
     * @param fbAction Firebird action
     * @return JDBC action encoded as byte array
     */
    private static byte[] mapAction(String fbAction) {
        return ACTION_MAPPING.get(fbAction);
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
        checkCatalogAndSchema(catalog, schema);

        XSQLVAR[] xsqlvars = new XSQLVAR[14];

        xsqlvars[0] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "PKTABLE_CAT", "COLUMNINFO");
        xsqlvars[1] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "PKTABLE_SCHEM", "COLUMNINFO");
        xsqlvars[2] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "PKTABLE_NAME", "COLUMNINFO");
        xsqlvars[3] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "PKCOLUMN_NAME", "COLUMNINFO");
        xsqlvars[4] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "FKTABLE_CAT", "COLUMNINFO");
        xsqlvars[5] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "FKTABLE_SCHEM", "COLUMNINFO");
        xsqlvars[6] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "FKTABLE_NAME", "COLUMNINFO");
        xsqlvars[7] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "FKCOLUMN_NAME", "COLUMNINFO");
        xsqlvars[8] = new XSQLVAR(ISCConstants.SQL_SHORT, 0, "KEY_SEQ", "COLUMNINFO");
        xsqlvars[9] = new XSQLVAR(ISCConstants.SQL_SHORT, 0, "UPDATE_RULE", "COLUMNINFO");
        xsqlvars[10] = new XSQLVAR(ISCConstants.SQL_SHORT, 0, "DELETE_RULE", "COLUMNINFO");
        xsqlvars[11] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "FK_NAME", "COLUMNINFO");
        xsqlvars[12] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "PK_NAME", "COLUMNINFO");
        xsqlvars[13] = new XSQLVAR(ISCConstants.SQL_SHORT, 0, "DEFERRABILITY", "COLUMNINFO");

        String sql = GET_IMPORTED_KEYS;

        // check the original case identifiers first
        List<String> params = new ArrayList<String>();
        params.add(stripQuotes(stripEscape(table), false));
        
        ResultSet rs = doQuery(sql, params);
        
        // if nothing found, check the uppercased identifiers
        if (!rs.next()) {
            rs.close();
            params.clear();
            params.add(stripQuotes(stripEscape(table), true));
            
            rs = doQuery(sql, params);
            
            // if nothing found, return an empty result set
            if (!rs.next()) {
                rs.close();
                return new FBResultSet(xsqlvars, Collections.<byte[][]>emptyList());
            }
        }

        List<byte[][]> rows = new ArrayList<byte[][]>();
        do {
            byte[][] row = new byte[14][];
            row[0] = null;
            row[1] = null;
            row[2] = getBytes(rs.getString("PKTABLE_NAME"));
            row[3] = getBytes(rs.getString("PKCOLUMN_NAME"));
            row[4] = null;
            row[5] = null;
            row[6] = getBytes(rs.getString("FKTABLE_NAME"));
            row[7] = getBytes(rs.getString("FKCOLUMN_NAME"));
            row[8] = createShort(rs.getShort("KEY_SEQ"));
            String updateRule = rs.getString("UPDATE_RULE");
            row[9] = mapAction(updateRule);
            String deleteRule = rs.getString("DELETE_RULE");
            row[10] = mapAction(deleteRule);
            row[11] = getBytes(rs.getString("FK_NAME"));
            row[12] = getBytes(rs.getString("PK_NAME"));
            row[13] = IMPORTED_KEY_NOT_DEFERRABLE;
            rows.add(row);
        } while (rs.next());
        rs.close();
        return new FBResultSet(xsqlvars, rows);
    }

    private static final String GET_EXPORTED_KEYS = "select "
    /*+" null as PKTABLE_CAT "
    +" ,null as PKTABLE_SCHEM "*/
    +"cast(PK.RDB$RELATION_NAME as varchar(31)) as PKTABLE_NAME"
    +",cast(ISP.RDB$FIELD_NAME as varchar(31)) as PKCOLUMN_NAME"
    /*+" ,null as FKTABLE_CAT "
    +" ,null as FKTABLE_SCHEM "*/
    +",cast(FK.RDB$RELATION_NAME as varchar(31)) as FKTABLE_NAME"
    +",cast(ISF.RDB$FIELD_NAME as varchar(31)) as FKCOLUMN_NAME"
    +",CAST((ISP.RDB$FIELD_POSITION + 1) as SMALLINT) as KEY_SEQ"
    +",cast(RC.RDB$UPDATE_RULE as varchar(11)) as UPDATE_RULE"
    +",cast(RC.RDB$DELETE_RULE as varchar(11)) as DELETE_RULE"
    +",cast(PK.RDB$CONSTRAINT_NAME as varchar(31)) as PK_NAME"
    +",cast(FK.RDB$CONSTRAINT_NAME as varchar(31)) as FK_NAME "
    /*+" ,null as DEFERRABILITY "*/
    +"from "
    +"RDB$RELATION_CONSTRAINTS PK"
    +",RDB$RELATION_CONSTRAINTS FK"
    +",RDB$REF_CONSTRAINTS RC"
    +",RDB$INDEX_SEGMENTS ISP"
    +",RDB$INDEX_SEGMENTS ISF "
    +"WHERE CAST(PK.RDB$RELATION_NAME AS VARCHAR(40)) = ? "
    +"and FK.RDB$CONSTRAINT_NAME = RC.RDB$CONSTRAINT_NAME "
    +"and PK.RDB$CONSTRAINT_NAME = RC.RDB$CONST_NAME_UQ "
    +"and ISP.RDB$INDEX_NAME = PK.RDB$INDEX_NAME "
    +"and ISF.RDB$INDEX_NAME = FK.RDB$INDEX_NAME "
    +"and ISP.RDB$FIELD_POSITION = ISF.RDB$FIELD_POSITION "
    +"order by 3, 5 ";

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

        xsqlvars[0] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "PKTABLE_CAT", "COLUMNINFO");
        xsqlvars[1] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "PKTABLE_SCHEM", "COLUMNINFO");
        xsqlvars[2] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "PKTABLE_NAME", "COLUMNINFO");
        xsqlvars[3] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "PKCOLUMN_NAME", "COLUMNINFO");
        xsqlvars[4] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "FKTABLE_CAT", "COLUMNINFO");
        xsqlvars[5] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "FKTABLE_SCHEM", "COLUMNINFO");
        xsqlvars[6] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "FKTABLE_NAME", "COLUMNINFO");
        xsqlvars[7] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "FKCOLUMN_NAME", "COLUMNINFO");
        xsqlvars[8] = new XSQLVAR(ISCConstants.SQL_SHORT, 0, "KEY_SEQ", "COLUMNINFO");
        xsqlvars[9] = new XSQLVAR(ISCConstants.SQL_SHORT, 0, "UPDATE_RULE", "COLUMNINFO");
        xsqlvars[10] = new XSQLVAR(ISCConstants.SQL_SHORT, 0, "DELETE_RULE", "COLUMNINFO");
        xsqlvars[11] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "FK_NAME", "COLUMNINFO");
        xsqlvars[12] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "PK_NAME", "COLUMNINFO");
        xsqlvars[13] = new XSQLVAR(ISCConstants.SQL_SHORT, 0, "DEFERRABILITY", "COLUMNINFO");

        Clause tableClause = new Clause("PK.RDB$RELATION_NAME", table);

        String sql = GET_EXPORTED_KEYS;

        // check the original case identifiers first
        ArrayList params = new ArrayList();
        params.add(stripQuotes(stripEscape(table), false));

        List<byte[][]> rows = new ArrayList<byte[][]>();
        ResultSet rs = doQuery(sql, params);
        
        // if nothing found, check the uppercased identifiers
        if (!rs.next()) {
            rs.close();
            params.clear();
            params.add(stripQuotes(stripEscape(table), true));
            
            rs = doQuery(sql, params);
            
            // if nothing found, return an empty result set
            if (!rs.next()) {
                rs.close();
                return new FBResultSet(xsqlvars, rows);
            }
        }

        do {
            byte[][] row = new byte[14][];
            row[0] = null;
            row[1] = null;
            row[2] = getBytes(rs.getString("PKTABLE_NAME"));
            row[3] = getBytes(rs.getString("PKCOLUMN_NAME"));
            row[4] = null;
            row[5] = null;
            row[6] = getBytes(rs.getString("FKTABLE_NAME"));
            row[7] = getBytes(rs.getString("FKCOLUMN_NAME"));
            row[8] = createShort(rs.getShort("KEY_SEQ"));
            String updateRule = rs.getString("UPDATE_RULE");
            row[9] = mapAction(updateRule);
            String deleteRule = rs.getString("DELETE_RULE");
            row[10] = mapAction(deleteRule);
            row[11] = getBytes(rs.getString("FK_NAME"));
            row[12] = getBytes(rs.getString("PK_NAME"));
            row[13] = IMPORTED_KEY_NOT_DEFERRABLE;

            rows.add(row);
        } while(rs.next());
        rs.close();
        return new FBResultSet(xsqlvars, rows);
    }

    private static final String GET_CROSS_KEYS = "select "
    /*+" null as PKTABLE_CAT "
    +" ,null as PKTABLE_SCHEM "*/
    +"cast(PK.RDB$RELATION_NAME as varchar(31)) as PKTABLE_NAME"
    +",cast(ISP.RDB$FIELD_NAME as varchar(31)) as PKCOLUMN_NAME"
    /*+" ,null as FKTABLE_CAT "
    +" ,null as FKTABLE_SCHEM "*/
    +",cast(FK.RDB$RELATION_NAME as varchar(31)) as FKTABLE_NAME"
    +",cast(ISF.RDB$FIELD_NAME as varchar(31)) as FKCOLUMN_NAME"
    +",CAST((ISP.RDB$FIELD_POSITION + 1) as SMALLINT) as KEY_SEQ"
    +",cast(RC.RDB$UPDATE_RULE as varchar(11)) as UPDATE_RULE"
    +",cast(RC.RDB$DELETE_RULE as varchar(11)) as DELETE_RULE"
    +",cast(PK.RDB$CONSTRAINT_NAME as varchar(31)) as PK_NAME"
    +",cast(FK.RDB$CONSTRAINT_NAME as varchar(31)) as FK_NAME"
    /*+" ,null as DEFERRABILITY "*/
    +" from "
    +"RDB$RELATION_CONSTRAINTS PK"
    +",RDB$RELATION_CONSTRAINTS FK"
    +",RDB$REF_CONSTRAINTS RC"
    +",RDB$INDEX_SEGMENTS ISP"
    +",RDB$INDEX_SEGMENTS ISF "
    +"WHERE CAST(PK.RDB$RELATION_NAME AS VARCHAR(40)) = ? and "
    +"CAST(FK.RDB$RELATION_NAME AS VARCHAR(40)) = ? and "
    +" FK.RDB$CONSTRAINT_NAME = RC.RDB$CONSTRAINT_NAME "
    +"and PK.RDB$CONSTRAINT_NAME = RC.RDB$CONST_NAME_UQ "
    +"and ISP.RDB$INDEX_NAME = PK.RDB$INDEX_NAME "
    +"and ISF.RDB$INDEX_NAME = FK.RDB$INDEX_NAME "
    +"and ISP.RDB$FIELD_POSITION = ISF.RDB$FIELD_POSITION "
    +"order by 3, 5 ";

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

        xsqlvars[0] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "PKTABLE_CAT", "COLUMNINFO");
        xsqlvars[1] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "PKTABLE_SCHEM", "COLUMNINFO");
        xsqlvars[2] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "PKTABLE_NAME", "COLUMNINFO");
        xsqlvars[3] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "PKCOLUMN_NAME", "COLUMNINFO");
        xsqlvars[4] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "FKTABLE_CAT", "COLUMNINFO");
        xsqlvars[5] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "FKTABLE_SCHEM", "COLUMNINFO");
        xsqlvars[6] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "FKTABLE_NAME", "COLUMNINFO");
        xsqlvars[7] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "FKCOLUMN_NAME", "COLUMNINFO");
        xsqlvars[8] = new XSQLVAR(ISCConstants.SQL_SHORT, 0, "KEY_SEQ", "COLUMNINFO");
        xsqlvars[9] = new XSQLVAR(ISCConstants.SQL_SHORT, 0, "UPDATE_RULE", "COLUMNINFO");
        xsqlvars[10] = new XSQLVAR(ISCConstants.SQL_SHORT, 0, "DELETE_RULE", "COLUMNINFO");
        xsqlvars[11] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "FK_NAME", "COLUMNINFO");
        xsqlvars[12] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "PK_NAME", "COLUMNINFO");
        xsqlvars[13] = new XSQLVAR(ISCConstants.SQL_SHORT, 0, "DEFERRABILITY", "COLUMNINFO");

        String sql = GET_CROSS_KEYS;
        
        List<String> params = new ArrayList<String>();
        
        // check the original case first
        params.add(stripQuotes(stripEscape(primaryTable), false));
        params.add(stripQuotes(stripEscape(foreignTable), false));

        ResultSet rs = doQuery(sql, params);

        // if nothing found, check the uppercased identifiers
        if (!rs.next()) {
            rs.close();
            params.clear();
            params.add(stripQuotes(stripEscape(primaryTable), true));
            params.add(stripQuotes(stripEscape(foreignTable), true));

            rs = doQuery(sql, params);
            
            // return empty result set if nothing found
            if (!rs.next()) {
                rs.close();
                return new FBResultSet(xsqlvars, Collections.<byte[][]>emptyList());
            }
        }

        List<byte[][]> rows = new ArrayList<byte[][]>();
        do {
            byte[][] row = new byte[14][];
            row[0] = null;
            row[1] = null;
            row[2] = getBytes(rs.getString("PKTABLE_NAME"));
            row[3] = getBytes(rs.getString("PKCOLUMN_NAME"));
            row[4] = null;
            row[5] = null;
            row[6] = getBytes(rs.getString("FKTABLE_NAME"));
            row[7] = getBytes(rs.getString("FKCOLUMN_NAME"));
            row[8] = createShort(rs.getShort("KEY_SEQ"));
            String updateRule = rs.getString("UPDATE_RULE");
            row[9] = mapAction(updateRule);
            String deleteRule = rs.getString("DELETE_RULE");
            row[10] = mapAction(deleteRule);
            row[11] = getBytes(rs.getString("FK_NAME"));
            row[12] = getBytes(rs.getString("PK_NAME"));
            row[13] = IMPORTED_KEY_NOT_DEFERRABLE;

            rows.add(row);
        } while(rs.next());
        rs.close();
        return new FBResultSet(xsqlvars, rows);
    }

    /**
     * Function to convert integer values to encoded byte arrays for shorts.
     *
     * @param value integer value to convert
     * @return encoded byte array representing the value
     */
    private static byte[] createShort(int value) {
        assert (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) : String.format("Value \"%d\" outside range of short", value);
        return XSQLVAR.shortToBytes((short) value);
    }

    /**
     * Function to convert integer values to encoded byte arrays for integers.
     *
     * @param value integer value to convert
     * @return encoded byte array representing the value
     */
    private static byte[] createInt(int value) {
        return XSQLVAR.intToBytes(value);
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
        XSQLVAR[] xsqlvars = new XSQLVAR[18];

        xsqlvars[0] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "TYPE_NAME", "TYPEINFO");
        xsqlvars[1] = new XSQLVAR(ISCConstants.SQL_SHORT, 0, "DATA_TYPE", "TYPEINFO");
        xsqlvars[2] = new XSQLVAR(ISCConstants.SQL_LONG, 0, "PRECISION", "TYPEINFO");
        xsqlvars[3] = new XSQLVAR(ISCConstants.SQL_VARYING, 1, "LITERAL_PREFIX", "TYPEINFO");
        xsqlvars[4] = new XSQLVAR(ISCConstants.SQL_VARYING, 1, "LITERAL_SUFFIX", "TYPEINFO");
        xsqlvars[5] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "CREATE_PARAMS", "TYPEINFO");
        xsqlvars[6] = new XSQLVAR(ISCConstants.SQL_SHORT, 0, "NULLABLE", "TYPEINFO");
        xsqlvars[7] = new XSQLVAR(ISCConstants.SQL_TEXT, 1, "CASE_SENSITIVE", "TYPEINFO");
        xsqlvars[8] = new XSQLVAR(ISCConstants.SQL_SHORT, 0, "SEARCHABLE", "TYPEINFO");
        xsqlvars[9] = new XSQLVAR(ISCConstants.SQL_TEXT, 1, "UNSIGNED_ATTRIBUTE", "TYPEINFO");
        xsqlvars[10] = new XSQLVAR(ISCConstants.SQL_TEXT, 1, "FIXED_PREC_SCALE", "TYPEINFO");
        xsqlvars[11] = new XSQLVAR(ISCConstants.SQL_TEXT, 1, "AUTO_INCREMENT", "TYPEINFO");
        xsqlvars[12] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "LOCAL_TYPE_NAME", "TYPEINFO");
        xsqlvars[13] = new XSQLVAR(ISCConstants.SQL_SHORT, 0, "MINIMUM_SCALE", "TYPEINFO");
        xsqlvars[14] = new XSQLVAR(ISCConstants.SQL_SHORT, 0, "MAXIMUM_SCALE", "TYPEINFO");
        xsqlvars[15] = new XSQLVAR(ISCConstants.SQL_LONG, 0, "SQL_DATA_TYPE", "TYPEINFO");
        xsqlvars[16] = new XSQLVAR(ISCConstants.SQL_LONG, 0, "SQL_DATETIME_SUB", "TYPEINFO");
        xsqlvars[17] = new XSQLVAR(ISCConstants.SQL_LONG, 0, "NUM_PREC_RADIX", "TYPEINFO");

        //dialect 3 only
        List<byte[][]> rows = new ArrayList<byte[][]>();

        //BIGINT=-5
        rows.add(new byte[][]{ getBytes("BIGINT"), createShort(Types.BIGINT), BIGINT_PRECISION, null, null, null,
                TYPE_NULLABLE, CASEINSENSITIVE, TYPE_SEARCHABLE, SIGNED, FIXEDSCALE, NOTAUTOINC, null, SHORT_ZERO,
                SHORT_ZERO, createInt(ISCConstants.SQL_INT64), null, RADIX_TEN });

        //LONGVARBINARY=-4
        rows.add(new byte[][]{ getBytes("BLOB SUB_TYPE 0"), createShort(Types.LONGVARBINARY), INT_ZERO, null, null,
                null, TYPE_NULLABLE, CASESENSITIVE, TYPE_PRED_NONE, UNSIGNED, FIXEDSCALE, NOTAUTOINC, null,
                SHORT_ZERO, SHORT_ZERO, createInt(ISCConstants.SQL_BLOB), null, RADIX_TEN });

        //LONGVARCHAR=-1
        rows.add(new byte[][]{ getBytes("BLOB SUB_TYPE 1"), createShort(Types.LONGVARCHAR), INT_ZERO, null, null,
                null, TYPE_NULLABLE, CASESENSITIVE, TYPE_PRED_NONE, UNSIGNED, FIXEDSCALE, NOTAUTOINC, null,
                SHORT_ZERO, SHORT_ZERO, createInt(ISCConstants.SQL_BLOB), null, RADIX_TEN });

        //CHAR=1
        rows.add(new byte[][]{ getBytes("CHAR"), createShort(Types.CHAR), createInt(32767), getBytes("'"),
                getBytes("'"), getBytes("length"), TYPE_NULLABLE, CASESENSITIVE, TYPE_SEARCHABLE, UNSIGNED,
                FIXEDSCALE, NOTAUTOINC, null, SHORT_ZERO, SHORT_ZERO, createInt(ISCConstants.SQL_TEXT), null,
                RADIX_TEN });

        //NUMERIC=2
        rows.add(new byte[][]{ getBytes("NUMERIC"), createShort(Types.NUMERIC), NUMERIC_PRECISION, null, null,
                getBytes("precision,scale"), TYPE_NULLABLE, CASEINSENSITIVE, TYPE_SEARCHABLE, SIGNED, FIXEDSCALE,
                NOTAUTOINC, null, SHORT_ZERO, NUMERIC_PRECISION, createInt(ISCConstants.SQL_INT64), null, RADIX_TEN });

        //DECIMAL=3
        rows.add(new byte[][]{ getBytes("DECIMAL"), createShort(Types.DECIMAL), DECIMAL_PRECISION, null, null,
                getBytes("precision,scale"), TYPE_NULLABLE, CASEINSENSITIVE, TYPE_SEARCHABLE, SIGNED, FIXEDSCALE,
                NOTAUTOINC, null, SHORT_ZERO, DECIMAL_PRECISION, createInt(ISCConstants.SQL_INT64), null, RADIX_TEN });

        //INTEGER=4
        rows.add(new byte[][]{ getBytes("INTEGER"), createShort(Types.INTEGER), INTEGER_PRECISION, null, null, null,
                TYPE_NULLABLE, CASEINSENSITIVE, TYPE_SEARCHABLE, SIGNED, FIXEDSCALE, NOTAUTOINC, null, SHORT_ZERO,
                SHORT_ZERO, createInt(ISCConstants.SQL_LONG), null, RADIX_TEN });

        //SMALLINT=5
        rows.add(new byte[][]{ getBytes("SMALLINT"), createShort(Types.SMALLINT), SMALLINT_PRECISION, null, null,
                null, TYPE_NULLABLE, CASEINSENSITIVE, TYPE_SEARCHABLE, SIGNED, FIXEDSCALE, NOTAUTOINC, null,
                SHORT_ZERO, SHORT_ZERO, createInt(ISCConstants.SQL_SHORT), null, RADIX_TEN });

        //FLOAT=6
        rows.add(new byte[][]{ getBytes("FLOAT"), createShort(Types.FLOAT), FLOAT_PRECISION, null, null, null,
                TYPE_NULLABLE, CASEINSENSITIVE, TYPE_SEARCHABLE, SIGNED, VARIABLESCALE, NOTAUTOINC, null, SHORT_ZERO,
                SHORT_ZERO, createInt(ISCConstants.SQL_FLOAT), null, RADIX_TEN });

        //DOUBLE=8
        rows.add(new byte[][]{ getBytes("DOUBLE PRECISION"), createShort(Types.DOUBLE), DOUBLE_PRECISION, null, null,
                null, TYPE_NULLABLE, CASEINSENSITIVE, TYPE_SEARCHABLE, SIGNED, VARIABLESCALE, NOTAUTOINC, null,
                SHORT_ZERO, SHORT_ZERO, createInt(ISCConstants.SQL_DOUBLE), null, RADIX_TEN });

        //VARCHAR=12
        rows.add(new byte[][]{ getBytes("VARCHAR"), createShort(Types.VARCHAR), createInt(32765), getBytes("'"),
                getBytes("'"), getBytes("length"), TYPE_NULLABLE, CASESENSITIVE, TYPE_SEARCHABLE, UNSIGNED,
                FIXEDSCALE, NOTAUTOINC, null, SHORT_ZERO, SHORT_ZERO, createInt(ISCConstants.SQL_VARYING), null,
                RADIX_TEN });

        //DATE=91
        rows.add(new byte[][]{ getBytes("DATE"), createShort(Types.DATE), DATE_PRECISION, null, null, null,
                TYPE_NULLABLE, CASEINSENSITIVE, TYPE_SEARCHABLE, UNSIGNED, FIXEDSCALE, NOTAUTOINC, null, SHORT_ZERO,
                SHORT_ZERO, createInt(ISCConstants.SQL_TYPE_DATE), null, RADIX_TEN });

        //TIME=92
        rows.add(new byte[][]{ getBytes("TIME"), createShort(Types.TIME), TIME_PRECISION, null, null, null,
                TYPE_NULLABLE, CASEINSENSITIVE, TYPE_SEARCHABLE, UNSIGNED, FIXEDSCALE, NOTAUTOINC, null, SHORT_ZERO,
                SHORT_ZERO, createInt(ISCConstants.SQL_TYPE_TIME), null, RADIX_TEN });

        //TIMESTAMP=93
        rows.add(new byte[][]{ getBytes("TIMESTAMP"), createShort(Types.TIMESTAMP), TIMESTAMP_PRECISION, null, null,
                null, TYPE_NULLABLE, CASEINSENSITIVE, TYPE_SEARCHABLE, UNSIGNED, FIXEDSCALE, NOTAUTOINC, null,
                SHORT_ZERO, SHORT_ZERO, createInt(ISCConstants.SQL_TIMESTAMP), null, RADIX_TEN });

        //OTHER=1111
        rows.add(new byte[][]{ getBytes("ARRAY"), createShort(Types.OTHER), INT_ZERO, null, null, null, TYPE_NULLABLE,
                CASESENSITIVE, TYPE_PRED_NONE, UNSIGNED, FIXEDSCALE, NOTAUTOINC, null, SHORT_ZERO, SHORT_ZERO,
                createInt(ISCConstants.SQL_ARRAY), null, RADIX_TEN });

        //BLOB=2004
        rows.add(new byte[][]{ getBytes("BLOB SUB_TYPE <0 "), createShort(Types.BLOB), INT_ZERO, null, null, null,
                TYPE_NULLABLE, CASESENSITIVE, TYPE_PRED_NONE, UNSIGNED, FIXEDSCALE, NOTAUTOINC, null, SHORT_ZERO,
                SHORT_ZERO, createInt(ISCConstants.SQL_BLOB), null, RADIX_TEN });

        //BOOLEAN=16
        if (getDatabaseMajorVersion() >= 3) {
            rows.add(new byte[][] {getBytes("BOOLEAN"), createShort(Types.BOOLEAN), BOOLEAN_PRECISION,
                    null, null, null, TYPE_NULLABLE, CASEINSENSITIVE, TYPE_PRED_BASIC, UNSIGNED, FIXEDSCALE,
                    NOTAUTOINC, null, SHORT_ZERO, SHORT_ZERO, createInt(ISCConstants.SQL_BOOLEAN), null, RADIX_BINARY});
        }

        return new FBResultSet(xsqlvars, rows);
    }

    private static final String GET_INDEX_INFO = "SELECT "
        + "cast(ind.RDB$RELATION_NAME as varchar(31)) AS TABLE_NAME"
        + ",ind.RDB$UNIQUE_FLAG AS UNIQUE_FLAG"
        + ",cast(ind.RDB$INDEX_NAME as varchar(31)) as INDEX_NAME"
        + ",ise.rdb$field_position + 1 as ORDINAL_POSITION"
        + ",cast(ise.rdb$field_name as varchar(31)) as COLUMN_NAME"
        + ",ind.RDB$EXPRESSION_SOURCE as EXPRESSION_SOURCE"
        + ",ind.RDB$INDEX_TYPE as ASC_OR_DESC "
        + "FROM "
        + "rdb$indices ind "
        + "LEFT JOIN rdb$index_segments ise ON ind.rdb$index_name = ise.rdb$index_name "
        + "WHERE "
        + "CAST(ind.rdb$relation_name AS VARCHAR(40)) = ? "
        + "ORDER BY 2, 3, 4";

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

        xsqlvars[0] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "TABLE_CAT", "INDEXINFO");
        xsqlvars[1] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "TABLE_SCHEM", "INDEXINFO");
        xsqlvars[2] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "TABLE_NAME", "INDEXINFO");
        xsqlvars[3] = new XSQLVAR(ISCConstants.SQL_TEXT, 1, "NON_UNIQUE", "INDEXINFO");
        xsqlvars[4] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "INDEX_QUALIFIER", "INDEXINFO");
        xsqlvars[5] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "INDEX_NAME", "INDEXINFO");
        xsqlvars[6] = new XSQLVAR(ISCConstants.SQL_SHORT, 0, "TYPE", "INDEXINFO");
        xsqlvars[7] = new XSQLVAR(ISCConstants.SQL_SHORT, 0, "ORDINAL_POSITION", "INDEXINFO");
        xsqlvars[8] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "COLUMN_NAME", "INDEXINFO");
        xsqlvars[9] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "ASC_OR_DESC", "INDEXINFO");
        xsqlvars[10] = new XSQLVAR(ISCConstants.SQL_LONG, 0, "CARDINALITY", "INDEXINFO");
        xsqlvars[11] = new XSQLVAR(ISCConstants.SQL_LONG, 0, "PAGES", "INDEXINFO");
        xsqlvars[12] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "FILTER_CONDITION", "INDEXINFO");

        if (table == null) {
            return new FBResultSet(xsqlvars, Collections.<byte[][]>emptyList());
        }

        List<String> params = new ArrayList<String>();
        params.add(stripQuotes(stripEscape(table), false));

        ResultSet rs = doQuery(GET_INDEX_INFO, params);

        // if no direct match happened, check the uppercased match
        if (!rs.next()) {
            rs.close();
            params.set(0, stripQuotes(stripEscape(table), true));
            rs = doQuery(GET_INDEX_INFO, params);

            // open the second result set and check whether we have rows
            // if no rows are available, we have to exit now, otherwise the 
            // following do/while loop will throw SQLException that the
            // result set is not positioned on a row
            if (!rs.next()) {
                rs.close();
                return new FBResultSet(xsqlvars, Collections.<byte[][]>emptyList());
            }
        }

        List<byte[][]> rows = new ArrayList<byte[][]>();
        
        do {
            byte[][] row = new byte[13][];
            row[0] = null;
            row[1] = null;
            row[2] = getBytes(rs.getString("TABLE_NAME"));
            boolean isNotUnique = (rs.getInt("UNIQUE_FLAG") == 0);
            if (unique && isNotUnique) {
                // Skip indices that are not unique, as requested
                continue;
            }
            row[3] = isNotUnique ? TRUE_BYTES : FALSE_BYTES;
            row[4] = null;
            row[5] = getBytes(rs.getString("INDEX_NAME"));
            row[6] = TABLE_INDEX_OTHER;
            String columnName = rs.getString("COLUMN_NAME");
            if (rs.wasNull()) {
                row[7] = SHORT_ONE;
                String expressionSource = rs.getString("EXPRESSION_SOURCE");
                if (expressionSource != null) {
                    row[8] = getBytes(expressionSource);
                    if (expressionSource.length() > xsqlvars[8].sqllen) {
                        xsqlvars[8].sqllen = expressionSource.length();
                    }
                } else {
                    row[8] = null;
                }
            } else {
                row[7] = createShort(rs.getShort("ORDINAL_POSITION"));
                row[8] = getBytes(columnName);
            }
            int ascOrDesc = rs.getInt("ASC_OR_DESC");
            if (ascOrDesc == 0) {
                row[9] = ASC_BYTES;
            } else if (ascOrDesc == 1) {
                row[9] = DESC_BYTES;
            } else {
                row[9] = null;
            }
            // NOTE: We are setting CARDINALITY and PAGES to NULL as we don't have this info; might contravene JDBC spec
            // TODO use 1 / RDB$STATISTICS for approximation of CARDINALITY?
            row[10] = null;
            // TODO query RDB$PAGES for PAGES information?
            row[11] = null;
            row[12] = null;

            rows.add(row);
            
        } while (rs.next());
        rs.close();
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
     * UDTs are not supported by Firebird. This method will always return an empty ResultSet.
     *
     * {@inheritDoc}
     */
    public ResultSet getUDTs(String catalog, String schemaPattern,
              String typeNamePattern, int[] types) throws SQLException {
        XSQLVAR[] xsqlvars = new XSQLVAR[7];

        xsqlvars[0] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "TYPE_CAT", "UDT");
        xsqlvars[1] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "TYPE_SCHEM", "UDT");
        xsqlvars[2] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "TYPE_NAME", "UDT");
        xsqlvars[3] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "CLASS_NAME", "UDT");
        xsqlvars[4] = new XSQLVAR(ISCConstants.SQL_LONG, 0, "DATA_TYPE", "UDT");
        xsqlvars[5] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "REMARKS", "UDT");
        xsqlvars[6] = new XSQLVAR(ISCConstants.SQL_SHORT, 0, "BASE_TYPE", "UDT");

        return new FBResultSet(xsqlvars, Collections.<byte[][]>emptyList());
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
     * UDTs are not supported by Firebird. This method will always return an empty ResultSet.
     *
     * {@inheritDoc}
     */
    public ResultSet getAttributes(String catalog, String schemaPattern, String typeNamePattern, String attributeNamePattern) throws SQLException {
        XSQLVAR[] xsqlvars = new XSQLVAR[21];

        xsqlvars[0] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "TYPE_CAT", "ATTRIBUTES");
        xsqlvars[1] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "TYPE_SCHEM", "ATTRIBUTES");
        xsqlvars[2] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "TYPE_NAME", "ATTRIBUTES");
        xsqlvars[3] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "ATTR_NAME", "ATTRIBUTES");
        xsqlvars[4] = new XSQLVAR(ISCConstants.SQL_LONG, 0, "DATA_TYPE", "ATTRIBUTES");
        xsqlvars[5] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "ATTR_TYPE_NAME", "ATTRIBUTES");
        xsqlvars[6] = new XSQLVAR(ISCConstants.SQL_LONG, 0, "ATTR_SIZE", "ATTRIBUTES");
        xsqlvars[7] = new XSQLVAR(ISCConstants.SQL_LONG, 0, "DECIMAL_DIGITS", "ATTRIBUTES");
        xsqlvars[8] = new XSQLVAR(ISCConstants.SQL_LONG, 0, "NUM_PREC_RADIX", "ATTRIBUTES");
        xsqlvars[9] = new XSQLVAR(ISCConstants.SQL_LONG, 0, "NULLABLE", "ATTRIBUTES");
        xsqlvars[10] = new XSQLVAR(ISCConstants.SQL_VARYING, 80, "REMARKS", "ATTRIBUTES");
        xsqlvars[11] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "ATTR_DEF", "ATTRIBUTES");
        xsqlvars[12] = new XSQLVAR(ISCConstants.SQL_LONG, 0, "SQL_DATA_TYPE", "ATTRIBUTES");
        xsqlvars[13] = new XSQLVAR(ISCConstants.SQL_LONG, 0, "SQL_DATETIME_SUB", "ATTRIBUTES");
        xsqlvars[14] = new XSQLVAR(ISCConstants.SQL_LONG, 0, "CHAR_OCTET_LENGTH", "ATTRIBUTES");
        xsqlvars[15] = new XSQLVAR(ISCConstants.SQL_SHORT, 0, "ORDINAL_POSITION", "ATTRIBUTES");
        xsqlvars[16] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "IS_NULLABLE", "ATTRIBUTES");
        xsqlvars[17] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "SCOPE_CATALOG", "ATTRIBUTES");
        xsqlvars[18] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "SCOPE_SCHEMA", "ATTRIBUTES");
        xsqlvars[19] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "SCOPE_TABLE", "ATTRIBUTES");
        xsqlvars[20] = new XSQLVAR(ISCConstants.SQL_SHORT, 0, "SOURCE_DATA_TYPE", "ATTRIBUTES");

        return new FBResultSet(xsqlvars, Collections.<byte[][]>emptyList());
    }

    /**
     * Retrieves whether this database supports savepoints.
     *
     * @return true if savepoints are supported; false otherwise 
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsSavepoints() throws SQLException {
        return gdsHelper.compareToVersion(1, 5) >= 0;
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
        return true;
    }

    /**
     * Supertypes are not supported by Firebird. This method will always return an empty ResultSet.
     *
     * {@inheritDoc}
     */
    public ResultSet getSuperTypes(String catalog, String schemaPattern, String tableNamePattern) throws SQLException {
        XSQLVAR[] xsqlvars = new XSQLVAR[6];

        xsqlvars[0] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "TYPE_CAT", "SUPERTYPES");
        xsqlvars[1] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "TYPE_SCHEM", "SUPERTYPES");
        xsqlvars[2] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "TYPE_NAME", "SUPERTYPES");
        xsqlvars[3] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "SUPERTYPE_CAT", "SUPERTYPES");
        xsqlvars[4] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "SUPERTYPE_SCHEM", "SUPERTYPES");
        xsqlvars[5] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "SUPERTYPE_NAME", "SUPERTYPES");

        List<byte[][]> rows = Collections.emptyList();

        return new FBResultSet(xsqlvars, rows);
    }

    /**
     * Supertables are not supported by Firebird. This method will always return an empty ResultSet.
     *
     * {@inheritDoc}
     */
    public ResultSet getSuperTables(String catalog, String schemaPattern, String tableNamePattern) throws SQLException {
        
        XSQLVAR[] xsqlvars = new XSQLVAR[4];

        xsqlvars[0] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "TABLE_CAT", "SUPERTABLES");
        xsqlvars[1] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "TABLE_SCHEM", "SUPERTABLES");
        xsqlvars[2] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "TABLE_NAME", "SUPERTABLES");
        xsqlvars[3] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "SUPERTABLE_NAME", "SUPERTABLES");

        List<byte[][]> rows = Collections.emptyList();

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
        return holdability == ResultSet.CLOSE_CURSORS_AT_COMMIT ||
            holdability == ResultSet.HOLD_CURSORS_OVER_COMMIT;
    }

    /**
     * Retrieves the default holdability of this <code>ResultSet</code>.
     *
     * @return the default holdability
     * @exception SQLException if a database access error occurs 
     */
    public int getResultSetHoldability() throws SQLException {
        return ResultSet.CLOSE_CURSORS_AT_COMMIT; 
    }

    /**
     * Get the major version number of the database.
     *
     * @return The major version number 
     * @exception SQLException if a database access error occurs 
     */
    public int getDatabaseMajorVersion() throws SQLException {
        return gdsHelper.getDatabaseProductMajorVersion();
    }

    /**
     * Get the minor version number of the database.
     * @return The minor version number 
     * @exception SQLException if a database access error occurs 
     */
    public int getDatabaseMinorVersion() throws SQLException {
        return gdsHelper.getDatabaseProductMinorVersion();
    }
    
    /**
     * Get the major version of the ODS (On-Disk Structure) of the database.
     * @return The major version number
     * @exception SQLException if a database access error occurs
     */
    public int getOdsMajorVersion() throws SQLException {
    	return gdsHelper.getIscDBHandle().getODSMajorVersion();
    }
    
    /**
     * Get the minor version of the ODS (On-Disk Structure) of the database.
     * @return The minor version number
     * @exception SQLException if a database access error occurs
     */
    public int getOdsMinorVersion() throws SQLException {
    	return gdsHelper.getIscDBHandle().getODSMinorVersion();
    }

    /**
     * Indicates whether the SQLSTATEs returned by SQLException.getSQLState is 
     * X/Open (now known as Open Group) SQL CLI or SQL99
     *
     * @return the type of SQLSTATEs
     * @exception SQLException should never be thrown in this implementation 
     */
    public int getSQLStateType() throws SQLException {
        return DatabaseMetaData.sqlStateSQL99;
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
        // TODO Return context info?
        XSQLVAR[] xsqlvars = new XSQLVAR[4];

        xsqlvars[0] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "NAME", "CLIENTINFO");
        xsqlvars[1] = new XSQLVAR(ISCConstants.SQL_LONG, 4, "MAX_LEN", "CLIENTINFO");
        xsqlvars[2] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "DEFAULT", "CLIENTINFO");
        xsqlvars[3] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "DESCRIPTION", "CLIENTINFO");

        List<byte[][]> rows = Collections.emptyList();

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
        // FIXME implement this method to return actual result
        XSQLVAR[] xsqlvars = new XSQLVAR[17];

        xsqlvars[0] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "FUNCTION_CAT", "FUNCTION_COLUMNS");
        xsqlvars[1] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "FUNCTION_SCHEM", "FUNCTION_COLUMNS");
        xsqlvars[2] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "FUNCTION_NAME", "FUNCTION_COLUMNS");
        xsqlvars[3] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "COLUMN_NAME", "FUNCTION_COLUMNS");
        xsqlvars[4] = new XSQLVAR(ISCConstants.SQL_SHORT, 0, "COLUMN_TYPE", "FUNCTION_COLUMNS");
        xsqlvars[5] = new XSQLVAR(ISCConstants.SQL_LONG, 0, "DATA_TYPE", "FUNCTION_COLUMNS");
        xsqlvars[6] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "TYPE_NAME", "FUNCTION_COLUMNS");
        xsqlvars[7] = new XSQLVAR(ISCConstants.SQL_LONG, 0, "PRECISION", "FUNCTION_COLUMNS");
        xsqlvars[8] = new XSQLVAR(ISCConstants.SQL_LONG, 0, "LENGTH", "FUNCTION_COLUMNS");
        xsqlvars[9] = new XSQLVAR(ISCConstants.SQL_SHORT, 0, "SCALE", "FUNCTION_COLUMNS");
        xsqlvars[10] = new XSQLVAR(ISCConstants.SQL_SHORT, 0, "RADIX", "FUNCTION_COLUMNS");
        xsqlvars[11] = new XSQLVAR(ISCConstants.SQL_SHORT, 0, "NULLABLE", "FUNCTION_COLUMNS");
        xsqlvars[12] = new XSQLVAR(ISCConstants.SQL_VARYING, 80, "REMARKS", "FUNCTION_COLUMNS");
        xsqlvars[13] = new XSQLVAR(ISCConstants.SQL_LONG, 0, "CHAR_OCTET_LENGTH", "FUNCTION_COLUMNS");
        xsqlvars[14] = new XSQLVAR(ISCConstants.SQL_LONG, 0, "ORDINAL_POSITION", "FUNCTION_COLUMNS");
        xsqlvars[15] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "IS_NULLABLE", "FUNCTION_COLUMNS");
        xsqlvars[16] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "SPECIFIC_NAME", "FUNCTION_COLUMNS");

        return new FBResultSet(xsqlvars, Collections.<byte[][]>emptyList());
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
        // FIXME implement this method to return actual result
        XSQLVAR[] xsqlvars = new XSQLVAR[6];

        xsqlvars[0] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "FUNCTION_CAT", "FUNCTIONS");
        xsqlvars[1] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "FUNCTION_SCHEM", "FUNCTIONS");
        xsqlvars[2] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "FUNCTION_NAME", "FUNCTIONS");
        xsqlvars[3] = new XSQLVAR(ISCConstants.SQL_VARYING, 80, "REMARKS", "FUNCTIONS");
        xsqlvars[4] = new XSQLVAR(ISCConstants.SQL_SHORT, 0, "FUNCTION_TYPE", "FUNCTIONS");
        xsqlvars[5] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "SPECIFIC_NAME", "FUNCTIONS");

        return new FBResultSet(xsqlvars, Collections.<byte[][]>emptyList());
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

        xsqlvars[0] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "TABLE_SCHEM", "TABLESCHEMAS");
        xsqlvars[1] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "TABLE_CATALOG", "TABLESCHEMAS");

        List<byte[][]> rows = Collections.emptyList();

        return new FBResultSet(xsqlvars, rows);
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface != null && iface.isAssignableFrom(FBDatabaseMetaData.class);
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (!isWrapperFor(iface))
            throw new SQLException("Unable to unwrap to class " + iface.getName());
        
        return iface.cast(this);
    }    
    
    protected static boolean isAllCondition(String pattern) {
        return "%".equals(pattern);
    }

    /**
     * Determine if there are no SQL wildcard characters ('%' or '_') in the 
     * given pattern.
     *
     * @param pattern The pattern to be checked for wildcards
     * @return <code>true</code> if there are no wildcards in the pattern, 
     *         <code>false</code> otherwise
     */
    public static boolean hasNoWildcards(String pattern) {
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
    public static String stripEscape(String pattern) {
        if (pattern == null) return null;
        StringBuilder stripped = new StringBuilder(pattern.length());
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
    public static String stripQuotes(String pattern, boolean uppercase) {
        if (pattern == null) return null;
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
    
    public ResultSet getPseudoColumns(String catalog, String schemaPattern,
            String tableNamePattern, String columnNamePattern) throws SQLException {
        // TODO Write implementation
        throw new FBDriverNotCapableException();
    }

    public boolean generatedKeyAlwaysReturned() throws SQLException {
        // TODO Double check if this is correct
        return false;
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
        List<String> params = new ArrayList<String>();
        params.add(procedureName);
        ResultSet rs = doQuery(sql, params);
        if (rs.next()) sResult = rs.getString(1);
        rs.close();

        return sResult;
    } 

    /*
     * (non-Javadoc)
     * 
     * @see org.firebirdsql.jdbc.FirebirdDatabaseMetaData#getTriggerSourceCode(java.lang.String)
     */
    public String getTriggerSourceCode(String triggerName) throws SQLException {
        String sResult = null;
        String sql = "Select RDB$TRIGGER_SOURCE From RDB$TRIGGERS Where "
                + "RDB$TRIGGER_NAME = ?";
        List<String> params = new ArrayList<String>();
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
        List<String> params = new ArrayList<String>();
        params.add(viewName);
        ResultSet rs = doQuery(sql, params);
        if (rs.next()) sResult = rs.getString(1);
        rs.close();

        return sResult;
    }

    protected void checkCatalogAndSchema(String catalog, String schema) throws SQLException {
        /* 
         * we ignore incorrect catalog and schema specification as 
         * suggested by Thomas Kellerer in JDBC Forum 
        */
    }

    protected static final class Clause {
        private final String condition;
        private final String value;
        private final String originalCaseValue;

        public Clause (String columnName, String pattern) {
            if (pattern == null || isAllCondition(pattern)) {
                condition = "";
                value = null;
                originalCaseValue = null;
            } else if (hasNoWildcards(pattern)) {
                value = stripQuotes(stripEscape(pattern), true);
                originalCaseValue = stripQuotes(stripEscape(pattern), false);
                condition = "CAST(" + columnName + " AS VARCHAR(40)) = ? and ";
            } else {
                // We are padding the column with 31 spaces to accommodate arguments longer than the actual column length.
                // The argument itself is padded with 15 spaces and a % to prevent false positives, this allows 15 character longer patterns
                value = stripQuotes(pattern, true) + SPACES_15 + "%";
                originalCaseValue = stripQuotes(pattern, false) + SPACES_15 + "%";
                condition = columnName + " || '" + SPACES_31 + "' like ? escape '\\' and ";
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

    protected static byte[] getBytes(String value){
        return value != null ? value.getBytes(): null;
    }
    
    private FBPreparedStatement getStatement(String sql) throws SQLException {
        FBPreparedStatement s = statements.get(sql);
        
        if (s != null && s.isClosed()) {
            statements.remove(sql);
            s = null;
        }
        
        if (s != null) 
            return s;
        
        if (connection == null) {
            InternalTransactionCoordinator.MetaDataTransactionCoordinator metaDataTransactionCoordinator = 
                new InternalTransactionCoordinator.MetaDataTransactionCoordinator();
            
            s = new FBPreparedStatement(gdsHelper, sql,
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY,
                    ResultSet.CLOSE_CURSORS_AT_COMMIT, 
                    metaDataTransactionCoordinator, metaDataTransactionCoordinator,
                    true, true, false);
        } else {
            s = (FBPreparedStatement)connection.prepareMetaDataStatement(
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
    protected ResultSet doQuery(String sql, List<String> params)
            throws SQLException {
        
        FBPreparedStatement s = getStatement(sql);
        
        for (int i = 0; i < params.size(); i++)
            s.setStringForced(i + 1, params.get(i));

        return s.executeMetaDataQuery();
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
        return RowIdLifetime.ROWID_UNSUPPORTED;
    }

    private static final int JDBC_MAJOR_VERSION = 4;
    private static final int JDBC_MINOR_VERSION;
    static {
        // JDK 1.6 (or lower): JDBC 4.0
        int tempVersion = 0;
        try {
            String javaImplementation = getSystemPropertyPrivileged("java.specification.version");
            if (javaImplementation != null && "1.7".compareTo(javaImplementation) <= 0) {
                // JDK 1.7 or higher: JDBC 4.1
                tempVersion = 1;
            }
        } catch (RuntimeException ex) {
            // default to 0 (JDBC 4.0) when privileged call fails
        }
        JDBC_MINOR_VERSION = tempVersion;
    }

    public int getJDBCMajorVersion() {
        return JDBC_MAJOR_VERSION;
    }

    public int getJDBCMinorVersion() {
        return JDBC_MINOR_VERSION;
    }
    
    private static String getSystemPropertyPrivileged(final String propertyName) {
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
           public String run() {
               return System.getProperty(propertyName);
           } 
        });
    }
}
