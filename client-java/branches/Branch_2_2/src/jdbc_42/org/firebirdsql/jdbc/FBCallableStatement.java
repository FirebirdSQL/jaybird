/*
 * $Id$
 * 
 * Firebird Open Source J2ee connector - jdbc driver
 * 
 * Distributable under LGPL license. You may obtain a copy of the License at
 * http://www.gnu.org/copyleft/lgpl.html
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the LGPL License for more details.
 * 
 * This file was created by members of the firebird development team. All
 * individual contributions remain the Copyright (C) of those individuals.
 * Contributors to this file are either listed here or can be obtained from a
 * CVS history command.
 * 
 * All rights reserved.
 */
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.impl.GDSHelper;

import java.sql.*;

/**
 * JDBC 4.x compliant implementation of {@link java.sql.CallableStatement}.
 */
public class FBCallableStatement extends AbstractCallableStatement {

    /**
     * Create instance of this class.
     * 
     * @param c
     *            instance of {@link org.firebirdsql.jdbc.AbstractConnection}
     * @param sql
     *            SQL statement containing procedure call.
     *
     * @throws java.sql.SQLException
     *             if SQL error occured.
     */
    public FBCallableStatement(GDSHelper c, String sql, int rsType, int rsConcurrency,
            int rsHoldability, StoredProcedureMetaData storedProcedureMetaData,
            FBObjectListener.StatementListener statementListener,
            FBObjectListener.BlobListener blobListener) throws SQLException {
        super(c, sql, rsType, rsConcurrency, rsHoldability, storedProcedureMetaData,
                statementListener, blobListener);
    }

    public NClob getNClob(int parameterIndex) throws SQLException {
        assertHasData(getCurrentResultSet());
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getCurrentResultSet().getNClob(parameterIndex);
    }

    public NClob getNClob(String parameterName) throws SQLException {
        return getNClob(findOutParameter(parameterName));
    }

    public RowId getRowId(int parameterIndex) throws SQLException {
        assertHasData(getCurrentResultSet());
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getCurrentResultSet().getRowId(parameterIndex);
    }

    public RowId getRowId(String parameterName) throws SQLException {
        return getRowId(findOutParameter(parameterName));
    }

    public SQLXML getSQLXML(int parameterIndex) throws SQLException {
        assertHasData(getCurrentResultSet());
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getCurrentResultSet().getSQLXML(parameterIndex);
    }

    public SQLXML getSQLXML(String parameterName) throws SQLException {
        return getSQLXML(findOutParameter(parameterName));
    }

    public void setNClob(String parameterName, NClob value) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setRowId(String parameterName, RowId x) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setSQLXML(String parameterName, SQLXML xmlObject) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    @Override
    public void registerOutParameter(int parameterIndex, SQLType sqlType) throws SQLException {
        registerOutParameter(parameterIndex, sqlType.getVendorTypeNumber());
    }

    @Override
    public void registerOutParameter(int parameterIndex, SQLType sqlType, int scale) throws SQLException {
        registerOutParameter(parameterIndex, sqlType.getVendorTypeNumber(), scale);
    }

    @Override
    public void registerOutParameter(int parameterIndex, SQLType sqlType, String typeName) throws SQLException {
        registerOutParameter(parameterIndex, sqlType.getVendorTypeNumber(), typeName);
    }

    @Override
    public void registerOutParameter(String parameterName, SQLType sqlType) throws SQLException {
        registerOutParameter(parameterName, sqlType.getVendorTypeNumber());
    }

    @Override
    public void registerOutParameter(String parameterName, SQLType sqlType, int scale) throws SQLException {
        registerOutParameter(parameterName, sqlType.getVendorTypeNumber(), scale);
    }

    @Override
    public void registerOutParameter(String parameterName, SQLType sqlType, String typeName) throws SQLException {
        registerOutParameter(parameterName, sqlType.getVendorTypeNumber(), typeName);
    }
}
