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

package org.firebirdsql.jdbc;

import java.sql.*;
import org.firebirdsql.gds.XSQLVAR;

/**
 * This is Blob-based implementation of {@link FBStringField} for auto-commit case. 
 * It should be used for fields declared in database as <code>BLOB SUB_TYPE 1</code>. 
 * This implementation provides all conversion routines {@link FBStringField} has.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */

public class FBCachedLongVarCharField extends FBLongVarCharField {

    FBCachedLongVarCharField(XSQLVAR field, FBResultSet rs, int numCol) throws SQLException {
        super(field, rs, numCol);
    }

    Blob getBlob() throws SQLException {
        if (rs.row[numCol]==null)
            return BLOB_NULL_VALUE;

        return new FBCachedBlob(rs.row[numCol]);
    }
}