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
import java.sql.SQLException;

import org.firebirdsql.jca.FBManagedConnection;

/**
 * Firebird connection class implementing JDBC 3.0 methods.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class FBConnection extends AbstractConnection {

    /**
     * Create instance of this class for the specified managed connection.
     * 
     * @param mc
     *            managed connection.
     */
    public FBConnection(FBManagedConnection mc) {
        super(mc);
    }

    /**
     * Creates an unnamed savepoint in the current transaction and 
     * returns the new <code>Savepoint</code> object that represents it.
     *
     * @return the new <code>Savepoint</code> object
     * @exception SQLException if a database access error occurs
     *            or this <code>Connection</code> object is currently in
     *            auto-commit mode
     * @see Savepoint
     * @since 1.4
     */
    public Savepoint setSavepoint() throws SQLException {
        return (Savepoint)setFirebirdSavepoint();
    }

    /**
     * Creates a savepoint with the given name in the current transaction
     * and returns the new <code>Savepoint</code> object that represents it.
     *
     * @param name a <code>String</code> containing the name of the savepoint
     * @return the new <code>Savepoint</code> object
     * @exception SQLException if a database access error occurs
     *            or this <code>Connection</code> object is currently in
     *            auto-commit mode
     * @see Savepoint
     * @since 1.4
     */
    public Savepoint setSavepoint(String name) throws SQLException {
        return (Savepoint)setFirebirdSavepoint(name);
    }

    /**
     * Undoes all changes made after the given <code>Savepoint</code> object
     * was set. 
     * <P>
     * This method should be used only when auto-commit has been disabled.
     *
     * @param savepoint the <code>Savepoint</code> object to roll back to
     * @exception SQLException if a database access error occurs,
     *            the <code>Savepoint</code> object is no longer valid,
     *            or this <code>Connection</code> object is currently in
     *            auto-commit mode
     * @see Savepoint
     * @see #rollback
     * @since 1.4
     */
    public void rollback(Savepoint savepoint) throws SQLException {
        rollback((FirebirdSavepoint)savepoint);
    }

    /**
     * Removes the given <code>Savepoint</code> object from the current 
     * transaction. Any reference to the savepoint after it have been removed 
     * will cause an <code>SQLException</code> to be thrown.
     *
     * @param savepoint the <code>Savepoint</code> object to be removed
     * @exception SQLException if a database access error occurs or
     *            the given <code>Savepoint</code> object is not a valid 
     *            savepoint in the current transaction
     * @since 1.4
     */
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        releaseSavepoint((FirebirdSavepoint)savepoint);
    }

}
