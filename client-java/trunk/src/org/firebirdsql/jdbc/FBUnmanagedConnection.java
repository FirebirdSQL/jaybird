/*   The contents of this file are subject to the Mozilla Public
 *   License Version 1.1 (the "License"); you may not use this file
 *   except in compliance with the License. You may obtain a copy of
 *   the License at http://www.mozilla.org/MPL/
 *   Alternatively, the contents of this file may be used under the
 *   terms of the GNU Lesser General Public License Version 2 or later (the
 *   "LGPL"), in which case the provisions of the GPL are applicable
 *   instead of those above. You may obtain a copy of the Licence at
 *   http://www.gnu.org/copyleft/lgpl.html
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    relevant License for more details.
 *
 *    This file was created by members of the firebird development team.
 *    All individual contributions remain the Copyright (C) of those
 *    individuals.  Contributors to this file are either listed here or
 *    can be obtained from a CVS history command.
 *
 *    All rights reserved.
 */

/*
 * CVS modification log:
 * $Log$
 * Revision 1.2  2001/07/18 20:07:31  d_jencks
 * Added better GDSExceptions, new NativeSQL, and CallableStatement test from Roman Rokytskyy
 *
 * Revision 1.2  2001/07/15 20:26:31  rrokytskyy
 * Commit from public CVS
 *
 * Revision 1.1  2001/07/13 18:16:15  d_jencks
 * Implementation of jdbc 1.0 Driver contributed by Roman Rokytskyy
 *
 * Revision 1.1  2001/07/09 09:09:51  rrokytskyy
 * Initial revision
 *
 */



package org.firebirdsql.jdbc;

import org.firebirdsql.jca.FBManagedConnection;
import java.sql.SQLException;
import java.util.Map;

/**
 * This class represents a java.sql.Connection implementation that has control
 * over its own transaction (unlike in the managed case the transaction is
 * controlled by the third party).
 * <br>
 * Implementation assumes that only one transaction can be associated with
 * this connection and this transaction can be managed only using the
 * <code>commit()</code> and <code>rollback()</code> methods of this class.
 * Associated transaction is active until the connection is closed.
 * <p>
 * Implementation depends on the
 * <code>org.firebirdsql.jca.FBManagedConnection</code> class that is used
 * to manage associated transaction.
 *
 * @author Roman Rokytskyy (rrokytskyy@yahoo.co.uk)
 * @see FBConnection
 */

public class FBUnmanagedConnection extends FBConnection {
    private boolean autoCommit;

    /**
     * Creates an instance of the <code>FBUnmanagedConnection</code>.
     *
     * @param mc instance of managed connection that will manage the
     * transaction.
     */
    public FBUnmanagedConnection(FBManagedConnection mc)
        throws javax.resource.ResourceException, SQLException
    {
        // this is not completely correct implementation: we cause the managed
        // connection to be set twice: first in FBConnection constructor
        // and later in the setManagedConnection method. This may lead to
        // problems later on.
        super(mc);
        mc.associateConnection(this);

        // start the transaction (java.sql.Connection has no begin() method).
        getLocalTransaction().begin();
        setAutoCommit(true);
    }


    /**
     * Makes all changes made since the previous
     * commit/rollback permanent and releases any database locks
     * currently held by the Connection. This method should be
     * used only when auto-commit mode has been disabled.
     *
     * @exception SQLException if a database access error occurs
     */
    public void commit() throws SQLException {
        try {
            getLocalTransaction().commit();
            getLocalTransaction().begin();
        } catch(javax.resource.ResourceException resex) {
            throw new SQLException(resex.toString());
        }
    }

    /**
     * Drops all changes made since the previous
     * commit/rollback and releases any database locks currently held
     * by this Connection. This method should be used only when auto-
     * commit has been disabled.
     *
     * @exception SQLException if a database access error occurs
     */
    public void rollback() throws SQLException {
        if (isClosed())
            throw new SQLException("You cannot rollback closed connection.");
        try{
            getLocalTransaction().rollback();
            getLocalTransaction().begin();
        } catch(javax.resource.ResourceException resex) {
            throw new SQLException(resex.toString());
        }
    }

    /**
     * Converts the given SQL statement into the system's native SQL grammar.
     *
     * @param sql a SQL statement that may contain one or more '?'
     * parameter placeholders
     * @return the native form of this statement
     * @exception SQLException if a database access error occurs
     * @todo check if this implementation is correct.
     */
    /* public String nativeSQL(String sql) throws SQLException {
        try {
            return new FBEscapedParser().parse(sql);
        } catch(FBSQLParseException pex) {
            throw new SQLException(pex.toString());
        }
        }*/

    /**
     * Returns the current state of the autoCommit behaviour.
     *
     * @return <code>true</code> if connection commits after each executed
     * statement, otherwise <code>false</code>.
     */
    public boolean getAutoCommit() throws SQLException {
        return autoCommit;
    }

    /**
     * Sets the autoCommit behaviour of the connection.
     * <p>
     * <i>Currently not implemented</i>
     *
     * @param autoCommit <code>true</code> if connection is required to commit
     * after each executed statement, otherwise <code>false</code>
     * @throws <code>java.sql.SQLException</code> when the operation cannot
     * be completed.
     * @todo implement the correct autoCommit.
     */
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        this.autoCommit = autoCommit;
    }

    /**
     * Gets the type map object associated with this connection.
     * Unless the application has added an entry to the type map,
     * the map returned will be empty.
     *
     * @return the <code>java.util.Map</code> object associated
     *         with this <code>Connection</code> object
     */
    public Map getTypeMap() throws java.sql.SQLException {
        return new java.util.HashMap();
    }
}

