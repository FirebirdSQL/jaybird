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

package org.firebirdsql.jca.pool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;

import org.firebirdsql.jca.FBManagedConnectionFactory;
import org.firebirdsql.jca.TestXABase;
import org.firebirdsql.logging.Logger;


/**
 *  Unit Test for class ManagedConnectionPool
 *
 *
 * Created: Wed Jan  2 00:06:35 2002
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 */
public class TestPoolingConnectionManager extends TestXABase 
{


   boolean failed;


   /** 
    * Creates a new <code>TestPoolingConnectionManager</code> instance.
    *
    * @param name test name
    */
   public TestPoolingConnectionManager (String name)
   {
      super(name);
   }

   public void testGetManagedConnections() throws Exception
   {
      FBManagedConnectionFactory mcf = initMcf();//new FBManagedConnectionFactory();
      ManagedConnectionPool.PoolParams pp = new ManagedConnectionPool.PoolParams();
      pp.minSize = 0;
      pp.maxSize = 5;
      pp.blockingTimeout = 100;
      pp.idleTimeout = 500;
      //Subject subject = new Subject();
      final ConnectionRequestInfo cri = mcf.getDefaultConnectionRequestInfo();//new FBConnectionRequestInfo();
      FBPoolingConnectionManager cm = new FBPoolingConnectionManager(pp, mcf);
      ArrayList cs = new ArrayList();
      for (int i = 0; i < pp.maxSize; i++)
      {
         ManagedConnection mc = cm.getManagedConnection(cri);
         assertTrue("Got a null connection!", mc != null);
         cs.add(mc);
      } // end of for ()
      assertTrue("Wrong number of connections counted: " + cm.getConnectionCount(), cm.getConnectionCount() == pp.maxSize);
      try 
      {
         cm.getManagedConnection(cri);
         fail("Got a connection more than maxSize!");         
      }
      catch (ResourceException re)
      {
         //expected
      } // end of try-catch
      for (Iterator i = cs.iterator(); i.hasNext();)
      {
         cm.returnManagedConnection((ManagedConnection)i.next(), false);
      } // end of for ()
      cm.shutdown();
      assertTrue("Wrong number of connections counted: " + cm.getConnectionCount(), cm.getConnectionCount() == 0);
      
   }

