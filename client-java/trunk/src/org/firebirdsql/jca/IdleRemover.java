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


package org.firebirdsql.jca;

import java.util.ArrayList;


import java.util.Collection;
import java.util.Iterator;

import org.firebirdsql.logging.Logger;

/**
 * IdleRemover.java
 *
 *
 * Created: Thu Jan  3 21:44:35 2002
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 */

public class IdleRemover 
{

   private final Logger log = Logger.getLogger(getClass());

   private final Collection pools = new ArrayList();

   private long interval = Long.MAX_VALUE;

   private long next = Long.MAX_VALUE;//important initialization!

   private static final IdleRemover remover = new IdleRemover();

   private final Thread removerThread;

   public static void  registerPool(ManagedConnectionPool mcp, long interval)
   {
      remover.internalRegisterPool(mcp, interval);
   }

   public static void unregisterPool(ManagedConnectionPool mcp)
   {
      remover.internalUnregisterPool(mcp);
   }

   private IdleRemover ()
   {
      removerThread = new Thread(
         new Runnable() {

            public void run()
            {
               synchronized (pools)
               {
                  while (true)
                  {
                     try 
                     {
                        pools.wait(interval);
                        log.debug("run: IdleRemover notifying pools, interval: " + interval);
                        for (Iterator i = pools.iterator(); i.hasNext(); ) 
                        {
                           ((ManagedConnectionPool)i.next()).removeTimedOut();
                        } // end of if ()
                        next = System.currentTimeMillis() + interval;
                        if (next < 0) 
                        {
                           next = Long.MAX_VALUE;      
                        } // end of if ()
                     }
                     catch (InterruptedException ie)
                     {
                        log.info("run: IdleRemover has been interrupted, returning");
                        return;  
                     } // end of try-catch
                        
                  } // end of while ()
                  
               }
            }
         });
      removerThread.start();
      
   }

   private void internalRegisterPool(ManagedConnectionPool mcp, long interval)
   {
      log.debug("internalRegisterPool: registering pool with interval " + interval + " old interval: " + this.interval);
      synchronized (pools)
      {
         pools.add(mcp);
         if (interval > 1 && interval/2 < this.interval) 
         {
            this.interval = interval/2;
            long maybeNext = System.currentTimeMillis() + this.interval;
            if (next > maybeNext && maybeNext > 0) 
            {
               log.debug("internalRegisterPool: about to notify thread: old next: " + next + ", new next: " + maybeNext);
               next = maybeNext;
               pools.notify();
               //removerThread.interrupt();
            } // end of if ()
            
         } // end of if ()
         
      }
   }

   private void internalUnregisterPool(ManagedConnectionPool mcp)
   {
      synchronized (pools)
      {
         pools.remove(mcp);
         if (pools.size() == 0) 
         {
            log.debug("internalUnregisterPool: setting interval to Long.MAX_VALUE");
            interval = Long.MAX_VALUE;
         } // end of if ()
         
      }
   }

   private void stop()
   {
      log.debug("stop: stopping IdleRemover");
      interval = -1;
      removerThread.interrupt();
   }
}// IdleRemover
