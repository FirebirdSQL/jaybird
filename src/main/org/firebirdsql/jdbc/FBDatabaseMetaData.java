/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.jdbc;

import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.ng.DatatypeCoder;
import org.firebirdsql.gds.ng.DefaultDatatypeCoder;
import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.fields.RowDescriptorBuilder;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.fields.RowValueBuilder;
import org.firebirdsql.jca.FBManagedConnectionFactory;
import org.firebirdsql.jdbc.escape.FBEscapedFunctionHelper;
import org.firebirdsql.jdbc.field.JdbcTypeConverter;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;
import org.firebirdsql.util.FirebirdSupportInfo;

import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.*;
import java.util.*;

import static org.firebirdsql.gds.ISCConstants.*;
import static org.firebirdsql.util.FirebirdSupportInfo.supportInfoFor;

/**
 * Comprehensive information about the database as a whole.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
@SuppressWarnings("RedundantThrows")
public class FBDatabaseMetaData implements FirebirdDatabaseMetaData {

    private final static Logger log = LoggerFactory.getLogger(FBDatabaseMetaData.class);
    private static final int OBJECT_NAME_LENGTH_BEFORE_V4_0 = 31;
    private static final int OBJECT_NAME_LENGTH_V4_0 = 63;
    private static final int OBJECT_NAME_LENGTH = OBJECT_NAME_LENGTH_V4_0;
    // Extra space to allow for longer patterns (avoids string right truncation errors)
    static final int OBJECT_NAME_PARAMETER_LENGTH = OBJECT_NAME_LENGTH + 10;
    private static final String OBJECT_NAME_TYPE = "varchar(" + OBJECT_NAME_LENGTH + ")";
    private static final String OBJECT_NAME_PARAMETER = "cast(? as varchar(" + OBJECT_NAME_PARAMETER_LENGTH + ")) ";

    private static final int DRIVER_MAJOR_VERSION = 4;
    private static final int DRIVER_MINOR_VERSION = 0;
    private static final String DRIVER_VERSION = DRIVER_MAJOR_VERSION + "." + DRIVER_MINOR_VERSION;

    private static final int SUBTYPE_NUMERIC = 1;
    private static final int SUBTYPE_DECIMAL = 2;

    protected static final DatatypeCoder datatypeCoder =
            DefaultDatatypeCoder.forEncodingFactory(EncodingFactory.createInstance(StandardCharsets.UTF_8));

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
    private static final byte[] INT_ZERO = createInt(0);
    private static final byte[] SHORT_ZERO = createShort(0);
    private static final byte[] SHORT_ONE = createShort(1);
    private static final byte[] RADIX_BINARY = createInt(2);
    private static final byte[] RADIX_TEN = createInt(10);
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
    private static final byte[] BOOLEAN_PRECISION = createInt(1);
    private static final byte[] DECFLOAT_16_PRECISION = createInt(16);
    private static final byte[] DECFLOAT_34_PRECISION = createInt(34);
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
    private final FirebirdSupportInfo firebirdSupportInfo;

    private static final int STATEMENT_CACHE_SIZE = 12;
    private final Map<String, FBPreparedStatement> statements = new LruPreparedStatementCache(STATEMENT_CACHE_SIZE);
    private final FirebirdVersionMetaData versionMetaData;

    protected FBDatabaseMetaData(FBConnection c) throws SQLException {
        this.gdsHelper = c.getGDSHelper();
        this.connection = c;
        firebirdSupportInfo = supportInfoFor(c);
        versionMetaData = FirebirdVersionMetaData.getVersionMetaDataFor(c);
    }

    @Override
    public void close() {
        synchronized (statements) {
            if (statements.isEmpty()) {
                return;
            }
            try {
                for (FBStatement stmt : statements.values()) {
                    try {
                        stmt.close();
                    } catch (Exception e) {
                        log.warn("error closing cached statements in DatabaseMetaData.close; "
                                + "see debug level for stacktrace");
                        log.debug("error closing cached statements in DatabaseMetaData.close", e);
                    }
                }
            } finally {
                statements.clear();
            }
        }
    }

    @Override
    public boolean allProceduresAreCallable() throws SQLException {
        //returns all procedures whether or not you have execute permission
        return false;
    }

    @Override
    public boolean allTablesAreSelectable() throws SQLException {
        //returns all tables matching criteria independent of access permissions.
        return false;
    }

    @Override
    public String getURL() throws SQLException {
        // TODO Think of a less complex way to obtain the url or just return null?
        GDSType gdsType = ((FBManagedConnectionFactory) connection.mc.getManagedConnectionFactory()).getGDSType();
        return GDSFactory.getJdbcUrl(gdsType, connection.mc.getDatabase());
    }

    @Override
    public String getUserName() throws SQLException {
        return gdsHelper.getUserName();
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return false;//could be true, not yetimplemented
    }

    @Override
    public boolean nullsAreSortedHigh() throws SQLException {
        // in Firebird 1.5.x NULLs are always sorted at the end
        // in Firebird 2.0.x NULLs are sorted low
        return false;
    }

    @Override
    public boolean nullsAreSortedLow() throws SQLException {
        // in Firebird 1.5.x NULLs are always sorted at the end
        // in Firebird 2.0.x NULLs are sorted low
        return gdsHelper.compareToVersion(2, 0) >= 0;
    }

    @Override
    public boolean nullsAreSortedAtStart() throws SQLException {
        // in Firebird 1.5.x NULLs are always sorted at the end
        // in Firebird 2.0.x NULLs are sorted low
        return false;
    }

    @Override
    public boolean nullsAreSortedAtEnd() throws SQLException {
        // in Firebird 1.5.x NULLs are always sorted at the end
        // in Firebird 2.0.x NULLs are sorted low
        return gdsHelper.compareToVersion(2, 0) < 0;
    }

    @Override
    public String getDatabaseProductName() throws SQLException {
        return gdsHelper.getDatabaseProductName();
    }

    @Override
    public String getDatabaseProductVersion() throws SQLException {
        return gdsHelper.getDatabaseProductVersion();
    }

    @Override
    public String getDriverName() throws SQLException {
        return "Jaybird JCA/JDBC driver";
    }

    @Override
    public String getDriverVersion() throws SQLException {
        // TODO Return full version?
        return DRIVER_VERSION;
    }

    @Override
    public int getDriverMajorVersion() {
        return DRIVER_MAJOR_VERSION;
    }

    @Override
    public int getDriverMinorVersion() {
        return DRIVER_MINOR_VERSION;
    }

    @Override
    public boolean usesLocalFiles() throws SQLException {
        return false;
    }

    @Override
    public boolean usesLocalFilePerTable() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsMixedCaseIdentifiers() throws SQLException {
        return false;
    }

    // TODO implement statement pooling on the server.. then in the driver
    @Override
    public boolean supportsStatementPooling() throws SQLException {
        return false;
    }

    @Override
    public boolean locatorsUpdateCopy() throws SQLException {
        // Firebird creates a new blob when making changes
        return true;
    }

    @Override
    public boolean storesUpperCaseIdentifiers() throws SQLException {
        return true;
    }

    @Override
    public boolean storesLowerCaseIdentifiers() throws SQLException {
        return false;
    }

    @Override
    public boolean storesMixedCaseIdentifiers() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
        return true;
    }

    @Override
    public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
        return false;
    }

    @Override
    public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
        return false;
    }

    @Override
    public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
        return false;
    }

    @Override
    public String getIdentifierQuoteString() throws SQLException {
        // TODO Return " " when connected with dialect 1?
        return "\"";
    }

    @Override
    public String getSQLKeywords() throws SQLException {
        return versionMetaData.getSqlKeywords();
    }

    /**
     * {@inheritDoc}
     * <p>
     * NOTE: Some of the functions listed may only work on Firebird 2.1 or higher, or when equivalent UDFs
     * are installed.
     * </p>
     */
    @Override
    public String getNumericFunctions() throws SQLException {
        return collectionToCommaSeparatedList(FBEscapedFunctionHelper.getSupportedNumericFunctions());
    }

    private static String collectionToCommaSeparatedList(Collection<String> collection) {
        StringBuilder sb = new StringBuilder();
        for (String item : collection) {
            sb.append(item);
            sb.append(',');
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     * <p>
     * NOTE: Some of the functions listed may only work on Firebird 2.1 or higher, or when equivalent UDFs
     * are installed.
     * </p>
     */
    @Override
    public String getStringFunctions() throws SQLException {
        return collectionToCommaSeparatedList(FBEscapedFunctionHelper.getSupportedStringFunctions());
    }

    /**
     * {@inheritDoc}
     * <p>
     * NOTE: Some of the functions listed may only work on Firebird 2.1 or higher, or when equivalent UDFs
     * are installed.
     * </p>
     */
    @Override
    public String getSystemFunctions() throws SQLException {
        return collectionToCommaSeparatedList(FBEscapedFunctionHelper.getSupportedSystemFunctions());
    }

    /**
     * {@inheritDoc}
     * <p>
     * NOTE: Some of the functions listed may only work on Firebird 2.1 or higher, or when equivalent UDFs
     * are installed.
     * </p>
     */
    @Override
    public String getTimeDateFunctions() throws SQLException {
        return collectionToCommaSeparatedList(FBEscapedFunctionHelper.getSupportedTimeDateFunctions());
    }

    @Override
    public String getSearchStringEscape() throws SQLException {
        return "\\";
    }

    @Override
    public String getExtraNameCharacters() throws SQLException {
        return "$";
    }

    @Override
    public boolean supportsAlterTableWithAddColumn() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsAlterTableWithDropColumn() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsColumnAliasing() throws SQLException {
        return true;
    }

    @Override
    public boolean nullPlusNonNullIsNull() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsConvert() throws SQLException {
        return true;
    }

    /**
     * {@inheritDoc}
     * <p>
     * See also {@link org.firebirdsql.jdbc.escape.ConvertFunction} for caveats.
     * </p>
     */
    @Override
    public boolean supportsConvert(int fromType, int toType) throws SQLException {
        switch (fromType) {
        case JaybirdTypeCodes.DECFLOAT:
            if (!firebirdSupportInfo.supportsDecfloat()) {
                return false;
            }
            // Intentional fallthrough
        case Types.TINYINT: // Doesn't exist in Firebird; handled as if SMALLINT
        case Types.SMALLINT:
        case Types.INTEGER:
        case Types.BIGINT:
        case Types.FLOAT:
        case Types.REAL:
        case Types.DOUBLE:
        case Types.NUMERIC:
        case Types.DECIMAL:
            // Numerical values all convertible to the same types.
            switch (toType) {
            case Types.TINYINT: // Doesn't exist in Firebird; handled as if SMALLINT
            case Types.SMALLINT:
            case Types.INTEGER:
            case Types.BIGINT:
            case Types.FLOAT:
            case Types.REAL:
            case Types.DOUBLE:
            case Types.NUMERIC:
            case Types.DECIMAL:
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.CLOB:
            case Types.NCHAR:
            case Types.LONGNVARCHAR:
            case Types.NVARCHAR:
            case Types.NCLOB:
                return true;
            // casting numerical values to binary types will result in ASCII bytes of string conversion, not to the
            // binary representation of the number (eg 1 will be converted to binary 0x31 (ASCII '1'), not 0x01)
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
            case Types.BLOB:
                return true;
            case JaybirdTypeCodes.DECFLOAT:
                return firebirdSupportInfo.supportsDecfloat();
            default:
                return false;
            }

        case Types.CHAR:
        case Types.VARCHAR:
        case Types.LONGVARCHAR:
        case Types.CLOB:
        case Types.NCHAR:
        case Types.LONGNVARCHAR:
        case Types.NVARCHAR:
        case Types.NCLOB:
        case Types.BINARY:
        case Types.VARBINARY:
        case Types.LONGVARBINARY:
        case Types.BLOB:
        case Types.ROWID: // Internally rowid is not discernible from BINARY
            // String and binary values all convertible to the same types
            // Be aware though that casting of binary to non-string/binary will perform the same conversion as
            // if it is an ASCII string value. Eg the binary string value 0x31 cast to integer will be 1, not 49.
            switch (toType) {
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.CLOB:
            case Types.NCHAR:
            case Types.LONGNVARCHAR:
            case Types.NVARCHAR:
            case Types.NCLOB:
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
            case Types.BLOB:
                return true;
            case Types.TINYINT: // Doesn't exist in Firebird; handled as if SMALLINT
            case Types.SMALLINT:
            case Types.INTEGER:
            case Types.BIGINT:
            case Types.FLOAT:
            case Types.REAL:
            case Types.DOUBLE:
            case Types.NUMERIC:
            case Types.DECIMAL:
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
                return fromType != Types.ROWID;
            case JaybirdTypeCodes.DECFLOAT:
                return fromType != Types.ROWID && firebirdSupportInfo.supportsDecfloat();
            case Types.BOOLEAN:
                return fromType != Types.ROWID && firebirdSupportInfo.supportsBoolean();
            case Types.ROWID:
                // As size of rowid is context dependent, we can't cast to it using the convert escape
                return false;
            case JaybirdTypeCodes.TIME_WITH_TIMEZONE:
            case JaybirdTypeCodes.TIMESTAMP_WITH_TIMEZONE:
                // TODO JDBC-540
                return false;
            default:
                return false;
            }

        case Types.DATE:
            switch(toType) {
            case Types.DATE:
            case Types.TIMESTAMP:
                return true;
            case Types.TIME:
            case JaybirdTypeCodes.TIME_WITH_TIMEZONE:
                return false;
            case JaybirdTypeCodes.TIMESTAMP_WITH_TIMEZONE:
                // TODO JDBC-540
                return false;
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.CLOB:
            case Types.NCHAR:
            case Types.LONGNVARCHAR:
            case Types.NVARCHAR:
            case Types.NCLOB:
                return true;
            // casting date/time values to binary types will result in ASCII bytes of string conversion
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
            case Types.BLOB:
                return true;
            default:
                return false;
            }
        case Types.TIME:
            switch(toType) {
            case Types.TIMESTAMP:
            case Types.TIME:
                return true;
            case Types.DATE:
                return false;
            case JaybirdTypeCodes.TIME_WITH_TIMEZONE:
            case JaybirdTypeCodes.TIMESTAMP_WITH_TIMEZONE:
                // TODO JDBC-540
                return false;
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.CLOB:
            case Types.NCHAR:
            case Types.LONGNVARCHAR:
            case Types.NVARCHAR:
            case Types.NCLOB:
                return true;
            // casting date/time values to binary types will result in ASCII bytes of string conversion
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
            case Types.BLOB:
                return true;
            default:
                return false;
            }
        case Types.TIMESTAMP:
            switch(toType) {
            case Types.TIMESTAMP:
            case Types.TIME:
            case Types.DATE:
                return true;
            case JaybirdTypeCodes.TIME_WITH_TIMEZONE:
            case JaybirdTypeCodes.TIMESTAMP_WITH_TIMEZONE:
                // TODO JDBC-540
                return false;
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.CLOB:
            case Types.NCHAR:
            case Types.LONGNVARCHAR:
            case Types.NVARCHAR:
            case Types.NCLOB:
                return true;
            // casting date/time values to binary types will result in ASCII bytes of string conversion
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
            case Types.BLOB:
                return true;
            default:
                return false;
            }

        case Types.NULL:
            // If a type can be cast to itself, then null can be cast to it as well
            return toType != Types.NULL && supportsConvert(toType, toType);

        case Types.BOOLEAN:
            if (firebirdSupportInfo.supportsBoolean()) {
                switch (toType) {
                case Types.BOOLEAN:
                case Types.CHAR:
                case Types.VARCHAR:
                case Types.LONGVARCHAR:
                case Types.CLOB:
                case Types.NCHAR:
                case Types.LONGNVARCHAR:
                case Types.NVARCHAR:
                case Types.NCLOB:
                    return true;
                // casting boolean values to binary types will result in ASCII bytes of string conversion
                case Types.BINARY:
                case Types.VARBINARY:
                case Types.LONGVARBINARY:
                case Types.BLOB:
                    return true;
                default:
                    return false;
                }
            }
            return false;

        case JaybirdTypeCodes.TIME_WITH_TIMEZONE:
        case JaybirdTypeCodes.TIMESTAMP_WITH_TIMEZONE:
            // TODO JDBC-540
            return false;

        case Types.ARRAY:
            // Arrays are not supported by Jaybird (and casting would be tricky anyway)
            return false;
        // Unsupported types
        case Types.BIT:
        case Types.OTHER:
        case Types.JAVA_OBJECT:
        case Types.DISTINCT:
        case Types.STRUCT:
        case Types.REF:
        case Types.DATALINK:
        case Types.SQLXML:
        case JaybirdTypeCodes.REF_CURSOR:
        default:
            return false;
        }
    }

    @Override
    public boolean supportsTableCorrelationNames() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsDifferentTableCorrelationNames() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsExpressionsInOrderBy() throws SQLException {
        return firebirdSupportInfo.isVersionEqualOrAbove(1, 5);
    }

    @Override
    public boolean supportsOrderByUnrelated() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsGroupBy() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsGroupByUnrelated() throws SQLException {
        // TODO Verify
        return false;
    }

    @Override
    public boolean supportsGroupByBeyondSelect() throws SQLException {
        // TODO Verify
        return false;
    }

    @Override
    public boolean supportsLikeEscapeClause() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsMultipleResultSets() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsMultipleTransactions() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsNonNullableColumns() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsMinimumSQLGrammar() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsCoreSQLGrammar() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsExtendedSQLGrammar() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsANSI92EntryLevelSQL() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsANSI92IntermediateSQL() throws SQLException {
        // TODO Verify
        return false;
    }

    @Override
    public boolean supportsANSI92FullSQL() throws SQLException {
        // TODO Verify
        return false;
    }

    @Override
    public boolean supportsIntegrityEnhancementFacility() throws SQLException {
        return true; // rrokytskyy: yep, they call so foreign keys + cascade deletes
    }

    @Override
    public boolean supportsOuterJoins() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsFullOuterJoins() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsLimitedOuterJoins() throws SQLException {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @return the vendor term, always {@code null} because schemas are not supported by database server (see JDBC CTS
     * for details).
     */
    @Override
    public String getSchemaTerm() throws SQLException {
        return null;
    }

    @Override
    public String getProcedureTerm() throws SQLException {
        return "PROCEDURE";
    }

    /**
     * {@inheritDoc}
     *
     * @return the vendor term, always {@code null} because catalogs are not supported by database server (see JDBC CTS
     * for details).
     */
    @Override
    public String getCatalogTerm() throws SQLException {
        return null;
    }

    @Override
    public boolean isCatalogAtStart() throws SQLException {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @return the separator string, always {@code null} because catalogs are not supported by database server (see
     * JDBC CTS for details).
     */
    @Override
    public String getCatalogSeparator() throws SQLException {
        return null;
    }

    @Override
    public boolean supportsSchemasInDataManipulation() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSchemasInProcedureCalls() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSchemasInTableDefinitions() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSchemasInIndexDefinitions() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsCatalogsInDataManipulation() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsCatalogsInProcedureCalls() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsCatalogsInTableDefinitions() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsPositionedDelete() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsPositionedUpdate() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsSelectForUpdate() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsStoredProcedures() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsSubqueriesInComparisons() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsSubqueriesInExists() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsSubqueriesInIns() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsSubqueriesInQuantifieds() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsCorrelatedSubqueries() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsUnion() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsUnionAll() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
        return false;//only when commit retaining is executed I think
    }

    @Override
    public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
        return false;//commit retaining only.
    }

    @Override
    public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsOpenStatementsAcrossRollback() throws SQLException {
        return true;
    }

    //----------------------------------------------------------------------
    // The following group of methods exposes various limitations
    // based on the target database with the current driver.
    // Unless otherwise specified, a result of zero means there is no
    // limit, or the limit is not known.

    @Override
    public int getMaxBinaryLiteralLength() throws SQLException {
        return 0; // TODO 32764 Test (assumed on length/2 and max string literal length)
    }

    @Override
    public int getMaxCharLiteralLength() throws SQLException {
        return 32765;
    }

    @Override
    public int getMaxColumnNameLength() throws SQLException {
        return getMaxObjectNameLength();
    }

    private int getMaxObjectNameLength() {
        if (gdsHelper.compareToVersion(4, 0) < 0) {
            return OBJECT_NAME_LENGTH_BEFORE_V4_0;
        } else {
            return OBJECT_NAME_LENGTH_V4_0;
        }
    }

    @Override
    public int getMaxColumnsInGroupBy() throws SQLException {
        return 0; //I don't know
    }

    @Override
    public int getMaxColumnsInIndex() throws SQLException {
        return 0; //I don't know
    }

    @Override
    public int getMaxColumnsInOrderBy() throws SQLException {
        return 0; //I don't know
    }

    @Override
    public int getMaxColumnsInSelect() throws SQLException {
        return 0; //I don't know
    }

    @Override
    public int getMaxColumnsInTable() throws SQLException {
        return 32767; // Depends on datatypes and sizes, at most 64 kbyte excluding blobs (but including blob ids)
    }

    @Override
    public int getMaxConnections() throws SQLException {
        return 0; //I don't know
    }

    @Override
    public int getMaxCursorNameLength() throws SQLException {
        return 31;
    }

    @Override
    public int getMaxIndexLength() throws SQLException {
        if (gdsHelper.compareToVersion(2, 0) < 0) {
            return 252; // See http://www.firebirdsql.org/en/firebird-technical-specifications/
        } else {
            return 0; // 1/4 of page size, maybe retrieve page size and use that?
        }
    }

    @Override
    public int getMaxSchemaNameLength() throws SQLException {
        return 0; //No schemas
    }

    @Override
    public int getMaxProcedureNameLength() throws SQLException {
        return getMaxObjectNameLength();
    }

    @Override
    public int getMaxCatalogNameLength() throws SQLException {
        return 0; //No catalogs
    }

    @Override
    public int getMaxRowSize() throws SQLException {
        if (gdsHelper.compareToVersion(1, 5) >= 0)
            return 65531;
        else
            return 0;
    }

    @Override
    public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
        return false; // Blob sizes are not included in rowsize
    }

    @Override
    public int getMaxStatementLength() throws SQLException {
        // TODO 10MB (or 2GB?) for Firebird 3 (test if we don't need to change anything else to support this)
        return 65536;
    }

    @Override
    public int getMaxStatements() throws SQLException {
        // Limited by max handles, but this includes other objects than statements
        return 0;
    }

    @Override
    public int getMaxTableNameLength() throws SQLException {
        return getMaxObjectNameLength();
    }

    @Override
    public int getMaxTablesInSelect() throws SQLException {
        // TODO Check if there is a max
        return 0;
    }

    @Override
    public int getMaxUserNameLength() throws SQLException {
        // TODO getMaxObjectNameLength?
        return 31;//I don't know??
    }

    //----------------------------------------------------------------------

    @Override
    public int getDefaultTransactionIsolation() throws SQLException {
        return Connection.TRANSACTION_READ_COMMITTED;
    }

    @Override
    public boolean supportsTransactions() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsTransactionIsolationLevel(int level) throws SQLException {
        switch (level) {
            case Connection.TRANSACTION_NONE: return false;
            case Connection.TRANSACTION_READ_COMMITTED: return true;
            case Connection.TRANSACTION_READ_UNCOMMITTED: return false;
            case Connection.TRANSACTION_REPEATABLE_READ: return true;
            case Connection.TRANSACTION_SERIALIZABLE: return true;
            default: return false;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Although Firebird supports both DML and DDL in transactions, it is not possible to use objects in the same
     * transaction that defines them. For example, it is not possible to insert into a table in the same transaction
     * that created it.
     * </p>
     */
    @Override
    public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsDataManipulationTransactionsOnly() throws SQLException {
        return false;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Read the note on {@link #supportsDataDefinitionAndDataManipulationTransactions()}.
     * </p>
     */
    @Override
    public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
        return false;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Read the note on {@link #supportsDataDefinitionAndDataManipulationTransactions()}.
     * </p>
     */
    @Override
    public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
        return false;
    }

    private static final String GET_PROCEDURES_START = "select "
        + "cast(RDB$PROCEDURE_NAME as " + OBJECT_NAME_TYPE + ") as PROCEDURE_NAME,"
        + "RDB$DESCRIPTION as REMARKS,"
        + "RDB$PROCEDURE_OUTPUTS as PROCEDURE_TYPE "
        + "from "
        + "RDB$PROCEDURES "
        + "where ";
    private static final String GET_PROCEDURES_END = "1 = 1 order by 1";

    @Override
    public ResultSet getProcedures(String catalog, String schemaPattern, String procedureNamePattern)
            throws SQLException {
        final RowDescriptor rowDescriptor = new RowDescriptorBuilder(9, datatypeCoder)
                .at(0).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "PROCEDURE_CAT", "PROCEDURES").addField()
                .at(1).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "PROCEDURE_SCHEM", "ROCEDURES").addField()
                .at(2).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "PROCEDURE_NAME", "PROCEDURES").addField()
                .at(3).simple(SQL_VARYING, 31, "FUTURE1", "PROCEDURES").addField()
                .at(4).simple(SQL_VARYING, 31, "FUTURE2", "PROCEDURES").addField()
                .at(5).simple(SQL_VARYING, 31, "FUTURE3", "PROCEDURES").addField()
                // Field in Firebird is actually a blob, using Integer.MAX_VALUE for length
                .at(6).simple(SQL_VARYING, Integer.MAX_VALUE, "REMARKS", "PROCEDURES").addField()
                .at(7).simple(SQL_SHORT, 0, "PROCEDURE_TYPE", "PROCEDURES").addField()
                .at(8).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "SPECIFIC_NAME", "PROCEDURES").addField()
                .toRowDescriptor();

        Clause procedureClause = new Clause("RDB$PROCEDURE_NAME", procedureNamePattern);

        String sql = GET_PROCEDURES_START;
        sql += procedureClause.getCondition();
        sql += GET_PROCEDURES_END;

        List<String> params = procedureClause.hasCondition()
                ? Collections.singletonList(procedureClause.getValue())
                : Collections.<String>emptyList();

        try (ResultSet rs = doQuery(sql, params)) {
            if (!rs.next()) {
                return new FBResultSet(rowDescriptor, Collections.<RowValue>emptyList());
            }

            final List<RowValue> rows = new ArrayList<>();
            final RowValueBuilder valueBuilder = new RowValueBuilder(rowDescriptor);
            do {
                rows.add(valueBuilder
                        .at(2).set(getBytes(rs.getString("PROCEDURE_NAME")))
                        .at(6).set(getBytes(rs.getString("REMARKS")))
                        .at(7).set(rs.getShort("PROCEDURE_TYPE") == 0 ? PROCEDURE_NO_RESULT : PROCEDURE_RETURNS_RESULT)
                        .at(8).set(valueBuilder.get(2))
                        .toRowValue(true)
                );
            } while (rs.next());
            return new FBResultSet(rowDescriptor, rows);
        }
    }

    private static final String GET_PROCEDURE_COLUMNS_START = "select "
        + "cast(PP.RDB$PROCEDURE_NAME as " + OBJECT_NAME_TYPE + ") as PROCEDURE_NAME,"
        + "cast(PP.RDB$PARAMETER_NAME as " + OBJECT_NAME_TYPE + ") as COLUMN_NAME,"
        + "PP.RDB$PARAMETER_TYPE as COLUMN_TYPE,"
        + "F.RDB$FIELD_TYPE as FIELD_TYPE,"
        + "F.RDB$FIELD_SUB_TYPE as FIELD_SUB_TYPE,"
        + "F.RDB$FIELD_PRECISION as FIELD_PRECISION,"
        + "F.RDB$FIELD_SCALE as FIELD_SCALE,"
        + "F.RDB$FIELD_LENGTH as FIELD_LENGTH,"
        + "F.RDB$NULL_FLAG as NULL_FLAG,"
        + "PP.RDB$DESCRIPTION as REMARKS,"
        + "F.RDB$CHARACTER_LENGTH AS CHAR_LEN,"
        + "PP.RDB$PARAMETER_NUMBER + 1 AS PARAMETER_NUMBER,"
        + "F.RDB$CHARACTER_SET_ID "
        + "from "
        + "RDB$PROCEDURE_PARAMETERS PP,"
        + "RDB$FIELDS F "
        + "where ";
    private static final String GET_PROCEDURE_COLUMNS_END = " PP.RDB$FIELD_SOURCE = F.RDB$FIELD_NAME "
        + "order by "
        + "PP.RDB$PROCEDURE_NAME,"
        + "PP.RDB$PARAMETER_TYPE desc,"
        + "PP.RDB$PARAMETER_NUMBER ";

    @Override
    public ResultSet getProcedureColumns(String catalog, String schemaPattern, String procedureNamePattern,
            String columnNamePattern) throws SQLException {
        final RowDescriptor rowDescriptor = new RowDescriptorBuilder(20, datatypeCoder)
                .at(0).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "PROCEDURE_CAT", "COLUMNINFO").addField()
                .at(1).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "PROCEDURE_SCHEM", "COLUMNINFO").addField()
                .at(2).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "PROCEDURE_NAME", "COLUMNINFO").addField()
                .at(3).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "COLUMN_NAME", "COLUMNINFO").addField()
                .at(4).simple(SQL_SHORT, 0, "COLUMN_TYPE", "COLUMNINFO").addField()
                .at(5).simple(SQL_LONG, 0, "DATA_TYPE", "COLUMNINFO").addField()
                .at(6).simple(SQL_VARYING, 31, "TYPE_NAME", "COLUMNINFO").addField()
                .at(7).simple(SQL_LONG, 0, "PRECISION", "COLUMNINFO").addField()
                .at(8).simple(SQL_LONG, 0, "LENGTH", "COLUMNINFO").addField()
                .at(9).simple(SQL_SHORT, 0, "SCALE", "COLUMNINFO").addField()
                .at(10).simple(SQL_SHORT, 0, "RADIX", "COLUMNINFO").addField()
                .at(11).simple(SQL_SHORT, 0, "NULLABLE", "COLUMNINFO").addField()
                // Field in Firebird is actually a blob, using Integer.MAX_VALUE for length
                .at(12).simple(SQL_VARYING, Integer.MAX_VALUE, "REMARKS", "COLUMNINFO").addField()
                .at(13).simple(SQL_VARYING, 31, "COLUMN_DEF", "COLUMNINFO").addField()
                .at(14).simple(SQL_LONG, 0, "SQL_DATA_TYPE", "COLUMNINFO").addField()
                .at(15).simple(SQL_LONG, 0, "SQL_DATETIME_SUB", "COLUMNINFO").addField()
                .at(16).simple(SQL_LONG, 0, "CHAR_OCTET_LENGTH", "COLUMNINFO").addField()
                .at(17).simple(SQL_LONG, 0, "ORDINAL_POSITION", "COLUMNINFO").addField()
                .at(18).simple(SQL_VARYING, 3, "IS_NULLABLE", "COLUMNINFO").addField()
                .at(19).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "SPECIFIC_NAME", "COLUMNINFO").addField()
                .toRowDescriptor();

        Clause procedureClause = new Clause("PP.RDB$PROCEDURE_NAME", procedureNamePattern);
        Clause columnClause = new Clause("PP.RDB$PARAMETER_NAME", columnNamePattern);

        String sql = GET_PROCEDURE_COLUMNS_START;
        sql += procedureClause.getCondition();
        sql += columnClause.getCondition();
        sql += GET_PROCEDURE_COLUMNS_END;

        List<String> params = new ArrayList<>(2);
        if (procedureClause.hasCondition()) {
            params.add(procedureClause.getValue());
        }
        if (columnClause.hasCondition()) {
            params.add(columnClause.getValue());
        }

        try (ResultSet rs = doQuery(sql, params)) {
            // if nothing found, return an empty result set
            if (!rs.next()) {
                return new FBResultSet(rowDescriptor, Collections.<RowValue>emptyList());
            }

            final List<RowValue> rows = new ArrayList<>();
            final RowValueBuilder valueBuilder = new RowValueBuilder(rowDescriptor);
            do {
                final short columnType = rs.getShort("COLUMN_TYPE");
                final short fieldType = rs.getShort("FIELD_TYPE");
                final short fieldSubType = rs.getShort("FIELD_SUB_TYPE");
                final short fieldScale = rs.getShort("FIELD_SCALE");
                final int characterSetId = rs.getInt("RDB$CHARACTER_SET_ID");
                // TODO: Find out what the difference is with NULL_FLAG in RDB$PROCEDURE_PARAMETERS (might be ODS dependent)
                final short nullFlag = rs.getShort("NULL_FLAG");
                final int dataType = getDataType(fieldType, fieldSubType, fieldScale, characterSetId);
                valueBuilder
                        .at(2).set(getBytes(rs.getString("PROCEDURE_NAME")))
                        .at(3).set(getBytes(rs.getString("COLUMN_NAME")))
                        // TODO: Unsure if procedureColumnOut is correct, maybe procedureColumnResult, or need ODS dependent use of RDB$PROCEDURE_TYPE to decide on selectable or executable?
                        // TODO: ResultSet columns should not be first according to JDBC 4.1 description
                        .at(4).set(columnType == 0 ? PROCEDURE_COLUMN_IN : PROCEDURE_COLUMN_OUT)
                        .at(5).set(createInt(dataType))
                        .at(6).set(getBytes(getDataTypeName(fieldType, fieldSubType, fieldScale)))
                        .at(8).set(createInt(rs.getShort("FIELD_LENGTH")))
                        .at(10).set(RADIX_TEN_SHORT)
                        .at(11).set(nullFlag == 1 ? PROCEDURE_NO_NULLS : PROCEDURE_NULLABLE)
                        .at(12).set(getBytes(rs.getString("REMARKS")))
                        // TODO: Need to write ODS version dependent method to retrieve some of the info for indexes 13 (From 2.0 defaults for procedure parameters), 14 and 15
                        // TODO: Find correct value for ORDINAL_POSITION (+ order of columns and intent, see JDBC-229)
                        .at(17).set(createInt(rs.getInt("PARAMETER_NUMBER")))
                        // TODO: Find out if there is a conceptual difference with NULLABLE (idx 11)
                        .at(18).set(nullFlag == 1 ? NO_BYTES : YES_BYTES)
                        .at(19).set(valueBuilder.get(2));

                switch (dataType) {
                case Types.DECIMAL:
                case Types.NUMERIC:
                    valueBuilder
                            .at(7).set(createInt(rs.getShort("FIELD_PRECISION")))
                            .at(9).set(createShort(-1 * fieldScale));
                    break;
                case Types.CHAR:
                case Types.VARCHAR:
                case Types.BINARY:
                case Types.VARBINARY:
                    short charLen = rs.getShort("CHAR_LEN");
                    if (!rs.wasNull()) {
                        valueBuilder.at(7).set(createInt(charLen));
                    } else {
                        valueBuilder.at(8).set(valueBuilder.get(8));
                    }
                    valueBuilder.at(16).set(valueBuilder.get(8));
                    break;
                case Types.FLOAT:
                    valueBuilder.at(7).set(FLOAT_PRECISION);
                    break;
                case Types.DOUBLE:
                    valueBuilder.at(7).set(DOUBLE_PRECISION);
                    break;
                case Types.BIGINT:
                    valueBuilder
                            .at(7).set(BIGINT_PRECISION)
                            .at(9).set(SHORT_ZERO);
                    break;
                case Types.INTEGER:
                    valueBuilder
                            .at(7).set(INTEGER_PRECISION)
                            .at(9).set(SHORT_ZERO);
                    break;
                case Types.SMALLINT:
                    valueBuilder
                            .at(7).set(SMALLINT_PRECISION)
                            .at(9).set(SHORT_ZERO);
                    break;
                case Types.DATE:
                    valueBuilder.at(7).set(DATE_PRECISION);
                    break;
                case Types.TIME:
                    valueBuilder.at(7).set(TIME_PRECISION);
                    break;
                case Types.TIMESTAMP:
                    valueBuilder.at(7).set(TIMESTAMP_PRECISION);
                    break;
                case Types.BOOLEAN:
                    valueBuilder
                            .at(7).set(BOOLEAN_PRECISION)
                            .at(10).set(RADIX_BINARY_SHORT);
                    break;
                case JaybirdTypeCodes.DECFLOAT:
                    switch (fieldType) {
                    case dec16_type:
                        valueBuilder.at(7).set(DECFLOAT_16_PRECISION);
                        break;
                    case dec34_type:
                        valueBuilder.at(7).set(DECFLOAT_34_PRECISION);
                        break;
                    }
                    break;
                }

                rows.add(valueBuilder.toRowValue(true));
            } while (rs.next());
            return new FBResultSet(rowDescriptor, rows);
        }
    }

    public static final String TABLE = "TABLE";
    public static final String SYSTEM_TABLE = "SYSTEM TABLE";
    public static final String VIEW = "VIEW";
    public static final String GLOBAL_TEMPORARY = "GLOBAL TEMPORARY";
    /**
     * Table types supported for Firebird 2.5 and up (will also work with 2.1 and earlier though)
     */
    public static final String[] ALL_TYPES_2_5 = {TABLE, SYSTEM_TABLE, VIEW, GLOBAL_TEMPORARY};
    /**
     * Table types supported for Firebird 2.1 and lower
     */
    public static final String[] ALL_TYPES_2_1 = {TABLE, SYSTEM_TABLE, VIEW};
    @SuppressWarnings("unused")
    public static final String[] ALL_TYPES = ALL_TYPES_2_5;

    @Override
    public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String types[])
            throws SQLException {
        if (hasGlobalTemporaryTables()) {
            return getTables_2_5(tableNamePattern, types);
        } else {
            return getTables_2_1(tableNamePattern, types);
        }
    }

    private static final String GET_TABLE_ORDER_BY = " order by 4, 3";

    //@formatter:off
    private static final String LEGACY_IS_TABLE = " rdb$relation_type is null and rdb$view_blr is null ";
    private static final String LEGACY_IS_VIEW = " rdb$relation_type is null and rdb$view_blr is not null ";

    private static final String TABLE_COLUMNS_2_5 =
            " select cast(null as " + OBJECT_NAME_TYPE + ") as TABLE_CAT,"
            + "cast(null as " + OBJECT_NAME_TYPE + ") as TABLE_SCHEM,"
            + "cast(RDB$RELATION_NAME as " + OBJECT_NAME_TYPE + ") as TABLE_NAME,"
            + "cast(case"
            + "  when rdb$relation_type = 0 or " + LEGACY_IS_TABLE + " then case when RDB$SYSTEM_FLAG = 1 then '" + SYSTEM_TABLE + "' else '" + TABLE + "' end"
            + "  when rdb$relation_type = 1 or " + LEGACY_IS_VIEW + " then '" + VIEW + "'"
            + "  when rdb$relation_type = 2 then '" + TABLE + "'" // external table; assume as normal table
            + "  when rdb$relation_type = 3 then '" + SYSTEM_TABLE + "'" // virtual (monitoring) table: assume system
            + "  when rdb$relation_type in (4, 5) then '" + GLOBAL_TEMPORARY + "'"
            + "end as varchar(31)) as TABLE_TYPE,"
            + "RDB$DESCRIPTION as REMARKS,"
            + "cast(null as " + OBJECT_NAME_TYPE + ") as TYPE_CAT,"
            + "cast(null as " + OBJECT_NAME_TYPE + ") as TYPE_SCHEM,"
            + "cast(null as varchar(31)) as TYPE_NAME,"
            + "cast(null as " + OBJECT_NAME_TYPE + ") as SELF_REFERENCING_COL_NAME,"
            + "cast(null as varchar(31)) as REF_GENERATION,"
            + "cast(RDB$OWNER_NAME as " + OBJECT_NAME_TYPE + ") as OWNER_NAME "
            + "from RDB$RELATIONS ";
    //@formatter:on

    /**
     * Implementation of {@link #getTables(String, String, String, String[])} for Firebird 2.5 and up.
     */
    private ResultSet getTables_2_5(String tableNamePattern, String[] types) throws SQLException {
        if (types == null) {
            types = ALL_TYPES_2_5;
        }

        Clause nameClause = new Clause("RDB$RELATION_NAME", tableNamePattern);
        String sql = TABLE_COLUMNS_2_5;
        List<String> params;
        if (nameClause.hasCondition()) {
            sql = sql + " where " + nameClause.getCondition();
            params = Collections.singletonList(nameClause.getValue());
        } else {
            params = Collections.emptyList();
        }
        Set<String> typeSet = new HashSet<>(Arrays.asList(types));

        if (!typeSet.containsAll(Arrays.asList(ALL_TYPES_2_5))) {
            // Only construct conditions when we don't query for all
            StringBuilder typeCondition = new StringBuilder(112);
            if (typeSet.contains(SYSTEM_TABLE) && typeSet.contains(TABLE)) {
                typeCondition.append(" (rdb$relation_type in (0, 2, 3) or " + LEGACY_IS_TABLE + ") ");
            } else if (typeSet.contains(SYSTEM_TABLE)) {
                typeCondition.append(" (rdb$relation_type in (0, 3) or " + LEGACY_IS_TABLE + ") and rdb$system_flag = 1 "); // We assume that external tables are never system and that virtual tables are always system
            } else if (typeSet.contains(TABLE)) {
                typeCondition.append(" (rdb$relation_type in (0, 2) or " + LEGACY_IS_TABLE + ") and rdb$system_flag = 0 "); // We assume that external tables are never system and that virtual tables are always system
            }

            if (typeSet.contains(VIEW)) {
                if (typeCondition.length() > 0) {
                    typeCondition.append(" or ");
                }
                typeCondition.append(" (rdb$relation_type = 1 or " + LEGACY_IS_VIEW + ") "); // We assume (but don't check) that views are never system
            }

            if (typeSet.contains(GLOBAL_TEMPORARY)) {
                if (typeCondition.length() > 0) {
                    typeCondition.append(" or ");
                }
                typeCondition.append(" rdb$relation_type in (4, 5) ");
            }

            if (typeCondition.length() == 0) {
                // Requested types are unknown, query nothing
                typeCondition.append(" 1 = 0 ");
            }

            sql = sql + (nameClause.hasCondition() ? " (" + typeCondition + ") " : " where " + typeCondition + " ");
        } else if (nameClause.hasCondition()) {
            // Clause condition always ends in "and"
            sql += " 1=1 ";
        }

        sql = sql + GET_TABLE_ORDER_BY;

        return doQuery(sql, params);
    }

    //@formatter:off
    private static final String TABLE_COLUMNS_FORMAT_2_1 =
            " select cast(null as " + OBJECT_NAME_TYPE + ") as TABLE_CAT,"
            + "cast(null as " + OBJECT_NAME_TYPE + ") as TABLE_SCHEM,"
            + "cast(RDB$RELATION_NAME as " + OBJECT_NAME_TYPE + ") as TABLE_NAME,"
            + "cast('%s' as varchar(31)) as TABLE_TYPE,"
            + "RDB$DESCRIPTION as REMARKS,"
            + "cast(null as " + OBJECT_NAME_TYPE + ") as TYPE_CAT,"
            + "cast(null as " + OBJECT_NAME_TYPE + ") as TYPE_SCHEM,"
            + "cast(null as varchar(31)) as TYPE_NAME,"
            + "cast(null as " + OBJECT_NAME_TYPE + ") as SELF_REFERENCING_COL_NAME,"
            + "cast(null as varchar(31)) as REF_GENERATION,"
            + "cast(RDB$OWNER_NAME as " + OBJECT_NAME_TYPE + ") as OWNER_NAME "
            + "from RDB$RELATIONS ";

    private static final String TABLE_COLUMNS_SYSTEM_2_1 =
            String.format(TABLE_COLUMNS_FORMAT_2_1, SYSTEM_TABLE);

    private static final String TABLE_COLUMNS_NORMAL =
            String.format(TABLE_COLUMNS_FORMAT_2_1, TABLE);

    private static final String TABLE_COLUMNS_VIEW =
            String.format(TABLE_COLUMNS_FORMAT_2_1, VIEW);

    private static final String GET_TABLES_ALL_2_1 =
            TABLE_COLUMNS_SYSTEM_2_1
            + " where ? = 'T' and RDB$SYSTEM_FLAG = 1 and rdb$view_blr is null"
            + " union"
            + TABLE_COLUMNS_NORMAL
            + " where ? = 'T' and RDB$SYSTEM_FLAG = 0 and rdb$view_blr is null"
            + " union"
            + TABLE_COLUMNS_VIEW
            + " where ? = 'T' and rdb$view_blr is not null "
            + GET_TABLE_ORDER_BY;

    private static final String GET_TABLES_EXACT_2_1 =
            TABLE_COLUMNS_SYSTEM_2_1
            + " where ? = 'T' and RDB$SYSTEM_FLAG = 1 and rdb$view_blr is null"
            + " and RDB$RELATION_NAME = " + OBJECT_NAME_PARAMETER
            + " union"
            + TABLE_COLUMNS_NORMAL
            + " where ? = 'T' and RDB$SYSTEM_FLAG = 0 and rdb$view_blr is null"
            + " and RDB$RELATION_NAME = " + OBJECT_NAME_PARAMETER
            + " union"
            + TABLE_COLUMNS_VIEW
            + " where ? = 'T' and rdb$view_blr is not null"
            + " and RDB$RELATION_NAME = " + OBJECT_NAME_PARAMETER
            + GET_TABLE_ORDER_BY;

    private static final String GET_TABLES_LIKE_2_1 =
            TABLE_COLUMNS_SYSTEM_2_1
            + " where ? = 'T' and RDB$SYSTEM_FLAG = 1 and rdb$view_blr is null"
            + " and trim(trailing from RDB$RELATION_NAME) like " + OBJECT_NAME_PARAMETER + " escape '\\'"
            + " union"
            + TABLE_COLUMNS_NORMAL
            + " where ? = 'T' and RDB$SYSTEM_FLAG = 0 and rdb$view_blr is null"
            + " and trim(trailing from RDB$RELATION_NAME) like " + OBJECT_NAME_PARAMETER + " escape '\\'"
            + " union"
            + TABLE_COLUMNS_VIEW
            + " where ? = 'T' and rdb$view_blr is not null"
            + " and trim(trailing from RDB$RELATION_NAME) like " + OBJECT_NAME_PARAMETER + " escape '\\'"
            + GET_TABLE_ORDER_BY;

    private static final String GET_TABLES_STARTING_WITH_2_1 =
            TABLE_COLUMNS_SYSTEM_2_1
            + " where ? = 'T' and RDB$SYSTEM_FLAG = 1 and rdb$view_blr is null"
            + " and RDB$RELATION_NAME starting with " + OBJECT_NAME_PARAMETER
            + " union"
            + TABLE_COLUMNS_NORMAL
            + " where ? = 'T' and RDB$SYSTEM_FLAG = 0 and rdb$view_blr is null"
            + " and RDB$RELATION_NAME starting with " + OBJECT_NAME_PARAMETER
            + " union"
            + TABLE_COLUMNS_VIEW
            + " where ? = 'T' and rdb$view_blr is not null"
            + " and RDB$RELATION_NAME starting with " + OBJECT_NAME_PARAMETER
            + GET_TABLE_ORDER_BY;
    //@formatter:on

    /**
     * Implementation of {@link #getTables(String, String, String, String[])} for Firebird 2.1 and lower.
     */
    private ResultSet getTables_2_1(String tableNamePattern, String[] types) throws SQLException {
        if (types == null) {
            types = ALL_TYPES_2_1;
        }
        MetadataPattern metadataPattern = MetadataPattern.compile(tableNamePattern);
        String sql;
        List<String> params;
        MetadataPattern.ConditionType conditionType = metadataPattern.getConditionType();
        if (conditionType == MetadataPattern.ConditionType.NONE) {
            sql = GET_TABLES_ALL_2_1;
            params = Arrays.asList(
                    getWantsSystemTables(types),
                    getWantsTables(types),
                    getWantsViews(types));
        } else {
            switch (conditionType) {
            case SQL_EQUALS:
                sql = GET_TABLES_EXACT_2_1;
                break;
            case SQL_LIKE:
                sql = GET_TABLES_LIKE_2_1;
                break;
            case SQL_STARTING_WITH:
                sql = GET_TABLES_STARTING_WITH_2_1;
                break;
            default:
                throw new AssertionError("Unexpected condition type " + conditionType);
            }
            String conditionValue = metadataPattern.getConditionValue();
            params = Arrays.asList(
                    getWantsSystemTables(types),
                    conditionValue,
                    getWantsTables(types),
                    conditionValue,
                    getWantsViews(types),
                    conditionValue);
        }
        return doQuery(sql, params);
    }

    @Override
    public ResultSet getSchemas() throws SQLException {
        return getSchemas(null, null);
    }

    @Override
    public ResultSet getCatalogs() throws SQLException {
        final RowDescriptor rowDescriptor = new RowDescriptorBuilder(1, datatypeCoder)
                .at(0).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_CAT", "TABLECATALOGS").addField()
                .toRowDescriptor();

        return new FBResultSet(rowDescriptor, Collections.<RowValue>emptyList());
    }

    @Override
    public ResultSet getTableTypes() throws SQLException {
        final RowDescriptor rowDescriptor = new RowDescriptorBuilder(1, datatypeCoder)
                .at(0).simple(SQL_VARYING, 31, "TABLE_TYPE", "TABLETYPES").addField()
                .toRowDescriptor();

        final String[] types = hasGlobalTemporaryTables()
                ? ALL_TYPES_2_5
                : ALL_TYPES_2_1;

        final List<RowValue> rows = new ArrayList<>(types.length);
        for (String type : types) {
            rows.add(RowValue.of(rowDescriptor, getBytes(type)));
        }

        return new FBResultSet(rowDescriptor, rows);
    }

    private boolean hasGlobalTemporaryTables() throws SQLException {
        return getOdsMajorVersion() == 11 && getOdsMinorVersion() >= 2 || getOdsMajorVersion() > 11;
    }

    //@formatter:off
    private static final String GET_COLUMNS_COMMON =
            "SELECT cast(RF.RDB$RELATION_NAME as " + OBJECT_NAME_TYPE + ") AS RELATION_NAME," +
            "cast(RF.RDB$FIELD_NAME as " + OBJECT_NAME_TYPE + ") AS FIELD_NAME," +
            "F.RDB$FIELD_TYPE AS FIELD_TYPE," +
            "F.RDB$FIELD_SUB_TYPE AS FIELD_SUB_TYPE," +
            "F.RDB$FIELD_PRECISION AS FIELD_PRECISION," +
            "F.RDB$FIELD_SCALE AS FIELD_SCALE," +
            "F.RDB$FIELD_LENGTH AS FIELD_LENGTH," +
            "F.RDB$CHARACTER_LENGTH AS CHAR_LEN," +
            "RF.RDB$DESCRIPTION AS REMARKS," +
            "RF.RDB$DEFAULT_SOURCE AS DEFAULT_SOURCE," +
            "F.RDB$DEFAULT_SOURCE AS DOMAIN_DEFAULT_SOURCE," +
            "RF.RDB$FIELD_POSITION + 1 AS FIELD_POSITION," +
            "RF.RDB$NULL_FLAG AS NULL_FLAG," +
            "F.RDB$NULL_FLAG AS SOURCE_NULL_FLAG," +
            "F.RDB$COMPUTED_BLR AS COMPUTED_BLR," +
            "F.RDB$CHARACTER_SET_ID,";

    private static final String GET_COLUMNS_3_0_START =
            GET_COLUMNS_COMMON +
            "CASE WHEN RF.RDB$IDENTITY_TYPE IS NULL THEN CAST('NO' AS VARCHAR(3)) ELSE CAST('YES' AS VARCHAR(3)) END AS IS_IDENTITY," +
            "CASE RF.RDB$IDENTITY_TYPE WHEN 0 THEN CAST('ALWAYS' AS VARCHAR(10)) WHEN 1 THEN CAST('BY DEFAULT' AS VARCHAR(10)) ELSE NULL END AS JB_IDENTITY_TYPE " +
            "FROM RDB$RELATION_FIELDS RF," +
            "RDB$FIELDS F " +
            "WHERE ";

    private static final String GET_COLUMNS_START =
            GET_COLUMNS_COMMON +
            "'NO' AS IS_IDENTITY," +
            "CAST(NULL AS VARCHAR(10)) AS JB_IDENTITY_TYPE " +
            "FROM RDB$RELATION_FIELDS RF," +
            "RDB$FIELDS F " +
            "WHERE ";

    public static final String GET_COLUMNS_END = " RF.RDB$FIELD_SOURCE = F.RDB$FIELD_NAME " +
        "order by RF.RDB$RELATION_NAME, RF.RDB$FIELD_POSITION";
    //@formatter:on

    /**
     * {@inheritDoc}
     *
     * <p>
     * Jaybird defines these additional columns:
     * <ol start="25">
     * <li><b>JB_IS_IDENTITY</b> String  => Indicates whether this column is an identity column (<b>NOTE: Jaybird
     * specific column; retrieve by name!</b>).
     * There is a subtle difference with the meaning of {@code IS_AUTOINCREMENT}. This column indicates if the column
     * is a true identity column.
     * <ul>
     * <li> YES           --- if the column is an identity column</li>
     * <li> NO            --- if the column is not an identity column</li>
     * </ul>
     * </li>
     * <li><b>JB_IDENTITY_TYPE</b> String  => Type of identity column (<b>NOTE: Jaybird specific column; retrieve by
     * name!</b>)
     * <ul>
     * <li> ALWAYS        --- for a GENERATED ALWAYS AS IDENTITY column (not yet supported in Firebird 3!)</li>
     * <li> BY DEFAULT    --- for a GENERATED BY DEFAULT AS IDENTITY column</li>
     * <li> null          --- if the column is not an identity type (or the identity type is unknown)</li>
     * </ul>
     * </li>
     * </ol>
     * </p>
     */
    @Override
    public ResultSet getColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern)
            throws SQLException {
        final RowDescriptor rowDescriptor = new RowDescriptorBuilder(26, datatypeCoder)
                .at(0).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_CAT", "COLUMNINFO").addField()
                .at(1).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_SCHEM", "COLUMNINFO").addField()
                .at(2).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_NAME", "COLUMNINFO").addField()
                .at(3).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "COLUMN_NAME", "COLUMNINFO").addField()
                .at(4).simple(SQL_LONG, 0, "DATA_TYPE", "COLUMNINFO").addField()
                .at(5).simple(SQL_VARYING | 1, 31, "TYPE_NAME", "COLUMNINFO").addField()
                .at(6).simple(SQL_LONG, 0, "COLUMN_SIZE", "COLUMNINFO").addField()
                .at(7).simple(SQL_LONG, 0, "BUFFER_LENGTH", "COLUMNINFO").addField()
                .at(8).simple(SQL_LONG, 0, "DECIMAL_DIGITS", "COLUMNINFO").addField()
                .at(9).simple(SQL_LONG, 0, "NUM_PREC_RADIX", "COLUMNINFO").addField()
                .at(10).simple(SQL_LONG, 0, "NULLABLE", "COLUMNINFO").addField()
                // Field in Firebird is actually a blob, using Integer.MAX_VALUE for length
                .at(11).simple(SQL_VARYING | 1, Integer.MAX_VALUE, "REMARKS", "COLUMNINFO").addField()
                .at(12).simple(SQL_VARYING | 1, 31, "COLUMN_DEF", "COLUMNINFO").addField()
                .at(13).simple(SQL_LONG, 0, "SQL_DATA_TYPE", "COLUMNINFO").addField()
                .at(14).simple(SQL_LONG, 0, "SQL_DATETIME_SUB", "COLUMNINFO").addField()
                .at(15).simple(SQL_LONG, 0, "CHAR_OCTET_LENGTH", "COLUMNINFO").addField()
                .at(16).simple(SQL_LONG, 0, "ORDINAL_POSITION", "COLUMNINFO").addField()
                .at(17).simple(SQL_VARYING, 3, "IS_NULLABLE", "COLUMNINFO").addField()
                .at(18).simple(SQL_VARYING, OBJECT_NAME_LENGTH, getScopeCatalogColumnName(), "COLUMNINFO").addField()
                .at(19).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "SCOPE_SCHEMA", "COLUMNINFO").addField()
                .at(20).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "SCOPE_TABLE", "COLUMNINFO").addField()
                .at(21).simple(SQL_SHORT, 0, "SOURCE_DATA_TYPE", "COLUMNINFO").addField()
                .at(22).simple(SQL_VARYING, 3, "IS_AUTOINCREMENT", "COLUMNINFO").addField()
                .at(23).simple(SQL_VARYING, 3, "IS_GENERATEDCOLUMN", "COLUMNINFO").addField()
                .at(24).simple(SQL_VARYING, 3, "JB_IS_IDENTITY", "COLUMNINFO").addField()
                .at(25).simple(SQL_VARYING, 10, "JB_IDENTITY_TYPE", "COLUMNINFO").addField()
                .toRowDescriptor();

        Clause tableClause = new Clause("RF.RDB$RELATION_NAME", tableNamePattern);
        Clause columnClause = new Clause("RF.RDB$FIELD_NAME", columnNamePattern);

        String sql = hasIdentityColumns() ? GET_COLUMNS_3_0_START : GET_COLUMNS_START;
        sql += tableClause.getCondition();
        sql += columnClause.getCondition();
        sql += GET_COLUMNS_END;

        List<String> params = new ArrayList<>(2);
        if (tableClause.hasCondition()) {
            params.add(tableClause.getValue());
        }
        if (columnClause.hasCondition()) {
            params.add(columnClause.getValue());
        }

        try (ResultSet rs = doQuery(sql, params)) {
            if (!rs.next()) {
                return new FBResultSet(rowDescriptor, Collections.<RowValue>emptyList());
            }

            final List<RowValue> rows = new ArrayList<>();
            final RowValueBuilder valueBuilder = new RowValueBuilder(rowDescriptor);
            do {
                final short fieldType = rs.getShort("FIELD_TYPE");
                final short fieldSubType = rs.getShort("FIELD_SUB_TYPE");
                final short fieldScale = rs.getShort("FIELD_SCALE");
                final int characterSetId = rs.getInt("RDB$CHARACTER_SET_ID");
                final int dataType = getDataType(fieldType, fieldSubType, fieldScale, characterSetId);
                valueBuilder
                        .at(2).set(getBytes(rs.getString("RELATION_NAME")))
                        .at(3).set(getBytes(rs.getString("FIELD_NAME")))
                        .at(4).set(createInt(dataType))
                        .at(5).set(getBytes(getDataTypeName(fieldType, fieldSubType, fieldScale)))
                        .at(9).set(RADIX_TEN);

                switch (dataType) {
                case Types.DECIMAL:
                case Types.NUMERIC:
                    valueBuilder
                            .at(6).set(createInt(rs.getShort("FIELD_PRECISION")))
                            .at(8).set(createInt(fieldScale * (-1)));
                    break;
                case Types.CHAR:
                case Types.VARCHAR:
                case Types.BINARY:
                case Types.VARBINARY:
                    valueBuilder.at(15).set(createInt(rs.getShort("FIELD_LENGTH")));
                    short charLen = rs.getShort("CHAR_LEN");
                    if (!rs.wasNull()) {
                        valueBuilder.at(6).set(createInt(charLen));
                    } else {
                        valueBuilder.at(6).set(valueBuilder.get(15));
                    }
                    break;
                case Types.FLOAT:
                    valueBuilder.at(6).set(FLOAT_PRECISION);
                    break;
                case Types.DOUBLE:
                    valueBuilder.at(6).set(DOUBLE_PRECISION);
                    break;
                case Types.BIGINT:
                    valueBuilder
                            .at(6).set(BIGINT_PRECISION)
                            .at(8).set(INT_ZERO);
                    break;
                case Types.INTEGER:
                    valueBuilder
                            .at(6).set(INTEGER_PRECISION)
                            .at(8).set(INT_ZERO);
                    break;
                case Types.SMALLINT:
                    valueBuilder
                            .at(6).set(SMALLINT_PRECISION)
                            .at(8).set(INT_ZERO);
                    break;
                case Types.DATE:
                    valueBuilder.at(6).set(DATE_PRECISION);
                    break;
                case Types.TIME:
                    valueBuilder.at(6).set(TIME_PRECISION);
                    break;
                case Types.TIMESTAMP:
                    valueBuilder.at(6).set(TIMESTAMP_PRECISION);
                    break;
                case Types.BOOLEAN:
                    valueBuilder
                            .at(6).set(BOOLEAN_PRECISION)
                            .at(9).set(RADIX_BINARY);
                    break;
                case JaybirdTypeCodes.DECFLOAT:
                    switch (fieldType) {
                    case dec16_type:
                        valueBuilder.at(6).set(DECFLOAT_16_PRECISION);
                        break;
                    case dec34_type:
                        valueBuilder.at(6).set(DECFLOAT_34_PRECISION);
                        break;
                    }
                    break;
                }

                final short nullFlag = rs.getShort("NULL_FLAG");
                final short sourceNullFlag = rs.getShort("SOURCE_NULL_FLAG");
                valueBuilder.at(10).set(nullFlag == 1 || sourceNullFlag == 1
                        ? COLUMN_NO_NULLS
                        : COLUMN_NULLABLE)
                        .at(11).set(getBytes(rs.getString("REMARKS")));

                String column_def = rs.getString("DEFAULT_SOURCE");
                if (column_def == null) {
                    column_def = rs.getString("DOMAIN_DEFAULT_SOURCE");
                }
                if (column_def != null) {
                    // TODO This looks suspicious (what if it contains default)
                    int defaultPos = column_def.toUpperCase().indexOf("DEFAULT");
                    if (defaultPos >= 0)
                        column_def = column_def.substring(7).trim();
                    valueBuilder.at(12).set(getBytes(column_def));
                }

                valueBuilder
                        .at(16).set(createInt(rs.getInt("FIELD_POSITION")))
                        .at(17).set(nullFlag == 1 || sourceNullFlag == 1 ? NO_BYTES : YES_BYTES);

                final boolean isIdentity = Objects.equals("YES", rs.getString("IS_IDENTITY"));
                if (isIdentity) {
                    // identity column is an autoincrement for sure
                    valueBuilder.at(22).set(YES_BYTES);
                } else {
                    switch (dataType) {
                    case Types.INTEGER:
                    case Types.TINYINT:
                    case Types.BIGINT:
                    case Types.SMALLINT:
                        // Could be autoincrement by trigger, but we simply don't know
                        valueBuilder.at(22).set(EMPTY_STRING_BYTES);
                        break;
                    case Types.NUMERIC:
                    case Types.DECIMAL:
                        if (fieldScale == 0) {
                            // Could be autoincrement by trigger, but we simply don't know
                            valueBuilder.at(22).set(EMPTY_STRING_BYTES);
                        } else {
                            // Scaled NUMERIC/DECIMAL: definitely not autoincrement
                            valueBuilder.at(22).set(NO_BYTES);
                        }
                        break;
                    default:
                        // All other types are never autoincrement
                        valueBuilder.at(22).set(NO_BYTES);
                    }
                }
                // Retrieving COMPUTED_BLR to check if it was NULL or not
                rs.getString("COMPUTED_BLR");
                // consider identity columns to be generated columns
                boolean isGenerated = !rs.wasNull() || isIdentity;
                valueBuilder.at(23).set(isGenerated ? YES_BYTES : NO_BYTES);

                valueBuilder.at(24).set(isIdentity ? YES_BYTES : NO_BYTES);
                valueBuilder.at(25).set(getBytes(rs.getString("JB_IDENTITY_TYPE")));

                rows.add(valueBuilder.toRowValue(true));
            } while (rs.next());
            return new FBResultSet(rowDescriptor, rows);
        }
    }

    private boolean hasIdentityColumns() throws SQLException {
        return getOdsMajorVersion() >= 12;
    }

    /**
     * Gets the name of the correct scope catalog column name based on the JDBC version for use in
     * {@link #getColumns(String, String, String, String)}.
     * <p>
     * Rationale: in older versions of JDBC this column was misspelled as <code>"SCOPE_CATLOG"</code> instead of
     * <code>"SCOPE_CATALOG"</code>. This was fixed in JDBC 4.1
     * </p>
     *
     * @return The scope catalog name.
     */
    private String getScopeCatalogColumnName() {
        final String scopeCatalog;
        if (getJDBCMajorVersion() > 4 || getJDBCMajorVersion() == 4 && getJDBCMinorVersion() >= 1) {
            scopeCatalog = "SCOPE_CATALOG";
        } else {
            scopeCatalog = "SCOPE_CATLOG";
        }
        return scopeCatalog;
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
    private static final int dec16_type = 24;
    private static final int dec34_type = 25;
    private static final int dec_fixed_type = 26;
    private static final int double_type = 27;
    private static final int timestamp_type = 35;
    private static final int varchar_type = 37;
//  private static final int cstring_type = 40;
    private static final int blob_type = 261;
    private static final short boolean_type = 23;

    private static int getDataType(int fieldType, int fieldSubType, int fieldScale, int characterSetId) {
        // TODO Preserved for backwards compatibility, is this really necessary?
        if (fieldType == blob_type && fieldSubType > 1) {
            return Types.OTHER;
        }
        final int jdbcType = JdbcTypeConverter.fromMetaDataToJdbcType(fieldType, fieldSubType, fieldScale);
        // Metadata from RDB$ tables does not contain character set in subtype, manual fixup
        if (characterSetId == CS_BINARY) {
            if (jdbcType == Types.CHAR) {
                return Types.BINARY;
            } else if (jdbcType == Types.VARCHAR) {
                return Types.VARBINARY;
            }
        }
        return jdbcType;
    }

    // TODO Unify with AbstractFieldMetadata
    private static String getDataTypeName(int sqltype, int sqlsubtype, int sqlscale) {
        switch (sqltype) {
            case smallint_type:
                if (sqlsubtype == SUBTYPE_NUMERIC || (sqlsubtype == 0 && sqlscale < 0)) {
                    return "NUMERIC";
                } else if (sqlsubtype == SUBTYPE_DECIMAL) {
                    return "DECIMAL";
                } else {
                    return "SMALLINT";
                }
            case integer_type:
                if (sqlsubtype == SUBTYPE_NUMERIC || (sqlsubtype == 0 && sqlscale < 0)) {
                    return "NUMERIC";
                } else if (sqlsubtype == SUBTYPE_DECIMAL) {
                    return "DECIMAL";
                } else {
                    return "INTEGER";
                }
            case double_type:
            case d_float_type:
                if (sqlsubtype == SUBTYPE_NUMERIC || (sqlsubtype == 0 && sqlscale < 0)) {
                    return "NUMERIC";
                } else if (sqlsubtype == SUBTYPE_DECIMAL) {
                    return "DECIMAL";
                } else {
                    return "DOUBLE PRECISION";
                }
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
                if (sqlsubtype == SUBTYPE_NUMERIC || (sqlsubtype == 0 && sqlscale < 0)) {
                    return "NUMERIC";
                } else if (sqlsubtype == SUBTYPE_DECIMAL) {
                    return "DECIMAL";
                } else {
                    return "BIGINT";
                }
            case blob_type:
                if (sqlsubtype < 0) {
                    // TODO Include actual subtype?
                    return "BLOB SUB_TYPE <0";
                } else if (sqlsubtype == BLOB_SUB_TYPE_BINARY) {
                    return "BLOB SUB_TYPE 0";
                } else if (sqlsubtype == BLOB_SUB_TYPE_TEXT) {
                    return "BLOB SUB_TYPE 1";
                } else {
                    return "BLOB SUB_TYPE " + sqlsubtype;
                }
            case quad_type:
                return "ARRAY";
            case boolean_type:
                return "BOOLEAN";
            case dec_fixed_type:
                switch (sqlsubtype) {
                case SUBTYPE_DECIMAL:
                    return "DECIMAL";
                case SUBTYPE_NUMERIC:
                default:
                    return "NUMERIC";
                }
            case dec16_type:
            case dec34_type:
                return "DECFLOAT";
            default:
                return "NULL";
        }
    }

    private static final String GET_COLUMN_PRIVILEGES_START = "select "
        + "cast(RF.RDB$RELATION_NAME as " + OBJECT_NAME_TYPE + ") as TABLE_NAME,"
        + "cast(RF.RDB$FIELD_NAME as " + OBJECT_NAME_TYPE + ") as COLUMN_NAME,"
        + "cast(UP.RDB$GRANTOR as " + OBJECT_NAME_TYPE + ") as GRANTOR,"
        + "cast(UP.RDB$USER as " + OBJECT_NAME_TYPE + ") as GRANTEE,"
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
        + "UP.RDB$RELATION_NAME = " + OBJECT_NAME_PARAMETER + " and ((";
    private static final String GET_COLUMN_PRIVILEGES_END = " UP.RDB$OBJECT_TYPE = 0) or "
        + "(RF.RDB$FIELD_NAME is null and UP.RDB$OBJECT_TYPE = 0)) "
        + "order by 2,5 ";

    private static final Map<String, byte[]> PRIVILEGE_MAPPING;
    static {
        Map<String, byte[]> tempMapping = new HashMap<>();
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
     * @param firebirdPrivilege
     *         Firebird privilege
     * @return JDBC privilege encoded as byte array
     */
    private static byte[] mapPrivilege(String firebirdPrivilege) {
        return PRIVILEGE_MAPPING.get(firebirdPrivilege);
    }

    @Override
    public ResultSet getColumnPrivileges(String catalog, String schema, String table, String columnNamePattern)
            throws SQLException {
        final RowDescriptor rowDescriptor = new RowDescriptorBuilder(8, datatypeCoder)
                .at(0).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_CAT", "COLUMNPRIV").addField()
                .at(1).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_SCHEM", "COLUMNPRIV").addField()
                .at(2).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_NAME", "COLUMNPRIV").addField()
                .at(3).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "COLUMN_NAME", "COLUMNPRIV").addField()
                .at(4).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "GRANTOR", "COLUMNPRIV").addField()
                .at(5).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "GRANTEE", "COLUMNPRIV").addField()
                .at(6).simple(SQL_VARYING, 31, "PRIVILEGE", "COLUMNPRIV").addField()
                .at(7).simple(SQL_VARYING, 31, "IS_GRANTABLE", "COLUMNPRIV").addField()
                .toRowDescriptor();

        Clause columnClause = new Clause("RF.RDB$FIELD_NAME", columnNamePattern);

        String sql = GET_COLUMN_PRIVILEGES_START;
        sql += columnClause.getCondition();
        sql += GET_COLUMN_PRIVILEGES_END;

        List<String> params = new ArrayList<>(2);
        params.add(table);
        if (columnClause.hasCondition()) {
            params.add(columnClause.getValue());
        }

        try (ResultSet rs = doQuery(sql, params)) {
            // return empty result set
            if (!rs.next()) {
                return new FBResultSet(rowDescriptor, Collections.<RowValue>emptyList());
            }

            final List<RowValue> rows = new ArrayList<>();
            final RowValueBuilder valueBuilder = new RowValueBuilder(rowDescriptor);
            do {
                rows.add(valueBuilder
                        .at(2).set(getBytes(rs.getString("TABLE_NAME")))
                        .at(3).set(getBytes(rs.getString("COLUMN_NAME")))
                        .at(4).set(getBytes(rs.getString("GRANTOR")))
                        .at(5).set(getBytes(rs.getString("GRANTEE")))
                        .at(6).set(mapPrivilege(rs.getString("PRIVILEGE")))
                        .at(7).set(rs.getShort("IS_GRANTABLE") == 0 ? NO_BYTES : YES_BYTES)
                        .toRowValue(true)
                );
            } while (rs.next());
            return new FBResultSet(rowDescriptor, rows);
        }
    }

    private static final String GET_TABLE_PRIVILEGES_START = "select "
        + "cast(RDB$RELATION_NAME as " + OBJECT_NAME_TYPE + ") as TABLE_NAME,"
        + "cast(RDB$GRANTOR as " + OBJECT_NAME_TYPE + ") as GRANTOR,"
        + "cast(RDB$USER as " + OBJECT_NAME_TYPE + ") as GRANTEE,"
        + "cast(RDB$PRIVILEGE as varchar(6)) as PRIVILEGE,"
        + "RDB$GRANT_OPTION as IS_GRANTABLE "
        + "from"
        + " RDB$USER_PRIVILEGES "
        + "where ";
    private static final String GET_TABLE_PRIVILEGES_END = " RDB$OBJECT_TYPE = 0 and"
        + " RDB$FIELD_NAME is null "
        + "order by 1, 4";

    @Override
    public ResultSet getTablePrivileges(String catalog, String schemaPattern, String tableNamePattern)
            throws SQLException {
        final RowDescriptor rowDescriptor = buildTablePrivilegeRSMetaData();

        Clause tableClause = new Clause("RDB$RELATION_NAME", tableNamePattern);

        String sql = GET_TABLE_PRIVILEGES_START;
        sql += tableClause.getCondition();
        sql += GET_TABLE_PRIVILEGES_END;

        List<String> params = tableClause.hasCondition()
                ? Collections.singletonList(tableClause.getValue())
                : Collections.<String>emptyList();

        try (ResultSet rs = doQuery(sql, params)) {
            // if nothing found, return an empty result set
            if (!rs.next()) {
                return new FBResultSet(rowDescriptor, Collections.<RowValue>emptyList());
            }

            return processTablePrivileges(rowDescriptor, rs);
        }
    }

    protected final RowDescriptor buildTablePrivilegeRSMetaData() {
        return new RowDescriptorBuilder(7, datatypeCoder)
                .at(0).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_CAT", "TABLEPRIV").addField()
                .at(1).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_SCHEM", "TABLEPRIV").addField()
                .at(2).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_NAME", "TABLEPRIV").addField()
                .at(3).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "GRANTOR", "TABLEPRIV").addField()
                .at(4).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "GRANTEE", "TABLEPRIV").addField()
                .at(5).simple(SQL_VARYING, 31, "PRIVILEGE", "TABLEPRIV").addField()
                .at(6).simple(SQL_VARYING, 31, "IS_GRANTABLE", "TABLEPRIV").addField()
                .toRowDescriptor();
    }

    protected final FBResultSet processTablePrivileges(final RowDescriptor rowDescriptor, final ResultSet fbTablePrivileges) throws SQLException {
        final List<RowValue> rows = new ArrayList<>();
        final RowValueBuilder valueBuilder = new RowValueBuilder(rowDescriptor);
        do {
            rows.add(valueBuilder
                    .at(2).set(getBytes(fbTablePrivileges.getString("TABLE_NAME")))
                    .at(3).set(getBytes(fbTablePrivileges.getString("GRANTOR")))
                    .at(4).set(getBytes(fbTablePrivileges.getString("GRANTEE")))
                    .at(5).set(mapPrivilege(fbTablePrivileges.getString("PRIVILEGE")))
                    .at(6).set(fbTablePrivileges.getShort("IS_GRANTABLE") == 0 ? NO_BYTES : YES_BYTES)
                    .toRowValue(true)
            );
        } while (fbTablePrivileges.next());
        return new FBResultSet(rowDescriptor, rows);
    }

    //@formatter:off
    private static final String GET_BEST_ROW_IDENT =
            "SELECT " +
            "CAST(rf.rdb$field_name AS " + OBJECT_NAME_TYPE + ") AS column_name," +
            "f.rdb$field_type AS field_type," +
            "f.rdb$field_sub_type AS field_sub_type," +
            "f.rdb$field_scale AS field_scale," +
            "f.rdb$field_precision AS field_precision," +
            "f.RDB$CHARACTER_SET_ID " +
            "FROM rdb$relation_constraints rc " +
            "INNER JOIN rdb$index_segments idx ON idx.rdb$index_name = rc.rdb$index_name " +
            "INNER JOIN rdb$relation_fields rf ON rf.rdb$field_name = idx.rdb$field_name " +
            "    AND rf.rdb$relation_name = rc.rdb$relation_name " +
            "INNER JOIN rdb$fields f ON f.rdb$field_name = rf.rdb$field_source " +
            "WHERE " +
            "rc.rdb$relation_name = " + OBJECT_NAME_PARAMETER +
            "AND rc.rdb$constraint_type = 'PRIMARY KEY'";
    //@formatter:on

    /**
     * {@inheritDoc}
     * <p>
     * Jaybird considers the primary key (scoped as {@code bestRowSession} as the best identifier for all scopes.
     * Pseudo column {@code RDB$DB_KEY} (scoped as {@code bestRowTransaction} is considered the second-best alternative
     * for scopes {@code bestRowTemporary} and {@code bestRowTransaction} if {@code table} has no primary key.
     * </p>
     * <p>
     * Jaybird currently considers {@code RDB$DB_KEY} to be {@link DatabaseMetaData#bestRowTransaction} even if the
     * dbkey_scope is set to 1 (session). This may change in the future. See also {@link #getRowIdLifetime()}.
     * </p>
     */
    @Override
    public ResultSet getBestRowIdentifier(String catalog, String schema, String table, int scope, boolean nullable)
            throws SQLException {
        final RowDescriptor rowDescriptor = new RowDescriptorBuilder(8, datatypeCoder)
                .at(0).simple(SQL_SHORT, 0, "SCOPE", "ROWIDENTIFIER").addField()
                .at(1).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "COLUMN_NAME", "ROWIDENTIFIER").addField()
                .at(2).simple(SQL_LONG, 0, "DATA_TYPE", "ROWIDENTIFIER").addField()
                .at(3).simple(SQL_VARYING, 31, "TYPE_NAME", "ROWIDENTIFIER").addField()
                .at(4).simple(SQL_LONG, 0, "COLUMN_SIZE", "ROWIDENTIFIER").addField()
                .at(5).simple(SQL_LONG, 0, "BUFFER_LENGTH", "ROWIDENTIFIER").addField()
                .at(6).simple(SQL_SHORT, 0, "DECIMAL_DIGITS", "ROWIDENTIFIER").addField()
                .at(7).simple(SQL_SHORT, 0, "PSEUDO_COLUMN", "ROWIDENTIFIER").addField()
                .toRowDescriptor();

        final RowValueBuilder rowValueBuilder = new RowValueBuilder(rowDescriptor);

        final List<RowValue> rows = getPrimaryKeyIdentifier(table, rowValueBuilder);

        // if no primary key exists, add RDB$DB_KEY as pseudo-column
        if (rows.size() == 0) {
            // NOTE: Currently is always ROWID_VALID_TRANSACTION
            final RowIdLifetime rowIdLifetime = getRowIdLifetime();
            if (rowIdLifetime == RowIdLifetime.ROWID_VALID_TRANSACTION && scope == DatabaseMetaData.bestRowSession) {
                // consider RDB$DB_KEY scope transaction
                return new FBResultSet(rowDescriptor, Collections.<RowValue>emptyList());
            }

            try (ResultSet pseudoColumns = getPseudoColumns(catalog, schema, escapeWildcards(table), "RDB$DB\\_KEY")) {
                if (!pseudoColumns.next()) {
                    return new FBResultSet(rowDescriptor, Collections.<RowValue>emptyList());
                }
                rows.add(rowValueBuilder
                        .at(0).set(createShort(
                                rowIdLifetime == RowIdLifetime.ROWID_VALID_TRANSACTION
                                        ? DatabaseMetaData.bestRowTransaction
                                        : DatabaseMetaData.bestRowSession))
                        .at(1).set(getBytes("RDB$DB_KEY"))
                        .at(2).set(createInt(Types.ROWID))
                        .at(3).set(getBytes(getDataTypeName(char_type, 0, CS_BINARY)))
                        .at(4).set(createInt(pseudoColumns.getInt(6)))
                        .at(7).set(createShort(DatabaseMetaData.bestRowPseudo))
                        .toRowValue(true));
            }
        }

        return new FBResultSet(rowDescriptor, rows);
    }

    /**
     * Get primary key of the table as best row identifier.
     *
     * @param table
     *         name of the table.
     * @param valueBuilder
     *         Builder for row values
     * @return list of result set values, when empty, no primary key has been defined for a table or the table does not
     * exist. The returned list can be modified by caller if needed.
     * @throws SQLException
     *         if something went wrong.
     */
    private List<RowValue> getPrimaryKeyIdentifier(String table, final RowValueBuilder valueBuilder)
            throws SQLException {
        try (ResultSet rs = doQuery(GET_BEST_ROW_IDENT, Collections.singletonList(table))) {
            final List<RowValue> rows = new ArrayList<>();
            while (rs.next()) {
                short fieldType = rs.getShort("FIELD_TYPE");
                short fieldSubType = rs.getShort("FIELD_SUB_TYPE");
                short fieldScale = rs.getShort("FIELD_SCALE");
                int characterSetId = rs.getInt("RDB$CHARACTER_SET_ID");
                rows.add(valueBuilder
                        .at(0).set(createShort(DatabaseMetaData.bestRowSession))
                        .at(1).set(getBytes(rs.getString("COLUMN_NAME")))
                        .at(2).set(createShort(getDataType(fieldType, fieldSubType, fieldScale, characterSetId)))
                        .at(3).set(getBytes(getDataTypeName(fieldType, fieldSubType, fieldScale)))
                        .at(4).set(createInt(rs.getInt("FIELD_PRECISION")))
                        .at(6).set(createShort(fieldScale))
                        .at(7).set(createShort(bestRowNotPseudo))
                        .toRowValue(true)
                );
            }
            return rows;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Jaybird considers both {@code RDB$DB_KEY} and {@code RDB$RECORD_VERSION} (Firebird 3 and higher) as version
     * columns.
     * </p>
     * <p>
     * Jaybird only returns pseudo-column as version columns, so 'last updated' columns updated by a trigger,
     * calculated columns, or other forms of change tracking are not reported by this method.
     * </p>
     */
    @Override
    public ResultSet getVersionColumns(String catalog, String schema, String table) throws SQLException {
        final RowDescriptor rowDescriptor = new RowDescriptorBuilder(8, datatypeCoder)
                .at(0).simple(SQL_SHORT, 0, "SCOPE", "VERSIONCOL").addField()
                .at(1).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "COLUMN_NAME", "VERSIONCOL").addField()
                .at(2).simple(SQL_LONG, 0, "DATA_TYPE", "VERSIONCOL").addField()
                .at(3).simple(SQL_VARYING, 31, "TYPE_NAME", "VERSIONCOL").addField()
                .at(4).simple(SQL_LONG, 0, "COLUMN_SIZE", "VERSIONCOL").addField()
                .at(5).simple(SQL_LONG, 0, "BUFFER_LENGTH", "VERSIONCOL").addField()
                .at(6).simple(SQL_SHORT, 0, "DECIMAL_DIGITS", "VERSIONCOL").addField()
                .at(7).simple(SQL_SHORT, 0, "PSEUDO_COLUMN", "VERSIONCOL").addField()
                .toRowDescriptor();

        if (table == null || "".equals(table)) {
            return new FBResultSet(rowDescriptor, Collections.<RowValue>emptyList());
        }

        try (ResultSet pseudoColumns = getPseudoColumns(catalog, schema, escapeWildcards(table), "%")) {
            if (!pseudoColumns.next()) {
                return new FBResultSet(rowDescriptor, Collections.<RowValue>emptyList());
            }

            List<RowValue> rowValues = new ArrayList<>(2);
            RowValueBuilder rowValueBuilder = new RowValueBuilder(rowDescriptor);
            do {
                String columnName = pseudoColumns.getString(4);
                boolean isDbKey = "RDB$DB_KEY".equals(columnName);
                boolean isRecordVersion = !isDbKey && "RDB$RECORD_VERSION".equals(columnName);
                // Protect against future addition of other pseudo columns
                if (!(isDbKey || isRecordVersion)) continue;

                rowValueBuilder
                        .at(1).set(getBytes(columnName))
                        .at(2).set(createInt(pseudoColumns.getInt(5)))
                        .at(3).set(getBytes(isDbKey ? "CHAR" : "BIGINT"))
                        .at(4).set(createInt(pseudoColumns.getInt(6)))
                        .at(5).set(createInt(isDbKey ? pseudoColumns.getInt(11) : 8))
                        .at(6).set(isRecordVersion ? SHORT_ZERO : null)
                        .at(7).set(createShort(DatabaseMetaData.versionColumnPseudo));
                rowValues.add(rowValueBuilder.toRowValue(true));
            } while (pseudoColumns.next());

            return new FBResultSet(rowDescriptor, rowValues);
        }
    }

    private static final String GET_PRIMARY_KEYS = "select "
        + "cast(RC.RDB$RELATION_NAME as " + OBJECT_NAME_TYPE + ") as TABLE_NAME,"
        + "cast(ISGMT.RDB$FIELD_NAME as " + OBJECT_NAME_TYPE + ") as COLUMN_NAME,"
        + "CAST((ISGMT.RDB$FIELD_POSITION + 1) as SMALLINT) as KEY_SEQ,"
        + "cast(RC.RDB$CONSTRAINT_NAME as " + OBJECT_NAME_TYPE + ") as PK_NAME "
        + "from "
        + "RDB$RELATION_CONSTRAINTS RC "
        + "INNER JOIN RDB$INDEX_SEGMENTS ISGMT ON RC.RDB$INDEX_NAME = ISGMT.RDB$INDEX_NAME "
        + "where RC.RDB$RELATION_NAME = " + OBJECT_NAME_PARAMETER
        + "and RC.RDB$CONSTRAINT_TYPE = 'PRIMARY KEY' "
        + "order by ISGMT.RDB$FIELD_NAME ";

    @Override
    public ResultSet getPrimaryKeys(String catalog, String schema, String table) throws SQLException {
        RowDescriptor rowDescriptor = new RowDescriptorBuilder(6, datatypeCoder)
                .at(0).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_CAT", "COLUMNINFO").addField()
                .at(1).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_SCHEM", "COLUMNINFO").addField()
                .at(2).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_NAME", "COLUMNINFO").addField()
                .at(3).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "COLUMN_NAME", "COLUMNINFO").addField()
                .at(4).simple(SQL_SHORT, 0, "KEY_SEQ", "COLUMNINFO").addField()
                .at(5).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "PK_NAME", "COLUMNINFO").addField()
                .toRowDescriptor();

        List<String> params = Collections.singletonList(table);

        try (ResultSet rs = doQuery(GET_PRIMARY_KEYS, params)) {
            // if nothing found, return empty result set
            if (!rs.next()) {
                return new FBResultSet(rowDescriptor, Collections.<RowValue>emptyList());
            }

            final List<RowValue> rows = new ArrayList<>();
            final RowValueBuilder valueBuilder = new RowValueBuilder(rowDescriptor);
            do {
                rows.add(valueBuilder
                        .at(2).set(getBytes(rs.getString("TABLE_NAME")))
                        .at(3).set(getBytes(rs.getString("COLUMN_NAME")))
                        .at(4).set(createShort(rs.getShort("KEY_SEQ")))
                        .at(5).set(getBytes(rs.getString("PK_NAME")))
                        .toRowValue(true)
                );
            } while (rs.next());
            return new FBResultSet(rowDescriptor, rows);
        }
    }

    private static final String GET_IMPORTED_KEYS = "select "
    +"cast(PK.RDB$RELATION_NAME as " + OBJECT_NAME_TYPE + ") as PKTABLE_NAME"
    +",cast(ISP.RDB$FIELD_NAME as " + OBJECT_NAME_TYPE + ") as PKCOLUMN_NAME"
    +",cast(FK.RDB$RELATION_NAME as " + OBJECT_NAME_TYPE + ") as FKTABLE_NAME"
    +",cast(ISF.RDB$FIELD_NAME as " + OBJECT_NAME_TYPE + ") as FKCOLUMN_NAME"
    +",CAST((ISP.RDB$FIELD_POSITION + 1) as SMALLINT) as KEY_SEQ"
    +",cast(RC.RDB$UPDATE_RULE as varchar(11)) as UPDATE_RULE"
    +",cast(RC.RDB$DELETE_RULE as varchar(11)) as DELETE_RULE"
    +",cast(PK.RDB$CONSTRAINT_NAME as " + OBJECT_NAME_TYPE + ") as PK_NAME"
    +",cast(FK.RDB$CONSTRAINT_NAME as " + OBJECT_NAME_TYPE + ") as FK_NAME "
    +"from "
    +"RDB$RELATION_CONSTRAINTS PK"
    +",RDB$RELATION_CONSTRAINTS FK"
    +",RDB$REF_CONSTRAINTS RC"
    +",RDB$INDEX_SEGMENTS ISP"
    +",RDB$INDEX_SEGMENTS ISF "
    +"WHERE FK.RDB$RELATION_NAME = " + OBJECT_NAME_PARAMETER
    +"and FK.RDB$CONSTRAINT_NAME = RC.RDB$CONSTRAINT_NAME "
    +"and PK.RDB$CONSTRAINT_NAME = RC.RDB$CONST_NAME_UQ "
    +"and ISP.RDB$INDEX_NAME = PK.RDB$INDEX_NAME "
    +"and ISF.RDB$INDEX_NAME = FK.RDB$INDEX_NAME "
    +"and ISP.RDB$FIELD_POSITION = ISF.RDB$FIELD_POSITION "
    +"order by 1, 5 ";

    private static final Map<String, byte[]> ACTION_MAPPING;
    static {
        Map<String, byte[]> tempMap = new HashMap<>();
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
     * @param fbAction
     *         Firebird action
     * @return JDBC action encoded as byte array
     */
    private static byte[] mapAction(String fbAction) {
        return ACTION_MAPPING.get(fbAction);
    }

    @Override
    public ResultSet getImportedKeys(String catalog, String schema, String table) throws SQLException {
        final RowDescriptor rowDescriptor = new RowDescriptorBuilder(14, datatypeCoder)
                .at(0).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "PKTABLE_CAT", "COLUMNINFO").addField()
                .at(1).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "PKTABLE_SCHEM", "COLUMNINFO").addField()
                .at(2).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "PKTABLE_NAME", "COLUMNINFO").addField()
                .at(3).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "PKCOLUMN_NAME", "COLUMNINFO").addField()
                .at(4).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "FKTABLE_CAT", "COLUMNINFO").addField()
                .at(5).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "FKTABLE_SCHEM", "COLUMNINFO").addField()
                .at(6).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "FKTABLE_NAME", "COLUMNINFO").addField()
                .at(7).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "FKCOLUMN_NAME", "COLUMNINFO").addField()
                .at(8).simple(SQL_SHORT, 0, "KEY_SEQ", "COLUMNINFO").addField()
                .at(9).simple(SQL_SHORT, 0, "UPDATE_RULE", "COLUMNINFO").addField()
                .at(10).simple(SQL_SHORT, 0, "DELETE_RULE", "COLUMNINFO").addField()
                .at(11).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "FK_NAME", "COLUMNINFO").addField()
                .at(12).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "PK_NAME", "COLUMNINFO").addField()
                .at(13).simple(SQL_SHORT, 0, "DEFERRABILITY", "COLUMNINFO").addField()
                .toRowDescriptor();

        List<String> params = Collections.singletonList(table);

        try (ResultSet rs = doQuery(GET_IMPORTED_KEYS, params)) {
            // if nothing found, return an empty result set
            if (!rs.next()) {
                return new FBResultSet(rowDescriptor, Collections.<RowValue>emptyList());
            }

            final List<RowValue> rows = new ArrayList<>();
            final RowValueBuilder valueBuilder = new RowValueBuilder(rowDescriptor);
            do {
                rows.add(valueBuilder
                        .at(2).set(getBytes(rs.getString("PKTABLE_NAME")))
                        .at(3).set(getBytes(rs.getString("PKCOLUMN_NAME")))
                        .at(6).set(getBytes(rs.getString("FKTABLE_NAME")))
                        .at(7).set(getBytes(rs.getString("FKCOLUMN_NAME")))
                        .at(8).set(createShort(rs.getShort("KEY_SEQ")))
                        .at(9).set(mapAction(rs.getString("UPDATE_RULE")))
                        .at(10).set(mapAction(rs.getString("DELETE_RULE")))
                        .at(11).set(getBytes(rs.getString("FK_NAME")))
                        .at(12).set(getBytes(rs.getString("PK_NAME")))
                        .at(13).set(IMPORTED_KEY_NOT_DEFERRABLE)
                        .toRowValue(true)
                );
            } while (rs.next());
            return new FBResultSet(rowDescriptor, rows);
        }
    }

    private static final String GET_EXPORTED_KEYS = "select "
    +"cast(PK.RDB$RELATION_NAME as " + OBJECT_NAME_TYPE + ") as PKTABLE_NAME"
    +",cast(ISP.RDB$FIELD_NAME as " + OBJECT_NAME_TYPE + ") as PKCOLUMN_NAME"
    +",cast(FK.RDB$RELATION_NAME as " + OBJECT_NAME_TYPE + ") as FKTABLE_NAME"
    +",cast(ISF.RDB$FIELD_NAME as " + OBJECT_NAME_TYPE + ") as FKCOLUMN_NAME"
    +",CAST((ISP.RDB$FIELD_POSITION + 1) as SMALLINT) as KEY_SEQ"
    +",cast(RC.RDB$UPDATE_RULE as varchar(11)) as UPDATE_RULE"
    +",cast(RC.RDB$DELETE_RULE as varchar(11)) as DELETE_RULE"
    +",cast(PK.RDB$CONSTRAINT_NAME as " + OBJECT_NAME_TYPE + ") as PK_NAME"
    +",cast(FK.RDB$CONSTRAINT_NAME as " + OBJECT_NAME_TYPE + ") as FK_NAME "
    +"from "
    +"RDB$RELATION_CONSTRAINTS PK"
    +",RDB$RELATION_CONSTRAINTS FK"
    +",RDB$REF_CONSTRAINTS RC"
    +",RDB$INDEX_SEGMENTS ISP"
    +",RDB$INDEX_SEGMENTS ISF "
    +"WHERE PK.RDB$RELATION_NAME = " + OBJECT_NAME_PARAMETER
    +"and FK.RDB$CONSTRAINT_NAME = RC.RDB$CONSTRAINT_NAME "
    +"and PK.RDB$CONSTRAINT_NAME = RC.RDB$CONST_NAME_UQ "
    +"and ISP.RDB$INDEX_NAME = PK.RDB$INDEX_NAME "
    +"and ISF.RDB$INDEX_NAME = FK.RDB$INDEX_NAME "
    +"and ISP.RDB$FIELD_POSITION = ISF.RDB$FIELD_POSITION "
    +"order by 3, 5 ";

    @Override
    public ResultSet getExportedKeys(String catalog, String schema, String table) throws SQLException {
        final RowDescriptor rowDescriptor = new RowDescriptorBuilder(14, datatypeCoder)
                .at(0).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "PKTABLE_CAT", "COLUMNINFO").addField()
                .at(1).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "PKTABLE_SCHEM", "COLUMNINFO").addField()
                .at(2).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "PKTABLE_NAME", "COLUMNINFO").addField()
                .at(3).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "PKCOLUMN_NAME", "COLUMNINFO").addField()
                .at(4).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "FKTABLE_CAT", "COLUMNINFO").addField()
                .at(5).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "FKTABLE_SCHEM", "COLUMNINFO").addField()
                .at(6).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "FKTABLE_NAME", "COLUMNINFO").addField()
                .at(7).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "FKCOLUMN_NAME", "COLUMNINFO").addField()
                .at(8).simple(SQL_SHORT, 0, "KEY_SEQ", "COLUMNINFO").addField()
                .at(9).simple(SQL_SHORT, 0, "UPDATE_RULE", "COLUMNINFO").addField()
                .at(10).simple(SQL_SHORT, 0, "DELETE_RULE", "COLUMNINFO").addField()
                .at(11).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "FK_NAME", "COLUMNINFO").addField()
                .at(12).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "PK_NAME", "COLUMNINFO").addField()
                .at(13).simple(SQL_SHORT, 0, "DEFERRABILITY", "COLUMNINFO").addField()
                .toRowDescriptor();

        List<String> params = Collections.singletonList(table);

        try (ResultSet rs = doQuery(GET_EXPORTED_KEYS, params)) {
            // if nothing found, return an empty result set
            if (!rs.next()) {
                return new FBResultSet(rowDescriptor, Collections.<RowValue>emptyList());
            }

            List<RowValue> rows = new ArrayList<>();
            final RowValueBuilder valueBuilder = new RowValueBuilder(rowDescriptor);
            do {
                rows.add(valueBuilder
                        .at(2).set(getBytes(rs.getString("PKTABLE_NAME")))
                        .at(3).set(getBytes(rs.getString("PKCOLUMN_NAME")))
                        .at(6).set(getBytes(rs.getString("FKTABLE_NAME")))
                        .at(7).set(getBytes(rs.getString("FKCOLUMN_NAME")))
                        .at(8).set(createShort(rs.getShort("KEY_SEQ")))
                        .at(9).set(mapAction(rs.getString("UPDATE_RULE")))
                        .at(10).set(mapAction(rs.getString("DELETE_RULE")))
                        .at(11).set(getBytes(rs.getString("FK_NAME")))
                        .at(12).set(getBytes(rs.getString("PK_NAME")))
                        .at(13).set(IMPORTED_KEY_NOT_DEFERRABLE)
                        .toRowValue(true)
                );
            } while (rs.next());
            return new FBResultSet(rowDescriptor, rows);
        }
    }

    private static final String GET_CROSS_KEYS = "select "
    +"cast(PK.RDB$RELATION_NAME as " + OBJECT_NAME_TYPE + ") as PKTABLE_NAME"
    +",cast(ISP.RDB$FIELD_NAME as " + OBJECT_NAME_TYPE + ") as PKCOLUMN_NAME"
    +",cast(FK.RDB$RELATION_NAME as " + OBJECT_NAME_TYPE + ") as FKTABLE_NAME"
    +",cast(ISF.RDB$FIELD_NAME as " + OBJECT_NAME_TYPE + ") as FKCOLUMN_NAME"
    +",CAST((ISP.RDB$FIELD_POSITION + 1) as SMALLINT) as KEY_SEQ"
    +",cast(RC.RDB$UPDATE_RULE as varchar(11)) as UPDATE_RULE"
    +",cast(RC.RDB$DELETE_RULE as varchar(11)) as DELETE_RULE"
    +",cast(PK.RDB$CONSTRAINT_NAME as " + OBJECT_NAME_TYPE + ") as PK_NAME"
    +",cast(FK.RDB$CONSTRAINT_NAME as " + OBJECT_NAME_TYPE + ") as FK_NAME"
    +" from "
    +"RDB$RELATION_CONSTRAINTS PK"
    +",RDB$RELATION_CONSTRAINTS FK"
    +",RDB$REF_CONSTRAINTS RC"
    +",RDB$INDEX_SEGMENTS ISP"
    +",RDB$INDEX_SEGMENTS ISF "
    +"WHERE PK.RDB$RELATION_NAME = " + OBJECT_NAME_PARAMETER
    +"and FK.RDB$RELATION_NAME = " + OBJECT_NAME_PARAMETER
    +"and FK.RDB$CONSTRAINT_NAME = RC.RDB$CONSTRAINT_NAME "
    +"and PK.RDB$CONSTRAINT_NAME = RC.RDB$CONST_NAME_UQ "
    +"and ISP.RDB$INDEX_NAME = PK.RDB$INDEX_NAME "
    +"and ISF.RDB$INDEX_NAME = FK.RDB$INDEX_NAME "
    +"and ISP.RDB$FIELD_POSITION = ISF.RDB$FIELD_POSITION "
    +"order by 3, 5 ";

    @Override
    public ResultSet getCrossReference(
            String primaryCatalog, String primarySchema, String primaryTable,
            String foreignCatalog, String foreignSchema, String foreignTable) throws SQLException {
        final RowDescriptor rowDescriptor = new RowDescriptorBuilder(14, datatypeCoder)
                .at(0).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "PKTABLE_CAT", "COLUMNINFO").addField()
                .at(1).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "PKTABLE_SCHEM", "COLUMNINFO").addField()
                .at(2).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "PKTABLE_NAME", "COLUMNINFO").addField()
                .at(3).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "PKCOLUMN_NAME", "COLUMNINFO").addField()
                .at(4).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "FKTABLE_CAT", "COLUMNINFO").addField()
                .at(5).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "FKTABLE_SCHEM", "COLUMNINFO").addField()
                .at(6).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "FKTABLE_NAME", "COLUMNINFO").addField()
                .at(7).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "FKCOLUMN_NAME", "COLUMNINFO").addField()
                .at(8).simple(SQL_SHORT, 0, "KEY_SEQ", "COLUMNINFO").addField()
                .at(9).simple(SQL_SHORT, 0, "UPDATE_RULE", "COLUMNINFO").addField()
                .at(10).simple(SQL_SHORT, 0, "DELETE_RULE", "COLUMNINFO").addField()
                .at(11).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "FK_NAME", "COLUMNINFO").addField()
                .at(12).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "PK_NAME", "COLUMNINFO").addField()
                .at(13).simple(SQL_SHORT, 0, "DEFERRABILITY", "COLUMNINFO").addField()
                .toRowDescriptor();

        final List<String> params = Arrays.asList(primaryTable, foreignTable);

        try (ResultSet rs = doQuery(GET_CROSS_KEYS, params)) {
            // return empty result set if nothing found
            if (!rs.next()) {
                return new FBResultSet(rowDescriptor, Collections.<RowValue>emptyList());
            }

            final List<RowValue> rows = new ArrayList<>();
            final RowValueBuilder valueBuilder = new RowValueBuilder(rowDescriptor);
            do {
                rows.add(valueBuilder
                        .at(2).set(getBytes(rs.getString("PKTABLE_NAME")))
                        .at(3).set(getBytes(rs.getString("PKCOLUMN_NAME")))
                        .at(6).set(getBytes(rs.getString("FKTABLE_NAME")))
                        .at(7).set(getBytes(rs.getString("FKCOLUMN_NAME")))
                        .at(8).set(createShort(rs.getShort("KEY_SEQ")))
                        .at(9).set(mapAction(rs.getString("UPDATE_RULE")))
                        .at(10).set(mapAction(rs.getString("DELETE_RULE")))
                        .at(11).set(getBytes(rs.getString("FK_NAME")))
                        .at(12).set(getBytes(rs.getString("PK_NAME")))
                        .at(13).set(IMPORTED_KEY_NOT_DEFERRABLE)
                        .toRowValue(true)
                );
            } while (rs.next());
            return new FBResultSet(rowDescriptor, rows);
        }
    }

    /**
     * Function to convert integer values to encoded byte arrays for shorts.
     *
     * @param value
     *         integer value to convert
     * @return encoded byte array representing the value
     */
    private static byte[] createShort(int value) {
        assert (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) : String.format("Value \"%d\" outside range of short", value);
        return datatypeCoder.encodeShort(value);
    }

    /**
     * Function to convert integer values to encoded byte arrays for integers.
     *
     * @param value
     *         integer value to convert
     * @return encoded byte array representing the value
     */
    private static byte[] createInt(int value) {
        return datatypeCoder.encodeInt(value);
    }

    @Override
    public ResultSet getTypeInfo() throws SQLException {
        final RowDescriptor rowDescriptor = new RowDescriptorBuilder(18, datatypeCoder)
                .at(0).simple(SQL_VARYING, 31, "TYPE_NAME", "TYPEINFO").addField()
                .at(1).simple(SQL_LONG, 0, "DATA_TYPE", "TYPEINFO").addField()
                .at(2).simple(SQL_LONG, 0, "PRECISION", "TYPEINFO").addField()
                .at(3).simple(SQL_VARYING, 1, "LITERAL_PREFIX", "TYPEINFO").addField()
                .at(4).simple(SQL_VARYING, 1, "LITERAL_SUFFIX", "TYPEINFO").addField()
                .at(5).simple(SQL_VARYING, 31, "CREATE_PARAMS", "TYPEINFO").addField()
                .at(6).simple(SQL_SHORT, 0, "NULLABLE", "TYPEINFO").addField()
                .at(7).simple(SQL_TEXT, 1, "CASE_SENSITIVE", "TYPEINFO").addField()
                .at(8).simple(SQL_SHORT, 0, "SEARCHABLE", "TYPEINFO").addField()
                .at(9).simple(SQL_TEXT, 1, "UNSIGNED_ATTRIBUTE", "TYPEINFO").addField()
                .at(10).simple(SQL_TEXT, 1, "FIXED_PREC_SCALE", "TYPEINFO").addField()
                .at(11).simple(SQL_TEXT, 1, "AUTO_INCREMENT", "TYPEINFO").addField()
                .at(12).simple(SQL_VARYING, 31, "LOCAL_TYPE_NAME", "TYPEINFO").addField()
                .at(13).simple(SQL_SHORT, 0, "MINIMUM_SCALE", "TYPEINFO").addField()
                .at(14).simple(SQL_SHORT, 0, "MAXIMUM_SCALE", "TYPEINFO").addField()
                .at(15).simple(SQL_LONG, 0, "SQL_DATA_TYPE", "TYPEINFO").addField()
                .at(16).simple(SQL_LONG, 0, "SQL_DATETIME_SUB", "TYPEINFO").addField()
                .at(17).simple(SQL_LONG, 0, "NUM_PREC_RADIX", "TYPEINFO").addField()
                .toRowDescriptor();

        final byte[] blobTypePred = firebirdSupportInfo.supportsFullSearchableBlobs()
                ? TYPE_SEARCHABLE
                : TYPE_PRED_BASIC;

        //dialect 3 only
        final List<RowValue> rows = new ArrayList<>(20);

        // DECFLOAT=-6001 (TODO Change when standardized)
        if (firebirdSupportInfo.supportsDecfloat()) {
            rows.add(RowValue.of(rowDescriptor,
                    getBytes("DECFLOAT"), createInt(JaybirdTypeCodes.DECFLOAT), DECFLOAT_34_PRECISION, null, null,
                    getBytes("precision"), TYPE_NULLABLE, CASEINSENSITIVE, TYPE_SEARCHABLE, SIGNED, VARIABLESCALE,
                    NOTAUTOINC, null, SHORT_ZERO, SHORT_ZERO, createInt(SQL_DEC34), null, RADIX_TEN));
        }

        //BIGINT=-5
        rows.add(RowValue.of(rowDescriptor,
                getBytes("BIGINT"), createInt(Types.BIGINT), BIGINT_PRECISION, null, null, null,
                TYPE_NULLABLE, CASEINSENSITIVE, TYPE_SEARCHABLE, SIGNED, FIXEDSCALE, NOTAUTOINC, null, SHORT_ZERO,
                SHORT_ZERO, createInt(SQL_INT64), null, RADIX_TEN));

        //LONGVARBINARY=-4
        rows.add(RowValue.of(rowDescriptor,
                getBytes("BLOB SUB_TYPE BINARY"), createInt(Types.LONGVARBINARY), INT_ZERO, null, null,
                null, TYPE_NULLABLE, CASESENSITIVE, blobTypePred, UNSIGNED, FIXEDSCALE, NOTAUTOINC, null,
                SHORT_ZERO, SHORT_ZERO, createInt(SQL_BLOB), null, RADIX_TEN));

        //VARBINARY=-3
        rows.add(RowValue.of(rowDescriptor,
                getBytes("VARCHAR"), createInt(Types.VARBINARY), createInt(32765), null, null, getBytes("length"),
                TYPE_NULLABLE, CASESENSITIVE, TYPE_SEARCHABLE, UNSIGNED, FIXEDSCALE, NOTAUTOINC, null, SHORT_ZERO,
                SHORT_ZERO, createInt(SQL_VARYING), null, RADIX_TEN));

        //BINARY=-2
        rows.add(RowValue.of(rowDescriptor,
                getBytes("CHAR"), createInt(Types.BINARY), createInt(32767), null, null, getBytes("length"),
                TYPE_NULLABLE, CASESENSITIVE, TYPE_SEARCHABLE, UNSIGNED, FIXEDSCALE, NOTAUTOINC, null, SHORT_ZERO,
                SHORT_ZERO, createInt(SQL_TEXT), null, RADIX_TEN));

        //LONGVARCHAR=-1
        rows.add(RowValue.of(rowDescriptor,
                getBytes("BLOB SUB_TYPE TEXT"), createInt(Types.LONGVARCHAR), INT_ZERO, getBytes("'"), getBytes("'"),
                null, TYPE_NULLABLE, CASESENSITIVE, blobTypePred, UNSIGNED, FIXEDSCALE, NOTAUTOINC, null,
                SHORT_ZERO, SHORT_ZERO, createInt(SQL_BLOB), null, RADIX_TEN));

        //CHAR=1
        rows.add(RowValue.of(rowDescriptor,
                getBytes("CHAR"), createInt(Types.CHAR), createInt(32767), getBytes("'"),
                getBytes("'"), getBytes("length"), TYPE_NULLABLE, CASESENSITIVE, TYPE_SEARCHABLE, UNSIGNED,
                FIXEDSCALE, NOTAUTOINC, null, SHORT_ZERO, SHORT_ZERO, createInt(SQL_TEXT), null,
                RADIX_TEN));

        // also for numeric
        final byte[] maxDecimalPrecision = createInt(firebirdSupportInfo.maxDecimalPrecision());
        final byte[] maxDecimalScale = maxDecimalPrecision;
        //NUMERIC=2
        rows.add(RowValue.of(rowDescriptor,
                getBytes("NUMERIC"), createInt(Types.NUMERIC), maxDecimalPrecision, null, null,
                getBytes("precision,scale"), TYPE_NULLABLE, CASEINSENSITIVE, TYPE_SEARCHABLE, SIGNED, FIXEDSCALE,
                NOTAUTOINC, null, SHORT_ZERO, maxDecimalScale, createInt(SQL_INT64), null, RADIX_TEN));

        //DECIMAL=3
        rows.add(RowValue.of(rowDescriptor,
                getBytes("DECIMAL"), createInt(Types.DECIMAL), maxDecimalPrecision, null, null,
                getBytes("precision,scale"), TYPE_NULLABLE, CASEINSENSITIVE, TYPE_SEARCHABLE, SIGNED, FIXEDSCALE,
                NOTAUTOINC, null, SHORT_ZERO, maxDecimalScale, createInt(SQL_INT64), null, RADIX_TEN));

        //INTEGER=4
        rows.add(RowValue.of(rowDescriptor,
                getBytes("INTEGER"), createInt(Types.INTEGER), INTEGER_PRECISION, null, null, null,
                TYPE_NULLABLE, CASEINSENSITIVE, TYPE_SEARCHABLE, SIGNED, FIXEDSCALE, NOTAUTOINC, null, SHORT_ZERO,
                SHORT_ZERO, createInt(SQL_LONG), null, RADIX_TEN));

        //SMALLINT=5
        rows.add(RowValue.of(rowDescriptor,
                getBytes("SMALLINT"), createInt(Types.SMALLINT), SMALLINT_PRECISION, null, null,
                null, TYPE_NULLABLE, CASEINSENSITIVE, TYPE_SEARCHABLE, SIGNED, FIXEDSCALE, NOTAUTOINC, null,
                SHORT_ZERO, SHORT_ZERO, createInt(SQL_SHORT), null, RADIX_TEN));

        //FLOAT=6
        rows.add(RowValue.of(rowDescriptor,
                getBytes("FLOAT"), createInt(Types.FLOAT), FLOAT_PRECISION, null, null, null,
                TYPE_NULLABLE, CASEINSENSITIVE, TYPE_SEARCHABLE, SIGNED, VARIABLESCALE, NOTAUTOINC, null, SHORT_ZERO,
                SHORT_ZERO, createInt(SQL_FLOAT), null, RADIX_TEN));

        //DOUBLE=8
        rows.add(RowValue.of(rowDescriptor,
                getBytes("DOUBLE PRECISION"), createInt(Types.DOUBLE), DOUBLE_PRECISION, null, null,
                null, TYPE_NULLABLE, CASEINSENSITIVE, TYPE_SEARCHABLE, SIGNED, VARIABLESCALE, NOTAUTOINC, null,
                SHORT_ZERO, SHORT_ZERO, createInt(SQL_DOUBLE), null, RADIX_TEN));

        //VARCHAR=12
        rows.add(RowValue.of(rowDescriptor,
                getBytes("VARCHAR"), createInt(Types.VARCHAR), createInt(32765), getBytes("'"),
                getBytes("'"), getBytes("length"), TYPE_NULLABLE, CASESENSITIVE, TYPE_SEARCHABLE, UNSIGNED,
                FIXEDSCALE, NOTAUTOINC, null, SHORT_ZERO, SHORT_ZERO, createInt(SQL_VARYING), null,
                RADIX_TEN));

        //BOOLEAN=16
        if (getDatabaseMajorVersion() >= 3) {
            rows.add(RowValue.of(rowDescriptor,
                    getBytes("BOOLEAN"), createInt(Types.BOOLEAN), BOOLEAN_PRECISION,
                    null, null, null, TYPE_NULLABLE, CASEINSENSITIVE, TYPE_PRED_BASIC, UNSIGNED, FIXEDSCALE,
                    NOTAUTOINC, null, SHORT_ZERO, SHORT_ZERO, createInt(SQL_BOOLEAN), null, RADIX_BINARY));
        }

        //DATE=91
        rows.add(RowValue.of(rowDescriptor,
                getBytes("DATE"), createInt(Types.DATE), DATE_PRECISION, getBytes("date'"), getBytes("'"), null,
                TYPE_NULLABLE, CASEINSENSITIVE, TYPE_SEARCHABLE, UNSIGNED, FIXEDSCALE, NOTAUTOINC, null, SHORT_ZERO,
                SHORT_ZERO, createInt(SQL_TYPE_DATE), null, RADIX_TEN));

        //TIME=92
        rows.add(RowValue.of(rowDescriptor,
                getBytes("TIME"), createInt(Types.TIME), TIME_PRECISION, getBytes("time'"), getBytes("'"), null,
                TYPE_NULLABLE, CASEINSENSITIVE, TYPE_SEARCHABLE, UNSIGNED, FIXEDSCALE, NOTAUTOINC, null, SHORT_ZERO,
                SHORT_ZERO, createInt(SQL_TYPE_TIME), null, RADIX_TEN));

        //TIMESTAMP=93
        rows.add(RowValue.of(rowDescriptor,
                getBytes("TIMESTAMP"), createInt(Types.TIMESTAMP), TIMESTAMP_PRECISION, getBytes("timestamp'"),
                getBytes("'"), null, TYPE_NULLABLE, CASEINSENSITIVE, TYPE_SEARCHABLE, UNSIGNED, FIXEDSCALE, NOTAUTOINC,
                null, SHORT_ZERO, SHORT_ZERO, createInt(SQL_TIMESTAMP), null, RADIX_TEN));

        //OTHER=1111
        rows.add(RowValue.of(rowDescriptor,
                getBytes("ARRAY"), createInt(Types.OTHER), INT_ZERO, null, null, null, TYPE_NULLABLE,
                CASESENSITIVE, TYPE_PRED_NONE, UNSIGNED, FIXEDSCALE, NOTAUTOINC, null, SHORT_ZERO, SHORT_ZERO,
                createInt(SQL_ARRAY), null, RADIX_TEN));

        //BLOB=2004
        // Should we split this into all negative blob types currently known in the DB?
        // Blob is potentially searchable with like, etc, acting as if it isn't.
        rows.add(RowValue.of(rowDescriptor,
                getBytes("BLOB SUB_TYPE <0 "), createInt(Types.BLOB), INT_ZERO, null, null, null,
                TYPE_NULLABLE, CASESENSITIVE, TYPE_PRED_NONE, UNSIGNED, FIXEDSCALE, NOTAUTOINC, null, SHORT_ZERO,
                SHORT_ZERO, createInt(SQL_BLOB), null, RADIX_TEN));

        return new FBResultSet(rowDescriptor, rows);
    }

    private static final String GET_INDEX_INFO = "SELECT "
        + "cast(ind.RDB$RELATION_NAME as " + OBJECT_NAME_TYPE + ") AS TABLE_NAME"
        + ",ind.RDB$UNIQUE_FLAG AS UNIQUE_FLAG"
        + ",cast(ind.RDB$INDEX_NAME as " + OBJECT_NAME_TYPE + ") as INDEX_NAME"
        + ",ise.rdb$field_position + 1 as ORDINAL_POSITION"
        + ",cast(ise.rdb$field_name as " + OBJECT_NAME_TYPE + ") as COLUMN_NAME"
        + ",ind.RDB$EXPRESSION_SOURCE as EXPRESSION_SOURCE"
        + ",ind.RDB$INDEX_TYPE as ASC_OR_DESC "
        + "FROM "
        + "rdb$indices ind "
        + "LEFT JOIN rdb$index_segments ise ON ind.rdb$index_name = ise.rdb$index_name "
        + "WHERE "
        + "ind.rdb$relation_name = " + OBJECT_NAME_PARAMETER
        + "ORDER BY 2, 3, 4";

    @Override
    public ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique, boolean approximate)
            throws SQLException {
        final RowDescriptor rowDescriptor = new RowDescriptorBuilder(13, datatypeCoder)
                .at(0).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_CAT", "INDEXINFO").addField()
                .at(1).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_SCHEM", "INDEXINFO").addField()
                .at(2).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_NAME", "INDEXINFO").addField()
                .at(3).simple(SQL_TEXT, 1, "NON_UNIQUE", "INDEXINFO").addField()
                .at(4).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "INDEX_QUALIFIER", "INDEXINFO").addField()
                .at(5).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "INDEX_NAME", "INDEXINFO").addField()
                .at(6).simple(SQL_SHORT, 0, "TYPE", "INDEXINFO").addField()
                .at(7).simple(SQL_SHORT, 0, "ORDINAL_POSITION", "INDEXINFO").addField()
                // Field with EXPRESSION_SOURCE (used for expression indexes) in Firebird is actually a blob, using Integer.MAX_VALUE for length
                .at(8).simple(SQL_VARYING, Integer.MAX_VALUE, "COLUMN_NAME", "INDEXINFO").addField()
                .at(9).simple(SQL_VARYING, 31, "ASC_OR_DESC", "INDEXINFO").addField()
                .at(10).simple(SQL_LONG, 0, "CARDINALITY", "INDEXINFO").addField()
                .at(11).simple(SQL_LONG, 0, "PAGES", "INDEXINFO").addField()
                .at(12).simple(SQL_VARYING, 31, "FILTER_CONDITION", "INDEXINFO").addField()
                .toRowDescriptor();

        List<String> params = Collections.singletonList(table);

        try (ResultSet rs = doQuery(GET_INDEX_INFO, params)) {
            if (!rs.next()) {
                return new FBResultSet(rowDescriptor, Collections.<RowValue>emptyList());
            }

            final List<RowValue> rows = new ArrayList<>();
            final RowValueBuilder valueBuilder = new RowValueBuilder(rowDescriptor);
            do {
                final boolean isNotUnique = rs.getInt("UNIQUE_FLAG") == 0;
                if (unique && isNotUnique) {
                    // Skip indices that are not unique, as requested
                    continue;
                }
                valueBuilder
                        .at(2).set(getBytes(rs.getString("TABLE_NAME")))
                        .at(3).set(isNotUnique ? TRUE_BYTES : FALSE_BYTES)
                        .at(5).set(getBytes(rs.getString("INDEX_NAME")))
                        .at(6).set(TABLE_INDEX_OTHER);
                String columnName = rs.getString("COLUMN_NAME");
                if (rs.wasNull()) {
                    valueBuilder.at(7).set(SHORT_ONE);
                    String expressionSource = rs.getString("EXPRESSION_SOURCE");
                    if (expressionSource != null) {
                        valueBuilder.at(8).set(getBytes(expressionSource));
                    }
                } else {
                    valueBuilder
                            .at(7).set(createShort(rs.getShort("ORDINAL_POSITION")))
                            .at(8).set(getBytes(columnName));
                }
                int ascOrDesc = rs.getInt("ASC_OR_DESC");
                if (ascOrDesc == 0) {
                    valueBuilder.at(9).set(ASC_BYTES);
                } else if (ascOrDesc == 1) {
                    valueBuilder.at(9).set(DESC_BYTES);
                }
                // NOTE: We are setting CARDINALITY and PAGES to NULL as we don't have this info; might contravene JDBC spec
                // TODO index 10: use 1 / RDB$STATISTICS for approximation of CARDINALITY?
                // TODO index 11: query RDB$PAGES for PAGES information?

                rows.add(valueBuilder.toRowValue(true));
            } while (rs.next());
            return new FBResultSet(rowDescriptor, rows);
        }
    }

    @Override
    public boolean supportsResultSetType(int type) throws SQLException {
        // TODO Return false for TYPE_SCROLL_SENSITVE as we only support it by downgrading to INSENSITIVE?
        switch (type){
            case ResultSet.TYPE_FORWARD_ONLY:
            case ResultSet.TYPE_SCROLL_INSENSITIVE :
            case ResultSet.TYPE_SCROLL_SENSITIVE :
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean supportsResultSetConcurrency(int type, int concurrency) throws SQLException {
        // TODO Return false for TYPE_SCROLL_SENSITVE as we only support it by downgrading to INSENSITIVE?
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

    @Override
    public boolean ownUpdatesAreVisible(int type) throws SQLException {
        // TODO Return false for TYPE_SCROLL_SENSITVE as we only support it by downgrading to INSENSITIVE?
        return ResultSet.TYPE_SCROLL_INSENSITIVE == type ||
                ResultSet.TYPE_SCROLL_SENSITIVE == type;
    }

    @Override
    public boolean ownDeletesAreVisible(int type) throws SQLException {
        // TODO Return false for TYPE_SCROLL_SENSITVE as we only support it by downgrading to INSENSITIVE?
        return ResultSet.TYPE_SCROLL_INSENSITIVE == type ||
                ResultSet.TYPE_SCROLL_SENSITIVE == type;
    }

    @Override
    public boolean ownInsertsAreVisible(int type) throws SQLException {
        // TODO Return false for TYPE_SCROLL_SENSITVE as we only support it by downgrading to INSENSITIVE?
        return ResultSet.TYPE_SCROLL_INSENSITIVE == type ||
                ResultSet.TYPE_SCROLL_SENSITIVE == type;
    }

    @Override
    public boolean othersUpdatesAreVisible(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean othersDeletesAreVisible(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean othersInsertsAreVisible(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean updatesAreDetected(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean deletesAreDetected(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean insertsAreDetected(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean supportsBatchUpdates() throws SQLException {
        return true;
    }

    /**
     * {@inheritDoc}
     * <p>
     * UDTs are not supported by Firebird. This method will always return an empty ResultSet.
     * </p>
     */
    @Override
    public ResultSet getUDTs(String catalog, String schemaPattern, String typeNamePattern, int[] types)
            throws SQLException {
        final RowDescriptor rowDescriptor = new RowDescriptorBuilder(7, datatypeCoder)
                .at(0).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TYPE_CAT", "UDT").addField()
                .at(1).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TYPE_SCHEM", "UDT").addField()
                .at(2).simple(SQL_VARYING, 31, "TYPE_NAME", "UDT").addField()
                .at(3).simple(SQL_VARYING, 31, "CLASS_NAME", "UDT").addField()
                .at(4).simple(SQL_LONG, 0, "DATA_TYPE", "UDT").addField()
                .at(5).simple(SQL_VARYING, 31, "REMARKS", "UDT").addField()
                .at(6).simple(SQL_SHORT, 0, "BASE_TYPE", "UDT").addField()
                .toRowDescriptor();

        return new FBResultSet(rowDescriptor, Collections.<RowValue>emptyList());
    }

    @Override
    public Connection getConnection() throws SQLException {
        return connection;
    }

    /**
     * {@inheritDoc}
     * <p>
     * UDTs are not supported by Firebird. This method will always return an empty ResultSet.
     * </p>
     */
    @Override
    public ResultSet getAttributes(String catalog, String schemaPattern, String typeNamePattern,
            String attributeNamePattern) throws SQLException {
        final RowDescriptor rowDescriptor = new RowDescriptorBuilder(21, datatypeCoder)
                .at(0).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TYPE_CAT", "ATTRIBUTES").addField()
                .at(1).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TYPE_SCHEM", "ATTRIBUTES").addField()
                .at(2).simple(SQL_VARYING, 31, "TYPE_NAME", "ATTRIBUTES").addField()
                .at(3).simple(SQL_VARYING, 31, "ATTR_NAME", "ATTRIBUTES").addField()
                .at(4).simple(SQL_LONG, 0, "DATA_TYPE", "ATTRIBUTES").addField()
                .at(5).simple(SQL_VARYING, 31, "ATTR_TYPE_NAME", "ATTRIBUTES").addField()
                .at(6).simple(SQL_LONG, 0, "ATTR_SIZE", "ATTRIBUTES").addField()
                .at(7).simple(SQL_LONG, 0, "DECIMAL_DIGITS", "ATTRIBUTES").addField()
                .at(8).simple(SQL_LONG, 0, "NUM_PREC_RADIX", "ATTRIBUTES").addField()
                .at(9).simple(SQL_LONG, 0, "NULLABLE", "ATTRIBUTES").addField()
                .at(10).simple(SQL_VARYING, 80, "REMARKS", "ATTRIBUTES").addField()
                .at(11).simple(SQL_VARYING, 31, "ATTR_DEF", "ATTRIBUTES").addField()
                .at(12).simple(SQL_LONG, 0, "SQL_DATA_TYPE", "ATTRIBUTES").addField()
                .at(13).simple(SQL_LONG, 0, "SQL_DATETIME_SUB", "ATTRIBUTES").addField()
                .at(14).simple(SQL_LONG, 0, "CHAR_OCTET_LENGTH", "ATTRIBUTES").addField()
                .at(15).simple(SQL_SHORT, 0, "ORDINAL_POSITION", "ATTRIBUTES").addField()
                .at(16).simple(SQL_VARYING, 31, "IS_NULLABLE", "ATTRIBUTES").addField()
                .at(17).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "SCOPE_CATALOG", "ATTRIBUTES").addField()
                .at(18).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "SCOPE_SCHEMA", "ATTRIBUTES").addField()
                .at(19).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "SCOPE_TABLE", "ATTRIBUTES").addField()
                .at(20).simple(SQL_SHORT, 0, "SOURCE_DATA_TYPE", "ATTRIBUTES").addField()
                .toRowDescriptor();

        return new FBResultSet(rowDescriptor, Collections.<RowValue>emptyList());
    }

    @Override
    public boolean supportsSavepoints() throws SQLException {
        return firebirdSupportInfo.supportsSavepoint();
    }

    @Override
    public boolean supportsNamedParameters() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsMultipleOpenResults() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsGetGeneratedKeys() throws SQLException {
        return firebirdSupportInfo.supportsInsertReturning()
                && GeneratedKeysSupportFactory.isGeneratedKeysSupportLoaded()
                && connection.getGeneratedKeysSupport().supportsGetGeneratedKeys();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Supertypes are not supported by Firebird. This method will always return an empty ResultSet.
     * </p>
     */
    @Override
    public ResultSet getSuperTypes(String catalog, String schemaPattern, String tableNamePattern) throws SQLException {
        final RowDescriptor rowDescriptor = new RowDescriptorBuilder(6, datatypeCoder)
                .at(0).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TYPE_CAT", "SUPERTYPES").addField()
                .at(1).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TYPE_SCHEM", "SUPERTYPES").addField()
                .at(2).simple(SQL_VARYING, 31, "TYPE_NAME", "SUPERTYPES").addField()
                .at(3).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "SUPERTYPE_CAT", "SUPERTYPES").addField()
                .at(4).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "SUPERTYPE_SCHEM", "SUPERTYPES").addField()
                .at(5).simple(SQL_VARYING, 31, "SUPERTYPE_NAME", "SUPERTYPES").addField()
                .toRowDescriptor();

        return new FBResultSet(rowDescriptor, Collections.<RowValue>emptyList());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Supertables are not supported by Firebird. This method will always return an empty ResultSet.
     * </p>
     */
    @Override
    public ResultSet getSuperTables(String catalog, String schemaPattern, String tableNamePattern) throws SQLException {
        final RowDescriptor rowDescriptor = new RowDescriptorBuilder(4, datatypeCoder)
                .at(0).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_CAT", "SUPERTABLES").addField()
                .at(1).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_SCHEM", "SUPERTABLES").addField()
                .at(2).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_NAME", "SUPERTABLES").addField()
                .at(3).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "SUPERTABLE_NAME", "SUPERTABLES").addField()
                .toRowDescriptor();

        return new FBResultSet(rowDescriptor, Collections.<RowValue>emptyList());
    }

    @Override
    public boolean supportsResultSetHoldability(int holdability) throws SQLException {
        return holdability == ResultSet.CLOSE_CURSORS_AT_COMMIT ||
                holdability == ResultSet.HOLD_CURSORS_OVER_COMMIT;
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        // TODO Retrieve default holdable connection property
        return ResultSet.CLOSE_CURSORS_AT_COMMIT;
    }

    @Override
    public int getDatabaseMajorVersion() throws SQLException {
        return gdsHelper.getDatabaseProductMajorVersion();
    }

    @Override
    public int getDatabaseMinorVersion() throws SQLException {
        return gdsHelper.getDatabaseProductMinorVersion();
    }

    @Override
    public int getOdsMajorVersion() throws SQLException {
        return gdsHelper.getCurrentDatabase().getOdsMajor();
    }

    @Override
    public int getOdsMinorVersion() throws SQLException {
        return gdsHelper.getCurrentDatabase().getOdsMinor();
    }

    @Override
    public int getDatabaseDialect() throws SQLException {
        return gdsHelper.getCurrentDatabase().getDatabaseDialect();
    }

    @Override
    public int getConnectionDialect() throws SQLException {
        return gdsHelper.getCurrentDatabase().getConnectionDialect();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Firebird primarily uses SQL standard SQL states, but may occasionally use values from X/Open.
     * </p>
     */
    @Override
    public int getSQLStateType() throws SQLException {
        return DatabaseMetaData.sqlStateSQL;
    }

    @Override
    public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException {
        return false;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The holdable result sets remain open, others are closed, but this happens before the statement is executed.
     * </p>
     */
    @Override
    public boolean autoCommitFailureClosesAllResultSets() throws SQLException {
        // the holdable result sets remain open, others are closed, but this
        // happens before the statement is executed
        return false;
    }

    @Override
    public ResultSet getClientInfoProperties() throws SQLException {
        // TODO Return context info?
        final RowDescriptor rowDescriptor = new RowDescriptorBuilder(4, datatypeCoder)
                .at(0).simple(SQL_VARYING, 31, "NAME", "CLIENTINFO").addField()
                .at(1).simple(SQL_LONG, 4, "MAX_LEN", "CLIENTINFO").addField()
                .at(2).simple(SQL_VARYING, 31, "DEFAULT", "CLIENTINFO").addField()
                .at(3).simple(SQL_VARYING, 31, "DESCRIPTION", "CLIENTINFO").addField()
                .toRowDescriptor();

        return new FBResultSet(rowDescriptor, Collections.<RowValue>emptyList());
    }

    @Override
    public ResultSet getFunctionColumns(String catalog, String schemaPattern, String functionNamePattern,
            String columnNamePattern) throws SQLException {
        // FIXME implement this method to return actual result
        final RowDescriptor rowDescriptor = new RowDescriptorBuilder(17, datatypeCoder)
                .at(0).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "FUNCTION_CAT", "FUNCTION_COLUMNS").addField()
                .at(1).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "FUNCTION_SCHEM", "FUNCTION_COLUMNS").addField()
                .at(2).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "FUNCTION_NAME", "FUNCTION_COLUMNS").addField()
                .at(3).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "COLUMN_NAME", "FUNCTION_COLUMNS").addField()
                .at(4).simple(SQL_SHORT, 0, "COLUMN_TYPE", "FUNCTION_COLUMNS").addField()
                .at(5).simple(SQL_LONG, 0, "DATA_TYPE", "FUNCTION_COLUMNS").addField()
                .at(6).simple(SQL_VARYING, 31, "TYPE_NAME", "FUNCTION_COLUMNS").addField()
                .at(7).simple(SQL_LONG, 0, "PRECISION", "FUNCTION_COLUMNS").addField()
                .at(8).simple(SQL_LONG, 0, "LENGTH", "FUNCTION_COLUMNS").addField()
                .at(9).simple(SQL_SHORT, 0, "SCALE", "FUNCTION_COLUMNS").addField()
                .at(10).simple(SQL_SHORT, 0, "RADIX", "FUNCTION_COLUMNS").addField()
                .at(11).simple(SQL_SHORT, 0, "NULLABLE", "FUNCTION_COLUMNS").addField()
                .at(12).simple(SQL_VARYING, 80, "REMARKS", "FUNCTION_COLUMNS").addField()
                .at(13).simple(SQL_LONG, 0, "CHAR_OCTET_LENGTH", "FUNCTION_COLUMNS").addField()
                .at(14).simple(SQL_LONG, 0, "ORDINAL_POSITION", "FUNCTION_COLUMNS").addField()
                .at(15).simple(SQL_VARYING, 31, "IS_NULLABLE", "FUNCTION_COLUMNS").addField()
                .at(16).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "SPECIFIC_NAME", "FUNCTION_COLUMNS").addField()
                .toRowDescriptor();

        return new FBResultSet(rowDescriptor, Collections.<RowValue>emptyList());
    }

    @Override
    public ResultSet getFunctions(String catalog, String schemaPattern, String functionNamePattern)
            throws SQLException {
        // FIXME implement this method to return actual result
        final RowDescriptor rowDescriptor = new RowDescriptorBuilder(6, datatypeCoder)
                .at(0).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "FUNCTION_CAT", "FUNCTIONS").addField()
                .at(1).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "FUNCTION_SCHEM", "FUNCTIONS").addField()
                .at(2).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "FUNCTION_NAME", "FUNCTIONS").addField()
                .at(3).simple(SQL_VARYING, 80, "REMARKS", "FUNCTIONS").addField()
                .at(4).simple(SQL_SHORT, 0, "FUNCTION_TYPE", "FUNCTIONS").addField()
                .at(5).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "SPECIFIC_NAME", "FUNCTIONS").addField()
                .toRowDescriptor();

        return new FBResultSet(rowDescriptor, Collections.<RowValue>emptyList());
    }

    @Override
    public ResultSet getSchemas(String catalog, String schemaPattern) throws SQLException {
        final RowDescriptor rowDescriptor = new RowDescriptorBuilder(2, datatypeCoder)
                .at(0).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_SCHEM", "TABLESCHEMAS").addField()
                .at(1).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_CATALOG", "TABLESCHEMAS").addField()
                .toRowDescriptor();

        return new FBResultSet(rowDescriptor, Collections.<RowValue>emptyList());
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface != null && iface.isAssignableFrom(FBDatabaseMetaData.class);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (!isWrapperFor(iface))
            throw new SQLException("Unable to unwrap to class " + iface.getName());

        return iface.cast(this);
    }

    /**
     * Determine if there are no SQL wildcard characters ('%' or '_') in the given pattern.
     *
     * @param pattern
     *         The pattern to be checked for wildcards
     * @return <code>true</code> if there are no wildcards in the pattern,
     * <code>false</code> otherwise
     * @deprecated Will be removed in Jaybird 5
     */
    @Deprecated
    public static boolean hasNoWildcards(String pattern) {
        if (pattern == null) return true;

        for (int pos = 0; pos < pattern.length(); pos++) {
            char ch = pattern.charAt(pos);
            if (ch == '_' || ch == '%') {
                return false;
            } else if (ch == '\\' && pos < pattern.length() - 1) {
                char nextCh = pattern.charAt(pos + 1);
                if (nextCh == '\\' || nextCh == '%' || nextCh == '_') {
                    // We were an escape, skip next character
                    pos += 1;
                }
            }
        }
        return true;
    }

    /**
     * Strips all backslash-escapes from a string.
     *
     * @param pattern
     *         The string to be stripped
     * @return pattern with all backslash-escapes removed
     * @deprecated Will be removed in Jaybird 5
     */
    @Deprecated
    public static String stripEscape(String pattern) {
        if (pattern == null) return null;
        StringBuilder stripped = new StringBuilder(pattern.length());
        for (int pos = 0; pos < pattern.length(); pos++) {
            char ch = pattern.charAt(pos);
            if (ch != '\\') {
                stripped.append(ch);
            } else if (pos < pattern.length() - 1 && pattern.charAt(pos + 1) == '\\') {
                // We are an escape for a backslash, append backslash and skip next position
                stripped.append('\\');
                pos += 1;
            }
        }
        return stripped.toString();
    }

    /**
     * Escapes the like wildcards and escape ({@code \_%} in the provided search string with a {@code \}.
     * <p>
     * Primary purpose is to escape object names with wildcards for use in metadata patterns for literal matches, but
     * it can also be used to escape for SQL {@code LIKE}.
     * </p>
     *
     * @param objectName
     *         Object name to escape.
     * @return Object name with wildcards escaped.
     */
    public static String escapeWildcards(String objectName) {
        return MetadataPattern.escapeWildcards(objectName);
    }

    protected String getWantsSystemTables(String[] types) {
        for (String type : types) {
            if (SYSTEM_TABLE.equals(type)) {
                return "T";
            }
        }
        return "F";
    }

    protected String getWantsTables(String[] types) {
        for (String type : types) {
            if (TABLE.equals(type)) {
                return "T";
            }
        }
        return "F";
    }

    protected String getWantsViews(String[] types) {
        for (String type : types) {
            if (VIEW.equals(type)) {
                return "T";
            }
        }
        return "F";
    }

    //@formatter:off

    // Suitable for Firebird 2.5 and earlier
    private static final String GET_PSEUDO_COLUMNS_FRAGMENT_FB_25 =
            "select "
            + " RDB$RELATION_NAME, "
            + " RDB$DBKEY_LENGTH, "
            + " 'F' AS HAS_RECORD_VERSION, "
            + " '' AS RECORD_VERSION_NULLABLE " // unknown nullability (and doesn't matter, no RDB$RECORD_VERSION)
            + "from rdb$relations ";

    // Suitable for Firebird 3 and higher
    private static final String GET_PSEUDO_COLUMNS_FRAGMENT_FB_30 =
            "select "
            + " RDB$RELATION_NAME, "
            + " RDB$DBKEY_LENGTH, "
            + " RDB$DBKEY_LENGTH = 8 as HAS_RECORD_VERSION, "
            + " case "
            + "   when RDB$RELATION_TYPE in (0, 1, 4, 5) then 'NO' " // table, view, GTT preserve + delete: never null
            + "   when RDB$RELATION_TYPE in (2, 3) then 'YES' " // external + virtual: always null
            + "   else ''" // unknown or unsupported (by Jaybird) type: unknown nullability
            + " end as RECORD_VERSION_NULLABLE "
            + "from rdb$relations ";

    private static final String GET_PSEUDO_COLUMNS_END = " order by RDB$RELATION_NAME";

    //@formatter:on

    @Override
    public ResultSet getPseudoColumns(String catalog, String schemaPattern, String tableNamePattern,
            String columnNamePattern) throws SQLException {

        final RowDescriptor rowDescriptor = new RowDescriptorBuilder(12, datatypeCoder)
                .at(0).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_CAT", "PSEUDOCOLUMNS").addField()
                .at(1).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_SCHEM", "PSEUDOCOLUMNS").addField()
                .at(2).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_NAME", "PSEUDOCOLUMNS").addField()
                .at(3).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "COLUMN_NAME", "PSEUDOCOLUMNS").addField()
                .at(4).simple(SQL_LONG, 0, "DATA_TYPE", "PSEUDOCOLUMNS").addField()
                .at(5).simple(SQL_LONG, 0, "COLUMN_SIZE", "PSEUDOCOLUMNS").addField()
                .at(6).simple(SQL_LONG, 0, "DECIMAL_DIGITS", "PSEUDOCOLUMNS").addField()
                .at(7).simple(SQL_LONG, 0, "NUM_PREC_RADIX", "PSEUDOCOLUMNS").addField()
                .at(8).simple(SQL_VARYING, 50, "COLUMN_USAGE", "PSEUDOCOLUMNS").addField()
                // Field in Firebird is actually a blob, using Integer.MAX_VALUE for length
                .at(9).simple(SQL_VARYING | 1, Integer.MAX_VALUE, "REMARKS", "PSEUDOCOLUMNS").addField()
                .at(10).simple(SQL_LONG, 0, "CHAR_OCTET_LENGTH", "PSEUDOCOLUMNS").addField()
                .at(11).simple(SQL_VARYING, 3, "IS_NULLABLE", "PSEUDOCOLUMNS").addField()
                .toRowDescriptor();

        if ("".equals(tableNamePattern) || "".equals(columnNamePattern)) {
            // Matching table and/or column not possible
            return new FBResultSet(rowDescriptor, Collections.<RowValue>emptyList());
        }

        final boolean supportsRecordVersion = firebirdSupportInfo.supportsRecordVersionPseudoColumn();
        final MetadataPatternMatcher matcher = MetadataPattern.compile(columnNamePattern).toMetadataPatternMatcher();
        final boolean retrieveDbKey = matcher.matches("RDB$DB_KEY");
        final boolean retrieveRecordVersion = supportsRecordVersion && matcher.matches("RDB$RECORD_VERSION");

        if (!(retrieveDbKey || retrieveRecordVersion)) {
            // No matching columns
            return new FBResultSet(rowDescriptor, Collections.<RowValue>emptyList());
        }

        Clause tableNameClause = new Clause("RDB$RELATION_NAME", tableNamePattern);
        String sql = (supportsRecordVersion ? GET_PSEUDO_COLUMNS_FRAGMENT_FB_30 : GET_PSEUDO_COLUMNS_FRAGMENT_FB_25);
        if (tableNameClause.hasCondition()) {
            sql += " where " + tableNameClause.getCondition(false);
        }
        sql += GET_PSEUDO_COLUMNS_END;
        List<String> params = tableNameClause.hasCondition()
                ? Collections.<String>singletonList(tableNameClause.getValue())
                : Collections.<String>emptyList();

        try (ResultSet rs = doQuery(sql, params)) {
            if (!rs.next()) {
                return new FBResultSet(rowDescriptor, Collections.<RowValue>emptyList());
            }

            byte[] dbKeyBytes = retrieveDbKey ? getBytes("RDB$DB_KEY") : null;
            byte[] dbKeyRemark = retrieveDbKey
                    ? getBytes("The RDB$DB_KEY column in a select list will be renamed by Firebird to DB_KEY in the "
                    + "result set (both as column name and label). Result set getters in Jaybird will map this, but "
                    + "in introspection of ResultSetMetaData, DB_KEY will be reported. Identification as a "
                    + "Types.ROWID will only work in a select list (ResultSetMetaData), not for parameters "
                    + "(ParameterMetaData), but Jaybird will allow setting a RowId value.")
                    : null;
            byte[] recordVersionBytes = retrieveRecordVersion ? getBytes("RDB$RECORD_VERSION") : null;
            byte[] noUsageRestrictions = getBytes(PseudoColumnUsage.NO_USAGE_RESTRICTIONS.name());

            List<RowValue> rows = new ArrayList<>();
            RowValueBuilder valueBuilder = new RowValueBuilder(rowDescriptor);
            do {
                byte[] tableNameBytes = getBytes(rs.getString("RDB$RELATION_NAME"));

                if (retrieveDbKey) {
                    int dbKeyLength = rs.getInt("RDB$DBKEY_LENGTH");
                    byte[] dbKeyLengthBytes = createInt(dbKeyLength);
                    valueBuilder
                            .at(2).set(tableNameBytes)
                            .at(3).set(dbKeyBytes)
                            .at(4).set(createInt(Types.ROWID))
                            .at(5).set(dbKeyLengthBytes)
                            .at(7).set(RADIX_TEN)
                            .at(8).set(noUsageRestrictions)
                            .at(9).set(dbKeyRemark)
                            .at(10).set(dbKeyLengthBytes)
                            .at(11).set(NO_BYTES);
                    rows.add(valueBuilder.toRowValue(true));
                }

                if (retrieveRecordVersion && rs.getBoolean("HAS_RECORD_VERSION")) {
                    valueBuilder
                            .at(2).set(tableNameBytes)
                            .at(3).set(recordVersionBytes)
                            .at(4).set(createInt(Types.BIGINT))
                            .at(5).set(BIGINT_PRECISION)
                            .at(6).set(INT_ZERO)
                            .at(7).set(RADIX_TEN)
                            .at(8).set(noUsageRestrictions)
                            .at(11).set(getBytes(rs.getString("RECORD_VERSION_NULLABLE")));
                    rows.add(valueBuilder.toRowValue(true));
                }
            } while (rs.next());

            return new FBResultSet(rowDescriptor, rows);
        }
    }

    @Override
    public boolean generatedKeyAlwaysReturned() throws SQLException {
        // TODO Double check if this is correct
        return false;
    }

    @Override
    public String getProcedureSourceCode(String procedureName) throws SQLException {
        String sResult = null;
        String sql = "Select RDB$PROCEDURE_SOURCE From RDB$PROCEDURES Where "
                + "RDB$PROCEDURE_NAME = ?";
        List<String> params = new ArrayList<>();
        params.add(procedureName);
        try (ResultSet rs = doQuery(sql, params)) {
            if (rs.next()) sResult = rs.getString(1);
        }

        return sResult;
    }

    @Override
    public String getTriggerSourceCode(String triggerName) throws SQLException {
        String sResult = null;
        String sql = "Select RDB$TRIGGER_SOURCE From RDB$TRIGGERS Where RDB$TRIGGER_NAME = ?";
        List<String> params = new ArrayList<>();
        params.add(triggerName);
        try (ResultSet rs = doQuery(sql, params)) {
            if (rs.next()) sResult = rs.getString(1);
        }

        return sResult;
    }

    @Override
    public String getViewSourceCode(String viewName) throws SQLException {
        String sResult = null;
        String sql = "Select RDB$VIEW_SOURCE From RDB$RELATIONS Where RDB$RELATION_NAME = ?";
        List<String> params = new ArrayList<>();
        params.add(viewName);
        try (ResultSet rs = doQuery(sql, params)) {
            if (rs.next()) sResult = rs.getString(1);
        }

        return sResult;
    }

    protected static final class Clause {
        private final String condition;
        private final String value;

        public Clause(String columnName, String pattern) {
            MetadataPattern metadataPattern = MetadataPattern.compile(pattern);
            condition = metadataPattern.renderCondition(columnName);
            value = metadataPattern.getConditionValue();
        }

        /**
         * @return Result of {@code getCondition(true)}
         */
        public String getCondition() {
            return getCondition(true);
        }

        public String getCondition(boolean includeAnd) {
            if (!hasCondition()) {
                return "";
            }
            if (includeAnd) {
                return condition + " and ";
            }
            return condition;
        }

        public String getValue() {
            return value;
        }

        public boolean hasCondition() {
            return !condition.isEmpty();
        }
    }

    protected static byte[] getBytes(String value) {
        return value != null ? value.getBytes(StandardCharsets.UTF_8) : null;
    }

    private FBPreparedStatement getStatement(String sql, boolean standalone) throws SQLException {
        synchronized (statements) {
            if (!standalone) {
                // Check cache
                FBPreparedStatement cachedStatement = statements.get(sql);

                if (cachedStatement != null) {
                    if (cachedStatement.isClosed()) {
                        statements.remove(sql);
                    } else {
                        return cachedStatement;
                    }
                }
            }

            InternalTransactionCoordinator.MetaDataTransactionCoordinator metaDataTransactionCoordinator =
                    new InternalTransactionCoordinator.MetaDataTransactionCoordinator(connection.txCoordinator);

            FBPreparedStatement newStatement = new FBPreparedStatement(gdsHelper, sql,
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT,
                    metaDataTransactionCoordinator, metaDataTransactionCoordinator, true, standalone, false);

            if (!standalone) {
                statements.put(sql, newStatement);
            }

            return newStatement;
        }
    }

    /**
     * Execute an sql query with a given set of parameters.
     *
     * @param sql
     *         The sql statement to be used for the query
     * @param params
     *         The parameters to be used in the query
     * @throws SQLException
     *         if a database access error occurs
     */
    protected ResultSet doQuery(String sql, List<String> params) throws SQLException {
        return doQuery(sql, params, false);
    }

    /**
     * Execute an sql query with a given set of parameters.
     *
     * @param sql
     *         The sql statement to be used for the query
     * @param params
     *         The parameters to be used in the query
     * @param standalone
     *         The query to be executed is a standalone query (should not be cached and be closed asap)
     * @throws SQLException
     *         if a database access error occurs
     */
    protected ResultSet doQuery(String sql, List<String> params, boolean standalone) throws SQLException {
        FBPreparedStatement s = getStatement(sql, standalone);

        for (int i = 0; i < params.size(); i++) {
            s.setStringForced(i + 1, params.get(i));
        }

        return s.executeMetaDataQuery();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Minimum lifetime supported by Firebird is transaction-scope, and this can be changed to session-scope with
     * {@code isc_dpb_dbkey_scope} set to {@code 1} (eg connection property {@code dbkey_scope=1}). This implementation,
     * however, will always report {@link RowIdLifetime#ROWID_VALID_TRANSACTION}.
     * </p>
     */
    @Override
    public RowIdLifetime getRowIdLifetime() throws SQLException {
        return RowIdLifetime.ROWID_VALID_TRANSACTION;
    }

    static final int JDBC_MAJOR_VERSION = 4;
    static final int JDBC_MINOR_VERSION;
    static {
        int tempVersion;
        try {
            String javaImplementation = getSystemPropertyPrivileged("java.specification.version");
            if (javaImplementation == null) {
                // Assume minimum: JDBC 4.1
                tempVersion = 1;
            } else {
                int javaVersionMajor;
                try {
                    javaVersionMajor = (int) Double.parseDouble(javaImplementation);
                } catch (NumberFormatException e) {
                    javaVersionMajor = 1;
                }
                if (javaVersionMajor >= 9) {
                    // JDK 9 or higher: JDBC 4.3
                    tempVersion = 3;
                } else if ("1.8".compareTo(javaImplementation) <= 0) {
                    // JDK 1.8 or higher: JDBC 4.2
                    tempVersion = 2;
                } else {
                    // JDK 1.7 (or lower): JDBC 4.1
                    tempVersion = 1;
                }
            }
        } catch (RuntimeException ex) {
            // default to 1 (JDBC 4.1) when privileged call fails
            tempVersion = 1;
        }
        JDBC_MINOR_VERSION = tempVersion;
    }

    @Override
    public int getJDBCMajorVersion() {
        return JDBC_MAJOR_VERSION;
    }

    @Override
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

    private static class LruPreparedStatementCache extends LinkedHashMap<String, FBPreparedStatement> {
        private final int maxCapacity;

        private LruPreparedStatementCache(int maxCapacity) {
            super(16, 0.75f, true);
            this.maxCapacity = maxCapacity;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, FBPreparedStatement> eldest) {
            if (size() <= maxCapacity) {
                return false;
            }
            try {
                FBPreparedStatement statement = eldest.getValue();
                statement.close();
            } catch (Exception e) {
                log.debug("Closing eldest cached metadata statement yielded an exception; ignored", e);
            }
            return true;
        }
    }
}
