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

package org.firebirdsql.jdbc.field;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;

import org.firebirdsql.gds.XSQLVAR;
import org.firebirdsql.jdbc.*;


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
    
    private FBBlob blob;

    // Rather then hold cached data in the XSQLDAVar we will hold it in here.
    int length;
    byte[] data;

    FBLongVarCharField(XSQLVAR field, FieldDataProvider dataProvider, int requiredType) 
        throws SQLException 
    {
        super(field, dataProvider, requiredType);
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
    
    public Blob getBlob() throws SQLException {
        
        if (blob != null)
            return blob;
        
        if (getFieldData()==null)
            return BLOB_NULL_VALUE;

        blob = new FBBlob(gdsHelper, field.decodeLong(getFieldData()));
        return blob;
    }
    
    public InputStream getBinaryStream() throws SQLException {
        Blob blob = getBlob();

        if (blob == BLOB_NULL_VALUE)
            return STREAM_NULL_VALUE;

        return blob.getBinaryStream();
    }
    
    public byte[] getBytes() throws SQLException {

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
                throw new FBSQLException(ioex);
            }

            try {
                bout.close();
            } catch(IOException ioex) {
                throw new FBSQLException(ioex);
            }
        }

        return bout.toByteArray();
    }

    public byte[] getCachedObject() throws SQLException {
        if (getFieldData()==null) 
            return BYTES_NULL_VALUE;

          return getBytes();
    }

    public String getString() throws SQLException {
        byte[] data = getBytes();
        
        if (data == BYTES_NULL_VALUE)
            return STRING_NULL_VALUE;
        
        return field.decodeString(data, javaEncoding, mappingPath);
    }

    public void setBlob(FBBlob blob) throws SQLException {
        setFieldData(field.encodeLong(blob.getBlobId()));
    }
    
    public void setString(String value) throws SQLException {
        
        if (value == STRING_NULL_VALUE) {
            setNull();
            return;
        }
        
        byte[] data = field.encodeString(value, javaEncoding, mappingPath);
        setBinaryStream(new ByteArrayInputStream(data), data.length);
    }

    public void setBytes(byte[] value) throws SQLException {

        if (value == BYTES_NULL_VALUE) {
            setNull();
            return;
        }

        byte[] data = field.encodeString(value, javaEncoding, mappingPath);
        setBinaryStream(new ByteArrayInputStream(data), data.length);
    }

    private void copyBinaryStream(InputStream in, int length) throws SQLException {
        FBBlob blob =  new FBBlob(gdsHelper);
        blob.copyStream(in, length);
        setFieldData(field.encodeLong(blob.getBlobId()));
    }

    public void setBinaryStream(InputStream in, int length) throws SQLException {
        
        if (in == STREAM_NULL_VALUE) {
            setNull();
            return;
        }
        
//        if (!gdsHelper.getAutoCommit()) {
            copyBinaryStream(in, length);
//        } else {
//            byte[] buff = new byte[BUFF_SIZE];
//            ByteArrayOutputStream bout = new ByteArrayOutputStream(length);
//
//            int chunk;
//            try {
//                while (length >0) {
//                    chunk =in.read(buff, 0, ((length<BUFF_SIZE) ? length:BUFF_SIZE));
//                    bout.write(buff, 0, chunk);
//                    length -= chunk;
//                }
//                bout.close();
//            }
//            catch (IOException ioe) {
//                throw new FBSQLException(ioe);
//            }
//
//            this.data = bout.toByteArray();
//            this.length = data.length;
//            isCachedData = true;
//        }
    }

    public void flushCachedData() throws SQLException {
        if (isCachedData){
            copyBinaryStream(
                new ByteArrayInputStream(data), length);
            isCachedData=false;
        }
    }

}