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

import java.io.*;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;

import org.firebirdsql.gds.*;
import org.firebirdsql.jdbc.*;

/**
 * Describe class <code>FBBlobField</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
public class FBBlobField extends FBField implements FBFlushableField {
    private FBBlob blob;

	// Rather then hold cached data in the XSQLDAVar we will hold it in here.
	private int length;
	private InputStream binaryStream;
    private Reader characterStream;
    private byte[] bytes;

    FBBlobField(XSQLVAR field, FieldDataProvider dataProvider, int requiredType) throws SQLException {
        super(field, dataProvider, requiredType);
    }
    
    public void close() throws SQLException {
        try {
            if (blob != null) blob.close();
        } catch(IOException ioex) {
            throw new FBSQLException(ioex);
        } finally {       
            // forget this blob instance, resource waste
            // but simplifies our life. BLOB handle will be
            // released by a server automatically later
            
            blob = null;
            bytes = null;
            binaryStream = null;
            characterStream = null;
            length = 0;
        }
    }

    public Blob getBlob() throws SQLException {
        if (blob != null) return blob;
        final byte[] bytes = getFieldData();
        if (bytes == null) return BLOB_NULL_VALUE;

        /*@todo convert this into a method of FirebirdConnection */
        blob = new FBBlob(gdsHelper, field.decodeLong( bytes ));

        return blob;
    }
    
    public Clob getClob() throws SQLException {
    	FBBlob blob = (FBBlob) getBlob();
    	if (blob == BLOB_NULL_VALUE) return CLOB_NULL_VALUE;
    	return new FBClob(blob);
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
        if (blob == BLOB_NULL_VALUE) return STREAM_NULL_VALUE;

        return blob.getBinaryStream();
    }

    public byte[] getBytes() throws SQLException {
        // getBytes() is not defined for BLOB types, only for BINARY
//        if (field.sqlsubtype < 0)
//            throw (SQLException)createException(
//                BYTES_CONVERSION_ERROR);

        return getBytesInternal();
    }
    
    public byte[] getBytesInternal() throws SQLException {
        final byte[] blobIdBuffer = getFieldData();
        if (blobIdBuffer == null) return BYTES_NULL_VALUE;
        
        final long blobId = field.decodeLong(blobIdBuffer);
        synchronized (((Synchronizable)getBlob()).getSynchronizationObject()) {
            try {
                final IscBlobHandle blobHandle = gdsHelper.openBlob(blobId, FBBlob.SEGMENTED);
                
                try {
                    final int blobLength = gdsHelper.getBlobLength(blobHandle);
                    final int bufferLength = gdsHelper.getBlobBufferLength();
                    final byte[] resultBuffer = new byte[blobLength];
                    
                    int offset = 0;
                    
                    while (offset < blobLength) {
                        final byte[] segmentBuffer = gdsHelper.getBlobSegment(blobHandle, bufferLength);
                        
                        if (segmentBuffer.length == 0) {
                            // unexpected EOF
                            throw (SQLException) createException(BYTES_CONVERSION_ERROR);
                        }
                        
                        System.arraycopy(segmentBuffer, 0, resultBuffer, offset, segmentBuffer.length);
                        offset += segmentBuffer.length;
                    }
                    
                    return resultBuffer;
                    
                } finally {
                    gdsHelper.closeBlob(blobHandle);
                }
            } catch (GDSException e) {
                throw new FBSQLException(e);
            }
        }
    }

    public byte[] getCachedData() throws SQLException {
        if (getFieldData() != null) {
            return getBytesInternal();
        } else if (bytes != null) {
            return bytes;
        } else {
            return BYTES_NULL_VALUE;
        }
    }
    
    public FBFlushableField.CachedObject getCachedObject() throws SQLException {
        if (getFieldData() == null) 
            return new FBFlushableField.CachedObject(bytes, binaryStream, characterStream, length);

        final byte[] bytesInternal = getBytesInternal();
        return new CachedObject(bytesInternal, null, null, bytesInternal.length);
    }
    
    public void setCachedObject(FBFlushableField.CachedObject cachedObject) throws SQLException {
        // setNull() to reset field to empty state
        setNull();
        bytes = cachedObject.bytes;
        binaryStream = cachedObject.binaryStream;
        characterStream = cachedObject.characterStream;
        length = cachedObject.length;
    }

    public String getString() throws SQLException {
        // getString() is not defined for BLOB fields, only for BINARY
        if (field.sqlsubtype < 0)
            throw (SQLException)createException(STRING_CONVERSION_ERROR).fillInStackTrace();

        Blob blob = getBlob();

        if (blob == BLOB_NULL_VALUE) return STRING_NULL_VALUE;

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
        // setNull() to reset field to empty state
        setNull();
        if (in != READER_NULL_VALUE) {
            characterStream = in;
            this.length = length;
        }
    }

    public void setBinaryStream(InputStream in, int length) throws SQLException {
        // setNull() to reset field to empty state
        setNull();
        if (in != STREAM_NULL_VALUE) {
            binaryStream = in;
            this.length = length;
        }
    }
    
    public void flushCachedData() throws SQLException {
        if (binaryStream != null)
            copyBinaryStream(binaryStream, length);
        else if (characterStream != null)
            copyCharacterStream(characterStream, length, javaEncoding);
        else if (bytes != null)
            copyBytes(bytes, length);
        else if (blob == null)
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
    
    public void setBytes(byte[] value) throws SQLException {
        // setNull() to reset field to empty state
        setNull();
        if (value != BYTES_NULL_VALUE) {
            bytes = value;
            length = value.length;
        }
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
        // setNull() to reset field to empty state
        setNull();
        setFieldData(field.encodeLong(blob.getBlobId()));
        this.blob = blob;
    }
    
    public void setClob(FBClob clob) throws SQLException {
    	FBBlob blob = clob.getWrappedBlob();
    	setBlob(blob);
    }

    public void setNull() {
        super.setNull();
        try {
            if (blob != null) blob.close();
        } catch (IOException e) {
            //ignore
        }

        blob = null;
        binaryStream = null;
        characterStream = null;
        bytes = null;
        length = 0;
    }
}
