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
package org.firebirdsql.jdbc.field;

import java.sql.DataTruncation;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.firebirdsql.gds.ng.fields.FieldDescriptor;

import static java.util.Collections.unmodifiableSet;

/**
 * Class implementing workaround for "operation was cancelled" bug in server.
 * When we send some string data exceeding maximum length of the corresponding
 * field causes "operation was cancelled" in remote module of the server instead
 * of "arithmetic exception..." error. This makes code debugging harder, since
 * error message is not very informative.
 * <p>
 * However, we cannot simply check length locally. Maximum allowed length in bytes
 * is connected with the character set of the field as defined lengh * maximum
 * number of bytes per character in that encoding. However, this does not work
 * for system tables which have defined length 31, character set UNICODE_FSS and
 * maximum allowed length of 31 (instead of 31 * 3 = 63).
 * <p>
 * Until this bug is fixed in the engine we will simply check if field belongs 
 * to the system table and do not throw data truncation error locally. 
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public final class FBWorkaroundStringField extends FBStringField {

    /**
     * Create instance of this class for the specified field and result set.
     *
     * @param fieldDescriptor Field descriptor
     * @param dataProvider data provider for this field
     * @param requiredType required type.
     *
     * @throws SQLException if something went wrong.
     */
    FBWorkaroundStringField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider, int requiredType)
            throws SQLException {
        super(fieldDescriptor, dataProvider, requiredType);
    }

    public void setString(String value) throws SQLException {
        if (setWhenNull(value)) return;
        byte[] data = getDatatypeCoder().encodeString(value);

        if (data.length > fieldDescriptor.getLength() && !isSystemTable(fieldDescriptor.getOriginalTableName())) {
            // special handling for the LIKE ? queries with CHAR(1) fields
            if (!(value.length() <= fieldDescriptor.getLength() + 2
                    && (value.charAt(0) == '%' || value.charAt(value.length() - 1) == '%'))) {
                throw new DataTruncation(fieldDescriptor.getPosition() + 1, true, false, data.length,
                        fieldDescriptor.getLength());
            }
        }
        setFieldData(data);
    }    
    
    /**
     * Set string value without any check of its length. This is a workaround 
     * for the problem described above.
     * 
     * @param value value to set.
     * 
     * @throws SQLException if something went wrong.
     */
    public void setStringForced(String value) throws SQLException {
        if (setWhenNull(value)) return;
        byte[] data = getDatatypeCoder().encodeString(value);
        setFieldData(data);
    }   
    
    /**
     * Get string value of this field.
     * 
     * @return string value of this filed or <code>null</code> if the value is 
     * NULL.
     */
    public String getString() throws SQLException {
        String result = super.getString();
        
        if (result == null || JdbcTypeConverter.isJdbcType(fieldDescriptor, Types.VARCHAR) || isTrimTrailing()) {
            return result;
        }

        // fix incorrect padding done by the database for multibyte charsets
        final int maxBytesPerChar = getDatatypeCoder().getEncodingDefinition().getMaxBytesPerChar();
        if ((fieldDescriptor.getLength() % maxBytesPerChar) == 0 && result.length() > possibleCharLength) {
            result = result.substring(0, possibleCharLength);
        }

        return result;
    }

    /**
     * List of system tables (source from Firebird 5.0.0.762).
     */
    private static final Set<String> SYSTEM_TABLES = unmodifiableSet(new HashSet<>(Arrays.asList("MON$ATTACHMENTS",
            "MON$CALL_STACK", "MON$COMPILED_STATEMENTS", "MON$CONTEXT_VARIABLES", "MON$DATABASE", "MON$IO_STATS",
            "MON$MEMORY_USAGE", "MON$RECORD_STATS", "MON$STATEMENTS", "MON$TABLE_STATS", "MON$TRANSACTIONS",
            "RDB$AUTH_MAPPING", "RDB$BACKUP_HISTORY", "RDB$CHARACTER_SETS", "RDB$CHECK_CONSTRAINTS", "RDB$COLLATIONS",
            "RDB$CONFIG", "RDB$DATABASE", "RDB$DB_CREATORS", "RDB$DEPENDENCIES", "RDB$EXCEPTIONS", "RDB$FIELDS",
            "RDB$FIELD_DIMENSIONS", "RDB$FILES", "RDB$FILTERS", "RDB$FORMATS", "RDB$FUNCTIONS",
            "RDB$FUNCTION_ARGUMENTS", "RDB$GENERATORS", "RDB$INDEX_SEGMENTS", "RDB$INDICES", "RDB$KEYWORDS",
            "RDB$LOG_FILES", "RDB$PACKAGES", "RDB$PAGES", "RDB$PROCEDURES", "RDB$PROCEDURE_PARAMETERS",
            "RDB$PUBLICATIONS", "RDB$PUBLICATION_TABLES", "RDB$REF_CONSTRAINTS", "RDB$RELATIONS",
            "RDB$RELATION_CONSTRAINTS", "RDB$RELATION_FIELDS", "RDB$ROLES", "RDB$SECURITY_CLASSES", "RDB$TIME_ZONES",
            "RDB$TRANSACTIONS", "RDB$TRIGGERS", "RDB$TRIGGER_MESSAGES", "RDB$TYPES", "RDB$USER_PRIVILEGES",
            "RDB$VIEW_RELATIONS", "SEC$DB_CREATORS", "SEC$GLOBAL_AUTH_MAPPING", "SEC$USERS", "SEC$USER_ATTRIBUTES")));

    /**
     * Check if specified table is a system table.
     *
     * @param tableName
     *         name of the table to check.
     * @return {@code true} if specified table is a system table, otherwise {@code false}
     */
    private boolean isSystemTable(String tableName) {
        return SYSTEM_TABLES.contains(tableName);
    }
}
