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

import java.io.*;

import java.sql.SQLException;
import java.sql.Blob;

import org.firebirdsql.gds.XSQLVAR;

/**
 * Describe class <code>FBBlobField</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
public class FBBlobField extends FBField {
    private static final int BUFF_SIZE = 4096;

    FBConnection c;
    boolean isCachedData = false;

    FBBlobField(XSQLVAR field, FBResultSet rs, int numCol) throws SQLException {
        super(field, rs, numCol);
    }

    void setConnection(FBConnection c) {
        this.c = c;
    }
    
    String getIscEncoding() {
        return c.getIscEncoding();
    }

    Blob getBlob() throws SQLException {
        return getBlob(false);
    }

    Blob getBlob(boolean create) throws SQLException {
        if (rs.row[numCol]==null)
            return BLOB_NULL_VALUE;

//        if (rs.row[numCol] instanceof Blob)
//            return (Blob)rs.row[numCol];

        Long blobId = (Long)rs.row[numCol];

        if (blobId == null)
            blobId = new Long(0);

        return new FBBlob(c, blobId.longValue());
    }

    InputStream getAsciiStream() throws SQLException {
        return getBinaryStream();
    }

    InputStream getBinaryStream() throws SQLException {
        // getBinaryStream() is not defined for BLOB types, only for BINARY
        if (field.sqlsubtype < 0)
            throw (SQLException)createException(
                BINARY_STREAM_CONVERSION_ERROR).fillInStackTrace();
        
        Blob blob = getBlob();

        if (blob == BLOB_NULL_VALUE)
            return STREAM_NULL_VALUE;

        return blob.getBinaryStream();
    }

    byte[] getBytes() throws SQLException {
        // getBytes() is not defined for BLOB types, only for BINARY
        if (field.sqlsubtype < 0)
            throw (SQLException)createException(
                BYTES_CONVERSION_ERROR);

        return getBytesInternal();
    }
    
    byte[] getBytesInternal() throws SQLException {
        Blob blob = getBlob();

        if (blob == BLOB_NULL_VALUE)
            return BYTES_NULL_VALUE;

        InputStream in = blob.getBinaryStream();

        if (in == STREAM_NULL_VALUE)
            return BYTES_NULL_VALUE;

        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        // copy stream data
        byte[] buff = new byte[BUFF_SIZE];
        int counter = 0;
        try {
            while((counter = in.read(buff)) != -1) {
                bout.write(buff, 0, counter);
            }
        } catch(IOException ioex) {
            throw (SQLException)createException(
                BYTES_CONVERSION_ERROR + " " + ioex.getMessage());
        } finally {
            try {
                in.close();
            } catch(IOException ioex) {
                throw new SQLException("Unable to close BLOB input stream.");
            }

            try {
                bout.close();
            } catch(IOException ioex) {
                throw new SQLException("Unable to close ByteArrayOutputStream.");
            }
        }

        return bout.toByteArray();
    }

    Object getObject() throws SQLException {
        if (field.sqlsubtype < 0)
            return getBlob();
        else
            return getBytes();
    }

    Object getCachedObject() throws SQLException {
        if (rs.row[numCol]==null)
            return BLOB_NULL_VALUE;

        return new FBCachedBlob(getBytesInternal());
    }

    String getString() throws SQLException {
        // getString() is not defined for BLOB fields, only for BINARY
        if (field.sqlsubtype < 0)
            throw (SQLException)createException(
                STRING_CONVERSION_ERROR).fillInStackTrace();

        Blob blob = getBlob();

        if (blob == BLOB_NULL_VALUE)
            return STRING_NULL_VALUE;

        return toString(getBytes(), getIscEncoding());
    }

    InputStream getUnicodeStream() throws SQLException {
        return getBinaryStream();
    }

    //--- setXXX methods

    void setAsciiStream(InputStream in, int length) throws SQLException {
        setBinaryStream(in, length);
    }

    void setCharacterStream(Reader in, int length) throws SQLException {
        if (!c.getAutoCommit()) {
            copyCharacterStream(in, length);
        } else {
            char[] buff = new char[BUFF_SIZE];
            ByteArrayOutputStream bout = new ByteArrayOutputStream(length);
            OutputStreamWriter boutw = new OutputStreamWriter(bout);

            int chunk;
            try {
                while (length >0) {
                    chunk =in.read(buff, 0, ((length<BUFF_SIZE) ? length:BUFF_SIZE));
                    boutw.write(buff, 0, chunk);
                    length -= chunk;
                }
                boutw.close();
                bout.close();
            }
            catch (IOException ioe) {
                throw new SQLException("read/write blob problem: " + ioe);
            }
            
            field.sqldata = bout.toByteArray();
            field.sqllen = ((byte[])field.sqldata).length;
            isCachedData = true;
        }
    }
    
    void setBinaryStream(InputStream in, int length) throws SQLException {
        if (!c.getAutoCommit()) {
            copyBinaryStream(in, length);
        } else {
            byte[] buff = new byte[BUFF_SIZE];
            ByteArrayOutputStream bout = new ByteArrayOutputStream(length);
            
            int chunk;
            try {
                while (length >0) {
                    chunk =in.read(buff, 0, ((length<BUFF_SIZE) ? length:BUFF_SIZE));
                    bout.write(buff, 0, chunk);
                    length -= chunk;
                }
                bout.close();
            }
            catch (IOException ioe) {
                throw new SQLException("read/write blob problem: " + ioe);
            }
            
            field.sqldata = bout.toByteArray();
            field.sqllen = ((byte[])field.sqldata).length;
            isCachedData = true;
        }
    }
    
    void flushCachedData() throws SQLException {
//        if (field.sqldata instanceof byte[]) {
        if (isCachedData){
            copyBinaryStream(
                new ByteArrayInputStream((byte[])field.sqldata), field.sqllen);
            isCachedData=false;
        }
//        }
    }
    
    private void copyBinaryStream(InputStream in, int length) throws SQLException {
        FBBlob blob =  new FBBlob(c, 0);
        blob.copyStream(in, length);
        field.sqldata = new Long(blob.getBlobId());
    }

    private void copyCharacterStream(Reader in, int length) throws SQLException {
        FBBlob blob =  new FBBlob(c, 0);
        blob.copyCharacterStream(in, length);
        field.sqldata = new Long(blob.getBlobId());
    }
    
    void setBytes(byte[] value) throws SQLException {
        setBinaryStream(new ByteArrayInputStream(value), value.length);
    }
    void setString(String value) throws SQLException {
        setBytes(getBytes(value, getIscEncoding()));
    }

    void setUnicodeStream(InputStream in, int length) throws SQLException {
        setBinaryStream(in, length);
    }

    void setBlob(FBBlob blob) throws SQLException {
        field.sqldata = new Long(blob.getBlobId());
    }
}
