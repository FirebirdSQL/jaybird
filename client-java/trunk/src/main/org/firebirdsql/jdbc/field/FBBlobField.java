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
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.sql.Blob;

import org.firebirdsql.gds.XSQLVAR;
import org.firebirdsql.jdbc.*;

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

	// Rather then hold cached data in the XSQLDAVar we will hold it in here.
	int length;
	byte[] data;

    FBBlobField(XSQLVAR field, FieldDataProvider dataProvider, int requiredType) 
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

        final byte[] bytes = getFieldData();

        if (bytes == null)
            return BLOB_NULL_VALUE;

        /*@todo convert this into a method of FirebirdConnection */
        blob = new FBBlob(c, field.decodeLong( bytes ));

        return blob;
    }

    public InputStream getAsciiStream() throws SQLException {
        return getBinaryStream();
    }

    public InputStream getBinaryStream() throws SQLException {
        // getBinaryStream() is not defined for BLOB types, only for BINARY
        if (field.sqlsubtype < 0)
            throw (SQLException)createException(
                BINARY_STREAM_CONVERSION_ERROR).fillInStackTrace();
        
        Blob blob = getBlob();

        if (blob == BLOB_NULL_VALUE)
            return STREAM_NULL_VALUE;

        return blob.getBinaryStream();
    }

    public byte[] getBytes() throws SQLException {
        // getBytes() is not defined for BLOB types, only for BINARY
        if (field.sqlsubtype < 0)
            throw (SQLException)createException(
                BYTES_CONVERSION_ERROR);

        return getBytesInternal();
    }
    
    public byte[] getBytesInternal() throws SQLException {
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

    /*
    public Object getObject() throws SQLException {
        if (field.sqlsubtype < 0)
            return getBlob();
        else if (field.sqlsubtype == 1)
            return getString();
        else
            return getBytes();
    }
    */

    public byte[] getCachedObject() throws SQLException {
        if (getFieldData()==null) 
            return BYTES_NULL_VALUE;

		  return getBytesInternal();
    }

    public String getString() throws SQLException {
        // getString() is not defined for BLOB fields, only for BINARY
        if (field.sqlsubtype < 0)
            throw (SQLException)createException(
                STRING_CONVERSION_ERROR).fillInStackTrace();

        Blob blob = getBlob();

        if (blob == BLOB_NULL_VALUE)
            return STRING_NULL_VALUE;

        return field.decodeString(getBytes(), javaEncoding, mappingPath);
    }

    public InputStream getUnicodeStream() throws SQLException {
        return getBinaryStream();
    }

    //--- setXXX methods

    public void setAsciiStream(InputStream in, int length) throws SQLException {
        setBinaryStream(in, length);
    }

    public void setCharacterStream(Reader in, int length) throws SQLException {
        
        if (in == READER_NULL_VALUE) {
            setNull();
            return;
        }
        
        if (!c.getAutoCommit()) {
            copyCharacterStream(in, length, javaEncoding);
        } else {
            char[] buff = new char[BUFF_SIZE];
            ByteArrayOutputStream bout = new ByteArrayOutputStream(length);
            try {
            OutputStreamWriter boutw = new OutputStreamWriter(bout, javaEncoding);

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
                throw new FBSQLException(ioe);
            }
            } catch(UnsupportedEncodingException ex) {
                throw new FBSQLException("Cannot set character stream because " +
                        "the unsupported encoding is detected in the JVM: " +
                        javaEncoding + ". Please report this to the driver developers."
                    );
            }
            
            this.data = bout.toByteArray();
            this.length = ((byte[])this.data).length;
            isCachedData = true;
        }
    }
    
    public void setBinaryStream(InputStream in, int length) throws SQLException {
        
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
                throw new FBSQLException(ioe);
            }
            
            this.data = bout.toByteArray();
            this.length = ((byte[])this.data).length;
            isCachedData = true;
        }
    }
    
    public void flushCachedData() throws SQLException {
        if (isCachedData){
            copyBinaryStream(
                new ByteArrayInputStream((byte[])this.data), this.length);
            isCachedData=false;
        }
    }
    
    private void copyBinaryStream(InputStream in, int length) throws SQLException {
        
        /** @todo check if this is correct!!! */
        if (!c.getAutoCommit())
            c.ensureInTransaction();
        
        FBBlob blob =  new FBBlob(c);
        blob.copyStream(in, length);
        setFieldData(field.encodeLong(blob.getBlobId()));
    }

    private void copyCharacterStream(Reader in, int length, String encoding) throws SQLException {
        
        /** @todo check if this is correct!!! */
        if (!c.getAutoCommit())
            c.ensureInTransaction();
        
        FBBlob blob =  new FBBlob(c);
        blob.copyCharacterStream(in, length, encoding);
        setFieldData(field.encodeLong(blob.getBlobId()));
    }
    
    public void setBytes(byte[] value) throws SQLException {
        
        if (value == BYTES_NULL_VALUE) {
            setNull();
            return;
        }
        
        setBinaryStream(new ByteArrayInputStream(value), value.length);
    }

    public void setString(String value) throws SQLException {
        if (value == STRING_NULL_VALUE) {
            setNull();
            return;
        }
        
        setBytes(field.encodeString(value,javaEncoding, mappingPath));
    }

    public void setUnicodeStream(InputStream in, int length) throws SQLException {
        setBinaryStream(in, length);
    }

    public void setBlob(FBBlob blob) throws SQLException {
        setFieldData(field.encodeLong(blob.getBlobId()));
    }
}
