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

import EDU.oswego.cs.dl.util.concurrent.FIFOSemaphore;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Set;
import java.lang.reflect.Array;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
//import javax.security.auth.Subject;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

/**
 * ManagedConnectionPool.java
 *
 *
 * Created: Sun Dec 30 21:17:36 2001
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 */

public class ManagedConnectionPool 
{

   private /*final*/ ManagedConnectionFactory mcf;
   private /*final*/ ConnectionRequestInfo defaultCri;
   private /*final*/ PoolParams poolParams;

   private final LinkedList mcs = new LinkedList();

   private /*final*/ FIFOSemaphore permits;

   private final static Logger log = LoggerFactory.getLogger(ManagedConnectionPool.class,false);

   private final Counter connectionCounter = new Counter();

   //used to fill pool after first connection returned.
   private boolean started = false;

   public ManagedConnectionPool(ManagedConnectionFactory mcf, ConnectionRequestInfo cri, PoolParams poolParams)
   {
      this.mcf = mcf;
      defaultCri = cri;
      this.poolParams = poolParams;
      permits = new FIFOSemaphore(this.poolParams.maxSize);
      IdleRemover.registerPool(this, poolParams.idleTimeout);
   }

   public ManagedConnection getConnection(ConnectionRequestInfo cri)
      throws ResourceException
   {
      cri = (cri == null)? defaultCri: cri;
      try 
      {
         if (permits.attempt(poolParams.blockingTimeout)) 
         {
            //We have a permit to get a connection. Is there one in the pool already?
            ManagedConnection mc = null;
            synchronized (mcs)
            {
               if (mcs.size() > 0) 
               {
                  mc = ((MCHolder)mcs.removeFirst()).getMC();
               } // end of if ()
            }
            try 
            {
               if (mc != null) 
               {
                  //Yes, we retrieved a ManagedConnection from the pool. Does it match?
                  mc = mcf.matchManagedConnections(new SetOfOne(mc), null, cri);
                  if (mc == null) 
                  {
                     //match didn't work!
                     throw new FBResourceException("Error in setting up ManagedConnectionPool: matchManagedConnection failed with ConnectionRequestInfo: " + cri);
                  } // end of if ()
                  return mc;
               } // end of if ()
               else 
               {
                  //No, the pool was empty, so we have to make a new one.
                  mc = createConnection(cri);
                  //lack of synch on "started" probably ok, if 2 reads occur we will just
                  //run fillPool twice, no harm done.
                  if (!started) 
                  {
                     started = true;
                     PoolFiller.fillPool(this);
                  } // end of if ()
                  return mc;
               } // end of else
            } catch (ResourceException re) 
            {
               //return permit and rethrow
               permits.release();
               throw re;
            } // end of try-catch
         } // end of if ()
         else 
         {
            //we timed out
            throw new FBResourceException("No ManagedConnections Available!");        
         } // end of else
      
      } catch (InterruptedException ie) 
      {
         throw new FBResourceException("Interrupted while requesting permit!");
      } // end of try-catch
      
   }

   public void returnConnection(ManagedConnection mc, boolean kill)
   {
      try 
      {
         mc.cleanup();
         if (kill) 
         {
            doDestroy(mc);
         } // end of if ()
         else 
         {
            synchronized (mcs)
            {
               mcs.addLast(new MCHolder(mc));
            }
         } // end of else
      } catch (ResourceException re) 
      {
         //if (log!=null) log.info("ResourceException returning ManagedConnection to pool:", re);
      } finally 
      {
         permits.release();
      } // end of try-catch
   }

   public void removeTimedOut()
   {
      if (log!=null) log.debug("Checking for timed out connections");  
      synchronized (mcs)
      {
         for (Iterator i = mcs.iterator(); i.hasNext(); ) 
         {
            MCHolder mch = (MCHolder)i.next();
            if (mch.isTimedOut()) 
            {
               if (log!=null) log.debug("Removing a timed-out connection");
               i.remove();
               doDestroy(mch.getMC());
            } // end of if ()
            else
            {
               //They were put in chronologically, so if one isn't timed out, following ones won't be either.
               break;               
            } // end of else
         } // end of for ()
      }
      //refill if necessary, asynchronously.
      PoolFiller.fillPool(this);
   }

   public void shutdown()
   {
      synchronized (mcs)
      {
         for (Iterator i = mcs.iterator(); i.hasNext(); ) 
         {
            ManagedConnection mc = ((MCHolder)i.next()).getMC();
            i.remove();
            doDestroy(mc);
         } // end of for ()
      }
      IdleRemover.unregisterPool(this);
   }

   public void fillToMin()
   {
      ArrayList newMCs = new ArrayList();
      try 
      {
         while (connectionCounter.getCount() < poolParams.minSize)
         {
            newMCs.add(getConnection(defaultCri));
         } // end of while ()
      }
      catch (ResourceException re)
      {
         //Whatever the reason, stop trying to add more!
      } // end of try-catch
      for (Iterator i = newMCs.iterator(); i.hasNext(); )
      {
         returnConnection((ManagedConnection)i.next(), false);
      } // end of for ()
      
   }

