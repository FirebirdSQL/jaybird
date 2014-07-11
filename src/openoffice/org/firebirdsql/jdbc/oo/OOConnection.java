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
package org.firebirdsql.jdbc.oo;

import java.sql.*;

import org.firebirdsql.jca.FBManagedConnection;
import org.firebirdsql.jdbc.*;

public class OOConnection extends FBConnection {

    private OODatabaseMetaData metaData;

    public OOConnection(FBManagedConnection mc) {
        super(mc);
    }

    public synchronized DatabaseMetaData getMetaData() throws SQLException {
        if (metaData == null) metaData = new OODatabaseMetaData(this);
        return metaData;
    }

    public synchronized Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        Statement stmt = new OOStatement(getGDSHelper(), resultSetType,
                resultSetConcurrency, resultSetHoldability, txCoordinator);

        activeStatements.add(stmt);
        return stmt;
    }

    public synchronized PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
            int resultSetHoldability, boolean metaData, boolean generatedKeys) throws SQLException {
        FBObjectListener.StatementListener coordinator = txCoordinator;
        if (metaData)
            coordinator = new InternalTransactionCoordinator.MetaDataTransactionCoordinator(txCoordinator);

        FBObjectListener.BlobListener blobCoordinator;
        blobCoordinator = metaData ? null : txCoordinator;

        PreparedStatement stmt = new OOPreparedStatement(getGDSHelper(), sql, resultSetType, resultSetConcurrency,
                resultSetHoldability, coordinator, blobCoordinator, metaData, false, generatedKeys);

        activeStatements.add(stmt);
        return stmt;
    }
}
