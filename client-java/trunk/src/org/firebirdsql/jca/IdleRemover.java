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
                        for (Iterator i = pools.iterator(); i.hasNext(); ) 
                        {
                           ((ManagedConnectionPool)i.next()).removeTimedOut();
                        } // end of if ()
                        next = System.currentTimeMillis() + interval;
                        
                     }
                     catch (InterruptedException ie)
                     {
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
      synchronized (pools)
      {
         pools.add(mcp);
         if (interval > 1 && interval/2 < this.interval) 
         {
            this.interval = interval/2;
            long maybeNext = System.currentTimeMillis() + this.interval;
            if (next > maybeNext) 
            {
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
            interval = Long.MAX_VALUE;
         } // end of if ()
         
      }
   }

   private void stop()
   {
      interval = -1;
      removerThread.interrupt();
   }
}// IdleRemover
