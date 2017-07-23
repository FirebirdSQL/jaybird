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

import org.firebirdsql.gds.ng.fields.FieldDescriptor;

/**
 * Class implementing workaround for "operation was cancelled" bug in server.
 * When we send some string data exceeding maximum length of the corresponding
 * field causes "operation was cancelled" in remote module of the server instead
 * of "arithmetic exception..." error. This makes code debugging harder, since
 * error message is not very informative.
 * <p>
 * However we cannot simply check length locally. Maximum allowed length in bytes 
 * is connected with the character set of the field as defined lengh * maximum
 * number of bytes per character in that encoding. However this does not work
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

    private boolean trimString;
    
    /**
     * Create instance of this class for the specified field and result set.
     * 
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
    
    public void setTrimString(boolean trimString) {
        this.trimString = trimString;
    }

    public void setString(String value) throws SQLException {
        byte[] data = setStringForced(value);

        if (value == null) return;

        assert data != null : "Expected non-null data here";
        if (data.length > fieldDescriptor.getLength() && !isSystemTable(fieldDescriptor.getOriginalTableName())) {
            // special handling for the LIKE ? queries with CHAR(1) fields
            if (!(value.length() <= fieldDescriptor.getLength() + 2
                    && (value.charAt(0) == '%' || value.charAt(value.length() - 1) == '%')))
                throw new DataTruncation(fieldDescriptor.getPosition() + 1, true, false, data.length,
                        fieldDescriptor.getLength());
        }
    }    
    
    /**
     * Set string value without any check of its length. This is a workaround 
     * for the problem described above.
     * 
     * @param value value to set.
     * 
     * @throws SQLException if something went wrong.
     */
    public byte[] setStringForced(String value) throws SQLException {
        if (value == null) {
            setNull();
            return null;
        }
        byte[] data = getDatatypeCoder().encodeString(value, encodingDefinition.getEncoding());
        setFieldData(data);
        return data;
    }   
    
    /**
     * Get string value of this field.
     * 
     * @return string value of this filed or <code>null</code> if the value is 
     * NULL.
     */
    public String getString() throws SQLException {
        String result = super.getString();
        
        if (result == null)
            return null;
        
        if (isType(fieldDescriptor, Types.VARCHAR))
            return result;
        
        // fix incorrect padding done by the database for multibyte charsets
        if ((fieldDescriptor.getLength() % encodingDefinition.getMaxBytesPerChar()) == 0
                && result.length() > possibleCharLength)
            result = result.substring(0, possibleCharLength);
        
        if (trimString)
            result = result.trim();
        
        return result;
    }

    /**
     * List of system tables from Firebird 1.5
     */
    private static final String[] SYSTEM_TABLES = new String[] {
        "RDB$CHARACTER_SETS", 
        "RDB$CHECK_CONSTRAINTS", 
        "RDB$COLLATIONS", 
        "RDB$DATABASE", 
        "RDB$DEPENDENCIES", 
        "RDB$EXCEPTIONS", 
        "RDB$FIELDS", 
        "RDB$FIELD_DIMENSIONS", 
        "RDB$FILES", 
        "RDB$FILTERS", 
        "RDB$FORMATS", 
        "RDB$FUNCTIONS", 
        "RDB$FUNCTION_ARGUMENTS", 
        "RDB$GENERATORS", 
        "RDB$INDEX_SEGMENTS", 
        "RDB$INDICES", 
        "RDB$LOG_FILES", 
        "RDB$PAGES", 
        "RDB$PROCEDURES", 
        "RDB$PROCEDURE_PARAMETERS", 
        "RDB$REF_CONSTRAINTS", 
        "RDB$RELATIONS", 
        "RDB$RELATION_CONSTRAINTS", 
        "RDB$RELATION_FIELDS", 
        "RDB$ROLES", 
        "RDB$SECURITY_CLASSES", 
        "RDB$TRANSACTIONS", 
        "RDB$TRIGGERS", 
        "RDB$TRIGGER_MESSAGES", 
        "RDB$TYPES", 
        "RDB$USER_PRIVILEGES", 
        "RDB$VIEW_RELATIONS"
    };
    
    /**
     * Check if specified table is system table. This method simply traverses
     * hardcoded list of system tables and compares table names.
     * 
     * @param tableName name of the table to check.
     * 
     * @return <code>true</code> if specified table is system, otherwise
     * <code>false</code>
     */
    private boolean isSystemTable(String tableName) {
        boolean result = false;

        for (String systemTable : SYSTEM_TABLES) {
            if (systemTable.equals(tableName)) {
                result = true;
                break;
            }
        }
        
        return result;
    }
}
