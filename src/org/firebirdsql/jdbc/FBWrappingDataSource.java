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
import org.firebirdsql.jca.FBConnectionRequestInfo;
import org.firebirdsql.jca.FBManagedConnectionFactory;
import org.firebirdsql.jca.FBPoolingConnectionManager;
import org.firebirdsql.jca.ManagedConnectionPool;

import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Connection;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Set;
import javax.naming.Reference;
import javax.resource.Referenceable;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;


/**
 *
 *   @see <related>
 *   @author David Jencks (davidjencks@earthlink.net)
 *   @version $ $
 */



/**
 * This DataSource implementation is for use in an unmanaged environment.
 * It allows you to set the databaseName, User, and Password. 
 * It creates a ManagedConnectionFactory and normal FBDataSource.  
 * All getConnection() calls are forwarded to the internal FBDataSource.
 */


public class FBWrappingDataSource implements DataSource, Serializable, Referenceable {

    transient private FBManagedConnectionFactory mcf;

   transient private FBPoolingConnectionManager cm;

    transient private FBDataSource ds;

    transient private PrintWriter log;

    private Reference jndiReference;

    private int loginTimeout = 0;

    private String description;

   private boolean pooling = false;

   private final ManagedConnectionPool.PoolParams poolParams = new ManagedConnectionPool.PoolParams();

    public FBWrappingDataSource() throws ResourceException {
        mcf = new FBManagedConnectionFactory();
    }


    public void setReference(Reference ref) {
        this.jndiReference = ref;
    }

    public Reference getReference() {
        return jndiReference;
    }

   public void setPooling(final boolean pooling)
   {
      this.pooling = pooling;
   }

   public boolean getPooling()
   {
      return pooling;
   }

   public void setMinSize(final int minSize)
   {
      poolParams.minSize = minSize;
   }

   public int getMinSize()
   {
      return poolParams.minSize;
   }

   public void setMaxSize(int maxSize)
   {
      poolParams.maxSize = maxSize;
   }

   public int getMaxSize()
   {
      return poolParams.maxSize;
   }

   public void setBlockingTimeout(final int blockingTimeout)
   {
      poolParams.blockingTimeout = blockingTimeout;
   }

   public int getBlockingTimeout()
   {
      return poolParams.blockingTimeout;
   }

   public void setIdleTimeout(long idleTimeout)
   {
      poolParams.idleTimeout = idleTimeout;
   }

   public long getIdleTimeout()
   {
      return poolParams.idleTimeout;
   }

   public void setIdleTimeoutMinutes(long idleTimeout)
   {
      poolParams.idleTimeout = idleTimeout * 1000 * 60;
   }

   public long getIdleTimeoutMinutes()
   {
      return poolParams.idleTimeout / (1000 * 60);
   }

   public FBConnectionRequestInfo getConnectionRequestInfo()
   {
      return mcf.getDefaultConnectionRequestInfo();
   }

   public void setConnectionRequestInfo(FBConnectionRequestInfo cri)
   {
      mcf.setConnectionRequestInfo(cri);
   }

   public Set getTransactionParameters()
   {
      return mcf.getTpb();
   }

   public void setTransactionParameters(Set tpb)
   {
      mcf.setTpb(tpb);
   }

   public int getConnectionCount()
   {
      return cm.getConnectionCount();
   }

  /**
   * <p>Attempt to establish a database connection.
   *
   * @return  a Connection to the database
   * @exception SQLException if a database-access error occurs.
   */
    public Connection getConnection() throws  SQLException {
       checkStarted();
        return ds.getConnection();
    }


  /**
   * <p>Attempt to establish a database connection.
   *
   * @param user the database user on whose behalf the Connection is
   *  being made
   * @param password the user's password
   * @return  a Connection to the database
   * @exception SQLException if a database-access error occurs.
   */
    public Connection getConnection(String userName, String userPassword) throws  SQLException {
       checkStarted();
        return ds.getConnection(userName, userPassword);
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
   * with the java.sql.Drivermanager class.  When a DataSource object is
   * created the log writer is initially null, in other words, logging
   * is disabled.
   *
   * @return the log writer for this data source, null if disabled
   * @exception SQLException if a database-access error occurs.
   */
    public PrintWriter getLogWriter() throws  SQLException {
        return log;
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
   * with the java.sql.Drivermanager class. When a DataSource object is
   * created the log writer is initially null, in other words, logging
   * is disabled.
   *
   * @param out the new log writer; to disable, set to null
   * @exception SQLException if a database-access error occurs.
   */
    public void setLogWriter(PrintWriter out) throws  SQLException {
        log = out;
    }


  /**
   * <p>Sets the maximum time in seconds that this data source will wait
   * while attempting to connect to a database.  A value of zero
   * specifies that the timeout is the default system timeout
   * if there is one; otherwise it specifies that there is no timeout.
   * When a DataSource object is created the login timeout is
   * initially zero.
   *
   * @param seconds the data source login time limit
   * @exception SQLException if a database access error occurs.
   */
    public void setLoginTimeout(int seconds) throws  SQLException {
       //loginTimeout = seconds;
       setBlockingTimeout(seconds * 1000);
    }


  /**
   * Gets the maximum time in seconds that this data source can wait
   * while attempting to connect to a database.  A value of zero
   * means that the timeout is the default system timeout
   * if there is one; otherwise it means that there is no timeout.
   * When a DataSource object is created the login timeout is
   * initially zero.
   *
   * @return the data source login time limit
   * @exception SQLException if a database access error occurs.
   */
    public int getLoginTimeout() throws  SQLException {
       //return loginTimeout;
       return getBlockingTimeout()/1000;
    }

    //Now the useful properties for the DataSource.


    public void setDatabaseName(String database)
    {
        mcf.setDatabase(database);
    }

    public String getDatabaseName()
    {
        return mcf.getDatabase();
    }

    public void setUser(String userName)
    {
        FBConnectionRequestInfo cri = mcf.getDefaultConnectionRequestInfo();
        cri.setUser(userName);
        mcf.setConnectionRequestInfo(cri);
    }

    public String getUser()
    {
        return  mcf.getDefaultConnectionRequestInfo().getUser();
    }

    public void setPassword(String userPassword)
    {
        FBConnectionRequestInfo cri = mcf.getDefaultConnectionRequestInfo();
        cri.setPassword(userPassword);
        mcf.setConnectionRequestInfo(cri);
    }

    public String getPassword()
    {
        return  mcf.getDefaultConnectionRequestInfo().getPassword();
    }

    //This seems pretty useless, but the jdbc 3 spec says its required...
    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getDescription()
    {
        return description;
    }


    /**
     * Get the BlobBufferLength value.
     * @return the BlobBufferLength value.
     */
    public int getBlobBufferLength()
    {
        return mcf.getBlobBufferLength();
    }

    /**
     * Set the BlobBufferLength value.
     * @param newBlobBufferLength The new BlobBufferLength value.
     */
    public void setBlobBufferLength(final int blobBufferLength)
    {
        mcf.setBlobBufferLength(blobBufferLength);
    }

    

   private void checkStarted() throws SQLException
   {
      if (ds == null) 
      {
         try 
         {
          
            if (pooling) 
            {
               cm = new FBPoolingConnectionManager(poolParams, mcf);
               ds = (FBDataSource)mcf.createConnectionFactory(cm);  
            } // end of if ()
            else
            {
               ds = (FBDataSource)mcf.createConnectionFactory();
            } // end of else
         }
         catch (ResourceException re)
         {
            throw new SQLException("Couldn't create ConnectionFactory! " + re);
         } // end of try-catch
        
      } // end of if ()
   }      

}





