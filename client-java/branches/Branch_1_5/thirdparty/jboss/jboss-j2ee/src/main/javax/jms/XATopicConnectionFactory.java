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
public interface XATopicConnectionFactory extends XAConnectionFactory, TopicConnectionFactory
{
    public XATopicConnection createXATopicConnection() throws JMSException;

    public XATopicConnection createXATopicConnection(String username,
                                                     String password) throws JMSException;



}
