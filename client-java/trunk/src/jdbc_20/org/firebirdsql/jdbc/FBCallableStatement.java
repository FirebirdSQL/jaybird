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

import java.sql.CallableStatement;
import java.sql.SQLException;

import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.jdbc.FBObjectListener;

/**
 * JDBC 2.0 compliant implementation of {@link CallableStatement} interface.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class FBCallableStatement extends AbstractCallableStatement {

    /**
     * Create instance of this class.
     * 
     * @param c instance of {@link AbstractConnection}.
     * @param sql SQL statement to prepare.
     * 
     * @throws SQLException if something went wrong.
     */
    public FBCallableStatement(GDSHelper gdsHelper, String sql, 
                               int rsType, int rsConcurrency, int rsHoldability,
                               StoredProcedureMetaData storedProcedureMetaData,
                               FBObjectListener.StatementListener statementListener,
                               FBObjectListener.BlobListener blobListener)
        throws SQLException 
    {
        super(gdsHelper, sql, rsType, rsConcurrency, rsHoldability, storedProcedureMetaData, statementListener, blobListener);
    }

    public void registerOutParameter(int paramIndex, int sqlType, 
                                     String typeName) throws SQLException
    {
        registerOutParameter(paramIndex, sqlType);
    }
}
