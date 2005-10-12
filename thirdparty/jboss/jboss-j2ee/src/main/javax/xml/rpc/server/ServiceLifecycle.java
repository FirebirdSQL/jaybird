/*
 * JBoss, the OpenSource WebOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.rpc.server;

import javax.xml.rpc.ServiceException;

/** This interface defines a lifecycle interface for a JAX-RPC service endpoint.
 * If the service endpoint class implements the ServiceLifeycle interface, the
 * servlet container based JAX-RPC runtime system is required to manage the
 * lifecycle of the corresponding service endpoint objects.
 * 
 * @author Scott.Stark@jboss.org
 * @author Rahul Sharma (javadoc)
 * @version $Revision$
 */
public interface ServiceLifecycle
{
   public void init(Object context) throws ServiceException;
   public void destroy();
}
