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
public interface ConnectionConsumer
{
    public ServerSessionPool getServerSessionPool() throws JMSException;

    public void close() throws JMSException;

}
