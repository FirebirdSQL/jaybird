/*
 SPDX-FileCopyrightText: Copyright 2002-2004 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2003 Blas Rodriguez Somoza
 SPDX-FileCopyrightText: Copyright 2007 Gabriel Reid
 SPDX-FileCopyrightText: Copyright 2014-2025 Mark Rotteveel
 SPDX-License-Identifier: LGPL-2.1-or-later
*/
package org.firebirdsql.jdbc.field;

import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.jdbc.FBCachedBlob;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;

/**
 * This is Blob-based implementation of {@link FBStringField} for auto-commit case. 
 * It should be used for fields declared in database as {@code BLOB SUB_TYPE 1}.
 * This implementation provides all conversion routines {@link FBStringField} has.
 * 
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 */
final class FBCachedLongVarCharField extends FBLongVarCharField {

    @NullMarked
    FBCachedLongVarCharField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider, int requiredType,
            @Nullable GDSHelper gdsHelper) throws SQLException {
        super(fieldDescriptor, dataProvider, requiredType, gdsHelper);
    }

    @Override
    public Blob getBlob() {
        return getBlobInternal();
    }

    @Override
    protected FBCachedBlob getBlobInternal() {
        final byte[] fieldData = getFieldData();
        return fieldData != null ? new FBCachedBlob(fieldData) : null;
    }

    @Override
    public Clob getClob() {
        final FBCachedBlob blob = getBlobInternal();
        return blob != null ? new FBCachedClob(blob, blobConfig) : null;
    }

}