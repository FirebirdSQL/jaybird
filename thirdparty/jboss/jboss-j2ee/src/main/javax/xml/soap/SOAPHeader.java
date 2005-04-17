/*
 * JBoss, the OpenSource WebOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.soap;

import java.util.Iterator;

/** A representation of the SOAP header element. A SOAP header element consists
 * of XML data that affects the way the application-specific content is
 * processed by the message provider. For example, transaction semantics,
 * authentication information, and so on, can be specified as the content of a
 * SOAPHeader object.
 * 
 * A SOAPEnvelope object contains an empty SOAPHeader object by default. If the
 * SOAPHeader object, which is optional, is not needed, it can be retrieved and
 * deleted with the following line of code. The variable se is a SOAPEnvelope
 * object.
 
      se.getHeader().detachNode();
 
  * A SOAPHeader object is created with the SOAPEnvelope method addHeader.
 * This method, which creates a new header and adds it to the envelope, may be
 * called only after the existing header has been removed. 
      se.getHeader().detachNode();
      SOAPHeader sh = se.addHeader();
  * A SOAPHeader object can have only SOAPHeaderElement objects as its
 * immediate children. The method addHeaderElement creates a new HeaderElement
 * object and adds it to the SOAPHeader object. In the following line of code,
 * the argument to the method addHeaderElement is a Name object that is the
 * name for the new HeaderElement object. 

      SOAPHeaderElement shElement = sh.addHeaderElement(name);
 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public interface SOAPHeader
   extends SOAPElement
{
	public SOAPHeaderElement addHeaderElement(Name name)
      throws SOAPException;
	public Iterator examineAllHeaderElements();
	public Iterator examineHeaderElements(String actor);
	public Iterator examineMustUnderstandHeaderElements(String actor);
	public Iterator extractAllHeaderElements();
	public Iterator extractHeaderElements(String actor);
}
