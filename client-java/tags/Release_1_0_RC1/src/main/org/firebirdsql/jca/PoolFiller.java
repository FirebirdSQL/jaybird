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


import java.util.LinkedList;


/**
 * PoolFiller.java
 *
 *
 * Created: Fri Jan  4 13:35:21 2002
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 */

public class PoolFiller 
{

   private final LinkedList pools = new LinkedList();

   private final Thread fillerThread;

   private static final PoolFiller filler = new PoolFiller();

   public static void fillPool(ManagedConnectionPool mcp)
   {
      filler.internalFillPool(mcp);
   }

   public PoolFiller ()
   {
      fillerThread = new Thread(
         new Runnable() {
            public void run()
            {
               while (true)//keep going unless interrupted
               {
                  try 
                  {
                     ManagedConnectionPool mcp = null;
                     while (true)//keep iterating through pools till empty, exception escapes.
                     {
                     
                        synchronized (pools)
                        {
                           mcp = (ManagedConnectionPool)pools.removeFirst();
                        }
                        if (mcp == null) 
                        {
                           break;
                        } // end of if ()
                        
                        mcp.fillToMin();
                     } // end of while (true)

                  }
                  catch (Exception e)
                  {//end of pools list
                  } // end of try-catch
                        
                  try 
                  {
                     synchronized (pools)
                     {
                        pools.wait();                        
                     }
                  }
                  catch (InterruptedException ie)
                  {
                     return;
                  } // end of try-catch
               } // end of while ()
            }  
         });
      fillerThread.start();
   }

   private void internalFillPool(ManagedConnectionPool mcp)
   {
      synchronized (pools)
      {
         pools.addLast(mcp);
         pools.notify();
      }
   }
   
}// PoolFiller
