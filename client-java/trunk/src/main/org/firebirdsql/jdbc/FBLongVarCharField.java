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

import org.firebirdsql.gds.XSQLVAR;

import java.sql.*;
import java.io.*;

/**
 * This is Blob-based implementation of {@link FBStringField}. It should be used
 * for fields declared in database as <code>BLOB SUB_TYPE 1</code>. This 
 * implementation provides all conversion routines {@link FBStringField} has.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
public class FBLongVarCharField extends FBStringField implements FBFlushableField{

    private static final int BUFF_SIZE = 4096;
    
    private boolean isCachedData = false;

    FBLongVarCharField(XSQLVAR field, FBResultSet rs, int numCol) throws SQLException {
        super(field, rs, numCol);
    }
    
    Blob getBlob() throws SQLException {
        return getBlob(false);
    }

    Blob getBlob(boolean create) throws SQLException {
        if (rs.row[numCol]==null)
            return BLOB_NULL_VALUE;

        Long blobId = new Long(XSQLVAR.decodeLong(rs.row[numCol]));

        if (blobId == null)
            blobId = new Long(0);

        return new FBBlob(c, blobId.longValue());
    }
    
    InputStream getBinaryStream() throws SQLException {
        Blob blob = getBlob();

        if (blob == BLOB_NULL_VALUE)
            return STREAM_NULL_VALUE;

        return blob.getBinaryStream();
    }
    
    byte[] getBytes() throws SQLException {

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
        return getString();
    }
    
    public byte[] getCachedObject() throws SQLException {
        if (rs.row[numCol]==null) 
            return BYTES_NULL_VALUE;

          return getBytes();
    }

    String getString() throws SQLException {
        byte[] data = getBytes();
        
        if (data == BYTES_NULL_VALUE)
            return STRING_NULL_VALUE;
        
        return field.decodeString(data, javaEncoding);
    }

    void setString(String value) throws SQLException {
        byte[] data = field.encodeString(value, javaEncoding);
        setBinaryStream(new ByteArrayInputStream(data), data.length);
    }

    void setBytes(byte[] value) throws SQLException {
        byte[] data = field.encodeString(value, javaEncoding);
        setBinaryStream(new ByteArrayInputStream(data), data.length);
    }

    private void copyBinaryStream(InputStream in, int length) throws SQLException {
        FBBlob blob =  new FBBlob(c, 0);
        blob.copyStream(in, length);
        field.sqldata = XSQLVAR.encodeLong(blob.getBlobId());
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

    public void flushCachedData() throws SQLException {
        if (isCachedData){
            copyBinaryStream(
                new ByteArrayInputStream((byte[])field.sqldata), field.sqllen);
            isCachedData=false;
        }
    }

}