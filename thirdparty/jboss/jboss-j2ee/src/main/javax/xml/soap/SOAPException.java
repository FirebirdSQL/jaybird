/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.soap;

/**
 * This is a JDK-1.3 version, which maintains the cause itself.
 *
 * @author Thomas.Diesler@jboss.org
 * @version $Revision$
 */
public class SOAPException extends Exception
{
   private Throwable cause;

   public SOAPException()
   {
   }

   public SOAPException(String message)
   {
      super(message);
   }

   public SOAPException(String message, Throwable cause)
   {
      super(message);
      this.cause = cause;
   }

   public SOAPException(Throwable cause)
   {
      super();
      this.cause = cause;
   }

   public Throwable getCause()
   {
      return cause;
   }
}
