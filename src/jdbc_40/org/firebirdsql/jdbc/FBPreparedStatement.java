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
import org.firebirdsql.jdbc.FBObjectListener.BlobListener;
import org.firebirdsql.jdbc.FBObjectListener.StatementListener;

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
            boolean metaDataQuery, boolean standaloneStatement, boolean generatedKeys)
            throws SQLException {
        super(gdsHelper, sql, rsType, rsConcurrency, rsHoldability, 
            statementListener, blobListener, metaDataQuery, standaloneStatement, generatedKeys);
    }
    
    public FBPreparedStatement(GDSHelper c, int rsType, int rsConcurrency,
            int rsHoldability, StatementListener statementListener,
            BlobListener blobListener) throws SQLException {
        super(c, rsType, rsConcurrency, rsHoldability, statementListener, blobListener);
    }
    
    
    //--------------------------------------------------------------------------
    // JDBC 3.0
    //--------------------------------------------------------------------------


    public ParameterMetaData getParameterMetaData() throws SQLException {
        return new JDBC40ParameterMetaData(fixedStmt.getInSqlda().sqlvar, gdsHelper);
    }
    
    public void setURL(int parameterIndex, URL url) throws SQLException {
        throw new FBDriverNotCapableException();
    }
    
//    public ResultSet getGeneratedKeys() throws SQLException {
//        throw new FBDriverNotCapableException();
//    }
//    
//    public int executeUpdate(String param1, int param2) throws SQLException {
//        throw new FBDriverNotCapableException();
//    }
//    
//    public int executeUpdate(String param1, int[] param2) throws SQLException {
//        throw new FBDriverNotCapableException();
//    }
//    
//    public int executeUpdate(String param1, String[] param2) throws SQLException {
//        throw new FBDriverNotCapableException();
//    }
//    
//    public boolean execute(String param1, int param2) throws SQLException {
//        throw new FBDriverNotCapableException();
//    }
//
//    public boolean execute(String param1, int[] param2) throws SQLException {
//        throw new FBDriverNotCapableException();
//    }
//
//    public boolean execute(String param1, String[] param2) throws SQLException {
//        throw new FBDriverNotCapableException();
//    }

    //--------------------------------------------------------------------------
    // JDBC 3.0
    //--------------------------------------------------------------------------

    // java.sql.Statement ------------------------------------------------------
    
    public boolean isPoolable() throws SQLException {
        return false;
    }

    public void setPoolable(boolean poolable) throws SQLException {
        // ignore
    }

    // java.sql.Statement ------------------------------------------------------
    
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

    // java.sql.Wrapper -.------------------------------------------------------
    
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface != null && iface.isAssignableFrom(FBPreparedStatement.class);
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (!isWrapperFor(iface))
            throw new FBDriverNotCapableException();
        
        return (T)this;
    }
}
