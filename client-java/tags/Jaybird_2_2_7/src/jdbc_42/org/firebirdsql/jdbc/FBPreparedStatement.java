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

import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.jdbc.FBObjectListener.BlobListener;
import org.firebirdsql.jdbc.FBObjectListener.StatementListener;

import java.sql.*;

/**
 * JDBC-4.2 implementation of {@link java.sql.PreparedStatement}.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class FBPreparedStatement extends AbstractPreparedStatement {

    public FBPreparedStatement(GDSHelper c, int rsType, int rsConcurrency, int rsHoldability,
            StatementListener statementListener, BlobListener blobListener) throws SQLException {
        super(c, rsType, rsConcurrency, rsHoldability, statementListener, blobListener);
    }

    public FBPreparedStatement(GDSHelper gdsHelper, String sql, int rsType, int rsConcurrency,
            int rsHoldability, StatementListener statementListener,
            BlobListener blobListener, boolean metaDataQuery,
            boolean standaloneStatement, boolean generatedKeys) throws SQLException {
        super(gdsHelper, sql, rsType, rsConcurrency, rsHoldability, statementListener,
                blobListener, metaDataQuery, standaloneStatement, generatedKeys);
    }

    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    @Override
    public void setObject(int parameterIndex, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        setObject(parameterIndex, x, targetSqlType.getVendorTypeNumber(), scaleOrLength);
    }

    @Override
    public void setObject(int parameterIndex, Object x, SQLType targetSqlType) throws SQLException {
        setObject(parameterIndex, x, targetSqlType.getVendorTypeNumber());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Jaybird does not support update counts exceeding {@link Integer#MAX_VALUE}, this method calls
     * {@link #executeUpdate()}.
     * </p>
     */
    @Override
    public long executeLargeUpdate() throws SQLException {
        return executeUpdate();
    }
}