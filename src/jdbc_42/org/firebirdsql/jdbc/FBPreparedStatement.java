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

import org.firebirdsql.gds.impl.GDSHelper;

import java.sql.SQLException;
import java.sql.SQLType;

/**
 * JDBC 4.2 implementation of {@link java.sql.PreparedStatement} interface.
 * <p>
 * Contains methods specific to the JDBC 4.2 implementation, or exists if there are methods in higher JDBC versions
 * that cannot be defined in JDBC 4.2.
 * </p>
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class FBPreparedStatement extends AbstractPreparedStatement {

    protected FBPreparedStatement(GDSHelper c, int rsType, int rsConcurrency, int rsHoldability,
            FBObjectListener.StatementListener statementListener, FBObjectListener.BlobListener blobListener)
            throws SQLException {
        super(c, rsType, rsConcurrency, rsHoldability, statementListener, blobListener);
    }

    protected FBPreparedStatement(GDSHelper c, String sql, int rsType, int rsConcurrency, int rsHoldability,
            FBObjectListener.StatementListener statementListener, FBObjectListener.BlobListener blobListener,
            boolean metaDataQuery, boolean standaloneStatement, boolean generatedKeys) throws SQLException {
        super(c, sql, rsType, rsConcurrency, rsHoldability, statementListener, blobListener, metaDataQuery, standaloneStatement, generatedKeys);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: behaves as {@link #setObject(int, Object, int, int)} called with
     * {@link SQLType#getVendorTypeNumber()}.
     * </p>
     */
    @Override
    public void setObject(int parameterIndex, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        setObject(parameterIndex, x, targetSqlType.getVendorTypeNumber(), scaleOrLength);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: behaves as {@link #setObject(int, Object, int)} called with
     * {@link SQLType#getVendorTypeNumber()}.
     * </p>
     */
    @Override
    public void setObject(int parameterIndex, Object x, SQLType targetSqlType) throws SQLException {
        setObject(parameterIndex, x, targetSqlType.getVendorTypeNumber());
    }
}
