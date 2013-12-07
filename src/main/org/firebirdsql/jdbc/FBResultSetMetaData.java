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

import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.util.*;

import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.XSQLVAR;
import org.firebirdsql.gds.impl.GDSHelper;

/**
 * Implementation of {@link java.sql.ResultSetMetaData} interface.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class FBResultSetMetaData implements FirebirdResultSetMetaData {

    private final XSQLVAR[] xsqlvars;
    private Map extendedInfo;
    private final GDSHelper connection;
    private final ColumnStrategy columnStrategy;

    /**
     * Creates a new <code>FBResultSetMetaData</code> instance.
     *
     * @param xsqlvars a <code>XSQLVAR[]</code> value
     * @param connection a <code>AbstractConnection</code> value
     * @exception SQLException if an error occurs
     *
     * TODO Need another constructor for metadata from constructed
     * result set, where we supply the ext field info.
     */
    protected FBResultSetMetaData(XSQLVAR[] xsqlvars, GDSHelper connection) throws SQLException {
        this.xsqlvars = xsqlvars;
        this.connection = connection;
        
        // Decide how to handle column names and column labels
        if (connection != null && connection.getDatabaseParameterBuffer().hasArgument(ISCConstants.isc_dpb_column_label_for_name)) {
            columnStrategy = ColumnStrategy.COLUMN_LABEL_FOR_NAME;
        } else {
            columnStrategy = ColumnStrategy.DEFAULT;
        }
    }

    private String getIscEncoding() {
        if (connection != null)
            return connection.getIscEncoding();
        else
            return "NONE";
    }

    /**
     * Returns the number of columns in this <code>ResultSet</code> object.
     *
     * @return the number of columns
     */
    public  int getColumnCount() {
        return xsqlvars.length;
    }


    /**
     * Indicates whether the designated column is automatically numbered, thus read-only.
     *
     * @param column the first column is 1, the second is 2, ...
     * @return <code>true</code> if so; <code>false</code> otherwise
     */
    public  boolean isAutoIncrement(int column) {
        return false;
    }


    /**
     * Indicates whether a column's case matters.
     *
     * @param column the first column is 1, the second is 2, ...
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean isCaseSensitive(int column) throws  SQLException {
        return true;
    }


    /**
     * Indicates whether the designated column can be used in a where clause.
     *
     * @param column the first column is 1, the second is 2, ...
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean isSearchable(int column) throws  SQLException {
        if (((getXsqlvar(column).sqltype & ~1) == ISCConstants.SQL_ARRAY)
            || ((getXsqlvar(column).sqltype & ~1) == ISCConstants.SQL_BLOB)) {
            return false;
        }
        else {
            return true;
        }
    }


    /**
     * Indicates whether the designated column is a cash value.
     *
     * @param column the first column is 1, the second is 2, ...
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean isCurrency(int column) throws  SQLException {
        return false;
    }


    /**
     * Indicates the nullability of values in the designated column.
     *
     * @param column the first column is 1, the second is 2, ...
     * @return the nullability status of the given column; one of <code>columnNoNulls</code>,
     *          <code>columnNullable</code> or <code>columnNullableUnknown</code>
     * @exception SQLException if a database access error occurs
     */
    public  int isNullable(int column) throws  SQLException {
        if ((getXsqlvar(column).sqltype & 1) == 1) {
            return columnNullable;
        }
        else {
            return columnNoNulls;
        }
    }


    /**
     * The constant indicating that a
     * column does not allow <code>NULL</code> values.
     */
    int columnNoNulls = 0;

    /**
     * The constant indicating that a
     * column allows <code>NULL</code> values.
     */
    int columnNullable = 1;

    /**
     * The constant indicating that the
     * nullability of a column's values is unknown.
     */
    int columnNullableUnknown = 2;

    /**
     * Indicates whether values in the designated column are signed numbers.
     *
     * @param column the first column is 1, the second is 2, ...
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean isSigned(int column) throws  SQLException {
        switch (getXsqlvar(column).sqltype & ~1) {
            case ISCConstants.SQL_SHORT:
            case ISCConstants.SQL_LONG:
            case ISCConstants.SQL_FLOAT:
            case ISCConstants.SQL_DOUBLE:
            case ISCConstants.SQL_D_FLOAT:
            case ISCConstants.SQL_INT64:
                return true;
            default:
                return false;
        }
    }


    /**
     * Indicates the designated column's normal maximum width in characters.
     *
     * @param column the first column is 1, the second is 2, ...
     * @return the normal maximum number of characters allowed as the width
     *          of the designated column
     * @exception SQLException if a database access error occurs
     */
    public  int getColumnDisplaySize(int column) throws  SQLException {
        int colType = getColumnType(column);
        switch (colType){
            case Types.DECIMAL:
            case Types.NUMERIC: {
                ExtendedFieldInfo fieldInfo = getExtFieldInfo(column);
                if (fieldInfo == null)
                    return estimatePrecision(column);
                else
                    return fieldInfo.fieldPrecision;
            }

            case Types.CHAR:
            case Types.VARCHAR: {
                XSQLVAR var = getXsqlvar(column);
                int charset = var.sqlsubtype & 0xFF;
                int charSetSize = charset == 127 /* CS_dynamic */ ?
                    EncodingFactory.getIscEncodingSize(getIscEncoding()) :
                    EncodingFactory.getCharacterSetSize(charset);
                return var.sqllen / charSetSize;
            }

            case Types.FLOAT:
                return 9;
            case Types.DOUBLE:
                return 17;
            case Types.INTEGER:
                return 11;
            case Types.BIGINT:
                return 21;
            case Types.SMALLINT:
                return 6;
            case Types.DATE:
                return 10;
            case Types.TIME:
                return 8;
            case Types.TIMESTAMP:
                return 19;
            case Types.BOOLEAN:
                return 1;
            default:

               return 0;
            }
        }


    /**
     * Gets the designated column's suggested title for use in printouts and
     * displays.
     *
     * @param column the first column is 1, the second is 2, ...
     * @return the suggested column title
     * @exception SQLException if a database access error occurs
     */
    public  String getColumnLabel(int column) throws  SQLException {
        return columnStrategy.getColumnLabel(getXsqlvar(column));
    }


    /**
     * Get the designated column's name.
     *
     * @param column the first column is 1, the second is 2, ...
     * @return column name
     * @exception SQLException if a database access error occurs
     */
    public  String getColumnName(int column) throws  SQLException {
        return columnStrategy.getColumnName(getXsqlvar(column));
    }

    public String getSourceColumnName(int column) throws SQLException {
        String result = getXsqlvar(column).sqlname;
        
        if (result == null)
            result = "";
        
        return result;
    }

    /**
     * Get the designated column's table's schema.
     *
     * @param column the first column is 1, the second is 2, ...
     * @return schema name or "" if not applicable
     * @exception SQLException if a database access error occurs
     */
    public  String getSchemaName(int column) throws  SQLException {
        //not really implemented
        return "";
    }


    /**
     * Get the designated column's number of decimal digits.
     *
     * @param column the first column is 1, the second is 2, ...
     * @return precision
     * @exception SQLException if a database access error occurs
     */
    public  int getPrecision(int column) throws  SQLException {
        int colType = getColumnType(column);

        switch (colType){

            case Types.DECIMAL:
            case Types.NUMERIC: {
                ExtendedFieldInfo fieldInfo = getExtFieldInfo(column);
                if (fieldInfo == null)
                    return estimatePrecision(column);
                else
                    return fieldInfo.fieldPrecision;
            }

            case Types.CHAR:
            case Types.VARCHAR: {
                XSQLVAR var = getXsqlvar(column);
                int charset = var.sqlsubtype & 0xFF;
                int charSetSize = charset == 127 /* CS_dynamic */ ?
                    EncodingFactory.getIscEncodingSize(getIscEncoding()) :
                    EncodingFactory.getCharacterSetSize(charset);
                return var.sqllen / charSetSize;
            }

            case Types.FLOAT:
                return 7;
            case Types.DOUBLE:
                return 15;
            case Types.INTEGER:
                return 10;
            case Types.BIGINT:
                return 19;
            case Types.SMALLINT:
                return 5;
            case Types.DATE:
                return 10;
            case Types.TIME:
                return 8;
            case Types.TIMESTAMP:
                return 19;
            case Types.BOOLEAN:
                return 1;
            default:
                return 0;
            }
        }


    /**
     * Gets the designated column's number of digits to right of the decimal point.
     *
     * @param column the first column is 1, the second is 2, ...
     * @return scale
     * @exception SQLException if a database access error occurs
     */
    public  int getScale(int column) throws  SQLException {
        return getXsqlvar(column).sqlscale * (-1);
    }


    /**
     * Gets the designated column's table name.
     *
     * @param column the first column is 1, the second is 2, ...
     * @return table name or "" if not applicable
     * @exception SQLException if a database access error occurs
     */
    public  String getTableName(int column) throws  SQLException {
        String result = getXsqlvar(column).relname;
        if (result == null) result = "";
        return result;
    }

    /**
     * Gets the designated column's table alias.
     *
     * @param column the first column is 1, the second is 2, ...
     * @return table alias or "" if not applicable
     * @exception SQLException if a database access error occurs
     */
    public String getTableAlias(int column) throws SQLException {
        String result = getXsqlvar(column).relaliasname;
        if (result == null) result = getTableName(column);
        return result;
    }

    
    /**
     * Gets the designated column's table's catalog name.
     *
     * @param column the first column is 1, the second is 2, ...
     * @return column name or "" if not applicable
     * @exception SQLException if a database access error occurs
     */
    public String getCatalogName(int column) throws  SQLException {
        return "";
    }


    /**
     * Retrieves the designated column's SQL type.
     *
     * @param column the first column is 1, the second is 2, ...
     * @return SQL type from java.sql.Types
     * @exception SQLException if a database access error occurs
     * @see Types
     */
    public  int getColumnType(int column) throws  SQLException {
        int sqltype = getXsqlvar(column).sqltype & ~1;
        int sqlscale = getXsqlvar(column).sqlscale;
        int sqlsubtype = getXsqlvar(column).sqlsubtype;

        if (sqlscale < 0) {
            switch (sqltype) {
                case ISCConstants.SQL_SHORT:
                case ISCConstants.SQL_LONG:
                case ISCConstants.SQL_INT64:
                case ISCConstants.SQL_DOUBLE:
                    // NOTE: can't be BIGINT because of scale
                    if (sqlsubtype == 2)
                        return Types.DECIMAL;
                    else
                        return Types.NUMERIC;
                default:
                    break;
            }
        }

        switch (sqltype) {
            case ISCConstants.SQL_SHORT:
                return Types.SMALLINT;
            case ISCConstants.SQL_LONG:
                return Types.INTEGER;
            case ISCConstants.SQL_DOUBLE:
            case ISCConstants.SQL_D_FLOAT:
                return Types.DOUBLE;
            case ISCConstants.SQL_FLOAT:
                return Types.FLOAT;
            case ISCConstants.SQL_TEXT:
                return Types.CHAR;
            case ISCConstants.SQL_VARYING:
                return Types.VARCHAR;
            case ISCConstants.SQL_TIMESTAMP:
                return Types.TIMESTAMP;
            case ISCConstants.SQL_TYPE_TIME:
                return Types.TIME;
            case ISCConstants.SQL_TYPE_DATE:
                return Types.DATE;
            case ISCConstants.SQL_INT64:
                if (sqlsubtype == 1)
                    return Types.NUMERIC;
                else if (sqlsubtype == 2)
                    return Types.DECIMAL;
                else
                    return Types.BIGINT;
            case ISCConstants.SQL_BLOB:
                if (sqlsubtype < 0)
                    return Types.BLOB;
                else if (sqlsubtype == 0 || sqlsubtype > 1)
                    return Types.LONGVARBINARY;
                else if (sqlsubtype == 1)
                    return Types.LONGVARCHAR;
                else
                    return Types.OTHER;
            case ISCConstants.SQL_QUAD:
                return Types.OTHER;
            case ISCConstants.SQL_BOOLEAN:
                return Types.BOOLEAN;
            default:
                return Types.NULL;
        }
    }


    /**
     * Retrieves the designated column's database-specific type name.
     *
     * @param column the first column is 1, the second is 2, ...
     * @return type name used by the database. If the column type is
     * a user-defined type, then a fully-qualified type name is returned.
     * @exception SQLException if a database access error occurs
     */
    public  String getColumnTypeName(int column) throws  SQLException {    	
        // Must return the same value as DatabaseMetaData getColumns Type_Name
        int sqltype = getXsqlvar(column).sqltype & ~1;
        int sqlscale = getXsqlvar(column).sqlscale;
        int sqlsubtype = getXsqlvar(column).sqlsubtype;

        if (sqlscale < 0) {
            switch (sqltype) {
                case ISCConstants.SQL_SHORT:
                case ISCConstants.SQL_LONG:
                case ISCConstants.SQL_INT64:
                case ISCConstants.SQL_DOUBLE:
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
            case ISCConstants.SQL_SHORT:
                return "SMALLINT";
            case ISCConstants.SQL_LONG:
                return "INTEGER";
            case ISCConstants.SQL_DOUBLE:
            case ISCConstants.SQL_D_FLOAT:
                return "DOUBLE PRECISION";
            case ISCConstants.SQL_FLOAT:
                return "FLOAT";
            case ISCConstants.SQL_TEXT:
                return "CHAR";
            case ISCConstants.SQL_VARYING:
                return "VARCHAR";
            case ISCConstants.SQL_TIMESTAMP:
                return "TIMESTAMP";
            case ISCConstants.SQL_TYPE_TIME:
                return "TIME";
            case ISCConstants.SQL_TYPE_DATE:
                return "DATE";
            case ISCConstants.SQL_INT64:
                //this might need some help for long mapping
                if (sqlsubtype == 1)
                    return "NUMERIC";
                else if (sqlsubtype == 2)
                    return "DECIMAL";
                else
                    return "BIGINT";
            case ISCConstants.SQL_BLOB:
                if (sqlsubtype < 0)
                    return "BLOB SUB_TYPE <0";
                else if (sqlsubtype == 0)
                    return "BLOB SUB_TYPE 0";
                else if (sqlsubtype == 1)
                    return "BLOB SUB_TYPE 1";
                else
                    return "BLOB SUB_TYPE " + sqlsubtype;
            case ISCConstants.SQL_QUAD:
                return "ARRAY";
            case ISCConstants.SQL_BOOLEAN:
                return "BOOLEAN";
            default:
                return "NULL";
        }
        
    }



    /**
     * Indicates whether the designated column is definitely not writable.
     *
     * @param column the first column is 1, the second is 2, ...
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean isReadOnly(int column) throws  SQLException {
        //Need to consider priveleges!!
        return false;
    }


    /**
     * Indicates whether it is possible for a write on the designated column to succeed.
     *
     * @param column the first column is 1, the second is 2, ...
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean isWritable(int column) throws  SQLException {
        //Needs priveleges???
        return true;
    }

    /**
     * Indicates whether a write on the designated column will definitely succeed.
     *
     * @param column the first column is 1, the second is 2, ...
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public  boolean isDefinitelyWritable(int column) throws  SQLException {
        //Need to consider privileges!!!
        return true;
    }


    //--------------------------JDBC 2.0-----------------------------------

    /**
     * <p>Returns the fully-qualified name of the Java class whose instances
     * are manufactured if the method <code>ResultSet.getObject</code>
     * is called to retrieve a value
     * from the column.  <code>ResultSet.getObject</code> may return a subclass of the
     * class returned by this method.
     *
     * @return the fully-qualified name of the class in the Java programming
     *         language that would be used by the method
     * <code>ResultSet.getObject</code> to retrieve the value in the specified
     * column. This is the class name used for custom mapping.
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public String getColumnClassName(int column) throws  SQLException {
        switch (getXsqlvar(column).sqltype & ~1) {
            
            case ISCConstants.SQL_TEXT:
            case ISCConstants.SQL_VARYING:
                return String.class.getName();
            
            case ISCConstants.SQL_SHORT:
            case ISCConstants.SQL_LONG:
                return Integer.class.getName();
            
            case ISCConstants.SQL_FLOAT:
            case ISCConstants.SQL_DOUBLE:
            case ISCConstants.SQL_D_FLOAT:
                return Double.class.getName();
            
            case ISCConstants.SQL_TIMESTAMP:
                return Timestamp.class.getName();
            
            case ISCConstants.SQL_BLOB:
                
                XSQLVAR field = getXsqlvar(column);
                
                if (field.sqlsubtype < 0)
                    return Blob.class.getName();
                
                if (field.sqlsubtype == 1)
                    return String.class.getName();
                
                else
                    return byte[].class.getName();

            case ISCConstants.SQL_ARRAY:
                return Array.class.getName();
            
            case ISCConstants.SQL_QUAD:
                return Long.class.getName();
            
            case ISCConstants.SQL_TYPE_TIME:
                return Time.class.getName();
            
            case ISCConstants.SQL_TYPE_DATE:
                return Date.class.getName();
            
            case ISCConstants.SQL_INT64:
                if (getXsqlvar(column).sqlscale == 0) {
                    return Long.class.getName();
                }
                else {
                    return BigDecimal.class.getName();
                }

            case ISCConstants.SQL_BOOLEAN:
                return Boolean.class.getName();

            default:
                throw new FBSQLException("Unknown SQL type.",
                        FBSQLException.SQL_STATE_INVALID_PARAM_TYPE);
        }
    }


    //private methods

    private XSQLVAR getXsqlvar(int columnIndex) {
        //return stmt.getOutSqlda().sqlvar[columnIndex - 1];
        return xsqlvars[columnIndex - 1];
    }

    private ExtendedFieldInfo getExtFieldInfo(int columnIndex)
        throws SQLException
    {
        if (extendedInfo == null)
        {
            this.extendedInfo = getExtendedFieldInfo(connection);
        } // end of if ()

        FieldKey key = new FieldKey(
            getXsqlvar(columnIndex).relname,
            getXsqlvar(columnIndex).sqlname);

        return (ExtendedFieldInfo)extendedInfo.get(key);
    }

    private int estimatePrecision(int columnIndex) {
        int sqltype = getXsqlvar(columnIndex).sqltype & ~1;
        // TODO: Why unused?
        int sqlscale = getXsqlvar(columnIndex).sqlscale;

        switch(sqltype) {
            case ISCConstants.SQL_SHORT : return 5;
            case ISCConstants.SQL_LONG : return 10;
            case ISCConstants.SQL_INT64 : return 19;
            case ISCConstants.SQL_DOUBLE : return 19;
            default : return 0;
        }
    }


    private static final String GET_FIELD_INFO = "SELECT "
        + "  RF.RDB$RELATION_NAME as RELATION_NAME"
        + ", RF.RDB$FIELD_NAME as FIELD_NAME"
        + ", F.RDB$FIELD_LENGTH as FIELD_LENGTH"
        + ", F.RDB$FIELD_PRECISION as FIELD_PRECISION"
        + ", F.RDB$FIELD_SCALE as FIELD_SCALE"
        + ", F.RDB$FIELD_SUB_TYPE as FIELD_SUB_TYPE"
        + ", F.RDB$CHARACTER_LENGTH as CHAR_LEN"
        + ", F.RDB$CHARACTER_SET_ID as CHARACTER_SET_ID"
        + " FROM"
        + "  RDB$RELATION_FIELDS RF "
        + ", RDB$FIELDS F "
        + " WHERE "
        + "  RF.RDB$FIELD_SOURCE = F.RDB$FIELD_NAME"
        + " AND"
        + "  RF.RDB$FIELD_NAME = ?"
        + " AND"
        + "  RF.RDB$RELATION_NAME = ?"
        ;

    /**
     * This class is an old-fashion data structure that stores additional
     * information about fields in a database.
     */
    private static class ExtendedFieldInfo {
    	// TODO Find out why there are unused fields here
        String relationName;
        String fieldName;
        int fieldLength;
        int fieldPrecision;
        int fieldScale;
        int fieldSubtype;
        int characterLength;
        int characterSetId;
    }

    /**
     * This class should be used as a composite key in an internal field
     * mapping structures.
     */
    private static final class FieldKey {
        private String relationName;
        private String fieldName;

        /**
         * Create instance of this class for the specified relation and field
         * names.
         *
         * @param relationName relation name.
         * @param fieldName field name.
         */
        FieldKey(String relationName, String fieldName) {
            this.relationName = relationName;
            this.fieldName = fieldName;
        }

        /**
         * Check if <code>obj</code> is equal to this object.
         *
         * @param obj object to check.
         *
         * @return <code>true</code> if <code>obj</code> is instance of this
         * class and has equal relation and field names.
         */
        public boolean equals(Object obj) {

            if (obj == this) return true;

            if (obj == null || !(obj instanceof FieldKey)) return false;

            FieldKey that = (FieldKey)obj;
            
            return (relationName != null ? relationName.equals(that.relationName) : that.relationName == null) 
            		&&
            		(fieldName != null ? fieldName.equals(that.fieldName) : that.fieldName == null);
        }

        /**
         * Get hash code of this instance.
         *
         * @return combination of hash codes of <code>relationName</code> field
         * and <code>fieldName</code> field.
         */
        public int hashCode() {
            int result = 971;
            result = 23 * result + (relationName != null ? relationName.hashCode() : 0);
            result = 23 * result + (fieldName != null ? fieldName.hashCode() : 0);
            return result;
        }


    }

    /**
     * This method retrieves extended information from the system tables in
     * a database. Since this method is expensinve, use it with care.
     *
     * @return mapping between {@link FieldKey} instances and
     * {@link ExtendedFieldInfo} instances.
     *
     * @throws SQLException if extended field information cannot be obtained.
     */
    private Map getExtendedFieldInfo(GDSHelper gdsHelper) throws SQLException {

        if (gdsHelper == null) return Collections.EMPTY_MAP;

        //
        // Apparently there is a limit in the UNION
        // It is necesary to split in several querys
        // Although the problem reported with 93 UNION use only 70
        //
        int pending = xsqlvars.length;
        HashMap result = new HashMap();
        while (pending > 0){
            StringBuffer sb = new StringBuffer();
            ArrayList params = new ArrayList();

            int maxLength = (pending>70) ? 70 : pending;
            for (int i = 0; i < maxLength; i++) {

                String relationName = xsqlvars[i].relname;
                String fieldName = xsqlvars[i].sqlname;

                if (relationName == null || fieldName == null) continue;

                sb.append(GET_FIELD_INFO);

                params.add(fieldName);
                params.add(relationName);

                if (i < maxLength - 1)
                    sb.append("\n").append("UNION").append("\n");

            }

            FBDatabaseMetaData metaData = new FBDatabaseMetaData(gdsHelper);
            ResultSet rs = metaData.doQuery(sb.toString(), params);

            try {

                while(rs.next()) {
                    ExtendedFieldInfo fieldInfo = new ExtendedFieldInfo();

                    fieldInfo.relationName = rs.getString("RELATION_NAME");
                    fieldInfo.fieldName = rs.getString("FIELD_NAME");
                    fieldInfo.fieldLength = rs.getInt("FIELD_LENGTH");
                    fieldInfo.fieldPrecision = rs.getInt("FIELD_PRECISION");
                    fieldInfo.fieldScale = rs.getInt("FIELD_SCALE");
                    fieldInfo.fieldSubtype = rs.getInt("FIELD_SUB_TYPE");
                    fieldInfo.characterSetId = rs.getInt("CHARACTER_SET_ID");
                    fieldInfo.characterLength = rs.getInt("CHAR_LEN");

                    if (rs.wasNull())
                        fieldInfo.characterLength =
                            fieldInfo.fieldLength /
                            EncodingFactory.getCharacterSetSize(fieldInfo.characterSetId);

                    result.put(
                        new FieldKey(fieldInfo.relationName, fieldInfo.fieldName),
                        fieldInfo);
                }
            } finally {
                rs.close();
            }
            pending -= maxLength;
        }
        return result;
    }
    
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface != null && iface.isAssignableFrom(getClass());
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (!isWrapperFor(iface))
            throw new FBDriverNotCapableException();
        
        return iface.cast(this);
    }
    
    /**
     * Strategy for retrieving column labels and column names
     */
    private enum ColumnStrategy {
        /**
         * Default, JDBC-compliant, strategy for column naming. 
         * <p>
         * columnLabel is the AS clause (xsqlvar.aliasname) if specified, 
         * otherwise xsqlvar.sqlname.
         * </p>
         * <p>
         * columnName is xsqlvar.sqlname if specified, otherwise xsqlvar.aliasname (TODO: change this?)
         * <p>
         */
        DEFAULT {
            @Override
            String getColumnName(XSQLVAR xsqlvar) {
                if (xsqlvar.sqlname == null) {
                    return getColumnLabel(xsqlvar);
                } else {
                    return xsqlvar.sqlname;
                }
            }
        },
        /**
         * Alternative strategy for column naming (related to columnLabelForName connection property)
         * <p>
         * This strategy is not JDBC-compliant, but is provided as a workaround for use with com.sun.rowset.CachedRowSetImpl and
         * for people expecting the old behavior.
         * <p>
         * columnLabel is the AS clause (xsqlvar.aliasname) if specified, 
         * otherwise xsqlvar.sqlname.
         * </p>
         * <p>
         * columnName is identical to columnLabel.
         * </p>
         */
        COLUMN_LABEL_FOR_NAME {
            @Override
            String getColumnName(XSQLVAR xsqlvar) {
                return getColumnLabel(xsqlvar);
            }
        }        
        ;       
        
        /**
         * Retrieve the columnName for the specified column.
         * 
         * @param xsqlvar Column XSQLVAR
         * @return value for the columnName
         */
        abstract String getColumnName(XSQLVAR xsqlvar);
        
        /**
         * Retrieve the columnLabel for the specified column.
         * 
         * @param xsqlvar Column XSQLVAR
         * @return value for the columnLabel
         */
        String getColumnLabel(XSQLVAR xsqlvar) {
            if (xsqlvar.aliasname != null) {
                return xsqlvar.aliasname;
            } else if (xsqlvar.sqlname != null) {
                return xsqlvar.sqlname;
            } else {
                return "";
            }
        }
    }
}
