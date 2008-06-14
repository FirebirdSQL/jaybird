/*
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
import java.net.URL;
import java.sql.*;

import org.firebirdsql.gds.impl.GDSHelper;

/**
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy
 *         </a>
 */
public class FBPreparedStatement extends AbstractPreparedStatement {

    public FBPreparedStatement(GDSHelper gdsHelper, String sql, int rsType,
            int rsConcurrency, int rsHoldability,
            FBObjectListener.StatementListener statementListener,
            FBObjectListener.BlobListener blobListener,
            boolean metaDataQuery, boolean standaloneStatement)
            throws SQLException {
        super(gdsHelper, sql, rsType, rsConcurrency, rsHoldability, 
            statementListener, blobListener, metaDataQuery, standaloneStatement);
    }

    /**
     * Sets the designated parameter to the given <code>java.net.URL</code>
     * value. The driver converts this to an SQL <code>DATALINK</code> value
     * when it sends it to the database.
     * 
     * @param parameterIndex
     *            the first parameter is 1, the second is 2, ...
     * @param url
     *            the <code>java.net.URL</code> object to be set
     * @exception SQLException
     *                if a database access error occurs
     * @since 1.4
     */
    public void setURL(int parameterIndex, URL url) throws SQLException {
        // TODO: implement this java.sql.PreparedStatement method
        throw new SQLException("Not yet implemented");
    }

    /**
     * Retrieves the number, types and properties of this
     * <code>PreparedStatement</code> object's parameters.
     * 
     * @return a <code>ParameterMetaData</code> object that contains
     *         information about the number, types and properties of this
     *         <code>PreparedStatement</code> object's parameters
     * @exception SQLException
     *                if a database access error occurs
     * @see ParameterMetaData
     * @since 1.4
     */
    public ParameterMetaData getParameterMetaData() throws SQLException {
        return new FBParameterMetaData(fixedStmt.getInSqlda().sqlvar, gdsHelper);
    }
    
    /**
     * jdbc 3
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public ResultSet getGeneratedKeys() throws SQLException {
        // TODO: implement this java.sql.Statement method
        throw new SQLException("not yet implemented");
    }
    
    /**
     * jdbc 3
     * @param param1 <description>
     * @param param2 <description>
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public int executeUpdate(String param1, int param2) throws SQLException {
        // TODO: implement this java.sql.Statement method
        throw new SQLException("Not yet implemented");
    }
    
    /**
     * jdbc 3
     * @param param1 <description>
     * @param param2 <description>
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public int executeUpdate(String param1, int[] param2) throws SQLException {
        // TODO: implement this java.sql.Statement method
        throw new SQLException("Not yet implemented");
    }
    
    /**
     * jdbc 3
     * @param param1 <description>
     * @param param2 <description>
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public int executeUpdate(String param1, String[] param2) throws SQLException {
        // TODO: implement this java.sql.Statement method
        throw new SQLException("Not yet implemented");
    }
    
    /**
     * jdbc 3
     * @param param1 <description>
     * @param param2 <description>
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public boolean execute(String param1, int param2) throws SQLException {
        // TODO: implement this java.sql.Statement method
        throw new SQLException("not yet implemented");
    }

    /**
     * jdbc 3
     * @param param1 <description>
     * @param param2 <description>
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public boolean execute(String param1, int[] param2) throws SQLException {
        // TODO: implement this java.sql.Statement method
        throw new SQLException("not yet implemented");
    }

    /**
     * jdbc 3
     * @param param1 <description>
     * @param param2 <description>
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public boolean execute(String param1, String[] param2) throws SQLException {
        // TODO: implement this java.sql.Statement method
        throw new SQLException("not yet implemented");
    }

    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        // TODO: implement this java.sql.PreparedStatement method
        throw new SQLException("not yet implemented");
    }

    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        // TODO: implement this java.sql.PreparedStatement method
        throw new SQLException("not yet implemented");
    }

    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        // TODO: implement this java.sql.PreparedStatement method
        throw new SQLException("not yet implemented");
    }

    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        // TODO: implement this java.sql.PreparedStatement method
        throw new SQLException("not yet implemented");
    }

    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        // TODO: implement this java.sql.PreparedStatement method
        throw new SQLException("not yet implemented");
    }

    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        // TODO: implement this java.sql.PreparedStatement method
        throw new SQLException("not yet implemented");
    }

    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        // TODO: implement this java.sql.PreparedStatement method
        throw new SQLException("not yet implemented");
    }

    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        // TODO: implement this java.sql.PreparedStatement method
        throw new SQLException("not yet implemented");
    }

    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        // TODO: implement this java.sql.PreparedStatement method
        throw new SQLException("not yet implemented");
    }

    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        // TODO: implement this java.sql.PreparedStatement method
        throw new SQLException("not yet implemented");
    }

    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        // TODO: implement this java.sql.PreparedStatement method
        throw new SQLException("not yet implemented");
    }

    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        // TODO: implement this java.sql.PreparedStatement method
        throw new SQLException("not yet implemented");
    }

    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        // TODO: implement this java.sql.PreparedStatement method
        throw new SQLException("not yet implemented");
    }

    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        // TODO: implement this java.sql.PreparedStatement method
        throw new SQLException("not yet implemented");
    }

    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        // TODO: implement this java.sql.PreparedStatement method
        throw new SQLException("not yet implemented");
    }

    public void setNString(int parameterIndex, String value) throws SQLException {
        // TODO: implement this java.sql.PreparedStatement method
        throw new SQLException("not yet implemented");
    }

    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        // TODO: implement this java.sql.PreparedStatement method
        throw new SQLException("not yet implemented");
    }

    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        // TODO: implement this java.sql.PreparedStatement method
        throw new SQLException("not yet implemented");
    }

    public boolean isPoolable() throws SQLException {
        // TODO: implement this java.sql.PreparedStatement method
        throw new SQLException("not yet implemented");
    }

    public void setPoolable(boolean poolable) throws SQLException {
        // TODO: implement this java.sql.PreparedStatement method
        throw new SQLException("not yet implemented");
    }

    public boolean isWrapperFor(Class arg0) throws SQLException {
        // TODO: implement this java.sql.PreparedStatement method
        throw new SQLException("not yet implemented");
    }

    public Object unwrap(Class arg0) throws SQLException {
        // TODO: implement this java.sql.PreparedStatement method
        throw new SQLException("not yet implemented");
    }

    
}
