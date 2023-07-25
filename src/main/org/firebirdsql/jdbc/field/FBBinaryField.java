/*
 * Firebird Open Source JDBC Driver
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
import org.firebirdsql.jaybird.util.IOUtils;
import org.firebirdsql.jdbc.FBRowId;

import java.io.*;
import java.sql.DataTruncation;
import java.sql.RowId;
import java.sql.SQLException;

/**
 * Field for binary fields ({@code (VAR)CHAR CHARACTER SET OCTETS}).
 * <p>
 * Implements behavior that considers fields of {@code (VAR)CHAR CHARACTER SET OCTETS} to be of types
 * {@link java.sql.Types#BINARY} or {@link java.sql.Types#VARBINARY}.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
class FBBinaryField extends FBField {

    FBBinaryField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider, int requiredType) throws SQLException {
        super(fieldDescriptor, dataProvider, requiredType);
    }

    @Override
    public Object getObject() throws SQLException {
        return getBytes();
    }

    @Override
    public String getString() throws SQLException {
        return getDatatypeCoder().decodeString(getFieldData());
    }

    @Override
    public void setString(String value) throws SQLException {
        setBytes(getDatatypeCoder().encodeString(value));
    }

    @Override
    public byte[] getBytes() throws SQLException {
        if (isNull()) return null;
        // protect against unintentional modification of cached or shared byte-arrays (e.g. in DatabaseMetaData)
        return getFieldData().clone();
    }

    @Override
    public void setBytes(byte[] value) throws SQLException {
        if (value != null && value.length > fieldDescriptor.getLength()) {
            throw new DataTruncation(fieldDescriptor.getPosition() + 1, true, false, value.length,
                    fieldDescriptor.getLength());
        }

        setFieldData(value);
    }

    @Override
    public InputStream getBinaryStream() throws SQLException {
        if (isNull()) return null;
        return new ByteArrayInputStream(getFieldData());
    }

    @Override
    protected void setBinaryStreamInternal(InputStream in, long length) throws SQLException {
        if (setWhenNull(in)) return;
        if (length > fieldDescriptor.getLength()) {
            throw new DataTruncation(fieldDescriptor.getPosition() + 1, true, false, (int) length,
                    fieldDescriptor.getLength());
        }

        try {
            setBytes(IOUtils.toBytes(in, (int) length));
        } catch (IOException ioex) {
            SQLException conversionException = invalidSetConversion(InputStream.class);
            conversionException.initCause(ioex);
            throw conversionException;
        }
    }

    @Override
    protected void setCharacterStreamInternal(Reader in, long length) throws SQLException {
        if (setWhenNull(in)) return;
        if (length > fieldDescriptor.getLength()) {
            throw new DataTruncation(fieldDescriptor.getPosition() + 1, true, false, (int) length,
                    fieldDescriptor.getLength());
        }

        try {
            setString(IOUtils.toString(in, (int) length));
        } catch (IOException ioex) {
            SQLException conversionException = invalidSetConversion(Reader.class);
            conversionException.initCause(ioex);
            throw conversionException;
        }
    }

    // Binary fields supports setting RowId, because Firebird doesn't support detection of row id parameters

    @Override
    public RowId getRowId() throws SQLException {
        byte[] bytes = getFieldData();
        return bytes != null ? new FBRowId(bytes) : null;
    }

    @Override
    public void setRowId(RowId value) throws SQLException {
        setBytes(value != null ? value.getBytes() : null);
    }
    
}
