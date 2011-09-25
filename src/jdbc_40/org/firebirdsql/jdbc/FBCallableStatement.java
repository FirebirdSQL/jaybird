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

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;

import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.impl.GDSHelper;

/**
 * JDBC 3.0 compliant implementation of {@link java.sql.CallableStatement}.
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
    public FBCallableStatement(GDSHelper c, String sql, int rsType,
            int rsConcurrency, int rsHoldability,
            StoredProcedureMetaData storedProcedureMetaData,
            FBObjectListener.StatementListener statementListener,
            FBObjectListener.BlobListener blobListener) throws SQLException {
        super(c, sql, rsType, rsConcurrency, rsHoldability, storedProcedureMetaData, statementListener,
                blobListener);
    }

    // --------------------------------------------------------------------------
    // JDBC 3.0
    // --------------------------------------------------------------------------

    public ParameterMetaData getParameterMetaData() throws SQLException {

        statementListener.executionStarted(this);

        Object syncObject = getSynchronizationObject();
        synchronized (syncObject) {
            try {
                prepareFixedStatement(
                    procedureCall.getSQL(selectableProcedure), true);
            } catch (GDSException ge) {
                throw new FBSQLException(ge);
            }
        }

        return new JDBC40ParameterMetaData(fixedStmt.getInSqlda().sqlvar,
                gdsHelper);
    }

    // java.sql.CallableStatement methods --------------------------------------

    public void registerOutParameter(String param1, int param2)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void registerOutParameter(String param1, int param2, int param3)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void registerOutParameter(String param1, int param2, String param3)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setURL(String param1, URL param2) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setNull(String param1, int param2) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setBoolean(String param1, boolean param2) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setByte(String param1, byte param2) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setShort(String param1, short param2) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setInt(String param1, int param2) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setLong(String param1, long param2) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setFloat(String param1, float param2) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setDouble(String param1, double param2) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setBigDecimal(String param1, BigDecimal param2)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setString(String param1, String param2) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setBytes(String param1, byte[] param2) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setDate(String param1, Date param2) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setTime(String param1, Time param2) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setTimestamp(String param1, Timestamp param2)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setAsciiStream(String param1, InputStream param2, int param3)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setBinaryStream(String param1, InputStream param2, int param3)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setObject(String param1, Object param2, int param3, int param4)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setObject(String param1, Object param2, int param3)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setObject(String param1, Object param2) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setCharacterStream(String param1, Reader param2, int param3)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setDate(String param1, Date param2, Calendar param3)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setTime(String param1, Time param2, Calendar param3)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setTimestamp(String param1, Timestamp param2, Calendar param3)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setNull(String param1, int param2, String param3)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void registerOutParameter(int parameterIndex, int sqlType,
            String typeName) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public ResultSet getGeneratedKeys() throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public int executeUpdate(String param1, int param2) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public int executeUpdate(String param1, int[] param2) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public int executeUpdate(String param1, String[] param2)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public boolean execute(String param1, int param2) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public boolean execute(String param1, int[] param2) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public boolean execute(String param1, String[] param2) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    // -------------------------------------------------------------------------
    // JDBC 4.0
    // -------------------------------------------------------------------------

    // java.sql.CallableStatement ----------------------------------------------

    public Reader getCharacterStream(int parameterIndex) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public Reader getCharacterStream(String parameterName) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public Reader getNCharacterStream(int parameterIndex) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public Reader getNCharacterStream(String parameterName) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public NClob getNClob(int parameterIndex) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public NClob getNClob(String parameterName) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public String getNString(int parameterIndex) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public String getNString(String parameterName) throws SQLException {
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

    public void setAsciiStream(String parameterName, InputStream x, long length)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setAsciiStream(String parameterName, InputStream x)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setBinaryStream(String parameterName, InputStream x, long length)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setBinaryStream(String parameterName, InputStream x)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setBlob(String parameterName, Blob x) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setBlob(String parameterName, InputStream inputStream,
            long length) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setBlob(String parameterName, InputStream inputStream)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setCharacterStream(String parameterName, Reader reader,
            long length) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setCharacterStream(String parameterName, Reader reader)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setClob(String parameterName, Clob x) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setClob(String parameterName, Reader reader, long length)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setClob(String parameterName, Reader reader)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setNCharacterStream(String parameterName, Reader value,
            long length) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setNCharacterStream(String parameterName, Reader value)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setNClob(String parameterName, NClob value) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setNClob(String parameterName, Reader reader, long length)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setNClob(String parameterName, Reader reader)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setNString(String parameterName, String value)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setRowId(String parameterName, RowId x) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setSQLXML(String parameterName, SQLXML xmlObject)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    // java.sql.PreparedStatement ----------------------------------------------

    public void setAsciiStream(int parameterIndex, InputStream x, long length)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setAsciiStream(int parameterIndex, InputStream x)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setBinaryStream(int parameterIndex, InputStream x, long length)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setBinaryStream(int parameterIndex, InputStream x)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setBlob(int parameterIndex, InputStream inputStream, long length)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setBlob(int parameterIndex, InputStream inputStream)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setCharacterStream(int parameterIndex, Reader reader,
            long length) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setCharacterStream(int parameterIndex, Reader reader)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setClob(int parameterIndex, Reader reader, long length)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setNCharacterStream(int parameterIndex, Reader value,
            long length) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setNCharacterStream(int parameterIndex, Reader value)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setNClob(int parameterIndex, Reader reader, long length)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setNString(int parameterIndex, String value)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setSQLXML(int parameterIndex, SQLXML xmlObject)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setURL(int parameterIndex, URL x) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    // java.sql.Statement ------------------------------------------------------
    
    public boolean isPoolable() throws SQLException {
        return false;
    }

    public void setPoolable(boolean poolable) throws SQLException {
        // ignore
    }
    
    // java.sql.Wrapper -.------------------------------------------------------
    
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface != null 
            && iface.isAssignableFrom(FBCallableStatement.class);
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (!isWrapperFor(iface))
            throw new FBDriverNotCapableException();
        
        return iface.cast(this);
    }

    public <T> T getObject(int parameterIndex, Class<T> type) throws SQLException {
        // TODO Write implementation
        throw new FBDriverNotCapableException();
    }

    public <T> T getObject(String parameterName, Class<T> type) throws SQLException {
        // TODO Write implementation
        throw new FBDriverNotCapableException();
    }
    
}
