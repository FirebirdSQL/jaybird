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

import org.firebirdsql.gds.XSQLVAR;
import org.firebirdsql.gds.impl.AbstractIscStmtHandle;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.jdbc.FBObjectListener.ResultSetListener;

import java.io.Reader;
import java.sql.*;
import java.util.ArrayList;

/**
 * JDBC 4.2 implementation of {@link java.sql.ResultSet} interface.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class FBResultSet extends AbstractResultSet {

    public FBResultSet(GDSHelper gdsHelper, AbstractStatement fbStatement,
            AbstractIscStmtHandle stmt, ResultSetListener listener,
            boolean metaDataQuery, int rsType, int rsConcurrency,
            int rsHoldability, boolean cached) throws SQLException {
        
        super(gdsHelper, fbStatement, stmt, listener, metaDataQuery, rsType,
                rsConcurrency, rsHoldability, cached);
    }

    public FBResultSet(XSQLVAR[] xsqlvars, ArrayList rows) throws SQLException {
        super(xsqlvars, rows);
    }

    public NClob getNClob(int columnIndex) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public NClob getNClob(String columnLabel) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public RowId getRowId(int columnIndex) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public RowId getRowId(String columnLabel) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public SQLXML getSQLXML(int columnIndex) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public SQLXML getSQLXML(String columnLabel) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void updateNClob(int columnIndex, NClob clob) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void updateNClob(int columnIndex, Reader reader, long length)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void updateNClob(int columnIndex, Reader reader) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void updateNClob(String columnLabel, NClob clob) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void updateNClob(String columnLabel, Reader reader, long length)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void updateNClob(String columnLabel, Reader reader)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void updateRowId(int columnIndex, RowId x) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void updateRowId(String columnLabel, RowId x) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void updateSQLXML(int columnIndex, SQLXML xmlObject)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void updateSQLXML(String columnLabel, SQLXML xmlObject)
            throws SQLException {
        throw new FBDriverNotCapableException();
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
