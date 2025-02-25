// SPDX-FileCopyrightText: Copyright 2016-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.field;

import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.jaybird.util.IOUtils;
import org.firebirdsql.jdbc.FBRowId;
import org.jspecify.annotations.NullMarked;

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

    @NullMarked
    FBBinaryField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider, int requiredType)
            throws SQLException {
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
    @SuppressWarnings({ "java:S1168", "DataFlowIssue" })
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

    @SuppressWarnings("DataFlowIssue")
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
            throw invalidSetConversion(InputStream.class, ioex);
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
            throw invalidSetConversion(Reader.class, ioex);
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
