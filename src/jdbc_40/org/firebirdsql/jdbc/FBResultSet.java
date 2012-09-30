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

import java.io.Reader;
import java.sql.*;
import java.util.ArrayList;

import org.firebirdsql.gds.XSQLVAR;
import org.firebirdsql.gds.impl.AbstractIscStmtHandle;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.jdbc.FBObjectListener.ResultSetListener;

/**
 * Implementation of {@link java.sql.ResultSet} interface.
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
}
