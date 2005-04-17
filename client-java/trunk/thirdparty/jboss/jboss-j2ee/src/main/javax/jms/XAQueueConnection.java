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
public interface XAQueueConnection extends XAConnection, QueueConnection
{
    public XAQueueSession createXAQueueSession() throws JMSException;

    public QueueSession createQueueSession(boolean transacted,
                                           int acknowledgeMode) throws JMSException;

}