   public int getConnectionCount()
   {
      return connectionCounter.getCount();
   }

   private ManagedConnection createConnection(ConnectionRequestInfo cri) throws ResourceException
   {
      try 
      {
         connectionCounter.inc();
         return mcf.createManagedConnection(null, cri);         
      }
      catch (ResourceException re)
      {
         connectionCounter.dec();
         throw re;
      } // end of try-catch
   }  

   private void doDestroy(ManagedConnection mc)
   {   
      connectionCounter.dec();
      try 
      {
         mc.destroy();
      }
      catch (ResourceException re)
      {
         //if (log!=null) log.info("Exception destroying ManagedConnection", re);
      } // end of try-catch
   }

   public static class PoolParams
   {
      public int minSize = 0;
      public int maxSize = 10;
      public int blockingTimeout = 5000;//milliseconds
      public long idleTimeout = 1000*60*30;//milliseconds, 30 minutes.
   }


   private class MCHolder
   {
      private final ManagedConnection mc;
      private final long age;

      MCHolder(final ManagedConnection mc)
      {
         this.mc = mc;
         this.age = System.currentTimeMillis();
      }

      ManagedConnection getMC()
      {
         return mc;
      }

      boolean isTimedOut()
      {
         return System.currentTimeMillis() - age > poolParams.idleTimeout;
      }
   }

   private static class Counter
   {
      private int count = 0;

      synchronized int getCount() 
      {
         return count;
      }

      synchronized void inc()
      {
         count++;
      }

      synchronized void dec()
      {
         count--;
      }
   }

   public static class SetOfOne  implements Set
   {
      private final Object object;

      public SetOfOne(Object object)
      {
         if (object == null) 
         {
            throw new IllegalArgumentException("SetOfOne must contain a non-null object!");
         } // end of if ()
         
         this.object = object;
      }
      // implementation of java.util.Set interface

      /**
       *
       * @return <description>
       */
      public int hashCode() {
         return object.hashCode();
      }

      /**
       *
       * @param param1 <description>
       * @return <description>
       */
      public boolean equals(Object other) {
         if (other instanceof SetOfOne) 
         {
            return this.object == ((SetOfOne)other).object;
         } // end of if ()
         
         return false;
      }

      /**
       *
       * @param param1 <description>
       * @return <description>
       */
      public boolean add(Object param1) {
         throw new UnsupportedOperationException("can't add to SetOfOne");
      }

      /**
       *
       * @return <description>
       */
      public int size() {
         return 1;
      }

      /**
       *
       * @return <description>
       */
      public Object[] toArray() {
         return new Object[] {object};
      }

      /**
       *
       * @param param1 <description>
       * @return <description>
       */
      public Object[] toArray(Object[] array) {
         if (array.length < 1) 
         {
            array = (Object[])Array.newInstance(array.getClass().getComponentType(), 1); 
         } // end of if ()
         array[0] = object;
         return array;
      }

      /**
       *
       * @param param1 <description>
       * @return <description>
       */
      public boolean contains(Object object) {
         return this.object.equals(object);
      }

      /**
       *
       */
      public void clear() {
         throw new UnsupportedOperationException("can't clear SetOfOne");
      }

      /**
       *
       * @param param1 <description>
       * @return <description>
       */
      public boolean remove(Object param1) {
         throw new UnsupportedOperationException("can't remove from SetOfOne");
      }

      /**
       *
       * @return <description>
       */
      public boolean isEmpty() {
         return false;
      }

      /**
       *
       * @return <description>
       */
      public Iterator iterator() {
         return new Iterator() {
               boolean done = false;

               public boolean hasNext()
               {
                  return !done;
               }

               public Object next()
               {
                  if (done) 
                  {
                     throw new NoSuchElementException();
                  } // end of if ()
                  done = true;
                  return object;
               }

               public void remove()
               {
                  throw new UnsupportedOperationException();
               }
                  
            };
      }

      /**
       *
       * @param param1 <description>
       * @return <description>
       */
      public boolean containsAll(Collection col)
      {
         if (col == null || col.size() != 1 )
         {
            return false;
         } // end of if ()
         
         return object.equals(col.iterator().next());
      }

      /**
       *
       * @param param1 <description>
       * @return <description>
       */
      public boolean addAll(Collection param1) {
         throw new UnsupportedOperationException();
      }

      /**
       *
       * @param param1 <description>
       * @return <description>
       */
      public boolean removeAll(Collection param1) {
         throw new UnsupportedOperationException();
      }

      /**
       *
       * @param param1 <description>
       * @return <description>
       */
      public boolean retainAll(Collection param1) {
         throw new UnsupportedOperationException();
      }

   }
   
}// ManagedConnectionPool
