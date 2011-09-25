/*
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

import java.io.InputStream;
import java.io.Reader;
import java.sql.*;
import java.util.ArrayList;

import org.firebirdsql.gds.XSQLVAR;
import org.firebirdsql.gds.impl.AbstractIscStmtHandle;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.jdbc.FBObjectListener.ResultSetListener;


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

    public Reader getNCharacterStream(int columnIndex) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public Reader getNCharacterStream(String columnLabel) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public NClob getNClob(int columnIndex) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public NClob getNClob(String columnLabel) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public String getNString(int columnIndex) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public String getNString(String columnLabel) throws SQLException {
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

    public void updateAsciiStream(int columnIndex, InputStream x, long length)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void updateAsciiStream(int columnIndex, InputStream x)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void updateAsciiStream(String columnLabel, InputStream x, long length)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void updateAsciiStream(String columnLabel, InputStream x)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void updateBinaryStream(int columnIndex, InputStream x, long length)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void updateBinaryStream(int columnIndex, InputStream x)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void updateBinaryStream(String columnLabel, InputStream x,
            long length) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void updateBinaryStream(String columnLabel, InputStream x)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void updateBlob(int columnIndex, InputStream inputStream, long length)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void updateBlob(int columnIndex, InputStream inputStream)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void updateBlob(String columnLabel, InputStream inputStream,
            long length) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void updateBlob(String columnLabel, InputStream inputStream)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void updateCharacterStream(int columnIndex, Reader x, long length)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void updateCharacterStream(int columnIndex, Reader x)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void updateCharacterStream(String columnLabel, Reader reader,
            long length) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void updateCharacterStream(String columnLabel, Reader reader)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void updateClob(int columnIndex, Reader reader, long length)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void updateClob(int columnIndex, Reader reader) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void updateClob(String columnLabel, Reader reader, long length)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void updateClob(String columnLabel, Reader reader)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void updateNCharacterStream(int columnIndex, Reader x, long length)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void updateNCharacterStream(int columnIndex, Reader x)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void updateNCharacterStream(String columnLabel, Reader reader,
            long length) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void updateNCharacterStream(String columnLabel, Reader reader)
            throws SQLException {
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

    public void updateNString(int columnIndex, String string)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void updateNString(String columnLabel, String string)
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

    // java.sql.Wrapper interface
    
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface != null && iface.isAssignableFrom(this.getClass());
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (!isWrapperFor(iface))
            throw new FBDriverNotCapableException();
        
        return iface.cast(this);
    }
    
    // JDBC 4.1

    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        // TODO Write implementation
        throw new FBDriverNotCapableException();
    }

    public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
        // TODO Write implementation
        throw new FBDriverNotCapableException();
    }
}
