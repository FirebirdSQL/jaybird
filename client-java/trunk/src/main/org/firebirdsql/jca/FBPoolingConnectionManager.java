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

package org.firebirdsql.jca;

/**
 * FBPoolingConnectionManager.java
 *
 *
 * Created: Wed Jan  2 12:16:09 2002
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 */
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;

public class FBPoolingConnectionManager
   implements ConnectionManager 
{

   private /*final*/ ManagedConnectionPool.PoolParams poolParams;
   private /*final*/ ManagedConnectionFactory mcf;
   private /*final*/ PoolingStrategy poolingStrategy;


   public FBPoolingConnectionManager(ManagedConnectionPool.PoolParams poolParams,
                                  ManagedConnectionFactory mcf)
   {
      this.poolParams = poolParams;
      this.mcf = mcf;
      poolingStrategy = new PoolByCri();
   }

   //temporary methods for testing

   ManagedConnection getManagedConnection(ConnectionRequestInfo cri) 
      throws ResourceException
   {
      return poolingStrategy.getConnection(cri);
   }

   void returnManagedConnection(ManagedConnection mc, boolean kill)
   {
      poolingStrategy.returnConnection(mc, kill);
   }

   public int getConnectionCount()
   {
      return poolingStrategy.getConnectionCount();
   }

   void shutdown()
   {
      poolingStrategy.shutdown();
   }
   // implementation of javax.resource.spi.ConnectionManager interface

   /**
    *
    * @param param1 <description>
    * @param param2 <description>
    * @return <description>
    * @exception javax.resource.ResourceException <description>
    */
   public Object allocateConnection(ManagedConnectionFactory mcf, ConnectionRequestInfo cri)
      throws ResourceException
   {
      if (this.mcf != mcf) 
      {
         throw new ResourceException("Wrong ManagedConnectionFactory sent to PoolingConnectionManager!");
      } // end of if ()
      final ManagedConnection mc = getManagedConnection(cri);
      mc.addConnectionEventListener(new ConnectionEventListener() {
            final ManagedConnection localmc = mc;

            public void connectionClosed(ConnectionEvent event)
            {
               //there can only be one Connection handle for this managed connection.
               localmc.removeConnectionEventListener(this);
               returnManagedConnection(localmc, false);
            }

            public void localTransactionStarted(ConnectionEvent event)
            {
               //nothing we can do about it.
            }

            public void localTransactionCommitted(ConnectionEvent event)
            {
            }

            public void localTransactionRolledback(ConnectionEvent event)
            {
            }

            public void connectionErrorOccurred(ConnectionEvent event)
            {
               localmc.removeConnectionEventListener(this);
               returnManagedConnection(localmc, true);
            }
         });
      return mc.getConnection(null, cri);
   }

   private interface PoolingStrategy
   {
      ManagedConnection getConnection(ConnectionRequestInfo cri) 
         throws ResourceException;

      void returnConnection(ManagedConnection mc, boolean kill);

      int getConnectionCount();

      void shutdown();
   }


   private class PoolByCri implements PoolingStrategy
   {
      private final Map pools = new HashMap();
      private final Map mcToPoolMap = new HashMap();

      public ManagedConnection getConnection(ConnectionRequestInfo cri) 
         throws ResourceException
      {
         ManagedConnectionPool mcp = null;
         synchronized (pools)
         {
            mcp = (ManagedConnectionPool)pools.get(cri);
            if (mcp == null) 
            {
               mcp = new ManagedConnectionPool(mcf, cri, poolParams/*, log*/);
               pools.put(cri, mcp);
            }   
         } 
         ManagedConnection mc = mcp.getConnection(cri);
         mcToPoolMap.put(mc, mcp);
         return mc;
      }

      public void returnConnection(ManagedConnection mc, boolean kill)
      {
         ManagedConnectionPool mcp = (ManagedConnectionPool)mcToPoolMap.get(mc);
         if (mcp == null) 
         {
            throw new IllegalArgumentException("Returned to wrong Pool!!");
         } // end of if ()
         mcp.returnConnection(mc, kill);
      }
      public int getConnectionCount()
      {
         int count = 0;
         for (Iterator i = pools.values().iterator(); i.hasNext(); )
         {
            count += ((ManagedConnectionPool)i.next()).getConnectionCount();
         } // end of for ()
         return count;
      }

      public void shutdown()
      {
         for (Iterator i = pools.values().iterator(); i.hasNext(); )
         {
            ((ManagedConnectionPool)i.next()).shutdown();
         } // end of for ()
      }
   }


}// FBPoolingConnectionManager
