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
import java.sql.Array;
import java.sql.Blob;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;

import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.XSQLVAR;
import org.firebirdsql.gds.impl.GDSHelper;

/**
 * Describe class <code>FBParameterMetaData</code> here.
 *
 * @author <a href="mailto:skidder@users.sourceforge.net">Nickolay Samofatov</a>
 * @version 1.0
 */
public class FBParameterMetaData implements FirebirdParameterMetaData {

    private final XSQLVAR[] xsqlvars;
    private final GDSHelper connection;

    /**
     * Creates a new <code>FBParameterMetaData</code> instance.
     *
     * @param xsqlvars a <code>XSQLVAR[]</code> value
     * @param connection a <code>AbstractConnection</code> value
     * @exception SQLException if an error occurs
     *
     */
    protected FBParameterMetaData(XSQLVAR[] xsqlvars, GDSHelper connection) throws SQLException {
        this.xsqlvars = xsqlvars;
        this.connection = connection;
    }

    private String getIscEncoding() {
        if (connection != null)
            return connection.getIscEncoding();
        else
            return "NONE";
    }

    /**
     * Retrieves the number of parameters in the <code>PreparedStatement</code>
     * object for which this <code>ParameterMetaData</code> object contains
     * information.
     *
     * @return the number of parameters
     * @exception SQLException if a database access error occurs
     * @since 1.4
     */
    public  int getParameterCount() {
        return xsqlvars.length;
    }


    /**
     * Retrieves whether null values are allowed in the designated parameter.
     *
     * @param parameter the first parameter is 1, the second is 2, ...
     * @return the nullability status of the given parameter; one of
     *        <code>ParameterMetaData.parameterNoNulls</code>,
     *        <code>ParameterMetaData.parameterNullable</code>, or
     *        <code>ParameterMetaData.parameterNullableUnknown</code>
     * @exception SQLException if a database access error occurs
     * @since 1.4
     */
    public  int isNullable(int parameter) throws  SQLException {
        if ((getXsqlvar(parameter).sqltype & 1) == 1) {
            return parameterNullable;
        }
        else {
            return parameterNoNulls;
        }
    }


