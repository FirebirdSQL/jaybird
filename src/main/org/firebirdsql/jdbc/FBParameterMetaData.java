/*
 SPDX-FileCopyrightText: Copyright 2003 Nikolay Samofatov
 SPDX-FileCopyrightText: Copyright 2003-2007 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2005-2006 Steven Jardine
 SPDX-FileCopyrightText: Copyright 2011-2024 Mark Rotteveel
 SPDX-License-Identifier: LGPL-2.1-or-later
*/
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.util.InternalApi;

import java.sql.ParameterMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

/**
 * An object that can be used to get information about the types and properties for each parameter marker in a
 * {@link java.sql.PreparedStatement} object.
 * <p>
 * This class is internal API of Jaybird. Future versions may radically change, move, or make inaccessible this type.
 * For the public API, refer to the {@link ParameterMetaData} and {@link FirebirdParameterMetaData} interfaces.
 * </p>
 *
 * @author Nickolay Samofatov
 * @author Mark Rotteveel
 * @version 1.0
 */
@SuppressWarnings("RedundantThrows")
@InternalApi
public class FBParameterMetaData extends AbstractFieldMetaData implements FirebirdParameterMetaData {

    /**
     * Creates a new {@code FBParameterMetaData} instance.
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
