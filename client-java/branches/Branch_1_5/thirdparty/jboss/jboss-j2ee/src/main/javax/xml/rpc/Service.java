/* JBoss, the OpenSource WebOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.rpc;

import javax.xml.namespace.QName;
import javax.xml.rpc.encoding.TypeMappingRegistry;
import javax.xml.rpc.handler.HandlerRegistry;
import java.net.URL;
import java.rmi.Remote;
import java.util.Iterator;

/** Service class acts as a factory for:
 * <ul>
 *    <li>Dynamic proxy for the target service endpoint.
 *    <li>Instance of the type javax.xml.rpc.Call for the dynamic invocation of a remote operation on the target service endpoint.
 *    <li>Instance of a generated stub class
 * </ul>
 *
 * @author Scott.Stark@jboss.org
 * @author Thomas.Diesler@jboss.org
 * @version $Revision$
 */
public interface Service
{
   /** Gets the location of the WSDL document for this Service.
    *
    * @return URL for the location of the WSDL document for this service
    */
   public URL getWSDLDocumentLocation();

   /** Returns an Iterator for the list of QNames of service endpoints grouped by this service
    *
    * @return Returns java.util.Iterator with elements of type javax.xml.namespace.QName
    * @throws ServiceException If this Service class does not have access to the required WSDL metadata
    */
   public Iterator getPorts() throws ServiceException;

   /** Gets the name of this service.
    * @return Qualified name of this service
    */
   public QName getServiceName();

   /**
    * Creates a Call object not associated with specific operation or target service endpoint.
    * This Call object needs to be configured using the setter methods on the Call interface.
    *
    * @return Call object
    * @throws ServiceException   If any error in the creation of the Call object
    */
   public Call createCall() throws ServiceException;

   /** Gets the TypeMappingRegistry for this Service object. The returned TypeMappingRegistry instance is pre-configured
    * to support the standard type mapping between XML and Java types types as required by the JAX-RPC specification.
    *
    * @return The TypeMappingRegistry for this Service object.
    * @throws java.lang.UnsupportedOperationException if the Service class does not support the configuration of TypeMappingRegistry.
    */
   public TypeMappingRegistry getTypeMappingRegistry();

   /** Returns the configured HandlerRegistry instance for this Service instance.
    *
    * @return HandlerRegistry
    * @throws java.lang.UnsupportedOperationException if the Service class does not support the configuration of a HandlerRegistry
    */
   public HandlerRegistry getHandlerRegistry();

   /**
    * The getPort method returns either an instance of a generated stub implementation class or a dynamic proxy.
    * The parameter serviceEndpointInterface specifies the service endpoint interface that is supported by the returned stub or proxy.
    * In the implementation of this method, the JAX-RPC runtime system takes the responsibility of selecting a protocol binding (and a port)
    * and configuring the stub accordingly. The returned Stub instance should not be reconfigured by the client.
    */
   public Remote getPort(Class seiClass) throws ServiceException;

   /** Creates a Call instance.
    *
    * @param portName Qualified name for the target service endpoint
    * @return Call instance
    * @throws ServiceException  If any error in the creation of the Call object
    */
   public Call createCall(QName portName) throws ServiceException;

   /** Gets an array of preconfigured Call objects for invoking operations on the specified port.
    * There is one Call object per operation that can be invoked on the specified port.
    * Each Call object is pre-configured and does not need to be configured using the setter methods on Call interface.
    *
    * Each invocation of the getCalls method returns a new array of preconfigured Call objects
    *
    * This method requires the Service implementation class to have access to the WSDL related metadata.
    *
    * @param portName  Qualified name for the target service endpoint
    * @return Call[] Array of pre-configured Call objects
    * @throws ServiceException If this Service class does not have access to the required WSDL metadata or if an illegal portName is specified.
    */
   public Call[] getCalls(QName portName) throws ServiceException;

   /**
    * The getPort method returns either an instance of a generated stub implementation class or a dynamic proxy.
    * A service client uses this dynamic proxy to invoke operations on the target service endpoint.
    * The serviceEndpointInterface specifies the service endpoint interface that is supported by the created dynamic proxy or stub instance.
    *
    * @param portName Qualified name of the service endpoint in the WSDL service descr
    * @param seiClass Service endpoint interface supported by the dynamic proxy or stub instance
    * @return Stub instance or dynamic proxy that supports the specified service endpoint interface
    * @throws ServiceException This exception is thrown in the following cases:
    * <ul>
    *    <li>If there is an error in creation of the dynamic proxy or stub instance
    *    <li>If there is any missing WSDL metadata as required by this method
    *    <li>Optionally, if an illegal serviceEndpointInterface or portName is specified 
    * </ul>
    */
   public Remote getPort(QName portName, Class seiClass) throws ServiceException;

   /** Creates a Call instance.
    *
    * @param portName  Qualified name for the target service endpoint
    * @param operationName   Name of the operation for which this Call object is to be created.
    * @return  Call instance
    * @throws ServiceException  If any error in the creation of the Call object
    */
   public Call createCall(QName portName, String operationName) throws ServiceException;

   /** Creates a Call instance.
    *
    * @param portName  Qualified name for the target service endpoint
    * @param operationName Qualified name of the operation for which this Call object is to be created.
    * @return  Call instance
    * @throws ServiceException  If any error in the creation of the Call object
    */
   public Call createCall(QName portName, QName operationName) throws ServiceException;
}

