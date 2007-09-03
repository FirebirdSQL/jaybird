/*
 * Firebird Open Source J2ee connector - jdbc driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
 
package org.firebirdsql.jdbc.field;

import java.sql.SQLException;
import java.sql.Blob;

import org.firebirdsql.gds.XSQLVAR;
import org.firebirdsql.jdbc.FBCachedBlob;

/**
 * Describe class <code>FBBlobField</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
public class FBCachedBlobField extends FBBlobField {

    FBCachedBlobField(XSQLVAR field, FieldDataProvider dataProvider, int requiredType) 
        throws SQLException 
    {
        super(field, dataProvider, requiredType);
    }

    public Blob getBlob() throws SQLException {
        if (getFieldData()==null)
            return BLOB_NULL_VALUE;

        return new FBCachedBlob(getFieldData());
    }

    public byte[] getBytes() throws SQLException {
        return getFieldData();
    }
    
}
