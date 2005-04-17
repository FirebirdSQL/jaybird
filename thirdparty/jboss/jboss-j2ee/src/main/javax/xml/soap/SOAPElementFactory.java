/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.soap;

/** SOAPElementFactory is a factory for XML fragments that will eventually end
 * up in the SOAP part. These fragments can be inserted as children of the
 * SOAPHeader or SOAPBody or SOAPEnvelope.
 * 
 * Elements created using this factory do not have the properties of an element
 * that lives inside a SOAP header document. These elements are copied into the
 * XML document tree when they are inserted. 

 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public class SOAPElementFactory
{
   private SOAPFactory soapFactory;

   /**
    * 
    * @return
    * @throws SOAPException
    */ 
   public static SOAPElementFactory newInstance() throws SOAPException
   {
      SOAPFactory factory = SOAPFactory.newInstance();
      return new SOAPElementFactory(factory);
   }
   /**
    * @deprecated Use javax.xml.soap.SOAPFactory.createElement(javax.xml.soap.Name) 
    * @return
    * @throws SOAPException
    */ 
	public SOAPElement create(String localName) throws SOAPException
   {
      return soapFactory.createElement(localName);
   }
   /**
    * @deprecated Use javax.xml.soap.SOAPFactory.createElement(String localName, String prefix, String uri) instead
    * @param localName
    * @param prefix
    * @param uri
    * @return
    * @throws SOAPException
    */ 
	public SOAPElement create(String localName, String prefix, String uri) 
      throws SOAPException
   {
      return soapFactory.createElement(localName, prefix, uri); 
   }
   /**
    * @deprecated Use javax.xml.soap.SOAPFactory.createElement(javax.xml.soap.Name) 
    * @param name
    * @return
    * @throws SOAPException
    */ 
	public SOAPElement create(Name name) throws SOAPException
   {
      return soapFactory.createElement(name);
   }

   private SOAPElementFactory(SOAPFactory soapFactory)
   {
      this.soapFactory = soapFactory;
   }
}
