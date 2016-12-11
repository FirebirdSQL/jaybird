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

import org.firebirdsql.gds.GDSException;

import java.sql.RowIdLifetime;
import java.sql.SQLException;

/**
 * JDBC 4.2 implementation of {@link java.sql.DatabaseMetaData}
 */
public class FBDatabaseMetaData extends AbstractDatabaseMetaData {

    public FBDatabaseMetaData(AbstractConnection c) throws GDSException {
        super(c);
    }

    /**
     * Indicates whether or not this data source supports the SQL <code>ROWID</code> type,
     * and if so  the lifetime for which a <code>RowId</code> object remains valid. 
     * <p>
     * The returned int values have the following relationship: 
     * <pre>
     *     ROWID_UNSUPPORTED < ROWID_VALID_OTHER < ROWID_VALID_TRANSACTION
     *         < ROWID_VALID_SESSION < ROWID_VALID_FOREVER
     * </pre>
     * so conditional logic such as 
     * <pre>
     *     if (metadata.getRowIdLifetime() > DatabaseMetaData.ROWID_VALID_TRANSACTION)
     * </pre>
     * can be used. Valid Forever means valid across all Sessions, and valid for 
     * a Session means valid across all its contained Transactions. 
     *
     * @return the status indicating the lifetime of a <code>RowId</code>
     * @throws java.sql.SQLException if a database access error occurs
     * @since 1.6
     */
    public RowIdLifetime getRowIdLifetime() throws SQLException {
        return RowIdLifetime.ROWID_UNSUPPORTED;
    }

    public int getJDBCMajorVersion() {
        return 4;
    }
    
    public int getJDBCMinorVersion() {
        return 2;
    }
}
