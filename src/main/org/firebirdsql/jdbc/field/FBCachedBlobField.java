/*
 SPDX-FileCopyrightText: Copyright 2002-2003 Blas Rodriguez Somoza
 SPDX-FileCopyrightText: Copyright 2002-2007 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2007 Gabriel Reid
 SPDX-FileCopyrightText: Copyright 2013-2024 Mark Rotteveel
 SPDX-License-Identifier: LGPL-2.1-or-later
*/
package org.firebirdsql.jdbc.field;

import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.jdbc.FBCachedBlob;
import org.firebirdsql.jdbc.FirebirdBlob;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.sql.Clob;
import java.sql.SQLException;

/**
 * Field implementation for blobs other than {@code BLOB SUB_TYPE TEXT} which caches the blob content locally.
 *
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 */
final class FBCachedBlobField extends FBBlobField {

    @NullMarked
    FBCachedBlobField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider, int requiredType,
            @Nullable GDSHelper gdsHelper) throws SQLException {
        super(fieldDescriptor, dataProvider, requiredType, gdsHelper);
    }

    @Override
    protected FirebirdBlob getBlobInternal() {
        if (blob != null) return blob;
        final byte[] bytes = getFieldData();
        if (bytes == null) return null;
        blob = new FBCachedBlob(bytes);
        return blob;
    }

    @Override
    public Clob getClob() throws SQLException {
    	if (isNull()) return null;
    	return new FBCachedClob((FBCachedBlob) getBlob(), blobConfig);
    }

    @Override
    public byte[] getBytes() throws SQLException {
        // TODO Looks suspicious compared to the implementation in FBBlobField
        return getFieldData();
    }
}
