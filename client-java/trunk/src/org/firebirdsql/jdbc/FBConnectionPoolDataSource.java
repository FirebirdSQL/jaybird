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

package org.firebirdsql.jdbc;


// imports --------------------------------------
import java.sql.SQLException;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

/**
 *
 *   @see <related>
 *   @author David Jencks (davidjencks@earthlink.net)
 *   @version $ $
 */




/**
 * A ConnectionPoolDataSource object is a factory for PooledConnection
 * objects.  An object that implements this interface will typically be
 * registered with a JNDI service.
 */

public class FBConnectionPoolDataSource implements ConnectionPoolDataSource {

  /**
   * <p>Attempt to establish a database connection.
   *
   * @return  a Connection to the database
   * @exception SQLException if a database-access error occurs.
   */
    public PooledConnection getPooledConnection() throws SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * <p>Attempt to establish a database connection.
   *
   * @param user the database user on whose behalf the Connection is being made
   * @param password the user's password
   * @return  a Connection to the database
   * @exception SQLException if a database-access error occurs.
   */
    public PooledConnection getPooledConnection(String user, String password) throws SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * <p>Get the log writer for this data source.
   *
   * <p>The log writer is a character output stream to which all logging
   * and tracing messages for this data source object instance will be
   * printed.  This includes messages printed by the methods of this
   * object, messages printed by methods of other objects manufactured
   * by this object, and so on.  Messages printed to a data source
   * specific log writer are not printed to the log writer associated
   * with the java.sql.Drivermanager class.  When a data source object is
   * created the log writer is initially null, in other words, logging
   * is disabled.
   *
   * @return the log writer for this data source, null if disabled
   * @exception SQLException if a database-access error occurs.
   */
    public java.io.PrintWriter getLogWriter() throws SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * <p>Set the log writer for this data source.
   *
   * <p>The log writer is a character output stream to which all logging
   * and tracing messages for this data source object instance will be
   * printed.  This includes messages printed by the methods of this
   * object, messages printed by methods of other objects manufactured
   * by this object, and so on.  Messages printed to a data source
   * specific log writer are not printed to the log writer associated
   * with the java.sql.Drivermanager class. When a data source object is
   * created the log writer is initially null, in other words, logging
   * is disabled.
   *
   * @param out the new log writer; to disable, set to null
   * @exception SQLException if a database-access error occurs.
   */
    public void setLogWriter(java.io.PrintWriter out) throws SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * <p>Sets the maximum time in seconds that this data source will wait
   * while attempting to connect to a database.  A value of zero
   * specifies that the timeout is the default system timeout
   * if there is one; otherwise it specifies that there is no timeout.
   * When a data source object is created the login timeout is
   * initially zero.
   *
   * @param seconds the data source login time limit
   * @exception SQLException if a database access error occurs.
   */
    public void setLoginTimeout(int seconds) throws SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Gets the maximum time in seconds that this data source can wait
   * while attempting to connect to a database.  A value of zero
   * means that the timeout is the default system timeout
   * if there is one; otherwise it means that there is no timeout.
   * When a data source object is created the login timeout is
   * initially zero.
   *
   * @return the data source login time limit
   * @exception SQLException if a database access error occurs.
   */
    public int getLoginTimeout() throws SQLException {
        throw new SQLException("Not yet implemented");
    }


 }