    /**
     * The <code>testShortBlocking</code> method attempts to test the
     * performance of the FIFO queue based pool with lots of threads
     * and simulated work.  It appears to have periods of very long
     * response times and generally is slower than I (david jencks)
     * expect.  This needs more investigation with a performance
     * optimizing tool.
     *
     * @exception Exception if an error occurs
     */
    public void testShortBlocking() throws Exception
   {
      final int reps = 100;
      final int threadsPerConnection = 20;
      final long sleepTime = 2;//time connection is "used"
      final long overhead = 6;//overhead of context switching etc.
      failed = false;
      Collection threads = new ArrayList();
      FBManagedConnectionFactory mcf = initMcf();
      ManagedConnectionPool.PoolParams pp = new ManagedConnectionPool.PoolParams();
      pp.minSize = 0;
      pp.maxSize = 30;
      pp.blockingTimeout = (int)(threadsPerConnection * (sleepTime + overhead)) + 1000;
      pp.idleTimeout = 1000000;//milliseconds
      final ConnectionRequestInfo cri = mcf.getDefaultConnectionRequestInfo();
      final FBPoolingConnectionManager cm = new FBPoolingConnectionManager(pp, mcf);
      Collection cs = new ArrayList();
      //prefill the pool
      for (int i = 0; i < pp.maxSize; i++)
      {
          cs.add(cm.getManagedConnection(cri));
      }
      for (Iterator i = cs.iterator(); i.hasNext(); )
      {
          ManagedConnection mc = (ManagedConnection)i.next();
          cm.returnManagedConnection(mc, false);
      }
      cs.clear();
      if (log != null)
        log.info("Running short blocking test with blocking timeout: " + pp.blockingTimeout);
        
      final Logger llog = log;
      for (int i = 0; i < pp.maxSize * threadsPerConnection; i++)
      {
         Runnable t = new Runnable() {
               public void run() 
               {
                  for (int j = 0; j < reps; j++)
                  {
                      long startTime = System.currentTimeMillis();
                     try 
                     {
                        ManagedConnection mc = cm.getManagedConnection(cri);
                        Thread.sleep(sleepTime);
                        cm.returnManagedConnection(mc, false);
                     }
                      catch (ResourceException re)
                     {
                         long duration = System.currentTimeMillis() - startTime;
                         llog.info("presumed blocking timeout at iteration: " + j + ", duration: " + duration);
                         TestPoolingConnectionManager.this.failed = true;
                     } // end of try-catch
                     catch (InterruptedException ie)
                     {
                         return;
                     } // end of catch
                     
                     
                  } // end of for ()
                  
               }
            };
         Thread th = new Thread(t);
         threads.add(th);
         th.start();
      } // end of for ()
      for (Iterator i = threads.iterator(); i.hasNext();)
      {
          ((Thread)i.next()).join();
      } // end of for ()
      
      assertTrue("Wrong number of connections counted: " + cm.getConnectionCount(), cm.getConnectionCount() == pp.maxSize);
      assertTrue("Blocking Timeout occurred in ShortBlocking test", !failed);
      cm.shutdown();
      
   }

   
   public void testIdleTimeout() throws Exception
   {
      if (log != null) log.debug("testIdleTimeout");
      FBManagedConnectionFactory mcf = initMcf();//new FBManagedConnectionFactory();
      ManagedConnectionPool.PoolParams pp = new ManagedConnectionPool.PoolParams();
      pp.minSize = 0;
      pp.maxSize = 5;
      pp.blockingTimeout = 10;
      pp.idleTimeout = 100;
      ConnectionRequestInfo cri = mcf.getDefaultConnectionRequestInfo();
      final FBPoolingConnectionManager cm = new FBPoolingConnectionManager(pp, mcf);
      Collection mcs = new ArrayList(pp.maxSize);
      for (int i = 0 ; i < pp.maxSize; i++)
      {
         mcs.add(cm.getManagedConnection(cri));
      } // end of for ()
      for (Iterator i =  mcs.iterator(); i.hasNext(); )
      {
         cm.returnManagedConnection((ManagedConnection)i.next(), false);
      } // end of for ()
      
      assertTrue("Wrong number of connections counted: " + cm.getConnectionCount(), cm.getConnectionCount() == pp.maxSize);
      if (log != null) log.debug("about to wait for idletimeout");
      Thread.sleep((long)pp.idleTimeout * 10 + 1000);
      if (log != null) log.debug("waited for idle timeout");
      //      cm.removeTimedOut();
      assertTrue("Wrong number of connections counted: " + cm.getConnectionCount(), cm.getConnectionCount() == 0);
      

   }

   public void testFillToMin() throws Exception
   {
      FBManagedConnectionFactory mcf = initMcf();//new FBManagedConnectionFactory();
      ManagedConnectionPool.PoolParams pp = new ManagedConnectionPool.PoolParams();
      pp.minSize = 3;
      pp.maxSize = 5;
      pp.blockingTimeout = 1000;
      pp.idleTimeout = 20000;

      ConnectionRequestInfo cri = mcf.getDefaultConnectionRequestInfo();
      final FBPoolingConnectionManager cm = new FBPoolingConnectionManager(pp, mcf);
      ManagedConnection mc = cm.getManagedConnection(cri);
      cm.returnManagedConnection(mc, false);
      Thread.sleep(2000);//allow filltoMin to work
      assertTrue("Wrong number of connections counted: " + cm.getConnectionCount(), cm.getConnectionCount() == pp.minSize);
      Thread.sleep((long)(pp.idleTimeout * 7)/2);//try to get in the middle of cleanups
      //      cm.removeTimedOut();
      assertTrue("Wrong number of connections counted: " + cm.getConnectionCount(), cm.getConnectionCount() == pp.minSize);
      cm.shutdown();

   }

}// 
