/*
 * JBoss, the OpenSource WebOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.soap;

import java.util.Locale;

import org.w3c.dom.Document;

/** An object that represents the contents of the SOAP body element in a SOAP
 * message. A SOAP body element consists of XML data that affects the way the
 * application-specific content is processed.
 * 
 * A SOAPBody object contains SOAPBodyElement objects, which have the content
 * for the SOAP body. A SOAPFault object, which carries status and/or error
 * information, is an example of a SOAPBodyElement object.

 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public interface SOAPBody
   extends SOAPElement
{
	public abstract SOAPBodyElement addBodyElement(Name name)
      throws SOAPException;
	public abstract SOAPBodyElement addDocument(Document doc)
      throws SOAPException;
	public abstract SOAPFault addFault() throws SOAPException;
	public abstract SOAPFault addFault(Name faultCode, String faultString)
      throws SOAPException;
	public abstract SOAPFault addFault(Name faultCode, String faultString,
      Locale locale) throws SOAPException;
	public abstract SOAPFault getFault();
	public abstract boolean hasFault();
}
