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

import org.firebirdsql.gds.ng.fields.RowDescriptor;

import java.sql.ParameterMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

/**
 * An object that can be used to get information about the types and properties for each parameter marker in a
 * <code>PreparedStatement</code> object.
 *
 * @author <a href="mailto:skidder@users.sourceforge.net">Nickolay Samofatov</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @version 1.0
 */
public class FBParameterMetaData extends AbstractFieldMetaData implements FirebirdParameterMetaData {

    /**
     * Creates a new <code>FBParameterMetaData</code> instance.
     *
     * @param rowDescriptor
     *         a row descriptor
     * @param connection
     *         a <code>FBConnection</code> value
     * @throws SQLException
     *         if an error occurs
     */
    protected FBParameterMetaData(RowDescriptor rowDescriptor, FBConnection connection) throws SQLException {
        super(rowDescriptor, connection);
    }

    /**
     * Retrieves the number of parameters in the <code>PreparedStatement</code>
     * object for which this <code>ParameterMetaData</code> object contains
     * information.
     *
     * @return the number of parameters
     * @throws SQLException
     *         if a database access error occurs
     */
    @Override
    public int getParameterCount() throws SQLException {
        return getFieldCount();
    }

    /**
     * Retrieves whether null values are allowed in the designated parameter.
     *
     * @param parameter
     *         the first parameter is 1, the second is 2, ...
     * @return the nullability status of the given parameter; one of
     * <code>ParameterMetaData.parameterNoNulls</code>,
     * <code>ParameterMetaData.parameterNullable</code>, or
     * <code>ParameterMetaData.parameterNullableUnknown</code>
     * @throws SQLException
     *         if a database access error occurs
     */
    @Override
    public int isNullable(int parameter) throws SQLException {
        return (getFieldDescriptor(parameter).getType() & 1) == 1
                ? ParameterMetaData.parameterNullable
                : ParameterMetaData.parameterNoNulls;
    }

    /**
     * Retrieves whether values for the designated parameter can be signed numbers.
     *
     * @param parameter
     *         the first parameter is 1, the second is 2, ...
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @throws SQLException
     *         if a database access error occurs
     */
    @Override
    public boolean isSigned(int parameter) throws SQLException {
        return isSignedInternal(parameter);
    }

    /**
     * Retrieves the designated parameter's specified column size.
     * <p/>
     * <p>
     * The returned value represents the maximum column size for the given parameter.
     * For numeric data, this is the maximum precision.  For character data, this is the length in characters.
     * For datetime datatypes, this is the length in characters of the String representation (assuming the
     * maximum allowed precision of the fractional seconds component). For binary data, this is the length in bytes.
     * For the ROWID datatype, this is the length in bytes. 0 is returned for data types where the
     * column size is not applicable.
     * </p>
     * <p>
     * <b>NOTE</b> For <code>NUMERIC</code> and <code>DECIMAL</code> the reported precision is the maximum precision
     * allowed by the underlying storage data type, it is <b>not the declared precision</b>.
     * </p>
     *
     * @param parameter
     *         the first parameter is 1, the second is 2, ...
     * @return precision
     * @throws SQLException
     *         if a database access error occurs
     */
    @Override
    public int getPrecision(int parameter) throws SQLException {
        return getPrecisionInternal(parameter);
    }

    /**
     * Retrieves the designated parameter's number of digits to right of the decimal point.
     * 0 is returned for data types where the scale is not applicable.
     *
     * @param parameter
     *         the first parameter is 1, the second is 2, ...
     * @return scale
     * @throws SQLException
     *         if a database access error occurs
     */
    @Override
    public int getScale(int parameter) throws SQLException {
        return getScaleInternal(parameter);
    }

    /**
     * Retrieves the designated parameter's SQL type.
     *
     * @param parameter
     *         the first parameter is 1, the second is 2, ...
     * @return SQL type from <code>java.sql.Types</code>
     * @throws SQLException
     *         if a database access error occurs
     * @see java.sql.Types
     */
    @Override
    public int getParameterType(int parameter) throws SQLException {
        return getFieldType(parameter);
    }

    /**
     * Retrieves the designated parameter's database-specific type name.
     *
     * @param parameter
     *         the first parameter is 1, the second is 2, ...
     * @return type the name used by the database. If the parameter type is
     * a user-defined type, then a fully-qualified type name is returned.
     * @throws SQLException
     *         if a database access error occurs
     */
    @Override
    public String getParameterTypeName(int parameter) throws SQLException {
        return getFieldTypeName(parameter);
    }

    /**
     * Retrieves the fully-qualified name of the Java class whose instances
     * should be passed to the method <code>PreparedStatement.setObject</code>.
     *
     * @param parameter
     *         the first parameter is 1, the second is 2, ...
     * @return the fully-qualified name of the class in the Java programming
     * language that would be used by the method
     * <code>PreparedStatement.setObject</code> to set the value
     * in the specified parameter. This is the class name used
     * for custom mapping.
     * @throws SQLException
     *         if a database access error occurs
     */
    @Override
    public String getParameterClassName(int parameter) throws SQLException {
        return getFieldClassName(parameter);
    }

    /**
     * Retrieves the designated parameter's mode.
     *
     * @param parameter
     *         the first parameter is 1, the second is 2, ...
     * @return mode of the parameter; one of
     * <code>ParameterMetaData.parameterModeIn</code>,
     * <code>ParameterMetaData.parameterModeOut</code>, or
     * <code>ParameterMetaData.parameterModeInOut</code>
     * <code>ParameterMetaData.parameterModeUnknown</code>.
     * @throws SQLException
     *         if a database access error occurs
     */
    @Override
    public int getParameterMode(int parameter) throws SQLException {
        return ParameterMetaData.parameterModeIn;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <code>FBParameterMetaData</code> does not support extended field info, so it always returns an empty Map.
     * </p>
     */
    @Override
    protected Map<FieldKey, ExtendedFieldInfo> getExtendedFieldInfo(FBConnection connection) throws SQLException {
        return Collections.emptyMap();
    }
}
