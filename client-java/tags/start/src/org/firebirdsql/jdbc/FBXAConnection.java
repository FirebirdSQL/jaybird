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
import javax.sql.PooledConnection;
import javax.sql.XAConnection;
import javax.sql.ConnectionEventListener;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.resource.spi.LocalTransaction;


/**
 *
 *   @see <related>
 *   @author David Jencks (davidjencks@earthlink.net)
 *   @version $ $
 */


import java.sql.*;

/**This class implements both the pooled connection and XAConnection interfaces.

/**
 * <p>A PooledConnection object is a connection object that provides
 * hooks for connection pool management.  A PooledConnection object
 * represents a physical connection to a data source.
 */


/**
 * <P>An XAConnection object provides support for distributed 
 * transactions.  An XAConnection may be enlisted in a distributed
 * transaction by means of an XAResource object.
 */

public class FBXAConnection implements PooledConnection, XAConnection {

//Pooled Connection Implementation
  /**
   * <p>Create an object handle for this physical connection.  The object
   * returned is a temporary handle used by application code to refer to
   * a physical connection that is being pooled.
   *
   * @return  a Connection object
   * @exception SQLException if a database-access error occurs.
   */
    public Connection getConnection() throws  SQLException {
        throw new SQLException("Not yet implemented");
    }

      
  /**
   * <p>Close the physical connection.
   *
   * @exception SQLException if a database-access error occurs.
   */
    public void close() throws  SQLException {
        throw new SQLException("Not yet implemented");
    }

      
  /**<P> Add an event listener.
   */
    public void addConnectionEventListener(ConnectionEventListener listener) {
    }



  /**<P> Remove an event listener.
   */
    public void removeConnectionEventListener(ConnectionEventListener listener) {
    }
    
    
    
    //JDBC 3.0-----------------
    
    public void closeAll() {
    }

    //XAConnection implementation
  /**<P>In both javax.sql.XAConnection and javax.resource.spi.MangagedConnection
   * <P>Return an XA resource to the caller.
   *
   * @return the XAResource
   * @exception SQLException if a database-access error occurs
   */
    public javax.transaction.xa.XAResource getXAResource() throws  SQLException {
        return null;
    }

    
 } 





