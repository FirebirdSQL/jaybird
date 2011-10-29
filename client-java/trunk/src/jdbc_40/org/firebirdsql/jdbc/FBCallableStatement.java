/*
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

import java.sql.*;

import org.firebirdsql.gds.impl.GDSHelper;

/**
 * JDBC 4.x compliant implementation of {@link java.sql.CallableStatement}.
 */
public class FBCallableStatement extends AbstractCallableStatement {

    /**
     * Create instance of this class.
     * 
     * @param c
     *            instance of {@link AbstractConnection}
     * @param sql
     *            SQL statement containing procedure call.
     * 
     * @throws SQLException
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
        throw new FBDriverNotCapableException();
    }

    public NClob getNClob(String parameterName) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public RowId getRowId(int parameterIndex) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public RowId getRowId(String parameterName) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public SQLXML getSQLXML(int parameterIndex) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public SQLXML getSQLXML(String parameterName) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setNClob(String parameterName, NClob value) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setRowId(String parameterName, RowId x) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setSQLXML(String parameterName, SQLXML xmlObject) throws SQLException {
        throw new FBDriverNotCapableException();
    }
}
