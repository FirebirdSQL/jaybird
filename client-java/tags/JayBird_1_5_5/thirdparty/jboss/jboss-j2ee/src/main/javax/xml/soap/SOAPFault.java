/*
 * JBoss, the OpenSource WebOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.soap;

import java.util.Locale;

/** An element in the SOAPBody object that contains error and/or status
 * information. This information may relate to errors in the SOAPMessage
 * object or to problems that are not related to the content in the message
 * itself. Problems not related to the message itself are generally errors in
 * processing, such as the inability to communicate with an upstream server. 
 * 
 * The SOAPFault interface provides methods for retrieving the information
 * contained in a SOAPFault object and for setting the fault code, the fault
 * actor, and a string describing the fault. A fault code is one of the codes
 * defined in the SOAP 1.1 specification that describe the fault. An actor is
 * an intermediate recipient to whom a message was routed. The message path may
 * include one or more actors, or, if no actors are specified, the message goes
 * only to the default actor, which is the final intended recipient.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public interface SOAPFault
   extends SOAPBodyElement
{
   public Detail addDetail() throws SOAPException;
   public Detail getDetail();
   public String getFaultActor();
   public String getFaultCode();
   public Name getFaultCodeAsName();
   public String getFaultString();
   public Locale getFaultStringLocale();
   public void setFaultActor(String faultActor) throws SOAPException;
   public void setFaultCode(String faultCode) throws SOAPException;
   public void setFaultCode(Name faultCodeQName) throws SOAPException;
   public void setFaultString(String faultString) throws SOAPException;
   public void setFaultString(String faultString, Locale locale)
      throws SOAPException;
}