    /**
     * Retrieves whether values for the designated parameter can be signed numbers.
     *
     * @param parameter the first parameter is 1, the second is 2, ...
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since 1.4
     */
    public  boolean isSigned(int parameter) throws  SQLException {
        switch (getXsqlvar(parameter).sqltype & ~1) {
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
     * Retrieves the designated parameter's number of decimal digits.
     *
     * @param parameter the first parameter is 1, the second is 2, ...
     * @return precision
     * @exception SQLException if a database access error occurs
     * @since 1.4
     */
    public  int getPrecision(int parameter) throws  SQLException {
        int colType = getParameterType(parameter);

        switch (colType){

            case Types.DECIMAL:
            case Types.NUMERIC:

                return estimatePrecision(parameter);

            case Types.CHAR:
            case Types.VARCHAR: {
                XSQLVAR var = getXsqlvar(parameter);
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
            case Types.SMALLINT:
                return 5;
            case Types.DATE:
                return 10;
            case Types.TIME:
                return 8;
            case Types.TIMESTAMP:
                return 19;
            default:
                return 0;
        }
    }


    /**
     * Retrieves the designated parameter's number of digits to right of the decimal point.
     *
     * @param parameter the first parameter is 1, the second is 2, ...
     * @return scale
     * @exception SQLException if a database access error occurs
     * @since 1.4
     */
    public  int getScale(int parameter) throws  SQLException {
        return getXsqlvar(parameter).sqlscale * (-1);
    }


    /**
     * Retrieves the designated parameter's SQL type.
     *
     * @param parameter the first parameter is 1, the second is 2, ...
     * @return SQL type from <code>java.sql.Types</code>
     * @exception SQLException if a database access error occurs
     * @since 1.4
     * @see Types
     */
    public  int getParameterType(int parameter) throws  SQLException {
        int sqltype = getXsqlvar(parameter).sqltype & ~1;
        int sqlscale = getXsqlvar(parameter).sqlscale;
        int sqlsubtype = getXsqlvar(parameter).sqlsubtype;

        if (sqlscale < 0) {
            switch (sqltype) {
                case ISCConstants.SQL_SHORT:
                case ISCConstants.SQL_LONG:
                case ISCConstants.SQL_INT64:
                case ISCConstants.SQL_DOUBLE:
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
                else
                if (sqlsubtype == 2)
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
            default:
                return Types.NULL;
        }
    }


    /**
     * Retrieves the designated parameter's database-specific type name.
     *
     * @param parameter the first parameter is 1, the second is 2, ...
     * @return type the name used by the database. If the parameter type is
     * a user-defined type, then a fully-qualified type name is returned.
     * @exception SQLException if a database access error occurs
     * @since 1.4
     */
    public  String getParameterTypeName(int parameter) throws  SQLException {
        // Must return the same value as DatamaseMetaData getColumns Type_Name
        int sqltype = getXsqlvar(parameter).sqltype & ~1;
        int sqlscale = getXsqlvar(parameter).sqlscale;
        int sqlsubtype = getXsqlvar(parameter).sqlsubtype;

        if (sqlscale < 0) {
            switch (sqltype) {
                case ISCConstants.SQL_SHORT:
                case ISCConstants.SQL_LONG:
                case ISCConstants.SQL_INT64:
                case ISCConstants.SQL_DOUBLE:
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
                if (sqlsubtype == 1)
                    return "NUMERIC";
                else if (sqlsubtype == 2)
                    return "DECIMAL";
                else
                    return "BIGINT";
            case ISCConstants.SQL_BLOB:
                if (sqlsubtype < 0)
                    return "BLOB SUB_TYPE " + sqlsubtype;
                else if (sqlsubtype == 0)
                    return "BLOB SUB_TYPE 0";
                else if (sqlsubtype == 1)
                    return "BLOB SUB_TYPE 1";
                else
                    return "BLOB SUB_TYPE " + sqlsubtype;
            case ISCConstants.SQL_QUAD:
                return "ARRAY";
            default:
                return "NULL";
        }
    }


    /**
     * Retrieves the fully-qualified name of the Java class whose instances
     * should be passed to the method <code>PreparedStatement.setObject</code>.
     *
     * @param parameter the first parameter is 1, the second is 2, ...
     * @return the fully-qualified name of the class in the Java programming
     *         language that would be used by the method
     *         <code>PreparedStatement.setObject</code> to set the value
     *         in the specified parameter. This is the class name used
     *         for custom mapping.
     * @exception SQLException if a database access error occurs
     * @since 1.4
     */
    public String getParameterClassName(int parameter) throws  SQLException {
        switch (getXsqlvar(parameter).sqltype & ~1) {

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
                XSQLVAR field = getXsqlvar(parameter);
            
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
                if (getXsqlvar(parameter).sqlscale == 0) {
                    return Long.class.getName();
                }
                else {
                    return BigDecimal.class.getName();
                }
            
            default:
                throw new SQLException("Unkown SQL type", 
                        FBSQLException.SQL_STATE_INVALID_PARAM_TYPE);
        }
    }

    /**
     * Retrieves the designated parameter's mode.
     *
     * @param param the first parameter is 1, the second is 2, ...
     * @return mode of the parameter; one of
     *        <code>ParameterMetaData.parameterModeIn</code>,
     *        <code>ParameterMetaData.parameterModeOut</code>, or
     *        <code>ParameterMetaData.parameterModeInOut</code>
     *        <code>ParameterMetaData.parameterModeUnknown</code>.
     * @exception SQLException if a database access error occurs
     * @since 1.4
     */
    public int getParameterMode(int param) throws SQLException {
        return parameterModeIn;
    }

    //private methods

    private XSQLVAR getXsqlvar(int parameterIndex) {
        return xsqlvars[parameterIndex - 1];
    }

    private int estimatePrecision(int parameterIndex) {
        int sqltype = getXsqlvar(parameterIndex).sqltype & ~1;
        int sqlscale = getXsqlvar(parameterIndex).sqlscale;

        switch(sqltype) {
            case ISCConstants.SQL_SHORT : return 5;
            case ISCConstants.SQL_LONG : return 10;
            case ISCConstants.SQL_INT64 : return 19;
            case ISCConstants.SQL_DOUBLE : return 19;
            default : return 0;
        }
    }
    
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface != null && iface.isAssignableFrom(getClass());
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (!isWrapperFor(iface))
            throw new FBDriverNotCapableException();
        
        return iface.cast(this);
    }

}
