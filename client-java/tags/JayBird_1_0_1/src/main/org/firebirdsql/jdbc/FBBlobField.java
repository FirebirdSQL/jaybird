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
public class FBBlobField extends FBField implements FBFlushableField {
    private static final int BUFF_SIZE = 4096;

    private boolean isCachedData = false;
    
    private FBBlob blob;

    FBBlobField(XSQLVAR field, FBResultSet rs, int numCol) throws SQLException {
        super(field, rs, numCol);
    }
    
    public void close() throws SQLException {
        try {
            if (blob != null) 
                blob.close();
        } catch(IOException ioex) {
            throw new FBSQLException(ioex);
        } finally {       
            // forget this blob instance, resource waste
            // but simplifies our life. BLOB handle will be
            // released by a server automatically later
            
            blob = null;
        }
    }

    Blob getBlob() throws SQLException {
        
        if (blob != null)
            return blob;
    
    /*
    // This code was commented by R.Rokytskyy
    // since getBlob(boolean) was used only in getBlob() method.
    // and it makes a little sense to keep two methods.
    
        return getBlob(false);
    }

    Blob getBlob(boolean create) throws SQLException {
    */
        if (rs.row[numCol]==null)
            return BLOB_NULL_VALUE;

        Long blobId = new Long(XSQLVAR.decodeLong(rs.row[numCol]));

        
        // Commented by R.Rokytskyy, this is dead code, remove before release
        if (blobId == null)
            blobId = new Long(0);
        

        blob = new FBBlob(c, blobId.longValue());
        
        return blob;
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
        else if (field.sqlsubtype == 1)
            return getString();
        else
            return getBytes();
    }

    public byte[] getCachedObject() throws SQLException {
        if (rs.row[numCol]==null) 
            return BYTES_NULL_VALUE;

		  return getBytesInternal();
    }

    String getString() throws SQLException {
        // getString() is not defined for BLOB fields, only for BINARY
        if (field.sqlsubtype < 0)
            throw (SQLException)createException(
                STRING_CONVERSION_ERROR).fillInStackTrace();

        Blob blob = getBlob();

        if (blob == BLOB_NULL_VALUE)
            return STRING_NULL_VALUE;

        return field.decodeString(getBytes(), javaEncoding);
    }

    InputStream getUnicodeStream() throws SQLException {
        return getBinaryStream();
    }

    //--- setXXX methods

    void setAsciiStream(InputStream in, int length) throws SQLException {
        setBinaryStream(in, length);
    }

    void setCharacterStream(Reader in, int length) throws SQLException {
        
        if (in == READER_NULL_VALUE) {
            setNull();
            return;
        }
        
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
        
        if (in == STREAM_NULL_VALUE) {
            setNull();
            return;
        }
        
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
    
    public void flushCachedData() throws SQLException {
        if (isCachedData){
            copyBinaryStream(
                new ByteArrayInputStream((byte[])field.sqldata), field.sqllen);
            isCachedData=false;
        }
    }
    
    private void copyBinaryStream(InputStream in, int length) throws SQLException {
        
        /** @todo check if this is correct!!! */
        if (!c.getAutoCommit())
            c.ensureInTransaction();
        
        FBBlob blob =  new FBBlob(c);
        blob.copyStream(in, length);
        field.sqldata = XSQLVAR.encodeLong(blob.getBlobId());
    }

    private void copyCharacterStream(Reader in, int length) throws SQLException {
        
        /** @todo check if this is correct!!! */
        if (!c.getAutoCommit())
            c.ensureInTransaction();
        
        FBBlob blob =  new FBBlob(c);
        blob.copyCharacterStream(in, length);
        field.sqldata = XSQLVAR.encodeLong(blob.getBlobId());
    }
    
    void setBytes(byte[] value) throws SQLException {
        
        if (value == BYTES_NULL_VALUE) {
            setNull();
            return;
        }
        
        setBinaryStream(new ByteArrayInputStream(value), value.length);
    }

    void setString(String value) throws SQLException {
        if (value == STRING_NULL_VALUE) {
            setNull();
            return;
        }
        
        setBytes(field.encodeString(value,javaEncoding));
    }

    void setUnicodeStream(InputStream in, int length) throws SQLException {
        setBinaryStream(in, length);
    }

    void setBlob(FBBlob blob) throws SQLException {
        field.sqldata = XSQLVAR.encodeLong(blob.getBlobId());
    }
}
