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
@SuppressWarnings("RedundantThrows")
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

    @Override
    public int getParameterCount() throws SQLException {
        return getFieldCount();
    }

    @Override
    public int isNullable(int parameter) throws SQLException {
        return (getFieldDescriptor(parameter).getType() & 1) == 1
                ? ParameterMetaData.parameterNullable
                : ParameterMetaData.parameterNoNulls;
    }

    @Override
    public boolean isSigned(int parameter) throws SQLException {
        return isSignedInternal(parameter);
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>NOTE</b> For <code>NUMERIC</code> and <code>DECIMAL</code> the reported precision is the maximum precision
     * allowed by the underlying storage data type, it is <b>not the declared precision</b>.
     * </p>
     */
    @Override
    public int getPrecision(int parameter) throws SQLException {
        return getPrecisionInternal(parameter);
    }

    @Override
    public int getScale(int parameter) throws SQLException {
        return getScaleInternal(parameter);
    }

    @Override
    public int getParameterType(int parameter) throws SQLException {
        return getFieldType(parameter);
    }

    @Override
    public String getParameterTypeName(int parameter) throws SQLException {
        return getFieldTypeName(parameter);
    }

    @Override
    public String getParameterClassName(int parameter) throws SQLException {
        return getFieldClassName(parameter);
    }

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
