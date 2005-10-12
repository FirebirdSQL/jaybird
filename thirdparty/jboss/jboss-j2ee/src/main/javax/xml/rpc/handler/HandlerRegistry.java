/*
 * JBoss, the OpenSource WebOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.rpc.handler;

import java.io.Serializable;
import java.util.List;
import javax.xml.namespace.QName;

/** This interface provides support for the programmatic configuration of
 * handlers in a HandlerRegistry.
 * 
 * A handler chain is registered per service endpoint, as indicated by the
 * qualified name of a port. The getHandlerChain returns the handler chain
 * (as a java.util.List) for the specified service endpoint. The returned
 * handler chain is configured using the java.util.List interface. Each element
 * in this list is required to be of the Java type
 * javax.xml.rpc.handler.HandlerInfo. 
 * 
 * @author Scott.Stark@jboss.org
 * @author Rahul Sharma (javadoc)
 * @version $Revision$
 */
public interface HandlerRegistry
   extends Serializable
{
   public List getHandlerChain(QName portName);
   public void setHandlerChain(QName portName, List chain);
}
