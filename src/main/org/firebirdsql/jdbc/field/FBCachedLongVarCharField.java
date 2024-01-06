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

import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.jdbc.FBCachedBlob;

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

    FBCachedLongVarCharField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider, int requiredType,
            GDSHelper gdsHelper) throws SQLException {
        super(fieldDescriptor, dataProvider, requiredType, gdsHelper);
    }

    @Override
    public Blob getBlob() throws SQLException {
        if (isNull()) return null;
        return new FBCachedBlob(getFieldData());
    }

    @Override
    public Clob getClob() throws SQLException {
    	if (isNull()) return null;
    	return new FBCachedClob((FBCachedBlob)getBlob(), blobConfig);
    }
}