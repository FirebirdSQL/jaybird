package org.firebirdsql.jdbc;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;

import org.firebirdsql.gds.XSQLVAR;
import org.firebirdsql.gds.impl.AbstractIscStmtHandle;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.jdbc.FBObjectListener.ResultSetListener;


public class FBResultSet extends AbstractResultSet {

    public FBResultSet(GDSHelper gdsHelper, AbstractStatement fbStatement,
            AbstractIscStmtHandle stmt, ResultSetListener listener,
            boolean trimStrings, int rsType, int rsConcurrency,
            int rsHoldability, boolean cached) throws SQLException {
        super(gdsHelper, fbStatement, stmt, listener, trimStrings, rsType,
                rsConcurrency, rsHoldability, cached);
        // TODO Auto-generated constructor stub
    }
    public FBResultSet(XSQLVAR[] xsqlvars, ArrayList rows) throws SQLException {
        super(xsqlvars, rows);
        // TODO Auto-generated constructor stub
    }

    //------------------------- JDBC 4.0 --------------------------------------
    
    public int getHoldability() throws SQLException {
        return super.getHoldability();
    }
    public Reader getNCharacterStream(int columnIndex) throws SQLException {
        return getCharacterStream(columnIndex);
    }
    public Reader getNCharacterStream(String columnLabel) throws SQLException {
        return getCharacterStream(findColumn(columnLabel));
    }
    public NClob getNClob(int columnIndex) throws SQLException {
        return (NClob)getClob(columnIndex);
    }
    public NClob getNClob(String columnLabel) throws SQLException {
        return getNClob(findColumn(columnLabel));
    }
    public String getNString(int columnIndex) throws SQLException {
        return getString(columnIndex);
    }
    public String getNString(String columnLabel) throws SQLException {
        return getNString(findColumn(columnLabel));
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
    public boolean isClosed() throws SQLException {
        return super.isClosed();
    }
    
    public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
        // TODO fix the length
        updateAsciiStream(columnIndex, x);
    }
    public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
        try {
            updateCharacterStream(columnIndex, new InputStreamReader(x, "ASCII"));
        } catch(UnsupportedEncodingException ex) {
            throw new FBSQLException(ex);
        }
    }
    public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
        updateAsciiStream(findColumn(columnLabel), x);
    }
    public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
        updateAsciiStream(findColumn(columnLabel), x);
    }
    
    public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
        throw new FBDriverNotCapableException();
    }
    public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
        throw new FBDriverNotCapableException();
    }
    public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
        throw new FBDriverNotCapableException();
    }
    public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
        throw new FBDriverNotCapableException();
    }
    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
        throw new FBDriverNotCapableException();
    }
    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
        throw new FBDriverNotCapableException();
    }
    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
        throw new FBDriverNotCapableException();
    }
    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
        throw new FBDriverNotCapableException();
    }
    public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        throw new FBDriverNotCapableException();
    }
    public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
        throw new FBDriverNotCapableException();
    }
    public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        throw new FBDriverNotCapableException();
    }
    public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
        throw new FBDriverNotCapableException();
    }
    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
        throw new FBDriverNotCapableException();
    }
    public void updateClob(int columnIndex, Reader reader) throws SQLException {
        throw new FBDriverNotCapableException();
    }
    public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
        throw new FBDriverNotCapableException();
    }
    public void updateClob(String columnLabel, Reader reader) throws SQLException {
        throw new FBDriverNotCapableException();
    }
    public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        throw new FBDriverNotCapableException();
    }
    public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
        throw new FBDriverNotCapableException();
    }
    public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        throw new FBDriverNotCapableException();
    }
    public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
        throw new FBDriverNotCapableException();
    }
    public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
        throw new FBDriverNotCapableException();
    }
    public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
        throw new FBDriverNotCapableException();
    }
    public void updateNClob(int columnIndex, Reader reader) throws SQLException {
        throw new FBDriverNotCapableException();
    }
    public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
        throw new FBDriverNotCapableException();
    }
    public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
        throw new FBDriverNotCapableException();
    }
    public void updateNClob(String columnLabel, Reader reader) throws SQLException {
        throw new FBDriverNotCapableException();
    }
    public void updateNString(int columnIndex, String nString) throws SQLException {
        throw new FBDriverNotCapableException();
    }
    public void updateNString(String columnLabel, String nString) throws SQLException {
        throw new FBDriverNotCapableException();
    }
    public void updateRowId(int columnIndex, RowId x) throws SQLException {
        throw new FBDriverNotCapableException();
    }
    public void updateRowId(String columnLabel, RowId x) throws SQLException {
        throw new FBDriverNotCapableException();
    }
    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
        throw new FBDriverNotCapableException();
    }
    public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
        throw new FBDriverNotCapableException();
    }
    public boolean isWrapperFor(Class arg0) throws SQLException {
        return arg0 != null && arg0.isAssignableFrom(FBDatabaseMetaData.class);
    }
    public Object unwrap(Class arg0) throws SQLException {
        if (!isWrapperFor(arg0))
            throw new FBSQLException("No compatible class found.");
        
        return this;
    }
    

}
