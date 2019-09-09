/*
 * $Id$
 *
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
import org.firebirdsql.jdbc.FBCachedBlob;
import org.firebirdsql.jdbc.FirebirdBlob;

import java.sql.Clob;
import java.sql.SQLException;

/**
 * Describe class <code>FBBlobField</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
final class FBCachedBlobField extends FBBlobField {

    FBCachedBlobField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider, int requiredType)
            throws SQLException {
        super(fieldDescriptor, dataProvider, requiredType);
    }

    @Override
    protected FirebirdBlob getBlobInternal() {
        if (blob != null) return blob;
        final byte[] bytes = getFieldData();
        if (bytes == null) return null;
        blob = new FBCachedBlob(bytes);
        return blob;
    }
    
    public Clob getClob() throws SQLException {
    	if (isNull()) return null;
    	return new FBCachedClob((FBCachedBlob)getBlob(), gdsHelper.getJavaEncoding());
    }

    public byte[] getBytes() throws SQLException {
        // TODO Looks suspicious compared to the implementation in FBBlobField
        return getFieldData();
    }
}
