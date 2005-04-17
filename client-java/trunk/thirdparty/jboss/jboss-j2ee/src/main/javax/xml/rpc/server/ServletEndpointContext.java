/*
 * JBoss, the OpenSource WebOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.rpc.server;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.xml.rpc.handler.MessageContext;
import java.security.Principal;

/**
 * This interface provides an endpoint context maintained by the underlying
 * servlet container based JAX-RPC runtime system. For service endpoints
 * deployed on a servlet container based JAX-RPC runtime system, the context
 * parameter in the ServiceLifecycle.init method is required to be of the Java
 * type javax.xml.rpc.server.ServletEndpointContext.
 * 
 * A servlet container based JAX-RPC runtime system implements the
 * ServletEndpointContext interface. The JAX-RPC runtime system is required to
 * provide appropriate session, message context, servlet context and user
 * principal information per method invocation on the endpoint class.
 * 
 * @author Scott.Stark@jboss.org
 * @author Rahul Sharma, Roberto Chinnici (javadoc)
 * @version $Revision$
 */
public interface ServletEndpointContext
{
   /**
    * The getHttpSession method returns the current HTTP session (as a javax.servlet.http.HTTPSession).
    * When invoked by the service endpoint within a remote method implementation, the getHttpSession returns the HTTP
    * session associated currently with this method invocation. This method returns null if there is no HTTP session
    * currently active and associated with this service endpoint. An endpoint class should not rely on an active HTTP
    * session being always there; the underlying JAX-RPC runtime system is responsible for managing whether or not there
    * is an active HTTP session.
    *
    * The getHttpSession method throws JAXRPCException if invoked by an non HTTP bound endpoint.
    *
    * @return The HTTP session associated with the current invocation or null if there is no active session.
    */
   public HttpSession getHttpSession();

   /**
    * The method getMessageContext returns the MessageContext targeted for this endpoint instance.
    * This enables the service endpoint instance to acccess the MessageContext propagated by request HandlerChain
    * (and its contained Handler instances) to the target endpoint instance and to share any SOAP message processing related context.
    * The endpoint instance can access and manipulate the MessageContext and share the SOAP message processing related context with the response HandlerChain.
    *
    * @return MessageContext; If there is no associated MessageContext, this method returns null.
    */
   public MessageContext getMessageContext();

   /**
    * The method getServletContext returns the ServletContext associated with the web application that contain this endpoint.
    * According to the Servlet specification, There is one context per web application (installed as a WAR) per JVM .
    * A servlet based service endpoint is deployed as part of a web application.
    * @return ServletContext
    */
   public ServletContext getServletContext();

   /**
    * Returns a java.security.Principal instance that contains the name of the authenticated user for the current method
    * invocation on the endpoint instance. This method returns null if there is no associated principal yet.
    * The underlying JAX-RPC runtime system takes the responsibility of providing the appropriate authenticated principal
    * for a remote method invocation on the service endpoint instance.
    *
    * @return A java.security.Principal for the authenticated principal associated with the current invocation on the
    * servlet endpoint instance; Returns null if there no authenticated user associated with a method invocation.

    */
   public Principal getUserPrincipal();

   /**
    * Returns a boolean indicating whether the authenticated user for the current method invocation on the endpoint
    * instance is included in the specified logical "role".
    *
    * @param role a String specifying the name of the role
    * @return a boolean indicating whether the authenticated user associated with the current method invocation belongs
    * to a given role; false if the user has not been authenticated
    */
   public boolean isUserInRole(String role);
}
