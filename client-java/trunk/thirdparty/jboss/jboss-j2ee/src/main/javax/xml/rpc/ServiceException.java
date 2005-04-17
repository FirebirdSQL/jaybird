/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.rpc;

/**
 * This is a JDK-1.3 version, which maintains the cause itself.
 *
 * @author Thomas.Diesler@jboss.org
 * @version $Revision$
 */
public class ServiceException extends Exception
{
   private Throwable cause;

   public ServiceException()
   {
   }

   public ServiceException(String message)
   {
      super(message);
   }

   public ServiceException(String message, Throwable cause)
   {
      super(message);
      this.cause = cause;
   }

   public ServiceException(Throwable cause)
   {
      super();
      this.cause = cause;
   }

   public Throwable getLinkedCause()
   {
      return getCause();
   }

   public Throwable getCause()
   {
      return cause;
   }
}
