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
import org.firebirdsql.gds.GDS;
import org.firebirdsql.gds.isc_stmt_handle;
import org.firebirdsql.gds.XSQLDA;
import org.firebirdsql.gds.XSQLVAR;

import java.math.BigDecimal;

import java.sql.ResultSetMetaData;

import java.sql.Blob;
import java.sql.Array;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

/**
 *
 *   @see <related>
 *   @author David Jencks (davidjencks@earthlink.net)
 *   @version $ $
 */


/**
 * An object that can be used to get information about the types
 * and properties of the columns in a <code>ResultSet</code> object.
 * The following code fragment creates the <code>ResultSet</code> object rs,
 * creates the <code>ResultSetMetaData</code> object rsmd, and uses rsmd
 * to find out how many columns rs has and whether the first column in rs
 * can be used in a <code>WHERE</code> clause.
 * <PRE>
 *
 *     ResultSet rs = stmt.executeQuery("SELECT a, b, c FROM TABLE2");
 *     ResultSetMetaData rsmd = rs.getMetaData();
 *     int numberOfColumns = rsmd.getColumnCount();
 *     boolean b = rsmd.isSearchable(1);
 *
 * </PRE>
 */

public class FBResultSetMetaData implements ResultSetMetaData {

    // private isc_stmt_handle stmt;
    private XSQLVAR[] xsqlvars;

    /*    FBResultSetMetaData(isc_stmt_handle stmt) {
        this.stmt = stmt;
        }*/

    FBResultSetMetaData(XSQLVAR[] xsqlvars) {
        this.xsqlvars = xsqlvars;
        }

    /**
     * Returns the number of columns in this <code>ResultSet</code> object.
     *
     * @return the number of columns
     * @exception SQLException if a database access error occurs
     */
    public  int getColumnCount() {
        //return stmt.getOutSqlda().sqln;;
        return xsqlvars.length;
    }


