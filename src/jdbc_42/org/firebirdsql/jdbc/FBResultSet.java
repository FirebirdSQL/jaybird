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

import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.fields.RowValue;

import java.sql.SQLException;
import java.sql.SQLType;
import java.util.List;

/**
 * JDBC 4.2 implementation of {@link java.sql.ResultSet} interface.
 * <p>
 * Contains methods specific to the JDBC 4.2 implementation, or exists if there are methods in higher JDBC versions
 * that cannot be defined in JDBC 4.2.
 * </p>
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class FBResultSet extends AbstractResultSet {

    public FBResultSet(FBConnection connection, FBStatement fbStatement, FbStatement stmt,
            FBObjectListener.ResultSetListener listener, boolean metaDataQuery, int rsType, int rsConcurrency,
            int rsHoldability, boolean cached) throws SQLException {
        super(connection, fbStatement, stmt, listener, metaDataQuery, rsType, rsConcurrency, rsHoldability, cached);
    }

    public FBResultSet(RowDescriptor rowDescriptor, List<RowValue> rows, FBObjectListener.ResultSetListener listener)
            throws SQLException {
        super(rowDescriptor, rows, listener);
    }

    public FBResultSet(RowDescriptor rowDescriptor, List<RowValue> rows) throws SQLException {
        super(rowDescriptor, rows);
    }

    public FBResultSet(RowDescriptor rowDescriptor, FBConnection connection, List<RowValue> rows, boolean retrieveBlobs)
            throws SQLException {
        super(rowDescriptor, connection, rows, retrieveBlobs);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Jaybird delegates to {@link #updateObject(int, Object, int)} and ignores the value of <code>targetSqlType</code>
     * </p>
     */
    @Override
    public void updateObject(int columnIndex, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        updateObject(columnIndex, x, scaleOrLength);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Jaybird delegates to {@link #updateObject(String, Object, int)} and ignores the value of <code>targetSqlType</code>
     * </p>
     */
    @Override
    public void updateObject(String columnLabel, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        updateObject(columnLabel, x, scaleOrLength);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Jaybird delegates to {@link #updateObject(int, Object)} and ignores the value of <code>targetSqlType</code>
     * </p>
     */
    @Override
    public void updateObject(int columnIndex, Object x, SQLType targetSqlType) throws SQLException {
        updateObject(columnIndex, x);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Jaybird delegates to {@link #updateObject(String, Object)} and ignores the value of <code>targetSqlType</code>
     * </p>
     */
    @Override
    public void updateObject(String columnLabel, Object x, SQLType targetSqlType) throws SQLException {
        updateObject(columnLabel, x);
    }
}
