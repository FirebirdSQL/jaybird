/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.jdbc.field;

import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.jdbc.FBBlob;
import org.firebirdsql.jdbc.FBClob;
import org.firebirdsql.jdbc.FBSQLException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;

/**
 * This is Blob-based implementation of {@link FBStringField}. It should be used
 * for fields declared in database as <code>BLOB SUB_TYPE 1</code>. This 
 * implementation provides all conversion routines {@link FBStringField} has.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class FBLongVarCharField extends FBStringField implements FBFlushableField{

    private static final int BUFF_SIZE = 4096;
    
    private FBBlob blob;

    // Rather then hold cached data in the XSQLDAVar we will hold it in here.
    private int length;
    private byte[] bytes;
    private InputStream binaryStream;
    private Reader characterStream;

    FBLongVarCharField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider, int requiredType)
            throws SQLException {
        super(fieldDescriptor, dataProvider, requiredType);
    }
    
    public void close() throws SQLException {
        try {
            if (blob != null) 
                blob.free();
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
        
        if (isNull())
            return null;

        blob = new FBBlob(gdsHelper, getDatatypeCoder().decodeLong(getFieldData()));
        return blob;
    }
    
    public Clob getClob() throws SQLException {
    	FBBlob blob = (FBBlob) getBlob();
    	
    	if (blob == null){
    		return null;
    	}
    	
    	return new FBClob(blob);
    }
    
    public InputStream getBinaryStream() throws SQLException {
        Blob blob = getBlob();

        if (blob == null)
            return null;

        return blob.getBinaryStream();
    }
    
    public byte[] getBytes() throws SQLException {

        Blob blob = getBlob();

        if (blob == null)
            return null;

        InputStream in = blob.getBinaryStream();

        if (in == null)
            return null;

        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        // copy stream data
        byte[] buff = new byte[BUFF_SIZE];
        int counter;
        try {
            while((counter = in.read(buff)) != -1) {
                bout.write(buff, 0, counter);
            }
        } catch(IOException ioex) {
            throw new TypeConversionException(BYTES_CONVERSION_ERROR + " " + ioex.getMessage());
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
        if (isNull()) {
            return bytes;
        }

        return getBytes();
    }
    
    public FBFlushableField.CachedObject getCachedObject() throws SQLException {
        if (isNull())
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
        
        if (data == null)
            return null;
        
        return getDatatypeCoder().decodeString(data, encodingDefinition.getEncoding(), mappingPath);
    }

    public void setBlob(FBBlob blob) throws SQLException {
        setFieldData(getDatatypeCoder().encodeLong(blob.getBlobId()));
        this.blob = blob;
    }
    
    public void setClob(FBClob clob) throws SQLException {
    	FBBlob blob = clob.getWrappedBlob();
    	setBlob(blob);
    }

    public void setCharacterStream(Reader in, int length) throws SQLException {
        if (in == null) {
            setNull();
            return;
        }
        
        this.binaryStream = null;
        this.characterStream = in;
        this.bytes = null;
        this.length = length;
    }

    public void setString(String value) throws SQLException {
        
        if (value == null) {
            setNull();
            return;
        }
        
        setBytes(getDatatypeCoder().encodeString(value, encodingDefinition.getEncoding(), mappingPath));
    }

    public void setBytes(byte[] value) throws SQLException {

        if (value == null) {
            setNull();
            return;
        }
        
        this.binaryStream = null;
        this.characterStream = null;
        this.bytes = value;
        this.length = value.length;
    }

    public void setBinaryStream(InputStream in, int length) throws SQLException {
        
        if (in == null) {
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
            copyCharacterStream(characterStream, length, encodingDefinition.getJavaEncodingName());
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
        setFieldData(getDatatypeCoder().encodeLong(blob.getBlobId()));
    }

    private void copyCharacterStream(Reader in, int length, String encoding) throws SQLException {
        FBBlob blob =  new FBBlob(gdsHelper);
        blob.copyCharacterStream(in, length, encoding);
        setFieldData(getDatatypeCoder().encodeLong(blob.getBlobId()));
    }
    
    private void copyBytes(byte[] bytes, int length) throws SQLException {
        FBBlob blob = new FBBlob(gdsHelper);
        blob.copyBytes(bytes, 0, length);
        setFieldData(getDatatypeCoder().encodeLong(blob.getBlobId()));
    }

}