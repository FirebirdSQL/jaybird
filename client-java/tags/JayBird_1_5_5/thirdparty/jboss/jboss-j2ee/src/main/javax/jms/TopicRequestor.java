/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package javax.jms;

/**
  * Provides a basic request/reply layer ontop of JMS.
  * Pass the constructor details of the session/topic to send requests upon.
  * Then call the request method to send a request.  The method will block
  * until the reply is received.
  *
  * @author Chris Kimpton (chris@kimptoc.net)
  * @author adrian brock (adrian@jboss.com)
  * @version $Revision$
 */
public class TopicRequestor
{
   // Constants -----------------------------------------------------

   private TopicSession topicSession = null;
   private Topic topic = null;
   private TopicPublisher requestPublisher = null;
   private TemporaryTopic responseTopic = null;
   private TopicSubscriber responseSubscriber = null;

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------

   public TopicRequestor(TopicSession session, Topic topic) throws JMSException
   {
      topicSession = session;
      this.topic = topic;
      requestPublisher = topicSession.createPublisher(topic);
      responseTopic = topicSession.createTemporaryTopic();
      responseSubscriber = topicSession.createSubscriber(responseTopic);
   }
   
   // Public --------------------------------------------------------

   public Message request(Message message) throws JMSException
   {
      message.setJMSReplyTo(responseTopic);
      requestPublisher.publish(message);
      return responseSubscriber.receive();
   }

   public void close() throws JMSException
   {
      responseSubscriber.close();
      responseTopic.delete();
      topicSession.close();
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
