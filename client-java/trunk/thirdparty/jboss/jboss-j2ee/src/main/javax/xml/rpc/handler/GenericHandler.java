/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.rpc.handler;

import javax.xml.namespace.QName;

/** The GenericHandler class implements the Handler interface. SOAP Message
 * Handler developers should typically subclass GenericHandler class unless
 * the Handler class needs another class as a superclass.
 * 
 * The GenericHandler class is a convenience abstract class that makes writing
 * Handlers easy. This class provides default implementations of the lifecycle
 * methods init and destroy and also different handle methods. A Handler
 * developer should only override methods that it needs to specialize as part
 * of the derived Handler implementation class.
 * 
 * @author Scott.Stark@jboss.org
 * @author Rahul Sharma (javadoc)
 * @version $Revision$
 */
public abstract class GenericHandler
   implements Handler
{
   /** Default constructor. */
   protected GenericHandler()
   {
   }

   /**
    * Gets the header blocks processed by this Handler instance.
    *
    * @return Array of QNames of header blocks processed by this handler instance.
    * QName is the qualified name of the outermost element of the Header block.
    */
   public abstract QName[] getHeaders();

   /**
    * The init method to enable the Handler instance to initialize itself. This method should be overridden if the
    * derived Handler class needs to specialize implementation of this method.
    * @param config handler configuration
    */
   public void init(HandlerInfo config)
   {
   }

   /**
    * The destroy method indicates the end of lifecycle for a Handler instance. This method should be overridden if
    * the derived Handler class needs to specialize implementation of this method.
    */
   public void destroy()
   {
   }

   /**
    * The handleRequest method processes the request SOAP message. The default implementation of this method returns true.
    * This indicates that the handler chain should continue processing of the request SOAP message.
    * This method should be overridden if the derived Handler class needs to specialize implementation of this method.
    * @param msgContext the message msgContext
    * @return true/false
    */
   public boolean handleRequest(MessageContext msgContext)
   {
      return true;
   }

   /**
    * The handleResponse method processes the response message. The default implementation of this method returns true.
    * This indicates that the handler chain should continue processing of the response SOAP message.
    * This method should be overridden if the derived Handler class needs to specialize implementation of this method.
    * @param msgContext the message msgContext
    * @return true/false
    */
   public boolean handleResponse(MessageContext msgContext)
   {
      return true;
   }

   /**
    * The handleFault method processes the SOAP faults based on the SOAP message processing model.
    * The default implementation of this method returns true. This indicates that the handler chain should continue
    * processing of the SOAP fault. This method should be overridden if the derived Handler class needs to specialize
    * implementation of this method.
    * @param msgContext the message msgContext
    * @return the message msgContext
    */
   public boolean handleFault(MessageContext msgContext)
   {
      return true;
   }
}
