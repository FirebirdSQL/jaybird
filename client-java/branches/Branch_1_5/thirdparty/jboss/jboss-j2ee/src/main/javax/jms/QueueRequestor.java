/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package javax.jms;

/**
 * A queue requestor
 *
 * @author Chris Kimpton (chris@kimptoc.net)
 * @author Adrian Brock (adrian@jboss.com)
 * @version $Revision$
 */
public class QueueRequestor
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   private QueueSession queueSession = null;
   private Queue queue = null;
   private QueueSender requestSender = null;
   private QueueReceiver replyReceiver = null;
   private TemporaryQueue replyQueue = null;

   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------

   public QueueRequestor(QueueSession session, Queue queue) throws JMSException
   {
      queueSession = session;
      this.queue = queue;

      requestSender = queueSession.createSender(queue);
      replyQueue = queueSession.createTemporaryQueue();
      replyReceiver = queueSession.createReceiver(replyQueue);
   }
   
   // Public --------------------------------------------------------

   public Message request(Message message) throws JMSException
   {
      message.setJMSReplyTo(replyQueue);
      message.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);
      requestSender.send(message);
      return replyReceiver.receive();
   }

   public void close() throws JMSException
   {
      replyReceiver.close();
      replyQueue.delete();
      queueSession.close();
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------

    // PUBLIC METHODS ------------------------------------------

    // INSTANCE VARIABLES ----------------------------------------
}