    /**
     * Indicates whether the designated column is automatically numbered, thus read-only.
     *
     * @param column the first column is 1, the second is 2, ...
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
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
        if (((getXsqlvar(column).sqltype & ~1) == GDS.SQL_ARRAY)
            || ((getXsqlvar(column).sqltype & ~1) == GDS.SQL_BLOB)) {
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
            case GDS.SQL_SHORT:
            case GDS.SQL_LONG:
            case GDS.SQL_FLOAT:
            case GDS.SQL_DOUBLE:
            case GDS.SQL_D_FLOAT:
            case GDS.SQL_INT64:
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
        //These are mostly wrong!!
        switch (getXsqlvar(column).sqltype & ~1) {
            case GDS.SQL_TEXT:
                return getXsqlvar(column).sqllen;
            case GDS.SQL_VARYING:
                return getXsqlvar(column).sqllen;
            case GDS.SQL_SHORT:
                return getXsqlvar(column).sqllen;
            case GDS.SQL_LONG:
                return getXsqlvar(column).sqllen;
            case GDS.SQL_FLOAT:
                return getXsqlvar(column).sqllen;
            case GDS.SQL_DOUBLE:
                return getXsqlvar(column).sqllen;
            case GDS.SQL_D_FLOAT:
                return getXsqlvar(column).sqllen;
            case GDS.SQL_TIMESTAMP:
                return getXsqlvar(column).sqllen;
            case GDS.SQL_BLOB:
                return 0;
            case GDS.SQL_ARRAY:
                return 0;
            case GDS.SQL_QUAD:
                return getXsqlvar(column).sqllen;
            case GDS.SQL_TYPE_TIME:
                return getXsqlvar(column).sqllen;
            case GDS.SQL_TYPE_DATE:
                return 10;
            case GDS.SQL_INT64:
                if (getXsqlvar(column).sqlscale == 0) {
                    return getXsqlvar(column).sqllen;
                }
                else {
                    return getXsqlvar(column).sqllen;
                }
            default:
                throw new SQLException("Unkown sql type");
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
        return (getXsqlvar(column).aliasname == null) ? 
            getXsqlvar(column).sqlname: getXsqlvar(column).aliasname;
    }


    /**
     * Get the designated column's name.
     *
     * @param column the first column is 1, the second is 2, ...
     * @return column name
     * @exception SQLException if a database access error occurs
     */
    public  String getColumnName(int column) throws  SQLException {
        //return getXsqlvar(column).sqlname;
        return getColumnLabel(column);
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
        //presumable wrong!!
        return getXsqlvar(column).sqllen;
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
        return getXsqlvar(column).relname;
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
                case GDS.SQL_SHORT:
                case GDS.SQL_LONG:
                case GDS.SQL_INT64:
                case GDS.SQL_DOUBLE:
                    if (sqlsubtype == 2)
                        return Types.DECIMAL;
                    else
                        return Types.NUMERIC;
                default:
                    break;
            }
        }

        switch (sqltype) {
            case GDS.SQL_SHORT:
                return Types.SMALLINT;
            case GDS.SQL_LONG:
                return Types.INTEGER;
            case GDS.SQL_DOUBLE:
            case GDS.SQL_D_FLOAT:
                return Types.DOUBLE;
            case GDS.SQL_FLOAT:
                return Types.FLOAT;
            case GDS.SQL_TEXT:
                return Types.CHAR;
            case GDS.SQL_VARYING:
                return Types.VARCHAR;
            case GDS.SQL_TIMESTAMP:
                return Types.TIMESTAMP;
            case GDS.SQL_TYPE_TIME:
                return Types.TIME;
            case GDS.SQL_TYPE_DATE:
                return Types.DATE;
            case GDS.SQL_INT64:
                if (sqlsubtype == 2)
                    return Types.DECIMAL;
                else
                    return Types.NUMERIC;
            case GDS.SQL_BLOB:
                if (sqlsubtype < 0)
                    return Types.BLOB;
                else if (sqlsubtype == 0)
                    return Types.LONGVARBINARY;
                else if (sqlsubtype == 1)
                    return Types.LONGVARCHAR;
                else
                    return Types.OTHER;
            case GDS.SQL_QUAD:
                return Types.OTHER;
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
        // Must return the same value as DatamaseMetaData getColumns Type_Name
        int sqltype = getXsqlvar(column).sqltype & ~1;
        int sqlscale = getXsqlvar(column).sqlscale;
        int sqlsubtype = getXsqlvar(column).sqlsubtype;

        if (sqlscale < 0) {
            switch (sqltype) {
                case GDS.SQL_SHORT:
                case GDS.SQL_LONG:
                case GDS.SQL_INT64:
                case GDS.SQL_DOUBLE:
                    if (sqlsubtype == 2)
                        return "DECIMAL";
                    else
                        return "NUMERIC";
                default:
                    break;
            }
        }

        switch (sqltype) {
            case GDS.SQL_SHORT:
                return "SMALLINT";
            case GDS.SQL_LONG:
                return "INTEGER";
            case GDS.SQL_DOUBLE:
            case GDS.SQL_D_FLOAT:
                return "DOUBLE PRECISION";
            case GDS.SQL_FLOAT:
                return "FLOAT";
            case GDS.SQL_TEXT:
                return "CHAR";
            case GDS.SQL_VARYING:
                return "VARCHAR";
            case GDS.SQL_TIMESTAMP:
                return "TIMESTAMP";
            case GDS.SQL_TYPE_TIME:
                return "TIME";
            case GDS.SQL_TYPE_DATE:
                return "DATE";
            case GDS.SQL_INT64:
                if (sqlsubtype == 2)
                    return "DECIMAL";
                else
                    return "NUMERIC";
            case GDS.SQL_BLOB:
                if (sqlsubtype < 0)
                    return "BLOB SUB_TYPE <0";
                else if (sqlsubtype == 0)
                    return "BLOB SUB_TYPE 0";
                else if (sqlsubtype == 1)
                    return "BLOB SUB_TYPE 1";
                else
                    return "BLOB SUB_TYPE >1";
            case GDS.SQL_QUAD:
                return "ARRAY";
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
        throw new SQLException("Not yet implemented");
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
        throw new SQLException("Not yet implemented");
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
        throw new SQLException("Not yet implemented");
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
            case GDS.SQL_TEXT:
                return String.class.getName();
            case GDS.SQL_VARYING:
                return String.class.getName();
            case GDS.SQL_SHORT:
                return Short.class.getName();
            case GDS.SQL_LONG:
                return Long.class.getName();
            case GDS.SQL_FLOAT:
                return Float.class.getName();
            case GDS.SQL_DOUBLE:
                return Double.class.getName();
            case GDS.SQL_D_FLOAT:
                return Double.class.getName();
            case GDS.SQL_TIMESTAMP:
                return java.sql.Timestamp.class.getName();
            case GDS.SQL_BLOB:
                return Blob.class.getName();
            case GDS.SQL_ARRAY:
                return Array.class.getName();
            case GDS.SQL_QUAD:
                return Long.class.getName();
            case GDS.SQL_TYPE_TIME:
                return java.sql.Time.class.getName();
            case GDS.SQL_TYPE_DATE:
                return java.sql.Date.class.getName();
            case GDS.SQL_INT64:
                if (getXsqlvar(column).sqlscale == 0) {
                    return Long.class.getName();
                }
                else {
                    return BigDecimal.class.getName();
                }
            default:
                throw new SQLException("Unkown sql type");
        }
    }


    //private methods

    private XSQLVAR getXsqlvar(int columnIndex) {
        //return stmt.getOutSqlda().sqlvar[columnIndex - 1];
        return xsqlvars[columnIndex - 1];
    }


}
