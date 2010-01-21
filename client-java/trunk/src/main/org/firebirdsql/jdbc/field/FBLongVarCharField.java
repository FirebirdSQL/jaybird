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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;

import org.firebirdsql.gds.XSQLVAR;
import org.firebirdsql.jdbc.*;
import org.firebirdsql.jdbc.field.FBFlushableField.CachedObject;


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
    
    private FBBlob blob;

    // Rather then hold cached data in the XSQLDAVar we will hold it in here.
    private int length;
    private byte[] bytes;
    private InputStream binaryStream;
    private Reader characterStream;

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
            this.bytes = null;
            this.binaryStream = null;
            this.characterStream = null;
            this.length = 0;
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
    
    public Clob getClob() throws SQLException {
    	FBBlob blob = (FBBlob) getBlob();
    	
    	if (blob == BLOB_NULL_VALUE){
    		return CLOB_NULL_VALUE;
    	}
    	
    	return new FBClob(blob);
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

    public byte[] getCachedData() throws SQLException {
        if (getFieldData() == null) {
            
            if (bytes != null)
                return bytes;
            else
                return BYTES_NULL_VALUE;
        }

          return getBytes();
    }
    
    public FBFlushableField.CachedObject getCachedObject() throws SQLException {
        if (getFieldData() == null) 
            return new FBFlushableField.CachedObject(bytes, binaryStream, characterStream, length);
        
        return new CachedObject(getBytes(), null, null, 0);
    }
    
    public void setCachedObject(FBFlushableField.CachedObject cachedObject) throws SQLException {
        this.bytes = cachedObject.bytes;
        this.binaryStream = cachedObject.binaryStream;
        this.characterStream = cachedObject.characterStream;
        this.length = cachedObject.length;
    }

    public String getString() throws SQLException {
        byte[] data = getBytes();
        
        if (data == BYTES_NULL_VALUE)
            return STRING_NULL_VALUE;
        
        return field.decodeString(data, javaEncoding, mappingPath);
    }

    public void setBlob(FBBlob blob) throws SQLException {
        setFieldData(field.encodeLong(blob.getBlobId()));
        this.blob = blob;
    }
    
    public void setClob(FBClob clob) throws SQLException {
    	FBBlob blob = clob.getWrappedBlob();
    	setBlob(blob);
    }

    public void setCharacterStream(Reader in, int length) throws SQLException {
        if (in == READER_NULL_VALUE) {
            setNull();
            return;
        }
        
        this.binaryStream = null;
        this.characterStream = in;
        this.bytes = null;
        this.length = length;
    }

    public void setString(String value) throws SQLException {
        
        if (value == STRING_NULL_VALUE) {
            setNull();
            return;
        }
        
        setBytes(field.encodeString(value,javaEncoding, mappingPath));
    }

    public void setBytes(byte[] value) throws SQLException {

        if (value == BYTES_NULL_VALUE) {
            setNull();
            return;
        }
        
        this.binaryStream = null;
        this.characterStream = null;
        this.bytes = value;
        this.length = value.length;
    }

    public void setBinaryStream(InputStream in, int length) throws SQLException {
        
        if (in == STREAM_NULL_VALUE) {
            setNull();
            return;
        }
            
        this.binaryStream = in;
        this.characterStream = null;
        this.bytes = null;
        this.length = length;
    }

    public void flushCachedData() throws SQLException {
        if (binaryStream != null)
            copyBinaryStream(this.binaryStream, this.length);
        else
        if (characterStream != null)
            copyCharacterStream(characterStream, length, javaEncoding);
        else
        if (bytes != null)
            copyBytes(bytes, length);
        else
        if (blob == null)
            setNull();
        
        this.characterStream = null;
        this.binaryStream = null;
        this.bytes = null;
        this.length = 0;
    }
    
    private void copyBinaryStream(InputStream in, int length) throws SQLException {
        
        FBBlob blob =  new FBBlob(gdsHelper);
        blob.copyStream(in, length);
        setFieldData(field.encodeLong(blob.getBlobId()));
    }

    private void copyCharacterStream(Reader in, int length, String encoding) throws SQLException {
        FBBlob blob =  new FBBlob(gdsHelper);
        blob.copyCharacterStream(in, length, encoding);
        setFieldData(field.encodeLong(blob.getBlobId()));
    }
    
    private void copyBytes(byte[] bytes, int length) throws SQLException {
        FBBlob blob = new FBBlob(gdsHelper);
        blob.copyBytes(bytes, 0, length);
        setFieldData(field.encodeLong(blob.getBlobId()));
    }

}