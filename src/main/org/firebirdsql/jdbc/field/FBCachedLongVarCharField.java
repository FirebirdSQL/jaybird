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

import org.firebirdsql.gds.XSQLVAR;
import org.firebirdsql.jdbc.FBCachedBlob;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;

/**
 * This is Blob-based implementation of {@link FBStringField} for auto-commit case.
 * It should be used for fields declared in database as <code>BLOB SUB_TYPE 1</code>.
 * This implementation provides all conversion routines {@link FBStringField} has.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */

public class FBCachedLongVarCharField extends FBLongVarCharField {

    FBCachedLongVarCharField(XSQLVAR field, FieldDataProvider dataProvider, int requiredType) throws SQLException {
        super(field, dataProvider, requiredType);
    }

    public Blob getBlob() throws SQLException {
        final byte[] fieldData = getFieldData();
        if (fieldData == null) return BLOB_NULL_VALUE;

        return new FBCachedBlob(fieldData);
    }

    public Clob getClob() throws SQLException {
        final byte[] fieldData = getFieldData();
        if (fieldData == null) return CLOB_NULL_VALUE;

        return new FBCachedClob((FBCachedBlob) getBlob(), gdsHelper.getJavaEncoding());
    }
}