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

import org.firebirdsql.jca.FBTpb;




/**
 * This DataSource implementation is for use in an unmanaged environment.
 * It allows you to set the databaseName, User, and Password. 
 * It creates a ManagedConnectionFactory and normal FBDataSource.  
 * All getConnection() calls are forwarded to the internal FBDataSource.
 * It supports pooling connections.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class FBWrappingDataSource extends FBSimpleDataSource implements DataSource, Serializable, Referenceable {

   transient private FBPoolingConnectionManager cm;

   private boolean pooling = false;

   private final ManagedConnectionPool.PoolParams poolParams = new ManagedConnectionPool.PoolParams();

    public FBWrappingDataSource() throws ResourceException {
        mcf = new FBManagedConnectionFactory();
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

   public FBTpb getTransactionParameters()
   {
      return mcf.getTpb();
   }

   public void setTransactionParameters(FBTpb tpb)
   {
      mcf.setTpb(tpb);
   }

    public Integer getTransactionIsolation() throws ResourceException
    {
        return mcf.getTransactionIsolation();
    }

    public void setTransactionIsolation(Integer level) throws ResourceException
    {
        mcf.setTransactionIsolation(level);
    }

   public int getConnectionCount()
   {
       if (cm == null) 
       {
           return 0;
       } // end of if ()
       
       return cm.getConnectionCount();
   }

   protected synchronized DataSource getDataSource() throws SQLException
   {
      if (ds == null) 
      {
         try 
         {
            if (getDatabase() == null)
            {
                throw new SQLException("DataSource has no databaseName");
            }
          
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
      
      return ds;
   }      

}





