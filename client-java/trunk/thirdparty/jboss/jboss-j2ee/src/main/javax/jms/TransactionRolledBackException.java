/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package javax.jms;

/**
  * This exception must be thrown when a call to Session.commit results in a rollback of 
  * the current transaction. 
  *
  * @author Chris Kimpton (chris@kimptoc.net)
  * @version $Revision$
 **/
public class TransactionRolledBackException extends JMSException
{
   // CONSTRUCTORS -----------------------------------------------------

   /** 
     * Construct a TransactionRolledBackException with reason and error code for exception
     */
   public TransactionRolledBackException(String reason, String errorCode)
   {
      super(reason,errorCode);
   }

   /** 
     * Construct a TransactionRolledBackException with reason and with error code defaulting to null
     */
   public TransactionRolledBackException(String reason)
   {
      super(reason,null);
   }

}
