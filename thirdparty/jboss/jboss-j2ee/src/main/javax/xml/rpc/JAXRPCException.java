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
public class JAXRPCException extends RuntimeException
{
   private Throwable cause;

   public JAXRPCException()
   {
   }

   public JAXRPCException(String message)
   {
      super(message);
   }

   public JAXRPCException(String message, Throwable cause)
   {
      super(message);
      this.cause = cause;
   }

   public JAXRPCException(Throwable cause)
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
