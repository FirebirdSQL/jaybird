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
import org.firebirdsql.util.IOUtils;

import java.io.*;
import java.sql.DataTruncation;
import java.sql.SQLException;

/**
 * Field for binary fields ({@code (VAR)CHAR CHARACTER SET OCTETS}).
 * <p>
 * Implements behavior that considers fields of {@code (VAR)CHAR CHARACTER SET OCTETS} to be of types
 * {@link java.sql.Types#BINARY} or {@link java.sql.Types#VARBINARY}.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class FBBinaryField extends FBField {

    FBBinaryField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider, int requiredType) throws SQLException {
        super(fieldDescriptor, dataProvider, requiredType);
    }

    @Override
    public String getString() throws SQLException {
        if (isNull()) return null;
        return getDatatypeCoder().decodeString(getFieldData(),
                getDatatypeCoder().getEncodingFactory().getDefaultEncoding(), mappingPath);
    }

    @Override
    public void setString(String value) throws SQLException {
        if (value == null) {
            setNull();
            return;
        }

        setBytes(getDatatypeCoder().encodeString(value, getDatatypeCoder().getEncodingFactory().getDefaultEncoding(),
                mappingPath));
    }

    @Override
    public byte[] getBytes() throws SQLException {
        if (isNull()) return null;
        // protect against unintentional modification of cached or shared byte-arrays (eg in DatabaseMetaData)
        return getFieldData().clone();
    }

    @Override
    public void setBytes(byte[] value) throws SQLException {
        if (value == null) {
            setNull();
            return;
        }

        if (value.length > fieldDescriptor.getLength()) {
            throw new DataTruncation(-1, true, false, value.length, fieldDescriptor.getLength());
        }

        setFieldData(value);
    }

    @Override
    public InputStream getBinaryStream() throws SQLException {
        if (isNull()) return null;
        return new ByteArrayInputStream(getFieldData());
    }

    @Override
    public void setBinaryStream(InputStream in, long length) throws SQLException {
        if (in == null) {
            setNull();
            return;
        }

        if (length > fieldDescriptor.getLength()) {
            throw new DataTruncation(-1, true, false, (int) length, fieldDescriptor.getLength());
        }

        try {
            setBytes(IOUtils.toBytes(in, (int) length));
        } catch (IOException ioex) {
            throw new TypeConversionException(BINARY_STREAM_CONVERSION_ERROR);
        }
    }

    @Override
    public void setCharacterStream(Reader in, long length) throws SQLException {
        if (in == null) {
            setNull();
            return;
        }

        if (length > fieldDescriptor.getLength()) {
            throw new DataTruncation(-1, true, false, (int) length, fieldDescriptor.getLength());
        }

        try {
            setString(IOUtils.toString(in, (int) length));
        } catch (IOException ioex) {
            throw new TypeConversionException(CHARACTER_STREAM_CONVERSION_ERROR);
        }
    }
}
