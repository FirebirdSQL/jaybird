/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.firebirdsql.logging;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;

/**
 * Logger.java
 * With luck this can be set to an empty subclass of log4j.Logger when that is available.
 *
 * Created: Fri Feb  1 18:58:03 2002
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 */

public class Logger
{

   private static boolean loggingAvailable = true;

   private final Category log;

   protected Logger (String name)
   {
      if (loggingAvailable) 
      {
         Category myLog = null;
         try 
         {
            myLog = Category.getInstance(name);
         }
         catch (Throwable t)
         {
            loggingAvailable = false;
         } // end of try-catch
         log = myLog;
      } // end of if ()
      else
      {
         log = null;
      } // end of else
   }

   public static Logger getLogger(String name)
   {
      return new Logger(name);
   }

   public static Logger getLogger(Class clazz)
   {
      return new Logger(clazz.getName());
   }

   public boolean isDebugEnabled()
   {
      return loggingAvailable && log.isEnabledFor(Priority.DEBUG);
   }

   public void debug(Object message)
   {
      if (loggingAvailable) 
      {
         log.log(Priority.DEBUG, message);
      } // end of if ()
   }
   
   public void debug(Object message, Throwable t)
   {
      if (loggingAvailable) 
      {
         log.log(Priority.DEBUG, message, t);
      }
   }
   
   public boolean isInfoEnabled()
   {
      return loggingAvailable && log.isEnabledFor(Priority.INFO);
   }

   public void info(Object message)
   {
      if (loggingAvailable) 
      {
         log.log(Priority.INFO, message);
      }
   }
   
   public void info(Object message, Throwable t)
   {
      if (loggingAvailable) 
      {
         log.log(Priority.INFO, message, t);
      }
   }
   
   public void warn(Object message)
   {
      if (loggingAvailable) 
      {
         log.log(Priority.WARN, message);
      }
   }
   
   public void warn(Object message, Throwable t)
   {
      if (loggingAvailable) 
      {
         log.log(Priority.WARN, message, t);
      }
   }
   
   public void error(Object message)
   {
      if (loggingAvailable) 
      {
         log.log(Priority.ERROR, message);
      }
   }
   
   public void error(Object message, Throwable t)
   {
      if (loggingAvailable) 
      {
         log.log(Priority.ERROR, message, t);
      }
   }
   
   public void fatal(Object message)
   {
      if (loggingAvailable) 
      {
         log.log(Priority.FATAL, message);
      }
   }
   
   public void fatal(Object message, Throwable t)
   {
      if (loggingAvailable) 
      {
         log.log(Priority.FATAL, message, t);
      }
   }
   
}// Logger
