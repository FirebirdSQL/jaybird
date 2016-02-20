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
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.fields.RowValue;

import java.sql.SQLException;
import java.util.List;

/**
 * JDBC 4.1 implementation of {@link java.sql.ResultSet} interface.
 * <p>
 * Contains methods specific to the JDBC 4.1 implementation, or exists if there are methods in higher JDBC versions
 * that cannot be defined in JDBC 4.1.
 * </p>
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class FBResultSet extends AbstractResultSet {

    public FBResultSet(GDSHelper gdsHelper, FBStatement fbStatement, FbStatement stmt,
            FBObjectListener.ResultSetListener listener, boolean metaDataQuery, int rsType, int rsConcurrency,
            int rsHoldability, boolean cached) throws SQLException {
        super(gdsHelper, fbStatement, stmt, listener, metaDataQuery, rsType, rsConcurrency, rsHoldability, cached);
    }

    public FBResultSet(RowDescriptor rowDescriptor, List<RowValue> rows, FBObjectListener.ResultSetListener listener)
            throws SQLException {
        super(rowDescriptor, rows, listener);
    }

    public FBResultSet(RowDescriptor rowDescriptor, List<RowValue> rows) throws SQLException {
        super(rowDescriptor, rows);
    }

    public FBResultSet(RowDescriptor rowDescriptor, GDSHelper gdsHelper, List<RowValue> rows, boolean retrieveBlobs)
            throws SQLException {
        super(rowDescriptor, gdsHelper, rows, retrieveBlobs);
    }
}
