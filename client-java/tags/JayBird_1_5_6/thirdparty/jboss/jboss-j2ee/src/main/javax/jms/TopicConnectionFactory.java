/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package javax.jms;

/**
  *
  * @author Chris Kimpton (chris@kimptoc.net)
  * @version $Revision$
 **/
public interface TopicConnectionFactory extends ConnectionFactory
{
    public TopicConnection createTopicConnection() throws JMSException;

    public TopicConnection createTopicConnection(String username,
                                                 String password) throws JMSException;
}
