/*
 * JBoss, the OpenSource WebOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.rpc.handler;

import javax.xml.namespace.QName;
import javax.xml.rpc.JAXRPCException;
import javax.xml.rpc.soap.SOAPFaultException;

/** This interface is required to be implemented by a SOAP message handler.
 * The handleRequest, handleResponse and handleFault methods for a SOAP message
 * handler get access to the SOAPMessage from the SOAPMessageContext. The
 * implementation of these methods can modify the SOAPMessage including the
 * headers and body elements.
 *  
 * @author Scott.Stark@jboss.org
 * @author Rahul Sharma (javadoc)
 * @version $Revision$
 */
public interface Handler
{
   /** Gets the header blocks processed by this Handler instance.
    *
    * @return Array of QNames of header blocks processed by this handler instance.
    * QName is the qualified name of the outermost element of the Header block.
    */
   public QName[] getHeaders();

   /** The init method enables the Handler instance to initialize itself. The init method passes the handler
    * configuration as a HandlerInfo instance. The HandlerInfo is used to configure the Handler (for example: setup
    * access to an external resource or service) during the initialization.
    * In the init method, the Handler class may get access to any resources
    * (for example; access to a logging service or database) and maintain these as part of its instance variables.
    * Note that these instance variables must not have any state specific to the SOAP message processing performed in
    * the various handle method.
    *
    * @param config HandlerInfo configuration for the initialization of this handler
    * @throws JAXRPCException - if initialization of the handler fails
    */
   public void init(HandlerInfo config) throws JAXRPCException;

   /** The destroy method indicates the end of lifecycle for a Handler instance. The Handler implementation class should
    * release its resources and perform cleanup in the implementation of the destroy method.
    *
    * @throws JAXRPCException - if there was any error during destroy
    */
   public void destroy() throws JAXRPCException;

   /** The handleRequest method processes the request message.
    *
    * @param msgContext MessageContext parameter provides access to the request message.
    * @return boolean boolean Indicates the processing mode
    * <ul>
    * <li> Return true to indicate continued processing of the request handler chain.
    * The HandlerChain takes the responsibility of invoking the next entity.
    * The next entity may be the next handler in the HandlerChain or if this handler is the last handler in the chain,
    * the next entity is the service endpoint object. </li>
    *
    * <li> Return false to indicate blocking of the request handler chain. In this case, further processing of the request
    * handler chain is blocked and the target service endpoint is not dispatched. The JAX-RPC runtime system takes the
    * responsibility of invoking the response handler chain next with the SOAPMessageContext. The Handler implementation
    * class has the the responsibility of setting the appropriate response SOAP message in either handleRequest and/or
    * handleResponse method. In the default processing model, the response handler chain starts processing from the same
    * Handler instance (that returned false) and goes backward in the execution sequence. </li>
    * </ul>
    *
    * @throws JAXRPCException - indicates a handler-specific runtime error.
    * If JAXRPCException is thrown by a handleRequest method, the HandlerChain terminates the further processing of this handler chain.
    * On the server side, the HandlerChain generates a SOAP fault that indicates that the message could not be processed
    * for reasons not directly attributable to the contents of the message itself but rather to a runtime error during
    * the processing of the message. On the client side, the exception is propagated to the client code
    *
    * @throws SOAPFaultException - indicates a SOAP fault. The Handler implementation class has the the responsibility
    * of setting the SOAP fault in the SOAP message in either handleRequest and/or handleFault method.
    * If SOAPFaultException is thrown by a server-side request handler's handleRequest method, the HandlerChain
    * terminates the further processing of the request handlers in this handler chain and invokes the handleFault
    * method on the HandlerChain with the SOAP message msgContext. Next, the HandlerChain invokes the handleFault method
    * on handlers registered in the handler chain, beginning with the Handler instance that threw the exception and
    * going backward in execution. The client-side request handler's handleRequest method should not throw the SOAPFaultException.
    */
   public boolean handleRequest(MessageContext msgContext) throws JAXRPCException, SOAPFaultException;

   /** The handleResponse method processes the response SOAP message.
    *
    * @param msgContext MessageContext parameter provides access to the response SOAP message
    * @return boolean Indicates the processing mode
    * <ul>
    * <li> Return true to indicate continued processing ofthe response handler chain. The HandlerChain invokes the handleResponse method on the next Handler in the handler chain.</li>
    * <li>Return false to indicate blocking of the response handler chain. In this case, no other response handlers in the handler chain are invoked.</li>
    * </ul>
    * @throws JAXRPCException - indicates a handler specific runtime error. If JAXRPCException is thrown by a
    * handleResponse method, the HandlerChain terminates the further processing of this handler chain. On the server
    * side, the HandlerChain generates a SOAP fault that indicates that the message could not be processed for reasons
    * not directly attributable to the contents of the message itself but rather to a runtime error during the processing
    * of the message. On the client side, the runtime exception is propagated to the client code.
    */
   public boolean handleResponse(MessageContext msgContext);

   /** The handleFault method processes the SOAP faults based on the SOAP message processing model.
    *
    * @param msgContext MessageContext parameter provides access to the SOAP message
    * @return boolean Indicates the processing mode
    * <ul>
    * <li> Return true to indicate continued processing of SOAP Fault. The HandlerChain invokes the handleFault method
    * on the next Handler in the handler chain. </li>
    * <li> Return false to indicate end of the SOAP fault processing. In this case, no other handlers in the handler
    * chain are invoked. </li>
    * </ul>
    *
    * @throws JAXRPCException - indicates handler specific runtime error.
    * If JAXRPCException is thrown by a handleFault method, the HandlerChain terminates the further processing of this
    * handler chain. On the server side, the HandlerChain generates a SOAP fault that indicates that the message could
    * not be processed for reasons not directly attributable to the contents of the message itself but rather to a runtime
    * error during the processing of the message. On the client side, the JAXRPCException is propagated to the client code.
    */
   public boolean handleFault(MessageContext msgContext);

}
