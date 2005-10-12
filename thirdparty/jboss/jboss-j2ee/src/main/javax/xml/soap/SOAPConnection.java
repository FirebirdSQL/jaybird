/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.soap;

/** A point-to-point connection that a client can use for sending messages directly to a remote
 * party (represented by a URL, for instance).
 *
 * The SOAPConnection class is optional. Some implementations may not implement this interface in which case the call
 * to SOAPConnectionFactory.newInstance() (see below) will throw an UnsupportedOperationException.
 *
 * A client can obtain a SOAPConnection object using a SOAPConnectionFactory object as in the following example:
 *
 *    SOAPConnectionFactory factory = SOAPConnectionFactory.newInstance();
 *    SOAPConnection con = factory.createConnection();
 *
 * A SOAPConnection object can be used to send messages directly to a URL following the request/response paradigm.
 * That is, messages are sent using the method call, which sends the message and then waits until it gets a reply.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public abstract class SOAPConnection
{
   public SOAPConnection()
   {
   }

   /** Sends the given message to the specified endpoint and blocks until it has returned the response.
    *
    * @param request the SOAPMessage object to be sent
    * @param to an Object that identifies where the message should be sent.
    * It is required to support Objects of type java.lang.String, java.net.URL, and when JAXM is present javax.xml.messaging.URLEndpoint
    * @return the SOAPMessage object that is the response to the message that was sent
    * @throws SOAPException  if there is a SOAP error
    */
   public abstract SOAPMessage call(SOAPMessage request, Object to) throws SOAPException;

   /** Closes this SOAPConnection object.
    *
    * @throws SOAPException if there is a SOAP error
    */
   public abstract void close() throws SOAPException;
}
