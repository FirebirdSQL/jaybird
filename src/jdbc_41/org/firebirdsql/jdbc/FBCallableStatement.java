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

import java.sql.SQLException;

/**
 * JDBC 4.1 implementation of {@link java.sql.PreparedStatement} interface.
 * <p>
 * Contains methods specific to the JDBC 4.1 implementation, or exists if there are methods in higher JDBC versions
 * that cannot be defined in JDBC 4.1.
 * </p>
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:sjardine@users.sourceforge.net">Steven Jardine</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class FBCallableStatement extends AbstractCallableStatement {

    protected FBCallableStatement(GDSHelper c, String sql, int rsType, int rsConcurrency, int rsHoldability,
            StoredProcedureMetaData storedProcMetaData, FBObjectListener.StatementListener statementListener,
            FBObjectListener.BlobListener blobListener) throws SQLException {
        super(c, sql, rsType, rsConcurrency, rsHoldability, storedProcMetaData, statementListener, blobListener);
    }
}
